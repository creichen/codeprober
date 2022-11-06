package codeprober.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarFile;

import org.json.JSONArray;

import codeprober.metaprogramming.StdIoInterceptor;

/**
 * Provides an AST by running a compiler and using reflection to fetch the
 * JastAdd AST from the compiler.
 * <p>
 * Created by gda10jth on 1/15/16.
 */
public class ASTProvider {
	private static class LoadedJar {
		static final String DRAST_ROOT_NODE_FIELD = "DrAST_root_node";
		static final String CODEPROBER_REPORT_STYLES_FIELD = "CodeProber_report_styles";

		public final String jarPath;
		public final long jarLastModified;
		public final CompilerClassLoader classLoader;
		public final Class<?> mainClazz;
		public final JarFile jar;
		public final Method mainMth;
		public final Field drAstField;
		public final Field clientStylesField;;

		public Object
		getASTRoot() throws IllegalAccessException {
			return this.drAstField.get(this.mainClazz);
		}

		public ClientStyles
		getClientStyles() {
			return new ClientStyles(this.getClientStylesRaw());
		}

		protected String[]
		getClientStylesRaw() {
			if (this.clientStylesField == null) {
				return null;
			}
			final Object styles;
			try {
				styles = this.clientStylesField.get(this.mainClazz);
			} catch (IllegalAccessException exn) {
				exn.printStackTrace();
				return null;
			}

			if (styles == null) {
				return null;
			}

			if (!(styles instanceof Object[])) {
				System.err.println(CODEPROBER_REPORT_STYLES_FIELD + " does not contain a string/object array: " + styles.getClass());
				return null;
			}

			Object[] ostyles = (Object[]) styles;
			for (Object o : ostyles) {
				if (!(o instanceof String)) {
					System.err.println(CODEPROBER_REPORT_STYLES_FIELD + " contains non-String object: " + ((o == null)? "null" : o.getClass()));
					return null;
				}
			}
			return (String[]) ostyles;
		}

		public Field getField(String fieldName, boolean required) {
			try {
				Field field = this.mainClazz.getField(fieldName);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException exn) {
				System.err.println("Coulnd not load field '" + fieldName + "': " + exn);
				if (!required) {
					return null;
				}
				throw new RuntimeException(exn);
			}
		}

		public LoadedJar(String jarPath, long jarLastModified, CompilerClassLoader classLoader, Class<?> mainClazz, JarFile jar,
				Method mainMth) {
			this.jarPath = jarPath;
			this.jarLastModified = jarLastModified;
			this.classLoader = classLoader;
			this.mainClazz = mainClazz;
			this.jar = jar;
			this.mainMth = mainMth;
			this.drAstField = this.getField(DRAST_ROOT_NODE_FIELD, true);
			this.clientStylesField = this.getField(CODEPROBER_REPORT_STYLES_FIELD, false);
		}
	}

	public static void purgeCache() {
		if (lastJar != null) {
			try {
				lastJar.jar.close();
			} catch (IOException e) {
				System.out.println("Error when closing jar file");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		lastJar = null;
	}

	private static LoadedJar lastJar = null;

	private static LoadedJar loadJar(String jarPath)
			throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		if (hasUnchangedJar(jarPath)) {
			return lastJar;
		}
		if (lastJar != null) {
			lastJar.jar.close();
		}
		final File jarFile = new File(jarPath);
		final long jarLastMod = jarFile.lastModified();
		CompilerClassLoader urlClassLoader = new CompilerClassLoader(jarFile.toURI().toURL());

		// Find and instantiate the main class from the Jar file.
		JarFile jar = new JarFile(jarFile);
		String mainClassName = jar.getManifest().getMainAttributes().getValue("Main-Class");
		Class<?> klass = Class.forName(mainClassName, true, urlClassLoader);
		Method mainMethod = klass.getMethod("main", String[].class);

		lastJar = new LoadedJar(jarPath, jarLastMod, urlClassLoader, klass, jar, mainMethod);
		return lastJar;
	}

	public static boolean hasUnchangedJar(String jarPath) {
		File jarFile = new File(jarPath);
		final long jarLastMod = jarFile.lastModified();
		return lastJar != null && lastJar.jarPath.equals(jarPath) && lastJar.jarLastModified == jarLastMod;
	}

	public static class ParseResult {
		public final boolean success;
		public final JSONArray captures;
		public final Supplier<ClientStyles> stylesClosure;

		public ParseResult(boolean success, JSONArray captures, Supplier<ClientStyles> stylesClosure) {
			this.success = success;
			this.captures = captures;
			this.stylesClosure = stylesClosure;
		}
	}

	/**
	 * Runs the target compiler.
	 */
	public static ParseResult parseAst(String jarPath, String[] args,
			BiConsumer<Object, Function<String, Class<?>>> rootConsumer) {
		try {
			LoadedJar ljar = loadJar(jarPath);

			// Find the main method we are looking for and invoke the method to get the new
			// root.
			try {
				long start = System.currentTimeMillis();
				Object prevRoot = ljar.getASTRoot();
				JSONArray captures = null;
				try {
					System.setProperty("java.security.manager", "allow");
					try {
						SystemExitControl.disableSystemExit();
					} catch (UnsupportedOperationException uoe) {
						uoe.printStackTrace();
						captures = StdIoInterceptor.performDefaultCapture(() -> {
							System.err.println("Failed installing System.exit interceptor");
							System.err.println("Restart code-prober.jar with the system property 'java.security.manager=allow'");
							System.err.println("Example:");
							System.err.println("   java -Djava.security.manager=allow -jar path/to/code-prober.jar path/to/your/analyzer-or-compiler.jar");
						});
						return new ParseResult(false, captures, ljar::getClientStyles);
					}

					final AtomicReference<Exception> innerError = new AtomicReference<>();
					captures = StdIoInterceptor.performDefaultCapture(() -> {
						try {
							ljar.mainMth.invoke(null, new Object[] { args });
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							innerError.set(e);
						}
					});
					if (innerError.get() != null) {
						throw innerError.get();
					}
				} catch (InvocationTargetException e) {
					System.out.println("ASTPRovider caught " + e);
					e.printStackTrace();
					System.out.println("target: " + e.getTargetException());
					if (!(e.getTargetException() instanceof SystemExitControl.ExitTrappedException)) {
						e.printStackTrace();
						System.err.println(
								"compiler error : " + (e.getMessage() != null ? e.getMessage() : e.getCause()));
						return new ParseResult(false, captures, ljar::getClientStyles);
					}
				} finally {
					SystemExitControl.enableSystemExit();
					System.out.printf("Compiler finished after : %d ms%n", (System.currentTimeMillis() - start));
				}
				Object root = ljar.getASTRoot();
				if (root == prevRoot) {
					// Parse ended without unexpected error (System.exit is expected), but nothing changed
					System.out.println("DrAST_root_node didn't change after main invocation, treating this as a parse failure.");
					System.out.println("If you perform semantic checks and call System.exit(..) if you get errors, then please do so *after* assigning DrAST_root_node");
					System.out.println("I.e do 1: parse. 2: update DrAST_root_node. 3: perform semantic checks (optional)");
					return new ParseResult(false, captures, ljar::getClientStyles);
				}
				rootConsumer.accept(root, otherCls -> {
//					System.out.println("Load underlying class: " + otherCls);
					try {
						return Class.forName(otherCls, true, ljar.classLoader);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				});
				return new ParseResult(true, captures, ljar::getClientStyles);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} finally {
				SystemExitControl.enableSystemExit();
			}
		} catch (NoSuchMethodException e) {
			System.err.println("Could not find the compiler's main method.");
		} catch (NoSuchFieldException e) {
			System.err.println("Could not find the compiler's main method.");
		} catch (FileNotFoundException e) {
			System.err.println("Could not find jar file, check path");
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		SystemExitControl.enableSystemExit();
		return new ParseResult(false, null, null);
	}
}
