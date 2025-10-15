/**
 * CWV ISSUE: JavaScript causing FID/INP (First Input Delay / Interaction to Next Paint) problems
 * These scripts block the main thread and make interactions sluggish
 */

(function() {
    'use strict';

    // CWV ISSUE: Heavy synchronous event handlers
    function setupHeavyEventHandlers() {
        // Add expensive click handlers that block the main thread
        document.addEventListener('click', (e) => {
            // Heavy computation on every click - terrible for INP
            const startTime = performance.now();
            let result = 0;
            
            // Simulate expensive operation (100ms block)
            while (performance.now() - startTime < 100) {
                result += Math.random() * Math.random();
                JSON.parse(JSON.stringify({ data: new Array(100).fill(result) }));
            }
            
            console.log('Heavy click handler executed:', result);
        });

        // Heavy scroll handler without throttling/debouncing
        document.addEventListener('scroll', () => {
            // Synchronous heavy operations on every scroll event
            const elements = document.querySelectorAll('*');
            const positions = Array.from(elements).map(el => ({
                top: el.getBoundingClientRect().top,
                left: el.getBoundingClientRect().left,
                computed: window.getComputedStyle(el)
            }));
            
            // Force reflow and repaint
            elements.forEach(el => {
                el.offsetHeight; // Force reflow
                el.style.transform = `translateZ(0)`; // Force layer
            });
            
            console.log('Heavy scroll handler:', positions.length);
        }, { passive: false }); // Not passive - blocks scrolling

        // Heavy mousemove handler
        document.addEventListener('mousemove', (e) => {
            // Expensive DOM queries on every mouse move
            const allElements = document.querySelectorAll('*');
            allElements.forEach(el => {
                const rect = el.getBoundingClientRect();
                const isHovered = (
                    e.clientX >= rect.left &&
                    e.clientX <= rect.right &&
                    e.clientY >= rect.top &&
                    e.clientY <= rect.bottom
                );
                
                if (isHovered) {
                    // Expensive style calculations
                    const styles = window.getComputedStyle(el);
                    const matrix = new DOMMatrix(styles.transform);
                    // Complex calculations
                    Math.sqrt(matrix.a * matrix.a + matrix.b * matrix.b);
                }
            });
        });

        // Heavy keyboard handler
        document.addEventListener('keydown', (e) => {
            // Block main thread on every keypress
            const start = Date.now();
            while (Date.now() - start < 50) {
                // Busy wait - blocks input processing
                Math.random();
            }
        });
    }

    // CWV ISSUE: Synchronous XHR requests
    function setupSyncXHRRequests() {
        const buttons = document.querySelectorAll('button');
        
        buttons.forEach((button, index) => {
            button.addEventListener('click', () => {
                // Synchronous XHR blocks the main thread
                const xhr = new XMLHttpRequest();
                xhr.open('GET', `/api/data/${index}`, false); // false = synchronous
                try {
                    xhr.send();
                } catch (e) {
                    console.error('Sync XHR failed (expected):', e);
                }
                
                // Also do heavy processing after click
                heavyCalculation();
            });
        });
    }

    // CWV ISSUE: Heavy calculation function that blocks thread
    function heavyCalculation() {
        const iterations = 10000000;
        let result = 0;
        
        for (let i = 0; i < iterations; i++) {
            result += Math.sqrt(i) * Math.sin(i) * Math.cos(i);
        }
        
        return result;
    }

    // CWV ISSUE: Forced synchronous layouts (reflow thrashing)
    function setupReflowThrashing() {
        setInterval(() => {
            const elements = document.querySelectorAll('.cmp-container, .cmp-teaser, .cmp-image');
            
            // Read-write-read-write pattern causes layout thrashing
            elements.forEach(el => {
                // Read - forces layout
                const height = el.offsetHeight;
                
                // Write - invalidates layout
                el.style.height = (height + 1) + 'px';
                
                // Read again - forces another layout
                const width = el.offsetWidth;
                
                // Write again - invalidates layout
                el.style.width = (width + 1) + 'px';
            });
        }, 1000);
    }

    // CWV ISSUE: Long tasks that block the main thread
    function scheduleLongTasks() {
        // Schedule recurring long tasks
        setInterval(() => {
            const startTime = performance.now();
            
            // Create a task that takes > 50ms (long task threshold)
            while (performance.now() - startTime < 200) {
                // Simulate complex computations
                const largeArray = new Array(10000).fill(0).map(() => Math.random());
                largeArray.sort();
                largeArray.reverse();
                JSON.stringify(largeArray);
            }
            
            console.log('Long task completed');
        }, 3000);
    }

    // CWV ISSUE: Heavy form validation
    function setupHeavyFormValidation() {
        const inputs = document.querySelectorAll('input, textarea');
        
        inputs.forEach(input => {
            input.addEventListener('input', (e) => {
                // Heavy validation on every keystroke
                const value = e.target.value;
                
                // Expensive regex operations
                const patterns = [
                    /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
                    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
                    /^(\+\d{1,3}[- ]?)?\d{10}$/,
                    /^[A-Z]{2}\d{6}$/
                ];
                
                patterns.forEach(pattern => {
                    for (let i = 0; i < 1000; i++) {
                        pattern.test(value + i);
                    }
                });
                
                // Heavy DOM manipulation on validation
                const allElements = document.querySelectorAll('*');
                allElements.forEach(el => {
                    el.getAttribute('class');
                });
            });
        });
    }

    // CWV ISSUE: Expensive button interactions
    function setupExpensiveButtons() {
        const buttons = document.querySelectorAll('.cmp-button, button');
        
        buttons.forEach(button => {
            button.addEventListener('click', (e) => {
                // Prevent default immediately
                e.preventDefault();
                
                // Heavy synchronous operations before any visual feedback
                const startTime = performance.now();
                
                // Simulate complex business logic
                let calculations = 0;
                while (performance.now() - startTime < 150) {
                    calculations++;
                    const data = {
                        timestamp: Date.now(),
                        random: Math.random(),
                        calculations: calculations
                    };
                    
                    // Expensive serialization
                    JSON.stringify(data);
                    JSON.parse(JSON.stringify(data));
                    
                    // Force layout
                    document.body.offsetHeight;
                }
                
                // Only now show feedback
                button.textContent = 'Clicked!';
                setTimeout(() => {
                    button.textContent = button.getAttribute('data-original-text') || 'Click Me';
                }, 1000);
            });
        });
    }

    // CWV ISSUE: Heavy animation frame callbacks
    function setupHeavyAnimationFrame() {
        function heavyRAF() {
            // Expensive operation in animation frame
            const allElements = document.querySelectorAll('*');
            
            allElements.forEach(el => {
                // Force style recalculation
                const computed = window.getComputedStyle(el);
                const transform = computed.transform;
                
                // Expensive calculation
                if (transform !== 'none') {
                    const matrix = new DOMMatrix(transform);
                    const angle = Math.atan2(matrix.b, matrix.a);
                    const scale = Math.sqrt(matrix.a * matrix.a + matrix.b * matrix.b);
                    
                    // More calculations
                    Math.sin(angle) * scale;
                }
            });
            
            // Force layout
            document.body.offsetHeight;
            
            requestAnimationFrame(heavyRAF);
        }
        
        requestAnimationFrame(heavyRAF);
    }

    // CWV ISSUE: Blocking image processing
    function setupBlockingImageProcessing() {
        const images = document.querySelectorAll('img');
        
        images.forEach(img => {
            img.addEventListener('load', () => {
                // Heavy processing when image loads
                const canvas = document.createElement('canvas');
                const ctx = canvas.getContext('2d');
                
                canvas.width = img.naturalWidth || 800;
                canvas.height = img.naturalHeight || 600;
                
                // Draw and process (blocks main thread)
                ctx.drawImage(img, 0, 0);
                
                try {
                    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
                    const data = imageData.data;
                    
                    // Heavy pixel processing
                    for (let i = 0; i < data.length; i += 4) {
                        // Apply expensive filters
                        const r = data[i];
                        const g = data[i + 1];
                        const b = data[i + 2];
                        
                        // Complex color calculations
                        data[i] = Math.sin(r / 255) * 255;
                        data[i + 1] = Math.cos(g / 255) * 255;
                        data[i + 2] = Math.tan(b / 255) * 255;
                    }
                    
                    ctx.putImageData(imageData, 0, 0);
                } catch (e) {
                    // Security error for cross-origin images
                    console.log('Image processing blocked (CORS)');
                }
            });
        });
    }

    // CWV ISSUE: Expensive search/filter operations
    function setupExpensiveSearch() {
        const searchInputs = document.querySelectorAll('input[type="search"], input[type="text"]');
        
        searchInputs.forEach(input => {
            input.addEventListener('input', (e) => {
                const query = e.target.value.toLowerCase();
                
                // Expensive search without debouncing
                const allTextNodes = [];
                const walker = document.createTreeWalker(
                    document.body,
                    NodeFilter.SHOW_TEXT,
                    null,
                    false
                );
                
                // Collect all text nodes (expensive)
                while (walker.nextNode()) {
                    allTextNodes.push(walker.currentNode);
                }
                
                // Search through all text
                let matches = 0;
                allTextNodes.forEach(node => {
                    if (node.textContent.toLowerCase().includes(query)) {
                        matches++;
                        // Force layout for each match
                        node.parentElement.offsetHeight;
                    }
                });
                
                console.log(`Found ${matches} matches for "${query}"`);
            });
        });
    }

    // Initialize all FID/INP issues
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeINPIssues);
    } else {
        initializeINPIssues();
    }

    function initializeINPIssues() {
        console.warn('CWV: Initializing interactivity issues for testing...');
        
        setupHeavyEventHandlers();
        setupSyncXHRRequests();
        setupReflowThrashing();
        scheduleLongTasks();
        setupHeavyFormValidation();
        setupExpensiveButtons();
        setupHeavyAnimationFrame();
        setupBlockingImageProcessing();
        setupExpensiveSearch();
    }
})();


