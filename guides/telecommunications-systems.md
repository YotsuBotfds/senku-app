---
id: GD-149
slug: telecommunications-systems
title: Telecommunications Systems
category: communications
difficulty: intermediate
tags:
  - rebuild
  - essential
icon: 📡
description: Telegraph, telephone, and advanced telecommunications — from basic wire communication through switchboards, mesh networks, optical signaling, and community network planning
related:
  - ham-radio
  - crystal-radio-receiver
  - visual-audio-signal-systems
  - mesh-networking-digital-infrastructure
  - community-radio-network-setup
  - community-broadcast-systems
  - electrical-wiring
read_time: 35
word_count: 9040
version: '1.0'
last_updated: '2026-02-22'
liability_level: low
---

:::warning
**Safety Notice:** Telegraph and telephone systems involve electrical circuits that can store dangerous voltages. Battery-powered telegraph systems are low-voltage (safe), but large installation telegraph batteries can deliver high current causing electrical burns. Telephone magnetos (hand-crank ringers) generate significant AC voltage (50-100V) that can cause shock. Never work on energized telegraph/telephone circuits. Insulate all connections. Always discharge capacitors before handling telephone equipment.

**Radio transmission legal notice:** Radio transmission is regulated in most countries. Unlicensed transmission on certain frequencies is illegal. Improvised antennas and transmitters may cause radiation hazards if not properly shielded. High-voltage transmission equipment can cause electrocution. Always follow local regulations, obtain proper licensing before transmitting, and ensure all equipment meets safety standards. In post-collapse scenarios, these restrictions may not apply, but knowledge of regulations prevents interference with emergency services.
:::

<section id="overview">

## Overview: Telecommunications Systems

Telecommunications systems enable human communication across distances beyond shouting range. In survival and post-collapse scenarios, understanding wire-based communication (telegraph and telephone), radio fundamentals, antenna design, and network operation becomes critical—broadcast infrastructure may be damaged or non-functional, requiring improvised or salvaged equipment.

**Communication Methods & Their Roles:**
- **Telegraph:** Discrete on/off signals encoding Morse code via copper wire; range 10-100 miles per station (unlimited with relays); requires only 6-12V battery per station
- **Telephone (wired):** Continuous analog voice signals over copper wire; range 1-10 miles direct (20-100 miles with amplifiers); requires 6-12V battery plus hand-crank magneto for ringing
- **Radio (HF/VHF/UHF):** Long-distance resilient communication; depends on ionosphere (HF) or line-of-sight (VHF/UHF). Requires license in most countries but extremely valuable for emergency coordination.
- **Morse code (CW):** Ultra-reliable narrow-band transmission; tolerates poor signal conditions; requires operator training.
- **Optical signaling:** Mirror flashes, signal flags (no power required, line-of-sight only, limited range).
- **PA systems:** Community announcements; can be acoustic (no electricity) or electronic (requires power but reaches entire settlement).

**Practical Range & Power:**
- HF radio: 500+ km per hop (ionospheric bounce), 1-100W power typical
- VHF radio: 10-100 km line-of-sight, 5-50W typical
- UHF radio: 5-50 km, higher frequency loss, 1-25W typical
- Morse code: 3-5× greater effective range than voice at same power
- Optical flash: 10-50 km (daytime limited)
- Telegraph: 10-100 miles per station, unlimited with relays
- Telephone: 1-10 miles direct, 20-100 miles with amplifiers

For one-way community alerting and scheduled bulletins, hand off to [Community Broadcast Systems](../community-broadcast-systems.html). For local RF nets, repeaters, and operator procedures, use [Community Radio Network Setup](../community-radio-network-setup.html) and [Ham Radio & Field Communications](../ham-radio.html).

:::info-box
**Why redundant communications matter in collapse scenarios:** Grid-dependent cell networks fail completely without power. Radio propagates without infrastructure—it needs only power source and antenna. Wired telegraph and telephone systems depend on local power and line condition, making them valuable for permanent installations where the wire infrastructure survives. Combined knowledge of all three systems (telegraph, telephone, radio) ensures resilient communication options.
:::

</section>

<section id="telegraph-systems">

## Telegraph Systems: Principles & Construction

### Overview: Wired Electrical Communication

Wired electrical communication—telegraph and telephone—represents the first technology that allows rapid exchange of information across distances beyond shouting range. Unlike runners or horseback riders, electrical signals travel at the speed of light through copper wire, enabling coordination of commerce, defense, and communities across continents.

### Theory of Operation

All wired communication depends on a complete circuit: electrons flow from a battery's positive terminal, through the circuit (switch, wire, receiver), and back to the battery's negative terminal. No current flows unless the circuit is complete.

**The basic telegraph circuit:** Battery (+) → Key (switch) → Long-distance wire → Sounder electromagnet → Return wire → Battery (-)

When the key is pressed (circuit completes), current flows through the sounder's coil, creating a magnetic field that pulls an iron armature. The armature strikes a metal piece, producing a "click." Release the key (circuit opens), current stops, spring pushes the armature back—"clack." Morse code is created by varying the timing of clicks.

![Telegraph and telephone circuit diagram showing parallel connections](../assets/svgs/telegraph-telephone-1.svg)

![Telegraph and telephone signal transmission diagram](../assets/svgs/telegraph-telephone-2.svg)

### Signal Strength and Distance

Electrical current flowing through wire encounters resistance proportional to wire length and inversely proportional to wire thickness. Over distance, the signal weakens.

**Resistance in copper wire:**
- #14 AWG (thin): ~2.5 ohms per 1000 feet
- #12 AWG (medium): ~1.6 ohms per 1000 feet
- #10 AWG (thick): ~1 ohm per 1000 feet

**Resistance in steel wire:**
- #12 AWG: ~6 ohms per 1000 feet (4× worse than copper)
- #10 AWG: ~4 ohms per 1000 feet

At 5 miles (26,400 feet) with thin steel wire (#18), total resistance = ~160 ohms. A 6V battery pushing current through 160 ohms delivers weak signal. Higher voltage, thicker wire, or relay stations extend range.

:::tip
For every doubling of wire gauge (e.g., #18 to #16 to #14), resistance is roughly halved. Thick wire is the first choice for extending range.
:::

### Telegraph Key

A telegraph key is a manually operated switch. The operator taps it in Morse code patterns. A good key must be responsive, return reliably, and maintain clean electrical contact.

**Specifications:**
- Contact points: Copper, silver-plated, or hard brass
- Contact gap when open: 1/4" to 3/8" (prevents arcing)
- Spring tension: Light resistance, quick return
- Actuation: Finger-operable, no special strength required

**Salvageable sources:** Old telephone equipment, radio transmitters, telegraph surplus, vintage musical instrument switches.

**Improvised materials:** Steel spring strip, copper coins, wooden base, binding posts from old electrical equipment.

### Sounder/Buzzer

The sounder is an electromagnet that produces audible clicks. When current flows through the coil, it magnetizes the iron core, pulling an iron armature. A spring returns the armature when current stops.

**Specifications:**
- Electromagnet coil: 100-400 turns of #20-#24 magnet wire around iron bolt
- Armature gap: 1/8" to 1/4" (critical for response)
- Operating voltage: 4-12V (higher voltage = stronger pull)
- Current draw: 0.1-0.5 amps (depends on coil resistance)
- Output: Mechanical "click" audible 10-20 feet away

**Salvageable sources:** Old door buzzers, electric bells, relay coils from telephone equipment.

### Telegraph Operating Procedures

**Morse Code Basics:**

Morse code encodes letters and numbers as sequences of dots (short signals, ~0.1-0.2 seconds) and dashes (long signals, ~0.3-0.5 seconds). Timing is critical:

- **Dot:** 1 unit of time
- **Dash:** 3 units of time
- **Gap between elements in letter:** 1 unit
- **Gap between letters:** 3 units
- **Gap between words:** 7 units

**Example: "HELLO"**
```
H: .... (4 dots)  [gap]
E: .    (1 dot)   [gap]
L: .-.. (dot-dash-dot-dot)  [gap]
L: .-.. (dot-dash-dot-dot)  [gap]
O: --- (3 dashes) [word gap]
```

**Practice method:** Use a telegraph key and sounder to practice at slower speeds (5-10 words per minute) before attempting higher speeds. Consistent timing matters more than speed.

### Two-Station Telegraph System Setup

**Step-by-step installation:**

1. **Select stations:** Choose two locations with clear line-of-sight or along roads/property lines. Ideal spacing: 1-10 miles (within range of direct telegraph without relay).

2. **Install poles:** If no existing structures, drive wooden poles 20-30 feet tall, 100-300 feet apart, 3-4 feet into the ground. Guy-wire corner and exposed poles heavily.

3. **Mount insulators:** Attach glass or ceramic insulators to poles with metal rings. Space insulators to prevent cross-contact of multiple wires.

4. **Run wire:** Unroll copper or steel wire along the line, securing with binding wire through each insulator. Leave slight slack to allow for wind and temperature expansion.

5. **Ground system:** At each station, drive a copper rod 6-8 feet into moist earth (near a spring or stream is ideal). Connect ground rod to the telegraph equipment with thick copper wire.

6. **Station equipment:** Mount telegraph key and sounder on a wooden shelf inside a weatherproof shelter. Connect battery in series with the key-sounder circuit.

7. **Test:** Start with short range (100-200 feet of wire). Operator A closes the key while Operator B listens at the sounder. Clicks should be clear and reliable.

8. **Extend range:** If first test works, extend the line gradually, testing at each extension point.

### Telegraph Range & Performance

**Range by wire type and battery:**
- Copper #12 wire: ~5 miles at 6V
- Copper #10 wire: ~10 miles at 6V
- Steel #12 wire: ~2 miles at 6V (high resistance loss)
- 12V battery gives ~1.5× range over 6V. Beyond 20V, limited benefit.

**Factors affecting range:**
- **Ground quality:** Wet soil near water: <1 ohm resistance (~10% better range). Dry sandy soil: 10-100+ ohms (~30-50% worse range).
- **Atmospheric:** Fog/humidity degrades signal 5-10% via insulator moisture. Thunderstorms induce noise.

**To maximize telegraph range:**

1. Use the thickest copper wire available (cost vs. benefit)
2. Minimize contact resistance (clean all connections, solder not crimped)
3. Maximize battery voltage (use 12V if 6V doesn't reach)
4. Minimize ground resistance (drive second ground rod in parallel)
5. Reduce key resistance (contacts should be clean copper, not corroded)
6. Adjust sounder gap to 1/4" for best sensitivity

:::warning
Beyond 50 miles direct telegraph range, signal becomes unrecognizable due to attenuation and noise. Relay stations with human repeaters become necessary for reliable communication.
:::

</section>

<section id="telephone-systems">

## Telephone Systems: Principles & Construction

### Theory of Telephone Operation

**The basic telephone circuit:** Battery (+) → Microphone → Long-distance wire → Earpiece → Return wire → Battery (-)

Unlike telegraph's binary on/off, telephone modulates the current continuously. The microphone varies electrical resistance in response to sound waves. This modulation travels down the wire and drives an electromagnet in the earpiece, which vibrates to recreate the original sound.

### Carbon Granule Microphone

The microphone converts voice vibrations into electrical current changes. Carbon granules have pressure-sensitive resistance: compressed carbon has lower resistance; loose carbon has higher resistance.

**Specifications:**
- Carbon granules: Salvaged from worn-out dry-cell batteries (scrape interior)
- Packed weight: 2-3 grams in microphone chamber
- Diaphragm: Thin metal disk (aluminum or steel, ~0.5mm thick)
- Frequency response: 100-3000 Hz (adequate for speech)

**Construction:** Use a thin diaphragm pressed against carbon granules in a small chamber. Current flowing through carbon is modulated by vibration. Simple and robust; easily salvaged from old telephone handsets.

### Earpiece

The earpiece is a small electromagnet with a diaphragm. Current through the coil modulates the magnetic field, pulling the diaphragm in and out to recreate sound.

**Specifications:**
- Permanent magnet: Salvaged from old speaker or motor (~0.5-1.0 Tesla strength)
- Coil: 100-300 turns of #24-#28 magnet wire
- Diaphragm: Thin iron disk (0.3-0.5mm), mounted 1/32" from magnet face
- Frequency response: 200-3000 Hz (adequate for speech)

:::warning
Earpieces with electromagnets can generate high magnetic fields. Keep them away from steel tools, medical implants, and compass. Never point at electronic devices.
:::

### Two-Station Telephone Setup

**Configuration:**

Each station needs a microphone (for speaking) and earpiece (for hearing).

**Two-wire vs. Four-wire:**
   - **Two-wire:** Cheaper, uses only 1 pair of conductors. One wire carries signal A→B, the return wire carries signal B→A through same conductors. Adequate for 1-5 miles.
   - **Four-wire:** Uses two separate pairs (4 wires total). One pair A→B, separate pair B→A. Better for distances >5 miles.

**Battery at each station:** Each station requires its own battery in series with the local microphone. Batteries must match (both 6V or both 12V).

**Magneto ringer (optional):** For calling attention, install a hand-crank magneto at each station to ring a bell at the far end.

**Wiring diagram for 2-wire telephone:**
```
Station A                                    Station B
Battery (+)                                  Battery (+)
   ↓                                            ↓
Microphone ─── Long-distance wire ─── Earpiece
   ↑                                            ↑
Earpiece ─── Return wire (or ground) ─── Microphone
   ↓                                            ↓
Battery (-)                                  Battery (-)
```

### Telephone Operating Procedures

**For a two-station system:**

1. **Establish connection:** Operator A holds the microphone to mouth (6-8 inches away) and earpiece to ear. Battery and microphone must be in series in the local circuit.

2. **Speak clearly:** Use normal conversational volume. Microphone is very sensitive to vibration; any drop or jolt will produce loud noise at the far end.

3. **Acknowledge receipt:** Use verbal acknowledgments ("I hear you," "go ahead," "over"). Long pauses make the other operator wonder if the line is dead.

4. **Listen actively:** Turn up the volume (if adjustable) to compensate for weak signal. Strain to hear the other operator's voice.

5. **End the call:** When done, hang up the handset (if equipped with hook) or explicitly say "goodbye" and wait for acknowledgment before disconnecting battery.

### Multi-Station Switchboard Network

For 3+ stations, you need a switchboard. A simple switchboard has jacks for each line and patch cords to connect any two lines:

1. **Central office:** Locate at a central position in the community (town hall, general store, etc.).

2. **Jack panel:** Mount one jack per station on the switchboard. Jacks are spring-loaded contacts that accept plugs.

3. **Patch cords:** Flexible cords with plugs on each end. Inserting into two jacks connects those stations' lines together.

4. **Operator:** Trained person who answers the ringer signal, determines which station is calling and which station they want to reach, then inserts the patch cord to connect them.

5. **Magneto ringers:** Each remote station has a hand-crank magneto to alert the central operator that someone wants to call.

:::info-box
Early switchboards (1880s) were fully manual. Operators wore headsets, listened to calls, and controlled connections via patch cords. This job created one of the first mass-employment opportunities for women in industrialized countries.
:::

### Party Line Etiquette

On a party line (multiple stations sharing one line), everyone can hear everyone else's calls. Etiquette rules:

1. **Check line before cranking:** Before making a call, listen on your earpiece. If you hear voice, someone else is using the line. Wait until conversation ends.

2. **Keep calls brief:** Other stations are waiting. Long calls inconvenience the whole community.

3. **Privacy:** Don't listen to other parties' conversations. If you accidentally hear someone else's call, politely hang up.

4. **Emergency priority:** If someone cranks the emergency signal (continuous crank, not discrete pulses), all other stations should disconnect immediately.

### Telephone Operating Procedures (Switchboard)

**For a switchboard system:**

1. **Calling party:** Cranks the magneto ringer on their telephone to alert the central operator.

2. **Central operator:** Hears the ringer, answers the call, and asks which station the caller wants to reach.

3. **Operator inserts patch cord:** Takes a patch cord, inserts one plug into the calling station's jack, the other into the destination station's jack. This connects the two stations' lines together.

4. **Ringing the destination:** Operator hand-cranks a magneto connected to the destination station's line to ring the bell and alert the far station.

5. **Destination answers:** When the far station picks up their handset, the circuit is complete and conversation can begin.

6. **Supervision:** The operator remains on the line to monitor the call. When the conversation ends, both parties hang up (usually an audible "thunk" is heard). The operator then removes the patch cord.

:::tip
Early telephone operators were trained to be courteous and efficient. A skilled operator could handle 15-20 calls per hour and keep a switchboard running smoothly. The operator's voice quality and friendliness mattered—they were the "face" of the telephone service.
:::

### Telephone Range & Performance

**Telephone range:**

| Condition | Performance |
|-----------|------------|
| Direct, 1 mile | Excellent |
| Direct, 5 miles | Acceptable |
| Direct, 10 miles | Barely audible |
| With repeater/amplifier | Extends range to 20-100 miles |

**To maximize telephone range:**

1. Use copper wire only (steel loses too much signal)
2. Ensure microphone and earpiece are in good condition (clean, adjusted)
3. Match battery voltage to equipment (usually 6-12V)
4. Minimize microphone-to-earpiece circuit resistance (use thick conductors)
5. Ensure good coupling between microphone current and earpiece (coil impedance matching)
6. Install repeater (amplifier) station every 20-30 miles for long-distance service

</section>

<section id="wire-networks">

## Wire Networks: Infrastructure & Deployment

### Wire and Insulators Selection

**Wire selection:**

| Type | Gauge | Resistance/1000 ft | Cost | Use |
|------|-------|-------------------|------|-----|
| Copper | #12 | 1.6 ohm | High | Long distances, critical lines |
| Copper | #14 | 2.5 ohm | High | Medium distances (2-5 miles) |
| Steel | #10 | 4 ohm | Low | Short distances with high voltage |
| Steel | #12 | 6 ohm | Very low | Temporary lines, short range |

**Insulator materials (in order of preference):**
1. Glass or ceramic—ideal, nearly infinite resistance, weather-proof
2. Porcelain—good, durable, resists temperature swings
3. Hard plastic—adequate, may degrade in UV light
4. Dry hardwood (oak)—temporary only, rots over months
5. Paper or cloth—never use (conducts when wet)

### Pole Installation & Grounding

**Pole specifications:**
- Height: 20-30 feet for telegraph/telephone
- Spacing: 100-300 feet apart (closer = stronger signal but higher cost)
- Depth: 3-4 feet into the ground minimum
- Guy-wiring: Heavy guy-wires from pole top to ground anchors on corner and exposed poles

**Ground system:**
- Ground rod: Copper, 6-8 feet long, driven deep into moist earth
- Connection: Thick copper wire from ground rod to telegraph/telephone equipment
- Multiple stations: Each station has its own ground rod; can connect multiple rods in series for lower resistance

### Wired Communication Troubleshooting

**No signal at all:**

**Diagnostic checklist (in order):**

1. **Battery dead?** Use a light bulb as a test load. If the bulb doesn't light, battery is dead. Replace with fresh cells. A dead battery will read high voltage with a multimeter but collapse when current is drawn.

2. **Connections loose?** Tug on each wire at every connection point. Binding posts should be tight enough that wires don't move. Clean oxidized contacts with fine sandpaper (120 grit).

3. **Key or microphone stuck?** For telegraph: press the key 10 times rapidly. Should click 10 times clearly. For telephone: tap the microphone chamber. Should hear 10 clicks in the earpiece.

4. **Wire broken?** Walk the entire line looking for physical breaks, cuts, tree branches resting on the wire, or water dripping on insulators. Use a multimeter to test continuity: place one lead on each end of the wire. Should read <1 ohm.

5. **Ground fault?** Drive a short stake into earth near the station. Connect a test wire from the ground rod to the circuit at different points. If you suddenly hear signal, there's a ground fault upstream.

**Weak or intermittent signal:**

**Symptoms:**
- Sounder clicks faintly
- Telephone earpiece barely audible
- Signal works in morning but fails by afternoon (temperature effect on resistance)
- Signal drops in rain or fog

**Root causes:**

**1. Battery aging:** Even if battery shows voltage, it may have high internal resistance. Test by connecting a light bulb and checking voltage doesn't sag more than 10%. If voltage sags >20%, replace battery.

**2. Corroded contacts:** Oxidation of copper or brass surfaces increases resistance. Affects telegraph keys, telephone contact points, and binding posts.
- Fix: Clean with fine sandpaper, apply thin coat of oil or grease to prevent future oxidation

**3. Wet insulators:** Moisture from fog, rain, or humidity causes current to leak through the insulator instead of down the wire.
- Fix: Check insulator condition during humid weather. Replace any cracked or stained insulators. In very humid climates, periodic cleaning of insulator surfaces helps.

**4. Ground fault:** Leakage from wire to ground before reaching destination. Common causes: water dripping on wire, rust on pole eating through insulation, corroded insulator.
- Fix: Walk the line looking for these conditions. Test resistance between wire and ground at several points (should be >1 megohm = 1,000,000 ohms). <100,000 ohms indicates fault.

**5. Loose insulation:** Cracked or peeling insulation allows current to leak.
- Fix: Wrap repairs with electrical tape. For permanent repair, cut out damaged section and splice with solder + tape.

**Static, Noise, or Interference:**

**Symptoms:**
- Crackling in telephone earpiece
- Sounder makes irregular clicks between messages
- Noise worse during thunderstorms or high humidity

**Causes and fixes:**

**Loose connections (most common):** Intermittent contact causes crackling.
- Fix: Tug on every wire. Solder any dry or dull-looking joints (good solder is shiny, cone-shaped).

**Corroded contact surfaces:** Oxidation increases resistance and causes noise.
- Fix: Clean with sandpaper. Apply contact cleaner or thin oil.

**Ground loops:** Multiple paths to ground can cause low-frequency hum.
- Fix: Ensure only one connection from equipment to earth. If multiple ground rods, connect them together but use single wire to circuit.

**Nearby AC power lines:** 50-60 Hz current from power lines induces hum.
- Fix: Route telegraph/telephone wires at least 6-8 feet away from power lines. If unavoidable, use shielded cable with shield connected to ground at one end only.

**Lightning and atmospheric electricity:** Electric fields in air before/during storms induce noise.
- Fix: Install spark gap (1/8" gap between two metal points) at each station to ground. During storms, consider disconnecting the line at the spark gap for protection.

### Wired System Maintenance

**Weekly (for active telegraph/telephone systems):**
- Test local equipment (key, sounder, microphone, earpiece) with short test wire
- Check battery voltage (should be full voltage, no sag under light load)
- Listen for unusual noise or crackling

**Monthly:**
- Walk the entire line, looking for physical damage: broken wire, corroded insulators, water on wire, vegetation growing on poles
- Test ground rod resistance with multimeter: should be <1 ohm
- Clean key and sounder contact points with fine sandpaper
- Test wire continuity with multimeter at several points

**Seasonally (before winter or monsoon season):**
- Check all pole anchoring and guy-wires: tighten any loose bolts
- Clean insulators of dirt and bird droppings (these conduct moisture)
- Inspect for tree branches touching or hanging over wire
- Test line during rain/fog conditions to identify leakage faults

**Equipment care:**

**Telegraph key:**
- Clean contact surfaces monthly with 180-grit sandpaper
- Check spring tension; adjust if key feels too stiff or doesn't return quickly
- Oil the pivot point lightly (do not over-oil; excess oil collects dirt)
- Replace contact coins if they become pitted or corroded (solder new coins on)

**Sounder:**
- Adjust armature gap to 1/4" for best response (test and readjust if performance degrades)
- Clean contact tips (where armature strikes) every month
- Check that iron core is not rusted; if rust is visible, it reduces electromagnet strength
- Oil pivot points lightly

**Microphone:**
- Do not tap or drop (diaphragm can dent, reducing sensitivity)
- Clean diaphragm surface with soft cloth
- Check carbon granules have not settled or packed (shake gently if needed; should be loose)
- Replace carbon granules if they appear dirty or corroded (old carbon from new batteries contains fresh material)

**Earpiece:**
- Keep magnet away from steel tools and metal shavings (they will stick to the magnet)
- Do not expose to strong heat (permanent magnets weaken above 100°C)
- Clean iron diaphragm with soft cloth; never use abrasive materials
- Adjust gap to 1/32" if response becomes weak

**Wire and pole:**
- Inspect wire for breaks, kinks, or corrosion every month
- If wire shows heavy rust, brush or scrape to improve conductivity
- Replace any sections that are heavily corroded (copper wire should be bright; steel wire should be shiny)
- Check that binding wire holding insulator loops is tight; replace any loose bindings
- Replace any insulators that are cracked, discolored, or soft to touch
- Inspect poles for rot: use a knife to check for soft wood at ground level
- Replace any pole showing significant rot (safety hazard)
- Check that pole is vertical and not leaning (guy-wires should be equally tense)
- Tighten any loose bolts on cross-arms and hardware
- Inspect ground rod for rust; copper rods should be shiny
- Check that connection from ground rod to equipment is secure
- Measure ground resistance monthly (should be <1 ohm)
- If resistance rises above 5 ohms, drive a second ground rod in parallel

:::tip
A well-maintained telegraph/telephone system can operate reliably for decades. Preventative maintenance is much cheaper than emergency repairs. Set a regular schedule and stick to it.
:::

</section>

<section id="radio-fundamentals">

## Radio Fundamentals & Transmission

### Electromagnetic Radiation

Radio waves: oscillating electric and magnetic fields perpendicular to propagation direction. Frequency f and wavelength λ related: c = f × λ where c = 299,792,458 m/s (speed of light). Examples: 1 MHz radio → 300m wavelength. 100 MHz FM → 3m wavelength. 2.4 GHz WiFi → 12.5 cm wavelength. Higher frequency = shorter wavelength = smaller antenna = higher bandwidth.

### Amplitude Modulation (AM)

Amplitude of carrier wave varied at audio frequency. Audio signal modulates carrier amplitude: A(t) = A₀[1 + m sin(ωₐt)] sin(ωc t) where m is modulation index (0-1), ωₐ is audio frequency, ωc is carrier. Bandwidth = 2 × audio frequency (single sideband 1 × audio). AM 540-1700 kHz band: each station occupies 10 kHz (maximum audio frequency 5 kHz). Susceptible to noise and interference (amplitude fluctuations carry noise).

### Frequency Modulation (FM)

Frequency of carrier varied at audio frequency. Audio modulates frequency: f(t) = fc + Δf sin(ωₐ t) where Δf is frequency deviation (±75 kHz typical for FM broadcast). Bandwidth = 2(Δf + fₐ) = 2(75 + 5) = 160 kHz per station. FM 88-108 MHz band: 100 stations possible. Better noise immunity (frequency variations don't affect amplitude, noise usually amplitude modulation). Wider bandwidth = fewer stations per band but higher audio quality.

### Frequency Bands & Regulations

**Amateur Radio Allocations**

**HF (3-30 MHz):** 160m, 80m, 40m, 20m, 15m, 10m bands. Long-distance communication via ionosphere reflection (skip). Prone to interference, atmospheric noise (especially 160m/80m).

**VHF (30-300 MHz):** 2m, 70cm bands (144-148 MHz, 420-450 MHz). Line-of-sight propagation 10-100 km range typical. Local communication, repeaters.

**UHF (300-3000 MHz):** 23cm, 13cm bands and higher. Very short-range line-of-sight, high bandwidth. Propagation losses significant (path loss = 20 log₁₀(f×d) where f is frequency MHz, d is distance miles).

### Transmitter Power Limits

FCC Amateur Radio: 1500W PEP (peak envelope power) on HF, 1200W on VHF/UHF, less on higher frequencies. Higher power = greater range but more interference potential. Low-power operation (QRP) 5-10W common for emergency communication and portable operation (lower power consumption, smaller battery, lighter equipment).

### Licensing & Regulations

Amateur radio license required in most countries. US FCC Technician class license allows VHF/UHF transmission after written exam. General class adds HF privileges. Requirements: equipment must not radiate outside licensed frequency band (within ±5 ppm accuracy typical). Call sign required, transmitted periodically (every 10 minutes minimum). Fines: $250,000 maximum for violating frequency restrictions or operating without license.

:::warning
In genuine post-collapse scenarios (government infrastructure non-functional), licensing becomes moot. However, understanding frequency allocations and protocols prevents unintended interference with remaining organized emergency services.
:::

</section>

<section id="antenna-design">

## Antenna Design & Types

### Dipole Antenna

Simplest antenna: two quarter-wavelength conductors extending opposite directions from feedpoint. Total length ≈ λ/2 (slight shortening factor 0.96 typical). Example: 40m band (7 MHz) dipole = 0.96 × 40/2 = 19.2 meters long. Resonant at design frequency (lowest impedance ~70Ω at feedpoint, easily matched). Radiation pattern: maximum radiation broadside (perpendicular to antenna), minimum (nulls) at ends.

**Construction:** Use #12 copper wire (easily soldered), insulated or bare. Hang horizontally between trees or poles, raise 30-50 feet above ground for best performance. Feedpoint (center junction) connects to 50Ω coaxial cable via connector. For temporary portable dipole, use 20-30 feet of #14 wire, suspend via rope.

### Monopole Antenna

Quarter-wavelength conductor mounted vertical, ground plane below. Radiation pattern: omnidirectional (horizontal). Length = λ/4. Example: 2m VHF band monopole = 0.96 × 2/4 = 0.48 meters (19 inches). Ground plane improves radiation (should be 1-2 wavelengths minimum area). Used in mobile/fixed installations where low profile desired.

**Construction:** Vertical rod (aluminum, copper, or fiberglass), base mounting with radial ground straps (4-8 copper wires extending outward from base). Radials should be as long as antenna height. Vehicle roof acts as excellent ground plane for mobile monopole.

### Yagi Antenna

Directional antenna: driven element (dipole) plus parasitic elements (reflector 5% longer, directors 5% shorter). Gains 7-10 dB (5-10× power increase). Narrow beamwidth 30-50 degrees. Popular for directional communication and long-distance links. Three-element Yagi 3-4 feet long on 2m band (frequency determines size).

**Construction:** Aluminum boom (1-inch diameter), aluminum tubing or rod for elements. Driven element connects to coaxial feed. Spacing: typical 0.2-0.3 wavelengths between elements. Mounting: rotator allows pointing toward desired direction. Excellent for repeater access or weak-signal reception.

### Parabolic Dish

High-gain antenna: small dipole or horn at focus of large reflector. Gain increases with dish size and frequency (proportional to (2πd/λ)² where d is dish diameter). 1-meter dish at 2.4 GHz = 34 dB gain = 2500× power multiplication. Used in microwave links and satellite communications. Very narrow beamwidth requires careful aiming.

**Construction:** Salvage satellite TV dish (C-band or Ku-band). Remove LNB (low-noise block downconverter). Mount small dipole or horn at focus. Achieves extraordinary gain at microwave frequencies. Alignment critical—even 2-3 degree offset reduces signal significantly.

:::tip
**Antenna optimization:** Height is more valuable than power. A modest 5W transceiver with antenna 50 feet high often outperforms 50W radio at ground level. Elevation advantage overcomes path loss better than raw transmit power.
:::

</section>

<section id="transmitter-design">

## Transmitter & Receiver Design

### Oscillator

Generates carrier frequency signal. Crystal oscillator most stable: piezoelectric quartz crystal resonates at design frequency (typically 0.001% accuracy). Common frequencies: 1-50 MHz range. Higher frequencies less stable (frequency changes with temperature). Temperature compensation (TXCO): oscillator frequency varies with temperature, automatic correction maintains ±0.5 ppm accuracy.

**Stability requirement:** Frequency must remain constant during transmission to avoid QRM (interference to adjacent channels). 0.1% frequency drift = 50 Hz shift on 50 kHz channel (acceptable). 1% drift = 500 Hz shift (objectionable, interferes with adjacent channel).

### Modulation Stage

Modulator mixes audio signal with carrier. Amplitude modulation: audio controls amplifier bias/gain. Frequency modulation: audio-controlled oscillator frequency varies carrier. Phase modulation: audio varies oscillator phase. Resulting signal: carrier frequency ± (modulation frequency × deviation factor). Output typically 10-100 mW, must be amplified.

### Power Amplifier

Amplifier increases modulated signal to desired transmit power. Class A amplifier: linear (output proportional to input), good for SSB modulation. Efficiency ~50%. Class C amplifier: highly efficient (80-90%) but non-linear (unsuitable for SSB). Good for FM/ASK modulation. Requires efficient heat sinking to dissipate wasted power: 100W transmitter, 90% efficiency = 10W waste heat (must dissipate via heatsink).

### Output Stage & Impedance Matching

Final amplifier stage feeds antenna. Low-impedance output 50Ω typically (matches transmission line and antenna). Impedance matching transformer or tuner ensures maximum power transfer (minimum reflection). Reflected power from antenna mismatch causes heat in transmission line and efficiency loss. SWR (standing wave ratio) <1.5:1 acceptable, <1.2:1 excellent.

**SWR measurement:** Use analog or digital SWR meter in transmission line. Place meter between amplifier output and antenna. If SWR >2:1, antenna is severely mismatched—adjust length, feedpoint location, or add matching network.

:::warning
Mismatched antenna with SWR >3:1 on high-power amplifier causes coaxial cable heating. Severe mismatch can melt cable insulation and create fire hazard. Always verify SWR <2:1 before high-power operation.
:::

### Receiver: Tuned RF Amplifier

Receives weak antenna signal (µV range typical). RF amplifier with tuned input filters desired frequency, rejects out-of-band interference. Gain 10-20 dB (10-100×). Noise figure 2-5 dB (determines receiver sensitivity). Low-noise amplifier (LNA) essential for weak-signal reception: noise figure <1 dB achievable with careful design.

**Noise figure explained:** A receiver with noise figure 6 dB has internal noise equal to 6 dB amplification of input noise. Weak signals get amplified equally with noise, reducing signal-to-noise ratio. Lower noise figure = better weak-signal reception. Critical for CW (Morse code) reception where signal may be barely audible.

### Superheterodyne Receiver

Received signal mixed (frequency conversion) with local oscillator. Intermediate frequency (IF) = Receive frequency - Local oscillator frequency (or + for upper sideband). Example: 146 MHz signal mixed with 145 MHz local oscillator = 1 MHz IF. All received stations converted to same IF, then amplified and filtered (IF filter). Then demodulated to recover modulation (audio).

### Demodulation

**AM demodulation:** Diode detector or synchronous detector. Simple diode: rectifies AM signal, capacitor filter removes RF ripple, leaving audio. Synchronous: multiplies received signal by recovered carrier frequency, more selective (better rejection of adjacent stations).

**FM demodulation:** Frequency discriminator or phase-locked loop (PLL). Discriminator: frequency-sensitive filter converts frequency deviation to amplitude, demodulates like AM. PLL: locks oscillator to incoming frequency, control voltage output is demodulated audio. PLL more robust to interference.

### Automatic Gain Control (AGC)

Receiver gain varies with signal strength (strong signals overload, weak signals inaudible). AGC circuit adjusts gain automatically: strong signal → reduce gain, weak signal → increase gain. Maintains relatively constant audio output regardless of received signal strength. Attack time (fast gain reduction) 100 ms, decay time (slow gain recovery) 1-2 seconds. AGC prevents receiver saturation and improves dynamic range.

:::tip
**Manual gain override:** For weak-signal CW reception, disable AGC and manually set RF/AF gain. AGC's attack time causes fading—manual control allows operator to compensate for QSB (signal fading).
:::

</section>

<section id="morse-code">

## Morse Code & CW (Continuous Wave)

### Morse Code Basics

Dots (dit) and dashes (dah) represent letters/numbers. Timing: dot duration = 1 unit, dash = 3 units, space between elements = 1 unit, space between letters = 3 units, space between words = 7 units. Speed measured in words-per-minute (WPM): PARIS standard word (50 dots) at 1 WPM = 1.2 seconds per word. Typical speeds: 5-25 WPM novice, 25-40 WPM experienced operator.

**Common letters:** E (•), T (-), A (•-), N (-•), I (••), O (---), S (•••), H (••••), SOS distress (•••---•••).

### Morse Code Quick Reference

| Letter | Code | Letter | Code | Number | Code |
|--------|------|--------|------|--------|------|
| A | · — | N | — · | 0 | — — — — — |
| B | — · · · | O | — — — | 1 | · — — — — |
| C | — · — · | P | · — — · | 2 | · · — — — |
| D | — · · | Q | — — · — | 3 | · · · — — |
| E | · | R | · — · | 4 | · · · · — |
| F | · · — · | S | · · · | 5 | · · · · · |
| G | — — · | T | — | 6 | — · · · · |
| H | · · · · | U | · · — | 7 | — — · · · |
| I | · · | V | · · · — | 8 | — — — · · |
| J | · — — — | W | · — — | 9 | — — — — · |
| K | — · — | X | — · · — |
| L | · — · · | Y | — · — — |
| M | — — | Z | — — · · |

### CW Transmission

Continuous wave: carrier frequency modulated on/off at Morse code rate. No amplitude or frequency modulation, just on/off keying. Requires: oscillator, keyer (electronic circuit switching carrier on/off), power amplifier. Very simple transmitter (can be hand-built in hours). Requires training to send/receive (motor skill, hearing interpretation).

**Hand-keying vs electronic keyer:** Hand-key (straight key) produces variable timing, less precise but works with simple equipment. Electronic keyer generates perfect dot/dash timing, easier on wrist, more consistent speed.

### CW Reception

Received CW signal very weak (µV typically). RF amplifier must achieve high gain (20-30 dB minimum) to pull signal out of noise. Tuned receiver (narrow filter, <100 Hz bandwidth) rejects interference outside frequency. Audio oscillator (beat frequency oscillator, BFO) shifted 700 Hz from receiver frequency produces audio tone audible to operator. Operator listens to audio tone pattern, decodes Morse code.

### Advantages of CW

Narrow bandwidth (100-300 Hz vs 2.7 kHz for SSB voice = 27× less spectral space). CW signal 3 dB stronger than voice for same transmit power (because CW is continuous during transmission, voice has 50% average power due to speech pauses). Result: CW effective range 5-10× greater than voice at same power. CW immunity to interference superior: pulse noise causes minimal disruption to Morse code reception (operator can tolerate 30% noise corruption).

**Survival advantage:** CW requires no microphone, preamp, or modulator—just oscillator, keyer, and amplifier. Hand-cranked generator can power CW transmitter. Voice transmitter requires stable power (battery or solar), sensitive audio circuitry.

:::info-box
**CW Effective Range Table:**

| Transmit Power | Voice Range (line-of-sight) | CW Range | Conditions |
|---|---|---|---|
| 5W | 10-20 km | 50-100 km | 2m band, clear terrain |
| 20W | 20-40 km | 100-200 km | 2m band, repeater access |
| 100W HF | 500+ km | 2000+ km | Skip propagation possible |
| 1W | 5-10 km direct | 30-50 km | 2m portable, hilltop antenna |

:::

</section>

<section id="signal-propagation">

## Signal Propagation Modes

### Ground Wave

Signal follows Earth's curvature, diffraction around terrain. Most effective at low frequencies (LF, MF, HF). Range depends on frequency and power: 1 MHz at 1 kW ground wave reaches ~100 km. 100 MHz (VHF) ground wave ~10 km only. Ground conductivity significant: seawater excellent (extends range), sand poor (reduces range). Used for maritime communication and long-distance broadcasting at HF/MF.

### Ionospheric Reflection (Skip)

Radio waves at HF frequencies (3-30 MHz) refracted by ionosphere (charged particle layers 80-400 km altitude). Rays bounce off ionosphere, returning to Earth, reflecting off ground, bouncing back to ionosphere, repeated globally. Range: 500-3000 km per hop (one bounce). Multiple hops enable worldwide communication at low power.

### Ionosphere Variability

Ionosphere strength varies: daily (weakest at night), seasonally (weakest in summer), and with 11-year solar cycle. Solar flares increase ionospheric density (extends range, improves propagation). Sunspots increase frequency of flares. Current solar cycle (2024): cycle 25, approaching peak (high sunspot activity). Best HF conditions last 1-2 years around solar maximum.

### Tropospheric Propagation

VHF/UHF signals refracted by troposphere (weather layer, 0-15 km altitude). Under normal conditions: line-of-sight only (horizon limit). Temperature inversion (warm layer above cold layer) causes ducting: signals guided around Earth's curvature, extending range 300-500 km. Occurs after sunset during stable atmospheric conditions. Rare but dramatic improvement enables long-distance 2m band contacts.

:::tip
**Predicting skip on HF:** Check solar flux index (SFI) and K-index. SFI >100 indicates good skip propagation. K-index <4 means low geomagnetic storm activity (favorable). Check Space Weather Prediction Center (NOAA) before attempting long-distance HF contacts.
:::

</section>

<section id="repeaters-mesh-networks">

## Repeaters, Mesh Networks & Advanced Systems

### Repeater Stations

Fixed station receiving weak VHF/UHF signal from mobile, retransmitting amplified signal on different frequency. Extends communication range: mobile-to-mobile direct range 10-50 km, mobile-to-repeater 50 km, repeater-to-mobile 50 km = total 100 km. Repeater typically located on high hill (maximizes line-of-sight coverage). Operates 24/7, shared community resource.

**Duplex operation:** Repeater transmits and receives simultaneously on different frequencies (duplex spacing). Example: 146.52 MHz input, 146.92 MHz output (±0.6 MHz offset, standard for 2m band). Mobile transmits on input frequency, listens on output. Requires simultaneous Tx/Rx equipment, duplexer filter separating Tx/Rx lines (very high isolation >80 dB required to prevent Tx energy destroying receiver).

**Repeater linking:** Multiple repeaters linked via UHF microwave link or internet (VOIP gateway). Transmit on one repeater, receive on distant linked repeater. Enables regional/national communication networks. Internet linking allows worldwide communication using local repeater (DeStar, Echolink systems). Reduces need for long-distance HF propagation.

**Community repeater setup (survival context):** Post-collapse community may establish local repeater from salvaged equipment (two transceiver units, duplexer, antenna system, power source). Location on high point (water tower, hill, tall building) critical—extends coverage to entire settlement. Requires backup power (solar panel, battery bank, or hand-cranked generator). Antenna system: separated transmit/receive antennas 10+ feet apart, or single antenna with high-isolation duplexer.

### Optical Signaling & Line-of-Sight Methods

For situations where radio/wire infrastructure is unavailable, optical signaling provides long-range, line-of-sight communication without power.

**Mirror/Heliograph signaling:** Reflected sunlight directed at distant observer. 1-square-meter mirror visible 50+ km under clear conditions. Method: place observer at destination, adjust mirror to reflect sun's light toward observer. Receiver sees "flashes" which can be timed to encode Morse code.

**Signal flags:** Pre-arranged flag positions encode messages. Slower than Morse (requires continuous visibility to receiver) but reliable. Used on ships, mountaintops, and across valleys.

### Mesh Networks & Community Coordination

In post-collapse scenarios, a network of distributed repeaters and nodes creates redundant communication infrastructure. Design principles:

1. **Network topology:** Each node has range of 20-50 km. Position nodes at high points (hilltops, tall buildings) to maximize line-of-sight overlap. Overlap creates mesh—signal can route multiple paths.

2. **Frequency planning:** Central frequency for all simplex communication (all stations listen/transmit on same frequency). Reduces confusion, requires discipline (wait for clear frequency before transmitting).

3. **Trunking:** In large networks, frequency allocation may be managed by automated trunking system (radio directs calls to available frequency). Complex but efficient.

4. **Backup communication:** Every node maintains fallback to wired telegraph/telephone for essential service (medical, security) in case radio infrastructure fails.

</section>

<section id="telegraph-telephone-common-mistakes">

## Common Mistakes & How to Avoid Them

### Telegraph Operation

**1. Poor Morse code timing:**
- Problem: Dashes too short, dots too long, inconsistent spacing
- Result: Receiver interprets letters incorrectly
- Prevention: Practice at slower speeds (5-10 wpm) until timing is automatic. Use a metronome. Record your sending and compare to standards.

**2. Key worn too loose:**
- Problem: Key bounces multiple times with each press, creating extra clicks
- Result: Receiver gets garbled message with extra letters
- Prevention: Adjust spring tension so key returns cleanly with one bounce. Test by pressing rapidly.

**3. Ground connection forgotten:**
- Problem: No return path for current, circuit incomplete
- Result: No signal at all
- Prevention: Check ground rod is driven deep and connected before testing. Test with a light bulb first.

**4. Contact points not cleaned:**
- Problem: Oxidized contacts increase resistance, weakening signal
- Result: Weak or intermittent clicks
- Prevention: Clean contacts monthly with sandpaper. After cleaning, apply thin oil to prevent re-oxidation.

### Telephone Operation

**1. Microphone too close to mouth:**
- Problem: Loud pops and distortion from plosive sounds
- Result: Receiver hears unintelligible sound
- Prevention: Hold microphone 4-6 inches from mouth, not touching lips.

**2. Earpiece volume too high:**
- Problem: Hearing feedback/howl if earpiece is near microphone
- Result: Conversation becomes unintelligible
- Prevention: Keep earpiece away from microphone. If volume adjustment available, start low and increase only as needed.

**3. Interrupted by static without investigating:**
- Problem: Operator assumes line is bad and hangs up
- Result: Communication is abandoned when problem may be easily fixed
- Prevention: When static occurs, try: (a) moving receiver away from microphone, (b) checking battery, (c) cleaning contacts, (d) checking ground. Only give up after systematic troubleshooting.

**4. Speaking over the other party:**
- Problem: Both operators try to talk simultaneously
- Result: No one understands anything
- Prevention: Use "over" or "go ahead" protocol. Speaker finishes, says "over," then listens. Only one person speaks at a time.

### Line Installation

**1. Wire sagging too much:**
- Problem: Wire dips to ground, shorts out, or breaks under snow load
- Result: Line becomes unusable
- Prevention: Ensure proper pole spacing (100-300 feet). Use appropriate wire gauge for distance. Allow slight sag but not below safe height.

**2. Insulators not properly installed:**
- Problem: Wire directly touches pole or other metal, current leaks to ground
- Result: Weak or no signal
- Prevention: Always use insulators between wire and pole/hardware. Use multiple insulators if needed. Never wrap wire directly around metal.

**3. Ground rod not driven deep enough:**
- Problem: High ground resistance (10-100+ ohms)
- Result: Weak signal, especially after rain (ground dries out)
- Prevention: Drive copper rod at least 6-8 feet deep. Tap down with sledgehammer until solid. In dry areas, drive second rod nearby and connect them together.

**4. Lightning protection neglected:**
- Problem: First lightning strike destroys all equipment at station
- Result: Expensive repairs, weeks of downtime
- Prevention: Install spark gap (1/8" gap) at each station connected to ground rod. This sacrifices itself to protect equipment. Inspect and rebuild after each lightning strike.

:::warning
Never work on telegraph/telephone lines during thunderstorms. Lightning can strike from clear sky at the edge of a distant storm. If you hear thunder, stop work and disconnect the line immediately.
:::

</section>

<section id="radio-operating-procedures">

## Radio Operating Procedures & Protocols

### Station Layout (Amateur Radio)

Optimal station arrangement:
1. **Transceiver:** Central position, microphone/key easily accessible
2. **Antenna tuner:** Between transceiver output and antenna
3. **Power supply:** Low-noise supply, heavy gauge wiring to radio
4. **Antenna feedline:** Coaxial cable, direct route without sharp bends (causes loss)
5. **Grounding:** All equipment bonded to common ground rod (8-foot copper rod, 6 feet minimum)

**Cable runs:** Keep RF cables away from power cables (prevents RF noise injection). If parallel runs unavoidable, cross at right angles. Shield microphone cable (prevents hum pickup from AC power lines).

### Frequency Coordination

Know local repeater frequencies and offsets. Transmit on input frequency, listen on output frequency. Many regions have frequency coordinators who manage repeater allocations to prevent interference.

**Simplex operation:** Direct radio-to-radio without repeater. Choose clear frequency (listen first, wait 5 seconds before transmitting). Common simplex frequencies: 146.52 MHz (2m national calling frequency), 223.50 MHz (1.25m), 446.00 MHz (70cm).

### Testing Before Full Power

Start with low power (1-5W). Listen for interference to other signals (scan band before transmitting). Adjust antenna if SWR high. Gradually increase power while monitoring SWR and signal reports from other stations.

### Call Format (Amateur Radio)

**Calling:** "CQ CQ CQ, this is [your call sign], calling CQ."
**Response:** "CQ, this is [responder's call sign], loud and clear."
**Connection:** Exchange signal reports, QTH (location), equipment, name. Keep transmissions brief (reduce power consumption, allow others to use frequency).

**Signal reports (RST system):**
- **R (readability):** 1 = unreadable, 5 = perfectly readable
- **S (strength):** 1 = barely audible, 9 = extremely strong
- **T (tone):** 1 = very rough, 9 = perfectly clear (CW only)

Example: "You're 59" (strong, perfectly readable, good tone).

### Voice Procedure

Speak clearly, natural pace. Avoid slang. Use phonetic alphabet for callsigns in noisy conditions: "Whiskey Four Xray Bravo" for W4XB.

### Simplex Etiquette

- Listen before transmitting (5-10 seconds minimum)
- Keep transmissions under 2 minutes
- Pause briefly between exchanges (allow others to break in if urgent)
- Identify by callsign every 10 minutes (legal requirement)
- End with "over" to indicate waiting for response, "out" to end conversation

### Emergency Procedures

**MAYDAY call:** "MAYDAY MAYDAY MAYDAY, this is [your call], I have emergency situation..." Use on any channel if life/property at risk. All stations must cease transmission and monitor. MAYDAY is recognized worldwide, overrides all other traffic.

</section>

<section id="range-performance">

## Range & Performance Predictions

### Path Loss & Distance

Free-space path loss (ideal line-of-sight): L(dB) = 20 log₁₀(4πd/λ) where d = distance (meters), λ = wavelength.

Practical calculation:
- 2m band (146 MHz): 10 km distance → path loss ~92 dB
- 70cm band (450 MHz): 10 km distance → path loss ~104 dB (12 dB worse)
- Same power transmitter, 70cm receiver must be 4× more sensitive to equal range

### Terrain Effects

**Fresnel zone:** Radio path acts as line-of-sight even if obstructed by terrain. First Fresnel zone should be 60% clear (no major obstructions). Obstruction in Fresnel zone causes significant path loss.

First Fresnel zone radius: r = √(λd₁d₂/d) where d₁ = distance from transmitter to obstruction, d₂ = distance from obstruction to receiver, d = total distance.

**Example:** 2m band (0.2m wavelength), 20 km path, obstruction 10 km from transmitter:
r = √(0.2 × 10 × 10 / 20) = √10 ≈ 3.2 km

A mountain 3.2 km tall at 10 km midpoint blocks path entirely. However, if peak is 1 km high, it penetrates first Fresnel zone causing 3-5 dB path loss (acceptable).

### Real-World Range Table

| Band | Power | Antenna | Terrain | Range | Notes |
|---|---|---|---|---|---|
| 2m | 5W | 1/4-wave | Flat | 5-10 km | Portable, line-of-sight |
| 2m | 50W | Dipole 30 ft | Hilly | 40-80 km | Fixed station, elevated antenna |
| 70cm | 5W | 1/4-wave | Flat | 3-5 km | Very terrain-dependent |
| HF (40m) | 100W | Dipole 50 ft | Any | 500+ km | Ionospheric skip, variable |
| HF (160m) | 1000W | Dipole 100 ft | Any | 1000+ km | Night propagation good |

### Effect of Antenna Height

Doubling antenna height (on same band) gains approximately 6 dB (4× power equivalent). Height advantage is most valuable improvement for fixed stations. Mobile/portable antennas benefit less (limited mounting height).

**Low vs High Antenna:**
- Low antenna (5 ft): 5W with 2m antenna reaches 5 km
- High antenna (50 ft): Same 5W with 2m antenna reaches 15-20 km (3-4× improvement)

:::info-box
**Survival Radio Power Budget:**

100W transmitter at 50 feet antenna elevation:
- VHF line-of-sight: 80-100 km typical
- HF with skip: 500+ km (weather/season dependent)
- Battery capacity: 50Ah at 12V = 600 Wh, sustained 100W use = 6 hours
- Solar panel: 100W rated, 300-500 Wh daily (season/location dependent)

Recommendation for survival comms: 20W radio (lower power, lighter), 20Ah battery (lower weight portable), small solar panel (100W) for base station recharging.

:::

</section>

<section id="community-pa">

## Community PA & Announcement Systems

### Purpose & Importance

In post-collapse contexts, centralized announcement systems enable rapid communication of critical information: emergency alerts, gathering notices, event schedules, weather warnings. A community PA system extends information reach beyond shouting distance (100-150 feet maximum) to entire settlement (0.5-2 miles depending on power and terrain). Warning sirens provide urgent alerts (fire, attack, evacuation) audible across entire region.

### Acoustic PA Systems Without Electricity

Mechanical sound amplification requires no power—ideal for guaranteed function during outages.

**Horn-Based Systems:**
- **Metal horns (salvaged or hand-made):** Driven by human voice or mechanical mechanism. A 4-6 foot metal or PVC horn amplifies sound 10-20 dB (10-100× amplitude increase). Achievable range: 0.5-1 mile in calm conditions. Handheld or mounted on pole. Construction: tapered cone of metal/PVC with mouthpiece. Test: speak into mouthpiece—sound travels much further than unaided voice.
- **Speaking tubes (whisper tubes):** Copper or PVC piping running through settlement, large funnels at speaking points. Traditional design used in tall buildings pre-electricity. Acoustic advantage: directs sound into confined tube, prevents dissipation. Disadvantage: limited to predefined routes, bidirectional (anyone can speak/listen).
- **Megaphone (hand-cranked or built):** Simple hand-held horn amplifier. 20-30 foot range, audible but exhausting to use continuously. Cheap to build or salvage.

**Mechanical Resonators:**
- **Large bells or gongs:** Suspended, struck with hammer. Audible 1-2 miles depending on size and material. Different tones can signal different meanings: one tone = assembly, two tones = fire, three tones = all-clear. Requires person with strength to ring continuously. Does not convey detailed information, only alert signals.
- **Drums:** Similar to bells but shorter range (0.5-1 mile). Benefit: rhythm patterns can encode messages. Disadvantage: labor-intensive.

### Signal Coding for Emergency Communication

Without text-based communication, standardized tone patterns enable rapid understanding of threat/situation.

**Siren Signal Protocol (Example):**
- **One long blast (10+ seconds):** General assembly/gathering. All community members report to central point for announcement.
- **Two short blasts (2 sec each, 2 sec apart):** Fire alert. Identify location, community directs response team.
- **Three short blasts (repeating):** Evacuation/danger. Leave current location, proceed to evacuation rally point.
- **Steady tone (5-10 sec):** All-clear signal. Threat has passed, resume normal activity.
- **Irregular/chaotic blast pattern:** Total emergency (attack, disaster). Take shelter immediately.

</section>

<section id="security-encryption">

## Security & Encryption

### Wire Security Concerns

Telegraph and telephone signals travel along physical wires that can be intercepted:

**Vulnerabilities:**
- Anyone with access to the wire can connect a receiver and listen in
- Someone could cut the wire and insert a repeater to copy all messages
- Operators with access to switchboards could listen to any call
- Telegraph messages are sent in clear Morse code (not encrypted by default)

**Physical security:**
- Locate telegraph/telephone office in a locked, guarded building
- Run wires through private property only (avoid public roads where they're accessible)
- Install lightning protection (spark gaps) but also physical barriers to prevent tampering
- Rotate operators regularly to prevent unauthorized listening

### Morse Code Encryption

Raw Morse code is readable by anyone with a sounder. To create privacy, the message can be encrypted before transmission:

**Substitution cipher (simple):**
- Agree on a mapping of letters (e.g., A→Q, B→W, C→E, etc.)
- Sender encrypts message using the cipher
- Transmits encrypted letters in Morse code
- Receiver decrypts using the shared cipher

Example: "HELLO" encrypted with a simple shift cipher (each letter shifts by 3): "KHOOR"

**Limitations:** Substitution ciphers are vulnerable to frequency analysis. If 30+ words are transmitted, the cipher can usually be broken in hours.

**One-time pad (unbreakable):**
- Create a random list of 100+ numbers
- Each sender and receiver has an identical list
- Each message is XORed or added (modulo 26) with the random numbers
- After transmission, cross off those numbers; never reuse

Example: Message "HELLO", one-time pad [3, 1, 5, 2, 4], encrypted: "KHKPQ"

**Advantages:** Mathematically proven unbreakable if the pad is truly random and never reused.
**Disadvantages:** Requires advance creation and secure delivery of identical pads to both stations.

:::info-box
During World War I and II, military telegraph systems used rotor-based cipher machines (like the Enigma machine) to encrypt messages. The rotor configuration was changed daily, providing security against most decryption attempts. One-time pads were used by governments and intelligence services.
:::

**Operational security (OPSEC):**
- Use coded language for sensitive information (e.g., weather forecast = shipment status)
- Transmit sensitive calls/messages during night when fewer people are present
- Change operator rotation to prevent familiarity with messages
- Limit knowledge of important messages to those who need to know
- For critical military/commercial messages, use encryption + trusted courier for final delivery

### Radio Encryption

In survival/post-collapse scenarios, sensitive communication (medical emergencies, security threats, resource allocation) requires protection from interception. Amateur radio is not private (all signals receivable by public scanners). Encryption considerations:

- **Open communication:** All transmissions audible to anyone with receiver. Useful for emergency broadcasts (medical help, all-clear signals), community announcements.
- **Private communication:** Encrypted or using obscure frequency/time reduces passive interception. Necessary for: tactical coordination, medical privacy, security information.

**Encryption methods:**

**Analog voice encryption (not reliable):**
- Scrambling: audio frequency inversion or other simple transforms. Easily broken with scanner (listen to inverted audio). Not recommended.

**Digital encryption (strong):**
- Uses cryptographic algorithms (AES, DES). Encrypted digital data looks like random noise to unencrypted receiver. Requires shared encryption key between parties. Example: Digital voice systems like D-Star, DMR (Digital Mobile Radio), or P25 include encryption.

**Practical approach:** Most post-collapse radio comms will be analog (simpler, less dependent on infrastructure). Encryption in analog domain is difficult. Alternative: use frequency coordination (operate on known, agreed frequency, establish scheduled times), code words (pre-arranged meanings), or restrict sensitive comms to direct radio links (not repeaters).

:::tip
**Code words for community:** Establish simple code (e.g., "Medical 10" = someone injured, "Security 20" = suspicious activity). Train all operators. Code words allow information transfer without spelling out details on open frequency.
:::

**Frequency agility:** Changing transmit/receive frequency unpredictably reduces chance of systematic monitoring. Modern radios support frequency banks (multiple channels). Community could establish random daily frequency selection from 5-10 approved channels (all operators pre-load all channels). Higher burden, but greater privacy from passive eavesdropping.

</section>

<section id="safety">

## Safety: Electrical Hazards & RF Protection

### Electrical Safety

**Telegraph and telephone systems operate at low voltage (6-12V), which is safe to touch.** However, larger systems and long-distance lines may use higher voltages (20-40V), which can cause injury.

**Safety rules:**

1. **Never work on a live line:** Always disconnect the battery before working on telegraph/telephone equipment. If the line might be live, short the conductors to ground first to discharge any stored energy (unlikely at low voltage but good practice).

2. **Ground your body:** Before touching circuit components, touch a ground rod or earth connection to discharge any static electricity. This is especially important in dry conditions.

3. **Use one hand:** When testing a live circuit, keep one hand in your pocket and test with the other hand only. If you become the path for current (hand-to-hand across chest), heart could be affected. One-hand testing reduces this risk.

4. **Inspect for moisture:** Don't work on outdoor telegraph/telephone lines when wet or during rain. Moisture increases conductivity and risk of shock.

5. **Watch for AC power lines:** Keep telegraph/telephone wires at least 6-8 feet away from AC power lines. If they touch or are too close, 110-240V AC from the power line could be induced into the telegraph line. A touch at that point would be fatal.

6. **Never mix AC and DC:** If you're troubleshooting and need to connect test equipment, use DC-only (battery powered). AC applied to telegraph/telephone equipment will destroy components and create shock hazard.

### Line and Pole Safety

**Pole climbing (if needed for repairs):**

1. Use a safety belt and rope when climbing above 10 feet
2. Wear gloves and long sleeves to prevent splinters
3. Test each step before putting full weight on it
4. Never climb in high wind or thunderstorm
5. Have a second person below to assist and call for help if needed

**Working under overhead wires:**

1. Never throw objects at wire to clear them (object could stick and arc)
2. If wire is blocking work, use an insulated stick (dry wood or PVC) to move it gently
3. Never use a metal ladder if working near telegraph/telephone wires
4. Never allow crane or vehicle to pass under wire without checking clearance (rule of thumb: maintain 6-8 feet minimum clearance below power lines)

**Ground rod installation:**

1. Wear safety glasses when driving ground rod (sparks from sledgehammer)
2. Check ground is soft and not hitting rock or buried pipe before driving deeply
3. If driving hits something hard, stop and investigate (could be underground electric line, gas line, or well)
4. Never drive ground rod into area with known underground utilities

**General cautions:**

- Never assume a line is dead; always test for voltage before assuming it's safe
- Never work alone on anything involving climbing or digging
- Keep a first aid kit nearby in case of injury
- For any doubt, ask an experienced electrician or telegraph operator for guidance

### RF Exposure

Transmitting antennas radiate electromagnetic energy. High frequencies (UHF, microwave) concentrate energy near antenna (near-field), causing tissue heating. Safety guidelines (FCC, ICNIRP):

**Power density limits (general population):**
- 2 MHz: 100 mW/cm² continuous exposure
- 50 MHz: 1 mW/cm²
- 300 MHz: 1 mW/cm²
- 1500 MHz: 5 mW/cm²

At 5 feet from 50W 2m antenna: power density ~50 mW/cm² (slightly above limit). At 20 feet: ~3 mW/cm² (below limit).

**Practical safety:** Keep distance from antenna during operation. Vertical monopoles have nulls (safe zones) directly above and below. Horizontal dipoles have nulls at ends (safe zone at antenna ends, but not broadside). For high-power operation, restrict antenna area (fencing) or limit access during operation.

### Lightning Protection

**Risk:** Lightning strike on antenna and feedline can vaporize cable, destroy radio, cause fire in building.

**Mitigation:**
1. Proper grounding (separate ground rod for antenna, low-inductance bonding to station ground)
2. Lightning arrestor (gas-tube surge protector at feedline entrance to building)
3. Disconnect feedline during thunderstorms (safest method, breaks RF path)
4. Surge protector on power supply (protects from transient)

**Frequency of strikes:** In high-strike areas (Florida, Rocky Mountains), disconnect antenna seasonally or during storm season. In low-strike areas, permanent antenna with good grounding acceptable.

### Battery Hazards

**Lead-acid battery:**
- Hydrogen gas release during charging (explosive concentration possible)
- Acid splash (wear eye protection when checking water level)
- Never use spark-producing tools near battery (ignition risk)

**Lithium battery:**
- Extreme fire hazard if overcharged or short-circuited (thermal runaway)
- Use BMS (battery management system) to prevent overcharge
- Store away from flammables
- Never leave charging unattended

**Safe practices:** Charge batteries in ventilated area, never in occupied spaces. Use proper charger (prevents overcharge). Inspect regularly for swelling/damage. Keep fire extinguisher nearby (CO2 for lithium fires, not water).

### Tower Climbing

If antenna mounted on tall structure requiring climbing for maintenance:
- Use safety harness and rope
- Never climb alone (spotters required)
- Climb during calm weather (no wind >20 mph)
- Avoid climbing when tired or during darkness
- Never climb during thunderstorms

Professional climbers recommended for >40 feet height. DIY climbing on <30 feet structures acceptable with proper safety gear.

:::warning
RF burns and tower accidents are leading causes of radio operator injury/death. Professional safety practices (distance from antenna, proper grounding, fall protection) are not optional—they're essential. Many regulations exist for good reason.
:::

</section>

<section id="troubleshooting">

## Troubleshooting Common Problems

### Radio Transmission Issues

**No signal transmission:**

**Diagnosis:**
1. Verify power (check battery voltage, wall power)
2. Check microphone/key connection (disconnected cable common cause)
3. Listen to frequency first (may be in use, causing TX inhibit)
4. Test antenna connection (loose N-connector, corrosion)
5. Verify antenna with handheld (test on known-good radio)

**Solutions:** Reconnect cables, clean connectors with isopropyl alcohol, test with alternate antenna, measure continuity with multimeter.

**Weak signal or no reception:**

**Check in order:**
1. Antenna connection (same as TX diagnosis)
2. Antenna tuning (monopole resonance frequency drifts with temperature, humidity)
3. Low signal = frequency propagation issue (skip may be poor, distant station weak)
4. Receiver gain set too low (check AF/RF gain controls)

**Verification:** Switch to known strong station (local repeater) to confirm receiver works. If repeater loud and clear, distant signal weakness is propagation, not equipment failure.

**Interference on frequency:**

**Sources:**
1. Adjacent channel interference (nearby transmitter on adjacent frequency)
2. Harmonic interference (transmitter fundamental outside band, harmonic lands in receive band)
3. Intermodulation (two strong signals mix, producing new frequency in your band)

**Mitigation:**
- Improve antenna isolation (separate Tx/Rx antennas 10+ feet apart)
- Change frequency (if possible, switch to clear simplex channel)
- Reduce power (lower transmitter power reduces interference radiated)
- Add filter (notch filter rejects strong interfering frequency)

**Distorted audio:**

**Causes:**
1. Microphone input too hot (audio clipping in modulation stage)
2. Receiver overload (strong signal saturating receiver, causing intermod)
3. Loose connector causing intermittent ground (crackling/popping sound)
4. Failing amplifier (non-linear distortion, no recovery)

**Fixes:**
- Reduce microphone gain or distance from mic
- Reduce RF gain if receiver is saturated
- Clean connectors, apply contact cleaner
- Test with battery meter, replace amplifier if failing

**Equipment failure in field:**

**Portable radio stops transmitting:**
1. Check battery (low battery inhibits TX on many radios—LED indicator dims)
2. Power cycle (off 10 seconds, on)
3. Reset to factory settings (may have accidental lockout enabled)
4. Test with alternate battery (verify battery is failure, not radio)

**Fallback options:** Switch to CW (requires only oscillator + keyer, more robust than voice), reduce power if still receives (may reduce power generation if solar/cranked), establish scheduled contact times (reduces need for long monitoring periods).

:::tip
**Preventive maintenance:** Check connections monthly. Clean antenna connectors/feedpoint. Test antenna resonance (SWR). Replace batteries on schedule (don't wait for field failure).
:::

</section>

<section id="maintenance">

## Maintenance & Care

### Radio Antenna Maintenance

**Dipole/Monopole:**
- Inspect for corrosion (white/green oxidation on copper)
- Clean contacts with fine brush + isopropyl alcohol
- Check solder joints for cold joints (dull finish—reheat to shiny)
- Verify tightness of all mechanical connections (wind/ice causes loosening)
- Apply dielectric grease to exposed connections (prevents water/corrosion)

**Coaxial feedline:**
- Inspect outer jacket for cracks (water intrusion causes shorts)
- Check connector for corrosion (white deposits inside N-connector indicates oxidation)
- Measure DC resistance end-to-end (should be <0.5 ohms for 50-100 feet cable)
- Replace if cable damaged (using corroded or damaged cable ruins SWR, causes signal loss)

**Testing antenna resonance:** Use antenna analyzer (portable, $100-300) or SWR meter. Measure SWR at design frequency—should be <1.3:1. If drifted, slightly shorten antenna (1% trim ≈ 1% frequency increase) or use antenna tuner.

### Transceiver Care

**Cooling:** Ensure ventilation (don't cover vent holes). 100W radio dissipates 10-15W heat at normal use—requires air circulation. In field use, ensure adequate air gap behind radio.

**Power supply:** Use regulated supply (prevents voltage spikes). Verify correct polarity before connection (reverse polarity destroys radio). Install DC fuse/breaker (10A typical for 100W radio).

**Connector maintenance:** Every 6 months, disconnect power, remove battery, unplug microphone. Inspect all connectors for corrosion. Clean with isopropyl alcohol + fine brush. Reconnect firmly.

### Battery Management

**Lead-acid batteries:** Check water level monthly (if non-sealed). Charge every 3 months (prevents sulfation from deep discharge). Replace every 3-5 years (capacity degrades ~20% per year).

**Lithium (LiFePO4):** Much longer life (10 years typical). Avoid over-discharge <10% state-of-charge (limits remaining capacity). Store charged 50-80% if not using for months (prevents internal discharge).

**Battery sizing:** A 20Ah 12V battery = 240 Wh. At 100W average consumption = 2.4 hours operating time. For 8-hour portable operation, need 100Ah (960 Wh) battery—impractical. Realistic portable comms: 10-20 hour battery (solar recharge daily), or 2-4 hour portable ops then recharge.

### Grounding & Lightning Protection

**Station grounding:** Copper rod 8-10 feet deep, bonded with #4 copper wire to all equipment. Prevents RF ground loops (causes hum) and provides lightning protection path.

**Antenna grounding:** Separate ground rod for antenna (prevents large currents flowing through house). Connect antenna system ground to station ground via low-inductance strap (wide copper, short path).

**Lightning considerations:** Antenna systems are lightning attractors. During thunderstorms, disconnect antenna feedline from radio (at feedthrough panel outside building). This breaks RF path but prevents lightning transient entering radio. Critical in areas with frequent lightning.

:::warning
Direct lightning strike on antenna can vaporize coaxial cable, destroy equipment, and potentially cause fire. Proper grounding and disconnect-during-storms procedure is essential. Many hams have lost equipment to lightning—professional station design includes lightning arrestor (gas tube surge protector) at feedline entrance.
:::

</section>

<section id="reference-tables">

## Reference Tables & Specifications

### Telegraph Key Specifications

- Contact force: 50-150 grams
- Return time: <100 milliseconds
- Voltage rating: Any (low voltage)
- Current capacity: 1-5 amps

### Sounder Specifications

- Coil resistance: 2-20 ohms
- Operating voltage: 4-12V DC
- Current draw: 0.2-1 amp
- Click volume: 70-80 dB at 1 foot

### Microphone Specifications

- Carbon weight: 2-3 grams
- Operating voltage: 1-12V
- Current draw: 0.1-0.5 amp
- Output impedance: 50-500 ohms

### Earpiece Specifications

- Coil resistance: 50-500 ohms
- Operating voltage: 1-12V
- Current draw: 10-100 mA
- Frequency response: 200-3000 Hz

### Wire Performance Summary

| Wire Type | Gauge | Resistance per 1000 ft | 1-Mile Range at 6V | Cost Factor | Best Use |
|-----------|-------|----------------------|-------------------|------------|----------|
| Copper | #10 | 1.0 ohm | Excellent (10+ mi) | High | Long trunk lines |
| Copper | #12 | 1.6 ohm | Excellent (8-10 mi) | Medium | Standard installation |
| Copper | #14 | 2.5 ohm | Good (5-8 mi) | Medium | Short to medium |
| Steel | #10 | 4 ohm | Good (5 mi at 12V) | Very Low | Budget short range |
| Steel | #12 | 6 ohm | Fair (2-3 mi at 6V) | Very Low | Temporary lines only |

</section>

<section id="summary">

## Summary

Telecommunications systems span three essential technologies for survival and community coordination:

**1. Telegraph (Wired, DC):** Simplest long-range communication. Requires only 6-12V battery, key, sounder, and copper wire. Achieves 10-100 miles per station. Perfect for locations with existing wire infrastructure. Morse code ensures reliable message transmission despite noise.

**2. Telephone (Wired, AC/DC):** Voice communication over same wire infrastructure as telegraph. Range 1-10 miles direct, extendable via amplifiers and switchboards. Switchboard design enables community-wide calling networks. Manual switching operators allow local coordination without external power (magneto-powered ringing).

**3. Radio (Wireless RF):** No physical infrastructure required. HF enables worldwide skip propagation; VHF/UHF enables local line-of-sight repeater networks. Requires licensing in normal times, but provides most resilient long-distance communication when other infrastructure fails.

**In post-collapse scenarios, redundancy is survival.** A community should develop capability in all three modes:
- Telegraph: Permanent wire lines for essential services (medical, security)
- Telephone: Community-wide voice communication on same wire network
- Radio: Emergency backup for distance communication when wires fail

**Key principles across all systems:**
- **Height matters more than power** for antenna and wire systems
- **Maintenance prevents failures**—regular inspection and cleaning extend useful life decades
- **Ground quality is critical**—proper grounding improves range, prevents noise, provides safety
- **Simplicity improves resilience**—hand-cranked generators power telegraph/telephone; CW transmission requires minimal equipment and power
- **Training ensures operation**—operators must understand Morse code, basic troubleshooting, and safety procedures

Mastering these three technologies ensures survival-level communication resilience against all scenarios.

</section>

:::affiliate
**If you're preparing in advance,** assemble comprehensive telecommunications equipment for establishing resilient multi-technology communication systems:

- [Icom IC-705 All-Mode Portable Transceiver](https://www.amazon.com/dp/B08CXNR569?tag=offlinecompen-20) — Dual-mode portable radio covering HF/VHF/UHF for maximum frequency coverage and technology redundancy
- [UGREEN 5-Port Gigabit Ethernet Switch](https://www.amazon.com/dp/B0D9JBTBZB?tag=offlinecompen-20) — Network infrastructure for data telecommunications and mesh network deployment
- [EVISTR 64GB Digital Voice Recorder](https://www.amazon.com/dp/B07CPNR79C?tag=offlinecompen-20) — Recording device for documenting telecommunications training and archiving critical communications

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</output>
