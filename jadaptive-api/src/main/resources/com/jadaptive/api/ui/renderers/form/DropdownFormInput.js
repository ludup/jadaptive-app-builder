class DropdownFormInput {
    constructor(element) {
        this.container = element;
        this.input = this.selectOption('input.form-control');
        this.menu = this.selectOption('.dropdown-menu');
        this.items = element.querySelectorAll('.dropdown-item');
		this.toggleMenu = this.selectOption('[jad\\:role="toggle-menu"]');
		this.reference = this.selectOption('[jad\\:role="reference"]');
        this.allowAnyText = element.dataset.allowAnyText === 'true';
        this.lastFocusedItem = null;
        this.lastSelectedItem = null;

        // Initialize Bootstrap Dropdown instance manually
        this.dropdown = new bootstrap.Dropdown(this.input);

        this.initEvents();
    }
	
	selectOption(selector) {
		var el = this.container.querySelector(selector);
		if(!el) {
			throw new Error('Required element ' + selector + ' not found in DropdownFormInput');
		}
		return el;
	}
	
	getVisibleItems() {
		return Array.from(this.items).filter(
	            item => item.style.display !== 'none'
	    );
	}
	
	setActiveItem(item) {
        if (!item) {
            return;
        }
        this.items.forEach(entry => {
            entry.classList.remove('active');
            entry.setAttribute('aria-selected', 'false');
        });
        item.classList.add('active');
        item.setAttribute('aria-selected', 'true');
	}
	
	isInputSelectionActive() {
		if (document.activeElement !== this.input) {
		    return false;
		}
		if (this.input.selectionStart === null || this.input.selectionEnd === null) {
		    return false;
		}
		return this.input.selectionEnd > this.input.selectionStart;
	}
	
	focusInputEnd() {
		this.input.focus();
		const len = this.input.value.length;
		this.input.setSelectionRange(len, len);
	}

	focusInputAll() {
		this.input.focus();
		const len = this.input.value.length;
		this.input.setSelectionRange(0, len);
	}
	
	focusNextFormField(currentField) {
		const field = currentField || this.input;
		const root = field.closest('form') || document;
		const focusableSelector = 'a[href], button:not([disabled]), input:not([disabled]):not([type="hidden"]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';
		const focusable = Array.from(root.querySelectorAll(focusableSelector))
			.filter(el => el.offsetParent !== null || el === field);
		const currentIndex = focusable.indexOf(field);
		if (currentIndex === -1) {
			return;
		}
		const nextField = focusable[currentIndex + 1];
		if (nextField) {
			nextField.focus();
		}
	}

	focusItemByDelta(currentItem, delta) {
		const visibleItems = this.getVisibleItems();
		const currentIndex = visibleItems.indexOf(currentItem);
		if (currentIndex === -1) {
		    return;
		}

		const nextIndex = currentIndex + delta;
		if (nextIndex < 0) {
		    this.focusInputEnd();
		    return;
		}

		const nextItem = visibleItems[nextIndex];
		if (nextItem) {
		    nextItem.focus();
		    this.setActiveItem(nextItem);
		}
	}
	
    hideAndSelectLast() {

        if (this.allowAnyText) {
			this.dropdown.hide();
            return;
        }

        const visibleItems = this.getVisibleItems();
        const isVisible = (item) => item && item.style.display !== 'none';
        const selectedItem = isVisible(this.lastFocusedItem)
            ? this.lastFocusedItem
            : (isVisible(this.lastSelectedItem) ? this.lastSelectedItem : visibleItems[0]);

		this.setSelectedItem(selectedItem);
    }
	
	setSelectedItem(target) {
		
		if(target) {
			this.input.value = target.textContent ? target.textContent : '';
			this.lastSelectedItem = target;
			this.lastFocusedItem = target;	
		}
		else {
			this.input.value = '';
		}
		
		this.reference.value = target ? target.dataset.resourcekey : '';
		this.dropdown.hide();

		// Trigger a change event so other scripts know the value changed
		this.input.dispatchEvent(new Event('change'));
	}
	
    initEvents() {

        // Filter items on typing
        this.input.addEventListener('input', () => {
            this.applyFilter();
        });

        // Select item on click
        this.menu.addEventListener('click', (e) => {
            const target = e.target.closest('.dropdown-item');
            if (target) {
				e.preventDefault();
				this.setSelectedItem(target);
            }
        });

        // Track focused item within the menu
        this.menu.addEventListener('focusin', (e) => {
            const target = e.target.closest('.dropdown-item');
            if (target) {
                this.lastFocusedItem = target;
                this.setActiveItem(target);
            }
        });

        // Handle keys in the menu
        this.menu.addEventListener('keydown', (e) => {

			if(e.key === 'Escape') {
				e.preventDefault();
				this.hideAndSelectLast();
			}
            else if (e.key === 'Home' || e.key === 'End') {
                const visibleItems = this.getVisibleItems();
                const targetItem = e.key === 'Home'
                    ? visibleItems[0]
                    : visibleItems[visibleItems.length - 1];

                if (targetItem) {
                    e.preventDefault();
                    targetItem.focus();
                    this.setActiveItem(targetItem);
                }
                return;
            }
			else if (e.key === 'Enter') {
	            const currentItem = e.target.closest('.dropdown-item');
	            if (!currentItem) {
	                return;
	            }
	
	            e.preventDefault();
				this.setSelectedItem(currentItem);
				this.focusNextFormField(this.toggleMenu);
			}

            if (e.key !== 'ArrowDown' && e.key !== 'ArrowUp') {
                return;
            }

            const currentItem = e.target.closest('.dropdown-item');
            if (!currentItem) {
                return;
            }

            e.preventDefault();
            this.focusItemByDelta(currentItem, e.key === 'ArrowDown' ? 1 : -1);
        });

		// When focus is lost from the input field and it is not the menu taking it,
		// the hide the menu if it is visible.

		this.input.addEventListener('focusout', (e) => {
			
			const nextTarget = e.relatedTarget;
            if (nextTarget && (this.menu.contains(nextTarget) || nextTarget === this.menu)) {
                return;
            }

			this.hideAndSelectLast();
		});

        // Auto-select first visible item when menu loses focus to an outside element
        this.menu.addEventListener('focusout', (e) => {
		
            const nextTarget = e.relatedTarget;
            if (nextTarget && (this.menu.contains(nextTarget) || nextTarget === this.input)) {
                return;
            }

           	this.hideAndSelectLast();
        });

        // Show all options when clicking the input
		this.input.addEventListener('focusin', (e) => {
				
			this.applyFilter();

			if (!this.isInputSelectionActive()) {
				this.focusInputAll();
			}
		});

        // Show dropdown and focus first option on ArrowDown
        this.input.addEventListener('keydown', (e) => {
		    if (e.key === 'Escape') {
				e.preventDefault();
				
				this.hideAndSelectLast();
			}
            else if (e.key === 'ArrowDown') {
                e.preventDefault();
                this.dropdown.show();

                if (this.isInputSelectionActive()) {
                    return;
                }

                const firstVisible = this.getVisibleItems()[0];
                if (firstVisible) {
                    firstVisible.focus();
                }
            } else if (e.key === 'ArrowUp') {
                // If dropdown is open and user presses ArrowUp in input, keep caret at end
                if (this.isInputSelectionActive()) {
                    return;
                }
                const len = this.input.value.length;
                this.input.setSelectionRange(len, len);
            }
        });
		
		// Toggle dropdown when clicking the toggle button or pressing space on it and select all text in the input for easy replacement
		this.toggleMenu.addEventListener('click', (e) => {
			e.preventDefault();
			this.focusInputAll();
			this.applyFilter(true);
		});
		
		this.toggleMenu.addEventListener('keydown', (e) => {
			if (e.key === ' ') {
				e.preventDefault();
				this.applyFilter(true);
				this.focusInputAll();
			}
		});
			
    }
	
	applyFilter(matchAll = false) {
		
		const filter = this.input.value.toLowerCase();
		let hasVisibleItems = false;

		this.items.forEach(item => {
		    const text = item.textContent.toLowerCase();
		    if (matchAll || text.includes(filter)) {
		        item.style.display = '';
		        hasVisibleItems = true;
		    } else {
		        item.style.display = 'none';
		    }
		});

		// Show menu if there's a match, hide if not
		if (hasVisibleItems && filter.length > 0) {
		    this.dropdown.show();
		} else if (filter.length === 0) {
		    this.dropdown.show(); // Show all options if empty
		} else {
		    this.dropdown.hide();
		}
	}
}

// Global initialization
document.querySelectorAll('.dropdown-form-field').forEach(el => new DropdownFormInput(el));