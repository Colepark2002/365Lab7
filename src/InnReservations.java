import java.sql.*;
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
        test.roomsAndRates();
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

    public void roomsAndRates() throws SQLException {
        String query = "SELECT RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor,"
                + " SUM(DATEDIFF(res.Checkout, res.CheckIn)) / 180.0 AS RoomPopularityScore,"
                + " MIN(res.CheckIn) AS NextAvailableCheckIn,"
                + " MAX(res.Checkout) AS MostRecentCheckout"
                + " FROM lab7_rooms "
                + " LEFT JOIN lab7_reservations res"
                + " ON RoomCode = res.Room"
                + " AND res.Checkout > NOW() - INTERVAL 180 DAY"
                + " GROUP BY RoomCode"
                + " ORDER BY RoomPopularityScore DESC";

        try (Connection conn = DriverManager.getConnection(System.getenv("lab7_JDBC_URL"), System.getenv("lab7_JDBC_USER"), System.getenv("lab7_JDBC_PW"));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Print column names
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rsmd.getColumnName(i) + " ");
            }
            System.out.println();

            // Print room data
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + " ");
                }
                System.out.println();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
