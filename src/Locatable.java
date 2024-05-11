/** An interface representing entities with a location. */
public interface Locatable {
  /**
   * Get the current location of the entity.
   *
   * @return The current location as a Point object.
   */
  Point getLocation();
}
