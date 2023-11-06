package codeprober.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import java.util.List;

public class TestArgsParser {
	static class ConfA {
		public Integer value;
		public boolean flag;
		public Integer manyCamelCaseConstructs;
		public Integer withAST;
		public ConfA(Integer value,
			     boolean flag,
			     Integer manyCamelCaseConstructs,
			     Integer withAST) {
			this.value = value;
			this.flag = flag;
			this.manyCamelCaseConstructs = manyCamelCaseConstructs;
			this.withAST = withAST;
		}
	}

	@Test
	public void testArgNames() {
		ArgsParser<ConfA> p = new ArgsParser(ConfA.class);
		List<? extends ArgsParser.ArgOpt<?>> args = p.getArgs();
		assertEquals(4, args.size());

		assertEquals(2, args.get(0).matches("--value"));
		assertEquals(1, args.get(0).matches("--value="));
		assertEquals(1, args.get(0).matches("--value=13"));
		assertEquals(0, args.get(0).matches("--no-value"));

		assertEquals(1, args.get(1).matches("--flag"));
		assertEquals(1, args.get(1).matches("--no-flag"));
		assertEquals(0, args.get(1).matches("--flag="));
		assertEquals(0, args.get(1).matches("--no-flag="));

		assertEquals(2, args.get(1).matches("--many-camel-case-constructs"));
		assertEquals(1, args.get(1).matches("--many-camel-case-constructs="));
		assertEquals(1, args.get(1).matches("--many-camel-case-constructs=42"));
		assertEquals(0, args.get(1).matches("--no-many-camel-case-constructs"));

		assertEquals(2, args.get(1).matches("--with-ast"));
		assertEquals(1, args.get(1).matches("--with-ast="));
		assertEquals(1, args.get(1).matches("--with-ast=23"));
		assertEquals(0, args.get(1).matches("--no-with-ast"));
	}
}
