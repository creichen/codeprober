package codeprober.protocol.data;

import org.json.JSONObject;

public class InitInfo implements codeprober.util.JsonUtil.ToJsonable {
  public final String type;
  public final protocolgen_spec_InitInfo_1 version;
  public final Integer changeBufferTime;
  public final Integer workerProcessCount;
  public final Boolean disableVersionCheckerByDefault;
  public final BackingFile backingFile;
  public final InitSettings initSettings;
  public InitInfo(protocolgen_spec_InitInfo_1 version, Integer changeBufferTime, Integer workerProcessCount, Boolean disableVersionCheckerByDefault, BackingFile backingFile) {
    this(version, changeBufferTime, workerProcessCount, disableVersionCheckerByDefault, backingFile, null);
  }
  public InitInfo(protocolgen_spec_InitInfo_1 version, Integer changeBufferTime, Integer workerProcessCount, Boolean disableVersionCheckerByDefault) {
    this(version, changeBufferTime, workerProcessCount, disableVersionCheckerByDefault, null, null);
  }
  public InitInfo(protocolgen_spec_InitInfo_1 version, Integer changeBufferTime, Integer workerProcessCount) {
    this(version, changeBufferTime, workerProcessCount, null, null, null);
  }
  public InitInfo(protocolgen_spec_InitInfo_1 version, Integer changeBufferTime) {
    this(version, changeBufferTime, null, null, null, null);
  }
  public InitInfo(protocolgen_spec_InitInfo_1 version) {
    this(version, null, null, null, null, null);
  }
  public InitInfo(protocolgen_spec_InitInfo_1 version, Integer changeBufferTime, Integer workerProcessCount, Boolean disableVersionCheckerByDefault, BackingFile backingFile, InitSettings initSettings) {
    this.type = "init";
    this.version = version;
    this.changeBufferTime = changeBufferTime;
    this.workerProcessCount = workerProcessCount;
    this.disableVersionCheckerByDefault = disableVersionCheckerByDefault;
    this.backingFile = backingFile;
    this.initSettings = initSettings;
  }

  public static InitInfo fromJSON(JSONObject obj) {
    codeprober.util.JsonUtil.requireString(obj.getString("type"), "init");
    return new InitInfo(
      protocolgen_spec_InitInfo_1.fromJSON(obj.getJSONObject("version"))
    , obj.has("changeBufferTime") ? (obj.getInt("changeBufferTime")) : null
    , obj.has("workerProcessCount") ? (obj.getInt("workerProcessCount")) : null
    , obj.has("disableVersionCheckerByDefault") ? (obj.getBoolean("disableVersionCheckerByDefault")) : null
    , obj.has("backingFile") ? (BackingFile.fromJSON(obj.getJSONObject("backingFile"))) : null
    , obj.has("initSettings") ? (InitSettings.fromJSON(obj.getJSONObject("initSettings"))) : null
    );
  }
  public JSONObject toJSON() {
    JSONObject _ret = new JSONObject();
    _ret.put("type", type);
    _ret.put("version", version.toJSON());
    if (changeBufferTime != null) _ret.put("changeBufferTime", changeBufferTime);
    if (workerProcessCount != null) _ret.put("workerProcessCount", workerProcessCount);
    if (disableVersionCheckerByDefault != null) _ret.put("disableVersionCheckerByDefault", disableVersionCheckerByDefault);
    if (backingFile != null) _ret.put("backingFile", backingFile.toJSON());
    if (initSettings != null) _ret.put("initSettings", initSettings.toJSON());
    return _ret;
  }
}
