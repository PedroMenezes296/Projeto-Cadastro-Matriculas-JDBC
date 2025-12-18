package storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

public class DbUtils {

    /**
     * Executa um script SQL simples (CREATE/INSERT/DELETE/TRUNCATE etc.),
     * assumindo que cada comando termina com ';' e que não há DELIMITER,
     * procedures ou gatilhos complexos. Comentários -- e /* *\/ são ignorados.
     */
    public static void runSqlScriptFromClasspath(String resourcePath) {
        var cl = Thread.currentThread().getContextClassLoader();

        try (var in = cl.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource não encontrado: " + resourcePath);
            }

            String sql = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

            // remove comentários comuns
            sql = sql.replaceAll("(?m)--.*?$", "");      // -- comentário
            sql = sql.replaceAll("(?s)/\\*.*?\\*/", ""); // /* comentário */

            try (var c = storage.DbBootstrap.getConnection();
                 var st = c.createStatement()) {

                c.setAutoCommit(false);

                // quebra por ; (suficiente para scripts de INSERT/CREATE simples)
                String[] comandos = sql.split(";(\\s*\\r?\\n|\\s*$)");
                for (String cmd : comandos) {
                    String s = cmd.trim();
                    if (!s.isEmpty()) st.execute(s);
                }

                c.commit();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao rodar script (classpath): " + resourcePath + " | " + e.getMessage(), e);
        }
    }
}