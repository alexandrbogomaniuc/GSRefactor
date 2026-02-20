import DialogView from '../../DialogView';
import I18 from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AlignDescriptor from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';
import TextField from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import GameDialogButton from './GameDialogButton';
import Sprite from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class GameForceSitOutDialogView extends DialogView
{
	static get MONEY_TEXT_FORMAT()
	{
		return {
			align: AlignDescriptor.LEFT,
			fill: 0x00e376,
			fontFamily: "fnt_nm_barlow",
			fontSize: 25
		};
	}

	constructor()
	{
		super();
	}

	//INIT VIEW...
	_initDialogView()
	{
		super._initDialogView();

		this._baseContainer.position.set(0, 0);
		this._buttonsContainer.position.set(0, 0);
		this._messageContainer.position.set(0, 0);
	}
	
	_addDialogBase()
	{
		let lBeck_spr = this._baseContainer.addChild(APP.library.getSprite("dialogs/force_sit_out/back"));

		let icon = this._baseContainer.addChild(APP.library.getSprite("dialogs/force_sit_out/icon"));
		icon.position.set(0, -101);
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				lButton_btn = new GameDialogButton("dialogs/active_btn", "dialogs/deactive_btn", "TADialogButtonOK", undefined, undefined, GameDialogButton.BUTTON_TYPE_ACCEPT);
				lButton_btn.isOkButton = true;
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
			lYOffset_num = 56;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	_setMessage(messageId)
	{
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset(messageId);

		if(this.uiInfo.compensateSpecialWeapons > 0)
		{
			msg.position.set(0, -70);
		}
		else
		{
			msg.position.set(0, -41);
		}
		
		this._messageContainer.addChild(msg);

		
		if (this.uiInfo.compensateSpecialWeapons > 0 || this.uiInfo.totalReturnedSpecialWeapons > 0)
		{
			msg = I18.generateNewCTranslatableAsset("TADialogCompensateSWInfo");
			msg.position.set(0, -19);
			this._messageContainer.addChild(msg);

			let lTextContainer_sprt = this._messageContainer.addChild(new Sprite());
			lTextContainer_sprt.position.set(-5, 15);

			let lMoneyField_tf = lTextContainer_sprt.addChild(new TextField(GameForceSitOutDialogView.MONEY_TEXT_FORMAT));
			lMoneyField_tf.maxWidth = 85;
			lMoneyField_tf.anchor.set(0, 0.5);
			lMoneyField_tf.position.set(0, 0);

			let lCompensateAndReturnMoney_num = this.uiInfo.compensateSpecialWeapons + this.uiInfo.totalReturnedSpecialWeapons;
			lMoneyField_tf.text = APP.currencyInfo.i_formatIncomeWithPlusSign(lCompensateAndReturnMoney_num, true);
		}
	}

	get _supportedButtonsCount ()
	{
		return 1;
	}
}

export default GameForceSitOutDialogView;