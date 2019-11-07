package Ex3_Pop3Server;

import java.io.*;
import java.net.Socket;

public class PopServerThread extends Thread {
    private Socket socket;
    private SampleDataBase database;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String[] Commands = {"USER", "PASS", "STAT", "LIST", "RETR", "DELE", "QUIT"};

    private enum States {INIT, USEROK, AUTHOK}

    private States state;

    public PopServerThread(Socket socket) throws IOException {
        this.socket = socket;
        database = new SampleDataBase();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        state = States.INIT;
    }

    private void sendResponse(String response) throws IOException {
        if (socket.isConnected()) {
            writer.write(response + "\n");
            writer.flush();
        } else {
            System.out.println("Error: not connected.");
        }
    }

    public void run() {
        String clientRequest;
        try {
            sendResponse("+OK Welcome to Christophs POP3");
            do {
                clientRequest = reader.readLine();
                sendResponse(processCommand(clientRequest));
            } while (!clientRequest.equalsIgnoreCase("QUIT"));
            socket.close();
            writer.close();
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String processCommand(String clientRequest) {
        String[] parts = clientRequest.split(" ");
        switch (parts[0]) {
            case "USER":
                if (state == States.INIT) {
                    state = States.USEROK;
                    return "+OK Mailbox valid";
                }
                return "-ERR Invalid command";

            case "PASS":
                if (state == States.USEROK) {
                    state = States.AUTHOK;
                    return "+OK Authorization valid";
                }
                return "-ERR Invalid command";

            case "STAT":
                if (state == States.AUTHOK) {
                    int sizeOfMessages = 0;
                    for (int i = 0; i < database.messages.size(); i++) {
                        sizeOfMessages += database.messages.get(i).getBytes().length;
                    }
                    return "+OK " + database.messages.size() + " " + sizeOfMessages;
                }
                return "-ERR Invalid command";

            case "LIST":
                if (state == States.AUTHOK) {
                    int messageSize = database.messages.size();
                    String str = "";
                    for (int i = 0; i < messageSize; i++) {
                        str = str + (i + 1) + " " + database.messages.get(i).getBytes().length + "\n";
                    }
                    return "+OK " + database.messages.size() + " messages: \n" + str + ".";
                }
                return "-ERR Please login first!!!";


            case "RETR":
                if (state == States.AUTHOK) {
                    String str2 = "";
                    try {
                        str2 = database.messages.get(Integer.parseInt(parts[1]) - 1);
                        return "+OK " + database.messages.get(Integer.parseInt(parts[1]) - 1).length() + " Octets\n"
                                + str2 + ".";
                    } catch (Exception ex) {
                        return "-ERR message not found";
                    }
                }
                return "-ERR Please login first!!!";


            case "DELE":
                if (state == States.AUTHOK) {
                    try {
                        database.messages.remove(Integer.parseInt(parts[1]));
                        return "+OK Message deleted";
                    } catch (Exception ex) {
                        return "-ERR message not found";
                    }
                }
                return "-ERR Please login first!!!";


            case "QUIT":
                return "+OK GoodBye";

            default:
                return "-ERR unknown command";

        }
    }
}
