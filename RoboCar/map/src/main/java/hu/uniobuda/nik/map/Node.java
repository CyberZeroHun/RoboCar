package hu.uniobuda.nik.map;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Forisz on 13/12/16.
 */

public class Node {

	List<Node> neighbours = new ArrayList<>();
	Node parent;
	int f;
	int g;
	int h;
	int x;
	int y;
	int cost;

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
