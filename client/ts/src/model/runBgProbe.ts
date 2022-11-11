import { customCSSName, setCustomCSS } from "../ui/CustomCSS";

class BgProbe {
  static probeCounter = 0;

  env: ModalEnv;
  id: string;

  localProbeMarkers : ProbeMarker[] = []; // "regular" markers (flexible hover text, must be error|warning|hint|info)
  localStickies : string[] = [];    // "sticky" markers (css styling)

  constructor(env: ModalEnv) {
    this.env = env;
    this.id = `invisible-probe-${BgProbe.probeCounter++}`
    env.probeMarkers[this.id] = this.localProbeMarkers;
  }

  // Remove all sticky markers
  clearStickyMarkers() {
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

  // Process Rpc update: refresh all markers
  update(res: RpcResponse) {
    const prevLen = this.localProbeMarkers.length;

    // drop probe markers, but do not refresh yet
    this.localProbeMarkers.length = 0;

    this.clearStickyMarkers();

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

const runBgProbe = (env: ModalEnv, locator: NodeLocator, attr: AstAttrWithValue) => {
  const bgProbe = new BgProbe(env);

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
    if (state === 'loading') {
      reloadOnDone = true;
    } else {
      performRpc();
    }
  };
  env.onChangeListeners[bgProbe.id] = refresh;
  refresh();
};

export default runBgProbe;
