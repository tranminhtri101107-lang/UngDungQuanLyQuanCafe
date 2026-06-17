package View;

import Controller.DBHelper;
import Controller.ExportController;
import Controller.MaHoaBaoMat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Màn hình đăng nhập POS
 * CHƯƠNG 5 — JDBC: Truy vấn tài khoản
 * CHƯƠNG 6 — Bảo mật: Băm SHA-256 trước khi so sánh
 */
public class LoginForm extends JFrame {

    private JTextField     txtTaiKhoan;
    private JPasswordField txtMatKhau;
    private JLabel         lblLoi;

    public LoginForm() {
        setTitle("Hutea POS — Đăng nhập");
        setSize(460, 510);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Nền tối toàn màn hình
        JPanel pnlBg = new JPanel(new GridBagLayout());
        pnlBg.setBackground(new Color(18, 25, 35));
        add(pnlBg, BorderLayout.CENTER);

        // Card trắng trung tâm
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(340, 390));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220), 1),
                BorderFactory.createEmptyBorder(30, 32, 28, 32)));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;

        // Icon Logo Hutea (Đã sửa lại thành file hutea.jpg)
        g.gridy = 0; g.insets = new Insets(0, 0, 10, 0);
        JLabel ico = new JLabel(
                UIHelper.resizeIcon("/Resource/hutea.jpg", 90, 90),
                SwingConstants.CENTER
        );
        card.add(ico, g);

        // Tên app
        g.gridy = 1; g.insets = new Insets(0, 0, 4, 0);
        JLabel lblApp = new JLabel("HUTEA POS", SwingConstants.CENTER);
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblApp.setForeground(new Color(28, 37, 50));
        card.add(lblApp, g);

        // Phụ đề
        g.gridy = 2; g.insets = new Insets(0, 0, 26, 0);
        JLabel lblSub = new JLabel("Hệ thống quản lý quán cafe", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(150,150,150));
        card.add(lblSub, g);

        // Label Tài khoản
        g.gridy = 3; g.insets = new Insets(0, 0, 5, 0);
        JLabel lTK = new JLabel("Tài khoản");
        lTK.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lTK.setForeground(new Color(70,70,70));
        card.add(lTK, g);

        // TextField Tài khoản
        g.gridy = 4; g.insets = new Insets(0, 0, 14, 0);
        txtTaiKhoan = new JTextField();
        txtTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTaiKhoan.setPreferredSize(new Dimension(0, 40));
        txtTaiKhoan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200), 1),
                BorderFactory.createEmptyBorder(5, 11, 5, 11)));
        card.add(txtTaiKhoan, g);

        // Label Mật khẩu
        g.gridy = 5; g.insets = new Insets(0, 0, 5, 0);
        JLabel lMK = new JLabel("Mật khẩu");
        lMK.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lMK.setForeground(new Color(70,70,70));
        card.add(lMK, g);

        // PasswordField
        g.gridy = 6; g.insets = new Insets(0, 0, 8, 0);
        txtMatKhau = new JPasswordField();
        txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMatKhau.setPreferredSize(new Dimension(0, 40));
        txtMatKhau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200), 1),
                BorderFactory.createEmptyBorder(5, 11, 5, 11)));
        card.add(txtMatKhau, g);

        // Label lỗi (ẩn mặc định)
        g.gridy = 7; g.insets = new Insets(0, 0, 12, 0);
        lblLoi = new JLabel(" ", SwingConstants.CENTER);
        lblLoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLoi.setForeground(new Color(231, 76, 60));
        card.add(lblLoi, g);

        // Nút đăng nhập
        g.gridy = 8; g.insets = new Insets(0, 0, 0, 0);
        JButton btnDN = new JButton("ĐĂNG NHẬP");
        btnDN.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDN.setBackground(new Color(28, 37, 50));
        btnDN.setForeground(Color.WHITE);
        btnDN.setFocusPainted(false); btnDN.setBorderPainted(false); btnDN.setOpaque(true);
        btnDN.setPreferredSize(new Dimension(0, 42));
        btnDN.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDN.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnDN.setBackground(UIHelper.C_PRIMARY); }
            public void mouseExited (MouseEvent e) { btnDN.setBackground(new Color(28,37,50)); }
        });
        card.add(btnDN, g);

        pnlBg.add(card, new GridBagConstraints());

        // Footer
        JLabel lblVer = new JLabel("Hutea POS v1.0  •  © 2026", SwingConstants.CENTER);
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVer.setForeground(new Color(80,90,100));
        lblVer.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(lblVer, BorderLayout.SOUTH);
        getContentPane().setBackground(new Color(18, 25, 35));

        // Sự kiện
        btnDN.addActionListener(e -> dangNhap());
        txtMatKhau.addActionListener(e -> dangNhap());
        txtTaiKhoan.addActionListener(e -> txtMatKhau.requestFocus());
    }

    /**
     * CHƯƠNG 5 + CHƯƠNG 6
     */
    private void dangNhap() {
        String tk = txtTaiKhoan.getText().trim();
        String mk = new String(txtMatKhau.getPassword());

        if (tk.isEmpty() || mk.isEmpty()) {
            setLoi("Vui lòng nhập đầy đủ thông tin!"); return;
        }

        // Chương 6 — Băm SHA-256
        String mkHash = MaHoaBaoMat.toSHA256(mk);

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT MaTK, HoTen, MaVaiTro FROM TAI_KHOAN WHERE TenDangNhap=? AND MatKhau=?")) {

            ps.setString(1, tk); ps.setString(2, mkHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int    maTK     = rs.getInt("MaTK");
                    String hoTen    = rs.getString("HoTen");
                    int    maVaiTro = rs.getInt("MaVaiTro");

                    ExportController.ghiLog("Đăng nhập: " + hoTen);

                    // Đã sửa: Gọi thẳng vào MainDashboard thay vì BanHangForm truyền thống
                    new MainDashboard(maTK, maVaiTro, hoTen).setVisible(true);

                    dispose(); // Đóng form đăng nhập
                } else {
                    setLoi("Sai tài khoản hoặc mật khẩu!");
                    txtMatKhau.setText("");
                    txtMatKhau.requestFocus();
                }
            }
        } catch (SQLException ex) {
            setLoi("Lỗi kết nối CSDL! Kiểm tra config.xml");
            ex.printStackTrace();
        }
    }

    private void setLoi(String msg) {
        lblLoi.setText("⚠  " + msg);
        txtMatKhau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.C_DANGER, 1),
                BorderFactory.createEmptyBorder(5, 11, 5, 11)));
    }
}