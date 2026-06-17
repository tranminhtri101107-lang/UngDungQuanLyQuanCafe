package Controller;

import Model.NhanVien;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CHƯƠNG 5 — JDBC + MVC:
 * Controller xử lý nghiệp vụ Nhân Viên — View không gọi DB trực tiếp
 */
public class NhanVienController {

    private static final String SQL_LAY_DS =
            "SELECT tk.MaTK, tk.TenDangNhap, tk.HoTen, vt.TenVaiTro, " +
                    "tk.LuongTheoGio, tk.SoGioLam, tk.NgayVaoLam, tk.ChuKyTangLuong " +
                    "FROM TAI_KHOAN tk JOIN VAI_TRO vt ON tk.MaVaiTro = vt.MaVaiTro ORDER BY tk.MaTK";

    private static final String SQL_TIM =
            "SELECT tk.MaTK, tk.TenDangNhap, tk.HoTen, vt.TenVaiTro, " +
                    "tk.LuongTheoGio, tk.SoGioLam, tk.NgayVaoLam, tk.ChuKyTangLuong " +
                    "FROM TAI_KHOAN tk JOIN VAI_TRO vt ON tk.MaVaiTro = vt.MaVaiTro " +
                    "WHERE tk.HoTen LIKE ? OR tk.TenDangNhap LIKE ?";

    // ── Lấy danh sách ────────────────────────────────────────────
    public List<NhanVien> layDanhSach() throws SQLException {
        List<NhanVien> ds = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_LAY_DS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(mapRow(rs));
        }
        return ds;
    }

    // ── Tìm kiếm ─────────────────────────────────────────────────
    public List<NhanVien> timKiem(String tuKhoa) throws SQLException {
        List<NhanVien> kq = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_TIM)) {
            String p = "%" + tuKhoa + "%";
            ps.setString(1, p); ps.setString(2, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) kq.add(mapRow(rs));
            }
        }
        return kq;
    }

    // ── Thêm nhân viên ────────────────────────────────────────────
    public void them(NhanVien nv) throws SQLException {
        // CHƯƠNG 6 — Băm mật khẩu mặc định 123456 trước khi lưu
        String hash = MaHoaBaoMat.toSHA256("123456");
        String sql  = "INSERT INTO TAI_KHOAN " +
                "(TenDangNhap,MatKhau,HoTen,MaVaiTro,LuongTheoGio,SoGioLam,NgayVaoLam,ChuKyTangLuong) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getTenDangNhap());
            ps.setString(2, hash);
            ps.setString(3, nv.getHoTen());
            ps.setInt   (4, mapVaiTro(nv.getVaiTro()));
            ps.setInt   (5, nv.getLuongTheoGio());
            ps.setInt   (6, nv.getSoGioLam());
            ps.setDate  (7, Date.valueOf(nv.getNgayVaoLam())); // LocalDate → sql.Date
            ps.setInt   (8, nv.getChuKyTangLuong());
            ps.executeUpdate();
        }
    }

    // ── Sửa ───────────────────────────────────────────────────────
    public void sua(NhanVien nv) throws SQLException {
        String sql = "UPDATE TAI_KHOAN SET TenDangNhap=?,HoTen=?,MaVaiTro=?," +
                "LuongTheoGio=?,SoGioLam=?,NgayVaoLam=?,ChuKyTangLuong=? WHERE MaTK=?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getTenDangNhap());
            ps.setString(2, nv.getHoTen());
            ps.setInt   (3, mapVaiTro(nv.getVaiTro()));
            ps.setInt   (4, nv.getLuongTheoGio());
            ps.setInt   (5, nv.getSoGioLam());
            ps.setDate  (6, Date.valueOf(nv.getNgayVaoLam()));
            ps.setInt   (7, nv.getChuKyTangLuong());
            ps.setInt   (8, nv.getMaTK());
            ps.executeUpdate();
        }
    }

    // ── Xóa ───────────────────────────────────────────────────────
    public void xoa(int maTK) throws SQLException {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM TAI_KHOAN WHERE MaTK=?")) {
            ps.setInt(1, maTK);
            ps.executeUpdate();
        }
    }

    // ── Helper: ResultSet → NhanVien ─────────────────────────────
    private NhanVien mapRow(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("NgayVaoLam");
        LocalDate ngay = (sqlDate != null) ? sqlDate.toLocalDate() : LocalDate.now();
        return new NhanVien(
                rs.getInt   ("MaTK"),
                rs.getString("TenDangNhap"),
                rs.getString("HoTen"),
                rs.getString("TenVaiTro"),
                rs.getInt   ("LuongTheoGio"),
                rs.getInt   ("SoGioLam"),
                ngay,
                rs.getInt   ("ChuKyTangLuong")
        );
    }

    private int mapVaiTro(String ten) {
        return ten.equalsIgnoreCase("Quản lý") ? 1 : 2;
    }
}