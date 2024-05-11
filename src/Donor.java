import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Set;

/**
 * Donor class represents a donor entity with information such as name, location, contact details,
 * funding programs, and donation amounts. It implements the Locatable interface to provide
 * location-related functionality.
 */
public class Donor implements Locatable {
  private String name;
  private Point centralOffice;
  private String contact;
  private Set<String> fundingPrograms;
  private int donation;
  private HelperMethod helperMethod = new HelperMethod();
  private Connection connection = null;

  public int getDonation() {
    return donation;
  }

  public void setDonation(int donation) {
    this.donation = donation;
  }

  /** Default constructor for the Donor class. */
  public Donor() {}

  /**
   * Parameterized constructor for creating a new Donor instance with specified details.
   *
   * @param name The name of the donor.
   * @param centralOffice The central office location of the donor.
   * @param contact The contact details of the donor.
   * @param fundingPrograms The set of funding programs associated with the donor.
   */
  public Donor(String name, Point centralOffice, String contact, Set<String> fundingPrograms) {
    this.name = name;
    this.centralOffice = centralOffice;
    this.contact = contact;
    this.fundingPrograms = fundingPrograms;
  }

  /**
   * Defines a new donor or updates existing donor information in the system, including associated
   * funding programs.
   *
   * @return True if the donor information is successfully defined or updated; false otherwise.
   * @throws SQLException If there is an issue with the SQL operations.
   * @throws IOException If there is an issue with input/output operations.
   * @throws ClassNotFoundException If the required class is not found during database operations.
   */
  public boolean defineDonor() throws SQLException, IOException, ClassNotFoundException {
    try {
      connection = DatabaseManager.getConnection();

      int donorID = helperMethod.isDonorExists(name);
      /* If the donor doesn't exist, add that donor to the dB */
      if (donorID == Constants.DONOR_NOT_FOUND) {
        PreparedStatement insertDonor =
            connection.prepareStatement(
                "insert into donor (name, locationX, locationY, contact) values (?, ?, ?, ?)");
        insertDonor.setString(1, name);
        insertDonor.setInt(2, getLocation().getX());
        insertDonor.setInt(3, getLocation().getY());
        insertDonor.setString(4, contact);
        insertDonor.executeUpdate();

        donorID = helperMethod.isDonorExists(name);
        insertFundingPrograms(connection, donorID);

      }
      /* If the donor already exists, update the information */
      else {

        PreparedStatement updateDonor =
            connection.prepareStatement(
                "update donor set locationX = ?, locationY = ?, contact = ? where donor_id = ?;");
        updateDonor.setInt(1, getLocation().getX());
        updateDonor.setInt(2, getLocation().getY());
        updateDonor.setString(3, contact);
        updateDonor.setInt(4, donorID);
        updateDonor.executeUpdate();

        PreparedStatement deleteFundingProgramForDonor =
            connection.prepareStatement("delete from funding_program where donor_id = ?;");
        deleteFundingProgramForDonor.setInt(1, donorID);
        deleteFundingProgramForDonor.executeUpdate();

        insertFundingPrograms(connection, donorID);
      }

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
   * Inserts funding programs associated with a donor into the database.
   *
   * @param connection The database connection.
   * @param donorID The ID of the donor.
   * @throws SQLException If there is an issue with the SQL operations.
   */
  private void insertFundingPrograms(Connection connection, int donorID) throws SQLException {
    for (String program : fundingPrograms) {
      PreparedStatement insertProgram =
          connection.prepareStatement("insert into funding_program (name, donor_id) values (?, ?)");
      insertProgram.setString(1, program);
      insertProgram.setInt(2, donorID);
      insertProgram.executeUpdate();
    }
  }

  /**
   * Generates a report of donations made by the donor within a specified date range.
   *
   * @param startDate The start date of the reporting period.
   * @param endDate The end date of the reporting period.
   * @param outstream The PrintWriter to write the report output.
   * @throws SQLException If there is an issue with the SQL operations.
   * @throws IOException If there is an issue with input/output operations.
   * @throws ClassNotFoundException If the required class is not found during database operations.
   */
  public void donorReport(String startDate, String endDate, PrintWriter outstream)
      throws SQLException, IOException, ClassNotFoundException {
    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement donorReportQuery =
          connection.prepareStatement(
              """
                          SELECT d.name AS donor_name, fp.name AS program_name, SUM(rdr.donation) AS total_donation
                          FROM donor d
                               JOIN funding_program fp ON d.donor_id = fp.donor_id
                               JOIN receive_donation_record rdr ON fp.program_id = rdr.program_id
                          WHERE rdr.date BETWEEN ? AND ?
                          GROUP BY d.name, fp.name
                          ORDER BY d.name, fp.name
                      """);
      donorReportQuery.setDate(1, Date.valueOf(startDate));
      donorReportQuery.setDate(2, Date.valueOf(endDate));

      ResultSet donorReportResultSet = donorReportQuery.executeQuery();

      String currentDonor = null;
      while (donorReportResultSet.next()) {
        String donorName = donorReportResultSet.getString("donor_name");
        String programName = donorReportResultSet.getString("program_name");
        int totalDonation = donorReportResultSet.getInt("total_donation");

        if (!donorName.equals(currentDonor)) {
          if (currentDonor != null) {
            /* Print a blank line between donors (except for the first donor) */
            outstream.println();
          }
          /* Start a new donor block */
          outstream.println(donorName);
          currentDonor = donorName;
        }

        /* Print funding program details */
        outstream.printf("\t%s\t%d%n", programName, totalDonation);
      }
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
  }

  /**
   * Gets the location of the donor.
   *
   * @return The location of the donor as a Point object.
   */
  @Override
  public Point getLocation() {
    return centralOffice;
  }

  /**
   * Returns a string representation of the Donor object.
   *
   * @return A string containing donor name, central office location, contact details, funding
   *     programs, and donation amount.
   */
  @Override
  public String toString() {
    return "Donor{"
        + "name='"
        + name
        + '\''
        + ", centralOffice="
        + centralOffice.getX()
        + " "
        + centralOffice.getY()
        + ", contact='"
        + contact
        + '\''
        + ", fundingPrograms="
        + fundingPrograms
        + " donation="
        + donation
        + '}';
  }
}
