import { customCSSName, setCustomCSS } from "../ui/CustomCSS";

class BgProbe {
  static probeCounter = 0;

  env: ModalEnv;
  id: string;
  enabled: boolean;
  refresh: () => void;
  name: string;
  description: string;

  localProbeMarkers : ProbeMarker[] = []; // "regular" markers (flexible hover text, must be error|warning|hint|info)
  localStickies : string[] = [];    // "sticky" markers (css styling)

  constructor(env: ModalEnv, name: string, config: BGProbeConfig) {
    this.env = env;
    this.name = name;
    this.description = config?.description;
    this.id = `invisible-probe-${BgProbe.probeCounter++}`
    this.enabled = !(config?.enabled === '-');
    this.refresh = () => {console.log("bad refresh");};
    env.probeMarkers[this.id] = this.localProbeMarkers;
  }

  setEnabled(enable: boolean) {
    if (enable != this.enabled) {
      this.enabled = enable;
      if (enable) {
	// enable
	this.refresh();
      } else {
	// disable
	this.clearMarkers();
	this.env.updateMarkers();
      }
    }
  }

  // Remove all markers
  clearMarkers() {
    // probe markers
    this.localProbeMarkers.length = 0;

    // sticky markers
    this.localStickies.forEach(this.env.clearStickyHighlight);
    this.localStickies.length = 0;
  }

  // Add and track "sticky" marker
  addStickyMarker(highlight: StickyHighlight) {
    const sticky_id = this.id + '.' + this.localStickies.length.toString();
    this.localStickies.push(sticky_id);

    var localizedClasses : string[] = [];

    // translate local CSS names to global names
    highlight.classNames.forEach((clientCSSID) => {
      localizedClasses.push(customCSSName(clientCSSID));
    });
    highlight.classNames = localizedClasses;

    this.env.setStickyHighlight(sticky_id, highlight);
  }

  createControlMenu(): HTMLElement {
    const elt_name = 'bg-probe-control-for-' + this.name;

    const div = document.createElement('div');
    const checkbox = document.createElement('input');
    const label = document.createElement('label');
    label.htmlFor = elt_name;
    label.appendChild(document.createTextNode(this.description));
    checkbox['type'] = 'checkbox';
    checkbox.id = elt_name;
    checkbox.checked = this.enabled;
    const bgProbe = this;
    checkbox.onchange = () => {
      bgProbe.setEnabled(checkbox.checked);
    }
    div.appendChild(checkbox);
    div.appendChild(label);
    return div;
  }

  // Process Rpc update: refresh all markers
  update(res: RpcResponse) {
    const prevLen = this.localProbeMarkers.length;

    // drop probe markers, but do not refresh yet
    this.clearMarkers();

    // Update to stylesheets
    if (res.clientStyles) {
      setCustomCSS(res.clientStyles);
    }

    [res.errors, res.reports].forEach((reportlist : IssueReport[]) =>
      reportlist.forEach(({ severity, highlightClasses, start: errStart, end: errEnd, msg }) => {
	if (errEnd === 0) {
	  // Mark Down Everything!
	  errEnd = -1;
	}
	if (severity) {
	  // Regular issue report
          this.localProbeMarkers.push({ severity, errStart, errEnd, msg });
	}
	if (highlightClasses) {
	  // Sticky marker: no label text
	  this.addStickyMarker({ classNames: highlightClasses,
				 span: startEndToSpan(errStart, errEnd )});
	}
      }))
    if (prevLen !== 0 || this.localProbeMarkers.length !== 0) {
      // refresh env markers, if needed-- this will refresh markers from all probes
      this.env.updateMarkers();
    }
  }
}

const runBgProbe = (env: ModalEnv, config: BGProbeConfig, locator: NodeLocator, attr: AstAttrWithValue) => {
  const bgProbe = new BgProbe(env, attr.name, config);

  let state: 'loading' | 'idle' = 'idle';
  let reloadOnDone = false;
  const onRpcDone = () => {
    state = 'idle';
    if (reloadOnDone) {
      reloadOnDone = false;
      performRpc();
    }
  }
  const performRpc = () => {
    state = 'loading';
    env.performRpcQuery({
      attr,
      locator,
      bgProbe: true,
    })
      .then((res: RpcResponse) => {
	bgProbe.update(res);
        onRpcDone();
      })
      .catch((err) => {
        console.warn('Failed refreshing invisible probe', err);
        onRpcDone();
      });
  };

  const refresh = () => {
    if (bgProbe.enabled) {
      if (state === 'loading') {
	reloadOnDone = true;
      } else {
	performRpc();
      }
    }
  };
  env.onChangeListeners[bgProbe.id] = refresh;
  bgProbe.refresh = refresh;
  refresh();

  return bgProbe;
};

export default runBgProbe;
