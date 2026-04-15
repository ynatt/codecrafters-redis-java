import vel.vn.commands.CommandReader;
import vel.vn.resp.RESPParser;
import vel.vn.resp.RespValue;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        int port = 6379;
        CommandReader commandReader = new CommandReader();
        try (var serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                while (true) {
                    var clientSocket = serverSocket.accept();
                    executor.submit(() -> {
                        System.out.println("Client connected");
                        try (var out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                            var in = new BufferedInputStream(clientSocket.getInputStream(), 512)) {
                            RespValue command;
                            while ((command = RESPParser.parse(in)) != null) {
                                System.out.println(command);
                                String response = commandReader.handle(command).encode();
                                System.out.println(response);
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