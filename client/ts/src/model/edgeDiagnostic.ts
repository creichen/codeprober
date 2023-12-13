import { EdgeDiagnostic, Diagnostic } from '../protocol';
import { NodeLocator } from '../protocol';
import registerOnHover from "../ui/create/registerOnHover";
import NodeLocatorElementBuilder from '../ui/NodeLocatorElementBuilder';

interface EdgeTable {
  builder: NodeLocatorElementBuilder;
  head : HTMLElement;
  body : HTMLElement;
}

class Edge {
  private data: EdgeDiagnostic;

  constructor(ed: EdgeDiagnostic) {
    this.data = ed;
  }

  get startNode(): NodeLocator {
    return this.data.startNode;
  }

  get endNode(): NodeLocator {
    return this.data.endNode;
  }

  get start(): number {
    return this.startNode.result.start;
  }

  get end(): number {
    return this.endNode.result.end;
  }

  get style(): string {
    return this.data.style;
  }

  get styleNoAlpha(): string {
    const style = this.style;
    if (style && style.length > 1 && style[0] == '#') {
      if (style.length == 5) {
	// #RGBA:  trim last hex digit (alpha)
	return style.substring(0, 4);
      }
      if (style.length == 9) {
	// #RRGGBBAA:  trim last two hex digits (alpha)
	return style.substring(0, 7);
      }
    }
    return style;
  }

  get diagnostic(): Diagnostic {
    return { type: this.data.type,
	     start: this.start,
	     end: this.end,
	     msg: this.data.style };
  }

  /**
   * The table is not yet appended to anything.  You have to append table.head to make it visible.
   */
  appendToTable({ builder, head, body } : EdgeTable) {
    const row = document.createElement('TR');
    const l = document.createElement('TD');
    l.append(builder.buildElement(this.data.startNode));
    const m = document.createElement('TD');
    const arrow = document.createElement('SPAN');
    m.append(arrow);
    arrow.className = 'edge-diagnostic-arrow';
    arrow.style.color = this.styleNoAlpha;
    arrow.append(document.createTextNode(this.data.edgeInfo));
    const r = document.createElement('TD');
    r.append(builder.buildElement(this.data.endNode));
    row.append(l);
    row.append(m);
    row.append(r);
    body.append(row);

    registerOnHover(m, on => {
      if (!on || (builder.encoding.lateInteractivityEnabledChecker?.() ?? true)) {
	const span = builder.span(this.endNode);
        builder.env.updateSpanHighlight(on ? span : null);
      }

      // Highlight table elements
      for (const container of [l, m, r]) {
	if (!on || (builder.encoding.lateInteractivityEnabledChecker?.() ?? true)) {
          container.style.cursor = 'default';
          container.classList.add('clickHighlightOnHover');
	} else {
          container.style.cursor = 'not-allowed';
          container.classList.remove('clickHighlightOnHover');
	}
      }
    });
  }

  /**
   * The table is not yet appended to anything.  You have to append table.head to make it visible.
   */
  static createEdgeTable(builder: NodeLocatorElementBuilder) : EdgeTable {
    const head = document.createElement('TABLE');
    const body = document.createElement('TBODY');
    head.appendChild(body);
    return { builder : builder,
	     head: head,
	     body: body };
  }
}

export default Edge;
