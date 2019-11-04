
import java.net.Socket;

public class Process implements Comparable<Process> {

    private int id;
    private boolean isActive = false;
    private boolean isCoordinator = false;
    private boolean isCoordinatorMonitor = false;
    private Socket socketWithServer;

    public Socket getSocketWithServer() {
        return socketWithServer;
    }

    public void setSocketWithServer(Socket socket) {
        this.socketWithServer = socket;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }

    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    public boolean isCoordinatorMonitor() {
        return isCoordinatorMonitor;
    }

    public void setCoordinatorMonitor(boolean isMonitor) {
        this.isCoordinatorMonitor = isMonitor;
    }

    @Override
    public String toString() {
        return this.socketWithServer + ";" + this.id + ";" + this.isActive + ";" + this.isCoordinator + ";"
                + this.isCoordinatorMonitor;
    }

    public void showInfo() {
        System.out.println(this.toString());
        System.out.println("My ID: " + this.getId());
        System.out.println("Active: " + this.isActive());
        System.out.println("Coordinator: " + this.isCoordinator());
        System.out.println("Coordinator Monitor: " + this.isCoordinatorMonitor());
    }

    //To find higher id (new coordinator)
    @Override
    public int compareTo(Process t) {
        return t.getId() - this.id;
    }
}
