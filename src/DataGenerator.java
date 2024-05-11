import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

/* If you don't specify an access modifier for a method in an interface, it is implicitly public. */
public interface DataGenerator {
  boolean defineService(String serviceName, int inspectionFrequency)
      throws SQLException, IOException, ClassNotFoundException;

  boolean defineShelter(String name, Point location, int maxCapacity, String staffInCharge)
      throws SQLException, IOException, ClassNotFoundException;

  boolean serviceForShelter(String shelterName, String serviceName)
      throws SQLException, IOException, ClassNotFoundException;

  boolean declareShelterOccupancy(String name, String date, int occupancy)
      throws SQLException, IOException, ClassNotFoundException;

  boolean addStaff(String name, Set<String> services, boolean volunteer, String manager)
      throws SQLException, IOException, ClassNotFoundException;

  boolean defineDonor(String name, Point centralOffice, String contact, Set<String> fundingPrograms)
      throws SQLException, IOException, ClassNotFoundException;

  boolean receiveDonation(String donor, String fundingProgram, String date, int donation)
      throws SQLException, IOException, ClassNotFoundException;

  boolean disburseFunds(String shelterReceiving, String date, int funds)
      throws SQLException, IOException, ClassNotFoundException;
}
