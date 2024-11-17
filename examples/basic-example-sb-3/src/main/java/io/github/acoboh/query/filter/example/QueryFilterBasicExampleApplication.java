package io.github.acoboh.query.filter.example;

import io.github.acoboh.query.filter.example.filterdef.PostFilterDef;
import io.github.acoboh.query.filter.jpa.annotations.EnableQueryFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableQueryFilter(basePackageClasses = PostFilterDef.class)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class QueryFilterBasicExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryFilterBasicExampleApplication.class, args);
    }

}
