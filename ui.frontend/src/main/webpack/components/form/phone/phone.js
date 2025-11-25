import jQuery from "jquery";

jQuery(function($) {
    "use strict";

    /**
     * Phone Number Component
     * Handles phone number formatting, validation, and country code selection
     */
    (function() {
        const PHONE_SELECTOR = '[data-cmp-hook-form-phone]';
        
        /**
         * Phone number patterns for validation by country code
         */
        const phonePatterns = {
            '+1': /^\d{10}$/, // US/Canada - 10 digits
            '+44': /^\d{10,11}$/, // UK - 10-11 digits
            '+91': /^\d{10}$/, // India - 10 digits
            '+61': /^\d{9,10}$/, // Australia - 9-10 digits
            '+81': /^\d{10,11}$/, // Japan - 10-11 digits
            '+86': /^\d{11}$/, // China - 11 digits
            '+49': /^\d{10,11}$/, // Germany - 10-11 digits
            '+33': /^\d{9}$/, // France - 9 digits
            '+39': /^\d{9,10}$/, // Italy - 9-10 digits
            '+34': /^\d{9}$/, // Spain - 9 digits
            '+7': /^\d{10}$/, // Russia - 10 digits
            '+52': /^\d{10}$/, // Mexico - 10 digits
            '+55': /^\d{10,11}$/, // Brazil - 10-11 digits
            '+27': /^\d{9}$/, // South Africa - 9 digits
            '+82': /^\d{9,10}$/, // South Korea - 9-10 digits
            '+62': /^\d{9,12}$/, // Indonesia - 9-12 digits
            '+66': /^\d{9}$/, // Thailand - 9 digits
            '+31': /^\d{9}$/, // Netherlands - 9 digits
            '+46': /^\d{9,10}$/, // Sweden - 9-10 digits
            '+47': /^\d{8}$/, // Norway - 8 digits
            '+45': /^\d{8}$/, // Denmark - 8 digits
            '+41': /^\d{9}$/, // Switzerland - 9 digits
            '+43': /^\d{10,11}$/, // Austria - 10-11 digits
            '+32': /^\d{9}$/, // Belgium - 9 digits
            '+48': /^\d{9}$/, // Poland - 9 digits
            '+20': /^\d{10}$/, // Egypt - 10 digits
            '+971': /^\d{9}$/, // UAE - 9 digits
            '+65': /^\d{8}$/, // Singapore - 8 digits
            '+852': /^\d{8}$/, // Hong Kong - 8 digits
            '+64': /^\d{8,10}$/ // New Zealand - 8-10 digits
        };
        
        /**
         * Format phone number as user types
         */
        function formatPhoneNumber(value, countryCode) {
            // Remove all non-numeric characters
            const cleaned = value.replace(/\D/g, '');
            
            // Format based on country code
            switch(countryCode) {
                case '+1': { // US/Canada format: (XXX) XXX-XXXX
                    if (cleaned.length <= 3) {
                        return cleaned;
                    }
                    if (cleaned.length <= 6) {
                        return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3)}`;
                    }
                    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6, 10)}`;
                }
                
                case '+44': { // UK format: XXXX XXX XXXX
                    if (cleaned.length <= 4) {
                        return cleaned;
                    }
                    if (cleaned.length <= 7) {
                        return `${cleaned.slice(0, 4)} ${cleaned.slice(4)}`;
                    }
                    return `${cleaned.slice(0, 4)} ${cleaned.slice(4, 7)} ${cleaned.slice(7, 11)}`;
                }
                
                case '+91': { // India format: XXXXX XXXXX
                    if (cleaned.length <= 5) {
                        return cleaned;
                    }
                    return `${cleaned.slice(0, 5)} ${cleaned.slice(5, 10)}`;
                }
                
                default: // Generic format with spaces every 3 digits
                    return cleaned.replace(/(\d{3})(?=\d)/g, '$1 ');
            }
        }
        
        /**
         * Validate phone number based on country code
         */
        function validatePhoneNumber(value, countryCode) {
            const cleaned = value.replace(/\D/g, '');
            const pattern = phonePatterns[countryCode] || /^\d{7,15}$/; // Default: 7-15 digits
            return pattern.test(cleaned);
        }
        
        /**
         * Initialize phone component
         */
        function initPhoneComponent($component) {
            const $countryCode = $component.find('[data-cmp-hook-phone="countryCode"]');
            const $numberInput = $component.find('[data-cmp-hook-phone="number"]');
            const $fullNumberInput = $component.find('[data-cmp-hook-phone="fullNumber"]');
            const $errorBlock = $component.find('.cmp-form-phone__error-block');
            
            if (!$countryCode.length || !$numberInput.length) {
                return;
            }
            
            // Update full number hidden field
            function updateFullNumber() {
                const countryCode = $countryCode.val();
                const phoneNumber = $numberInput.val().replace(/\D/g, '');
                $fullNumberInput.val(countryCode + phoneNumber);
            }
            
            // Show/hide error message
            function updateErrorDisplay(show) {
                if (show) {
                    $errorBlock.fadeIn(200);
                } else {
                    $errorBlock.fadeOut(200);
                }
            }
            
            // Format input as user types
            $numberInput.on('input', function() {
                const countryCode = $countryCode.val();
                const rawValue = $(this).val();
                const cursorPosition = this.selectionStart;
                const oldLength = rawValue.length;
                
                // Format the number
                const formatted = formatPhoneNumber(rawValue, countryCode);
                
                // Update the input value
                $(this).val(formatted);
                
                // Restore cursor position (accounting for added formatting)
                const newLength = formatted.length;
                const newPosition = cursorPosition + (newLength - oldLength);
                this.setSelectionRange(newPosition, newPosition);
                
                // Update full number
                updateFullNumber();
                
                // Validate
                const cleanedValue = formatted.replace(/\D/g, '');
                const isValid = cleanedValue.length === 0 || validatePhoneNumber(formatted, countryCode);
                $(this).attr('aria-invalid', !isValid);
                
                if (isValid || cleanedValue.length === 0) {
                    $(this).removeClass('cmp-form-phone__number--error');
                    updateErrorDisplay(false);
                } else {
                    $(this).addClass('cmp-form-phone__number--error');
                    updateErrorDisplay(true);
                }
            });
            
            // Re-format when country code changes
            $countryCode.on('change', function() {
                const countryCode = $(this).val();
                const currentValue = $numberInput.val();
                const formatted = formatPhoneNumber(currentValue, countryCode);
                $numberInput.val(formatted);
                updateFullNumber();
                
                // Re-validate
                const cleanedValue = formatted.replace(/\D/g, '');
                const isValid = cleanedValue.length === 0 || validatePhoneNumber(formatted, countryCode);
                $numberInput.attr('aria-invalid', !isValid);
                
                if (isValid || cleanedValue.length === 0) {
                    $numberInput.removeClass('cmp-form-phone__number--error');
                    updateErrorDisplay(false);
                } else {
                    $numberInput.addClass('cmp-form-phone__number--error');
                    updateErrorDisplay(true);
                }
            });
            
            // Handle paste events
            $numberInput.on('paste', function() {
                setTimeout(function() {
                    const pastedValue = $numberInput.val();
                    
                    // Check if pasted value includes country code
                    if (pastedValue.startsWith('+')) {
                        // Extract country code from pasted value
                        const match = pastedValue.match(/^(\+\d+)/);
                        if (match) {
                            const pastedCountryCode = match[1];
                            // Check if we support this country code
                            const $option = $countryCode.find(`option[value="${pastedCountryCode}"]`);
                            if ($option.length) {
                                $countryCode.val(pastedCountryCode);
                            }
                            // Remove country code from number
                            const numberOnly = pastedValue.replace(/^\+\d+\s*/, '');
                            $numberInput.val(numberOnly);
                        }
                    }
                    
                    // Format the number
                    const formatted = formatPhoneNumber($numberInput.val(), $countryCode.val());
                    $numberInput.val(formatted);
                    updateFullNumber();
                }, 10);
            });
            
            // Initialize full number on load
            updateFullNumber();
        }
        
        // Initialize all phone components on page
        $(PHONE_SELECTOR).each(function() {
            initPhoneComponent($(this));
        });
        
        // Re-initialize on dynamic content load (e.g., AEM editor)
        $(document).on('DOMNodeInserted', function(e) {
            const $target = $(e.target);
            if ($target.is(PHONE_SELECTOR)) {
                initPhoneComponent($target);
            } else {
                $target.find(PHONE_SELECTOR).each(function() {
                    initPhoneComponent($(this));
                });
            }
        });
        
    })();

});

