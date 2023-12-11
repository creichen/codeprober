package codeprober.protocol;

import java.util.Locale;

public enum DiagnosticType {

	// Squigglys
	ERROR, WARNING, INFO, HINT,

	// Lines
	LINE_PP, LINE_AA, LINE_AP, LINE_PA,

	;

	public static DiagnosticType parseFromJson(String string) {
		try {
			return DiagnosticType.valueOf(string.toUpperCase(Locale.ENGLISH));
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid DiagnosticSeverity value '" + string + "'");
			e.printStackTrace();
			return null;
		}
	}

	public boolean isEdge() {
	    switch (this) {
	    case LINE_PP:
	    case LINE_AA:
	    case LINE_AP:
	    case LINE_PA:
		return true;
	    default:
		return false;
	    }
	}
}
