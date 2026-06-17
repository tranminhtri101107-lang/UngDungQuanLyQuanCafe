package View;

import Controller.NhanVienController;
import Model.NhanVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * CHƯƠNG 1 (GIAO DIỆN) & CHƯƠNG 5 (JDBC):
 * Form Quản lý nhân viên với thiết kế DataGrid + Modal Popup chuẩn POS
 */
public class QuanLyNhanVienForm extends JPanel {

    private final NhanVienController ctrl = new NhanVienController();
    private DefaultTableModel model;
    private JTable table;
    private JTextField txtTim;

    public QuanLyNhanVienForm() {
        setLayout(new BorderLayout());
        add(UIHelper.taoHeader("👥  Quản lý Nhân Viên & Lương Part-time"), BorderLayout.NORTH);

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.add(buildToolbar(), BorderLayout.NORTH);
        pnlMain.add(buildTable(), BorderLayout.CENTER);

        add(pnlMain, BorderLayout.CENTER);

        loadTable();
    }

    // ── THANH CÔNG CỤ (NÚT BẤM & TÌM KIẾM) ─────────────────────────
    private JPanel buildToolbar() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Nhóm nút chức năng bên trái
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JButton btnThem  = UIHelper.taoNut(" Thêm", UIHelper.C_SUCCESS, 110, 36);
        btnThem.setIcon(UIHelper.resizeIcon("/Resource/adduser.png", 16, 16));

        JButton btnSua   = UIHelper.taoNut(" Cập nhật", UIHelper.C_WARNING, 120, 36);
        btnSua.setIcon(UIHelper.resizeIcon("/Resource/edit.png", 16, 16));

        JButton btnXoa   = UIHelper.taoNut(" Xóa", UIHelper.C_DANGER, 100, 36);
        btnXoa.setIcon(UIHelper.resizeIcon("/Resource/delete.png", 16, 16));

        JButton btnLuong = UIHelper.taoNut(" Kiểm tra tăng lương", UIHelper.C_PURPLE, 180, 36);
        btnLuong.setIcon(UIHelper.resizeIcon("/Resource/salary.png", 16, 16));

        left.add(btnThem); left.add(btnSua); left.add(btnXoa); left.add(btnLuong);

        // Nhóm tìm kiếm bên phải
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        txtTim = new JTextField(15);
        txtTim.setFont(UIHelper.F_BODY); txtTim.setPreferredSize(new Dimension(200, 36));
        txtTim.putClientProperty("JTextField.placeholderText", "🔍 Tìm tên hoặc mã...");

        JButton btnTim = UIHelper.taoNut("Tìm", UIHelper.C_PRIMARY, 80, 36);
        JButton btnHuy = UIHelper.taoNut("Hủy", UIHelper.C_GRAY, 80, 36);
        right.add(txtTim); right.add(btnTim); right.add(btnHuy);

        // Gắn sự kiện
        btnThem.addActionListener(e -> showDialog(null));
        btnSua.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.showError(this, "Vui lòng chọn một nhân viên để sửa!"); return; }
            showDialog(getNhanVienFromRow(r));
        });
        btnXoa.addActionListener(e -> xoa());
        btnLuong.addActionListener(e -> kiemTraTangLuong());

        btnTim.addActionListener(e -> timKiem());
        btnHuy.addActionListener(e -> { txtTim.setText(""); loadTable(); });

        pnl.add(left, BorderLayout.WEST);
        pnl.add(right, BorderLayout.EAST);
        return pnl;
    }

    // ── BẢNG DỮ LIỆU FULL MÀN HÌNH ─────────────────────────────────
    private JPanel buildTable() {
        String[] cols = {"Mã TK", "Tên ĐN", "Họ Tên", "Vai Trò", "Lương/Giờ", "Giờ Làm", "Ngày Vào", "Chu Kỳ", "TỔNG LƯƠNG"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UIHelper.styleTable(table);
        UIHelper.canGiuaCot(table, 0, 3, 5, 7);
        UIHelper.canPhaiCot(table, 4, 8);

        int[] widths = {55, 105, 150, 95, 95, 75, 100, 75, 120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(UIHelper.C_BG);
        JScrollPane sc = new JScrollPane(table);
        sc.setBorder(BorderFactory.createEmptyBorder());
        pnl.add(sc, BorderLayout.CENTER);
        return pnl;
    }

    // ── HỘP THOẠI POPUP THÊM/SỬA NHÂN VIÊN ─────────────────────────
    private void showDialog(NhanVien nv) {
        boolean isEdit = (nv != null);
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog((Frame) owner, isEdit ? "✏ Cập nhật Nhân viên" : "➕ Thêm Nhân viên mới", true);
        dlg.setSize(420, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 5, 8, 5); g.anchor = GridBagConstraints.WEST;

        JTextField txtTenDN = UIHelper.taoTextField(true);
        JTextField txtHoTen = UIHelper.taoTextField(true);
        JComboBox<String> cbVaiTro = new JComboBox<>(new String[]{"Quản lý", "Nhân viên"});
        cbVaiTro.setFont(UIHelper.F_BODY); cbVaiTro.setPreferredSize(new Dimension(220, 32));

        JTextField txtLuong = UIHelper.taoTextField(true); txtLuong.setText("21000");
        JTextField txtGio = UIHelper.taoTextField(true); txtGio.setText("0");
        JTextField txtNgay = UIHelper.taoTextField(true); txtNgay.setText(LocalDate.now().toString());
        JTextField txtChuKy = UIHelper.taoTextField(true); txtChuKy.setText("6");

        // Nếu là sửa, đưa dữ liệu cũ lên form
        if (isEdit) {
            txtTenDN.setText(nv.getTenDangNhap());
            txtTenDN.setEditable(false); // Không cho sửa tên đăng nhập để tránh lỗi hệ thống
            txtTenDN.setBackground(new Color(245,245,245));
            txtHoTen.setText(nv.getHoTen());
            cbVaiTro.setSelectedItem(nv.getVaiTro());
            txtLuong.setText(String.valueOf(nv.getLuongTheoGio()));
            txtGio.setText(String.valueOf(nv.getSoGioLam()));
            txtNgay.setText(nv.getNgayVaoLam().toString());
            txtChuKy.setText(String.valueOf(nv.getChuKyTangLuong()));
        }

        Object[][] fields = {
                {"Tên đăng nhập:", txtTenDN},
                {"Họ và tên:", txtHoTen},
                {"Vai trò:", cbVaiTro},
                {"Lương/Giờ (đ):", txtLuong},
                {"Số giờ làm:", txtGio},
                {"Ngày vào làm:", txtNgay},
                {"Chu kỳ tăng (tháng):", txtChuKy}
        };

        for (int i = 0; i < fields.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0; g.fill = GridBagConstraints.NONE;
            form.add(UIHelper.taoLabel(fields[i][0].toString()), g);
            g.gridx = 1; g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL;
            form.add((Component) fields[i][1], g);
        }

        // Thanh nút bấm ở dưới popup
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bot.setBackground(new Color(245, 245, 245));
        bot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton btnHuyDialog = UIHelper.taoNut("Hủy bỏ", UIHelper.C_GRAY, 100, 34);
        JButton btnLuu = UIHelper.taoNut("✅ Lưu dữ liệu", UIHelper.C_SUCCESS, 135, 34);

        btnHuyDialog.addActionListener(e -> dlg.dispose());
        btnLuu.addActionListener(e -> {
            try {
                NhanVien n = new NhanVien(
                        isEdit ? nv.getMaTK() : 0,
                        txtTenDN.getText().trim(),
                        txtHoTen.getText().trim(),
                        cbVaiTro.getSelectedItem().toString(),
                        Integer.parseInt(txtLuong.getText().trim()),
                        Integer.parseInt(txtGio.getText().trim()),
                        LocalDate.parse(txtNgay.getText().trim()),
                        Integer.parseInt(txtChuKy.getText().trim())
                );

                if (isEdit) ctrl.sua(n);
                else ctrl.them(n);

                loadTable();
                dlg.dispose();
                JOptionPane.showMessageDialog(this, isEdit ? "✅ Cập nhật thành công!" : "✅ Thêm thành công!\nMật khẩu mặc định: 123456");
            } catch (NumberFormatException | DateTimeParseException ex) {
                UIHelper.showError(dlg, "Lỗi nhập liệu! Ngày nhập dạng (YYYY-MM-DD), Lương và Giờ phải là số.");
            } catch (SQLException ex) {
                UIHelper.showError(dlg, "Lỗi DB: " + ex.getMessage());
            }
        });

        bot.add(btnHuyDialog); bot.add(btnLuu);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(bot, BorderLayout.SOUTH);
        dlg.setVisible(true); // Hiển thị popup
    }

    // ── CÁC HÀM XỬ LÝ LOGIC ────────────────────────────────────────

    private void loadTable() {
        model.setRowCount(0);
        try { ctrl.layDanhSach().forEach(this::addRow); }
        catch (SQLException e) { UIHelper.showError(this, "Lỗi: " + e.getMessage()); }
    }

    private void timKiem() {
        model.setRowCount(0);
        try { ctrl.timKiem(txtTim.getText()).forEach(this::addRow); }
        catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }

    private void addRow(NhanVien nv) {
        model.addRow(new Object[]{
                nv.getMaTK(), nv.getTenDangNhap(), nv.getHoTen(), nv.getVaiTro(),
                nv.getLuongTheoGio(), nv.getSoGioLam(), nv.getNgayVaoLam(),
                nv.getChuKyTangLuong(),
                String.format("%,d đ", nv.tinhTongLuong())
        });
    }

    private NhanVien getNhanVienFromRow(int r) {
        return new NhanVien(
                Integer.parseInt(model.getValueAt(r, 0).toString()),
                model.getValueAt(r, 1).toString(),
                model.getValueAt(r, 2).toString(),
                model.getValueAt(r, 3).toString(),
                Integer.parseInt(model.getValueAt(r, 4).toString()),
                Integer.parseInt(model.getValueAt(r, 5).toString()),
                LocalDate.parse(model.getValueAt(r, 6).toString()),
                Integer.parseInt(model.getValueAt(r, 7).toString())
        );
    }

    private void xoa() {
        int r = table.getSelectedRow();
        if (r < 0) { UIHelper.showError(this, "Chọn nhân viên cần xóa!"); return; }
        if (!UIHelper.confirm(this, "Bạn có chắc chắn muốn xóa nhân viên này khỏi hệ thống?")) return;
        try {
            int maTK = Integer.parseInt(model.getValueAt(r, 0).toString());
            ctrl.xoa(maTK);
            loadTable();
        } catch (SQLException e) { UIHelper.showError(this, "Lỗi: " + e.getMessage()); }
    }

    private void kiemTraTangLuong() {
        StringBuilder sb = new StringBuilder("Nhân viên đến hạn xét tăng lương:\n\n");
        boolean co = false;
        try {
            for (NhanVien nv : ctrl.layDanhSach()) {
                if (nv.isDenHanTangLuong(LocalDate.now())) {
                    long thang = java.time.temporal.ChronoUnit.MONTHS.between(nv.getNgayVaoLam(), LocalDate.now());
                    sb.append("• ").append(nv.getHoTen()).append("  (đã làm ").append(thang).append(" tháng)\n");
                    co = true;
                }
            }
        } catch (SQLException e) { UIHelper.showError(this, e.getMessage()); return; }
        JOptionPane.showMessageDialog(this, co ? sb.toString() : "Chưa có nhân viên nào đến hạn xét duyệt.", "Kiểm tra tăng lương", JOptionPane.INFORMATION_MESSAGE);
    }
}