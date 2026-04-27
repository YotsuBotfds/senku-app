// Senku UI primitives — shared across all surface mocks.
// Names mirror the Java/XML side where possible:
//   IdentityStrip  ↔ replacement for the toolbar pill cluster (DetailActivity top chrome)
//   PaperCard      ↔ guide reader page surface (parchment)
//   AnswerCanvas   ↔ answer body on dark canvas (rev03_bg_1/bg_2)
//   EvidenceRow    ↔ DetailSourcePresentationFormatter / bg_evidence_panel
//   MetaLine       ↔ DetailMetaPresentationFormatter — replaces 8-pill chip wall
//   FollowupBar    ↔ DetailFollowupActive
//   EmergencyBlock ↔ EmergencySurfacePolicy / bg_emergency_banner
//   GuideCrossRef  ↔ DetailRelatedGuidePresentationFormatter

// ─── Hairline rule ───────────────────────────────────────────
function Hairline({ strong = false, vertical = false, style = {} }) {
  const c = strong ? T.hairlineStrong : T.hairline;
  return (
    <div style={{
      ...(vertical
        ? { width: 1, alignSelf: 'stretch', background: c }
        : { height: 1, width: '100%', background: c }),
      ...style,
    }} />
  );
}

// ─── Mono caps inline label (the rev03 11sp JetBrains Mono caps) ─
function Mono({ children, color, size = 10, ls = '0.10em', style = {} }) {
  return (
    <span style={{
      fontFamily: T.fMono,
      fontSize: size,
      letterSpacing: ls,
      textTransform: 'uppercase',
      color: color || T.ink2,
      fontWeight: 500,
      ...style,
    }}>{children}</span>
  );
}

// ─── Status dot (live-readiness indicator) ──────────────────
function Dot({ color = T.ok, size = 6, style = {} }) {
  return <span style={{ display: 'inline-block', width: size, height: size, borderRadius: size, background: color, ...style }} />;
}

// ─── Identity strip (replaces toolbar pill cluster) ─────────
// Flat horizontal bar, 36dp tall. Status & breadcrumb left, actions right.
// No capsule — relies on hairline below.
function IdentityStrip({
  context = 'GUIDE',                // GUIDE | ANSWER | THREAD | HOME | SEARCH
  primary = 'GD-345',
  secondary = 'Rain shelter',
  badge,                            // optional small chip
  actions = ['back', 'home', 'pin'],
  compact = false,
}) {
  const isAnswer = context === 'ANSWER' || context === 'THREAD';
  const isPaper = context === 'GUIDE-PAPER';
  const ink = isPaper ? T.paperInk : T.ink0;
  const inkMuted = isPaper ? T.paperInkMuted : T.ink2;
  const hair = isPaper ? 'rgba(31,35,24,0.10)' : T.hairline;

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      height: 36,
      padding: compact ? '0 12px' : '0 16px',
      borderBottom: `1px solid ${hair}`,
      gap: 12,
      flex: '0 0 auto',
    }}>
      {/* Action: back chevron, no chip */}
      <button style={{
        all: 'unset', cursor: 'pointer',
        width: 22, height: 22, display: 'inline-flex',
        alignItems: 'center', justifyContent: 'center',
        color: inkMuted,
      }} aria-label="back">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M15 18l-6-6 6-6" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </button>
      <span style={{ width: 1, height: 14, background: hair }} />

      {/* Context label + breadcrumb */}
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 8, minWidth: 0, flex: 1 }}>
        <Mono color={isAnswer ? T.accent : (isPaper ? T.paperWarn : T.olive60)}>{context.replace('-PAPER', '')}</Mono>
        <span style={{
          fontFamily: T.fMono, fontSize: 11, color: inkMuted, letterSpacing: '0.04em',
        }}>{primary}</span>
        {secondary && (
          <span style={{
            fontFamily: T.fUI, fontSize: 13, color: ink, fontWeight: 500,
            overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', minWidth: 0,
          }}>· {secondary}</span>
        )}
      </div>

      {badge}

      {/* Right-side actions, icon-only, 22px */}
      <div style={{ display: 'flex', gap: 10, color: inkMuted }}>
        {actions.includes('home') && (
          <ActionIcon><path d="M3 11l9-8 9 8M5 9.5V20a1 1 0 001 1h4v-6h4v6h4a1 1 0 001-1V9.5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" fill="none"/></ActionIcon>
        )}
        {actions.includes('pin') && (
          <ActionIcon><path d="M12 2v6M12 8l4 4-4 8-4-8 4-4z" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" fill="none"/></ActionIcon>
        )}
        {actions.includes('share') && (
          <ActionIcon><path d="M18 8a3 3 0 100-6 3 3 0 000 6zM6 15a3 3 0 100-6 3 3 0 000 6zm12 7a3 3 0 100-6 3 3 0 000 6zM8.6 13.5l6.8 4M15.4 6.5l-6.8 4" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" fill="none"/></ActionIcon>
        )}
        {actions.includes('search') && (
          <ActionIcon><circle cx="11" cy="11" r="6" stroke="currentColor" strokeWidth="1.6" fill="none"/><path d="M20 20l-4.3-4.3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></ActionIcon>
        )}
        {actions.includes('more') && (
          <ActionIcon><circle cx="6" cy="12" r="1.4" fill="currentColor"/><circle cx="12" cy="12" r="1.4" fill="currentColor"/><circle cx="18" cy="12" r="1.4" fill="currentColor"/></ActionIcon>
        )}
      </div>
    </div>
  );
}

function ActionIcon({ children }) {
  return (
    <button style={{
      all: 'unset', cursor: 'pointer',
      width: 22, height: 22, display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
    }}>
      <svg width="18" height="18" viewBox="0 0 24 24">{children}</svg>
    </button>
  );
}

// ─── Compact meta line (replaces metadata pill wall) ────────
// Tab-stop separated, single row, expandable. Reads like log output.
function MetaLine({ items = [], onPaper = false, dim = false }) {
  const ink = onPaper ? T.paperInkMuted : (dim ? T.ink3 : T.ink2);
  return (
    <div style={{
      display: 'flex',
      flexWrap: 'wrap',
      gap: 0,
      fontFamily: T.fMono,
      fontSize: 10.5,
      letterSpacing: '0.05em',
      textTransform: 'uppercase',
      color: ink,
      lineHeight: '16px',
    }}>
      {items.map((it, i) => (
        <React.Fragment key={i}>
          {i > 0 && <span style={{ padding: '0 8px', opacity: 0.5 }}>·</span>}
          {typeof it === 'string'
            ? <span>{it}</span>
            : <span style={{ color: it.color || ink }}>{it.dot && <Dot color={it.color || T.ok} style={{ marginRight: 5, transform: 'translateY(-1px)' }} />}{it.text}</span>}
        </React.Fragment>
      ))}
    </div>
  );
}

// ─── Chip (tag pill) — quieter than current ─────────────────
function Chip({ children, color, onPaper = false, solid = false, leading }) {
  if (solid) {
    return (
      <span style={{
        display: 'inline-flex', alignItems: 'center', gap: 5,
        padding: '3px 9px',
        background: color || T.olive10,
        color: T.ink0,
        borderRadius: 999,
        fontFamily: T.fUI, fontSize: 11.5, fontWeight: 500, letterSpacing: '-0.005em',
      }}>{leading}{children}</span>
    );
  }
  const ink = onPaper ? T.paperInk : T.ink1;
  const stroke = onPaper ? 'rgba(31,35,24,0.18)' : T.hairlineStrong;
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 5,
      padding: '2px 9px',
      border: `1px solid ${stroke}`,
      color: color || ink,
      borderRadius: 999,
      fontFamily: T.fUI, fontSize: 11.5, fontWeight: 500, letterSpacing: '-0.005em',
      background: 'transparent',
    }}>{leading}{children}</span>
  );
}

// ─── Section label (mono caps with tick mark) ───────────────
function SectionLabel({ children, onPaper = false, color, style = {} }) {
  const c = color || (onPaper ? T.paperInkMuted : T.ink2);
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 8,
      ...style,
    }}>
      <span style={{ width: 16, height: 1, background: c, opacity: 0.5 }} />
      <Mono color={c} ls="0.12em">{children}</Mono>
    </div>
  );
}

// ─── Tick — small leading line for sub-content ──────────────
function Tick({ color = T.olive40 }) {
  return <span style={{ display: 'inline-block', width: 10, height: 1, background: color, marginRight: 8, transform: 'translateY(-3px)' }} />;
}

// ─── Source / evidence card ─────────────────────────────────
function EvidenceCard({ id = 'GD-220', title = 'Abrasives Manufacturing', quote, anchor, role = 'anchor', match = 0.87, onPaper = false }) {
  const bg = onPaper ? 'rgba(31,35,24,0.04)' : T.bg2;
  const ink = onPaper ? T.paperInk : T.ink0;
  const inkMuted = onPaper ? T.paperInkMuted : T.ink2;
  const stroke = onPaper ? 'rgba(31,35,24,0.10)' : T.hairlineStrong;

  return (
    <div style={{
      borderLeft: `2px solid ${T.accent}`,
      paddingLeft: 12,
      paddingTop: 10, paddingBottom: 10, paddingRight: 12,
      background: bg,
      borderRadius: 0,
      borderTop: `1px solid ${stroke}`,
      borderRight: `1px solid ${stroke}`,
      borderBottom: `1px solid ${stroke}`,
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
        <Mono color={T.accent}>{id}</Mono>
        <Mono color={inkMuted}>· {role.toUpperCase()}</Mono>
        <span style={{ flex: 1 }} />
        <Mono color={inkMuted}>{Math.round(match * 100)}%</Mono>
      </div>
      <div style={{ ...typeStyle('uiBody'), color: ink, fontWeight: 600 }}>{title}</div>
      {quote && (
        <div style={{ ...typeStyle('smallBody'), color: inkMuted, marginTop: 4, fontStyle: 'italic' }}>
          “{quote}”
        </div>
      )}
      {anchor && (
        <div style={{ ...typeStyle('smallBody'), color: inkMuted, marginTop: 4 }}>
          ↳ {anchor}
        </div>
      )}
    </div>
  );
}

// ─── Composer (followup) ────────────────────────────────────
function Composer({ value = '', placeholder = 'Ask a follow-up…', dark = true, sticky = true, hint }) {
  return (
    <div style={{
      borderTop: `1px solid ${T.hairlineStrong}`,
      background: dark ? T.bg0 : T.paper,
      flex: '0 0 auto',
    }}>
      {hint && (
        <div style={{ padding: '6px 16px 0', display: 'flex', alignItems: 'center', gap: 8 }}>
          <Mono color={T.ink3}>{hint}</Mono>
        </div>
      )}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        padding: '10px 14px',
        gap: 10,
      }}>
        <span style={{
          width: 22, height: 22, borderRadius: 22,
          border: `1px solid ${T.hairlineStrong}`,
          display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
          color: T.ink2,
        }}>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/>
          </svg>
        </span>
        <div style={{
          flex: 1,
          ...typeStyle('uiBody'),
          color: value ? T.ink0 : T.ink3,
        }}>
          {value || placeholder}
        </div>
        <button style={{
          all: 'unset', cursor: 'pointer',
          padding: '6px 12px',
          borderRadius: 8,
          background: value ? T.accent : 'transparent',
          color: value ? T.bg0 : T.ink3,
          fontFamily: T.fUI, fontSize: 13, fontWeight: 600,
          letterSpacing: '-0.005em',
          border: value ? 'none' : `1px solid ${T.hairlineStrong}`,
        }}>Send</button>
      </div>
    </div>
  );
}

// ─── Bottom nav (phone) ─────────────────────────────────────
function BottomNav({ active = 'library' }) {
  const items = [
    { id: 'library', label: 'Library', icon: <path d="M4 5h16M4 12h16M4 19h10" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/> },
    { id: 'ask', label: 'Ask', icon: <path d="M5 6h14a2 2 0 012 2v8a2 2 0 01-2 2H9l-4 3v-3a2 2 0 01-2-2V8a2 2 0 012-2z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" fill="none"/> },
    { id: 'saved', label: 'Saved', icon: <path d="M6 4h12v16l-6-4-6 4V4z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" fill="none"/> },
  ];
  return (
    <div style={{
      display: 'flex',
      justifyContent: 'space-around',
      borderTop: `1px solid ${T.hairlineStrong}`,
      padding: '6px 0 0',
      flex: '0 0 auto',
    }}>
      {items.map(it => {
        const on = it.id === active;
        const c = on ? T.accent : T.ink3;
        return (
          <div key={it.id} style={{
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2,
            padding: '4px 14px',
            color: c,
          }}>
            <svg width="19" height="19" viewBox="0 0 24 24">{it.icon}</svg>
            <span style={{ fontFamily: T.fUI, fontSize: 10.5, fontWeight: 500, letterSpacing: '0.02em' }}>{it.label}</span>
            {on && <span style={{ width: 14, height: 2, background: T.accent, borderRadius: 2, marginTop: 1 }} />}
            {!on && <span style={{ width: 14, height: 2, marginTop: 1 }} />}
          </div>
        );
      })}
    </div>
  );
}

// ─── Side rail (phone landscape / tablet) ───────────────────
function SideRail({ active = 'library', wide = false }) {
  const items = [
    { id: 'library', label: 'Library', icon: <path d="M4 5h16M4 12h16M4 19h10" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/> },
    { id: 'ask', label: 'Ask', icon: <path d="M5 6h14a2 2 0 012 2v8a2 2 0 01-2 2H9l-4 3v-3a2 2 0 01-2-2V8a2 2 0 012-2z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" fill="none"/> },
    { id: 'saved', label: 'Saved', icon: <path d="M6 4h12v16l-6-4-6 4V4z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" fill="none"/> },
  ];
  return (
    <div style={{
      width: wide ? 80 : 56,
      borderRight: `1px solid ${T.hairlineStrong}`,
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      padding: '14px 0',
      gap: 4,
      flex: '0 0 auto',
    }}>
      {/* Brand mark */}
      <div style={{
        width: 30, height: 30, borderRadius: 6,
        border: `1px solid ${T.olive40}`,
        background: T.bg2,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        marginBottom: 14,
      }}>
        <span style={{ fontFamily: T.fMono, fontSize: 11, color: T.accent, fontWeight: 600 }}>S</span>
      </div>
      {items.map(it => {
        const on = it.id === active;
        return (
          <div key={it.id} style={{
            width: '100%',
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3,
            padding: '8px 0',
            color: on ? T.accent : T.ink3,
            position: 'relative',
          }}>
            {on && <span style={{ position: 'absolute', left: 0, top: 8, bottom: 8, width: 2, background: T.accent }} />}
            <svg width="19" height="19" viewBox="0 0 24 24">{it.icon}</svg>
            {wide && <span style={{ fontFamily: T.fUI, fontSize: 10.5, fontWeight: 500 }}>{it.label}</span>}
          </div>
        );
      })}
    </div>
  );
}

Object.assign(window, {
  Hairline, Mono, Dot, IdentityStrip, MetaLine, Chip,
  SectionLabel, Tick, EvidenceCard, Composer, BottomNav, SideRail,
});
