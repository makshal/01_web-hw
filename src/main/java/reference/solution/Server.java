package reference.solution;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Server {

    private final Map<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }

    public void listen(int port) {
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = parseRequest(in);
            if (request == null) {
                sendResponse(out, "400 Bad Request", "text/plain", "Bad Request".getBytes(StandardCharsets.UTF_8));
                return;
            }

            Handler handler = handlers
                    .getOrDefault(request.method(), new ConcurrentHashMap<>())
                    .get(request.path());

            if (handler != null) {
                handler.handle(request, out);
            } else {
                sendResponse(out, "404 Not Found", "text/plain", "Not Found".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }
    }

    private Request parseRequest(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) return null;

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) return null;

        String method = parts[0];
        String fullPath = parts[1];

        String path;
        String queryString = null;

        int positionOfChar = fullPath.indexOf('?');
        if (positionOfChar != -1) {
            path = fullPath.substring(0, positionOfChar);
            queryString = fullPath.substring(positionOfChar + 1);
        } else {
            path = fullPath;
        }

        List<NameValuePair> queryParams = Collections.emptyList(); // Если queryString пустой → params остаётся emptyList()
        if (queryString != null && !queryString.isEmpty()) {
            queryParams = URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
        }

        Map<String, String> headers = new ConcurrentHashMap<>();
        String headerLine;
        int contentLength = 0;

        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(":", 2);
            if (headerParts.length == 2) {
                String headerName = headerParts[0].trim();
                String headerValue = headerParts[1].trim();
                headers.put(headerName, headerValue);
                if ("Content-Length".equalsIgnoreCase(headerName)) {
                    try {
                        contentLength = Integer.parseInt(headerValue);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        String body = null;
        if (contentLength > 0) {
            char[] bodyChars = new char[contentLength];
            int read = 0;
            while (read < contentLength) {
                int r = in.read(bodyChars, read, contentLength - read);
                if (r == -1) break;
                read += r;
            }
            body = new String(bodyChars, 0, read);
        }

        List<NameValuePair> postParams = Collections.emptyList();

        if (headers.get("Content-Type") != null && headers.get("Content-Type").equals("x-www-form-urlencoded")) {
            postParams = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
        }

        return new Request(method, path, headers, body, queryParams, postParams);
    }

    private void sendResponse(BufferedOutputStream out, String status, String contentType, byte[] body) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), false);
        pw.printf("HTTP/1.1 %s\r\n", status);
        pw.printf("Content-Type: %s\r\n", contentType);
        pw.printf("Content-Length: %d\r\n", body.length);
        pw.print("\r\n");
        pw.flush();
        out.write(body);
        out.flush();
    }
}

