package Model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * CHƯƠNG 1 — OOP: Model Nhân Viên
 * Encapsulation: tất cả field private, truy cập qua getter/setter
 */
public class NhanVien {
    private int       maTK;
    private String    tenDangNhap;
    private String    hoTen;
    private String    vaiTro;
    private int       luongTheoGio;   // VNĐ/giờ
    private int       soGioLam;       // Tổng giờ làm trong tháng
    private LocalDate ngayVaoLam;     // Dùng LocalDate thay sql.Date (tốt hơn)
    private int       chuKyTangLuong; // Số tháng

    public NhanVien(int maTK, String tenDangNhap, String hoTen, String vaiTro,
                    int luongTheoGio, int soGioLam, LocalDate ngayVaoLam, int chuKyTangLuong) {
        this.maTK           = maTK;
        this.tenDangNhap    = tenDangNhap;
        this.hoTen          = hoTen;
        this.vaiTro         = vaiTro;
        this.luongTheoGio   = luongTheoGio;
        this.soGioLam       = soGioLam;
        this.ngayVaoLam     = ngayVaoLam;
        this.chuKyTangLuong = chuKyTangLuong;
    }

    // ── Business Logic ───────────────────────────────────────────
    /** Tính tổng lương — logic thuần từ dữ liệu object, không cần DB */
    public long tinhTongLuong() { return (long) luongTheoGio * soGioLam; }

    /** Kiểm tra đến hạn xét tăng lương */
    public boolean isDenHanTangLuong(LocalDate hienTai) {
        if (chuKyTangLuong <= 0 || ngayVaoLam == null) return false;
        return ChronoUnit.MONTHS.between(ngayVaoLam, hienTai) >= chuKyTangLuong;
    }

    // ── Getters ──────────────────────────────────────────────────
    public int       getMaTK()           { return maTK; }
    public String    getTenDangNhap()    { return tenDangNhap; }
    public String    getHoTen()          { return hoTen; }
    public String    getVaiTro()         { return vaiTro; }
    public int       getLuongTheoGio()   { return luongTheoGio; }
    public int       getSoGioLam()       { return soGioLam; }
    public LocalDate getNgayVaoLam()     { return ngayVaoLam; }
    public int       getChuKyTangLuong() { return chuKyTangLuong; }

    // ── Setters ──────────────────────────────────────────────────
    public void setTenDangNhap(String v)   { tenDangNhap    = v; }
    public void setHoTen(String v)         { hoTen          = v; }
    public void setVaiTro(String v)        { vaiTro         = v; }
    public void setLuongTheoGio(int v)     { luongTheoGio   = v; }
    public void setSoGioLam(int v)         { soGioLam       = v; }
    public void setNgayVaoLam(LocalDate v) { ngayVaoLam     = v; }
    public void setChuKyTangLuong(int v)   { chuKyTangLuong = v; }

    @Override public String toString() {
        return "NhanVien{maTK=" + maTK + ", hoTen='" + hoTen + "', vaiTro='" + vaiTro + "'}";
    }
}