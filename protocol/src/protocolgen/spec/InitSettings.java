package protocolgen.spec;
import protocolgen.Nullable;

public class InitSettings extends Streamable {
	public final Object editorContents = opt(String.class);
	public final Object lightTheme = opt(Boolean.class);
	public final Object captureStdio = opt(Boolean.class);
	public final Object captureTraces = opt(Boolean.class);
	public final Object autoflushTraces = opt(Boolean.class);
	public final Object duplicateProbeOnAttrClick = opt(Boolean.class);
	public final Object showAllProperties = opt(Boolean.class);
	public final Object positionRecoveryStrategy = opt(String.class);
	public final Object astCacheStrategy = opt(String.class);
	public final Object syntaxHighlighting = opt(fromTsEnum("SyntaxHighlightingLanguageId"));
	public final Object mainArgsOverride = opt(nullable(arr(String.class)));
	public final Object customFileSuffix = opt(nullable(String.class));
	public final Object locationStyle = opt(nullable(fromTsEnum("TextSpanStyle")));
	public final Object hideSettingsPanel = opt(Boolean.class);
	// probeWindowStates?: WindowState[];
}
