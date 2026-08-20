package com.aws.carddemo.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty("spring.datasource.url")
@EntityScan("com.aws.carddemo.domain")
@EnableJpaRepositories("com.aws.carddemo.domain")
class WebPersistenceConfiguration {}
