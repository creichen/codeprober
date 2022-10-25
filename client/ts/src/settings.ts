import { getAppropriateFileSuffix } from "./model/syntaxHighlighting";
import { TextSpanStyle } from "./ui/create/createTextSpanIndicator";

interface Settings {
  editorContents?: string;
  lightTheme?: boolean;
  captureStdio?: boolean;
  duplicateProbeOnAttrClick?: boolean;
  showAllProperties?: boolean;
  positionRecoveryStrategy?: string;
  astCacheStrategy?: string;
  probeWindowStates?: ProbeWindowState[];
  syntaxHighlighting?: SyntaxHighlightingLanguageId;
  mainArgsOverride?: string[] | null;
  customFileSuffix?: string | null;
  locationStyle?: TextSpanStyle | null;
}

let settingsObj: Settings | null = null;

const settings = {
  get: (): Settings => {
    if (!settingsObj) {
      try {
        // TODO remove 'pasta-settings' fallback after an appropriate amount of time
        settingsObj = JSON.parse(localStorage.getItem('codeprober-settings') || localStorage.getItem('pasta-settings') || '{}');
      } catch (e) {
        console.warn('Bad data in localStorage, resetting settings', e);
        settingsObj = {};
      }
    }
    return settingsObj || {};
  },
  set: (newSettings: Settings) => {
    settingsObj = newSettings;
    localStorage.setItem('codeprober-settings', JSON.stringify(settingsObj));
  },

  getEditorContents: () => settings.get().editorContents,
  setEditorContents: (editorContents: string) => settings.set({ ...settings.get(), editorContents }),

  isLightTheme: () => settings.get().lightTheme ?? false,
  setLightTheme: (lightTheme: boolean) => settings.set({ ...settings.get(), lightTheme }),

  shouldDuplicateProbeOnAttrClick: () => settings.get().duplicateProbeOnAttrClick ?? true,
  setShouldDuplicateProbeOnAttrClick: (duplicateProbeOnAttrClick: boolean) => settings.set({ ...settings.get(), duplicateProbeOnAttrClick }),
  shouldCaptureStdio: () => settings.get().captureStdio ?? true,
  setShouldCaptureStdio: (captureStdio: boolean) => settings.set({ ...settings.get(), captureStdio }),

  getPositionRecoveryStrategy: () => settings.get().positionRecoveryStrategy ?? 'ALTERNATE_PARENT_CHILD',
  setPositionRecoveryStrategy: (positionRecoveryStrategy: string) => settings.set({ ...settings.get(), positionRecoveryStrategy }),

  getAstCacheStrategy: () => settings.get().astCacheStrategy ?? 'PARTIAL',
  setAstCacheStrategy: (astCacheStrategy: string) => settings.set({ ...settings.get(), astCacheStrategy }),

  getProbeWindowStates: () => settings.get().probeWindowStates ?? [],
  setProbeWindowStates: (probeWindowStates: ProbeWindowState[]) => settings.set({ ...settings.get(), probeWindowStates }),

  getSyntaxHighlighting: () => settings.get().syntaxHighlighting ?? 'java',
  setSyntaxHighlighting: (syntaxHighlighting: SyntaxHighlightingLanguageId) => settings.set({ ...settings.get(), syntaxHighlighting }),

  getMainArgsOverride: () => settings.get().mainArgsOverride ?? null,
  setMainArgsOverride: (mainArgsOverride: string[] | null) => settings.set({ ...settings.get(), mainArgsOverride }),

  getCustomFileSuffix: () => settings.get().customFileSuffix ?? null,
  setCustomFileSuffix: (customFileSuffix: string | null) => settings.set({ ...settings.get(), customFileSuffix }),
  getCurrentFileSuffix: (): string => settings.getCustomFileSuffix() ?? `.${getAppropriateFileSuffix(settings.getSyntaxHighlighting())}`,

  shouldShowAllProperties: () => settings.get().showAllProperties ?? false,
  setShouldShowAllProperties: (showAllProperties: boolean) => settings.set({ ...settings.get(), showAllProperties }),

  getLocationStyle: () => settings.get().locationStyle ?? 'full',
  setLocationStyle: (locationStyle: TextSpanStyle | null) => settings.set({ ...settings.get(), locationStyle }),
};

export default settings;
