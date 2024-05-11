import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Represents a service that can be available at a shelter or a camp. */
public class Service {

  private String serviceName;

  public String getServiceName() {
    return this.serviceName;
  }

  private int inspectionFrequency;

  public int getInspectionFrequency() {
    return this.inspectionFrequency;
  }

  /**
   * Creates a new service with the specified name and inspection frequency.
   *
   * @param serviceName The name of the service.
   * @param inspectionFrequency How frequently the service needs inspection.
   */
  public Service(String serviceName, int inspectionFrequency) {
    this.serviceName = serviceName;
    this.inspectionFrequency = inspectionFrequency;
  }

  /**
   * Defines the service by adding it to the system or updating its inspection frequency.
   *
   * @return True if the service was successfully defined or updated.
   * @throws SQLException If a SQL exception occurs.
   * @throws IOException If an I/O exception occurs.
   * @throws ClassNotFoundException If the required class is not found.
   */
  public boolean defineService() throws SQLException, IOException, ClassNotFoundException {
    HelperMethod helperMethod = new HelperMethod();
    Connection connection = null;

    try {
      connection = DatabaseManager.getConnection();

      /* If service does not exist, add service */
      if (helperMethod.isServiceExists(serviceName) == Constants.SERVICE_NOT_FOUND) {
        PreparedStatement insertService =
            connection.prepareStatement("insert into service (name, frequency) values (?, ?)");
        insertService.setString(1, serviceName);
        insertService.setInt(2, inspectionFrequency);
        insertService.executeUpdate();
      }
      /* If service exists, update the frequency */
      else {
        PreparedStatement updateService =
            connection.prepareStatement("update service set frequency = ? where name = ?;");
        updateService.setInt(1, inspectionFrequency);
        updateService.setString(2, serviceName);
        updateService.executeUpdate();
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
   * Returns a string representation of the Service object.
   *
   * @return String representation of the Service.
   */
  @Override
  public String toString() {
    return "Service{"
        + "serviceName='"
        + serviceName
        + '\''
        + ", inspectionFrequency="
        + inspectionFrequency
        + '}';
  }
}
