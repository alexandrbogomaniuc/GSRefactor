import DialogView from '../../DialogView';
import I18 from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Button from '../../../../../../ui/LobbyButton';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameDialogButton from './GameDialogButton';

class GameMidRoundExitDialogView extends DialogView
{
	constructor()
	{
		super();
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
		this._baseContainer.addChild(APP.library.getSprite("dialogs/back"));

		let icon = this._baseContainer.addChild(APP.library.getSprite("dialogs/mid_round_exit_icon"));
		icon.position.set(-292, -174.5);
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;

		switch (aIntId_int)
		{
			case 0:
				lButton_btn = new GameDialogButton("dialogs/active_btn", "dialogs/deactive_btn", "TADialogButtonYes", undefined, undefined, GameDialogButton.BUTTON_TYPE_ACCEPT);
				lButton_btn.isOkButton = true;
				break;
			case 1:
				lButton_btn = new GameDialogButton("dialogs/active_btn", "dialogs/deactive_btn", "TADialogButtonNo", undefined, undefined, GameDialogButton.BUTTON_TYPE_CANCEL);
				lButton_btn.isCancelButton = true;
				break;
			case 2:
				lButton_btn = new Button("dialogs/exit_btn", null, true, undefined, undefined, Button.BUTTON_TYPE_CANCEL);
				APP.isMobile && (lButton_btn.hitArea = new PIXI.Rectangle(lButton_btn.hitArea.x * 2, lButton_btn.hitArea.y * 2, lButton_btn.hitArea.width * 2, lButton_btn.hitArea.height * 2));
				lButton_btn.isCancelButton = true;
				break;
			default:
				throw new Error (`Unsupported button id: ${aIntId_int}`);
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

export default GameMidRoundExitDialogView;