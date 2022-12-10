import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
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
            System.out.println("Cancel Reservation: 4");
            System.out.println("Change Reservation: 5");
            System.out.println("Detailed Reservation Info: 5");
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
                    System.out.println("Rooms and Rates\n");
                    test.roomsAndRates();
                    break;

                case 2:
                    System.out.println("Reservations\n");
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
        System.out.println("Enter RoomCode: ");
        String roomcode = sc.nextLine();
        System.out.println("Enter Desired Bed Type: ");
        String bedTypeDes = sc.nextLine();
        System.out.println("Enter Begin Date [YYYY-MM-DD]: ");
        String checkIn = sc.nextLine();
        System.out.println("Enter End Date [YYYY-MM-DD]: ");
        String checkOut = sc.nextLine();
        System.out.println("Enter Number of Children: ");
        int numChild = sc.nextInt();
        System.out.println("Enter Number of Adults: ");
        int numAdult = sc.nextInt();

        int res_code = 0;
        double rate = 0.0;
        String bedType = "";
        int room_occ = 0;
        String roomName = "";


        try (Connection conn = DriverManager.getConnection(System.getenv("lab7_JDBC_URL"), System.getenv("lab7_JDBC_USER"), System.getenv("lab7_JDBC_PW"));
             Statement stmt = conn.createStatement();)
        {
            try(PreparedStatement prepared = conn.prepareStatement("select Room from lab7_reservations WHERE Room =? and CheckIn >= ? and CheckOut <= ?;");) {
                prepared.setString(1, roomcode);
                prepared.setDate(2, Date.valueOf(checkIn));
                prepared.setDate(3, Date.valueOf(checkOut));

                ResultSet rs = prepared.executeQuery();


                try (Statement stm2 = conn.createStatement())
                {
                    //Get max code from reservations to generate potential future next code.
                    String sql2 = "SELECT max(CODE) as CODE from lab7_reservations;";
                    ResultSet res_set2 = stm2.executeQuery(sql2);

                    while(res_set2.next())
                    {
                        String code = res_set2.getString("CODE");
                        res_code = Integer.parseInt(code) + 1;
                    }
                }

                catch (SQLException e)
                {
                    e.printStackTrace();
                }

                // Make sure inputted dates are at or later than the current date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate currDate = LocalDate.now();
                LocalDate checkInDate = LocalDate.parse(checkIn,formatter);
                LocalDate checkOutDate = LocalDate.parse(checkOut, formatter);
                int inCheck = currDate.compareTo(checkInDate);
                int outCheck = currDate.compareTo(checkOutDate);

                // if no reservation was found it means we can place a reservation at said date and room
                if (!rs.next() && (inCheck <= 0 && outCheck <= 0))
                {
                    try(PreparedStatement prep1 =
                                conn.prepareStatement("Select RoomName, maxOcc, basePrice, bedType from lab7_rooms where RoomId = ?;"))
                    {
                        prep1.setString(1,roomcode);

                        ResultSet rs2 = prep1.executeQuery();
                        while(rs2.next())
                        {
                            rate = Double.parseDouble(rs2.getString("basePrice"));
                            bedType = rs2.getString("bedType");
                            room_occ = Integer.parseInt(rs2.getString("maxOcc"));
                            roomName = rs2.getString("RoomName");
                        }

                        //Check if Room Occupancy is Large Enough
                        if(room_occ < numChild + numAdult)
                        {
                            System.out.println("\nWe cannot make this reservation as the max occupancy of the room is lower than your required occupancy\nPlease try again");
                        }
                        else
                        {
                            try(PreparedStatement prep2 =
                                        conn.prepareStatement("Insert into lab7_reservations (CODE, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids)" +
                                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"))
                            {
                                prep2.setInt(1, res_code);
                                prep2.setString(2, roomcode);
                                prep2.setDate(3, Date.valueOf(checkIn));
                                prep2.setDate(4, Date.valueOf(checkOut));
                                prep2.setDouble(5, rate);
                                prep2.setString(6, last);
                                prep2.setString(7, first);
                                prep2.setInt(8, numAdult);
                                prep2.setInt(9, numChild);



                                System.out.println("\nDo you want to make this reservation?: y or n");
                                String answer = sc.nextLine();

                                if(answer.equals("y"))
                                {
                                    prep2.executeUpdate();
                                    conn.commit();
                                    System.out.println("\nSuccessfully added the reservation with info: ");
                                    System.out.println("\n");
                                    System.out.println("First Name: " + first);
                                    System.out.println("LastName: " + last);
                                    System.out.println("RoomCode: " + roomcode);
                                    System.out.println("RoomName: " + roomName);
                                    System.out.println("BedType: " + bedType);
                                    System.out.println("StartDate: " + checkIn);
                                    System.out.println("EndDate: " + checkOut);
                                    System.out.println("Number of Kids: " + numChild);
                                    System.out.println("Number of Adults: " + numAdult);
                                    System.out.println("RESERVATION CODE: " + res_code);

                                    //calculate total cost of the stay
                                    double totalCost = 0;
                                    ZoneId timezone = ZoneId.of( "America/Los_Angeles" );
                                    for(LocalDate date = checkInDate; date.isBefore(checkOutDate); date = date.plusDays(1))
                                    {
                                        Calendar calender = Calendar.getInstance();
                                        ZonedDateTime zoned_time = date.atStartOfDay(timezone);
                                        Instant i = zoned_time.toInstant();

                                        java.util.Date d = java.util.Date.from(i);
                                        calender.setTime(d);

                                        int dayOfWeek = calender.get(Calendar.DAY_OF_WEEK);

                                        if(dayOfWeek >= 2 && dayOfWeek <=6)
                                        {
                                            // weekday case
                                            totalCost += (rate);
                                        }
                                        else if (dayOfWeek == 0 || dayOfWeek == 7)
                                        {
                                            // weekend case
                                            totalCost += (rate * 1.10);
                                        }
                                    }
                                    System.out.println("Total cost of stay: " + totalCost);
                                }
                                else
                                {
                                    System.out.println("\nReservation Canceled");
                                }

                            } catch(SQLException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            } catch(SQLException e)
            {
                e.printStackTrace();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void cancelReservation(int reservationCode) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Are you sure you want to cancel reservation " + reservationCode + "? (y/n)");
        String confirmation = scanner.nextLine();
        if (!confirmation.equalsIgnoreCase("y")) {
            return; // Cancel the operation
        }
        try (Connection conn = DriverManager.getConnection(System.getenv("lab7_JDBC_URL"), System.getenv("lab7_JDBC_USER"), System.getenv("lab7_JDBC_PW"));
             Statement stmt = conn.createStatement();) {
            PreparedStatement prepared = conn.prepareStatement("DELETE FROM lab7_reservations WHERE CODE = ?");
            prepared.setInt(1, reservationCode);
            prepared.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
