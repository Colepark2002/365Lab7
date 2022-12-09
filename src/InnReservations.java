import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class InnReservations
{
    public static void main(String[] args) throws SQLException {
        InnReservations test = new InnReservations();
        System.out.println("Rooms and Rates: 1");
        System.out.println("Reservations: 2");
        System.out.println("Revenue: 3");
        System.out.print("Type the number to the right of the colon to access the desired option: ");
        Scanner sc = new Scanner(System.in);
        int input = sc.nextInt();
        System.out.println(""+input);
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
