import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/CampusWifiDB";
            String username = "root";
            String password = "M123##678d##";

            Connection con = DriverManager.getConnection(
                    url, username, password
            );

            System.out.println("Database Connected Successfully!");
            return con;

        } catch (SQLException e) {
            System.out.println("Database Connection Failed!");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        getConnection();
    }
}