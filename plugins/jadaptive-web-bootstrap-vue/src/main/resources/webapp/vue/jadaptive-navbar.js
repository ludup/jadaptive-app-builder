Vue.component('jadaptive-navbar', {
	  data: function() {
		  return {
			  resource: undefined,
			  items: []
		  }
	  },
	  computed: { 
		  orderedMenus: function() {

			  var map = new Map();
			  var roots = [];
			  $.each(this.items, function(idx, obj) {
			      map.set(obj.uuid, obj);
			      obj.menus = [];
			      obj.href = '#' + obj.resource;
			  });
			  
			  $.each(this.items, function(idx, obj) {
				  const v4 = new RegExp(/^[0-9A-F]{8}-[0-9A-F]{4}-4[0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$/i);
				  var parent = obj.parent.match(v4);
			      if(parent) {
			    	  var p = map.get(obj.parent);
			    	  p.menus.push(obj);
			      } else {
			    	  roots.push(obj);
			      }
			  });
			  
			  return roots;
		  }
      },
      mounted() { 
          axios.get("/api/applicationMenu/list")
          .then(response => {
          	this.items = response.data.result
          })
      },
      methods: {
    	  isSelected: function(resource) {
    		  return this.resource === resource;
    	  },
    	  select: function(resource) {
    		  debugger;
    		  this.resource = resource;
    	  },
      },
	  template: `<b-navbar type="dark" variant="dark" class="col-12">
		    <b-navbar-nav class="col-12">
		       <img src="https://www.jadaptive.com/app/api/files/public/6216fa2b-18be-48cc-9b8e-c1426fc2cc6b/jadaptive-logo.png">
		       <div class="col-1"></div>
		       <template v-for="menu in orderedMenus">
		       	   <b-nav-item 
		       	       v-if="menu.menus.length === 0" 
		       	       href="#" 
		       	       v-on:click="select(menu.resource)" 
		       	       v-bind:class="{active: isSelected(menu.resource)}">
		       	       <router-link to="/bar">{{ menu.title }}</router-link>
		  		   </b-nav-item>
		  		   <b-nav-item-dropdown v-else text="Lang" right>
		  		       <template v-for="item in menu.menus">
		  		           <b-dropdown-item href="#"
		  		           		v-on:click="select(menu.resource)" 
		       	       			v-bind:class="{active: isSelected(menu.resource)}">
		  		              <router-link to="/foo">{{ menu.title }}</router-link>
		  		           </b-dropdown-item>
		  		       </template>
		  		   </b-nav-item-dropdown>
		       </template>
		    </b-navbar-nav>
		  </b-navbar>`
});