import { NodeLocator, Property } from '../protocol';

interface WindowStateDataProbe {
  type: 'probe';
  locator: NodeLocator;
  property: Property;
  nested: NestedWindows;
  showDiagnostics?: boolean;
  stickyHighlight?: string;
  isDefault: boolean; // default probes are not serialised in .cpr
}
interface WindowStateDataAst {
  type: 'ast';
  locator: NodeLocator;
  direction: 'upwards' | 'downwards';
  transform: { [id: string]: number },
  filterText?: string;
}
interface WindowStateDataMinimized {
  type: 'minimized-probe';
  data: WindowStateDataProbe;
}

type WindowStateData = WindowStateDataProbe | WindowStateDataAst | WindowStateDataMinimized;

function isDefaultProbeData(probe: WindowStateData): boolean {
  switch (probe.type) {
    case 'probe':           return probe.isDefault === true;
    case 'minimized-probe': return isDefaultProbeData(probe.data);
    default:                return false;
  }
};

// "Bespoke" probes are manually created probes that we should preserve,
// as opposed to default probes that are imposed by the workspace settings.
function isBespokeProbe(probe: WindowState): boolean {
  return !isDefaultProbeData(probe.data);
};


interface WindowState {
  modalPos: ModalPosition;
  data: WindowStateData;
}

type NestedWindows = { [key: string]: { data: WindowStateData }[], };

export { WindowStateData, WindowStateDataProbe, WindowStateDataAst, NestedWindows, isBespokeProbe, WindowState };
export default WindowState;
