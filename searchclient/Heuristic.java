package searchclient;

import java.awt.Point;
import java.util.Comparator;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public abstract class Heuristic implements Comparator<Node> {
	private final HashMap<Character,Set<Point>> goals;
	private final HashMap<Character,Set<Integer[][]>> goalMaps;
	
	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		// Map all goals and boxes to their corresponding characters
		HashMap<Character,Set<Point>> goals = new HashMap<Character,Set<Point>>();
		HashMap<Character,Set<Point>> boxes = new HashMap<Character,Set<Point>>();
		for (int row = 0; row < Node.MAX_ROW; row++) {
			for (int col = 0; col < Node.MAX_COL; col++) {
				if (initialState.goals[row][col] > 0) {
					if (goals.containsKey(initialState.goals[row][col])) {
						goals.get(initialState.goals[row][col]).add(new Point(row,col));
					} else {
						Set<Point> points = new HashSet<Point>();
						points.add(new Point(row,col));
						goals.put(initialState.goals[row][col],points);
					}
				}
				if (initialState.boxes[row][col] > 0){
					if (boxes.containsKey(initialState.boxes[row][col])) {
						boxes.get(initialState.boxes[row][col]).add(new Point(row,col));
					} else {
						Set<Point> points = new HashSet<Point>();
						points.add(new Point(row,col));
						boxes.put(initialState.boxes[row][col],points);
					}
				}
			}
		}
		this.goals = goals;

		HashMap<Character, Set<Integer[][]>> goalMaps = new HashMap<Character,Set<Integer[][]>>();
		for (Entry<Character,Set<Point>> entry : goals.entrySet()){
			char key = entry.getKey();
			for (Point goal : goals.get(key)){
				Integer[][] map = dijkstra(goal, initialState.walls);
				if (goalMaps.containsKey(key)){
					goalMaps.get(key).add(map);
				} else {
					Set<Integer[][]> maps = new HashSet<Integer[][]>();
					maps.add(map);
					goalMaps.put(key, maps);
				}
			}
		}
		this.goalMaps = goalMaps;
		
	}

	public int h(Node n){
		int h = 0;

		for (int row = 0; row < Node.MAX_ROW; row++) {
			for (int col = 0; col < Node.MAX_COL; col++) {
				if (n.boxes[row][col] > 0 && Character.toLowerCase(n.boxes[row][col]) != n.goals[row][col]){
					int distance = Integer.MAX_VALUE;
					for (Integer[][] map : goalMaps.get(Character.toLowerCase(n.boxes[row][col]))){
						if (map[row][col] < distance)
							distance = map[row][col];
					}
					h += distance;
				}
			}
		}

		return h;
	}

	/**
	 * Bruteforce pathfinding, currently returns a heuristic map of distances from 'initial' to all other nodes on the map.
	 * initial should generally be a goal or sub-goal for the agent.
	 * Performance heavy, use sparingly and reuse results where possible.
	**/
	public Integer[][] dijkstra(Point initial, boolean[][] walls){
		Integer[][] map = new Integer[walls.length][walls[0].length];
		for (int i = 0; i < map.length; i++)
			for (int j = 0; j < map[i].length; j++)
				map[i][j] = Integer.MAX_VALUE;

		map[initial.x][initial.y] = 0;

		traverse(initial, map, walls);

//		for (int i = 0; i < map.length; i++){
//			for (int j = 0; j < map[i].length; j++){
//				if (map[i][j] == Integer.MAX_VALUE)
//					System.err.print("* ");
//				else
//					System.err.print(map[i][j] + " ");
//			}
//			System.err.println();
//		}

		return map;
	}

	private void traverse(Point current, Integer[][] map, boolean[][] walls){
		if (!walls[current.x + 1][current.y] && map[current.x][current.y] + 1 < map[current.x + 1][current.y]){ //Go EAST
			map[current.x + 1][current.y] = map[current.x][current.y] + 1;
			traverse(new Point(current.x + 1, current.y), map, walls);
		}
		if (!walls[current.x - 1][current.y] && map[current.x][current.y] + 1 < map[current.x - 1][current.y]) { //Go WEST
			map[current.x - 1][current.y] = map[current.x][current.y] + 1;
			traverse(new Point(current.x - 1, current.y), map, walls);
		}
		if (!walls[current.x][current.y + 1] && map[current.x][current.y] + 1 < map[current.x][current.y + 1]) { //Go NORTH
			map[current.x][current.y + 1] = map[current.x][current.y] + 1;
			traverse(new Point(current.x, current.y + 1), map, walls);
		}
		if (!walls[current.x][current.y - 1] && map[current.x][current.y] + 1 < map[current.x][current.y - 1]) { //Go SOUTH
			map[current.x][current.y - 1] = map[current.x][current.y] + 1;
			traverse(new Point(current.x, current.y - 1), map, walls);
		}
	}

	public int h_old(Node n) {
		int h = 0;
		// Map all boxes to their corresponding characters
		HashMap<Character,Set<Point>> boxes = new HashMap<Character,Set<Point>>();
		for (int row = 0; row < Node.MAX_ROW; row++) {
			for (int col = 0; col < Node.MAX_COL; col++) {
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
		
		int agentDistance = Integer.MAX_VALUE;
		
		for (Entry<Character,Set<Point>> goalEntry : goals.entrySet()) {
			char boxChar = Character.toUpperCase(goalEntry.getKey());
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
