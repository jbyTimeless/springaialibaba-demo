package com.boyan.saa.test;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
public class DataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDataSourceConnection() {
        // 测试1：直接获取数据库连接
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ 数据源连接成功！");
            System.out.println("连接URL：" + conn.getMetaData().getURL());
            System.out.println("驱动版本：" + conn.getMetaData().getDriverVersion());
        } catch (Exception e) {
            System.err.println("❌ 数据源连接失败：" + e.getMessage());
            e.printStackTrace();
        }

        // 测试2：执行简单SQL
        try {
            String result = jdbcTemplate.queryForObject("SELECT 1", String.class);
            System.out.println("✅ SQL执行成功，结果：" + result);
        } catch (Exception e) {
            System.err.println("❌ SQL执行失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}