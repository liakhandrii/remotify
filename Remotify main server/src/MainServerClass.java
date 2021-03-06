import java.io.IOException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;


public class MainServerClass {

	public static void main(String[] args) {
		try {
			//replace password with proper
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/remotify?autoReconnect=true", "root", "PASSWORD");
			Thread serverThread = new Thread(new MainServer(con));
			serverThread.setDaemon(true);
			serverThread.start();
			try {
				serverThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

}
