import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';


class GUServerRebootDialogController extends GUDialogController
{
    static get EVENT_DIALOG_PRESENTED()             { return GUDialogController.EVENT_DIALOG_PRESENTED }
    static get EVENT_PRESENTED_DIALOG_UPDATED()     { return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

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

        webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);

        webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
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
            case GUSLobbyWebSocketInteractionController.ERROR_CODES.SERVER_REBOOT:
                this.__activateDialog();
                break;
        }
    }

    _onLobbyServerConnectionOpened(event)
    {
        this.__deactivateDialog();
    }
}

export default GUServerRebootDialogController;