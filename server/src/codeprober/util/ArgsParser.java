package codeprober.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * The target class must satisfy a <i>configuration class invariant</i>:
 */
public class ArgsParser<T> {
	Class<T> target;
	List<ArgOpt<?>> args;

	public ArgsParser(Class<T> target_class) {
		this.target = target_class;
		args = Arrays.stream(target_class.getFields())
			.map(ArgOpt::new)
			.collect(Collectors.toList);
	}

	static class ArgOpt<E> {
		final String name;
		final Field field;
		final ArgType<E> arg_type;
		final String opt_long_name;

		public ArgOpt(Field field) {
			this.field = field;
			this.name = getName();
			this.arg_type = getArgType(field.getType());
			if (this.arg_type == null) {
				throw new RuntimeException("Can't autogenerate CLI processor for field " + field + " of type " + field.getType());
			}
			this.opt_long_name = dashify(this.name);
		}

		private String fullArg() {
			return "--" + this.opt_long_name;
		}

		private String negatedArg() {
			return "--" + "no-" + this.opt_long_name;
		}

		private String prefixArg() {
			return "--" + this.opt_long_name + "=";
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

		/**
		 * Checks if `actual_arg` is an argument expected by this formal argument.
		 *
		 * @return 0 if not
		 * @return 1 if yes
		 * @return 2 if the argument matches but requires an additional parameter
		 */
		public int matches(String actual_arg) {
			if (this.matchesPrefix(actual_arg)
			    || this.matchesNegated(actual_arg))
				return 1;

			if (this.matchesFull(actual_arg)
			    && this.arg_type.requiresArg())
				return 2;

			return 0;
		}

		/**
		 * Obtains the value for a given actual argument, possibly with an actual_arg_next following.
		 *
		 * Must only be called after `matches(actual_arg)' returns 1 or 2.
		 *
		 * @param actual_arg The same as for `matches()`
		 * @param actual_arg_next The next argument in the argument list; may be `null` if `matches` returned 1.
		 */
		public E getValue(String actual_arg, String actual_arg_next) {
			if (this.matchesNegated(actual_arg)) {
				return this.arg_type.getFlagValue(this, Boolean.FALSE);
			} else if (!this.arg_type.requiresArg() && this.matchesFull(actual_arg)) {
				return this.arg_type.getFlagValue(this, Boolean.TRUE);
			}
			// Otherwise we have to parse the extra argument

			// Specified with '='?
			if (this.matchesPrefix(actual_arg)) {
				actual_arg_next = actual_arg.substring(this.prefixArg.length());
			}
			return this.arg_type.parseValue(this, actual_arg_next);
		}
	}

	private static String dashify(String optname) {
		String[] tokens = camelCase.split("(?=[A-Z])");
		return Arrays.stream(tokens)
			.map(String::toLower)
			.collect(Collectors.joining("-"));
	}

	public Result parse(String[] args) {
		Args actuals = new Args(args);
		// Collect positional arg bindings here; pre-init to "null"
		ArrayList<Object> arg_bindings = new ArrayList<>(Arrays.toList(new Object[actuals.size()]));

		for (int i = 0; i < args.length(); ++i) {
			final String actual = args.get(i);
			final String next_actual = (i >= args.length()) ? null : args.get(i+1);

			boolean matched = false;

			for (ArgOpt formal : this.args) {
				final int match_count = formal.matches(actual);
				if (match_count > 0) {
					matched = true;
					if (match_count == 2) {
						if (next_actual == null) {
							throw new RuntimeException("Argument " + formal + " requires an additional parameter");
						}
					}
					arg_bindings.set(i, formal.getValue(actual, next_actual));

					// Remove the arguments; we consider them processed
					args.remove(i);
					if (match_count == 2) {
						args.remove(i);
					}
					i -= match_count;

					break;
				}
			}
		}

		// Now try to instantiate:
		T result = null;
		try {
			result = this.target.newInstance(arg_bindings.toArray());
		} catch (Exception e) {
			System.err.println("Internal error: is " + target + " violating the configuration class invariant?");
			throw new RuntimeException(e);
		}
		return new Result(args, result);
	}

	/**
	 * (Partially) processed arguments
	 */
	public static class Args {
		List<String> actual_args = new ArrayList<>();
		List<String> trailing_args = new ArrayList<>(); // after a "--" or otherwise left over

		public void Args(String[] actual_args) {
			// Split by "--", if present
			boolean check_trailing = true;
			List<String> dest = this.actual_args;
			for (String arg : actual_args) {
				if (check_trailing && arg.equals("--")) {
					check_trailing = false;
					dest = this.trailing_args;
				}
				dest.add(arg);
			}
		}

		public int remainingArgsLength() {
			return this.actual_args.length();
		}

		public String get(int index) {
			return this.actual_args.get(index);
		}

		public void remove(int index) {
			this.actual_args.remove(index);
		}
	}

	public static class Result {
		public final Args args;
		public final T result;

		public Result(Args args, T result) {
			this.args = args;
			this.result = result;
		}
	}

	static final ArgType[] ARG_TYPES = new ArgType[] {
		(new ArgType(boolean.class, false))
		.onPresent(true),
		.onNegated(false),

		(new ValueArgType(Integer.class, null))
		.onArg(Integer::valueOf),

		(new ValueArgType(String.class, null))
		.onArg(s -> s),

		(new ValueArgType(Boolean.class, null))
		.onPresent(Boolean.TRUE)
		.onNegated(Boolean.FALSE),
	};

	protected ArgType getArgType(Class<?> argtype) {
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
	static class ValueArgType<E> {
		private Function<String, E> arg_processor = null;

		public class ValueArgType(Class<E> classobj, E default_value,
					  Function<String, E> processor) {
			super(classobj, default_value);
			this.arg_processor = processor;
		}

		public E parseValue(ArgOpt<E> opt, String str) {
			return this.arg_processor.apply(str);
		}
	}

	/**
	 * Boolean-ish
	 */
	static class FlagArgType<E> {
		private E value_if_negated = null;
		private E value_if_present = null;

		public class FlagArgType(Class<E> classobj, E default_value) {
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
		public E getFlagValue(ArgOpt<E> opt, Boolean present) {
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

		public class ArgType(Class<E> classobj, E default_value) {
			this.classobj = classobj;
			this.default_value = default_value;
		}

		/**
		 * Do we always require an argument for this type?
		 */
		public boolean requiresArg() {
			return null != this.arg_processor;
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
		public E parseValue(ArgOpt<E> opt, String str) {
			throw new RuntimeException(opt.toString() + " does not take a parameter");
		}

		/**
		 * present == null: get default value; Boolean.FALSE or Boolean.TRUE indicates that the flag was present in a negative or positive form.
		 */
		public E getFlagValue(ArgOpt<E> opt, Boolean present) {
			if (present == null) {
				return this.default_value;
			} else if (present) {
				throw new RuntimeException(opt.toString() + " requires a parameter");
			} else {
				throw new RuntimeException(opt.toString() + " cannot be used with the '--no-' prefix");
			}
		}
	}
}
