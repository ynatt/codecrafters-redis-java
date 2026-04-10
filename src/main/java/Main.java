import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args){
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        // Uncomment the code below to pass the first stage
        Socket clientSocket = null;
        int port = 6379;
        try (var serverSocket = new ServerSocket(port)) {
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            clientSocket = serverSocket.accept();
            System.out.println("Client is connected");
            try(var clientInputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                var serverOutputWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
                while (clientSocket.isConnected()) {
                    String inputLine = clientInputReader.readLine();
                    System.out.println(inputLine);
                    if ("PING".equals(inputLine)) {
                        serverOutputWriter.write("+PONG\r\n");
                        serverOutputWriter.flush();
                    } else {
                        System.out.println("Unexpected input:");
                        System.out.println(inputLine);
                        serverOutputWriter.write("Unexpected input:" + inputLine + "\n");
                        serverOutputWriter.flush();
                    }

                }
                System.out.println("Client is disconnected");
            }
//            clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}
