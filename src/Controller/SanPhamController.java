package Controller;

import Model.SanPham;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CHƯƠNG 5 — JDBC: Controller Sản Phẩm */
public class SanPhamController {

    public List<SanPham> layDanhSach() throws SQLException {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT sp.MaSP, sp.TenSP, sp.GiaBan, dm.TenDanhMuc " +
                "FROM SAN_PHAM sp JOIN DANH_MUC dm ON sp.MaDanhMuc=dm.MaDanhMuc " +
                "ORDER BY dm.MaDanhMuc, sp.TenSP";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new SanPham(
                        rs.getInt("MaSP"), rs.getString("TenSP"),
                        rs.getInt("GiaBan"), rs.getString("TenDanhMuc")));
            }
        }
        return ds;
    }

    public void them(String tenSP, int giaBan, int maDanhMuc) throws SQLException {
        String sql = "INSERT INTO SAN_PHAM (TenSP,GiaBan,MaDanhMuc) VALUES (?,?,?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenSP); ps.setInt(2, giaBan); ps.setInt(3, maDanhMuc);
            ps.executeUpdate();
        }
    }

    public void sua(int maSP, String tenSP, int giaBan) throws SQLException {
        String sql = "UPDATE SAN_PHAM SET TenSP=?,GiaBan=? WHERE MaSP=?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenSP); ps.setInt(2, giaBan); ps.setInt(3, maSP);
            ps.executeUpdate();
        }
    }

    public void xoa(int maSP) throws SQLException {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM SAN_PHAM WHERE MaSP=?")) {
            ps.setInt(1, maSP); ps.executeUpdate();
        }
    }

    /** Lấy danh mục để hiển thị ComboBox */
    public List<String[]> layDanhMuc() throws SQLException {
        List<String[]> ds = new ArrayList<>();
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT MaDanhMuc,TenDanhMuc FROM DANH_MUC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(new String[]{rs.getString("MaDanhMuc"), rs.getString("TenDanhMuc")});
        }
        return ds;
    }
}