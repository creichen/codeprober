package codeprober.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import codeprober.protocol.EnumString;

import java.util.List;

public class TestArgsParser {
	static class ConfA {
		public Integer value;
		public boolean flag;
		public Integer manyCamelCaseConstructs;
		public Boolean withAST;
		public ConfA(Integer value,
			     boolean flag,
			     Integer manyCamelCaseConstructs,
			     Boolean withAST) {
			this.value = value;
			this.flag = flag;
			this.manyCamelCaseConstructs = manyCamelCaseConstructs;
			this.withAST = withAST;
		}
	}

	@Test
	public void testArgNames() {
		DataStructOpt<ConfA> p = new DataStructOpt(ConfA.class);

		List<? extends DataStructOpt.ArgOpt<?>> args = p.getArgs();
		assertEquals(4, args.size());

		assertEquals(2, args.get(0).matches("--value"));
		assertEquals(1, args.get(0).matches("--value="));
		assertEquals(1, args.get(0).matches("--value=13"));
		assertEquals(0, args.get(0).matches("--no-value"));

		assertEquals(1, args.get(1).matches("--flag"));
		assertEquals(1, args.get(1).matches("--no-flag"));
		assertEquals(0, args.get(1).matches("--flag="));
		assertEquals(0, args.get(1).matches("--no-flag="));

		assertEquals(2, args.get(2).matches("--many-camel-case-constructs"));
		assertEquals(1, args.get(2).matches("--many-camel-case-constructs="));
		assertEquals(1, args.get(2).matches("--many-camel-case-constructs=42"));
		assertEquals(0, args.get(2).matches("--no-many-camel-case-constructs"));

		assertEquals(1, args.get(3).matches("--with-ast"));
		assertEquals(0, args.get(3).matches("--with-ast="));
		assertEquals(0, args.get(3).matches("--with-ast=23"));
		assertEquals(1, args.get(3).matches("--no-with-ast"));
	}

	@Test
	public void testEmptyCons() {
		DataStructOpt<ConfA> p = new DataStructOpt(ConfA.class);
		ConfA conf = p.get();
		assertNull(conf.value);
		assertFalse(conf.flag);
		assertNull(conf.manyCamelCaseConstructs);
		assertNull(conf.withAST);
	}

	@Test
	public void testPartialCons1() throws Exception {
		DataStructOpt<ConfA> p = new DataStructOpt(ConfA.class);
		p.set("--value=13", null);
		p.set("--with-ast", null);
		p.set("--flag", null);
		ConfA conf = p.get();

		assertEquals(Integer.valueOf(13), conf.value);
		assertTrue(conf.flag);
		assertNull(conf.manyCamelCaseConstructs);
		assertTrue(conf.withAST);
	}

	@Test
	public void testPartialCons2() throws Exception {
		DataStructOpt<ConfA> p = new DataStructOpt(ConfA.class);
		p.set("--value=-13", null);
		p.set("--no-with-ast", null);
		p.set("--no-flag", null);
		ConfA conf = p.get();

		assertEquals(Integer.valueOf(-13), conf.value);
		assertFalse(conf.flag);
		assertNull(conf.manyCamelCaseConstructs);
		assertFalse(conf.withAST);
	}

	static class ConfB {
		public String str;
		@EnumString(options = {"yes", "no", "maybe"})
		public String opt;
		public ConfB(String s, String o) {
			this.str = s;
			this.opt = o;
		}
	}

	@Test
	public void testStringEnumOK() throws Exception {
		DataStructOpt<ConfB> p = new DataStructOpt(ConfB.class);
		p.set("--str=foo", null);
		p.set("--opt=yes", null);
		ConfB conf = p.get();

		assertEquals("foo", conf.str);
		assertEquals("yes", conf.opt);
	}

	@Test(expected = DataStructOpt.OptException.class)
	public void testStringEnumFail() throws Exception {
		DataStructOpt<ConfB> p = new DataStructOpt(ConfB.class);
		p.set("--str=foo", null);
		p.set("--opt=potato", null);
	}

	@Test
	public void testPrefix() throws Exception {
		DataStructOpt<ConfB> p = new DataStructOpt(ConfB.class).withOptPrefix("pfx");

		List<? extends DataStructOpt.ArgOpt<?>> args = p.getArgs();
		assertEquals(2, args.size());

		assertEquals(0, args.get(0).matches("--str="));
		assertEquals(1, args.get(0).matches("--pfx-str="));

		p.set("--pfx-str=foo", null);
		p.set("--pfx-opt=yes", null);
		ConfB conf = p.get();

		assertEquals("foo", conf.str);
		assertEquals("yes", conf.opt);
	}

}
