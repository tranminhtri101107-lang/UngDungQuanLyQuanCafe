import Controller.DBHelper;
import Controller.ExportController;
import Network.NotificationServer;
import View.LoginForm;

import javax.swing.*;
import java.awt.*;

/**
 * Entry Point — Khởi động ứng dụng Hutea POS
 *
 * Thứ tự khởi động:
 * 1. CHƯƠNG 4 — XML: Đọc config.xml lấy thông tin kết nối DB
 * 2. CHƯƠNG 7 — Networking: Khởi động TCP Server chạy nền
 * 3. CHƯƠNG 2 — Threading: Mở LoginForm trên Swing EDT (Event Dispatch Thread)
 */
public class App {

    public static void main(String[] args) {

        // ── BƯỚC 1: Đọc config.xml (CHƯƠNG 4 — DOM Parser) ──
        DBHelper.loadConfig();

        // Khởi động TCP Server
        // Server chạy nền, tự tắt khi app đóng (daemon thread)
        ExportController.ghiLog("=== Ứng dụng khởi động — TCP Server cổng "
                + NotificationServer.PORT + " ===");

        applyTheme();

        //  Mở LoginForm trên Swing EDT
        // invokeLater đảm bảo UI chạy đúng trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginForm login = new LoginForm();
            login.setVisible(true);
        });
    }

    /**
     * Áp dụng FlatLaf IntelliJ theme — giao diện phẳng hiện đại
     * Fallback về Nimbus nếu không có thư viện FlatLaf
     */
    private static void applyTheme() {
        try {
            // FlatLaf — cần thêm dependency: com.formdev:flatlaf:3.4
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatIntelliJLaf());

            // Tinh chỉnh thêm cho toàn bộ app
            UIManager.put("defaultFont",     new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Button.arc",      10);
            UIManager.put("Component.arc",   8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.rowHeight", 30);
            UIManager.put("TableHeader.height", 38);

        } catch (Exception e) {
            System.out.println("[App] FlatLaf không có — dùng Nimbus. " + e.getMessage());
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ex) {
                // Dùng theme mặc định của hệ thống
            }
        }
    }
}