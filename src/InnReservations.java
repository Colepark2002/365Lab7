import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class InnReservations
{
    public static void main(String[] args) throws SQLException {
        InnReservations test = new InnReservations();
        test.tryConnect();
    }
    public void tryConnect() throws SQLException
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded");
        } catch (ClassNotFoundException ex)
        {
            System.err.println("Unable to load JDBC Driver");
            System.exit(-1);
        }

        try (Connection conn = DriverManager.getConnection(System.getenv("lab7_JDBC_URL"),
                System.getenv("lab7_JDBC_USER"),
                System.getenv("lab7_JDBC_PW")))
        {
            System.out.println("Connected");
        } catch (Exception e)
        {
            System.out.println("Did not Connect");
        }
    }
}
