/*--------------------------------------------------------------------
* jQuery Scroller
* by Brett Smith (brett@sshtools.com), http://www.sshtools.com
* Copyright (c) SSHTools
--------------------------------------------------------------------*/

(function($) {
	$.widget("ui.scroller", {
		options: { 
	        min: 0,
	        max: 100,
	        value: 0,
	        extent: 50,
	        reverse: false,
	        
	    }, 
	    
	    scroll: function(delta) {
	    	var v = this.options.value + delta;
	    	if(v < this.options.min) {
	    		v = this.options.min;
	    	}
	    	if(v > this.options.max) {
	    		v = this.options.max;
	    	}
	    	if(v != this.options.value) {
	    		this.options.value = v;
				this.element.trigger('scrolled', this);
				if(this._waker)
					window.clearInterval(this._waker);
				if(!this._woken) {
					this._woken = true;
					this._checkStyle();
				}
				var self = this;
				this._waker = window.setInterval(function() {
					self._woken = false;
					self._checkStyle();
				}, 1000);
	    	}
	    	return this.options.value;
	    },
	    
	    containIn: function(height) {
    		var mart = this._getCSSPxInt(this.element, 'marginTop', 0);
    		var marb = this._getCSSPxInt(this.element, 'marginBottom', 0);
    		var padt = this._getCSSPxInt(this.element, 'paddingTop', 0);
    		var padb = this._getCSSPxInt(this.element, 'paddingBottom', 0);
    		var bort = this._getCSSPxInt(this.element, 'borderTopWidth', 0);
    		var borb = this._getCSSPxInt(this.element, 'borderBottomWidth', 0);
    		this.element.css({
    			height: height - mart - marb - padt - padb - borb - bort
    		});
	    },
	    
	    max: function(maxValue) {
	    	if(maxValue) {
		    	this.options.max = maxValue;
		    	this._sizeAndPositionThumb();
		    }
	    	return this.options.max;
	    },
	    
	    min: function(minValue) {
	    	if(minValue) {
		    	this.options.min = maxValue;
		    	this._sizeAndPositionThumb();
	    	}
	    	return this.options.min;
	    },
	    
	    value: function(newValue) {
	    	if(newValue) {
		    	this.options.value = newValue;
		    	this._sizeAndPositionThumb();
	    	}
	    	return this.options.value;
	    },
	    
	    extent: function(newExtent) {
	    	if(newExtent) {
		    	this.options.extent = newExtent;
		    	this._sizeAndPositionThumb();
	    	}
	    	return this.options.extent;
	    },
	    
	    init: function(min,max,value,extent) {
	    	this.options.min = min;
    		this.options.max = max;
    		this.options.value = value;
    		this.options.extent = extent;
	    	this._sizeAndPositionThumb();
	    },
	    
	    _getCSSPxInt: function(el, name, defval) {
	    	var z = el.css(name);
    		var v = z ? parseInt(z.replace(/[^-\d\.]/g, ''),10) : defval;
    		if(isNaN(v))
    			v = defval;
    		return v;
	    },
	    
	    _calcThumbSize: function() {
    		var mart = this._getCSSPxInt(this.element, 'marginTop', 0);
    		var padt = this._getCSSPxInt(this.element, 'paddingTop', 0);
    		var padb = this._getCSSPxInt(this.element, 'paddingBottom', 0);
    		var marb = this._getCSSPxInt(this.element, 'marginBottom', 0);
	    	var range = this.options.max - this.options.min;
    		var elHeight = this.element.height();
    		var trackHeight = elHeight;
    		var ext = this.options.extent;
    		var minh = parseInt(this.thumb.css('minHeight').replace(/[^-\d\.]/g, ''),10);
    		var maxh = parseInt(this.thumb.css('maxHeight').replace(/[^-\d\.]/g, ''),10);
    		if(isNaN(maxh))
    			maxh = trackHeight; 
            var h;
            if(range <= 0)
            	h = maxh;
            else if(range < ext) {
            	h = parseInt(trackHeight - range,10);
            }
            else 
            	h = parseInt( ( trackHeight - ext ) * (ext / range),10);
            if(h < minh)
            	h = minh;
            if(h > maxh)
            	h = maxh;
            return h;
	    },

		_checkStyle: function() {
			var has = this.element.hasClass('ui-active');
			var wants = this._dragging || this._woken;
			if(has != wants) {
				if(wants)
					this.element.addClass('ui-active');
				else
					this.element.removeClass('ui-active');
			}
		},
	    
	    _calcValueFactor: function() {
	    	var range = this.options.max - this.options.min;
    		var elHeight = this.element.height();
    		var handleSize = this._calcThumbSize();	 
    		return ( elHeight - handleSize ) / range;
	    },
	    
	    _sizeAndPositionThumb: function() {
    		var padt = this._getCSSPxInt(this.element, 'paddingTop', 0);
    		var mart = this._getCSSPxInt(this.element, 'marginTop', 0);
    		var bort = this._getCSSPxInt(this.element, 'borderWidthTop', 0);
    		var handleSize = this._calcThumbSize();
    		var vv = this.options.value;
    		if(this.options.reverse)
    			vv = this.options.max - vv;
    		var pos = ( this.options.max - this.options.min <= 0 ? 0 : parseInt(vv * this._calcValueFactor(), 10) ) + padt + mart + bort;
    		this.thumb.css({
	 			height: handleSize,
	 			top: pos 
	 		});	 
	    	
	    },
	    
		_handleMotion: function( event, ui ) {
			ui.position.left = this.start_left; 

    		var mart = this._getCSSPxInt(this.element, 'marginTop', 0);
    		var padt = this._getCSSPxInt(this.element, 'paddingTop', 0);
    		var padb = this._getCSSPxInt(this.element, 'paddingBottom', 0);
    		var marb = this._getCSSPxInt(this.element, 'marginBottom', 0);
    		var y = ui.position.top - padt;
    		var eh = this.element.height() + padb + padt;
			
			if(y < 0)
				y = 0;
			if(y + padt + ui.helper.height() + 1 >  eh - padb) 
				y = eh - padb - padt - ui.helper.height(); 

    		ui.position.top = y + padt;
    		
    		var v;
			if(this.options.reverse)
				v = this.options.max - ( Math.round( y / this._calcValueFactor() ) );
			else
				v = this.options.max - this.options.min <= 0 ? this.options.min : Math.round( y / this._calcValueFactor() ) + this.options.min;

			this.options.value = v;
		},

	    _create: function() {
	    	var self = this;

			this._dragging = false;
			this._woken = false;
			this._waker = false;
			
	    	if(!this.element.hasClass('ui-scroller'))
	    		this.element.addClass('ui-scroller');
	    	if(!this.element.hasClass('ui-slider'))
	    		this.element.addClass('ui-slider');
	    	if(!this.element.hasClass('ui-slider-horizontal'))
	    		this.element.addClass('ui-slider-horizontal');
	    	if(!this.element.hasClass('ui-slider')) 
	    		this.element.addClass('ui-slider');
	    	if(!this.element.hasClass('ui-corner-all')) 
	    		this.element.addClass('ui-corner-all');
	    	if(!this.element.hasClass('ui-widget')) 
	    		this.element.addClass('ui-widget');
	    	if(!this.element.hasClass('ui-widget-content')) 
	    		this.element.addClass('ui-widget-content');
	    	
	    	if('scrolled' in this.options)
	    		this.element.on('scrolled', function(event, ui) {
	    			self.options.scrolled(event, ui);
	    		});
	    	if('scrollstart' in this.options)
	    		this.element.on('scrollstart', function(event, ui) {
	    			self.options.scrollstart(event, ui);
	    		});
	    	if('scrollend' in this.options)
	    		this.element.on('scrollend', function(event, ui) {
	    			self.options.scrollend(event, ui);
	    		});
	    	
	    	// Create thumb
	    	var thumbId = this.element.id;
	    	if(!thumbId)
	    		thumbId = 'scroller';
	    	this.thumb = jQuery('<span/>', {
	    		id:  + '-handle'
	    	}).appendTo(this.element);
    		this.thumb.addClass('ui-slider-handle ui-corner-all ui-state-default');
    		
    		this.thumb.draggable({
    			
    			drag: function( event, ui ) {
    				self._handleMotion(event, ui);
    				self.element.trigger('scrolled', self);
    			},
    			start: function( event, ui ) {
    				self._dragging = true;
    				self._handleMotion(event, ui);
    				self.element.trigger('scrollstart', self);
    				self._checkStyle();
    			},
    			stop: function( event, ui ) {
    				self._dragging = false;
    				self._handleMotion(event, ui);
    				self.element.trigger('scrollend', this);
    				self._checkStyle();
    			},
    			create: function( event, ui ) {
    				self.start_left = self.thumb.position().left;
    			}
    		});
	    	
	    	this._sizeAndPositionThumb();
	    }
	});

}(jQuery));