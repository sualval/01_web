package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> hashMap = new ConcurrentHashMap<>();

    public void addHandler(String method, String path, Handler handler) {
        hashMap.putIfAbsent(method, new ConcurrentHashMap<>());
        hashMap.get(method).put(path, handler);
    }

    public void start(int countThreadsPool, int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            final ExecutorService es = Executors.newFixedThreadPool(countThreadsPool);

            while (true) {
                try {
                    var socket = serverSocket.accept();
                    es.submit(() -> run(socket));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(Socket socket) {

        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                return;
            }

            final var method = parts[0];
            final var path = parts[1];
            final var body = parts[2];
            final var request = new Request(method, path, body);
            System.out.println(method+" "+Thread.currentThread().getName());

            if (!hashMap.containsKey(method)) {
                notFound(out);
                return;
            }

            var methodHandlers = hashMap.get(method);
            if (!methodHandlers.containsKey(path)) {
                notFound(out);
                return;
            }
            methodHandlers.get(path).handle(request, out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notFound(BufferedOutputStream out) throws IOException {
        out.write(("HTTP/1.1 404 Not Found\r\n" + "Content-Length: 0\r\n" + "Connection: close\r\n" + "\r\n").getBytes());
        out.flush();
    }


}