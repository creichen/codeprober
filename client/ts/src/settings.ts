import { getAppropriateFileSuffix } from "./model/syntaxHighlighting";
import WindowState from './model/WindowState';
import { TextSpanStyle } from "./ui/create/createTextSpanIndicator";
import UIElements from './ui/UIElements';
import decodeDefaultProbes from './model/decodeDefaultProbes';
import { InitSettings } from "./protocol";


interface Settings extends InitSettings {
  mainArgsOverride?: string[] | null;
  probeWindowStates?: WindowState[];
}

let settingsObj: Settings | null = null;
let defaultSettings: Settings = {};
let overrideSettings: Settings = {};

const applyDefaults = (settings: Settings): Settings => {
  return { ...defaultSettings, ...settings, ...overrideSettings };
}

const clearHashFromLocation = () => history.replaceState('', document.title, `${window.location.pathname}${window.location.search}`);

window.saveStateAsUrl = () => {
  const encoded = encodeURIComponent(JSON.stringify(settings.get()));
  // delete location.hash;'
  // console.log('loc:', location.toString());
  navigator.clipboard.writeText(
    `${window.location.origin}${window.location.pathname}${window.location.search}${window.location.search.length === 0 ? '?' : '&'}settings=${encoded}`
  );
  const btn = new UIElements().saveAsUrlButton;
  const saveText = btn.textContent;
  setTimeout(() => {
    btn.textContent = saveText;
    btn.style.border = 'unset';
    delete (btn.style as any).border;
  }, 1000);
  btn.textContent = `Copied to clipboard`;
  btn.style.border = '1px solid green'
}

const settings = {
  getWithoutDefaults: (): Settings => {
    if (!settingsObj) {
      let settingsMatch: RegExpExecArray | null;
      if ((settingsMatch = /[?&]settings=[^?&]+/.exec(location.search)) != null) {
        const trimmedSearch = settingsMatch.index === 0
          ? (
            settingsMatch[0].length < location.search.length
              ? `?${location.search.slice(settingsMatch[0].length + 1)}`
              : `${location.search.slice(0, settingsMatch.index)}${location.search.slice(settingsMatch.index + settingsMatch[0].length)}`
          )
          : `${location.search.slice(0, settingsMatch.index)}${location.search.slice(settingsMatch.index + settingsMatch[0].length)}`
        ;

        history.replaceState('', document.title, `${window.location.pathname}${trimmedSearch}`);
        try {
          settingsObj = JSON.parse(decodeURIComponent(settingsMatch[0].slice(`?settings=`.length)))
          clearHashFromLocation();
          if (settingsObj) {
            settings.set(settingsObj);
          }
        } catch (e) {
          console.warn('Invalid windowState in hash', e);
        }
      }
      if (!settingsObj) {
        try {
          // TODO remove 'pasta-settings' fallback after an appropriate amount of time
          settingsObj = JSON.parse(localStorage.getItem('codeprober-settings') || localStorage.getItem('pasta-settings') || '{}');
        } catch (e) {
          console.warn('Bad data in localStorage, resetting settings', e);
          settingsObj = {};
        }
      }
    }
    return settingsObj || {};
  },
  get: (): Settings => {
    return applyDefaults(settings.getWithoutDefaults());
  },
  set: (newSettings: Settings) => {
    settingsObj = newSettings;
    localStorage.setItem('codeprober-settings', JSON.stringify(settingsObj));
  },
  setDefaults: (newDefaultSettings: Settings, newOverrideSettings: Settings) => {
    defaultSettings = newDefaultSettings;
    overrideSettings = newOverrideSettings;
  },

  getEditorContents: () => settings.get().editorContents,
  setEditorContents: (editorContents: string) => settings.set({ ...settings.getWithoutDefaults(), editorContents }),

  isLightTheme: () => settings.get().lightTheme ?? false,
  setLightTheme: (lightTheme: boolean) => settings.set({ ...settings.getWithoutDefaults(), lightTheme }),

  shouldDuplicateProbeOnAttrClick: () => settings.get().duplicateProbeOnAttrClick ?? true,
  setShouldDuplicateProbeOnAttrClick: (duplicateProbeOnAttrClick: boolean) => settings.set({ ...settings.getWithoutDefaults(), duplicateProbeOnAttrClick }),
  shouldCaptureStdio: () => settings.get().captureStdio ?? true,
  setShouldCaptureStdio: (captureStdio: boolean) => settings.set({ ...settings.getWithoutDefaults(), captureStdio }),
  shouldCaptureTraces: () => settings.get().captureTraces ?? false,
  setShouldCaptureTraces: (captureTraces: boolean) => settings.set({ ...settings.getWithoutDefaults(), captureTraces }),
  shouldAutoflushTraces: () => settings.get().autoflushTraces ?? true,
  setShouldAutoflushTraces: (autoflushTraces: boolean) => settings.set({ ...settings.getWithoutDefaults(), autoflushTraces }),

  getPositionRecoveryStrategy: () => settings.get().positionRecoveryStrategy ?? 'ALTERNATE_PARENT_CHILD',
  setPositionRecoveryStrategy: (positionRecoveryStrategy: string) => settings.set({ ...settings.getWithoutDefaults(), positionRecoveryStrategy }),

  getAstCacheStrategy: () => settings.get().astCacheStrategy ?? 'PARTIAL',
  setAstCacheStrategy: (astCacheStrategy: string) => settings.set({ ...settings.getWithoutDefaults(), astCacheStrategy }),

  getDefaultProbes:(): WindowState[] => {
    const default_states = settings.get().defaultProbes ?? "";
    return decodeDefaultProbes(default_states);
  },

  getProbeWindowStates: (): WindowState[] => {
    const ret = settings.get().probeWindowStates ?? [];

    return ret.map((item) => {
      if (typeof item.data === 'undefined') {
	console.log('must upgrade');
        // Older variant of this data, upgrade it
        return {
          modalPos: item.modalPos,
          data: {
            type: 'probe',
            locator: (item as any).locator, // as any to access previously typed data
            property: (item as any).property, // as any to access previously typed data
            nested: {},
          }
        };
      }
      return item;
    });
  },
  setProbeWindowStates: (probeWindowStates: WindowState[]) => settings.set({ ...settings.getWithoutDefaults(),
									     probeWindowStates: probeWindowStates.filter((w) => w.isDefault != true) }),

  getSyntaxHighlighting: () => settings.get().syntaxHighlighting ?? 'java',
  setSyntaxHighlighting: (syntaxHighlighting: SyntaxHighlightingLanguageId) => settings.set({ ...settings.getWithoutDefaults(), syntaxHighlighting }),

  getDisableUI: () => (settings.get().disableUI ?? '').split(',').filter(s => s),

  getMainArgsOverride: () => settings.get().mainArgsOverride ?? null,
  setMainArgsOverride: (mainArgsOverride: string[] | null) => settings.set({ ...settings.getWithoutDefaults(), mainArgsOverride }),

  getCustomFileSuffix: () => settings.get().customFileSuffix ?? null,
  setCustomFileSuffix: (customFileSuffix: string | null) => settings.set({ ...settings.getWithoutDefaults(), customFileSuffix }),
  getCurrentFileSuffix: (): string => settings.getCustomFileSuffix() ?? `.${getAppropriateFileSuffix(settings.getSyntaxHighlighting())}`,

  shouldShowAllProperties: () => settings.get().showAllProperties ?? false,
  setShouldShowAllProperties: (showAllProperties: boolean) => settings.set({ ...settings.getWithoutDefaults(), showAllProperties }),

  getLocationStyle: () => settings.get().locationStyle ?? 'full',
  setLocationStyle: (locationStyle: TextSpanStyle | null) => settings.set({ ...settings.getWithoutDefaults(), locationStyle }),

  shouldHideSettingsPanel: () => settings.get()?.hideSettingsPanel ?? false,
  setShouldHideSettingsPanel: (shouldHide: boolean) => settings.set({ ...settings.getWithoutDefaults(), hideSettingsPanel: shouldHide }),

  shouldEnableTesting: () => window.location.search.includes('enableTesting=true'),

  isReadOnlyMode: () => settings.get().readOnly ?? false,
  setReadOnlyMode: (readOnly: boolean) => settings.set({ ...settings.getWithoutDefaults(), readOnly }),

  isChangeTrackingMode: () => settings.get().changeTracking ?? false,
  setChangeTrackingMode: (changeTracking: boolean) => settings.set({ ...settings.getWithoutDefaults(), changeTracking }),

  // Either change tracking mode or no read-only mode:
  isEditingAllowed: () => settings.isChangeTrackingMode() || !settings.isReadOnlyMode()
};

export default settings;
