
package controller;

import config.Koneksi;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;

    private String hashPassword(String password)
            throws Exception {

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance(
                        "PBKDF2WithHmacSHA256"
                );

        byte[] hash = factory.generateSecret(spec)
                .getEncoded();

        return "pbkdf2_sha256$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt)
                + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    private void showMessage(
            HttpServletResponse response,
            String title,
            String message,
            String link,
            String linkText
    ) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='id'><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' "
                    + "content='width=device-width, initial-scale=1'>");
            out.println("<title>Rentify - " + title + "</title>");
            out.println("<style>");
            out.println("body{font-family:Arial,sans-serif;"
                    + "background:#DCEFF2;display:flex;"
                    + "justify-content:center;align-items:center;"
                    + "min-height:100vh;margin:0}");
            out.println(".card{background:white;padding:40px;"
                    + "border-radius:20px;text-align:center;"
                    + "max-width:400px;box-shadow:0 3px 8px #0002}");
            out.println("a{color:#2B70C8;text-decoration:none}");
            out.println("</style></head><body>");
            out.println("<div class='card'>");
            out.println("<h2>" + title + "</h2>");
            out.println("<p>" + message + "</p>");
            out.println("<a href='" + link + "'>"
                    + linkText + "</a>");
            out.println("</div></body></html>");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String nama = request.getParameter("nama");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (nama == null || email == null || password == null
                || nama.trim().isEmpty()
                || email.trim().isEmpty()
                || password.isEmpty()) {

            showMessage(response,
                    "Data Belum Lengkap",
                    "Nama, email, dan kata sandi wajib diisi.",
                    "signup.html", "Kembali ke pendaftaran");
            return;
        }

        nama = nama.trim();
        email = email.trim().toLowerCase(java.util.Locale.ROOT);

        if (nama.length() > 100 || email.length() > 100) {
            showMessage(response,
                    "Data Tidak Valid",
                    "Nama maksimal 100 karakter dan email maksimal "
                            + "100 karakter.",
                    "signup.html", "Kembali ke pendaftaran");
            return;
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showMessage(response,
                    "Email Tidak Valid",
                    "Silakan masukkan alamat email yang valid.",
                    "signup.html", "Kembali ke pendaftaran");
            return;
        }

        if (password.length() < 8
                || password.length() > 128) {
            showMessage(response,
                    "Kata Sandi Tidak Valid",
                    "Kata sandi harus terdiri dari 8 sampai 128 karakter.",
                    "signup.html", "Kembali ke pendaftaran");
            return;
        }

        String sql = "INSERT INTO users "
                + "(name, email, password_hash, role, status) "
                + "VALUES (?, ?, ?, 'user', 'active')";

        try {
            String passwordHash = hashPassword(password);

            try (Connection conn = Koneksi.getConnection();
                 PreparedStatement ps =
                         conn.prepareStatement(sql)) {

                ps.setString(1, nama);
                ps.setString(2, email);
                ps.setString(3, passwordHash);

                ps.executeUpdate();

                showMessage(response,
                        "Pendaftaran Berhasil!",
                        "Akun Rentify kamu berhasil dibuat. "
                                + "Silakan masuk menggunakan akun tersebut.",
                        "signin.html", "Masuk ke Rentify");
            }

        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                showMessage(response,
                        "Email Sudah Terdaftar",
                        "Email tersebut sudah digunakan. "
                                + "Silakan gunakan email lain.",
                        "signup.html", "Kembali ke pendaftaran");
            } else {
                getServletContext().log(
                        "Gagal menyimpan akun Rentify.", e);

                showMessage(response,
                        "Pendaftaran Gagal",
                        "Terjadi kesalahan saat menyimpan akun. "
                                + "Silakan coba kembali.",
                        "signup.html", "Kembali ke pendaftaran");
            }

        } catch (Exception e) {
            getServletContext().log(
                    "Gagal memproses kata sandi.", e);

            showMessage(response,
                    "Pendaftaran Gagal",
                    "Kata sandi tidak dapat diproses. "
                            + "Silakan coba kembali.",
                    "signup.html", "Kembali ke pendaftaran");
        }
    }
}
