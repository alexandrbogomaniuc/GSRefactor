import GUSettingsScreenController from './GUSettingsScreenController';
import GUSLobbySettingsScreenView from '../../../../../view/uis/secondary/settings/GUSLobbySettingsScreenView';
import GUSLobbySoundButtonController from '../../GUSLobbySoundButtonController';
import GUSLobbyTooltipsController from '../../tooltips/GUSLobbyTooltipsController';
import { APP } from '../../../../../../unified/controller/main/globals';
import { LOBBY_MESSAGES } from '../../../../external/GUSLobbyExternalCommunicator';

class GUSLobbySettingsScreenController extends GUSettingsScreenController
{
	static get EVENT_ON_SETTINGS_TOOLTIPS_STATE_CHANGED()	{ return GUSLobbySettingsScreenView.EVENT_ON_SETTINGS_TOOLTIPS_STATE_CHANGED; }
	static get EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED()	{ return GUSLobbySettingsScreenView.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED; }
	
	constructor()
	{
		super ();
	}

	__init()
	{
		this._fTooltipsRememberState_bln = null;
		this._fTutorialRememberState_bln = null;
		this._fIsToggleStateChanged_bln = null;
		
		super.__init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.commonPanelController.soundButtonController.on(GUSLobbySoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._updateMuteSettings, this);
        APP.commonPanelController.soundButtonController.on(GUSLobbySoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._updateUnMuteSettings, this);	
		if (!APP.isTutorialSupported)
		{
			APP.tooltipsController.on(GUSLobbyTooltipsController.EVENT_ON_TIPS_STATE_CHANGED, this._onTooltipsStateChanged, this);
		}
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_ssv = this.view;

		if (APP.isTutorialSupported)
		{
			lView_ssv.on(GUSLobbySettingsScreenView.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, this._onTutorialStateChanged, this);
		}
		else
		{
			lView_ssv.on(GUSLobbySettingsScreenView.EVENT_ON_SETTINGS_TOOLTIPS_STATE_CHANGED, this.emit, this);
		}
	}

	_showScreen()
	{
		let lView_ssv = this.view;

		if (APP.isBattlegroundGame)
		{
			let lBattleGroundInfo_bgi = APP.battlegroundController.info;
			
			if (APP.battlegroundController.isSecondaryScreenTimerRequired())
			{
				clearInterval(this.interval);

				lView_ssv.updateTimeIndicator(lBattleGroundInfo_bgi.getFormattedTimeToStart(false));
				this.interval = setInterval(this._tick.bind(this), 100);

				if (!APP.dialogsController.lobbyBattlegroundNotEnoughPlayersDialogController.info.isActive)
				{
					APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_COUNTDOWN_HIDE);
				}
			}
		}

		if (APP.isTutorialSupported)
		{
			this._fTutorialRememberState_bln = APP.tooltipsController.info.tooltipsEnabled;
			if (APP.isBattlegroundGame)
			{
				this._fTutorialRememberState_bln = APP.tooltipsController.info.gameTipsEnabled;
			}
			lView_ssv.updateTutorialToggleState(this._fTutorialRememberState_bln);
		}
		else
		{
			let lTooltipsState_bln = APP.tooltipsController.info.tooltipsEnabled;
			lView_ssv.updateTooltipsToggleState(lTooltipsState_bln);
			this._fTooltipsRememberState_bln = lTooltipsState_bln;
		}

		super._showScreen();
	}

	_hideSceeen()
	{
		let lView_ssv = this.view;
		if (lView_ssv)
		{
			if (APP.isBattlegroundGame)
			{
				clearInterval(this.interval);
				this.view.hideTimeIndicator();

				if (
						APP.battlegroundController.isSecondaryScreenTimerRequired()
						&& !APP.dialogsController.lobbyBattlegroundNotEnoughPlayersDialogController.info.isActive
					)
				{
					APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_COUNTDOWN_SHOW);
				}
			}
			lView_ssv.visible = false;
			this.emit(GUSettingsScreenController.EVENT_SCREEN_DEACTIVATED, {tutorialShowAgainRequired: !!this._fIsToggleStateChanged_bln});
		}
	}

	_tick()
	{
		this.view.updateTimeIndicator(APP.battlegroundController.getFormattedTimeToStart(false));

		if (APP.battlegroundController.info.getTimeToStartInMillis() <= 0)
		{
			clearInterval(this.interval);
			this._hideSceeen();
		}
	}

	_onClose()
	{
		this._fIsToggleStateChanged_bln = false;
		this._fNewTutorialState_bln = null;
		if (APP.isTutorialSupported)
		{
			// this._fNewTutorialState_bln = this._fTutorialRememberState_bln;
			
			this._validateTutorialState();
		}
		else
		{
			let lTooltipsState_bln = APP.tooltipsController.info.tooltipsEnabled;

			if (lTooltipsState_bln != this._fTooltipsRememberState_bln)
			{
				this.view.updateTooltipsToggleState(this._fTooltipsRememberState_bln);
				this.emit(GUSLobbySettingsScreenController.EVENT_ON_SETTINGS_TOOLTIPS_STATE_CHANGED, { value: this._fTooltipsRememberState_bln });
			}

			this._fTooltipsRememberState_bln = null;
		}

		super._onClose();
	}

	_onOkClicked()
	{
		if (APP.isTutorialSupported)
		{
			this._validateTutorialState();
		}
		super._onOkClicked();
		
		this._fIsToggleStateChanged_bln = false;
	}

	_validateTutorialState()
	{
		if (this._fNewTutorialState_bln !== null && this._fNewTutorialState_bln !== undefined)
		{
			this.view.updateTutorialToggleState(this._fTutorialRememberState_bln);
			this.emit(GUSLobbySettingsScreenController.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, {value: this._fNewTutorialState_bln});
		}

		this._fTutorialRememberState_bln = null;
		this._fNewTutorialState_bln = null;
	}

	_onTooltipsStateChanged(aEvent_obj)
	{
		let lView_ssv = this.view;

		if (lView_ssv)
		{
			let lTooltipsState_bln = aEvent_obj.state;
			lView_ssv.updateTooltipsToggleState(lTooltipsState_bln);
		}
	}

	_onTutorialStateChanged(aData_obj)
	{
		this._fNewTutorialState_bln = aData_obj.value;
		this._fIsToggleStateChanged_bln = true;
	}

	destroy()
	{
		let lView_ssv = this.view;

		if (lView_ssv)
		{
			lView_ssv.off(GUSLobbySettingsScreenView.EVENT_ON_SETTINGS_TOOLTIPS_STATE_CHANGED, this.emit, this);
			lView_ssv.off(GUSLobbySettingsScreenView.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, this._onTutorialStateChanged, this);
		}

		if (APP.commonPanelController && APP.commonPanelController.soundButtonController)
		{
			APP.commonPanelController.soundButtonController.off(GUSLobbySoundButtonController.EVENT_SOUND_ON_BUTTON_CLICKED, this._updateMuteSettings, this);
        	APP.commonPanelController.soundButtonController.off(GUSLobbySoundButtonController.EVENT_SOUND_OFF_BUTTON_CLICKED, this._updateUnMuteSettings, this);
		}

		if (APP.tooltipsController)
		{
			APP.tooltipsController.off(GUSLobbyTooltipsController.EVENT_ON_TIPS_STATE_CHANGED, this._onTooltipsStateChanged, this);
		}

		this._fTooltipsRememberState_bln = null;
		this._fTutorialRememberState_bln = null;
		this._fIsToggleStateChanged_bln = null;

		super.destroy();
	}
}

export default GUSLobbySettingsScreenController