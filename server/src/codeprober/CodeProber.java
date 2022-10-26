package codeprober;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import codeprober.ast.AstNode;
import codeprober.rpc.JsonRequestHandler;
import codeprober.server.WebServer;
import codeprober.server.WebSocketServer;
import codeprober.util.FileMonitor;
import codeprober.util.VersionInfo;

public class CodeProber {

	public static void printUsage() {
		System.out.println(
				"Usage: java -jar code-prober.jar path/to/your/analyzer-or-compiler.jar [args-to-forward-to-your-main]");
	}

	private static String loadTextFile(String filename) {
		try {
			byte[] src_bytes = Files.readAllBytes(Paths.get(filename));
			return new String(src_bytes, "utf-8");
		} catch (java.io.IOException __) {
			return null;
		}
	}

	public static void main(String[] mainArgs) {
		System.out.println("Starting server, version: " + VersionInfo.getInstance().toString() + "..");
		if (mainArgs.length == 0) {
			printUsage();
			System.exit(1);
		}

		// Configuration with Teal specifics
		Map<String, String> client_config = new HashMap<>();
		client_config.put("language", "teal");
		String src = loadTextFile(mainArgs[mainArgs.length - 1]);
		if (src != null) {
			src = client_config.put("source", src);
		}
		AstNode.setBeaverLeftmostColumn(0);
		// ----------------------------------------

		final String jarPath = mainArgs[0];
		final JsonRequestHandler handler = new DefaultRequestHandler(jarPath,
				Arrays.copyOfRange(mainArgs, 1, mainArgs.length - 1));

		new Thread(WebServer::start).start();

		final List<Runnable> onJarChangeListeners = Collections.<Runnable>synchronizedList(new ArrayList<>());
		new Thread(() -> WebSocketServer.start(client_config,
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
