package com.ctel.dbaas.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "autoScaleEntityManagerFactoryBean",
        basePackages = "com.ctel.dbaas.repository.auto_scale",
        transactionManagerRef = "autoScaleTransactionManager"
)
public class AutoScaleDataSourceConfig {

    @Bean
    public DataSource autoScaleDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl(EnvConfig.DB_AUTO_SCALE_URL);
        hikariConfig.setUsername(EnvConfig.DB_USERNAME);
        hikariConfig.setPassword(EnvConfig.DB_PASSWORD);

        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("AutoScale::springHikariCP");
        hikariConfig.setConnectionTimeout(EnvConfig.DB_CONNECTION_TIMEOUT);
        hikariConfig.setMaximumPoolSize(EnvConfig.DB_MAX_POOL_SIZE);
        hikariConfig.setMinimumIdle(EnvConfig.DB_MIN_IDLE);

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public EntityManagerFactoryBuilder autoScaleEntityManagerFactoryBuilder() {
        AbstractJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(EnvConfig.JPA_SHOW_SQL);
        vendorAdapter.setGenerateDdl(EnvConfig.JPA_GENERATE_DDL);

        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.ddl-auto", EnvConfig.HIBERNATE_DDL_AUTO);
        jpaProperties.put("hibernate.format_sql", String.valueOf(EnvConfig.HIBERNATE_FORMAT_SQL));

        return new EntityManagerFactoryBuilder(vendorAdapter, jpaProperties, null);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean autoScaleEntityManagerFactoryBean(
            @Qualifier("autoScaleDataSource") DataSource autoScaleDataSource) {

        return autoScaleEntityManagerFactoryBuilder()
                .dataSource(autoScaleDataSource)
                .packages("com.ctel.dbaas.entity.auto_scale")
                .build();
    }

    @Bean
    public PlatformTransactionManager autoScaleTransactionManager(
            @Qualifier("autoScaleEntityManagerFactoryBean") LocalContainerEntityManagerFactoryBean autoScaleEntityManagerFactoryBean) {
        return new JpaTransactionManager(autoScaleEntityManagerFactoryBean.getObject());
    }

}
