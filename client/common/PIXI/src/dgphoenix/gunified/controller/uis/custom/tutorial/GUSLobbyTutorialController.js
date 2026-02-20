import SimpleUIController from '../../../../../unified/controller/uis/base/SimpleUIController';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES } from "../../../external/GUSLobbyExternalCommunicator";
import { APP } from '../../../../../unified/controller/main/globals';
import { FRAME_RATE } from '../../../../../unified/controller/time/Ticker';
import GUSLobbyTutorialView from '../../../../view/uis/tutorial/GUSLobbyTutorialView';
import GUSLobbySecondaryScreenController from "../secondary/GUSLobbySecondaryScreenController";
import GUSLobbyApplication from '../../../main/GUSLobbyApplication';
import GUSLobbyDialogsController from '../dialogs/GUSLobbyDialogsController';
import GUSLobbySettingsScreenController from '../secondary/settings/GUSLobbySettingsScreenController';
import GUSLobbyCommonPanelController from '../commonpanel/GUSLobbyCommonPanelController';
import GUSLobbyTooltipsController from '../tooltips/GUSLobbyTooltipsController';
import GUSettingsScreenController from "../secondary/settings/GUSettingsScreenController"

class GUSLobbyTutorialController extends SimpleUIController
{
	static get EVENT_ON_TIME_TO_SHOW_TUTORIAL_LAYER()	{ return "EVENT_ON_TIME_TO_SHOW_TUTORIAL_LAYER" }
	static get EVENT_ON_TIME_TO_HIDE_TUTORIAL_LAYER()	{ return "EVENT_ON_TIME_TO_HIDE_TUTORIAL_LAYER" }
	static get AUTOHIDE_TIME() 							{ return 10*30*FRAME_RATE }

	static get EVENT_ON_TUTORIAL_NEEDED_STATE_CHANGED()	{ return "EVENT_ON_TUTORIAL_NEEDED_STATE_CHANGED"}

	constructor(aOptInfo_si, aOptView_sv)
	{
		super(aOptInfo_si || null, aOptView_sv || new GUSLobbyTutorialView());

		this._fIsDoNotShowAgainChecked_bl = false;
		this._fHasTutorialBeenShown_bl = false;
		this._isFireSettingButtonClicked_bl = false;
		this._isInfoButtonClicked_bl = false;
		this._fObjectsData_obj = null;

		APP.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onMessageReceived, this);
		APP.secondaryScreenController.on(GUSLobbySecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._checkIfShowTutorialAgainRequired, this);
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.tooltipsController.on(GUSLobbyTooltipsController.EVENT_ON_TIPS_STATE_CHANGED, this._onTipsStateChanged, this)
		APP.commonPanelController.on(GUSLobbyCommonPanelController.EVENT_FIRE_SETTINGS_BUTTON_CLICKED, this._onFireSettingsButtonClicked, this)
		APP.on(GUSLobbyApplication.EVENT_ON_INFO_BUTTON_CLICKED, this._onInfoButtonClicked, this)
		APP.on(GUSLobbyApplication.EVENT_ON_SETTINGS_BUTTON_CLICKED, this._onSettingButtonClicked, this)
		
		APP.secondaryScreenController.settingsScreenController.on(GUSLobbySettingsScreenController.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, this._onSettingsScreenTutorialNeededStateChanged, this);
	}

	get isTutorialDisplayed()
	{
		return this.view && this.view.visible;
	}

	get showAgain()
	{
		return !this._fIsDoNotShowAgainChecked_bl;
	}

	i_initViewOnStage(aStage_s)
	{
		this._fStage_s = aStage_s;

		this.view.i_init(aStage_s);
		this.view.on(GUSLobbyTutorialView.DO_NOT_SHOW_AGAIN_BUTTON_CLICKED, this._onShowAgainClicked, this);
		this.view.on(GUSLobbyTutorialView.VIEW_HIDDEN, this.i_hideTutorial, this);
	}

	initView(aView)
	{
		super.initView(aView);

		this.view && this.view.hide();
	}
	
	_onFireSettingsButtonClicked()
	{
		this._isFireSettingButtonClicked_bl = true;
		this.i_showTutorialIfNeeded();
	}

	_onInfoButtonClicked()
	{
		APP.secondaryScreenController.settingsScreenController.off(GUSettingsScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenClosed, this);
		APP.secondaryScreenController.off(GUSLobbySecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenClosed, this);
		
		this._isInfoButtonClicked_bl = true;
		this.i_showTutorialIfNeeded();
	}

	_onSettingButtonClicked()
	{
		this._isInfoButtonClicked_bl = false;
		this._isFireSettingButtonClicked_bl = false;
		APP.secondaryScreenController.settingsScreenController.on(GUSettingsScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenClosed, this);
	}

	i_showTutorialIfNeeded()
	{
		if (APP.lobbyScreen.visible || !APP.layout.isGamesLayoutVisible)
		{
			return;
		}

		if (APP.playerController.info.toolTipEnabled && !this._fHasTutorialBeenShown_bl)
		{
			if(APP.commonPanelController.isFireSettingsButtonActivated || this._isFireSettingButtonClicked_bl)
			{
				APP.commonPanelController.on(GUSLobbyCommonPanelController.EVENT_FIRE_SETTINGS_STATE_CHANGE, this._fireSettingsButtonActivated, this)
				return;
			}

			if (APP.secondaryScreenController.isSecondaryScreenActive || this._isInfoButtonClicked_bl)
			{
				APP.secondaryScreenController.on(GUSLobbySecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenClosed, this);
				return;
			}

			if (APP.dialogsController.info.hasActiveDialog)
			{
				APP.dialogsController.on(GUSLobbyDialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
				return;
			}

			this.emit(GUSLobbyTutorialController.EVENT_ON_TIME_TO_SHOW_TUTORIAL_LAYER);

			this.view && this.view.i_startAppearingAnimation(this._fObjectsData_obj) && this.view.show();
			this._fHasTutorialBeenShown_bl = true;
		}
	}

	_fireSettingsButtonActivated()
	{
		APP.commonPanelController.off(GUSLobbyCommonPanelController.EVENT_FIRE_SETTINGS_STATE_CHANGE, this._fireSettingsButtonActivated, this);
		this._isFireSettingButtonClicked_bl = false;
		
		this.i_showTutorialIfNeeded();
	}

	_onSettingsScreenTutorialNeededStateChanged(event)
	{
		this.emit(GUSLobbyTutorialController.EVENT_ON_TUTORIAL_NEEDED_STATE_CHANGED, {state: event.value});
	}

	_onSecondaryScreenClosed(event)
	{
		APP.secondaryScreenController.off(GUSLobbySecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenClosed, this);
		APP.secondaryScreenController.settingsScreenController.off(GUSettingsScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenClosed, this);
		
		this.i_showTutorialIfNeeded();
		this._isInfoButtonClicked_bl = false;

	}

	_checkIfShowTutorialAgainRequired(event)
	{
		if(event && event.tutorialShowAgainRequired !== undefined && event.tutorialShowAgainRequired !== null && this._fHasTutorialBeenShown_bl)
		{
			this._fHasTutorialBeenShown_bl = !event.tutorialShowAgainRequired;
		}
	}
	
	_onDialogDeactivated()
	{
		if (!APP.dialogsController.info.hasActiveDialog)
		{
			APP.dialogsController.off(GUSLobbyDialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
		}
		
		this.i_showTutorialIfNeeded();
	}

	i_hideTutorial()
	{
		this.view.hide();
		this.emit(GUSLobbyTutorialController.EVENT_ON_TIME_TO_HIDE_TUTORIAL_LAYER);

		let lShowAgain_bl = this.showAgain;

		if (!lShowAgain_bl)
		{
			this.emit(GUSLobbyTutorialController.EVENT_ON_TUTORIAL_NEEDED_STATE_CHANGED, {state: lShowAgain_bl});
		}
	}

	_onTipsStateChanged(e)
	{
		if(!e.state)
		{
			this._fIsDoNotShowAgainChecked_bl = false;
		}
	}

	_onShowAgainClicked(e)
	{
		if (this._fIsDoNotShowAgainChecked_bl)
		{
			this._fIsDoNotShowAgainChecked_bl = false;
		}
		else
		{
			this._fIsDoNotShowAgainChecked_bl = true;
		}
	}

	_onMessageReceived(e)
	{
		switch (e.type)
		{
			case GAME_MESSAGES.TIME_TO_SHOW_TUTORIAL:
				this._fObjectsData_obj = e.data;
				this.i_showTutorialIfNeeded();
				APP.secondaryScreenController.on(GUSLobbySecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenClosed, this);
				break;
			case GAME_MESSAGES.BACK_TO_LOBBY:
				this._fHasTutorialBeenShown_bl = false;
				this._isFireSettingButtonClicked_bl = false;
				this._isInfoButtonClicked_bl = false;
				this._fIsDoNotShowAgainChecked_bl = false;
				this.view && this.view._hideTutorial();
				break;
		}
	}

	destroy()
	{
		super.destroy();
		
		APP.exterternalCommunicator.off(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onMessageReceived, this);
	}
}
export default GUSLobbyTutorialController;