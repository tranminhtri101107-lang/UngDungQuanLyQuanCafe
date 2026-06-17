import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class TestKetNoiSQL {
    public static void main(String[] args) {
        System.out.println("⏳ Đang đọc file config.xml...");

        try {
            // 1. Chỉ đường dẫn tới file config.xml của bạn (Nằm trong thư mục src)
            File xmlFile = new File("src/config.xml");

            // 2. Đọc và phân tích file XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // 3. Lấy dữ liệu từ các thẻ
            String url = doc.getElementsByTagName("url").item(0).getTextContent();
            String user = doc.getElementsByTagName("user").item(0).getTextContent();
            String pass = doc.getElementsByTagName("pass").item(0).getTextContent();

            System.out.println("   -> URL: " + url);
            System.out.println("   -> User: " + user);
            System.out.println("   -> Pass: " + pass);
            System.out.println("\n⏳ Đang thử kết nối đến SQL Server...");

            // 4. Test kết nối
            Connection conn = DriverManager.getConnection(url, user, pass);

            if (conn != null) {
                System.out.println("✅ CHÚC MỪNG! KẾT NỐI SQL SERVER THÀNH CÔNG RỰC RỠ!");
                conn.close(); // Test xong thì đóng kết nối
            }

        } catch (Exception e) {
            System.out.println("❌ KẾT NỐI THẤT BẠI! LỖI CHI TIẾT:");
            e.printStackTrace();
        }
    }
}