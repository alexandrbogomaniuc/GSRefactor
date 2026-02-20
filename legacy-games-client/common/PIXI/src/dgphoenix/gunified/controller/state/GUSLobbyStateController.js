import SimpleController from '../../../unified/controller/base/SimpleController';
import GUSLobbyStateInfo from '../../model/state/GUSLobbyStateInfo';
import GUSLobbyApplication from '../main/GUSLobbyApplication';
import GUSLobbyScreen from '../../view/main/GUSLobbyScreen';
import GUSLobbySecondaryScreenController from '../uis/custom/secondary/GUSLobbySecondaryScreenController';
import GUDialogsController from '../uis/custom/dialogs/GUDialogsController';
import { APP } from '../../../unified/controller/main/globals';

class GUSLobbyStateController extends SimpleController
{
	static get EVENT_ON_LOBBY_VISIBILITY_CHANGED() { return "onLobbyVisibilityChanged"; }
	static get EVENT_ON_SECONDARY_SCREEN_STATE_CHANGE() { return "onSecondaryScreenStateChanged"; }
	static get EVENT_ON_DIALOGS_VISIBILITY_CHANGED() { return "onDialogsVisibilityChanged"; }

	constructor(aOptInfo_ussi)
	{
		super(aOptInfo_ussi || new GUSLobbyStateInfo());
	}

	__initControlLevel()
	{
		this._onPreloaderShow();

		APP.on(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		APP.on(GUSLobbyApplication.EVENT_ON_LOBBY_SCREEN_SHOWED, this._onLobbyScreenShowed, this);
	}

	_onLobbyStarted()
	{
		this._onLobbyScreenVisibilityChanged(false);

		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_GAME_URL_READY, this._onGameURLReady, this);
		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_LOBBY_SHOW, this._onLobbyShow, this);
		APP.lobbyScreen.on(GUSLobbyScreen.EVENT_ON_LOBBY_HIDE, this._onLobbyHide, this);

		APP.secondaryScreenController.on(GUSLobbySecondaryScreenController.EVENT_SCREEN_SHOWED, this._onSecondaryScreenActivated, this);
		APP.secondaryScreenController.on(GUSLobbySecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenDeactivated, this);

		APP.dialogsController.on(GUDialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		APP.dialogsController.on(GUDialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);
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
		this._onSecondaryScreenStateChanged(GUSLobbyStateInfo.SCREEN_NONE);
	}

	_onLobbyScreenVisibilityChanged(aVal_bln)
	{
		if (aVal_bln === this.info.lobbyScreenVisible)
		{
			return;
		}
		this.info.lobbyScreenVisible = aVal_bln;

		this.emit(GUSLobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, { visible: this.info.lobbyScreenVisible });
	}

	_onSecondaryScreenStateChanged(aVal_str)
	{
		this.info.secondaryScreenState = aVal_str;

		this.emit(GUSLobbyStateController.EVENT_ON_SECONDARY_SCREEN_STATE_CHANGE, { state: this.info.secondaryScreenState });
	}

	_onDialogActivated()
	{
		this.info.dialogVisible = true;
		this.emit(GUSLobbyStateController.EVENT_ON_DIALOGS_VISIBILITY_CHANGED, { state: true });
	}

	_onDialogDeactivated()
	{
		this.info.dialogVisible = false;
		this.emit(GUSLobbyStateController.EVENT_ON_DIALOGS_VISIBILITY_CHANGED, { state: false });
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
		APP.off(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		APP.off(GUSLobbyApplication.EVENT_ON_LOBBY_SCREEN_SHOWED, this._onLobbyScreenShowed, this);

		APP.lobbyScreen.off(GUSLobbyScreen.EVENT_ON_GAME_URL_READY, this._onGameURLReady, this);
		APP.lobbyScreen.off(GUSLobbyScreen.EVENT_ON_LOBBY_SHOW, this._onLobbyShow, this);
		APP.lobbyScreen.off(GUSLobbyScreen.EVENT_ON_LOBBY_HIDE, this._onLobbyHide, this);

		APP.secondaryScreenController.off(GUSLobbySecondaryScreenController.EVENT_SCREEN_ACTIVATED, this._onSecondaryScreenActivated, this);
		APP.secondaryScreenController.off(GUSLobbySecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this._onSecondaryScreenDeactivated, this);

		super.destroy();
	}
}

export default GUSLobbyStateController;