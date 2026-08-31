package io.github.acoboh.query.filter.jpa.openapi.spring;

import io.github.acoboh.query.filter.jpa.annotations.EnableQueryFilter;
import io.github.acoboh.query.filter.jpa.config.QueryFilterAutoconfigure;
import io.github.acoboh.query.filter.jpa.openapi.config.QueryFilterOpenApiAutoconfigurer;
import io.github.acoboh.query.filter.jpa.openapi.domain.PostFilterDef;
import io.github.acoboh.query.filter.jpa.openapi.repositories.PostRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Minimal Spring configuration used to test the OpenAPI documentation
 * customizer against a real JPA metamodel, backed by a Testcontainers
 * PostgreSQL instance.
 *
 * @author Adrián Cobo
 */
public class OpenApiIntegrationTestBase {

    /**
     * Basic configuration for OpenAPI customizer tests
     *
     * @author Adrián Cobo
     */
    @Configuration
    @EnableWebMvc
    @EnableJpaRepositories(basePackageClasses = PostRepository.class)
    @EnableTransactionManagement
    @Import({ QueryFilterAutoconfigure.class, QueryFilterOpenApiAutoconfigurer.class })
    @EnableQueryFilter(basePackageClasses = PostFilterDef.class)
    public static class Config {

        /**
         * PostgreSQL Container
         */
        @Container
        public static PostgreSQLContainer psqlContainer = new PostgreSQLContainer("postgres:14-alpine")
                .withDatabaseName("test_db").withUsername("user").withPassword("password");

        static {
            psqlContainer.start();
        }

        @Bean
        static PropertySourcesPlaceholderConfigurer propertySources() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        DataSource dataSource() {
            PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setURL(psqlContainer.getJdbcUrl());
            dataSource.setUser(psqlContainer.getUsername());
            dataSource.setPassword(psqlContainer.getPassword());
            return dataSource;
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
            entityManagerFactoryBean.setPersistenceUnitName(getClass().getSimpleName());
            entityManagerFactoryBean.setPersistenceProvider(new HibernatePersistenceProvider());
            entityManagerFactoryBean.setDataSource(dataSource());
            entityManagerFactoryBean.setPackagesToScan("io.github.acoboh.query.filter.jpa.openapi.model");

            JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);

            Properties properties = new Properties();
            properties.setProperty("hibernate.dialect", PostgreSQLDialect.class.getName());
            properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            entityManagerFactoryBean.setJpaProperties(properties);

            return entityManagerFactoryBean;
        }

        @Bean
        JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(entityManagerFactory);
            return transactionManager;
        }

    }

}
