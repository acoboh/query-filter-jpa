package io.github.acoboh.query.filter.jpa.spring;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.acoboh.query.filter.jpa.annotations.EnableQueryFilter;
import io.github.acoboh.query.filter.jpa.config.QueryFilterAutoconfigure;
import io.github.acoboh.query.filter.jpa.contributor.PostgresqlArrayBuilderContributor;
import io.github.acoboh.query.filter.jpa.domain.FilterBlogDef;
import io.github.acoboh.query.filter.jpa.logging.InlineQueryLogEntryCreator;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

/**
 * Class with configuration for Spring Tests.
 * <p>
 * Special thanks to Vladimir Mihalcea.
 *
 * @author Adrián Cobo
 */
public class SpringIntegrationTest {

	@Configuration
	@EnableJpaRepositories(basePackageClasses = PostBlogRepository.class)
	@EnableTransactionManagement
	@Import(QueryFilterAutoconfigure.class)
	@EnableQueryFilter(basePackageClasses = FilterBlogDef.class)
	public static class Config {

		@Container
		public static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>("postgres:14-alpine")
				.withDatabaseName("test_db").withUsername("user").withPassword("password");

		static {
			psqlContainer.start();
		}

		@Bean
		static PropertySourcesPlaceholderConfigurer propertySources() {
			return new PropertySourcesPlaceholderConfigurer();
		}

		@Bean(destroyMethod = "close")
		HikariDataSource actualDataSource() {
			Properties properties = new Properties();
			properties.setProperty("maximumPoolSize", String.valueOf(3));

			HikariConfig hikariConfig = new HikariConfig(properties);
			hikariConfig.setAutoCommit(false);

			PGSimpleDataSource dataSource = new PGSimpleDataSource();
			dataSource.setURL(psqlContainer.getJdbcUrl());
			dataSource.setUser(psqlContainer.getUsername());
			dataSource.setPassword(psqlContainer.getPassword());

			hikariConfig.setDataSource(dataSource);
			return new HikariDataSource(hikariConfig);
		}

		@Bean
		DataSource dataSource() {
			SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
			loggingListener.setQueryLogEntryCreator(new InlineQueryLogEntryCreator());
			return ProxyDataSourceBuilder.create(actualDataSource()).name("DATA_SOURCE_PROXY").listener(loggingListener)
					.build();

		}

		@Bean
		LocalContainerEntityManagerFactoryBean entityManagerFactory() {
			LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
			entityManagerFactoryBean.setPersistenceUnitName(getClass().getSimpleName());
			entityManagerFactoryBean.setPersistenceProvider(new HibernatePersistenceProvider());
			entityManagerFactoryBean.setDataSource(dataSource());
			entityManagerFactoryBean.setPackagesToScan(packagesToScan());

			JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
			entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
			entityManagerFactoryBean.setJpaProperties(properties());
			return entityManagerFactoryBean;
		}

		@Bean
		JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
			JpaTransactionManager transactionManager = new JpaTransactionManager();
			transactionManager.setEntityManagerFactory(entityManagerFactory);
			return transactionManager;
		}

		@Bean
		TransactionTemplate transactionTemplate(EntityManagerFactory entityManagerFactory) {
			return new TransactionTemplate(transactionManager(entityManagerFactory));
		}

		@Bean
		JdbcTemplate jdbcTemplate(DataSource dataSource) {
			return new JdbcTemplate(dataSource);
		}

		protected Properties properties() {
			Properties properties = new Properties();
			properties.setProperty("hibernate.dialect", PostgreSQL10Dialect.class.getName());
			properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
			additionalProperties(properties);
			return properties;
		}

		protected void additionalProperties(Properties properties) {
			properties.setProperty("hibernate.metadata_builder_contributor",
					PostgresqlArrayBuilderContributor.class.getName());
		}

		protected String[] packagesToScan() {
			return new String[] { "io.github.acoboh.query.filter.jpa.model",
					"io.github.acoboh.query.filter.jpa.model.discriminators" };
		}

	}

}
