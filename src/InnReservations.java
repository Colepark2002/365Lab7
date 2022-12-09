import java.sql.*;
import java.util.Scanner;

public class InnReservations
{
    public static void main(String[] args) throws SQLException {
        InnReservations test = new InnReservations();
        boolean quit = false;
        while(!quit)
        {
            System.out.println("\n\nQuit: 0");
            System.out.println("Rooms and Rates: 1");
            System.out.println("Reservations: 2");
            System.out.println("Revenue: 3");
            System.out.print("Type the number to the right of the colon to access the desired option: ");
            Scanner sc = new Scanner(System.in);
            int input = sc.nextInt();
            System.out.println("" + input);
            switch (input)
            {
                case 0:
                    quit = true;
                    break;

                case 1:
                    test.roomsAndRates();
                    break;

                case 2:
                    System.out.println("Reservations");
                    test.reservations();
                    break;

                case 3:
                    System.out.println("Revenue");
                    break;

                default:
                    System.out.println("Invalid Input Please try again");
                    break;
            }
        }
    }

    public void roomsAndRates() throws SQLException {
        String query = "SELECT RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor,"
                + " IF(SUM(DATEDIFF(res.Checkout, res.CheckIn)) > 180, 180, SUM(DATEDIFF(res.Checkout, res.CheckIn))) / 180.0 AS RoomPopularityScore,"
                + " greatest(current_date, MAX(res.Checkout)) AS NextAvailableCheckIn,"
                + " MAX(res.Checkout) AS MostRecentCheckout,"
                + " DATEDIFF(MAX(res.CheckOut), MAX(res.CheckIn)) as MostRecentCheckoutDays"
                + " FROM lab7_rooms "
                + " JOIN lab7_reservations res"
                + " ON RoomCode = res.Room"
                + " AND res.Checkout > current_date - INTERVAL 180 DAY"
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
                    System.out.print(rs.getString(i) + ", ");
                }
                System.out.println();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void reservations() throws SQLException
    {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter First Name: ");
        String first = sc.nextLine();
        System.out.println("Enter Last Name: ");
        String last = sc.nextLine();
        System.out.println("Enter RoomCode or Any: ");
        String roomcode = sc.nextLine();
        System.out.println("Enter Desired Bed Type: ");
        String bedtype = sc.nextLine();
        System.out.println("Enter Begin Date: ");
        String checkIn = sc.nextLine();
        System.out.println("Enter End Date: ");
        String checkOut = sc.nextLine();
        System.out.println("Enter Number of Children: ");
        int numChild = sc.nextInt();
        System.out.println("Enter Number of Adults: ");
        int numAdult = sc.nextInt();


        try (Connection conn = DriverManager.getConnection(System.getenv("lab7_JDBC_URL"), System.getenv("lab7_JDBC_USER"), System.getenv("lab7_JDBC_PW"));
             Statement stmt = conn.createStatement();)
        {
            PreparedStatement prepared = conn.prepareStatement("SELECT * FROM lab7_rooms");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
