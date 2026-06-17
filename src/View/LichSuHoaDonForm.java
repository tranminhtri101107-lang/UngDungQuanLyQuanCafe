package View;

import Controller.HoaDonController;
import Model.HoaDon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Form lịch sử hóa đơn
 * CHƯƠNG 5 — JDBC: Lấy toàn bộ lịch sử từ DB có điều kiện LỌC
 */
public class LichSuHoaDonForm extends JPanel {

    private final HoaDonController    ctrl  = new HoaDonController();
    private DefaultTableModel          model;
    private JLabel                     lblTong;
    private JComboBox<String>          cbNgay, cbThang, cbNam;
    private static final DecimalFormat DF = new DecimalFormat("#,###");

    public LichSuHoaDonForm() {
        setLayout(new BorderLayout());

        add(UIHelper.taoHeader("📋  Lịch Sử Giao Dịch"), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildSouth(),  BorderLayout.SOUTH);

        load(); // Tải tất cả khi vừa mở
    }

    private JPanel buildCenter() {
        // ── THANH LỌC TÌM KIẾM THEO NGÀY THÁNG NĂM ──
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlFilter.setBackground(Color.WHITE);
        pnlFilter.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        cbNgay  = new JComboBox<>(new String[]{"Tất cả", "1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"});
        cbThang = new JComboBox<>(new String[]{"Tất cả", "1","2","3","4","5","6","7","8","9","10","11","12"});
        cbNam   = new JComboBox<>(new String[]{"Tất cả", "2024","2025","2026","2027"});

        cbNam.setSelectedItem("2026"); // Mặc định chọn năm 2026 cho tiện lợi

        cbNgay.setFont(UIHelper.F_BODY); cbThang.setFont(UIHelper.F_BODY); cbNam.setFont(UIHelper.F_BODY);

        // Gắn icon kinhlup.png vào nút Lọc
        JButton btnLoc = UIHelper.taoNut(" Lọc", UIHelper.C_PRIMARY, 100, 32);
        btnLoc.setIcon(UIHelper.resizeIcon("/Resource/kinhlup.png", 16, 16));
        btnLoc.addActionListener(e -> locDuLieu());

        pnlFilter.add(new JLabel("Ngày:")); pnlFilter.add(cbNgay);
        pnlFilter.add(new JLabel("Tháng:")); pnlFilter.add(cbThang);
        pnlFilter.add(new JLabel("Năm:")); pnlFilter.add(cbNam);
        pnlFilter.add(btnLoc);

        // ── BẢNG DỮ LIỆU ──
        String[] cols = {"Mã HĐ","Thời Gian","Bàn","Tổng Tiền (đ)","Phương Thức","Nhân Viên"};
        model = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        UIHelper.canGiuaCot(table, 0, 4);
        UIHelper.canPhaiCot(table, 3);
        table.getColumnModel().getColumn(0).setMaxWidth(72);
        table.getColumnModel().getColumn(1).setPreferredWidth(148);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(145);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);

        JScrollPane sc = new JScrollPane(table); sc.setBorder(BorderFactory.createEmptyBorder());

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(UIHelper.C_BG);
        pnl.add(pnlFilter, BorderLayout.NORTH); // Đưa thanh lọc vào trên cùng của bảng
        pnl.add(sc, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel buildSouth() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,0,0,new Color(220,220,220)),
                BorderFactory.createEmptyBorder(10,18,10,18)));

        lblTong = new JLabel("Đang tải...");
        lblTong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTong.setForeground(UIHelper.C_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); right.setOpaque(false);
        JButton btnLM = UIHelper.taoNut("🔄  Làm mới / Xem tất cả", UIHelper.C_PRIMARY, 190,34);

        // Nút làm mới sẽ trả các ComboBox về mặc định và tải lại toàn bộ
        btnLM.addActionListener(e -> {
            cbNgay.setSelectedIndex(0);
            cbThang.setSelectedIndex(0);
            cbNam.setSelectedItem("2026");
            load();
        });
        right.add(btnLM);

        pnl.add(lblTong, BorderLayout.WEST);
        pnl.add(right,   BorderLayout.EAST);
        return pnl;
    }

    // Hàm thực thi tìm kiếm gọi lệnh SQL mới
    private void locDuLieu() {
        int ngay  = cbNgay.getSelectedIndex() == 0 ? 0 : Integer.parseInt(cbNgay.getSelectedItem().toString());
        int thang = cbThang.getSelectedIndex() == 0 ? 0 : Integer.parseInt(cbThang.getSelectedItem().toString());
        int nam   = cbNam.getSelectedIndex() == 0 ? 0 : Integer.parseInt(cbNam.getSelectedItem().toString());

        model.setRowCount(0);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        long tong = 0;
        try {
            for (HoaDon hd : ctrl.layTheoNgayThangNam(ngay, thang, nam)) {
                tong += hd.getTongTien();
                model.addRow(new Object[]{
                        hd.getMaHD(),
                        hd.getNgayLap() != null ? hd.getNgayLap().format(dtf) : "—",
                        hd.getSoBan(),
                        DF.format(hd.getTongTien()),
                        hd.getPhuongThucThanhToan(),
                        hd.getTenNhanVien()
                });
            }
        } catch (SQLException e) { UIHelper.showError(this,"Lỗi: "+e.getMessage()); }
        lblTong.setText("Đã lọc " + model.getRowCount() + " HĐ  |  Tổng: " + DF.format(tong) + " đ");
    }

    private void load() {
        model.setRowCount(0);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        long tong = 0;
        try {
            for (HoaDon hd : ctrl.layDanhSach()) {
                tong += hd.getTongTien();
                model.addRow(new Object[]{
                        hd.getMaHD(),
                        hd.getNgayLap() != null ? hd.getNgayLap().format(dtf) : "—",
                        hd.getSoBan(),
                        DF.format(hd.getTongTien()),
                        hd.getPhuongThucThanhToan(),
                        hd.getTenNhanVien()
                });
            }
        } catch (SQLException e) { UIHelper.showError(this,"Lỗi: "+e.getMessage()); }
        lblTong.setText("Tổng " + model.getRowCount() + " HĐ  |  " + DF.format(tong) + " đ");
    }
}