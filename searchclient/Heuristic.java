package searchclient;

import java.awt.Point;
import java.util.Comparator;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.*;

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
		HashMap<Character, Set<Point>> boxes = findBoxes(n);
		HashMap<Point, HashMap<Point, Integer>> dists = findDistances(n, boxes);

		for (Entry<Point, HashMap<Point, Integer>> boxEntry : dists.entrySet()) {
			Point closestGoal = null;
			int minDist = Integer.MAX_VALUE;
			for (Entry<Point, Integer> goal : boxEntry.getValue().entrySet()) {
				if (closestGoal == null || minDist > goal.getValue()) {
					closestGoal = goal.getKey();
					minDist = goal.getValue();
				}
			}
			h += minDist;
		}
		
		if (h == 0) return 0;
		
		return h;
	}

	private HashMap<Point, HashMap<Point, Integer>> findDistances(Node n, HashMap<Character, Set<Point>> boxes)
	{
		HashMap<Point, HashMap<Point, Integer>> distances = new HashMap<Point, HashMap<Point, Integer>>();
		
		for (Entry<Character, Set<Point>> boxesEntrySet : boxes.entrySet()) 
			for (Point box : boxesEntrySet.getValue())
				distances.put(box, new HashMap<Point, Integer>());

		for (Entry<Character, Set<Point>> goalEntry : goals.entrySet()) {
			char boxChar = Character.toUpperCase(goalEntry.getKey());

			// For all goals in the level
			for (Point goal : goalEntry.getValue()) {
				// Box on goal then ignore
				if (boxChar == n.boxes[goal.x][goal.y])
					continue;

				int shortestDistance = Integer.MAX_VALUE;
				Point shortestBox = null;

				for (Point box : boxes.get(boxChar)) {
					int distance = Math.abs(goal.x-box.x) + Math.abs(goal.y-box.y);
					distances.get(box).put(goal, distance);
				}
			}
		}

		return distances;
	} 

	public int h_1(Node n) {
		int h = 0;
		HashMap<Character, Set<Point>> boxes = findBoxes(n);

		int agentDistance = Integer.MAX_VALUE;

		for (Entry<Character,Set<Point>> goalEntry : goals.entrySet()) {
			char boxChar = Character.toUpperCase(goalEntry.getKey());
			// List<Point> sortedSet = new ArrayList<Point>(goalEntry.getValue());
			// Collections.sort(sortedSet, new Comparator<Point>() {
			// 	public int compare(Point p1, Point p2) {
			// 		return (p1.x - p2.x) + (p1.y - p2.y);
			// 	}
			// });

			for (Point goal : goalEntry.getValue()) {
				// Box on goal
				if (boxChar == n.boxes[goal.x][goal.y])
					continue;
				// Find the box closest to the goal
				int shortestDistance = Integer.MAX_VALUE;
				Point shortestBox = null;

				for (Point box : boxes.get(boxChar)) {
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
				// Remove box to prevent goals from finding the same closest box
				boxes.get(boxChar).remove(shortestBox);
			}
		}

		if (h == 0) {
			return h;
		}

		// return h;
		return h + agentDistance;
	}

	private HashMap<Character, Set<Point>> findBoxes(Node n)
	{
		// Map all boxes to their corresponding characters
		HashMap<Character,Set<Point>> boxes = new HashMap<Character,Set<Point>>();
		for (int row = 0; row < n.MAX_ROW; row++) {
			for (int col = 0; col < n.MAX_COL; col++) {
				char boxChar = n.boxes[row][col];
				// No box in cell
				if (boxChar == 0)
					continue;
				// Box on goal
				if (boxChar == Character.toUpperCase(n.goals[row][col]))
					continue;
				// Add box' point to boxes
				Point boxPoint = new Point(row,col);
				if (boxes.containsKey(boxChar)) {
					boxes.get(boxChar).add(boxPoint);
				} else {
					Set<Point> points = new HashSet<Point>();
					points.add(boxPoint);
					boxes.put(boxChar,points);
				}
			}
		}
		return boxes;
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
