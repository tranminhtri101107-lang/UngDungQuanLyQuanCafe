package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * CHƯƠNG 1 (GUI): THIẾT KẾ MODERN ADMIN DASHBOARD
 * Sử dụng CardLayout để chuyển trang mượt mà không nháy màn hình (Single-Page App)
 * Kết hợp Sidebar Navigation chuyên nghiệp chuẩn hệ thống F&B thực tế.
 */
public class MainDashboard extends JFrame {

    private JPanel pnlCenter;
    private CardLayout cardLayout;
    private int maTK;

    // Lưu trữ nút đang được chọn để tô màu Active
    private JButton btnActive = null;

    // Bảng màu chuẩn Modern UI
    private final Color C_SIDEBAR_BG = new Color(17, 24, 39);     // Xanh đen (Slate 900)
    private final Color C_SIDEBAR_HOVER = new Color(55, 65, 81);  // Xám nhạt khi hover
    private final Color C_SIDEBAR_ACTIVE = new Color(16, 185, 129); // Xanh lá cây (Brand)

    public MainDashboard(int maTK, int maVaiTro, String tenNhanVien) {
        this.maTK = maTK;        setTitle("CafeManagement - HUTEA POS v2.0");
        setSize(1280, 768); // Kích thước chuẩn HD
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. KHỞI TẠO KHU VỰC TRUNG TÂM (CARD LAYOUT)
        cardLayout = new CardLayout();
        pnlCenter = new JPanel(cardLayout);

        // Nhúng các Form của bạn vào bộ bài (CardLayout)
        pnlCenter.add(new BanHangForm(maTK, maVaiTro, tenNhanVien), "BAN_HANG");

        pnlCenter.add(new QuanLySanPhamForm(), "SAN_PHAM");
        pnlCenter.add(new QuanLyNhanVienForm(), "NHAN_VIEN");
        pnlCenter.add(new LichSuHoaDonForm(), "LICH_SU");
        pnlCenter.add(new ThongKeForm(), "THONG_KE");

        // 2. KHỞI TẠO SIDEBAR BÊN TRÁI
        JPanel pnlSidebar = new JPanel(new BorderLayout());
        pnlSidebar.setBackground(C_SIDEBAR_BG);
        pnlSidebar.setPreferredSize(new Dimension(250, 0));

        // -- 2.1 Logo Quán ở trên cùng
        JLabel lblLogo = new JLabel("HUTEA POS", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogo.setForeground(Color.WHITE);
        // Có thể chèn logo hutea.jpg của bạn vào đây:
        // lblLogo.setIcon(UIHelper.resizeIcon("/Resource/hutea.jpg", 40, 40));
        lblLogo.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        pnlSidebar.add(lblLogo, BorderLayout.NORTH);

        // -- 2.2 Các nút điều hướng ở giữa
        JPanel pnlMenu = new JPanel();
        pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
        pnlMenu.setBackground(C_SIDEBAR_BG);

        // Tạo các nút menu (Đã xóa ký tự Emoji và gắn Icon chuẩn của dự án)
        JButton btnBanHang = createMenuButton("  Bán hàng tại quầy", "BAN_HANG");
        btnBanHang.setIcon(UIHelper.resizeIcon("/Resource/hutea.jpg", 20, 20)); // Lưu ý đuôi .jpg

        JButton btnMenu    = createMenuButton("  Menu Đồ uống", "SAN_PHAM");
        btnMenu.setIcon(UIHelper.resizeIcon("/Resource/menu.png", 20, 20));

        JButton btnNhanVien= createMenuButton("  Nhân sự & Lương", "NHAN_VIEN");
        btnNhanVien.setIcon(UIHelper.resizeIcon("/Resource/staff.png", 20, 20));

        JButton btnLichSu  = createMenuButton("  Lịch sử giao dịch", "LICH_SU");
        btnLichSu.setIcon(UIHelper.resizeIcon("/Resource/bill.png", 20, 20));

        JButton btnThongKe = createMenuButton("  Báo cáo Doanh thu", "THONG_KE");
        btnThongKe.setIcon(UIHelper.resizeIcon("/Resource/chart.png", 20, 20));

        pnlMenu.add(btnBanHang);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách giữa các nút
        pnlMenu.add(btnMenu);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlMenu.add(btnNhanVien);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlMenu.add(btnLichSu);
        pnlMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlMenu.add(btnThongKe);

        // Xử lý phân quyền: Nếu là Nhân viên (2) thì ẩn nút Quản lý
        if (maVaiTro == 2) {
            btnMenu.setVisible(false);
            btnNhanVien.setVisible(false);
            btnThongKe.setVisible(false);
        }

        pnlSidebar.add(pnlMenu, BorderLayout.CENTER);

        // -- 2.3 Phần thông tin Đăng xuất ở dưới cùng
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(C_SIDEBAR_BG);
        pnlBottom.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblUser = new JLabel("👤 " + tenNhanVien);
        lblUser.setForeground(new Color(156, 163, 175)); // Xám nhạt
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setBackground(new Color(220, 38, 38)); // Đỏ
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginForm().setVisible(true);
        });

        pnlBottom.add(lblUser, BorderLayout.NORTH);
        pnlBottom.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.CENTER);
        pnlBottom.add(btnLogout, BorderLayout.SOUTH);
        pnlSidebar.add(pnlBottom, BorderLayout.SOUTH);

        // 3. RÁP SIDEBAR VÀ CENTER VÀO FRAME CHÍNH
        add(pnlSidebar, BorderLayout.WEST);
        add(pnlCenter, BorderLayout.CENTER);

        // Mặc định chọn nút Bán Hàng khi vừa mở lên
        setActiveButton(btnBanHang, "BAN_HANG");
    }

    /**
     * Hàm helper vẽ nút Sidebar chuẩn Web (Bỏ viền, căn trái, thêm hiệu ứng Hover)
     */
    private JButton createMenuButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(new Color(209, 213, 219)); // Màu chữ xám bạc
        btn.setBackground(C_SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20)); // Padding to cho dễ bấm
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Bo góc nút bằng cách xóa border mặc định của Java
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        // Xử lý sự kiện Hover chuột và Click
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != btnActive) btn.setBackground(C_SIDEBAR_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != btnActive) btn.setBackground(C_SIDEBAR_BG);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                setActiveButton(btn, cardName);
            }
        });

        // Bỏ size tối đa để nút phình to ra hết chiều ngang của Sidebar
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        return btn;
    }

    /**
     * Đổi màu nút đang chọn và gọi CardLayout chuyển trang
     */
    private void setActiveButton(JButton btn, String cardName) {
        // Reset nút cũ
        if (btnActive != null) {
            btnActive.setBackground(C_SIDEBAR_BG);
            btnActive.setForeground(new Color(209, 213, 219));
        }
        // Kích hoạt nút mới
        btnActive = btn;
        btnActive.setBackground(C_SIDEBAR_ACTIVE);
        btnActive.setForeground(Color.WHITE);

        // Lật bài (CardLayout)
        cardLayout.show(pnlCenter, cardName);
    }
}