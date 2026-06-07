package com.lambrk.config;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.lambrk.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {

  @Bean
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }

  @Bean
  @Profile("!test")
  public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
    return hibernateProperties -> {
      // Virtual thread-friendly configuration
      hibernateProperties.put("hibernate.connection.provider_disables_autocommit", "true");
      hibernateProperties.put("hibernate.jdbc.batch_size", "25");
      hibernateProperties.put("hibernate.order_inserts", "true");
      hibernateProperties.put("hibernate.order_updates", "true");
      hibernateProperties.put("hibernate.jdbc.batch_versioned_data", "true");
      hibernateProperties.put("hibernate.query.plan_cache_max_size", "4096");
      hibernateProperties.put("hibernate.query.in_clause_parameter_padding", "true");

      // Connection pooling for virtual threads
      hibernateProperties.put("hibernate.hikari.maximumPoolSize", "50");
      hibernateProperties.put("hibernate.hikari.minimumIdle", "10");
      hibernateProperties.put("hibernate.hikari.connectionTimeout", "30000");
      hibernateProperties.put("hibernate.hikari.idleTimeout", "600000");
      hibernateProperties.put("hibernate.hikari.maxLifetime", "1800000");

      // Performance optimizations
      hibernateProperties.put("hibernate.generate_statistics", "false");
      hibernateProperties.put("hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS", "100");

      // Disable second-level cache to reduce memory footprint
      hibernateProperties.put("hibernate.cache.use_second_level_cache", "false");
      hibernateProperties.put("hibernate.cache.use_query_cache", "false");
    };
  }

  @Bean
  @Profile("test")
  public HibernatePropertiesCustomizer testHibernatePropertiesCustomizer() {
    return hibernateProperties -> {
      // Virtual thread-friendly configuration
      hibernateProperties.put("hibernate.connection.provider_disables_autocommit", "true");
      hibernateProperties.put("hibernate.jdbc.batch_size", "25");
      hibernateProperties.put("hibernate.order_inserts", "true");
      hibernateProperties.put("hibernate.order_updates", "true");
      hibernateProperties.put("hibernate.jdbc.batch_versioned_data", "true");
      hibernateProperties.put("hibernate.query.plan_cache_max_size", "4096");
      hibernateProperties.put("hibernate.query.in_clause_parameter_padding", "true");

      // Connection pooling for virtual threads
      hibernateProperties.put("hibernate.hikari.maximumPoolSize", "10");
      hibernateProperties.put("hibernate.hikari.minimumIdle", "2");
      hibernateProperties.put("hibernate.hikari.connectionTimeout", "30000");
      hibernateProperties.put("hibernate.hikari.idleTimeout", "600000");
      hibernateProperties.put("hibernate.hikari.maxLifetime", "1800000");

      // Performance optimizations
      hibernateProperties.put("hibernate.generate_statistics", "false");
      hibernateProperties.put("hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS", "100");

      // Disable second-level cache for tests
      hibernateProperties.put("hibernate.cache.use_second_level_cache", "false");
      hibernateProperties.put("hibernate.cache.use_query_cache", "false");
    };
  }
}
