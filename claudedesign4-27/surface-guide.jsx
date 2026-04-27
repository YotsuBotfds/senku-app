// Guide reader surface — paper card on canvas, serif body.
// Distinct from Answer detail by surface treatment (parchment vs olive canvas).

function GuideSurface({ form = 'phone-portrait' }) {
  const PaperHeader = ({ tablet = false }) => (
    <div style={{
      padding: tablet ? '20px 28px 14px' : '14px 18px 10px',
      borderBottom: '1px solid rgba(31,35,24,0.12)',
    }}>
      <Row gap={8}>
        <Mono color={T.paperWarn}>FIELD MANUAL</Mono>
        <Mono color={T.paperInkMuted}>· REV 04-27</Mono>
        <Mono color={T.paperInkMuted}>· PK 2</Mono>
      </Row>
      <div style={{
        ...typeStyle(tablet ? 'canvasTitle' : 'sectionTitle'),
        color: T.paperInk,
        marginTop: 6,
        fontSize: tablet ? 32 : 22,
        lineHeight: tablet ? '36px' : '27px',
      }}>Foundry &amp; Metal Casting</div>
      <Row gap={6} style={{ marginTop: 6 }}>
        <Mono color={T.paperInkMuted}>GD-132</Mono>
        <Mono color={T.paperInkMuted}>·</Mono>
        <Mono color={T.paperInkMuted}>17 sections</Mono>
        <Mono color={T.paperInkMuted}>·</Mono>
        <Mono color={T.paperInkMuted}>opened from GD-220</Mono>
      </Row>
    </div>
  );

  const PaperBody = ({ withDanger = false }) => (
    <Stack gap={18} style={{ padding: form.includes('tablet') ? '20px 28px 24px' : '16px 18px 24px' }}>
      {withDanger && (
        <div style={{
          borderTop: `2px solid ${T.paperDanger}`,
          background: 'rgba(147,84,56,0.08)',
          padding: '12px 14px',
        }}>
          <Row gap={8}>
            <Mono color={T.paperDanger}>DANGER</Mono>
            <Mono color={T.paperInkMuted}>· extreme burn hazard</Mono>
          </Row>
          <div style={{ ...typeStyle('answerBody'), color: T.paperInk, marginTop: 6 }}>
            A single drop of water contacting molten metal causes a violent steam explosion, spraying molten metal 3+ meters in all directions. <strong>Every</strong> tool, mold, crucible, and surface that contacts molten metal must be completely dry.
          </div>
        </div>
      )}

      <div>
        <SectionLabel onPaper>§ 1 · Area readiness</SectionLabel>
        <div style={{ ...typeStyle('sectionTitle'), color: T.paperInk, marginTop: 8 }}>Reviewed Answer-Card Boundary</div>
        <div style={{ ...typeStyle('answerBody'), color: T.paperInk, marginTop: 10 }}>
          Use this section only for foundry-area readiness, visible hazard screening, material and source labeling, no-go triggers, access control, and expert or owner handoff.
        </div>
        <div style={{ ...typeStyle('answerBody'), color: T.paperInk, marginTop: 10 }}>
          Start with the current activity status, who owns the foundry area, whether molten metal work is planned or active, what can be observed without touching hot equipment, and whether any no-go trigger is already visible.
        </div>
      </div>

      <Hairline style={{ background: 'rgba(31,35,24,0.10)' }} />

      <div>
        <SectionLabel onPaper>§ 2 · Required reading</SectionLabel>
        <Stack gap={6} style={{ marginTop: 10 }}>
          {[
            ['GD-220', 'Abrasives Manufacturing'],
            ['GD-499', 'Bellows & Forge Blower Construction'],
            ['GD-225', 'Bloomery Furnace Construction'],
          ].map(([id, t]) => (
            <Row key={id} gap={10} style={{
              padding: '8px 10px',
              border: '1px solid rgba(31,35,24,0.10)',
              borderLeft: `2px solid ${T.paperWarn}`,
            }}>
              <Mono color={T.paperWarn}>{id}</Mono>
              <span style={{ ...typeStyle('uiBody'), color: T.paperInk }}>{t}</span>
              <span style={{ flex: 1 }} />
              <span style={{ color: T.paperInkMuted }}>›</span>
            </Row>
          ))}
        </Stack>
      </div>
    </Stack>
  );

  const Toc = () => (
    <Stack gap={2} style={{ padding: '14px 14px' }}>
      <SectionLabel>Sections · 17</SectionLabel>
      <Stack gap={0} style={{ marginTop: 8 }}>
        {[
          ['1', 'Area readiness', true],
          ['2', 'Required reading', false],
          ['3', 'Hazard screen', false],
          ['4', 'Material labeling', false],
          ['5', 'No-go triggers', false],
          ['6', 'Access control', false],
          ['7', 'Owner handoff', false],
        ].map(([n, t, on]) => (
          <Row key={n} gap={10} style={{
            padding: '7px 6px',
            borderLeft: `2px solid ${on ? T.accent : 'transparent'}`,
            paddingLeft: 10,
          }}>
            <Mono color={T.ink3}>§{n}</Mono>
            <span style={{ ...typeStyle('uiBody'), color: on ? T.ink0 : T.ink2, fontWeight: on ? 600 : 400 }}>{t}</span>
          </Row>
        ))}
      </Stack>
    </Stack>
  );

  const PaperPanel = ({ children, dropShadow = true, tablet = false }) => (
    <div style={{
      background: T.paper,
      color: T.paperInk,
      borderRadius: tablet ? 4 : 0,
      boxShadow: dropShadow ? '0 18px 32px -16px rgba(0,0,0,0.55)' : 'none',
      flex: 1, minHeight: 0,
      display: 'flex', flexDirection: 'column',
    }}>
      <div style={{ flex: 1, minHeight: 0, overflow: 'auto' }}>
        {children}
      </div>
    </div>
  );

  if (form === 'phone-portrait') {
    return (
      <>
        <IdentityStrip context="GUIDE" primary="GD-132" secondary="Foundry & Metal Casting" actions={['home', 'pin', 'more']} />
        <div style={{ flex: 1, minHeight: 0, background: T.bg0, padding: '8px 8px 0', display: 'flex' }}>
          <PaperPanel>
            <PaperHeader />
            <PaperBody withDanger />
          </PaperPanel>
        </div>
        <BottomNav active="library" />
      </>
    );
  }
  if (form === 'phone-landscape') {
    return (
      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        <SideRail active="library" />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <IdentityStrip context="GUIDE" primary="GD-132" secondary="Foundry & Metal Casting" actions={['home', 'pin']} />
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '170px 1fr', minHeight: 0, background: T.bg0 }}>
            <div style={{ borderRight: `1px solid ${T.hairline}`, overflow: 'auto' }}>
              <Toc />
            </div>
            <div style={{ padding: '6px 6px 0', display: 'flex' }}>
              <PaperPanel>
                <PaperHeader />
                <PaperBody />
              </PaperPanel>
            </div>
          </div>
        </div>
      </div>
    );
  }
  if (form === 'tablet-portrait') {
    return (
      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        <SideRail active="library" wide />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <IdentityStrip context="GUIDE" primary="GD-132" secondary="Foundry & Metal Casting" actions={['home', 'pin', 'share']} />
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '220px 1fr', minHeight: 0, background: T.bg0 }}>
            <div style={{ borderRight: `1px solid ${T.hairline}`, overflow: 'auto' }}>
              <Toc />
            </div>
            <div style={{ padding: '14px 16px 0', display: 'flex' }}>
              <PaperPanel tablet>
                <PaperHeader tablet />
                <PaperBody withDanger />
              </PaperPanel>
            </div>
          </div>
        </div>
      </div>
    );
  }
  // tablet-landscape — three pane: nav rail / TOC / paper / cross-ref
  return (
    <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
      <SideRail active="library" wide />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <IdentityStrip context="GUIDE" primary="GD-132" secondary="Foundry & Metal Casting" actions={['home', 'pin', 'share']} />
        <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '210px 1fr 280px', minHeight: 0, background: T.bg0 }}>
          <div style={{ borderRight: `1px solid ${T.hairline}`, overflow: 'auto' }}>
            <Toc />
          </div>
          <div style={{ padding: '16px 16px 0', display: 'flex' }}>
            <PaperPanel tablet>
              <PaperHeader tablet />
              <PaperBody withDanger />
            </PaperPanel>
          </div>
          <div style={{ borderLeft: `1px solid ${T.hairline}`, background: T.bg1, overflow: 'auto' }}>
            <Stack gap={14} style={{ padding: 18 }}>
              <SectionLabel>Cross-reference · 6</SectionLabel>
              {[
                ['GD-220', 'Abrasives Manufacturing', 'anchor', T.accent],
                ['GD-172', 'Bearing Manufacturing', 'related', T.olive60],
                ['GD-499', 'Bellows & Forge Blower', 'required', T.warn],
                ['GD-224', 'Blacksmithing', 'related', T.olive60],
                ['GD-225', 'Bloomery Furnace', 'required', T.warn],
                ['GD-110', 'Bridges, Dams & Infra.', 'related', T.olive60],
              ].map(([id, t, role, c]) => (
                <div key={id} style={{
                  padding: '10px 12px',
                  borderLeft: `2px solid ${c}`,
                  background: T.bg2,
                  border: `1px solid ${T.hairline}`,
                }}>
                  <Row gap={6}>
                    <Mono color={c}>{id}</Mono>
                    <Mono color={T.ink3}>· {role}</Mono>
                  </Row>
                  <div style={{ ...typeStyle('uiBody'), color: T.ink0, marginTop: 4, fontWeight: 500 }}>{t}</div>
                </div>
              ))}
            </Stack>
          </div>
        </div>
      </div>
    </div>
  );
}

window.GuideSurface = GuideSurface;
