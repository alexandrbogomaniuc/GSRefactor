import GUDialogView from '../../GUDialogView';
import GUSLobbyButton from '../../../GUSLobbyButton';
import GUSLobbyGameDialogButton from './GUSLobbyGameDialogButton';
import { APP } from '../../../../../../unified/controller/main/globals';
import I18 from '../../../../../../unified/controller/translations/I18';
import Sprite from '../../../../../../unified/view/base/display/Sprite';

class GUGameMidRoundExitDialogView extends GUDialogView
{
	constructor(aBackAssetName_str)
	{
		super();

		this._fBackAssetName_str = aBackAssetName_str;
		this._addBaseTexture();
	}

	//INIT VIEW...
	_initDialogView()
	{
		super._initDialogView();

		this._baseContainer.position.set(0, 0);
		this._buttonsContainer.position.set(0, 130);
		this._messageContainer.position.set(0, 0);
	}

	_addDialogBase()
	{
		let lBase_sprt = this._fBase_sprt = new Sprite();
		this._baseContainer.addChild(lBase_sprt);

		let icon = this._baseContainer.addChild(APP.library.getSprite("dialogs/mid_round_exit_icon"));
		icon.position.set(-292, -174.5);
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
			case 2:
				lButton_btn = new GUSLobbyButton("dialogs/exit_btn", null, true, undefined, undefined, GUSLobbyButton.BUTTON_TYPE_CANCEL);
				APP.isMobile && (lButton_btn.hitArea = new PIXI.Rectangle(lButton_btn.hitArea.x * 2, lButton_btn.hitArea.y * 2, lButton_btn.hitArea.width * 2, lButton_btn.hitArea.height * 2));
				lButton_btn.isCancelButton = true;
				break;
			default:
				throw new Error(`Unsupported button id: ${aIntId_int}`);
		}
		return lButton_btn;
	}

	_alignButtonView (aIntButtonId_int, aButtonsCount_int, aButtonView_domdb)
	{
		var lXOffset_num = 0;
		var lYOffset_num = 0;

		if (aIntButtonId_int === 0)
		{
			lXOffset_num = -57;
			lYOffset_num = -7;
		}
		else if (aIntButtonId_int === 1)
		{
			lXOffset_num = 57;
			lYOffset_num = -7;
		}
		else if (aIntButtonId_int === 2)
		{
			lXOffset_num = 296;
			lYOffset_num = -302;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	_setMessage()
	{
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset("TADialogMRECaption");
		msg.position.set(-219, -173.5);
		this._messageContainer.addChild(msg);

		msg = I18.generateNewCTranslatableAsset("TADialogMRELeavingRoom");
		msg.position.set(0, -96);
		this._messageContainer.addChild(msg);

		msg = I18.generateNewCTranslatableAsset("TADialogMRENote");
		msg.position.set(0, 0);
		this._messageContainer.addChild(msg);

		let line = this._messageContainer.addChild(APP.library.getSprite("dialogs/line"));
		line.scale.x = 0.66;
		line.position.set(0, 83);
	}

	get _supportedButtonsCount()
	{
		return 3;
	}
}

export default GUGameMidRoundExitDialogView;