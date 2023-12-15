package codeprober.util;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import codeprober.protocol.data.InitSettings;

public class ParsedArgs {
	public static enum ConcurrencyMode {
		DISABLED, COORDINATOR, WORKER,
	}

	public final boolean runTest;
	public final ConcurrencyMode concurrencyMode;
	public final Integer workerProcessCount;
	public final String jarPath;
	public final String[] extraArgs;
	public final String oneshotRequest;
	public final File oneshotOutput;
	public final InitSettings defaultSettings;
	public final InitSettings overrideSettings;

	public ParsedArgs(boolean runTest, ConcurrencyMode concurrencyMode, Integer workerProcessCount, String jarPath,
			  String[] extraArgs, String oneshotRequest, File oneshotOutput,
			  InitSettings defaultSettings, InitSettings overrideSettings)	{
		this.runTest = runTest;
		this.concurrencyMode = concurrencyMode;
		this.workerProcessCount = workerProcessCount;
		this.jarPath = jarPath;
		this.extraArgs = extraArgs;
		this.oneshotRequest = oneshotRequest;
		this.oneshotOutput = oneshotOutput;
		this.defaultSettings = defaultSettings;
		this.overrideSettings = overrideSettings;
	}

	public static void printUsage() {
		DataStructOpt<InitSettings> defaultSettingsParser = new DataStructOpt<>(InitSettings.class);
		System.out.println(
				"Usage: java -jar code-prober.jar [--test] [--concurrent=N] [path/to/your/analyzer-or-compiler.jar [args-to-forward-to-your-main]]");

		System.out.println("\nAdditional options:");
		List<String[]> help = new ArrayList<>();
		defaultSettingsParser.addHelp(help);
		int len_left = 0, len_mid = 0;
		for (String[] option : help) {
			len_left = Integer.max(len_left, option[0].length());
			len_mid = Integer.max(len_mid, option[1].length());
		}

		// Characters before explanation text in help
		int pad_size = len_left + len_mid + 2 + 4;

		int MAX_WIDTH = 160; // max chars that fit in terminal
		int rhs_available = MAX_WIDTH - pad_size;

		for (String[] option : help) {
			String right = option[2];
			List<String> next_lines = null;
			// Too wide?
			if (right.length() > rhs_available
			    // Is it even worth trying to fix this?
			    && rhs_available > 24) {
				next_lines = DataStructOpt.splitStringByWidth(right, rhs_available);
				if (next_lines.size() > 0) {
					right = next_lines.get(0);
					next_lines.remove(0);
				}
			}
			System.out.println(String.format("  %-" +len_left+ "s %" + len_mid + "s   %s",
							 option[0],
							 option[1],
							 right));
			if (next_lines != null) {
				for (String rest : next_lines) {
					System.out.println(String.format("  %-" + pad_size + "s%s",
									 "",
									 rest));
				}
			}
		}
		System.out.println("\nTo override user settings, use '--force-X' instead of '--X'.");
		System.out.println("\n--default-probes takes a comma-separated list of the form <nodetype>:<attr>[:<visibility>], where <visibility> can be 'on' (default) or 'off'\n");
	}

	public static ParsedArgs parse(String[] args) {
		boolean runTests = false;
		AtomicReference<ConcurrencyMode> concurrency = new AtomicReference<>(ConcurrencyMode.DISABLED);
		final Consumer<ConcurrencyMode> setConcurrencyMode = (mode -> {
			if (concurrency.get() != ConcurrencyMode.DISABLED) {
				throw new IllegalArgumentException(
						"Can only specify either '--concurrent', '--concurrent=[processCount]' or '--worker', not multiple options simultaneously");
			}
			concurrency.set(mode);
		});

		String jarPath = null;
		String[] extraArgs = null;
		Integer workerCount = null;
		String oneshotRequest = null;
		File oneshotOutput = null;
		DataStructOpt<InitSettings> defaultSettingsParser = new DataStructOpt<>(InitSettings.class);
		DataStructOpt<InitSettings> overrideSettingsParser = new DataStructOpt<>(InitSettings.class).withOptPrefix("force");
		DataStructOpt<?>[] settingsParsers = new DataStructOpt<?>[] {
			defaultSettingsParser,
			overrideSettingsParser
		};

		gatherArgs: for (int i = 0; i < args.length; ++i) {
			switch (args[i]) {
			case "--test": {
				if (runTests) {
					throw new IllegalArgumentException("Duplicate '--test'");
				}
				runTests = true;
				break;
			}
			case "--concurrent": {
				setConcurrencyMode.accept(ConcurrencyMode.COORDINATOR);
				break;
			}
			case "--worker": {

				setConcurrencyMode.accept(ConcurrencyMode.WORKER);
				break;
			}
			case "--help": {
				printUsage();
				System.exit(1);
				break;
			}

			default: {
				if (args[i].startsWith("--oneshot=")) {
					if (oneshotRequest != null) {
						throw new IllegalArgumentException("Cannot specify multiple --oneshot values");
					}
					oneshotRequest = args[i].substring("--oneshot=".length());
					continue;
				}
				if (args[i].startsWith("--output=")) {
					if (oneshotOutput != null) {
						throw new IllegalArgumentException("Cannot specify multiple --output values");
					}
					oneshotOutput = new File(args[i].substring("--output=".length())).getAbsoluteFile();
					if (oneshotOutput.getParentFile() == null || !oneshotOutput.getParentFile().exists()) {
						throw new IllegalArgumentException(
								"Directory of output file ('" + oneshotOutput + "') does not exist");
					}
					continue;
				}
				if (args[i].startsWith("--concurrent=")) {
					setConcurrencyMode.accept(ConcurrencyMode.COORDINATOR);
					try {
						workerCount = Integer.parseInt(args[i].substring("--concurrent=".length()));
						if (workerCount <= 0) {
							throw new IllegalArgumentException("Minimum worker count is 1, got '" + workerCount + "'");
						}
					} catch (NumberFormatException e) {
						System.out.println("Invalid value for '--concurrent'");
						e.printStackTrace();
						System.exit(1);
					}
					continue;
				}
				for (DataStructOpt<?> settingsParser : settingsParsers) {
					if (settingsParser.matches(args[i]) == DataStructOpt.OPT_MATCH) {
						try {
							settingsParser.set(args[i], null);
						} catch (DataStructOpt.OptException exn) {
							throw new IllegalArgumentException(exn.toString());
						}
						continue gatherArgs;
					}
				}

				jarPath = args[i];
				extraArgs = Arrays.copyOfRange(args, i + 1, args.length);
				break gatherArgs;
			}
			}
		}
		if (oneshotRequest != null) {
			if (jarPath == null) {
				throw new IllegalArgumentException("Must specify analyzer-or-compiler.jar when using --oneshot");
			}
			if (runTests) {
				throw new IllegalArgumentException("Cannot run tests and handle oneshot requests at the same time");
			}
			if (concurrency.get() != ConcurrencyMode.DISABLED) {
				throw new IllegalArgumentException("Concurrency is not supported for oneshot requests");
			}
			if (oneshotOutput == null) {
				throw new IllegalArgumentException("Must specify --output= as well when running oneshot requests");
			}
		} else {
			if (oneshotOutput != null) {
				throw new IllegalArgumentException("--output= is only valid together with --oneshot=");
			}
		}
		return new ParsedArgs(runTests, concurrency.get(), workerCount, jarPath, extraArgs, oneshotRequest,
				      oneshotOutput,
				      defaultSettingsParser.get(),
				      overrideSettingsParser.get()
			);
	}
}
