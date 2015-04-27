import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

public class MainServer implements Runnable {

	private final ServerSocket servSocket;
	private static final int PORT = 10481;
	private final Connection con;

	public MainServer(Connection con) throws IOException {
		servSocket = new ServerSocket(PORT);
		this.con = con;
	}

	@Override
	public void run() {
		System.out.println("Server started at port " + PORT);
		try {
			while (!Thread.currentThread().isInterrupted()) {
				Socket socket = servSocket.accept();
				System.out.println("A client connected "
						+ socket.getInetAddress());
				try {
					Thread singleServer = new Thread(new ServerThread(socket,
							con));
					singleServer.setDaemon(true);
					singleServer.start();
				} catch (IOException e) {
					socket.close();
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				servSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}

class ServerThread implements Runnable {

	private Socket socket;
	private final Connection con;

	public ServerThread(Socket socket, Connection con) throws IOException {
		this.socket = socket;
		socket.setSoTimeout(10000);
		this.con = con;
	}

	@Override
	public void run() {
		String intServerIP = "";
		String extServerIP = "";
		while (true) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String type = in.readLine();
				if (type == null){ 
					if (!intServerIP.equals(""))
						removeServerFromList(extServerIP, intServerIP);
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;
				}
				
				if (type != null && !type.equals("c")) {
					switch (type) {
					case "server_connect":
						intServerIP = in.readLine();
						extServerIP = socket.getInetAddress().getHostAddress();
						addServerToList(extServerIP, intServerIP);
						break;
					case "server_disconnect":
						removeServerFromList(extServerIP, intServerIP);
						System.out.println("A client server disconnected"
								+ socket.getInetAddress());
						socket.close();
						break;
					case "client_getList":
						PrintWriter out = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(
										socket.getOutputStream())), true);

						for (String serverIP : listServers(socket
								.getInetAddress().getHostAddress())) {
							out.println(serverIP);
						}
						out.println("end");

						break;
					}
				}

			}catch (SocketTimeoutException e) {
				System.out.println(socket.getInetAddress().getHostAddress()
						+ " server lost connection");
				if (!intServerIP.equals(""))
					removeServerFromList(extServerIP, intServerIP);
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			} catch (IOException e) {
				if (!intServerIP.equals(""))
					removeServerFromList(extServerIP, intServerIP);
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			}
		}

	}

	private void addServerToList(String extIP, String intIP) {
		Statement st;
		try {
			st = con.createStatement();
			st.executeUpdate("INSERT INTO servers (extIP, intIP) VALUES ('"
					+ extIP + "', '" + intIP + "');");
			System.out.println("Added a server " + extIP + " " + intIP);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<String> listServers(String clientExtIP) {
		ArrayList<String> res = new ArrayList<String>();

		Statement st;
		try {
			st = con.createStatement();
			ResultSet rs = st
					.executeQuery("SELECT * FROM servers WHERE extIP='"
							+ clientExtIP + "';");
			while (rs.next()) {
				res.add(rs.getString("intIP"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		HashSet<String> hs = new HashSet<>();
		hs.addAll(res);
		res.clear();
		res.addAll(hs);
		return res;
	}

	private void removeServerFromList(String extIP, String intIP) {
		Statement st;
		try {
			st = con.createStatement();
			st.executeUpdate("DELETE FROM servers WHERE extIP='" + extIP
					+ "' AND intIP='" + intIP + "';");
			System.out.println("Removed a server " + extIP + " " + intIP);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
