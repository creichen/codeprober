package protocolgen.spec;

import java.util.Arrays;
import java.util.Optional;
import protocolgen.TsEnumRef;

public class Streamable {

	protected static Object opt(Object val) { return Optional.of(val); }
	protected static Object arr(Object val) { return Arrays.asList(val); }
	protected static Object oneOf(String... vals) { return vals; }
	protected static Object nullable(Object obj) { return protocolgen.Nullable.of(obj); }
	protected static Object fromTsEnum(String enum_type) { return TsEnumRef.get(enum_type); }
}
