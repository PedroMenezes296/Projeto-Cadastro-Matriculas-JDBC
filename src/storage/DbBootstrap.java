package storage;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DbBootstrap {

    private static final Properties PROPS = new Properties();

    private static void loadProps() throws IOException {
        if (PROPS.isEmpty()) {
            try (FileInputStream fis = new FileInputStream("db.properties")) {
                PROPS.load(fis);
            }
        }
    }

    public static Connection getConnection() throws Exception {
        loadProps();
        String url  = PROPS.getProperty("dburl");
        String user = PROPS.getProperty("user");
        String pass = PROPS.getProperty("password");

        // Garante o driver no Java SE
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, pass);
    }

    public static void initSchema() throws Exception {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS aluno (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  matricula INT UNIQUE NOT NULL,
                  nome VARCHAR(120) NOT NULL,
                  curso VARCHAR(80) NOT NULL,
                  email VARCHAR(120),
                  ativo BOOLEAN NOT NULL DEFAULT TRUE
                )
            """);
        }
    }
}
