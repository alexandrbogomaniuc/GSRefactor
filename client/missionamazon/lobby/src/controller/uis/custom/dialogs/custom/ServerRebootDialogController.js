import DialogController from "../DialogController";
import { APP } from "../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import LobbyApp from "../../../../../LobbyAPP";
import LobbyWebSocketInteractionController from "../../../../interaction/server/LobbyWebSocketInteractionController";
import LobbyExternalCommunicator, { GAME_MESSAGES } from "../../../../../external/LobbyExternalCommunicator";
import I18 from "../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18";


class ServerRebootDialogController extends DialogController
{
    static get EVENT_DIALOG_PRESENTED()             { return DialogController.EVENT_DIALOG_PRESENTED }
    static get EVENT_PRESENTED_DIALOG_UPDATED()     { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED }

    constructor(aOptInfo_usuii, parentController)
    {
        super(aOptInfo_usuii, undefined, parentController);
    }

    __init()
    {
        super.__init();
    }

    __getExternalViewForSelfInitialization()
    {
        return this.__getViewLevelSelfInitializationViewProvider().serverRebootDialogView;
    }

    __initControlLevel()
    {
        super.__initControlLevel();

        let webSocketInteractionController = APP.webSocketInteractionController;

        webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);

        webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
    }

    //VALIDATION...
    __validateViewLevel()
    {
        if (this.info.isActive)
        {
            let lView_vo = this.__fView_uo;

            lView_vo.setMessage("TADialogMessageServerReboot");
            lView_vo.setEmptyMode();
        }
    }
    //...VALIDATION

    _onServerErrorMessage(event)
    {
        let serverData = event.messageData;

        switch (serverData.code)
        {
            case LobbyWebSocketInteractionController.ERROR_CODES.SERVER_REBOOT:
                this.__activateDialog();
                break;
        }
    }

    _onLobbyServerConnectionOpened(event)
    {
        this.__deactivateDialog();
    }
}

export default ServerRebootDialogController;