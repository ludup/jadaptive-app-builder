Vue.component('text-field', {
  props: ['field'],
  template: `
      <div class='form-group'>
	    <label>{{ field.name }}</label>
	    <input class='form-control'>
	    <small class='form-text text-muted'>{{ field.description }}</small>
	  </div>`
});

Vue.component('component-panel', {
	  props: ['fields'],
	  template: `
	    <div>
	          <div class="col-xs-12" v-for="field in fields">
	            <template v-if="field.fieldType === 'TEXT'">
                 <text-field v-bind:field="field"></text-field> 
              </template>
              <template v-else><p>No component defined</p></template>
            </div>
      </div>`
});

Vue.component('template-panel', {
	  props: ['template'],
	  template: `
	    <form>
	        <component-panel  v-bind:fields="template.fields"></component-panel>
	        <div v-if="template.categories.length > 0">
	          <ul class="nav nav-tabs">
	             <li class="nav-item" v-for="(category, index) in template.categories">
                    <a v-if="index === 0" class="nav-link active" :href="category.anchor">{{ category.name }}</a>
                    <a v-else class="nav-link" :href="category.anchor">{{ category.name }}</a>
                 </li>
              </ul>
              <div class="tab-content">
                 <div v-for="(category, index) in template.categories">
                    <div v-if="index === 0" class="tab-pane show active" :id="category.resourceKey" role="tabpanel">
	                   <component-panel v-bind:fields="category.fields"></component-panel>
	                </div>
	                <div v-else class="tab-pane" :id="category.resourceKey" role="tabpanel">
	                   <component-panel v-bind:fields="category.fields"></component-panel>
	                </div>
	             </div>
              </div>
            </div>
	    </form>`
});