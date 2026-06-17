package View;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/** Helper UI dùng chung — màu sắc, font, nút chuẩn cho toàn bộ View */
public class UIHelper {

    // Màu theme POS
    public static final Color C_HEADER  = new Color(28,  37,  50);
    public static final Color C_PRIMARY = new Color(52,  152, 219);
    public static final Color C_SUCCESS = new Color(39,  174, 96);
    public static final Color C_WARNING = new Color(243, 156, 18);
    public static final Color C_DANGER  = new Color(231, 76,  60);
    public static final Color C_PURPLE  = new Color(142, 68,  173);
    public static final Color C_GRAY    = new Color(149, 165, 166);
    public static final Color C_BG      = new Color(240, 242, 245);

    // Font
    public static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font F_HEADER = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font F_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);

    private UIHelper() {}

    /** Tạo panel tiêu đề xanh đen trên đầu form */
    public static JPanel taoHeader(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_HEADER);
        p.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JLabel l = new JLabel(title);
        l.setFont(F_TITLE); l.setForeground(Color.WHITE);
        p.add(l, BorderLayout.WEST);
        return p;
    }

    /** Tạo nút màu có hover effect */
    public static JButton taoNut(String text, Color bg) { return taoNut(text, bg, 120, 34); }

    public static JButton taoNut(String text, Color bg, int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(F_HEADER); btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (w > 0) btn.setPreferredSize(new Dimension(w, h));
        else       btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, h));
        Color darker = bg.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(darker); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    /** Label chuẩn cho form */
    public static JLabel taoLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(F_HEADER);
        l.setForeground(new Color(60,60,60)); l.setPreferredSize(new Dimension(160,28));
        return l;
    }

    /** TextField chuẩn */
    public static JTextField taoTextField(boolean editable) {
        JTextField tf = new JTextField(); tf.setFont(F_BODY); tf.setEditable(editable);
        tf.setPreferredSize(new Dimension(220, 32));
        if (!editable) { tf.setBackground(new Color(245,245,245)); tf.setForeground(Color.GRAY); }
        return tf;
    }

    /** Áp style cho JTable */
    public static void styleTable(JTable t) {
        t.setRowHeight(30); t.setFont(F_BODY);
        t.setSelectionBackground(new Color(214,234,248)); t.setSelectionForeground(Color.BLACK);
        t.setGridColor(new Color(235,237,239)); t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setFont(F_HEADER); h.setBackground(C_HEADER); h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(0,38)); h.setReorderingAllowed(false);
    }

    public static void canGiuaCot(JTable t, int... cols) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c : cols) t.getColumnModel().getColumn(c).setCellRenderer(r);
    }

    public static void canPhaiCot(JTable t, int... cols) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : cols) t.getColumnModel().getColumn(c).setCellRenderer(r);
    }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // Đặt trong lớp UIHelper hoặc lớp Base
    public static ImageIcon getIcon(String fileName) {
        // Lưu ý: Đường dẫn bắt đầu bằng "/" để trỏ vào root của classpath
        java.net.URL imgUrl = UIHelper.class.getResource("/resource/" + fileName);
        if (imgUrl != null) {
            return new ImageIcon(imgUrl);
        } else {
            System.err.println("Không tìm thấy file icon: " + fileName);
            return null;
        }
    }

    public static ImageIcon resizeIcon(String path, int width, int height) {

        ImageIcon icon = new ImageIcon(
                UIHelper.class.getResource(path)
        );

        Image img = icon.getImage().getScaledInstance(
                width,
                height,
                Image.SCALE_SMOOTH
        );

        return new ImageIcon(img);
    }

}