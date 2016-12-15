package hu.uniobuda.nik.map;

/**
 * Created by Forisz on 16/11/16.
 */

public class MapData {

	private static final String TAG = MapData.class.getSimpleName();

	private static final int DEFAULT_WIDTH = 7;
	private static final int DEFAULT_HEIGHT = 7;

	private static final byte[][] TEST_MAP_1;

	/**
	 * Determines that the current tile is a background tile on the map
	 */
	public static final byte BACKGROUND = 0;

	/**
	 * Determines that the current tile is an obstacle tile on the map
	 */
	public static final byte OBSTACLE = 1;

	/**
	 * Determines that the current tile is the robot indicator tile on the map
	 */
	public static final byte ROBOT = 2;

	/**
	 * Determines that the current tile is the destination indicator tile on the map
	 */
	public static final byte DESTINATION = 3;

	/**
	 * Determines that the current tile is part of a path on the map
	 */
	public static final byte PATH = 4;

	//@formatter:off
	static {
		TEST_MAP_1 = new byte [][] {
			{BACKGROUND,	BACKGROUND,	BACKGROUND,	BACKGROUND,	BACKGROUND, BACKGROUND,	BACKGROUND},
			{DESTINATION,	OBSTACLE,	OBSTACLE,	BACKGROUND,	BACKGROUND,	BACKGROUND, BACKGROUND},
			{BACKGROUND,	OBSTACLE,	BACKGROUND,	BACKGROUND,	BACKGROUND,	BACKGROUND, BACKGROUND},
			{BACKGROUND,	OBSTACLE,	BACKGROUND,	BACKGROUND,	BACKGROUND,	BACKGROUND, ROBOT},
			{BACKGROUND,	OBSTACLE,	OBSTACLE,	BACKGROUND,	BACKGROUND,	BACKGROUND, BACKGROUND},
			{BACKGROUND,	OBSTACLE,	OBSTACLE,	BACKGROUND,	BACKGROUND,	BACKGROUND, BACKGROUND},
			{BACKGROUND,	BACKGROUND,	BACKGROUND,	BACKGROUND,	BACKGROUND,	BACKGROUND, BACKGROUND}
		};
	}
	//@formatter:on

	private int width;
	private int height;
	byte[][] data;

	public void setData(int x, int y, byte value) {
		this.data[x][y] = value;
	}

	public int getWidth() {
		return width;
	}

	public MapData setWidth(int width) {
		this.width = width;
		return this;
	}

	public int getHeight() {
		return height;
	}

	public MapData setHeight(int height) {
		this.height = height;
		return this;
	}

	public byte[][] getData() {
		return data;
	}

	public byte getData(int x, int y) {
		return data[x][y];
	}

	public MapData setData(byte[][] data) {
		this.data = data;
		return this;
	}

	public MapData() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public MapData(int width, int height) {
		this.width = width;
		this.height = height;
	//	data = new byte[width][height];
		data = TEST_MAP_1;
	}
}
