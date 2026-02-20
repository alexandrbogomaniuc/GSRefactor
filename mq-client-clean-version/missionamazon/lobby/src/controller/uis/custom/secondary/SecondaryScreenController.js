import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import SecondaryScreenView from '../../../../view/uis/custom/secondary/SecondaryScreenView';
import SettingsScreenController from './settings/SettingsScreenController';
import PaytableScreenController from './paytable/PaytableScreenController';
import EditProfileScreenController from './edit_profile/EditProfileScreenController'
import CommonPanelController from '../commonpanel/CommonPanelController'
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyAPP from '../../../../LobbyAPP';
import LobbyStateInfo from '../../../../model/state/LobbyStateInfo'
import LobbyWebSocketInteractionController from '../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyExternalCommunicator from '../../../../external/LobbyExternalCommunicator';
import {GAME_MESSAGES} from '../../../../external/LobbyExternalCommunicator';
import FRBController from '../../../custom/frb/FRBController';
import TournamentModeController from '../../../custom/tournament/TournamentModeController';
import GameBattlegroundContinueReadingDialogController from '../dialogs/custom/game/GameBattlegroundContinueReadingDialogController';

class SecondaryScreenController extends SimpleUIController
{
	static get EVENT_SCREEN_ACTIVATED()
	{
		return "onSecondaryScreenActivated";
	}

	static get EVENT_SCREEN_SHOWED()
	{
		return "onSecondaryScreenShowed";
	}

	static get EVENT_SCREEN_DEACTIVATED()
	{
		return "onSecondaryScreenDeactivated";
	}

	init(aViewParentContainer_sprt)
	{
		this._fViewParentContainer_sprt = aViewParentContainer_sprt;

		super.init();
	}

	initView()
	{
		this._fSecondaryScreenView_ssv = new SecondaryScreenView(this._fViewParentContainer_sprt)
		this._initView(this._fSecondaryScreenView_ssv);
	}

	get secondaryScreenView()
	{
		return this._fSecondaryScreenView_ssv;
	}

	get settingsScreenController()
	{
		return this._settingsScreenController;
	}

	get paytableScreenController()
	{
		return this._paytableScreenController;
	}

	get editProfileScreenController()
	{
		return this._editProfileScreenController;
	}

	//INIT...
	constructor(...args)
	{
		super(...args);

		this._fViewParentContainer_sprt = null;
		this._fSettingsScreenController_ssc = null;
		this._fPaytableScreenController_psc = null;
		this._fEditProfileScreenController_epsc = null;

		this._fCurrentScreenController_ssc = null;
		this._fTournamentModeInfo_tmi = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._settingsScreenController.init();
		this._paytableScreenController.init();
		this._editProfileScreenController.init();

		this._addEventListeners();
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_ssv = this.view;

		this._settingsScreenController.initView(lView_ssv.settingsScreenView);
		this._settingsScreenController.hideScreen();
		this._paytableScreenController.hideScreen();
		this._editProfileScreenController.initView(lView_ssv.editProfileScreenView);
		this._editProfileScreenController.hideScreen();
	}
	//...INIT

	_addEventListeners()
	{
		this._settingsScreenController.on(SettingsScreenController.EVENT_ON_OK_BUTTON_CLICKED, this._onSettingsScreenOkClicked, this);
		this._editProfileScreenController.on(EditProfileScreenController.EVENT_ON_CANCEL_BUTTON_CLICKED, this._onEditProfileScreenCancelClicked, this);
		this._editProfileScreenController.on(EditProfileScreenController.EVENT_ON_CLOSE_SCREEN, this._onEditProfileClose, this);

		APP.on(LobbyAPP.EVENT_ON_SETTINGS_BUTTON_CLICKED, this._onSettingsButtonClicked, this);
		APP.on(LobbyAPP.EVENT_ON_INFO_BUTTON_CLICKED, this._onInfoButtonClicked, this);
		APP.on(LobbyAPP.EVENT_ON_PROFILE_BUTTON_CLICKED, this._onProfileButtonClicked, this);
		APP.on(LobbyAPP.EVENT_ON_CLOSE_SCREEN_BUTTON_CLICKED, this._onCloseButtonClicked, this);
		APP.on(LobbyAPP.EVENT_ON_SOME_BONUS_STATE_CHANGED, this._onSomeBonusStateChanged, this);

		this._settingsScreenController.on(SettingsScreenController.EVENT_ON_CLOSE_BTN_CLICKED, this._onCloseButtonClicked, this);
		this._settingsScreenController.on(SettingsScreenController.EVENT_SCREEN_DEACTIVATED, this._hideSettingsScreenAccordingToRoundStart, this);

		this._paytableScreenController.on(PaytableScreenController.EVENT_ON_CLOSE_BTN_CLICKED, this._onCloseButtonClicked, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		APP.externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameExternalMessageReceived.bind(this));

		APP.commonPanelController.on(CommonPanelController.EVENT_BACK_TO_LOBBY_BUTTON_CLICKED, this._onCommonPanelBackToLobbyClicked, this);
		APP.commonPanelController.on(CommonPanelController.EVENT_FIRE_SETTINGS_BUTTON_CLICKED, this._onCommonPanelFireSettingsClicked, this);

		APP.FRBController.on(FRBController.EVENT_FRB_RESTART_REQUIRED, this._onFRBRestartRequired, this);

		APP.dialogsController.gameBattlegroundContinueReadingDialogController.on(
			GameBattlegroundContinueReadingDialogController.EVENT_BATTLEGROUND_CONTINUE_READING_INFO_CLICKED, this._hideSceeen, this
			);

		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);
	}

	_onFRBRestartRequired(event)
	{
		this._hideSceeen();
	}

	_onCommonPanelBackToLobbyClicked()
	{
		this._hideSceeen();
	}

	_onCommonPanelFireSettingsClicked()
	{
		this._hideSceeen();
	}

	_onSettingsScreenOkClicked(event)
	{
		this._hideSceeen();
	}

	_onEditProfileScreenCancelClicked(event)
	{
		this._hideSceeen();
	}

	_onEditProfileClose()
	{
		this._hideSceeen();
	}

	_onSettingsButtonClicked(event)
	{
		this._showScreen(this._settingsScreenController);
	}

	_onInfoButtonClicked(event)
	{
		this._showScreen(this._paytableScreenController);
	}

	_onProfileButtonClicked(event)
	{
		this._showScreen(this._editProfileScreenController);
	}

	_onCloseButtonClicked(event)
	{
		this._hideSceeen(true);
	}

	_onLobbyServerConnectionClosed(event)
	{
		if (event.wasClean)
		{
			return;
		}

		this._hideSceeen(true);
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;

		switch (serverData.code)
		{
			case LobbyWebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN:
				this._hideSceeen(true);
				break;
		}
	}

	_onSomeBonusStateChanged()
	{
		this._hideSceeen();
	}

	_onGameExternalMessageReceived(event)
	{
		let msgType = event.type;
		switch (msgType)
		{
			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
				this._hideSceeen(true);
				break;
			case GAME_MESSAGES.BATTLEGROUND_ROUND_CANCELED:
			case GAME_MESSAGES.ROUND_RESULT_ACTIVATED:
				this._hideSceeen(true);
				break;
		}
	}

	//MENU BUTTONS...
	_onMenuInfoClicked(event)
	{
		this._showScreen(this._paytableScreenController);
	}

	_onMenuProfileClicked(event)
	{
		this._showScreen(this._editProfileScreenController);
	}

	_onMenuSettingsClicked(event)
	{
		this._showScreen(this._settingsScreenController);
	}

	_onMenuCloseClicked(event)
	{
		this._hideSceeen(true);
	}

	_onTournamentModeStateChanged(event)
	{
		let l_cpv = this.view;
		let lTournamentModeInfo_tmi = this._fTournamentModeInfo_tmi;

		if (lTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			this._hideSceeen(true);
		}
	}
	//...MENU BUTTONS

	_showScreen(aScreenController_ssc)
	{
		if (!aScreenController_ssc || this._fCurrentScreenController_ssc === aScreenController_ssc)
		{
			return;
		}

		this._hideCurrentScreen();
		aScreenController_ssc.showScreen();
		this._fCurrentScreenController_ssc = aScreenController_ssc;

		this.emit(SecondaryScreenController.EVENT_SCREEN_SHOWED, {screenId: this._getCurrentScreenId()});

		let lView_ssv = this.view;
		if (lView_ssv)
		{
			if (!lView_ssv.visible)
			{
				lView_ssv.visible = true;
				this.emit(SecondaryScreenController.EVENT_SCREEN_ACTIVATED);
			}
		}
	}

	_hideSceeen(aOptCancelChangesOnScreen_bl)
	{
		this._hideCurrentScreen(aOptCancelChangesOnScreen_bl);
		let lView_ssv = this.view;
		if (lView_ssv && lView_ssv.visible)
		{
			lView_ssv.visible = false;
			this.emit(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED);
		}
	}

	_hideCurrentScreen(aOptCancelChangesOnScreen_bl = false)
	{
		if (this._fCurrentScreenController_ssc)
		{
			if (aOptCancelChangesOnScreen_bl && this._fCurrentScreenController_ssc.cancelChanges)
			{
				this._fCurrentScreenController_ssc.cancelChanges();
			}
			this._fCurrentScreenController_ssc.hideScreen();
			this._fCurrentScreenController_ssc = null;
		}
	}

	_hideSettingsScreenAccordingToRoundStart(event)
	{
		let lView_ssv = this.view;
		if (lView_ssv && lView_ssv.visible)
		{
			lView_ssv.visible = false;
			this.emit(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, event);
			this._fCurrentScreenController_ssc = null;
		}
	}

	hideAllScreenButtons()
	{
		this.view.hideAllScreenButtons();
	}

	_getCurrentScreenId()
	{
		let lId_str = LobbyStateInfo.SCREEN_NONE;

		if (this._fCurrentScreenController_ssc instanceof SettingsScreenController) lId_str = LobbyStateInfo.SCREEN_SETTINGS;
		else if (this._fCurrentScreenController_ssc instanceof PaytableScreenController) lId_str = LobbyStateInfo.SCREEN_PAYTABLE;
		else if (this._fCurrentScreenController_ssc instanceof EditProfileScreenController) lId_str = LobbyStateInfo.SCREEN_PROFILE;

		return lId_str;
	}

	//SCREENS...
	get _settingsScreenController()
	{
		return this._fSettingsScreenController_ssc || (this._fSettingsScreenController_ssc = new SettingsScreenController());
	}

	get _paytableScreenController()
	{
		return this._fPaytableScreenController_psc || (this._fPaytableScreenController_psc = this._initPaytableScreenController());
	}

	_initPaytableScreenController()
	{
		//both the desktop and mobile payout table are made on Vue

		return new PaytableScreenController();
	}

	get _editProfileScreenController()
	{
		return this._fEditProfileScreenController_epsc || (this._fEditProfileScreenController_epsc = new EditProfileScreenController());
	}
	//...SCREENS
}

export default SecondaryScreenController