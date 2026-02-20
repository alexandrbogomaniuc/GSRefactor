import SimpleUIController from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import VueApplicationController from '../../../../../vue/VueApplicationController';
import CrashAPP from '../../../../../CrashAPP';
import SubloadingController from '../../../../subloading/SubloadingController';
import { SUBLOADING_ASSETS_TYPES } from '../../../../../config/Constants';


class PaytableScreenController extends SimpleUIController
{
	static get EVENT_ON_CLOSE_BTN_CLICKED()		{ return 'onCloseBtnClicked'; }
	static get EVENT_ON_PAYTABLE_DATA_READY() 	{ return 'EVENT_ON_PAYTABLE_DATA_READY'; }
	static get EVENT_ON_SCREEN_SHOW()			{ return "onScreenShow"; }
	static get EVENT_ON_SCREEN_HIDE()			{ return "onScreenHide"; }

	showScreen()
	{
		this._showScreen();
	}

	hideScreen()
	{
		this._hideSceeen();
	}

	get paytableData()
	{
		return this._fPaytableData_obj;
	}

	get paytableCurrency()
	{
		return this._fCurrencyData_obj;
	}

	get printableRulesPath()
	{
		return this._fPrintableRulesPath_str;
	}

	constructor()
	{
		super();

		this._fPaytableData_obj = null;
		this._fIsScreenShown_bl = false;
		this._fIsWaitingToShowScreen = false;
		this._fPrintableRulesPath_str = null;

		this._fVueApplicationController_vac = APP.vueApplicationController;
		this._fSubloadingController_sc = APP.subloadingController;

		this._fIsBGLoading_bl = APP.appParamsInfo.backgroundLoadingAllowed;
	}

	//INIT...
	__initControlLevel()
	{
		super.__initControlLevel();
		
		this._fVueApplicationController_vac.on(VueApplicationController.EVENT_ON_PAYTABLE_CLOSE_BUTTON_CLICKED, this._onPaytableCloseButtonClicked, this);
		this._fVueApplicationController_vac.on(VueApplicationController.EVENT_ON_PAYTABLE_PRINTABLE_RULES_BUTTON_CLICKED, this._onPaytablePrintableRulesButtonClicked, this);

		this._fIsBGLoading_bl && this._fSubloadingController_sc.on(SubloadingController.EVENT_ON_LOADING_COMPLETED, this._onLoaded, this);
		APP.once(CrashAPP.EVENT_ON_GAME_STARTED, this._onGameStarted, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CRASH_GAME_INFO_MESSAGE, this._onGameInfoMessage, this);
	}
	//...INIT

	_onGameStarted()
	{
		APP.webSocketInteractionController.once(GameWebSocketInteractionController.EVENT_ON_SERVER_ENTER_GAME_MESSAGE, this._onServerEnterGameMessage, this);

		if (!this._fIsBGLoading_bl)
		{
			this._fVueApplicationController_vac.addDOMScreen();
		}
	}

	_onLoaded(aEvent_e)
	{
		if (aEvent_e.assetsType === SUBLOADING_ASSETS_TYPES.paytable)
		{
			this._fVueApplicationController_vac.addDOMScreen();
			this._fIsWaitingToShowScreen && this._showScreen();
		}
	}

	_createPrintablePath()
	{
		let lAppPathUrl_str = APP.appParamsInfo.gamePath || APP.applicationFolderURL;
		let lCustomerFolder_str = APP.appParamsInfo.customerId || "_standard";
		let lLocalizationFolder_str = "en";//I18.currentLocale; /*TODO: use I18.currentLocale instead of fixed 'en' value when localisations for printable rules will be supported*/
		
		let lRulesName = APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode ? 'TripleMaxBlast-GameRules' : 'TripleCashOrCrash-GameRules';
		if (APP.isBattlegroundGame)
		{
			lRulesName = 'MaxBlastChamps-GameRules';
		}

		if (lAppPathUrl_str)
		{
			this._fPrintableRulesPath_str = `${lAppPathUrl_str}assets/rules/${lCustomerFolder_str}/${lLocalizationFolder_str}/${lRulesName}.pdf`;
		}
	}

	_onServerEnterGameMessage(event)
	{
		this._createPrintablePath();
	}

	_onGameInfoMessage(event)
	{
		let data = event.messageData;
		this._fPaytableData_obj = {
			"maxWin" : data.maxPlayerProfitInRound || 0,
			"maxTotalWin" : data.totalPlayersProfitInRound || 0,
			"maxMultiplier" : data.maxMultiplier || 0,
			"rakePercent" : data.rakePercent || 0,
			"maxPlayers" : data.maxRoomPlayers || 100
		};
		this.emit(PaytableScreenController.EVENT_ON_PAYTABLE_DATA_READY);
	}

	_showScreen()
	{
		if (this._fIsBGLoading_bl && !this._fSubloadingController_sc.i_isLoaded(SUBLOADING_ASSETS_TYPES.paytable)) 
		{
			this._fSubloadingController_sc.i_showLoadingScreen();
			this._fIsWaitingToShowScreen = true;
			return;
		}

		this._fSubloadingController_sc && this._fSubloadingController_sc.i_hideLoadingScreen();

		this._fIsWaitingToShowScreen = false;
		this._fIsScreenShown_bl = true;

		this.emit(PaytableScreenController.EVENT_ON_SCREEN_SHOW);
	}

	_hideSceeen()
	{
		if (this._fIsBGLoading_bl)
		{
			this._fSubloadingController_sc.i_hideLoadingScreen();
			this._fIsWaitingToShowScreen = false;
		}

		if (this._isScreenShown)
		{
			this.emit(PaytableScreenController.EVENT_ON_SCREEN_HIDE);
		}

		this._fIsScreenShown_bl = false;
	}

	_goToPrintableRules(event)
	{
		if (!this._fPrintableRulesPath_str)
		{
			return;
		}
		window.open(this._fPrintableRulesPath_str);
	}

	_onPaytableCloseButtonClicked(event)
	{
		this.emit(PaytableScreenController.EVENT_ON_CLOSE_BTN_CLICKED);
	}

	_onPaytablePrintableRulesButtonClicked(event)
	{
		this._goToPrintableRules();
	}

	get _isScreenShown()
	{
		return this._fIsScreenShown_bl;
	}

	destroy()
	{
		super.destroy();
	}
}

export default PaytableScreenController