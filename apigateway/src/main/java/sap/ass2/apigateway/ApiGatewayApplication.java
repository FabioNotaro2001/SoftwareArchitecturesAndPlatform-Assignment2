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
				.uri("http://localhost:9000"))
			.route("USERS_MANAGER_ROUTE", r -> r.path("/api/users/**")
				.uri("http://localhost:9100"))
			.route("EBIKES_MANAGER_ROUTE", r -> r.path("/api/ebikes/**")
				.uri("http://localhost:9200"))
			.route("RIDES_MANAGER_ROUTE", r -> r.path("/api/rides/**")
				.uri("http://localhost:9300"))
			.build();
	}
}