import DialogController from "../DialogController";
import { APP } from "../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import LobbyScreen from "../../../../../main/LobbyScreen";
import LobbyApp from "../../../../../LobbyAPP";


class RoomMovedErrorRequestsLimitDialogController extends DialogController
{
    static get EVENT_DIALOG_PRESENTED()                         { return DialogController.EVENT_DIALOG_PRESENTED };
    static get EVENT_PRESENTED_DIALOG_UPDATED()                 { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

    constructor(aOptInfo_usuii, parentController)
    {
        super(aOptInfo_usuii, undefined, parentController);
    }

    //INIT...
    __init()
    {
        super.__init();
    }

    __getExternalViewForSelfInitialization()
    {
        return this.__getViewLevelSelfInitializationViewProvider().roomMovedErrorRequestsLimitDialogView;
    }

    __initViewLevel()
    {
        super.__initViewLevel();
    }

    __initControlLevel()
    {
        super.__initControlLevel();

        APP.once(LobbyApp.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
    }
    //...INIT

    //VALIDATION...
    __validateViewLevel()
    {
        if (this.info.isActive)
        {
            let lView_uo = this.__fView_uo;

            lView_uo.setMessage('TADialogRoomMovedDialog');
            lView_uo.setOkMode();
        }

        super.__validateViewLevel();
    }
    //...VALIDATION

    __onDialogOkButtonClicked(event)
    {
        super.__onDialogOkButtonClicked(event);
        this.__deactivateDialog();
    }

    _onLobbyStarted(event)
    {
        APP.lobbyScreen.on(LobbyScreen.EVENT_ON_ERROR_ROOM_MOVED_REQUESTS_LIMIT_REACHED, this._onRoomMovedErrorRequestsLimitReached, this);
    }

    _onRoomMovedErrorRequestsLimitReached(event)
    {
        this.__activateDialog();
    }
}

export default RoomMovedErrorRequestsLimitDialogController;