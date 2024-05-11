import java.io.IOException;
import java.sql.*;

/**
 * The OccupancyRecord class represents the occupancy record of a shelter or camp on a specific
 * date. It provides methods to declare and validate shelter occupancy.
 */
public class OccupancyRecord {
  private String name;
  private String date;
  private int occupancy;

  /**
   * Constructor to initialize an OccupancyRecord object with shelter name, date, and occupancy.
   *
   * @param name Name of the shelter or camp
   * @param date Date of the occupancy record
   * @param occupancy Occupancy value for the shelter or camp on the specified date
   */
  public OccupancyRecord(String name, String date, int occupancy) {
    this.name = name;
    this.date = date;
    this.occupancy = occupancy;
  }

  /**
   * Declares the shelter or camp occupancy for a specific date, validating constraints.
   *
   * @return true if the entry was successful, false otherwise
   * @throws SQLException If a database access error occurs
   * @throws IOException If an I/O error occurs
   * @throws ClassNotFoundException If the class is not found
   */
  public boolean declareShelterOccupancy()
      throws SQLException, IOException, ClassNotFoundException {
    HelperMethod helperMethod = new HelperMethod();
    Connection connection = null;

    try {
      connection = DatabaseManager.getConnection();

      /* Validating the shelter */
      int shelterID = helperMethod.isShelterExists(name);
      if (shelterID == Constants.SHELTER_NOT_FOUND) {
        return false;
      }

      /* Check constraint to ensure 365 occupancy figures for each shelter in a non-leap year */
      PreparedStatement shelterOccupancyInAYear =
          connection.prepareStatement(
              "SELECT COUNT(*) FROM shelter_occupancy_record WHERE shelter_id = ? AND YEAR(date) = YEAR(?);");
      shelterOccupancyInAYear.setInt(1, shelterID);
      shelterOccupancyInAYear.setDate(2, Date.valueOf(date));
      ResultSet shelterOccupancyInAYearResultSet = shelterOccupancyInAYear.executeQuery();
      if (shelterOccupancyInAYearResultSet.next()) {

        int shelterOccupancyInAYearCount = shelterOccupancyInAYearResultSet.getInt(1);
        if (helperMethod.isLeapYear(date)
            ? shelterOccupancyInAYearCount > 366
            : shelterOccupancyInAYearCount > 365) {
          return false;
        }
      }

      /* Checking if the occupancy is less than the max capacity */
      PreparedStatement shelterCapacity =
          connection.prepareStatement("select capacity from shelter where shelter_id = ?;");
      shelterCapacity.setInt(1, shelterID);
      ResultSet shelterCapacityResultSet = shelterCapacity.executeQuery();
      if (shelterCapacityResultSet.next()) {
        int shelterCapacityValue = shelterCapacityResultSet.getInt("capacity");
        if (shelterCapacityValue < occupancy) {
          return false;
        }
      }

      PreparedStatement insertShelterOccupancy =
          connection.prepareStatement(
              "insert into shelter_occupancy_record (date, occupancy, shelter_id) values (?, ?,?)");
      insertShelterOccupancy.setDate(1, Date.valueOf(date));
      insertShelterOccupancy.setInt(2, occupancy);
      insertShelterOccupancy.setInt(3, shelterID);
      insertShelterOccupancy.executeUpdate();

    } catch (SQLException sqlException) {
      throw new SQLException(sqlException.getMessage());
    } catch (IOException ioException) {
      throw new IOException(ioException.getMessage());
    } catch (ClassNotFoundException classNotFoundException) {
      throw new ClassNotFoundException(classNotFoundException.getMessage());
    } finally {
      /* Ensure the connection is closed */
      DatabaseManager.closeConnection(connection);
    }
    return true;
  }

  /**
   * Returns a string representation of the OccupancyRecord object.
   *
   * @return String representation of the object
   */
  @Override
  public String toString() {
    return "OccupancyRecord{"
        + "name='"
        + name
        + '\''
        + ", date='"
        + date
        + '\''
        + ", occupancy="
        + occupancy
        + '}';
  }
}
