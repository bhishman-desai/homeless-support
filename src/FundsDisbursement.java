import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * FundsDisbursement class represents a record of funds being disbursed to a shelter, capturing
 * details such as the shelter receiving the funds, disbursement date, and the amount of funds. It
 * provides a method to disburse funds and record the transaction in the system.
 */
public class FundsDisbursement {
  private String shelterReceiving;
  private String date;
  private int funds;

  /**
   * Constructor for creating a new FundsDisbursement instance.
   *
   * @param shelterReceiving The name of the shelter receiving the funds.
   * @param date The disbursement date.
   * @param funds The amount of funds being disbursed.
   */
  public FundsDisbursement(String shelterReceiving, String date, int funds) {
    this.shelterReceiving = shelterReceiving;
    this.date = date;
    this.funds = funds;
  }

  /**
   * Disburses funds to the specified shelter and records the transaction in the system.
   *
   * @return True if the funds are successfully disbursed and recorded; false otherwise.
   * @throws IOException If there is an issue with input/output operations.
   * @throws SQLException If there is an issue with the SQL operations.
   * @throws ClassNotFoundException If the required class is not found during database operations.
   */
  public boolean disburseFunds() throws IOException, SQLException, ClassNotFoundException {
    HelperMethod helperMethod = new HelperMethod();
    Connection connection = null;

    try {
      connection = DatabaseManager.getConnection();

      /* Validating the shelter */
      int shelterID = helperMethod.isShelterExists(shelterReceiving);
      if (shelterID == Constants.SHELTER_NOT_FOUND) {
        return false;
      }

      PreparedStatement insertDisburseFund =
          connection.prepareStatement(
              "insert into disburse_fund_record (date, funds, shelter_id) values (?, ?,?)");
      insertDisburseFund.setDate(1, Date.valueOf(date));
      insertDisburseFund.setInt(2, funds);
      insertDisburseFund.setInt(3, shelterID);
      insertDisburseFund.executeUpdate();

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
   * Returns a string representation of the FundsDisbursement object.
   *
   * @return A string containing the shelter receiving funds, disbursement date, and the amount of
   *     funds.
   */
  @Override
  public String toString() {
    return "FundsDisbursement{"
        + "shelterReceiving='"
        + shelterReceiving
        + '\''
        + ", date='"
        + date
        + '\''
        + ", funds="
        + funds
        + '}';
  }
}
