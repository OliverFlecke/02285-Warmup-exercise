package searchclient;

import java.awt.Point;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import searchclient.NotImplementedException;

public abstract class Heuristic implements Comparator<Node> {
	private final HashMap<Character,HashSet<Point>> goalMap;
	private int h, agentDistance;
	private Point agent;
	
	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		this.goalMap = extractMap(initialState.goals);
	}
	
	private HashSet<Point> findClosest(Point p1, HashSet<Point> points)
	{
		HashSet<Point> closest = new HashSet<Point>();
		
		int shortestDistance = Integer.MAX_VALUE;
		
		for (Point p2 : points)
		{
			int distance = manhattanDistance(p1, p2);
			
			if (distance < shortestDistance) 
			{
				shortestDistance = distance;
				
				closest.clear();
				closest.add(p2);
			} 
			else if (distance == shortestDistance)
			{
				closest.add(p2);
			}
		}
		
		return closest;
	}
	
	private Point matchClosest(Point goal, HashSet<Point> goals, HashSet<Point> closestBoxes)
	{		
		for (Point box : closestBoxes)
		{
			HashSet<Point> closestGoals = findClosest(box, goals);
			
			if (closestGoals.contains(goal))
			{				
				return box;
			}
		}
		return null;
	}
	
	private void matchGoals(HashSet<Point> goals, HashSet<Point> boxes)
	{		
		HashSet<Point> unsolved = new HashSet<Point>();
		
		for (Point goal : goals)
		{
			HashSet<Point> closestBoxes = findClosest(goal, boxes);
			
			Point box = matchClosest(goal, goals, closestBoxes);
			
			if (box != null)
			{
				int distance = manhattanDistance(goal, box);
				h += distance;
				
				//if (distance > 0)
				//{
				//	distance = manhattanDistance(agent, box);
				//	if (distance < agentDistance)
				//		agentDistance = distance;
				//}				
				boxes.remove(box);
			}
			else 
			{				
				unsolved.add(goal);	
			}
		}
		// Compute intersection
		goals.retainAll(unsolved);
	}

	public int h(Node n) {
		h = 0;
		agentDistance = Integer.MAX_VALUE;
		agent = new Point(n.agentRow, n.agentCol);
		
		HashMap<Character,HashSet<Point>> boxMap = extractMap(n.boxes);
		
		for (Entry<Character,HashSet<Point>> goalEntry : goalMap.entrySet())
		{
			char boxChr = Character.toUpperCase(goalEntry.getKey());
			
			HashSet<Point> boxes = boxMap.get(boxChr);
			HashSet<Point> goals = goalEntry.getValue();
			
			int matchCount = 0;
			while (!goals.isEmpty())
			{
				matchGoals(goals, boxes);
				if (++matchCount == 100)
				{
					System.err.println("Stuck matching");
					break;
				}
			}			
		}
		//return h;
		if (h == 0) return 0;
		else return h + agentDistance;
	}
	
	private int manhattanDistance(Point p1, Point p2)
	{
		return Math.abs(p1.x-p2.x) + Math.abs(p1.y-p2.y);
	}
	
	private HashMap<Character,HashSet<Point>> extractMap(char[][] array) 
	{
		HashMap<Character,HashSet<Point>> map = new HashMap<Character,HashSet<Point>>();
		
		for (int row = 0; row < array.length; row++) 
		{
			for (int col = 0; col < array[0].length; col++)
			{
				char chr = array[row][col];
				
				if (chr == 0) continue;				
				
				if (map.containsKey(chr))
				{
					map.get(chr).add(new Point(row, col));
				}
				else
				{
					map.put(chr, new HashSet<Point>(Arrays.asList(new Point(row, col))));
				}
			}
		}
		
		return map;
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
