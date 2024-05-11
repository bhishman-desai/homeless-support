import java.io.IOException;
import java.sql.*;

/**
 * DonationRecord class represents a record of a donation, capturing details such as donor
 * information, funding program, donation date, and amount. It provides a method to receive and
 * record donations in the system.
 */
public class DonationRecord {
  private String donor;
  private String fundingProgram;
  private String date;
  private int donation;

  /**
   * Constructor for creating a new DonationRecord instance.
   *
   * @param donor The name of the donor.
   * @param fundingProgram The name of the funding program.
   * @param date The date of the donation.
   * @param donation The amount of the donation.
   */
  public DonationRecord(String donor, String fundingProgram, String date, int donation) {
    this.donor = donor;
    this.fundingProgram = fundingProgram;
    this.date = date;
    this.donation = donation;
  }

  /**
   * Receives and records a donation in the system. Checks if the donor and funding program are
   * valid and associated with each other before recording the donation.
   *
   * @return True if the donation is successfully recorded; false otherwise.
   * @throws SQLException If there is an issue with the SQL operations.
   * @throws IOException If there is an issue with input/output operations.
   * @throws ClassNotFoundException If the required class is not found during database operations.
   */
  public boolean receiveDonation() throws SQLException, IOException, ClassNotFoundException {
    HelperMethod helperMethod = new HelperMethod();
    Connection connection = null;

    try {
      connection = DatabaseManager.getConnection();

      /* Checking if donor exists in the system */
      int donorID = helperMethod.isDonorExists(donor);
      if (donorID == Constants.DONOR_NOT_FOUND) {
        return false;
      }

      /* Checking if program is associated with the given donor */
      PreparedStatement isProgramDonorAssociated =
          connection.prepareStatement(
              "select program_id from funding_program where name = ? and donor_id = ?;");
      isProgramDonorAssociated.setString(1, fundingProgram);
      isProgramDonorAssociated.setInt(2, donorID);
      ResultSet isProgramDonorAssociatedResultSet = isProgramDonorAssociated.executeQuery();
      if (!isProgramDonorAssociatedResultSet.next()) {
        return false;
      }
      int programID = isProgramDonorAssociatedResultSet.getInt("program_id");
      PreparedStatement insertReceiveDonationRecord =
          connection.prepareStatement(
              "insert into receive_donation_record (date, donation, donor_id, program_id) values (?, ?, ?, ?)");
      insertReceiveDonationRecord.setDate(1, Date.valueOf(date));
      insertReceiveDonationRecord.setInt(2, donation);
      insertReceiveDonationRecord.setInt(3, donorID);
      insertReceiveDonationRecord.setInt(4, programID);
      insertReceiveDonationRecord.executeUpdate();

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
   * Returns a string representation of the DonationRecord object.
   *
   * @return A string containing donor information, funding program, donation date, and amount.
   */
  @Override
  public String toString() {
    return "DonationRecord{"
        + "donor='"
        + donor
        + '\''
        + ", fundingProgram='"
        + fundingProgram
        + '\''
        + ", date='"
        + date
        + '\''
        + ", donation="
        + donation
        + '}';
  }
}
