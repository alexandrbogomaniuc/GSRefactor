import SimpleUIController from '../../../../../../unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbyVueApplicationController from '../../../../vue/GUSLobbyVueApplicationController';
import GUSLobbyApplication from '../../../../main/GUSLobbyApplication';
import GUSLobbySubloadingController from '../../../../subloading/GUSLobbySubloadingController';
import { SUBLOADING_ASSETS_TYPES } from '../../../../../model/subloading/GUSubloadingInfo';
import { LOBBY_MESSAGES } from '../../../../external/GUSLobbyExternalCommunicator';
import GUSLobbyBattlegroundController from '../../../../custom/battleground/GUSLobbyBattlegroundController';

const DEF_MAX_BULLETS_LIMIT_ON_MAP = 10;

class GUSLobbyPaytableScreenController extends SimpleUIController
{
	static get EVENT_ON_CLOSE_BTN_CLICKED()		{ return 'onCloseBtnClicked'; }
	static get EVENT_ON_PAYTABLE_DATA_READY()	{ return 'EVENT_ON_PAYTABLE_DATA_READY'; }
	static get EVENT_ON_SCREEN_SHOW()			{ return "onScreenShow"; }
	static get EVENT_ON_SCREEN_HIDE()			{ return "onScreenHide"; }
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

		this.emit(GUSLobbyPaytableScreenController.EVENT_ON_PAYTABLE_DATA_READY);
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

		this._fIsBGLoading_bl = APP.appParamsInfo.backgroundLoadingAllowed && !!SUBLOADING_ASSETS_TYPES.paytable;
	}

	//INIT...
	__initControlLevel()
	{
		super.__initControlLevel();

		this._fVueApplicationController_vac.on(GUSLobbyVueApplicationController.EVENT_ON_PAYTABLE_CLOSE_BUTTON_CLICKED, this._onPaytableCloseButtonClicked, this);
		this._fVueApplicationController_vac.on(GUSLobbyVueApplicationController.EVENT_ON_PAYTABLE_PRINTABLE_RULES_BUTTON_CLICKED, this._onPaytablePrintableRulesButtonClicked, this);

		this._fIsBGLoading_bl && this._fSubloadingController_sc.on(GUSLobbySubloadingController.EVENT_ON_LOADING_COMPLETED, this._onLoaded, this);
		APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);

		APP.on(GUSLobbyApplication.EVENT_ON_SOME_BONUS_STATE_CHANGED, this._onSomeBonusStateChanged, this);

		if (APP.isBattlegroundGame)
		{
			APP.battlegroundController.on(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_TIME_TO_START_UPDATED, this._activateTimer, this);
		}
	}
	//...INIT

	_onSomeBonusStateChanged()
	{
		this._createPrintablePath();
	}

	_onLobbyStarted()
	{
		APP.webSocketInteractionController.once(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

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
		let lCustomerFolder_str = APP.appParamsInfo.customerId || "_standard";
		let lLocalizationFolder_str = "en";//I18.currentLocale; /*TODO: use I18.currentLocale instead of fixed 'en' value when localisations for printable rules will be supported*/
		let lModeFolder_str = "regular";

		if (APP.FRBController.info.isActivated || APP.lobbyBonusController.info.isActivated || APP.tournamentModeController.info.isTournamentMode)
		{
			lModeFolder_str = "bonus";
		}

		let lRulesName = 'MAXDUEL-GameRules';
		if (APP.isMobile)
		{
			lRulesName += "_mobile";
		}

		if (APP.playerController.info.isDisableAutofiring)
		{
			lRulesName += "_disableAF";
		}

		if (lAppPathUrl_str)
		{
			this._fPrintableRulesPath_str = `${lAppPathUrl_str}assets/rules/${lCustomerFolder_str}/${lLocalizationFolder_str}/${lModeFolder_str}/${lRulesName}.pdf`;
		}
	}

	_onServerEnterLobbyMessage()
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
		this.isActiveScreen = true;

		this._fSubloadingController_sc && this._fSubloadingController_sc.i_hideLoadingScreen();

		this._fIsWaitingToShowScreen = false;
		this._fIsScreenShown_bl = true;

		this.emit(GUSLobbyPaytableScreenController.EVENT_ON_SCREEN_SHOW);

		if (APP.isBattlegroundGame)
		{
			if (
					APP.battlegroundController.isSecondaryScreenTimerRequired()
					&& !APP.dialogsController.lobbyBattlegroundNotEnoughPlayersDialogController.info.isActive
				)
			{
				APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_COUNTDOWN_HIDE);
			}

			this._activateTimer();
		}
	}

	_activateTimer()
	{
		if (!APP.isBattlegroundGame)
		{
			return;
		}

		if (APP.battlegroundController.isSecondaryScreenTimerRequired() && this._isScreenShown)
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

			this.emit(GUSLobbyPaytableScreenController.EVENT_ON_CONTINUE_READING_DIALOG_REQUIRED);
		}
	}

	get isActiveScreen()
	{
		return this._fIsActiveScreen;
	}

	set isActiveScreen(aVal_bl)
	{
		this._fIsActiveScreen = aVal_bl;
	}

	_hideSceeen()
	{
		this.isActiveScreen = false;
		clearInterval(this.interval);

		if (this._fIsBGLoading_bl)
		{
			this._fSubloadingController_sc.i_hideLoadingScreen();
			this._fIsWaitingToShowScreen = false;
		}

		if (this._isScreenShown)
		{
			this.emit(GUSLobbyPaytableScreenController.EVENT_ON_SCREEN_HIDE);
		}

		if (
				APP.isBattlegroundGame
				&& APP.battlegroundController.isSecondaryScreenTimerRequired() 
				&& !APP.dialogsController.lobbyBattlegroundNotEnoughPlayersDialogController.info.isActive
			)
		{
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_COUNTDOWN_SHOW);
		}

		this._fIsScreenShown_bl = false;
	}

	_goToPrintableRules()
	{
		if (!this._fPrintableRulesPath_str)
		{
			return;
		}
		window.open(this._fPrintableRulesPath_str);
	}

	_onPaytableCloseButtonClicked()
	{
		this.emit(GUSLobbyPaytableScreenController.EVENT_ON_CLOSE_BTN_CLICKED);
	}

	_onPaytablePrintableRulesButtonClicked()
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

export default GUSLobbyPaytableScreenController