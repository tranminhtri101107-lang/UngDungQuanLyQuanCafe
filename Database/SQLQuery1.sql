-- ============================================================
-- FILE: setup_full.sql
-- MÔ TẢ: Tạo toàn bộ CSDL QuanLyQuanCafe từ đầu
-- CÁCH DÙNG: Bôi đen toàn bộ và nhấn F5 trong SSMS
-- ============================================================

-- Xóa DB cũ nếu có (cẩn thận khi dùng ở môi trường production)
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'QuanLyQuanCafe')
    DROP DATABASE QuanLyQuanCafe;
GO

CREATE DATABASE QuanLyQuanCafe;
GO

USE QuanLyQuanCafe;
GO

-- ============================================================
-- BẢNG 1: VAI_TRO — Quản lý / Nhân viên
-- ============================================================
CREATE TABLE VAI_TRO (
    MaVaiTro    INT IDENTITY(1,1) PRIMARY KEY,
    TenVaiTro   NVARCHAR(50) NOT NULL
);
GO

-- ============================================================
-- BẢNG 2: TAI_KHOAN — Thông tin đăng nhập + lương part-time
-- ============================================================
CREATE TABLE TAI_KHOAN (
    MaTK            INT IDENTITY(1,1) PRIMARY KEY,
    TenDangNhap     VARCHAR(50)     UNIQUE NOT NULL,
    MatKhau         VARCHAR(255)    NOT NULL,           -- Lưu dạng SHA-256
    HoTen           NVARCHAR(100)   NOT NULL,
    MaVaiTro        INT             NOT NULL,
    LuongTheoGio    INT             DEFAULT 21000,      -- VNĐ/giờ
    SoGioLam        INT             DEFAULT 0,
    NgayVaoLam      DATE            DEFAULT GETDATE(),
    ChuKyTangLuong  INT             DEFAULT 6,          -- Số tháng
    CONSTRAINT FK_TaiKhoan_VaiTro FOREIGN KEY (MaVaiTro) REFERENCES VAI_TRO(MaVaiTro)
);
GO

-- ============================================================
-- BẢNG 3: DANH_MUC — Phân loại đồ uống
-- ============================================================
CREATE TABLE DANH_MUC (
    MaDanhMuc   INT IDENTITY(1,1) PRIMARY KEY,
    TenDanhMuc  NVARCHAR(100) NOT NULL
);
GO

-- ============================================================
-- BẢNG 4: SAN_PHAM — Menu đồ uống
-- ============================================================
CREATE TABLE SAN_PHAM (
    MaSP        INT IDENTITY(1,1) PRIMARY KEY,
    TenSP       NVARCHAR(100)   NOT NULL,
    GiaBan      INT             NOT NULL,
    MaDanhMuc   INT             NOT NULL,
    CONSTRAINT FK_SanPham_DanhMuc FOREIGN KEY (MaDanhMuc) REFERENCES DANH_MUC(MaDanhMuc)
);
GO

-- ============================================================
-- BẢNG 5: HOA_DON — Header hóa đơn
-- ============================================================
CREATE TABLE HOA_DON (
    MaHD                    INT IDENTITY(1,1) PRIMARY KEY,
    NgayLap                 DATETIME        DEFAULT GETDATE(),
    MaTK                    INT             NOT NULL,
    SoBan                   NVARCHAR(50),
    TongTien                BIGINT          DEFAULT 0,
    PhuongThucThanhToan     NVARCHAR(50)    DEFAULT N'TIỀN MẶT',
    CONSTRAINT FK_HoaDon_TaiKhoan FOREIGN KEY (MaTK) REFERENCES TAI_KHOAN(MaTK)
);
GO

-- ============================================================
-- BẢNG 6: CHI_TIET_HOA_DON — Các món trong hóa đơn
-- Lưu ý: Dùng IDENTITY PK riêng thay vì khóa chính kép
--        để tránh lỗi khi khách gọi cùng 1 món nhiều lần
-- ThanhTien là Computed Column — tự tính SoLuong * GiaBan
-- ============================================================
CREATE TABLE CHI_TIET_HOA_DON (
    MaChiTiet   INT IDENTITY(1,1) PRIMARY KEY,
    MaHD        INT             NOT NULL,
    MaSP        INT             NOT NULL,
    TenSP       NVARCHAR(100)   NOT NULL,   -- Snapshot tên tại thời điểm mua
    SoLuong     INT             NOT NULL    CHECK (SoLuong > 0),
    GiaBan      INT             NOT NULL,   -- Snapshot giá tại thời điểm mua
    ThanhTien   AS (SoLuong * GiaBan) PERSISTED,
    CONSTRAINT FK_ChiTiet_HoaDon    FOREIGN KEY (MaHD)  REFERENCES HOA_DON(MaHD)    ON DELETE CASCADE,
    CONSTRAINT FK_ChiTiet_SanPham   FOREIGN KEY (MaSP)  REFERENCES SAN_PHAM(MaSP)
);
GO

-- ============================================================
-- INDEX — Tăng tốc truy vấn thống kê
-- ============================================================
CREATE INDEX IDX_HoaDon_NgayLap ON HOA_DON(NgayLap);
CREATE INDEX IDX_ChiTiet_MaHD   ON CHI_TIET_HOA_DON(MaHD);
GO

-- ============================================================
-- DỮ LIỆU MẪU
-- ============================================================

-- Vai trò
INSERT INTO VAI_TRO (TenVaiTro) VALUES (N'Quản lý'), (N'Nhân viên');
GO

-- Tài khoản mẫu — mật khẩu mặc định: 123456
-- SHA-256('123456') = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
INSERT INTO TAI_KHOAN (TenDangNhap, MatKhau, HoTen, MaVaiTro, LuongTheoGio, SoGioLam, NgayVaoLam, ChuKyTangLuong)
VALUES
('admin',
 '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
 N'Trần Minh Trí', 1, 30000, 160, '2026-01-01', 6),
('nhanvien1',
 '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
 N'Nguyễn Thị Lan', 2, 21000, 80, '2026-03-01', 6);
GO

-- Danh mục
INSERT INTO DANH_MUC (TenDanhMuc) VALUES
(N'Cà phê'), (N'Trà & Nước ép'), (N'Bánh & Ăn vặt');
GO

-- Sản phẩm
INSERT INTO SAN_PHAM (TenSP, GiaBan, MaDanhMuc) VALUES
(N'Cà phê đen',         16000, 1),
(N'Cà phê sữa',         18000, 1),
(N'Bạc xỉu',            20000, 1),
(N'Bạc xỉu muối',       27000, 1),
(N'Cà phê muối',        25000, 1),
(N'Americano',          30000, 1),
(N'Cà phê cốt dừa',     38000, 1),
(N'Cà phê kem trứng',   27000, 1),
(N'Espresso',           25000, 1),
(N'Trà đào cam sả',     35000, 2),
(N'Trà sữa trân châu',  40000, 2),
(N'Nước ép cam',        30000, 2);
GO

-- Xác nhận
SELECT 'Setup hoàn tất!' AS KetQua;
SELECT * FROM VAI_TRO;
SELECT * FROM TAI_KHOAN;
SELECT * FROM SAN_PHAM;
GO