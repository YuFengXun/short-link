package com.shortlink.repository;

import com.shortlink.entity.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA 数据访问层
 * 继承 JpaRepository<实体类, 主键类型>
 * 自带 save()、findById()、findAll()、deleteById() 等方法
 * 方法名按规则写（findByShortCode），JPA 自动推断 SQL，不用写实现
 */

@Repository
public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    // 根据短码查询 —— 方法名即 SQL，JPA 自动实现
    Optional<ShortLink> findByShortCode(String shortCode);

    // 判断短码是否已存在
    boolean existsByShortCode(String shortCode);
}
