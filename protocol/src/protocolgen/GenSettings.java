package protocolgen;
import java.util.stream.Stream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Given a bunch of typescript files, heuristically extract enum type declarations
 *
 */
public class GenSettings {
	final static String SRC_PATH = GenSettings.getOpt("TS_SRC_DIR");
	final static String GLOBALS = "globals.d.ts";

	public static String getOpt(String opt) {
		final String v = System.getProperty(opt);
		if (null == v) {
			System.err.println("Must specify -D" + opt + "=...");
			System.exit(1);
		}
		return v;
	}

	public static void main(String[] args) throws IOException {
		String dest_file = GenSettings.getOpt("JAVA_DST_FILE");
		TSDeclarations decls = new TSDeclarations();

		try (Stream<Path> paths = Files.walk(Paths.get(SRC_PATH))) {
		    paths.filter(p -> p.toFile().isFile() && p.toString().endsWith("ts"))
			.forEach(fpath -> {
				decls.incorporateDeclsFromSource(fpath);
			    });
		};

		try (BufferedWriter wr = Files.newBufferedWriter(Paths.get(dest_file))) {
			wr.write("// Generated by GenSettings; DO NOT EDIT\n");
			wr.write("package protocolgen;\n");
			wr.write("public class TsSrcEnums {\n");
			wr.write("\tstatic {\n");
			for (String etype : new TreeSet<>(decls.type_env.keySet())) {
				TSEnum ts_enum = decls.type_env.get(etype);
				String options = ts_enum.options.stream()
					.map(s -> "\"" + s + "\"")
					.collect(Collectors.joining(", "));
				wr.write(//"\t\tenums.put(\"" + etype + "\",
					 "\t\tnew TsEnumRef("
					 + "\"" + etype + "\", "
					 + ts_enum.quotedSrcFile() + ", new String[] {" + options + "});\n");
			}
			wr.write("\t}\n");
			wr.write("}\n");
		} catch (IOException exn) {
			exn.printStackTrace();
			System.exit(1);
		}
	}

	static class TSEnum {
		public final List<? extends String> options;
		public final String src_file;

		public TSEnum(String src_file, List<? extends String> options) {
			if (src_file.startsWith(SRC_PATH)) {
				src_file = src_file.substring(SRC_PATH.length());
			}
			this.src_file = src_file;
			this.options = options;
		}

		/**
		 * Quoted string that represents the file name this type is defined in, or the string "null" if the definition is global
		 */
		public String quotedSrcFile() {
			if (src_file.equals(GLOBALS)) {
				return "null";
			}
			return "\"" + this.src_file + "\"";
		}

	}

	static class TSDeclarations {
		public final Map<String, TSEnum> type_env = new HashMap<>();

		public void incorporateDeclsFromSource(Path file) {
			try {
				TSDeclarationLineParser parser = new TSDeclarationLineParser(Files.readAllLines(file),
											     file.toString());
				parser.parse();
			} catch (IOException exn) {
				exn.printStackTrace();
			}
		}

		class TSDeclarationLineParser {
			List<String> lines;
			int line_nr = 0;
			String filename;

			public TSDeclarationLineParser(List<String> lines, String filename) {
				this.lines = lines;
				this.filename = filename;
			}

			public String
			next() {
				if (this.hasNext()) {
					return this.lines.get(line_nr++);
				}
				return null;
			}

			public boolean
			hasNext() {
				return line_nr < this.lines.size();
			}

			/**
			 * Parses input file line by line, searching for magic markers, and delegating to type parsers
			 * for detail parsing.
			 */
			public void parse() {
				String l;
				do {
					l = this.next();
					if (null == l) {
						break;
					}
					final String ls = l.trim();
					String magic_comment = null;
					if (ls.startsWith("type")) {
						TokenStream ts = new TokenStream(ls);
						if (ts.peek().ty == TkTy.TYPE) {
							parseTypeDecl(ts);
						}
					}
				} while (null != l);
			}

			public void
			parseTypeDecl(TokenStream ts) {
				TypeDeclParser parser = new TypeDeclParser(ts);
				if (parser.parse()) {
					type_env.put(parser.getTypeName(),
						     new TSEnum(this.filename,
								parser.getEnumOptions()));
				}
			}

			public void
			abort(String msg) {
				System.err.println(filename + " L" + this.line_nr + ": " + msg);
				System.exit(1);
			}

			class TypeDeclParser {
				private TokenStream ts;
				private String type_name;
				private List<String> enum_options;

				public TypeDeclParser(TokenStream ts) {
					this.ts = ts;
				}

				public String getTypeName() {
					return this.type_name;
				}
				public List<? extends String> getEnumOptions() {
					return this.enum_options;
				}

				Token expect(TkTy ty) {
					Token tk = this.ts.next();
					if (tk.ty != ty) {
						throw new ParseException();
					}
					return tk;
				}

                                /* TYPE ID = STRINGSEQ ; */
				void parseDecl() {
					expect(TkTy.TYPE);
					String id = expect(TkTy.ID).str;
					expect(TkTy.EQ);
					List<String> options = parseStringSeq();
					expect(TkTy.SEMICOLON);
					this.type_name = id;
					this.enum_options = options;
				}

				/* STRINGSEQ ::= STRING '|' STRINGSEQ
				               | STRING
				*/
				List<String> parseStringSeq() {
					ArrayList<String> options = new ArrayList<>();
					while (true) {
						switch (ts.peek().ty) {
						case SEMICOLON:
							return options;
						case STRING:
							options.add(ts.next().str);
							if (ts.peek().ty != TkTy.SEMICOLON) {
								expect(TkTy.VBAR);
							}
							break;
						default:
							throw new ParseException();
						}
					}
				}

				public boolean parse() {
					try {
						parseDecl();
						return true;
					} catch (ParseException __) {
						return false;
					}
				}
			}

			/**
			 * Breaks the input into a token stream.  This is quite approximate, but enough
			 * for our purposes.
			 */
			class TokenStream {
				private LinkedList<Token> tokens = new LinkedList();

				public TokenStream(String first_line) {
					this.addTokensFrom(first_line);
				}

				public Token peek() {
					while (tokens.isEmpty()) {
						if (!TSDeclarationLineParser.this.hasNext()) {
							// end of file
							abort("Reached end-of-file after type decl start");
						}
						nextTokens();
					}
					if (tokens.isEmpty()) {
						abort("Reached end-of-file after type decl start (2)");
					}
					return tokens.peek();
				}

				public Token next() {
					this.peek();
					return tokens.pop();
				}

				private void addTokensFrom(String line) {
					if (line == null) {
						throw new RuntimeException("null line");
					}
					String string_mode = null; // non-null iff we are aggregating a string

					StringTokenizer tokenizer = new StringTokenizer(line,
											" \n;=|'", true);
					while (tokenizer.hasMoreTokens()) {
						Token tk = null;

						String next_token = tokenizer.nextToken();

						// Reading a string?  Keep reading until closing single quote
						if (null != string_mode) {
							if (next_token.equals("'")) {
								tokens.add(Token.str(string_mode));
								string_mode = null;
							} else {
								string_mode += next_token;
							}
							continue;
						}

						switch (next_token) {
						case "type":
							tk = Token.make(TkTy.TYPE);
							break;
						case ";":
							tk = Token.make(TkTy.SEMICOLON);
							break;
						case "=":
							tk = Token.make(TkTy.EQ);
							break;
						case "|":
							tk = Token.make(TkTy.VBAR);
							break;
						case "'":
							string_mode = "";
							break;
						case " ":
						case "\n":
							break;
						default:
							tk = Token.id(next_token);
						};
						if (tk != null) {
							this.tokens.add(tk);
						}
					}
					//System.err.println(" --> " + this.tokens);
				}

				private void nextTokens() {
					this.addTokensFrom(TSDeclarationLineParser.this.next());
				}
			}
		}
	}


	// Parsing support
	static enum TkTy {
		TYPE,
		ID,
		SEMICOLON,
		STRING,
		VBAR, // "|"
		EQ,
	}
	static class Token {
		public final TkTy ty;
		public final String str;

		public static Token
		make(TkTy ty) {
			return new Token(ty, null);
		}

		public static Token
		id(String s) {
			return new Token(TkTy.ID, s);
		}

		public static Token
		str(String s) {
			return new Token(TkTy.STRING, s);
		}

		public Token(TkTy ty, String str) {
			this.ty = ty;
			this.str = str;
		}
		public String toString() {
			if (this.str == null) {
				return this.ty.toString();
			}
			return this.ty.toString() + "(" + this.str + ")";
		}
	}

	static class ParseException extends RuntimeException {};
}