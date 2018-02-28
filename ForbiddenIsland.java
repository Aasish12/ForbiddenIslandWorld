import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Assignment 9 part 1
//Basani Aasish
//Aasish2020
//Abrams John
//16jabrams

//Represents a single square of the game area
class Cell {
  // represents absolute height of this cell, in feet
  double height;
  // In logical coordinates, with the origin at the top-left corner of the
  // screen
  int x;
  int y;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
  // reports whether this cell is flooded or not
  boolean isFlooded;

  // constructor for cell
  Cell(double height, int x, int y, boolean isFlooded) {
    this.height = height;
    this.x = x;
    this.y = y;
    this.isFlooded = isFlooded;
  }

  // alternate constructor (used for the ocean cell)
  Cell(double height, int x, int y) {
    this.height = height;
    this.x = x;
    this.y = y;
  }

  // Creates the image of cell to be used in the world scene
  WorldImage createImage(int size, int waterheight) {
    if (this.isFlooded) {
      double constant = height / waterheight * 255;
      Color flooded = new Color(0, 0, Math.min(255, (int) constant));
      return new RectangleImage(10, 10, OutlineMode.SOLID, flooded);
    }
    else if (this.height <= waterheight) {
      double r = height / waterheight * 255;
      Color indanger = new Color(Math.min(255, (int) r), 56, 0);
      return new RectangleImage(10, 10, OutlineMode.SOLID, indanger);
    }
    else {
      double constant = height / size * 255;
      Color start = new Color(0, Math.min(255, (int) constant), 0);
      return new RectangleImage(10, 10, OutlineMode.SOLID, start);
    }
  }

  // Updates the color of the cell depending on its isFlooded status
  void changeImage(int water) {
    if (this.height <= water && (this.left.isFlooded || this.right.isFlooded
        || this.bottom.isFlooded || this.top.isFlooded)) {
      this.isFlooded = true;
    }
  }
}

// to represent an ocean cell
class OceanCell extends Cell {
  // Constructor for ocean cell
  OceanCell(double height, int x, int y) {
    super(height, x, y);
    this.isFlooded = true;
  }

  // Creates the image of an ocean cell to be used in the world scene
  WorldImage createImage(int size, int waterheight) {
    return new RectangleImage(10, 10, OutlineMode.SOLID, new Color(0, 0, 255));
  }

  // Updates the color of the cell depending on its isFlooded status (ocean
  // cells dont change)
  void changeImage(int water) {
    // Ocean Cells don't change
  }
}

// represents the player
class Player {
  int x;
  int y;
  FromFileImage pilot;

  Player(int x, int y, FromFileImage p) {
    this.x = x;
    this.y = y;
    this.pilot = p;
  }
}

// represents parts to be picked up
class Target {
  int x;
  int y;
  boolean isPickedUp;

  Target(int x, int y) {
    this.x = x;
    this.y = y;
    isPickedUp = false;
  }

  // draws targets as pink circles
  WorldImage drawTarget() {
    return new CircleImage(5, OutlineMode.SOLID, Color.pink);
  }
}

// represents the Helicopter
class HelicopterTarget extends Target {
  FromFileImage heli;

  HelicopterTarget(int x, int y) {
    super(x, y);
    heli = new FromFileImage("helicopter.png");
  }
}

// to represent the forbidden island world
class ForbiddenIslandWorld extends World {
  // All the cells of the game, including the ocean
  IList<Cell> board;
  // the current height of the ocean
  int waterHeight;
  // Defines an int constant
  static final int ISLAND_SIZE = 64;
  // Defines an int time
  int timer = 0;
  // All the heights of the cells in the world
  ArrayList<ArrayList<Double>> heights = new ArrayList<ArrayList<Double>>();
  // All the cells of the game (in a 2d arrangement)
  ArrayList<ArrayList<Cell>> cells = new ArrayList<ArrayList<Cell>>();
  // all the pieces of the heli
  ArrayList<Target> targets = new ArrayList<Target>();
  // the helicopter
  HelicopterTarget heli;
  // the player
  Player p1;
  // the other player
  Player p2;
  // flag for winning game
  boolean theGameIsOver = false;
  // flag for losing game
  boolean theGameIsLost = false;

  // create the island and give it a starting water height
  ForbiddenIslandWorld(int waterHeight) {
    this.waterHeight = waterHeight;
  }

  // creates the height for every position in the game
  void createHeights() {
    int row;
    int column;
    int middle = ISLAND_SIZE / 2;
    for (row = 0; row <= ISLAND_SIZE; row++) {
      ArrayList<Double> rowList = new ArrayList<Double>();
      for (column = 0; column <= ISLAND_SIZE; column++) {
        double height = ISLAND_SIZE - (Math.abs((middle - row)) + Math.abs((middle - column)));
        if (height <= ISLAND_SIZE / 2) {
          double ocean = (double) waterHeight;
          rowList.add(ocean);
        }
        else {
          rowList.add(height);
        }
      }
      heights.add(rowList);
    }
  }

  // cretes a random height for every position in the game that should be a land
  // cell
  void createRandomHeights() {
    int row;
    int column;
    int middle = ISLAND_SIZE / 2;
    Random rand = new Random();
    for (row = 0; row <= ISLAND_SIZE; row++) {
      ArrayList<Double> rowList = new ArrayList<Double>();
      for (column = 0; column <= ISLAND_SIZE; column++) {
        double height = ISLAND_SIZE - (Math.abs((middle - row)) + Math.abs((middle - column)));
        if (height <= middle) {
          double ocean = (double) waterHeight;
          rowList.add(ocean);
        }
        else {
          int randHeight = rand.nextInt(ISLAND_SIZE / 2) + ISLAND_SIZE / 2 + 1;
          double rheight = (double) randHeight;
          rowList.add(rheight);
        }
      }
      heights.add(rowList);
    }
  }

  // creates heights for random terrain island using algorithm
  void terrainHeights() {
    int row;
    int column;
    for (row = 0; row <= ISLAND_SIZE; row++) {
      ArrayList<Double> rowList = new ArrayList<Double>();
      for (column = 0; column <= ISLAND_SIZE; column++) {
        rowList.add(0.0);
      }
      heights.add(rowList);
    }
    int middle = (ISLAND_SIZE) / 2;
    ArrayList<Double> t = heights.get(0);
    t.set(0, 0.0);
    t.set(ISLAND_SIZE, 0.0);
    t.set(middle, 1.0);
    heights.set(0, t);

    ArrayList<Double> b = heights.get(ISLAND_SIZE);
    b.set(0, 0.0);
    b.set(ISLAND_SIZE, 0.0);
    b.set(middle, 1.0);
    heights.set(ISLAND_SIZE, b);

    ArrayList<Double> m = heights.get(middle);
    m.set(middle, (double) (ISLAND_SIZE));
    m.set(0, 1.0);
    m.set(ISLAND_SIZE, 1.0);
    heights.set(middle, m);

    this.terrainHelp(0, middle, 0, middle);
    this.terrainHelp(middle, ISLAND_SIZE, 0, middle);
    this.terrainHelp(0, middle, middle, ISLAND_SIZE);
    this.terrainHelp(middle, ISLAND_SIZE, middle, ISLAND_SIZE);
  }

  // used to subdivide island into quadrants
  void terrainHelp(int minx, int maxx, int miny, int maxy) {
    ArrayList<Double> top = heights.get(miny);
    ArrayList<Double> bot = heights.get(maxy);
    double tl = top.get(minx);
    double tr = top.get(maxx);
    double bl = bot.get(minx);
    double br = bot.get(maxx);
    int middlex = (minx + maxx) / 2;
    int middley = (maxy + miny) / 2;
    Random rand = new Random();
    double t = rand.nextInt(20) + ((tl + tr) / 2) - 10;
    double b = rand.nextInt(20) + ((bl + br) / 2) - 10;
    double l = rand.nextInt(20) + ((tl + bl) / 2) - 10;
    double r = rand.nextInt(20) + ((tr + br) / 2) - 10;

    double m = rand.nextInt(20) + ((tl + tr + bl + br) / 4) - 10;

    top.set(middlex, t);

    ArrayList<Double> mid = heights.get(middley);
    mid.set(minx, l);
    mid.set(middlex, m);
    mid.set(maxx, r);

    bot.set(middlex, b);

    if (2 <= (maxx - minx)) {
      this.terrainHelp(minx, middlex, miny, middley);
      this.terrainHelp(middlex, maxx, miny, middley);
      this.terrainHelp(minx, middlex, middley, maxy);
      this.terrainHelp(middlex, maxx, middley, maxy);
    }
  }

  // creates all the cells in the game
  void createCells() {
    int row;
    int column;
    for (row = 0; row <= ISLAND_SIZE; row++) {
      ArrayList<Cell> rowList = new ArrayList<Cell>();
      for (column = 0; column <= ISLAND_SIZE; column++) {
        double height = heights.get(row).get(column);
        if (height == (double) waterHeight || height <= 0) {
          OceanCell create = new OceanCell((double) waterHeight, row, column);
          rowList.add(create);
        }
        else {
          Cell create = new Cell(height, row, column, false);
          rowList.add(create);
        }
      }
      cells.add(rowList);
    }
  }

  // links the cells to the neighboring cells
  void linkCells() {
    int row;
    int column;
    for (row = 0; row <= ISLAND_SIZE; row++) {
      for (column = 0; column <= ISLAND_SIZE; column++) {
        Cell cell = cells.get(row).get(column);

        if (row == 0 && column == 0) {
          cell.top = cell;
          cell.left = cell;
          cell.right = cells.get(row).get(column + 1);
          cell.bottom = cells.get(row + 1).get(column);
        }
        else if (row == 0 && column == ISLAND_SIZE) {
          cell.left = cells.get(row).get(column - 1);
          cell.top = cell;
          cell.right = cell;
          cell.bottom = cells.get(row + 1).get(column);
        }
        else if (row == 0) {
          cell.left = cells.get(row).get(column - 1);
          cell.top = cell;
          cell.right = cells.get(row).get(column + 1);
          cell.bottom = cells.get(row + 1).get(column);
        }
        else if (row == ISLAND_SIZE && column == 0) {
          cell.left = cell;
          cell.top = cells.get(row - 1).get(column);
          cell.right = cells.get(row).get(column + 1);
          cell.bottom = cell;
        }
        else if (row == ISLAND_SIZE && column == ISLAND_SIZE) {
          cell.left = cells.get(row).get(column - 1);
          cell.top = cells.get(row - 1).get(column);
          cell.right = cell;
          cell.bottom = cell;
        }
        else if (row == ISLAND_SIZE) {
          cell.left = cells.get(row).get(column - 1);
          cell.top = cells.get(row - 1).get(column);
          cell.right = cells.get(row).get(column + 1);
          cell.bottom = cell;
        }
        else if (column == 0) {
          cell.left = cell;
          cell.top = cells.get(row - 1).get(column);
          cell.right = cells.get(row).get(column + 1);
          cell.bottom = cells.get(row + 1).get(column);
        }
        else if (column == ISLAND_SIZE) {
          cell.left = cells.get(row).get(column - 1);
          cell.top = cells.get(row - 1).get(column);
          cell.right = cell;
          cell.bottom = cells.get(row + 1).get(column);
        }

        else {
          cell.left = cells.get(row).get(column - 1);
          cell.top = cells.get(row - 1).get(column);
          cell.right = cells.get(row).get(column + 1);
          cell.bottom = cells.get(row + 1).get(column);
        }
      }
    }
  }

  // EFFECT:
  // puts all the cells in the arraylist into a Ilist
  void createBoard() {
    int row;
    int column;
    board = new MtList<Cell>();
    for (row = 0; row <= ISLAND_SIZE; row++) {
      for (column = 0; column <= ISLAND_SIZE; column++) {
        board = new ConsList<Cell>(cells.get(row).get(column), board);
      }
    }
  }

  // creates the world scene
  public WorldScene makeScene() {
    WorldScene island = new WorldScene(ISLAND_SIZE * 10, ISLAND_SIZE * 10);
    for (Cell c : board) {
      island.placeImageXY(c.createImage(ISLAND_SIZE, waterHeight), c.x * 10, c.y * 10);
    }
    island.placeImageXY(p1.pilot, p1.x * 10, p1.y * 10);
    island.placeImageXY(p2.pilot, p2.x * 10, p2.y * 10);
    for (Target t : targets) {
      if ((p1.x == t.x && p1.y == t.y) || (p2.x == t.x && p2.y == t.y)) {
        t.isPickedUp = true;
      }
    }
    for (Target t : targets) {
      if (!t.isPickedUp) {
        island.placeImageXY(t.drawTarget(), t.x * 10, t.y * 10);
      }
    }
    if (heli.x == p1.x && heli.y == p1.y && heli.x == p2.x && heli.y == p2.y) {
      int count = 0;
      for (Target t : targets) {
        if (!t.isPickedUp) {
          island.placeImageXY(heli.heli, heli.x * 10, heli.y * 10);
          count++;
        }
      }
      if (count == 0) {
        theGameIsOver = true;
      }
    }
    for (Cell c : board) {
      if ((c.x == p1.x && c.y == p1.y && c.isFlooded)
          || (c.x == p2.x && c.y == p2.y && c.isFlooded)) {
        theGameIsLost = true;
      }
    }
    island.placeImageXY(heli.heli, heli.x * 10, heli.y * 10);
    return island;
  }

  // gives heli position at highest point on island
  void createHeli() {
    ArrayList<Cell> largest = new ArrayList<Cell>();
    Cell max = new Cell(0, 0, 0);
    for (Cell c : board) {
      if (c.height > max.height) {
        max = c;
      }
    }
    for (Cell c : board) {
      if (c.height == max.height) {
        largest.add(c);
      }
    }
    Random rand = new Random();
    int helilanding = rand.nextInt(largest.size());
    Cell landing = largest.get(helilanding);
    heli = new HelicopterTarget(landing.x, landing.y);
  }

  // EFFECT: // floods the island every 10 seconds by 1 foot
  public void onTick() {
    timer++;
    if (timer % 10 == 0) {
      waterHeight++;
      for (Cell c : board) {
        c.changeImage(waterHeight);
      }
    }
  }

  // moves players, restarts game
  public void onKeyEvent(String key) {
    if (key.equals("left")) {
      boolean wl = true;
      for (Cell c : board) {
        if (c.x == p1.x - 1 && c.y == p1.y && c.isFlooded) {
          wl = false;
        }
      }
      if (wl) {
        p1.x = p1.x - 1;
      }
    }
    else if (key.equals("right")) {
      boolean wr = true;
      for (Cell c : board) {
        if (c.x == p1.x + 1 && c.y == p1.y && c.isFlooded) {
          wr = false;
        }
      }
      if (wr) {
        p1.x = p1.x + 1;
      }
    }
    else if (key.equals("up")) {
      boolean wu = true;
      for (Cell c : board) {
        if (c.y == p1.y - 1 && c.x == p1.x && c.isFlooded) {
          wu = false;
        }
      }
      if (wu) {
        p1.y = p1.y - 1;
      }
    }
    else if (key.equals("down")) {
      boolean wd = true;
      for (Cell c : board) {
        if (c.y == p1.y + 1 && c.x == p1.x && c.isFlooded) {
          wd = false;
        }
      }
      if (wd) {
        p1.y = p1.y + 1;

      }
    }
    if (key.equals("a")) {
      boolean wl = true;
      for (Cell c : board) {
        if (c.x == p2.x - 1 && c.y == p2.y && c.isFlooded) {
          wl = false;
        }
      }
      if (wl) {
        p2.x = p2.x - 1;
      }
    }
    else if (key.equals("d")) {
      boolean wr = true;
      for (Cell c : board) {
        if (c.x == p2.x + 1 && c.y == p2.y && c.isFlooded) {
          wr = false;
        }
      }
      if (wr) {
        p2.x = p2.x + 1;
      }
    }
    else if (key.equals("w")) {
      boolean wu = true;
      for (Cell c : board) {
        if (c.y == p2.y - 1 && c.x == p2.x && c.isFlooded) {
          wu = false;
        }
      }
      if (wu) {
        p2.y = p2.y - 1;
      }
    }
    else if (key.equals("s")) {
      boolean wd = true;
      for (Cell c : board) {
        if (c.y == p2.y + 1 && c.x == p2.x && c.isFlooded) {
          wd = false;
        }
      }
      if (wd) {
        p2.y = p2.y + 1;

      }
    }
    else if (key.equals("m")) {
      ForbiddenIslandWorld mountain = new ForbiddenIslandWorld(32);
      mountain.createHeights();
      mountain.createCells();
      mountain.createBoard();
      mountain.createTargets();
      mountain.createHeli();
      mountain.linkCells();
      mountain.randPos();
      theGameIsOver = true;
      mountain.bigBang(ForbiddenIslandWorld.ISLAND_SIZE * 10, ForbiddenIslandWorld.ISLAND_SIZE * 10,
          0.1);
    }
    else if (key.equals("r")) {
      ForbiddenIslandWorld random = new ForbiddenIslandWorld(32);
      random.createRandomHeights();
      random.createCells();
      random.createBoard();
      random.createTargets();
      random.createHeli();
      random.linkCells();
      random.randPos();
      theGameIsOver = true;
      random.bigBang(ForbiddenIslandWorld.ISLAND_SIZE * 10, ForbiddenIslandWorld.ISLAND_SIZE * 10,
          0.1);
    }
    else if (key.equals("t")) {
      ForbiddenIslandWorld terrain = new ForbiddenIslandWorld(0);
      terrain.terrainHeights();
      terrain.createCells();
      terrain.createBoard();
      terrain.createTargets();
      terrain.createHeli();
      terrain.linkCells();
      terrain.randPos();
      theGameIsOver = true;
      terrain.bigBang(ForbiddenIslandWorld.ISLAND_SIZE * 10, ForbiddenIslandWorld.ISLAND_SIZE * 10,
          0.1);
    }
  }

  // how to end world
  public WorldEnd worldEnds() {
    if (theGameIsOver) {
      return new WorldEnd(true, this.makeAFinalSceneWon());
    }
    else if (theGameIsLost) {
      return new WorldEnd(true, this.makeAFinalSceneLost());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // the final victory scene
  public WorldScene makeAFinalSceneWon() {
    WorldScene ending = new WorldScene(ISLAND_SIZE * 10, ISLAND_SIZE * 10);
    ending.placeImageXY(new TextImage("You Won!! ", 25, FontStyle.BOLD, Color.red),
        ISLAND_SIZE / 2 * 10, ISLAND_SIZE / 2 * 10);
    return ending;
  }

  // the final loss scene
  public WorldScene makeAFinalSceneLost() {
    WorldScene ending = new WorldScene(ISLAND_SIZE * 10, ISLAND_SIZE * 10);
    ending.placeImageXY(new TextImage("Sorry You Lost!!", 25, FontStyle.BOLD, Color.red),
        ISLAND_SIZE / 2 * 10, ISLAND_SIZE / 2 * 10);
    return ending;

  }

  // generates random land position
  void randPos() {
    ArrayList<Cell> cellList = new ArrayList<Cell>();
    for (Cell c : board) {
      if (!c.isFlooded) {
        cellList.add(c);
      }
    }
    Random rand = new Random();
    int spotp2 = rand.nextInt(cellList.size());
    Cell cellp2 = cellList.get(spotp2);
    p2 = new Player(cellp2.x, cellp2.y, new FromFileImage("pilot2.png"));
    cellList.remove(cellp2);
    int spotp1 = rand.nextInt(cellList.size());
    Cell cellp1 = cellList.get(spotp1);
    p1 = new Player(cellp1.x, cellp1.y, new FromFileImage("pilot.png"));
    cellList.remove(cellp1);

  }

  // creates random amount of targets from 1 - 5, placed randomly
  void createTargets() {
    ArrayList<Cell> landList = new ArrayList<Cell>();
    for (Cell c : board) {
      if (!c.isFlooded) {
        landList.add(c);
      }
    }
    Random rand = new Random();
    for (int i = 0; i <= rand.nextInt(10); i++) {
      int spot = rand.nextInt(landList.size());
      Cell cell = landList.get(spot);
      landList.remove(spot);
      Target helipiece = new Target(cell.x, cell.y);
      targets.add(helipiece);
    }
  }
}

class IListIterator<T> implements Iterator<T> {
  IList<T> items;

  IListIterator(IList<T> items) {
    this.items = items;
  }

  // In IListIterator
  public boolean hasNext() {
    return this.items.isCons();
  }

  // In IListIterator
  public T next() {
    ConsList<T> itemsAsCons = this.items.asCons();
    T answer = itemsAsCons.first;
    this.items = itemsAsCons.rest;
    return answer;
  }

  public void remove() {
    throw new UnsupportedOperationException("Don't do this!");
  }
}

// Declare that every IList is an Iterable:
interface IList<T> extends Iterable<T> {
  boolean isCons();

  ConsList<T> asCons();

}

// to represent a not empty list of elements
class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  // the iterator method
  public Iterator<T> iterator() {
    return new IListIterator<T>(this);
  }

  // determines if this list a cons
  public boolean isCons() {
    return true;
  }

  public ConsList<T> asCons() {
    return this;
  }
}

// to represent a empty list of elements
class MtList<T> implements IList<T> {

  public Iterator<T> iterator() {
    return new IListIterator<T>(this);
  }

  public boolean isCons() {
    return false;
  }

  public ConsList<T> asCons() {
    throw new ClassCastException("should not happen");
  }
}

// represents examples and tests
class ForbiddenIslandExamples {
  ArrayList<ArrayList<Double>> expectedh = new ArrayList<ArrayList<Double>>(
      Arrays.asList(new ArrayList<Double>(Arrays.asList(0.0, 1.0, 2.0, 1.0, 0.0)),
          new ArrayList<Double>(Arrays.asList(1.0, 2.0, 3.0, 2.0, 1.0)),
          new ArrayList<Double>(Arrays.asList(2.0, 3.0, 4.0, 3.0, 2.0)),
          new ArrayList<Double>(Arrays.asList(1.0, 2.0, 3.0, 2.0, 1.0)),
          new ArrayList<Double>(Arrays.asList(0.0, 1.0, 2.0, 1.0, 0.0))));

  OceanCell o1 = new OceanCell(0.0, 0, 0);
  OceanCell o2 = new OceanCell(1.0, 0, 1);

  ArrayList<Cell> r1 = new ArrayList<Cell>(Arrays.asList(o1, o2, new OceanCell(2.0, 0, 2),
      new OceanCell(1.0, 0, 3), new OceanCell(0.0, 0, 4)));
  ArrayList<Cell> r2 = new ArrayList<Cell>(
      Arrays.asList(new OceanCell(1.0, 1, 0), new OceanCell(2.0, 1, 1), new Cell(3.0, 1, 2, false),
          new OceanCell(2.0, 1, 3), new OceanCell(1.0, 1, 4)));
  ArrayList<Cell> r3 = new ArrayList<Cell>(
      Arrays.asList(new OceanCell(2.0, 2, 0), new Cell(3.0, 2, 1, false),
          new Cell(4.0, 2, 2, false), new Cell(3.0, 2, 3, false), new OceanCell(2.0, 2, 4)));
  ArrayList<Cell> r4 = new ArrayList<Cell>(
      Arrays.asList(new OceanCell(1.0, 3, 0), new OceanCell(2.0, 3, 1), new Cell(3.0, 3, 2, false),
          new OceanCell(2.0, 3, 3), new OceanCell(1.0, 3, 4)));
  ArrayList<Cell> r5 = new ArrayList<Cell>(
      Arrays.asList(new OceanCell(0.0, 4, 0), new OceanCell(1.0, 4, 1), new OceanCell(2.0, 4, 2),
          new OceanCell(1.0, 4, 3), new OceanCell(0.0, 4, 4)));
  ArrayList<ArrayList<Cell>> expectedc = new ArrayList<ArrayList<Cell>>(
      Arrays.asList(r1, r2, r3, r4, r5));
  ForbiddenIslandWorld diamond;

  // initialize mountain world
  void initDiamond() {
    diamond = new ForbiddenIslandWorld(32);
    diamond.createHeights();
    diamond.createCells();
    diamond.createBoard();
    diamond.createTargets();
    diamond.createHeli();
    diamond.linkCells();
    diamond.randPos();
  }

  // initialize random mountain world
  void initRandom() {
    diamond = new ForbiddenIslandWorld(32);
    diamond.createRandomHeights();
    diamond.createCells();
    diamond.createBoard();
    diamond.createTargets();
    diamond.createHeli();
    diamond.linkCells();
    diamond.randPos();
  }

  // initialize random terrain world
  void initTerrain() {
    diamond = new ForbiddenIslandWorld(0);
    diamond.terrainHeights();
    diamond.createCells();
    diamond.createBoard();
    diamond.createTargets();
    diamond.createHeli();
    diamond.linkCells();
    diamond.randPos();
  }

  // tests that the player can not be placed on a flooded cell
  boolean testPlayerOnFlooded(Tester t) {
    initDiamond();
    boolean mFlooded = true;
    for (Cell c : diamond.board) {
      if (c.x == diamond.p1.x && c.y == diamond.p1.y) {
        mFlooded = c.isFlooded;
      }
    }
    initRandom();
    boolean rFlooded = true;
    for (Cell c : diamond.board) {
      if (c.x == diamond.p1.x && c.y == diamond.p1.y) {
        rFlooded = c.isFlooded;
      }
    }
    initTerrain();
    boolean tFlooded = true;
    for (Cell c : diamond.board) {
      if (c.x == diamond.p1.x && c.y == diamond.p1.y) {
        tFlooded = c.isFlooded;
      }
    }
    return !mFlooded && !rFlooded && !tFlooded;
  }

  // check if the heights are between 0 and Island size
  boolean testHeightRange(Tester t) {
    initDiamond();
    boolean inRange = true;
    for (Cell c : diamond.board) {
      if (c.height < 0 || c.height > diamond.ISLAND_SIZE) {
        inRange = false;
      }
    }
    initRandom();
    boolean inRange2 = true;
    for (Cell c : diamond.board) {
      if (c.height < 0 || c.height > diamond.ISLAND_SIZE) {
        inRange2 = false;
      }
    }
    initTerrain();
    boolean inRange3 = true;
    for (Cell c : diamond.board) {
      if (c.height < 0 || c.height > diamond.ISLAND_SIZE) {
        inRange3 = false;
      }
    }
    return inRange && inRange2 && inRange3;
  }

  // tests there are between 1 and 5 targets
  boolean testTargetNumbers(Tester t) {
    initDiamond();
    boolean inRange = false;
    if (diamond.targets.size() > 0 && diamond.targets.size() < 6) {
      inRange = true;
    }
    initRandom();
    boolean inRange2 = false;
    if (diamond.targets.size() > 0 && diamond.targets.size() < 6) {
      inRange = true;
    }
    initTerrain();
    boolean inRange3 = false;
    if (diamond.targets.size() > 0 && diamond.targets.size() < 6) {
      inRange = true;
    }
    return inRange && inRange2 && inRange3;
  }

  // are all cells flooded when the waterheight is larger than the island size
  boolean testAllCellsFlood(Tester t) {
    diamond = new ForbiddenIslandWorld(diamond.ISLAND_SIZE + 1);
    diamond.createHeights();
    diamond.createCells();
    diamond.createBoard();
    diamond.createTargets();
    diamond.createHeli();
    diamond.linkCells();
    diamond.randPos();
    boolean allFlooded = true;
    for (Cell c : diamond.board) {
      if (!c.isFlooded) {
        allFlooded = false;
      }
    }

    diamond = new ForbiddenIslandWorld(diamond.ISLAND_SIZE + 1);
    diamond.createRandomHeights();
    diamond.createCells();
    diamond.createBoard();
    diamond.createTargets();
    diamond.createHeli();
    diamond.linkCells();
    diamond.randPos();
    boolean allFlooded2 = true;
    for (Cell c : diamond.board) {
      if (!c.isFlooded) {
        allFlooded2 = false;
      }
    }

    diamond = new ForbiddenIslandWorld(diamond.ISLAND_SIZE + 1);
    diamond.terrainHeights();
    diamond.createCells();
    diamond.createBoard();
    diamond.createTargets();
    diamond.createHeli();
    diamond.linkCells();
    diamond.randPos();
    boolean allFlooded3 = true;
    for (Cell c : diamond.board) {
      if (!c.isFlooded) {
        allFlooded3 = false;
      }
    }
    return allFlooded && allFlooded2 && allFlooded3;
  }

  // is the helicopter placed on the highest point?
  boolean testHeliPos(Tester t) {
    initDiamond();
    double heliHeight = 0;
    for (Cell c : diamond.board) {
      if (c.x == diamond.heli.x && c.y == diamond.heli.y) {
        heliHeight = c.height;
      }
    }
    boolean isHighest = true;
    for (Cell c : diamond.board) {
      if (c.height > heliHeight) {
        isHighest = false;
      }
    }
    initRandom();
    double heliHeight2 = 0;
    for (Cell c : diamond.board) {
      if (c.x == diamond.heli.x && c.y == diamond.heli.y) {
        heliHeight2 = c.height;
      }
    }
    boolean isHighest2 = true;
    for (Cell c : diamond.board) {
      if (c.height > heliHeight) {
        isHighest2 = false;
      }
    }
    initTerrain();
    double heliHeight3 = 0;
    for (Cell c : diamond.board) {
      if (c.x == diamond.heli.x && c.y == diamond.heli.y) {
        heliHeight3 = c.height;
      }
    }
    boolean isHighest3 = true;
    for (Cell c : diamond.board) {
      if (c.height > heliHeight) {
        isHighest3 = false;
      }
    }
    return isHighest && isHighest2 && isHighest3;
  }

  // is the middle the highest point on the mountain island
  boolean testMountainMiddle(Tester t) {
    initDiamond();
    ArrayList<Cell> c1 = diamond.cells.get(diamond.ISLAND_SIZE);
    Cell middle = c1.get(diamond.ISLAND_SIZE);
    Double middleHeight = middle.height;
    boolean isHighest = true;
    for (Cell c : diamond.board) {
      if (c.height > middleHeight) {
        isHighest = false;
      }
    }
    return isHighest;
  }

  /*
   * // only works when the island size is 4 boolean
   * 
   * testCreateHeights(Tester t) { this.initDiamond(); return
   * t.checkExpect(diamond.heights, expectedh); }
   * 
   * boolean testCreateCells(Tester t) { this.initDiamond(); return
   * t.checkExpect(diamond.cells, expectedc); }
   *
   */

  // runs the game
  void testBigBang(Tester t) {
    this.initTerrain();
    diamond.bigBang(diamond.ISLAND_SIZE * 10, diamond.ISLAND_SIZE * 10, .2);
  }
}