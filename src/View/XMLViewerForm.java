package View;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 * CHƯƠNG 4 — XML (DOM Parser):
 * Đọc config.xml và hiển thị cây DOM dưới dạng JTree
 */
public class XMLViewerForm extends JFrame {

    public XMLViewerForm() {
        setTitle("Cấu hình hệ thống — config.xml");
        setSize(500, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(UIHelper.taoHeader("🔧  Cấu hình hệ thống (config.xml)"), BorderLayout.NORTH);

        // Xây dựng JTree từ DOM
        JTree tree = new JTree(buildTree());
        tree.setFont(UIHelper.F_BODY);
        tree.setRowHeight(28);
        tree.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Mở rộng toàn bộ nút
        for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);

        JScrollPane scroll = new JScrollPane(tree);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
        add(scroll, BorderLayout.CENTER);

        // Thanh dưới: đường dẫn + nút đóng
        JPanel bot = new JPanel(new BorderLayout(10, 0));
        bot.setBackground(Color.WHITE);
        bot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        File f = new File("config.xml");
        if (!f.exists()) f = new File("src/config.xml");
        JLabel lblPath = new JLabel("📁  " + f.getAbsolutePath());
        lblPath.setFont(UIHelper.F_SMALL);
        lblPath.setForeground(new Color(120, 120, 120));

        JButton btnDong = UIHelper.taoNut("✖  Đóng", UIHelper.C_GRAY, 95, 32);
        btnDong.addActionListener(e -> dispose());

        bot.add(lblPath, BorderLayout.CENTER);
        bot.add(btnDong, BorderLayout.EAST);
        add(bot, BorderLayout.SOUTH);
    }

    /**
     * CHƯƠNG 4 — DOM Parser: đọc config.xml và tạo cây JTree
     */
    private DefaultMutableTreeNode buildTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("config.xml");
        try {
            File file = new File("config.xml");
            if (!file.exists()) file = new File("src/config.xml");

            if (!file.exists()) {
                root.add(new DefaultMutableTreeNode("⚠  Không tìm thấy config.xml!"));
                return root;
            }

            // Khởi tạo DOM Parser (Chương 4 — 4.1)
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder        builder = factory.newDocumentBuilder();
            Document               doc     = builder.parse(file);
            doc.getDocumentElement().normalize();

            // Duyệt đệ quy cây DOM → JTree
            addDomNode(root, doc.getDocumentElement());

        } catch (Exception e) {
            root.add(new DefaultMutableTreeNode("⚠  Lỗi đọc XML: " + e.getMessage()));
        }
        return root;
    }

    /**
     * Đệ quy thêm node DOM vào JTree
     * Ẩn mật khẩu — thay bằng "••••••••"
     */
    private void addDomNode(DefaultMutableTreeNode parent, Node domNode) {
        if (domNode.getNodeType() != Node.ELEMENT_NODE) return;

        Element el  = (Element) domNode;
        String  tag = el.getTagName();
        String  content = "";

        // Lấy text content nếu chỉ có 1 text child
        if (el.getChildNodes().getLength() == 1
                && el.getFirstChild().getNodeType() == Node.TEXT_NODE) {
            content = el.getTextContent().trim();
            // Ẩn mật khẩu
            if (tag.equalsIgnoreCase("pass") && !content.isEmpty())
                content = "••••••••";
        }

        String label = content.isEmpty() ? "<" + tag + ">" : tag + "  :  " + content;
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(label);
        parent.add(node);

        // Duyệt tiếp các node con
        NodeList children = domNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
            addDomNode(node, children.item(i));
    }
}