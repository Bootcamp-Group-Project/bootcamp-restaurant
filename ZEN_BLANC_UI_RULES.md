# ZEN BLANC — UI/UX Hierarchy Rules for Claude
> Copy these rules into any new canteen/POS project prompt so Claude replicates the same design language.

---

## 1. DESIGN PHILOSOPHY

This system uses **Zen Blanc** — a luxury minimalist aesthetic inspired by a Michelin one-star brasserie. Every decision prioritises:

- **Restraint over decoration** — no gradients, no shadows, no rounded corners except 2–4 px
- **Typography as hierarchy** — two fonts only: serif for display, sans-serif for UI
- **Warmth through colour** — warm whites and champagne gold instead of cold greys and blue
- **Air and space** — generous padding, wide letter-spacing, thin hairlines as dividers
- **Bilingual labels** — Traditional Chinese as primary language, English in small caps as secondary labels (e.g. `MAIN COURSE`, `AVAILABLE`)

---

## 2. COLOUR TOKENS

Always define these as CSS variables. Never hardcode hex values inline.

```css
:root {
  --lux-bg:           #FAFAF8;   /* page background — warm off-white */
  --lux-surface:      #FFFFFF;   /* card / panel background */
  --lux-surface-dim:  #F2EFE9;   /* subtle inset / hover background */
  --lux-border:       #E8E4DE;   /* default border — warm light grey */
  --lux-border-hover: #C4A882;   /* border on hover — gold */
  --lux-ink:          #1A1A1A;   /* primary text / filled buttons */
  --lux-ink-soft:     #3A3428;   /* secondary text — warm dark brown */
  --lux-muted:        #7A7168;   /* placeholder / meta text */
  --lux-hint:         #B0A898;   /* disabled / hint text */
  --lux-gold:         #C4A882;   /* accent — champagne gold */
  --lux-gold-dark:    #8B7355;   /* gold for labels / links */
  --lux-error:        #9B5A5A;   /* error / destructive — muted red */
  --lux-success:      #3A6B4A;   /* available / positive — muted green */
  --lux-warn:         #8B6914;   /* warning — warm amber */
}
```

---

## 3. TYPOGRAPHY

### Fonts
| Role | Font | Import |
|------|------|--------|
| Display / Headings / Dish names | Cormorant Garamond | Google Fonts |
| Body / UI / Buttons / Labels | DM Sans | Google Fonts |

```html
<link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,300;0,400;0,500;0,600;1,300;1,400&family=DM+Sans:wght@300;400;500&display=swap" rel="stylesheet">
```

### Scale Rules
| Element | Font | Size | Weight | Letter-spacing | Case |
|---------|------|------|--------|----------------|------|
| Page / section title | Cormorant Garamond | 26 px | 400 | 0.02em | Normal |
| Dish / item name | Cormorant Garamond | 15 px | 400 | 0 | Normal |
| Brand name | DM Sans | 13 px | 400 | 3 px | UPPERCASE |
| Category label | DM Sans | 10 px | 400 | 2.5 px | UPPERCASE |
| Button text | DM Sans | 11 px | 400 | 2 px | UPPERCASE |
| Badge text | DM Sans | 10 px | 400 | 1.5 px | UPPERCASE |
| Price | DM Sans | 13 px | 500 | 0 | Normal |
| Meta / stock | DM Sans | 10 px | 400 | 0.5 px | Normal |

**Rule:** Never use font-weight above 500. Bold text destroys the luxury feel.

---

## 4. SPACING SYSTEM

Use multiples of 4 px.

| Token | Value | Use |
|-------|-------|-----|
| xs | 4 px | icon gap, tight inline spacing |
| sm | 8 px | inner card padding, gap between small elements |
| md | 12–16 px | standard gap, section padding |
| lg | 20–24 px | card padding, section spacing |
| xl | 32–40 px | between major sections |

**Rule:** When in doubt, add more space. Crowded layouts are never luxury.

---

## 5. COMPONENT RULES

### 5.1 Header / Navigation Bar
- Sticky top, `background: var(--lux-surface)`, `border-bottom: 1px solid var(--lux-border)`
- Left: brand name (UPPERCASE, 3 px letter-spacing) + subtitle (gold, 2 px letter-spacing)
- Decorative gold hairline flanks the brand name: `——  BRAND NAME  ——`
- Right: order window badge → role badge → user ID → sign out link
- No icons — text only

### 5.2 Menu Card
```
┌─────────────────────────┐
│   Square image (1:1)    │
├─────────────────────────┤
│  ── gold hairline ──    │
│  MAIN COURSE  (label)   │
│  Dish Name (serif)      │
│  [AVAILABLE]            │
│  $60  · 30 left         │
│                         │
│  ☐ Special option       │
│  ☐ Special option       │
│                         │
│  [    + ADD    ]        │
└─────────────────────────┘
```
- Border: `1px solid var(--lux-border)`, `border-radius: 4px`
- Hover: border changes to `var(--lux-gold)`
- Sold-out state: `opacity: 0.55`
- Image: square, `object-fit: cover`

### 5.3 Badges
Three types only:

| Badge | Border | Text colour | Use |
|-------|--------|-------------|-----|
| Open / Active | `--lux-gold` | `--lux-gold-dark` | Order window open, active state |
| Closed / Inactive | `--lux-border` | `--lux-hint` + `opacity:0.7` | Order window closed |
| Available | `--lux-success` | `--lux-success` | Dish in stock |
| Sold Out | `--lux-error` | `--lux-error` | Dish out of stock |
| Role | `--lux-border` | `--lux-muted` | STUDENT / TEACHER / ADMIN |

All badges: pill shape (`border-radius: 20px`), 10 px font, 1.5 px letter-spacing, UPPERCASE.

### 5.4 Buttons
Three variants only:

| Variant | Background | Border | Text | Use |
|---------|-----------|--------|------|-----|
| `lux-btn-ink` | `--lux-ink` | `--lux-ink` | `--lux-bg` | Primary CTA: Add, Submit, Save |
| `lux-btn-ghost` | transparent | `--lux-border` | `--lux-ink` | Secondary: View Orders, Reset |
| `lux-btn-gold` | transparent | `--lux-gold` | `--lux-gold-dark` | Tertiary: Admin actions |

Rules:
- `border-radius: 2px` (almost square)
- `font-size: 11px`, `letter-spacing: 2px`, `text-transform: uppercase`
- `transition: all 0.2s ease` on all buttons
- Never use colour fills other than `--lux-ink` for primary buttons

### 5.5 Order / Cart Panel (Sidebar)
- Fixed right-side panel, `background: var(--lux-surface)`, `border-left: 1px solid var(--lux-border)`
- Sections separated by hairlines
- Labels in small caps: `YOUR ORDER`, `SUBTOTAL`, `MY ORDERS TODAY`
- Two action buttons stacked: Primary (ink filled) on top, Secondary (ghost) below
- Empty state: plain muted text, no illustrations

### 5.6 Form Inputs
```css
.lux-input {
  border: 1px solid var(--lux-border);
  border-radius: 2px;
  padding: 7px 10px;
  font-family: 'DM Sans';
  font-size: 12px;
  color: var(--lux-ink);
  background: var(--lux-surface);
  outline: none;
}
.lux-input:focus {
  border-color: var(--lux-gold);
}
```

### 5.7 Gold Hairlines as Dividers
Never use thick borders or box shadows as dividers. Use:
- `lux-hairline` — full-width 1 px `var(--lux-border)` between sections
- `lux-hairline-gold-short` — 28 px wide gold line, left-aligned, under section titles
- `lux-hairline-gold-center` — 28 px wide gold line, centred, inside cards

---

## 6. PORTAL HIERARCHY

This system has three distinct portals. Each has its own visual weight.

### Portal 1 — Student / Teacher Ordering Page (`index.html`)
- Lightest, most spacious
- Menu grid layout (4 columns desktop, 2 mobile)
- Right-side cart panel always visible
- Primary action: `+ ADD` → `PREVIEW ORDER →` → `SUBMIT`
- No admin controls visible

### Portal 2 — Admin Panel (`admin.html`)
- Dark left sidebar navigation (`background: #1A1A1A`, text in gold and white)
- Main content area uses the same Zen Blanc tokens
- Table-based management (dishes, drinks, orders)
- Functional over decorative — tighter spacing than ordering page
- All destructive actions in `--lux-error` text, no filled red buttons

### Portal 3 — Kitchen Portal (`kitchen.html`)
- Most functional, least decorative
- Order cards with large status badges
- High-contrast for kitchen readability (ink on white)
- Actions: Mark Ready, Mark Picked Up

---

## 7. LANGUAGE / COPY RULES

| Context | Language | Example |
|---------|----------|---------|
| Section titles | Traditional Chinese | `精選主食` |
| Category labels | English small caps | `MAIN COURSE` |
| Button text | English | `+ ADD`, `PREVIEW ORDER →` |
| Status badges | English | `AVAILABLE`, `SOLD OUT` |
| Alert / error messages | Traditional Chinese | `請先登入才能下單` |
| Admin labels | Mixed (Chinese primary) | `儲存`, `刪除`, `訂購時段` |
| Price | `$` + number | `$60` |
| Stock | `· N left` | `· 30 left` |

---

## 8. INTERACTION RULES

- **No loading spinners** — use instant render with placeholder state
- **No animations** except `transition: all 0.2s ease` on hover states
- **No modals for simple actions** — use inline alerts (`alert()`) sparingly for errors only
- **Order preview** uses a centred modal overlay — the only modal in the system
- **Hover = border turns gold** — applies to cards, inputs, and ghost buttons
- **Disabled state = opacity 0.45–0.55** — never use grey fill

---

## 9. RULES CLAUDE MUST FOLLOW

When building any new canteen/POS with this system:

1. **Always import both fonts** — Cormorant Garamond + DM Sans. Never substitute.
2. **Always define all CSS variables** in `:root` before any component styles.
3. **Never use Tailwind utility classes for colour** — only CSS variables.
4. **Dish names and section titles use serif** — everything else uses sans-serif.
5. **Buttons are almost square** (`border-radius: 2px`) and uppercase.
6. **Gold is accent only** — never fill backgrounds with gold.
7. **Three button variants only** — ink (primary), ghost (secondary), gold (tertiary).
8. **Images are always square** (`aspect-ratio: 1/1`, `object-fit: cover`).
9. **Hairlines, not shadows** — `box-shadow` is forbidden for dividers.
10. **Generous whitespace** — if it feels too empty, it's probably right.
11. **Bilingual** — Chinese for user-facing copy, English for labels and buttons.
12. **Status is always a pill badge** — never coloured text alone or coloured backgrounds.

---

## 10. ANTI-PATTERNS — NEVER DO THESE

| ❌ Forbidden | ✅ Correct alternative |
|---|---|
| `box-shadow` for card depth | 1 px border + hover border-gold |
| `border-radius: 8px+` | `border-radius: 2–4px` |
| `font-weight: 700` (bold) | `font-weight: 400–500` only |
| Coloured button backgrounds (red, green, blue) | Ink, ghost, or gold variants only |
| Icon-only buttons | Text buttons (UPPERCASE labels) |
| Full-width modals for errors | `alert()` or inline error text |
| Gradient backgrounds | Flat `--lux-bg` (#FAFAF8) only |
| Cold grey palette | Warm beige/cream tones only |
| Sans-serif for dish names | Cormorant Garamond serif |
| `display: none` for sold-out items | Show with `opacity: 0.55` + disabled button |

---

*Generated from The Academy Canteen — Zen Blanc design system.*
