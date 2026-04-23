---
id: GD-852
slug: library-organization-preservation
title: Library Organization, Cataloging & Preservation Systems
category: culture-knowledge
difficulty: intermediate
tags:
  - rebuild
  - governance
  - knowledge
  - library
  - archival
  - organization
  - document-preservation
icon: 📚
description: Comprehensive guide to organizing community libraries using Dewey Decimal, Library of Congress, and simplified classification systems. Covers cataloging methods, card catalog construction, physical organization, book repair, preservation standards, archival practices, and practical implementation for communities of all sizes.
related:
  - archival-records
  - bookbinding-printing
  - data-information-salvage
  - education-system-design
  - education-teaching
  - governance
  - history-preservation-methodology
  - mathematics-measurement-systems
  - papermaking
  - written-language-record-keeping
read_time: 16
word_count: 7400
last_updated: '2026-02-24'
version: '3.0'
custom_css: |
  body.dark-mode{--bg:#0d0d1a;--surface:#0f1219;--card:#050810;--text:#d0d0d0}
  section p{margin-bottom:15px;text-align:justify}
  section ul,section ol{margin-left:25px;margin-bottom:15px}
  section li{margin-bottom:8px}
  .field-notes h4{color:var(--accent2);margin-bottom:10px}
  svg{max-width:100%;height:auto;margin:20px 0;display:block}
  .cross-references{background:var(--card);border-left:4px solid var(--accent);border-radius:4px;padding:20px;margin:25px 0}
  .cross-references h4{color:var(--accent);margin-bottom:15px}
  .cross-references ul{list-style:none;margin-left:0}
  .cross-references li{margin-bottom:8px}
  .cross-references a{color:var(--accent2);text-decoration:none;border-bottom:1px dashed var(--accent2);transition:all .3s}
  .cross-references a:hover{color:var(--accent);border-bottom-color:var(--accent)}
  @media(max-width:1024px){.toc-nav{width:100%;display:grid;grid-template-columns:repeat(2,1fr);gap:20px}.toc-nav h2{grid-column:1 / -1}}
liability_level: low
---
<section id="overview">

## Overview

In a post-collapse world, libraries become the vital repositories of human knowledge. Libraries preserve technical manuals, medical references, agricultural guides, and the cultural heritage that defines civilization. A well-organized library system enables communities to access critical information rapidly, support education, and prevent the loss of essential knowledge.

Before digital systems, libraries were humanity's primary knowledge repositories. They served critical functions that become even more important when infrastructure fails: providing instant access to medical, construction, and survival information when grid electricity disappears; supporting self-education and youth development; maintaining cultural continuity; and preserving technical knowledge across generations.

This guide covers the core principles of library science adapted for offline communities: classification systems, physical organization, cataloging methods, preservation techniques, and management practices that can be maintained without electricity or digital infrastructure.

![Library Organization and Classification Systems](../assets/svgs/library-organization-preservation-1.svg)

![Library Cataloging and Preservation Methods](../assets/svgs/library-organization-preservation-2.svg)

### Purpose of This Guide

Learn to:

-   Organize large book collections using proven systems (Dewey, Library of Congress, or simplified approaches)
-   Create searchable catalogs without computers using card systems and indexes
-   Physically organize materials for efficient retrieval and browsing
-   Preserve materials for decades using simple techniques and proper environmental controls
-   Manage lending systems that prevent loss and theft
-   Design library spaces for community learning
-   Preserve and transmit oral knowledge alongside written records

</section>

<section id="why-libraries">

## 1. Why Libraries Matter in Post-Collapse Communities

### Knowledge Access During Crisis

When grid electricity disappears, online databases become inaccessible. Communities need instant access to medical references, food preservation techniques, construction methods, and mechanical repair manuals. A well-organized library ensures critical information is available when needed most.

### Education and Youth Development

Young people growing up without formal schooling require access to educational materials. Libraries support self-education, apprenticeship programs, and skill transmission. Books provide consistent, high-quality learning resources independent of individual teachers' availability.

### Cultural Continuity

Libraries preserve art, literature, history, and philosophy. These materials maintain cultural identity and prevent psychological collapse. Reading provides mental health benefits during crisis periods and connects communities to the broader human experience.

### Technical Knowledge Preservation

Specialized knowledge—engineering, medicine, agriculture, metallurgy—requires comprehensive written documentation. Libraries prevent the loss of centuries of technological development and ensure advanced skills remain transmissible across generations.

:::tip
The most valuable library materials are technical references (medical texts, engineering manuals, agricultural guides), followed by educational materials, then cultural works. Prioritize acquiring these categories during the transition period.
:::

</section>

<section id="classification-systems">

## 2. Classification Systems: Dewey, LC & Post-Collapse Simplified

A good classification system enables patrons to find information quickly and predictably. Three main approaches serve different community sizes and expertise levels.

### Hierarchical Organization Principle

The most efficient classification organizes knowledge hierarchically, from general to specific. This enables both systematic browsing and quick location of materials.

**Example: Agricultural knowledge hierarchical structure**

```
AGRICULTURE (broad category)
├─ Grain crops
│  ├─ Wheat
│  ├─ Barley
│  └─ Rye
├─ Vegetables
│  ├─ Root vegetables
│  ├─ Leafy vegetables
│  └─ Legumes
├─ Fruit trees
└─ Soil management
```

A person looking for wheat growing information navigates: Agriculture → Grain crops → Wheat, then finds specific subsections on planting, spacing, and disease resistance.

### Dewey Decimal System (DDC)

The Dewey Decimal System is the most widely adopted library classification method worldwide. While the complete system contains thousands of subdivisions, this simplified version provides enough granularity for community libraries while remaining manageable by hand.

**Basic Structure:** The system divides all knowledge into 10 main classes (000-900), each subdivided into 10 divisions, each subdivided into 10 sections. A three-digit number identifies the subject; decimal places allow further specificity.

<table><thead><tr><th scope="col">Range</th><th scope="col">Category</th><th scope="col">Examples</th></tr></thead><tbody><tr><td>000-099</td><td>Computer Science, Information & General Works</td><td>Bibliography, General Knowledge, Information Science</td></tr><tr><td>100-199</td><td>Philosophy & Psychology</td><td>Ethics, Logic, Psychology, Self-help</td></tr><tr><td>200-299</td><td>Religion & Mythology</td><td>Religious texts, Church history, Mythology</td></tr><tr><td>300-399</td><td>Social Sciences</td><td>Government, Law, Economics, Sociology, Education</td></tr><tr><td>400-499</td><td>Language</td><td>Dictionaries, Grammar, Writing guides</td></tr><tr><td>500-599</td><td>Science & Mathematics</td><td>Chemistry, Physics, Biology, Medicine, Geology</td></tr><tr><td>600-699</td><td>Technology & Applied Sciences</td><td>Engineering, Construction, Agriculture, Cooking</td></tr><tr><td>700-799</td><td>Arts & Recreation</td><td>Architecture, Music, Painting, Photography, Sports</td></tr><tr><td>800-899</td><td>Literature</td><td>Fiction, Poetry, Drama, Essays</td></tr><tr><td>900-999</td><td>History, Geography & Biography</td><td>History, Geography, Travel, Biography</td></tr></tbody></table>

**Call Numbers for Community Libraries:** Simplified format: XXX (three-digit Dewey class) + Author Surname Abbreviation
- 621.3 ENG (Engineering book by author with surname starting 'ENG')
- 796.33 GRA (Sports book by author starting 'GRA')
- 615.5 LAR (Medical reference by author starting 'LAR')

**Critical Subjects for Survival Communities:**

-   **615 - Pharmacology & Medicine:** Essential. Medical reference texts, nursing guides, pharmaceutical information.
-   **621-629 - Applied Physics & Engineering:** Critical. Mechanical repair, construction, water systems, power generation.
-   **631-639 - Agriculture & Animal Husbandry:** Essential. Crop production, food preservation, veterinary care.
-   **641 - Food & Drink:** Essential. Cooking, food preservation, nutrition, food safety.
-   **682-684 - Woodworking & Metalworking:** High value. Smithing, carpentry, tool making.
-   **701-799 - Arts & Architecture:** Important for structural knowledge and morale.
-   **398 - Folklore & Mythology:** Preserve cultural knowledge and traditional practices.

:::warning
Don't arbitrarily create new classification numbers. Stick to standard Dewey—this ensures any trained librarian can navigate your system and makes knowledge transferable to other communities.
:::

### Library of Congress Classification (LC)

The Library of Congress system uses letters and numbers to classify materials. While more complex than Dewey, it's particularly useful for large specialized collections and is widely used in academic and research libraries.

<table><thead><tr><th scope="col">Code</th><th scope="col">Category</th><th scope="col">Applications</th></tr></thead><tbody><tr><td>R</td><td>Medicine</td><td>Medical texts, anatomy, nursing, pharmacology</td></tr><tr><td>T</td><td>Technology</td><td>Engineering, construction, manufacturing, utilities</td></tr><tr><td>S</td><td>Agriculture</td><td>Farming, forestry, animal husbandry, food science</td></tr><tr><td>Q</td><td>Science</td><td>Physics, chemistry, biology, geology, mathematics</td></tr><tr><td>K</td><td>Law</td><td>Legal systems, regulations, governance</td></tr><tr><td>J</td><td>Political Science</td><td>Government, politics, public administration</td></tr><tr><td>H</td><td>Social Sciences</td><td>Economics, sociology, education, psychology</td></tr><tr><td>P</td><td>Language & Literature</td><td>Dictionaries, fiction, poetry, drama</td></tr><tr><td>G</td><td>Geography & Anthropology</td><td>Maps, travel guides, cultural studies</td></tr><tr><td>E-F</td><td>History of Americas</td><td>Historical texts, biographies, regional history</td></tr></tbody></table>

### Simplified Classification for Post-Collapse Communities

For communities lacking library experience, a simplified system balances comprehensiveness with practicality:

```
KNOWLEDGE CLASSIFICATION SYSTEM

A. SURVIVAL & SUBSISTENCE
   A1: Food acquisition
   A2: Shelter & construction
   A3: Water & hygiene
   A4: Fire & fuel

B. AGRICULTURE & FOOD
   B1: Crop growing
   B2: Livestock
   B3: Food preservation
   B4: Fermentation & brewing

C. MEDICINE & HEALTH
   C1: Herbs & medicinal plants
   C2: Injuries & illness
   C3: Sanitation & disease prevention
   C4: Childbirth & infant care

D. CRAFT & TECHNOLOGY
   D1: Metalworking
   D2: Woodworking
   D3: Textiles & clothing
   D4: Ceramics & pottery
   D5: Tool-making

E. KNOWLEDGE & SKILLS
   E1: Mathematics & measurement
   E2: Writing & record-keeping
   E3: Calendar & timekeeping
   E4: Navigation

F. GOVERNANCE & ETHICS
   F1: Law & justice
   F2: Community organization
   F3: Dispute resolution
   F4: History & genealogy

G. ART & CULTURE
   G1: Music & instruments
   G2: Visual arts
   G3: Stories & literature
   G4: Ceremony & ritual

H. REFERENCE & ADMINISTRATION
   H1: Catalogs & indexes
   H2: Maps & diagrams
   H3: Numerical data
   H4: Records & documents
```

### When to Use Each System

**Use Dewey for:** Small to medium community libraries (under 5,000 books), mixed collections, ease of use by untrained staff.

**Use LC for:** Large collections (10,000+ books), specialized libraries (medical, technical), integration with existing research library systems.

**Use Simplified for:** Communities without library training, newly established libraries, highly localized collections.

:::tip
For most survival communities, Dewey Decimal is simpler to implement and maintain. Only adopt LC if you're preserving a large institutional library or integrating with existing LC-classified collections. The simplified system works best for completely new libraries with limited staff training.
:::

</section>

<section id="card-catalogs">

## 3. Card Catalogs: Physical Design & Standards

Card catalogs enable users to search library collections without computers. A well-designed manual card system allows rapid searching by title, author, and subject while maintaining cross-references to related materials.

### Card Types and Formats

Standard library cards measure 3" x 5" (76mm x 127mm)—large enough for legible handwriting, small enough to minimize storage space. Use acid-free cards if possible.

<table><thead><tr><th scope="col">Card Type</th><th scope="col">Purpose</th><th scope="col">Organization</th></tr></thead><tbody><tr><td>Author Card</td><td>Primary record entry by author surname</td><td>Alphabetical by last name, then first name</td></tr><tr><td>Title Card</td><td>Searchable by book title</td><td>Alphabetical by first significant word (skip "A", "The", "An")</td></tr><tr><td>Subject Card</td><td>Searchable by topic/subject matter</td><td>Alphabetical by standardized subject headings</td></tr><tr><td>Cross-reference Card</td><td>Indicates related subjects</td><td>Points to other card locations</td></tr></tbody></table>

### Catalog Card Content

Cards should contain: call number (top left, large and visible), author (last name first), complete title, publication information, subject headings, edition details, and any special features. Use black ink pen (fountain pen works well) and write legibly in capital letters for call numbers and title case for other information.

### Card File Organization

Maintain three separate card files: Author File, Title File, and Subject File. Each file is alphabetically sorted in wood or cardboard card drawers with dividers marking alphabetical sections (A-B, C-D, etc.). This provides three search pathways for users.

### Cross-Referencing and Related Materials

Materials on similar topics are shelved near each other and cross-referenced in catalogs.

**Example: A book on vegetable preservation**

- **Primary location**: Food Preservation (category 641.5)
- **Cross-references**: Agriculture/Vegetables (631.5), Root vegetables (631.4), Fermentation (641.4)

A patron looking for fermentation techniques finds the main reference under its primary location but the catalog also notes related information in vegetables sections.

:::note
#### Field Notes: Card Catalog Maintenance

Establish monthly maintenance routine: Check for damaged cards, verify call numbers match actual book locations, update circulation status. When acquiring a new book, write one author card, one title card, and 2-3 subject cards the same day. Keep blank cards and ink pens near the catalog. Assign this task to a dedicated person or small team—consistency matters. Use carbon paper to create multiple card copies simultaneously for accuracy and time savings.
:::

:::tip
If handwriting multiple copies, use carbon paper to create author and title cards simultaneously. This saves time and ensures consistency between card copies. For very frequently consulted materials, consider creating multiple copies of subject cards to distribute across locations.
:::

</section>

<section id="cataloging-metadata">

## 4. Cataloging & Metadata

### Creating a Complete Catalog Entry

A catalog entry records essential information about a work, allowing retrieval and location. Standard elements should include:

1. **Call number**: The identifier for locating the item (e.g., 635.9 GRE for gardening)
2. **Title**: Full title as it appears on title page
3. **Author/Creator**: Who wrote or created the work (with dates if known)
4. **Publication information**: Place, publisher, year (City : Publisher, YYYY)
5. **Physical description**: Format (book, manuscript), page count/length, size, illustrations present
6. **Subject headings**: Key topics covered (use standardized vocabulary)
7. **Abstract/Summary**: Brief description of content and scope
8. **Location**: Shelf location within the library
9. **Condition notes**: Any damage or preservation concerns requiring special handling
10. **Access restrictions**: If any (fragile materials, reference-only, etc.)

**Example catalog entry:**

```
CALL NUMBER: 635.9 WIL
TITLE: Medicinal Herbs and Plant Identification in Valley Regions
AUTHOR: Sarah Wilson, Year 4
PUBLICATION: Handwritten manuscript, Year 4, Month 7
PHYSICAL DESCRIPTION: Bound manuscript, 68 pages, leather covers
SUBJECT HEADINGS: Medicinal plants—Identification; Herbal medicine; Plant cultivation
ABSTRACT: Detailed guide to medicinal herbs suitable for local climate, including identification characteristics, harvesting times, preparation methods, and traditional uses. Includes hand-drawn illustrations of plant growth stages and leaf patterns.
LOCATION: Medicine shelf, Section C1, position 4
CONDITION: Good; some water staining on back pages but content fully legible
ACCESS NOTES: Valuable resource; not recommended for circulation. In-library use only.
```

### Master Catalog and Cross-Indexes

The master catalog lists all items in the library sequentially. Separate indexes allow retrieval by author, subject, or keyword.

**Master catalog format:** Simple sequential listing with entry numbers corresponding to items.

**Author index:** Alphabetical by author surname with entries referencing master catalog numbers.

**Subject index:** Alphabetical by standardized subject headings with entries referencing related materials by call number.

For communities with advanced literacy, add keyword indexes enabling searching without knowing exact titles or authors. A person seeking information on fermentation looks up that keyword and finds all related works across the collection.

:::info-box
**Record-Keeping Benefits:** Detailed records accomplish multiple goals: enable tracking of lost/overdue materials, provide data for collection development decisions, establish usage patterns, document preservation work, and create institutional memory. Spend 15 minutes daily on record updates rather than waiting to do it all monthly—this keeps records current and prevents backlogs.
:::

</section>

<section id="book-repair">

## 5. Book Repair & Maintenance

Books are fragile. Improper handling and storage cause rapid deterioration. Systematic preservation extends book life from decades to centuries and prevents loss of knowledge.

### Common Damage Types and Prevention

<table><thead><tr><th scope="col">Damage Type</th><th scope="col">Cause</th><th scope="col">Prevention</th><th scope="col">Repair</th></tr></thead><tbody><tr><td>Spine Breaking</td><td>Forcing book fully open, dropping</td><td>Handle gently, don't over-flex. Support open books.</td><td>Cloth tape over spine on inside; rebind if severe</td></tr><tr><td>Water Damage</td><td>Flooding, moisture, condensation</td><td>Store in dry location (below 60% humidity). Use desiccants.</td><td>Air dry flat (2-4 weeks). Press between newsprint to flatten.</td></tr><tr><td>Mold & Mildew</td><td>High humidity, poor ventilation</td><td>Maintain 30-50% humidity. Allow air circulation. Remove moldy books.</td><td>Brush dry, air in sunlight, treat with boric acid solution</td></tr><tr><td>Torn Pages</td><td>Rough handling, age-brittleness</td><td>Handle with clean hands. Support pages when reading.</td><td>Japanese tissue repair: glue acid-free tissue patch behind tear</td></tr><tr><td>Loose Binding</td><td>Age, heavy use, moisture</td><td>Avoid bending, support spine during reading</td><td>Rebind or reinforce with book tape and cloth</td></tr><tr><td>Foxing (Stains)</td><td>Iron oxidation in paper, mold spores</td><td>Store in stable humidity and temperature. Handle with clean hands.</td><td>Cannot be reversed; prevent spread by maintaining dry conditions</td></tr></tbody></table>

### Basic Repair Supplies

Maintain a book repair kit with these essentials:

-   **Acid-free book tape:** For reinforcing torn spines and page edges
-   **Japanese tissue:** Thin, strong paper for page repairs
-   **PVA adhesive:** pH-neutral glue (not white glue, which is acidic)
-   **Bone folder:** Smooth tool for creasing and folding
-   **Book press or weights:** Heavy objects for flattening pages
-   **Acid-free cardboard:** For backing and reinforcement
-   **Cotton cloth:** For reinforcement strips
-   **Scalpel or craft knife:** For precise cutting

### Tear Repair Procedure

Materials needed: Japanese tissue, PVA glue, bone folder, heavy weight

1.  Clean area around tear with soft brush to remove dirt
2.  Cut Japanese tissue slightly larger than tear (1-2cm on all sides)
3.  Apply thin, even coat of PVA glue to repair tissue
4.  Place tissue over tear from inside of page (if possible)
5.  Use bone folder to smooth out air bubbles
6.  Gently close book and weight it down for 24 hours
7.  Check repair after drying; trim excess tissue if needed

### Spine Reinforcement

For books with deteriorating spines:

1.  Open book to inside cover
2.  Apply 2-3cm width book tape along entire length of spine (inside)
3.  Use bone folder to press tape firmly to prevent bubbles
4.  If external spine is damaged, reinforce from inside with cloth tape first
5.  Allow adhesive to set fully before circulation

:::warning
Don't attempt major rebinding unless trained. Poor rebinding can cause more damage than the original problem. For severely damaged books, consult with experienced conservators or preserve as reference-only materials. Create a "Repair Log" documenting what books have been repaired, the work done, and when. This prevents redundant work and tracks material condition over time.
:::

</section>

<section id="archival-storage">

## 6. Archival Storage Environment

Environmental conditions dramatically impact book longevity. Books stored in optimal conditions can last centuries; books in poor conditions deteriorate in decades. This section covers archival storage principles critical to knowledge preservation.

### Ideal Storage Environment

<table><thead><tr><th scope="col">Factor</th><th scope="col">Ideal Range</th><th scope="col">Acceptable Range</th><th scope="col">Consequences of Poor Conditions</th></tr></thead><tbody><tr><td>Temperature</td><td>60-70°F (15-21°C)</td><td>55-75°F (13-24°C)</td><td>Fluctuations cause expansion/contraction; heat accelerates deterioration</td></tr><tr><td>Relative Humidity</td><td>30-40%</td><td>25-50%</td><td>Too dry: embrittlement; Too wet: mold, warping, ink bleeding</td></tr><tr><td>Light Exposure</td><td>Darkness</td><td>&lt;50 lux, no UV</td><td>Fading, photo-degradation of paper and inks</td></tr><tr><td>Air Quality</td><td>Dust-free, non-polluted</td><td>Filtered air</td><td>Dust deposits; Pollutants cause chemical deterioration</td></tr></tbody></table>

### Storage Space Design

**Ideal locations:** Interior basement (below grade, naturally cool), interior closets (stable temperature), or specially constructed storage room.

**Avoid:** Attics (temperature/humidity extremes), basements prone to flooding, spaces near HVAC vents (air currents), exterior walls (temperature swings).

### Shelving and Book Arrangement

-   **Shelving material:** Acid-free wood (maple, oak) or steel shelving. Avoid particle board (off-gasses chemicals).
-   **Shelf arrangement:** Store books upright when possible (prevents spine damage). Rare/valuable books may be stored flat or at slight angle.
-   **Book placement:** Don't pack shelves too tightly; allow air circulation. Leave 5-10cm space behind books on back wall.
-   **Weight distribution:** Place heavier books on lower shelves to prevent structural strain.
-   **Support:** Use bookends to prevent leaning and warping.

### Acid-Free Storage Materials

Paper becomes brittle when exposed to acid. All materials in contact with books should be acid-free:

-   **Shelving:** Unfinished wood, not plywood or particle board
-   **Boxes:** Acid-free archival cardboard (not regular cardboard)
-   **Wrapping:** Acid-free tissue or muslin
-   **Padding:** Acid-free polyester batting or wool felt
-   **Dividers:** Acid-free cardboard or plastic
-   **Adhesives:** PVA (polyvinyl acetate), avoid rubber cement or traditional pastes

:::note
#### Field Notes: DIY Humidity Control

Ideal storage has stable humidity 30-40%. If space humidity fluctuates: Use passive desiccants (silica gel, calcium chloride) during humid months. Replace when saturated. Maintain air circulation with small fan. Monitor with inexpensive humidity meter ($5-10). In dry climates, reverse process: Add moisture with damp sponges in sealed containers (monitor carefully to prevent mold).
:::

### Pest and Mold Prevention

**Pests:** Silverfish, bookworms, and roaches consume paper. Prevention:

-   Maintain storage at 50°F or below (slows insect reproduction)
-   Keep humidity at 40% or below (unfavorable to pests)
-   Inspect new acquisitions for signs of infestation before shelving
-   Quarantine infested books separately
-   Use diatomaceous earth (food-grade) around storage area perimeter

**Mold:** Grows above 50% humidity. Prevention:

-   Maintain humidity 30-40%
-   Allow air circulation (don't block air vents)
-   Avoid storing books in damp basements without humidity control
-   Remove any books showing mold signs immediately to prevent spread

:::warning
If mold is already present in stored books, isolate them immediately. Mold spores spread rapidly to adjacent materials. Treat moldy books outdoors with careful brushing and sunlight exposure before returning to storage. The most common and costly mistake: Not maintaining proper storage conditions (temperature/humidity). This single factor causes more book deterioration than all other factors combined. Monitor this obsessively.
:::

</section>

<section id="historical-systems">

## 7. Historical Library Systems

Understanding how libraries functioned historically provides perspective on what systems work when technology is limited. Ancient and medieval libraries developed sophisticated approaches to knowledge organization.

### Medieval Guild Libraries and Cross-Cultural Systems

Medieval monastery and guild libraries developed classification systems reflecting their knowledge domains. Islamic Golden Age libraries in Baghdad, Damascus, and Cairo created sophisticated catalogs with cross-referencing systems enabling scholars to locate materials across vast collections. These historical libraries demonstrate that without electricity, communities successfully organized thousands of materials using hierarchical classification and careful record-keeping.

**Key historical principles still applicable:**

- **Standardization:** Libraries in different locations used compatible systems enabling knowledge exchange
- **Redundancy:** Critical texts were copied and stored in multiple locations as backup
- **Cross-referencing:** Subject cards and indexes connected related materials despite physical separation
- **Apprenticeship:** Knowledge of library systems was transmitted through practical training
- **Community involvement:** Libraries functioned as community institutions with public input on acquisitions

These practices remain relevant for post-collapse communities rebuilding libraries. Regional library networks using standardized classification systems can coordinate acquisitions and share catalogs, just as medieval libraries traded copies of rare materials.

### Making Copies of Critical Texts

For the most important works, create backup copies using methods proven historically:

1. **Handwritten copies**: Scribes carefully copy important texts by hand. This is labor-intensive but creates a redundant copy that's immediately available if the original is damaged.

2. **Carved copies**: For diagrams, important passages, or symbolic texts, carve into stone or hardwood. This creates a permanent, damage-resistant copy.

3. **Memorized copies**: Have community scholars memorize critical texts. If the physical copies are lost, the knowledge survives in human memory.

</section>

<section id="practical-implementation">

## 8. Practical Implementation for Different Community Scales

Implementing a library requires different approaches depending on community size and available resources. This section provides realistic guidance for communities of varying sizes.

### Small Community Library (50-200 people, 300-1000 books)

**Infrastructure:**
- Designate simple storage space (room, large building section, or weatherproof structure)
- Build or acquire basic wooden shelving (acid-free wood preferred)
- Establish climate control: passive humidity management with desiccants, simple temperature monitoring
- Create simple wooden or cardboard card drawers for three card catalogs

**Staffing:**
- 1 part-time librarian (10-15 hours/week)
- 2-3 rotating volunteers for circulation desk and shelving
- No specialized preservation staff (librarian handles repairs)

**Classification:**
- Simplified 8-category system (A-H as outlined above) or simplified Dewey
- Staff training limited to basics—stick to straightforward categorization
- Handwritten catalog cards with essential information only

**Acquisition:**
- Focus on survival essentials: medicine, agriculture, basic tools, educational materials
- Accept community donations but inspect for damage/mold before adding
- Prioritize multiple copies of critical references (medical, agricultural)

**Implementation timeline:**
- Week 1-2: Assess community needs, identify librarian, scout location
- Week 3-8: Create storage space, build shelving, make repair kit
- Week 9-12: Establish classification system, write procedures manual, train staff
- Week 13+: Acquire materials, create catalogs, open to community gradually

### Medium Community Library (200-1000 people, 1000-5000 books)

**Infrastructure:**
- Dedicated building with multiple rooms (circulation area, stacks, storage, reference section)
- Professional-grade shelving with labeled sections
- Humidity and temperature monitoring with basic climate control
- Separate storage for fragile/rare materials with tighter environmental control
- Multiple card catalog systems with proper card drawers

**Staffing:**
- 1 head librarian (full-time)
- 1 assistant librarian (part-time, 20-30 hours/week)
- 2-3 circulation desk staff (part-time, rotating shifts)
- Optional: Part-time conservation specialist for major repair work

**Classification:**
- Dewey Decimal System (simplified version)
- Comprehensive staff training on classification principles
- Standardized cataloging procedures with written guidelines
- Author, title, and subject cards with cross-references

**Acquisition:**
- Develop formal acquisition policy reviewed by library committee
- Balance between survival essentials and educational/cultural materials
- Maintain 2-3 copies of high-demand technical references
- Establish regular review schedule (quarterly) for collection assessment

**Programs:**
- Reading discussion groups
- Literacy classes for adult education
- Young people's reading program
- Apprenticeship support (library provides references for training)
- Research assistance for community projects

**Implementation timeline:**
- Month 1: Establish library committee, draft policies, identify staff
- Month 2-3: Renovate space, acquire shelving and supplies, train staff
- Month 4-5: Create comprehensive catalogs, establish procedures
- Month 6+: Open to public with limited hours, gradually expand services

### Large Community Library (1000+ people, 5000+ books)

**Infrastructure:**
- Multi-room facility with dedicated departments (circulation, reference, archives, restoration)
- Professional shelving systems with multiple locations and organized sections
- Precise environmental controls with temperature/humidity monitoring devices
- Separate archival storage for rare, fragile, or historically significant materials
- Reference section with quick-access materials
- Study areas and quiet reading spaces

**Staffing:**
- Director/Head Librarian (full-time)
- Head of Collections (part-time)
- Head of Services/Public Programming (part-time)
- Preservation/Conservation Specialist (part-time)
- 4-6 circulation and reference staff (mix of full and part-time)
- 1-2 support/facility staff

**Classification:**
- Full Dewey Decimal or Library of Congress system
- Comprehensive cataloging with authority control (standardized subject headings)
- Multiple indexes (author, title, subject, keyword)
- Detailed condition notes and preservation tracking

**Acquisition & Collections:**
- Formal acquisition policy with committee oversight
- Differentiated collections: reference (non-circulating), general circulating, specialized sections
- Multi-branch system possible: main library + branch libraries or satellite stations
- Budget for both materials and preservation supplies

**Programs & Services:**
- Literacy and education programs
- Research support and reference services
- Community reading and discussion groups
- Apprenticeship training support
- Oral history documentation project
- School support services
- Exhibition space for community materials

**Governance:**
- Library committee with 5-7 community representatives
- Monthly operational meetings, quarterly review meetings
- Annual community assessment and strategic planning
- Formal appeal process for collection disputes

**Implementation timeline:**
- Month 1-2: Establish governance committee and long-term plan
- Month 3-6: Build infrastructure, hire and train staff, develop procedures
- Month 7-10: Build core collection, establish comprehensive catalogs
- Month 11+: Open with initial services, gradually expand programs and hours

:::warning
**Critical Staffing Principle:** All procedures must be documented and at least 2 people must understand each critical function. Single-person knowledge creates vulnerability. If the only person who knows catalog procedures gets sick or leaves, the library becomes dysfunctional. Cross-train continuously.
:::

### Reference vs Circulating Collections

Different materials serve different purposes. Strategic separation prevents loss of critical materials:

**Reference Collection (Non-Circulating):**

**Purpose:** Materials consulted for specific information that must remain available for immediate access.

**Typical materials:**
- Medical reference texts (anatomy, pharmacology, emergency treatment)
- Technical manuals and engineering handbooks
- Dictionaries, encyclopedias, atlases
- Rare or unique books (single copy only)
- Government records and legal documents
- Maps and charts
- Indexes and bibliographies

**Circulating Collection (Borrowable):**

**Purpose:** Materials individuals take home for extended reading or study.

**Typical materials:**
- Novels, poetry, fiction
- Educational texts (if multiple copies available)
- Technical books with multiple copies
- Practical guides (cooking, gardening, handicrafts)
- History and biography
- Children's and young adult literature

:::warning
Never allow critical reference materials to circulate. A single medical reference that doesn't return during a crisis could cost lives. Keep at least one copy of essential materials in-library at all times. If space-constrained, expand storage or create satellite collection before losing knowledge.
:::

</section>

<section id="oral-knowledge">

## 9. Oral Knowledge Preservation

Libraries preserve written knowledge, but much human learning is oral tradition—stories, skills, songs, and practices passed verbally. Systematic preservation of oral knowledge complements written collections.

### Types of Oral Knowledge Worth Preserving

-   **Technical skills:** Descriptions of traditional techniques, tool use, material properties
-   **Medical knowledge:** Herbal remedies, treatment techniques, diagnostic methods
-   **Agricultural practices:** Planting calendars, soil management, seasonal knowledge specific to local conditions
-   **Cultural stories:** History, mythology, folklore that defines community identity
-   **Family histories:** Genealogy, ancestral stories, personal survival accounts
-   **Wisdom traditions:** Philosophy, spiritual practices, ethical teachings

### Documentation Methods

**Method 1: Transcription**

Record oral knowledge in written form through recorded interviews or conversations transcribed to permanent written record (handwritten or typed). Organize chronologically, thematically, or by speaker, store in archival folders with preservation standards, and index for searchability and cross-referencing.

**Method 2: Apprenticeship Recording**

Document skills through structured teaching documentation: Master craftsperson documents their knowledge while teaching apprentice. Create step-by-step written guides with illustrations. Record variation in technique, materials, and local adaptations. Preserve unusual or valuable techniques before they're lost.

**Method 3: Systematic Interview Archive**

Create formal oral history project:

1.  Identify knowledge-holders in community (elders, experienced craftspeople, traditional healers)
2.  Develop interview protocol with standardized questions
3.  Conduct taped or recorded interviews
4.  Transcribe and organize materials
5.  Index by subject, interviewee, and knowledge type
6.  Make available for reference and study

:::note
#### Field Notes: Oral History Project

Start small: Interview 3-5 community elders on topics critical to your area (food preservation, traditional medicine, local agriculture). Transcribe within 1 week while memory is fresh. File copies in multiple locations as backup. This preserves irreplaceable knowledge while honoring community elders. Over time, build archive of 50-100 interviews covering critical knowledge domains.
:::

:::tip
Pair oral history documentation with apprenticeship. When a master teaches an apprentice while documented, knowledge is both preserved AND actively transmitted, ensuring practical competence alongside written record.
:::

</section>

<section id="see-also">

## See Also

#### Related Guides in This Compendium

-   [Bookbinding & Printing Technology](bookbinding-printing.html) - Preserving written knowledge through physical production
-   [Education & Teaching Systems](education-teaching.html) - Using libraries in community learning
-   [Archival Records & Documentation](archival-records.html) - Preservation standards and techniques
-   [Papermaking & Basic Paper Production](papermaking.html) - Understanding paper degradation and preservation
-   [Community Governance & Decision-Making](governance.html) - Establishing library policies and community access
-   [Written Language & Record-Keeping](written-language-record-keeping.html) - Documentation systems
-   [History Preservation Methodology](history-preservation-methodology.html) - Recording and preserving local history

:::affiliate
**If you're preparing in advance,** these resources support library development and knowledge preservation:

- [A Practical Guide to Dewey Decimal Classification](https://www.amazon.com/dp/1538127199?tag=offlinecompen-20) — Systematic organization method for library collections
- [Preservation and Conservation for Libraries and Archives](https://www.amazon.com/dp/083891005X?tag=offlinecompen-20) — Long-term care and disaster recovery for knowledge preservation
- [Managing Library Collections: Selection, Organization and Development](https://www.amazon.com/dp/0838910556?tag=offlinecompen-20) — Building and curating knowledge collections for communities
- [The Librarian's Reference Guide to Cataloging and Classification](https://www.amazon.com/dp/083891020X?tag=offlinecompen-20) — Indexing and cataloging systems for knowledge retrieval
- [Lineco Museum Archival Storage Boxes](https://www.amazon.com/dp/B07WJ7M424?tag=offlinecompen-20) — Professional acid-free boxes for storing and organizing library materials and archival documents
- [Elan Waterproof Field Notebook 3-Pack](https://www.amazon.com/dp/B0BSXTC5Z2?tag=offlinecompen-20) — Durable journals for maintaining library cataloging records and classification documentation
- [Lineco Acid-Free Interleaving Tissue Paper](https://www.amazon.com/dp/B000KNPLLU?tag=offlinecompen-20) — Archival separators for protecting valuable library materials from deterioration and damage

<span class="affiliate-note">As an Amazon Associate we earn from qualifying purchases. These match the tools discussed in this guide — see the gear page for full pros/cons.</span>
:::

</section>
