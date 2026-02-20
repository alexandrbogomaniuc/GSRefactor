import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyApplication from '../../../../main/GUSLobbyApplication';
import GULobbyScreen from '../../../../../view/main/GUSLobbyScreen';

class GUSRoomMovedErrorRequestsLimitDialogController extends GUDialogController
{
	static get EVENT_DIALOG_PRESENTED () { return GUDialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initGUSRoomMovedErrorRequestsLimitDialogController();
	}

	_initGUSRoomMovedErrorRequestsLimitDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().roomMovedErrorRequestsLimitDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyAppStarted, this);
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var info = this.info;
		if (info.isActive)
		{
			var view = this.__fView_uo;
			view.setMessage('TADialogRoomMovedDialog');
            view.setOkMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

    __onDialogOkButtonClicked(event)
    {
        super.__onDialogOkButtonClicked(event);
        this.__deactivateDialog();
    }

	_onLobbyAppStarted(event)
    {
        APP.lobbyScreen.on(GULobbyScreen.EVENT_ON_ERROR_ROOM_MOVED_REQUESTS_LIMIT_REACHED, this._onRoomMovedErrorRequestsLimitReached, this);
    }

	_onRoomMovedErrorRequestsLimitReached()
    {
        this.__activateDialog();
    }

}

export default GUSRoomMovedErrorRequestsLimitDialogController