import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class CrossingLinesSorter {

	public static void main(String[] args) {
		CrossingLinesSorter run = new CrossingLinesSorter();
		Solution best = run.run();
		byte c = 0;
		for (int i=0; i<best.history.length; i++) {
			c = run.getCompleted(best.history[i],c);
			System.out.printf("%d %s\n",c,Arrays.toString(best.history[i]));
		}
		run.display(best);
	}

	//group of simultaneous events that occur across multiple lines.
	private static class Event {

		int numEvents;	//number of simultaneous events
		int[] indexes;	//event index of each line, -1 for no event
		int[] sizes;	//number of lines per event

		public Event(int numEvents, int[] indexes, int[] sizes) {
			this.numEvents = numEvents;
			this.indexes = indexes;
			this.sizes = sizes;
		}
	}

	private static class Solution implements Comparable<Solution> {

		byte[][] history;	//the line orderings for each event group in reverse order
		byte completed;		//the number of valid events given the history
		int crossed;		//the number of line crossings in the history

		public Solution(byte[][] history, byte completed, int crossed) {
			this.history = history;
			this.completed = completed;
			this.crossed = crossed;
		}

		//prioritise smallest crossings first, then completed events second
		public int compareTo(Solution other) {
			int crossDiff = (this.crossed - other.crossed);
			return (crossDiff == 0 ? other.completed - this.completed : crossDiff);
		}

		//compare only by ordering at earliest valid event
		public boolean equals(Object o) {
			if (o instanceof Solution) {
				Solution other = (Solution) o;
				return (Arrays.equals(this.history[this.history.length - 1],
					other.history[other.history.length - 1]) && this.completed == other.completed &&
					this.crossed == other.crossed);
			}
			return false;
		}

		public int hashCode() {
			// return (completed * 31 + crossed) * 31 + Arrays.deepHashCode(history);
			return (completed * 31 + crossed) * 31 + Arrays.hashCode(history[
				history.length - 1]); //bug fix
		}
	}

	private int totalLines;				//total number of lines
	private byte[] initialOrder;		//final order of lines
	private int totalEvents;			//number of event groups
	private ArrayList<Event> events;	//list of event groups

	public CrossingLinesSorter() {
		//parse input file, with events in reverse chronological order
		Scanner in = new Scanner(System.in);

		String[] line = in.nextLine().split(" ");
		totalLines = Integer.parseInt(line[0]);
		totalEvents = Integer.parseInt(line[1]);

		//parse final order
		initialOrder = new byte[totalLines];
		line = in.nextLine().split(" ");
		for (int i=0; i<totalLines; i++) {
			initialOrder[i] = Byte.parseByte(line[i]);
		}

		//parse event groups
		events = new ArrayList<>(totalEvents);
		for (int i=0; i<totalEvents; i++) {
			line = in.nextLine().split(" ");
			int numEvents = Integer.parseInt(line[0]);
			int[] indexes = new int[totalLines];
			int[] sizes = new int[numEvents];
			for (int j=0; j<totalLines; j++) {
				int index = Integer.parseInt(line[j + 1]);
				indexes[j] = index;
				if (index >= 0) {
					sizes[index]++;
				}
			}
			events.add(new Event(numEvents,indexes,sizes));
		}
	}

	/*performs terrible graph search to determine line ordering at each event group with minimal 
	number of line crossings, by processing event groups in reverse order*/
	public Solution run() {
		//initialise search structures
		PriorityQueue<Solution> q = new PriorityQueue<>();
		HashSet<Solution> seen = new HashSet<>();

		//get initial solution - ordering equal to provided final order of lines
		byte[][] initialHistory = new byte[1][];
		initialHistory[0] = Arrays.copyOf(initialOrder,totalLines);
		byte initialCompleted = getCompleted(initialOrder,(byte) 0);
		Solution first = new Solution(initialHistory,initialCompleted,0);
		q.add(first);
		seen.add(first);

		//initialise debugging variables
		int maxCompleted = 0;
		int gen = 0;

		//initialise memory tracking variables
		int minCompleted = 0;
		int store = 1;

		//run graph search
		while (!q.isEmpty()) {
			Solution out = q.poll(); //best solution

			store -= out.history.length; //update memory tracking

			//for debugging purposes
			if (out.completed > maxCompleted) {
				maxCompleted = out.completed;
				System.err.printf("DEBUG: %d groups have been completed with %d crossings in %d" + 
					" generations\n",maxCompleted,out.crossed,gen);
			}

			if (out.completed == totalEvents) { //if solution provides valid ordering for all groups
				return out;
			}

			//generate children solutions from every possible crossing
			ArrayList<Solution> nextSolutions = getNext(out);
			for (Solution next : nextSolutions) {
				if (seen.add(next)) {
					q.add(next);
					store += next.history.length;
				}
			}

			//if queue is too large, remove 'poor' solutions - necessary for memory limits
			while (store >= 30e6) {
				Iterator<Solution> it = q.iterator();
				int rem = 0;
				while (it.hasNext()) {
					Solution next = it.next();
					if (next.completed <= minCompleted) {
						it.remove();
						rem++;
						store -= next.history.length;
					}
				}
				System.err.printf("DEBUG: all solutions with fewer than %d groups completed have" + 
					" been removed (%d found) at generation %d\n",minCompleted,rem,gen);
				minCompleted++;
			}
			gen++;
		}
		throw new IllegalStateException();
	}

	/*given a current ordering of lines, walk backwards from the current event group and determine 
	the number of event groups that are valid without changing the ordering - 
	a group is valid if the lines in each event form an adjacent uninterrupted block*/
	private byte getCompleted(byte[] current, byte completed) {
		byte nextCompleted = completed;
		while (nextCompleted < totalEvents) {
			Event toMatch = events.get(nextCompleted); //the next event to check
			int look = -1; //the current event index
			int count = 0; //the number of observed lines for the current event

			for (int i=0; i<totalLines; i++) {
				int inEvent = toMatch.indexes[current[i]]; /*get the event index of the ith line in 
				the ordering*/
				if (inEvent == look) { //if in the same event as the previous line
					count++;
				}
				else {
					if (look >= 0 && count < toMatch.sizes[look]) { /*if the event has changed but 
						not all lines in the event were observed*/
						return nextCompleted; /*the next event is invalid given this ordering - 
						return the number that were*/
					}

					//set the current index and size
					look = inEvent;
					count = 1;
				}
			}
			nextCompleted++; //the next group is valid
		}
		return nextCompleted;
	}

	/*generate children solutions by crossing lines at the earliest valid grouping to satisfy 
	events in the previous grouping*/
	private ArrayList<Solution> getNext(Solution prev) {
		ArrayList<Solution> out = new ArrayList<>(); //list of children
		Event toMatch = events.get(prev.completed); //the next event to be satisfied
		byte[] current = prev.history[prev.history.length - 1]; /*the earliest ordering in the 
		history*/

		int look = -1; //the current event index
		int count = 0; //the number of observed lines for the current event

		//repair the parent to satisfy the next group
		for (int i=0; i<totalLines; i++) {
			int inEvent = toMatch.indexes[current[i]]; /*get the event index of the ith line in 
			the ordering*/
			if (inEvent == look) { //if in the same event as the previous line
				count++;
			}
			else {
				if (look >= 0 && count < toMatch.sizes[look]) { /*if the event has changed but 
					not all lines in the event were observed*/

					//get the range covered by lines in the event, including other lines in between
					int start = i - count;
					int end = i + 1;
					while (count < toMatch.sizes[look]) {
						if (toMatch.indexes[current[end]] == look) {
							count++;
						}
						end++;
					}
					end--;

					//describe the range by correctness and incorrectness
					int numWrong = (end - start + 1) - toMatch.sizes[look]; /*the number of lines 
					interrupting the event block*/
					int[] wrongLocs = new int[numWrong]; //indexes of lines in incorrect locations
					int size = toMatch.sizes[look];
					int[] rightLocs = new int[size]; //indexes of lines in event
					numWrong = 0;
					size = 0;
					for (int j=start; j<=end; j++) {
						if (toMatch.indexes[current[j]] == look) { //found line in event
							rightLocs[size] = j;
							size++;
						}
						else { //found line interrupting event block
							wrongLocs[numWrong] = j;
							numWrong++;
						}
					}

					//evaluate reordering of lines interrupting the event block
					int[] diff = new int[numWrong];
					for (int dir=0; dir<=(1 << numWrong) - 1; dir++) {
						//copy history of parent
						byte[][] nextHistory = new byte[prev.history.length + 1][];
						for (int j=0; j<prev.history.length; j++) {
							nextHistory[j] = Arrays.copyOf(prev.history[j],totalLines);
						}

						//copy current ordering outside the range of the event
						byte[] nextCurrent = new byte[totalLines];
						for (int j=0; j<start; j++) {
							nextCurrent[j] = current[j];
						}
						for (int j=end + 1; j<totalLines; j++) {
							nextCurrent[j] = current[j];
						}

						//move each line up or down based on bitset defined by dir
						int top = start;
						int bottom = end;
						int newCrosses1 = 0;
						int newCrosses2 = 0;
						int away = 0;
						//move incorrect lines up
						for (int j=0; j<numWrong; j++) {
							if ((dir & (1 << (numWrong - 1 - j))) == 0) {
								diff[j] = away;
								newCrosses1 += away;
								newCrosses2 += wrongLocs[j] - start - j;
								nextCurrent[top] = current[wrongLocs[j]];
								top++;
							}
							else {
								away++;
							}
						}
						away = 0;
						//move incorrect lines down
						for (int j=numWrong - 1; j>=0; j--) {
							if ((dir & (1 << (numWrong - 1 - j))) != 0) {
								diff[j] = away;
								newCrosses1 += away;
								newCrosses2 += end - wrongLocs[j] - (numWrong - 1 - j);
								nextCurrent[bottom] = current[wrongLocs[j]];
								bottom--;
							}
							else {
								away++;
							}
						}
						//place correct lines in single block between reordered incorrect lines
						for (int j=start; j<=end; j++) {
							if (toMatch.indexes[current[j]] == look) {
								nextCurrent[top] = current[j];
								top++;
							}
						}

						//add child
						nextHistory[prev.history.length] = nextCurrent;
						out.add(new Solution(nextHistory,getCompleted(nextCurrent,prev.completed),
							prev.crossed + newCrosses1 / 2 + newCrosses2));
					}
					break;
				}

				//set the current index and size
				look = inEvent;
				count = 1;
			}
		}
		return out;
	}

	private static class LinesComponent extends JComponent {

		private static class Line {
			final int x1; 
			final int y1;
			final int x2;
			final int y2;   
			final Color color;

			public Line(int x1, int y1, int x2, int y2, Color color) {
				this.x1 = x1;
				this.y1 = y1;
				this.x2 = x2;
				this.y2 = y2;
				this.color = color;
			}
		}

		private final LinkedList<Line> lines = new LinkedList<Line>();

		public void addLine(int x1, int x2, int x3, int x4) {
			addLine(x1, x2, x3, x4, Color.black);
		}

		public void addLine(int x1, int x2, int x3, int x4, Color color) {
			lines.add(new Line(x1,x2,x3,x4, color));        
			repaint();
		}

		public void clearLines() {
			lines.clear();
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			for (Line line : lines) {
				g.setColor(line.color);
				g.drawLine(line.x1, line.y1, line.x2, line.y2);
			}
		}
	}

	private static final Color[] defaultColours = new Color[]{new Color(200,55,55),
		new Color(200,170,55),new Color(255,180,0),new Color(255,255,0),new Color(180,255,0),
		new Color(150,200,55),new Color(0,255,0),new Color(55,200,170),new Color(0,255,200),
		new Color(0,230,255),new Color(0,170,255),new Color(0,0,255),new Color(255,0,255),
		new Color(255,0,150),new Color(180,75,100)};
	private static final int GAP = 10;

	//demo display of a given line ordering
	private void display(Solution best) {
		JFrame testFrame = new JFrame();
		testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		LinesComponent comp = new LinesComponent();
		comp.setPreferredSize(new Dimension(GAP * (totalEvents + best.history.length + 2),
			GAP * (totalLines + 1)));
		testFrame.getContentPane().add(comp, BorderLayout.CENTER);

		Color[] colours = Arrays.copyOf(defaultColours,totalLines);
		for (int i=defaultColours.length; i<totalLines; i++) {
			colours[i] = new Color((float)Math.random(),(float)Math.random(),(float)Math.random());
		}

		byte c = 0;
		for (int i=0; i<best.history.length; i++) {
			byte nextC = getCompleted(best.history[i],c);
			for (int j=0; j<totalLines; j++) {
				comp.addLine(GAP * (totalEvents + best.history.length + 2) - (nextC + i + 1) * GAP,
					(j + 1) * GAP,GAP * (totalEvents + best.history.length + 2) - (c + i + 1) * GAP,
					(j + 1) * GAP,colours[best.history[i][j]]);
			}
			for (int j=c; j<nextC; j++) {
				Event nextEvent = events.get(j);
				boolean[] seenEvent = new boolean[nextEvent.numEvents];
				for (int k=0; k<totalLines; k++) {
					int look = nextEvent.indexes[best.history[i][k]];
					if (look >= 0 && !seenEvent[look]) {
						seenEvent[look] = true;
						comp.addLine(GAP * (totalEvents + best.history.length + 2) - 
							(j + i + 1) * GAP - GAP/2,(k + 1) * GAP - 2,
							GAP * (totalEvents + best.history.length + 2) - 
							(j + i + 1) * GAP - GAP/2,(k + nextEvent.sizes[look]) * GAP + 4,
							Color.BLACK);
					}
				}
			}
			if (i < best.history.length - 1) {
				for (int j=0; j<totalLines; j++) {
					for (int k=0; k<totalLines; k++) {
						if (best.history[i][j] == best.history[i + 1][k]) {
							comp.addLine(GAP * (totalEvents + best.history.length + 2) - 
								(nextC + i + 2) * GAP,(k + 1) * GAP,GAP * 
								(totalEvents + best.history.length + 2) - (nextC + i + 1) * GAP,
								(j + 1) * GAP,colours[best.history[i][j]]);
							break;
						}
					}
				}
			}
			c = nextC;
		}

		testFrame.pack();
		testFrame.setVisible(true);
	}

}