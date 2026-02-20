import DialogView from '../../DialogView';
import GameDialogButton from './GameDialogButton';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class GamePicksUpSpecialWeaponsFirstTimeDialogView extends DialogView
{
	constructor()
	{
		super();
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
		let lBeck_spr = this._baseContainer.addChild(APP.library.getSprite("dialogs/force_sit_out/back"));
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
			default:
				throw new Error (`Unsupported button id: ${aIntId_int}`);
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

export default GamePicksUpSpecialWeaponsFirstTimeDialogView;