import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class RemotifyMain {

	public static void main(String[] args) throws IOException {
		getMyIP();
		ServerOperator so = new ServerOperator();
		Socket socket = null;
		try {
			socket = new Socket("remotify.cloudapp.net", 10481);
			final PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream())), true);
			out.println("server_connect");
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

		if (socket != null) {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream())), true);
			out.println("server_disconnect");
		}
	}

	
}