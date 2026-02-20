import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import LobbyStateInfo from '../../model/state/LobbyStateInfo';

import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SecondaryScreenController from '../uis/custom/secondary/SecondaryScreenController';
import LobbyApp from '../../LobbyAPP';
import LobbyScreen from '../../main/LobbyScreen';
import DialogsController from '../uis/custom/dialogs/DialogsController';
import LobbyExternalCommunicator, { GAME_MESSAGES } from './../../external/LobbyExternalCommunicator';

class LobbyStateController extends SimpleController
{
	static get EVENT_ON_LOBBY_VISIBILITY_CHANGED()		{ return "onLobbyVisibilityChanged"; }
	static get EVENT_ON_SECONDARY_SCREEN_STATE_CHANGE()	{ return "onSecondaryScreenStateChanged"; }
	static get EVENT_ON_DIALOGS_VISIBILITY_CHANGED()	{ return "onDialogsVisibilityChanged"; }

	constructor(aOptInfo_ussi)
	{
		super(aOptInfo_ussi || new LobbyStateInfo());
	}

	__initControlLevel()
	{
		this._onPreloaderShow();

		APP.on(LobbyApp.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		APP.on(LobbyApp.EVENT_ON_LOBBY_SCREEN_SHOWED, this._onLobbyScreenShowed, this);
	}

	_onLobbyStarted()
	{
		this._onLobbyScreenVisibilityChanged(false);

		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_GAME_URL_READY, this._onGameURLReady, this);
		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_LOBBY_SHOW, this._onLobbyShow, this);
		APP.lobbyScreen.on(LobbyScreen.EVENT_ON_LOBBY_HIDE, this._onLobbyHide, this);

		APP.secondaryScreenController.on(SecondaryScreenController.EVENT_SCREEN_SHOWED, this._onSecondaryScreenActivated, this);
		APP.secondaryScreenController.on(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenDeactivated, this);

		APP.dialogsController.on(DialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		APP.dialogsController.on(DialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
	}

	_onLobbyScreenShowed()
	{
		this._onPreloaderHide();
		this._onLobbyScreenVisibilityChanged(true);
	}

	_onGameURLReady()
	{
		this._onPreloaderShow();
		this._onLobbyScreenVisibilityChanged(false);
	}

	_onLobbyShow()
	{
		this._onPreloaderHide();
		this._onLobbyScreenVisibilityChanged(true);
	}

	_onLobbyHide()
	{
		this._onPreloaderHide();
		this._onLobbyScreenVisibilityChanged(false);
	}

	_onSecondaryScreenActivated(aEvent_obj)
	{
		this._onSecondaryScreenStateChanged(aEvent_obj.screenId);
	}

	_onSecondaryScreenDeactivated()
	{
		this._onSecondaryScreenStateChanged(LobbyStateInfo.SCREEN_NONE);
	}

	_onLobbyScreenVisibilityChanged(aVal_bln)
	{
		if (aVal_bln === this.info.lobbyScreenVisible)
		{
			return;
		}
		this.info.lobbyScreenVisible = aVal_bln;

		this.emit(LobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, {visible: this.info.lobbyScreenVisible});
	}

	_onSecondaryScreenStateChanged(aVal_str)
	{
		this.info.secondaryScreenState = aVal_str;

		this.emit(LobbyStateController.EVENT_ON_SECONDARY_SCREEN_STATE_CHANGE, {state: this.info.secondaryScreenState});
	}

	_onDialogActivated()
	{
		this.info.dialogVisible = true;
		this.emit(LobbyStateController.EVENT_ON_DIALOGS_VISIBILITY_CHANGED, {state: true});
	}

	_onDialogDeactivated()
	{
		this.info.dialogVisible = false;
		this.emit(LobbyStateController.EVENT_ON_DIALOGS_VISIBILITY_CHANGED, {state: false});
	}

	_onPreloaderShow()
	{
		this.info.preloaderVisible = true;
	}

	_onPreloaderHide()
	{
		this.info.preloaderVisible = false;
	}

	destroy()
	{
		APP.off(LobbyApp.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		APP.off(LobbyApp.EVENT_ON_LOBBY_SCREEN_SHOWED, this._onLobbyScreenShowed, this);

		APP.lobbyScreen.off(LobbyScreen.EVENT_ON_GAME_URL_READY, this._onGameURLReady, this);
		APP.lobbyScreen.off(LobbyScreen.EVENT_ON_LOBBY_SHOW, this._onLobbyShow, this);
		APP.lobbyScreen.off(LobbyScreen.EVENT_ON_LOBBY_HIDE, this._onLobbyHide, this);

		APP.secondaryScreenController.off(SecondaryScreenController.EVENT_SCREEN_ACTIVATED, this._onSecondaryScreenActivated, this);
		APP.secondaryScreenController.off(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenDeactivated, this);

		super.destroy();
	}
}

export default LobbyStateController;