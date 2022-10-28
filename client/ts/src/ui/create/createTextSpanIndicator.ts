import settings from "../../settings";
import registerOnHover from "./registerOnHover";

type TextSpanStyle = 'full' | 'full-compact' | 'lines' | 'lines-compact' | 'start' | 'start-line';

interface TextSpanIndicatorArgs {
  span: Span;
  marginLeft?: boolean;
  onHover?: (isHovered: boolean) => void;
  onClick?: () => void;
  styleOverride?: TextSpanStyle;
}
const createTextSpanIndicator = (args: TextSpanIndicatorArgs) => {
  const { span, marginLeft, onHover, onClick } = args;
  const indicator = document.createElement('span');
  indicator.style.fontSize = '0.75rem';
  indicator.style.color = 'gray';
  if (marginLeft) {
    indicator.style.marginLeft = '0.25rem';
  }
  indicator.style.marginRight = '0.25rem';

  const warn = span.lineStart === 0 && span.colStart === 0 && span.lineEnd === 0 && span.colEnd === 0 ? '⚠️' : '';
  switch (args.styleOverride ?? settings.getLocationStyle()) {
    case 'full-compact':
      if (span.lineStart === span.lineEnd) {
        indicator.innerText = `[${span.lineStart}:${span.colStart}-${span.colEnd}]${warn}`;
        break;
      }
      // Else, fall through
    case 'full':
      indicator.innerText = `[${span.lineStart}:${span.colStart}→${span.lineEnd}:${span.colEnd}]${warn}`;
      break;
    case 'lines-compact':
      if (span.lineStart === span.lineEnd) {
        indicator.innerText = `[${span.lineStart}]${warn}`;
        break;
      }
      // Else, fall through
    case 'lines':
      indicator.innerText = `[${span.lineStart}→${span.lineEnd}]${warn}`;
      break;
    case 'start':
      indicator.innerText = `[${span.lineStart}:${span.colStart}]${warn}`;
      break;
    case 'start-line':
      indicator.innerText = `[${span.lineStart}]${warn}`;
      break;
  }

  if (onHover) {
    indicator.classList.add('highlightOnHover');
    registerOnHover(indicator, onHover);
  }
  if (onClick) {
    indicator.onclick = () => onClick();
  }
  return indicator;
}

export { TextSpanStyle }
export default createTextSpanIndicator;
