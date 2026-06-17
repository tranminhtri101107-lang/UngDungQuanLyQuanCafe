package Controller;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * CHƯƠNG 4 — XML (DOM Parser): Đọc config.xml lấy thông tin kết nối
 * CHƯƠNG 5 — JDBC: Cung cấp Connection tập trung cho toàn bộ Controller
 */
public class DBHelper {

    private static String  url    = "";
    private static String  user   = "";
    private static String  pass   = "";
    private static boolean loaded = false;

    private DBHelper() {} // Không cho tạo instance

    /**
     * CHƯƠNG 4 — Đọc file config.xml bằng DOM Parser
     * Gọi 1 lần duy nhất trong App.main()
     */
    public static void loadConfig() {
        try {
            File file = new File("config.xml");
            if (!file.exists()) {
                // Thử tìm trong src/
                file = new File("src/config.xml");
                if (!file.exists()) {
                    System.err.println("[DBHelper] Không tìm thấy config.xml!");
                    return;
                }
            }

            // DOM Parser (Chương 4 — 4.1)
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder        builder = factory.newDocumentBuilder();
            Document               doc     = builder.parse(file);
            doc.getDocumentElement().normalize();

            // Trích xuất từ thẻ XML
            url  = doc.getElementsByTagName("url") .item(0).getTextContent().trim();
            user = doc.getElementsByTagName("user").item(0).getTextContent().trim();
            pass = doc.getElementsByTagName("pass").item(0).getTextContent().trim();

            loaded = true;
            System.out.println("[DBHelper] Đọc config.xml thành công!");

        } catch (Exception e) {
            System.err.println("[DBHelper] Lỗi đọc config.xml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * CHƯƠNG 5 — JDBC: Trả về Connection tới SQL Server
     * Dùng try-with-resources ở nơi gọi để tự động đóng
     */
    public static Connection getConnection() throws SQLException {
        if (!loaded) {
            throw new SQLException("Chưa load config.xml! Gọi DBHelper.loadConfig() trước.");
        }
        return DriverManager.getConnection(url, user, pass);
    }

    public static boolean isLoaded() { return loaded; }
    public static String  getUrl()   { return url; }
}