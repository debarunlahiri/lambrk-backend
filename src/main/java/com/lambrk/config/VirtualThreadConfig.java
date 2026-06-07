package com.lambrk.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
public class VirtualThreadConfig implements WebMvcConfigurer {

  @Bean(name = "virtualThreadExecutor")
  public Executor virtualThreadExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    configurer.setTaskExecutor(new TaskExecutorAdapter(virtualThreadExecutor()));
    configurer.setDefaultTimeout(30000);
  }
}
