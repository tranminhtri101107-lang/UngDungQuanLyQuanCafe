package View;

import Controller.ExportController;
import Model.ChiTietHoaDon;
import Model.SanPham;
import Network.NotificationClient;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Màn hình chính — layout 3 cột:
 * Tích hợp logic: GỘP BÀN, CHUYỂN MÓN LẺ, TÁCH BILL THANH TOÁN
 */
public class BanHangForm extends JPanel {

    final int    maTK;
    final int    maVaiTro;
    final String tenNhanVien;

    static final Color C_BAN_TRONG    = Color.WHITE;
    static final Color C_BAN_COKHACH  = new Color(255, 80,  80);
    static final Color C_HEADER_BAN   = new Color(50,  50,  90);
    static final Color C_BTN_CHON     = new Color(0,   150, 60);
    static final Color C_BTN_THANHTOAN= new Color(0,   100, 200);
    static final Color C_BG           = new Color(235, 235, 240);

    final Map<String, DefaultTableModel>    duLieuBan   = new HashMap<>();
    final Map<String, java.time.LocalDateTime> thoiGianMoBan = new HashMap<>();
    final Map<String, JPanel>               cardBan     = new HashMap<>();

    final java.util.List<JPanel>            listGridKhuVuc = new java.util.ArrayList<>();

    // Đã đưa togGiamTT và txtGiamTT lên thành thuộc tính của lớp để hàm capNhatTong có thể đọc được
    JToggleButton                      togTrong, togCoKhach, btnDonBan, togGiamTT;
    JLabel                             lblTenBan, lblGioMo;
    JTable                             tblOrder;
    DefaultTableModel                  modelOrder;
    JTextField                         txtGiamPct, txtPhuThu, txtKhachDua, txtGiamTT;
    JLabel                             lblTongHoaDon, lblThanhToan, lblTienThua;
    JButton                            btnMoBan, btnCapNhat, btnHuyOrder, btnThanhToan;
    JComboBox<String>                  cbChuyenBan, cbPhuongThuc;
    String                             banDangChon = null;

    JPanel                             pnlGiaoDienBanHang;
    final NotificationClient notifClient = new NotificationClient();

    public BanHangForm(int maTK, int maVaiTro, String tenNhanVien) {
        this.maTK        = maTK;
        this.maVaiTro    = maVaiTro;
        this.tenNhanVien = tenNhanVien;

        setLayout(new BorderLayout());
        setBackground(C_BG);

        JPanel pnlTrai = buildCotTrai();
        pnlTrai.setPreferredSize(new Dimension(400, 0));

        JPanel pnlPhai = buildCotPhai();
        pnlPhai.setPreferredSize(new Dimension(440, 0));

        JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlTrai, buildCotGiua());
        split1.setDividerSize(2); // Thu nhỏ viền phân cách cho tinh tế
        split1.setBorder(null);
        split1.setResizeWeight(0.0);
        split1.setEnabled(false);

        JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split1, pnlPhai);
        split2.setDividerSize(4);
        split2.setBorder(null);
        split2.setResizeWeight(1.0);

        pnlGiaoDienBanHang = new JPanel(new BorderLayout());
        pnlGiaoDienBanHang.add(split2, BorderLayout.CENTER);
        add(pnlGiaoDienBanHang, BorderLayout.CENTER);

        notifClient.ketNoi(msg ->
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }));
    }

    private JPanel buildCotTrai() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(C_BG);

        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.setBackground(new Color(245, 245, 245));
        pnlTop.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,new Color(200,200,200)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblDs = new JLabel("Sơ đồ bàn");
        lblDs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDs.setForeground(new Color(40, 40, 40));

        JPanel pnlSwitches = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlSwitches.setOpaque(false);

        // Bỏ nút Thu gọn, chỉ giữ lại 2 nút Lọc trạng thái
        togTrong   = taoCustomSwitch(true);
        togCoKhach = taoCustomSwitch(true);

        togTrong.addActionListener(e -> { togTrong.repaint(); locBan(); });
        togCoKhach.addActionListener(e -> { togCoKhach.repaint(); locBan(); });

        pnlSwitches.add(taoCumSwitch("Trống (Trắng)", togTrong));
        pnlSwitches.add(taoCumSwitch("Có khách (Đỏ)", togCoKhach));

        pnlTop.add(lblDs, BorderLayout.NORTH);
        pnlTop.add(pnlSwitches, BorderLayout.CENTER);

        // --- DÙNG THANH TAB ĐỂ HIỂN THỊ CÁC KHU VỰC ---
        JTabbedPane tabKhuVuc = new JTabbedPane();
        tabKhuVuc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabKhuVuc.setBackground(Color.WHITE);
        tabKhuVuc.setFocusable(false);
        tabKhuVuc.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        tabKhuVuc.addTab("  VIP  ", taoGridKhuVuc("VIP ", 1, 6));
        tabKhuVuc.addTab("  TẦNG TRỆT  ", taoGridKhuVuc("BÀN ", 1, 6));
        tabKhuVuc.addTab("  SÂN VƯỜN  ", taoGridKhuVuc("SÂN ", 1, 4));
        tabKhuVuc.addTab("  TẦNG LẦU  ", taoGridKhuVuc("LẦU ", 1, 4));

        pnl.add(pnlTop, BorderLayout.NORTH);
        pnl.add(tabKhuVuc, BorderLayout.CENTER);
        return pnl;
    }

    // Hàm mới tạo lưới thẻ bàn cho từng Tab
    private JScrollPane taoGridKhuVuc(String prefix, int tu, int den) {
        JPanel grid = new JPanel(new GridLayout(0, 3, 8, 8));
        grid.setBackground(C_BG);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = tu; i <= den; i++) {
            String tenBan = prefix + i;
            JPanel card = buildCardBan(tenBan);
            cardBan.put(tenBan, card);
            grid.add(card);
        }
        listGridKhuVuc.add(grid);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(C_BG);
        container.add(grid, BorderLayout.NORTH); // Ép thẻ nổi lên trên, ko bị giãn dọc

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildCardBan(String tenBan) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(Color.WHITE); // Mặc định bàn trống nền Trắng
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,210,210), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(85, 80));

        JLabel lblIcon = new JLabel(
                UIHelper.resizeIcon("/Resource/coffee.png", 32, 32),
                SwingConstants.CENTER
        );

        JLabel lblTen = new JLabel(tenBan, SwingConstants.CENTER);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTen.setForeground(new Color(60, 60, 60));

        // Đã gỡ bỏ cái nhãn "Badge" thừa thãi bên dưới thẻ

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { chonBan(tenBan, card, null); }
            @Override public void mouseEntered(MouseEvent e) {
                if (!tenBan.equals(banDangChon)) {
                    card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_BTN_THANHTOAN, 1), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!tenBan.equals(banDangChon)) {
                    card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(210,210,210), 1), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                }
            }
        });

        card.add(lblIcon, BorderLayout.CENTER);
        card.add(lblTen, BorderLayout.SOUTH);
        return card;
    }

    JPanel buildCotGiua() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createMatteBorder(0,1,0,1,new Color(200,200,200)));

        // HEADER: Tìm kiếm
        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setBackground(new Color(245,245,245));
        pnlSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,new Color(220,220,220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JTextField txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", " Nhập tên món cần tìm...");
        txtSearch.setPreferredSize(new Dimension(0, 36));

        JComboBox<String> cbDanhMuc = new JComboBox<>(new String[]{"Tất cả"});
        cbDanhMuc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbDanhMuc.setPreferredSize(new Dimension(120, 36));
        cbDanhMuc.setBackground(Color.WHITE);

        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(cbDanhMuc, BorderLayout.EAST);

        // BODY: Lưới Thẻ Món Ăn (Grid)
        JPanel pnlMenuGrid = new JPanel(new GridLayout(0, 3, 12, 12)); // Chia 3 cột, khoảng cách 12px
        pnlMenuGrid.setBackground(Color.WHITE);
        pnlMenuGrid.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Bọc Grid trong một Panel phụ để thẻ không bị kéo dài thòng lòng khi ít món
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.add(pnlMenuGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(pnlWrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // LOAD DATA NGẦM:
        new javax.swing.SwingWorker<java.util.List<SanPham>, Void>() {
            @Override protected java.util.List<SanPham> doInBackground() throws Exception {
                return new Controller.SanPhamController().layDanhSach();
            }
            @Override protected void done() {
                try {
                    java.util.List<SanPham> dsSP = get();
                    java.util.LinkedHashSet<String> dms = new java.util.LinkedHashSet<>();
                    dsSP.forEach(sp -> dms.add(sp.getTenDanhMuc()));
                    dms.forEach(cbDanhMuc::addItem);

                    // Logic render lại thẻ món mỗi khi gõ tìm kiếm hoặc lọc danh mục
                    Runnable renderMenu = () -> {
                        pnlMenuGrid.removeAll();
                        String dm = cbDanhMuc.getSelectedItem().toString();
                        String kw = txtSearch.getText().trim().toLowerCase();

                        dsSP.stream()
                                .filter(sp -> (dm.equals("Tất cả") || sp.getTenDanhMuc().equals(dm)))
                                .filter(sp -> sp.getTenSP().toLowerCase().contains(kw))
                                .forEach(sp -> pnlMenuGrid.add(taoTheMonAn(sp)));

                        pnlMenuGrid.revalidate();
                        pnlMenuGrid.repaint();
                    };

                    renderMenu.run(); // Render lần đầu tiên

                    cbDanhMuc.addActionListener(e -> renderMenu.run());
                    txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                        public void insertUpdate(javax.swing.event.DocumentEvent e) { renderMenu.run(); }
                        public void removeUpdate(javax.swing.event.DocumentEvent e) { renderMenu.run(); }
                        public void changedUpdate(javax.swing.event.DocumentEvent e) { renderMenu.run(); }
                    });

                } catch (Exception ex) { JOptionPane.showMessageDialog(BanHangForm.this, "Lỗi tải Menu: " + ex.getMessage()); }
            }
        }.execute();

        pnl.add(pnlSearch, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    // Hàm sinh giao diện 1 thẻ món ăn (Hình vuông)
    private JPanel taoTheMonAn(SanPham sp) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 8, 10, 8)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 💡 1. LOGIC LOAD ẢNH ĐỘNG TỪ THƯ MỤC
        String imagePath = "src/Resource/Drinks/" + sp.getMaSP() + ".jpg";
        File imgFile = new File(imagePath);
        Icon icon;
        if (imgFile.exists()) {
            // Nếu tìm thấy ảnh bạn vừa lưu -> Lấy ra và thu nhỏ lại kích thước 45x45
            ImageIcon originalIcon = new ImageIcon(imgFile.getAbsolutePath());
            Image img = originalIcon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        } else {
            // Nếu món nào chưa có ảnh thì vẫn dùng ảnh cốc mặc định
            icon = UIHelper.resizeIcon("/Resource/menu.png", 45, 45);
        }
        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);

        // Dùng thẻ HTML để tự động xuống hàng nếu tên món quá dài
        JLabel lblTen = new JLabel("<html><center>" + sp.getTenSP() + "</center></html>", SwingConstants.CENTER);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTen.setForeground(new Color(40, 40, 40));
        lblTen.setPreferredSize(new Dimension(80, 36));

        JLabel lblGia = new JLabel(String.format("%,d đ", sp.getGiaBan()), SwingConstants.CENTER);
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGia.setForeground(C_BTN_CHON);

        card.add(lblIcon, BorderLayout.NORTH);
        card.add(lblTen, BorderLayout.CENTER);
        card.add(lblGia, BorderLayout.SOUTH);

        // Sự kiện Click chọn món
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_BTN_CHON, 2), BorderFactory.createEmptyBorder(9,7,9,7))); }
            @Override public void mouseExited(MouseEvent e) { card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220), 1), BorderFactory.createEmptyBorder(10,8,10,8))); }
            @Override public void mouseClicked(MouseEvent e) {
                if (banDangChon == null) {
                    JOptionPane.showMessageDialog(BanHangForm.this, "Vui lòng chọn bàn trước khi gọi món!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 💡 2. LOGIC KIỂM TRA MÓN CÓ ĐƯỢC CHỌN SIZE HAY KHÔNG
                boolean coSize = false;
                try {
                    java.util.Properties sizeProps = new java.util.Properties();
                    File f = new File("size_config.properties");
                    if (f.exists()) {
                        sizeProps.load(new java.io.FileInputStream(f));
                        // Đọc file xem mã SP này có được tick chọn Size L lúc quản lý không
                        coSize = "true".equals(sizeProps.getProperty(String.valueOf(sp.getMaSP()), "false"));
                    }
                } catch (Exception ignored) {}

                if (coSize) {
                    // Nếu món CÓ TÙY CHỌN SIZE -> Hiện form Popup cho phép đổi Size và Số lượng
                    TuyChinhMonDialog dialog = new TuyChinhMonDialog(SwingUtilities.getWindowAncestor(BanHangForm.this), sp);
                    dialog.setVisible(true);

                    if (dialog.isXacNhan()) {
                        themMonVaoOrder(sp.getMaSP(), dialog.getTenMonCustom(), dialog.getGiaCustom(), dialog.getSoLuong());
                    }
                } else {
                    // Nếu món BÌNH THƯỜNG -> Ném thẳng vào hóa đơn ngay lập tức với số lượng = 1
                    themMonVaoOrder(sp.getMaSP(), sp.getTenSP(), sp.getGiaBan(), 1);
                }
            }
        });
        return card;
    }

    // Hàm thêm món đã được nâng cấp để nhận các tham số custom từ Popup
    private void themMonVaoOrder(int maSP, String tenMon, int giaBan, int soLuongThem) {
        if (banDangChon == null) return;

        // Nếu món (cùng size) đã tồn tại trong bill -> Cộng dồn số lượng
        for (int i=0; i<modelOrder.getRowCount(); i++) {
            if (modelOrder.getValueAt(i, 1).toString().equals(tenMon)) {
                int slCu = Integer.parseInt(modelOrder.getValueAt(i, 2).toString());
                modelOrder.setValueAt(slCu + soLuongThem, i, 2);
                capNhatTong();
                return;
            }
        }

        // Nếu món chưa có -> Thêm dòng mới
        String giaStr = String.format("%,d", giaBan);
        modelOrder.addRow(new Object[]{false, tenMon, soLuongThem, giaStr, giaStr, "✕", maSP});
        capNhatTong();
    }

    JPanel buildCotPhai() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        // --- 1. HEADER: INFO BÀN & NÚT DỌN/CHUYỂN ---
        JPanel pnlBanInfo = new JPanel(new BorderLayout());
        pnlBanInfo.setBackground(new Color(245,245,245));
        pnlBanInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,new Color(200,200,200)),
                BorderFactory.createEmptyBorder(10,15,10,15)));

        JPanel pnlTenBan = new JPanel(new GridLayout(2,1));
        pnlTenBan.setOpaque(false);
        lblTenBan = new JLabel("Chưa chọn bàn");
        lblTenBan.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblGioMo = new JLabel("...");
        lblGioMo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGioMo.setForeground(Color.GRAY);
        pnlTenBan.add(lblTenBan); pnlTenBan.add(lblGioMo);

        JPanel pnlActionBan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlActionBan.setOpaque(false);

        btnDonBan = new JToggleButton("🧹 Dọn");
        btnDonBan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDonBan.setFocusPainted(false);

        cbChuyenBan = new JComboBox<>();
        for(int i=1; i<=6; i++) cbChuyenBan.addItem("VIP " + i);
        for(int i=1; i<=6; i++) cbChuyenBan.addItem("BÀN " + i);
        for(int i=1; i<=4; i++) cbChuyenBan.addItem("SÂN " + i);
        for(int i=1; i<=4; i++) cbChuyenBan.addItem("LẦU " + i);
        cbChuyenBan.setPreferredSize(new Dimension(85, 26));

        JButton btnChuyen = new JButton("Chuyển");
        btnChuyen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChuyen.setBackground(new Color(200,50,50));
        btnChuyen.setForeground(Color.WHITE);
        btnChuyen.setFocusPainted(false);

        pnlActionBan.add(btnDonBan);
        pnlActionBan.add(cbChuyenBan);
        pnlActionBan.add(btnChuyen);

        pnlBanInfo.add(pnlTenBan, BorderLayout.WEST);
        pnlBanInfo.add(pnlActionBan, BorderLayout.EAST);

        // --- 2. CENTER: BẢNG HÓA ĐƠN ---
        String[] cols = {"[ ]","MÓN","SL","Đ.GIÁ","T.TIỀN","X", "MaSP"};
        modelOrder = new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : Object.class; }
            @Override public boolean isCellEditable(int r,int c) { return c==0 || c==2; }
        };
        tblOrder = new JTable(modelOrder);
        tblOrder.setFont(new Font("Segoe UI",Font.PLAIN,13));
        tblOrder.setRowHeight(30);
        tblOrder.setShowVerticalLines(false);
        tblOrder.setGridColor(new Color(230,230,230));
        tblOrder.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        tblOrder.getTableHeader().setBackground(new Color(50,50,90));
        tblOrder.getTableHeader().setForeground(Color.WHITE);
        tblOrder.getTableHeader().setPreferredSize(new Dimension(0,32));
        tblOrder.getTableHeader().setReorderingAllowed(false);
        tblOrder.getTableHeader().setResizingAllowed(false);

        int[] ws = {35, 175, 45, 80, 90, 30, 0};
        for (int i=0;i<ws.length;i++) {
            tblOrder.getColumnModel().getColumn(i).setPreferredWidth(ws[i]);
            if (i==0||i==5||i==6) tblOrder.getColumnModel().getColumn(i).setMaxWidth(ws[i]);
        }
        tblOrder.getColumnModel().getColumn(6).setMinWidth(0);

        tblOrder.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c) {
                JLabel l=new JLabel("✕",SwingConstants.CENTER);
                l.setForeground(Color.RED); l.setFont(new Font("Segoe UI",Font.BOLD,14));
                return l;
            }
        });

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem miTach = new JMenuItem("✂  Tách số lượng ra dòng riêng");
        miTach.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        popupMenu.add(miTach);
        tblOrder.setComponentPopupMenu(popupMenu);

        popupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                int row = tblOrder.getSelectedRow();
                if (row >= 0) {
                    try { miTach.setEnabled(Integer.parseInt(modelOrder.getValueAt(row, 2).toString()) > 1); }
                    catch (Exception ex) { miTach.setEnabled(false); }
                } else { miTach.setEnabled(false); }
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        miTach.addActionListener(e -> {
            int row = tblOrder.getSelectedRow();
            if (row < 0) return;
            int currentSL = Integer.parseInt(modelOrder.getValueAt(row, 2).toString());
            String input = JOptionPane.showInputDialog(BanHangForm.this, "Nhập số lượng muốn tách ra để tính tiền riêng:\n(Đang có: " + currentSL + ")", "1");
            if (input != null && input.matches("\\d+")) {
                int splitSL = Integer.parseInt(input);
                if (splitSL > 0 && splitSL < currentSL) {
                    modelOrder.setValueAt(currentSL - splitSL, row, 2);
                    String ten = modelOrder.getValueAt(row, 1).toString();
                    String gia = modelOrder.getValueAt(row, 3).toString();
                    long donGia = Long.parseLong(gia.replaceAll("[^0-9]", ""));
                    String ttMoi = String.format("%,d", donGia * splitSL);
                    int maSP = Integer.parseInt(modelOrder.getValueAt(row, 6).toString());
                    modelOrder.addRow(new Object[]{false, ten, splitSL, gia, ttMoi, "✕", maSP});
                    capNhatTong(); salveOrderVaBan();
                } else { JOptionPane.showMessageDialog(BanHangForm.this, "Số lượng không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE); }
            }
        });

        tblOrder.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = tblOrder.rowAtPoint(e.getPoint());
                    if (row >= 0) tblOrder.setRowSelectionInterval(row, row);
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                int row = tblOrder.rowAtPoint(e.getPoint());
                int col = tblOrder.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 5 && SwingUtilities.isLeftMouseButton(e)) {
                    modelOrder.removeRow(row); capNhatTong(); salveOrderVaBan();
                }
            }
        });

        modelOrder.addTableModelListener(ev -> {
            if (ev.getColumn()==2) { capNhatThanhTien(ev.getFirstRow()); }
            if (ev.getColumn()==0) { capNhatTong(); }
        });

        JScrollPane scrollOrder = new JScrollPane(tblOrder);
        scrollOrder.setBorder(BorderFactory.createEmptyBorder());

        // --- 3. BOTTOM: KHU VỰC THANH TOÁN MỚI ---
        JPanel pnlTT = new JPanel(new GridBagLayout());
        pnlTT.setBackground(Color.WHITE);
        pnlTT.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        GridBagConstraints gg = new GridBagConstraints();
        gg.insets=new Insets(6,5,6,5);
        gg.fill = GridBagConstraints.HORIZONTAL;

        // Dòng 0: Tổng tiền món
        gg.gridx=0; gg.gridy=0; gg.gridwidth=2;
        JLabel lblT1 = new JLabel("Tổng tiền hàng:");
        lblT1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlTT.add(lblT1, gg);

        gg.gridx=2; gg.gridwidth=2;
        lblTongHoaDon = new JLabel("0 đ", SwingConstants.RIGHT);
        lblTongHoaDon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlTT.add(lblTongHoaDon, gg);

        // Dòng 1: Giảm giá & Phụ thu
        gg.gridwidth=1;
        gg.gridx=0; gg.gridy=1; gg.weightx=0.2;
        JLabel lblGiam = new JLabel("Chiết khấu (%):"); lblGiam.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlTT.add(lblGiam, gg);

        gg.gridx=1; gg.weightx=0.3;
        txtGiamPct = new JTextField("0"); txtGiamPct.setHorizontalAlignment(JTextField.RIGHT);
        pnlTT.add(txtGiamPct, gg);

        gg.gridx=2; gg.weightx=0.2;
        JLabel lblPhuThu = new JLabel("Phụ thu (đ):"); lblPhuThu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlTT.add(lblPhuThu, gg);

        gg.gridx=3; gg.weightx=0.3;
        txtPhuThu = new JTextField("0"); txtPhuThu.setHorizontalAlignment(JTextField.RIGHT);
        txtPhuThu.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                String text = txtPhuThu.getText().replaceAll("[^0-9]", "");
                if (!text.isEmpty()) {
                    try { txtPhuThu.setText(String.format("%,d", Long.parseLong(text))); } catch (Exception ignored) {}
                }
            }
        });
        pnlTT.add(txtPhuThu, gg);

        // Dòng 2: Đường gạch ngang
        gg.gridx=0; gg.gridy=2; gg.gridwidth=4;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220,220,220));
        pnlTT.add(sep, gg);

        // Dòng 3: Khách cần trả
        gg.gridx=0; gg.gridy=3; gg.gridwidth=2;
        JLabel lThanhToan = new JLabel("KHÁCH CẦN TRẢ:");
        lThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        pnlTT.add(lThanhToan, gg);

        gg.gridx=2; gg.gridwidth=2;
        lblThanhToan = new JLabel("0 đ", SwingConstants.RIGHT);
        lblThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblThanhToan.setForeground(new Color(200, 50, 50));
        pnlTT.add(lblThanhToan, gg);

        // Dòng 4: Khách đưa
        gg.gridx=0; gg.gridy=4; gg.gridwidth=1;
        JLabel lblKD = new JLabel("Khách đưa:"); lblKD.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlTT.add(lblKD, gg);

        gg.gridx=1; gg.gridwidth=3;
        txtKhachDua = new JTextField("0");
        txtKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtKhachDua.setHorizontalAlignment(JTextField.RIGHT);
        txtKhachDua.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                String text = txtKhachDua.getText().replaceAll("[^0-9]", "");
                if (!text.isEmpty()) {
                    try { txtKhachDua.setText(String.format("%,d", Long.parseLong(text))); } catch (Exception ignored) {}
                }
            }
        });
        txtKhachDua.addCaretListener(e -> tinhTienThua());
        pnlTT.add(txtKhachDua, gg);

        // Dòng 5: Gợi ý tiền nhanh (Quick Cash)
        gg.gridx=0; gg.gridy=5; gg.gridwidth=4;
        JPanel pnlQuickCash = new JPanel(new GridLayout(1, 5, 6, 0));
        pnlQuickCash.setOpaque(false);
        String[] tienNhanh = {"Đủ tiền", "50k", "100k", "200k", "500k"};
        long[] giaTriNhanh = {0, 50000, 100000, 200000, 500000};

        for (int i=0; i<tienNhanh.length; i++) {
            JButton btnQC = new JButton(tienNhanh[i]);
            btnQC.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnQC.setForeground(new Color(60,60,60));
            btnQC.setMargin(new Insets(4, 2, 4, 2));
            btnQC.setFocusPainted(false);
            btnQC.setBackground(new Color(235, 235, 240));
            btnQC.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int index = i;
            btnQC.addActionListener(e -> {
                if (index == 0) { txtKhachDua.setText(lblThanhToan.getText().replaceAll("[^0-9]", "")); }
                else { txtKhachDua.setText(String.format("%,d", giaTriNhanh[index])); }
                tinhTienThua();
            });
            pnlQuickCash.add(btnQC);
        }
        pnlTT.add(pnlQuickCash, gg);

        // Dòng 6: Tiền thừa
        gg.gridx=0; gg.gridy=6; gg.gridwidth=2;
        JLabel lblTT = new JLabel("Tiền thừa:"); lblTT.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlTT.add(lblTT, gg);

        gg.gridx=2; gg.gridwidth=2;
        lblTienThua = new JLabel("0 đ", SwingConstants.RIGHT);
        lblTienThua.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTienThua.setForeground(new Color(0, 150, 60));
        pnlTT.add(lblTienThua, gg);

        // Dòng 7: Nút thanh toán
        gg.gridx=0; gg.gridy=7; gg.gridwidth=4; gg.insets=new Insets(15,0,0,0);
        JPanel pnlBtnTT = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlBtnTT.setOpaque(false);

        JButton btnTienMat = new JButton("💵 TIỀN MẶT");
        btnTienMat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTienMat.setBackground(new Color(0, 150, 60));
        btnTienMat.setForeground(Color.WHITE);
        btnTienMat.setFocusPainted(false);
        btnTienMat.setPreferredSize(new Dimension(0, 45));
        btnTienMat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnCK = new JButton("💳 CHUYỂN KHOẢN");
        btnCK.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCK.setBackground(new Color(0, 100, 200));
        btnCK.setForeground(Color.WHITE);
        btnCK.setFocusPainted(false);
        btnCK.setPreferredSize(new Dimension(0, 45));
        btnCK.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnTienMat.addActionListener(e -> thanhToan("TIỀN MẶT"));
        btnCK.addActionListener(e -> thanhToan("CHUYỂN KHOẢN"));

        pnlBtnTT.add(btnTienMat);
        pnlBtnTT.add(btnCK);
        pnlTT.add(pnlBtnTT, gg);

        // Ráp khung
        JPanel pnlNutTren = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        pnlNutTren.setBackground(Color.WHITE);
        pnlNutTren.setBorder(BorderFactory.createMatteBorder(1,0,1,0,new Color(220,220,220)));

        btnMoBan    = mkBtn2("✔  Mở bàn",         new Color(0,150,60),   Color.WHITE, 110);
        btnCapNhat  = mkBtn2("✔  Cập nhật",        new Color(0,150,60),   Color.WHITE, 110);
        btnHuyOrder = mkBtn2("✖  Hủy order",       new Color(200,50,50),  Color.WHITE, 110);

        btnCapNhat.setVisible(false);
        pnlNutTren.add(btnMoBan); pnlNutTren.add(btnCapNhat); pnlNutTren.add(btnHuyOrder);

        JPanel pnlOrderArea = new JPanel(new BorderLayout());
        pnlOrderArea.add(pnlNutTren, BorderLayout.NORTH);
        pnlOrderArea.add(scrollOrder, BorderLayout.CENTER);

        pnl.add(pnlBanInfo, BorderLayout.NORTH);
        pnl.add(pnlOrderArea, BorderLayout.CENTER);
        pnl.add(pnlTT, BorderLayout.SOUTH);

        bindCotPhaiEvents(btnChuyen);
        return pnl;
    }

    private void bindCotPhaiEvents(JButton btnChuyen) {
        btnMoBan.addActionListener(e -> moBan());
        btnCapNhat.addActionListener(e -> capNhatOrder());
        btnHuyOrder.addActionListener(e -> huyOrder());
        btnDonBan.addActionListener(e -> donBan());
        btnChuyen.addActionListener(e -> chuyenBan());

        txtGiamPct.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { capNhatTong(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { capNhatTong(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        txtPhuThu.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { capNhatTong(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { capNhatTong(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
    }

    private void chuyenBan() {
        if (banDangChon == null || !duLieuBan.containsKey(banDangChon)) {
            JOptionPane.showMessageDialog(this, "Bàn hiện tại đang trống!"); return;
        }
        String banMoi = cbChuyenBan.getSelectedItem().toString();
        if (banDangChon.equals(banMoi)) {
            JOptionPane.showMessageDialog(this, "Không thể chuyển sang chính bàn hiện tại!"); return;
        }

        java.util.List<Integer> selectedRows = new java.util.ArrayList<>();
        for (int i=0; i<modelOrder.getRowCount(); i++) {
            if ((Boolean)modelOrder.getValueAt(i,0)) selectedRows.add(i);
        }
        boolean chuyenToanBo = selectedRows.isEmpty();

        String msg = chuyenToanBo ? "Gộp / Chuyển TOÀN BỘ sang " + banMoi + "?" : "Tách các món ĐÃ CHỌN sang " + banMoi + "?";
        if (!UIHelper.confirm(this, msg)) return;

        DefaultTableModel orderBanMoi = duLieuBan.get(banMoi);
        if (orderBanMoi == null) {
            orderBanMoi = new DefaultTableModel(new String[]{"[ ]","MÓN","SL","GIÁ","TT","X","MaSP"},0);
            thoiGianMoBan.put(banMoi, java.time.LocalDateTime.now());
            duLieuBan.put(banMoi, orderBanMoi);
        }

        if (chuyenToanBo) {
            for (int i=0; i<modelOrder.getRowCount(); i++) mergeRowToTable(orderBanMoi, i);
            lamSachBan(banDangChon);
        } else {
            for (int i = selectedRows.size()-1; i>=0; i--) {
                mergeRowToTable(orderBanMoi, selectedRows.get(i));
                modelOrder.removeRow(selectedRows.get(i));
            }
            salveOrderVaBan();
            capNhatTong();
            if (modelOrder.getRowCount() == 0) lamSachBan(banDangChon);
        }

        capNhatCardBan(banMoi, true);
        locBan();
        JOptionPane.showMessageDialog(this, "✅ Chuyển thành công!");
        chonBan(banMoi, cardBan.get(banMoi), null);
    }

    private void mergeRowToTable(DefaultTableModel dest, int srcRow) {
        String ten = modelOrder.getValueAt(srcRow,1).toString();
        int sl = Integer.parseInt(modelOrder.getValueAt(srcRow,2).toString());
        String gia = modelOrder.getValueAt(srcRow,3).toString();
        int maSP = Integer.parseInt(modelOrder.getValueAt(srcRow,6).toString());

        boolean found = false;
        for (int i=0; i<dest.getRowCount(); i++) {
            if (dest.getValueAt(i,1).toString().equals(ten)) {
                int slCu = Integer.parseInt(dest.getValueAt(i,2).toString());
                dest.setValueAt(slCu + sl, i, 2);
                long g = Long.parseLong(gia.replaceAll("[^0-9]",""));
                dest.setValueAt(String.format("%,d", (slCu + sl) * g), i, 4);
                found = true; break;
            }
        }
        if (!found) {
            dest.addRow(new Object[]{false, ten, sl, gia, modelOrder.getValueAt(srcRow,4), "✕", maSP});
        }
    }

    private void thanhToan(String pt) {
        if (banDangChon==null || modelOrder.getRowCount()==0) {
            JOptionPane.showMessageDialog(this,"Chưa có món nào!"); return;
        }

        java.util.List<Integer> selectedRows = new java.util.ArrayList<>();
        for(int i=0; i<modelOrder.getRowCount(); i++) {
            if((Boolean)modelOrder.getValueAt(i,0)) selectedRows.add(i);
        }
        boolean thanhToanToanBo = selectedRows.isEmpty();

        java.util.List<ChiTietHoaDon> ds = new java.util.ArrayList<>();

        int loopCount = thanhToanToanBo ? modelOrder.getRowCount() : selectedRows.size();
        for(int i=0; i<loopCount; i++) {
            int r = thanhToanToanBo ? i : selectedRows.get(i);
            String ten = modelOrder.getValueAt(r,1).toString();
            int sl = Integer.parseInt(modelOrder.getValueAt(r,2).toString());
            int gia = Integer.parseInt(modelOrder.getValueAt(r,3).toString().replaceAll("[^0-9]",""));
            int maSP = Integer.parseInt(modelOrder.getValueAt(r,6).toString());

            ds.add(new ChiTietHoaDon(0,0,maSP,ten,sl,gia));
        }

        long tongTienThucTe = Long.parseLong(lblThanhToan.getText().replaceAll("[^0-9]",""));

        if (pt.equals("TIỀN MẶT")) {
            long khachDua = 0;
            try { khachDua = Long.parseLong(txtKhachDua.getText().replaceAll("[^0-9]","")); } catch(Exception e){}
            if (khachDua > 0 && khachDua < tongTienThucTe) {
                JOptionPane.showMessageDialog(this,"Tiền khách đưa không đủ!"); return;
            }
        }

        String msg = thanhToanToanBo ? "Thanh toán TOÀN BỘ " + banDangChon + "?" : "Thanh toán hóa đơn TÁCH LẺ của " + banDangChon + "?";
        if (JOptionPane.showConfirmDialog(this, msg + "\nTổng: " + String.format("%,d đ", tongTienThucTe) + "\nHình thức: " + pt, "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try {
            int maHD = new Controller.HoaDonController().thanhToan(maTK, banDangChon, ds, pt);
            String path = ExportController.xuatHoaDon(maHD, banDangChon, ds, tongTienThucTe, pt, tenNhanVien);
            ExportController.ghiLog("TT #"+maHD+"|"+banDangChon+"|"+tongTienThucTe+"đ|"+pt);
            notifClient.guiThongBaoThanhToan(banDangChon, tongTienThucTe, tenNhanVien, pt);

            if (thanhToanToanBo) {
                lamSachBan(banDangChon);
            } else {
                for (int i=selectedRows.size()-1; i>=0; i--) {
                    modelOrder.removeRow(selectedRows.get(i));
                }
                salveOrderVaBan();
                capNhatTong();
                if (modelOrder.getRowCount() == 0) lamSachBan(banDangChon);
            }
            JOptionPane.showMessageDialog(this, "✅ Thanh toán thành công!\nMã HĐ: #"+maHD+"\n"+path);
        } catch(Exception e) { UIHelper.showError(this,"Lỗi: "+e.getMessage()); }
    }

    private void locBan() {
        if (togTrong == null || togCoKhach == null) return;
        for (String tenB : cardBan.keySet()) {
            boolean coKhach = duLieuBan.containsKey(tenB);
            cardBan.get(tenB).setVisible(coKhach ? togCoKhach.isSelected() : togTrong.isSelected());
        }
        revalidate(); repaint();
    }

    private void donBan() {
        if (banDangChon == null) return;
        if (!duLieuBan.containsKey(banDangChon)) {
            JOptionPane.showMessageDialog(this, "Bàn này đang trống!"); btnDonBan.setSelected(false); return;
        }
        if (UIHelper.confirm(this, "Hủy hóa đơn hiện tại và dọn " + banDangChon + "?")) {
            lamSachBan(banDangChon); JOptionPane.showMessageDialog(this, "Đã dọn sạch " + banDangChon + "!");
        }
        btnDonBan.setSelected(false);
    }

    void chonBan(String tenBan, JPanel card, JLabel lblBadge) {
        if (banDangChon != null) {
            JPanel old = cardBan.get(banDangChon);
            if (old != null) old.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(210,210,210),1), BorderFactory.createEmptyBorder(10,10,10,10)));
        }
        banDangChon = tenBan;
        // Bo viền xanh dày hơn khi chọn bàn
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_BTN_THANHTOAN, 2), BorderFactory.createEmptyBorder(9,9,9,9)));
        lblTenBan.setText(tenBan);

        if (duLieuBan.containsKey(tenBan)) {
            if (thoiGianMoBan.get(tenBan) != null) lblGioMo.setText(thoiGianMoBan.get(tenBan).format(java.time.format.DateTimeFormatter.ofPattern("(dd/MM/yyyy) HH:mm")));
            DefaultTableModel savedModel = duLieuBan.get(tenBan);
            modelOrder.setRowCount(0);
            for (int i=0; i<savedModel.getRowCount(); i++)
                modelOrder.addRow(new Object[]{false, savedModel.getValueAt(i,1), savedModel.getValueAt(i,2), savedModel.getValueAt(i,3), savedModel.getValueAt(i,4), "✕", savedModel.getValueAt(i,6)});
            btnMoBan.setVisible(false); btnCapNhat.setVisible(true);
        } else {
            lblGioMo.setText(""); modelOrder.setRowCount(0); btnMoBan.setVisible(true); btnCapNhat.setVisible(false);
        }
        capNhatTong();
    }

    private void themMonVaoOrder(int row, DefaultTableModel modelMon) {
        if (banDangChon == null) { JOptionPane.showMessageDialog(this,"Chọn bàn trước!"); return; }

        // Lấy MaSP từ bảng Menu
        int maSP = Integer.parseInt(modelMon.getValueAt(row, 0).toString());
        String tenMon = modelMon.getValueAt(row, 1).toString();
        String giaBan = modelMon.getValueAt(row, 2).toString();

        for (int i=0; i<modelOrder.getRowCount(); i++) {
            if (modelOrder.getValueAt(i, 1).toString().equals(tenMon)) {
                int sl = Integer.parseInt(modelOrder.getValueAt(i, 2).toString());
                modelOrder.setValueAt(sl + 1, i, 2); capNhatTong(); return;
            }
        }
        modelOrder.addRow(new Object[]{false, tenMon, 1, giaBan, giaBan, "✕", maSP});
        capNhatTong();
    }

    private void moBan() {
        if (banDangChon == null) return;
        thoiGianMoBan.put(banDangChon, java.time.LocalDateTime.now()); salveOrderVaBan();
        btnMoBan.setVisible(false); btnCapNhat.setVisible(true); capNhatCardBan(banDangChon, true);
        lblGioMo.setText(thoiGianMoBan.get(banDangChon).format(java.time.format.DateTimeFormatter.ofPattern("(dd/MM/yyyy) HH:mm")));
        locBan();
    }

    private void capNhatOrder() { if (banDangChon != null) { salveOrderVaBan(); JOptionPane.showMessageDialog(this,"Đã cập nhật!"); } }
    private void huyOrder() {
        if (banDangChon==null) return;
        if (UIHelper.confirm(this,"Hủy toàn bộ order?")) { lamSachBan(banDangChon); }
    }

    public void lamSachBan(String ten) {
        duLieuBan.remove(ten); thoiGianMoBan.remove(ten);
        if (ten.equals(banDangChon)) {
            modelOrder.setRowCount(0); btnMoBan.setVisible(true); btnCapNhat.setVisible(false); lblGioMo.setText(""); capNhatTong();
        }
        capNhatCardBan(ten, false); locBan();
    }

    private void salveOrderVaBan() {
        DefaultTableModel saved = new DefaultTableModel(new String[]{"[ ]","MÓN","SL","GIÁ","TT","X","MaSP"},0);
        for (int i=0; i<modelOrder.getRowCount(); i++) {
            Object[] row = new Object[7];
            for (int j=0;j<7;j++) row[j] = modelOrder.getValueAt(i,j);
            saved.addRow(row);
        }
        duLieuBan.put(banDangChon, saved);
    }

    private void capNhatCardBan(String ten, boolean coKhach) {
        JPanel card = cardBan.get(ten); if (card==null) return;

        // Đổi màu Nền toàn bộ thẻ thay vì đổi màu chữ Badge
        if (coKhach) {
            card.setBackground(new Color(255, 230, 230)); // Đỏ nhạt cho bàn có khách
        } else {
            card.setBackground(Color.WHITE); // Trắng cho bàn trống
        }
        card.repaint();
    }

    private void capNhatThanhTien(int row) {
        if (row < 0 || row >= modelOrder.getRowCount()) return;
        try {
            int sl = Integer.parseInt(modelOrder.getValueAt(row, 2).toString());
            if (sl <= 0) { modelOrder.removeRow(row); capNhatTong(); return; }
            String giaStr = modelOrder.getValueAt(row, 3).toString().replaceAll("[^0-9]", "");
            modelOrder.setValueAt(String.format("%,d", (long) sl * Long.parseLong(giaStr)), row, 4);
        } catch (Exception ignored) {}
        capNhatTong();
    }

    private void capNhatTong() {
        long tong = 0;
        boolean coChonLe = false;
        for (int i = 0; i < modelOrder.getRowCount(); i++) {
            if ((Boolean) modelOrder.getValueAt(i, 0)) { coChonLe = true; break; }
        }

        for (int i = 0; i < modelOrder.getRowCount(); i++) {
            if (!coChonLe || (Boolean) modelOrder.getValueAt(i, 0)) {
                try { tong += Long.parseLong(modelOrder.getValueAt(i, 4).toString().replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
            }
        }

        long giamPct = 0, phuThu = 0, giamTT = 0;
        try { giamPct = Long.parseLong(txtGiamPct.getText().trim()); } catch (Exception ignored) {}
        try { phuThu = Long.parseLong(txtPhuThu.getText().replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

        // CẬP NHẬT: Thêm biến tính tiền "Giảm trực tiếp" nếu nút Toggle được gạt sang xanh
        if (togGiamTT != null && togGiamTT.isSelected()) {
            try { giamTT = Long.parseLong(txtGiamTT.getText().replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
        }

        long thanhToan = tong - giamTT - (tong * giamPct / 100) + phuThu;

        lblTongHoaDon.setText(String.format("%,d đ", tong));
        lblThanhToan .setText(String.format("%,d đ", Math.max(0, thanhToan)));
        tinhTienThua();
    }

    private void tinhTienThua() {
        try {
            long thanhtoan = Long.parseLong(lblThanhToan.getText().replaceAll("[^0-9]",""));
            long khachDua = txtKhachDua.getText().replaceAll("[^0-9]","").isEmpty() ? 0 : Long.parseLong(txtKhachDua.getText().replaceAll("[^0-9]",""));
            lblTienThua.setText(String.format("%,d đ", Math.max(0, khachDua - thanhtoan)));
        } catch (Exception ignored) {}
    }

    private JToggleButton taoCustomSwitch(boolean selected) {
        JToggleButton tog = new JToggleButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? new Color(46, 204, 113) : new Color(180, 180, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                int ks = getHeight() - 4;
                g2.setColor(Color.WHITE); g2.fillOval(isSelected() ? getWidth() - ks - 2 : 2, 2, ks, ks);
                g2.dispose();
            }
        };
        tog.setPreferredSize(new Dimension(36, 18)); tog.setContentAreaFilled(false); tog.setBorderPainted(false);
        tog.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); tog.setSelected(selected); return tog;
    }

    private JPanel taoCumSwitch(String text, JToggleButton tog) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); p.setOpaque(false);
        JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { tog.doClick(); } });
        p.add(tog); p.add(lbl); return p;
    }

    private JLabel mkLabelTT(String text) {
        JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI",Font.BOLD,12));
        l.setPreferredSize(new Dimension(100,28)); return l;
    }

    private JButton mkBtn2(String text, Color bg, Color fg, int w) {
        JButton b = new JButton(text); b.setFont(new Font("Segoe UI",Font.BOLD,12)); b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true); b.setPreferredSize(new Dimension(w,30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
}