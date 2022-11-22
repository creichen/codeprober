
type HelpType = 'general' | 'recovery-strategy' | 'probe-window' | 'magic-stdout-messages'
  | 'ast-cache-strategy' | 'syntax-highlighting' | 'main-args-override' | 'customize-file-suffix'
  | 'property-list-usage' | 'show-all-properties' | 'duplicate-probe-on-attr' | 'capture-stdout'
  | 'location-style'
  ;

interface PackedLoc {
  start: number;
  end: number;
}

interface Span {
  lineStart: number;
  colStart: number;
  lineEnd: number;
  colEnd: number;
}

type EditorPreloader = () => {
  script: string[];
  style: string[];
  predicate: () => boolean;
};

type BGProbeConfig = {
  description: string;
  key: string,
  enabled: '+' | '-' | '' | 'force' ; // '-' disables the probe by default, 'force' does not offer a UI element for disabling it
}

type TextMarker = {
  clear?: () => void;
}
type TextMarkFn = (mark: Span & { severity: MarkerSeverity; message: string }) => TextMarker;

type LocationAdjuster = (line: number, col: number) => [number, number];

type SyntaxHighlightingLanguageId = 'plaintext' | 'abap' | 'apex' | 'azcli' | 'bat' | 'bicep' | 'cameligo' | 'clojure' | 'coffeescript' | 'c' | 'cpp' | 'csharp' | 'csp' | 'css' | 'dart' | 'dockerfile' | 'ecl' | 'elixir' | 'flow9' | 'fsharp' | 'go' | 'graphql' | 'handlebars' | 'hcl' | 'html' | 'ini' | 'java' | 'javascript' | 'julia' | 'kotlin' | 'less' | 'lexon' | 'lua' | 'liquid' | 'm3' | 'markdown' | 'mips' | 'msdax' | 'mysql' | 'objective-c' | 'pascal' | 'pascaligo' | 'perl' | 'pgsql' | 'php' | 'postiats' | 'powerquery' | 'powershell' | 'proto' | 'pug' | 'python' | 'qsharp' | 'r' | 'razor' | 'redis' | 'redshift' | 'restructuredtext' | 'ruby' | 'rust' | 'sb' | 'scala' | 'scheme' | 'scss' | 'shell' | 'sol' | 'aes' | 'sparql' | 'sql' | 'st' | 'swift' | 'systemverilog' | 'teal' | 'verilog' | 'tcl' | 'twig' | 'typescript' | 'vb' | 'xml' | 'yaml' | 'json';

type EditorInitializer = (initialValue: string, onChange: (newValue: string, adjusters?: LocationAdjuster[] ) => void, initialSyntaxHIghlight: SyntaxHighlightingLanguageId) => {
  setLocalState?: (newValue: string) => void;
  getLocalState?: () => string;
  updateSpanHighlight?: (baseHighlight: Span | null, stickyHighlights: StickyHighlight[]) => void;
  registerStickyMarker?: (initialSpan: Span) => StickyMarker;
  markText?: TextMarkFn;
  themeToggler: (isLightTheme: boolean) => void;
  syntaxHighlightingToggler: (langId: SyntaxHighlightingLanguageId) => void;
};

type MarkerSeverity = 'error' | 'warning' | 'info' | 'hint' | 'line-pa';
interface ProbeMarker {
  severity: MarkerSeverity;
  errStart: number;
  errEnd: number;
  msg: string;
}

interface StickyMarker {
  getSpan: () => Span;
  remove: () => void;
}

interface ProbeWindowState {
  modalPos: ModalPosition;
  locator: NodeLocator;
  attr: AstAttrWithValue
}

interface ProbeMeasurement {
  fullRpcMs: number;
  serverSideMs: number;
  serverParseOnlyMs: number;
  serverCreateLocatorMs: number;
  serverApplyLocatorMs: number;
  attrEvalMs: number;
}

interface ProbeStatisticsCollector {
  addProbeEvaluationTime: (measurement: ProbeMeasurement) => void;
};

interface StickyHighlight {
  classNames: string[];
  span: Span;
}
interface ModalEnv {
  performRpcQuery: (args: {
    attr: AstAttrWithValue;
    locator: NodeLocator;
    bgProbe? : boolean;
  }) => Promise<any>;
  getLocalState: () => string;
  updateSpanHighlight: (span: Span | null) => void;
  setStickyHighlight: (probeId: string, hl: StickyHighlight) => void;
  clearStickyHighlight: (probeId: string) => void;
  probeMarkers: { [probeId: string]: ProbeMarker[] };
  onChangeListeners: { [key: string]: (adjusters?: LocationAdjuster[]) => void };
  probeWindowStateSavers: { [key: string]: (target: ProbeWindowState[]) => void };
  triggerWindowSave: () => void;
  registerStickyMarker: (initialSpan: Span) => StickyMarker;
  updateMarkers: () => void;
  captureStdout: () => boolean;
  duplicateOnAttr: () => boolean;
  statisticsCollector: ProbeStatisticsCollector;
  currentlyLoadingModals: Set<string>;
}

type RpcBodyLine = string
  | { type: ('stdout' | 'stderr' | 'stream-arg'); value: string }
  | RpcBodyLine[]
  | { type: 'node'; value: NodeLocator }
  ;

interface AstAttrArg {
  type: string;
  name: string;
  detail: 'NORMAL' | 'AST_NODE' | 'OUTPUTSTREAM';
}

interface AstAttr {
  name: string;
  args?: AstAttrArg[];
  astChildName?: string;
}

type ArgValue = string | number | boolean | null | NodeLocator;

interface AstAttrWithValue {
  name: string;
  args?: (AstAttrArg & { value: ArgValue })[];
  'extract-reports'?: bool;
}

type NodeLocatorStep =
    { type: 'nta', value: { name: string; args: AstAttrWithValue[] } }
  | { type: 'child', value: number }
  | { type: 'tal', value: TypeAtLocStep }
  ;

interface TypeAtLoc extends PackedLoc {
  type: string;
  label?: string;
}

interface TypeAtLocStep extends TypeAtLoc {
  depth: number;
}

interface NodeLocator {
  // root: TypeAtLoc;
  result: TypeAtLoc;
  steps: NodeLocatorStep[];
}

interface IssueReport extends PackedLoc {
  severity: MarkerSeverity?;
  highlightClasses: string[]?; // CSS classes for "sticky" highlighting
  msg: string;
}

interface CustomCSS {
  clientID: string; // client-side string used to identify the CSS element
  darkCSS: string; // string-serialised CSS describing the element (e.g., "{ background-color:red; }")
  lightCSS: string?; // analogous for darkBody, for "light mode"
}

interface RpcResponse {
  body: RpcBodyLine[];
  args?: (Omit<AstAttrArg, 'name'> & { value: ArgValue })[];
  // args?: AstAttrWithValue['args'],
  locator: NodeLocator;
  // reports are extracted from node attributes
  reports: IssueReport[];
  // errors are extracted from stdio/stderr
  errors: IssueReport[];

  // Expected for request meta:listNodes
  nodes?: NodeLocator[];

  // Expected for requests meta:listAllProperties and meta:listProperties
  properties?: AstAttr[];
  clientStyles?: CustomCSS[];

  totalTime?: number;
  parseTime?: number;
  createLocatorTime?: number;
  applyLocatorTime?: number;
  attrEvalTime?: number;
}

interface ModalPosition { x: number, y: number };

interface Window {
  RagQuery: (line: number, col: number, autoSelectRoot?: boolean) => void;
  definedEditors: {
    [editorId: string]: {
      preload: EditorPreloader;
      init: EditorInitializer;
    };
  };
  loadPreload: (preloader: EditorPreloader, onDone: () => void) => void;
  displayHelp: (type: HelpType | 'probe-statistics') => void;
  initCodeProber: () => void;
  MiniEditorMain: () => void;
  clearUserSettings: () => void;
  ActiveLocatorRequest?: ActiveNodeLocatorRequest;
  codeprober_enable_full_ui: () => void; // enable all UI elements
}

interface ActiveNodeLocatorRequest {
  submit: (locator: NodeLocator) => void;
}



/*
  Battle plan for next week

  1)
  - Refactor away many references to type+span in the modals, replace with 'NodeLocator'

  2)
  - Make server respond with this structure too
      - In the meta info when responding to queries
      - In the 'encodeValue' representation of nodes
   For now, 'steps' is always empty

  3)
  - Implement 'getLocator' in the server.
      - Take the "deepest" (furthest from root) node in [Root,..,OurNode] that has a location
            - If no such node, fail 'getLocator'
      - We now have [Root,..,DeepestWithLoc,..,OurNode]
            - Best case, DeepestWithLoc == OurNode
            - [Root,..,DeepestWithloc] can be ignored.
            - Save and location of of 'DeepestWithLoc' for the response value.
      - Fill [Deepest,..,OurNode] with 'steps'. Happy case: the steps list is empty!
            - For each Parent/Child, encode relation between [P,C].
                - If C exists in the P.children array, make it a 'NodeLocatorChildStep'
                - Else, iterate over all >>cached<< NTA values in P
                    - Identity comparison over them. On match, create a 'NodeLocatorNtaStep'
                    - On zero matches, fail 'getLocator'

  4)
  - Use 'getLocator' instead of creating NodeLocator where 'steps' is always empty

  5)
  - Follow the 'steps' in requests to the server, re-evaluate NTA's as needed

  6)
  - Implement UI explaining this on the client side

  7)
  - Handle errors as best as possible.
    - NodeLocatorChildStep is possibly quite error prone on changes
*/
