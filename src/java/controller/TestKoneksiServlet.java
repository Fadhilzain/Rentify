
package controller;

import config.Koneksi;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/test-koneksi")
public class TestKoneksiServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html lang='id'><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>Test Koneksi Rentify</title>");
            out.println("<style>");
            out.println("body{background:#171717;color:white;"
                    + "font-family:Arial;padding:30px}");
            out.println("h1{color:#d4ff36}");
            out.println(".card{background:#252525;padding:24px;"
                    + "border-radius:12px;max-width:600px}");
            out.println(".success{color:#d4ff36}");
            out.println(".error{color:#ff7777}");
            out.println("a{color:#d4ff36}");
            out.println("</style></head><body>");

            out.println("<div class='card'>");
            out.println("<h1>Rentify.</h1>");

            try (Connection conn = Koneksi.getConnection()) {

                if (conn.isValid(3)) {
                    out.println("<h2 class='success'>"
                            + "Koneksi database berhasil!</h2>");
                    out.println("<p>Database: rentify_db</p>");
                    out.println("<p>Status: Terhubung</p>");
                } else {
                    response.setStatus(
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.println("<p class='error'>"
                            + "Koneksi tidak valid.</p>");
                }

            } catch (SQLException e) {

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                getServletContext().log(
                        "Koneksi database gagal", e);

                out.println("<h2 class='error'>"
                        + "Koneksi database gagal.</h2>");
                out.println("<p>Pesan: "
                        + escapeHtml(e.getMessage()) + "</p>");
            }

            out.println("<p><a href='index.html'>"
                    + "Kembali ke Beranda</a></p>");
            out.println("</div></body></html>");
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
}
