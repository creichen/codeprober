package codeprober.protocol.create;

import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import codeprober.AstInfo;
import codeprober.ast.AstNode;
import codeprober.locator.CreateLocator;
import codeprober.metaprogramming.InvokeProblem;
import codeprober.metaprogramming.Reflect;
import codeprober.util.MagicStdoutMessageParser;

public class EncodeResponseValue {
	public static JSONArray
	indentEncoding(JSONArray parent, boolean singleLine) {
		JSONArray indentedArray = new JSONArray();
		if (singleLine) {
			JSONObject wrapper = new JSONObject();
			wrapper.put("type", "singleline");
			wrapper.put("value", indentedArray);
			parent.put(wrapper);
		} else {
			parent.put(indentedArray);
		}
		return indentedArray;
	}


	public static void encode(AstInfo info, JSONArray out, JSONArray markersOut, Object value, HashSet<Object> alreadyVisitedNodes) {
		if (value == null) {
			out.put("null");
			return;
		}
		// Clone to avoid showing 'already visited' when this encoding 'branch' hasn't
		// visited it.
		alreadyVisitedNodes = new HashSet<Object>(alreadyVisitedNodes);

		if (info.baseAstClazz.isInstance(value)) {
			value = new AstNode(value);
		}

		if (value instanceof AstNode) {
			AstNode node = (AstNode) value;

			if (!alreadyVisitedNodes.contains(node.underlyingAstNode)) {
				try {
					Object preferredView = Reflect.invoke0(node.underlyingAstNode, "cpr_getOutput");
					alreadyVisitedNodes.add(node.underlyingAstNode);
					encode(info, out, markersOut, preferredView, alreadyVisitedNodes);
					return;
				} catch (InvokeProblem e) {
					// Fall down to default view
				}
			}

			try {
//				final AstNode astNode = new AstNode(value);
				final JSONObject locator = CreateLocator.fromNode(info, node);
				if (locator != null) {
					JSONObject wrapper = new JSONObject();
					wrapper.put("type", "node");
					wrapper.put("value", locator);
					out.put(wrapper);

					out.put("\n");
					if (node.isList() && !alreadyVisitedNodes.contains(node.underlyingAstNode)) {
						final int numEntries = node.getNumChildren(info);
						out.put("");
						if (numEntries == 0) {
							out.put("<empty list>");
						} else {
							alreadyVisitedNodes.add(node.underlyingAstNode);
							out.put("List contents [" + numEntries + "]:");
							for (AstNode child : node.getChildren(info)) {
								encode(info, out, markersOut, child, alreadyVisitedNodes);
							}
						}
						return;
					}
				} else {
					out.put("Couldn't create locator for " + node.underlyingAstNode);
					out.put("This could indicate a caching issue, where a detached AST node is stored ");
					out.put("somewhere even after a re-parse or flushTreeCache() is called.");
					out.put("Try setting the 'AST caching strategy' to 'None' or 'Purge'.");
					out.put("If that helps, then you maybe have a caching problem somewhere in the AST.");
					out.put("If that doesn't help, then please look at any error messages in the terminal where you started code-prober.jar.");
					out.put("If that doesn't help either, then you may have found a bug. Please report it!");
				}
				return;
			} catch (InvokeProblem e) {
				System.err.println("Failed creating locator to " + node);
				e.printStackTrace();
				// Fall down to default toString encoding below
			}
		} else {
			if (!alreadyVisitedNodes.contains(value)) {
				try {
					Object preferredView = Reflect.invoke0(value, "cpr_getOutput");
					alreadyVisitedNodes.add(value);
					encode(info, out, markersOut, preferredView, alreadyVisitedNodes);
					return;
				} catch (InvokeProblem e) {
					// Fall down to default view
				}
			}
		}

		boolean requestingSingleLine = false;
		try {
			Object singleLineResult = Reflect.invoke0(value, "cpr_singleLine");
			requestingSingleLine = Boolean.TRUE.equals(singleLineResult);
		} catch (InvokeProblem exn) {
			// no single line preference
		}

		try {
			Object marker = Reflect.invoke0(value, "cpr_getMarker");
			if (marker != null) {
				JSONObject decoded = null;
				if (marker instanceof String[]) {
					decoded = MagicStdoutMessageParser.interpretArray((String[]) marker);
				} else if (marker instanceof String) {
					decoded = MagicStdoutMessageParser.parse(true, (String) marker);
				} else {
					decoded = MagicStdoutMessageParser.parse(true, marker.toString());
				}
				if (decoded != null) {
					markersOut.put(decoded);
				}
			}
		} catch (InvokeProblem exn) {
			// don't get marker, then
		}

		if (value instanceof Iterable<?>) {
			if (alreadyVisitedNodes.contains(value)) {
				out.put("<< reference loop to already visited value " + value + " >>");
				return;
			}
			alreadyVisitedNodes.add(value);

			final JSONArray indent = indentEncoding(out, requestingSingleLine);
			Iterable<?> iter = (Iterable<?>) value;
			for (Object o : iter) {
				encode(info, indent, markersOut, o, alreadyVisitedNodes);
			}
			return;
		}
		if (value instanceof Iterator<?>) {
			if (alreadyVisitedNodes.contains(value)) {
				out.put("<< reference loop to already visited value " + value + " >>");
				return;
			}
			alreadyVisitedNodes.add(value);
			final JSONArray indent = indentEncoding(out, requestingSingleLine);
			Iterator<?> iter = (Iterator<?>) value;
			while (iter.hasNext()) {
				encode(info, indent, markersOut, iter.next(), alreadyVisitedNodes);
			}
			return;
		}
		if (value instanceof Object[]) {
			if (alreadyVisitedNodes.contains(value)) {
				out.put("<< reference loop to already visited value " + value + " >>");
				return;
			}
			alreadyVisitedNodes.add(value);

			final JSONArray indent = indentEncoding(out, requestingSingleLine);
			for (Object child : (Object[]) value) {
				encode(info, indent, markersOut, child, alreadyVisitedNodes);
			}
//			final JSONObject indentObj = new JSONObject();
//			indentObj.put("type", "indent");
//			indentObj.put("value", indent);
			return;
		}
		try {
			if (value.getClass().getMethod("toString").getDeclaringClass() == Object.class) {
//				if (value.getClass().isEnum()) {
//					out.put(value.toString());
//				}
				out.put("No toString() or cpr_getOutput() implementation in " + value.getClass().getName());
			}
		} catch (NoSuchMethodException e) {
			System.err.println("No toString implementation for " + value.getClass());
			e.printStackTrace();
		}
		for (String line : (value + "").split("\n")) {
			out.put(line);
		}
	}
}
