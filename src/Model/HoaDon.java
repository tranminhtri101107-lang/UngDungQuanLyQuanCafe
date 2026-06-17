package Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** CHƯƠNG 1 — OOP: Model Hóa Đơn */
public class HoaDon {
    private int           maHD;
    private LocalDateTime ngayLap;
    private int           maTK;
    private String        tenNhanVien;
    private String        soBan;
    private long          tongTien;
    private String        phuongThucThanhToan;
    private List<ChiTietHoaDon> danhSachChiTiet = new ArrayList<>();

    public HoaDon(int maHD, LocalDateTime ngayLap, int maTK, String tenNhanVien,
                  String soBan, long tongTien, String phuongThucThanhToan) {
        this.maHD                = maHD;
        this.ngayLap             = ngayLap;
        this.maTK                = maTK;
        this.tenNhanVien         = tenNhanVien;
        this.soBan               = soBan;
        this.tongTien            = tongTien;
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public int           getMaHD()                  { return maHD; }
    public LocalDateTime getNgayLap()               { return ngayLap; }
    public int           getMaTK()                  { return maTK; }
    public String        getTenNhanVien()            { return tenNhanVien; }
    public String        getSoBan()                 { return soBan; }
    public long          getTongTien()              { return tongTien; }
    public String        getPhuongThucThanhToan()   { return phuongThucThanhToan; }
    public List<ChiTietHoaDon> getDanhSachChiTiet() { return danhSachChiTiet; }

    public void setSoBan(String v)                        { soBan               = v; }
    public void setTongTien(long v)                       { tongTien            = v; }
    public void setPhuongThucThanhToan(String v)          { phuongThucThanhToan = v; }
    public void setDanhSachChiTiet(List<ChiTietHoaDon> v) { danhSachChiTiet     = v; }
}