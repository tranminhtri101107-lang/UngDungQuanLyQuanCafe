package Controller;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** CHƯƠNG 5 — JDBC: Thống kê doanh thu theo tháng/năm */
public class ThongKeController {

    public Map<String, Long> layDoanhThuTheoThang(int thang, int nam) throws SQLException {
        Map<String, Long> map = new LinkedHashMap<>(); // LinkedHashMap giữ thứ tự ngày
        String sql = "SELECT CAST(NgayLap AS DATE) AS Ngay, SUM(TongTien) AS TongThu " +
                "FROM HOA_DON WHERE MONTH(NgayLap)=? AND YEAR(NgayLap)=? " +
                "GROUP BY CAST(NgayLap AS DATE) ORDER BY Ngay ASC";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thang); ps.setInt(2, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getDate("Ngay").toString(), rs.getLong("TongThu"));
                }
            }
        }
        return map;
    }
}