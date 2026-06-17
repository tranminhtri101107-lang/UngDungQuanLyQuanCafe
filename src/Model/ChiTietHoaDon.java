package Model;

/** CHƯƠNG 1 — OOP: Model Chi Tiết Hóa Đơn (từng dòng món) */
public class ChiTietHoaDon {
    private int    maChiTiet;
    private int    maHD;
    private int    maSP;
    private String tenSP;    // Snapshot tên tại thời điểm mua
    private int    soLuong;
    private int    giaBan;   // Snapshot giá tại thời điểm mua

    public ChiTietHoaDon(int maChiTiet, int maHD, int maSP,
                         String tenSP, int soLuong, int giaBan) {
        this.maChiTiet = maChiTiet;
        this.maHD      = maHD;
        this.maSP      = maSP;
        this.tenSP     = tenSP;
        this.soLuong   = soLuong;
        this.giaBan    = giaBan;
    }

    /** Tính thành tiền — không cần DB */
    public long getThanhTien() { return (long) soLuong * giaBan; }

    public int    getMaChiTiet() { return maChiTiet; }
    public int    getMaHD()      { return maHD; }
    public int    getMaSP()      { return maSP; }
    public String getTenSP()     { return tenSP; }
    public int    getSoLuong()   { return soLuong; }
    public int    getGiaBan()    { return giaBan; }

    public void setSoLuong(int v) { soLuong = v; }
}