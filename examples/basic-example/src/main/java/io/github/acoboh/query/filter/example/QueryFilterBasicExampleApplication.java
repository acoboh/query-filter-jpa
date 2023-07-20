package io.github.acoboh.query.filter.example;

import org.query.filter.jpa.openapi.data.jpa.annotations.EnableQueryFilterOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.acoboh.query.filter.example.filterdef.PostFilterDef;
import io.github.acoboh.query.filter.jpa.annotations.EnableQueryFilter;

@SpringBootApplication
@EnableQueryFilter(basePackageClasses = PostFilterDef.class)
@EnableQueryFilterOpenApi
public class QueryFilterBasicExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueryFilterBasicExampleApplication.class, args);
	}

}
