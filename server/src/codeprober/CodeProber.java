package codeprober;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import java.nio.file.Files;
import java.nio.file.Paths;

import codeprober.rpc.JsonRequestHandler;
import codeprober.server.WebServer;
import codeprober.server.WebSocketServer;
import codeprober.util.FileMonitor;
import codeprober.util.VersionInfo;

import org.json.JSONArray;
import org.json.JSONObject;

public class CodeProber {
	/**
	 * Command line option.
	 */
	static abstract class Arg {
		private String name, description;

		public Arg(String name, String description) {
			this.name = name;
			this.description = description;
		}

		public boolean
		expectsArgument() {
			return false;
		}

		public String
		getName() { return this.name; }

		public String
		getDescription() { return this.description; }

		/**
		 * @param arg The argument following this option ("null" if no argument)
		 */
		public abstract void
		apply(JSONObject target, String arg);

		/**
		 * Pass argument as comma-separated list
		 */
		public static class Flag extends Arg {
			private Consumer<JSONObject> action;
			public Flag(String name, String descr, Consumer<JSONObject> action) {
				super(name, descr);
				this.action = action;
			}

			public void
			apply(JSONObject target, String arg) {
				this.action.accept(target);
			}
		}

	}
	/**
	 * Command ine option with arguments
	 */
	static abstract class ArgOption extends Arg {
		public ArgOption(String name, String description) {
			super(name, description);
		}

		@Override
		public boolean
		expectsArgument() {
			return true;
		}

		/**
		 * Pass argument as literal string
		 */
		public static class StringArg extends ArgOption {
			public StringArg(String name, String descr) { super(name, descr); }
			public void
			apply(JSONObject target, String arg) {
				target.put(this.getName(), arg);
			}
		}

		/**
		 * Pass argument as literal string
		 */
		public static class FileArg extends ArgOption {
			public FileArg(String name, String descr) { super(name, descr); }
			public void
			apply(JSONObject target, String arg) {
				try {
					byte[] src_bytes = Files.readAllBytes(Paths.get(arg));
					target.put(this.getName(),
						   new String(src_bytes, "utf-8"));
				} catch (java.io.IOException exn) {
					exn.printStackTrace();
				}
			}
		}

		/**
		 * Pass argument as comma-separated list
		 */
		public static class ListArg extends ArgOption {
			public ListArg(String name, String descr) { super(name, descr); }
			public void
			apply(JSONObject target, String arg) {
				JSONArray list = new JSONArray(arg.split(","));
				target.put(this.getName(), list);
			}
		}
	}

	static final Arg[] ARG_OPTIONS = new Arg[] {
		new Arg.Flag("no-jastadd-stdout", "Suppress stdout/stderr output from the client program",
			     (e -> { codeprober.metaprogramming.StreamInterceptor.SUPPRESS_STDERR_STDOUT = true; })),
		new ArgOption.StringArg("syntax", "Force specific syntax highlighting"),
		new ArgOption.StringArg("ast-cache", "Force specific ast-caching setup (FULL|PARTIAL|NONE|PURGE)"),
		new ArgOption.FileArg("source", "Load specific file, overriding browser-local storage"),
		new ArgOption.ListArg("autoprobes", "Comma-separated list of attributes on the root node to automatically extract and highlight"),
		new ArgOption.ListArg("disable-ui", "Comma-separated list of UI elements to disable.  (cf. UIElements.ts for a full list.)")
	};

	/**
	 * Returns the first non-option-argument index
	 *
	 * @param config Options to update
	 * @param First non-option mainArgs index
	 */
	public static int processArgs(JSONObject config, String[] mainArgs) {
		int startArgIndex = 0;
		while (startArgIndex < mainArgs.length && mainArgs[startArgIndex].startsWith("--")) {
			String argName = mainArgs[startArgIndex];
			startArgIndex += 1;
			if (mainArgs[startArgIndex].equals("--")) {
				// stop checking for args
				break;
			}

			argName = argName.substring(2);
			Arg argHandler = null;
			for (Arg opt : ARG_OPTIONS) {
				if (opt.getName().equals(argName)) {
					argHandler = opt;
					break;
				}
			}

			if (argHandler == null) {
				System.err.println("Unknown option --" + argName);
				printUsage();
				System.exit(1);
			}

			String arg = null;
			if (argHandler.expectsArgument()) {
				if (startArgIndex == mainArgs.length) {
					System.err.println("Missing argument to option --" + argName);
					printUsage();
					System.exit(1);
				}
				arg = mainArgs[startArgIndex++];
			}

			argHandler.apply(config, arg);
		}
		return startArgIndex;
	}

	public static void printUsage() {
		System.out.println(
				"Usage: java -jar code-prober.jar [args] path/to/your/analyzer-or-compiler.jar [args-to-forward-to-your-main]");
		System.out.println("Options:");
		for (Arg opt : ARG_OPTIONS) {
			System.out.println("\t--" + opt.getName()
					   + (opt.expectsArgument()? " x" : "  ")
					   + "   \t" + opt.getDescription());
		}
	}

	public static void main(String[] mainArgs) {
		System.out.println("Starting server, version: " + VersionInfo.getInstance().toString() + "..");

		JSONObject clientConfig = new JSONObject();
		int firstArg = processArgs(clientConfig, mainArgs);

		if (mainArgs.length == firstArg) {
			printUsage();
			System.exit(1);
		}
		final String jarPath = mainArgs[firstArg];
		final JsonRequestHandler handler = new DefaultRequestHandler(jarPath,
				Arrays.copyOfRange(mainArgs, firstArg + 1, mainArgs.length));

		new Thread(WebServer::start).start();

		final List<Runnable> onJarChangeListeners = Collections.<Runnable>synchronizedList(new ArrayList<>());
		new Thread(() -> WebSocketServer.start(clientConfig,
						       onJarChangeListeners, handler.createRpcRequestHandler())).start();

		new FileMonitor(new File(jarPath)) {
			public void onChange() {
				System.out.println("Jar changed!");
				synchronized (onJarChangeListeners) {
					onJarChangeListeners.forEach(Runnable::run);
				}
			};
		}.start();
	}
}
