package com.example.tendawaks;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootTest(classes = DbCheckTest.class)
public class DbCheckTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDbConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            Logger.getLogger(DbCheckTest.class.getName()).log(Level.INFO, metaData.getDatabaseProductName());
            Logger.getLogger("URL: {}", metaData.getURL());
        }
    }
}
