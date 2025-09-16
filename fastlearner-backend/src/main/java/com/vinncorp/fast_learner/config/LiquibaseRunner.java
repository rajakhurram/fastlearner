package com.vinncorp.fast_learner.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class LiquibaseRunner implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Override
    public void run(String... args) throws Exception {
        if ("test".equals(activeProfile)) {
            return; // Skip Liquibase execution in test profile
        }
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));

        Liquibase liquibase = new Liquibase("changelog/changelog-master.xml",
                new ClassLoaderResourceAccessor(),
                database);

        liquibase.update(new Contexts(), new LabelExpression());
    }
}