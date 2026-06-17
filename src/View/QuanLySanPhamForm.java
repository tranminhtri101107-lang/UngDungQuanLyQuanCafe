package View;

import Controller.SanPhamController;
import Model.SanPham;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class QuanLySanPhamForm extends JPanel {

    private final SanPhamController ctrl = new SanPhamController();
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtTenMon, txtGiaBan, txtTimKiem;
    private JComboBox<String[]> cbDanhMuc;
    private JLabel lblMaSP, lblAnhMon;
    private JCheckBox chkCoSize;
    private List<String[]> danhMucList;

    // --- CÁC BIẾN XỬ LÝ ẢNH & SIZE ---
    private File anhDangChon = null;
    private final String DIR_ANH = "src/Resource/Drinks/";
    private final String FILE_SIZE = "size_config.properties";
    private Properties sizeProps = new Properties();

    public QuanLySanPhamForm() {
        // Tự động tạo thư mục chứa ảnh nếu chưa có
        new File(DIR_ANH).mkdirs();
        loadSizeConfig();

        setLayout(new BorderLayout());
        add(UIHelper.taoHeader("☕  Quản lý Menu Đồ Uống"), BorderLayout.NORTH);

        // Chia đôi màn hình: Trái (Danh sách) - Phải (Chi tiết)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildCotTrai(), buildCotPhai());
        split.setDividerLocation(520);
        split.setDividerSize(3);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);

        loadDanhMuc();
        loadTable();
    }

    // ════════════════════════════════════════════════════════════
    // 1. CỘT TRÁI: TÌM KIẾM & DANH SÁCH MÓN
    // ════════════════════════════════════════════════════════════
    private JPanel buildCotTrai() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(UIHelper.C_BG);

        // Header: Thanh tìm kiếm
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        pnlSearch.setBackground(Color.WHITE);
        pnlSearch.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        txtTimKiem = new JTextField(20);
        txtTimKiem.setFont(UIHelper.F_BODY);
        txtTimKiem.setPreferredSize(new Dimension(250, 32));
        txtTimKiem.putClientProperty("JTextField.placeholderText", " Nhập tên món cần tìm...");

        JButton btnHuy = UIHelper.taoNut("✖ Xóa", UIHelper.C_GRAY, 80, 32);

        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnHuy);

        // Bảng dữ liệu
        String[] cols = {"Mã SP", "Tên món", "Giá bán (đ)", "Danh mục"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }

            // 💡 BƯỚC 1: Dạy cho cái bảng biết Cột 0 (Mã SP) là Số Nguyên (Integer) chứ không phải là Chữ
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : Object.class;
            }
        };
        table = new JTable(model);
        UIHelper.styleTable(table);
        UIHelper.canGiuaCot(table, 0);
        UIHelper.canPhaiCot(table, 2);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // 💡 BƯỚC 2: Ép hệ thống tự động sắp xếp Tăng dần (ASCENDING) theo Cột số 0 (Mã SP)
        sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));

        // Bấm vào bảng -> Hiển thị chi tiết sang cột Phải
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int mr = table.convertRowIndexToModel(row);
                String maSP = model.getValueAt(mr, 0).toString();

                lblMaSP.setText(maSP);
                lblMaSP.setForeground(new Color(200, 50, 50));
                txtTenMon.setText(model.getValueAt(mr, 1).toString());
                txtGiaBan.setText(model.getValueAt(mr, 2).toString().replace(",", "").replace(" đ","").trim());

                String dm = model.getValueAt(mr, 3).toString();
                for (int i = 0; i < cbDanhMuc.getItemCount(); i++) {
                    if (cbDanhMuc.getItemAt(i)[1].equals(dm)) { cbDanhMuc.setSelectedIndex(i); break; }
                }

                // Load cấu hình Size
                chkCoSize.setSelected("true".equals(sizeProps.getProperty(maSP, "false")));
                // Load hình ảnh
                hienThiAnhMon(maSP);
            }
        });

        // Auto filter khi gõ
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String kw = txtTimKiem.getText().trim();
                sorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + kw, 1));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        btnHuy.addActionListener(e -> txtTimKiem.setText(""));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        pnl.add(pnlSearch, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    // ════════════════════════════════════════════════════════════
    // 2. CỘT PHẢI: FORM CHI TIẾT & UPLOAD ẢNH
    // ════════════════════════════════════════════════════════════
    private JPanel buildCotPhai() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(220, 220, 220)));

        // --- KHU VỰC ẢNH MÓN ---
        JPanel pnlImage = new JPanel(new GridBagLayout());
        pnlImage.setBackground(Color.WHITE);
        pnlImage.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        lblAnhMon = new JLabel();
        lblAnhMon.setPreferredSize(new Dimension(160, 160));
        lblAnhMon.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 3, 2));
        lblAnhMon.setHorizontalAlignment(SwingConstants.CENTER);
        lblAnhMon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAnhMon.setToolTipText("Nhấn vào đây để tải ảnh lên");
        hienThiAnhMon("default"); // Load ảnh mặc định

        // Sự kiện Click chọn ảnh
        lblAnhMon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Chọn hình ảnh cho món nước");
                chooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh (JPG, PNG)", "jpg", "jpeg", "png"));
                if (chooser.showOpenDialog(QuanLySanPhamForm.this) == JFileChooser.APPROVE_OPTION) {
                    anhDangChon = chooser.getSelectedFile();
                    lblAnhMon.setIcon(loadAnh(anhDangChon.getAbsolutePath(), 155, 155));
                }
            }
        });

        pnlImage.add(lblAnhMon);

        // --- KHU VỰC NHẬP LIỆU ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 15, 8, 15);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0; g.gridx = 0; g.weightx = 0;
        form.add(UIHelper.taoLabel("Mã SP:"), g);
        g.gridx = 1; g.weightx = 1.0;
        lblMaSP = new JLabel("(Tự động)");
        lblMaSP.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMaSP.setForeground(Color.GRAY);
        form.add(lblMaSP, g);

        g.gridy = 1; g.gridx = 0; g.weightx = 0;
        form.add(UIHelper.taoLabel("Danh mục:"), g);
        g.gridx = 1;
        cbDanhMuc = new JComboBox<>();
        cbDanhMuc.setFont(UIHelper.F_BODY);
        cbDanhMuc.setPreferredSize(new Dimension(0, 34));
        form.add(cbDanhMuc, g);

        g.gridy = 2; g.gridx = 0; g.weightx = 0;
        form.add(UIHelper.taoLabel("Tên món:"), g);
        g.gridx = 1;
        txtTenMon = UIHelper.taoTextField(true);
        form.add(txtTenMon, g);

        g.gridy = 3; g.gridx = 0; g.weightx = 0;
        form.add(UIHelper.taoLabel("Giá bán (đ):"), g);
        g.gridx = 1;
        txtGiaBan = UIHelper.taoTextField(true);
        // Auto-format tiền tệ
        txtGiaBan.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                String text = txtGiaBan.getText().replaceAll("[^0-9]", "");
                if (!text.isEmpty()) {
                    try { txtGiaBan.setText(String.format("%,d", Long.parseLong(text))); } catch (Exception ignored) {}
                }
            }
        });
        form.add(txtGiaBan, g);

        g.gridy = 4; g.gridx = 0; g.weightx = 0;
        form.add(UIHelper.taoLabel("Tùy chọn:"), g);
        g.gridx = 1;
        chkCoSize = new JCheckBox("  Cho phép tùy chọn Size L (Cộng thêm giá)");
        chkCoSize.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkCoSize.setBackground(Color.WHITE);
        chkCoSize.setForeground(new Color(0, 100, 200));
        form.add(chkCoSize, g);

        // --- KHU VỰC NÚT BẤM (Dưới cùng) ---
        JPanel pnlBot = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlBot.setBackground(Color.WHITE);
        pnlBot.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnThem = UIHelper.taoNut("✨ Thêm mới", UIHelper.C_SUCCESS, 0, 42);
        JButton btnSua  = UIHelper.taoNut("💾 Lưu thay đổi", UIHelper.C_PRIMARY, 0, 42);
        JButton btnXoa  = UIHelper.taoNut("🗑 Xóa món", UIHelper.C_DANGER, 0, 42);
        JButton btnMoi  = UIHelper.taoNut("🔄 Làm mới", UIHelper.C_GRAY, 0, 42);

        btnThem.addActionListener(e -> them());
        btnSua .addActionListener(e -> sua());
        btnXoa .addActionListener(e -> xoa());
        btnMoi .addActionListener(e -> xoaForm());

        pnlBot.add(btnThem); pnlBot.add(btnSua);
        pnlBot.add(btnXoa);  pnlBot.add(btnMoi);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.add(pnlImage, BorderLayout.NORTH);
        pnlCenter.add(form, BorderLayout.CENTER);

        pnl.add(pnlCenter, BorderLayout.CENTER);
        pnl.add(pnlBot, BorderLayout.SOUTH);

        return pnl;
    }

    // ════════════════════════════════════════════════════════════
    // 3. LOGIC XỬ LÝ DATABASE & FILE
    // ════════════════════════════════════════════════════════════

    private void loadDanhMuc() {
        try {
            danhMucList = ctrl.layDanhMuc();
            cbDanhMuc.removeAllItems();
            for (String[] dm : danhMucList) cbDanhMuc.addItem(dm);
            cbDanhMuc.setRenderer((list, value, index, sel, focus) -> {
                JLabel l = new JLabel(value != null ? value[1] : "");
                l.setFont(UIHelper.F_BODY);
                l.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                return l;
            });
        } catch (SQLException e) { UIHelper.showError(this, "Lỗi tải danh mục: " + e.getMessage()); }
    }

    private void loadTable() {
        model.setRowCount(0);
        try {
            for (SanPham sp : ctrl.layDanhSach()) {
                model.addRow(new Object[]{ sp.getMaSP(), sp.getTenSP(), String.format("%,d", sp.getGiaBan()), sp.getTenDanhMuc() });
            }
        } catch (SQLException e) { UIHelper.showError(this, "Lỗi tải dữ liệu: " + e.getMessage()); }
    }

    private void them() {
        try {
            String ten = txtTenMon.getText().trim();
            int gia    = Integer.parseInt(txtGiaBan.getText().trim().replace(",",""));
            int maDM   = Integer.parseInt(((String[]) cbDanhMuc.getSelectedItem())[0]);
            if (ten.isEmpty()) { UIHelper.showError(this, "Tên món không được để trống!"); return; }

            // 1. Thêm vào DB
            ctrl.them(ten, gia, maDM);
            loadTable();

            // 2. Tìm mã SP vừa được tạo (Mã lớn nhất trong bảng)
            int newMaSP = -1;
            for(int i = 0; i < model.getRowCount(); i++) {
                int id = Integer.parseInt(model.getValueAt(i, 0).toString());
                if(id > newMaSP) newMaSP = id;
            }

            // 3. Lưu Ảnh và Cấu hình Size
            luuCauHinhAnhVaSize(String.valueOf(newMaSP));

            JOptionPane.showMessageDialog(this, "✅ Thêm món thành công!");
            xoaForm();
        } catch (NumberFormatException ex) { UIHelper.showError(this, "Giá bán không hợp lệ!"); }
        catch (SQLException ex)           { UIHelper.showError(this, "Lỗi: " + ex.getMessage()); }
    }

    private void sua() {
        if (table.getSelectedRow() < 0) { UIHelper.showError(this, "Vui lòng chọn món bên danh sách để sửa!"); return; }
        try {
            int    maSP = Integer.parseInt(lblMaSP.getText());
            String ten  = txtTenMon.getText().trim();
            int    gia  = Integer.parseInt(txtGiaBan.getText().trim().replace(",",""));

            ctrl.sua(maSP, ten, gia);
            luuCauHinhAnhVaSize(String.valueOf(maSP)); // Cập nhật lại ảnh và size

            loadTable();
            JOptionPane.showMessageDialog(this, "✅ Lưu thay đổi thành công!");
        } catch (NumberFormatException ex) { UIHelper.showError(this, "Giá bán không hợp lệ!"); }
        catch (SQLException ex)           { UIHelper.showError(this, "Lỗi: " + ex.getMessage()); }
    }

    private void xoa() {
        if (table.getSelectedRow() < 0) { UIHelper.showError(this, "Chọn món cần xóa!"); return; }
        if (!UIHelper.confirm(this, "Xóa món này khỏi menu?")) return;
        try {
            String maSP = lblMaSP.getText();
            ctrl.xoa(Integer.parseInt(maSP));

            // Xóa file cấu hình và ảnh đi kèm
            sizeProps.remove(maSP); saveSizeConfig();
            new File(DIR_ANH + maSP + ".jpg").delete();

            loadTable();
            xoaForm();
        } catch (SQLException e) { UIHelper.showError(this, "Lỗi: " + e.getMessage()); }
    }

    private void xoaForm() {
        lblMaSP.setText("(Tự động)"); lblMaSP.setForeground(Color.GRAY);
        txtTenMon.setText(""); txtGiaBan.setText("");
        chkCoSize.setSelected(false);
        hienThiAnhMon("default");
        table.clearSelection();
    }

    // ════════════════════════════════════════════════════════════
    // 4. HÀM HỖ TRỢ XỬ LÝ ẢNH & FILE PROPERTES
    // ════════════════════════════════════════════════════════════

    private void luuCauHinhAnhVaSize(String maSP) {
        // Lưu trạng thái Size
        sizeProps.setProperty(maSP, chkCoSize.isSelected() ? "true" : "false");
        saveSizeConfig();

        // Copy ảnh vào folder Resource nếu có chọn ảnh mới
        if (anhDangChon != null) {
            try {
                File dest = new File(DIR_ANH + maSP + ".jpg");
                Files.copy(anhDangChon.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                anhDangChon = null; // Xóa cache ảnh sau khi lưu
            } catch (IOException e) {
                System.err.println("Lỗi lưu ảnh: " + e.getMessage());
            }
        }
    }

    private void hienThiAnhMon(String maSP) {
        File f = new File(DIR_ANH + maSP + ".jpg");
        if (f.exists()) {
            lblAnhMon.setIcon(loadAnh(f.getAbsolutePath(), 155, 155));
            lblAnhMon.setText("");
        } else {
            lblAnhMon.setIcon(null);
            lblAnhMon.setText("📸 Nhấn để tải ảnh");
            lblAnhMon.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblAnhMon.setForeground(Color.GRAY);
        }
        anhDangChon = null;
    }

    // Hàm load ảnh an toàn (tránh lỗi null do lấy từ thư mục ngoài của project)
    private ImageIcon loadAnh(String path, int w, int h) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) { return new ImageIcon(); }
    }

    private void loadSizeConfig() {
        try {
            File f = new File(FILE_SIZE);
            if (f.exists()) sizeProps.load(new FileInputStream(f));
        } catch (Exception ignored) {}
    }

    private void saveSizeConfig() {
        try { sizeProps.store(new FileOutputStream(FILE_SIZE), "Cau hinh Size"); }
        catch (Exception ignored) {}
    }
}