// RateLimitBucketRepository.java
package com.example.auth.repository;

import com.example.auth.entity.RateLimitBucket;
import com.example.auth.entity.RateLimitBucketId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.OffsetDateTime;

public interface RateLimitBucketRepository extends JpaRepository<RateLimitBucket, RateLimitBucketId> {

    @Modifying
    @Query("DELETE FROM RateLimitBucket r WHERE r.expiresAt < :now")
    void deleteExpired(OffsetDateTime now);
}