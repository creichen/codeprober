package protocolgen;

/**
 * Pseudo-annotation that indicates that a field may be null
 */
public class Nullable {
	private Object obj;

	public Nullable(Object obj) {
		this.obj = obj;
	}
	public Object get() {
		return obj;
	}
	public static Nullable of(Object obj) {
		return new Nullable(obj);
	}
}
