
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    private final int PORT = 5555;
    Process process;

    public void initialize() {
        try {
            //Estabilish connection with server then create the process
            Socket processClient = new Socket("localhost", PORT);
            process = new Process();
            process.setId(generateIndex());
            process.setActive(true);
            process.setSocketWithServer(processClient);
            process.showInfo();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.initialize();

        main.sendMessage();
        main.waitForMessage();
    }

    //Thread to send messages to server
    public void sendMessage() {
        Scanner input = new Scanner(System.in);
        new Thread() {
            @Override
            public void run() {
                try {
                    //Send the process through message to the server to update the list of processes
                    DataOutputStream out = new DataOutputStream(process.getSocketWithServer().getOutputStream());
                    out.writeUTF("ADD " + process.toString());
                    System.out.println("====You can send message to the next in ring now====");
                    while(true) {
                        String message = input.nextLine();
                        out.writeUTF("TONEXT:" + message);
                    }
                } catch (IOException ex) {
                }
            }
        }.start();
    }

    //Thread to incoming messages from server
    public void waitForMessage() {
        new Thread() {
            @Override
            public void run() {
                try {
                    DataInputStream in = new DataInputStream(process.getSocketWithServer().getInputStream());

                    while (true) {
                        String incomingMessage = in.readUTF();
                        System.out.println("Message from server: " + incomingMessage);
                        //Update coordinator status
                        if (incomingMessage.startsWith("UPDATE:")) {
                            String[] messageSplit = incomingMessage.split(":");
                            process.setCoordinator(Boolean.valueOf(messageSplit[1]));
                            System.out.println("UPDATE Coordinator: " + process.isCoordinator());
                        }
                    }
                } catch (IOException ex) {
                }
            }
        }.start();
    }

    /*
    *   Generate an UUID object which contains 32 characters, then
    *   randomize a number between 0 - 32, convert the result to int which will be used as the process index
     */
    public int generateIndex() {
        Random random = new Random();
        int indexLength = random.nextInt(33);
        char[] uniqueKey = String.valueOf(UUID.randomUUID()).replace("-", "").toCharArray();
        int index = 0;

        for (int i = 0; i < indexLength; i++) {
            index += uniqueKey[i];
        }

        return index;
    }

}
