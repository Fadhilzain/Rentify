
package dao;

import config.Koneksi;
import model.Kendaraan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KendaraanDAO {

    private static final String SQL =
        "SELECT p.id, p.name, p.description, " +
        "p.price_per_day, p.deposit_amount, " +
        "p.stock, p.photo_path, c.name AS category_name " +
        "FROM products p " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "WHERE p.status = 'active' " +
        "ORDER BY p.id ASC";

    public List<Kendaraan> getSemuaKendaraan()
            throws SQLException {

        List<Kendaraan> daftar = new ArrayList<>();

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Kendaraan kendaraan = new Kendaraan(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("category_name"),
                    rs.getDouble("price_per_day"),
                    rs.getDouble("deposit_amount"),
                    rs.getInt("stock"),
                    rs.getString("photo_path")
                );

                daftar.add(kendaraan);
            }
        }

        return daftar;
    }
}
