/* (c) 2020 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.cloud.gateway;

import java.time.Duration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@SpringBootApplication
@Configuration
public class GatewayApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(GatewayApplication.class).run(args);
	}

	public @Bean RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes().build();
	}

	public @Bean Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
		TimeLimiterConfig timeLimiter = TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(1)).build();
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.ofDefaults();
		
		return factory -> factory.configureDefault(id -> //
		new Resilience4JConfigBuilder(id)//
				.circuitBreakerConfig(circuitBreakerConfig)//
				.timeLimiterConfig(timeLimiter)//
				.build()//
		);
	}
}
