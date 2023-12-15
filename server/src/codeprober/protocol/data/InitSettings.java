package codeprober.protocol.data;

import org.json.JSONObject;

public class InitSettings implements codeprober.util.JsonUtil.ToJsonable {
  public final String editorContents;
  public final Boolean lightTheme;
  public final Boolean captureStdio;
  public final Boolean captureTraces;
  public final Boolean autoflushTraces;
  public final Boolean duplicateProbeOnAttrClick;
  public final Boolean showAllProperties;
  public final String positionRecoveryStrategy;
  public final String astCacheStrategy;
  @codeprober.protocol.EnumString(options = {"plaintext", "abap", "apex", "azcli", "bat", "bicep", "cameligo", "clojure", "coffeescript", "c", "cpp", "csharp", "csp", "css", "dart", "dockerfile", "ecl", "elixir", "flow9", "fsharp", "go", "graphql", "handlebars", "hcl", "html", "ini", "java", "javascript", "julia", "kotlin", "less", "lexon", "lua", "liquid", "m3", "markdown", "mips", "msdax", "mysql", "objective-c", "pascal", "pascaligo", "perl", "pgsql", "php", "postiats", "powerquery", "powershell", "proto", "pug", "python", "qsharp", "r", "razor", "redis", "redshift", "restructuredtext", "ruby", "rust", "sb", "scala", "scheme", "scss", "shell", "sol", "aes", "sparql", "sql", "st", "swift", "systemverilog", "verilog", "tcl", "twig", "teal", "typescript", "vb", "xml", "yaml", "json"})
  public final String syntaxHighlighting;
  public final String customFileSuffix;
  @codeprober.protocol.EnumString(options = {"full", "full-compact", "lines", "lines-compact", "start", "start-line"})
  public final String locationStyle;
  public final Boolean hideSettingsPanel;
  public final Boolean readOnly;
  public final Boolean changeTracking;
  public final String defaultProbes;
  public final String disableUI;
  public final Boolean groupPropertiesByAspect;
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy, String astCacheStrategy, String syntaxHighlighting, String customFileSuffix, String locationStyle, Boolean hideSettingsPanel, Boolean readOnly, Boolean changeTracking, String defaultProbes, String disableUI) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, positionRecoveryStrategy, astCacheStrategy, syntaxHighlighting, customFileSuffix, locationStyle, hideSettingsPanel, readOnly, changeTracking, defaultProbes, disableUI, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy, String astCacheStrategy, String syntaxHighlighting, String customFileSuffix, String locationStyle, Boolean hideSettingsPanel, Boolean readOnly, Boolean changeTracking, String defaultProbes) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, positionRecoveryStrategy, astCacheStrategy, syntaxHighlighting, customFileSuffix, locationStyle, hideSettingsPanel, readOnly, changeTracking, defaultProbes, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy, String astCacheStrategy, String syntaxHighlighting, String customFileSuffix, String locationStyle, Boolean hideSettingsPanel, Boolean readOnly, Boolean changeTracking) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, positionRecoveryStrategy, astCacheStrategy, syntaxHighlighting, customFileSuffix, locationStyle, hideSettingsPanel, readOnly, changeTracking, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy, String astCacheStrategy, String syntaxHighlighting, String customFileSuffix, String locationStyle, Boolean hideSettingsPanel, Boolean readOnly) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, positionRecoveryStrategy, astCacheStrategy, syntaxHighlighting, customFileSuffix, locationStyle, hideSettingsPanel, readOnly, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy, String astCacheStrategy, String syntaxHighlighting, String customFileSuffix, String locationStyle, Boolean hideSettingsPanel) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, positionRecoveryStrategy, astCacheStrategy, syntaxHighlighting, customFileSuffix, locationStyle, hideSettingsPanel, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy, String astCacheStrategy, String syntaxHighlighting, String customFileSuffix, String locationStyle) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, positionRecoveryStrategy, astCacheStrategy, syntaxHighlighting, customFileSuffix, locationStyle, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy, String astCacheStrategy, String syntaxHighlighting, String customFileSuffix) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, positionRecoveryStrategy, astCacheStrategy, syntaxHighlighting, customFileSuffix, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy, String astCacheStrategy, String syntaxHighlighting) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, positionRecoveryStrategy, astCacheStrategy, syntaxHighlighting, null, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy, String astCacheStrategy) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, positionRecoveryStrategy, astCacheStrategy, null, null, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, positionRecoveryStrategy, null, null, null, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, showAllProperties, null, null, null, null, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, duplicateProbeOnAttrClick, null, null, null, null, null, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces) {
    this(editorContents, lightTheme, captureStdio, captureTraces, autoflushTraces, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces) {
    this(editorContents, lightTheme, captureStdio, captureTraces, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio) {
    this(editorContents, lightTheme, captureStdio, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme) {
    this(editorContents, lightTheme, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents) {
    this(editorContents, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }
  public InitSettings() {
    this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }
  public InitSettings(String editorContents, Boolean lightTheme, Boolean captureStdio, Boolean captureTraces, Boolean autoflushTraces, Boolean duplicateProbeOnAttrClick, Boolean showAllProperties, String positionRecoveryStrategy, String astCacheStrategy, String syntaxHighlighting, String customFileSuffix, String locationStyle, Boolean hideSettingsPanel, Boolean readOnly, Boolean changeTracking, String defaultProbes, String disableUI, Boolean groupPropertiesByAspect) {
    this.editorContents = editorContents;
    this.lightTheme = lightTheme;
    this.captureStdio = captureStdio;
    this.captureTraces = captureTraces;
    this.autoflushTraces = autoflushTraces;
    this.duplicateProbeOnAttrClick = duplicateProbeOnAttrClick;
    this.showAllProperties = showAllProperties;
    this.positionRecoveryStrategy = positionRecoveryStrategy;
    this.astCacheStrategy = astCacheStrategy;
    this.syntaxHighlighting = syntaxHighlighting;
    this.customFileSuffix = customFileSuffix;
    this.locationStyle = locationStyle;
    this.hideSettingsPanel = hideSettingsPanel;
    this.readOnly = readOnly;
    this.changeTracking = changeTracking;
    this.defaultProbes = defaultProbes;
    this.disableUI = disableUI;
    this.groupPropertiesByAspect = groupPropertiesByAspect;
  }

  public static InitSettings fromJSON(JSONObject obj) {
    return new InitSettings(
      obj.has("editorContents") ? (obj.getString("editorContents")) : null
    , obj.has("lightTheme") ? (obj.getBoolean("lightTheme")) : null
    , obj.has("captureStdio") ? (obj.getBoolean("captureStdio")) : null
    , obj.has("captureTraces") ? (obj.getBoolean("captureTraces")) : null
    , obj.has("autoflushTraces") ? (obj.getBoolean("autoflushTraces")) : null
    , obj.has("duplicateProbeOnAttrClick") ? (obj.getBoolean("duplicateProbeOnAttrClick")) : null
    , obj.has("showAllProperties") ? (obj.getBoolean("showAllProperties")) : null
    , obj.has("positionRecoveryStrategy") ? (obj.getString("positionRecoveryStrategy")) : null
    , obj.has("astCacheStrategy") ? (obj.getString("astCacheStrategy")) : null
    , obj.has("syntaxHighlighting") ? (codeprober.util.JsonUtil.requireString(obj.getString("syntaxHighlighting"), "plaintext", "abap", "apex", "azcli", "bat", "bicep", "cameligo", "clojure", "coffeescript", "c", "cpp", "csharp", "csp", "css", "dart", "dockerfile", "ecl", "elixir", "flow9", "fsharp", "go", "graphql", "handlebars", "hcl", "html", "ini", "java", "javascript", "julia", "kotlin", "less", "lexon", "lua", "liquid", "m3", "markdown", "mips", "msdax", "mysql", "objective-c", "pascal", "pascaligo", "perl", "pgsql", "php", "postiats", "powerquery", "powershell", "proto", "pug", "python", "qsharp", "r", "razor", "redis", "redshift", "restructuredtext", "ruby", "rust", "sb", "scala", "scheme", "scss", "shell", "sol", "aes", "sparql", "sql", "st", "swift", "systemverilog", "verilog", "tcl", "twig", "teal", "typescript", "vb", "xml", "yaml", "json")) : null
    , obj.has("customFileSuffix") ? (obj.getString("customFileSuffix")) : null
    , obj.has("locationStyle") ? (codeprober.util.JsonUtil.requireString(obj.getString("locationStyle"), "full", "full-compact", "lines", "lines-compact", "start", "start-line")) : null
    , obj.has("hideSettingsPanel") ? (obj.getBoolean("hideSettingsPanel")) : null
    , obj.has("readOnly") ? (obj.getBoolean("readOnly")) : null
    , obj.has("changeTracking") ? (obj.getBoolean("changeTracking")) : null
    , obj.has("defaultProbes") ? (obj.getString("defaultProbes")) : null
    , obj.has("disableUI") ? (obj.getString("disableUI")) : null
    , obj.has("groupPropertiesByAspect") ? (obj.getBoolean("groupPropertiesByAspect")) : null
    );
  }
  public JSONObject toJSON() {
    JSONObject _ret = new JSONObject();
    if (editorContents != null) _ret.put("editorContents", editorContents);
    if (lightTheme != null) _ret.put("lightTheme", lightTheme);
    if (captureStdio != null) _ret.put("captureStdio", captureStdio);
    if (captureTraces != null) _ret.put("captureTraces", captureTraces);
    if (autoflushTraces != null) _ret.put("autoflushTraces", autoflushTraces);
    if (duplicateProbeOnAttrClick != null) _ret.put("duplicateProbeOnAttrClick", duplicateProbeOnAttrClick);
    if (showAllProperties != null) _ret.put("showAllProperties", showAllProperties);
    if (positionRecoveryStrategy != null) _ret.put("positionRecoveryStrategy", positionRecoveryStrategy);
    if (astCacheStrategy != null) _ret.put("astCacheStrategy", astCacheStrategy);
    if (syntaxHighlighting != null) _ret.put("syntaxHighlighting", syntaxHighlighting);
    if (customFileSuffix != null) _ret.put("customFileSuffix", customFileSuffix);
    if (locationStyle != null) _ret.put("locationStyle", locationStyle);
    if (hideSettingsPanel != null) _ret.put("hideSettingsPanel", hideSettingsPanel);
    if (readOnly != null) _ret.put("readOnly", readOnly);
    if (changeTracking != null) _ret.put("changeTracking", changeTracking);
    if (defaultProbes != null) _ret.put("defaultProbes", defaultProbes);
    if (disableUI != null) _ret.put("disableUI", disableUI);
    if (groupPropertiesByAspect != null) _ret.put("groupPropertiesByAspect", groupPropertiesByAspect);
    return _ret;
  }
}
