import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This class provides methods for managing database connections. The use of static in the
 * DatabaseManager class aligns with the utility class pattern, providing global accessibility for
 * managing database connections.
 */
public class DatabaseManager {

  public static Connection getConnection()
      throws IOException, ClassNotFoundException, SQLException {
    Properties identity = new Properties();

    String propertyFilename = "credentials.prop";

    try (InputStream stream = new FileInputStream(propertyFilename)) {
      identity.load(stream);
    } catch (IOException ioException) {
      throw new IOException(ioException.getMessage());
    }
    String URL = identity.getProperty("url");
    String username = identity.getProperty("username");
    String password = identity.getProperty("password");

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException classNotFoundException) {
      throw new ClassNotFoundException(classNotFoundException.getMessage());
    }

    return DriverManager.getConnection(URL, username, password);
  }

  /**
   * Closes the provided database connection.
   *
   * @param connection The Connection object to be closed.
   * @throws SQLException If there is an issue closing the database connection.
   */
  public static void closeConnection(Connection connection) throws SQLException {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException sqlException) {
        throw new SQLException(sqlException.getMessage());
      }
    }
  }
}
