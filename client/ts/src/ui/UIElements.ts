
class UIElements {
  _registry: { [key: string]: HTMLElement } = {};
  _disabled: { [key: string]: boolean } = {};

  disable(name: string): void {
    this._disabled[name] = true;
    console.log('Disabling:', name)
    if (name in this._registry) {
      const elt = this._registry[name].parentElement;
      if (elt) {
	console.log(' -> disabling:', elt)
	elt.style.display = 'none';
      }
    }
  }

  enable(name: string): void {
    this._disabled[name] = false;
    if (name in this._registry) {
      const elt = this._registry[name].parentElement;
      if (elt) {
	console.log(' -> enabling:', elt)
	elt.style.display = 'block';
      }
    }
  }

  getElt(name: string): HTMLElement | null {
    let elt = document.getElementById(name);
    if (elt) {
      this._registry[name] = elt;
      if (name in this._disabled) {
	if (this._disabled[name]) {
	  this.disable(name); // updates the element's style
	} else {
	  this.enable(name);
	}
      }
    } else {
      console.warn('No such element: "' + name + '"')
    }
    return elt;
  }

  // Use lazy getters since the dom elements haven't been loaded
  // by the time this script initially runs.
  get positionRecoverySelector() { return this.getElt('control-position-recovery-strategy') as HTMLSelectElement; }
  get positionRecoveryHelpButton() { return this.getElt('control-position-recovery-strategy-help') as HTMLButtonElement; }

  get astCacheStrategySelector() { return this.getElt('ast-cache-strategy') as HTMLSelectElement; }
  get astCacheStrategyHelpButton() { return this.getElt('control-ast-cache-strategy-help') as HTMLButtonElement; }

  get syntaxHighlightingSelector() { return this.getElt('syntax-highlighting') as HTMLSelectElement; }
  get syntaxHighlightingHelpButton() { return this.getElt('control-syntax-highlighting-help') as HTMLButtonElement; }

  get shouldOverrideMainArgsCheckbox() { return this.getElt('control-should-override-main-args') as HTMLInputElement; }
  get configureMainArgsOverrideButton() { return this.getElt('configure-main-args') as HTMLButtonElement; }
  get mainArgsOverrideHelpButton() { return this.getElt('main-args-override-help') as HTMLButtonElement; }

  get shouldCustomizeFileSuffixCheckbox() { return this.getElt('control-customize-file-suffix') as HTMLInputElement; }
  get configureCustomFileSuffixButton() { return this.getElt('customize-file-suffix') as HTMLButtonElement; }
  get customFileSuffixHelpButton() { return this.getElt('customize-file-suffix-help') as HTMLButtonElement; }

  get showAllPropertiesCheckbox() { return this.getElt('control-show-all-properties') as HTMLInputElement; }
  get showAllPropertiesHelpButton() { return this.getElt('show-all-properties-help') as HTMLButtonElement; }

  get duplicateProbeCheckbox() { return this.getElt('control-duplicate-probe-on-attr') as HTMLInputElement; }
  get duplicateProbeHelpButton() { return this.getElt('duplicate-probe-on-attr-help') as HTMLButtonElement; }

  get captureStdoutCheckbox() { return this.getElt('control-capture-stdout') as HTMLInputElement; }
  get captureStdoutHelpButton() { return this.getElt('capture-stdout-help') as HTMLButtonElement; }

  get captureTracesCheckbox() { return this.getElt('control-capture-traces') as HTMLInputElement; }
  get captureTracesHelpButton() { return this.getElt('capture-traces-help') as HTMLButtonElement; }

  get autoflushTracesCheckbox() { return this.getElt('control-autoflush-traces') as HTMLInputElement; }
  get autoflushTracesContainer() { return this.getElt('container-autoflush-traces') as HTMLDivElement; }

  get locationStyleSelector() { return this.getElt('location-style') as HTMLSelectElement; }
  get locationStyleHelpButton() { return this.getElt('control-location-style-help') as HTMLButtonElement; }

  get generalHelpButton() { return this.getElt('display-help') as HTMLButtonElement; }
  get saveAsUrlButton() { return this.getElt('saveAsUrl') as HTMLButtonElement; }
  get darkModeCheckbox() { return this.getElt('control-dark-mode') as HTMLInputElement; }
  get readOnlyCheckbox() { return this.getElt('control-read-only-mode') as HTMLInputElement; }
  get changeTrackingCheckbox() { return this.getElt('control-change-tracking-mode') as HTMLInputElement; }
  get revertEditsButton() { return this.getElt('revert-edits-button') as HTMLButtonElement; }
  get displayStatisticsButton() { return this.getElt('display-statistics') as HTMLButtonElement; }
  get displayWorkerStatusButton() { return this.getElt('display-worker-status') as HTMLButtonElement; }
  get versionInfo() { return this.getElt('version') as HTMLDivElement; }
  get settingsHider() { return this.getElt('settings-hider') as HTMLButtonElement; }
  get settingsRevealer() { return this.getElt('settings-revealer') as HTMLButtonElement; }
  get showTests() { return this.getElt('show-tests') as HTMLButtonElement; }
  get minimizedProbeArea() { return this.getElt('minimized-probe-area') as HTMLDivElement; }
}

export default UIElements;
