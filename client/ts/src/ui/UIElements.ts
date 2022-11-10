
class UIElements {
  // TODO: Declarative table of UI elements plus lazy initialisation so we don't have to rely on getters being used before calls to disable/enable, can show list of options

  // Remembers the UI element that allows toggling this feature on/off
  _registry: { [key: string]: HTMLElement } = {};

  init() {
    this.controlPanel; // Populate _registry with reference to 'control-panel'
    const uie = this;
    window.codeprober_enable_full_ui = function() {
      for (const [key] of Object.entries(uie._registry)) {
	uie.enable(key);
      }
    }
  }

  // Disable UI element by name.
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

  // Enable UI element by name.
  enable(name: string): void {
    if (name in this._registry) {
      this._registry[name].style.display = 'block';
    }
  }

  // Get element that can be disabled directly
  getElt(name: string): HTMLElement | null {
    let elt = document.getElementById(name);
    if (elt) {
      this._registry[name] = elt;
    } else {
      console.warn('No such element: "' + name + '"')
    }
    return elt;
  }

  // Get element whose parent element should be used for disabling
  getEltInDiv(name: string): HTMLElement | null {
    let elt = document.getElementById(name);
    if (elt) {
      const parent = elt.parentElement;
      if (parent) {
	this._registry[name] = parent;
      }
    } else {
      console.warn('No such element: "' + name + '"')
    }
    return elt;
  }

  get controlPanel() { return this.getElt('control-panel') as HTMLButtonElement; }

  // Use lazy getters since the dom elements haven't been loaded
  // by the time this script initially runs.
  get positionRecoverySelector() { return this.getEltInDiv('control-position-recovery-strategy') as HTMLSelectElement; }
  get positionRecoveryHelpButton() { return this.getEltInDiv('control-position-recovery-strategy-help') as HTMLButtonElement; }

  get astCacheStrategySelector() { return this.getEltInDiv('ast-cache-strategy') as HTMLSelectElement; }
  get astCacheStrategyHelpButton() { return this.getEltInDiv('control-ast-cache-strategy-help') as HTMLButtonElement; }

  get syntaxHighlightingSelector() { return this.getEltInDiv('syntax-highlighting') as HTMLSelectElement; }
  get syntaxHighlightingHelpButton() { return this.getEltInDiv('control-syntax-highlighting-help') as HTMLButtonElement; }

  get shouldOverrideMainArgsCheckbox() { return this.getEltInDiv('control-should-override-main-args') as HTMLInputElement; }
  get configureMainArgsOverrideButton() { return this.getEltInDiv('configure-main-args') as HTMLButtonElement; }
  get mainArgsOverrideHelpButton() { return this.getEltInDiv('main-args-override-help') as HTMLButtonElement; }

  get shouldCustomizeFileSuffixCheckbox() { return this.getEltInDiv('control-customize-file-suffix') as HTMLInputElement; }
  get configureCustomFileSuffixButton() { return this.getEltInDiv('customize-file-suffix') as HTMLButtonElement; }
  get customFileSuffixHelpButton() { return this.getEltInDiv('customize-file-suffix-help') as HTMLButtonElement; }

  get showAllPropertiesCheckbox() { return this.getEltInDiv('control-show-all-properties') as HTMLInputElement; }
  get showAllPropertiesHelpButton() { return this.getEltInDiv('show-all-properties-help') as HTMLButtonElement; }

  get duplicateProbeCheckbox() { return this.getEltInDiv('control-duplicate-probe-on-attr') as HTMLInputElement; }
  get duplicateProbeHelpButton() { return this.getEltInDiv('duplicate-probe-on-attr-help') as HTMLButtonElement; }

  get captureStdoutCheckbox() { return this.getEltInDiv('control-capture-stdout') as HTMLInputElement; }
  get captureStdoutHelpButton() { return this.getEltInDiv('capture-stdout-help') as HTMLButtonElement; }

  get locationStyleSelector() { return this.getEltInDiv('location-style') as HTMLSelectElement; }
  get locationStyleHelpButton() { return this.getEltInDiv('control-location-style-help') as HTMLButtonElement; }

  get generalHelpButton() { return this.getElt('display-help') as HTMLButtonElement; }
  get darkModeCheckbox() { return this.getElt('control-dark-mode') as HTMLInputElement; }
  get displayStatisticsButton() { return this.getElt('display-statistics') as HTMLButtonElement; }
  get versionInfo() { return this.getElt('version') as HTMLDivElement; }
}

export default UIElements;
