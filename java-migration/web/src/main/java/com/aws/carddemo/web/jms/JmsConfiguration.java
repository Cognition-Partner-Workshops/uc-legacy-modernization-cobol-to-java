package com.aws.carddemo.web.jms;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

@Configuration
@EnableJms
@ConditionalOnProperty(name = "carddemo.jms.enabled", havingValue = "true")
public class JmsConfiguration {}
