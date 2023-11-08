package codeprober.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import codeprober.protocol.EnumString;

/**
 * The target class must satisfy a <i>configuration class invariant</i>: all fields declared in the class
 * must be set in the constructor, and the constructor must take the same number of arguments as there are
 * declared fields, which the order of arguments (including their types) matching field declaration order.
 */
public class DataStructOpt<T> {
	public static final int OPT_MISMATCH = 0;
	public static final int OPT_MATCH = 1;
	public static final int OPT_MATCH_REQUIRES_ARG = 2;

	protected Class<T> target;
	protected Constructor<T> constructor;
	protected List<ArgOpt<?>> formalArgs;
	protected Object[] constructor_args;

	public DataStructOpt(Class<T> target_class) {
		this.target = target_class;
		this.formalArgs = Arrays.stream(target_class.getFields())
			.map(ArgOpt::new)
			.collect(Collectors.toList());
		try {
			Class<?> types[] = Arrays.stream(target_class.getFields())
				.map(Field::getType)
				.collect(Collectors.toList())
				.toArray(new Class<?>[this.formalArgs.size()]);
			this.constructor = target_class.getConstructor(types);
			this.constructor_args = new Object[types.length];
		} catch (Exception exn) {
			System.err.println("ArgsParser doesn't seem to support this type-- must satisfy configuration class invariant!");
			throw new RuntimeException(exn);
		}

		// Initialise constructor args to defaults
		int index = 0;
		for (ArgOpt<?> arg : this.formalArgs) {
			this.constructor_args[index++] = arg.getDefault();
		}
	}

	/**
	 * Addas a prefix to all optoins managed by this DataStructOpt.
	 *
	 * For example, if an option "--foo" exists, then withPrefix("force") will rename
	 * that option to "--force-foo".
	 */
	public DataStructOpt<T> withOptPrefix(String prefix) {
		String opt_prefix = prefix + "-";
		for (ArgOpt<?> opt : this.formalArgs) {
			opt.setOptPrefix(opt_prefix);
		}
		return this;
	}

	public int matches(String actual_arg) {
		for (ArgOpt<?> opt : this.formalArgs) {
			int match = opt.matches(actual_arg);
			if (match != OPT_MISMATCH) {
				return match;
			}
		}
		return OPT_MISMATCH;
	}

	public T get() {
		try {
			return this.constructor.newInstance(this.constructor_args);
		} catch (Exception e) {
			System.err.println("Internal error: is " + target + " violating the configuration class invariant?");
			throw new RuntimeException(e);
		}
	}

	public int set(String actual_arg, String actual_arg_next) throws OptException {
		int offset = 0;
		for (ArgOpt<?> opt : this.formalArgs) {
			int match = opt.matches(actual_arg);
			if (match != OPT_MISMATCH) {
				this.constructor_args[offset] = opt.getValue(actual_arg, actual_arg_next);
			}
			++offset;
		}
		return OPT_MISMATCH;
	}

	public void addHelp(List<String[]> help) {
		for (ArgOpt<?> opt : this.formalArgs) {
			opt.addHelp(help);
		}
	}

	// for testing
	public List<? extends ArgOpt<?>> getArgs() {
		return this.formalArgs;
	}

	public static class ArgOpt<E> {
		final String name;
		final Field field;
		final ArgType<E> arg_type;
		final String opt_long_name;
		final String[] limited_options;
		String opt_prefix = "";

		public ArgOpt(Field field) {
			this.field = field;
			this.name = field.getName();
			this.arg_type = getArgType(field.getType());
			if (this.arg_type == null) {
				throw new RuntimeException("Can't autogenerate CLI processor for field " + field + " of type " + field.getType());
			}
			EnumString enum_string_annotation = field.getAnnotation(EnumString.class);
			if (enum_string_annotation == null) {
				this.limited_options = null;
			} else {
				this.limited_options = enum_string_annotation.options();
			}
			this.opt_long_name = dashify(this.name);
		}

		void setOptPrefix(String opt_prefix) {
			this.opt_prefix = opt_prefix;
		}

		private String fullArg() {
			return "--" + opt_prefix + this.opt_long_name;
		}

		private String negatedArg() {
			return "--" + opt_prefix + "no-" + this.opt_long_name;
		}

		private String prefixArg() {
			return "--" + opt_prefix + this.opt_long_name + "=";
		}

		private boolean matchesFull(String actual_arg) {
			return actual_arg.equals(this.fullArg());
		}

		private boolean matchesNegated(String actual_arg) {
			return actual_arg.equals(this.negatedArg());
		}

		private boolean matchesPrefix(String actual_arg) {
			return actual_arg.startsWith(this.prefixArg());
		}

		private boolean requiresArg() {
			return this.arg_type.requiresArg();
		}

		public void addHelp(List<String[]> help) {
			Object default_value = null;
			try {
				this.arg_type.getFlagValue(this, null);
			} catch (OptException exn) {};

			if (this.arg_type.requiresArg()) {
				String default_suffix = (default_value == null)
					? ""
					: " [default=" + default_value + "]";
				String description = "Set " + this.name;
				if (this.limited_options != null) {
					description += " to one of: "
						+ Arrays.stream(this.limited_options)
						.collect(Collectors.joining(", "));
				}
				help.add(new String[] { this.prefixArg(), "V", description + default_suffix });
			} else {
				String true_suffix = "";
				String false_suffix = "";

				if (default_value == Boolean.TRUE) {
					true_suffix = " [default]";
				} else if (default_value == Boolean.FALSE) {
					false_suffix = " [default]";
				}
				help.add(new String[] { this.fullArg(), "", "Enable " + this.name + true_suffix });
				help.add(new String[] { this.negatedArg(), "", "Disable " + this.name + false_suffix });
			}
		}

		/**
		 * Checks if `actual_arg` is an argument expected by this formal argument.
		 *
		 * @return OPT_MISMATCH if not
		 * @return OPT_MATCH if yes
		 * @return OPT_MATCH_REQUIRES_ARG if the argument matches but requires an additional parameter
		 */
		public int matches(String actual_arg) {
			if ((this.requiresArg() && this.matchesPrefix(actual_arg))
			    || (!this.requiresArg() && this.matchesNegated(actual_arg))) {
				return OPT_MATCH;
			}

			if (this.matchesFull(actual_arg)) {
				if (this.requiresArg()) {
					return OPT_MATCH_REQUIRES_ARG;
				}
				return OPT_MATCH;
			}

			return OPT_MISMATCH;
		}

		public void validateArg(String actual_arg) throws OptException {
			if (this.limited_options == null) {
				return;
			}
			for (String opt : this.limited_options) {
				if (opt.equals(actual_arg)) {
					return;
				}
			}
			throw new OptException("Invalid argument; allowed options are: " + Arrays.toString(this.limited_options));

		}

		/**
		 * Obtains the value for a given actual argument, possibly with an actual_arg_next following.
		 *
		 * Must only be called after `matches(actual_arg)' returns 1 or 2.
		 *
		 * @param actual_arg The same as for `matches()`
		 * @param actual_arg_next The next argument in the argument list; may be `null` if `matches` returned 1.
		 */
		public E getValue(String actual_arg, String actual_arg_next) throws OptException {
			if (this.matchesNegated(actual_arg)) {
				return this.arg_type.getFlagValue(this, Boolean.FALSE);
			} else if (!this.arg_type.requiresArg() && this.matchesFull(actual_arg)) {
				return this.arg_type.getFlagValue(this, Boolean.TRUE);
			}
			// Otherwise we have to parse the extra argument

			// Specified with '='?
			if (this.matchesPrefix(actual_arg)) {
				actual_arg_next = actual_arg.substring(this.prefixArg().length());
			}
			this.validateArg(actual_arg_next);
			return this.arg_type.parseValue(this, actual_arg_next);
		}

		public E getDefault() {
			try {
				return this.arg_type.getFlagValue(this, null);
			} catch (OptException exn) {
				return null;
			}
		}
	}

	static final ArgType[] ARG_TYPES = new ArgType[] {
		(new FlagArgType(boolean.class, false))
		.onPresent(true)
		.onNegated(false),

		(new ValueArgType<Integer>(Integer.class, null,
					   Integer::valueOf)),

		(new ValueArgType<String>(String.class, null,
					  s -> s)),

		(new FlagArgType<Boolean>(Boolean.class, null))
		.onPresent(Boolean.TRUE)
		.onNegated(Boolean.FALSE),
	};

	public static  ArgType getArgType(Class<?> argtype) {
		for (ArgType at : ARG_TYPES) {
			if (at.getType().equals(argtype)) {
				return at;
			}
		}
		return null;
	}

	/**
	 * Argument that expects an explicit value
	 */
	static class ValueArgType<E> extends ArgType<E> {
		private Function<String, E> arg_processor = null;

		public ValueArgType(Class<E> classobj, E default_value,
				    Function<String, E> processor) {
			super(classobj, default_value);
			this.arg_processor = processor;
		}

		@Override
		public E parseValue(ArgOpt<E> opt, String str) {
			return this.arg_processor.apply(str);
		}

		/**
		 * Do we always require an argument for this type?
		 */
		public boolean requiresArg() {
			return true;
		}
	}

	/**
	 * Boolean-ish
	 */
	static class FlagArgType<E> extends ArgType<E> {
		private E value_if_negated = null;
		private E value_if_present = null;

		public FlagArgType(Class<E> classobj, E default_value) {
			super(classobj, default_value);
		}
		public FlagArgType<E> onPresent(E e) {
			this.value_if_present = e;
			return this;
		}
		public FlagArgType<E> onNegated(E e) {
			this.value_if_negated = e;
			return this;
		}
		public boolean allowsNegation() {
			return null != this.value_if_negated;
		}
		public E getFlagValue(ArgOpt<E> opt, Boolean present) throws OptException {
			if (present == null) {
				return super.getFlagValue(opt, null);
			}
			return present
				? this.value_if_present
				: this.value_if_negated;
		}
	}

	/**
	 * Takes a value parameter
	 */
	static class ArgType<E> {
		private final Class<E> classobj;
		private final E default_value;

		public ArgType(Class<E> classobj, E default_value) {
			this.classobj = classobj;
			this.default_value = default_value;
		}

		/**
		 * Do we always require an argument for this type?
		 */
		public boolean requiresArg() {
			return false;
		}

		public Class<E> getType() {
			return this.classobj;
		}

		public boolean allowsNegation() {
			return false;
		}

		/**
		 * Gets the field's value from explicit argument 'str', if explicitly specified.
		 */
		public E parseValue(ArgOpt<E> opt, String str) throws OptException {
			throw new OptException(opt.toString() + " does not take a parameter");
		}

		/**
		 * present == null: get default value; Boolean.FALSE or Boolean.TRUE indicates that the flag was present in a negative or positive form.
		 */
		public E getFlagValue(ArgOpt<E> opt, Boolean present) throws OptException {
			if (present == null) {
				return this.default_value;
			} else if (present) {
				throw new OptException(opt.toString() + " requires a parameter");
			} else {
				throw new OptException(opt.toString() + " cannot be used with the '--no-' prefix");
			}
		}
	}

	/**
	 * Ill-formed argument
	 */
	public static class OptException extends Exception {
		public OptException() {
		}

		public OptException(String s) {
			super(s);
		}
	}

	public static List<String> splitStringByWidth(String s, int max_width) {
		List<String> result = new ArrayList<>();
		String current = null;
		for (String token : s.split(" ")) {
			if (current == null) {
				current = token;
			} else {
				if (current.length() + 1 + token.length() <= max_width) {
					current += " " + token;
				} else {
					result.add(current);
					current = token;
				}
			}
		}
		if (current != null) {
			result.add(current);
		}
		return result;
	}

	/**
	 * Turns a camelCase name into a parameter option-style dashified name
	 */
	public static String dashify(String optname) {
		String[] tokens = optname.split("(?<![A-Z])(?=[A-Z])");
		return Arrays.stream(tokens)
			.map(String::toLowerCase)
			.collect(Collectors.joining("-"));
	}
}
