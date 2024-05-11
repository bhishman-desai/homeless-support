import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HelperMethod class provides various helper methods for checking the existence of entities in the
 * system, date validation, identifying non-duplicate elements, and leap year determination.
 */
public class HelperMethod {
  Connection connection = null;

  /**
   * Checks if a service with the given name exists in the system.
   *
   * @param serviceName The name of the service to check.
   * @return The service ID if the service exists; otherwise, {@code Constants.SERVICE_NOT_FOUND}.
   * @throws SQLException If there is an issue with the SQL operations.
   * @throws IOException If there is an issue with input/output operations.
   * @throws ClassNotFoundException If the required class is not found during database operations.
   */
  public int isServiceExists(String serviceName)
      throws SQLException, IOException, ClassNotFoundException {

    /* Extra Check since the method is public */
    if (serviceName == null || serviceName.isEmpty()) {
      return Constants.SERVICE_NOT_FOUND;
    }

    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement isServiceExists =
          connection.prepareStatement("select service_id from service where name = ?;");
      isServiceExists.setString(1, serviceName);
      ResultSet isServiceExistsResultSet = isServiceExists.executeQuery();
      if (isServiceExistsResultSet.next()) {
        return isServiceExistsResultSet.getInt("service_id");
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
    return Constants.SERVICE_NOT_FOUND;
  }

  /**
   * Checks if a shelter with the given name exists in the system.
   *
   * @param shelterName The name of the shelter to check.
   * @return The shelter ID if the shelter exists; otherwise, {@code Constants.SHELTER_NOT_FOUND}.
   * @throws SQLException If there is an issue with the SQL operations.
   * @throws IOException If there is an issue with input/output operations.
   * @throws ClassNotFoundException If the required class is not found during database operations.
   */
  public int isShelterExists(String shelterName)
      throws SQLException, IOException, ClassNotFoundException {

    /* Extra Check since the method is public */
    if (shelterName == null || shelterName.isEmpty()) {
      return Constants.SHELTER_NOT_FOUND;
    }

    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement isShelterExists =
          connection.prepareStatement("select shelter_id from shelter where name = ?;");
      isShelterExists.setString(1, shelterName);
      ResultSet isShelterExistsResultSet = isShelterExists.executeQuery();
      if (isShelterExistsResultSet.next()) {
        return isShelterExistsResultSet.getInt("shelter_id");
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
    return Constants.SHELTER_NOT_FOUND;
  }

  /**
   * Checks if a staff with the given name exists in the system.
   *
   * @param staffName The name of the staff to check.
   * @return The staff ID if the staff exists; otherwise, {@code Constants.STAFF_NOT_FOUND}.
   * @throws SQLException If there is an issue with the SQL operations.
   * @throws IOException If there is an issue with input/output operations.
   * @throws ClassNotFoundException If the required class is not found during database operations.
   */
  public int isStaffExists(String staffName)
      throws SQLException, IOException, ClassNotFoundException {

    /* Extra Check since the method is public */
    if (staffName == null || staffName.isEmpty()) {
      return Constants.STAFF_NOT_FOUND;
    }

    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement isStaffExists =
          connection.prepareStatement("select staff_id from staff where name = ?;");
      isStaffExists.setString(1, staffName);
      ResultSet isStaffExistsResultSet = isStaffExists.executeQuery();
      if (isStaffExistsResultSet.next()) {
        return isStaffExistsResultSet.getInt("staff_id");
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
    return Constants.STAFF_NOT_FOUND;
  }

  /**
   * Checks if a donor with the given name exists in the system.
   *
   * @param donorName The name of the donor to check.
   * @return The donor ID if the donor exists; otherwise, {@code Constants.DONOR_NOT_FOUND}.
   * @throws SQLException If there is an issue with the SQL operations.
   * @throws IOException If there is an issue with input/output operations.
   * @throws ClassNotFoundException If the required class is not found during database operations.
   */
  public int isDonorExists(String donorName)
      throws SQLException, IOException, ClassNotFoundException {

    /* Extra Check since the method is public */
    if (donorName == null || donorName.isEmpty()) {
      return Constants.DONOR_NOT_FOUND;
    }

    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement isDonorExists =
          connection.prepareStatement("select donor_id from donor where name = ?;");
      isDonorExists.setString(1, donorName);
      ResultSet isDonorExistsResultSet = isDonorExists.executeQuery();
      if (isDonorExistsResultSet.next()) {
        return isDonorExistsResultSet.getInt("donor_id");
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
    return Constants.DONOR_NOT_FOUND;
  }

  /**
   * Checks if a given date string is invalid.
   *
   * @param date The date string to check.
   * @return True if the date is invalid; otherwise, false.
   */
  public boolean isDateInvalid(String date) {

    /* Extra Check since the method is public */
    if (date == null || date.isEmpty()) {
      return false;
    }

    try {
      /* Try parsing the date with the defined format */
      LocalDate.parse(date, Constants.DATE_FORMATTER);
      return false; /* Parsing successful, date is valid */
    } catch (Exception e) {
      return true; /* Parsing failed, date is invalid */
    }
  }

  /**
   * Finds the first non-duplicate elements from an array that are not in the list.
   *
   * @param array The array to inspect.
   * @param list The list to compare against.
   * @param number The maximum number of non-duplicate elements to find.
   * @return A list of non-duplicate elements.
   */
  public List<String> findFirstNonDuplicate(String[] array, List<String> list, int number) {

    /* Extra check as the method is public: If the earlier pair is larger than the inspection limit */
    if (number < 0 || array == null || list == null || list.isEmpty()) {
      return null;
    }
    /* Get all non-duplicate elements from an array which are not in the list */
    Set<String> set = new LinkedHashSet<>(Arrays.asList(array));
    set.removeAll(new HashSet<>(list));

    int count = Math.min(number, set.size());
    return set.stream().limit(count).collect(Collectors.toList());
  }

  /**
   * Checks if a given year is a leap year.
   *
   * @param dateString The date string representing the year.
   * @return True if the year is a leap year; otherwise, false.
   */
  public boolean isLeapYear(String dateString) {

    /* Extra Check since the method is public */
    if (dateString == null || dateString.isEmpty()) {
      return false;
    }

    try {
      LocalDate date = LocalDate.parse(dateString);

      return date.isLeapYear();
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Checks if a set of strings is not empty.
   *
   * @param stringSet The set of strings to be checked.
   * @return {@code true} if the set is not empty and contains no empty or null strings, {@code
   *     false} otherwise.
   */
  public boolean isSetEmpty(Set<String> stringSet) {
    /* Extra Check since the method is public */
    if (stringSet == null) {
      return false;
    }

    for (String string : stringSet) {
      if (string == null || string.isEmpty()) {
        return true;
      }
    }

    return false;
  }
}
