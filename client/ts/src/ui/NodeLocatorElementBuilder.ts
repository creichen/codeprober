import ModalEnv from '../model/ModalEnv';
import { createMutableLocator } from '../model/UpdatableNodeLocator';
import createTextSpanIndicator from "./create/createTextSpanIndicator";
import registerOnHover from "./create/registerOnHover";
import registerNodeSelector from "./create/registerNodeSelector";
import displayAttributeModal from "./popup/displayAttributeModal";
import { LineDecorator, ExpanderCallback, ExtraEncodingArgs } from 'popup/encodeRpcBodyLines';
import { NodeLocator } from '../protocol';
import trimTypeName from "./trimTypeName";

class NodeLocatorElementBuilder {
  private _encoding : ExtraEncodingArgs;
  private _enableNodeExpander : boolean = true;
  private _env : ModalEnv;
  private _decorators : ((container: HTMLElement, isToplevel: boolean, matchResult: ReturnType<LineDecorator>) => void)[] = [];

  get encoding() : ExtraEncodingArgs {
    return this._encoding;
  }

  get env() : ModalEnv {
    return this._env;
  }

  get nodeExpanderEnabled() : boolean {
    return this._enableNodeExpander;
  }

  disableNodeExpander() {
    this._enableNodeExpander = false;
  }

  enableNodeExpander() {
    this._enableNodeExpander = true;
  }

  addDecorator(decorator: (container: HTMLElement, isToplevel: boolean, matchResult: ReturnType<LineDecorator>) => void) {
    this._decorators.push(decorator);
  }

  constructor(env : ModalEnv, encoding: ExtraEncodingArgs = {}) {
    this._env = env;
    this._encoding = encoding;
  }

  span(locator : NodeLocator): Span {
    const { start, end, type, label } = locator.result;
    return {
      lineStart: (start >>> 12), colStart: (start & 0xFFF),
      lineEnd: (end >>> 12), colEnd: (end & 0xFFF),
    };
  }

  buildElement(locator: NodeLocator, nestingLevel: number = 0, bodyPath: number[] = [], includePositionIndicator = true) : HTMLElement {
    const { start, end, type, label } = locator.result;

    const container = document.createElement('div');
    const env = this.env;
    const encoding = this.encoding;
    let result = container;

    container.classList.add('node-ref')
    if (this.encoding.decorator) {
      for (const decorator of this._decorators) {
	decorator(container, nestingLevel <= 1, this.encoding.decorator(bodyPath));
      }
    }
    // container.appendChild(document.createTextNode(`area:${JSON.stringify(bodyPath)}`));
    const span = this.span(locator);
    const typeNode = document.createElement('span');
    typeNode.classList.add('syntax-type');
    typeNode.innerText = label ?? trimTypeName(type);
    typeNode.style.margin = 'auto 0';
    container.appendChild(typeNode);
    if (includePositionIndicator) {
      container.appendChild(createTextSpanIndicator({
        span,
        marginLeft: true,
        autoVerticalMargin: true,
      }));
    }
    container.classList.add('clickHighlightOnHover');
    container.style.width = 'fit-content';
    container.style.display = 'inline';
    registerOnHover(container, on => {
      if (!on || (encoding.lateInteractivityEnabledChecker?.() ?? true)) {
        env.updateSpanHighlight(on ? span : null);
        container.style.cursor = 'default';
        container.classList.add('clickHighlightOnHover');
      } else {
        container.style.cursor = 'not-allowed';
        container.classList.remove('clickHighlightOnHover');
      }
    });
    container.onmousedown = (e) => {
      e.stopPropagation();
    };
    if (!encoding.disableNodeSelectors) {
      registerNodeSelector(container, () => locator);
    }
    container.addEventListener('click', (e) => {
      if (encoding.lateInteractivityEnabledChecker?.() ?? true) {
        e.preventDefault();
        displayAttributeModal(env.getGlobalModalEnv(), null, createMutableLocator(locator));
      }
    });
    if (this.nodeExpanderEnabled && encoding.nodeLocatorExpanderHandler) {
      // if (existing) {
      //   if (existing.parentElement) existing.parentElement.removeChild(existing);
      //   target.appendChild(existing);
      // } else {
      const middleContainer = document.createElement('div');
      middleContainer.style.display = 'flex';
      middleContainer.style.flexDirection = 'row';
      container.style.display = 'flex';
      middleContainer.appendChild(container);

      if (!encoding.disableInlineExpansionButton) {
        const expander = document.createElement('div');
        expander.innerText = `▼`;
        expander.style.marginLeft = '0.25rem';
        expander.classList.add('linkedProbeCreator');
        expander.onmouseover = e => e.stopPropagation();
        expander.onmousedown = (e) => {
          e.stopPropagation();
        };
        middleContainer.appendChild(expander);
        const clickHandler = encoding.nodeLocatorExpanderHandler.onClick;
        expander.onclick = () => clickHandler({
          locator,
          locatorRoot: outerContainer,
          expansionArea,
          path: bodyPath,
        });
      }


      const outerContainer = document.createElement('div');
      outerContainer.classList.add('inline-window-root');
      outerContainer.style.display = 'inline-flex';
      // outerContainer.style.marginBottom = '0.125rem';
      outerContainer.style.flexDirection = 'column';
      outerContainer.appendChild(middleContainer);

      const existingExpansionArea = encoding.nodeLocatorExpanderHandler.getReusableExpansionArea(bodyPath);
      if (existingExpansionArea) {
        if (existingExpansionArea.parentElement) {
          existingExpansionArea.parentElement.removeChild(existingExpansionArea);
        }
      }
      const expansionArea = existingExpansionArea ?? document.createElement('div');
      outerContainer.appendChild(expansionArea);

      encoding.nodeLocatorExpanderHandler.onCreate({
        locator,
        locatorRoot: outerContainer,
        expansionArea,
        path: bodyPath,
        isFresh: !existingExpansionArea,
      });

      result = outerContainer;

    } else {
      result = container;
    }
    return  result;
  }
}

export default NodeLocatorElementBuilder;
