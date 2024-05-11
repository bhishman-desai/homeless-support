import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents staff members in the system with information such as name, services, volunteer status,
 * and manager.
 */
public class Staff {
  private String name;
  private Set<String> services;
  private boolean volunteer;
  private String manager;
  private HelperMethod helperMethod = new HelperMethod();
  private Connection connection = null;

  /** Default constructor for the Staff class. */
  public Staff() {}

  /**
   * Parameterized constructor for the Staff class.
   *
   * @param name the name of the staff member.
   * @param services the set of services provided by the staff member.
   * @param volunteer true if the staff member is a volunteer; false otherwise.
   * @param manager the name of the manager overseeing the staff member.
   */
  public Staff(String name, Set<String> services, boolean volunteer, String manager) {
    this.name = name;
    this.services = services;
    this.volunteer = volunteer;
    this.manager = manager;
  }

  /**
   * Adds a new staff member or updates existing staff information in the system.
   *
   * @return true if the operation is successful; false otherwise.
   * @throws SQLException if a SQL error occurs.
   * @throws IOException if an I/O error occurs.
   * @throws ClassNotFoundException if the required class is not found.
   */
  public boolean addStaff() throws SQLException, IOException, ClassNotFoundException {
    try {
      connection = DatabaseManager.getConnection();

      /* Checking if services exist in the system */
      for (String service : services) {
        if (helperMethod.isServiceExists(service) == Constants.SERVICE_NOT_FOUND) {
          return false;
        }
      }

      /* Checking if the manager exits in the system */
      int managerID = helperMethod.isStaffExists(manager);
      if (managerID == Constants.STAFF_NOT_FOUND) {
        return false;
      }

      int staffID = helperMethod.isStaffExists(name);
      /* If the staff doesn't exist, add a new staff to dB */
      if (staffID == Constants.STAFF_NOT_FOUND) {
        PreparedStatement insertStaff =
            connection.prepareStatement(
                "insert into staff (name, is_volunteer, manager_id) values (?, ?, ?)");
        insertStaff.setString(1, name);
        insertStaff.setBoolean(2, volunteer);
        insertStaff.setInt(3, managerID);
        insertStaff.executeUpdate();

        staffID = helperMethod.isStaffExists(name);
        insertStaffForService(connection, staffID);
      }
      /* If staff already exists, update the information */
      else {
        PreparedStatement updateStaff =
            connection.prepareStatement(
                "update staff set is_volunteer = ?, manager_id = ? where staff_id = ?;");
        updateStaff.setBoolean(1, volunteer);
        updateStaff.setInt(2, managerID);
        updateStaff.setInt(3, staffID);
        updateStaff.executeUpdate();

        PreparedStatement deleteServiceForStaffEntries =
            connection.prepareStatement("delete from staff_for_service where staff_id = ?;");
        deleteServiceForStaffEntries.setInt(1, staffID);
        deleteServiceForStaffEntries.executeUpdate();

        insertStaffForService(connection, staffID);
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
   * Inserts staff-service pairs into the database for a given staff member.
   *
   * @param connection the database connection.
   * @param staffID the ID of the staff member.
   * @throws SQLException if a SQL error occurs.
   * @throws IOException if an I/O error occurs.
   * @throws ClassNotFoundException if the required class is not found.
   */
  private void insertStaffForService(Connection connection, int staffID)
      throws SQLException, IOException, ClassNotFoundException {
    for (String service : services) {
      int serviceID = helperMethod.isServiceExists(service);
      PreparedStatement insertStaffForService =
          connection.prepareStatement(
              "insert into staff_for_service (staff_id, service_id) values (?, ?)");
      insertStaffForService.setInt(1, staffID);
      insertStaffForService.setInt(2, serviceID);
      insertStaffForService.executeUpdate();
    }
  }

  /**
   * Generates an inspection schedule for staff members based on service and shelter pairs.
   *
   * @param scheduleDays the number of days to schedule inspections.
   * @param inspectLimit the maximum number of inspections per day.
   * @return a map containing staff names and their corresponding inspection schedules.
   * @throws ClassNotFoundException if the required class is not found.
   * @throws IOException if an I/O error occurs.
   * @throws SQLException if a SQL error occurs.
   */
  public Map<String, List<String>> inspectionSchedule(int scheduleDays, int inspectLimit)
      throws ClassNotFoundException, IOException, SQLException {

    Map<String, List<String>> result = new HashMap<>();
    Map<Pair<String, String>, Service> staffServiceShelterMap = getAllStaffServiceShelterPairs();

    for (Pair<String, String> key : staffServiceShelterMap.keySet()) {

      /* Getting the name of the staff and creating an empty list of String for that staff */
      String staffName = key.getKey();
      result.computeIfAbsent(staffName, k -> initializeScheduleList(scheduleDays));

      /* Getting the service which needs to be inspected by this staff */
      Service service = staffServiceShelterMap.get(key);

      /* Recursive call to schedule the service at a shelter by the staff starting from tomorrow (assuming services have been inspected for today) */
      scheduleService(result.get(staffName), key, service, scheduleDays, 1);
    }

    /* Once the scheduling is done, now we want to check if the inspection matches the inspectLimit. And if not, we shift the inspection to earlier days */
    try {
      shiftPairs(result, inspectLimit);
    } catch (IllegalArgumentException illegalArgumentException) {
      return null;
    }

    return result;
  }

  /**
   * Initializes a list of empty strings representing inspection schedules for a given number of
   * days.
   *
   * @param scheduleDays the number of days to initialize.
   * @return a list of empty strings representing inspection schedules.
   */
  private List<String> initializeScheduleList(int scheduleDays) {
    List<String> scheduleList = new ArrayList<>();
    for (int day = 0; day < scheduleDays; day++) {
      scheduleList.add("");
    }
    return scheduleList;
  }

  /**
   * Schedules inspections for a given service and shelter pair on specific days.
   *
   * @param scheduleList the list representing the inspection schedule for a staff member.
   * @param key the staff-service pair.
   * @param service the service to be inspected.
   * @param scheduleDays the total number of days for scheduling.
   * @param currentDay the current day for scheduling.
   */
  private void scheduleService(
      List<String> scheduleList,
      Pair<String, String> key,
      Service service,
      int scheduleDays,
      int currentDay) {
    /* If the day of inspection is within the scheduleDays limit */
    if (currentDay <= scheduleDays) {
      int inspectionFrequency = service.getInspectionFrequency();
      /* Scheduling the inspection according to the inspection frequency (periodically) */
      if (currentDay % inspectionFrequency == 0) {
        String shelterServicePair = "(" + key.getValue() + "," + service.getServiceName() + ")";
        String existingString = scheduleList.get(currentDay - 1);
        /* If there is already an inspection scheduled, we add to that inspection with a space */
        scheduleList.set(
            currentDay - 1,
            existingString.isEmpty()
                ? shelterServicePair
                : existingString + " " + shelterServicePair);
      }

      /* Recursively checking for the next day */
      scheduleService(scheduleList, key, service, scheduleDays, currentDay + 1);
    }
  }

  /**
   * Shifts inspection pairs to earlier days to meet the inspectLimit constraint.
   *
   * @param staffScheduleMap the map containing staff names and their inspection schedules.
   * @param inspectLimit the maximum number of inspections per day.
   */
  private void shiftPairs(Map<String, List<String>> staffScheduleMap, int inspectLimit) {
    for (Map.Entry<String, List<String>> entry : staffScheduleMap.entrySet()) {
      List<String> values = entry.getValue();

      /* Starting from the last pair as we need to schedule to an earlier day */
      for (int i = values.size() - 1; i >= 0; i--) {
        String pairsString = values.get(i);
        String[] pairs = pairsString.split("[)]");

        /* Adding ) again */
        for (int j = 0; j < pairs.length; j++) {
          pairs[j] = pairs[j].trim() + ")";
        }

        /* Check if shifting is required for each pair on each day */
        if (pairs.length > inspectLimit) {
          /* Try to shift pairs to an earlier day */
          try {
            shiftPairsToEarlierDay(staffScheduleMap, entry.getKey(), i, pairs, inspectLimit);
          } catch (IllegalArgumentException illegalArgumentException) {
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
          }
        }
      }
    }
  }

  /**
   * Shifts inspection pairs to earlier days while considering inspectLimit constraints.
   *
   * @param staffScheduleMap the map containing staff names and their inspection schedules.
   * @param key the staff name for which pairs are being shifted.
   * @param currentIndex the current index of the day being considered.
   * @param currentPairs the pairs scheduled for the current day.
   * @param inspectLimit the maximum number of inspections per day.
   */
  private void shiftPairsToEarlierDay(
      Map<String, List<String>> staffScheduleMap,
      String key,
      int currentIndex,
      String[] currentPairs,
      int inspectLimit) {

    for (int earlierIndex = currentIndex - 1; earlierIndex >= 0; earlierIndex--) {

      /* Getting the earlier day's pairs */
      List<String> earlierPairs =
          new ArrayList<>(Arrays.asList(staffScheduleMap.get(key).get(earlierIndex).split("[)]")));
      earlierPairs.replaceAll(s -> s.trim() + ")");

      int ELEMENTS_TO_BE_ADDED = inspectLimit - earlierPairs.size();

      /* When there are no elements to be added, we move to earlier day */
      if (ELEMENTS_TO_BE_ADDED < 0) {
        continue;
      }

      /* Check if shifting satisfies constraints */
      List<String> elementsToAppend;
      try {
        elementsToAppend =
            helperMethod.findFirstNonDuplicate(currentPairs, earlierPairs, ELEMENTS_TO_BE_ADDED);
      } catch (IllegalArgumentException illegalArgumentException) {
        throw new IllegalArgumentException(illegalArgumentException.getMessage());
      }

      /* When the schedule moves to day one and still can't fit the limit, we return null */
      if (earlierIndex == 0 && currentPairs.length - elementsToAppend.size() > inspectLimit) {
        throw new IllegalArgumentException();
      }

      if (elementsToAppend == null || elementsToAppend.isEmpty()) {
        continue;
      }

      /* Add the pairs to earlier day */
      earlierPairs.addAll(elementsToAppend);

      /* Remove the pairs from current day */
      currentPairs =
          Arrays.stream(currentPairs)
              .filter(element -> !elementsToAppend.contains(element))
              .toArray(String[]::new);

      /* Update the pairs in the map */
      staffScheduleMap.get(key).set(earlierIndex, String.join(" ", earlierPairs));
      staffScheduleMap.get(key).set(currentIndex, String.join(" ", currentPairs));

      elementsToAppend.clear();
    }
  }

  /**
   * Retrieves staff-service-shelter pairs from the database.
   *
   * @return a map containing staff-service-shelter pairs and corresponding services.
   * @throws SQLException if a SQL error occurs.
   * @throws IOException if an I/O error occurs.
   * @throws ClassNotFoundException if the required class is not found.
   */
  private Map<Pair<String, String>, Service> getAllStaffServiceShelterPairs()
      throws SQLException, IOException, ClassNotFoundException {
    Map<Pair<String, String>, Service> result = new HashMap<>();
    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement getAllStaffServiceShelters =
          connection.prepareStatement(
              """
                 SELECT
                     s.name AS staff_name,
                     sh.name AS shelter_name,
                     srv.name AS service_name,
                     srv.frequency
                 FROM
                     staff s
                 JOIN
                     staff_for_service sf ON s.staff_id = sf.staff_id
                 JOIN
                     service srv ON sf.service_id = srv.service_id
                 JOIN
                     service_for_shelter ss ON srv.service_id = ss.service_id
                 JOIN
                     shelter sh ON ss.shelter_id = sh.shelter_id;
                    """);
      ResultSet resultSet = getAllStaffServiceShelters.executeQuery();
      while (resultSet.next()) {
        String staffName = resultSet.getString("staff_name");
        String shelterName = resultSet.getString("shelter_name");
        String serviceName = resultSet.getString("service_name");
        int frequency = resultSet.getInt("frequency");
        Pair<String, String> pair = new Pair<>(staffName, shelterName);
        Service service = new Service(serviceName, frequency);
        result.put(pair, service);
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

    return result;
  }

  /**
   * Converts the Staff object to its string representation.
   *
   * @return a string representation of the Staff object.
   */
  @Override
  public String toString() {
    return "Staff{"
        + "name='"
        + name
        + '\''
        + ", services="
        + services
        + ", volunteer="
        + volunteer
        + ", manager='"
        + manager
        + '\''
        + '}';
  }
}
