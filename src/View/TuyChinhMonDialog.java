package View;

import Model.SanPham;
import javax.swing.*;
import java.awt.*;

public class TuyChinhMonDialog extends JDialog {
    private SanPham spGoc;
    private int soLuong = 1;
    private int giaHienTai;
    private boolean xacNhan = false;

    private JLabel lblTongTien;
    private JTextField txtSoLuong;
    private JRadioButton radSizeM, radSizeL;

    public TuyChinhMonDialog(Window owner, SanPham sp) {
        super(owner, "Tùy chỉnh món", ModalityType.APPLICATION_MODAL);
        this.spGoc = sp;
        this.giaHienTai = sp.getGiaBan();

        setSize(360, 280);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JLabel lblTenMon = new JLabel(sp.getTenSP(), SwingConstants.CENTER);
        lblTenMon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTenMon.setOpaque(true);
        lblTenMon.setBackground(new Color(50, 50, 90));
        lblTenMon.setForeground(Color.WHITE);
        lblTenMon.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTenMon, BorderLayout.NORTH);

        // --- BODY (Chọn Size & Số lượng) ---
        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        JLabel lblSize = new JLabel("Chọn Size:");
        lblSize.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlCenter.add(lblSize, g);

        JPanel pnlSize = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSize.setBackground(Color.WHITE);
        radSizeM = new JRadioButton("Size M ");
        radSizeM.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        radSizeM.setBackground(Color.WHITE);
        radSizeM.setSelected(true);

        radSizeL = new JRadioButton("Size L (+10.000đ)");
        radSizeL.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        radSizeL.setBackground(Color.WHITE);

        ButtonGroup bg = new ButtonGroup();
        bg.add(radSizeM); bg.add(radSizeL);
        pnlSize.add(radSizeM); pnlSize.add(radSizeL);

        g.gridx = 1; pnlCenter.add(pnlSize, g);

        g.gridx = 0; g.gridy = 1;
        JLabel lblSL = new JLabel("Số lượng:");
        lblSL.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlCenter.add(lblSL, g);

        JPanel pnlSL = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlSL.setBackground(Color.WHITE);
        JButton btnTru = new JButton("-");
        btnTru.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTru.setPreferredSize(new Dimension(40, 32));
        btnTru.setFocusPainted(false);

        txtSoLuong = new JTextField("1", 3);
        txtSoLuong.setHorizontalAlignment(JTextField.CENTER);
        txtSoLuong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtSoLuong.setEditable(false);

        JButton btnCong = new JButton("+");
        btnCong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCong.setPreferredSize(new Dimension(40, 32));
        btnCong.setFocusPainted(false);

        pnlSL.add(btnTru); pnlSL.add(txtSoLuong); pnlSL.add(btnCong);
        g.gridx = 1; pnlCenter.add(pnlSL, g);

        add(pnlCenter, BorderLayout.CENTER);

        // --- FOOTER (Tổng tiền & Nút xác nhận) ---
        JPanel pnlBot = new JPanel(new BorderLayout());
        pnlBot.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        pnlBot.setBackground(new Color(245, 245, 245));

        lblTongTien = new JLabel(String.format("Tổng: %,d đ", sp.getGiaBan()));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongTien.setForeground(new Color(200, 50, 50));
        pnlBot.add(lblTongTien, BorderLayout.WEST);

        JButton btnThem = new JButton("✔  THÊM VÀO ĐƠN");
        btnThem.setBackground(new Color(0, 150, 60));
        btnThem.setForeground(Color.WHITE);
        btnThem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnThem.setFocusPainted(false);
        btnThem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pnlBot.add(btnThem, BorderLayout.EAST);

        add(pnlBot, BorderLayout.SOUTH);

        // --- SỰ KIỆN ---
        radSizeM.addActionListener(e -> capNhatGia());
        radSizeL.addActionListener(e -> capNhatGia());

        btnCong.addActionListener(e -> {
            soLuong++;
            txtSoLuong.setText(String.valueOf(soLuong));
            capNhatGia();
        });

        btnTru.addActionListener(e -> {
            if (soLuong > 1) {
                soLuong--;
                txtSoLuong.setText(String.valueOf(soLuong));
                capNhatGia();
            }
        });

        btnThem.addActionListener(e -> {
            xacNhan = true;
            dispose();
        });
    }

    private void capNhatGia() {
        giaHienTai = spGoc.getGiaBan() + (radSizeL.isSelected() ? 10000 : 0);
        lblTongTien.setText(String.format("Tổng: %,d đ", giaHienTai * soLuong));
    }

    public boolean isXacNhan() { return xacNhan; }
    public int getSoLuong() { return soLuong; }
    public int getGiaCustom() { return giaHienTai; }
    public String getTenMonCustom() {
        // Nối thêm chữ (Size L) vào tên món để in ra bill nếu khách chọn size L
        if (radSizeL.isSelected()) return spGoc.getTenSP() + " (Size L)";
        return spGoc.getTenSP();
    }
}