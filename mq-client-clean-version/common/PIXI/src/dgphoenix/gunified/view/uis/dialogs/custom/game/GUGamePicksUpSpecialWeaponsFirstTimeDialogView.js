import GUDialogView from '../../GUDialogView';
import GUSLobbyGameDialogButton from './GUSLobbyGameDialogButton';
import { APP } from '../../../../../../unified/controller/main/globals';
import I18 from '../../../../../../unified/controller/translations/I18';
import Sprite from '../../../../../../unified/view/base/display/Sprite';

class GUGamePicksUpSpecialWeaponsFirstTimeDialogView extends GUDialogView
{
	constructor(aBackAssetName_str)
	{
		super();

		this._fBackAssetName_str = aBackAssetName_str;
		this._addBaseTexture();
	}

	_initDialogView()
	{
		super._initDialogView();

		this._baseContainer.position.set(0, 0);
		this._buttonsContainer.position.set(0, 45);
		this._messageContainer.position.set(0, -50);
	}

	_addDialogBase()
	{
		let lBase_sprt = this._fBase_sprt = new Sprite();
		this._baseContainer.addChild(lBase_sprt);
	}

	_addBaseTexture()
	{
		this._fBase_sprt.addChild(APP.library.getSprite(this._fBackAssetName_str));
	}

	__retreiveDialogButtonViewInstance(aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				lButton_btn = new GUSLobbyGameDialogButton("dialogs/active_btn", "dialogs/deactive_btn", "TADialogButtonYes", undefined, undefined, GUSLobbyGameDialogButton.BUTTON_TYPE_ACCEPT);
				lButton_btn.isOkButton = true;
				break;
			case 1:
				lButton_btn = new GUSLobbyGameDialogButton("dialogs/active_btn", "dialogs/deactive_btn", "TADialogButtonNo", undefined, undefined, GUSLobbyGameDialogButton.BUTTON_TYPE_CANCEL);
				lButton_btn.isCancelButton = true;
				break;
			default:
				throw new Error(`Unsupported button id: ${aIntId_int}`);
		}
		return lButton_btn;
	}

	_alignButtonView (aIntButtonId_int, aButtonsCount_int, aButtonView_domdb)
	{
		let lXOffset_num = 0;
		let lYOffset_num = 0;

		if (aIntButtonId_int === 0)
		{
			lXOffset_num = -57;
			lYOffset_num = 0;
		}
		else if (aIntButtonId_int === 1)
		{
			lXOffset_num = 57;
			lYOffset_num = 0;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}

	_setMessage()
	{
		let lMes_cta = this._messageContainer.addChild(I18.generateNewCTranslatableAsset("TADialogPicksUpSpecialWeaponsFirstTimeInfo"));
	}

	get _supportedButtonsCount ()
	{
		return 2;
	}
}

export default GUGamePicksUpSpecialWeaponsFirstTimeDialogView;