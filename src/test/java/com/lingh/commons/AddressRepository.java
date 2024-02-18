package com.lingh.commons;

import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public final class AddressRepository {

    private final DataSource dataSource;

    public AddressRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS t_address (address_id BIGINT NOT NULL, address_name VARCHAR(100) NOT NULL, PRIMARY KEY (address_id))";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public void dropTable() throws SQLException {
        String sql = "DROP TABLE IF EXISTS t_address";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE t_address";
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    @SneakyThrows
    public void assertRollbackWithTransactions() {
        GlobalTransaction globalTransaction = GlobalTransactionContext.getCurrentOrCreate();
        globalTransaction.begin(60 * 1000);
        Connection connection = dataSource.getConnection();
        try {
            connection.createStatement().executeUpdate("INSERT INTO t_address (address_id, address_name) VALUES (2024, 'address_test_2024')");
            connection.createStatement().executeUpdate("INSERT INTO t_table_does_not_exist (test_id_does_not_exist) VALUES (2024)");
            globalTransaction.commit();
        } catch (SQLException e) {
            globalTransaction.rollback();
        }
        try (Connection conn = dataSource.getConnection();
             ResultSet resultSet = conn.createStatement().executeQuery("SELECT * FROM t_address WHERE address_id = 2024")) {
            assertThat(resultSet.next(), is(false));
        }
    }
}
