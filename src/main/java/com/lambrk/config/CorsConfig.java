package com.lambrk.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

  private static final List<String> EXPOSED_HEADERS =
      List.of(
          "Authorization", "Content-Type", "X-Total-Count", "X-Correlation-Id", "Cache-Control");

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "https://*.example.com"));
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
    configuration.setAllowedHeaders(
        Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "X-Correlation-Id",
            "X-Total-Count",
            "Cache-Control",
            "Pragma"));
    configuration.setExposedHeaders(EXPOSED_HEADERS);
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    CorsFilter corsFilter = new CorsFilter(source);
    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(corsFilter);
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }
}
