
package controller;

import config.Koneksi;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/kendaraan")
public class KendaraanServlet extends HttpServlet {

    private static final String SQL =
            "SELECT p.id, p.name, p.description, "
            + "p.price_per_day, p.deposit_amount, "
            + "p.stock, p.photo_path, c.name AS category_name "
            + "FROM products p "
            + "LEFT JOIN categories c ON p.category_id = c.id "
            + "WHERE p.status = 'active' "
            + "ORDER BY p.id ASC";

    private static final NumberFormat RUPIAH =
            NumberFormat.getNumberInstance(
                    new Locale("id", "ID"));

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html lang='id'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' "
                    + "content='width=device-width, initial-scale=1'>");
            out.println("<title>Kendaraan - Rentify</title>");

            out.println("<style>");
            out.println("*{box-sizing:border-box}");
            out.println("body{margin:0;background:#171717;color:#fff;"
                    + "font-family:Arial,sans-serif}");
            out.println("header{padding:20px 8%;background:#222;"
                    + "display:flex;justify-content:space-between;"
                    + "align-items:center;flex-wrap:wrap;gap:15px}");
            out.println(".logo{color:#d4ff36;font-size:26px;"
                    + "font-weight:bold}");
            out.println("nav a{color:white;text-decoration:none;"
                    + "margin-left:20px}");
            out.println("nav a:hover{color:#d4ff36}");
            out.println(".container{max-width:1200px;margin:auto;"
                    + "padding:40px 24px}");
            out.println("h1{color:#d4ff36;font-size:32px}");
            out.println(".muted{color:#bbb;font-size:14px;"
                    + "line-height:1.6}");
            out.println(".grid{display:grid;"
                    + "grid-template-columns:repeat(auto-fit,"
                    + "minmax(240px,1fr));gap:22px;margin-top:30px}");
            out.println(".card{background:#252525;border:1px solid #383838;"
                    + "border-radius:14px;padding:20px;overflow:hidden}");
            out.println(".vehicle-img{width:100%;height:175px;"
                    + "object-fit:cover;border-radius:9px;"
                    + "background:#333}");
            out.println(".placeholder{height:175px;background:#333;"
                    + "border-radius:9px;display:flex;"
                    + "align-items:center;justify-content:center;"
                    + "color:#aaa;margin-bottom:16px}");
            out.println(".card h2{font-size:21px}");
            out.println(".price{color:#d4ff36;font-size:20px;"
                    + "font-weight:bold}");
            out.println(".btn{display:inline-block;background:#d4ff36;"
                    + "color:#171717;padding:12px 18px;"
                    + "border-radius:8px;text-decoration:none;"
                    + "font-weight:bold;margin-top:12px}");
            out.println(".btn:hover{background:#b8e622}");
            out.println(".empty{background:#252525;padding:20px;"
                    + "border-radius:10px}");
            out.println("footer{text-align:center;background:#222;"
                    + "padding:24px;color:#aaa;margin-top:30px}");
            out.println("@media(max-width:600px){header{padding:20px}"
                    + "nav a{margin-left:10px}"
                    + ".container{padding:25px 16px}}");
            out.println("</style>");

            out.println("</head><body>");

            out.println("<header>");
            out.println("<div class='logo'>Rentify.</div>");
            out.println("<nav>");
            out.println("<a href='index.html'>Beranda</a>");
            out.println("<a href='kendaraan'>Kendaraan</a>");
            out.println("<a href='test-koneksi'>Database</a>");
            out.println("</nav></header>");

            out.println("<main class='container'>");
            out.println("<h1>Daftar Kendaraan</h1>");
            out.println("<p class='muted'>"
                    + "Pilih mobil atau motor sesuai kebutuhanmu.</p>");

            boolean hasData = false;

            out.println("<div class='grid'>");

            try (Connection conn = Koneksi.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SQL);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    hasData = true;

                    int id = rs.getInt("id");
                    String nama = escapeHtml(rs.getString("name"));
                    String deskripsi = escapeHtml(
                            rs.getString("description"));
                    String kategori = escapeHtml(
                            rs.getString("category_name"));
                    String foto = rs.getString("photo_path");

                    double harga = rs.getDouble("price_per_day");
                    double deposit = rs.getDouble("deposit_amount");
                    int stok = rs.getInt("stock");

                    out.println("<article class='card'>");

                    if (foto != null && !foto.isBlank()) {
                        String fotoUrl = request.getContextPath()
                                + "/images/"
                                + encodeFilename(foto);

                        out.println("<img class='vehicle-img' src='"
                                + escapeHtml(fotoUrl)
                                + "' alt='" + nama
                                + "' onerror=\"this.style.display='none'\">");
                    } else {
                        out.println("<div class='placeholder'>"
                                + "Foto kendaraan belum tersedia</div>");
                    }

                    out.println("<h2>" + nama + "</h2>");
                    out.println("<p class='muted'>"
                            + kategori + "</p>");
                    out.println("<p>" + deskripsi + "</p>");

                    out.println("<p class='price'>Rp "
                            + RUPIAH.format(harga)
                            + " / hari</p>");

                    out.println("<p class='muted'>Deposit: Rp "
                            + RUPIAH.format(deposit) + "</p>");

                    out.println("<p class='muted'>Stok tersedia: "
                            + stok + "</p>");

                    if (stok > 0) {
                        out.println("<a class='btn' href='"
                                + request.getContextPath()
                                + "/kendaraan?id=" + id
                                + "'>Lihat Kendaraan</a>");
                    } else {
                        out.println("<p class='muted'>"
                                + "Stok sedang habis.</p>");
                    }

                    out.println("</article>");
                }

            } catch (SQLException e) {

    getServletContext().log(
            "Gagal mengambil data kendaraan", e);

    response.setStatus(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    out.println("<div class='empty'>");
    out.println("<h2>Gagal mengambil data kendaraan</h2>");
    out.println("<p>Error: "
            + e.getErrorCode() + "</p>");
    out.println("<p>Pesan: "
            + escapeHtml(e.getMessage()) + "</p>");
    out.println("</div>");
}

            out.println("</div>");

            out.println("<p class='muted'>");
            out.println(hasData
                    ? "Katalog kendaraan Rentify."
                    : "Pastikan data kendaraan sudah tersedia.");
            out.println("</p>");

            out.println("</main>");
            out.println("<footer>&copy; 2026 Rentify. "
                    + "Rental kendaraan lebih mudah.</footer>");
            out.println("</body></html>");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String encodeFilename(String filename) {
        return filename.replace("\\", "")
                .replace("/", "")
                .replace("..", "");
    }
}
