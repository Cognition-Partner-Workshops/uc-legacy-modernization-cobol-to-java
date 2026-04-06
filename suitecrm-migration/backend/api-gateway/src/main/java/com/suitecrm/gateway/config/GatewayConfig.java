package com.suitecrm.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("authCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://auth-service"))

                // Contact Service
                .route("contact-service", r -> r
                        .path("/api/v1/contacts/**", "/api/v1/leads/**", "/api/v1/prospects/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("contactCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/contact")))
                        .uri("lb://contact-service"))

                // Account Service
                .route("account-service", r -> r
                        .path("/api/v1/accounts/**", "/api/v1/employees/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("accountCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/account")))
                        .uri("lb://account-service"))

                // Opportunity Service
                .route("opportunity-service", r -> r
                        .path("/api/v1/opportunities/**", "/api/v1/quotes/**",
                                "/api/v1/invoices/**", "/api/v1/products/**",
                                "/api/v1/contracts/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("opportunityCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/opportunity")))
                        .uri("lb://opportunity-service"))

                // Case Service
                .route("case-service", r -> r
                        .path("/api/v1/cases/**", "/api/v1/bugs/**",
                                "/api/v1/knowledge/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("caseCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/case")))
                        .uri("lb://case-service"))

                // Campaign Service
                .route("campaign-service", r -> r
                        .path("/api/v1/campaigns/**", "/api/v1/email-templates/**",
                                "/api/v1/target-lists/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("campaignCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/campaign")))
                        .uri("lb://campaign-service"))

                // Activity Service
                .route("activity-service", r -> r
                        .path("/api/v1/activities/**", "/api/v1/calls/**",
                                "/api/v1/meetings/**", "/api/v1/tasks/**",
                                "/api/v1/calendar/**", "/api/v1/notes/**",
                                "/api/v1/emails/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("activityCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/activity")))
                        .uri("lb://activity-service"))

                // Report Service
                .route("report-service", r -> r
                        .path("/api/v1/reports/**", "/api/v1/dashboards/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("reportCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/report")))
                        .uri("lb://report-service"))

                // Document Service
                .route("document-service", r -> r
                        .path("/api/v1/documents/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("documentCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/document")))
                        .uri("lb://document-service"))

                // Workflow Service
                .route("workflow-service", r -> r
                        .path("/api/v1/workflows/**", "/api/v1/schedulers/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(config -> config
                                        .setName("workflowCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/workflow")))
                        .uri("lb://workflow-service"))

                .build();
    }
}
