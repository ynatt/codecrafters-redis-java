import commands.CommandReader;
import resp.RespValue;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        int port = 6379;
        try (var serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                while (true) {
                    var clientSocket = serverSocket.accept();
                    executor.submit(() -> {
                        System.out.println("Client connected");
                        try (var out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                            var in = clientSocket.getInputStream()) {
                            var reader = new CommandReader(in);
                            RespValue command;
                            while ((command = reader.readCommand()) != null) {
                                System.out.println(command);
                                String response = reader.handle(command).encode();
                                out.write(response);
                                out.flush();
                            }
                        } catch (IOException e) {
                            System.out.println("IOException");
                            throw new RuntimeException(e);
                        }
                        System.out.println("Client disconnected");
                    });
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}