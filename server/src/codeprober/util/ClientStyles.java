package codeprober.util;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Client CSS styles
 */
public class ClientStyles {
	private static final String LIGHT = "light";
	private static final String DARK = "dark";

	private Map<String, StyleSpec> specs = new HashMap<>();
	public ClientStyles(String[] stylespec) {
		if (stylespec != null) {
			for (String s : stylespec) {
				this.add(s);
			}
		}
	}

	public boolean
	isEmpty() {
		return this.specs.isEmpty();
	}

	@Override
	public boolean
	equals(Object other) {
		if (!(other instanceof ClientStyles)) {
			return false;
		}
		ClientStyles otherStyles = (ClientStyles) other;
		return this.specs.equals(otherStyles.specs);
	}

	public void add(String spec) {
		String[] elts = spec.split("=", 2);
		if (elts.length != 2) {
			System.err.println("Bad style spec: '" + spec + "'");
			return;
		}
		String key = elts[0];
		final String body = elts[1];
		String mode = null;
		elts = key.split("#", 2);
		if (elts.length == 2) {
			key = elts[0];
			mode = elts[1];
		}

		if (!this.specs.containsKey(key)) {
			this.specs.put(key, new StyleSpec());
		}
		if (!this.specs.get(key).set(mode, body)) {
			System.err.println("Bad style mode '" + mode + "' in: '" + spec + "'");
		}
	}

	public JSONArray toJSON() {
		ArrayList<JSONObject> items = new ArrayList<>();
		for (Map.Entry<String, StyleSpec> element : this.specs.entrySet()) {
			items.add(element.getValue().toJSON(element.getKey()));
		}
		return new JSONArray(items);
	}

	private static class StyleSpec {
		private String defaultCSS;
		private String darkCSS;
		private String lightCSS;

		public boolean set(String mode, String body) {
			if (mode == null) {
				this.defaultCSS = body;
			} else if (mode.equals(LIGHT)) {
				this.lightCSS = body;
			} else if (mode.equals(DARK)) {
				this.darkCSS = body;
			} else {
				return false;
			}
			return true;
		}

		public JSONObject toJSON(String key) {
			final JSONObject j = new JSONObject();
			j.put("clientID", key);

			String dark = this.darkCSS;
			if (dark == null) {
				dark = this.defaultCSS;
			}
			if (dark == null) {
				dark = "{}"; // dark is required
			}
			j.put("darkCSS", dark);

			String light = this.lightCSS;
			if (light == null) {
				light = this.defaultCSS;
			}
			if (light != null) {
				j.put("lightCSS", light);
			}

			return j;
		}

		private static final boolean
		eq(Object l, Object r) {
			if (l == null) {
				return r == null;
			}
			if (r == null) {
				return false;
			}
			return l.equals(r);
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof StyleSpec)) {
				return false;
			}
			StyleSpec otherSpec = (StyleSpec) other;
			return eq(this.defaultCSS, otherSpec.defaultCSS)
				&& eq(this.lightCSS, otherSpec.lightCSS)
				&& eq(this.darkCSS, otherSpec.darkCSS);
		}
	}
}
