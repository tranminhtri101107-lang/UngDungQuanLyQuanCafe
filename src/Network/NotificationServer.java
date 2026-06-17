package Network;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CHƯƠNG 7 — LẬP TRÌNH MẠNG (TCP Server)
 * Server nhận thông báo từ máy thu ngân và broadcast đến tất cả client
 *
 * CHƯƠNG 2 — MULTITHREADING:
 * - Server chạy trên daemon thread riêng (không block UI)
 * - Mỗi client được xử lý trong 1 thread riêng biệt (ClientHandler)
 * - CopyOnWriteArrayList: thread-safe, an toàn khi nhiều thread cùng truy cập
 */
public class NotificationServer {

    public static final int PORT = 9999;
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Thread-safe list lưu danh sách client đang kết nối
    private final CopyOnWriteArrayList<PrintWriter> clients = new CopyOnWriteArrayList<>();

    private ServerSocket serverSocket;
    private boolean      dangChay = false;

    /** Khởi động server — chạy trên daemon thread */
    public void start() {
        dangChay = true;

        // CHƯƠNG 2 — Daemon Thread: tự tắt khi app đóng
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                System.out.println("[Server] Lắng nghe tại cổng " + PORT);

                while (dangChay) {
                    // Chờ kết nối mới từ client
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[Server] Client mới: " + clientSocket.getInetAddress());

                    // Mỗi client → 1 thread riêng (CHƯƠNG 2 — Multithreading)
                    Thread t = new Thread(new ClientHandler(clientSocket));
                    t.setDaemon(true);
                    t.start();
                }
            } catch (IOException e) {
                if (dangChay) System.err.println("[Server] Lỗi: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    /** Dừng server */
    public void stop() {
        dangChay = false;
        try { if (serverSocket != null) serverSocket.close(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    /** Broadcast tin nhắn đến tất cả client đang kết nối */
    private void broadcast(String message) {
        String formatted = "[" + LocalTime.now().format(TF) + "] " + message;
        System.out.println("[Server] Broadcast: " + formatted);
        for (PrintWriter pw : clients) {
            pw.println(formatted);
            pw.flush();
        }
    }

    // ── Inner class: xử lý 1 client ──────────────────────────────
    private class ClientHandler implements Runnable {
        private final Socket socket;
        ClientHandler(Socket s) { this.socket = s; }

        @Override public void run() {
            PrintWriter out = null;
            try {
                // OutputStreamWriter với UTF-8 để hỗ trợ tiếng Việt
                out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8"));

                clients.add(out); // Đăng ký nhận broadcast

                String line;
                while ((line = in.readLine()) != null) {
                    broadcast(line); // Nhận từ client → broadcast cho tất cả
                }
            } catch (IOException e) {
                System.out.println("[Server] Client ngắt kết nối.");
            } finally {
                if (out != null) clients.remove(out);
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    // --- THÊM HÀM MAIN VÀO ĐÂY ĐỂ CHẠY ĐỘC LẬP ---
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   HỆ THỐNG MÁY CHỦ HUTEA POS ĐANG BẬT   ");
        System.out.println("=========================================");

        NotificationServer server = new NotificationServer();
        server.start(); // Gọi hàm khởi động server của bạn

        // Giữ cho chương trình Server luôn thức để lắng nghe Client
        try {
            while (true) {
                Thread.sleep(10000); // Ngủ đông vòng lặp để không tốn CPU
            }
        } catch (InterruptedException e) {
            System.err.println("Server bị gián đoạn: " + e.getMessage());
        }
    }

}