package codeprober.protocol.data;

import org.json.JSONObject;

public class EdgeDiagnostic implements codeprober.util.JsonUtil.ToJsonable {
  public final codeprober.protocol.DiagnosticType type;
  public final NodeLocator startNode;
  public final NodeLocator endNode;
  public final String edgeInfo;
  public final String style;
  public EdgeDiagnostic(codeprober.protocol.DiagnosticType type, NodeLocator startNode, NodeLocator endNode, String edgeInfo, String style) {
    this.type = type;
    this.startNode = startNode;
    this.endNode = endNode;
    this.edgeInfo = edgeInfo;
    this.style = style;
  }

  public static EdgeDiagnostic fromJSON(JSONObject obj) {
    return new EdgeDiagnostic(
      codeprober.protocol.DiagnosticType.parseFromJson(obj.getString("type"))
    , NodeLocator.fromJSON(obj.getJSONObject("startNode"))
    , NodeLocator.fromJSON(obj.getJSONObject("endNode"))
    , obj.getString("edgeInfo")
    , obj.getString("style")
    );
  }
  public JSONObject toJSON() {
    JSONObject _ret = new JSONObject();
    _ret.put("type", type.name());
    _ret.put("startNode", startNode.toJSON());
    _ret.put("endNode", endNode.toJSON());
    _ret.put("edgeInfo", edgeInfo);
    _ret.put("style", style);
    return _ret;
  }
}
