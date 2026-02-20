import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import { CLIENT_MESSAGES } from '../../../../../model/interaction/server/GUSLobbyWebSocketInteractionInfo';
import GUSLobbyDialogsController from '../GUSLobbyDialogsController';
import GUSDialogsInfo from '../../../../../model/uis/custom/dialogs/GUSDialogsInfo';
import I18 from '../../../../../../unified/controller/translations/I18';
import GUSLobbyCommonAssetsController from '../../../../preloading/GUSLobbyCommonAssetsController';

class GUSBattlegroundRulesDialogController extends GUDialogController
{
	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeInfo_tni = null;
		
		this._initGUSBattlegroundRulesDialogController();
	}

	_initGUSBattlegroundRulesDialogController()
	{
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundRulesDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();

	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;

		APP.dialogsController.on(GUSLobbyDialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		APP.dialogsController.on(GUSLobbyDialogsController.EVENT_BATTLEGROUND_RULES_SHOW_REQUIRED, this._onBattlegroundRulesTime, this);

		APP.commonAssetsController.on(GUSLobbyCommonAssetsController.EVENT_ON_BTG_RULES_HTML_READY, this._onBattlegroundRulesHtmlReady, this);
	}

	_onDialogActivated(event)
	{
		if (event.dialogId !== GUSDialogsInfo.DIALOG_ID_BATTLEGROUND_RULES)
		{
			this.__deactivateDialog();
		}
	}

	_onBattlegroundRulesTime(event)
	{
		this.__activateDialog();
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var info = this.info;
		var view = this.__fView_uo;
		var messageAssetId;

		//buttons configuration...
		view.setOkMode();
		//...buttons configuration

		//message configuration...
		view.setMessage("TABattlegroundGameRulesCaption");
		//...message configuration

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		super.__deactivateDialog();

		APP.layout.clearHtmlOverlay();
	}

	__activateDialog()
	{
		super.__activateDialog();

		this.view.showHTMLContainer();

		APP.commonAssetsController.loadBattlgroundRulesHtml();
	}

	_onBattlegroundRulesHtmlReady(event)
	{
		if (this.info.isActive && this.view.isHTMLContainerActive)
		{
			let lRulesHTML_str = event.htmlData;
			
			this.view.setRulesHtml(lRulesHTML_str);
		}
	}

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.__deactivateDialog();
	}
}

export default GUSBattlegroundRulesDialogController