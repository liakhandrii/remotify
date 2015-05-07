import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class RemotifyMain {
    
    public static Socket socket = null;
    public static ServerFrame sf;
    
    public static void main(String[] args) throws IOException {
        ServerOperator so = new ServerOperator();
        String extIP = "";
        try {
            socket = new Socket("remotify.cloudapp.net", 10481);
            final PrintWriter out = new PrintWriter(new BufferedWriter(
                                                                       new OutputStreamWriter(socket.getOutputStream())), true);
            out.println("server_connect");
            out.println(getMyIP());
            BufferedReader in = new BufferedReader(new InputStreamReader(
                                                                         socket.getInputStream()));
            extIP = in.readLine();
            Thread connectionAlive = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        out.println("c");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    
                }
            };
            connectionAlive.setDaemon(true);
            connectionAlive.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        so.startServer();
        sf = new ServerFrame(so, extIP);
        
        sf.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
                super.windowClosing(e);
            }
        });
        
    }
    
    private static void disconnect(){
        if (socket != null) {
            PrintWriter out;
            try {
                out = new PrintWriter(new BufferedWriter(
                                                         new OutputStreamWriter(socket.getOutputStream())), true);
                out.println("server_disconnect");
            } catch (IOException e) {
                
            }
        }
    }
    
    public static String getMyIP() {
        
        try {
            Enumeration<NetworkInterface> e = NetworkInterface
            .getNetworkInterfaces();
            
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration<InetAddress> ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if (i.getHostAddress().startsWith("192.168.")||i.getHostAddress().startsWith("10."))
                        return i.getHostAddress();
                }
            }
        } catch (SocketException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return "Bad IP";
    }
    
}