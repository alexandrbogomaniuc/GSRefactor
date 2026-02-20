import SimpleUIController from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyExternalCommunicator from '../../../../../external/LobbyExternalCommunicator';
import {LOBBY_MESSAGES} from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import VueApplicationController from '../../../../../vue/VueApplicationController';
import LobbyAPP from '../../../../../LobbyAPP';
import SubloadingController from '../../../../subloading/SubloadingController';
import { SUBLOADING_ASSETS_TYPES } from '../../../../../config/Constants';
import PlayerInfo from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import DialogsInfo from '../../../../../model/uis/custom/dialogs/DialogsInfo';
import BattlegroundController from '../../../../custom/battleground/BattlegroundController';

const DEF_MAX_BULLETS_LIMIT_ON_MAP = 10;

class PaytableScreenController extends SimpleUIController
{
	static get EVENT_ON_CLOSE_BTN_CLICKED()		{ return 'onCloseBtnClicked'; }
	static get EVENT_ON_PAYTABLE_DATA_READY() 	{ return 'EVENT_ON_PAYTABLE_DATA_READY'; }
	static get EVENT_ON_SCREEN_SHOW()			{ return "onScreenShow"; }
	static get EVENT_ON_SCREEN_HIDE()			{ return "onScreenHide"; }
	static get EVENT_ON_WEAPON_PAID_MULTIPLIER_UPDATED()			{ return "EVENT_ON_WEAPON_PAID_MULTIPLIER_UPDATED"; }
	static get EVENT_ON_CONTINUE_READING_DIALOG_REQUIRED() 			{ return "EVENT_ON_CONTINUE_READING_DIALOG_REQUIRED"; }

	showScreen()
	{
		this._showScreen();
	}

	hideScreen()
	{
		this._hideSceeen();
	}

	onPaytableResponse(data)
	{
		this._fPaytableData_obj = data.paytable;
		this._fPaytableData_obj.maxBulletsLimitOnMap = data.maxBulletsLimitOnMap || DEF_MAX_BULLETS_LIMIT_ON_MAP;

		this.emit(PaytableScreenController.EVENT_ON_PAYTABLE_DATA_READY);
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

	get isActiveScreen()
	{
		return this._fIsScreenShown_bl;
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

		APP.battlegroundController.on(BattlegroundController.EVENT_BATTLEGROUND_TIME_TO_START_UPDATED, this._activateTimer, this);

		APP.once(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);

		APP.on(LobbyAPP.EVENT_ON_SOME_BONUS_STATE_CHANGED, this._onSomeBonusStateChanged, this);
		APP.on(LobbyAPP.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
	}
	//...INIT

	_onPlayerInfoUpdated(e)
	{
		if (e.data[PlayerInfo.KEY_WEAPON_COST_MULTIPLIERS])
		{
			this.emit(PaytableScreenController.EVENT_ON_WEAPON_PAID_MULTIPLIER_UPDATED, {weaponPaidMultiplier: e.data[PlayerInfo.KEY_WEAPON_COST_MULTIPLIERS]});
		}
	}

	_onSomeBonusStateChanged()
	{
		this._createPrintablePath();
	}

	_onLobbyStarted()
	{
		APP.webSocketInteractionController.once(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

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
		let lAppPathUrl_str = APP.appParamsInfo.lobbyPath || APP.applicationFolderURL;
		let lLocalizationFolder_str = "en";//I18.currentLocale; /*TODO: use I18.currentLocale instead of fixed 'en' value when localisations for printable rules will be supported*/
		let lFileName_str = 'MAXDUEL-GameRules';

		if (APP.isBattlegroundGame)
		{
			lFileName_str = 'MAXDUEL-Battleground-GameRules';
		}

		if (lAppPathUrl_str)
		{
			this._fPrintableRulesPath_str = `${lAppPathUrl_str}assets/rules/${lLocalizationFolder_str}/${lFileName_str}.pdf`;
		}
	}

	_onServerEnterLobbyMessage(event)
	{
		this._createPrintablePath();
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

		if( APP.battlegroundController.isSecondaryScreenTimerRequired()
			&&
			!APP.dialogsController.info.hasActiveDialogWithId(DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS)
			)
		{
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_COUNTDOWN_HIDE);
		}

		this._activateTimer();
	}

	_activateTimer()
	{
		if(APP.battlegroundController.isSecondaryScreenTimerRequired() && this._isScreenShown)
		{
			clearInterval(this.interval);

			this.interval = setInterval(this._tick.bind(this), 100);
		}
	}

	_tick()
	{
		if (APP.battlegroundController.info.getTimeToStartInMillis() <= 5500)
		{
			clearInterval(this.interval);
			this.emit(PaytableScreenController.EVENT_ON_CONTINUE_READING_DIALOG_REQUIRED);
		}
	}

	_hideSceeen()
	{
		clearInterval(this.interval);

		if (this._fIsBGLoading_bl)
		{
			this._fSubloadingController_sc.i_hideLoadingScreen();
			this._fIsWaitingToShowScreen = false;
		}

		if (this._isScreenShown)
		{
			this.emit(PaytableScreenController.EVENT_ON_SCREEN_HIDE);
		}

		if( APP.battlegroundController.isSecondaryScreenTimerRequired() 
			&& !APP.dialogsController.info.hasActiveDialogWithId(DialogsInfo.DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS)
			)
		{
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_COUNTDOWN_SHOW);
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