function toSpan({ start, end } : PackedLoc): Span {
  return {
    lineStart: (start >>> 12),
    colStart: start & 0xFFF,
    lineEnd: (end >>> 12),
    colEnd: end & 0xFFF,
  };
}

export default toSpan;
