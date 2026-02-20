import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyRedirectionController from '../../../../interaction/browser/redirection/GUSLobbyRedirectionController';

class GURedirectionDialogController extends GUDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GUDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initRedirectionDialogController();
	}

	_initRedirectionDialogController()
	{	
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().redirectionDialogView;
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let redirectionController = APP.redirectionController;
		redirectionController.on(GUSLobbyRedirectionController.EVENT_REDIRECTION, this._onRedirection, this);
	}

	//VALIDATION...
	__validateModelLevel()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel()
	{
		super.__validateViewLevel();
	}
	//...VALIDATION

	_onRedirection()
	{
		this.__activateDialog();
	}
}

export default GURedirectionDialogController