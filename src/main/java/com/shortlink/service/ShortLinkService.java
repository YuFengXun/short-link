package com.shortlink.service;

import com.shortlink.dto.ShortLinkStatsVO;
import com.shortlink.entity.ShortLink;
import com.shortlink.repository.ShortLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 短链接业务逻辑
 *
 * 核心流程：
 *   创建短链 → 生成 shortCode（Base62/自定义）→ 存数据库 → 写到 Redis 缓存
 *   访问短链 → 查 Redis → 没命中 → 查数据库 → 回写 Redis → 302 重定向
 */
@Service
@RequiredArgsConstructor
public class ShortLinkService {

    private final ShortLinkRepository repository;
    private final StringRedisTemplate redisTemplate;

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final long BASE = 62L;

    /**
     * 创建短链接
     * 如果传了 customCode，直接用它，不生成 Base62
     * 如果没传，用数据库自增 id 生成 Base62 短码
     */
    public ShortLink createShortLink(String longUrl, Integer expireHours, String customCode) {
        ShortLink shortLink = new ShortLink();
        shortLink.setLongUrl(longUrl);
        if (expireHours != null && expireHours > 0) {
            shortLink.setExpireTime(LocalDateTime.now().plusHours(expireHours));
        }

        if (customCode != null && !customCode.isEmpty()) {
            // 自定义短码模式：检查是否已存在
            if (repository.existsByShortCode(customCode)) {
                throw new RuntimeException("自定义短码已被使用");
            }
            shortLink.setShortCode(customCode);
            repository.save(shortLink);
        } else {
            // 自动生成模式：先 save 拿到 id，再生成 Base62 回填
            repository.save(shortLink);
            String shortCode = encodeBase62(shortLink.getId());
            shortLink.setShortCode(shortCode);
            repository.save(shortLink);
        }

        // 写入 Redis 缓存
        String redisKey = "short:" + shortLink.getShortCode();
        redisTemplate.opsForValue().set(redisKey, longUrl);
        if (expireHours != null && expireHours > 0) {
            redisTemplate.expire(redisKey, expireHours, TimeUnit.HOURS);
        }

        return shortLink;
    }

    /**
     * 根据短码获取长链接
     */
    public String getLongUrl(String shortCode) {
        String redisKey = "short:" + shortCode;

        String cachedUrl = redisTemplate.opsForValue().get(redisKey);
        if (cachedUrl != null) {
            repository.findByShortCode(shortCode).ifPresent(link -> {
                link.setVisitCount(link.getVisitCount() + 1);
                repository.save(link);
            });
            return cachedUrl;
        }

        Optional<ShortLink> optional = repository.findByShortCode(shortCode);
        if (optional.isEmpty()) {
            return null;
        }

        ShortLink shortLink = optional.get();

        if (shortLink.getExpireTime() != null && shortLink.getExpireTime().isBefore(LocalDateTime.now())) {
            return null;
        }

        redisTemplate.opsForValue().set(redisKey, shortLink.getLongUrl());
        if (shortLink.getExpireTime() != null) {
            long expireSeconds = java.time.Duration.between(LocalDateTime.now(), shortLink.getExpireTime()).getSeconds();
            if (expireSeconds > 0) {
                redisTemplate.expire(redisKey, expireSeconds, TimeUnit.SECONDS);
            }
        }

        shortLink.setVisitCount(shortLink.getVisitCount() + 1);
        repository.save(shortLink);

        return shortLink.getLongUrl();
    }

    /**
     * 获取短链接统计数据
     */
    public ShortLinkStatsVO getStats(String shortCode) {
        ShortLink shortLink = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("短链接不存在"));

        ShortLinkStatsVO stats = new ShortLinkStatsVO();
        stats.setShortCode(shortLink.getShortCode());
        stats.setLongUrl(shortLink.getLongUrl());
        stats.setVisitCount(shortLink.getVisitCount());
        stats.setCreatedAt(shortLink.getCreatedAt());
        stats.setExpireTime(shortLink.getExpireTime());
        stats.setExpired(shortLink.getExpireTime() != null
                && shortLink.getExpireTime().isBefore(LocalDateTime.now()));
        return stats;
    }

    /**
     * Base62 编码：将数字转为 62 进制字符串
     */
    private String encodeBase62(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(BASE62.charAt((int) (num % BASE)));
            num /= BASE;
        }
        return sb.reverse().toString();
    }
}
