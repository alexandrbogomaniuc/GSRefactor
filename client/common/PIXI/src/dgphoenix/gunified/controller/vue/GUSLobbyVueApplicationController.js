import EventDispatcher from '../../../unified/controller/events/EventDispatcher';
import GUSLobbyPaytableScreenController from '../uis/custom/secondary/paytable/GUSLobbyPaytableScreenController';
import { APP } from '../../../unified/controller/main/globals';

class GUSLobbyVueApplicationController extends EventDispatcher
{
	static get EVENT_TIME_TO_SHOW_VUE_LAYER() 						{ return 'EVENT_TIME_TO_SHOW_VUE_LAYER' }
	static get EVENT_TIME_TO_HIDE_VUE_LAYER() 						{ return 'EVENT_TIME_TO_HIDE_VUE_LAYER' }
	static get EVENT_ON_PAYTABLE_CLOSE_BUTTON_CLICKED() 			{ return 'EVENT_ON_PAYTABLE_CLOSE_BUTTON_CLICKED' }
	static get EVENT_ON_PAYTABLE_PRINTABLE_RULES_BUTTON_CLICKED() 	{ return 'EVENT_ON_PAYTABLE_PRINTABLE_RULES_BUTTON_CLICKED' }

	constructor()
	{
		super();

		this._fVm_v = null;
		this._fPaytableScreenController_psc = null;
	}

	i_init()
	{
		this._fPaytableScreenController_psc = APP.secondaryScreenController.paytableScreenController;
		this._fPaytableScreenController_psc.on(GUSLobbyPaytableScreenController.EVENT_ON_SCREEN_SHOW, this._onPaytableScreenShow, this);
		this._fPaytableScreenController_psc.on(GUSLobbyPaytableScreenController.EVENT_ON_SCREEN_HIDE, this._onPaytableScreenHide, this);
	}

	addDOMScreen()
	{
	}

	_onPaytableCloseButtonClicked(event)
	{
		this.emit(GUSLobbyVueApplicationController.EVENT_ON_PAYTABLE_CLOSE_BUTTON_CLICKED);
	}

	_onPaytablePrintableRulesButtonClicked(event)
	{
		this.emit(GUSLobbyVueApplicationController.EVENT_ON_PAYTABLE_PRINTABLE_RULES_BUTTON_CLICKED);
	}

	_onPaytableScreenShow(event)
	{
		this.emit(GUSLobbyVueApplicationController.EVENT_TIME_TO_SHOW_VUE_LAYER);
	}

	_onPaytableScreenHide(event)
	{
		this.emit(GUSLobbyVueApplicationController.EVENT_TIME_TO_HIDE_VUE_LAYER);
	}
}

export default GUSLobbyVueApplicationController;