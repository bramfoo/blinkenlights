package blinkenlights;

/**
 * Event class. Used for timing events that occur in the future.
 * 
 * @author Bram Lohman
 *
 */
public class Event implements Comparable<Event> {

  public static enum EventType {
    REDRAW, CLICK, COLOR_RESET
  }

  private final long time; // time that event is scheduled to occur
  private final Node target; // Node that event is for
  private final Node src; // Node that event is from (if applicable)
  private EventType type; // Type of event


  /**
   * Create a new event to occur
   * 
   * @param t Time at which the event occurs
   * 
   * 
   */
  public Event(long t, Node target, Node source, EventType type) {
    this.time = t;
    this.target = target;
    this.src = source;
    this.type = type;
  }

  public Node getTarget() {
    return this.target;
  }

  public Node getSource() {
    return this.src;
  }

  public EventType getType() {
    return this.type;
  }

  @Override
  // compare times when two events will occur
  public int compareTo(Event that) {
    if (this.time < that.time)
      return -1;
    else if (this.time > that.time)
      return +1;
    else
      return 0;
  }

  public long getTime() {
    return this.time;
  }
}
