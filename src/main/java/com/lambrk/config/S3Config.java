package com.lambrk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.bucket:reddit-files}")
    private String bucketName;

    @Value("${aws.s3.endpoint:}")
    private String endpoint;

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        S3Client.Builder builder = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider());

        if (!endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint))
                .serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build());
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider());

        if (!endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    @Bean
    public String s3BucketName() {
        return bucketName;
    }

    private AwsCredentialsProvider credentialsProvider() {
        if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
            return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            );
        }
        return DefaultCredentialsProvider.create();
    }
}
