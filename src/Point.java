/** Capture a two-dimensional (x, y) point, with integer coordinates. */
public class Point {
  private int x, y;

  /**
   * Creates a point at a given location.
   *
   * @param x The x coordinate
   * @param y The y coordinate
   */
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Returns the x coordinate of this point.
   *
   * @return The integer x coordinate
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the y coordinate of this point.
   *
   * @return The integer y coordinate
   */
  public int getY() {
    return y;
  }

  /**
   * Calculates and returns the straight-line distance from this point to another given point.
   *
   * @param to The target destination point to which the distance is calculated
   * @return The distance between this point and the specified point
   * @throws IllegalArgumentException If the specified point is null
   */
  public Double distanceTo(Point to) throws IllegalArgumentException {
    if (to == null) {
      throw new IllegalArgumentException("No second pont");
    }

    Double distance =
        Math.sqrt((to.x - this.x) * (to.x - this.x) + (to.y - this.y) * (to.y - this.y));
    return distance;
  }
}
