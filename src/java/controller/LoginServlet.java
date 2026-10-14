
package controller;

import config.Koneksi;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String PREFIX = "pbkdf2_sha256$";
    private static final int ITERATIONS = 210000;

    private boolean verifyPassword(
            String password, String storedHash
    ) throws Exception {

        if (storedHash == null) {
            return false;
        }

        if (!storedHash.startsWith(PREFIX)) {
            // Dukungan sementara untuk akun lama.
            return MessageDigest.isEqual(
                    password.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
        }

        String[] parts = storedHash.split("\\$");

        if (parts.length != 4) {
            return false;
        }

        int iterations = Integer.parseInt(parts[1]);

        if (iterations < 10000 || iterations > 1000000) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                expected.length * 8
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] actual = factory.generateSecret(spec).getEncoded();

        return MessageDigest.isEqual(expected, actual);
    }

    private String hashPassword(String password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                256
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = factory.generateSecret(spec).getEncoded();

        return PREFIX + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt)
                + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String base = request.getContextPath();

        if (email == null || password == null
                || email.trim().isEmpty() || password.isEmpty()) {
            response.sendRedirect(base + "/signin.html?error=empty");
            return;
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        String sql = "SELECT id, name, email, password_hash, role, status "
                + "FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    response.sendRedirect(base + "/signin.html?error=invalid");
                    return;
                }

                long id = rs.getLong("id");
                String name = rs.getString("name");
                String storedHash = rs.getString("password_hash");
                String role = rs.getString("role");
                String status = rs.getString("status");

                if (!"active".equalsIgnoreCase(status)) {
                    response.sendRedirect(base + "/signin.html?error=inactive");
                    return;
                }

                if (!verifyPassword(password, storedHash)) {
                    response.sendRedirect(base + "/signin.html?error=invalid");
                    return;
                }

                // Migrasikan kata sandi akun lama ke hash.
                if (!storedHash.startsWith(PREFIX)) {
                    String newHash = hashPassword(password);

                    try (PreparedStatement update = conn.prepareStatement(
                            "UPDATE users SET password_hash = ? WHERE id = ?")) {
                        update.setString(1, newHash);
                        update.setLong(2, id);
                        update.executeUpdate();
                    }
                }

                HttpSession oldSession = request.getSession(false);

                if (oldSession != null) {
                    oldSession.invalidate();
                }

                HttpSession session = request.getSession(true);
                session.setAttribute("userId", id);
                session.setAttribute("userName", name);
                session.setAttribute("userEmail", email);
                session.setAttribute("userRole", role);
                session.setMaxInactiveInterval(1800);

                response.sendRedirect(base + "/index.html");
            }

        } catch (SQLException e) {
            getServletContext().log("Kesalahan database saat login.", e);
            response.sendRedirect(base + "/signin.html?error=server");

        } catch (Exception e) {
            getServletContext().log("Kesalahan verifikasi kata sandi.", e);
            response.sendRedirect(base + "/signin.html?error=server");
        }
    }
}
