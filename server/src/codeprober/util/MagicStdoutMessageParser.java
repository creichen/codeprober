package codeprober.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class MagicStdoutMessageParser {

	static final Pattern PATTERN = Pattern.compile("(ERR|WARN|INFO|LINE-PP|LINE-AA|LINE-AP|LINE-PA)@(\\d+);(\\d+);(.*)");
	static java.io.PrintWriter dstream = null;
	static	{
			try {
				MagicStdoutMessageParser.dstream = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter("/tmp/flog")));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	public static JSONObject parse(boolean stdout, String line) {
		// Ignore whether the message is stdout or stderr, capture everything!

		final Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}
		dstream.println(">> >> ++ Matched '"+line+"'");
		dstream.flush();
		final int start = Integer.parseInt(matcher.group(2));
		final int end = Integer.parseInt(matcher.group(3));
		final String msg = matcher.group(4);
		final JSONObject obj = new JSONObject();
		switch (matcher.group(1)) {
		case "ERR":
			obj.put("severity", "error");
			break;
		case "WARN":
			obj.put("severity", "warning");
			break;
		default:
			obj.put("severity", matcher.group(1).toLowerCase(Locale.ENGLISH));
			break;
		}
		obj.put("start", start);
		obj.put("end", end);
		obj.put("msg", msg);
		return obj;
	}
}
