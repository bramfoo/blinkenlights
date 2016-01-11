package blinkenlights;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import blinkenlights.Event.EventType;

/**
 * Creates a (square) grid of clickable nodes, with default size of 50. Clicking a node increments
 * the value of that node, as well as the value of all the nodes in the same row and column. After
 * each increment, a node briefly lights up in yellow. If two neighbouring nodes (up, down, left,
 * right, not diagonal) contain a value of four and two, both these nodes light up briefly in green,
 * and then reset their values to zero.
 * 
 * @author Bram Lohman
 *
 */
public class Blinkenlights implements MouseListener {
  // Constants used in timing
  private static final long REFRESH_RATE = 50000000l; // 0.5 msec
  public static final long RESET_DELAY = 400000000l; // 4 msec
  private static final long UPDATE_RATE = 10000000l; // 0.1 msec

  private MinPQ<Event> pq; // Priority Queue holding (timed) system events
  private long systemTime; // The simulation time

  // Grid and Node properties
  private final int width;
  private final int height;
  private Node[] grid; // One-dimensional grid for optimisation
  private final double nodeRadius;

  // Frame properties
  private double drawMin = 0.0;
  private double drawMax = 1.0;

  // Mouse properties
  private boolean mousePressed = false;
  private double mouseX = 0;
  private double mouseY = 0;

  /**
   * Constructor. Sets up the JFrame and populates the grid with nodes, including the information
   * about their neighbours
   * 
   * @param gridSize The length/width of the (square) grid
   */
  public Blinkenlights(int gridSize) {
    // Configure grid, and nodes
    width = gridSize;
    height = gridSize;
    grid = new Node[width * height];
    nodeRadius = (drawMax - drawMin) / gridSize / 2;

    // Populate grid
    System.out.printf("Creating grid of size %d [%d x %d]%n", grid.length, width, height);

    double rx = nodeRadius; // X-position of node on JFrame
    double ry = nodeRadius; // Y-position of node on JFrame
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        grid[xy1D(x, y)] = new Node(rx, ry, nodeRadius, x, y, this);
        ry += 2 * nodeRadius;
      }
      ry = nodeRadius; // Reset for new row
      rx += 2 * nodeRadius;
    }

    // Provide each node with information about its neighbours
    for (Node n : grid) {
      n.addNeighbours(neighbours(n));
    }

    // Display JFrame, and add ourselves as a MouseListener
    Visual.addListener(this);
    Visual.show(0);
  }

  /**
   * Finds the neighbours of a particular node in the grid, using x and y grid coordinates
   * 
   * @param x The x-position of the node. 0 <= x < gridSize
   * @param y The y-position of the node. 0 <= y < gridSize
   * @return List of (non-diagonal) neighbouring nodes
   */
  private List<Node> neighbours(Node n) {
    int x = n.getCoord().x;
    int y = n.getCoord().y;
    ArrayList<Node> neighbours = new ArrayList<>();
    int j = y;
    for (int i = x - 1; i <= x + 1; i++)
      if (i >= 0 && i < width && j >= 0 && j < height && xy1D(i, j) != xy1D(x, y)) {
        neighbours.add(grid[xy1D(i, j)]);
      }

    int i = x;
    for (j = y - 1; j <= y + 1; j++) {
      if (i >= 0 && i < width && j >= 0 && j < height && xy1D(i, j) != xy1D(x, y)) {
        neighbours.add(grid[xy1D(i, j)]);
      }
    }

    // System.out.printf("Neighbours of [%d, %d] are %s%n", x, y,
    // n.toString());
    return neighbours;
  }

  /**
   * toString. Creates a text representation of the grid, with the values of each node printed
   */
  public String toString() {
    StringBuilder s = new StringBuilder();
    for (int y = 0; y < height; y++) {
      s.append("[");
      for (int x = 0; x < width; x++) {
        s.append(grid[xy1D(x, y)].getValue());
        if (x == width - 1) {
          continue;
        }
        s.append(", ");
      }
      s.append("]\n");
    }
    return s.toString();
  }

  /**
   * Helper function to transform a 2D grid value into 1D. Used for optimisation
   * 
   * @param x The 2D x-coordinate in the grid. 0 <= x < gridSize
   * @param y The 2D y-coordinate in the grid. 0 <= y < gridSize
   * @return The equivalent 1D coordinate. 0 <= result < 2 * gridSize
   */
  private int xy1D(int x, int y) {
    return y * width + x;
  }

  /**
   * (Re)draw the visual representation of the grid.
   * 
   * @param time The current system time. Used to plan the next call to redraw.
   */
  private void redraw(long time) {
    Visual.clear();
    for (Node n : grid)
      n.draw();
    Visual.show(0);

    // Add the next redraw event
    createEvent(REFRESH_RATE, null, null, EventType.REDRAW);

    // FIXME: Keep a list of nodes with value 4 and only check those, not
    // all of the nodes.
    // Check for nodes of value 4, and their neighbours
    for (Node n : grid) {
      n.checkReset();
    }
  }

  /**
   * Creates a new (future) event on the priority queue
   * 
   * @param future The period into the future at which the event will occur
   * @param target The Node for which the event is intended, if any. May be null.
   * @param source The Node from which the event originated, if any. May be null.
   * @param EventType The type of event
   */
  public void createEvent(long future, Node target, Node source, EventType type) {
    pq.insert(new Event(systemTime + future, target, source, type));
  }

  /**
   * Starts the program, and serves as the main loop.
   */
  public void go() {
    systemTime = System.nanoTime();
    // Initialize PQ with redraw event
    pq = new MinPQ<Event>();
    createEvent(0, null, null, EventType.REDRAW);

    // Start the (infinite) loop
    while (!pq.isEmpty()) {
      // Check for impending event
      // Update simulation time
      systemTime = System.nanoTime();
      long nearestEventTime = pq.min().getTime();

      // Sleep if no impending events
      if (nearestEventTime > systemTime) {
        // System.out.printf("Nearest event time is at %s (in the " +
        // " future): time is %s. Sleeping...%n", nearestEventTime,
        // systemTime);
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          System.out.println("Error sleeping");
        }
      }
      // Handle the event
      else {
        Event e = pq.delMin();
        Node targetNode = e.getTarget();

        if (targetNode != null) {
          targetNode.handleEvent(e.getType(), e.getSource());
        } else
          redraw(systemTime);
      }
    }
  }

  /**
   * Invoked when a mouse button has been pressed on a component. Determines which node has been
   * clicked (if any), and creates an event for that Node, if applicable
   */
  @Override
  public void mousePressed(MouseEvent e) {
    synchronized (this) {
      mouseX = Visual.userX(e.getX());
      mouseY = Visual.userY(e.getY());
      mousePressed = true;
    }

    // Determine which node it is contained in
    Point2D click = new Point2D.Double(mouseX, mouseY);

    // FIXME: This checks all the nodes...horribly inefficient. Divide &
    // conquer the grid instead
    Node clickedNode = null;
    for (Node n : grid) {
      double distanceSq = n.getLocation().distanceSq(click);
      if (distanceSq <= nodeRadius * nodeRadius)
        clickedNode = n;
    }

    if (clickedNode != null) {
      System.out.printf("Node [%s, %s] has been clicked!%n", clickedNode.getCoord().getX(),
          clickedNode.getCoord().getY());
      createEvent(UPDATE_RATE, clickedNode, clickedNode, EventType.CLICK);
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    // Not interested in this event
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    mousePressed = false;
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    // Not interested in this event
  }

  @Override
  public void mouseExited(MouseEvent e) {
    // Not interested in this event
  }

  /**
   * Main class. Starts the program
   * 
   * @param args List of arguments. Expects a single integer argument indicating the number of
   *        cells/nodes that make up a side of the (square) grid, with a maximum of 50. If none is
   *        provided, or it cannot be parsed, the default value of 50 will be used.
   */
  public static void main(String[] args) {
    int nodes = 50; // Default value
    if (args.length > 0) {
      try {
        nodes = Math.min(nodes, Integer.parseInt(args[0]));
      } catch (NumberFormatException nfe) {
        System.out.println(
            "Could not parse input: " + nfe.getMessage() + "; default value of 50 will be used");
      }
    }

    Blinkenlights b = new Blinkenlights(nodes);
    b.go();
  }
}
