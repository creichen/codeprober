package codeprober.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.Arrays;

import org.json.JSONObject;
import org.json.JSONArray;

public class MagicStdoutMessageParser {

	public static final Pattern MESSAGE_PATTERN = Pattern.compile("(ERR|WARN|INFO|HINT|STYLE|LINE-PP|LINE-AA|LINE-AP|LINE-PA)@(\\d+);(\\d+);(.*)");

	public static JSONObject encode(String code, int start, int end, String msg) {
		final JSONObject obj = new JSONObject();
		switch (code) {
		case "ERR":
			obj.put("severity", "error");
			break;
		case "WARN":
			obj.put("severity", "warning");
			break;
		case "STYLE":
			final String[] styles = msg.split(",", -1);
			obj.put("highlightClasses",
				new JSONArray(Stream.of(styles).map(String::trim).toArray(String[]::new)));
			break;
		default:
			obj.put("severity", code.toLowerCase(Locale.ENGLISH));
			break;
		}
		obj.put("start", start);
		obj.put("end", end);
		obj.put("msg", msg);
		obj.put("type", "marker");
		return obj;
	}

	/**
	 * Explicit tuple representation
	 */
	public static JSONObject interpretArray(String[] spec) {
		if (spec.length < 4 || spec[0] == null) {
			System.err.println("Ill-formed: " + Arrays.toString(spec));
			return null;
		}
		return MagicStdoutMessageParser.encode(spec[0], Integer.parseInt(spec[1]), Integer.parseInt(spec[2]), spec[3]);
	}

	public static JSONObject parse(boolean stdout, String line) {
		// Ignore whether the message is stdout or stderr, capture everything!

		final Matcher matcher = MESSAGE_PATTERN.matcher(line);
		if (!matcher.matches()) {
			return null;
		}
		final int start = Integer.parseInt(matcher.group(2));
		final int end = Integer.parseInt(matcher.group(3));
		final String msg = matcher.group(4);
		return MagicStdoutMessageParser.encode(matcher.group(1), start, end, msg);
	}
}
