package reference.solution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args){
        final var server = new Server();

        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/hello", (request, responseStream) -> {
            String response = "Hello, User!";
            try {
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plane" + "\r\n" +
                                "Content-Length: " + response.length() + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.write(response.getBytes());
                responseStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            var response = "Your message was received";
            try {
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plane" + "\r\n" +
                                "Content-Length: " + response.length() + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.write(response.getBytes());
                responseStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler("GET", "/image", ((request, responseStream) -> {
            Path path = Paths.get("images.jpg");
            var imageBytes = Files.readAllBytes(path);
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: image/jpeg\r\n" +
                            "Content-Length: " + imageBytes.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            responseStream.write(imageBytes);
            responseStream.flush();
        }));

        server.listen(9999);
    }
}
