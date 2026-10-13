package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {

    private static final String URL =
            "jdbc:mysql://localhost:3306/rentify_db"
            + "?useSSL=false"
            + "&serverTimezone=Asia/Jakarta"
            + "&characterEncoding=UTF-8";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection()
            throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "Driver MySQL tidak ditemukan. "
                    + "Periksa WEB-INF/lib dan Libraries.",
                    e
            );
        }

        return DriverManager.getConnection(
                URL, USER, PASSWORD
        );
    }
}