package com.rpa.whatsapp.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String WHATSAPP_JOBS_QUEUE = "whatsapp_jobs";

  @Bean
  public Queue whatsappJobsQueue() {
    return new Queue(WHATSAPP_JOBS_QUEUE, true);
  }
}
