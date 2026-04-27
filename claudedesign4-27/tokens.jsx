// Senku rev03 design tokens — pulled from android-app/app/src/main/res/values/colors.xml
// + dimens.xml + styles.xml. Lightly extended for web rendering (rgba helpers).
// Anchor source: YotsuBotfds/senku-app@master

const T = {
  // Canvas (dark olive ladder) — ascending lightness
  bg0: '#1A1D16',
  bg1: '#22271D',
  bg2: '#2C3224',
  bg3: '#3A4130',
  bg4: '#454D38',

  // Olive accents
  olive10: '#4A5139',
  olive20: '#5A6147',
  olive40: '#7A8263',
  olive60: '#9AA084',

  // Ink (on-canvas)
  ink0: '#F1EEE2',
  ink1: '#D7D3C2',
  ink2: '#A09C8A',
  ink3: '#7A7868',

  // Paper (parchment surface for guides)
  paper: '#E9E1CF',
  paperHi: '#F1E9D6',
  paperLo: '#D9CCB6',
  paperInk: '#1F2318',
  paperInkMuted: '#646456',
  paperRule: '#B9AA90',

  // Signals
  accent: '#C9B682',     // sand / wayfinding
  copper: '#C48A5A',
  moss: '#9BAB6A',
  danger: '#C4704B',
  warn: '#C49A4B',
  ok: '#7A9A5A',
  paperDanger: '#935438',
  paperWarn: '#7A5F2E',
  paperOk: '#546A3E',

  // Hairlines (always over canvas)
  hairline: 'rgba(241,238,226,0.08)',
  hairlineStrong: 'rgba(241,238,226,0.14)',
  hairlineEmphatic: 'rgba(241,238,226,0.22)',

  // Type
  fUI: '"Inter Tight", "Inter", system-ui, sans-serif',
  fSerif: '"Source Serif 4", "Source Serif Pro", Georgia, serif',
  fMono: '"JetBrains Mono", "IBM Plex Mono", ui-monospace, monospace',

  // Spacing scale
  s3: 3, s4: 4, s6: 6, s8: 8, s10: 10, s12: 12, s14: 14, s16: 16,
  s20: 20, s24: 24, s32: 32,

  // Radii (intentionally just two)
  rSm: 10,
  rMd: 14,
};

// Type scale (rev03)
const TT = {
  canvasTitle: { font: T.fUI, size: 28, lh: 32, ls: '-0.02em', weight: 600 },
  sectionTitle: { font: T.fUI, size: 19, lh: 24, ls: '-0.01em', weight: 600 },
  uiBody: { font: T.fUI, size: 14, lh: 21, ls: '-0.005em', weight: 400 },
  smallBody: { font: T.fUI, size: 13, lh: 19, ls: '-0.005em', weight: 400 },
  answerBody: { font: T.fSerif, size: 16, lh: 25, ls: '-0.005em', weight: 400 },
  monoCaps: { font: T.fMono, size: 10.5, lh: 14, ls: '0.10em', weight: 500, upper: true },
  tag: { font: T.fUI, size: 11, lh: 14, weight: 500 },
};

const typeStyle = (k) => {
  const v = TT[k]; if (!v) return {};
  return {
    fontFamily: v.font,
    fontSize: v.size,
    lineHeight: v.lh + 'px',
    letterSpacing: v.ls,
    fontWeight: v.weight,
    ...(v.upper ? { textTransform: 'uppercase' } : null),
  };
};

window.T = T;
window.TT = TT;
window.typeStyle = typeStyle;
