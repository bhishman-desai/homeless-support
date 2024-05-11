import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Represents a shelter or camp, providing methods to define, retrieve information, and analyze data
 * related to shelters.
 */
public class Shelter implements Locatable {

  private String name;

  public String getName() {
    return name;
  }

  private Point location;
  private int maxCapacity;

  public int getMaxCapacity() {
    return maxCapacity;
  }

  private String staffInCharge;
  private int fundsReceived;

  public int getFundsReceived() {
    return this.fundsReceived;
  }

  public void setFundsReceived(int fundsReceived) {
    this.fundsReceived = fundsReceived;
  }

  private HelperMethod helperMethod = new HelperMethod();
  private Connection connection = null;

  /** Constructs an empty Shelter object. */
  public Shelter() {}

  /**
   * Constructs a Shelter object with the specified attributes.
   *
   * @param name the name of the shelter.
   * @param location the location of the shelter.
   * @param maxCapacity the maximum capacity of the shelter.
   * @param staffInCharge the staff in charge of the shelter.
   */
  public Shelter(String name, Point location, int maxCapacity, String staffInCharge) {
    this.name = name;
    this.location = location;
    this.maxCapacity = maxCapacity;
    this.staffInCharge = staffInCharge;
  }

  /**
   * Returns the location of the shelter or camp.
   *
   * @return the location of the shelter.
   */
  @Override
  public Point getLocation() {
    return location;
  }

  /**
   * Defines a shelter by adding it to the database or updating its details if it already exists.
   *
   * @return true if the shelter is successfully defined; false otherwise.
   * @throws ClassNotFoundException if the required class is not found.
   * @throws IOException if an I/O error occurs.
   * @throws SQLException if a SQL error occurs.
   */
  public boolean defineShelter() throws ClassNotFoundException, IOException, SQLException {

    try {
      connection = DatabaseManager.getConnection();

      /* Checking if the staff with the given name exists in the system */
      if (helperMethod.isStaffExists(staffInCharge) == Constants.STAFF_NOT_FOUND) {
        return false;
      }

      /* If shelter does not exist, add shelter */
      if (helperMethod.isShelterExists(name) == Constants.SHELTER_NOT_FOUND) {
        PreparedStatement insertShelter =
            connection.prepareStatement(
                "insert into shelter (name, locationX, locationY, capacity, staff_in_charge) values (?, ?, ?, ?, ?)");
        insertShelter.setString(1, name);
        insertShelter.setInt(2, getLocation().getX());
        insertShelter.setInt(3, getLocation().getY());
        insertShelter.setInt(4, maxCapacity);
        insertShelter.setString(5, staffInCharge);
        insertShelter.executeUpdate();
      }
      /* If shelter exists, update the details */
      else {
        PreparedStatement updateShelter =
            connection.prepareStatement(
                "update shelter set locationX = ?, locationY = ?, capacity = ?, staff_in_charge = ? where name = ?;");
        updateShelter.setInt(1, getLocation().getX());
        updateShelter.setInt(2, getLocation().getY());
        updateShelter.setInt(3, maxCapacity);
        updateShelter.setString(4, staffInCharge);
        updateShelter.setString(5, name);
        updateShelter.executeUpdate();
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
   * Retrieves the names of shelters that are at or above a certain occupancy threshold.
   *
   * @param threshold the occupancy threshold as a percentage.
   * @return a set of shelter names meeting the specified criteria.
   * @throws SQLException if a SQL error occurs.
   * @throws IOException if an I/O error occurs.
   * @throws ClassNotFoundException if the required class is not found.
   */
  public Set<String> shelterAtCapacity(int threshold)
      throws SQLException, IOException, ClassNotFoundException {
    Set<String> result = new HashSet<>();

    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement shelterAtCapacity =
          connection.prepareStatement(
              """
                  SELECT s.name
                  FROM shelter s
                           JOIN shelter_occupancy_record sor ON s.shelter_id = sor.shelter_id
                  WHERE sor.date = (SELECT MAX(date) FROM shelter_occupancy_record WHERE shelter_id = s.shelter_id)
                    AND sor.occupancy >= s.capacity * ?""");
      shelterAtCapacity.setDouble(1, threshold / 100.00);
      ResultSet shelterCapacityResultSet = shelterAtCapacity.executeQuery();
      while (shelterCapacityResultSet.next()) {
        result.add(shelterCapacityResultSet.getString("name"));
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
   * Retrieves the names of shelters with occupancy variance meeting specified criteria.
   *
   * @param startDate the start date for the occupancy records.
   * @param endDate the end date for the occupancy records.
   * @param threshold the occupancy variance threshold as a percentage.
   * @return a set of shelter names meeting the specified criteria.
   * @throws SQLException if a SQL error occurs.
   * @throws IOException if an I/O error occurs.
   * @throws ClassNotFoundException if the required class is not found.
   */
  public Set<String> occupancyVariance(String startDate, String endDate, int threshold)
      throws SQLException, IOException, ClassNotFoundException {
    Set<String> result = new HashSet<>();

    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement shelterOccupancyVariance =
          connection.prepareStatement(
              """
                  SELECT s.name
                  FROM shelter s
                           JOIN
                       (SELECT shelter_id, MAX(occupancy) AS max_occupancy, MIN(occupancy) AS min_occupancy
                        FROM shelter_occupancy_record
                        WHERE date BETWEEN ? AND ?
                        GROUP BY shelter_id) as records ON s.shelter_id = records.shelter_id
                  WHERE (max_occupancy - min_occupancy) / s.capacity >= ?;
                   """);
      shelterOccupancyVariance.setDate(1, Date.valueOf(startDate));
      shelterOccupancyVariance.setDate(2, Date.valueOf(endDate));
      shelterOccupancyVariance.setDouble(3, threshold / 100.00);
      ResultSet shelterOccupancyVarianceResultSet = shelterOccupancyVariance.executeQuery();
      while (shelterOccupancyVarianceResultSet.next()) {
        result.add(shelterOccupancyVarianceResultSet.getString("name"));
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
   * Retrieves the names of underfunded shelters based on donation records.
   *
   * @param startDate the start date for the donation records.
   * @param endDate the end date for the donation records.
   * @param distance the maximum distance for considering a shelter within range.
   * @param threshold the threshold for selecting underfunded shelters.
   * @return a set of shelter names meeting the specified criteria.
   * @throws SQLException if a SQL error occurs.
   * @throws IOException if an I/O error occurs.
   * @throws ClassNotFoundException if the required class is not found.
   */
  public Set<String> underfundedShelter(
      String startDate, String endDate, int distance, int threshold)
      throws SQLException, IOException, ClassNotFoundException {

    /* Fetching donors which made a donation in the given range */
    List<Donor> donors = getAllDonors(startDate, endDate);

    /* Getting all shelters */
    List<Shelter> shelters = getAllShelters(startDate, endDate);

    Map<Donor, List<Shelter>> donorShelterMap = new HashMap<>();
    Map<Shelter, Integer> sheltersCalculatedDonationMap = new HashMap<>();
    List<Shelter> sortedUnderfundedShelters;
    Set<String> result = new HashSet<>();

    /* If there are no donors who made a donation in the date range or no shelters in the system */
    if (donors.isEmpty() || shelters.isEmpty()) {
      return null;
    }

    /* Adding shelters only which are within the range or the nearest shelters */
    for (Donor donor : donors) {
      List<Shelter> shelterList = new ArrayList<>();
      for (Shelter shelter : shelters) {
        if (withinTheRange(donor.getLocation(), shelter.getLocation(), distance)) {
          shelterList.add(shelter);
        }
      }

      /* If there are no shelters in the distance given, add the nearest shelter */
      if (shelterList.isEmpty()) {
        List<Shelter> nearestShelter = findNearestShelter(donor.getLocation(), shelters);
        if (nearestShelter != null) shelterList.addAll(nearestShelter);
      }

      donorShelterMap.put(donor, shelterList);
    }

    /* Calculate funds for each shelter */
    for (Map.Entry<Donor, List<Shelter>> entry : donorShelterMap.entrySet()) {
      Donor donor = entry.getKey();
      List<Shelter> sheltersWithinRange = entry.getValue();

      int totalFunds = donor.getDonation();

      /* Calculate the total capacity of all shelters */
      int totalCapacity = sheltersWithinRange.stream().mapToInt(s -> s.maxCapacity).sum();

      /* Distribute funds to each shelter based on their capacity */
      for (Shelter shelter : sheltersWithinRange) {
        double proportion = (double) shelter.maxCapacity / totalCapacity;
        int shelterFunds = (int) (proportion * totalFunds);
        if (sheltersCalculatedDonationMap.containsKey(shelter)) {
          int currentShelterFunds = sheltersCalculatedDonationMap.get(shelter);
          sheltersCalculatedDonationMap.put(shelter, currentShelterFunds + shelterFunds);
        } else {
          sheltersCalculatedDonationMap.put(shelter, shelterFunds);
        }
      }
    }

    sortedUnderfundedShelters = getSortedUnderfundedShelters(sheltersCalculatedDonationMap);

    for (int i = 0; i < Math.min(threshold, sortedUnderfundedShelters.size()); i++) {
      result.add(sortedUnderfundedShelters.get(i).getName());
    }

    return result;
  }

  /**
   * Retrieves a list of donors with their total donations within a specified date range.
   *
   * @param startDate the start date for the donation records.
   * @param endDate the end date for the donation records.
   * @return a list of donors with their total donations.
   * @throws SQLException if a SQL error occurs.
   * @throws IOException if an I/O error occurs.
   * @throws ClassNotFoundException if the required class is not found.
   */
  private List<Donor> getAllDonors(String startDate, String endDate)
      throws SQLException, IOException, ClassNotFoundException {
    List<Donor> result = new ArrayList<>();
    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement getAllDonors =
          connection.prepareStatement(
              """
                   SELECT d.name                         AS donor_name,
                          d.locationX                    AS donor_locationX,
                          d.locationY                    AS donor_locationY,
                          COALESCE(SUM(rdr.donation), 0) AS total_donation
                   FROM donor d
                            JOIN
                        receive_donation_record rdr ON d.donor_id = rdr.donor_id
                            AND rdr.date BETWEEN ? AND ?
                   GROUP BY d.donor_id;
                   """);

      getAllDonors.setString(1, startDate);
      getAllDonors.setString(2, endDate);
      ResultSet donorsResultSet = getAllDonors.executeQuery();
      while (donorsResultSet.next()) {
        String name = donorsResultSet.getString("donor_name");
        Point point =
            new Point(
                donorsResultSet.getInt("donor_locationX"),
                donorsResultSet.getInt("donor_locationY"));
        int donation = donorsResultSet.getInt("total_donation");
        Donor donor = new Donor(name, point, null, null);
        donor.setDonation(donation);
        result.add(donor);
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
   * Retrieves a list of shelters with their total funds received within a specified date range.
   *
   * @param startDate the start date for the fund disbursement records.
   * @param endDate the end date for the fund disbursement records.
   * @return a list of shelters with their total funds received.
   * @throws SQLException if a SQL error occurs.
   * @throws IOException if an I/O error occurs.
   * @throws ClassNotFoundException if the required class is not found.
   */
  private List<Shelter> getAllShelters(String startDate, String endDate)
      throws SQLException, IOException, ClassNotFoundException {
    List<Shelter> result = new ArrayList<>();
    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement getAllShelters =
          connection.prepareStatement(
              """
                    SELECT s.name                      AS shelter_name,
                           s.locationX                 AS shelter_locationX,
                           s.locationY                 AS shelter_locationY,
                           s.capacity                  AS shelter_capacity,
                           COALESCE(SUM(dfr.funds), 0) AS total_funds_received
                    FROM shelter s
                             LEFT JOIN
                         disburse_fund_record dfr ON s.shelter_id = dfr.shelter_id
                            AND dfr.date BETWEEN ? AND ?
                    GROUP BY s.shelter_id;
                    """);

      getAllShelters.setString(1, startDate);
      getAllShelters.setString(2, endDate);
      ResultSet sheltersResultSet = getAllShelters.executeQuery();
      while (sheltersResultSet.next()) {
        String name = sheltersResultSet.getString("shelter_name");
        Point point =
            new Point(
                sheltersResultSet.getInt("shelter_locationX"),
                sheltersResultSet.getInt("shelter_locationY"));
        int capacity = sheltersResultSet.getInt("shelter_capacity");
        int fund = sheltersResultSet.getInt("total_funds_received");
        Shelter shelter = new Shelter(name, point, capacity, null);
        shelter.setFundsReceived(fund);
        result.add(shelter);
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
   * Checks if a given donor location is within the specified distance of a shelter location.
   *
   * @param donorLocation the location of the donor.
   * @param shelterLocation the location of the shelter.
   * @param distance the maximum distance to consider.
   * @return true if the donor is within the distance of the shelter; false otherwise.
   */
  private boolean withinTheRange(Point donorLocation, Point shelterLocation, int distance) {
    return donorLocation.distanceTo(shelterLocation) <= distance;
  }

  /**
   * Finds the nearest shelters to a donor location from a list of shelters.
   *
   * @param donorLocation the location of the donor.
   * @param shelters the list of shelters to consider.
   * @return a list of nearest shelters.
   */
  private List<Shelter> findNearestShelter(Point donorLocation, List<Shelter> shelters) {
    if (shelters.isEmpty()) {
      return null;
    }

    List<Shelter> nearestShelters = new ArrayList<>();
    double minDistance = donorLocation.distanceTo(shelters.get(0).getLocation());

    for (Shelter shelter : shelters) {
      double distance = donorLocation.distanceTo(shelter.getLocation());

      if (distance == minDistance) {
        nearestShelters.add(shelter);
      } else if (distance < minDistance) {
        minDistance = distance;
        nearestShelters.clear();
        nearestShelters.add(shelter);
      }
    }

    return nearestShelters;
  }

  /**
   * Gets a sorted list of underfunded shelters based on calculated donation amounts.
   *
   * @param sheltersWithCalculatedDonation a map of shelters to their calculated donation amounts.
   * @return a sorted list of underfunded shelters.
   */
  private List<Shelter> getSortedUnderfundedShelters(
      Map<Shelter, Integer> sheltersWithCalculatedDonation) {
    List<Shelter> sortedUnderfundedShelters =
        new ArrayList<>(sheltersWithCalculatedDonation.keySet());

    /* Sort the shelters based on the ratio of calculatedDonation to maxCapacity */
    sortedUnderfundedShelters.sort(
        (shelterOne, shelterTwo) -> {
          double ratioOne =
              sheltersWithCalculatedDonation.get(shelterOne) / (double) shelterOne.getMaxCapacity();
          double ratioTwo =
              sheltersWithCalculatedDonation.get(shelterTwo) / (double) shelterTwo.getMaxCapacity();

          /* Compare based on the ratio */
          int result = Double.compare(ratioOne, ratioTwo); /* Ascending order */

          /* If the ratios are equal, compare based on fundsReceived */
          if (result == 0) {
            result = Integer.compare(shelterOne.getFundsReceived(), shelterTwo.getFundsReceived());
          }

          return result;
        });
    return sortedUnderfundedShelters;
  }

  /**
   * Converts the Shelter object to its string representation.
   *
   * @return a string representation of the Shelter object.
   */
  @Override
  public String toString() {
    return "Shelter{"
        + "name='"
        + name
        + '\''
        + ", location="
        + location.getX()
        + " "
        + location.getY()
        + ", maxCapacity="
        + maxCapacity
        + ", staffInCharge='"
        + staffInCharge
        + " fundsReceived="
        + fundsReceived
        + '\''
        + '}';
  }
}
