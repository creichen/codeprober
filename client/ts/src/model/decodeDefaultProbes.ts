import WindowState from './WindowState';
import { WindowStateData, WindowStateDataProbe, WindowStateDataAst, NestedWindows, WindowStateDataMinimized } from './WindowState';
// import WindowStateDataProbe from './WindowState';
// import WindowStateDataMinimized from './WindowState';

const decodeDefaultProbes = (encoding: string) : WindowState[] => {
  return (encoding.split(',')).flatMap((s : string) => {
    const spec = decodeURI(s).split(':');
    const show = spec && (spec.length <= 2
                       || spec[2] !== 'off');
    if (spec && spec.length > 1) {
      const data : WindowStateDataProbe = {
	type: 'probe',
	locator: {
	  result: {
	    external: false,
	    depth: 0,
	    start: 0,
	    end: 0,
	    type: spec[0]
	  },
	  steps: [],
	},
	property: { name: spec[1] },
	nested: {},
	showDiagnostics: show,
      };
      const min_data : WindowStateDataMinimized = {
	type: 'minimized-probe',
	data
      };
      const result : WindowState = {
	modalPos: { x: 0, y: 0 },
	data: min_data,
	isDefault: true
      };
      return [result];
    }
    console.log("Invalid spec:", spec, 'from encoding:', encoding);
    return [];
  })
}

export default decodeDefaultProbes;
