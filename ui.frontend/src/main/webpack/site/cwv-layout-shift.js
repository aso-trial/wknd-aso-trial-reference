/**
 * CWV ISSUE: JavaScript causing Cumulative Layout Shift (CLS) problems
 * These scripts dynamically modify the DOM causing visual instability
 */

(function() {
    'use strict';

    // CWV ISSUE: Cookie banner that pushes content down
    function injectCookieBanner() {
        // Wait 1 second then inject banner at top
        setTimeout(() => {
            const banner = document.createElement('div');
            banner.className = 'cookie-banner';
            banner.innerHTML = `
                <div class="cookie-banner__content">
                    <p>We use cookies to enhance your experience. By continuing to visit this site you agree to our use of cookies.</p>
                    <button class="cookie-banner__accept">Accept</button>
                </div>
            `;
            
            // Insert at top of body - pushes all content down (CLS violation)
            document.body.insertBefore(banner, document.body.firstChild);
            
            banner.querySelector('.cookie-banner__accept').addEventListener('click', () => {
                banner.remove(); // Removing also causes shift
            });
        }, 1000);
    }

    // CWV ISSUE: Promotional banner injected after delay
    function injectPromoBanner() {
        setTimeout(() => {
            const containers = document.querySelectorAll('.cmp-container');
            if (containers.length > 0) {
                const promoBanner = document.createElement('div');
                promoBanner.className = 'promo-banner';
                promoBanner.innerHTML = `
                    <h3>Special Offer!</h3>
                    <p>Sign up today and get 20% off your first order!</p>
                    <button>Learn More</button>
                `;
                
                // Insert in middle of content - major CLS violation
                containers[0].insertBefore(promoBanner, containers[0].children[2]);
            }
        }, 2000);
    }

    // CWV ISSUE: Dynamic content loading without placeholder
    function loadDynamicContent() {
        const dynamicContainers = document.querySelectorAll('[data-dynamic-content]');
        
        dynamicContainers.forEach((container) => {
            // Simulate API call delay
            setTimeout(() => {
                const content = document.createElement('div');
                content.className = 'cmp-dynamic-content';
                content.innerHTML = `
                    <h2>Dynamic Content Loaded</h2>
                    <p>This content was loaded dynamically without reserving space.</p>
                    <img src="https://via.placeholder.com/800x400" alt="Dynamic image">
                `;
                
                // No height reservation - causes layout shift
                container.appendChild(content);
            }, 1500);
        });
    }

    // CWV ISSUE: Ads loading without reserved space
    function loadAdBanners() {
        const adSlots = document.querySelectorAll('.ad-slot');
        
        adSlots.forEach((slot, index) => {
            setTimeout(() => {
                const ad = document.createElement('div');
                ad.className = 'cmp-ad-banner';
                ad.style.minHeight = '250px';
                ad.style.background = '#f0f0f0';
                ad.innerHTML = `
                    <div style="padding: 20px; text-align: center;">
                        <p>Advertisement</p>
                        <img src="https://via.placeholder.com/728x90" alt="Ad banner">
                    </div>
                `;
                
                // No placeholder height - CLS when ad loads
                slot.appendChild(ad);
            }, 800 + (index * 500));
        });
    }

    // CWV ISSUE: Social media embeds without placeholders
    function loadSocialEmbeds() {
        const embedContainers = document.querySelectorAll('.social-embed-container');
        
        embedContainers.forEach((container) => {
            setTimeout(() => {
                const iframe = document.createElement('iframe');
                iframe.src = 'about:blank'; // Placeholder URL
                iframe.width = '100%';
                iframe.height = '400'; // Height not reserved before load
                iframe.frameBorder = '0';
                
                // Iframe loads and expands - causes CLS
                container.appendChild(iframe);
            }, 2500);
        });
    }

    // CWV ISSUE: Images loaded without dimensions
    function loadImagesWithoutDimensions() {
        // Find all images and remove width/height attributes
        setTimeout(() => {
            const images = document.querySelectorAll('img');
            images.forEach((img) => {
                // Remove dimension hints - causes shift when image loads
                img.removeAttribute('width');
                img.removeAttribute('height');
                
                // Also remove aspect-ratio CSS if present
                img.style.aspectRatio = '';
            });
        }, 500);
    }

    // CWV ISSUE: Late-loading navigation modifications
    function modifyNavigationAfterLoad() {
        setTimeout(() => {
            const nav = document.querySelector('.cmp-navigation');
            if (nav) {
                // Add items without height reservation
                const newItems = [
                    { text: 'New Page 1', href: '/new1' },
                    { text: 'New Page 2', href: '/new2' },
                    { text: 'New Page 3', href: '/new3' }
                ];
                
                const navList = nav.querySelector('ul');
                if (navList) {
                    newItems.forEach((item) => {
                        const li = document.createElement('li');
                        li.className = 'cmp-navigation__item';
                        li.innerHTML = `<a href="${item.href}">${item.text}</a>`;
                        navList.appendChild(li); // Causes navigation to expand
                    });
                }
            }
        }, 1800);
    }

    // CWV ISSUE: Accordion that expands without smooth height transition
    function setupBadAccordions() {
        const accordions = document.querySelectorAll('.cmp-accordion__button');
        
        accordions.forEach((button) => {
            button.addEventListener('click', () => {
                const panel = button.nextElementSibling;
                if (panel) {
                    // Toggle without height reservation - CLS
                    if (panel.style.display === 'none' || !panel.style.display) {
                        panel.style.display = 'block'; // Instant expansion causes shift
                    } else {
                        panel.style.display = 'none';
                    }
                }
            });
        });
    }

    // CWV ISSUE: Tooltip positioning that causes reflows
    function setupBadTooltips() {
        const tooltipTriggers = document.querySelectorAll('[data-tooltip]');
        
        tooltipTriggers.forEach((trigger) => {
            trigger.addEventListener('mouseenter', () => {
                const tooltip = document.createElement('div');
                tooltip.className = 'tooltip';
                tooltip.textContent = trigger.dataset.tooltip;
                tooltip.style.position = 'relative'; // Should be absolute/fixed
                tooltip.style.background = '#333';
                tooltip.style.color = 'white';
                tooltip.style.padding = '8px 12px';
                tooltip.style.borderRadius = '4px';
                
                // Insert inline - causes layout shift
                trigger.appendChild(tooltip);
            });
            
            trigger.addEventListener('mouseleave', () => {
                const tooltip = trigger.querySelector('.tooltip');
                if (tooltip) {
                    tooltip.remove(); // Removal causes shift back
                }
            });
        });
    }

    // Initialize all CLS problems on DOMContentLoaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeCLSIssues);
    } else {
        initializeCLSIssues();
    }

    function initializeCLSIssues() {
        console.warn('CWV: Initializing layout shift issues for testing...');
        
        injectCookieBanner();
        injectPromoBanner();
        loadDynamicContent();
        loadAdBanners();
        loadSocialEmbeds();
        loadImagesWithoutDimensions();
        modifyNavigationAfterLoad();
        setupBadAccordions();
        setupBadTooltips();
    }
})();


