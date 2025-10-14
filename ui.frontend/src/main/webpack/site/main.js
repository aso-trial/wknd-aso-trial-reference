// Stylesheets
import './main.scss';

// CWV ISSUE: Heavy synchronous operations blocking main thread
// This simulates expensive computations that hurt LCP and FID
(function heavySyncOperation() {
    // Heavy computation on page load
    const startTime = Date.now();
    let result = 0;
    
    // Simulate complex calculations - blocks main thread for ~2 seconds
    while (Date.now() - startTime < 2000) {
        result += Math.random() * Math.random();
    }
    
    // Large array operations
    const largeArray = new Array(1000000).fill(0).map((_, i) => i * Math.random());
    const sorted = largeArray.sort((a, b) => b - a);
    const filtered = sorted.filter(x => x > 0.5);
    
    console.log('Heavy sync operation completed:', filtered.length);
})();

// CWV ISSUE: Synchronous localStorage operations
(function massiveLocalStorageOps() {
    for (let i = 0; i < 1000; i++) {
        localStorage.setItem(`cwv-test-${i}`, JSON.stringify({
            data: new Array(100).fill('x').join(''),
            timestamp: Date.now(),
            index: i
        }));
    }
})();

// Typescript/Javascript
import './util.js';
import './scroll-indicator';

// CWV ISSUE: Layout shift problems
import './cwv-layout-shift.js';

// CWV ISSUE: Interactivity problems (FID/INP)
import './cwv-interactivity-issues.js';

// CWV ISSUE: Font loading problems (FOIT/FOUT)
import './cwv-font-loading-issues.js';

import '../components/languagenavigation/languagenavigation.js';
import '../components/layout-container/modal.js';
import '../components/navigation/navigation.js';
import '../components/form/sign-in-buttons/sign-in-buttons.js';
import '../components/form/sign-in-form/sign-in-form.js';
import '../components/contentfragment/js/contributor.js';
