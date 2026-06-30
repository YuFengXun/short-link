package com.shortlink.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "short_link")
@Data
public class ShortLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;               // 自增主键，也是生成短码的核心种子

    @Column(unique = true, length = 50)
    private String shortCode;      // 短链接码（例如 "abc123"），访问时用这个

    @Column(nullable = false, length = 2048)
    private String longUrl;        // 原始长链接

    private Long visitCount = 0L;  // 访问次数

    private LocalDateTime expireTime;  // 过期时间（为空表示永不过期）

    private LocalDateTime createdAt = LocalDateTime.now();  // 创建时间
}
