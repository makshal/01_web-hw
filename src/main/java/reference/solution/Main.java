package reference.solution;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/hello", (request, responseStream) -> {
            var langValue = request.getQueryParam("lang");
            var response = langValue != null && !langValue.isEmpty() ? request.getQueryParam("lang") : "Error";
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

        server.addHandler("POST", "/submit", ((request, responseStream) -> {
            var postParams = request.getPostParams();
            var response = postParams != null && !postParams.isEmpty() ? request.getPostParams() : "Error";
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
        }));


        server.listen(9999);
    }
}
