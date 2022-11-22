package codeprober;

import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONObject;

import codeprober.CodeProber;

import junit.framework.TestCase;
import static org.junit.Assert.*;

public class TestArgs extends TestCase {

	public void testArgSplitEmpty() {
		for (int n : new Integer[]{-1, 0, 2, 4}) {
			for (boolean b : new Boolean[]{true, false}) {
				assertArrayEquals(new String[]{""},
						  CodeProber.ArgOption.escapedSplit("", ',', b, n));
			}
		}
	}

	public void testArgSplitBoundary() {
		assertArrayEquals(new String[]{"", ""},
				  CodeProber.ArgOption.escapedSplit(",", ',', false, -1));
		assertArrayEquals(new String[]{"a", ""},
				  CodeProber.ArgOption.escapedSplit("a,", ',', false, -1));
		assertArrayEquals(new String[]{"", "a", "b", ""},
				  CodeProber.ArgOption.escapedSplit(",a,b,", ',', false, -1));
		assertArrayEquals(new String[]{"", "a", "", "b", ""},
				  CodeProber.ArgOption.escapedSplit(",a,,b,", ',', false, -1));
		assertArrayEquals(new String[]{"", "a", "", "", "b", ""},
				  CodeProber.ArgOption.escapedSplit(",a,,,b,", ',', false, -1));
	}

	public void testArgSplitBasic() {
		assertArrayEquals(new String[]{"a"},
				  CodeProber.ArgOption.escapedSplit("a", ',', false, -1));
		assertArrayEquals(new String[]{"foo", "bar"},
				  CodeProber.ArgOption.escapedSplit("foo,bar", ',', false, -1));
		assertArrayEquals(new String[]{"foo", "bar", "quux"},
				  CodeProber.ArgOption.escapedSplit("foo,bar,quux", ',', false, -1));
	}

	public void testArgSplitMax() {
		assertArrayEquals(new String[]{"foo", "bar", "quux", "quuux"},
				  CodeProber.ArgOption.escapedSplit("foo,bar,quux,quuux", ',', false, 5));
		assertArrayEquals(new String[]{"foo", "bar", "quux", "quuux"},
				  CodeProber.ArgOption.escapedSplit("foo,bar,quux,quuux", ',', false, 4));
		assertArrayEquals(new String[]{"foo", "bar", "quux,quuux"},
				  CodeProber.ArgOption.escapedSplit("foo,bar,quux,quuux", ',', false, 3));
		assertArrayEquals(new String[]{"foo", "bar,quux,quuux"},
				  CodeProber.ArgOption.escapedSplit("foo,bar,quux,quuux", ',', false, 2));
		assertArrayEquals(new String[]{"foo,bar,quux,quuux"},
				  CodeProber.ArgOption.escapedSplit("foo,bar,quux,quuux", ',', false, 1));
		assertArrayEquals(new String[]{"foo", "bar", "quux", "quuux"},
				  CodeProber.ArgOption.escapedSplit("foo,bar,quux,quuux", ',', false, 0));
	}

	public void testArgSplitEscapes() {
		assertArrayEquals(new String[]{"foo", "bar\\,quux"},
				  CodeProber.ArgOption.escapedSplit("foo,bar\\,quux", ',', false, 0));
		assertArrayEquals(new String[]{"foo", "bar,quux"},
				  CodeProber.ArgOption.escapedSplit("foo,bar\\,quux", ',', true, 0));
		assertArrayEquals(new String[]{"foo\\,bar\\\\", "quux"},
				  CodeProber.ArgOption.escapedSplit("foo\\,bar\\\\,quux", ',', false, 0));
		assertArrayEquals(new String[]{"foo,bar\\", "quux"},
				  CodeProber.ArgOption.escapedSplit("foo\\,bar\\\\,quux", ',', true, 0));
		assertArrayEquals(new String[]{"\\,foo\\,bar\\\\", "quux\\,"},
				  CodeProber.ArgOption.escapedSplit("\\,foo\\,bar\\\\,quux\\,", ',', false, 0));
		assertArrayEquals(new String[]{",foo,bar\\", "quux,"},
				  CodeProber.ArgOption.escapedSplit("\\,foo\\,bar\\\\,quux\\,", ',', true, 0));
	}

}
