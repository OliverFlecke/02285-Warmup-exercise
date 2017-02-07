package searchclient;

import java.awt.Point;
import java.util.Comparator;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import searchclient.NotImplementedException;

public abstract class Heuristic implements Comparator<Node> {
	private final HashMap<Character,Set<Point>> goals;
	
	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		// Map all goals to their corresponding characters
		HashMap<Character,Set<Point>> goals = new HashMap<Character,Set<Point>>();
		for (int row = 0; row < initialState.MAX_ROW; row++) {
			for (int col = 0; col < initialState.MAX_COL; col++) {
				if (initialState.goals[row][col] > 0) {
					if (goals.containsKey(initialState.goals[row][col])) {
						goals.get(initialState.goals[row][col]).add(new Point(row,col));
					} else {
						Set<Point> points = new HashSet<Point>();
						points.add(new Point(row,col));
						goals.put(initialState.goals[row][col],points);
					}
				}
			}
		}
		this.goals = goals;
	}

	public int h(Node n) {
		int h = 0;
		// Map all boxes to their corresponding characters
		HashMap<Character,Set<Point>> boxes = new HashMap<Character,Set<Point>>();
		for (int row = 0; row < n.MAX_ROW; row++) {
			for (int col = 0; col < n.MAX_COL; col++) {
				if (n.boxes[row][col] > 0) {
					if (boxes.containsKey(n.boxes[row][col])) {
						boxes.get(n.boxes[row][col]).add(new Point(row,col));
					} else {
						Set<Point> points = new HashSet<Point>();
						points.add(new Point(row,col));
						boxes.put(n.boxes[row][col],points);
					}
				}
			}
		}
		
		int agentDistance = Integer.MAX_VALUE;
		
		for (Entry<Character,Set<Point>> goalEntry : goals.entrySet()) {
			for (Point goal : goalEntry.getValue()) {
				// Continue if the goal has a correct box
				// Remove the box from the HashMap if its on a goal?
				if (n.boxes[goal.x][goal.y] == Character.toUpperCase(goalEntry.getKey()))
					continue;
				// Find the box closest to the goal
				int shortestDistance = Integer.MAX_VALUE;
				Point shortestBox = null;
				for (Point box : boxes.get(Character.toUpperCase(goalEntry.getKey()))) {					
					int distance = Math.abs(goal.x-box.x) + Math.abs(goal.y-box.y);
					if (distance < shortestDistance) {
						shortestDistance = distance;
						shortestBox = box;
					}
				}
				int distance = Math.abs(n.agentRow-shortestBox.x) + Math.abs(n.agentCol-shortestBox.y);
				if (distance < agentDistance)
					agentDistance = distance;
				h += shortestDistance;
			}
		}
		
		// If in a goal state
		if (h == 0)
			return h;
		
		return h + agentDistance;
	}

	public abstract int f(Node n);

	@Override
	public int compare(Node n1, Node n2) {
		return this.f(n1) - this.f(n2);
	}

	public static class AStar extends Heuristic {
		public AStar(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return n.g() + this.h(n);
		}

		@Override
		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState, int W) {
			super(initialState);
			this.W = W;
		}

		@Override
		public int f(Node n) {
			return n.g() + this.W * this.h(n);
		}

		@Override
		public String toString() {
			return String.format("WA*(%d) evaluation", this.W);
		}
	}

	public static class Greedy extends Heuristic {
		public Greedy(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return this.h(n);
		}

		@Override
		public String toString() {
			return "Greedy evaluation";
		}
	}
}
