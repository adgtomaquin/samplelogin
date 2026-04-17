package com.example.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RateLimitBucketId implements Serializable {

    @Column(name = "bucket_key", length = 256)
    private String bucketKey;

    @Column(name = "endpoint", length = 50)
    private String endpoint;
}