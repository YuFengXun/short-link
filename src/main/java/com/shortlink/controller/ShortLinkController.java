package com.shortlink.controller;

import com.shortlink.common.Result;
import com.shortlink.dto.ShortLinkStatsVO;
import com.shortlink.dto.ShortenRequest;
import com.shortlink.entity.ShortLink;
import com.shortlink.service.ShortLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 短链接控制器
 * 接口列表：
 *   1. POST /api/shorten          — 生成短链接
 *   2. GET  /{shortCode}          — 访问短链接（302 重定向）
 *   3. GET  /api/shorten/{shortCode}/stats  — 查看统计
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     * POST /api/shorten
     * 请求体：{ "longUrl": "https://...", "expireHours": 24, "customCode": "mycode" }
     * customCode 是可选的，不传则自动生成
     */
    @PostMapping("/api/shorten")
    public Result<ShortLink> shorten(@Valid @RequestBody ShortenRequest request) {
        ShortLink shortLink = shortLinkService.createShortLink(
                request.getLongUrl(), request.getExpireHours(), request.getCustomCode());
        return Result.success(shortLink);
    }

    /**
     * 访问短链接，302 重定向到原始长链接
     * GET /abc123
     */
    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode,
                         jakarta.servlet.http.HttpServletResponse response) throws Exception {
        String longUrl = shortLinkService.getLongUrl(shortCode);
        if (longUrl == null) {
            response.setStatus(404);
            response.getWriter().write("short link not found or expired");
            return;
        }
        response.sendRedirect(longUrl);
    }

    /**
     * 获取短链接访问统计
     * GET /api/shorten/{shortCode}/stats
     */
    @GetMapping("/api/shorten/{shortCode}/stats")
    public Result<ShortLinkStatsVO> stats(@PathVariable String shortCode) {
        return Result.success(shortLinkService.getStats(shortCode));
    }
}
