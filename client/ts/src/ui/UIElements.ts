
class UIElements {
  _registry: { [key: string]: HTMLElement } = {};

  disable(name: string): void {
    if (name in this._registry) {
      this._registry[name].style.display = 'none';
    } else {
      console.warn('No such element: "' + name + '"');
      for (const [key] of Object.entries(this._registry)) {
	console.log(" - " + key);
      }
    }
  }

  getElt(name: string): HTMLElement | null {
    let elt = document.getElementById(name);
    if (elt) {
      this._registry[name] = elt;
    } else {
      console.warn('No such element: "' + name + '"')
    }
    return elt;
  }

  // Use lazy getters since the dom elements haven't been loaded
  // by the time this script initially runs.
  get positionRecoverySelector() { return this.getElt('control-position-recovery-strategy') as HTMLSelectElement; }
  get positionRecoveryHelpButton() { return this.getElt('control-position-recovery-strategy-help') as HTMLButtonElement; }

  get astCacheStrategySelector() { return this.getElt('ast-cache-strategy') as HTMLSelectElement; }
  get astCacheStrategyHelpButton() { return this.getElt('control-ast-cache-strategy-help') as HTMLButtonElement; }

  get syntaxHighlightingSelector() { return this.getElt('syntax-highlighting') as HTMLSelectElement; }
  get syntaxHighlightingHelpButton() { return this.getElt('control-syntax-highlighting-help') as HTMLButtonElement; }

  get shouldOverrideMainArgsCheckbox() { return this.getElt('control-should-override-main-args') as HTMLInputElement; }
  get configureMainArgsOverrideButton() { return this.getElt('configure-main-args') as HTMLButtonElement; }
  get mainArgsOverrideHelpButton() { return this.getElt('main-args-override-help') as HTMLButtonElement; }

  get shouldCustomizeFileSuffixCheckbox() { return this.getElt('control-customize-file-suffix') as HTMLInputElement; }
  get configureCustomFileSuffixButton() { return this.getElt('customize-file-suffix') as HTMLButtonElement; }
  get customFileSuffixHelpButton() { return this.getElt('customize-file-suffix-help') as HTMLButtonElement; }

  get showAllPropertiesCheckbox() { return this.getElt('control-show-all-properties') as HTMLInputElement; }
  get showAllPropertiesHelpButton() { return this.getElt('show-all-properties-help') as HTMLButtonElement; }

  get duplicateProbeCheckbox() { return this.getElt('control-duplicate-probe-on-attr') as HTMLInputElement; }
  get duplicateProbeHelpButton() { return this.getElt('duplicate-probe-on-attr-help') as HTMLButtonElement; }

  get captureStdoutCheckbox() { return this.getElt('control-capture-stdout') as HTMLInputElement; }
  get captureStdoutHelpButton() { return this.getElt('capture-stdout-help') as HTMLButtonElement; }

  get locationStyleSelector() { return this.getElt('location-style') as HTMLSelectElement; }
  get locationStyleHelpButton() { return this.getElt('control-location-style-help') as HTMLButtonElement; }

  get generalHelpButton() { return this.getElt('display-help') as HTMLButtonElement; }
  get darkModeCheckbox() { return this.getElt('control-dark-mode') as HTMLInputElement; }
  get displayStatisticsButton() { return this.getElt('display-statistics') as HTMLButtonElement; }
  get versionInfo() { return this.getElt('version') as HTMLDivElement; }
}

export default UIElements;
