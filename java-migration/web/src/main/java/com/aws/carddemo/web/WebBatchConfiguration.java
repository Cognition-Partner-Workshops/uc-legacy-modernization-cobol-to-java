package com.aws.carddemo.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty("spring.datasource.url")
@ComponentScan(
    basePackages = {"com.aws.carddemo.batch", "com.aws.carddemo.dataload"},
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
              com.aws.carddemo.batch.BatchApplication.class,
              com.aws.carddemo.dataload.DataloadApplication.class
            }))
class WebBatchConfiguration {}
