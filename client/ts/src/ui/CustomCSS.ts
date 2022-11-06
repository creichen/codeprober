const customCSSPrefix = 'code-prober-client-custom-';

// Represents a custom CSS definition attached to the document root
// Not optimised for performance: assumes that we do not update very often
class CustomCSSElement {
  cssElement: HTMLElement | null;
  config: CustomCSS;

  constructor(config: CustomCSS) {
    this.config = config;

    let style = document.createElement('style');
    this.cssElement = style;

    style.type = 'text/css';
    let globalID = this.cssName();
    let styleBody = `.${globalID} ${config.darkCSS}\n`;
    if (config.lightCSS) {
      styleBody += `body[data-theme-light='true'] .${globalID} ${config.lightCSS}\n`;
    }
    style.innerHTML = styleBody;
    document.getElementsByTagName('head')[0].appendChild(style);
  }

  // Does the CSS setup match?
  cssMatches(config: CustomCSS): boolean {
    return (config.darkCSS == this.config.darkCSS
      && config.lightCSS == this.config.lightCSS);
  }

  // Gets the name that is used in the actual stylesheet
  cssName(): string {
    return customCSSName(this.config.clientID);
  }

  remove() {
    if (this.cssElement) {
      this.cssElement.parentNode?.removeChild(this.cssElement);
      this.config.clientID = '<deleted>';
      this.cssElement = null;
    }
  }
}

let registeredCSSElements = new Map<string, CustomCSSElement>();// : { [key: string]: CustomCSSElement } = {};

// Sets the current list of custom CSS styles
export function setCustomCSS(styles: CustomCSS[]) {
  let observedElements : string[] = [];

  styles.forEach(style => {
    let oldCSSElement = registeredCSSElements.get(style.clientID);
    if (oldCSSElement && !oldCSSElement.cssMatches(style)) {
      oldCSSElement.remove();
    }
    registeredCSSElements.set(style.clientID, new CustomCSSElement(style));
    observedElements.push(style.clientID);
  });

  const observedCSSElements = new Set(observedElements);

  registeredCSSElements.forEach(( elt, clientID ) => {
    if (!observedCSSElements.has(clientID)) {
      elt.remove();
    }
  });
}

export function customCSSName(clientID: string): string {
  return customCSSPrefix + clientID;
}

