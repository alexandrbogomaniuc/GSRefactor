import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GameApp from '../../../../../CrashAPP';

class NetworkErrorDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initNetworkErrorDialogController();
	}

	_initNetworkErrorDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().networkErrorDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		APP.on(GameApp.EVENT_ON_GAME_ASSETS_LOADING_ERROR, this._onGameAssetsLoadingError, this);
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

			view.setMessage("TADialogMessageNetworkError");
			view.setEmptyMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__activateDialog ()
	{
		super.__activateDialog();

		if (!this.__fView_uo
			&& !this.__isViewLevelSelfInitializationViewProviderAccessible())
		{
			//view can be inaccessible at the time of initial loading, simple alert will be used instead
			var lMessage_str;
			
			if (I18.isPreloaderTranslationDescriptorReady)
			{
				var assetDescriptor = I18.getTranslatableAssetDescriptor("TADialogMessageNetworkError");
				lMessage_str = assetDescriptor.textDescriptor.text;
			}
			else
			{
				lMessage_str = "A network error has occurred. Please try again and contact customer support if the error persists.";
			}
			//when the dialog view is not accessible simple alert will be used
			alert(lMessage_str);
		}
	}

	_onGameAssetsLoadingError(event)
	{
		this.__activateDialog();
	}
}

export default NetworkErrorDialogController