import java.io.IOException;
import java.net.ServerSocket;


public class ServerOperator {
    
    private Thread serverThread;
    public final int PORT = 10480;
    private boolean firstStart = true;
    private ServerSocket ss;
    
    public ServerOperator() throws IOException{
        ss = new ServerSocket(PORT);
        serverThread = new Thread(new Server(ss));
        serverThread.setDaemon(true);
    }
    
    public void startServer() throws IOException{
        if (!firstStart){
            ss = new ServerSocket(PORT);
            serverThread = new Thread(new Server(ss));
            serverThread.setDaemon(true);
        }
        serverThread.start();
        firstStart = false;
    }
    
    public void stopServer(){
        try {
            ss.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        serverThread.interrupt();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public boolean isRunning(){
        return serverThread.isAlive();
    }
    
    public String getStatus(){
        if (serverThread.isAlive()){
            return "running";
        }else{
            return "stopped";
        }
    }
    
}
