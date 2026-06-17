package Network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * CHƯƠNG 7 — LẬP TRÌNH MẠNG (TCP Client)
 * Kết nối đến NotificationServer để:
 * 1. Gửi thông báo khi thu ngân thanh toán xong
 * 2. Nhận thông báo từ server hiển thị lên BanHangForm
 *
 * CHƯƠNG 2 — MULTITHREADING:
 * Thread lắng nghe (listenThread) chạy nền — không block UI Swing
 */
public class NotificationClient {

    private static final String HOST = "localhost";
    private static final int    PORT = NotificationServer.PORT;

    private Socket      socket;
    private PrintWriter out;
    private boolean     dangKetNoi = false;

    /**
     * Kết nối đến server không đồng bộ
     * @param onNhanDuoc Callback được gọi mỗi khi nhận tin nhắn mới
     *                   (dùng SwingUtilities.invokeLater nếu cập nhật UI)
     */
    public void ketNoi(Consumer<String> onNhanDuoc) {
        Thread t = new Thread(() -> {
            try {
                socket = new Socket(HOST, PORT);
                out    = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                dangKetNoi = true;
                System.out.println("[Client] Kết nối server thành công!");

                // Thread lắng nghe nhận tin (CHƯƠNG 2)
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8"));
                String line;
                while ((line = in.readLine()) != null) {
                    final String msg = line;
                    if (onNhanDuoc != null) onNhanDuoc.accept(msg);
                }
            } catch (IOException e) {
                System.out.println("[Client] Không thể kết nối server — chạy offline.");
                dangKetNoi = false;
            }
        });
        t.setDaemon(true); // Daemon: tắt cùng app
        t.start();
    }

    /**
     * Gửi thông báo thanh toán đến server
     */
    public void guiThongBaoThanhToan(String soBan, long tongTien,
                                     String tenNV, String phuongThuc) {
        if (!dangKetNoi || out == null) return;
        String msg = String.format("🧾 %s | %,d đ | %s | NV: %s",
                soBan, tongTien, phuongThuc, tenNV);
        out.println(msg);
    }

    /** Gửi tin nhắn tự do */
    public void gui(String message) {
        if (dangKetNoi && out != null) out.println(message);
    }

    /** Ngắt kết nối */
    public void ngat() {
        dangKetNoi = false;
        try { if (socket != null) socket.close(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isDangKetNoi() { return dangKetNoi; }
}