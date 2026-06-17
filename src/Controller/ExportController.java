package Controller;

import Model.ChiTietHoaDon;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * CHƯƠNG 3 — IO STREAM:
 * Xuất hóa đơn và báo cáo doanh thu ra file .txt
 *
 * Dùng:
 * - FileOutputStream + OutputStreamWriter: ghi UTF-8 (tiếng Việt không lỗi)
 * - BufferedWriter: tối ưu I/O theo vùng đệm
 * - PrintWriter: tiện dùng println
 * - FileWriter append=true: ghi log mà không xóa nội dung cũ
 */
public class ExportController {

    private static final DateTimeFormatter DTF      = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DTF_FILE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ExportController() {}

    /**
     * Xuất hóa đơn thanh toán ra file .txt
     * CHƯƠNG 3 — FileOutputStream + BufferedWriter + PrintWriter
     */
    public static String xuatHoaDon(int maHD, String soBan,
                                    List<ChiTietHoaDon> danhSach,
                                    long tongTien, String pt, String tenNV) throws IOException {

        // TẠO THƯ MỤC "hoadon" NẾU CHƯA CÓ
        new File("hoadon").mkdirs();
        String path = String.format("hoadon/HoaDon_%d_%s.txt", maHD, LocalDateTime.now().format(DTF_FILE));

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
             PrintWriter pw = new PrintWriter(bw)) {

            pw.println("╔══════════════════════════════════════╗");
            pw.println("║       QUÁN CAFE HUTEA — HÓA ĐƠN      ║");
            pw.println("╚══════════════════════════════════════╝");
            pw.println();
            pw.printf ("  Mã HĐ      : #%d%n",   maHD);
            pw.printf ("  Bàn        : %s%n",     soBan);
            pw.printf ("  Nhân viên  : %s%n",     tenNV);
            pw.printf ("  Thời gian  : %s%n",     LocalDateTime.now().format(DTF));
            pw.println("  ─────────────────────────────────────");
            pw.printf ("  %-22s %5s %12s%n", "Tên món", "SL", "Thành tiền");
            pw.println("  ─────────────────────────────────────");
            for (ChiTietHoaDon ct : danhSach) {
                String ten = ct.getTenSP().length() > 22 ? ct.getTenSP().substring(0,21)+"…" : ct.getTenSP();
                pw.printf("  %-22s %5d %,12d đ%n", ten, ct.getSoLuong(), ct.getThanhTien());
            }
            pw.println("  ─────────────────────────────────────");
            pw.printf ("  TỔNG TIỀN   : %,20d đ%n", tongTien);
            pw.printf ("  THANH TOÁN  : %-20s%n",   pt);
            pw.println();
            pw.println("  Cảm ơn quý khách! Hẹn gặp lại ♥");
            pw.println("═══════════════════════════════════════");
        }

        System.out.println("[ExportController] Xuất HĐ: " + path);
        return path;
    }

    /**
     * Xuất báo cáo doanh thu tháng ra file .txt
     */
    public static String xuatBaoCaoDoanhThu(int thang, int nam,
                                            Map<String, Long> data) throws IOException {

        // TẠO THƯ MỤC "doanhthu" NẾU CHƯA CÓ
        new File("doanhthu").mkdirs();
        String path = String.format("doanhthu/BaoCao_T%02d_%d_%s.txt",
                thang, nam, LocalDateTime.now().format(DTF_FILE));

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
             PrintWriter pw = new PrintWriter(bw)) {

            pw.println("╔══════════════════════════════════════╗");
            pw.printf ("║   BÁO CÁO DOANH THU  THÁNG %02d/%d   ║%n", thang, nam);
            pw.println("╚══════════════════════════════════════╝");
            pw.printf ("  Xuất lúc: %s%n", LocalDateTime.now().format(DTF));
            pw.println("  ─────────────────────────────────────");
            pw.printf ("  %-15s %18s%n", "Ngày", "Doanh Thu");
            pw.println("  ─────────────────────────────────────");

            long tong = 0;
            for (Map.Entry<String, Long> e : data.entrySet()) {
                String[] p = e.getKey().split("-");
                String ngay = p[2]+"/"+p[1]+"/"+p[0];
                pw.printf("  %-15s %,18d đ%n", ngay, e.getValue());
                tong += e.getValue();
            }

            pw.println("  ─────────────────────────────────────");
            pw.printf ("  TỔNG THÁNG      %,18d đ%n", tong);
            if (!data.isEmpty())
                pw.printf("  TRUNG BÌNH/NGÀY %,18d đ%n", tong / data.size());
            pw.println("═══════════════════════════════════════");
        }

        System.out.println("[ExportController] Xuất BC: " + path);
        return path;
    }

    /**
     * Ghi log hoạt động hệ thống (append — không xóa nội dung cũ)
     * CHƯƠNG 3 — FileWriter với append=true
     */
    public static void ghiLog(String noiDung) {

        // TẠO THƯ MỤC "logs" ĐỂ LƯU FILE LOG CHO GỌN
        new File("logs").mkdirs();
        try (FileWriter fw = new FileWriter("logs/system.log", true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("[" + LocalDateTime.now().format(DTF) + "] " + noiDung);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("[ExportController] Lỗi ghi log: " + e.getMessage());
        }
    }
}