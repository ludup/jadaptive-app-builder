Vue.component('text-field', {
  props: ['field'],
  template: `
      <div class='form-group'>
	    <label v-if="field.name !== ''" v-html="field.name"></label>
	    <input class='form-control'>
	    <small v-if="field.description !== ''" class='form-text text-muted'>{{ field.description }}</small>
	  </div>`
});

Vue.component('textarea-field', {
	  props: ['field'],
	  template: `
	      <div class='form-group'>
		    <label v-if="field.name !== ''" v-html="field.name"></label>
		    <textarea class='form-control'>
		    <small v-if="field.description !== ''" class='form-text text-muted'>{{ field.description }}</small>
		  </div>`
});

Vue.component('number-field', {
	  props: ['field'],
	  mounted: function() {
		  $(this.$el).inputSpinner();  
	  },
	  template: `
	      <div class='form-group'>
		    <label v-if="field.name !== ''" v-html="field.name"></label>
		    <input type="number" class="form-control" value="50" min="0" max="100" step="10"/>
		    <small v-if="field.description !== ''" class='form-text text-muted'>{{ field.description }}</small>
		  </div>`
});

Vue.component('checkbox-field', {
	  props: ['field'],
	  template: `
	      <div class='form-group'>
	         <div>
	          <label v-html="field.name"></label>
	         </div>
		      <input type="checkbox" data-toggle="toggle" class="form-check-input">
		      <small v-if="field.description !== ''" class='form-text text-muted'>{{ field.description }}</small>
		  </div>`
});

Vue.component('country-field', {
	  props: ['field'],
	  template: `<div class='form-group'>
<label v-if="field.name !== ''" v-html="field.name"></label>
<select class="form-control">
<option value=""></option>
<option value="AF">Afghanistan</option>
<option value="AX">&angst;land Islands</option>
<option value="AL">Albania</option>
<option value="DZ">Algeria</option>
<option value="AS">American Samoa</option>
<option value="AD">Andorra</option>
<option value="AO">Angola</option>
<option value="AI">Anguilla</option>
<option value="AQ">Antarctica</option>
<option value="AG">Antigua and Barbuda</option>
<option value="AR">Argentina</option>
<option value="AM">Armenia</option>
<option value="AW">Aruba</option>
<option value="AU">Australia</option>
<option value="AT">Austria</option>
<option value="AZ">Azerbaijan</option>
<option value="BS">Bahamas</option>
<option value="BH">Bahrain</option>
<option value="BD">Bangladesh</option>
<option value="BB">Barbados</option>
<option value="BY">Belarus</option>
<option value="BE">Belgium</option>
<option value="BZ">Belize</option>
<option value="BJ">Benin</option>
<option value="BM">Bermuda</option>
<option value="BT">Bhutan</option>
<option value="BO">Bolivia, Plurinational State of</option>
<option value="BQ">Bonaire, Sint Eustatius and Saba</option>
<option value="BA">Bosnia and Herzegovina</option>
<option value="BW">Botswana</option>
<option value="BV">Bouvet Island</option>
<option value="BR">Brazil</option>
<option value="IO">British Indian Ocean Territory</option>
<option value="BN">Brunei Darussalam</option>
<option value="BG">Bulgaria</option>
<option value="BF">Burkina Faso</option>
<option value="BI">Burundi</option>
<option value="KH">Cambodia</option>
<option value="CM">Cameroon</option>
<option value="CA">Canada</option>
<option value="CV">Cape Verde</option>
<option value="KY">Cayman Islands</option>
<option value="CF">Central African Republic</option>
<option value="TD">Chad</option>
<option value="CL">Chile</option>
<option value="CN">China</option>
<option value="CX">Christmas Island</option>
<option value="CC">Cocos (Keeling) Islands</option>
<option value="CO">Colombia</option>
<option value="KM">Comoros</option>
<option value="CG">Congo</option>
<option value="CD">Congo, the Democratic Republic of the</option>
<option value="CK">Cook Islands</option>
<option value="CR">Costa Rica</option>
<option value="CI">Côte d'Ivoire</option>
<option value="HR">Croatia</option>
<option value="CU">Cuba</option>
<option value="CW">Curaçao</option>
<option value="CY">Cyprus</option>
<option value="CZ">Czech Republic</option>
<option value="DK">Denmark</option>
<option value="DJ">Djibouti</option>
<option value="DM">Dominica</option>
<option value="DO">Dominican Republic</option>
<option value="EC">Ecuador</option>
<option value="EG">Egypt</option>
<option value="SV">El Salvador</option>
<option value="GQ">Equatorial Guinea</option>
<option value="ER">Eritrea</option>
<option value="EE">Estonia</option>
<option value="ET">Ethiopia</option>
<option value="FK">Falkland Islands (Malvinas)</option>
<option value="FO">Faroe Islands</option>
<option value="FJ">Fiji</option>
<option value="FI">Finland</option>
<option value="FR">France</option>
<option value="GF">French Guiana</option>
<option value="PF">French Polynesia</option>
<option value="TF">French Southern Territories</option>
<option value="GA">Gabon</option>
<option value="GM">Gambia</option>
<option value="GE">Georgia</option>
<option value="DE">Germany</option>
<option value="GH">Ghana</option>
<option value="GI">Gibraltar</option>
<option value="GR">Greece</option>
<option value="GL">Greenland</option>
<option value="GD">Grenada</option>
<option value="GP">Guadeloupe</option>
<option value="GU">Guam</option>
<option value="GT">Guatemala</option>
<option value="GG">Guernsey</option>
<option value="GN">Guinea</option>
<option value="GW">Guinea-Bissau</option>
<option value="GY">Guyana</option>
<option value="HT">Haiti</option>
<option value="HM">Heard Island and McDonald Islands</option>
<option value="VA">Holy See (Vatican City State)</option>
<option value="HN">Honduras</option>
<option value="HK">Hong Kong</option>
<option value="HU">Hungary</option>
<option value="IS">Iceland</option>
<option value="IN">India</option>
<option value="ID">Indonesia</option>
<option value="IR">Iran, Islamic Republic of</option>
<option value="IQ">Iraq</option>
<option value="IE">Ireland</option>
<option value="IM">Isle of Man</option>
<option value="IL">Israel</option>
<option value="IT">Italy</option>
<option value="JM">Jamaica</option>
<option value="JP">Japan</option>
<option value="JE">Jersey</option>
<option value="JO">Jordan</option>
<option value="KZ">Kazakhstan</option>
<option value="KE">Kenya</option>
<option value="KI">Kiribati</option>
<option value="KP">Korea, Democratic People's Republic of</option>
<option value="KR">Korea, Republic of</option>
<option value="KW">Kuwait</option>
<option value="KG">Kyrgyzstan</option>
<option value="LA">Lao People's Democratic Republic</option>
<option value="LV">Latvia</option>
<option value="LB">Lebanon</option>
<option value="LS">Lesotho</option>
<option value="LR">Liberia</option>
<option value="LY">Libya</option>
<option value="LI">Liechtenstein</option>
<option value="LT">Lithuania</option>
<option value="LU">Luxembourg</option>
<option value="MO">Macao</option>
<option value="MK">Macedonia, the former Yugoslav Republic of</option>
<option value="MG">Madagascar</option>
<option value="MW">Malawi</option>
<option value="MY">Malaysia</option>
<option value="MV">Maldives</option>
<option value="ML">Mali</option>
<option value="MT">Malta</option>
<option value="MH">Marshall Islands</option>
<option value="MQ">Martinique</option>
<option value="MR">Mauritania</option>
<option value="MU">Mauritius</option>
<option value="YT">Mayotte</option>
<option value="MX">Mexico</option>
<option value="FM">Micronesia, Federated States of</option>
<option value="MD">Moldova, Republic of</option>
<option value="MC">Monaco</option>
<option value="MN">Mongolia</option>
<option value="ME">Montenegro</option>
<option value="MS">Montserrat</option>
<option value="MA">Morocco</option>
<option value="MZ">Mozambique</option>
<option value="MM">Myanmar</option>
<option value="NA">Namibia</option>
<option value="NR">Nauru</option>
<option value="NP">Nepal</option>
<option value="NL">Netherlands</option>
<option value="NC">New Caledonia</option>
<option value="NZ">New Zealand</option>
<option value="NI">Nicaragua</option>
<option value="NE">Niger</option>
<option value="NG">Nigeria</option>
<option value="NU">Niue</option>
<option value="NF">Norfolk Island</option>
<option value="MP">Northern Mariana Islands</option>
<option value="NO">Norway</option>
<option value="OM">Oman</option>
<option value="PK">Pakistan</option>
<option value="PW">Palau</option>
<option value="PS">Palestinian Territory, Occupied</option>
<option value="PA">Panama</option>
<option value="PG">Papua New Guinea</option>
<option value="PY">Paraguay</option>
<option value="PE">Peru</option>
<option value="PH">Philippines</option>
<option value="PN">Pitcairn</option>
<option value="PL">Poland</option>
<option value="PT">Portugal</option>
<option value="PR">Puerto Rico</option>
<option value="QA">Qatar</option>
<option value="RE">Réunion</option>
<option value="RO">Romania</option>
<option value="RU">Russian Federation</option>
<option value="RW">Rwanda</option>
<option value="BL">Saint Barthélemy</option>
<option value="SH">Saint Helena, Ascension and Tristan da Cunha</option>
<option value="KN">Saint Kitts and Nevis</option>
<option value="LC">Saint Lucia</option>
<option value="MF">Saint Martin (French part)</option>
<option value="PM">Saint Pierre and Miquelon</option>
<option value="VC">Saint Vincent and the Grenadines</option>
<option value="WS">Samoa</option>
<option value="SM">San Marino</option>
<option value="ST">Sao Tome and Principe</option>
<option value="SA">Saudi Arabia</option>
<option value="SN">Senegal</option>
<option value="RS">Serbia</option>
<option value="SC">Seychelles</option>
<option value="SL">Sierra Leone</option>
<option value="SG">Singapore</option>
<option value="SX">Sint Maarten (Dutch part)</option>
<option value="SK">Slovakia</option>
<option value="SI">Slovenia</option>
<option value="SB">Solomon Islands</option>
<option value="SO">Somalia</option>
<option value="ZA">South Africa</option>
<option value="GS">South Georgia and the South Sandwich Islands</option>
<option value="SS">South Sudan</option>
<option value="ES">Spain</option>
<option value="LK">Sri Lanka</option>
<option value="SD">Sudan</option>
<option value="SR">Suriname</option>
<option value="SJ">Svalbard and Jan Mayen</option>
<option value="SZ">Swaziland</option>
<option value="SE">Sweden</option>
<option value="CH">Switzerland</option>
<option value="SY">Syrian Arab Republic</option>
<option value="TW">Taiwan, Province of China</option>
<option value="TJ">Tajikistan</option>
<option value="TZ">Tanzania, United Republic of</option>
<option value="TH">Thailand</option>
<option value="TL">Timor-Leste</option>
<option value="TG">Togo</option>
<option value="TK">Tokelau</option>
<option value="TO">Tonga</option>
<option value="TT">Trinidad and Tobago</option>
<option value="TN">Tunisia</option>
<option value="TR">Turkey</option>
<option value="TM">Turkmenistan</option>
<option value="TC">Turks and Caicos Islands</option>
<option value="TV">Tuvalu</option>
<option value="UG">Uganda</option>
<option value="UA">Ukraine</option>
<option value="AE">United Arab Emirates</option>
<option value="GB">United Kingdom</option>
<option value="US">United States</option>
<option value="UM">United States Minor Outlying Islands</option>
<option value="UY">Uruguay</option>
<option value="UZ">Uzbekistan</option>
<option value="VU">Vanuatu</option>
<option value="VE">Venezuela, Bolivarian Republic of</option>
<option value="VN">Viet Nam</option>
<option value="VG">Virgin Islands, British</option>
<option value="VI">Virgin Islands, U.S.</option>
<option value="WF">Wallis and Futuna</option>
<option value="EH">Western Sahara</option>
<option value="YE">Yemen</option>
<option value="ZM">Zambia</option>
<option value="ZW">Zimbabwe</option>
</select>
<small v-if="field.description !== ''" class='form-text text-muted'>{{ field.description }}</small>
</div>`
});	  

Vue.component('component-panel', {
	  props: ['fields'],
	  computed: {
	      orderedFields () {
	    	  var tmp = [].concat(this._props.fields);
	    	  tmp.sort(function(a, b){return a.weight - b.weight});
	    	  debugger;
	    	  return tmp;
	      }
	  },
	  template: `
	    <div>
	          <div class="col-xs-12" v-for="field in orderedFields">
	            <template>
                  <text-field v-if="field.fieldType === 'TEXT'" v-bind:field="field"></text-field> 
                  <textarea-field v-if="field.fieldType === 'TEXT_AREA'" v-bind:field="field"></textarea-field> 
                  <checkbox-field v-if="field.fieldType === 'CHECKBOX'" v-bind:field="field"></checkbox-field>
                  <country-field v-if="field.fieldType === 'COUNTRY'" v-bind:field="field"></country-field>
                  <number-field v-if="field.fieldType === 'NUMBER' || field.fieldType === 'DECIMAL'" v-bind:field="field"></number-field>
                </template>
            </div>
      </div>`
});

Vue.component('entity-table', {
	  props: ['page'],
	  components: {
		     'bootstrap-table': BootstrapTable
	  },
	  mounted: function() {
		  $('.jedit').on('click', function(e) {
			  e.preventDefault();
			  var uuid = $(this).data('uuid');
			  this.$el.hide();
		  });
	  },
	  computed: {
		  orderedColumns: function() {
			  var tmp = [];
	    	  if(this._props.page.template.fields) {
	    		  tmp = tmp.concat(this._props.page.template.fields);
		    	  tmp.sort(function(a, b){return a.weight - b.weight});
	    	  }

	    	  var columns = [];
	    	  $.each(tmp, function(idx, obj) {
	    		 if(!obj.ignoreColumn) {
		    		 columns.push({
		    			 title: obj.name,
		    			 field: obj.resourceKey,
		    			 visible: obj.defaultColumn,
		    			 formatter: function(val, row) {
		    				 if(obj.fieldType === 'CHECKBOX') {
		    					 return val ? "YES" : "NO";
		    				 }
		    				 return val;
		    			 }
		    		 });
	    		 }
	    	  });
	    	  
	    	  if(this._props.page.template.categories) {
		    	  tmp = [].concat(this._props.page.template.categories);
		    	  tmp.sort(function(a, b){return a.weight - b.weight});
		    	  
		    	  $.each(tmp, function(idx, cat) {
		    		  
		    		  if(cat.fields) {
			    		  tmp = [].concat(cat.fields);
				    	  tmp.sort(function(a, b){return a.weight - b.weight});
				    	  
				    	  $.each(tmp, function(idx, obj) {
				    		  if(!obj.ignoreColumn) {
				    	  
					    		 columns.push({
					    			 title: obj.name,
					    			 field: cat.resourceKey + '.' + obj.resourceKey,
					    			 visible: obj.defaultColumn,
					    			 formatter: function(val, row) {
					    				 if(obj.fieldType === 'CHECKBOX') {
					    					 return val ? "YES" : "NO";
					    				 }
					    				 return val;
					    			 }
					    		 });
				    		  }
					    	});
		    		  }
		    	  });
	    	  }
	    	  
	    	  columns.push({
	    		  title: "Actions",
	    		  formatter: function(val, row, idx) {
	    			  return '<a data-uuid="' + row.uuid + '" href="#"><i class="far fa-edit"></i></a>';
	    		  }
	    	  });
	    	  return columns;
		  },
		  options: function() {
			  return {
				  search: true,
				  showColumns: true
			  }
		  }
	  },
	  template: `<bootstrap-table :columns="orderedColumns" :data="page.result" :options="options"></bootstrap-table>`
});


Vue.component('template-panel', {
	  props: ['template'],
	  computed: {
          orderedFields () {
	    	  var tmp = [];
	    	  if(this._props.template.fields) {
	    		  tmp = tmp.concat(this._props.template.fields);
		    	  tmp.sort(function(a, b){return a.weight - b.weight});
	    	  }
	    	  return tmp;
          },
          orderedCategories () {
        	  var tmp = [];
	    	  if(this._props.template.categories) {
	    		  tmp = tmp.concat(this._props.template.categories);
		    	  tmp.sort(function(a, b){return a.weight - b.weight});
	    	  }
	    	  return tmp;
          }
      },
	  template: `
	    <form>
	        <component-panel  v-bind:fields="orderedFields"></component-panel>
	        <div v-if="template.categories.length > 0">
	          <ul class="nav nav-tabs" role="tablist">
	             <li class="nav-item" v-for="(category, index) in orderedCategories">
                    <a v-if="index === 0" data-toggle="tab" role="tab" :aria-controls="category.resourceKey" aria-selected="true" class="nav-link active" :href="category.anchor">{{ category.name }}</a>
                    <a v-else data-toggle="tab" role="tab" :aria-controls="category.resourceKey" aria-selected="false" class="nav-link" :href="category.anchor">{{ category.name }}</a>
                 </li>
              </ul>
              <div class="tab-content">
                 <template v-for="(category, index) in orderedCategories">
                    <div v-if="index === 0" class="tab-pane py-3 active" :id="category.resourceKey" role="tabpanel">
	                   <component-panel v-bind:fields="category.fields"></component-panel>
	                </div>
	                <div v-else class="tab-pane py-3" :id="category.resourceKey" role="tabpanel">
	                   <component-panel v-bind:fields="category.fields"></component-panel>
	                </div>
	             </template>
              </div>
            </div>
	    </form>`
});


Vue.component('side-menu', {
	  props: ['items'],
	  data: function() {
		  return {
			  resource: undefined
		  }
	  },
	  computed: { 
		  orderedMenus: function() {

			  var map = new Map();
			  var roots = [];
			  $.each(this._props.items, function(idx, obj) {
			      map.set(obj.uuid, obj);
			      obj.items = [];
			      obj.href = '#' + obj.resource;
			  });
			  
			  $.each(this._props.items, function(idx, obj) {
				  const v4 = new RegExp(/^[0-9A-F]{8}-[0-9A-F]{4}-4[0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$/i);
				  var parent = obj.parent.match(v4);
			      if(parent) {
			    	  var p = map.get(obj.parent);
			    	  p.items.push(obj);
			      } else {
			    	  roots.push(obj);
			      }
			  });
			  
			  return roots;
		  }
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
	  template: `
<aside id="sidebar" class="sidebar">
<div id="sidebar-menu" class="sidebar-menu">
   <template v-for="menu in orderedMenus">
	   <a v-if="menu.items.length === 0" href="#" class="sidebar-link" v-on:click="select(menu.resource)" v-bind:class="{active: isSelected(menu.resource)}">
	      <i class="sidebar-icon" :class="menu.icon"></i>
	      <div class="sidebar-title"><span>{{ menu.title }}</span>
	      </div>
	   </a>
	   <div v-else class="sidebar-link-group">
	      <a :href="menu.href" class="sidebar-link" data-toggle="collapse" role="button" aria-expanded="false" :aria-controls="menu.resource">
	          <i class="sidebar-icon" :class="menu.icon"></i>
	          <div class="sidebar-title"><span>{{ menu.title }}</span>
	          <i class="sidebar-collapse fas fa-sort-down"></i>
	          </div>
	      </a>
	      <div :id="menu.resource" class="collapse sidebar-link-group">
	          <template v-for="item in menu.items">
		          <a href="#" class="sidebar-link" v-on:click="select(item.resource)" v-bind:class="{active: isSelected(item.resource)}">
		          <i class="sidebar-icon" :class="item.icon"></i>
		              <div class="sidebar-title"><span>{{ item.title }}</span></div>
		          </a>
	          </template>
	      </div>
	   </div>
   </template>
</div>
</aside>`
});