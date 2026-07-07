package com.ctel.dbaas.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
        entityManagerFactoryRef = "dbaasEntityManagerFactoryBean",
        basePackages = "com.ctel.dbaas.repository.dbaas"
)
public class DBaaSDataSourceConfig {

    @Bean
    public DataSource dbaasDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl(EnvConfig.DB_DBAAS_URL);
        hikariConfig.setUsername(EnvConfig.DB_USERNAME);
        hikariConfig.setPassword(EnvConfig.DB_PASSWORD);

        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("DBaaS::springHikariCP");
        hikariConfig.setConnectionTimeout(EnvConfig.DB_CONNECTION_TIMEOUT);
        hikariConfig.setMaximumPoolSize(EnvConfig.DB_MAX_POOL_SIZE);
        hikariConfig.setMinimumIdle(EnvConfig.DB_MIN_IDLE);

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public EntityManagerFactoryBuilder dbaasEntityManagerFactoryBuilder() {
        AbstractJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(EnvConfig.JPA_SHOW_SQL);
        vendorAdapter.setGenerateDdl(EnvConfig.JPA_GENERATE_DDL);

        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.ddl-auto", EnvConfig.HIBERNATE_DDL_AUTO);
        jpaProperties.put("hibernate.format_sql", String.valueOf(EnvConfig.HIBERNATE_FORMAT_SQL));

        return new EntityManagerFactoryBuilder(vendorAdapter, jpaProperties, null);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean dbaasEntityManagerFactoryBean(
            @Qualifier("dbaasDataSource") DataSource dbaasDataSource) {

        return dbaasEntityManagerFactoryBuilder()
                .dataSource(dbaasDataSource)
                .packages("com.ctel.dbaas.entity.dbaas")
                .build();
    }

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("dbaasEntityManagerFactoryBean") LocalContainerEntityManagerFactoryBean dbaasEntityManagerFactoryBean) {
        return new JpaTransactionManager(dbaasEntityManagerFactoryBean.getObject());
    }

}
