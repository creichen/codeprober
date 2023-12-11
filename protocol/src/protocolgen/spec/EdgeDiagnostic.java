package protocolgen.spec;

import codeprober.protocol.DiagnosticType;

public class EdgeDiagnostic extends Streamable {
	public final Object type = DiagnosticType.class;
	public final Object startNode = NodeLocator.class;
	public final Object endNode = NodeLocator.class;
	public final Object edgeInfo = String.class;
	public final Object style = String.class;
}
