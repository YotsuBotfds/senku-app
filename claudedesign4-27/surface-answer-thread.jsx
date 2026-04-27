// Answer detail surface — olive canvas, serif body, evidence rail.
// Distinct from Guide reader: no paper card, content lives directly on canvas.

function AnswerSurface({ form = 'phone-portrait' }) {
  const isTablet = form.includes('tablet');

  const QuestionBlock = () => (
    <div style={{ padding: isTablet ? '20px 28px 14px' : '14px 18px 10px' }}>
      <Row gap={8}>
        <Mono color={T.accent}>ANSWER</Mono>
        <Mono color={T.ink3}>· this device</Mono>
        <Mono color={T.ink3}>· 1 turn</Mono>
        <span style={{ flex: 1 }} />
        <Row gap={5}>
          <Dot color={T.warn} size={5} />
          <Mono color={T.warn}>UNSURE</Mono>
        </Row>
      </Row>
      <div style={{
        ...typeStyle(isTablet ? 'canvasTitle' : 'sectionTitle'),
        color: T.ink0,
        marginTop: 8,
        fontSize: isTablet ? 28 : 20,
        lineHeight: isTablet ? '34px' : '26px',
      }}>How do I build a simple rain shelter from tarp and cord?</div>
      <Row gap={6} style={{ marginTop: 8 }}>
        <Mono color={T.ink3}>GD-345</Mono>
        <Mono color={T.ink3}>·</Mono>
        <Mono color={T.ink3}>3 sources</Mono>
        <Mono color={T.ink3}>·</Mono>
        <Mono color={T.ink3}>rev 04-27 04:21</Mono>
      </Row>
    </div>
  );

  const AnswerBody = () => (
    <Stack gap={14} style={{ padding: isTablet ? '4px 28px 24px' : '6px 18px 24px' }}>
      <div style={{ ...typeStyle('answerBody'), color: T.ink0, fontSize: isTablet ? 17 : 16, lineHeight: isTablet ? '27px' : '25px' }}>
        Build a <em>ridgeline first</em>, then drape and tension the tarp around it. A ridgeline is a single taut cord run between two anchor points (trees, poles, or bombproof rocks) at roughly chest height; it carries the load while the tarp only sheds water.
      </div>
      <div style={{ ...typeStyle('answerBody'), color: T.ink0, fontSize: isTablet ? 17 : 16, lineHeight: isTablet ? '27px' : '25px' }}>
        Pitch the tarp ridge along the prevailing wind, with the low edge facing windward. Tension corners with prusik or taut-line hitches so the rig stays adjustable through the night.
      </div>
      <div style={{
        background: T.bg2,
        borderLeft: `2px solid ${T.warn}`,
        padding: '10px 14px',
      }}>
        <Mono color={T.warn}>UNSURE FIT</Mono>
        <div style={{ ...typeStyle('smallBody'), color: T.ink1, marginTop: 4 }}>
          Senku found 3 guides that may apply but no single guide is a confident anchor. Treat this as guidance, not procedure. See sources below.
        </div>
      </div>
    </Stack>
  );

  const ProvenancePanel = () => (
    <Stack gap={10} style={{ padding: isTablet ? '14px 28px 18px' : '14px 18px 18px' }}>
      <Row gap={10}>
        <SectionLabel>Sources · 3</SectionLabel>
        <span style={{ flex: 1 }} />
        <Mono color={T.ink3}>tap to expand</Mono>
      </Row>
      <EvidenceCard id="GD-220" title="Abrasives Manufacturing" role="anchor" match={0.74}
        quote="Every melt starts with a foundry safety check, not with metal charge…" />
      <EvidenceCard id="GD-132" title="Foundry & Metal Casting" role="related" match={0.68}
        quote="Pitch the ridgeline along prevailing wind. Tension corners with prusik or taut-line hitches." />
      <EvidenceCard id="GD-345" title="Tarp & Cord Shelters" role="topic" match={0.61}
        quote="A simple ridgeline shelter requires only tarp, cord, and two anchor points." />
    </Stack>
  );

  const RelatedPanel = () => (
    <Stack gap={6} style={{ padding: isTablet ? '0 28px 18px' : '0 18px 18px' }}>
      <SectionLabel>Related guides · 4</SectionLabel>
      <Stack gap={4} style={{ marginTop: 6 }}>
        {[
          ['GD-294', 'Cave Shelter Systems & Cold-Weather'],
          ['GD-695', 'Hurricane & Severe Storm Sheltering'],
          ['GD-484', 'Insulation Materials & Cold-Soak'],
          ['GD-027', 'Primitive Technology & Stone Age'],
        ].map(([id, t]) => (
          <Row key={id} gap={10} style={{
            padding: '8px 10px',
            borderTop: `1px solid ${T.hairline}`,
          }}>
            <Mono color={T.ink2}>{id}</Mono>
            <span style={{ ...typeStyle('uiBody'), color: T.ink0 }}>{t}</span>
            <span style={{ flex: 1 }} />
            <span style={{ color: T.ink3 }}>›</span>
          </Row>
        ))}
      </Stack>
    </Stack>
  );

  // Form-factor specific layouts
  if (form === 'phone-portrait') {
    return (
      <>
        <IdentityStrip context="ANSWER" primary="GD-345" secondary="Rain shelter" actions={['home', 'share']} />
        <div style={{ flex: 1, minHeight: 0, overflow: 'auto', background: T.bg0 }}>
          <QuestionBlock />
          <Hairline />
          <AnswerBody />
          <Hairline />
          <ProvenancePanel />
          <Hairline />
          <RelatedPanel />
        </div>
        <Composer placeholder="Ask a follow-up about this answer…" hint="GD-345 · this device · context kept" />
      </>
    );
  }
  if (form === 'phone-landscape') {
    return (
      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        <SideRail active="ask" />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <IdentityStrip context="ANSWER" primary="GD-345" secondary="Rain shelter" actions={['home', 'share']} />
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1.2fr 1fr', minHeight: 0 }}>
            <div style={{ flex: 1, minHeight: 0, overflow: 'auto', borderRight: `1px solid ${T.hairline}` }}>
              <QuestionBlock />
              <Hairline />
              <AnswerBody />
            </div>
            <div style={{ flex: 1, minHeight: 0, overflow: 'auto', background: T.bg1 }}>
              <ProvenancePanel />
              <Hairline />
              <RelatedPanel />
            </div>
          </div>
          <Composer placeholder="Ask a follow-up…" />
        </div>
      </div>
    );
  }
  if (form === 'tablet-portrait') {
    return (
      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        <SideRail active="ask" wide />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <IdentityStrip context="ANSWER" primary="GD-345" secondary="Rain shelter" actions={['home', 'share', 'more']} />
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1.5fr 1fr', minHeight: 0 }}>
            <div style={{ flex: 1, minHeight: 0, overflow: 'auto', borderRight: `1px solid ${T.hairline}` }}>
              <QuestionBlock />
              <Hairline />
              <AnswerBody />
              <Hairline />
              <RelatedPanel />
            </div>
            <div style={{ flex: 1, minHeight: 0, overflow: 'auto', background: T.bg1 }}>
              <ProvenancePanel />
            </div>
          </div>
          <Composer placeholder="Ask a follow-up about this answer…" hint="GD-345 · context kept" />
        </div>
      </div>
    );
  }
  // tablet-landscape: 3-pane = thread / answer / evidence
  return (
    <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
      <SideRail active="ask" wide />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <IdentityStrip context="ANSWER" primary="GD-345" secondary="Rain shelter" actions={['home', 'share', 'more']} />
        <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '220px 1fr 320px', minHeight: 0 }}>
          <div style={{ borderRight: `1px solid ${T.hairline}`, overflow: 'auto', background: T.bg0 }}>
            <Stack gap={2} style={{ padding: '14px 12px' }}>
              <SectionLabel>Thread · 1</SectionLabel>
              <Stack gap={0} style={{ marginTop: 8 }}>
                {[
                  ['Q1', 'How do I build a simple rain shelter…', true, T.warn],
                ].map(([n, t, on, c]) => (
                  <div key={n} style={{
                    padding: '9px 8px 9px 10px',
                    borderLeft: `2px solid ${on ? c : 'transparent'}`,
                    background: on ? T.bg2 : 'transparent',
                  }}>
                    <Mono color={T.ink3}>{n}</Mono>
                    <div style={{ ...typeStyle('smallBody'), color: on ? T.ink0 : T.ink2, fontWeight: on ? 600 : 400, marginTop: 3 }}>{t}</div>
                  </div>
                ))}
              </Stack>
              <div style={{ marginTop: 16 }}>
                <SectionLabel>Sources · 3</SectionLabel>
                <Stack gap={0} style={{ marginTop: 8 }}>
                  {[
                    ['GD-220', 'Abrasives Manufacturing', T.accent],
                    ['GD-132', 'Foundry & Metal Casting', T.olive60],
                    ['GD-345', 'Tarp & Cord Shelters', T.olive60],
                  ].map(([id, t, c]) => (
                    <div key={id} style={{ padding: '7px 8px 7px 10px', borderLeft: `2px solid ${c}` }}>
                      <Mono color={c}>{id}</Mono>
                      <div style={{ ...typeStyle('smallBody'), color: T.ink1, marginTop: 2 }}>{t}</div>
                    </div>
                  ))}
                </Stack>
              </div>
            </Stack>
          </div>
          <div style={{ overflow: 'auto', display: 'flex', flexDirection: 'column' }}>
            <QuestionBlock />
            <Hairline />
            <AnswerBody />
            <Hairline />
            <RelatedPanel />
          </div>
          <div style={{ borderLeft: `1px solid ${T.hairline}`, overflow: 'auto', background: T.bg1 }}>
            <ProvenancePanel />
          </div>
        </div>
        <Composer placeholder="Ask a follow-up about this answer…" hint="GD-345 · context kept · 3 sources visible" />
      </div>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// FOLLOWUP THREAD
// ──────────────────────────────────────────────────────────────
function ThreadSurface({ form = 'phone-portrait' }) {
  const isTablet = form.includes('tablet');

  const turns = [
    { kind: 'q', text: 'How do I build a simple rain shelter from tarp and cord?', meta: 'Q1 · 04:21' },
    { kind: 'a', text: 'Build a ridgeline first, then drape and tension the tarp around it. Pitch ridge along prevailing wind.', anchor: 'GD-220', sources: ['GD-220', 'GD-132'], conf: 'unsure', meta: 'A1 · 04:21' },
    { kind: 'q', text: 'What should I do next after the ridge line is up?', meta: 'Q2 · 04:23' },
    { kind: 'a', text: 'Drape the tarp evenly across the ridge with both edges hanging the same length. Tension the four corners with taut-line hitches; aim for the windward edge to sit closest to the ground.', anchor: 'GD-345', sources: ['GD-345'], conf: 'confident', meta: 'A2 · 04:23' },
  ];

  const Turn = ({ t }) => {
    if (t.kind === 'q') {
      return (
        <div style={{ padding: '12px 16px', borderTop: `1px solid ${T.hairline}` }}>
          <Row gap={8} style={{ marginBottom: 4 }}>
            <Mono color={T.ink3}>{t.meta}</Mono>
            <Mono color={T.ink3}>· field question</Mono>
          </Row>
          <div style={{ ...typeStyle('sectionTitle'), color: T.ink0, fontSize: 17, lineHeight: '23px' }}>{t.text}</div>
        </div>
      );
    }
    return (
      <div style={{ padding: '8px 16px 16px' }}>
        <Row gap={8} style={{ marginBottom: 6 }}>
          <Mono color={t.conf === 'unsure' ? T.warn : T.ok}>{t.meta}</Mono>
          <Mono color={T.ink3}>· anchor {t.anchor}</Mono>
          <span style={{ flex: 1 }} />
          <Row gap={5}>
            <Dot color={t.conf === 'unsure' ? T.warn : T.ok} size={5} />
            <Mono color={t.conf === 'unsure' ? T.warn : T.ok}>{t.conf.toUpperCase()}</Mono>
          </Row>
        </Row>
        <div style={{ ...typeStyle('answerBody'), color: T.ink0 }}>{t.text}</div>
        <Row gap={5} style={{ marginTop: 10, flexWrap: 'wrap' }}>
          {t.sources.map(s => <Chip key={s}><Mono color={T.olive60}>{s}</Mono></Chip>)}
        </Row>
      </div>
    );
  };

  if (form === 'phone-portrait') {
    return (
      <>
        <IdentityStrip context="THREAD" primary="GD-220" secondary="Rain shelter · 2 turns" actions={['home', 'share']} />
        <div style={{ flex: 1, minHeight: 0, overflow: 'auto', background: T.bg0 }}>
          {turns.map((t, i) => <Turn key={i} t={t} />)}
        </div>
        <Composer placeholder="Ask a follow-up…" hint="thread context · 2 turns · GD-220 anchor" />
      </>
    );
  }
  if (form === 'phone-landscape') {
    return (
      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        <SideRail active="ask" />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <IdentityStrip context="THREAD" primary="GD-220" secondary="Rain shelter · 2 turns" actions={['home', 'share']} />
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1.3fr 1fr', minHeight: 0 }}>
            <div style={{ overflow: 'auto', borderRight: `1px solid ${T.hairline}` }}>
              {turns.slice(-2).map((t, i) => <Turn key={i} t={t} />)}
            </div>
            <div style={{ overflow: 'auto', background: T.bg1 }}>
              <Stack gap={10} style={{ padding: '14px 16px' }}>
                <SectionLabel>Sources · 2</SectionLabel>
                <EvidenceCard id="GD-220" title="Abrasives Manufacturing" role="anchor" match={0.74} quote="Pitch ridgeline along prevailing wind…" />
                <EvidenceCard id="GD-345" title="Tarp & Cord Shelters" role="topic" match={0.68} />
              </Stack>
            </div>
          </div>
          <Composer placeholder="Ask a follow-up…" />
        </div>
      </div>
    );
  }
  if (form === 'tablet-portrait') {
    return (
      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        <SideRail active="ask" wide />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <IdentityStrip context="THREAD" primary="GD-220" secondary="Rain shelter · 2 turns" actions={['home', 'share', 'more']} />
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1.4fr 1fr', minHeight: 0 }}>
            <div style={{ overflow: 'auto', borderRight: `1px solid ${T.hairline}` }}>
              {turns.map((t, i) => <Turn key={i} t={t} />)}
            </div>
            <div style={{ overflow: 'auto', background: T.bg1 }}>
              <Stack gap={10} style={{ padding: '14px 18px' }}>
                <SectionLabel>Sources · 2</SectionLabel>
                <EvidenceCard id="GD-220" title="Abrasives Manufacturing" role="anchor" match={0.74} quote="Pitch ridgeline along prevailing wind…" />
                <EvidenceCard id="GD-345" title="Tarp & Cord Shelters" role="topic" match={0.68} />
              </Stack>
            </div>
          </div>
          <Composer placeholder="Ask a follow-up about this thread…" hint="thread context kept · 2 turns" />
        </div>
      </div>
    );
  }
  // tablet-landscape
  return (
    <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
      <SideRail active="ask" wide />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <IdentityStrip context="THREAD" primary="GD-220" secondary="Rain shelter · 2 turns" actions={['home', 'share', 'more']} />
        <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '220px 1fr 320px', minHeight: 0 }}>
          <div style={{ borderRight: `1px solid ${T.hairline}`, overflow: 'auto', background: T.bg0 }}>
            <Stack gap={2} style={{ padding: '14px 12px' }}>
              <SectionLabel>Turns · 2</SectionLabel>
              <Stack gap={0} style={{ marginTop: 8 }}>
                {[['Q1','Rain shelter from tarp and cord', false], ['Q2','What to do after the ridge line', true]].map(([n, t, on]) => (
                  <div key={n} style={{ padding: '9px 8px 9px 10px', borderLeft: `2px solid ${on ? T.accent : 'transparent'}`, background: on ? T.bg2 : 'transparent' }}>
                    <Mono color={T.ink3}>{n}</Mono>
                    <div style={{ ...typeStyle('smallBody'), color: on ? T.ink0 : T.ink2, fontWeight: on ? 600 : 400, marginTop: 3 }}>{t}</div>
                  </div>
                ))}
              </Stack>
            </Stack>
          </div>
          <div style={{ overflow: 'auto' }}>
            {turns.map((t, i) => <Turn key={i} t={t} />)}
          </div>
          <div style={{ borderLeft: `1px solid ${T.hairline}`, overflow: 'auto', background: T.bg1 }}>
            <Stack gap={10} style={{ padding: '14px 18px' }}>
              <SectionLabel>Sources in thread · 2</SectionLabel>
              <EvidenceCard id="GD-220" title="Abrasives Manufacturing" role="anchor" match={0.74} quote="Pitch ridgeline along prevailing wind…" />
              <EvidenceCard id="GD-345" title="Tarp & Cord Shelters" role="topic" match={0.68} />
            </Stack>
          </div>
        </div>
        <Composer placeholder="Ask a follow-up about this thread…" hint="thread context kept · 2 turns · 2 sources" />
      </div>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// EMERGENCY (calm, serious)
// ──────────────────────────────────────────────────────────────
function EmergencySurface({ form = 'phone-portrait' }) {
  const isTablet = form.includes('tablet');
  const Banner = () => (
    <div style={{
      borderTop: `2px solid ${T.danger}`,
      borderBottom: `1px solid ${T.hairlineStrong}`,
      background: 'rgba(196,112,75,0.08)',
      padding: isTablet ? '14px 24px' : '12px 16px',
    }}>
      <Row gap={10}>
        <Dot color={T.danger} size={7} />
        <Mono color={T.danger} size={11} ls="0.14em">DANGER · EXTREME BURN HAZARD</Mono>
      </Row>
      <div style={{ ...typeStyle('answerBody'), color: T.ink0, marginTop: 8, fontWeight: 500 }}>
        Stop work immediately. Move to {' '}
        <span style={{ color: T.danger, textDecoration: 'underline', textUnderlineOffset: 3 }}>
          minimum 5 m from active work zone
        </span>
        . Confirm two paths of egress.
      </div>
    </div>
  );

  const Steps = () => (
    <Stack gap={0} style={{ padding: isTablet ? '16px 24px' : '14px 16px' }}>
      <SectionLabel>Immediate actions · 4</SectionLabel>
      <Stack gap={0} style={{ marginTop: 10 }}>
        {[
          ['1', 'Stop all hot work', 'No new charges, no new pours.'],
          ['2', 'Clear the floor to 5 m radius', 'Move personnel upwind.'],
          ['3', 'Confirm two paths of egress', 'Door and roll-up open and unobstructed.'],
          ['4', 'Notify the area owner', 'GD-132 lists current owner.'],
        ].map(([n, t, sub]) => (
          <Row key={n} gap={14} style={{
            padding: '12px 0',
            borderBottom: `1px solid ${T.hairline}`,
            alignItems: 'flex-start',
          }}>
            <span style={{
              width: 22, height: 22, borderRadius: 22,
              border: `1px solid ${T.danger}`,
              color: T.danger,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontFamily: T.fMono, fontSize: 11, fontWeight: 600, flex: '0 0 auto',
            }}>{n}</span>
            <div style={{ flex: 1, paddingTop: 2 }}>
              <div style={{ ...typeStyle('uiBody'), color: T.ink0, fontWeight: 500 }}>{t}</div>
              <div style={{ ...typeStyle('smallBody'), color: T.ink2, marginTop: 2 }}>{sub}</div>
            </div>
          </Row>
        ))}
      </Stack>
    </Stack>
  );

  const Sources = () => (
    <Stack gap={10} style={{ padding: isTablet ? '0 24px 18px' : '0 16px 18px' }}>
      <SectionLabel>Why this answer</SectionLabel>
      <EvidenceCard id="GD-132" title="Foundry & Metal Casting · §1 Area readiness" role="anchor" match={0.93}
        quote="A single drop of water contacting molten metal causes a violent steam explosion…" />
    </Stack>
  );

  return (
    <>
      <IdentityStrip context="ANSWER" primary="GD-132" secondary="Burn hazard response" actions={['home', 'share']} badge={<Chip leading={<Dot color={T.danger} size={5} />}><Mono color={T.danger}>DANGER</Mono></Chip>} />
      <div style={{ flex: 1, minHeight: 0, overflow: 'auto', background: T.bg0 }}>
        <Banner />
        <Steps />
        <Hairline />
        <Sources />
      </div>
      <Composer placeholder="Ask about safe re-entry…" hint="emergency context · GD-132 anchor" />
    </>
  );
}

Object.assign(window, { AnswerSurface, ThreadSurface, EmergencySurface });
