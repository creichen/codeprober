package codeprober.protocol.create;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import codeprober.AstInfo;
import codeprober.ast.AstNode;
import codeprober.locator.CreateLocator;
import codeprober.metaprogramming.InvokeProblem;
import codeprober.metaprogramming.Reflect;
import codeprober.protocol.data.Diagnostic;
import codeprober.protocol.DiagnosticType;
import codeprober.protocol.data.EdgeDiagnostic;
import codeprober.protocol.data.HighlightableMessage;
import codeprober.protocol.data.NodeLocator;
import codeprober.protocol.data.RpcBodyLine;
import codeprober.util.MagicStdoutMessageParser;

public class EncodeResponseValue {
	private static Pattern HLHOVER_PATTERN = Pattern.compile("HLHOVER@(\\d+);(\\d+)");

	private static NodeLocator getNodeLocator(AstInfo info, Object value) {
		if (info.baseAstClazz.isInstance(value)) {
			return CreateLocator.fromNode(info, new AstNode(value));
		}
		return null;
	}

	private static EdgeDiagnostic getEdgeDiagnostic(AstInfo info, DiagnosticType diag_ty,
							String diag_style, Object value) {
		try {
			Object edge_diag_obj =  Reflect.invoke0(value, "cpr_getEdgeDiagnostic");
			if (edge_diag_obj instanceof Object[]
			    && ((Object[]) edge_diag_obj).length >= 3) {
				Object[] spec = (Object[]) edge_diag_obj;
				NodeLocator lhs = EncodeResponseValue.getNodeLocator(info, spec[0]);
				String edgeInfo = (String) spec[1];
				NodeLocator rhs = EncodeResponseValue.getNodeLocator(info, spec[2]);
				if (lhs != null && rhs != null) {
					return new EdgeDiagnostic(diag_ty,
								  lhs,
								  rhs,
								  edgeInfo,
								  diag_style);
				} else {
					System.err.println(value.getClass() + ".cpr_getEdgeDiagnostic() did not return array of [ASTNode, String, ASTNode]");
					return null;
				}
			}
			System.err.println(value.getClass() + ".cpr_getEdgeDiagnostic() did not return Object[3]");
			// fall through
		} catch (InvokeProblem __) {
			// fall through
		} catch (ClassCastException __) {
			System.err.println(value.getClass() + ".cpr_getEdgeDiagnostic() did not return array of [ASTNode, String, ASTNode]");
			// fall through
		}
		return null;
	}

	public static void encodeTyped(AstInfo info, List<RpcBodyLine> out, List<Diagnostic> diagnostics, List<EdgeDiagnostic> edgeDiagnostics, Object value,
			HashSet<Object> alreadyVisitedNodes) {
		if (value == null) {
			out.add(RpcBodyLine.fromPlain("null"));
			return;
		}

		try {
			final Object diagnosticValue = Reflect.invoke0(value, "cpr_getDiagnostic");
			if (diagnosticValue != null) {
				if (diagnosticValue instanceof String) {
					boolean specialDiagnosticBody;
					Object preferredView;
					try {
						preferredView = Reflect.invoke0(value, "cpr_getOutput");
						specialDiagnosticBody = true;
					} catch (InvokeProblem e) {
						preferredView = diagnosticValue;
						specialDiagnosticBody = false;
					}

					final String diagStr = (String) diagnosticValue;
					if (diagStr.startsWith("HLHOVER@")) {
						final Matcher matcher = HLHOVER_PATTERN.matcher(diagStr);
						if (!matcher.matches()) {
							System.err.println("Invalid HLHOVER string '" + diagnosticValue + "'");
						} else {
							final int start = Integer.parseInt(matcher.group(1));
							final int end = Integer.parseInt(matcher.group(2));

							out.add(RpcBodyLine
								.fromHighlightMsg(new HighlightableMessage(start, end, preferredView.toString())));

						}
					} else {
						// Diagnostic string, but not HLHover?

//					if (((String) diagnosticValue).startsWith("))
						Diagnostic d = MagicStdoutMessageParser.parse(diagStr);
						if (d != null) {
							if (d.type.isEdge()) {
								EdgeDiagnostic ed = EncodeResponseValue.getEdgeDiagnostic(
									info, d.type, d.msg, value);
								if (ed != null) {
									edgeDiagnostics.add(ed);
									d = null;
									return;
								}
							}
							if (d != null) {
								diagnostics.add(d);
							}
						} else {
							System.err.println("Invalid diagnostic string '" + diagnosticValue + "'");
						}
					}
				} else {
					System.err.println("Unknown cpr_getDiagnostic return type. Expected String, got "
							+ diagnosticValue.getClass().getName());
				}
			}
		} catch (InvokeProblem e) {
			// OK, this is an optional attribute after all
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
					encodeTyped(info, out, diagnostics, edgeDiagnostics, preferredView, alreadyVisitedNodes);
					return;
				} catch (InvokeProblem e) {
					// Fall down to default view
				}
			}

			try {
				final NodeLocator locator = CreateLocator.fromNode(info, node);
				if (locator != null) {
					out.add(RpcBodyLine.fromNode(locator));

					out.add(RpcBodyLine.fromPlain("\n"));
					if (node.isList() && !alreadyVisitedNodes.contains(node.underlyingAstNode)) {
						final int numEntries = node.getNumChildren(info);
						out.add(RpcBodyLine.fromPlain(""));
						if (numEntries == 0) {
							out.add(RpcBodyLine.fromPlain("<empty list>"));
						} else {
							alreadyVisitedNodes.add(node.underlyingAstNode);
							out.add(RpcBodyLine.fromPlain("List contents [" + numEntries + "]:"));
							for (AstNode child : node.getChildren(info)) {
								encodeTyped(info, out, diagnostics, edgeDiagnostics, child, alreadyVisitedNodes);
							}
						}
						return;
					}
				} else {
					out.add(RpcBodyLine.fromPlain("Couldn't create locator for " + node.underlyingAstNode));
					out.add(RpcBodyLine
							.fromPlain("This could indicate a caching issue, where a detached AST node is stored "));
					out.add(RpcBodyLine.fromPlain("somewhere even after a re-parse or flushTreeCache() is called."));
					out.add(RpcBodyLine.fromPlain("Try setting the 'AST caching strategy' to 'None' or 'Purge'."));
					out.add(RpcBodyLine
							.fromPlain("If that helps, then you maybe have a caching problem somewhere in the AST."));
					out.add(RpcBodyLine.fromPlain(
							"If that doesn't help, then please look at any error messages in the terminal where you started code-prober.jar."));
					out.add(RpcBodyLine.fromPlain(
							"If that doesn't help either, then you may have found a bug. Please report it!"));
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
					encodeTyped(info, out, diagnostics, edgeDiagnostics, preferredView, alreadyVisitedNodes);
					return;
				} catch (InvokeProblem e) {
					// Fall down to default view
				}
			}
		}

		if (value instanceof Iterable<?>) {
			if (alreadyVisitedNodes.contains(value)) {
				out.add(RpcBodyLine.fromPlain("<< reference loop to already visited value " + value + " >>"));
				return;
			}
			alreadyVisitedNodes.add(value);

			final List<RpcBodyLine> indent = new ArrayList<>();
			Iterable<?> iter = (Iterable<?>) value;
			for (Object o : iter) {
				encodeTyped(info, indent, diagnostics, edgeDiagnostics, o, alreadyVisitedNodes);
			}
			out.add(RpcBodyLine.fromArr(indent));
			return;
		}
		if (value instanceof Iterator<?>) {
			if (alreadyVisitedNodes.contains(value)) {
				out.add(RpcBodyLine.fromPlain("<< reference loop to already visited value " + value + " >>"));
				return;
			}
			alreadyVisitedNodes.add(value);
			final List<RpcBodyLine> indent = new ArrayList<>();
			Iterator<?> iter = (Iterator<?>) value;
			while (iter.hasNext()) {
				encodeTyped(info, indent, diagnostics, edgeDiagnostics, iter.next(), alreadyVisitedNodes);
			}
			out.add(RpcBodyLine.fromArr(indent));
			return;
		}
		if (value instanceof Object[]) {
			if (alreadyVisitedNodes.contains(value)) {
				out.add(RpcBodyLine.fromPlain("<< reference loop to already visited value " + value + " >>"));
				return;
			}
			alreadyVisitedNodes.add(value);

			final List<RpcBodyLine> indent = new ArrayList<>();
			for (Object child : (Object[]) value) {
				encodeTyped(info, indent, diagnostics, edgeDiagnostics, child, alreadyVisitedNodes);
			}
			out.add(RpcBodyLine.fromArr(indent));
			return;
		}
		try {
			if (value.getClass().getMethod("toString").getDeclaringClass() == Object.class) {
//				if (value.getClass().isEnum()) {
//					out.put(value.toString());
//				}
				out.add(RpcBodyLine
						.fromPlain("No toString() or cpr_getOutput() implementation in " + value.getClass().getName()));
			}
		} catch (NoSuchMethodException e) {
			System.err.println("No toString implementation for " + value.getClass());
			e.printStackTrace();
		}
		for (String line : (value + "").split("\n")) {
			out.add(RpcBodyLine.fromPlain(line));
		}
	}
}
