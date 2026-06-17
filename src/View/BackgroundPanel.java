package View;

import javax.swing.*;
import java.awt.*;

/** JPanel tùy chỉnh vẽ ảnh nền — dùng cho LoginForm */
public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(Image image) { this.backgroundImage = image; }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null)
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}