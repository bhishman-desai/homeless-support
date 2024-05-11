import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Set;

public class Main {
  public static void main(String[] args) {
    HomelessSupport homelessSupport = new HomelessSupport();

    try (PrintWriter printWriter = new PrintWriter(new FileWriter("output.txt"))) {

      homelessSupport.donorReport("2023-09-06", "2023-12-15", printWriter);

    } catch (SQLException | ClassNotFoundException | IOException exception) {
      System.out.println(exception.getMessage());
      /*exception.printStackTrace();*/
    }
  }
}
