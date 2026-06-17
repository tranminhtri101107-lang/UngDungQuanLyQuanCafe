package Model;

/** CHƯƠNG 1 — OOP: Model Sản Phẩm (Menu đồ uống) */
public class SanPham {
    private int    maSP;
    private String tenSP;
    private int    giaBan;
    private String tenDanhMuc;

    public SanPham(int maSP, String tenSP, int giaBan, String tenDanhMuc) {
        this.maSP       = maSP;
        this.tenSP      = tenSP;
        this.giaBan     = giaBan;
        this.tenDanhMuc = tenDanhMuc;
    }

    public SanPham(int maSP, String tenSP, int giaBan) {
        this(maSP, tenSP, giaBan, "");
    }

    public int    getMaSP()       { return maSP; }
    public String getTenSP()      { return tenSP; }
    public int    getGiaBan()     { return giaBan; }
    public String getTenDanhMuc() { return tenDanhMuc; }

    public void setTenSP(String v)      { tenSP      = v; }
    public void setGiaBan(int v)        { giaBan     = v; }
    public void setTenDanhMuc(String v) { tenDanhMuc = v; }

    @Override public String toString() {
        return tenSP + " — " + String.format("%,d đ", giaBan);
    }
}