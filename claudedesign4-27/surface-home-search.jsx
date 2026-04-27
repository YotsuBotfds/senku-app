// Senku surface mocks — Home, Search, Guide reader, Answer detail, Followup thread, Emergency.
// Each surface is rendered into the SenkuFrame at any of the 4 form factors.
// Components do NOT clip themselves to a frame; they fill flex:1 and the frame clips.

// ─── Helpers ────────────────────────────────────────────────
function ScrollPane({ children, padX = 16, padY = 14, dark = true, ...rest }) {
  return (
    <div style={{
      flex: 1, minHeight: 0, overflow: 'auto',
      padding: `${padY}px ${padX}px`,
      background: dark ? T.bg0 : T.paper,
      ...rest.style,
    }}>{children}</div>
  );
}

function Stack({ gap = 12, children, style = {} }) {
  return <div style={{ display: 'flex', flexDirection: 'column', gap, ...style }}>{children}</div>;
}

function Row({ gap = 8, align = 'center', children, style = {} }) {
  return <div style={{ display: 'flex', alignItems: align, gap, ...style }}>{children}</div>;
}

// ──────────────────────────────────────────────────────────────
// HOME
// ──────────────────────────────────────────────────────────────
function HomeSurface({ form = 'phone-portrait' }) {
  const isLand = form.includes('landscape');
  const isTablet = form.includes('tablet');

  const PrimaryAction = () => (
    <div style={{
      borderRadius: T.rMd,
      background: T.bg2,
      border: `1px solid ${T.hairlineStrong}`,
      padding: 14,
    }}>
      <Row gap={10} align="center">
        <div style={{ width: 32, height: 32, borderRadius: 6, background: T.bg3, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><circle cx="11" cy="11" r="6" stroke={T.accent} strokeWidth="1.6"/><path d="M20 20l-4.3-4.3" stroke={T.accent} strokeWidth="1.6" strokeLinecap="round"/></svg>
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ ...typeStyle('uiBody'), color: T.ink2 }}>Search guide titles, IDs, field notes…</div>
        </div>
      </Row>
    </div>
  );

  const Categories = () => (
    <Stack gap={8}>
      <SectionLabel>Categories · 12</SectionLabel>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 6 }}>
        {[
          ['Shelter', 84, T.olive40],
          ['Water', 67, '#6B88A6'],
          ['Fire', 52, T.copper],
          ['Food', 91, T.moss],
          ['Medicine', 73, '#8E5A5F'],
          ['Tools', 119, T.accent],
        ].map(([name, n, c]) => (
          <div key={name} style={{
            padding: '10px 11px',
            border: `1px solid ${T.hairline}`,
            borderRadius: T.rSm,
            background: T.bg1,
          }}>
            <div style={{ width: 14, height: 2, background: c, marginBottom: 8 }} />
            <div style={{ ...typeStyle('uiBody'), color: T.ink0, fontWeight: 500 }}>{name}</div>
            <div style={{ ...typeStyle('smallBody'), color: T.ink3, marginTop: 2 }}>{n} guides</div>
          </div>
        ))}
      </div>
    </Stack>
  );

  const RecentThreads = () => (
    <Stack gap={8}>
      <SectionLabel>Recent threads</SectionLabel>
      {[
        ['How do I build a simple rain shelter…', 'GD-345', '04:21', 'unsure'],
        ['Best tinder when materials are wet', 'GD-027', '04:08', 'confident'],
        ['Boil water without a fire-safe pot', 'GD-094', 'yesterday', 'confident'],
      ].map(([q, id, t, conf], i) => (
        <div key={i} style={{
          padding: '10px 12px 11px',
          borderLeft: `2px solid ${conf === 'unsure' ? T.warn : T.olive40}`,
          background: T.bg1,
          borderTop: `1px solid ${T.hairline}`,
          borderRight: `1px solid ${T.hairline}`,
          borderBottom: `1px solid ${T.hairline}`,
        }}>
          <div style={{ ...typeStyle('uiBody'), color: T.ink0, marginBottom: 4, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{q}</div>
          <Row gap={0}>
            <Mono color={T.ink3}>{id}</Mono>
            <span style={{ padding: '0 8px', color: T.ink3, opacity: 0.5 }}><Mono>·</Mono></span>
            <Mono color={T.ink3}>{t}</Mono>
            <span style={{ padding: '0 8px', color: T.ink3, opacity: 0.5 }}><Mono>·</Mono></span>
            <Mono color={conf === 'unsure' ? T.warn : T.olive60}>{conf}</Mono>
          </Row>
        </div>
      ))}
    </Stack>
  );

  const PackStatus = () => (
    <div style={{
      padding: '10px 14px',
      background: T.bg1,
      border: `1px solid ${T.hairline}`,
      borderRadius: T.rSm,
    }}>
      <Row gap={8}>
        <Dot color={T.ok} />
        <Mono color={T.olive60}>PACK READY</Mono>
        <span style={{ flex: 1 }} />
        <Mono color={T.ink3}>754 guides · ed.2 · 184 mb</Mono>
      </Row>
    </div>
  );

  // Layouts per form factor ─────────────────────────────
  if (form === 'phone-portrait') {
    return (
      <>
        <IdentityStrip context="HOME" primary="SENKU" secondary="Field manual · ed.2" actions={['search', 'more']} />
        <ScrollPane padX={14} padY={14}>
          <Stack gap={16}>
            <PackStatus />
            <PrimaryAction />
            <Categories />
            <RecentThreads />
          </Stack>
        </ScrollPane>
        <BottomNav active="library" />
      </>
    );
  }
  if (form === 'phone-landscape') {
    return (
      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        <SideRail active="library" />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <IdentityStrip context="HOME" primary="SENKU" secondary="Field manual · ed.2" actions={['search', 'more']} />
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1.1fr 1fr', minHeight: 0 }}>
            <ScrollPane padX={14} padY={12}>
              <Stack gap={12}>
                <PackStatus />
                <PrimaryAction />
                <Categories />
              </Stack>
            </ScrollPane>
            <div style={{ borderLeft: `1px solid ${T.hairline}` }}>
              <ScrollPane padX={14} padY={12}>
                <RecentThreads />
              </ScrollPane>
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
          <IdentityStrip context="HOME" primary="SENKU" secondary="Field manual · edition 2" actions={['search', 'more']} />
          <ScrollPane padX={28} padY={22}>
            <Stack gap={20}>
              <div>
                <div style={{ ...typeStyle('canvasTitle'), color: T.ink0 }}>Field manual</div>
                <div style={{ ...typeStyle('uiBody'), color: T.ink2, marginTop: 6 }}>754 guides · 12 categories · ready offline</div>
              </div>
              <PackStatus />
              <PrimaryAction />
              <div style={{ display: 'grid', gridTemplateColumns: '1.1fr 1fr', gap: 24 }}>
                <Categories />
                <RecentThreads />
              </div>
            </Stack>
          </ScrollPane>
        </div>
      </div>
    );
  }
  // tablet-landscape
  return (
    <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
      <SideRail active="library" wide />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <IdentityStrip context="HOME" primary="SENKU" secondary="Field manual · edition 2" actions={['search', 'more']} />
        <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1.4fr 1fr', minHeight: 0 }}>
          <ScrollPane padX={36} padY={26}>
            <Stack gap={22}>
              <div>
                <div style={{ ...typeStyle('canvasTitle'), color: T.ink0, fontSize: 36, lineHeight: '40px' }}>Field manual</div>
                <div style={{ ...typeStyle('uiBody'), color: T.ink2, marginTop: 6 }}>754 guides · 12 categories · ready offline · ed. 2</div>
              </div>
              <PackStatus />
              <PrimaryAction />
              <Categories />
            </Stack>
          </ScrollPane>
          <div style={{ borderLeft: `1px solid ${T.hairline}`, background: T.bg1 }}>
            <ScrollPane padX={20} padY={20} style={{ background: 'transparent' }}>
              <RecentThreads />
            </ScrollPane>
          </div>
        </div>
      </div>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// SEARCH RESULTS
// ──────────────────────────────────────────────────────────────
function SearchSurface({ form = 'phone-portrait' }) {
  const results = [
    { title: 'Survival Basics & First 72 Hours', id: 'GD-023', cat: 'Survival', match: 0.92, role: 'starter', window: 'immediate', excerpt: 'Shelter Building: Protection from the Elements. Day signaling vs. night signaling…' },
    { title: 'Primitive Technology & Stone Age', id: 'GD-027', cat: 'Survival', match: 0.78, role: 'subsystem', window: 'mixed', excerpt: 'Fire Management — Best tinder: in survival situations, char cloth tops all materials…' },
    { title: 'Tarp & Cord Shelters', id: 'GD-345', cat: 'Shelter', match: 0.74, role: 'topic', window: 'immediate', excerpt: 'A simple ridgeline shelter requires only tarp, cord, and two anchor points…' },
    { title: 'Cave Shelter Systems & Cold-Weather', id: 'GD-294', cat: 'Shelter', match: 0.61, role: 'topic', window: 'long', excerpt: 'Caves provide thermal mass; insulation matters more than airtightness in cold climates…' },
  ];

  const Result = ({ r }) => (
    <div style={{
      borderTop: `1px solid ${T.hairline}`,
      padding: '14px 0',
    }}>
      <Row gap={10}>
        <Mono color={T.accent} size={11}>{r.id}</Mono>
        <span style={{ flex: 1 }} />
        <Row gap={6}>
          <span style={{ width: 28, height: 3, background: T.olive40, borderRadius: 2, position: 'relative' }}>
            <span style={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: `${r.match * 100}%`, background: T.accent, borderRadius: 2 }} />
          </span>
          <Mono color={T.ink2}>{Math.round(r.match * 100)}</Mono>
        </Row>
      </Row>
      <div style={{ ...typeStyle('sectionTitle'), color: T.ink0, marginTop: 4 }}>{r.title}</div>
      <Row gap={6} style={{ marginTop: 6, flexWrap: 'wrap' }}>
        <Mono color={T.olive60}>{r.cat}</Mono>
        <Mono color={T.ink3}>·</Mono>
        <Mono color={T.ink3}>{r.role}</Mono>
        <Mono color={T.ink3}>·</Mono>
        <Mono color={T.ink3}>window {r.window}</Mono>
      </Row>
      <div style={{ ...typeStyle('smallBody'), color: T.ink2, marginTop: 8 }}>{r.excerpt}</div>
    </div>
  );

  const SearchHeader = () => (
    <div style={{ padding: '12px 16px 6px', borderBottom: `1px solid ${T.hairline}` }}>
      <Row gap={8}>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" style={{ color: T.ink2 }}><circle cx="11" cy="11" r="6" stroke="currentColor" strokeWidth="1.6"/><path d="M20 20l-4.3-4.3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>
        <span style={{ ...typeStyle('uiBody'), color: T.ink0 }}>rain shelter</span>
        <span style={{ flex: 1 }} />
        <Mono color={T.ink3}>4 results · 12ms</Mono>
      </Row>
    </div>
  );

  const ResultList = ({ items }) => (
    <Stack gap={0}>
      {items.map((r, i) => <Result key={i} r={r} />)}
    </Stack>
  );

  const FilterRail = () => (
    <Stack gap={14} style={{ padding: '14px 16px' }}>
      <div>
        <SectionLabel>Filter · category</SectionLabel>
        <Stack gap={6} style={{ marginTop: 8 }}>
          {['Shelter (12)', 'Water (4)', 'Fire (3)', 'Survival (8)'].map(c => (
            <div key={c} style={{ ...typeStyle('uiBody'), color: T.ink1, display: 'flex', alignItems: 'center', gap: 8 }}>
              <span style={{ width: 10, height: 10, border: `1px solid ${T.olive40}`, borderRadius: 2 }} />
              {c}
            </div>
          ))}
        </Stack>
      </div>
      <div>
        <SectionLabel>Window</SectionLabel>
        <Stack gap={6} style={{ marginTop: 8 }}>
          {['Immediate', 'Short', 'Long', 'Mixed'].map(c => (
            <div key={c} style={{ ...typeStyle('uiBody'), color: T.ink1, display: 'flex', alignItems: 'center', gap: 8 }}>
              <span style={{ width: 10, height: 10, border: `1px solid ${T.olive40}`, borderRadius: 2 }} />
              {c}
            </div>
          ))}
        </Stack>
      </div>
    </Stack>
  );

  if (form === 'phone-portrait') {
    return (
      <>
        <IdentityStrip context="SEARCH" primary="rain shelter" secondary="" actions={['more']} />
        <SearchHeader />
        <ScrollPane padX={16} padY={4}>
          <ResultList items={results} />
        </ScrollPane>
        <BottomNav active="library" />
      </>
    );
  }
  if (form === 'phone-landscape') {
    return (
      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        <SideRail active="library" />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <IdentityStrip context="SEARCH" primary="rain shelter" secondary="" actions={['more']} />
          <SearchHeader />
          <ScrollPane padX={16} padY={4}>
            <ResultList items={results.slice(0, 3)} />
          </ScrollPane>
        </div>
      </div>
    );
  }
  if (form === 'tablet-portrait') {
    return (
      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        <SideRail active="library" wide />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <IdentityStrip context="SEARCH" primary="rain shelter" secondary="4 results" actions={['more']} />
          <SearchHeader />
          <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '220px 1fr', minHeight: 0 }}>
            <div style={{ borderRight: `1px solid ${T.hairline}` }}>
              <FilterRail />
            </div>
            <ScrollPane padX={28} padY={6}>
              <ResultList items={results} />
            </ScrollPane>
          </div>
        </div>
      </div>
    );
  }
  // tablet-landscape
  return (
    <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
      <SideRail active="library" wide />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <IdentityStrip context="SEARCH" primary="rain shelter" secondary="4 results" actions={['more']} />
        <SearchHeader />
        <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '240px 1fr 320px', minHeight: 0 }}>
          <div style={{ borderRight: `1px solid ${T.hairline}` }}>
            <FilterRail />
          </div>
          <ScrollPane padX={36} padY={6}>
            <ResultList items={results} />
          </ScrollPane>
          <div style={{ borderLeft: `1px solid ${T.hairline}`, background: T.bg1 }}>
            <Stack gap={10} style={{ padding: 18 }}>
              <SectionLabel>Preview · GD-023</SectionLabel>
              <div style={{ ...typeStyle('sectionTitle'), color: T.ink0 }}>Survival Basics & First 72 Hours</div>
              <Row gap={6}>
                <Mono color={T.olive60}>STARTER</Mono>
                <Mono color={T.ink3}>·</Mono>
                <Mono color={T.ink3}>17 sections</Mono>
              </Row>
              <Hairline />
              <div style={{ ...typeStyle('answerBody'), color: T.ink1 }}>
                Day signaling vs. night signaling. Daytime visibility relies on contrast: smoke, ground-marked panels, mirror flash. Nighttime relies on light: reflective surfaces, fire, signal flares.
              </div>
              <Hairline />
              <Mono color={T.ink3}>Tap to open</Mono>
            </Stack>
          </div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { HomeSurface, SearchSurface, ScrollPane, Stack, Row });
