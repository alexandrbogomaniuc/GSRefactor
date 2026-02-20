import Vue from 'vue';
import EventDispatcher from '../../../../common/PIXI/src/dgphoenix/unified/controller/events/EventDispatcher';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Screen from './components/screen/Screen.vue';
import BaseImage from './components/base/BaseImage.vue';
import BaseButton from './components/base/BaseButton.vue';
import Paytable from './components/paytable/Paytable.vue';
import BaseTextField from './components/base/BaseTextField.vue';
import PaytableContent from './components/paytable/PaytableContent.vue';
import PrintableRulesButton from './components/paytable/PrintableRulesButton.vue';
import PaytableScreenController from '../controller/uis/custom/secondary/paytable/PaytableScreenController';

class VueApplicationController extends EventDispatcher
{
	static get EVENT_TIME_TO_SHOW_VUE_LAYER() 						{ return 'EVENT_TIME_TO_SHOW_VUE_LAYER' };
	static get EVENT_TIME_TO_HIDE_VUE_LAYER() 						{ return 'EVENT_TIME_TO_HIDE_VUE_LAYER' };
	static get EVENT_ON_PAYTABLE_CLOSE_BUTTON_CLICKED() 			{ return 'EVENT_ON_PAYTABLE_CLOSE_BUTTON_CLICKED' };
	static get EVENT_ON_PAYTABLE_PRINTABLE_RULES_BUTTON_CLICKED() 	{ return 'EVENT_ON_PAYTABLE_PRINTABLE_RULES_BUTTON_CLICKED' };

	constructor()
	{
		super();

		this._fVm_v = null;
		this._fPaytableScreenController_psc = null;
	}

	i_init()
	{
		this._fPaytableScreenController_psc = APP.secondaryScreenController.paytableScreenController;
		this._fPaytableScreenController_psc.on(PaytableScreenController.EVENT_ON_SCREEN_SHOW, this._onPaytableScreenShow, this);
		this._fPaytableScreenController_psc.on(PaytableScreenController.EVENT_ON_SCREEN_HIDE, this._onPaytableScreenHide, this);
	}

	addDOMScreen()
	{
		Vue.component('Screen', Screen);
		Vue.component('Paytable', Paytable);
		Vue.component('BaseImage', BaseImage);
		Vue.component('BaseButton', BaseButton);
		Vue.component('BaseTextField', BaseTextField);
		Vue.component('PaytableContent', PaytableContent);
		
		/*if (I18.currentLocale == "en") //TODO: temporary allow printable rules only for EN (0024122: [REEVO] GENERAL SUPPORT-Triple Cash or Crash™️ Translation issue checking)
		{
			Vue.component('PrintableRulesButton', PrintableRulesButton);
		}*/

		this._fVm_v = new Vue({
			render: h => h(Screen),
			methods: {},
			mounted() {}
		});

		var container = APP.layout.vueScreenNode;

		this._fVm_v.$mount();
		container.appendChild(this._fVm_v.$el);

		Object.assign(container.style, {overflow: "hidden"});

		this._fVm_v.$on('paytable-close-button-click', this._onPaytableCloseButtonClicked.bind(this));
		this._fVm_v.$on('paytable-printable-rules-click', this._onPaytablePrintableRulesButtonClicked.bind(this));
	}

	_onPaytableCloseButtonClicked(event)
	{
		this.emit(VueApplicationController.EVENT_ON_PAYTABLE_CLOSE_BUTTON_CLICKED);
	}

	_onPaytablePrintableRulesButtonClicked(event)
	{
		this.emit(VueApplicationController.EVENT_ON_PAYTABLE_PRINTABLE_RULES_BUTTON_CLICKED);
	}

	_onPaytableScreenShow(event)
	{
		this.emit(VueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER);
	}

	_onPaytableScreenHide(event)
	{
		this.emit(VueApplicationController.EVENT_TIME_TO_HIDE_VUE_LAYER);
	}
}

export default VueApplicationController;