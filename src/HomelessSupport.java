import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The class HomelessSupport implements DataGenerator and DataManipulator interfaces to manage
 * various aspects of shelters, camps, staff, donors, donations, and reports.
 */
public class HomelessSupport implements DataGenerator, DataManipulator {
  private HelperMethod helperMethod = new HelperMethod();
  private Connection connection = null;

  /**
   * Define a new service in the system.
   *
   * @param serviceName Name of the service to be added
   * @param inspectionFrequency How frequently does it need inspection
   * @return true on successful addition
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public boolean defineService(String serviceName, int inspectionFrequency)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        serviceName == null || serviceName.isEmpty() || inspectionFrequency < 0;
    if (isInputNotValidated) {
      return false;
    }

    Service service = new Service(serviceName, inspectionFrequency);
    return service.defineService();
  }

  /**
   * Add a shelter or camp to the system.
   *
   * @param name Name of the shelter or camp
   * @param location Location of the shelter or camp
   * @param maxCapacity Maximum number of people it can accommodate
   * @param staffInCharge Staff member in charge of the shelter or camp
   * @return true if the shelter was successfully added
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public boolean defineShelter(String name, Point location, int maxCapacity, String staffInCharge)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        name == null
            || name.isEmpty()
            || location == null
            || maxCapacity <= 0
            || staffInCharge == null
            || staffInCharge.isEmpty();
    if (isInputNotValidated) {
      return false;
    }

    Shelter shelter = new Shelter(name, location, maxCapacity, staffInCharge);

    return shelter.defineShelter();
  }

  /**
   * Identify that a service is available at a given shelter or camp.
   *
   * @param shelterName Name of the shelter
   * @param serviceName Name of the service
   * @return true if the mapping was successful
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public boolean serviceForShelter(String shelterName, String serviceName)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        shelterName == null
            || shelterName.isEmpty()
            || serviceName == null
            || serviceName.isEmpty();
    if (isInputNotValidated) {
      return false;
    }

    int shelterID = helperMethod.isShelterExists(shelterName);
    int serviceID = helperMethod.isServiceExists(serviceName);
    boolean isDataNotValidated =
        shelterID == Constants.SHELTER_NOT_FOUND || serviceID == Constants.SERVICE_NOT_FOUND;
    if (isDataNotValidated) {
      return false;
    }

    try {
      connection = DatabaseManager.getConnection();

      PreparedStatement insertServiceShelterMap =
          connection.prepareStatement(
              "insert into service_for_shelter (service_id, shelter_id) values (?, ?)");
      insertServiceShelterMap.setInt(1, serviceID);
      insertServiceShelterMap.setInt(2, shelterID);
      insertServiceShelterMap.executeUpdate();
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
   * Declare shelter occupancy on a specific date.
   *
   * @param name Name of the shelter or camp
   * @param date Date of record
   * @param occupancy Occupancy of the shelter or camp
   * @return true if the entry was successful
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public boolean declareShelterOccupancy(String name, String date, int occupancy)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        name == null
            || name.isEmpty()
            || date == null
            || date.isEmpty()
            || helperMethod.isDateInvalid(date)
            || occupancy < 0;
    if (isInputNotValidated) {
      return false;
    }

    OccupancyRecord occupancyRecord = new OccupancyRecord(name, date, occupancy);
    return occupancyRecord.declareShelterOccupancy();
  }

  /**
   * Declare that a new staff member has joined the organization.
   *
   * @param name Name of the staff member to be added
   * @param services Services which that staff member can inspect
   * @param volunteer True if the staff member is a volunteer, false if paid
   * @param manager Name of the manager
   * @return true if staff is added successfully
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public boolean addStaff(String name, Set<String> services, boolean volunteer, String manager)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        name == null
            || name.isEmpty()
            || services == null
            || services.isEmpty()
            || helperMethod.isSetEmpty(services)
            || manager == null
            || manager.isEmpty();
    if (isInputNotValidated) {
      return false;
    }
    Staff staff = new Staff(name, services, volunteer, manager);
    return staff.addStaff();
  }

  /**
   * Identify a potential donor who will provide funding to support the shelters or camps.
   *
   * @param name Name of the donor
   * @param centralOffice Donor's location
   * @param contact Donor's contact info
   * @param fundingPrograms The grant programs which the donor contributes to
   * @return true if donor was added successfully
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public boolean defineDonor(
      String name, Point centralOffice, String contact, Set<String> fundingPrograms)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        name == null
            || name.isEmpty()
            || centralOffice == null
            || contact == null
            || contact.isEmpty()
            || fundingPrograms == null
            || fundingPrograms.isEmpty()
            || helperMethod.isSetEmpty(fundingPrograms);
    if (isInputNotValidated) {
      return false;
    }
    Donor donor = new Donor(name, centralOffice, contact, fundingPrograms);

    return donor.defineDonor();
  }

  /**
   * Record that a donor has provided funding under a given program on a particular date to the
   * organization.
   *
   * @param donor Name of the donor
   * @param fundingProgram The funding program through which the donation is to be made
   * @param date Date of donation
   * @param donation Donation amount
   * @return true if the donation was successful
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public boolean receiveDonation(String donor, String fundingProgram, String date, int donation)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        donor == null
            || donor.isEmpty()
            || fundingProgram == null
            || fundingProgram.isEmpty()
            || date == null
            || date.isEmpty()
            || helperMethod.isDateInvalid(date)
            || donation <= 0;
    if (isInputNotValidated) {
      return false;
    }
    DonationRecord donationRecord = new DonationRecord(donor, fundingProgram, date, donation);

    return donationRecord.receiveDonation();
  }

  /**
   * Record that we have used an amount of funds on a given date in support of the operations of one
   * of the shelters or camps.
   *
   * @param shelterReceiving Shelter name
   * @param date Date of disbursing
   * @param funds Total funds disbursed
   * @return true if the disbursing was successful
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public boolean disburseFunds(String shelterReceiving, String date, int funds)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        shelterReceiving == null
            || shelterReceiving.isEmpty()
            || date == null
            || date.isEmpty()
            || helperMethod.isDateInvalid(date)
            || funds <= 0;
    if (isInputNotValidated) {
      return false;
    }
    FundsDisbursement fundsDisbursement = new FundsDisbursement(shelterReceiving, date, funds);
    return fundsDisbursement.disburseFunds();
  }

  /**
   * Report the names of all shelters or camps whose most recent occupancy report has them operating
   * at or above the "threshold" percentage of their capacity.
   *
   * @param threshold Value to compare
   * @return Set of shelters which are operating at or below a threshold
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public Set<String> shelterAtCapacity(int threshold)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated = threshold < 0 || threshold > 100;
    if (isInputNotValidated) {
      return null;
    }

    Shelter shelter = new Shelter();
    return shelter.shelterAtCapacity(threshold);
  }

  /**
   * Report the names of all shelters or camps whose occupancy reports in the given date range.
   *
   * @param startDate Start date in the range
   * @param endDate End date in the range
   * @param threshold Threshold value to check
   * @return Set of shelters which have occupancy variance at or more than threshold
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public Set<String> occupancyVariance(String startDate, String endDate, int threshold)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        startDate == null
            || startDate.isEmpty()
            || helperMethod.isDateInvalid(startDate)
            || endDate == null
            || endDate.isEmpty()
            || helperMethod.isDateInvalid(endDate)
            || threshold < 0
            || threshold > 100;
    if (isInputNotValidated) {
      return null;
    }
    Shelter shelter = new Shelter();
    return shelter.occupancyVariance(startDate, endDate, threshold);
  }

  /**
   * Send a report about the activity of each donor in the given time range (including both
   * endpoints of the time range). The report is sent to the outstream parameter.
   *
   * @param startDate Start date in the range
   * @param endDate End date in the range
   * @param outstream The outstream file for the output
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public void donorReport(String startDate, String endDate, PrintWriter outstream)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        startDate == null
            || startDate.isEmpty()
            || endDate == null
            || endDate.isEmpty()
            || outstream == null;
    if (isInputNotValidated) {
      return;
    }
    Donor donor = new Donor();
    donor.donorReport(startDate, endDate, outstream);
  }

  /**
   * Report the names of the "threshold" shelters or camps who have the lowest per-occupant funding
   * in the given reporting period (including both start and end dates). These are the shelters or
   * camps that need more supports over the given reporting period.
   *
   * @param startDate Start date in the range
   * @param endDate End date in the range
   * @param distance The distance to find the shelters
   * @param threshold Threshold value
   * @return Set of shelters which are underfunded
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public Set<String> underfundedShelter(
      String startDate, String endDate, int distance, int threshold)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated =
        startDate == null
            || startDate.isEmpty()
            || endDate == null
            || endDate.isEmpty()
            || distance < 0
            || threshold < 0;
    if (isInputNotValidated) {
      return null;
    }
    Shelter shelter = new Shelter();
    return shelter.underfundedShelter(startDate, endDate, distance, threshold);
  }

  /**
   * Returns a schedule to know which staff member needs to be at which shelter or camp to inspect
   * which service each day.
   *
   * @param scheduleDays Number of days until which we want to schedule
   * @param inspectLimit Number of shelters a staff can inspect on a single day
   * @return The staff schedule along with the service to inspect at a shelter
   * @throws SQLException if a database access error occurs
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if the class is not found
   */
  @Override
  public Map<String, List<String>> inspectionSchedule(int scheduleDays, int inspectLimit)
      throws SQLException, IOException, ClassNotFoundException {
    boolean isInputNotValidated = scheduleDays < 0 || inspectLimit < 0;
    if (isInputNotValidated) {
      return null;
    }
    Staff staff = new Staff();
    return staff.inspectionSchedule(scheduleDays, inspectLimit);
  }
}
