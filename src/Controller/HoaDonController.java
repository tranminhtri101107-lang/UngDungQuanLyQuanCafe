package Controller;

import Model.ChiTietHoaDon;
import Model.HoaDon;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CHƯƠNG 5 — JDBC NÂNG CAO:
 * Transaction (AutoCommit=false) đảm bảo toàn vẹn dữ liệu
 * khi lưu cả HOA_DON lẫn CHI_TIET_HOA_DON cùng lúc
 */
public class HoaDonController {

    /**
     * Thanh toán — lưu hóa đơn + chi tiết trong 1 TRANSACTION
     * Nếu có lỗi ở bước nào → rollback toàn bộ
     */
    public int thanhToan(int maTK, String soBan,
                         List<ChiTietHoaDon> danhSach, String pt) throws SQLException {
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Tính tổng tiền
            long tong = danhSach.stream().mapToLong(ChiTietHoaDon::getThanhTien).sum();

            // Bước 1: INSERT HOA_DON và lấy MaHD vừa tạo
            int maHD;
            String sqlHD = "INSERT INTO HOA_DON (MaTK,SoBan,TongTien,PhuongThucThanhToan) VALUES (?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlHD, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, maTK); ps.setString(2, soBan);
                ps.setLong(3, tong); ps.setString(4, pt);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Không lấy được MaHD!");
                    maHD = keys.getInt(1);
                }
            }

            // Bước 2: INSERT tất cả CHI_TIET_HOA_DON dùng Batch
            String sqlCT = "INSERT INTO CHI_TIET_HOA_DON (MaHD,MaSP,TenSP,SoLuong,GiaBan) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCT)) {
                for (ChiTietHoaDon ct : danhSach) {
                    ps.setInt(1, maHD); ps.setInt(2, ct.getMaSP());
                    ps.setString(3, ct.getTenSP()); ps.setInt(4, ct.getSoLuong());
                    ps.setInt(5, ct.getGiaBan());
                    ps.addBatch(); // Gom lại gửi 1 lần — tối ưu hơn
                }
                ps.executeBatch();
            }

            conn.commit(); // Commit nếu không lỗi
            System.out.println("[HoaDonController] Thanh toán #" + maHD + " thành công!");
            return maHD;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); }
            catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    /** Lấy toàn bộ lịch sử hóa đơn */
    public List<HoaDon> layDanhSach() throws SQLException {
        List<HoaDon> ds = new ArrayList<>();
        String sql = "SELECT hd.MaHD,hd.NgayLap,hd.MaTK,tk.HoTen,hd.SoBan,hd.TongTien,hd.PhuongThucThanhToan " +
                "FROM HOA_DON hd JOIN TAI_KHOAN tk ON hd.MaTK=tk.MaTK ORDER BY hd.MaHD DESC";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(mapRow(rs));
        }
        return ds;
    }

    /** Lấy hóa đơn theo ngày — dùng cho ThongKeForm drill-down */
    public List<HoaDon> layTheoNgay(String ngaySQL) throws SQLException {
        List<HoaDon> ds = new ArrayList<>();
        String sql = "SELECT hd.MaHD,hd.NgayLap,hd.MaTK,tk.HoTen,hd.SoBan,hd.TongTien,hd.PhuongThucThanhToan " +
                "FROM HOA_DON hd JOIN TAI_KHOAN tk ON hd.MaTK=tk.MaTK " +
                "WHERE CAST(hd.NgayLap AS DATE)=? ORDER BY hd.MaHD DESC";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ngaySQL);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapRow(rs));
            }
        }
        return ds;
    }

    private HoaDon mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("NgayLap");
        return new HoaDon(
                rs.getInt("MaHD"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now(),
                rs.getInt("MaTK"), rs.getString("HoTen"),
                rs.getString("SoBan"), rs.getLong("TongTien"),
                rs.getString("PhuongThucThanhToan"));
    }

    /** * Lọc hóa đơn theo Ngày, Tháng, Năm
     * Nếu tham số truyền vào là 0 nghĩa là "Tất cả" (Bỏ qua điều kiện đó)
     */
    public List<HoaDon> layTheoNgayThangNam(int ngay, int thang, int nam) throws SQLException {
        List<HoaDon> ds = new ArrayList<>();

        // Sử dụng thủ thuật 1=1 để ghép chuỗi điều kiện AND một cách linh hoạt
        StringBuilder sql = new StringBuilder(
                "SELECT hd.MaHD,hd.NgayLap,hd.MaTK,tk.HoTen,hd.SoBan,hd.TongTien,hd.PhuongThucThanhToan " +
                        "FROM HOA_DON hd JOIN TAI_KHOAN tk ON hd.MaTK=tk.MaTK WHERE 1=1 "
        );

        if (ngay > 0)  sql.append(" AND DAY(hd.NgayLap) = ").append(ngay);
        if (thang > 0) sql.append(" AND MONTH(hd.NgayLap) = ").append(thang);
        if (nam > 0)   sql.append(" AND YEAR(hd.NgayLap) = ").append(nam);

        sql.append(" ORDER BY hd.MaHD DESC");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(mapRow(rs));
        }
        return ds;
    }

}