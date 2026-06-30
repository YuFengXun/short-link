package com.shortlink.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 短链接统计数据
 * 用于 GET /api/shorten/{shortCode}/stats 接口的返回
 */
@Data
public class ShortLinkStatsVO {
    private String shortCode;      // 短码
    private String longUrl;        // 原始长链接
    private Long visitCount;       // 访问次数
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime expireTime; // 过期时间
    private boolean expired;       // 是否已过期
}
