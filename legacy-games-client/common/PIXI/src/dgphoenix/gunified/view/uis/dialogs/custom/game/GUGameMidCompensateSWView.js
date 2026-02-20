import GUDialogView from '../../GUDialogView';
import AlignDescriptor from '../../../../../../unified/model/display/align/AlignDescriptor';
import GUSLobbyGameDialogButton from './GUSLobbyGameDialogButton';
import I18 from '../../../../../../unified/controller/translations/I18';
import TextField from '../../../../../../unified/view/base/display/TextField';
import { APP } from '../../../../../../unified/controller/main/globals';
import Sprite from '../../../../../../unified/view/base/display/Sprite';

class GUGameMidCompensateSWView extends GUDialogView
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
		this._buttonsContainer.position.set(0, 0);
		this._messageContainer.position.set(0, 0);
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
				lButton_btn = new GUSLobbyGameDialogButton("dialogs/active_btn", "dialogs/deactive_btn", "TADialogButtonOK", undefined, undefined, GUSLobbyGameDialogButton.BUTTON_TYPE_ACCEPT);
				lButton_btn.isOkButton = true;
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
			lYOffset_num = 56;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	_setMessage(messageId)
	{
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset(messageId);
		msg.text = msg.text.replace("/ROOM_ID/", this.uiInfo.roomId);
		msg.position.set(0, -66)
		this._messageContainer.addChild(msg);

		msg = I18.generateNewCTranslatableAsset("TADialogCompensateSWInfo");
		msg.position.set(0, -42);
		this._messageContainer.addChild(msg);

		if (this.uiInfo.compensateSpecialWeapons >= 0 || this.uiInfo.totalReturnedSpecialWeapons >= 0)
		{
			let lTextContainer_sprt = this._messageContainer.addChild(new Sprite());
			lTextContainer_sprt.position.set(0, 5);

			let lMoneyField_tf = lTextContainer_sprt.addChild(new TextField(GUGameMidCompensateSWView.MONEY_TEXT_FORMAT));
			lMoneyField_tf.maxWidth = 160;
			lMoneyField_tf.anchor.set(0.5, 0.5);
			lMoneyField_tf.position.set(0, 0);

			let lMoney_num = this.uiInfo.compensateSpecialWeapons + this.uiInfo.totalReturnedSpecialWeapons;
			lMoneyField_tf.text = APP.currencyInfo.i_formatIncomeWithPlusSign(lMoney_num, true);
		}

		let icon = this._messageContainer.addChild(APP.library.getSprite("dialogs/force_sit_out/icon"));
		icon.position.set(0, -101);
	}

	get _supportedButtonsCount ()
	{
		return 1;
	}
}

export default GUGameMidCompensateSWView;