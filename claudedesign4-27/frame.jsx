// SenkuFrame — minimal Android device chrome.
// Just status bar + gesture pill, no M3 toolbar (Senku has its own identity strip).
// Sizes: phone 400x880; phone-land 880x400; tablet 880x1240; tablet-land 1240x880.

const FRAME_SIZES = {
  'phone-portrait':   { w: 400,  h: 880,  label: 'Phone · portrait', land: false, tablet: false },
  'phone-landscape':  { w: 880,  h: 400,  label: 'Phone · landscape', land: true, tablet: false },
  'tablet-portrait':  { w: 880,  h: 1240, label: 'Tablet · portrait', land: false, tablet: true },
  'tablet-landscape': { w: 1240, h: 880,  label: 'Tablet · landscape', land: true, tablet: true },
};

function SenkuStatusBar({ width, dark = true, land = false }) {
  const fg = dark ? T.ink1 : T.paperInk;
  return (
    <div style={{
      height: 28,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: land ? '0 22px' : '0 18px',
      fontFamily: T.fUI,
      fontSize: 12,
      fontWeight: 500,
      color: fg,
      letterSpacing: '-0.01em',
      flex: '0 0 auto',
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <span>4:21</span>
        <span style={{ width: 4, height: 4, borderRadius: 4, background: T.danger, opacity: 0.85 }} />
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 7, fontFamily: T.fMono, fontSize: 10, letterSpacing: '0.05em', opacity: 0.85 }}>
        <span>OFFLINE</span>
        <span style={{ width: 14, height: 7, border: `1px solid ${fg}`, borderRadius: 2, position: 'relative', opacity: 0.7 }}>
          <span style={{ position: 'absolute', inset: 1, right: '20%', background: fg, borderRadius: 1 }} />
        </span>
      </div>
    </div>
  );
}

function SenkuGestureBar({ dark = true }) {
  const fg = dark ? T.ink1 : T.paperInk;
  return (
    <div style={{
      height: 22,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      flex: '0 0 auto',
    }}>
      <div style={{ width: 110, height: 4, borderRadius: 4, background: fg, opacity: 0.55 }} />
    </div>
  );
}

function SenkuFrame({ size = 'phone-portrait', children, surface = T.bg0, label, dark = true }) {
  const conf = FRAME_SIZES[size];
  return (
    <div style={{
      width: conf.w,
      height: conf.h,
      background: surface,
      borderRadius: conf.tablet ? 22 : 32,
      border: `1px solid ${T.bg3}`,
      boxShadow: '0 30px 80px -30px rgba(0,0,0,0.55), 0 1px 0 rgba(241,238,226,0.05) inset',
      overflow: 'hidden',
      display: 'flex',
      flexDirection: 'column',
      position: 'relative',
      fontFamily: T.fUI,
    }}>
      <SenkuStatusBar width={conf.w} dark={dark} land={conf.land} />
      <div style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
        {children}
      </div>
      <SenkuGestureBar dark={dark} />
    </div>
  );
}

window.SenkuFrame = SenkuFrame;
window.FRAME_SIZES = FRAME_SIZES;
