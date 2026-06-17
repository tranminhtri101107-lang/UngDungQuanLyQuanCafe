package View;

import Controller.ExportController;
import Controller.HoaDonController;
import Controller.ThongKeController;
import Model.HoaDon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Form thống kê doanh thu — biểu đồ cột + bảng + xuất báo cáo
 * CHƯƠNG 3 — IO Stream: Xuất báo cáo ra .txt
 * CHƯƠNG 5 — JDBC: Query GROUP BY ngày
 * TỐI ƯU HIỆU NĂNG: Sử dụng SwingWorker (Multithreading) để không treo UI
 */
public class ThongKeForm extends JPanel {

    private final ThongKeController tkCtrl = new ThongKeController();
    private final HoaDonController  hdCtrl = new HoaDonController();
    private final DecimalFormat      DF    = new DecimalFormat("#,###");

    private JComboBox<String> cbThang, cbNam;
    private JLabel            lblTong, lblTB, lblBanChay;
    private DefaultTableModel tableModel;
    private BarChartPanel     chartPanel;

    public ThongKeForm() {
        setLayout(new BorderLayout());

        add(UIHelper.taoHeader("📊  Thống Kê Doanh Thu"), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        // Gọi hàm cập nhật chạy nền ngay khi vừa mở form
        capNhatThongKe();
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout()); content.setBackground(UIHelper.C_BG);

        // ── Bộ lọc ────────────────────────────────────────────
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.CENTER,14,11));
        filter.setBackground(Color.WHITE);
        filter.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(220,220,220)));

        LocalDate today = LocalDate.now();
        cbThang = new JComboBox<>(new String[]{"1","2","3","4","5","6","7","8","9","10","11","12"});
        cbThang.setSelectedItem(String.valueOf(today.getMonthValue()));
        cbThang.setFont(UIHelper.F_BODY); cbThang.setPreferredSize(new Dimension(70,32));

        cbNam = new JComboBox<>(new String[]{"2024","2025","2026","2027"});
        cbNam.setSelectedItem(String.valueOf(today.getYear()));
        cbNam.setFont(UIHelper.F_BODY); cbNam.setPreferredSize(new Dimension(90,32));

        JButton btnXem  = UIHelper.taoNut("🔍  Xem thống kê", UIHelper.C_PRIMARY, 158,34);
        JButton btnXuat = UIHelper.taoNut("📄  Xuất báo cáo", UIHelper.C_SUCCESS,  152,34);

        filter.add(new JLabel("Tháng:")); filter.add(cbThang);
        filter.add(new JLabel("Năm:"));   filter.add(cbNam);
        filter.add(btnXem); filter.add(btnXuat);

        // ── Thẻ tổng quan ─────────────────────────────────────
        lblTong    = mkValLabel("0 đ",  UIHelper.C_PRIMARY);
        lblTB      = mkValLabel("0 đ",  UIHelper.C_SUCCESS);
        lblBanChay = mkValLabel("—",    UIHelper.C_DANGER);

        JPanel cards = new JPanel(new GridLayout(1,3,12,0));
        cards.setBackground(UIHelper.C_BG);
        cards.setBorder(BorderFactory.createEmptyBorder(10,15,6,15));
        cards.add(mkCard("💰  TỔNG DOANH THU",  lblTong));
        cards.add(mkCard("📅  TRUNG BÌNH/NGÀY", lblTB));
        cards.add(mkCard("🔥  BÁN CHẠY NHẤT",  lblBanChay));

        // ── Biểu đồ + bảng ────────────────────────────────────
        // Khởi tạo biểu đồ rỗng lúc mở app để UI hiện lên tức thì
        Map<String,Long> init = new java.util.LinkedHashMap<>();
        chartPanel = new BarChartPanel(init);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220),1),
                BorderFactory.createTitledBorder(null,"  Biểu đồ doanh thu",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP, UIHelper.F_HEADER)));

        tableModel = new DefaultTableModel(new String[]{"Ngày","Doanh Thu"},0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(tableModel);
        UIHelper.styleTable(tbl); UIHelper.canPhaiCot(tbl, 1);

        tbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2 && tbl.getSelectedRow()>=0)
                    xemChiTietNgay(tableModel.getValueAt(tbl.getSelectedRow(),0).toString());
            }
        });

        JScrollPane scTbl = new JScrollPane(tbl);
        scTbl.setPreferredSize(new Dimension(288,0));
        scTbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220),1),
                BorderFactory.createTitledBorder(null,"  Chi tiết (nhấn đúp để xem HĐ)",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP, UIHelper.F_HEADER)));

        JPanel centerRow = new JPanel(new BorderLayout(10,0));
        centerRow.setBackground(UIHelper.C_BG);
        centerRow.setBorder(BorderFactory.createEmptyBorder(5,15,15,15));
        centerRow.add(chartPanel, BorderLayout.CENTER);
        centerRow.add(scTbl,      BorderLayout.EAST);

        JPanel topPart = new JPanel(new BorderLayout());
        topPart.setBackground(UIHelper.C_BG);
        topPart.add(filter, BorderLayout.NORTH);
        topPart.add(cards,  BorderLayout.CENTER);

        content.add(topPart,   BorderLayout.NORTH);
        content.add(centerRow, BorderLayout.CENTER);

        btnXem .addActionListener(e -> capNhatThongKe());
        btnXuat.addActionListener(e -> xuatBaoCao());
        return content;
    }

    // TÍCH HỢP SWINGWORKER XỬ LÝ ĐA LUỒNG
    private void capNhatThongKe() {
        lblTong.setText("Đang tải...");
        lblTB.setText("Đang tải...");
        lblBanChay.setText("Đang tải...");
        tableModel.setRowCount(0);

        int t = Integer.parseInt(cbThang.getSelectedItem().toString());
        int n = Integer.parseInt(cbNam.getSelectedItem().toString());

        new SwingWorker<Map<String, Long>, Void>() {
            @Override
            protected Map<String, Long> doInBackground() throws Exception {
                // Chạy ngầm gọi CSDL, không làm đơ giao diện
                return tkCtrl.layDoanhThuTheoThang(t, n);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Long> data = get();
                    chartPanel.setData(data);
                    capNhatBang(data);
                    capNhatCards(data);
                } catch (Exception e) {
                    UIHelper.showError(ThongKeForm.this, "Lỗi tải dữ liệu: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void capNhatBang(Map<String,Long> data) {
        tableModel.setRowCount(0); if (data==null) return;
        DateTimeFormatter vn = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Map.Entry<String,Long> e : data.entrySet())
            tableModel.addRow(new Object[]{LocalDate.parse(e.getKey()).format(vn), DF.format(e.getValue())+" đ"});
    }

    private void capNhatCards(Map<String,Long> data) {
        if (data==null||data.isEmpty()) {
            lblTong.setText("0 đ"); lblTB.setText("0 đ"); lblBanChay.setText("Không có dữ liệu"); return;
        }
        long tong=0,max=0; String ngayMax="";
        for (Map.Entry<String,Long> e : data.entrySet()) {
            tong+=e.getValue(); if(e.getValue()>max){max=e.getValue();ngayMax=e.getKey();}
        }
        String[] p = ngayMax.split("-");
        lblTong.setText(DF.format(tong)+" đ");
        lblTB  .setText(DF.format(tong/data.size())+" đ");
        lblBanChay.setText(DF.format(max)+" đ  ("+p[2]+"/"+p[1]+")");
    }

    private void xemChiTietNgay(String ngayVN) {
        String[] p = ngayVN.split("/");
        String ngaySQL = p[2]+"-"+p[1]+"-"+p[0];

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog((Frame) owner, "Hóa đơn ngày "+ngayVN, true);
        dlg.setSize(660,380); dlg.setLocationRelativeTo(this); dlg.setLayout(new BorderLayout());

        String[] cols = {"Mã HĐ","Giờ","Bàn","Tổng Tiền","PT Thanh Toán","Nhân Viên"};
        DefaultTableModel m = new DefaultTableModel(cols,0);
        JTable t = new JTable(m); UIHelper.styleTable(t);
        UIHelper.canGiuaCot(t,0); UIHelper.canPhaiCot(t,3);

        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
        try {
            for (HoaDon hd : hdCtrl.layTheoNgay(ngaySQL))
                m.addRow(new Object[]{hd.getMaHD(),
                        hd.getNgayLap()!=null?hd.getNgayLap().format(tf):"—",
                        hd.getSoBan(), DF.format(hd.getTongTien())+" đ",
                        hd.getPhuongThucThanhToan(), hd.getTenNhanVien()});
        } catch (SQLException e) { UIHelper.showError(dlg,e.getMessage()); }

        dlg.add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT,12,8));
        bot.setBackground(Color.WHITE);
        bot.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(220,220,220)));
        JButton btnD = UIHelper.taoNut("Đóng", UIHelper.C_GRAY, 88,32);
        btnD.addActionListener(e -> dlg.dispose()); bot.add(btnD);
        dlg.add(bot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void xuatBaoCao() {
        int t = Integer.parseInt(cbThang.getSelectedItem().toString());
        int n = Integer.parseInt(cbNam.getSelectedItem().toString());
        try {
            Map<String,Long> data = tkCtrl.layDoanhThuTheoThang(t,n);
            String path = ExportController.xuatBaoCaoDoanhThu(t,n,data);
            JOptionPane.showMessageDialog(this,"✅  Đã xuất báo cáo!\n"+path,"Thành công",JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { UIHelper.showError(this,"Lỗi: "+e.getMessage()); }
    }

    // ── Biểu đồ cột ──────────────────────────────────────────
    static class BarChartPanel extends JPanel {
        private Map<String,Long> data;
        private static final DecimalFormat DF2 = new DecimalFormat("#,###");

        BarChartPanel(Map<String,Long> data) { this.data=data; setBackground(Color.WHITE); }
        void setData(Map<String,Long> data) { this.data=data; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W=getWidth(),H=getHeight(),pL=65,pR=15,pT=35,pB=45;
            int cW=W-pL-pR, cH=H-pT-pB;

            if (data==null||data.isEmpty()) {
                g2.setColor(new Color(180,180,180)); g2.setFont(UIHelper.F_BODY);
                String msg="Không có dữ liệu"; FontMetrics fm=g2.getFontMetrics();
                g2.drawString(msg,(W-fm.stringWidth(msg))/2,H/2); return;
            }

            long maxVal = data.values().stream().mapToLong(v->v).max().orElse(1);
            long rounded = (long)(Math.ceil(maxVal/50000.0)*50000);
            if (rounded==0) rounded=50000;

            g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
            for (int i=0; i<=5; i++) {
                long yv = rounded*i/5;
                int y = pT+cH-(int)((double)yv/rounded*cH);
                g2.setColor(new Color(240,240,240)); g2.setStroke(new BasicStroke(1));
                g2.drawLine(pL,y,W-pR,y);
                g2.setColor(new Color(130,130,130));
                String lbl = yv>=1000?yv/1000+"k":String.valueOf(yv);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(lbl, pL-fm.stringWidth(lbl)-5, y+4);
            }
            g2.setColor(new Color(180,180,180)); g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(pL,pT,pL,pT+cH); g2.drawLine(pL,pT+cH,W-pR,pT+cH);

            int n=data.size(), bAW=n>0?cW/n:cW, bW=Math.min(55,(int)(bAW*0.55)), idx=0;
            for (Map.Entry<String,Long> e : data.entrySet()) {
                int bH=(int)((double)e.getValue()/rounded*cH);
                int x=pL+idx*bAW+(bAW-bW)/2, y=pT+cH-bH;
                GradientPaint gp=new GradientPaint(x,y,new Color(52,152,219),x,pT+cH,new Color(174,214,241));
                g2.setPaint(gp); g2.fillRoundRect(x,y,bW,bH,6,6);
                g2.setColor(new Color(41,128,185,80)); g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(x,y,bW,bH,6,6);
                g2.setColor(new Color(44,62,80)); g2.setFont(new Font("Segoe UI",Font.BOLD,10));
                String vl=e.getValue()>=1000?e.getValue()/1000+"k":String.valueOf(e.getValue());
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(vl,x+(bW-fm.stringWidth(vl))/2,y-4);
                g2.setColor(new Color(100,100,100)); g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                String dl=e.getKey().substring(5); fm=g2.getFontMetrics();
                g2.drawString(dl,x+(bW-fm.stringWidth(dl))/2,pT+cH+15);
                idx++;
            }
        }
    }

    private JPanel mkCard(String title, JLabel val) {
        JPanel p=new JPanel(new GridLayout(2,1,0,5)); p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220),1),
                BorderFactory.createEmptyBorder(10,15,10,15)));
        JLabel t=new JLabel(title,SwingConstants.CENTER);
        t.setFont(UIHelper.F_HEADER); t.setForeground(new Color(100,100,100));
        p.add(t); p.add(val); return p;
    }

    private JLabel mkValLabel(String txt, Color c) {
        JLabel l=new JLabel(txt,SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI",Font.BOLD,19)); l.setForeground(c); return l;
    }
}