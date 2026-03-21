package com.qiao.demo.inventory.utils;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class DBUtil {
    // 使用 Properties 对象存储配置信息
    private static final Properties PROPERTIES = new Properties();

    // 静态代码块：随类加载仅执行一次，确保驱动只注册一次，提升性能
    static {
        try (InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException("致命错误：未在 resources 目录下找到 db.properties 配置文件");
            }
            PROPERTIES.load(is);
            // 通过反射加载 MySQL 驱动
            Class.forName(PROPERTIES.getProperty("db.driver"));
        } catch (Exception e) {
            // 在严谨的工程中，底层初始化失败应直接抛出运行时异常阻断程序
            throw new RuntimeException("数据库驱动加载或配置读取失败", e);
        }
        
    }

    /**
     * 获取数据库连接
     * @return Connection 实例
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    PROPERTIES.getProperty("db.url"),
                    PROPERTIES.getProperty("db.username"),
                    PROPERTIES.getProperty("db.password")
            );
        } catch (Exception e) {
            throw new RuntimeException("获取数据库连接失败，请检查 MySQL 服务是否开启或密码是否正确", e);
        }
    }

    /**
     * 安全释放数据库资源 (遵循先开后闭原则：ResultSet -> Statement -> Connection)
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (Exception e) {
            // 资源释放阶段的异常通常记录日志即可，不应影响主业务流程
            e.printStackTrace();
        }
    }
}
