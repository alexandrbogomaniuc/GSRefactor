import GUDialogView from '../../GUDialogView';
import AlignDescriptor from '../../../../../../unified/model/display/align/AlignDescriptor';
import GUSLobbyActiveGameDialogButton from './GUSLobbyActiveGameDialogButton';
import { APP } from '../../../../../../unified/controller/main/globals';
import I18 from '../../../../../../unified/controller/translations/I18';
import Sprite from '../../../../../../unified/view/base/display/Sprite';
import TextField from '../../../../../../unified/view/base/display/TextField';

class GUSGameRoundTransitionSWCompensationDialogView extends GUDialogView
{
	static get MONEY_TEXT_FORMAT()
	{
		return {
			align: AlignDescriptor.LEFT,
			fill: 0x00e376,
			fontFamily: "fnt_nm_cmn_barlow",
			fontSize: 25
		};
	}

	constructor()
	{
		super();

		this._baseContainer.addChild(APP.library.getSprite("dialogs/compensation_dialog_gackground"));
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
		let lOverlay_g = new PIXI.Graphics();
		lOverlay_g.beginFill(0x000000).drawRect(0, 0, 960, 540).endFill();
		lOverlay_g.alpha = 0.6;;
		lOverlay_g.position.set(-960 / 2, -540 / 2);
		this._baseContainer.addChild(lOverlay_g);
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				lButton_btn = new GUSLobbyActiveGameDialogButton("dialogs/active_btn", "TADialogButtonClose", undefined, undefined, GUSLobbyActiveGameDialogButton.BUTTON_TYPE_ACCEPT);
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
		msg.position.set(0, -42);
		this._messageContainer.addChild(msg);

		if (this.uiInfo.totalReturnedSpecialWeapons >= 0)
		{
			let lTextContainer_sprt = this._messageContainer.addChild(new Sprite());
			lTextContainer_sprt.position.set(0, 5);

			let lMoneyField_tf = lTextContainer_sprt.addChild(new TextField(GUSGameRoundTransitionSWCompensationDialogView.MONEY_TEXT_FORMAT));
			lMoneyField_tf.maxWidth = 160;
			lMoneyField_tf.anchor.set(0.5, 0.5);
			lMoneyField_tf.position.set(0, 0);
			lMoneyField_tf.text = APP.currencyInfo.i_formatIncomeWithPlusSign(this.uiInfo.totalReturnedSpecialWeapons, true);
		}
	}

	get _supportedButtonsCount ()
	{
		return 1;
	}
}

export default GUSGameRoundTransitionSWCompensationDialogView;