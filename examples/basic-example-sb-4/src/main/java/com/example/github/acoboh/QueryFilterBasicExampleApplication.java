package com.example.github.acoboh;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import com.example.github.acoboh.filterdef.PostFilterDef;
import io.github.acoboh.query.filter.jpa.annotations.EnableQueryFilter;

@SpringBootApplication
@EnableQueryFilter(basePackageClasses = PostFilterDef.class)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class QueryFilterBasicExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueryFilterBasicExampleApplication.class, args);
	}

}
