import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import RedirectionController from '../../../../interaction/browser/redirection/RedirectionController';

class RedirectionDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

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

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let redirectionController = APP.redirectionController;
		redirectionController.on(RedirectionController.EVENT_REDIRECTION, this._onRedirection, this);
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		super.__validateViewLevel();
	}
	//...VALIDATION

	_onRedirection(event)
	{
		this.__activateDialog();
	}
}

export default RedirectionDialogController