package Controller;

import java.security.MessageDigest;
import java.util.Base64; // Đã thêm thư viện Base64

/**
 * CHƯƠNG 6 — BẢO MẬT:
 * Băm mật khẩu bằng SHA-256 — hàm một chiều, không thể giải ngược
 * Mã hóa chuỗi cấu hình (Base64) - bảo mật cấu hình hệ thống
 */
public class MaHoaBaoMat {

    private MaHoaBaoMat() {}

    /**
     * Băm chuỗi mật khẩu thành chuỗi hex 64 ký tự (SHA-256)
     */
    public static String toSHA256(String matKhau) {
        try {
            MessageDigest md   = MessageDigest.getInstance("SHA-256");
            byte[]        hash = md.digest(matKhau.getBytes("UTF-8"));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();

        } catch (Exception ex) {
            throw new RuntimeException("[MaHoaBaoMat] Lỗi SHA-256: " + ex.getMessage());
        }
    }

    /**
     * So sánh mật khẩu nhập vào với hash trong DB
     */
    public static boolean kiemTra(String matKhauNhap, String hashTrongDB) {
        return toSHA256(matKhauNhap).equalsIgnoreCase(hashTrongDB);
    }

    // ════════════════════════════════════════════════════════════
    // MÃ HÓA VÀ GIẢI MÃ CHUỖI KẾT NỐI (CONFIG)
    // ════════════════════════════════════════════════════════════

    /**
     * Hàm mã hóa chuỗi (Dùng để mã hóa mật khẩu SQL trước khi lưu vào config)
     */
    public static String maHoaConfig(String chuoiGoc) {
        return Base64.getEncoder().encodeToString(chuoiGoc.getBytes());
    }

    /**
     * Hàm giải mã (Dùng trong DBHelper để dịch ngược chuỗi mã hóa thành mật khẩu thật)
     */
    public static String giaiMaConfig(String chuoiDaMaHoa) {
        return new String(Base64.getDecoder().decode(chuoiDaMaHoa));
    }
}