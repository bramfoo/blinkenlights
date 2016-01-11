package blinkenlights;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * This class provides basic drawing capabilities
 * 
 * @author Bram Lohman
 *
 */
public class Visual {

  private static JFrame frame;
  private static JLabel draw;

  // Double buffered graphics
  private static BufferedImage offscreenImg, onscreenImg;
  private static Graphics2D offscreen, onscreen;

  // Default canvas size
  private static final int DEFAULT_SIZE = 800;
  private static int width = DEFAULT_SIZE;
  private static int height = DEFAULT_SIZE;

  // Pre-defined colours
  public static final Color BLACK = Color.BLACK;
  public static final Color GREEN = Color.GREEN;
  public static final Color WHITE = Color.WHITE;
  public static final Color YELLOW = Color.YELLOW;
  private static final Color DEFAULT_PEN_COLOUR = BLACK;
  private static final Color DEFAULT_CLEAR_COLOUR = WHITE;

  // Current pen colour, radius and font
  private static Color penColour;
  private static double penRadius;
  private static Font font;

  private static final double DEFAULT_PEN_RADIUS = 0.002;
  private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);

  // Boundary of drawing canvas. No border
  private static final double BORDER = 0.00;
  private static final double DEFAULT_XMIN = 0.0;
  private static final double DEFAULT_XMAX = 1.0;
  private static final double DEFAULT_YMIN = 0.0;
  private static final double DEFAULT_YMAX = 1.0;
  private static double xmin, ymin, xmax, ymax;

  // For synchronization
  private static Object mouseLock = new Object();

  // Defer drawing until next call to show if needed
  private static boolean defer = false;

  // Singleton
  private static Visual d = new Visual();

  private Visual() {}

  // Initialiser
  static {
    init();
  }

  /**
   * Set the window size to the default size 800-by-800 pixels. This method must be called before
   * any other commands.
   */
  public static void setCanvasSize() {
    setCanvasSize(DEFAULT_SIZE, DEFAULT_SIZE);
  }

  /**
   * Set the window size to w-by-h pixels. This method must be called before any other commands.
   *
   * @param w the width as a number of pixels
   * @param h the height as a number of pixels
   * @throws a IllegalArgumentException if the width or height is 0 or negative
   */
  public static void setCanvasSize(int w, int h) {
    if (w < 1 || h < 1)
      throw new IllegalArgumentException("width and height must be positive");
    width = w;
    height = h;
    init();
  }

  // init
  private static void init() {
    if (frame != null)
      frame.setVisible(false);
    frame = new JFrame();
    offscreenImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    onscreenImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    offscreen = offscreenImg.createGraphics();
    onscreen = onscreenImg.createGraphics();
    setXscale();
    setYscale();
    offscreen.setColor(DEFAULT_CLEAR_COLOUR);
    offscreen.fillRect(0, 0, width, height);
    setPenColour();
    setPenRadius();
    setFont();
    clear();

    // Antialiasing
    RenderingHints hints =
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    offscreen.addRenderingHints(hints);

    // Frame items
    ImageIcon icon = new ImageIcon(onscreenImg);
    draw = new JLabel(icon);

    frame.setContentPane(draw);
    frame.setResizable(false);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closes all windows
    frame.setTitle("Blinkenlights");
    frame.pack();
    frame.requestFocusInWindow();
    frame.setVisible(true);
  }

  public static void addListener(MouseListener ml) {
    draw.addMouseListener(ml);
  }

  /**
   * Set the x-scale to be the default (between 0.0 and 1.0).
   */
  public static void setXscale() {
    setXscale(DEFAULT_XMIN, DEFAULT_XMAX);
  }

  /**
   * Set the y-scale to be the default (between 0.0 and 1.0).
   */
  public static void setYscale() {
    setYscale(DEFAULT_YMIN, DEFAULT_YMAX);
  }

  /**
   * Set the x-scale
   * 
   * @param min the minimum value of the x-scale
   * @param max the maximum value of the x-scale
   */
  public static void setXscale(double min, double max) {
    double size = max - min;
    synchronized (mouseLock) {
      xmin = min - BORDER * size;
      xmax = max + BORDER * size;
    }
  }

  /**
   * Set the y-scale
   * 
   * @param min the minimum value of the y-scale
   * @param max the maximum value of the y-scale
   */
  public static void setYscale(double min, double max) {
    double size = max - min;
    synchronized (mouseLock) {
      ymin = min - BORDER * size;
      ymax = max + BORDER * size;
    }
  }


  /**
   * Set the pen color to the default color (black).
   */
  public static void setPenColour() {
    setPenColour(DEFAULT_PEN_COLOUR);
  }

  /**
   * Set the pen color to the given color. The available pen colors are BLACK, GREEN, WHITE, and
   * YELLOW.
   * 
   * @param colour the Color to make the pen
   */
  public static void setPenColour(Color colour) {
    penColour = colour;
    offscreen.setColor(penColour);
  }

  /**
   * Set the pen size to the default (.002).
   */
  public static void setPenRadius() {
    setPenRadius(DEFAULT_PEN_RADIUS);
  }

  /**
   * Set the radius of the pen to the given size.
   * 
   * @param r the radius of the pen
   * @throws IllegalArgumentException if r is negative
   */
  public static void setPenRadius(double r) {
    if (r < 0)
      throw new IllegalArgumentException("pen radius must be nonnegative");
    penRadius = r;
    float scaledPenRadius = (float) (r * DEFAULT_SIZE);
    BasicStroke stroke =
        new BasicStroke(scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    // BasicStroke stroke = new BasicStroke(scaledPenRadius);
    offscreen.setStroke(stroke);
  }

  /**
   * Set the font to the default font (sans serif, 16 point).
   */
  public static void setFont() {
    setFont(DEFAULT_FONT);
  }

  /**
   * Set the font to the given value.
   * 
   * @param f the font to make text
   */
  public static void setFont(Font f) {
    font = f;
  }

  /**
   * Clear the screen to the default color (white).
   */
  public static void clear() {
    clear(DEFAULT_CLEAR_COLOUR);
  }

  /**
   * Clear the screen to the given color.
   * 
   * @param color the Color to make the background
   */
  public static void clear(Color color) {
    offscreen.setColor(color);
    offscreen.fillRect(0, 0, width, height);
    offscreen.setColor(penColour);
    draw();
  }

  // draw onscreen if defer is false
  private static void draw() {
    if (defer)
      return;
    onscreen.drawImage(offscreenImg, 0, 0, null);
    frame.repaint();
  }

  /**
   * Draw filled circle of radius r, centered on (x, y).
   * 
   * @param x the x-coordinate of the center of the circle
   * @param y the y-coordinate of the center of the circle
   * @param r the radius of the circle
   * @throws IllegalArgumentException if the radius of the circle is negative
   */
  public static void circle(double x, double y, double r) {
    if (r < 0)
      throw new IllegalArgumentException("circle radius must be nonnegative");
    double xs = scaleX(x);
    double ys = scaleY(y);
    double ws = factorX(2 * r);
    double hs = factorY(2 * r);
    if (ws <= 1 && hs <= 1)
      pixel(x, y);
    else
      offscreen.fill(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    draw();
  }

  /**
   * Write the given text string in the current font, centered on (x, y).
   * 
   * @param x the center x-coordinate of the text
   * @param y the center y-coordinate of the text
   * @param s the text
   */
  public static void text(double x, double y, String s) {
    offscreen.setFont(font);
    FontMetrics metrics = offscreen.getFontMetrics();
    double xs = scaleX(x);
    double ys = scaleY(y);
    int ws = metrics.stringWidth(s);
    int hs = metrics.getDescent();
    offscreen.drawString(s, (float) (xs - ws / 2.0), (float) (ys + hs));
    draw();
  }

  // helper functions that scale from user coordinates to screen coordinates and back
  private static double scaleX(double x) {
    return width * (x - xmin) / (xmax - xmin);
  }

  private static double scaleY(double y) {
    return height * (ymax - y) / (ymax - ymin);
  }

  private static double factorX(double w) {
    return w * width / Math.abs(xmax - xmin);
  }

  private static double factorY(double h) {
    return h * height / Math.abs(ymax - ymin);
  }

  public static double userX(double x) {
    return xmin + x * (xmax - xmin) / width;
  }

  public static double userY(double y) {
    return ymax - y * (ymax - ymin) / height;
  }

  /**
   * Draw one pixel at (x, y).
   * 
   * @param x the x-coordinate of the pixel
   * @param y the y-coordinate of the pixel
   */
  private static void pixel(double x, double y) {
    offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
  }

  /**
   * Display on screen, pause for t milliseconds, and turn on <em>animation mode</em>: subsequent
   * calls to drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt> will
   * not be displayed on screen until the next call to <tt>show()</tt>. This is useful for producing
   * animations (clear the screen, draw a bunch of shapes, display on screen for a fixed amount of
   * time, and repeat). It also speeds up drawing a huge number of shapes (call <tt>show(0)</tt> to
   * defer drawing on screen, draw the shapes, and call <tt>show(0)</tt> to display them all on
   * screen at once).
   * 
   * @param t number of milliseconds
   */
  public static void show(int t) {
    defer = false;
    draw();
    try {
      Thread.sleep(t);
    } catch (InterruptedException e) {
      System.out.println("Error sleeping");
    }
    defer = true;
  }
}
