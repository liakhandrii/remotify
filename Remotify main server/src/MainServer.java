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
