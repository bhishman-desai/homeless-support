import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* If you don't specify an access modifier for a method in an interface, it is implicitly public. */
public interface DataManipulator {
  Set<String> shelterAtCapacity(int threshold)
      throws SQLException, IOException, ClassNotFoundException;

  Set<String> occupancyVariance(String startDate, String endDate, int threshold)
      throws SQLException, IOException, ClassNotFoundException;

  void donorReport(String startDate, String endDate, PrintWriter outstream)
      throws SQLException, IOException, ClassNotFoundException;

  Set<String> underfundedShelter(String startDate, String endDate, int distance, int threshold)
      throws SQLException, IOException, ClassNotFoundException;

  Map<String, List<String>> inspectionSchedule(int scheduleDays, int inspectLimit)
      throws SQLException, IOException, ClassNotFoundException;
}
