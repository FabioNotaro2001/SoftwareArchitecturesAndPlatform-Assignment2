package sap.ass2.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;


@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
			.route("REGISTRY_ROUTE", r -> r.path("/api/registry/**")
				.filters(f -> f.circuitBreaker(c -> c.setName("registryCircuitBreaker")
													.setFallbackUri("forward:/fallback/registry")))
				.uri("http://registry:9000"))  // Nome del container per Registry
			.route("USERS_MANAGER_ROUTE", r -> r.path("/api/users/**")
				.filters(f -> f.circuitBreaker(c -> c.setName("usersCircuitBreaker")
													.setFallbackUri("forward:/fallback/users")))
				.uri("http://users:9100"))    // Nome del container per Users
			.route("EBIKES_MANAGER_ROUTE", r -> r.path("/api/ebikes/**")
				.filters(f -> f.circuitBreaker(c -> c.setName("ebikesCircuitBreaker")
													.setFallbackUri("forward:/fallback/ebikes")))
				.uri("http://ebikes:9200"))   // Nome del container per Ebikes
			.route("RIDES_MANAGER_ROUTE", r -> r.path("/api/rides/**")
				.filters(f -> f.circuitBreaker(c -> c.setName("ridesCircuitBreaker")
													.setFallbackUri("forward:/fallback/rides")))
				.uri("http://rides:9300"))    // Nome del container per Rides
			.build();
	}
}