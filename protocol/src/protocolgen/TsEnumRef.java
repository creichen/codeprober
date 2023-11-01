package protocolgen;

import java.util.Map;
import java.util.HashMap;


/**
 * Represents a reference to a predefined enum (defined in TypeScript code)
 */
public class TsEnumRef {
	private static Map<String, TsEnumRef> refs = new HashMap<>();

	public final String name;
	public final String src_file;
	public final String[] options;

	/**
	 * Construct new TsEnumRef object and registers it.
	 */
	public TsEnumRef(String name, String src_file, String[] options) {
		this.name = name;
		if (null != src_file && src_file.endsWith(".ts")) {
			src_file = src_file.substring(0, src_file.length() - 3);
		}
		this.src_file = src_file;
		this.options = options;
		TsEnumRef.refs.put(name, this);
	}

	public static TsEnumRef get(String name) {
		TsEnumRef ref = TsEnumRef.refs.get(name);
		if (null == ref) {
			throw new RuntimeException("Unknown TypeScript enum referenced: \""+name+"\"");
		}
		return ref;
	}

	public boolean isGlobal() {
		return null == this.src_file;
	}

	final static Object _dummy = new TsSrcEnums(); // Force execution of the static{} block in TsSrcEnums
}
