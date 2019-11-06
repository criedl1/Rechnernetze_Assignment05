package Pop3Client;

import java.io.*;
import java.net.Socket;

public class Pop3Client {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void connect(String host, int port) throws IOException {
        //socket erstellen mit hostname und port
        socket = new Socket(host,port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        readResponse();
    }

    public void disconnect() throws IOException {
        if (socket != null && socket.isConnected()) {
            socket.close();
            reader.close();
            writer.close();
            System.out.println("Disconnected.");
        } else {
            System.out.println("Error: not connected.");
        }
    }

    public String readResponse() throws IOException {
        String response = reader.readLine();
        if (response.startsWith("-ERR")) {
            System.out.println("Server error: " + response);
        } else {
            System.out.println(response);
        }
        return response;
    }

    public String sendCommand(String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
        return readResponse();
    }

    public static void main(String[] args) {
        String host = "firemail.de";
        String username = "hustlerino@firemail.de";
        String password = "nilsinils2";


        Pop3Client client = new Pop3Client();
        try {
            client.connect(host, 110);
            client.sendCommand("USER " + username);
            client.sendCommand("PASS " + password);

            client.sendCommand("LIST");

            client.sendCommand("RETR 1");
            String response = client.readResponse();
            while (!response.isEmpty()) {
                response = client.readResponse();
            }

            client.sendCommand("QUIT");
            client.disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
