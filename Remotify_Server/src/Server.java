import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class Server implements Runnable{
	
	private ServerSocket servSocket;
	private static final int PORT = 10480;
	
	public Server() throws IOException{
		servSocket = new ServerSocket(PORT);
	}

	@Override
	public void run() {
		try{
			while (!Thread.currentThread().isInterrupted()){
				Socket socket = servSocket.accept();
				System.out.println("A client connected "+socket.getInetAddress());
				try{
					Thread singleServer = new Thread(new ServerThread(socket));
					singleServer.setDaemon(true);
					singleServer.start();
				}catch (IOException e){
					socket.close();
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		while (true){
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String action = null;
				try{
					action = in.readLine();
				}catch (SocketTimeoutException e){
					System.out.println("A client disconnected (lost connection)" + socket.getInetAddress());
					socket.close();
					break;
				}
				if (action != null){
					if (action.equals("DISCONNECT")){
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
							if (!s.equals("c")){
								x = Integer.parseInt(s);
							}else{
								x = Integer.parseInt(in.readLine());
							}
							
							int y;
							s = in.readLine();
							if (!s.equals("c")){
								y = Integer.parseInt(s);
							}else{
								y = Integer.parseInt(in.readLine());
							}
							
							rc.move(x, y);
							break;
						case "SCROLL":
							int x1;
							String s1 = in.readLine();
							if (!s1.equals("c")){
								x1 = Integer.parseInt(s1);
							}else{
								x1 = Integer.parseInt(in.readLine());
							}
							
							int y1;
							s1 = in.readLine();
							if (!s1.equals("c")){
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
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
