import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import CommonPanelView from '../../../../view/uis/custom/commonpanel/CommonPanelView';
import CommonPanelInfo from '../../../../model/uis/custom/commonpanel/CommonPanelInfo';
import LobbyWebSocketInteractionController from '../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyPlayerController from '../../../custom/LobbyPlayerController';
import LobbySoundButtonController from '../secondary/LobbySoundButtonController';
import DialogController from '../dialogs/DialogController';
import LobbyExternalCommunicator from '../../../../external/LobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../external/LobbyExternalCommunicator';
import LobbyStateController from '../../../state/LobbyStateController';
import LobbyApp from '../../../../LobbyAPP';
import LobbyWeaponsPanelController from '../../../../controller/custom/LobbyWeaponsPanelController';
import FRBController from '../../../custom/frb/FRBController';
import TournamentModeController from '../../../custom/tournament/TournamentModeController';
import PlayerInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import { GAME_CLIENT_MESSAGES } from '../../../../model/interaction/server/LobbyWebSocketInteractionInfo';

class CommonPanelController extends SimpleUIController
{
	static get EVENT_TIME_SYNC_REQUEST()			{ return CommonPanelView.EVENT_TIME_SYNC_REQUEST; }
	static get EVENT_REFRESH_BALANCE_REQUEST()		{ return CommonPanelView.EVENT_REFRESH_BALANCE_REQUEST; }

	static get EVENT_FIRE_SETTINGS_BUTTON_CLICKED()	{ return CommonPanelView.EVENT_FIRE_SETTINGS_BUTTON_CLICKED; }
	static get EVENT_EDIT_PROFILE_BUTTON_CLICKED()	{ return CommonPanelView.EVENT_EDIT_PROFILE_BUTTON_CLICKED; }
	static get EVENT_SETTINGS_BUTTON_CLICKED()		{ return CommonPanelView.EVENT_SETTINGS_BUTTON_CLICKED; }
	static get EVENT_BACK_TO_LOBBY_BUTTON_CLICKED()	{ return CommonPanelView.EVENT_BACK_TO_LOBBY_BUTTON_CLICKED; }
	static get EVENT_INFO_BUTTON_CLICKED()			{ return CommonPanelView.EVENT_INFO_BUTTON_CLICKED; }

	static get EVENT_GROUP_BUTTONS_CHANGED()		{ return CommonPanelView.EVENT_GROUP_BUTTONS_CHANGED; }

	static get LAYER_ID_MOBILE_BUTTONS()			{ return CommonPanelView.LAYER_ID_MOBILE_BUTTONS; }
	static get LAYER_ID_FIRE_SETTINGS()				{ return CommonPanelView.LAYER_ID_FIRE_SETTINGS; }

	static getCommonPanelSize(aIsMobile_bl = false)
	{
		return aIsMobile_bl ? CommonPanelView.COMMON_PANEL_SIZE_MOBILE : CommonPanelView.COMMON_PANEL_SIZE_DESKTOP;
	}

	static getCommonPanelLayers(aIsMobile_bl = false)
	{
		if (aIsMobile_bl)
		{
			return CommonPanelView.MOBILE_COMMON_PANEL_LAYERS;
		}

		return CommonPanelView.COMMON_PANEL_LAYERS;
	}

	onSwitchToLobby()
	{
	}

	get lobbyWeaponsPanelController()
	{
		return this._lobbyWeaponsPanelController;
	}

	//INIT...
	constructor()
	{
		super(new CommonPanelInfo(), new CommonPanelView());

		this._fSoundButtonController_sbc = null;
		this._fLobbyWeaponsPanelController_lwpc = null;

		this._fReturnToGameDialogController_rtgdc = null;
		this._fReturnToGameDialogInfo_rtgdi = null;

		this._fTournamentModeInfo_tmi = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let lInfo_cpi = this.info;
		let lAppParamsInfo_apppi = APP.appParamsInfo;

		if (lAppParamsInfo_apppi.historyFunName)
		{
			lInfo_cpi.historyCallback = lAppParamsInfo_apppi.historyFunName;
		}

		if (lAppParamsInfo_apppi.homeFuncName)
		{
			lInfo_cpi.homeCallback = lAppParamsInfo_apppi.homeFuncName;
		}

		if (lAppParamsInfo_apppi.timerFrequency)
		{
			lInfo_cpi.timerFrequency = lAppParamsInfo_apppi.timerFrequency;
		}

		if (lAppParamsInfo_apppi.timerOffset)
		{
			lInfo_cpi.timeOffset = lAppParamsInfo_apppi.timerOffset;
		}

		this._soundButtonController.init();

		this._fPlayerController_lpc = APP.playerController;
		this._fPlayerController_lpc.on(LobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		this._fPlayerInfo_lpi = this._fPlayerController_lpc.info;

		this._lobbyWeaponsPanelController.i_init();

		let lLobbyStateController_lsc = APP.lobbyStateController;
		lLobbyStateController_lsc.on(LobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyVisibilityChanged, this);

		this._fReturnToGameDialogController_rtgdc = APP.dialogsController.returnToGameDialogController;
		this._fReturnToGameDialogController_rtgdc.on(DialogController.EVENT_PRESENTED_DIALOG_UPDATED, this._onPresentedDialogUpdated, this);
		this._fReturnToGameDialogInfo_rtgdi = this._fReturnToGameDialogController_rtgdc.i_getInfo();

		this._fFRBDialogController_frbdc = APP.dialogsController.FRBDialogController;
		this._fFRBDialogController_frbdc.on(DialogController.EVENT_PRESENTED_DIALOG_UPDATED, this._onPresentedDialogUpdated, this);
		this._fFRBDialogInfo_frbdi = this._fFRBDialogController_frbdc.i_getInfo();

		APP.externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		APP.on(LobbyApp.EVENT_ON_LOBBY_ASSETS_LOADING_ERROR, this._onLobbyAssetsLoadingError, this);
		APP.on(LobbyApp.EVENT_ON_WEBGL_CONTEXT_LOST, this._onLobbyWebglContextLost, this);
		APP.on(LobbyApp.EVENT_ON_PLAYER_INFO_UPDATED, this._onRoomPlayerInfoUpdated, this);

		APP.FRBController.on(FRBController.EVENT_FRB_LOBBY_INTRO_CONFIRMED, this._onFrbLobbyIntroConfirmed, this);
		APP.FRBController.on(FRBController.EVENT_ON_REFRESH_BALANCE_VALIDATE_REQUIRED, this._onRefreshBalanceValidateRequired, this);

		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);
	}

	_onLobbyAssetsLoadingError(event)
	{
		let lView_cpv = this.view;
		if (lView_cpv)
		{
			lView_cpv.off(CommonPanelView.EVENT_TIME_SYNC_REQUEST, this.emit, this);
			lView_cpv.off(CommonPanelView.EVENT_REFRESH_BALANCE_REQUEST, this.emit, this);
			this._fRefreshActive_bln = false;
		}
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_cpv = this.view;
		lView_cpv.on(CommonPanelView.EVENT_HISTORY_BUTTON_CLICKED, this._onHistoryButtonClicked, this);
		lView_cpv.on(CommonPanelView.EVENT_HOME_BUTTON_CLICKED, this._onHomeButtonClicked, this);
		lView_cpv.on(CommonPanelView.EVENT_TIME_SYNC_REQUEST, this.emit, this);
		lView_cpv.on(CommonPanelView.EVENT_REFRESH_BALANCE_REQUEST, this.emit, this);
		this._fRefreshActive_bln = true;

		lView_cpv.on(CommonPanelView.EVENT_FIRE_SETTINGS_BUTTON_CLICKED, this.emit, this);
		lView_cpv.on(CommonPanelView.EVENT_EDIT_PROFILE_BUTTON_CLICKED, this.emit, this);
		lView_cpv.on(CommonPanelView.EVENT_SETTINGS_BUTTON_CLICKED, this.emit, this);
		lView_cpv.on(CommonPanelView.EVENT_BACK_TO_LOBBY_BUTTON_CLICKED, this.emit, this);
		lView_cpv.on(CommonPanelView.EVENT_INFO_BUTTON_CLICKED, this.emit, this);

		lView_cpv.on(CommonPanelView.EVENT_GROUP_BUTTONS_CHANGED, this.emit, this);

		this._soundButtonController.initView(lView_cpv.soundButtonView);

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
	}

	__validateViewLevel()
	{
		super.__validateViewLevel.call(this);
		this.view.updateUI();
	}

	get _soundButtonController()
	{
		return this._fSoundButtonController_sbc || (this._fSoundButtonController_sbc = new LobbySoundButtonController());
	}

	get _lobbyWeaponsPanelController()
	{
		return this._fLobbyWeaponsPanelController_lwpc || (this._fLobbyWeaponsPanelController_lwpc = new LobbyWeaponsPanelController());
	}
	//...INIT

	//INTERFACE...
	initView(panelStages_st_arr)
	{
		this.view.init(panelStages_st_arr);
	}

	turnGameUI(aState_bl)
	{
		this.info.gameUIVisible = aState_bl;
		this.__validate();
	}
	//...INTERFACE

	_onPresentedDialogUpdated()
	{
		this.info.isDialogActive = this._fReturnToGameDialogInfo_rtgdi.isActive || this._fFRBDialogInfo_frbdi.isActive && this._fFRBDialogInfo_frbdi.isLobbyIntroType;
		this.__validate();
	}

	_onFrbLobbyIntroConfirmed(event)
	{
		if (!APP.lobbyScreen.gameInitiated)
		{
			this.view.applyFrbGameLoadingView();
		}
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		let data = event.data;
		let l_cpv = this.view;
		switch (msgType)
		{
			case GAME_MESSAGES.GAME_STARTED:
				{
					this.view.cancelFrbGameLoadingView();
				}
				break;
			case GAME_MESSAGES.FIRE_SETTINGS_STATE_CHANGED:
				if (l_cpv)
				{
					l_cpv.onFireSettingsStateChanged(data.isActive);
				}
			break;

			case GAME_MESSAGES.GAME_LOADING_ERROR:
				if (l_cpv)
				{
					l_cpv.off(CommonPanelView.EVENT_TIME_SYNC_REQUEST, this.emit, this);
					l_cpv.off(CommonPanelView.EVENT_REFRESH_BALANCE_REQUEST, this.emit, this);
					this._fRefreshActive_bln = false;
				}
			break;

			case GAME_MESSAGES.WEBGL_CONTEXT_LOST:
				this.info.isWebGlContextLost = true;
				if (l_cpv)
				{
					l_cpv.off(CommonPanelView.EVENT_TIME_SYNC_REQUEST, this.emit, this);
					l_cpv.off(CommonPanelView.EVENT_REFRESH_BALANCE_REQUEST, this.emit, this);
					this._fRefreshActive_bln = false;

					l_cpv.updateUI();
				}
			break;

			case GAME_MESSAGES.ON_SIT_OUT_REQUIRED:
				this._updateSitInStateIfRequired(false);
				break;

			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (data.requestClass === GAME_CLIENT_MESSAGES.SIT_OUT)
				{
					let lNewIsRoomPlayerSatIn_bl = !APP.playerController.info.isObserver;
					this._updateSitInStateIfRequired(lNewIsRoomPlayerSatIn_bl);
				}
				break;

			case GAME_MESSAGES.REFRESH_COMMON_PANEL_REQUIRED:
				if (this.info.gameUIVisible)
				{
					this._refreshUI(data.data, !APP.isBattlegroundGame);
				}
				break;
		}
	}

	_onRoomPlayerInfoUpdated(aEvent_e)
	{
		if (aEvent_e.data[PlayerInfo.KEY_SEATID] !== undefined)
		{
			let lNewIsRoomPlayerSatIn_bl = !APP.playerController.info.isObserver;
			this._updateSitInStateIfRequired(lNewIsRoomPlayerSatIn_bl);
		}

		if (aEvent_e.data[PlayerInfo.IS_BATTLGROUND_BUYIN_CONFIRMED] !== undefined)
		{
			this.__validate();
		}
	}

	_updateSitInStateIfRequired(aIsSitIn_bl)
	{
		if (this.info.isPlayerSatInDefined && aIsSitIn_bl === this.info.playerSatIn)
		{
			return;
		}

		this._updateSitInState(aIsSitIn_bl);
	}

	_updateSitInState(aIsSitIn_bl)
	{
		this.info.playerSatIn = aIsSitIn_bl;
		
		this.__validate();
	}

	_onLobbyWebglContextLost(event)
	{
		this.info.isWebGlContextLost = true;

		let l_cpv = this.view;

		l_cpv && l_cpv.updateUI();
	}

	onTimeSyncResponseReceived(data)
	{
		this.info.timeFromServer = data.date;
		this.view.onTimeSyncResponseReceived();
	}

	get soundButtonController()
	{
		return this._soundButtonController;
	}
	//...INTERFACE

	//IMPLEMENTATION...
	_onServerEnterLobbyMessage(aEvent_obj)
	{
		this.view.initBalanceRefreshTimer();
	}

	_onPlayerInfoUpdated(event)
	{
		this._refreshUI(event.data, !this.info.gameUIVisible);
	}

	_onLobbyVisibilityChanged(event)
	{
		if (event.visible)
		{
			// clear win field
			this._refreshUI({win: {value: 0}}, false);
		}
	}

	_refreshUI(aData_obj, aOptUpdBalance_bln = true)
	{
		let lUpdatedElements_int = 0;
		var lInfo_cpi = this.info;

		if (aOptUpdBalance_bln && aData_obj.balance && aData_obj.balance.value !== lInfo_cpi.gameBalance)
		{
			lUpdatedElements_int++;
			lInfo_cpi.gameBalance = aData_obj.balance.value;
			lInfo_cpi.gameIndicatorsUpdateTime = aData_obj.duration;
		}
		if (aData_obj.win && aData_obj.win.value !== lInfo_cpi.gameWin)
		{
			lUpdatedElements_int++;
			lInfo_cpi.gameWin = aData_obj.win.value;
			lInfo_cpi.gameIndicatorsUpdateTime = aData_obj.duration;
		}
		if (aData_obj.refreshBalanceAvailable && aData_obj.refreshBalanceAvailable.value !== lInfo_cpi.refreshBalanceAvailable)
		{
			lUpdatedElements_int++;
			lInfo_cpi.refreshBalanceAvailable = aData_obj.refreshBalanceAvailable.value;
		}
		if (aData_obj.stake && aData_obj.stake.value !== lInfo_cpi.gameCostPerBullet)
		{
			lUpdatedElements_int++;
			lInfo_cpi.gameCostPerBullet = aData_obj.stake.value;
		}

		if (lUpdatedElements_int > 0)
		{
			this.__validate();
		}
	}

	_onHistoryButtonClicked(event)
	{
		let jsHistoryCallback = this.info.historyCallback;
		if (window[jsHistoryCallback])
		{
			window[jsHistoryCallback].apply(window);
		}
	}

	_onHomeButtonClicked(event)
	{
		APP.goToHome();
	}

	_onRefreshBalanceValidateRequired()
	{
		let l_cpv = this.view;
		let frbInfo = APP.FRBController.info;
		if (frbInfo.frbCompletionState)
		{
			l_cpv && l_cpv.off(CommonPanelView.EVENT_REFRESH_BALANCE_REQUEST, this.emit, this);
			this._fRefreshActive_bln = false;
		}
		else
		{
			if (!this._fRefreshActive_bln && l_cpv)
			{
				l_cpv.on(CommonPanelView.EVENT_REFRESH_BALANCE_REQUEST, this.emit, this);
				this._fRefreshActive_bln = true;
			}
		}
	}
	//TOURNAMENT...
	_onTournamentModeStateChanged(event)
	{
		let l_cpv = this.view;
		let lTournamentModeInfo_tmi = this._fTournamentModeInfo_tmi;
		
		if (lTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			if (l_cpv)
			{
				l_cpv.updateUI();
				
				l_cpv.off(CommonPanelView.EVENT_TIME_SYNC_REQUEST, this.emit, this);
				l_cpv.off(CommonPanelView.EVENT_REFRESH_BALANCE_REQUEST, this.emit, this);
				this._fRefreshActive_bln = false;
			}
		}
	}
	//...TOURNAMENT
	//...IMPLEMENTATION
}

export default CommonPanelController