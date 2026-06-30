package com.shortlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建短链接的请求体
 *
 * 支持两种模式：
 *   1. 自动生成短码 — 只传 longUrl，系统用 Base62 生成
 *   2. 自定义短码   — 传 longUrl + customCode，使用你指定的后缀
 */
@Data
public class ShortenRequest {

    @NotBlank(message = "长链接不能为空")
    @Size(max = 2048, message = "链接太长")
    private String longUrl;

    // 可选：过期时间，单位小时（为空表示永不过期）
    private Integer expireHours;

    // 可选：自定义短码，字母数字组合，4-16位
    @Pattern(regexp = "^[a-zA-Z0-9]{4,16}$", message = "自定义短码只能是字母和数字，长度4-16位")
    private String customCode;
}
