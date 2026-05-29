# GroceryVault — UX Review & Redesign Proposal

> Response to `docs/grocery_ux_review.md`. Written from a Senior Android/Product UX perspective.
> This is a **product & UX review**, not a coding plan.

---

## 0. Note on current code state (read this first)

The request lists Detail-screen capabilities that **are not present on this branch (`grocery-edit-fab`)**. Reviewing the actual code:

| Capability (per request) | Detail screen — actual | Edit screen — actual |
|---|---|---|
| Quick Add field + Add button | ✅ present | — (uses per-row fields) |
| Suggestions / autocomplete | ✅ present | ✅ present |
| New items at top | ✅ (sort: newest-unchecked first) | n/a |
| **Collapsible Purchased section** | ❌ **not present** (only a "Hide purchased" toggle + dimmed inline rows) | n/a |
| **Individual item removal** | ❌ **not present** | ✅ present (🗑 per row) |
| **Undo snackbar** | ❌ **not present** | ❌ not present |
| Hide purchased toggle | ✅ present | n/a |
| Progress bar | ✅ present | n/a |
| Uncheck all | ❌ **not present** | n/a |

So the review below evaluates the **intended** Detail design (the capability set you listed) and the **Edit screen as it actually stands**. Where the live code differs, it's called out.

---

## 1. UX Analysis

### 1A. Grocery Detail Screen

**Layout today (top → bottom):** top bar (`"Grocery List"` static + `x/y done`, Close, Edit) → progress bar → "Hide purchased" toggle → a single `LazyColumn` whose rows are: list title, date, description, and **one** `SectionCard` that contains the quick-add field + every item row in a non-virtualized `Column`.

**Strengths**
- **Quick-add is genuinely fast**: autocomplete + "enter to add" + auto-refocus = type-enter-type-enter. This is the best part of the app and should be the template for everything else.
- **Newest-unchecked-first** means a just-added item is immediately visible.
- **Progress feedback** (bar + `x/y done`) is clear and motivating while shopping.

**Weaknesses / friction**
- **One giant non-virtualized list item.** Every grocery row lives inside a single `LazyColumn { item { SectionCard { Column { … } } } }`. For 20–100 items, all rows compose at once → jank, memory, and the loss of lazy recycling. **This is the single biggest structural problem and it affects both screens.**
- **The quick-add field scrolls away.** Because it sits inside the scrolling card, once you scroll down to check off items, the fastest action on the screen is off-screen. Re-adding means scrolling back up.
- **Header metadata wastes the most valuable space.** The list title is shown *twice* (top bar says "Grocery List", body repeats the real title), plus a timestamp and description — all above the items, pushing groceries down on first paint.
- **Purchased items only dim.** Without grouping/collapse, a mid-shop list becomes a long faded tail. The "Hide purchased" toggle is all-or-nothing (you lose the satisfying "done" record entirely).
- **Only the checkbox toggles.** The row text is dead space; the tap target is just the 48dp checkbox.
- **No per-item remove** (yet the Edit screen has it) — inconsistent, and it forces a full-screen detour to fix a typo.

**Unnecessary taps / scrolling:** scroll-up-to-re-add; navigate-to-Edit to remove one item; toggle requires hitting the small checkbox.

### 1B. Edit Grocery List Screen

**Layout today:** `FormTopBar` (Close + Save) → `LazyColumn`: Title field → Notes field → one `SectionCard("Groceries")` containing all editable rows (checkbox + autocomplete name + 🗑) → **`Add item` button as the last row, at the very bottom.**

**Strengths**
- Familiar form; autocomplete on every row; per-row delete exists; you can set initial checked state.

**Weaknesses / friction**
- **"Add item" is at the bottom** (the request's core concern). On a 30-item list you scroll past everything to add one item — every time.
- **Multi-item entry is slow.** Tap "Add" → a blank row is appended → it is **not auto-focused** and the keyboard does **not** auto-open → tap the field → type → repeat. There is no "enter to add the next row." Compare to the Detail quick-add, which is dramatically faster.
- **The "scroll to new row" is effectively broken.** `animateScrollToItem(ui.groceryItems.lastIndex)` targets a *LazyColumn* index, but all grocery rows live inside a single lazy item (index 2). So the scroll target doesn't correspond to the new row, and `ui` is the pre-add snapshot anyway.
- **Same non-virtualization problem** as Detail — all rows + their autocomplete dropdowns compose at once.
- **Checkbox inside an edit form is ambiguous** — it mixes "edit structure" (names/order) with "shopping state" (purchased), which belongs on Detail.
- **No reordering**, despite Edit being the only logical place to set order.

**Unnecessary taps / scrolling:** scroll-to-bottom-to-add (repeated), extra tap to focus each new row, no enter-to-continue.

### 1C. The cross-cutting insight

The Detail screen already has the **best input model in the app** (sticky-feeling quick-add with autocomplete + enter). The Edit screen reinvents item entry with a slower, form-based model. **The two screens overlap heavily** — and if Detail gains remove + inline rename, the Edit screen's reason to exist shrinks to "title + notes." That's worth challenging (see §5).

---

## 2. Alternative Designs

### 2A. Edit screen — where should "Add item" live?

#### Option 1 — Floating Action Button (the branch's namesake)
- **Concept:** Drop the bottom button; add an **Extended FAB** ("＋ Add item") pinned bottom-end. Tap → insert a new row **at the top** and auto-focus it.
- **Advantages:** Always reachable at any scroll position; canonical Material pattern; thumb-friendly for one-handed use; reclaims inline space.
- **Disadvantages:** A FAB can overlap the last row (needs bottom content padding); a plain icon FAB is less explicit (use the *extended* variant); FABs usually denote a screen's primary action, but Save already owns the top bar — acceptable, but a minor semantic overlap.
- **Large lists:** Excellent — one tap to add regardless of length, *if* new rows insert at top + focus (so you never chase the bottom).
- **Implementation complexity:** **Low.** `Scaffold(floatingActionButton=…)`, bottom padding, change insert position + focus; remove the broken auto-scroll.
- **Android-pattern fit:** Strong — extremely common.

#### Option 2 — Sticky quick-add row (bring Detail's model to Edit) — *recommended*
- **Concept:** A pinned input bar (above the keyboard) with autocomplete + "enter to add", appending a row and clearing while keeping focus. The editable rows scroll above it. New rows insert at top.
- **Advantages:** **Fastest possible multi-item entry** (type-enter-type-enter); unifies the entry model with Detail; zero scrolling to add; keyboard stays up. Best tap efficiency of all options.
- **Disadvantages:** Two text-entry affordances coexist (pinned quick-add vs editable rows) — needs clear visual separation; requires IME-inset handling (already solved via `rememberAnimatedImeBottomPadding`).
- **Large lists:** Excellent for entry; rows still need virtualization for display.
- **Implementation complexity:** **Medium.** Reuse `SuggestionAutoCompleteField` + insert/clear/refocus logic that already works on Detail; ideally virtualize rows.
- **Android-pattern fit:** Strong — the messaging/checklist "pinned composer" (Google Keep, Todoist, AnyList).

#### Option 3 — Inline "always-present empty row"
- **Concept:** Render one trailing empty input; typing into it creates the item and spawns a new empty row beneath (spreadsheet/Keep style). No button, no FAB.
- **Advantages:** Zero chrome; very discoverable ("type here"); continuous entry.
- **Disadvantages:** Still anchored at the **bottom** → scrolling on long lists unless paired with auto-scroll/focus; a trailing empty row can read as "unfinished".
- **Large lists:** Mediocre to reach; fine once there.
- **Implementation complexity:** **Low–Medium.**
- **Android-pattern fit:** Common in note apps; less so in formal forms.

#### Option 4 — Bottom action bar (`BottomAppBar`)
- **Concept:** A persistent bottom bar hosting "Add item" (+ room for "paste list", bulk actions).
- **Advantages:** Always reachable; extensible to multiple actions; clear affordance.
- **Disadvantages:** Heaviest chrome; competes with the IME; splitting Save (top) and Add (bottom) is slightly inconsistent; overkill if Add is the only action.
- **Large lists:** Good (always reachable).
- **Implementation complexity:** **Medium.**
- **Android-pattern fit:** Standard, but usually paired with a FAB and used for navigation/primary actions.

> **Pick:** Option 2 (sticky quick-add) for speed and consistency; Option 1 (Extended FAB) as the lower-effort, most-canonical fallback. Avoid 4 unless multiple bottom actions are planned.

### 2B. Detail screen — three directions

#### Direction A — Refined current (sticky quick-add + virtualized + single collapsible "Purchased") — *recommended*
- **Concept:** Keep the model but (1) pin quick-add so it never scrolls, (2) make rows real `LazyColumn` items (virtualized), (3) replace the all-or-nothing toggle with **one** collapsible "Purchased (n)" section, (4) move the title into the top bar and drop the duplicate title/date, (5) tap-row-to-toggle.
- **Advantages:** Builds on the proven quick-add; fixes scrolling + large-list perf; minimal new concepts.
- **Disadvantages:** Still a single-list mental model; the sticky bar costs some vertical space.
- **Large lists:** Great (virtualized; add always visible; done items collapsed).
- **Implementation complexity:** **Medium.**
- **Android-pattern fit:** Strong (Keep/Todoist-style checklist).

#### Direction B — Two modes: "Plan" vs "Shop"
- **Concept:** A toggle switches between Plan (add/edit, all items) and Shop (large rows, big checkboxes, only active items, purchased collapsed, keep-screen-on, optional aisle order).
- **Advantages:** Each task gets an optimized layout; large tap targets at the store; minimal clutter while shopping.
- **Disadvantages:** Users must learn the mode; more state; two layouts to maintain.
- **Large lists:** Excellent in Shop mode (active-only).
- **Implementation complexity:** **Medium–High.**
- **Android-pattern fit:** Mode toggles exist generally; a bespoke "shopping mode" is app-specific (but well-precedented in grocery apps).

#### Direction C — Aisle/category grouping with sticky headers
- **Concept:** Group items by category (Produce, Dairy, …) with sticky section headers; unchecked-first within each group. Categories come from a built-in mapping of common items, with manual override.
- **Advantages:** Matches store layout → genuinely faster shopping; sticky headers aid navigation on long lists.
- **Disadvantages:** Needs category data; mis-categorization annoys; most complex.
- **Large lists:** Best for in-store navigation.
- **Implementation complexity:** **High** (data model + UI).
- **Android-pattern fit:** Sticky headers are standard; categorized grocery lists are common (AnyList, Bring!).

---

## 3. ASCII Mockups (≈ phone width)

### 3A. Detail — CURRENT
```
┌────────────────────────────────┐
│ ✕  Grocery List            ✎    │  top bar: static title, Edit
│    3 / 8 done                   │
├────────────────────────────────┤
│ ▓▓▓▓▓▓░░░░░░░░░░░░░░░░  progress │
│ Hide purchased            (●—)  │  all-or-nothing toggle
│ ┌────────────────────────────┐ │
│ │ Weekly Groceries           │ │  ← title shown AGAIN
│ │ 12 Mar 2026, 18:45         │ │  ← date
│ │ Items for the week         │ │  ← description
│ │ ┌ Groceries ──────────────┐│ │
│ │ │ [ Quick add item…  ] ＋  ││ │  ← scrolls away as list grows
│ │ │ ─────────────────────── ││ │
│ │ │ ☐ Milk                  ││ │
│ │ │ ☐ Eggs                  ││ │
│ │ │ ☑ E̶g̶g̶s̶  (dimmed)         ││ │  ← purchased = just faded
│ │ └─────────────────────────┘│ │
│ └────────────────────────────┘ │
└────────────────────────────────┘
```

### 3B. Detail — PROPOSED (Direction A)
```
┌────────────────────────────────┐
│ ✕  Weekly Groceries        ⋮    │  real title; ⋮ = Uncheck all / Clear purchased
│    3 / 8 done                   │
├────────────────────────────────┤
│ ▓▓▓▓▓▓░░░░░░░░░░░░░░░░           │
│ ┌────────────────────────────┐ │
│ │ ＋ Quick add item…      ⏎  │ │  ← STICKY, never scrolls
│ └────────────────────────────┘ │
│ ☐ Milk                      ✕  │  ← real lazy rows
│ ☐ Eggs                      ✕  │     tap row = toggle
│ ☐ Apples                    ✕  │     ✕ = remove (subtle)
│ ☐ Rice                      ✕  │
│      … (virtualized) …          │
│ ▸ Purchased (3)                 │  ← collapsible, collapsed by default
├────────────────────────────────┤
│  "Removed Eggs"          UNDO   │  ← snackbar on remove
└────────────────────────────────┘
```

### 3C. Edit — CURRENT
```
┌────────────────────────────────┐
│ ✕  Edit Grocery List      Save  │
├────────────────────────────────┤
│ ┌ Title ───────────────────────┐│
│ │ Weekly Groceries             ││
│ └──────────────────────────────┘│
│ ┌ Notes (optional) ────────────┐│
│ │                              ││
│ └──────────────────────────────┘│
│ ┌ Groceries ───────────────────┐│
│ │ ☐ [ Milk            ]  🗑     ││
│ │ ☐ [ Eggs            ]  🗑     ││
│ │ ☐ [ Bread           ]  🗑     ││
│ │        … all rows …          ││
│ └──────────────────────────────┘│
│ [  ＋ Add item  ]                │  ← BOTTOM; scroll to reach, no auto-focus
└────────────────────────────────┘
```

### 3D. Edit — PROPOSED (sticky quick-add; FAB as fallback)
```
┌────────────────────────────────┐
│ ✕  Edit list              Save  │
├────────────────────────────────┤
│ ┌ Title ───────────────────────┐│
│ │ Weekly Groceries             ││
│ └──────────────────────────────┘│
│ Notes ▸ (tap to expand)         │  ← collapsed by default
│ ┌ Groceries (12) ──────────────┐│
│ │ ☐ [ Milk            ]  🗑     ││  ← virtualized rows
│ │ ☐ [ Eggs            ]  🗑     ││
│ │        … rows scroll …       ││
│ └──────────────────────────────┘│
├────────────────────────────────┤
│ [ ＋ Add item…          ]   ⏎   │  ← STICKY quick-add: type, enter, repeat
└────────────────────────────────┘
        ── or fallback ──
            ┌──────────────┐
            │  ＋ Add item  │  ← Extended FAB, bottom-end, inserts row at top
            └──────────────┘
```

---

## 4. Feature Prioritization

| Feature | Impact | Effort | Quadrant |
|---|---|---|---|
| **Sticky Add Item row** | High | Low–Med | **High impact / Low effort** |
| **Clear purchased** | High | Low | **High impact / Low effort** |
| **Bulk actions** (Uncheck all) | High | Low | **High impact / Low effort** |
| **FAB for Add Item** | Med–High | Low | **High impact / Low effort** (alt. to sticky — pick one) |
| **Inline editing** (rename in place) | High | Medium | **High impact / High(-ish) effort** (cheapest of this bucket) |
| **Shopping mode** | High | High | **High impact / High effort** |
| **Quantity support** | High | High | **High impact / High effort** (DB migration + sync map + forms) |
| **Swipe to delete** | Low–Med | Med | **Low impact / High(-ish) effort** (remove already exists; needs row restructure + accidental-delete handling — drops to Low/Low once rows are virtualized) |
| **Drag-and-drop reordering** | Low | High | **Low impact / High effort** |

**Quadrant summary**
- **High impact / Low effort (do next):** Sticky Add Item row, Clear purchased, Uncheck-all, (FAB as the alternative to sticky).
- **High impact / High effort (plan deliberately):** Inline editing (cheapest), Shopping mode, Quantity support.
- **Low impact / Low effort (nice-to-have):** Swipe-to-delete *after* rows are virtualized (it's a faster alternative to a remove icon, not new capability).
- **Low impact / High effort (avoid):** Drag-and-drop reordering.

---

## 5. Recommendation

### Recommended direction
**Unify the app around the quick-add model and a virtualized, collapsible-purchased list.** Concretely: bring Detail's **sticky quick-add** to the Edit screen (this beats a FAB because it enables type-enter-repeat), virtualize item rows on both screens, replace the Detail "Hide purchased" toggle with a single **collapsible "Purchased (n)"** section, and add **Clear purchased + Uncheck all**.

### Why
It targets the four stated goals (taps, scrolling, large-list usability, discoverability) with the **lowest-effort, highest-impact** changes, removes the biggest structural risk (non-virtualized 100-item lists), and gives the whole app **one consistent, fast entry model** instead of two competing ones.

### What to implement next (in order)
1. **Virtualize item rows** on both screens (real `LazyColumn` items). Prerequisite for everything else and the single biggest quality win.
2. **Sticky quick-add on Edit** (reuse the Detail field) — fixes the "Add item at the bottom" problem better than a FAB. Insert new rows at **top** and auto-focus.
3. **Clear purchased** + **Uncheck all** (one-tap list maintenance; "Uncheck all" makes recurring weekly lists reusable).
4. **Collapsible "Purchased (n)"** on Detail; retire the redundant Hide toggle. Tap-row-to-toggle; subtle remove + Undo (the safety net that makes deletion fast and forgiving — never a per-item confirm dialog).

### What to defer
- **Quantity support** (real need, but schema migration + sync-map change + form work — do it as its own focused effort).
- **Shopping mode** (high value once the basics land).
- **Inline rename** on Detail (after the above; it's what would let Edit shrink).
- **Aisle/category grouping** (big bet; revisit if users shop large lists in-store).

### What to never implement
- **Drag-and-drop manual reordering** — it fights the automatic "newest-unchecked-first / purchased-sink" sort, is high-effort, and adds little for groceries.
- **Per-item delete confirmation dialogs** — they tax a high-frequency action; use an **Undo snackbar** instead.

### The design challenge worth raising
**The Edit screen largely duplicates Detail.** If Detail gains inline rename (plus the remove and reorder-by-sort it can already express), Edit collapses to just "title + notes." Consider either (a) reducing Edit to a lightweight title/notes editor, or (b) folding all editing into Detail and removing Edit entirely. Fewer screens, one entry model, less code on the sync-sensitive path — and users stop bouncing between two places to manage the same list.
