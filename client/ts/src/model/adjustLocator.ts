import { PropertyArg, NodeLocator, NodeLocatorStep, Property } from '../protocol';
import adjustTypeAtLoc from "./adjustTypeAtLoc";

const adjustValue = (adj: LocationAdjuster, arg: PropertyArg) => {
  switch (arg.type) {
    case 'nodeLocator': {
      if (arg.value.value) {
        adjustLocator(adj, arg.value.value);
      }
      break;
    }
    case 'collection': {
      arg.value.entries.forEach(v => adjustValue(adj, v));
    }
  }
}

const adjustLocator = (adj: LocationAdjuster, loc: NodeLocator) => {
  adjustTypeAtLoc(adj, loc.result);

  const adjustStep = (step: NodeLocatorStep) => {
    switch (step.type) {
      case 'tal': {
        adjustTypeAtLoc(adj, step.value);
        break;
      }
      case 'nta': {
        step.value.property.args?.forEach(arg => adjustValue(adj, arg));
        break;
      }
    }
  };
  loc.steps.forEach(adjustStep);
};

const adjustLocatorAndProperty = (adj: LocationAdjuster, loc: NodeLocator, prop: Property) => {
  adjustLocator(adj, loc);
  prop.args?.forEach(arg => adjustValue(adj, arg));
}

export { adjustValue, adjustLocatorAndProperty };
export default adjustLocator;
