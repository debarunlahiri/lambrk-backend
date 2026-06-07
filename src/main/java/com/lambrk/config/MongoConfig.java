package com.lambrk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.lambrk.repository.mongo")
public class MongoConfig {
  // Spring Boot auto-configures the MongoClient from application.yml.
  // This class just enables auditing (@CreatedDate / @LastModifiedDate)
  // and scopes MongoDB repositories to their own sub-package to avoid
  // conflicts with the JPA repositories in com.lambrk.repository.
}
