import java.util.*;
import java.io.*;

public class PosterCodeFormatter {

	private static class Event {

		int numEvents;
		int[] indexes;
		int[] sizes;
		boolean shift;

		public Event(int numEvents, int[] indexes, int[] sizes) {
			this.numEvents = numEvents;
			this.indexes = indexes;
			this.sizes = sizes;
		}

		public void setShift(boolean shift) {
			this.shift = shift;
		}

	}

	private static class Positioning implements Comparable<Positioning> {

		int[] positions;
		int numChanges;
		int middleness;
		int roof;

		public Positioning(int[] positions, int numChanges, int middleness, int roof) {
			this.positions = positions;
			this.numChanges = numChanges;
			this.middleness = middleness;
			this.roof = roof;
		}

		public int compareTo(Positioning other) {
			int cd = Integer.compare(this.numChanges,other.numChanges);
			if (cd == 0) {
				int md = Integer.compare(this.middleness,other.middleness);
				return (md == 0 ? -Integer.compare(this.roof,other.roof) : md);
			}
			else {
				return cd;
			}
		}
	}

	private static class FreeEvent {

		int numWL;
		int[] wLines;
		int numEvents;
		int[] firstIndexes;
		int[] sizes;
		boolean shift;
		String[] text;

		public FreeEvent(int numWL, int[] wLines, int numEvents, int[] firstIndexes, int[] sizes, 
			boolean shift, String[] text) {
			this.numWL = numWL;
			this.wLines = wLines;
			this.numEvents = numEvents;
			this.firstIndexes = firstIndexes;
			this.sizes = sizes;
			this.shift = shift;
			this.text = text;
		}
	}

	private static class HorizontalLine {

		int x;
		int y;
		int length;
		String name;
		int lineWidth;

		public HorizontalLine(int x, int y, int length, String name, int lineWidth) {
			this.x = x;
			this.y = y;
			this.length = length;
			this.name = name;
			this.lineWidth = lineWidth;
		}

		public String toString() {
			return String.format("Call createHorizontalLine(%s, %s, %s, %s, \"%s\")",toDecimal(x),
				toDecimal(y),toDecimal(length),toDecimal(lineWidth),name);
		}
	}

	private static class CurvedLine {

		int x;
		int y;
		int xgap;
		int ygap;
		String name;
		int lineWidth;

		public CurvedLine(int x, int y, int xgap, int ygap, String name, int lineWidth) {
			this.x = x;
			this.y = y;
			this.xgap = xgap;
			this.ygap = ygap;
			this.name = name;
			this.lineWidth = lineWidth;
		}

		public String toString() {
			return String.format("Call createCurvedConnector(%s, %s, %s, %s, %s, \"%s\")",
				toDecimal(x),toDecimal(y),toDecimal(xgap),toDecimal(ygap),toDecimal(lineWidth),
				name);
		}
	}

	private static class RoundedRectangle {

		int x;
		int y;
		int height;

		public RoundedRectangle(int x, int y, int height) {
			this.x = x;
			this.y = y;
			this.height = height;
		}

		public String toString() {
			return String.format("Call createRoundedRectangle(%s, %s, %s, %s, %s)",toDecimal(x),
				toDecimal(y),toDecimal(EVENT_WIDTH - EVENT_LINE_WIDTH),toDecimal(height),
				toDecimal(EVENT_LINE_WIDTH));
		}
	}

	private static class VerticalArrow {

		int x;
		int y;
		int length;

		public VerticalArrow(int x, int y, int length) {
			this.x = x;
			this.y = y;
			this.length = length;
		}

		public String toString() {
			return String.format("Call createVerticalArrow(%s, %s, %s, %s)",toDecimal(x),
				toDecimal(y),toDecimal(length),toDecimal(ARROW_WIDTH));
		}
	}

	private static class ReturnZ {

		int x;
		int y;
		int xgap;
		int ygap;
		boolean hasArrow;

		public ReturnZ(int x, int y, int xgap, int ygap, boolean hasArrow) {
			this.x = x;
			this.y = y;
			this.xgap = xgap;
			this.ygap = ygap;
			this.hasArrow = hasArrow;
		}

		public String toString() {
			return String.format("Call createReturnZ(%s, %s, %s, %s, %s, %s, %s)",toDecimal(x),
				toDecimal(y),toDecimal(xgap),toDecimal(ygap),toDecimal(RETURN_RADIUS),
				toDecimal(RETURN_Z_WIDTH),(hasArrow ? "True" : "False"));
		}
	}

	private static class TextBox {

		double x;
		double y;
		String message;
		double width;
		double height;
		int rotation;

		public TextBox(double x, double y, String message, int rotation) {
			this(x,y,message,rotation,Double.MAX_VALUE);
		}

		public TextBox(double x, double y, String message, int rotation, double maxWidth) {
			this.x = x;
			this.y = y;
			this.message = message;
			double[] wh = getTextWidth(message,maxWidth);
			width = wh[0];
			height = wh[1];
			this.rotation = rotation;
		}

		public String toString() {
			return String.format("Call createText(%f, %f, %f, \"%s\", %d, %d)",x,y,width,message,
				FONT_SIZE,rotation);
		}

	}

	private static class Tag {

		double[] values;

		public Tag(double... v) {
			this.values = v;
		}

		public String toString() {
			switch (values.length) {
				case 4: {
					return String.format("Call createTag1(%f, %f, %f, %f, %s)",values[0],values[1],
						values[2],values[3],toDecimal(TAG_LINE_WIDTH));
				}
				case 6: {
					return String.format("Call createTag2(%f, %f, %f, %f, %f, %f, %s)",values[0],
						values[1],values[2],values[3],values[4],values[5],
						toDecimal(TAG_LINE_WIDTH));
				}
				case 8: {
					return String.format("Call createTag3(%f, %f, %f, %f, %f, %f, %f, %f, %s)",
						values[0],values[1],values[2],values[3],values[4],values[5],values[6],
						values[7],toDecimal(TAG_LINE_WIDTH));
				}
				case 10: {
					return String.format("Call createTag4(%f, %f, %f, %f, %f, %f, %f, %f, %f," + 
						" %f, %s)",values[0],values[1],values[2],values[3],values[4],values[5],
						values[6],values[7],values[8],values[9],toDecimal(TAG_LINE_WIDTH));
				}
				default: {
					throw new IllegalStateException(String.format("%d",values.length));
				}
			}
		}
	}

	private static class ShapeID implements Comparable<ShapeID> {

		private static final int RZ_TYPE = -1;
		private static final int C_LINE_TYPE = 0;
		private static final int H_LINE_TYPE = 1;
		private static final int TAG_TYPE = 2;
		private static final int RR_TYPE = 3;
		private static final int VA_TYPE = 4;
		private static final int TB_TYPE = 5;

		String string;
		int type;
		int height;

		public ShapeID(String string, int type, int height) {
			this.string = string;
			this.type = type;
			this.height = height;
		}

		public int compareTo(ShapeID other) {
			int td = Integer.compare(this.type,other.type);
			return (td == 0 ? Integer.compare(this.height,other.height) : td);
		}
	}

	private static class LeapInfo implements Comparable<LeapInfo> {

		int time;
		String[] info;
		boolean endPoint;
		boolean leapPoint;
		String text;

		public LeapInfo(int time, String[] info, boolean endPoint, boolean leapPoint, String text) {
			this.time = time;
			this.info = info;
			this.endPoint = endPoint;
			this.leapPoint = leapPoint;
			this.text = text;
		}

		public int compareTo(LeapInfo other) {
			return this.time - other.time;
		}
	}

	private static class SubLine {

		HorizontalLine line;
		int wl;
		String name;
		boolean active;

		public SubLine(HorizontalLine line, int wl, String name) {
			this.line = line;
			this.wl = wl;
			this.name = name;
			active = true;
		}

	}

	private static class SubCurvedLine {

		CurvedLine line;
		int wl;
		String nameStart;
		String nameEnd;

		public SubCurvedLine(CurvedLine line, int wl, String nameStart, String nameEnd) {
			this.line = line;
			this.wl = wl;
			this.nameStart = nameStart;
			this.nameEnd = nameEnd;
		}

	}

	private static class SubRoundedRectangle {

		RoundedRectangle rect;
		int wlStart;
		String nameStart;
		int wlEnd;
		String nameEnd;

		public SubRoundedRectangle(RoundedRectangle rect, int wlStart, String nameStart, int wlEnd, 
			String nameEnd) {
			this.rect = rect;
			this.wlStart = wlStart;
			this.nameStart = nameStart;
			this.wlEnd = wlEnd;
			this.nameEnd = nameEnd;
		}
	}

	private static class SubVerticalArrow {

		VerticalArrow arrow;
		int wlStart;
		String nameStart;
		int wlEnd;
		String nameEnd;

		public SubVerticalArrow(VerticalArrow arrow, int wlStart, String nameStart, int wlEnd,
			String nameEnd) {
			this.arrow = arrow;
			this.wlStart = wlStart;
			this.nameStart = nameStart;
			this.wlEnd = wlEnd;
			this.nameEnd = nameEnd;
		}
	}

	private static class SubReturnZ {

		ReturnZ returnZ;
		int wl;
		String nameStart;
		String nameEnd;

		public SubReturnZ(ReturnZ returnZ, int wl, String nameStart, String nameEnd) {
			this.returnZ = returnZ;
			this.wl = wl;
			this.nameStart = nameStart;
			this.nameEnd = nameEnd;
		}
	}

	private static class SubTextBox  {

		TextBox tb;
		int wl;
		String name;
		int x;

		public SubTextBox(TextBox tb, int wl, String name, int x) {
			this.tb = tb;
			this.wl = wl;
			this.name = name;
			this.x = x;
		}
	}

	private static class IntoS implements Comparable<IntoS> {

		HorizontalLine line;
		CurvedLine curve;
		int wl;

		public IntoS(HorizontalLine line, CurvedLine curve, int wl) {
			this.line = line;
			this.curve = curve;
			this.wl = wl;
		}

		public int compareTo(IntoS other) {
			return this.curve.y - other.curve.y;
		}
	}

	private static final String EVENTS_FILE = "../input/part1Events.in";
	private static final String ORDER_FILE = "../input/part1LineOrder.in";
	private static final String NAME_FILE = "../input/lineNames.in";
	private static final String LEAP_FILE = "../input/timeLeaps.in";
	private static final String[] FREE_FILES = new String[]{"../input/kaikanFreeEvents.in",
		"../input/endFreeEvents.in"};

	private static final int GROUP_GAP_PART_ONE = 5;
	private static final int GROUP_GAP_PART_TWO = 11;
	private static final int GROUP_GAP_PART_THREE = 5;
	private static final int LINE_WIDTH_PART_ONE = 10;
	private static final int LINE_WIDTH_PART_TWO = 6;
	private static final int LINE_WIDTH_PART_THREE = 10;
	private static final int EVENT_WIDTH = 22;
	private static final int EVENT_GAP_PART_ONE = GROUP_GAP_PART_ONE * LINE_WIDTH_PART_ONE;
	private static final int EVENT_GAP_PART_TWO = 8 * LINE_WIDTH_PART_TWO;
	private static final int EVENT_GAP_PART_THREE = GROUP_GAP_PART_THREE * LINE_WIDTH_PART_THREE;
	private static final int EVENT_LINE_WIDTH = 6;
	private static final int MIN_EVENT_HEIGHT = 12;
	private static final int START_X = 55;
	private static final int START_Y = 55 + LINE_WIDTH_PART_ONE + MIN_EVENT_HEIGHT;
	private static final int MARGIN_SIZE = 5;
	private static final int ARROW_WIDTH = EVENT_WIDTH - 2 * EVENT_LINE_WIDTH;
	private static final int PART_TWO_GAP = 100;
	private static final int RETURN_Z_WIDTH = 4;
	private static final int RETURN_RADIUS = (((GROUP_GAP_PART_TWO + 1) * LINE_WIDTH_PART_TWO * 2) /
		3);
	private static final double MINIMUM_TEXTBOX_WIDTH = 5.6;
	private static final double TEXTBOX_SAFETY_WIDTH = 0.1;
	private static final int FONT_SIZE = 8;
	private static final double MINIMUM_TEXTBOX_HEIGHT = 2.6;
	private static final double TEXT_HEIGHT = 0.428 * FONT_SIZE;
	private static final double TEXTBOX_HEIGHT = MINIMUM_TEXTBOX_HEIGHT + TEXT_HEIGHT;
	private static final int TEXTBOX_ROTATION = 45;
	private static final double TEXTBOX_INTRA_SPACE_GAP_MULTIPLE = 0.25;
	private static final double TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE = 0;
	private static final double TEXTBOX_SMALL_INTRA_SPACE_MULTIPLE = (1 + 
		TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE);
	private static final double PART_ONE_TEXT_WHITESPACE = 30;
	private static final double PART_TWO_A_TEXT_WHITESPACE = 10;
	private static final double PART_TWO_B_TEXT_WHITESPACE = 7;
	private static final double PART_THREE_TEXT_WHITESPACE = 26;
	private static final int TAG_LINE_WIDTH = 4;
	private static final double PART_ONE_MAX_TEXTBOX_WIDTH_ABOVE = 47;
	private static final double PART_ONE_MAX_TEXTBOX_WIDTH_BELOW = 57;
	private static final double PART_TWO_A_MAX_TEXTBOX_WIDTH = 32;
	private static final double PART_TWO_B_MAX_TEXTBOX_WIDTH_ABOVE = 62;
	private static final double PART_TWO_B_MAX_TEXTBOX_WIDTH_BELOW = 46;
	private static final double PART_THREE_MAX_TEXTBOX_WIDTH = 51;
	private static final double TAG_GAP_PART_ONE = ((GROUP_GAP_PART_ONE * LINE_WIDTH_PART_ONE) /
		10.0);
	private static final double TAG_GAP_PART_THREE = ((GROUP_GAP_PART_THREE * 
		LINE_WIDTH_PART_THREE) / 10.0);
	private static final int CURVE_ADJUSTMENT = 5;
	private static final int SHIFT_GAP_PART_ONE = 7;
	private static final int SHIFT_GAP_PART_THREE = 7;

	private int totalLines;

	private int[] endOrder;
	private int totalEvents;
	private ArrayList<Event> events;
	private ArrayList<String[]> eventInfo;

	private int[][] lineOrder;

	private int[] cutoff;

	private ArrayList<Event> groups;
	private int maxNumGroups;

	private int[][] linePositions;

	private ArrayList<String> lineNames;

	private int numLeapInfo;
	private ArrayList<LeapInfo> leaps;

	private ArrayList<ArrayList<FreeEvent>> frees;

	public static void main(String[] args) {
		PosterCodeFormatter main = new PosterCodeFormatter();
		main.run();
	}

	public void run() {
		initialiseEvents();
		initialiseOrder();
		initialiseCutoffs();
		initialiseGroups();
		setLinePositions();
		initialiseLineNames();
		initialiseTimeLeaps();
		initialiseFreeEvents();
		formatCode();
	}

	private void initialiseEvents() {
		try {
			Scanner in = new Scanner(new File(EVENTS_FILE),"utf-8");
			String next = in.nextLine();
			String[] line = next.split(" ");
			totalLines = Integer.parseInt(line[0]);
			totalEvents = Integer.parseInt(line[1]);
			endOrder = new int[totalLines];
			line = in.nextLine().split(" ");
			for (int i=0; i<totalLines; i++) {
				endOrder[i] = Byte.parseByte(line[i]);
			}
			events = new ArrayList<>(totalEvents);
			eventInfo = new ArrayList<>(totalEvents);
			for (int i=0; i<totalEvents; i++) {
				next = in.nextLine();
				next = next.replace("\"","\"\"");
				line = next.split(" ");
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
				Event add = new Event(numEvents,indexes,sizes);
				add.setShift(next.contains("shift"));
				events.add(add);
				String[] info = next.split("#")[1].split("/");
				eventInfo.add(info);
			}
			Collections.reverse(eventInfo);
			Collections.reverse(events);
		}
		catch (FileNotFoundException fnfe) {
			throw new RuntimeException();
		}
	}

	private void initialiseOrder() {
		try {
			Scanner in = new Scanner(new File(ORDER_FILE));
			lineOrder = new int[totalEvents][totalLines];
			int prevPos = 0;
			while (in.hasNext()) {
				String[] line = in.nextLine().split(" ");
				int pos = Integer.parseInt(line[0]);
				int[] order = new int[totalLines];
				for (int j=0; j<totalLines; j++) {
					if (j == 0) {
						order[j] = Integer.parseInt(line[j + 1].substring(1,
							line[j + 1].length() - 1));
					}
					else {
						order[j] = Integer.parseInt(line[j + 1].substring(0,
							line[j + 1].length() - 1));
					}
				}
				while (prevPos < pos) {
					for (int j=0; j<totalLines; j++) {
						lineOrder[totalEvents - 1 - prevPos][j] = order[j];
					}
					prevPos++;
				}
			}
		}
		catch (FileNotFoundException fnfe) {
			throw new RuntimeException();
		}
	}

	private void initialiseCutoffs() {
		cutoff = new int[totalLines];
		for (int i=0; i<totalLines; i++) {
			for (int j=totalEvents - 1; j>=0; j--) {
				if (events.get(j).indexes[i] >= 0) {
					cutoff[i] = j;
					break;
				}
			}
		}
	}

	private void initialiseGroups() {
		groups = new ArrayList<>();
		maxNumGroups = 0;
		for (int i=0; i<totalEvents; i++) {
			Event e = events.get(i);
			int numGroups = 0;
			ArrayList<Integer> sizeList = new ArrayList<>();
			int[] indexes = new int[totalLines];
			int lastEventIndex = -2;
			int currentSize = 0;
			for (int j=0; j<totalLines; j++) {
				int l = lineOrder[i][j];
				if (i > cutoff[l]) {
					indexes[l] = -1;
				}
				else {
					int eIndex = e.indexes[l];
					if (eIndex != lastEventIndex) {
						if (numGroups > 0) {
							sizeList.add(currentSize);
						}
						numGroups++;
						currentSize = 0;
					}
					indexes[l] = numGroups - 1;
					currentSize++;
					lastEventIndex = eIndex;
				}
			}
			sizeList.add(currentSize);
			int[] sizes = new int[numGroups];
			for (int j=0; j<numGroups; j++) {
				sizes[j] = sizeList.get(j);
			}
			maxNumGroups = Math.max(maxNumGroups,numGroups);
			Event group = new Event(numGroups,indexes,sizes);
			group.setShift(e.shift);
			groups.add(group);
		}
	}

	private void setLinePositions() {
		int totalPositions = totalLines + GROUP_GAP_PART_ONE * (maxNumGroups - 1);
		linePositions = new int[totalEvents + 1][totalLines];
		for (int i=0; i<totalLines; i++) {
			int l = lineOrder[0][i];
			linePositions[0][l] = middleRoof(totalPositions,totalLines) + i;
		}
		for (int i=0; i<totalEvents; i++) {
			int[] base = new int[totalLines];
			int currentPos = 0;
			Event group = groups.get(i);
			int prevInd = 0;
			for (int j=0; j<totalLines; j++) {
				int l = lineOrder[i][j];
				int ind = group.indexes[l];
				if (ind >= 0) {
					if (ind == prevInd) {
						base[l] = currentPos;
						currentPos++;
					}
					else {
						if (prevInd == 0 && group.shift) {
							int s0 = lineOrder[i][j - 2];
							int s1 = lineOrder[i][j - 1];
							if (group.sizes[0] == 2) {
								currentPos += SHIFT_GAP_PART_ONE;
								base[s1] = currentPos - 1;
							}
							else {
								base[s0] += 2;
								currentPos += 2 + SHIFT_GAP_PART_ONE;
								base[s1] = currentPos - 1;
							}
						}
						currentPos += GROUP_GAP_PART_ONE + 1;
						base[l] = currentPos - 1;
					}
					prevInd = ind;
				}
			}
			ArrayList<Positioning> possibles = new ArrayList<>();
			for (int j=0; j<totalPositions - currentPos + 1; j++) {
				int[] positions = new int[totalLines];
				int numChanges = 0;
				int middleness = (int) Math.abs(j - middleRoof(totalPositions,currentPos));
				for (int k=0; k<totalLines; k++) {
					if (i <= cutoff[k]) {
						positions[k] = base[k] + j;
						if (positions[k] != linePositions[i][k]) {
							numChanges++;
						}
					}
				}
				possibles.add(new Positioning(positions,numChanges,middleness,j));
			}
			Collections.sort(possibles);
			int[] best = possibles.get(0).positions;
			for (int j=0; j<totalLines; j++) {
				linePositions[i + 1][j] = best[j];
			}
		}
	}

	private int middleRoof(int totalHeight, int height) {
		return (totalHeight - height) / 2;
	}

	private void initialiseLineNames() {
		try {
			Scanner in = new Scanner(new File(NAME_FILE));
			lineNames = new ArrayList<>();
			while (in.hasNext()) {
				lineNames.add(in.nextLine());
			}
		}
		catch (FileNotFoundException fnfe) {
			throw new RuntimeException();
		}
	}

	private void initialiseTimeLeaps() {
		try {
			Scanner in = new Scanner(new File(LEAP_FILE));
			leaps = new ArrayList<>();
			numLeapInfo = Integer.parseInt(in.nextLine());
			for (int i=0; i<numLeapInfo; i++) {
				String l = in.nextLine();
				String[] line = l.split(" ");
				leaps.add(new LeapInfo(Integer.parseInt(line[0]),line,
					l.contains("time leaps") || l.contains("END"),l.contains("time leaps"),
					(l.contains("#") ? l.split("#")[1] : "")));
			}
			Collections.sort(leaps);
		}
		catch (FileNotFoundException fnfe) {
			throw new RuntimeException();
		}
	}

	private void initialiseFreeEvents() {
		frees = new ArrayList<>();
		for (String filename : FREE_FILES) {
			try {
				Scanner in = new Scanner(new File(filename));
				ArrayList<FreeEvent> freeList = new ArrayList<>();
				int numFreeEvents = Integer.parseInt(in.nextLine());
				for (int i=0; i<numFreeEvents; i++) {
					String l = in.nextLine();
					String[] line = l.split(" ");
					int numWL = Integer.parseInt(line[0]);
					int[] wLines = new int[numWL];
					for (int j=0; j<numWL; j++) {
						wLines[j] = Integer.parseInt(line[1 + j]);
					}
					int numEvents = Integer.parseInt(line[1 + numWL]);
					int[] firstIndexes = new int[numEvents];
					int[] sizes = new int[numEvents];
					for (int j=0; j<numEvents; j++) {
						firstIndexes[j] = Integer.parseInt(line[2 + numWL + 2 * j]);
						sizes[j] = Integer.parseInt(line[3 + numWL + 2 * j]);
					}
					boolean shift = l.contains("shift");
					String[] text = l.split("#")[1].split("/");
					freeList.add(new FreeEvent(numWL,wLines,numEvents,firstIndexes,sizes,shift,
						text));
				}
				frees.add(freeList);
			}
			catch (FileNotFoundException fnfe) {
				throw new RuntimeException();
			}
		}
	}

	private void formatCode() {
		System.out.printf("Sub run()\n");
		System.out.printf("\tdeleteAll\n");
		System.out.printf("\tCall initialise(\"A0\", %d, False)\n",MARGIN_SIZE);
		int secondstarty = formatFirst();
		System.out.printf("\trunSecond\n");
		System.out.printf("End Sub\n\n");
		System.out.printf("Sub runSecond()\n");
		int thirdstarty = formatSecond(secondstarty);
		System.out.printf("\trunThird\n");
		System.out.printf("End Sub\n");
		System.out.printf("Sub runThird()\n");
		formatThird(thirdstarty);
		System.out.printf("End Sub\n");
	}

	private int formatFirst() {
		ArrayList<ShapeID> shapes = new ArrayList<>();
		ArrayList<HorizontalLine> hLinesToAdd = new ArrayList<>();
		ArrayList<CurvedLine> cLinesToAdd = new ArrayList<>();
		ArrayList<RoundedRectangle> rrToAdd = new ArrayList<>();
		ArrayList<VerticalArrow> vaToAdd = new ArrayList<>();
		ArrayList<TextBox> textAbove = new ArrayList<>();
		ArrayList<TextBox> textBelow = new ArrayList<>();
		ArrayList<RoundedRectangle> rrsAbove = new ArrayList<>();
		ArrayList<RoundedRectangle> rrsBelow = new ArrayList<>();
		HorizontalLine[] currentLines = new HorizontalLine[totalLines];
		for (int i=0; i<totalLines; i++) {
			currentLines[i] = new HorizontalLine(START_X,
				START_Y + linePositions[0][i] * LINE_WIDTH_PART_ONE,0,lineNames.get(i),
				LINE_WIDTH_PART_ONE);
		}
		double minTXA = START_X / 10.0;
		double minTXB = START_X / 10.0;
		double maxTHeightA = 0;
		double maxTHeightB = 0;
		int currentx = START_X;
		double sinA = Math.sin(TEXTBOX_ROTATION * Math.PI / 180.0);
		double cosA = Math.cos(TEXTBOX_ROTATION * Math.PI / 180.0);
		double tanA = Math.tan(TEXTBOX_ROTATION * Math.PI / 180.0);
		for (int i=0; i<totalEvents; i++) {
			boolean hasTextAbove = false;
			boolean hasTextBelow = false;
			Event e = events.get(i);
			if (e.shift) {
				hasTextAbove = true;
			}
			else {
				int firstActive = -1;
				for (int j=0; j<totalLines; j++) {
					int l = lineOrder[i][j];
					if (firstActive < 0 && i < cutoff[l]) {
						firstActive = j;
					}
					int eInd = e.indexes[l];
					if (eInd >= 0) {
						int size = e.sizes[eInd];
						String text = eventInfo.get(i)[eInd];
						if (text.length() > 1) {
							int topGap = j - firstActive;
							int bottomGap = totalLines - (j + size);
							if (topGap < bottomGap) {
								hasTextAbove = true;
							}
							else {
								hasTextBelow = true;
							}
						}
						j += size - 1;
					}
				}
			}
			currentx = currentx + EVENT_GAP_PART_ONE + EVENT_WIDTH;
			if (hasTextAbove) {
				currentx = Math.max(currentx,actualToUsable(minTXA) + EVENT_WIDTH);
			}
			if (hasTextBelow) {
				currentx = Math.max(currentx,actualToUsable(minTXB) + EVENT_WIDTH);
			}
			ArrayList<IntoS> upCurves = new ArrayList<>();
			ArrayList<IntoS> downCurves = new ArrayList<>();
			for (int j=0; j<totalLines; j++) {
				if (i <= cutoff[j]) {
					if (linePositions[i][j] != linePositions[i + 1][j]) {
						hLinesToAdd.add(currentLines[j]);
						CurvedLine curve = new CurvedLine(
							currentLines[j].x + currentLines[j].length,currentLines[j].y,
							EVENT_GAP_PART_ONE,
							LINE_WIDTH_PART_ONE * (linePositions[i + 1][j] - linePositions[i][j]),
							lineNames.get(j),LINE_WIDTH_PART_ONE);
						cLinesToAdd.add(curve);
						if (linePositions[i][j] < linePositions[i + 1][j]) {
							downCurves.add(new IntoS(currentLines[j],curve,j));
						}
						else {
							upCurves.add(new IntoS(currentLines[j],curve,j));
						}
						currentLines[j] = new HorizontalLine(curve.x + curve.xgap,
							START_Y + linePositions[i + 1][j] * LINE_WIDTH_PART_ONE,0,
							lineNames.get(j),LINE_WIDTH_PART_ONE);
					}
					currentLines[j].length = currentx - currentLines[j].x;
					if (i == cutoff[j]) {
						hLinesToAdd.add(currentLines[j]);
					}
				}
			}
			Collections.sort(upCurves);
			Collections.sort(downCurves);
			int shiftCount = 0;
			for (int j=1; j<upCurves.size(); j++) {
				IntoS current = upCurves.get(j);
				IntoS prev = upCurves.get(j - 1);
				if (linePositions[i][current.wl] - linePositions[i][prev.wl] <= 1 + shiftCount || 
					linePositions[i + 1][current.wl] - linePositions[i + 1][prev.wl] <= 
					1 + shiftCount) {
					shiftCount++;
					current.curve.x += shiftCount * CURVE_ADJUSTMENT;
					current.line.length += shiftCount * CURVE_ADJUSTMENT;
					currentLines[current.wl].x += shiftCount * CURVE_ADJUSTMENT;
					currentLines[current.wl].length -= shiftCount * CURVE_ADJUSTMENT;
				}
				else {
					shiftCount = 0;
				}
			}
			shiftCount = 0;
			for (int j=downCurves.size() - 2; j>=0; j--) {
				IntoS current = downCurves.get(j);
				IntoS prev = downCurves.get(j + 1);
				if (linePositions[i][prev.wl] - linePositions[i][current.wl] <= 1 + shiftCount || 
					linePositions[i + 1][prev.wl] - linePositions[i + 1][current.wl] <= 
					1 + shiftCount) {
					shiftCount++;
					current.curve.x += shiftCount * CURVE_ADJUSTMENT;
					current.line.length += shiftCount * CURVE_ADJUSTMENT;
					currentLines[current.wl].x += shiftCount * CURVE_ADJUSTMENT;
					currentLines[current.wl].length -= shiftCount * CURVE_ADJUSTMENT;
				}
				else {
					shiftCount = 0;
				}
			}
			if (e.shift) {
				int j = 0;
				int l = lineOrder[i][j];
				while (e.indexes[l] != 0) {
					j++;
					l = lineOrder[i][j];
				}
				int size = e.sizes[0];
				if (size > 2) {
					RoundedRectangle rr = new RoundedRectangle(currentLines[l].x + 
						currentLines[l].length - EVENT_WIDTH + EVENT_LINE_WIDTH / 2,
						currentLines[l].y - (LINE_WIDTH_PART_ONE + MIN_EVENT_HEIGHT) / 2,
						(size - 2) * LINE_WIDTH_PART_ONE + MIN_EVENT_HEIGHT);
					rrToAdd.add(rr);
					rrsAbove.add(rr);
					j += size - 2;
					l = lineOrder[i][j];
				}
				for (int k=0; k<2; k++) {
					l = lineOrder[i][j + k];
					RoundedRectangle rr = new RoundedRectangle(currentLines[l].x + 
						currentLines[l].length - EVENT_WIDTH + EVENT_LINE_WIDTH / 2,
						currentLines[l].y - (LINE_WIDTH_PART_ONE + MIN_EVENT_HEIGHT) / 2,
						LINE_WIDTH_PART_ONE + MIN_EVENT_HEIGHT);
					rrToAdd.add(rr);
					if (k == 0 && size <= 2) {
						rrsAbove.add(rr);
					}
				}
				int l0 =  lineOrder[i][j];
				int atop = currentLines[l0].y + (LINE_WIDTH_PART_ONE + MIN_EVENT_HEIGHT + 
					EVENT_LINE_WIDTH) / 2;
				int abot = currentLines[l].y - (LINE_WIDTH_PART_ONE + MIN_EVENT_HEIGHT + 
					EVENT_LINE_WIDTH) / 2;
				VerticalArrow arrow = new VerticalArrow(currentLines[l].x + 
					currentLines[l].length - EVENT_WIDTH / 2,atop,abot - atop);
				vaToAdd.add(arrow);
				String text = eventInfo.get(i)[0];
				minTXA = Math.max((currentLines[l].x + currentLines[l].length - EVENT_WIDTH) / 10.0,
					minTXA);
				TextBox tbToAdd = new TextBox(minTXA,0,text,-TEXTBOX_ROTATION,
					PART_ONE_MAX_TEXTBOX_WIDTH_ABOVE);
				minTXA += (tbToAdd.height - TEXTBOX_HEIGHT) * sinA;
				tbToAdd.x = minTXA;
				tbToAdd.y = -tbToAdd.height * cosA;
				rotate(tbToAdd);
				maxTHeightA = Math.max(maxTHeightA,getActualHeight(tbToAdd));
				textAbove.add(tbToAdd);
				minTXA += ((tbToAdd.height * sinA) + (TEXTBOX_HEIGHT * cosA / tanA) + 
					(TEXTBOX_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT / sinA));
			}
			else {
				int alreadyAbove = 0;
				int alreadyBelow = 0;
				int firstActive = -1;
				int eventsSeen = 0;
				double belowXAdd = 0;
				double belowYAdd = 0;
				for (int j=0; j<totalLines; j++) {
					int l = lineOrder[i][j];
					if (firstActive < 0 && i < cutoff[l]) {
						firstActive = j;
					}
					int eInd = e.indexes[l];
					if (eInd >= 0) {
						eventsSeen++;
						int size = e.sizes[eInd];
						int l2 = lineOrder[i][j + size - 1];
						RoundedRectangle rr = new RoundedRectangle(currentLines[l].x + 
							currentLines[l].length - EVENT_WIDTH + EVENT_LINE_WIDTH / 2,
							currentLines[l].y - (LINE_WIDTH_PART_ONE + MIN_EVENT_HEIGHT) / 2,
							size * LINE_WIDTH_PART_ONE + MIN_EVENT_HEIGHT);
						rrToAdd.add(rr);
						String text = eventInfo.get(i)[eInd];
						if (text.length() > 1) {
							int topGap = j - firstActive;
							int bottomGap = totalLines - (j + size);
							if (topGap < bottomGap) {
								minTXA = Math.max((currentLines[l].x + currentLines[l].length - 
									EVENT_WIDTH) / 10.0,minTXA);
								TextBox tbToAdd = new TextBox(minTXA,0,text,-TEXTBOX_ROTATION,
									PART_ONE_MAX_TEXTBOX_WIDTH_ABOVE);
								if (alreadyAbove == 0) {
									minTXA += (tbToAdd.height - TEXTBOX_HEIGHT) * sinA;
									tbToAdd.x = minTXA;
								}
								else {
									TextBox prevTB = textAbove.get(textAbove.size() - 1);
									minTXA += ((prevTB.height * sinA) + 
										(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT * 
										sinA) + ((tbToAdd.height + 
										(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT)) *
										cosA / tanA));
									tbToAdd.x = minTXA;
								}
								tbToAdd.y = -tbToAdd.height * cosA;
								rotate(tbToAdd);
								maxTHeightA = Math.max(maxTHeightA,getActualHeight(tbToAdd));
								if (alreadyAbove > 0) {
									for (int k=0; k<alreadyAbove; k++) {
										TextBox toShift = textAbove.get(textAbove.size() - 1 - k);
										toShift.x += ((tbToAdd.height + 
											(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
											TEXTBOX_HEIGHT)) * cosA / tanA);
										toShift.y -= (tbToAdd.height + 
											(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
											TEXTBOX_HEIGHT)) * cosA;
										maxTHeightA = Math.max(maxTHeightA,getActualHeight(
											toShift));
									}
								}
								textAbove.add(tbToAdd);
								rrsAbove.add(rr);
								alreadyAbove++;
							}
							else {
								minTXB = Math.max((currentLines[l].x + currentLines[l].length - 
									EVENT_WIDTH) / 10.0,minTXB);
								TextBox tbToAdd = new TextBox(minTXB,0,text,TEXTBOX_ROTATION,
									PART_ONE_MAX_TEXTBOX_WIDTH_BELOW);
								tbToAdd.x = minTXB + tbToAdd.height * sinA + belowXAdd;
								tbToAdd.y = belowYAdd;
								if (eventsSeen == e.numEvents) {
									minTXB += (tbToAdd.height - TEXTBOX_HEIGHT) * sinA;
									tbToAdd.x = minTXB + tbToAdd.height * sinA + belowXAdd;
								}
								rotate(tbToAdd);
								maxTHeightB = Math.max(maxTHeightB,getActualHeight(tbToAdd));
								textBelow.add(tbToAdd);
								rrsBelow.add(rr);
								if (alreadyBelow > 0) {
									for (int k=0; k<alreadyBelow; k++) {
										TextBox toShift = textBelow.get(textBelow.size() - 2 - k);
										if (eventsSeen == e.numEvents) {
											toShift.x += (tbToAdd.height - TEXTBOX_HEIGHT) * sinA;
										}
										toShift.x += ((tbToAdd.height * sinA) + 
											(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
											TEXTBOX_HEIGHT * sinA) + ((textBelow.get(
											textBelow.size() - 2).height + 
											(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
											TEXTBOX_HEIGHT)) * cosA / tanA));
									}
								}
								alreadyBelow++;
								belowXAdd += ((tbToAdd.height + 
									(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT)) * 
									cosA / tanA);
								belowYAdd += ((tbToAdd.height + 
									(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT)) * 
									cosA);
							}
						}
						j += size - 1;
					}
				}
				if (alreadyAbove > 0) {
					minTXA += ((textAbove.get(textAbove.size() - 1).height * sinA) + 
						(TEXTBOX_HEIGHT * cosA / tanA) + (TEXTBOX_INTRA_SPACE_GAP_MULTIPLE * 
						TEXTBOX_HEIGHT / sinA));
				}
				if (alreadyBelow > 0) {
					minTXB += ((textBelow.get(textBelow.size() - 1).height * sinA) + 
						(TEXTBOX_HEIGHT * cosA / tanA) + (TEXTBOX_INTRA_SPACE_GAP_MULTIPLE * 
						TEXTBOX_HEIGHT / sinA));
					for (int k=1; k<alreadyBelow; k++) {
						TextBox tbc = textBelow.get(textBelow.size() - 1 - k);
						TextBox tbn = textBelow.get(textBelow.size() - k);
						minTXB +=  (tbn.height * sinA) + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
							TEXTBOX_HEIGHT * sinA) + ((tbc.height + 
							(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT)) * cosA / 
							tanA);
					}
				}
			}
		}
		int yAddAbove = actualToUsable(maxTHeightA + PART_ONE_TEXT_WHITESPACE);
		int yAddBelow = actualToUsable(maxTHeightB + PART_ONE_TEXT_WHITESPACE);
		for (HorizontalLine hl : hLinesToAdd) {
			hl.y += yAddAbove;
			shapes.add(new ShapeID(hl.toString(),ShapeID.H_LINE_TYPE,hl.y));
		}
		for (CurvedLine cl : cLinesToAdd) {
			cl.y += yAddAbove;
			shapes.add(new ShapeID(cl.toString(),ShapeID.C_LINE_TYPE,
				(cl.ygap < 0 ? cl.y : -cl.y - cl.ygap)));
		}
		for (RoundedRectangle rr : rrToAdd) {
			rr.y += yAddAbove;
			shapes.add(new ShapeID(rr.toString(),ShapeID.RR_TYPE,rr.height));
		}
		for (VerticalArrow va : vaToAdd) {
			va.y += yAddAbove;
			shapes.add(new ShapeID(va.toString(),ShapeID.VA_TYPE,va.y));
		}
		for (int i=0; i<textAbove.size(); i++) {
			TextBox tb = textAbove.get(i);
			RoundedRectangle rr = rrsAbove.get(i);
			double rrTopX = (rr.x + (EVENT_WIDTH - EVENT_LINE_WIDTH) / 2) / 10.0;
			double rrTopY = (rr.y - EVENT_LINE_WIDTH / 2) / 10.0;
			tb.y += START_Y / 10.0 + maxTHeightA;
			shapes.add(new ShapeID(tb.toString(),ShapeID.TB_TYPE,(int) tb.y));
			double[] tbLowerLine = getLowerLine(tb);
			double yVertBottom = (START_Y / 10.0 + maxTHeightA + PART_ONE_TEXT_WHITESPACE - 
				TAG_GAP_PART_ONE);
			Tag tag = new Tag(rrTopX,rrTopY,tbLowerLine[0],yVertBottom,tbLowerLine[0],
				tbLowerLine[1],tbLowerLine[2],tbLowerLine[3]);
			shapes.add(new ShapeID(tag.toString(),ShapeID.TAG_TYPE,(int) tbLowerLine[3]));
		}
		for (int i=0; i<textBelow.size(); i++) {
			TextBox tb = textBelow.get(i);
			RoundedRectangle rr = rrsBelow.get(i);
			double rrBottomX = (rr.x + (EVENT_WIDTH - EVENT_LINE_WIDTH) / 2) / 10.0;
			double rrBottomY = (rr.y + rr.height + EVENT_LINE_WIDTH / 2) / 10.0;
			tb.y += ((START_Y + (totalLines + GROUP_GAP_PART_ONE * (maxNumGroups - 1)) * 
				LINE_WIDTH_PART_ONE + yAddAbove) / 10.0 + PART_ONE_TEXT_WHITESPACE);
			shapes.add(new ShapeID(tb.toString(),ShapeID.TB_TYPE,(int) tb.y));
			double[] tbLowerLine = getLowerLine(tb);
			double yVertTop = ((START_Y + (totalLines + GROUP_GAP_PART_ONE * (maxNumGroups - 1)) * 
				LINE_WIDTH_PART_ONE + yAddAbove) / 10.0 + TAG_GAP_PART_ONE);
			Tag tag = new Tag(rrBottomX,rrBottomY,tbLowerLine[0],yVertTop,tbLowerLine[0],
				tbLowerLine[1],tbLowerLine[2],tbLowerLine[3]);
			shapes.add(new ShapeID(tag.toString(),ShapeID.TAG_TYPE,(int) yVertTop));
		}
		Collections.sort(shapes);
		for (ShapeID s : shapes) {
			System.out.printf("\t%s\n",s.string);
		}
		return (START_Y + (totalLines + GROUP_GAP_PART_ONE * maxNumGroups) * LINE_WIDTH_PART_ONE + 
			yAddAbove + yAddBelow);
	}

	private int formatSecond(int starty) {
		LinkedList<SubLine> sublines = new LinkedList<>();
		ArrayList<SubCurvedLine> subclines = new ArrayList<>();
		ArrayList<SubRoundedRectangle> subrrs = new ArrayList<>();
		ArrayList<SubVerticalArrow> subarrows = new ArrayList<>();
		ArrayList<SubReturnZ> subrzs = new ArrayList<>();
		ArrayList<Integer> starts = new ArrayList<>();
		ArrayList<LinkedList<SubLine>> minorsublines = new ArrayList<>();
		ArrayList<ArrayList<SubCurvedLine>> minorsubclines = new ArrayList<>();
		ArrayList<ArrayList<SubRoundedRectangle>> minorsubrrs = new ArrayList<>();
		ArrayList<ArrayList<SubVerticalArrow>> minorsubarrows = new ArrayList<>();
		ArrayList<ArrayList<SubReturnZ>> minorsubrzs = new ArrayList<>();
		ArrayList<Integer> minorx = new ArrayList<>();
		ArrayList<Integer> minortime = new ArrayList<>();
		ArrayList<ArrayList<SubTextBox>> minorTextAbove = new ArrayList<>();
		ArrayList<ArrayList<SubTextBox>> minorTextBelow = new ArrayList<>();
		ArrayList<ArrayList<SubTextBox>> minorTextEnd = new ArrayList<>();
		ArrayList<ArrayList<SubTextBox>> majorText = new ArrayList<>();
		ArrayList<SubTextBox> majorTextEnd = new ArrayList<>();
		ArrayList<Integer> maxmajorx = new ArrayList<>();
		int majorx = 0;
		int majortime = leaps.get(0).time;
		double sinA = Math.sin(TEXTBOX_ROTATION * Math.PI / 180.0);
		double cosA = Math.cos(TEXTBOX_ROTATION * Math.PI / 180.0);
		double tanA = Math.tan(TEXTBOX_ROTATION * Math.PI / 180.0);
		for (LeapInfo l : leaps) {
			int nexttime = l.time;
			switch (l.info[1]) {
				case "begin": {
					int wl = Integer.parseInt(l.info[2]);
					sublines.add(new SubLine(new HorizontalLine(majorx,0,1,lineNames.get(wl),
						LINE_WIDTH_PART_TWO),wl,"00"));
					starts.add(wl);
					minorx.add(0);
					LinkedList<SubLine> minorl = new LinkedList<>();
					minorl.add(new SubLine(new HorizontalLine(minorx.get(0),0,1,lineNames.get(wl),
						LINE_WIDTH_PART_TWO),wl,"00"));
					minorsublines.add(minorl);
					minorsubclines.add(new ArrayList<>());
					minorsubrrs.add(new ArrayList<>());
					minorsubarrows.add(new ArrayList<>());
					minorsubrzs.add(new ArrayList<>());
					minortime.add(nexttime);
					minorTextAbove.add(new ArrayList<>());
					minorTextBelow.add(new ArrayList<>());
					minorTextEnd.add(new ArrayList<>());
					for (int i=0; i<2; i++) {
						majorText.add(new ArrayList<>());
					}
					maxmajorx.add(1);
					break;
				}
				case "timeleap": {
					if (nexttime > majortime) {
						majorx += 2;
						majortime = nexttime;
					}
					int wl = Integer.parseInt(l.info[2]);
					String sublinename = String.format("%02d",Integer.parseInt(l.info[3]));
					ListIterator<SubLine> it = sublines.listIterator();
					int aboveIndex = sublines.size();
					SubLine originLine = null;
					for (int i=0; i<sublines.size(); i++) {
						SubLine next = it.next();
						if (next.wl == wl && next.name.compareTo(sublinename) < 0) {
							if (next.line.x < majorx) {
								originLine = next;
							}
							aboveIndex = i;
						}
					}
					it = sublines.listIterator(aboveIndex);
					it.next();
					originLine.line.length = majorx + 1 - originLine.line.x;
					if (starts.contains(wl)) {
						int ind = starts.indexOf(wl);
						maxmajorx.set(ind,majorx);
					}
					it.add(new SubLine(new HorizontalLine(majorx,0,1,lineNames.get(wl),
						LINE_WIDTH_PART_TWO),wl,sublinename));
					subclines.add(new SubCurvedLine(new CurvedLine(majorx - 1,0,1,0,
						lineNames.get(wl),LINE_WIDTH_PART_TWO),wl,originLine.name,sublinename));
					for (int i=0; i<starts.size(); i++) {
						if (starts.get(i).equals(wl)) {
							it = minorsublines.get(i).listIterator();
							for (int j=0; j<minorsublines.get(i).size(); j++) {
								SubLine next = it.next();
								if (next.wl == wl && next.name.equals(originLine.name)) {
									if (nexttime > minortime.get(i)) {
										minorx.set(i,minorx.get(i) + 2);
										minortime.set(i,nexttime);
									}
									next.line.length = minorx.get(i) + 1 - next.line.x;
									aboveIndex = j;
									for (int k=j + 1; k<minorsublines.get(i).size(); k++) {
										next = it.next();
										if (next.wl == wl && next.name.compareTo(sublinename) < 0) {
											aboveIndex = k;
										}
									}
									it = minorsublines.get(i).listIterator(aboveIndex);
									it.next();
									it.add(new SubLine(new HorizontalLine(minorx.get(i),0,1,
										lineNames.get(wl),LINE_WIDTH_PART_TWO),wl,sublinename));
									minorsubclines.get(i).add(new SubCurvedLine(new CurvedLine(
										minorx.get(i) - 1,0,1,0,lineNames.get(wl),
										LINE_WIDTH_PART_TWO),wl,originLine.name,sublinename));
									break;
								}
							}
						}
					}
					break;
				}
				case "split": {
					if (nexttime > majortime) {
						majorx += 2;
						majortime = nexttime;
					}
					int wl = Integer.parseInt(l.info[2]);
					String sublinename = String.format("%02d",Integer.parseInt(l.info[3]));
					ListIterator<SubLine> it = sublines.listIterator();
					SubLine toSplit = it.next();
					while (!(toSplit.wl == wl && toSplit.name.equals(sublinename))) {
						toSplit = it.next();
					}
					toSplit.line.length = majorx - 1 - toSplit.line.x;
					if (starts.contains(wl)) {
						int ind = starts.indexOf(wl);
						maxmajorx.set(ind,majorx);
					}
					toSplit.active = false;
					it.previous();
					it.add(new SubLine(new HorizontalLine(majorx,0,1,lineNames.get(wl),
						LINE_WIDTH_PART_TWO),wl,sublinename + "a"));
					subclines.add(new SubCurvedLine(new CurvedLine(majorx - 1,0,1,0,
						lineNames.get(wl),LINE_WIDTH_PART_TWO),wl,sublinename,sublinename + "a"));
					it.next();
					it.add(new SubLine(new HorizontalLine(majorx,0,1,lineNames.get(wl),
						LINE_WIDTH_PART_TWO),wl,sublinename + "b"));
					subclines.add(new SubCurvedLine(new CurvedLine(majorx - 1,0,1,0,
						lineNames.get(wl),LINE_WIDTH_PART_TWO),wl,sublinename,sublinename + "b"));
					for (int i=0; i<starts.size(); i++) {
						if (starts.get(i).equals(wl)) {
							it = minorsublines.get(i).listIterator();
							for (int j=0; j<minorsublines.get(i).size(); j++) {
								toSplit = it.next();
								if (toSplit.wl == wl && toSplit.name.equals(sublinename)) {
									if (nexttime > minortime.get(i)) {
										minorx.set(i,minorx.get(i) + 2);
										minortime.set(i,nexttime);
									}
									toSplit.line.length = minorx.get(i) - 1 - toSplit.line.x;
									toSplit.active = false;
									it.previous();
									it.add(new SubLine(new HorizontalLine(minorx.get(i),0,1,
										lineNames.get(wl),LINE_WIDTH_PART_TWO),wl,
										sublinename + "a"));
									minorsubclines.get(i).add(new SubCurvedLine(new CurvedLine(
										minorx.get(i) - 1,0,1,0,lineNames.get(wl),
										LINE_WIDTH_PART_TWO),wl,sublinename,sublinename + "a"));
									it.next();
									it.add(new SubLine(new HorizontalLine(minorx.get(i),0,1,
										lineNames.get(wl),LINE_WIDTH_PART_TWO),wl,
										sublinename + "b"));
									minorsubclines.get(i).add(new SubCurvedLine(new CurvedLine(
										minorx.get(i) - 1,0,1,0,lineNames.get(wl),
										LINE_WIDTH_PART_TWO),wl,sublinename,sublinename + "b"));
									break;
								}
							}
						}
					}
					break;
				}
				case "shift": {
					if (nexttime > majortime) {
						majorx += 2;
						majortime = nexttime;
					}
					int wl = Integer.parseInt(l.info[2]);
					String sublinename = getSubLineName(l.info[3]);
					int wlTo = Integer.parseInt(l.info[4]);
					ListIterator<SubLine> it = sublines.listIterator();
					int start = -1;
					boolean endExists = false;
					for (int i=0; i<sublines.size(); i++) {
						SubLine next = it.next();
						if (next.wl == wl && next.name.equals(sublinename)) {
							start = i;
							next.line.length = majorx - next.line.x;
							if (starts.contains(wl)) {
								int ind = starts.indexOf(wl);
								maxmajorx.set(ind,majorx);
							}
							next.active = false;
						}
						if (next.wl == wlTo && next.name.equals("00")) {
							endExists = true;
							next.line.length = majorx - next.line.x;
						}
					}
					if (!endExists) {
						it = sublines.listIterator(start);
						it.next();
						it.add(new SubLine(new HorizontalLine(majorx - 2,0,2,lineNames.get(wlTo),
							LINE_WIDTH_PART_TWO),wlTo,"00"));
					}
					subrrs.add(new SubRoundedRectangle(new RoundedRectangle(majorx,0,0),wl,
						sublinename,wl,sublinename));
					subrrs.add(new SubRoundedRectangle(new RoundedRectangle(majorx,0,0),wlTo,
						"00",wlTo,"00"));
					subarrows.add(new SubVerticalArrow(new VerticalArrow(majorx,0,0),wl,sublinename,
						wlTo,"00"));
					if (l.text.length() > 1) {
						majorTextEnd.add(new SubTextBox(new TextBox(0,0,l.text,0),wl,sublinename,
							majorx));
					}
					for (int i=0; i<starts.size(); i++) {
						if (starts.get(i).equals(wl)) {
							it = minorsublines.get(i).listIterator();
							for (int j=0; j<minorsublines.get(i).size(); j++) {
								SubLine next = it.next();
								if (next.wl == wl && next.name.equals(sublinename)) {
									if (nexttime > minortime.get(i)) {
										minorx.set(i,minorx.get(i) + 2);
										minortime.set(i,nexttime);
									}
									next.line.length = minorx.get(i) - next.line.x;
									next.active = false;
									boolean placeTextAbove = false;
									if (starts.contains(wlTo)) {
										minorsublines.get(i).add(new SubLine(new HorizontalLine(
											minorx.get(i) - 2,0,2,lineNames.get(wlTo),
											LINE_WIDTH_PART_TWO),wlTo,"00"));
									}
									else {
										if (j < minorsublines.get(i).size() - j - 1) {
											placeTextAbove = true;
										}
										it.add(new SubLine(new HorizontalLine(minorx.get(i) - 2,0,2,
											lineNames.get(wlTo),LINE_WIDTH_PART_TWO),wlTo,"00"));
									}
									minorsubrrs.get(i).add(new SubRoundedRectangle(
										new RoundedRectangle(minorx.get(i),0,0),wl,sublinename,wl,
										sublinename));
									minorsubrrs.get(i).add(new SubRoundedRectangle(
										new RoundedRectangle(minorx.get(i),0,0),wlTo,"00",wlTo,
										"00"));
									minorsubarrows.get(i).add(new SubVerticalArrow(
										new VerticalArrow(minorx.get(i),0,0),wl,sublinename,wlTo,
										"00"));
									if (l.text.length() > 1) {
										minorTextEnd.get(i).add(new SubTextBox(new TextBox(0,0,
											l.text,0),wl,sublinename,minorx.get(i)));
										if (placeTextAbove) {
											minorTextAbove.get(i).add(new SubTextBox(new TextBox(
												minorx.get(i),0,"",-TEXTBOX_ROTATION,
												PART_TWO_B_MAX_TEXTBOX_WIDTH_ABOVE),wl,sublinename,
												minorx.get(i)));
										}
										else {
											minorTextBelow.get(i).add(new SubTextBox(new TextBox(
												minorx.get(i),0,"",TEXTBOX_ROTATION,
												PART_TWO_B_MAX_TEXTBOX_WIDTH_BELOW),wlTo,"00",
												minorx.get(i)));
										}
									}
									break;
								}
							}
						}
						else if (starts.get(i).equals(wlTo)) {
							if (nexttime > minortime.get(i)) {
								minorx.set(i,minorx.get(i) + 2);
								minortime.set(i,nexttime);
							}
							it = minorsublines.get(i).listIterator();
							SubLine toAdd = new SubLine(new HorizontalLine(minorx.get(i) - 2,0,2,
								lineNames.get(wl),LINE_WIDTH_PART_TWO),wl,sublinename);
							toAdd.active = false;
							it.add(toAdd);
							minorsubrrs.get(i).add(new SubRoundedRectangle(new RoundedRectangle(
								minorx.get(i),0,0),wl,sublinename,wl,sublinename));
							minorsubrrs.get(i).add(new SubRoundedRectangle(new RoundedRectangle(
								minorx.get(i),0,0),wlTo,"00",wlTo,"00"));
							minorsubarrows.get(i).add(new SubVerticalArrow(new VerticalArrow(
								minorx.get(i),0,0),wl,sublinename,wlTo,"00"));
							if (l.text.length() > 1) {
								minorTextEnd.get(i).add(new SubTextBox(new TextBox(0,0,l.text,0),wl,
									sublinename,minorx.get(i)));
								minorTextAbove.get(i).add(new SubTextBox(new TextBox(minorx.get(i),
									0,"",-TEXTBOX_ROTATION,PART_TWO_B_MAX_TEXTBOX_WIDTH_ABOVE),wl,
									sublinename,minorx.get(i)));
							}
						}
					}
					break;
				}
				case "both":
				case "minor": {
					int wl = Integer.parseInt(l.info[2]);
					String sublinename = getSubLineName(l.info[3]);
					for (int i=0; i<starts.size(); i++) {
						ListIterator<SubLine> it = minorsublines.get(i).listIterator();
						for (int j=0; j<minorsublines.get(i).size(); j++) {
							SubLine sl = it.next();
							if (sl.wl == wl && sl.name.equals(sublinename) && sl.active) {
								if (nexttime > minortime.get(i)) {
									minorx.set(i,minorx.get(i) + 2);
									minortime.set(i,nexttime);
								}
								sl.line.length = minorx.get(i) - sl.line.x;
								if (l.endPoint) {
									sl.active = false;
									if (l.leapPoint) {
										for (int k=j + 1; k<minorsublines.get(i).size(); k++) {
											SubLine next = it.next();
											if (next.wl == wl && next.name.length() == 2) {
												if (sl.name.compareTo(next.name) < 0) {
													int xgap = (sl.line.length + sl.line.x - 
														next.line.x);
													minorsubrzs.get(i).add(new SubReturnZ(
														new ReturnZ(next.line.x,0,xgap,0,true),
														wl,sl.name,next.name));
													break;
												}
												else {
													int xgap = (sl.line.length + sl.line.x - 
														next.line.x - next.line.length);
													minorsubrzs.get(i).add(new SubReturnZ(
														new ReturnZ(
														next.line.x + next.line.length,0,xgap,0,
														false),wl,sl.name,next.name));
													sl = next;
												}
											}
										}
									}
								}
								minorsubrrs.get(i).add(new SubRoundedRectangle(
									new RoundedRectangle(minorx.get(i),0,0),wl,sublinename,wl,
									sublinename));
								if (l.text.length() > 1) {
									if (l.endPoint) {
										minorTextEnd.get(i).add(new SubTextBox(new TextBox(0,0,
											l.text,0),
											wl,sublinename,minorx.get(i)));
										if (j < minorsublines.get(i).size() - j - 1) {
											minorTextAbove.get(i).add(new SubTextBox(
												new TextBox(minorx.get(i),0,"",
												-TEXTBOX_ROTATION,
												PART_TWO_B_MAX_TEXTBOX_WIDTH_ABOVE),wl,
												sublinename,minorx.get(i)));
										}
										else {
											minorTextBelow.get(i).add(new SubTextBox(
												new TextBox(minorx.get(i),0,"",TEXTBOX_ROTATION,
												PART_TWO_B_MAX_TEXTBOX_WIDTH_BELOW),wl,
												sublinename,minorx.get(i)));
										}
									}
									else if (j < minorsublines.get(i).size() - j - 1) {
										minorTextAbove.get(i).add(new SubTextBox(new TextBox(
											minorx.get(i),0,l.text,-TEXTBOX_ROTATION,
											PART_TWO_B_MAX_TEXTBOX_WIDTH_ABOVE),wl,sublinename,
											minorx.get(i)));
									}
									else {
										minorTextBelow.get(i).add(new SubTextBox(new TextBox(
											minorx.get(i),0,l.text,TEXTBOX_ROTATION,
											PART_TWO_B_MAX_TEXTBOX_WIDTH_BELOW),wl,sublinename,
											minorx.get(i)));
									}
								}
								break;
							}
						}
					}
					if (l.info[1].equals("minor")) {
						break;
					}
				}
				case "major": {
					if (nexttime > majortime) {
						majorx += 2;
						majortime = nexttime;
					}
					int wl = Integer.parseInt(l.info[2]);
					String sublinename = getSubLineName(l.info[3]);
					ListIterator<SubLine> it = sublines.listIterator();
					int wlPrev = -1;
					for (int i=0; i<sublines.size(); i++) {
						SubLine sl = it.next();
						if (sl.wl == wl && sl.name.equals(sublinename)) {
							sl.line.length = majorx - sl.line.x;
							if (starts.contains(wl)) {
								int ind = starts.indexOf(wl);
								maxmajorx.set(ind,majorx);
							}
							else if (starts.contains(wlPrev)) {
								int ind = starts.indexOf(wlPrev);
								maxmajorx.set(ind,majorx);
							}
							if (l.endPoint) {
								sl.active = false;
								if (l.leapPoint) {
									for (int j=i + 1; j<sublines.size(); j++) {
										SubLine next = it.next();
										if (next.wl == wl && next.name.length() == 2) {
											if (sl.name.compareTo(next.name) < 0) {
												int xgap = sl.line.length + sl.line.x - next.line.x;
												subrzs.add(new SubReturnZ(new ReturnZ(next.line.x,0,
													xgap,0,true),wl,sl.name,next.name));
												break;
											}
											else {
												int xgap = (sl.line.length + sl.line.x - 
													next.line.x - next.line.length);
												subrzs.add(new SubReturnZ(new ReturnZ(
													next.line.x + next.line.length,0,xgap,0,false),
													wl,sl.name,next.name));
												sl = next;
											}
										}
									}
								}
							}
							break;
						}
						wlPrev = sl.wl;
					}
					subrrs.add(new SubRoundedRectangle(new RoundedRectangle(majorx,0,0),wl,
						sublinename,wl,sublinename));
					if (l.text.length() > 1) {
						boolean found = false;
						int activeAbove = 0;
						int activeBelow = 0;
						int ti = -1;
						for (int i=0; i<sublines.size(); i++) {
							SubLine sl = sublines.get(i);
							if (sl.name.equals("00") && starts.contains(sl.wl)) {
								if (found) {
									break;
								}
								else {
									ti++;
									activeAbove = 0;
								}
							}
							if (sl.wl == wl && sl.name.equals(sublinename)) {
								found = true;
							}
							else if (sl.active) {
								if (found) {
									activeBelow++;
								}
								else {
									activeAbove++;
								}
							}
						}
						if (activeAbove <= activeBelow) {
							if (l.endPoint) {
								majorTextEnd.add(new SubTextBox(new TextBox(0,0,l.text,0),wl,
									sublinename,majorx));
								majorText.get(ti * 2).add(new SubTextBox(new TextBox(0,0,"",
									-TEXTBOX_ROTATION,PART_TWO_A_MAX_TEXTBOX_WIDTH),wl,sublinename,
									majorx));
							}
							else {
								majorText.get(ti * 2).add(new SubTextBox(new TextBox(0,0,l.text,
									-TEXTBOX_ROTATION,PART_TWO_A_MAX_TEXTBOX_WIDTH),wl,sublinename,
									majorx));
							}
						}
						else {
							if (l.endPoint) {
								majorTextEnd.add(new SubTextBox(new TextBox(0,0,l.text,0),wl,
									sublinename,majorx));
								majorText.get(ti * 2 + 1).add(new SubTextBox(new TextBox(0,0,"", 
									TEXTBOX_ROTATION,PART_TWO_A_MAX_TEXTBOX_WIDTH),wl,sublinename,
									majorx));
							}
							else {
								majorText.get(ti * 2 + 1).add(new SubTextBox(new TextBox(0,0,l.text, 
									TEXTBOX_ROTATION,PART_TWO_A_MAX_TEXTBOX_WIDTH),wl,sublinename,
									majorx));
							}
						}
					}
					break;
				}
				case "multiboth":
				case "multiminor": {
					int wl = Integer.parseInt(l.info[2]);
					int numLines = Integer.parseInt(l.info[3]);
					String sublinename = getSubLineName(l.info[4]);
					HashSet<String> toInclude = new HashSet<>();
					for (int i=0; i<starts.size(); i++) {
						ListIterator<SubLine> it = minorsublines.get(i).listIterator();
						if (toInclude.size() == 0) {
							int j = 0;
							for (; j < minorsublines.get(i).size(); j++) {
								SubLine next = it.next();
								if (next.wl == wl && next.name.equals(sublinename)) {
									break;
								}
							}
							int numAbove = j;
							int numBelow = minorsublines.get(i).size() - j - 1;
							if (j < minorsublines.get(i).size()) {
								if (nexttime > minortime.get(i)) {
									minorx.set(i,minorx.get(i) + 2);
									minortime.set(i,nexttime);
								}
								it.previous();
								int wlEnd = -1;
								String nameEnd = null;
								for (; j < minorsublines.get(i).size() && 
									toInclude.size() < numLines; j++) {
									SubLine next = it.next();
									if (next.active) {
										next.line.length = minorx.get(i) - next.line.x;
										wlEnd = next.wl;
										nameEnd = next.name;
										numBelow = minorsublines.get(i).size() - j - 1;
										if (l.endPoint) {
											next.active = false;
											if (l.leapPoint) {
												ListIterator<SubLine> itf = 
													minorsublines.get(i).listIterator(j + 1);
												for (int k=j + 1; k<minorsublines.get(i).size();
													k++) {
													SubLine nextf = itf.next();
													if (nextf.wl == wl && 
														nextf.name.length() == 2) {
														if (next.name.compareTo(
															nextf.name) < 0) {
															int xgap = (next.line.length + 
																next.line.x - nextf.line.x);
															minorsubrzs.get(i).add(new SubReturnZ(
																new ReturnZ(nextf.line.x,0,xgap,0,
																true),wl,next.name,nextf.name));
															break;
														}
														else {
															int xgap = (next.line.length + 
																next.line.x - nextf.line.x - 
																nextf.line.length);
															minorsubrzs.get(i).add(new SubReturnZ(
																new ReturnZ(nextf.line.x + 
																nextf.line.length,0,xgap,0,false),
																wl,next.name,nextf.name));
															next = nextf;
														}
													}
												}
											}
										}
										toInclude.add("" + wlEnd + "-" + nameEnd);
									}
								}
								minorsubrrs.get(i).add(new SubRoundedRectangle(new RoundedRectangle(
									minorx.get(i),0,0),wl,sublinename,wlEnd,nameEnd));
								if (l.text.length() > 1) {
									if (l.endPoint) {
										minorTextEnd.get(i).add(new SubTextBox(new TextBox(0,0,
											l.text,0),wl,sublinename,minorx.get(i)));
										if (numAbove < numBelow) {
											minorTextAbove.get(i).add(new SubTextBox(new TextBox(
												minorx.get(i),0,"",-TEXTBOX_ROTATION,
												PART_TWO_B_MAX_TEXTBOX_WIDTH_ABOVE),wl,sublinename,
												minorx.get(i)));
										}
										else {
											minorTextBelow.get(i).add(new SubTextBox(new TextBox(
												minorx.get(i),0,"",TEXTBOX_ROTATION,
												PART_TWO_B_MAX_TEXTBOX_WIDTH_BELOW),wlEnd,nameEnd,
												minorx.get(i)));
									}
									}
									else if (numAbove < numBelow) {
										minorTextAbove.get(i).add(new SubTextBox(new TextBox(
											minorx.get(i),0,l.text,-TEXTBOX_ROTATION,
											PART_TWO_B_MAX_TEXTBOX_WIDTH_ABOVE),wl,sublinename,
											minorx.get(i)));
									}
									else {
										minorTextBelow.get(i).add(new SubTextBox(new TextBox(
											minorx.get(i),0,l.text,TEXTBOX_ROTATION,
											PART_TWO_B_MAX_TEXTBOX_WIDTH_BELOW),wlEnd,nameEnd,
											minorx.get(i)));
									}
								}
							}
						}
						else {
							int wlStart = -1;
							String nameStart = null;
							int wlEnd = -1;
							String nameEnd = null;
							int numAbove = 0;
							int numBelow = 0;
							for (int j=0; j<minorsublines.get(i).size(); j++) {
								SubLine next = it.next();
								if (next.active) {
									String line = "" + next.wl + "-" + next.name;
									if (toInclude.size() < numLines || toInclude.contains(line)) {
										if (wlStart < 0) {
											if (nexttime > minortime.get(i)) {
												minorx.set(i,minorx.get(i) + 2);
												minortime.set(i,nexttime);
											}
											wlStart = next.wl;
											nameStart = next.name;
											numAbove = j;
										}
										next.line.length = minorx.get(i) - next.line.x;
										wlEnd = next.wl;
										nameEnd = next.name;
										numBelow = minorsublines.get(i).size() - j - 1;
										if (l.endPoint) {
											next.active = false;
										}
										toInclude.add(line);
									}
									else {
										break;
									}
								}
							}
							if (wlStart >= 0) {
								minorsubrrs.get(i).add(new SubRoundedRectangle(new RoundedRectangle(
									minorx.get(i),0,0),wlStart,nameStart,wlEnd,nameEnd));
								if (l.text.length() > 1) {
									if (l.endPoint) {
										minorTextEnd.get(i).add(new SubTextBox(new TextBox(0,0,
											l.text,0),wlStart,nameStart,minorx.get(i)));
										if (numAbove < numBelow) {
											minorTextAbove.get(i).add(new SubTextBox(new TextBox(
												minorx.get(i),0,"",-TEXTBOX_ROTATION,
												PART_TWO_B_MAX_TEXTBOX_WIDTH_ABOVE),wlStart,
												nameStart,minorx.get(i)));
										}
										else {
											minorTextBelow.get(i).add(new SubTextBox(new TextBox(
												minorx.get(i),0,"",TEXTBOX_ROTATION,
												PART_TWO_B_MAX_TEXTBOX_WIDTH_BELOW),wlEnd,nameEnd,
												minorx.get(i)));
										}
									}
									else if (numAbove < numBelow) {
										minorTextAbove.get(i).add(new SubTextBox(new TextBox(
											minorx.get(i),0,l.text,-TEXTBOX_ROTATION,
											PART_TWO_B_MAX_TEXTBOX_WIDTH_ABOVE),wlStart,nameStart,
											minorx.get(i)));
									}
									else {
										minorTextBelow.get(i).add(new SubTextBox(new TextBox(
											minorx.get(i),0,l.text,TEXTBOX_ROTATION,
											PART_TWO_B_MAX_TEXTBOX_WIDTH_BELOW),wlEnd,nameEnd,
											minorx.get(i)));
									}
								}
							}
						}
					}
					if (l.info[1].equals("multiminor")) {
						break;
					}
				}
				case "multimajor": {
					if (nexttime > majortime) {
						majorx += 2;
						majortime = nexttime;
					}
					int wl = Integer.parseInt(l.info[2]);
					int numLines = Integer.parseInt(l.info[3]);
					String sublinename = getSubLineName(l.info[4]);
					ListIterator<SubLine> it = sublines.listIterator();
					int wlEnd = -1;
					String nameEnd = null;
					int wlPrev = -1;
					while (true) {
						SubLine next = it.next();
						if (next.wl == wl && next.name.equals(sublinename)) {
							next.line.length = majorx - next.line.x;
							if (starts.contains(wl)) {
								int ind = starts.indexOf(wl);
								maxmajorx.set(ind,majorx);
							}
							else if (starts.contains(wlPrev)) {
								int ind = starts.indexOf(wlPrev);
								maxmajorx.set(ind,majorx);
							}
							if (l.endPoint) {
								next.active = false;
								if (l.leapPoint) {
									int nextInd = it.nextIndex();
									ListIterator<SubLine> itf = sublines.listIterator(nextInd);
									while (true) {
										SubLine nextf = itf.next();
										if (nextf.wl == wl && nextf.name.length() == 2) {
											if (next.name.compareTo(nextf.name) < 0) {
												int xgap = (next.line.length + next.line.x - 
													nextf.line.x);
												subrzs.add(new SubReturnZ(new ReturnZ(nextf.line.x,
													0,xgap,0,true),wl,next.name,nextf.name));
												break;
											}
											else {
												int xgap = (next.line.length + next.line.x - 
													nextf.line.x - nextf.line.length);
												subrzs.add(new SubReturnZ(new ReturnZ(
													nextf.line.x + nextf.line.length,0,xgap,0,
													false),wl,next.name,nextf.name));
												next = nextf;
											}
										}
									}
								}
							}
							break;
						}
						wlPrev = next.wl;
					}
					int count = 1;
					while (count < numLines) {
						SubLine next = it.next();
						if (next.active) {
							next.line.length = majorx - next.line.x;
							if (starts.contains(next.wl)) {
								int ind = starts.indexOf(next.wl);
								maxmajorx.set(ind,majorx);
							}
							wlEnd = next.wl;
							nameEnd = next.name;
							count++;
							if (l.endPoint) {
								next.active = false;
								if (l.leapPoint) {
									int nextInd = it.nextIndex();
									ListIterator<SubLine> itf = sublines.listIterator(nextInd);
									while (true) {
										SubLine nextf = itf.next();
										if (nextf.wl == wl && nextf.name.length() == 2) {
											if (next.name.compareTo(nextf.name) < 0) {
												int xgap = (next.line.length + next.line.x - 
													nextf.line.x);
												subrzs.add(new SubReturnZ(new ReturnZ(nextf.line.x,
													0,xgap,0,true),wl,next.name,nextf.name));
												break;
											}
											else {
												int xgap = (next.line.length + next.line.x - 
													nextf.line.x - nextf.line.length);
												subrzs.add(new SubReturnZ(new ReturnZ(
													nextf.line.x + nextf.line.length,0,xgap,0,
													false),wl,next.name,nextf.name));
												next = nextf;
											}
										}
									}
								}
							}
						}
					}
					subrrs.add(new SubRoundedRectangle(new RoundedRectangle(majorx,0,0),wl,
						sublinename,wlEnd,nameEnd));
					if (l.text.length() > 1) {
						boolean foundTop = false;
						boolean foundBelow = false;
						int activeAbove = 0;
						int activeBelow = 0;
						int tiAbove = -1;
						int tiBelow = -1;
						for (int i=0; i<sublines.size(); i++) {
							SubLine sl = sublines.get(i);
							if (sl.name.equals("00") && starts.contains(sl.wl)) {
								if (foundBelow) {
									break;
								}
								else {
									tiBelow++;
									if (!foundTop) {
										tiAbove++;
										activeAbove = 0;
									}
								}
							}
							if (sl.wl == wl && sl.name.equals(sublinename)) {
								foundTop = true;
							}
							else if (sl.wl == wlEnd && sl.name.equals(nameEnd)) {
								foundBelow = true;
							}
							else if (sl.active) {
								if (!foundTop) {
									activeAbove++;
								}
								else if (foundBelow) {
									activeBelow++;
								}
							}
						}
						if (activeAbove <= activeBelow) {
							if (l.endPoint) {
								majorTextEnd.add(new SubTextBox(new TextBox(0,0,l.text,0),wl,
									sublinename,majorx));
								majorText.get(tiAbove * 2).add(new SubTextBox(new TextBox(0,0,"",
									-TEXTBOX_ROTATION,PART_TWO_A_MAX_TEXTBOX_WIDTH),wl,sublinename,
									majorx));
							}
							else {
								majorText.get(tiAbove * 2).add(new SubTextBox(new TextBox(0,0,
									l.text,-TEXTBOX_ROTATION,PART_TWO_A_MAX_TEXTBOX_WIDTH),wl,
									sublinename,majorx));
							}
						}
						else {
							if (l.endPoint) {
								majorTextEnd.add(new SubTextBox(new TextBox(0,0,l.text,0),wl,
									sublinename,majorx));
								majorText.get(tiBelow * 2 + 1).add(new SubTextBox(new TextBox(0,0,
									"",TEXTBOX_ROTATION,PART_TWO_A_MAX_TEXTBOX_WIDTH),wlEnd,
									nameEnd,majorx));
							}
							else {
								majorText.get(tiBelow * 2 + 1).add(new SubTextBox(new TextBox(0,0,
									l.text,TEXTBOX_ROTATION,PART_TWO_A_MAX_TEXTBOX_WIDTH),wlEnd,
									nameEnd,majorx));
							}
						}
					}
					break;
				}
			}
		}
		int currentx = START_X + PART_TWO_GAP;
		ArrayList<Integer> xPositions = new ArrayList<>();
		xPositions.add(currentx);
		double[] minTXN = new double[starts.size() + 1];
		int[] textNPos = new int[starts.size() * 2];
		double[] maxTHeightN = new double[starts.size() + 1];
		for (int i=0; i<minTXN.length; i++) {
			minTXN[i] = currentx / 10.0;
		}
		for (int i=1; i<=majorx / 2; i++) {
			currentx = currentx + EVENT_GAP_PART_TWO + EVENT_WIDTH;
			for (int j=0; j<starts.size() * 2; j++) {
				if (textNPos[j] < majorText.get(j).size() && 
					majorText.get(j).get(textNPos[j]).x == i * 2) {
					currentx = Math.max(currentx,actualToUsable(minTXN[(j + 1) / 2]) + EVENT_WIDTH);
				}
			}
			xPositions.add(currentx - EVENT_GAP_PART_TWO);
			xPositions.add(currentx);
			int[] already = new int[starts.size() * 2];
			for (int j=0; j<starts.size() * 2; j++) {
				double belowXAdd = 0;
				double belowYAdd = 0;
				while (textNPos[j] < majorText.get(j).size() && majorText.get(j).get(
					textNPos[j]).x == i * 2) {
					TextBox tb = majorText.get(j).get(textNPos[j]).tb;
					minTXN[(j + 1) / 2] = Math.max((currentx - EVENT_WIDTH) / 10.0,
						minTXN[(j + 1) / 2]);
					if (j % 2 == 0) {
						if (already[j] == 0) {
							minTXN[(j + 1) / 2] += (tb.height - TEXTBOX_HEIGHT) * sinA;
							tb.x = minTXN[(j + 1) / 2];
						}
						else {
							TextBox prevTB = majorText.get(j).get(textNPos[j] - 1).tb;
							minTXN[(j + 1) / 2] += ((prevTB.height * sinA) + 
								(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT * sinA) + 
								((tb.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
								TEXTBOX_HEIGHT)) * cosA / tanA));
							tb.x = minTXN[(j + 1) / 2];
						}
						tb.y = -tb.height * cosA;
						rotate(tb);
						maxTHeightN[(j + 1) / 2] = Math.max(maxTHeightN[(j + 1) / 2],
							getActualHeight(tb));
						if (already[j] > 0) {
							for (int k=0; k<already[j]; k++) {
								TextBox toShift = majorText.get(j).get(textNPos[j] - 1 - k).tb;
								toShift.x += (tb.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
									TEXTBOX_HEIGHT)) * cosA / tanA;
								toShift.y -= (tb.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
									TEXTBOX_HEIGHT)) * cosA;
								maxTHeightN[(j + 1) / 2] = Math.max(maxTHeightN[(j + 1) / 2],
									getActualHeight(toShift));
							}
						}
					}
					else {
						tb.x = minTXN[(j + 1) / 2] + tb.height * sinA + belowXAdd;
						tb.y = belowYAdd;
						boolean isLast = (textNPos[j] == majorText.get(j).size() - 1 || 
							majorText.get(j).get(textNPos[j] + 1).tb.x != i * 2);
						if (isLast) {
							minTXN[(j + 1) / 2] += (tb.height - TEXTBOX_HEIGHT) * sinA;
							tb.x = minTXN[(j + 1) / 2] + tb.height * sinA + belowXAdd;
						}
						rotate(tb);
						maxTHeightN[(j + 1) / 2] = Math.max(maxTHeightN[(j + 1) / 2],
							getActualHeight(tb));
						if (already[j] > 0) {
							for (int k=0; k<already[j]; k++) {
								TextBox toShift = majorText.get(j).get(textNPos[j] - 1 - k).tb;
								if (isLast) {
									toShift.x += (tb.height - TEXTBOX_HEIGHT) * sinA;
								}
								toShift.x += ((tb.height * sinA) + 
									(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT * 
									sinA) + ((majorText.get(j).get(textNPos[j] - 1).tb.height + 
									(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT)) * 
									cosA / tanA));
							}
						}
						belowXAdd += (tb.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
							TEXTBOX_HEIGHT)) * cosA / tanA;
						belowYAdd += (tb.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
							TEXTBOX_HEIGHT)) * cosA;
					}
					already[j]++;
					textNPos[j]++;
				}
				if (already[j] > 0) {
					if (j % 2 == 0) {
						minTXN[(j + 1) / 2] += (majorText.get(j).get(textNPos[j] - 1).tb.height * 
							sinA) + (TEXTBOX_HEIGHT * cosA / tanA) + 
							(TEXTBOX_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT / sinA);
					}
					else {
						minTXN[(j + 1) / 2] += (majorText.get(j).get(textNPos[j] - 1).tb.height * 
							sinA) + (TEXTBOX_HEIGHT * cosA / tanA) + 
							(TEXTBOX_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT / sinA);
						for (int k=1; k<already[j]; k++) {
							TextBox tbc = majorText.get(j).get(textNPos[j] - 1 - k).tb;
							TextBox tbn = majorText.get(j).get(textNPos[j] - k).tb;
							minTXN[(j + 1) / 2] += ((tbn.height * sinA) + 
								(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT * sinA) + 
								((tbc.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
								TEXTBOX_HEIGHT)) * cosA / tanA));
						}
					}
				}
			}
		}
		int totalYAdd = 0;
		int currenty = starty;
		ArrayList<ShapeID> shapes = new ArrayList<>();
		for (SubLine sl : sublines) {
			if (sl.name.equals("00") && starts.contains(sl.wl)) {
				int ind = starts.indexOf(sl.wl);
				if (maxTHeightN[ind] > 0) {
					currenty -= (GROUP_GAP_PART_TWO + 1) * LINE_WIDTH_PART_TWO;
				}
				if (ind > 0) {
					for (SubTextBox stb : majorText.get(ind * 2 - 1)) {
						if (stb.tb.message.length() > 1) {
							stb.tb.y += (currenty + totalYAdd) / 10.0 + PART_TWO_A_TEXT_WHITESPACE;
							shapes.add(new ShapeID(stb.tb.toString(),ShapeID.TB_TYPE,
								(int) stb.tb.y));
						}
					}
				}
				for (SubTextBox stb : majorText.get(ind * 2)) {
					if (stb.tb.message.length() > 1) {
						stb.tb.y += ((currenty + totalYAdd) / 10.0 + maxTHeightN[ind] + 
							(ind > 0 ? 1 : 0) * PART_TWO_A_TEXT_WHITESPACE);
						shapes.add(new ShapeID(stb.tb.toString(),ShapeID.TB_TYPE,(int) stb.tb.y));
					}
				}
				if (maxTHeightN[ind] > 0) {
					totalYAdd += actualToUsable(maxTHeightN[ind] + (ind > 0 ? 2 : 1) * 
						PART_TWO_A_TEXT_WHITESPACE);
				}
			}
			int xind = sl.line.x;
			int xindGap = sl.line.length;
			sl.line.x = xPositions.get(xind);
			sl.line.length = xPositions.get(xind + xindGap) - sl.line.x;
			sl.line.y = currenty + totalYAdd;
			currenty += (GROUP_GAP_PART_TWO + 1) * LINE_WIDTH_PART_TWO;
		}
		if (maxTHeightN[starts.size() - 1] > 0) {
			currenty -= (GROUP_GAP_PART_TWO + 1) * LINE_WIDTH_PART_TWO;
		}
		for (SubTextBox stb : majorText.get(starts.size() * 2 - 1)) {
			if (stb.tb.message.length() > 1) {
				stb.tb.y += (currenty + totalYAdd) / 10.0 + PART_TWO_A_TEXT_WHITESPACE;
				shapes.add(new ShapeID(stb.tb.toString(),ShapeID.TB_TYPE,(int) stb.tb.y));
			}
		}
		if (maxTHeightN[starts.size() - 1] > 0) {
			totalYAdd += actualToUsable(maxTHeightN[starts.size()] + PART_TWO_A_TEXT_WHITESPACE);
		}
		int centrey = (currenty + totalYAdd - starty - LINE_WIDTH_PART_TWO) / 2 + starty;
		int place = 0;
		for (SubLine sl : sublines) {
			shapes.add(new ShapeID(sl.line.toString(),ShapeID.H_LINE_TYPE,sl.line.y));
			if (place < starts.size() && starts.get(place).equals(sl.wl)) {
				int y = centrey + place * LINE_WIDTH_PART_TWO;
				CurvedLine cl = new CurvedLine(START_X,y,PART_TWO_GAP,sl.line.y - y,lineNames.get(sl.wl),LINE_WIDTH_PART_TWO);
				shapes.add(new ShapeID(cl.toString(),ShapeID.C_LINE_TYPE,(cl.ygap < 0 ? cl.y : -cl.y - cl.ygap)));
				place++;
			}
		}
		for (int i=0; i<starts.size(); i++) {
			for (SubTextBox stb : majorText.get(i * 2)) {
				if (stb.tb.message.length() > 1) {
					TextBox tb = stb.tb;
					double[] tbLowerLine = getLowerLine(tb);
					double rrTopX = (xPositions.get(stb.x) - EVENT_WIDTH / 2) / 10.0;
					for (SubLine sl : sublines) {
						if (sl.wl == stb.wl && sl.name.equals(stb.name)) {
							double rrTopY = (sl.line.y - MIN_EVENT_HEIGHT / 2.0) / 10.0;
							double yVertBottom = (rrTopY - (GROUP_GAP_PART_TWO * 
								LINE_WIDTH_PART_TWO) / 20.0);
							Tag tag = new Tag(rrTopX,rrTopY,tbLowerLine[0],yVertBottom,
								tbLowerLine[0],tbLowerLine[1],tbLowerLine[2],tbLowerLine[3]);
							shapes.add(new ShapeID(tag.toString(),ShapeID.TAG_TYPE,
								(int) tbLowerLine[3]));
							break;
						}
					}
				}
			}
			for (SubTextBox stb : majorText.get(i * 2 + 1)) {
				if (stb.tb.message.length() > 1) {
					TextBox tb = stb.tb;
					double[] tbLowerLine = getLowerLine(tb);
					double rrBottomX = (xPositions.get(stb.x) - EVENT_WIDTH / 2) / 10.0;
					for (SubLine sl : sublines) {
						if (sl.wl == stb.wl && sl.name.equals(stb.name)) {
							double rrBottomY = (sl.line.y + MIN_EVENT_HEIGHT / 2.0) / 10.0;
							double yVertTop = (rrBottomY + (GROUP_GAP_PART_TWO * 
								LINE_WIDTH_PART_TWO) / 20.0);
							Tag tag = new Tag(rrBottomX,rrBottomY,tbLowerLine[0],yVertTop,
								tbLowerLine[0],tbLowerLine[1],tbLowerLine[2],tbLowerLine[3]);
							shapes.add(new ShapeID(tag.toString(),ShapeID.TAG_TYPE,(int) yVertTop));
							break;
						}
					}
				}
			}
		}
		for (SubCurvedLine scl : subclines) {
			scl.line.x = xPositions.get(scl.line.x);
			scl.line.xgap = EVENT_GAP_PART_TWO;
			for (SubLine sl : sublines) {
				if (scl.wl == sl.wl && sl.name.equals(scl.nameStart)) {
					scl.line.y = sl.line.y;
					break;
				}
			}
			for (SubLine sl : sublines) {
				if (scl.wl == sl.wl && sl.name.equals(scl.nameEnd)) {
					scl.line.ygap = sl.line.y - scl.line.y;
					break;
				}
			}
			shapes.add(new ShapeID(scl.line.toString(),ShapeID.C_LINE_TYPE,
				(scl.line.ygap < 0 ? scl.line.y : -scl.line.y - scl.line.ygap)));
		}
		for (SubRoundedRectangle srr : subrrs) {
			srr.rect.x = xPositions.get(srr.rect.x) - EVENT_WIDTH + EVENT_LINE_WIDTH / 2;
			for (SubLine sl : sublines) {
				if (sl.wl == srr.wlStart && sl.name.equals(srr.nameStart)) {
					srr.rect.y = sl.line.y - (LINE_WIDTH_PART_TWO + MIN_EVENT_HEIGHT) / 2;
				}
				if (sl.wl == srr.wlEnd && sl.name.equals(srr.nameEnd)) {
					srr.rect.height = (sl.line.y + (LINE_WIDTH_PART_TWO + MIN_EVENT_HEIGHT) / 2 - 
						srr.rect.y);
					break;
				}
			}
			shapes.add(new ShapeID(srr.rect.toString(),ShapeID.RR_TYPE,srr.rect.y));
		}
		for (SubVerticalArrow svr: subarrows) {
			svr.arrow.x = xPositions.get(svr.arrow.x) - EVENT_WIDTH / 2;
			for (SubLine sl : sublines) {
				if (sl.wl == svr.wlStart && sl.name.equals(svr.nameStart)) {
					svr.arrow.y = sl.line.y + (LINE_WIDTH_PART_TWO + MIN_EVENT_HEIGHT + 
						EVENT_LINE_WIDTH) / 2;
				}
				if (sl.wl == svr.wlEnd && sl.name.equals(svr.nameEnd)) {
					svr.arrow.length = (sl.line.y - (LINE_WIDTH_PART_TWO + MIN_EVENT_HEIGHT + 
						EVENT_LINE_WIDTH) / 2) - svr.arrow.y;
					break;
				}
			}
			shapes.add(new ShapeID(svr.arrow.toString(),ShapeID.VA_TYPE,svr.arrow.y));
		}
		for (SubReturnZ srz : subrzs) {
			int xind = srz.returnZ.x;
			int xindGap = srz.returnZ.xgap;
			srz.returnZ.x = xPositions.get(xind);
			srz.returnZ.xgap = xPositions.get(xind + xindGap) - srz.returnZ.x;
			if (srz.returnZ.hasArrow) {
				srz.returnZ.x += EVENT_WIDTH / 2;
				srz.returnZ.xgap -= EVENT_WIDTH;
			}
			else {
				srz.returnZ.x -= EVENT_WIDTH / 2;
			}
			for (SubLine sl : sublines) {
				if (sl.wl == srz.wl && sl.name.equals(srz.nameStart)) {
					srz.returnZ.y = sl.line.y;
				}
				if (sl.wl == srz.wl && sl.name.equals(srz.nameEnd)) {
					srz.returnZ.ygap = sl.line.y - srz.returnZ.y;
				}
			}
			shapes.add(new ShapeID(srz.returnZ.toString(),ShapeID.RZ_TYPE,srz.returnZ.y));
		}
		for (SubTextBox stb : majorTextEnd) {
			if (stb.tb.message.length() > 1) {
				stb.tb.x = (xPositions.get(maxmajorx.get(starts.indexOf(stb.wl))) / 10.0 + 
					PART_TWO_A_TEXT_WHITESPACE);
				for (SubLine sl : sublines) {
					if (sl.wl == stb.wl && sl.name.equals(stb.name)) {
						stb.tb.y = (sl.line.y - LINE_WIDTH_PART_TWO / 2.0) / 10.0 - stb.tb.height;
						double tagy = sl.line.y / 10.0;
						Tag tag = new Tag(xPositions.get(stb.x) / 10.0,tagy,stb.tb.x + stb.tb.width,
							tagy);
						shapes.add(new ShapeID(tag.toString(),ShapeID.TAG_TYPE,
							(int) tag.values[1]));
						break;
					}
				}
				shapes.add(new ShapeID(stb.tb.toString(),ShapeID.TB_TYPE,(int) stb.tb.y));
			}
		}
		for (int i=0; i<starts.size(); i++) {
			currenty += (GROUP_GAP_PART_TWO + 1) * LINE_WIDTH_PART_TWO;
			LinkedList<SubLine> sublinesi = minorsublines.get(i);
			ArrayList<SubCurvedLine> subclinesi = minorsubclines.get(i);
			ArrayList<SubRoundedRectangle> subrrsi = minorsubrrs.get(i);
			ArrayList<SubVerticalArrow> subarrowsi = minorsubarrows.get(i);
			ArrayList<SubReturnZ> subrzsi = minorsubrzs.get(i);
			ArrayList<SubTextBox> subTextAbove = minorTextAbove.get(i);
			ArrayList<SubTextBox> subTextBelow = minorTextBelow.get(i);
			ArrayList<SubTextBox> subTextEnd = minorTextEnd.get(i);
			xPositions = new ArrayList<>();
			double minTXA = START_X / 10.0;
			double minTXB = START_X / 10.0;
			double maxTHeightA = 0;
			double maxTHeightB = 0;
			int textAbovePos = 0;
			int textBelowPos = 0;
			currentx = START_X;
			xPositions.add(currentx);
			for (int j=1; j<=(minorx.get(i) / 2); j++) {
				boolean hasTextAbove = (textAbovePos < subTextAbove.size() && 
					subTextAbove.get(textAbovePos).tb.x == j * 2);
				boolean hasTextBelow = (textBelowPos < subTextBelow.size() && 
					subTextBelow.get(textBelowPos).tb.x == j * 2);
				currentx = currentx + EVENT_GAP_PART_TWO + EVENT_WIDTH;
				if (hasTextAbove) {
					currentx = Math.max(currentx,actualToUsable(minTXA) + EVENT_WIDTH);
				}
				if (hasTextBelow) {
					currentx = Math.max(currentx,actualToUsable(minTXB) + EVENT_WIDTH);
				}
				xPositions.add(currentx - EVENT_GAP_PART_TWO);
				xPositions.add(currentx);
				int alreadyAbove = 0;
				int alreadyBelow = 0;
				double belowXAdd = 0;
				double belowYAdd = 0;
				while (textAbovePos < subTextAbove.size() && 
					subTextAbove.get(textAbovePos).tb.x == j * 2) {
					TextBox tb = subTextAbove.get(textAbovePos).tb;
					minTXA = Math.max((currentx - EVENT_WIDTH) / 10.0,minTXA);
					if (alreadyAbove == 0) {
						minTXA += (tb.height - TEXTBOX_HEIGHT) * sinA;
						tb.x = minTXA;
					}
					else {
						TextBox prevTB = subTextAbove.get(textAbovePos - 1).tb;
						minTXA += ((prevTB.height * sinA) + 
							(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT * sinA) + 
							((tb.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
							TEXTBOX_HEIGHT)) * cosA / tanA));
						tb.x = minTXA;
					}
					tb.y = -tb.height * cosA;
					rotate(tb);
					maxTHeightA = Math.max(maxTHeightA,getActualHeight(tb));
					if (alreadyAbove > 0) {
						for (int k=0; k<alreadyAbove; k++) {
							TextBox toShift = subTextAbove.get(textAbovePos - 1 - k).tb;
							toShift.x += (tb.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
								TEXTBOX_HEIGHT)) * cosA / tanA;
							toShift.y -= (tb.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
								TEXTBOX_HEIGHT)) * cosA;
							maxTHeightA = Math.max(maxTHeightA,getActualHeight(toShift));
						}
					}
					alreadyAbove++;
					textAbovePos++;
				}
				while (textBelowPos < subTextBelow.size() && 
					subTextBelow.get(textBelowPos).tb.x == j * 2) {
					TextBox tb = subTextBelow.get(textBelowPos).tb;
					minTXB = Math.max((currentx - EVENT_WIDTH) / 10.0,minTXB);
					tb.x = minTXB + tb.height * sinA + belowXAdd;
					tb.y = belowYAdd;
					boolean isLast = (textBelowPos == subTextBelow.size() - 1 || 
						subTextBelow.get(textBelowPos + 1).tb.x != j * 2);
					if (isLast) {
						minTXB += (tb.height - TEXTBOX_HEIGHT) * sinA;
						tb.x = minTXB + tb.height * sinA + belowXAdd;
					}
					rotate(tb);
					maxTHeightB = Math.max(maxTHeightB,getActualHeight(tb));
					if (alreadyBelow > 0) {
						for (int k=0; k<alreadyBelow; k++) {
							TextBox toShift = subTextBelow.get(textBelowPos - 1 - k).tb;
							if (isLast) {
								toShift.x += (tb.height - TEXTBOX_HEIGHT) * sinA;
							}
							toShift.x += ((tb.height * sinA) + 
								(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT * sinA) + 
								((subTextBelow.get(textBelowPos - 1).tb.height + 
								(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT)) * cosA / 
								tanA));
						}
					}
					alreadyBelow++;
					belowXAdd += (tb.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
						TEXTBOX_HEIGHT)) * cosA / tanA;
					belowYAdd += (tb.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
						TEXTBOX_HEIGHT)) * cosA;
					textBelowPos++;
				}
				if (alreadyAbove > 0) {
					minTXA += ((subTextAbove.get(textAbovePos - 1).tb.height * sinA) + 
						(TEXTBOX_HEIGHT * cosA / tanA) + (TEXTBOX_INTRA_SPACE_GAP_MULTIPLE * 
						TEXTBOX_HEIGHT / sinA));
				}
				if (alreadyBelow > 0) {
					minTXB += ((subTextBelow.get(textBelowPos - 1).tb.height * sinA) + 
						(TEXTBOX_HEIGHT * cosA / tanA) + (TEXTBOX_INTRA_SPACE_GAP_MULTIPLE * 
						TEXTBOX_HEIGHT / sinA));
					for (int k=1; k<alreadyBelow; k++) {
						TextBox tbc = subTextBelow.get(textBelowPos - 1 - k).tb;
						TextBox tbn = subTextBelow.get(textBelowPos - k).tb;
						minTXB += (tbn.height * sinA) + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
							TEXTBOX_HEIGHT * sinA) + ((tbc.height + 
							(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT)) * cosA / 
							tanA);
					}
				}
			}
			for (SubTextBox stb : subTextAbove) {
				if (stb.tb.message.length() > 1) {
					stb.tb.y += (currenty + totalYAdd) / 10.0 + maxTHeightA;
					shapes.add(new ShapeID(stb.tb.toString(),ShapeID.TB_TYPE,(int) stb.tb.y));
				}
			}
			totalYAdd += actualToUsable(maxTHeightA + PART_TWO_B_TEXT_WHITESPACE);
			for (SubLine sl : sublinesi) {
				int xind = sl.line.x;
				int xindGap = sl.line.length;
				sl.line.x = xPositions.get(xind);
				sl.line.length = xPositions.get(xind + xindGap) - sl.line.x;
				sl.line.y = currenty + totalYAdd;
				currenty += (GROUP_GAP_PART_TWO + 1) * LINE_WIDTH_PART_TWO;
				shapes.add(new ShapeID(sl.line.toString(),ShapeID.H_LINE_TYPE,sl.line.y));
			}
			for (SubTextBox stb : subTextAbove) {
				if (stb.tb.message.length() > 1) {
					TextBox tb = stb.tb;
					double[] tbLowerLine = getLowerLine(tb);
					double rrTopX = (xPositions.get(stb.x) - EVENT_WIDTH / 2) / 10.0;
					for (SubLine sl : sublinesi) {
						if (sl.wl == stb.wl && sl.name.equals(stb.name)) {
							double rrTopY = (sl.line.y - MIN_EVENT_HEIGHT / 2.0) / 10.0;
							double yVertBottom = (rrTopY - (GROUP_GAP_PART_TWO * 
								LINE_WIDTH_PART_TWO) * (2.0/3.0) / 10.0);
							double aboveTagX = Math.min(tbLowerLine[0],
								rrTopX + (GROUP_GAP_PART_TWO * LINE_WIDTH_PART_TWO) * (2.0/3.0) / 
								10.0);
							Tag tag = new Tag(rrTopX,rrTopY,aboveTagX,yVertBottom,tbLowerLine[0],
								yVertBottom,tbLowerLine[0],tbLowerLine[1],tbLowerLine[2],
								tbLowerLine[3]);
							shapes.add(new ShapeID(tag.toString(),ShapeID.TAG_TYPE,
								(int) tbLowerLine[3]));
							break;
						}
					}
				}
			}
			for (SubCurvedLine scl : subclinesi) {
				scl.line.x = xPositions.get(scl.line.x);
				scl.line.xgap = EVENT_GAP_PART_TWO;
				for (SubLine sl : sublinesi) {
					if (scl.wl == sl.wl && sl.name.equals(scl.nameStart)) {
						scl.line.y = sl.line.y;
						break;
					}
				}
				for (SubLine sl : sublinesi) {
					if (scl.wl == sl.wl && sl.name.equals(scl.nameEnd)) {
						scl.line.ygap = sl.line.y - scl.line.y;
						break;
					}
				}
				shapes.add(new ShapeID(scl.line.toString(),ShapeID.C_LINE_TYPE,
					(scl.line.ygap < 0 ? scl.line.y : -scl.line.y - scl.line.ygap)));
			}
			for (SubRoundedRectangle srr : subrrsi) {
				srr.rect.x = xPositions.get(srr.rect.x) - EVENT_WIDTH + EVENT_LINE_WIDTH / 2;
				for (SubLine sl : sublinesi) {
					if (sl.wl == srr.wlStart && sl.name.equals(srr.nameStart)) {
						srr.rect.y = sl.line.y - (LINE_WIDTH_PART_TWO + MIN_EVENT_HEIGHT) / 2;
					}
					if (sl.wl == srr.wlEnd && sl.name.equals(srr.nameEnd)) {
						srr.rect.height = (sl.line.y + (LINE_WIDTH_PART_TWO + MIN_EVENT_HEIGHT) / 
							2 - srr.rect.y);
						break;
					}
				}
				shapes.add(new ShapeID(srr.rect.toString(),ShapeID.RR_TYPE,srr.rect.y));
			}
			for (SubVerticalArrow svr: subarrowsi) {
				svr.arrow.x = xPositions.get(svr.arrow.x) - EVENT_WIDTH / 2;
				for (SubLine sl : sublinesi) {
					if (sl.wl == svr.wlStart && sl.name.equals(svr.nameStart)) {
						svr.arrow.y = sl.line.y + (LINE_WIDTH_PART_TWO + MIN_EVENT_HEIGHT + 
							EVENT_LINE_WIDTH) / 2;
					}
					if (sl.wl == svr.wlEnd && sl.name.equals(svr.nameEnd)) {
						svr.arrow.length = (sl.line.y - (LINE_WIDTH_PART_TWO + MIN_EVENT_HEIGHT + 
							EVENT_LINE_WIDTH) / 2) - svr.arrow.y;
						break;
					}
				}
				shapes.add(new ShapeID(svr.arrow.toString(),ShapeID.VA_TYPE,svr.arrow.y));
			}
			for (SubReturnZ srz : subrzsi) {
				int xind = srz.returnZ.x;
				int xindGap = srz.returnZ.xgap;
				srz.returnZ.x = xPositions.get(xind);
				srz.returnZ.xgap = xPositions.get(xind + xindGap) - srz.returnZ.x;
				if (srz.returnZ.hasArrow) {
					srz.returnZ.x += EVENT_WIDTH / 2;
					srz.returnZ.xgap -= EVENT_WIDTH;
				}
				else {
					srz.returnZ.x -= EVENT_WIDTH / 2;
				}
				for (SubLine sl : sublinesi) {
					if (sl.wl == srz.wl && sl.name.equals(srz.nameStart)) {
						srz.returnZ.y = sl.line.y;
					}
					if (sl.wl == srz.wl && sl.name.equals(srz.nameEnd)) {
						srz.returnZ.ygap = sl.line.y - srz.returnZ.y;
					}
				}
				shapes.add(new ShapeID(srz.returnZ.toString(),ShapeID.RZ_TYPE,srz.returnZ.y));
			}
			for (SubTextBox stb : subTextEnd) {
				if (stb.tb.message.length() > 1) {
					stb.tb.x = currentx / 10.0 + PART_TWO_B_TEXT_WHITESPACE;
					for (SubLine sl : sublinesi) {
						if (sl.wl == stb.wl && sl.name.equals(stb.name)) {
							stb.tb.y = ((sl.line.y - LINE_WIDTH_PART_TWO / 2.0) / 10.0 - 
								stb.tb.height);
							double tagy = sl.line.y / 10.0;
							Tag tag = new Tag(xPositions.get(stb.x) / 10.0,tagy,
								stb.tb.x + stb.tb.width,tagy);
							shapes.add(new ShapeID(tag.toString(),ShapeID.TAG_TYPE,
								(int) tag.values[1]));
							break;
						}
					}
					shapes.add(new ShapeID(stb.tb.toString(),ShapeID.TB_TYPE,(int) stb.tb.y));
				}
			}
			currenty -= (GROUP_GAP_PART_TWO + 1) * LINE_WIDTH_PART_TWO;
			for (SubTextBox stb : subTextBelow) {
				if (stb.tb.message.length() > 1) {
					stb.tb.y += (currenty + totalYAdd) / 10.0 + PART_TWO_B_TEXT_WHITESPACE;
					shapes.add(new ShapeID(stb.tb.toString(),ShapeID.TB_TYPE,(int) stb.tb.y));
					TextBox tb = stb.tb;
					double[] tbLowerLine = getLowerLine(tb);
					double rrBottomX = (xPositions.get(stb.x) - EVENT_WIDTH / 2) / 10.0;
					for (SubLine sl : sublinesi) {
						if (sl.wl == stb.wl && sl.name.equals(stb.name)) {
							double rrBottomY = (sl.line.y + MIN_EVENT_HEIGHT / 2.0) / 10.0;
							double yVertTop = rrBottomY + (GROUP_GAP_PART_TWO * 
								LINE_WIDTH_PART_TWO) * (1.0/3.0) / 10.0;
							double belowTagX = Math.min(tbLowerLine[0],
								rrBottomX + (GROUP_GAP_PART_TWO * LINE_WIDTH_PART_TWO) * (1.0/3.0) / 
								10.0);
							Tag tag = new Tag(rrBottomX,rrBottomY,belowTagX,yVertTop,tbLowerLine[0],
								yVertTop,tbLowerLine[0],tbLowerLine[1],tbLowerLine[2],
								tbLowerLine[3]);
							shapes.add(new ShapeID(tag.toString(),ShapeID.TAG_TYPE,(int) yVertTop));
							break;
						}
					}
				}
			}
			totalYAdd += actualToUsable(maxTHeightB + PART_TWO_B_TEXT_WHITESPACE);
		}
		Collections.sort(shapes);
		for (ShapeID s : shapes) {
			System.out.printf("\t%s\n",s.string);
		}
		return currenty + totalYAdd + GROUP_GAP_PART_TWO * LINE_WIDTH_PART_TWO;
	}

	private void formatThird(int starty) {
		ArrayList<ShapeID> shapes = new ArrayList<>();
		int totalYAdd = 0;
		double sinA = Math.sin(TEXTBOX_ROTATION * Math.PI / 180.0);
		double cosA = Math.cos(TEXTBOX_ROTATION * Math.PI / 180.0);
		double tanA = Math.tan(TEXTBOX_ROTATION * Math.PI / 180.0);
		for (ArrayList<FreeEvent> freeList : frees) {
			ArrayList<HorizontalLine> hLinesToAdd = new ArrayList<>();
			ArrayList<CurvedLine> cLinesToAdd = new ArrayList<>();
			ArrayList<RoundedRectangle> rrToAdd = new ArrayList<>();
			ArrayList<VerticalArrow> vaToAdd = new ArrayList<>();
			ArrayList<TextBox> textAbove = new ArrayList<>();
			ArrayList<TextBox> textBelow = new ArrayList<>();
			ArrayList<RoundedRectangle> rrsAbove = new ArrayList<>();
			ArrayList<RoundedRectangle> rrsBelow = new ArrayList<>();
			int[] currentWL = new int[0];
			int[] currentPositions = new int[0];
			HorizontalLine[] currentHLines = new HorizontalLine[0];
			int currentx = START_X - EVENT_GAP_PART_THREE - EVENT_WIDTH;
			int currentTopP = 0;
			int currentBottomP = 0;
			int highestTop = 0;
			int lowestBottom = 0;
			double minTXA = currentx / 10.0;
			double minTXB = currentx / 10.0;
			double maxTHeightA = 0;
			double maxTHeightB = 0;
			for (FreeEvent fe : freeList) {
				currentx += EVENT_GAP_PART_THREE + EVENT_WIDTH;
				boolean hasTextAbove = false;
				boolean hasTextBelow = false;
				if (fe.shift) {
					hasTextAbove = true;
				}
				else {
					for (int i=0; i<fe.numEvents; i++) {
						int numAbove = fe.firstIndexes[i];
						int numBelow = fe.numWL - (fe.firstIndexes[i] + fe.sizes[i]);
						if (numAbove < numBelow) {
							hasTextAbove = true;
						}
						else {
							hasTextBelow = true;
						}
					}
				}
				if (hasTextAbove) {
					currentx = Math.max(currentx,actualToUsable(minTXA) + EVENT_WIDTH);
				}
				if (hasTextBelow) {
					currentx = Math.max(currentx,actualToUsable(minTXB) + EVENT_WIDTH);
				}
				int[] nextWL = fe.wLines;
				int[] basePositions = new int[fe.numWL];
				if (fe.shift) {
					int currentPI = 0;
					for (int i=0; i<fe.sizes[0] - 2; i++) {
						basePositions[i] = currentPI;
						currentPI++;
					}
					if (currentPI > 0) {
						currentPI += 2;
					}
					for (int i=0; i<2; i++) {
						basePositions[fe.sizes[0] - 2 + i] = currentPI;
						currentPI += SHIFT_GAP_PART_THREE + 1;
					}
					for (int i=fe.sizes[0]; i<fe.numWL; i++) {
						basePositions[i] = currentPI;
						currentPI++;
					}
				}
				else {
					int currentPI = 0;
					int currentLI = 0;
					for (int i=0; i<fe.numEvents; i++) {
						int count = 0;
						while (currentLI < fe.firstIndexes[i]) {
							basePositions[currentLI] = currentPI;
							currentPI++;
							currentLI++;
							count++;
						}
						if (count > 0) {
							currentPI += GROUP_GAP_PART_THREE + 1;
						}
						for (int j=0; j<fe.sizes[i]; j++) {
							basePositions[currentLI] = currentPI;
							currentPI++;
							currentLI++;
						}
						currentPI += GROUP_GAP_PART_THREE + 1;
					}
					while (currentLI < fe.numWL) {
						basePositions[currentLI] = currentPI;
						currentPI++;
						currentLI++;
					}
				}
				int pHeight = basePositions[fe.numWL - 1] + 1;
				ArrayList<Positioning> possibles = new ArrayList<>();
				for (int i=currentTopP - (pHeight - 1); i<=currentBottomP; i++) {
					int[] positions = new int[fe.numWL];
					int numChanges = 0;
					int middleness = (int) Math.abs(i);
					int roof = i;
					for (int j=0; j<fe.numWL; j++) {
						positions[j] = basePositions[j] + i;
						for (int k=0; k<currentWL.length; k++) {
							if (nextWL[j] == currentWL[k]) {
								if (currentPositions[k] != positions[j]) {
									numChanges++;
								}
								break;
							}
						}
					}
					possibles.add(new Positioning(positions,numChanges,middleness,roof));
				}
				Collections.sort(possibles);
				int[] bestPositions = possibles.get(0).positions;
				HorizontalLine[] nextHLines = new HorizontalLine[fe.numWL];
				for (int i=0; i<fe.numWL; i++) {
					boolean found = false;
					highestTop = Math.min(highestTop,bestPositions[i]);
					lowestBottom = Math.max(lowestBottom,bestPositions[i]);
					for (int j=0; j<currentWL.length; j++) {
						if (nextWL[i] == currentWL[j]) {
							if (currentPositions[j] == bestPositions[i]) {
								nextHLines[i] = currentHLines[j];
							}
							else {
								CurvedLine cl = new CurvedLine(currentHLines[j].x + 
									currentHLines[j].length,currentHLines[j].y,EVENT_GAP_PART_THREE,
									(bestPositions[i] - currentPositions[j]) * 
									LINE_WIDTH_PART_THREE,lineNames.get(nextWL[i]),
									LINE_WIDTH_PART_THREE);
								nextHLines[i] = new HorizontalLine(cl.x + EVENT_GAP_PART_THREE,
									LINE_WIDTH_PART_THREE * bestPositions[i],0,
									lineNames.get(nextWL[i]),LINE_WIDTH_PART_THREE);
								hLinesToAdd.add(nextHLines[i]);
								cLinesToAdd.add(cl);
							}
							found = true;
							break;
						}
					}
					if (!found) {
						nextHLines[i] = new HorizontalLine(currentx - EVENT_WIDTH,
							LINE_WIDTH_PART_THREE * bestPositions[i],0,lineNames.get(nextWL[i]),
							LINE_WIDTH_PART_THREE);
						hLinesToAdd.add(nextHLines[i]);
					}
					nextHLines[i].length = currentx - nextHLines[i].x;
				}
				if (fe.shift) {
					if (fe.sizes[0] > 2) {
						RoundedRectangle rr = new RoundedRectangle(
							currentx - EVENT_WIDTH + EVENT_LINE_WIDTH / 2,
							nextHLines[0].y - (LINE_WIDTH_PART_THREE + MIN_EVENT_HEIGHT) / 2,
							(fe.sizes[0] - 2) * LINE_WIDTH_PART_THREE + MIN_EVENT_HEIGHT);
						rrToAdd.add(rr);
						rrsAbove.add(rr);
					}
					for (int i=0; i<2; i++) {
						RoundedRectangle rr = new RoundedRectangle(
							currentx - EVENT_WIDTH + EVENT_LINE_WIDTH / 2,
							nextHLines[fe.sizes[0] - 2 + i].y - (LINE_WIDTH_PART_THREE + 
							MIN_EVENT_HEIGHT) / 2,LINE_WIDTH_PART_THREE + MIN_EVENT_HEIGHT);
						rrToAdd.add(rr);
						if (i == 0 && fe.sizes[0] == 2) {
							rrsAbove.add(rr);
						}
					}
					int atop = nextHLines[fe.sizes[0] - 2].y + (LINE_WIDTH_PART_THREE + 
						MIN_EVENT_HEIGHT + EVENT_LINE_WIDTH) / 2;
					int abot = nextHLines[fe.sizes[0] - 1].y - (LINE_WIDTH_PART_THREE + 
						MIN_EVENT_HEIGHT + EVENT_LINE_WIDTH) / 2;
					vaToAdd.add(new VerticalArrow(currentx - EVENT_WIDTH / 2,atop,abot - atop));
					String text = fe.text[0];
					if (text.length() > 1) {
						minTXA = Math.max((currentx - EVENT_WIDTH) / 10.0,minTXA);
						TextBox tbToAdd = new TextBox(minTXA,0,text,-TEXTBOX_ROTATION,
							PART_THREE_MAX_TEXTBOX_WIDTH);
						minTXA += (tbToAdd.height - TEXTBOX_HEIGHT) * sinA;
						tbToAdd.x = minTXA;
						tbToAdd.y = -tbToAdd.height * cosA;
						rotate(tbToAdd);
						maxTHeightA = Math.max(maxTHeightA,getActualHeight(tbToAdd));
						textAbove.add(tbToAdd);
						minTXA += ((tbToAdd.height * sinA) + (TEXTBOX_HEIGHT * cosA / tanA) + 
							(TEXTBOX_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT / sinA));
					}
				}
				else {
					int alreadyAbove = 0;
					int alreadyBelow = 0;
					double belowXAdd = 0;
					double belowYAdd = 0;
					for (int i=0; i<fe.numEvents; i++) {
						RoundedRectangle rr = new RoundedRectangle(
							currentx - EVENT_WIDTH + EVENT_LINE_WIDTH / 2,
							nextHLines[fe.firstIndexes[i]].y - (LINE_WIDTH_PART_THREE + 
							MIN_EVENT_HEIGHT) / 2,
							fe.sizes[i] * LINE_WIDTH_PART_THREE + MIN_EVENT_HEIGHT);
						rrToAdd.add(rr);
						String text = fe.text[i];
						if (text.length() > 1) {
							int numAbove = fe.firstIndexes[i];
							int numBelow = fe.numWL - (fe.firstIndexes[i] + fe.sizes[i]);
							if (numAbove < numBelow) {
								minTXA = Math.max((currentx - EVENT_WIDTH) / 10.0,minTXA);
								TextBox tbToAdd = new TextBox(minTXA,0,text,-TEXTBOX_ROTATION,
									PART_THREE_MAX_TEXTBOX_WIDTH);
								if (alreadyAbove == 0) {
									minTXA += (tbToAdd.height - TEXTBOX_HEIGHT) * sinA;
									tbToAdd.x = minTXA;
								}
								else {
									TextBox prevTB = textAbove.get(textAbove.size() - 1);
									minTXA += ((prevTB.height * sinA) + 
										(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT * 
										sinA) + ((tbToAdd.height + 
										(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
										TEXTBOX_HEIGHT)) * cosA / tanA));
									tbToAdd.x = minTXA;
								}
								tbToAdd.y = -tbToAdd.height * cosA;
								rotate(tbToAdd);
								maxTHeightA = Math.max(maxTHeightA,getActualHeight(tbToAdd));
								if (alreadyAbove > 0) {
									for (int k=0; k<alreadyAbove; k++) {
										TextBox toShift = textAbove.get(textAbove.size() - 1 - k);
										toShift.x += (tbToAdd.height + 
											(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
											TEXTBOX_HEIGHT)) * cosA / tanA;
										toShift.y -= (tbToAdd.height + 
											(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
											TEXTBOX_HEIGHT)) * cosA;
										maxTHeightA = Math.max(maxTHeightA,
											getActualHeight(toShift));
									}
								}
								textAbove.add(tbToAdd);
								rrsAbove.add(rr);
								alreadyAbove++;
							}
							else {
								minTXB = Math.max((currentx - EVENT_WIDTH) / 10.0,minTXB);
								TextBox tbToAdd = new TextBox(minTXB,0,text,TEXTBOX_ROTATION,
									PART_THREE_MAX_TEXTBOX_WIDTH);
								tbToAdd.x = minTXB + tbToAdd.height * sinA + belowXAdd;
								tbToAdd.y = belowYAdd;
								if (i == fe.numEvents - 1) {
									minTXB += (tbToAdd.height - TEXTBOX_HEIGHT) * sinA;
									tbToAdd.x = minTXB + tbToAdd.height * sinA + belowXAdd;
								}
								rotate(tbToAdd);
								maxTHeightB = Math.max(maxTHeightB,getActualHeight(tbToAdd));
								textBelow.add(tbToAdd);
								rrsBelow.add(rr);
								if (alreadyBelow > 0) {
									for (int k=0; k<alreadyBelow; k++) {
										TextBox toShift = textBelow.get(textBelow.size() - 2 - k);
										if (i == fe.numEvents - 1) {
											toShift.x += (tbToAdd.height - TEXTBOX_HEIGHT) * sinA;
										}
										toShift.x += ((tbToAdd.height * sinA) + 
											(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
											TEXTBOX_HEIGHT * sinA) + ((textBelow.get(
											textBelow.size() - 2).height + 
											(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
											TEXTBOX_HEIGHT)) * cosA / tanA));
									}
								}
								alreadyBelow++;
								belowXAdd += ((tbToAdd.height + 
									(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT)) * 
									cosA / tanA);
								belowYAdd += ((tbToAdd.height + 
									(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT)) * 
									cosA);
							}
						}
					}
					if (alreadyAbove > 0) {
						minTXA += ((textAbove.get(textAbove.size() - 1).height * sinA) + 
							(TEXTBOX_HEIGHT * cosA / tanA) + (TEXTBOX_INTRA_SPACE_GAP_MULTIPLE * 
							TEXTBOX_HEIGHT / sinA));
					}
					if (alreadyBelow > 0) {
						minTXB += ((textBelow.get(textBelow.size() - 1).height * sinA) + 
							(TEXTBOX_HEIGHT * cosA / tanA) + (TEXTBOX_INTRA_SPACE_GAP_MULTIPLE * 
							TEXTBOX_HEIGHT / sinA));
						for (int k=1; k<alreadyBelow; k++) {
							TextBox tbc = textBelow.get(textBelow.size() - 1 - k);
							TextBox tbn = textBelow.get(textBelow.size() - k);
							minTXB += ((tbn.height * sinA) + 
								(TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * TEXTBOX_HEIGHT * sinA) + 
								((tbc.height + (TEXTBOX_SMALL_INTRA_SPACE_GAP_MULTIPLE * 
								TEXTBOX_HEIGHT)) * cosA / tanA));
						}
					}
				}
				currentWL = nextWL;
				currentPositions = bestPositions;
				currentHLines = nextHLines;
				currentTopP = bestPositions[0];
				currentBottomP = bestPositions[fe.numWL - 1];
			}
			for (int i=0; i<textAbove.size(); i++) {
				TextBox tb = textAbove.get(i);
				RoundedRectangle rr = rrsAbove.get(i);
				double rrTopX = (rr.x + (EVENT_WIDTH - EVENT_LINE_WIDTH) / 2) / 10.0;
				double rrTopY = ((rr.y + starty + totalYAdd - LINE_WIDTH_PART_THREE * highestTop + 
					actualToUsable(maxTHeightA + PART_THREE_TEXT_WHITESPACE) - EVENT_LINE_WIDTH / 
					2) / 10.0);
				tb.y += (starty + totalYAdd) / 10.0 + maxTHeightA;
				shapes.add(new ShapeID(tb.toString(),ShapeID.TB_TYPE,(int) tb.y));
				double[] tbLowerLine = getLowerLine(tb);
				double yVertBottom = ((starty + totalYAdd) / 10.0 + maxTHeightA + 
					PART_THREE_TEXT_WHITESPACE - TAG_GAP_PART_THREE);
				Tag tag = new Tag(rrTopX,rrTopY,tbLowerLine[0],yVertBottom,tbLowerLine[0],
					tbLowerLine[1],tbLowerLine[2],tbLowerLine[3]);
				shapes.add(new ShapeID(tag.toString(),ShapeID.TAG_TYPE,(int) tbLowerLine[3]));
			}
			totalYAdd += actualToUsable(maxTHeightA + PART_THREE_TEXT_WHITESPACE);
			for (HorizontalLine hl : hLinesToAdd) {
				hl.y += starty + totalYAdd - LINE_WIDTH_PART_THREE * highestTop;
				shapes.add(new ShapeID(hl.toString(),ShapeID.H_LINE_TYPE,hl.y));
			}
			for (CurvedLine cl : cLinesToAdd) {
				cl.y += starty + totalYAdd - LINE_WIDTH_PART_THREE * highestTop;
				shapes.add(new ShapeID(cl.toString(),ShapeID.C_LINE_TYPE,
					(cl.ygap < 0 ? cl.y : -cl.y - cl.ygap)));
			}
			for (RoundedRectangle rr : rrToAdd) {
				rr.y += starty + totalYAdd - LINE_WIDTH_PART_THREE * highestTop;
				shapes.add(new ShapeID(rr.toString(),ShapeID.RR_TYPE,rr.y));
			}
			for (VerticalArrow arrow : vaToAdd) {
				arrow.y += starty + totalYAdd - LINE_WIDTH_PART_THREE * highestTop;
				shapes.add(new ShapeID(arrow.toString(),ShapeID.VA_TYPE,arrow.y));
			}
			totalYAdd += LINE_WIDTH_PART_THREE * (lowestBottom - highestTop + 1);
			for (int i=0; i<textBelow.size(); i++) {
				TextBox tb = textBelow.get(i);
				RoundedRectangle rr = rrsBelow.get(i);
				double rrBottomX = (rr.x + (EVENT_WIDTH - EVENT_LINE_WIDTH) / 2) / 10.0;
				double rrBottomY = (rr.y + rr.height + EVENT_LINE_WIDTH / 2) / 10.0;
				tb.y += (starty + totalYAdd) / 10.0 + PART_THREE_TEXT_WHITESPACE;
				shapes.add(new ShapeID(tb.toString(),ShapeID.TB_TYPE,(int) tb.y));
				double[] tbLowerLine = getLowerLine(tb);
				double yVertTop = (starty + totalYAdd) / 10.0 + TAG_GAP_PART_THREE;
				Tag tag = new Tag(rrBottomX,rrBottomY,tbLowerLine[0],yVertTop,tbLowerLine[0],
					tbLowerLine[1],tbLowerLine[2],tbLowerLine[3]);
				shapes.add(new ShapeID(tag.toString(),ShapeID.TAG_TYPE,(int) yVertTop));
			}
			totalYAdd += actualToUsable(maxTHeightB + PART_THREE_TEXT_WHITESPACE);
		}
		Collections.sort(shapes);
		for (ShapeID s : shapes) {
			System.out.printf("\t%s\n",s.string);
		}
	}

	private static String toDecimal(int i) {
		if (i < 0) {
			return String.format("-%s",toDecimal(-i));
		}
		return String.format("%d.%d",i / 10,i % 10);
	}

	private static String getSubLineName(String s) {
		int len = s.length();
		char c = s.charAt(len - 1);
		if (c >= '0' && c <= '9') {
			return (len == 1 ? "0" + s : s);
		}
		else {
			return (len == 2 ? "0" + s : s);
		}
	}

	private static double[] getTextWidth(String s, double maxLength) {
		int length = s.length();
		double currentWord = 0;
		double currentLine = 0;
		double maxLine = 0;
		double currentSpare = 0;
		int numLines = 1;
		for (int i=0; i<length; i++) {
			switch (s.charAt(i)) {
				case 'a': {
					currentWord += 0.187 * FONT_SIZE;
					break;
				}
				case 'b':
				case 'd':
				case 'p':
				case 'q': {
					currentWord += 0.207 * FONT_SIZE;
					break;
				}
				case 'c': {
					currentWord += 0.164 * FONT_SIZE;
					break;
				}
				case 'e': {
					currentWord += 0.189 * FONT_SIZE;
					break;
				}
				case 'f': {
					currentWord += 0.116 * FONT_SIZE;
					break;
				}
				case 'g':
				case 'S':
				case 'T': {
					currentWord += 0.184 * FONT_SIZE;
					break;
				}
				case 'h':
				case 'n':
				case 'u':
				case 'R': {
					currentWord += 0.208 * FONT_SIZE;
					break;
				}
				case 'i':
				case 'j':
				case 'l': {
					currentWord += 0.091 * FONT_SIZE;
					break;
				}
				case 'k': {
					currentWord += 0.175 * FONT_SIZE;
					break;
				}
				case 'm': {
					currentWord += 0.315 * FONT_SIZE;
					break;
				}
				case 'o':
				case 'K':
				case 'P': {
					currentWord += 0.205 * FONT_SIZE;
					break;
				}
				case 'r': {
					currentWord += 0.14 * FONT_SIZE;
					break;
				}
				case 's': {
					currentWord += 0.16 * FONT_SIZE;
					break;
				}
				case 't':
				case 'I': {
					currentWord += 0.12 * FONT_SIZE;
					break;
				}
				case 'v': {
					currentWord += 0.17 * FONT_SIZE;
					break;
				}
				case 'w': {
					currentWord += 0.263 * FONT_SIZE;
					break;
				}
				case 'x': {
					currentWord += 0.176 * FONT_SIZE;
					break;
				}
				case 'y': {
					currentWord += 0.172 * FONT_SIZE;
					break;
				}
				case 'z': {
					currentWord += 0.155 * FONT_SIZE;
					break;
				}
				case 'A': {
					currentWord += 0.215 * FONT_SIZE;
					break;
				}
				case 'B': {
					currentWord += 0.22 * FONT_SIZE;
					break;
				}
				case 'C': {
					currentWord += 0.212 * FONT_SIZE;
					break;
				}
				case 'D': {
					currentWord += 0.241 * FONT_SIZE;
					break;
				}
				case 'E':
				case 'Y': {
					currentWord += 0.186 * FONT_SIZE;
					break;
				}
				case 'F':
				case 'L': {
					currentWord += 0.174 * FONT_SIZE;
					break;
				}
				case 'G': {
					currentWord += 0.244 * FONT_SIZE;
					break;
				}
				case 'H': {
					currentWord += 0.249 * FONT_SIZE;
					break;
				}
				case 'J':
				case '.':
				case '!':
				case ':': {
					currentWord += 0.095 * FONT_SIZE;
					break;
				}
				case 'M': {
					currentWord += 0.307 * FONT_SIZE;
					break;
				}
				case 'N': {
					currentWord += 0.258 * FONT_SIZE;
					break;
				}
				case 'O':
				case 'Q': {
					currentWord += 0.262 * FONT_SIZE;
					break;
				}
				case 'U': {
					currentWord += 0.246 * FONT_SIZE;
					break;
				}
				case 'V': {
					currentWord += 0.199 * FONT_SIZE;
					break;
				}
				case 'W': {
					currentWord += 0.311 * FONT_SIZE;
					break;
				}
				case 'X': {
					currentWord += 0.193 * FONT_SIZE;
					break;
				}
				case 'Z': {
					currentWord += 0.19 * FONT_SIZE;
					break;
				}
				case '\'': {
					currentWord += 0.062 * FONT_SIZE;
					break;
				}
				case ' ': {
					if (currentLine + currentWord > maxLength) {
						if (currentLine == 0) {
							maxLine = Math.max(maxLine,currentWord);
							numLines++;
							currentWord = 0;
							currentSpare = 0;
						}
						else {
							maxLine = Math.max(maxLine,currentLine - currentSpare);
							numLines++;
							if (currentWord > maxLength) {
								maxLine = Math.max(maxLine,currentWord);
								numLines++;
								currentLine = 0;
								currentWord = 0;
								currentSpare = 0;
							}
							else {
								currentLine = currentWord;
								currentWord = 0;
								currentSpare = 0.093 * FONT_SIZE;
								currentLine += currentSpare;
							}
						}
					}
					else {
						currentSpare = 0.093 * FONT_SIZE;
						currentLine += currentWord + currentSpare;
						currentWord = 0;
					}
					break;
				}
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9': {
					currentWord += 0.195 * FONT_SIZE;
					break;
				}
				case '\"': {
					currentWord += 0.129 * FONT_SIZE;
					break;
				}
				case '-': {
					currentWord += 0.113 * FONT_SIZE;
					if (currentLine + currentWord > maxLength) {
						if (currentLine == 0) {
							maxLine = Math.max(maxLine,currentWord);
							numLines++;
							currentWord = 0;
							currentSpare = 0;
						}
						else {
							maxLine = Math.max(maxLine,currentLine - currentSpare);
							numLines++;
							if (currentWord > maxLength) {
								maxLine = Math.max(maxLine,currentWord);
								numLines++;
								currentLine = 0;
							}
							else {
								currentLine = currentWord;
							}
							currentWord = 0;
							currentSpare = 0;
						}
					}
					else {
						currentSpare = 0;
						currentLine += currentWord;
						currentWord = 0;
					}
					break;
				}
				case '(':
				case ')': {
					currentWord += 0.106 * FONT_SIZE;
					break;
				}
				case '\u266a':
				case '\u2606': {
					currentWord += 0.353 * FONT_SIZE;
					break;
				}
				case ',': {
					currentWord += 0.088 * FONT_SIZE;
					break;
				}
				case '\\': {
					currentWord += 0.132 * FONT_SIZE;
					break;
				}
				case '@': {
					currentWord += 0.306 * FONT_SIZE;
					break;
				}
				case '\u00b2': {
					currentWord += 0.117 * FONT_SIZE;
					break;
				}
				default: {
					throw new UnsupportedOperationException(String.format(
						"Unsupported character: %s %s","" + s.charAt(i),s));
				}
			}
		}
		if (currentLine + currentWord > maxLength) {
			if (currentLine == 0) {
				maxLine = Math.max(maxLine,currentWord);
			}
			else {
				maxLine = Math.max(maxLine,currentLine - currentSpare);
				numLines++;
				maxLine = Math.max(maxLine,currentWord);
			}
		}
		else {
			maxLine = Math.max(maxLine,currentLine + currentWord);
		}
		return new double[]{maxLine + MINIMUM_TEXTBOX_WIDTH + TEXTBOX_SAFETY_WIDTH,
			MINIMUM_TEXTBOX_HEIGHT + TEXT_HEIGHT * numLines};
	}

	private static int actualToUsable(double v) {
		return (int) Math.ceil(v * 10.0);
	}

	private static void rotate(TextBox tb) {
		double x = tb.x;
		double y = tb.y;
		double h = tb.height;
		double w = tb.width;
		double a = tb.rotation * Math.PI / 180.0;
		tb.x += (w * (Math.cos(a) - 1) - h * Math.sin(a)) / 2.0;
		tb.y += (h * (Math.cos(a) - 1) + w * Math.sin(a)) / 2.0;
	}

	private static double getActualHeight(TextBox tb) {
		double y = tb.y;
		double h = tb.height;
		double w = tb.width;
		double a = tb.rotation * Math.PI / 180.0;
		double actY = y + (h * (1 + Math.signum(a) * Math.cos(a)) + w * Math.sin(a)) / 2.0;
		return Math.abs(actY);
	}

	private static double[] getLowerLine(TextBox tb) {
		double x = tb.x;
		double y = tb.y;
		double h = tb.height;
		double w = tb.width;
		double a = tb.rotation * Math.PI / 180.0;
		double llx = x + (w * (1 - Math.cos(a)) - h * Math.sin(a)) / 2.0;
		double lly = y + (h * (1 + Math.cos(a)) - w * Math.sin(a)) / 2.0;
		double lrx = x + (w * (1 + Math.cos(a)) - h * Math.sin(a)) / 2.0;
		double lry = y + (h * (1 + Math.cos(a)) + w * Math.sin(a)) / 2.0;
		return new double[]{llx,lly,lrx,lry};
	}
}