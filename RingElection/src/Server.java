
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class Server {

    private final int PORT = 5555;
    private ArrayList<Process> clients = new ArrayList<>();

    public Server() {
        System.out.println("======Initializing server======");
        try {
            ServerSocket server = new ServerSocket(PORT);

            //Accept new member, create a process object and wait for messages
            while (true) {
                Socket client = server.accept();
                Process newProcess = new Process();
                newProcess.setSocketWithServer(client);
                clients.add(newProcess);
                waitForMessage(client);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitForMessage(Socket client) {
        new Thread() {
            @Override
            public void run() {
                try {
                    DataInputStream in = new DataInputStream(client.getInputStream());
                    while (true) {
                        String msg = in.readUTF();
                        //System.out.println("Message from client in the port: " + client.getPort() + ": " + msg);
                        if (msg.startsWith("ADD")) {
                            addProcessToRing(msg);
                        } else if (msg.startsWith("TONEXT")) {
                            String[] messageSplit = msg.split(":");
                            //find in the list who sent the message
                            for (int i = 0; i < clients.size(); i++) {
                                Process c = clients.get(i);

                                if (c.getSocketWithServer().equals(client)) {
                                    int indexRecipient = i + 1;

                                    //If it's the last in the ring (client[n-1]) then recipient is the first index (client[0])
                                    if ((i + 1) == clients.size()) {
                                        indexRecipient = 0;
                                    }

                                    Process recipient = clients.get(indexRecipient);
                                    System.out.println("Sending message to ID: " + recipient.getId() + " from ID: " + c.getId());
                                    sendMessage(recipient.getId(), messageSplit[1]);
                                    break;
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    //remove a process if there is any exception and update members' list
                    int index = removeProcessFromRing(client);
                    System.out.println("Process ID " + index + " has left the ring");
                }
            }
        }.start();
    }

    /*
    *   Send message following the clients list (simulating a ring)
    *   clients[0] -> clients[1] -> clients[2] -> ... -> clients[n-1] -> clients[0]
     */
    public void sendMessage(int clientId, String message) {
        try {
            for (Process client : clients) {
                if (client.getId() == clientId) {
                    Socket c = client.getSocketWithServer();
                    DataOutputStream out = new DataOutputStream(c.getOutputStream());
                    out.writeUTF(message);
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Error sending data to client");
        }
    }

    /*
    *   Get the message from a new client and break it to add to the ring
    *   e.g: ADD Socket[....];0;true;false;false;...
     */
    public void addProcessToRing(String message) {
        //to remove the "ADD" which has a space from the content
        message = message.substring(message.indexOf(" ")).trim();

        String[] clientInfo = message.split(";");
        for (Process client : clients) {
            String clientPort = String.valueOf(client.getSocketWithServer().getPort());
            if (clientInfo[0].contains(clientPort)) {
                client.setId(Integer.parseInt(clientInfo[1]));
                client.setActive(Boolean.valueOf(clientInfo[2]));
                client.setCoordinator(Boolean.valueOf(clientInfo[3]));
                client.setCoordinatorMonitor(Boolean.valueOf(clientInfo[4]));
            }
        }
        showMembers();
        newCoordinatorElection();
    }

    //Remove a process from the ring and return his index
    public int removeProcessFromRing(Socket c) {
        int index = 0;
        for (int i = 0; i < clients.size(); i++) {
            Process client = clients.get(i);
            if (client.getSocketWithServer().equals(c)) {
                index = client.getId();
                if (client.isCoordinator()) {
                    newCoordinatorElection();
                }
                clients.remove(i);
                break;
            }
        }
        showMembers();
        return index;
    }

    public void newCoordinatorElection() {
        System.out.println("\n====New Election!====");
        ArrayList<Process> clientsTemp = new ArrayList<>(clients);

        Collections.sort(clientsTemp); //sorted higher id to lower

        Process currentCoordinator = clients.get(0);
        //get the current coordinator
        for (Process client : clients) {
            if (client.isCoordinator()) {
                currentCoordinator = client;
                break;
            }
        }

        for (Process client : clients) {
            //check if the current coordinator is gonna stay as coordinator
            if (client.getId() == clientsTemp.get(0).getId()) {
                if (client.isCoordinator()) { //don't change anything
                    System.out.println("ID: " + client.getId() + " stays as coordinator");
                } else { //set last coordinator as false and the new as true
                    currentCoordinator.setCoordinator(false);
                    client.setCoordinator(true);
                    sendMessage(currentCoordinator.getId(), "UPDATE:false");
                    sendMessage(client.getId(), "UPDATE:true");
                    System.out.println("New coordinator: " + client.getId());
                }
                break;
            }

        }
    }

    public void showMembers() {
        System.out.println("\nList of members(ID): ");
        String members = clients.stream().map(client -> String.valueOf(client.getId()))
                .collect(Collectors.joining(", "));
        System.out.println(members);
    }

    public static void main(String[] args) {
        new Server();
    }
}
