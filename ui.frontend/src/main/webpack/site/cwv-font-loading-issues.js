/**
 * CWV ISSUE: Dynamic font loading causing FOIT/FOUT and layout shifts
 */

(function() {
    'use strict';

    // CWV ISSUE: Loading additional fonts dynamically
    function loadAdditionalFonts() {
        // Load multiple font variations after page load
        const fontLinks = [
            'https://fonts.googleapis.com/css2?family=Lato:wght@300;400;700;900&display=block',
            'https://fonts.googleapis.com/css2?family=Oswald:wght@400;700&display=auto',
            'https://fonts.googleapis.com/css2?family=Raleway:wght@300;400;600&display=fallback',
            'https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;700&display=auto'
        ];

        fontLinks.forEach((url, index) => {
            setTimeout(() => {
                const link = document.createElement('link');
                link.rel = 'stylesheet';
                link.href = url;
                // No preconnect, no dns-prefetch
                document.head.appendChild(link);
                
                console.log('Loaded font:', url);
            }, 1000 + (index * 500));
        });
    }

    // CWV ISSUE: Applying fonts after load causes layout shift
    function applyDynamicFonts() {
        setTimeout(() => {
            // Change fonts after page is already rendered
            const elements = document.querySelectorAll('p, span, div');
            
            elements.forEach((el, index) => {
                if (index % 4 === 0) {
                    el.style.fontFamily = 'Lato, sans-serif';
                } else if (index % 4 === 1) {
                    el.style.fontFamily = 'Oswald, sans-serif';
                } else if (index % 4 === 2) {
                    el.style.fontFamily = 'Raleway, sans-serif';
                } else {
                    el.style.fontFamily = 'Open Sans, sans-serif';
                }
            });
            
            console.log('Applied dynamic fonts to', elements.length, 'elements');
        }, 2500);
    }

    // CWV ISSUE: Web Font Loader causing blocking
    function loadFontsWithWebFontLoader() {
        // Simulate WebFont Loader (blocking synchronous approach)
        const script = document.createElement('script');
        script.src = 'https://ajax.googleapis.com/ajax/libs/webfont/1.6.26/webfont.js';
        script.async = false; // Synchronous loading - blocks rendering
        
        script.onload = function() {
            if (window.WebFont) {
                WebFont.load({
                    google: {
                        families: ['Poppins:400,700', 'Montserrat:400,700']
                    },
                    // No font events handled properly
                    timeout: 5000,
                    loading: function() {
                        console.log('Fonts loading...');
                        // Could reserve space here, but don't
                    },
                    active: function() {
                        console.log('Fonts active - layout shifts now');
                        // Fonts loaded - layout shifts occur
                    },
                    inactive: function() {
                        console.log('Fonts failed to load');
                    }
                });
            }
        };
        
        setTimeout(() => {
            document.head.appendChild(script);
        }, 1500);
    }

    // CWV ISSUE: Font Face Observer causing visible text changes
    function useFontFaceObserverPoorly() {
        // Simulate Font Face Observer usage without proper handling
        setTimeout(() => {
            const fonts = [
                { family: 'Playfair Display', weight: 700 },
                { family: 'Source Sans Pro', weight: 600 },
                { family: 'Merriweather', weight: 400 }
            ];

            fonts.forEach(font => {
                // Check if font is loaded
                document.fonts.ready.then(() => {
                    // Apply font styles after checking
                    const elements = document.querySelectorAll('h1, h2, h3, p');
                    
                    elements.forEach(el => {
                        // Change font properties after measurement
                        el.style.fontFamily = font.family;
                        el.style.fontWeight = font.weight;
                        
                        // Force layout recalculation
                        el.offsetHeight;
                    });
                });
            });
        }, 2000);
    }

    // CWV ISSUE: Changing font sizes dynamically based on viewport
    function adjustFontSizesDynamically() {
        function updateFontSizes() {
            const viewportWidth = window.innerWidth;
            const baseSize = viewportWidth < 768 ? 14 : 16;
            
            // Change all font sizes - causes layout shifts
            document.querySelectorAll('*').forEach(el => {
                const computed = window.getComputedStyle(el);
                const currentSize = parseFloat(computed.fontSize);
                
                if (currentSize) {
                    const newSize = (currentSize / 16) * baseSize;
                    el.style.fontSize = newSize + 'px';
                }
            });
            
            console.log('Updated font sizes for viewport:', viewportWidth);
        }
        
        // Update on load
        setTimeout(updateFontSizes, 1000);
        
        // Update on resize (causes constant shifts)
        window.addEventListener('resize', updateFontSizes);
    }

    // CWV ISSUE: Loading icon fonts after interaction
    function loadIconFontsLate() {
        document.addEventListener('click', () => {
            const iconFontLink = document.createElement('link');
            iconFontLink.rel = 'stylesheet';
            iconFontLink.href = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css';
            
            // Icons appear after first click - layout shift
            document.head.appendChild(iconFontLink);
            
            // Then add icon elements
            setTimeout(() => {
                const buttons = document.querySelectorAll('button');
                buttons.forEach(button => {
                    const icon = document.createElement('i');
                    icon.className = 'fas fa-arrow-right';
                    icon.style.marginLeft = '8px';
                    button.appendChild(icon); // Expands button - CLS
                });
            }, 500);
        }, { once: true });
    }

    // CWV ISSUE: Custom font swapping implementation
    function implementBadFontSwapping() {
        // Initial render with system fonts
        document.body.style.fontFamily = 'system-ui, sans-serif';
        
        // Swap to custom fonts after delay
        setTimeout(() => {
            // Check if custom fonts are loaded
            const customFonts = [
                new FontFace('Playfair Display', 'url(https://fonts.gstatic.com/s/playfairdisplay/v30/nuFvD-vYSZviVYUb_rj3ij__anPXJzDwcbmjWBN2PKdFvUDQZNLo_U2r.woff2)'),
                new FontFace('Source Sans Pro', 'url(https://fonts.gstatic.com/s/sourcesanspro/v21/6xK3dSBYKcSV-LCoeQqfX1RYOo3qOK7l.woff2)')
            ];

            Promise.all(customFonts.map(font => font.load())).then(loadedFonts => {
                loadedFonts.forEach(font => {
                    document.fonts.add(font);
                });
                
                // Swap fonts - visible shift
                document.body.style.fontFamily = 'Source Sans Pro, sans-serif';
                document.querySelectorAll('h1, h2, h3').forEach(el => {
                    el.style.fontFamily = 'Playfair Display, serif';
                });
                
                console.log('Font swap completed - CLS occurred');
            }).catch(err => {
                console.error('Font loading failed:', err);
            });
        }, 2000);
    }

    // CWV ISSUE: Loading variable fonts inefficiently
    function loadVariableFontsInefficiently() {
        setTimeout(() => {
            // Load multiple weights instead of using variable font axis
            const weights = [100, 200, 300, 400, 500, 600, 700, 800, 900];
            
            weights.forEach((weight, index) => {
                setTimeout(() => {
                    const link = document.createElement('link');
                    link.rel = 'stylesheet';
                    link.href = `https://fonts.googleapis.com/css2?family=Inter:wght@${weight}&display=swap`;
                    document.head.appendChild(link);
                    
                    // Apply weight to elements
                    const elements = document.querySelectorAll(`[data-weight="${weight}"]`);
                    elements.forEach(el => {
                        el.style.fontFamily = 'Inter, sans-serif';
                        el.style.fontWeight = weight;
                    });
                }, index * 300);
            });
        }, 1000);
    }

    // CWV ISSUE: Fonts for critical above-the-fold content loaded late
    function loadCriticalFontsLate() {
        // Hero/banner fonts should be preloaded but aren't
        setTimeout(() => {
            const heroElements = document.querySelectorAll('.cmp-carousel--hero, .hero-banner, h1');
            
            heroElements.forEach(el => {
                // Large display font loading late
                el.style.fontFamily = 'Playfair Display, serif';
                el.style.fontSize = '4rem';
                el.style.fontWeight = '700';
                
                // Force reflow to measure text
                const width = el.offsetWidth;
                const height = el.offsetHeight;
                
                console.log('Critical text dimensions:', width, height);
            });
        }, 2500);
    }

    // Initialize all font loading issues
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeFontIssues);
    } else {
        initializeFontIssues();
    }

    function initializeFontIssues() {
        console.warn('CWV: Initializing font loading issues for testing...');
        
        loadAdditionalFonts();
        applyDynamicFonts();
        loadFontsWithWebFontLoader();
        useFontFaceObserverPoorly();
        adjustFontSizesDynamically();
        loadIconFontsLate();
        implementBadFontSwapping();
        loadVariableFontsInefficiently();
        loadCriticalFontsLate();
    }
})();


