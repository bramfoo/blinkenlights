package blinkenlights;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import blinkenlights.Event.EventType;

/**
 * Node class. A Node is placed in a square grid, and drawn on screen. It has both grid coordinates
 * and drawing coordinates. It contains an integer value that can be incremented, and is reset if
 * the node and a connecting neighbour have the values 2 and 4.
 * 
 * It can draw itself in a normal state, a brief incremented state (yellow), and a reset state
 * (green).
 * 
 * @author bram
 *
 */
public class Node {

  private int value;
  private Point coord;
  private double radius;
  private Point2D location;
  private List<Node> neighbours;
  private Node left;
  private Node right;
  private Node up;
  private Node down;
  private Blinkenlights parent;

  private Color penColour;
  private Color black = Color.BLACK; // Default state
  private Color green = Color.GREEN; // Reset state
  private Color yellow = Color.YELLOW; // Update state

  /**
   * Constructor
   * 
   * @param x The x-coordinate of the Node on the JFrame
   * @param y The y-coordinate of the Node on the JFrame
   * @param radius The radius of this node when drawn.
   * @param row Grid x-coordinate of node. 0 <= x < gridSize.
   * @param col Grid y-coordinate of node. 0 <= y < gridSize.
   * @param parent The grid coordinator, that can plan events for this Node
   */
  public Node(double x, double y, double radius, int row, int col, Blinkenlights parent) {
    value = 0;
    location = new Point2D.Double(x, y);
    coord = new Point(row, col);
    this.radius = radius;
    neighbours = new ArrayList<>();
    left = null;
    right = null;
    up = null;
    down = null;
    penColour = black;
    this.parent = parent;
    // System.out.println("Creating node at location " +
    // location.toString());
  }

  /**
   * Add a list of neighbours to this Node
   * 
   * @param neighbours The neighbours of this Node (up, down, left, right). Diagonal nodes are not
   *        neighbours
   */
  public void addNeighbours(List<Node> neighbours) {
    this.neighbours = neighbours;
    // Determine the neighbours' position. This makes propagating updates
    // easier
    left = null;
    right = null;
    up = null;
    down = null;
    for (Node n : neighbours) {
      // On the same column
      if (n.getCoord().getX() == coord.getX()) {
        if (coord.getY() <= n.getCoord().getY()) {
          up = n;
        }
        if (coord.getY() >= n.getCoord().getY()) {
          down = n;
        }
      }
      // On the same row
      if (n.getCoord().getY() == coord.getY()) {
        if (coord.getX() <= n.getCoord().getX()) {
          right = n;
        }
        if (coord.getX() >= n.getCoord().getX()) {
          left = n;
        }
      }
    }
  }

  /**
   * The current value of this Node
   * 
   * @return int with the current value
   */
  public int getValue() {
    return value;
  }

  /**
   * The (x, y) grid coordinates of this Node.
   * 
   * @return {@link Point} containing the coordinates
   */
  public Point getCoord() {
    return coord;
  }

  /**
   * The drawing location of this Node.
   * 
   * @return {@link Point2D}
   */
  public Point2D getLocation() {
    return location;
  }

  /**
   * Update the state of this Node. It (or a neighbour) has been clicked
   * 
   * @param src
   */
  private void update(Node src) {
    value += 1;

    // Propagate to row and column neighbours. If this node is the origin,
    // go both ways, otherwise only the opposite direction of the source, in
    // the same row/column
    if (src.coord.equals(coord)) {
      // We are the origin. Update all our neighbours
      for (Node n : neighbours)
        n.handleEvent(EventType.CLICK, src);
      return;
    } else {
      // On the same column
      if (src.getCoord().getX() == coord.getX()) {
        if (coord.getY() < src.getCoord().getY())
          if (down != null)
            down.handleEvent(EventType.CLICK, src);
        if (coord.getY() > src.getCoord().getY())
          if (up != null)
            up.handleEvent(EventType.CLICK, src);
      }
      // On the same row
      if (src.getCoord().getY() == coord.getY()) {
        if (coord.getX() < src.getCoord().getX())
          if (left != null)
            left.handleEvent(EventType.CLICK, src);
        if (coord.getX() > src.getCoord().getX())
          if (right != null)
            right.handleEvent(EventType.CLICK, src);
      }
    }
  }

  /**
   * Prints this Node's grid coordinates (x, y)
   */
  public String toString() {
    return "[" + coord.getX() + "," + coord.getY() + "]";
  }

  /**
   * Trigger a reset, by setting the pen colour to green, and creating a reset event in the future
   */
  public void doReset() {
    penColour = green;
    value = 0;
    parent.createEvent(Blinkenlights.RESET_DELAY, this, this, EventType.COLOR_RESET);
  }

  /**
   * Check if this node needs to be reset. This is the case if this node's value is 4, and it has a
   * neighbour with value 2.
   */
  public void checkReset() {
    if (value == 4)
      for (Node n : neighbours)
        if (n.value == 2) {
          System.out.printf("Node %s is resetting itself and node %s%n", this.toString(),
              n.toString());
          doReset();
          n.doReset();
          return;
        }
  }

  /**
   * Draw the node, in the given penColour (depending on its state) Also adds a green text with the
   * value of the node.
   */
  public void draw() {
    Visual.setPenColour(penColour);
    Visual.circle(location.getX(), location.getY(), radius);
    Visual.setPenColour(green);
    Visual.text(location.getX(), location.getY(), Integer.toString(value));
  }

  /**
   * React to an event, based on the event type
   * 
   * @param type The type of event
   */
  public void handleEvent(EventType type, Node src) {

    switch (type) {
      case CLICK:
        update(src);
        penColour = yellow;
        // Reset this Node's pen colour after a short delay
        parent.createEvent(Blinkenlights.RESET_DELAY, this, this, EventType.COLOR_RESET);
        break;
      case COLOR_RESET: // Reset pen colour
        penColour = black;
        break;
      case REDRAW: // Ignore
      default:
        break;
    }
  }
}
