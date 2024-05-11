/**
 * The Pair class represents a simple key-value pair.
 *
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
public class Pair<K, V> {

  private K key;
  private V value;

  /**
   * Constructs a new Pair with the specified key and value.
   *
   * @param key The key of the pair
   * @param value The value of the pair
   */
  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Gets the key of the pair.
   *
   * @return The key of the pair
   */
  public K getKey() {
    return key;
  }

  /**
   * Gets the value of the pair.
   *
   * @return The value of the pair
   */
  public V getValue() {
    return value;
  }

  /**
   * Returns a string representation of the Pair object.
   *
   * @return String representation of the object
   */
  @Override
  public String toString() {
    return "Pair{" + "key=" + key + ", value=" + value + '}';
  }
}
