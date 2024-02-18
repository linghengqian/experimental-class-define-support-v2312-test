package com.lingh;

import com.lingh.commons.AddressRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.core.exception.TransactionException;
import io.seata.rm.RMClient;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.tm.TMClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.Stream;

@SuppressWarnings({"deprecation", "resource"})
public class SeataTest {

    private final String jdbcUrl = "jdbc:tc:postgresql:16.2-bookworm://test-native/demo_ds_0?TC_DAEMON=true";

    private AddressRepository addressRepository;

    @Test
    void assertShardingInSeataTransactions() throws SQLException, TransactionException {
        try (GenericContainer<?> container = new FixedHostPortGenericContainer<>("seataio/seata-server:2.0.0")
                .withFixedExposedPort(26403, 8091)
                .withExposedPorts(7091)) {
            container.start();
            DataSource dataSource = createDataSource(container.getMappedPort(7091));
            addressRepository = new AddressRepository(dataSource);
            this.initEnvironment();
            addressRepository.assertRollbackWithTransactions();
            addressRepository.dropTable();
        }
    }

    private void initEnvironment() throws SQLException {
        addressRepository.createTableIfNotExists();
        addressRepository.truncateTable();
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, new Properties());
    }

    private DataSource createDataSource(final Integer seataServerHealthCheckPort) {
        Awaitility.await().atMost(Duration.ofMinutes(1)).ignoreExceptions()
                .until(() -> {
                    boolean flag = false;
                    HttpGet httpGet = new HttpGet("http://localhost:" + seataServerHealthCheckPort + "/health");
                    try (CloseableHttpClient httpclient = HttpClients.createDefault();
                         CloseableHttpResponse response = httpclient.execute(httpGet)) {
                        if (HttpStatus.SC_UNAUTHORIZED == response.getCode()) {
                            flag = true;
                        }
                        HttpEntity entity = response.getEntity();
                        EntityUtils.consume(entity);
                    }
                    return flag;
                });
        TMClient.init("test-native", "default_tx_group");
        RMClient.init("test-native", "default_tx_group");
        final String firstSql = """
                CREATE TABLE IF NOT EXISTS public.undo_log
                (
                    id            SERIAL       NOT NULL,
                    branch_id     BIGINT       NOT NULL,
                    xid           VARCHAR(128) NOT NULL,
                    context       VARCHAR(128) NOT NULL,
                    rollback_info BYTEA        NOT NULL,
                    log_status    INT          NOT NULL,
                    log_created   TIMESTAMP(0) NOT NULL,
                    log_modified  TIMESTAMP(0) NOT NULL,
                    CONSTRAINT pk_undo_log PRIMARY KEY (id),
                    CONSTRAINT ux_undo_log UNIQUE (xid, branch_id)
                );""";
        final String secondSql = "CREATE INDEX ix_log_created ON undo_log(log_created);";
        final String thirdSql = "COMMENT ON TABLE public.undo_log IS 'AT transaction mode undo table';";
        final String fourthSql = "COMMENT ON COLUMN public.undo_log.branch_id IS 'branch transaction id';";
        final String fifthSql = "COMMENT ON COLUMN public.undo_log.xid IS 'global transaction id';";
        final String sixthSql = "COMMENT ON COLUMN public.undo_log.context IS 'undo_log context,such as serialization';";
        final String seventhSql = "COMMENT ON COLUMN public.undo_log.rollback_info IS 'rollback info';";
        final String eighthSql = "COMMENT ON COLUMN public.undo_log.log_status IS '0:normal status,1:defense status';";
        final String ninthSql = "COMMENT ON COLUMN public.undo_log.log_created IS 'create datetime';";
        final String tenthSql = "COMMENT ON COLUMN public.undo_log.log_modified IS 'modify datetime';";
        final String eleventhSql = "CREATE SEQUENCE IF NOT EXISTS undo_log_id_seq INCREMENT BY 1 MINVALUE 1 ;";
        Stream.of(firstSql, secondSql, thirdSql, fourthSql, fifthSql, sixthSql, seventhSql, eighthSql, ninthSql, tenthSql, eleventhSql)
                .forEachOrdered(this::executeSqlToDB);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.testcontainers.jdbc.ContainerDatabaseDriver");
        config.setJdbcUrl(jdbcUrl);
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        return new DataSourceProxy(hikariDataSource);
    }

    private void executeSqlToDB(final String sqlString) {
        try (Connection ds0Connection = openConnection()) {
            ds0Connection.createStatement().executeUpdate(sqlString);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
