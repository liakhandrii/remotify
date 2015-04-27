import java.io.IOException;


public class ServerOperator {
	
	private Thread serverThread;
	
	public ServerOperator() throws IOException{
		serverThread = new Thread(new Server());
		serverThread.setDaemon(true);
	}
	
	public void startServer(){
		serverThread.start();
		try {
			serverThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stopServer(){
		serverThread.interrupt();
	}
	
	public boolean isRunning(){
		return serverThread.isAlive();
	}

}
