# Core Web Vitals Issues Documentation

This document describes all the Core Web Vitals (CWV) issues intentionally introduced into the WKND project for testing detection, analysis, and fixing workflows.

## Overview

We've introduced various performance issues across three main Core Web Vitals metrics:

1. **LCP (Largest Contentful Paint)** - How long it takes for the largest content element to render
2. **CLS (Cumulative Layout Shift)** - Visual stability, measuring unexpected layout shifts
3. **FID/INP (First Input Delay / Interaction to Next Paint)** - Interactivity responsiveness

---

## 1. LCP Issues (Largest Contentful Paint)

### 1.1 Render-Blocking Scripts
**Location:** `ui.apps/src/main/content/jcr_root/apps/wknd/components/page/customheaderlibs.html`

**Issues:**
- Synchronous external analytics script loads in `<head>` blocking render
- Large third-party libraries (Lodash, Moment.js) loaded synchronously
- No `async` or `defer` attributes on script tags

**Impact:** Delays LCP by 2-5 seconds as browser waits for scripts to download and execute before rendering.

**Lines:** 21-26

```html
<script src="https://cdn.example.com/heavy-analytics.js"></script>
<script src="https://cdn.jsdelivr.net/npm/lodash@4.17.21/lodash.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/moment@2.29.4/moment.min.js"></script>
```

### 1.2 Heavy Synchronous Operations on Page Load
**Location:** `ui.frontend/src/main/webpack/site/main.js`

**Issues:**
- 2-second busy-wait loop blocking main thread immediately on page load
- Large array operations (1M elements) sorted and filtered synchronously
- Massive localStorage operations (1000 writes) on every page load

**Impact:** Blocks main thread for ~2-3 seconds, preventing any rendering or interactivity.

**Lines:** 6-33

### 1.3 Font Loading Without Optimization
**Location:** `ui.apps/src/main/content/jcr_root/apps/wknd/components/page/customheaderlibs.html`

**Issues:**
- Google Fonts loaded with `display=block` causing FOIT (Flash of Invisible Text)
- Removed preconnect hints
- Loading multiple font families and weights unnecessarily

**Impact:** Text invisible until fonts load, delaying LCP for text-heavy pages.

**Lines:** 18-19

---

## 2. CLS Issues (Cumulative Layout Shift)

### 2.1 Dynamic Content Injection Without Reserved Space
**Location:** `ui.frontend/src/main/webpack/site/cwv-layout-shift.js`

**Issues:**
- Cookie banner inserted at top of page after 1s delay, pushing all content down
- Promotional banners injected without height reservation after 2s
- Dynamic content loaded without placeholder dimensions
- Ad slots load and expand without min-height reservation

**Impact:** Major layout shifts (CLS > 0.25) as content gets pushed down after initial render.

**Functions:**
- `injectCookieBanner()` - Inserts banner at document top
- `injectPromoBanner()` - Adds banner in middle of content
- `loadDynamicContent()` - Loads content without placeholders
- `loadAdBanners()` - Ads without space reservation

### 2.2 Images Without Dimensions
**Location:** `ui.frontend/src/main/webpack/site/cwv-layout-shift.js`

**Issues:**
- Removes width/height attributes from all images after 500ms
- Removes aspect-ratio CSS properties
- Images expand causing content to shift down

**Impact:** Every image causes layout shift when it loads.

**Function:** `loadImagesWithoutDimensions()`

### 2.3 CSS Causing Layout Shifts
**Location:** `ui.frontend/src/main/webpack/site/cwv-layout-issues.scss`

**Issues:**
- No aspect-ratio or min-height on image containers
- Navigation height changes after JS initialization
- Accordion panels expand without height reservation
- Animations that move content (slideIn, fadeInUp)
- Cookie banner positioned relative instead of fixed

**Impact:** Multiple small shifts accumulating to high CLS score.

**Key Selectors:**
- `.cmp-teaser__image`, `.cmp-carousel__image` - No dimensions
- `.cmp-navigation` - Height jumps on initialization
- `.cmp-accordion__panel` - Expands without reservation
- `.cookie-banner` - Pushes content when appearing

### 2.4 Late Navigation Modifications
**Location:** `ui.frontend/src/main/webpack/site/cwv-layout-shift.js`

**Issues:**
- New navigation items added after 1.8s without height reservation
- Navigation expands vertically causing shift

**Function:** `modifyNavigationAfterLoad()`

### 2.5 Social Embeds and Iframes Without Placeholders
**Location:** `ui.frontend/src/main/webpack/site/cwv-layout-shift.js`

**Issues:**
- Iframes inserted without height reservation
- Social media embeds load and expand

**Function:** `loadSocialEmbeds()`

---

## 3. FID/INP Issues (First Input Delay / Interaction to Next Paint)

### 3.1 Heavy Event Handlers Blocking Main Thread
**Location:** `ui.frontend/src/main/webpack/site/cwv-interactivity-issues.js`

**Issues:**
- Click handler with 100ms synchronous calculation on every click
- Scroll handler querying all elements without throttling/debouncing
- Non-passive scroll listeners blocking scroll performance
- Mousemove handler with expensive DOM queries
- Keyboard handler with 50ms busy-wait on every keypress

**Impact:** User interactions delayed by 100-200ms, poor responsiveness.

**Function:** `setupHeavyEventHandlers()`

### 3.2 Synchronous XHR Requests
**Location:** `ui.frontend/src/main/webpack/site/cwv-interactivity-issues.js`

**Issues:**
- Button clicks trigger synchronous XMLHttpRequest
- Blocks main thread until request completes

**Impact:** Button clicks freeze the entire page.

**Function:** `setupSyncXHRRequests()`

### 3.3 Layout Thrashing (Forced Reflows)
**Location:** `ui.frontend/src/main/webpack/site/cwv-interactivity-issues.js`

**Issues:**
- Read-write-read-write pattern every second
- Forces layout recalculation multiple times per iteration
- Queries offsetHeight/offsetWidth repeatedly

**Impact:** Janky animations, sluggish interactions.

**Function:** `setupReflowThrashing()`

### 3.4 Long Tasks (>50ms)
**Location:** `ui.frontend/src/main/webpack/site/cwv-interactivity-issues.js`

**Issues:**
- Recurring 200ms tasks every 3 seconds
- Large array operations blocking thread
- Complex computations without yielding

**Impact:** Page becomes unresponsive during long tasks.

**Function:** `scheduleLongTasks()`

### 3.5 Heavy Form Validation
**Location:** `ui.frontend/src/main/webpack/site/cwv-interactivity-issues.js`

**Issues:**
- Expensive regex validation on every keystroke
- 1000 regex tests per input event
- No debouncing or throttling

**Impact:** Typing feels sluggish, delayed input feedback.

**Function:** `setupHeavyFormValidation()`

### 3.6 Expensive Button Interactions
**Location:** `ui.frontend/src/main/webpack/site/cwv-interactivity-issues.js`

**Issues:**
- 150ms synchronous operation before showing feedback
- JSON serialization/deserialization in loop
- Forced layout recalculations

**Impact:** Button feels unresponsive, no immediate feedback.

**Function:** `setupExpensiveButtons()`

### 3.7 Heavy Animation Frame Callbacks
**Location:** `ui.frontend/src/main/webpack/site/cwv-interactivity-issues.js`

**Issues:**
- requestAnimationFrame doing expensive operations
- Queries all elements' computed styles every frame
- Complex matrix calculations
- Forces layouts every frame

**Impact:** Choppy animations, poor frame rate.

**Function:** `setupHeavyAnimationFrame()`

### 3.8 Blocking Image Processing
**Location:** `ui.frontend/src/main/webpack/site/cwv-interactivity-issues.js`

**Issues:**
- Canvas pixel manipulation on image load
- Heavy mathematical operations on pixel data
- Blocks main thread during processing

**Impact:** Page freezes when images load.

**Function:** `setupBlockingImageProcessing()`

### 3.9 Expensive Search Without Debouncing
**Location:** `ui.frontend/src/main/webpack/site/cwv-interactivity-issues.js`

**Issues:**
- TreeWalker traversing entire DOM on every keystroke
- No debouncing on search input
- Forces layout for every match

**Impact:** Search input extremely laggy.

**Function:** `setupExpensiveSearch()`

### 3.10 Heavy Navigation Handlers
**Location:** `ui.frontend/src/main/webpack/components/navigation/navigation.js`

**Issues:**
- 80ms busy-wait on navigation hover
- Forces multiple reflows (height, width)
- Queries all elements' computed styles on click

**Impact:** Navigation feels sluggish and unresponsive.

**Lines:** 92-130

---

## 4. Font Loading Issues (FOIT/FOUT)

### 4.1 Web Fonts Without font-display
**Location:** `ui.frontend/src/main/webpack/base/sass/_cwv-font-issues.scss`

**Issues:**
- Custom @font-face declarations without font-display property
- Causes FOIT (Flash of Invisible Text)
- No fallback font with similar metrics
- Multiple font families loaded (4+)
- Icon fonts without font-display: block

**Impact:** Text invisible during font load, then layout shift when fonts swap in.

### 4.2 Dynamic Font Loading
**Location:** `ui.frontend/src/main/webpack/site/cwv-font-loading-issues.js`

**Issues:**
- Loading additional fonts after page load (4 font families)
- Applying fonts dynamically causing layout shifts
- WebFont Loader with synchronous script
- Fonts changed after initial render
- No preload hints for critical fonts

**Impact:** Visible text reflow and layout shifts as fonts load and apply.

**Functions:**
- `loadAdditionalFonts()` - Loads 4 font families after delay
- `applyDynamicFonts()` - Changes fonts after 2.5s
- `loadFontsWithWebFontLoader()` - Blocking font loader
- `implementBadFontSwapping()` - System fonts → custom fonts swap

### 4.3 Critical Fonts Loaded Late
**Location:** `ui.frontend/src/main/webpack/site/cwv-font-loading-issues.js`

**Issues:**
- Hero/banner fonts (above-the-fold) loaded after 2.5s
- No preload for critical display fonts
- Large font sizes (4rem) causing noticeable shifts

**Impact:** Hero text shifts significantly affecting LCP and CLS.

**Function:** `loadCriticalFontsLate()`

---

## Testing Strategy

### What to Look For

1. **LCP Issues:**
   - Open DevTools Performance panel
   - Check for long "Scripting" tasks before first paint
   - Look for render-blocking resources in Network panel
   - LCP should be > 4 seconds (bad)

2. **CLS Issues:**
   - Enable "Layout Shift Regions" in DevTools Rendering panel
   - Watch for blue highlights showing layout shifts
   - Cookie banner, ads, and images should cause visible shifts
   - CLS score should be > 0.25 (bad)

3. **FID/INP Issues:**
   - Try clicking buttons - should feel sluggish
   - Type in search/input fields - delayed feedback
   - Hover over navigation - noticeable lag
   - Check Performance panel for long tasks (red triangles)
   - INP should be > 500ms (bad)

4. **Font Issues:**
   - Watch text appear/disappear/change during load
   - Notice layout shifts as fonts swap
   - Check Network panel for font loading timeline

### Pages to Test

- **Homepage** - All issues present
- **Article pages** - Text-heavy, font issues prominent
- **Form pages** - Input lag, validation issues
- **Product/listing pages** - Images, CLS issues

### Tools to Use

- **Chrome DevTools:**
  - Performance panel
  - Lighthouse (should score poorly)
  - Coverage tool (shows unused JS/CSS)
  - Rendering panel (Layout Shift Regions)

- **Web Vitals Extension:**
  - Shows real-time LCP, CLS, FID
  - Should all be in "Poor" range (red)

- **PageSpeed Insights:**
  - Run on deployed page
  - Should show numerous opportunities and diagnostics

---

## Expected Scores (Bad)

- **LCP:** > 4 seconds (should be < 2.5s)
- **CLS:** > 0.25 (should be < 0.1)
- **FID/INP:** > 300ms (should be < 100ms)
- **Lighthouse Performance Score:** < 50/100

---

## Files Modified Summary

### HTML Templates
- `ui.apps/src/main/content/jcr_root/apps/wknd/components/page/customheaderlibs.html`

### JavaScript Files (New)
- `ui.frontend/src/main/webpack/site/cwv-layout-shift.js` - CLS issues
- `ui.frontend/src/main/webpack/site/cwv-interactivity-issues.js` - FID/INP issues
- `ui.frontend/src/main/webpack/site/cwv-font-loading-issues.js` - Font issues

### JavaScript Files (Modified)
- `ui.frontend/src/main/webpack/site/main.js` - Imports all CWV issue modules
- `ui.frontend/src/main/webpack/components/navigation/navigation.js` - Heavy handlers

### CSS Files (New)
- `ui.frontend/src/main/webpack/site/cwv-layout-issues.scss` - CLS CSS
- `ui.frontend/src/main/webpack/base/sass/_cwv-font-issues.scss` - Font issues

### CSS Files (Modified)
- `ui.frontend/src/main/webpack/site/main.scss` - Imports CLS styles
- `ui.frontend/src/main/webpack/base/sass/_shared.scss` - Imports font issues

---

## Verification Checklist

Before deploying to production, verify on localhost:4502:

- [ ] Page takes > 3 seconds to show main content (LCP)
- [ ] Cookie banner pushes content down after 1 second
- [ ] Promotional banner appears after 2 seconds in content
- [ ] Images cause layout shifts when loading
- [ ] Clicking buttons feels sluggish (100ms+ delay)
- [ ] Typing in inputs has noticeable lag
- [ ] Hovering navigation items is slow
- [ ] Text disappears/reappears as fonts load (FOIT)
- [ ] Text size/spacing changes as fonts load (layout shift)
- [ ] Scrolling feels janky
- [ ] Long tasks visible in Performance panel
- [ ] Lighthouse score < 50

---

## Purpose

These issues are intentionally created to:

1. **Test detection systems** - Verify monitoring can catch CWV issues
2. **Test analysis tools** - Ensure proper identification of root causes
3. **Test fixing workflows** - Validate automated or manual fix suggestions
4. **Train teams** - Provide examples of common performance problems
5. **Benchmark improvements** - Before/after comparison

**DO NOT** deploy this branch to production without removing these issues!

---

## Cleanup Instructions

To remove all CWV issues:

1. Revert `customheaderlibs.html` to remove blocking scripts
2. Remove heavy sync operations from `main.js`
3. Delete these files:
   - `cwv-layout-shift.js`
   - `cwv-interactivity-issues.js`
   - `cwv-font-loading-issues.js`
   - `cwv-layout-issues.scss`
   - `cwv-font-issues.scss`
4. Remove imports from `main.js` and `main.scss`
5. Revert `navigation.js` to remove heavy handlers
6. Revert `_shared.scss` to remove font issue import

Alternatively, checkout the main branch to get clean code.

