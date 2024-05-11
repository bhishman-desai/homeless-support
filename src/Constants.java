import java.time.format.DateTimeFormatter;

/**
 * The "Constants" class defines integer constants for "NOT_FOUND" values and a DateTimeFormatter
 * for date formatting. It is designed to prevent instantiation, enforcing its use solely for
 * accessing static constants.
 */
public class Constants {
  public static final int SERVICE_NOT_FOUND = -1;
  public static final int SHELTER_NOT_FOUND = -1;
  public static final int STAFF_NOT_FOUND = -1;
  public static final int DONOR_NOT_FOUND = -1;
  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /* Private ensures that nobody can access the constructor of the method and if somehow they do, we will throw an exception as a double safety check */
  private Constants() {
    throw new AssertionError("Constants class should not be instantiated.");
  }
}
