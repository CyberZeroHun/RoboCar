package hu.uniobuda.nik.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Forisz on 13/12/16.
 */

public class PathFinder {

	List<Node> nodeList;
	Node destination;
	Node start;

	public Node getDestination() {
		return destination;
	}

	public PathFinder setDestination(Node destination) {
		this.destination = destination;
		return this;
	}

	public Node getStart() {
		return start;
	}

	public PathFinder setStart(Node start) {
		this.start = start;
		return this;
	}

	public PathFinder(MapData data) {
		nodeList = convertMapDataToNodeList(data);
	}

	public PathFinder(byte[][] data) {
		nodeList = convertMapDataToNodeList(data);
	}

	private List<Node> convertMapDataToNodeList(byte[][] dataArray) {
		List<Node> nodeList = new ArrayList<>(dataArray.length * dataArray[0].length);

		for (int x = 0; x < dataArray.length; x++) {
			for (int y = 0; y < dataArray[x].length; y++) {
				byte nodeType = dataArray[x][y];

				if (nodeType == MapData.BACKGROUND || nodeType == MapData.DESTINATION || nodeType == MapData.ROBOT) {
					Node node = new Node();
					node.x = x;
					node.y = y;
					nodeList.add(node);

					if (nodeType == MapData.ROBOT) {
						start = node;
					} else if (nodeType == MapData.DESTINATION) {
						destination = node;
					}
				}

			}
		}


		// Setup the neighbours in the node list
		for (Node node : nodeList) {
			for (Node tmpNode : nodeList) {
				if (Math.abs(tmpNode.x - node.x) + Math.abs(tmpNode.y - node.y) == 1) {
					node.neighbours.add(tmpNode);
				}
			}
		}

		return nodeList;
	}

	private List<Node> convertMapDataToNodeList(MapData data) {
		byte[][] dataArray = data.getData();

		return convertMapDataToNodeList(dataArray);
	}

	public List<Node> aStar() {
		if (start == null || destination == null)
			return null;

		Set<Node> open = new HashSet<>();
		Set<Node> closed = new HashSet<>();

		start.g = 0;
		start.h = estimateDistance(start, destination);
		start.f = start.h;

		// Let's start by adding the start node to our open list
		open.add(start);


		while (true) { // :S
			Node current = null;
			// If the open set is empty, then there is no route to the destination
			if (open.size() == 0) {
				throw new PathFinderException("No route to destination", null);
			}

			for (Node node : open) {
				if (current == null || node.f < current.f) {
					current = node;
				}
			}
			if (current == destination) {
				break;
			}

			open.remove(current);
			closed.add(current);

			for (Node neighbour : current.neighbours) {
				if (neighbour == null) {
					continue;
				}
				int nextG = current.g + neighbour.cost;

				if (nextG < neighbour.g) {
					open.remove(neighbour);
					closed.remove(neighbour);
				}

				if (!open.contains(neighbour) && !closed.contains(neighbour)) {
					neighbour.g = nextG;
					neighbour.h = estimateDistance(neighbour, destination);
					neighbour.f = neighbour.g + neighbour.h;
					neighbour.parent = current;
					open.add(neighbour);
				}
			}


		}


		List<Node> nodes = new ArrayList<>();

		Node current = destination;
		while (current.parent != null) {
			nodes.add(current);
			current = current.parent;
		}
		nodes.add(start);

		return nodes;
	}

	private static int estimateDistance(Node node1, Node node2) {
		return Math.abs(node1.x - node2.x) + Math.abs(node1.y - node2.y);
	}


}
