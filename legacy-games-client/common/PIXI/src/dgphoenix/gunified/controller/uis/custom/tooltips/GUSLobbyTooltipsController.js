import SimpleUIController from '../../../../../unified/controller/uis/base/SimpleUIController';
import GUSLobbyTooltipsInfo from '../../../../model/uis/custom/tooltips/GUSLobbyTooltipsInfo';
import GUSLobbyTooltipsView from '../../../../view/uis/tooltips/GUSLobbyTooltipsView';
import { APP } from '../../../../../unified/controller/main/globals';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES } from '../../../external/GUSLobbyExternalCommunicator';
import Timer from '../../../../../unified/controller/time/Timer';
import GUSLobbyWebSocketInteractionController from '../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbySettingsScreenController from '../secondary/settings/GUSLobbySettingsScreenController';
import GUSLobbyStateController from '../../../state/GUSLobbyStateController';
import GUSLobbyTutorialController from '../tutorial/GUSLobbyTutorialController'

class GUSLobbyTooltipsController extends SimpleUIController
{
	static get EVENT_ON_TIPS_STATE_CHANGED()	{ return "onTooltipsStateChanged"; }

	static get EVENT_ON_TIP_SHOWN()				{ return GUSLobbyTooltipsView.EVENT_ON_TIP_SHOWN; }
	static get EVENT_ON_TIP_HIDED()				{ return GUSLobbyTooltipsView.EVENT_ON_TIP_HIDED; }
	static get EVENT_ON_TIPS_ENDED()			{ return GUSLobbyTooltipsView.EVENT_ON_TIPS_ENDED; }
	
	constructor(aOptInfo)
	{
		super(aOptInfo || new GUSLobbyTooltipsInfo());

		this._fStartTimeout_t = null;
		this._fFRBInfo_frbi = null;
		this._fStartTimeout_t = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameExternalMessageReceived, this);
		APP.webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		APP.webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerGetStartGameUrlMessage, this);

		APP.secondaryScreenController.settingsScreenController.on(GUSLobbySettingsScreenController.EVENT_ON_SETTINGS_TOOLTIPS_STATE_CHANGED, this._onSettingsTooltipsClick, this);

		APP.tutorialController.on(GUSLobbyTutorialController.EVENT_ON_TUTORIAL_NEEDED_STATE_CHANGED, this._onTutorialTooltipsStateChanged, this);
		this._fFRBInfo_frbi = APP.FRBController.i_getInfo();
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let l_ltv = this.view;
		if (l_ltv)
		{
			l_ltv.on(GUSLobbyTooltipsView.EVENT_ON_TIP_SHOWN, this._onTooltipShown, this);
			l_ltv.on(GUSLobbyTooltipsView.EVENT_ON_TIP_HIDED, this._onTooltipHidden, this);
			l_ltv.on(GUSLobbyTooltipsView.EVENT_ON_TIPS_ENDED, this._onTooltipsEnded, this);

			let lLobbyStateController_lsc = APP.lobbyStateController;
			if (lLobbyStateController_lsc.info.lobbyScreenVisible)
			{
				this._tryToStartTimeout();
			}
			else
			{
				lLobbyStateController_lsc.once(GUSLobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyVisibilityChanged, this);
			}
		}
	}
	//...INIT

	get _isFRB()
	{
		return this._fFRBInfo_frbi.isActivated;
	}

	_onLobbyVisibilityChanged(aEvent_obj)
	{
		if (this.view && aEvent_obj.visible)
		{
			this._tryToStartTimeout();
		}
	}

	_tryToStartTimeout()
	{
		if (this.info.lobbyTipsEnabled)
		{
			this._fStartTimeout_t = new Timer(this._startTooltips.bind(this), 500, false);
		}
	}

	_onGameExternalMessageReceived(aEvent_obj)
	{
		switch (aEvent_obj.type)
		{
			case GAME_MESSAGES.TOOLTIP_STATE_CHANGE:
				this.info.gameTipsEnabled = aEvent_obj.data.value;
				this._tryEndAllTips();
				break;
		}
	}

	_onServerEnterLobbyMessage(aEvent_obj)
	{
		let lData_obj = aEvent_obj.messageData;

		this.info.lobbyTipsEnabled = !lData_obj.disableTooltips && !this._isFRB;
		this.info.gameTipsEnabled = !lData_obj.disableTooltips && !this._isFRB;

		this.emit(GUSLobbyTooltipsController.EVENT_ON_TIPS_STATE_CHANGED, { state: this.info.lobbyTipsEnabled });
	}

	_onServerGetStartGameUrlMessage()
	{
		APP.webSocketInteractionController.off(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerGetStartGameUrlMessage, this);
		this._stopTooltips();
	}

	_onTutorialTooltipsStateChanged(aEvent_obj)
	{
		let lTooltipsEnabled_bln = aEvent_obj.state;

		this.info.lobbyTipsEnabled = lTooltipsEnabled_bln;
		this.info.gameTipsEnabled = lTooltipsEnabled_bln;
		
		this.emit(GUSLobbyTooltipsController.EVENT_ON_TIPS_STATE_CHANGED, { state: lTooltipsEnabled_bln });
	}

	_onSettingsTooltipsClick(aEvent_obj)
	{
		let lTooltipsEnabled_bln = aEvent_obj.value;

		this.info.lobbyTipsEnabled = lTooltipsEnabled_bln;
		this.info.gameTipsEnabled = lTooltipsEnabled_bln;

		this.emit(GUSLobbyTooltipsController.EVENT_ON_TIPS_STATE_CHANGED, { state: lTooltipsEnabled_bln });

		if (this.info.lobbyTipsEnabled)
		{
			this._startTooltips();
		}
		else
		{
			this._stopTooltips();
		}
	}

	_startTooltips()
	{
		let l_ltv = this.view;

		if (l_ltv)
		{
			l_ltv.startTooltips();
		}
	}

	_stopTooltips()
	{
		let l_ltv = this.view;

		if (l_ltv)
		{
			l_ltv.stopTooltips();
		}
	}

	_onTooltipShown(aEvent_obj)
	{
		this.emit(GUSLobbyTooltipsController.EVENT_ON_TIP_SHOWN, { id: aEvent_obj.id });
	}

	_onTooltipHidden(aEvent_obj)
	{
		this.emit(GUSLobbyTooltipsController.EVENT_ON_TIP_HIDED, { id: aEvent_obj.id });
	}

	_onTooltipsEnded()
	{
		this.info.lobbyTipsEnabled = false;

		this._tryEndAllTips();
	}

	_tryEndAllTips()
	{
		if (!this.info.lobbyTipsEnabled && !this.info.gameTipsEnabled)
		{
			this.emit(GUSLobbyTooltipsController.EVENT_ON_TIPS_STATE_CHANGED, { state: false });
		}
	}

	destroy()
	{
		let l_ltv = this.view;

		if (l_ltv)
		{
			l_ltv.off(GUSLobbyTooltipsView.EVENT_ON_TIP_SHOWN, this._onTooltipShown, this);
			l_ltv.off(GUSLobbyTooltipsView.EVENT_ON_TIP_HIDED, this._onTooltipHidden, this);
			l_ltv.off(GUSLobbyTooltipsView.EVENT_ON_TIPS_ENDED, this._onTooltipsEnded, this);
		}

		APP.externalCommunicator.off(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameExternalMessageReceived, this);
		APP.webSocketInteractionController.off(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
		APP.webSocketInteractionController.off(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerGetStartGameUrlMessage, this);
		APP.secondaryScreenController.settingsScreenController.off(GUSLobbySettingsScreenController.EVENT_ON_SETTINGS_TOOLTIPS_STATE_CHANGED, this._onSettingsTooltipsClick, this);

		this._fStartTimeout_t && this._fStartTimeout_t.destructor();
		this._fStartTimeout_t = null;

		super.destroy();
	}
}

export default GUSLobbyTooltipsController