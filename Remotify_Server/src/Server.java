import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;


public class Server implements Runnable{
    
    private ServerSocket servSocket;
    private ArrayList<Thread> threads;
    private ArrayList<Socket> sockets;
    
    public Server(ServerSocket servSocket) throws IOException{
        this.servSocket = servSocket;
        threads = new ArrayList<Thread>();
        sockets = new ArrayList<Socket>();
    }
    
    @Override
    public void run() {
        try{
            while (!Thread.currentThread().isInterrupted()){
                Socket socket = servSocket.accept();
                RemotifyMain.sf.addTableRow(socket.getInetAddress().getHostAddress());
                System.out.println("A client connected " + socket.getInetAddress());
                try{
                    Thread singleServer = new Thread(new ServerThread(socket));
                    threads.add(singleServer);
                    sockets.add(socket);
                    singleServer.setDaemon(true);
                    singleServer.start();
                }catch (IOException e){
                    socket.close();
                    break;
                }
            }
        } catch (IOException e1) {
            for (Thread t: threads){
                t.interrupt();
            }
            for (Socket s: sockets){
                try {
                    s.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }finally{
            try {
                servSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
    
    
    
}

class ServerThread implements Runnable{
    
    private Socket socket;
    
    public ServerThread(Socket socket) throws IOException{
        this.socket = socket;
        socket.setSoTimeout(10000);
    }
    
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String action = null;
                try{
                    action = in.readLine();
                }catch (SocketTimeoutException e){
                    RemotifyMain.sf.removeTableRow(socket.getInetAddress().getHostAddress());
                    System.out.println("A client disconnected (lost connection)" + socket.getInetAddress());
                    socket.close();
                    break;
                }catch (SocketException e){
                    break;
                }
                if (action != null){
                    if (action.equals("DISCONNECT")){
                        RemotifyMain.sf.removeTableRow(socket.getInetAddress().getHostAddress());
                        System.out.println("A client disconnected " + socket.getInetAddress());
                        socket.close();
                        break;
                    }
                    
                    RemotifyController rc = new RemotifyController();
                    
                    switch (action){
                        case "CLICK":
                            rc.click();
                            break;
                        case "RIGHT_CLICK":
                            rc.rightClick();
                            break;
                        case "MOVE":
                            int x;
                            String s = in.readLine();
                            if (!s.equals("co")){
                                x = Integer.parseInt(s);
                            }else{
                                x = Integer.parseInt(in.readLine());
                            }
                            
                            int y;
                            s = in.readLine();
                            if (!s.equals("co")){
                                y = Integer.parseInt(s);
                            }else{
                                y = Integer.parseInt(in.readLine());
                            }
                            
                            rc.move(x, y);
                            break;
                        case "SCROLL":
                            int x1;
                            String s1 = in.readLine();
                            if (!s1.equals("co")){
                                x1 = Integer.parseInt(s1);
                            }else{
                                x1 = Integer.parseInt(in.readLine());
                            }
                            
                            int y1;
                            s1 = in.readLine();
                            if (!s1.equals("co")){
                                y1 = Integer.parseInt(s1);
                            }else{
                                y1 = Integer.parseInt(in.readLine());
                            }
                            
                            rc.scroll(x1, y1);
                            break;
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            } catch (AWTException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }
        
    }
    
}
