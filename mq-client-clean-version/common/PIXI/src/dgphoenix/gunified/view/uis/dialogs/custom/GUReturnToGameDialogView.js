import { APP } from '../../../../../unified/controller/main/globals';
import GUDialogView from '../GUDialogView';
import GUSLobbyGameDialogButton from './game/GUSLobbyGameDialogButton';
import GUSLobbyButton from '../../GUSLobbyButton';
import I18 from '../../../../../unified/controller/translations/I18';
import Sprite from '../../../../../unified/view/base/display/Sprite';

class GUReturnToGameDialogView extends GUDialogView
{
	constructor(aBackAssetName_str)
	{
		super();

		this._fMaxBetLevel_num = null;
		this._fBackAssetName_str = aBackAssetName_str;
		this._addBaseTexture();
	}

	setMessage(messageId, aOptRoomId_int = undefined, aOptCurrencySymbol_str = undefined, aOptBulletCost_num = undefined)
	{
		this._setMessage(messageId, aOptRoomId_int, aOptCurrencySymbol_str, aOptBulletCost_num);
	}

	//INIT VIEW...
	//override
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

		let lIcon_spr = this._baseContainer.addChild(APP.library.getSprite("dialogs/force_sit_out/icon"));
		lIcon_spr.position.set(-294, -174.5);
		lIcon_spr.scale.x = -1;

		let line = this._baseContainer.addChild(APP.library.getSprite("dialogs/line"));
		line.scale.x = 0.66;
		line.position.set(0, 32);
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
		let lXOffset_num = 0;
		let lYOffset_num = 0;

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

	_setMessage(messageId, aOptRoomId_int = undefined, aOptBulletCost_num = undefined)
	{
		this._messageContainer.destroyChildren();
		let lMinBulletCost_num = aOptBulletCost_num;
		let lMaxBulletCost_num = aOptBulletCost_num * this._maxBetLevel;

		let msg = I18.generateNewCTranslatableAsset(messageId);
		msg.text = msg.text.replace("/ROOM_ID/", aOptRoomId_int).replace("/MIN_VALUE/", APP.currencyInfo.i_formatNumber(lMinBulletCost_num, true)).replace("/MAX_VALUE/", APP.currencyInfo.i_formatNumber(lMaxBulletCost_num, true));
		this._messageContainer.addChild(msg);
		msg.position.set(0, -60);

		let lTitle_cta = this._messageContainer.addChild(I18.generateNewCTranslatableAsset("TADialogRTGCaption"));
		lTitle_cta.position.set(-265, -172.5);

		msg = I18.generateNewCTranslatableAsset("TADialogRTGNote");
		msg.position.set(0, 70);
		this._messageContainer.addChild(msg);
	}

	get _maxBetLevel()
	{
		return this._fMaxBetLevel_num || (this._fMaxBetLevel_num = this._initMaxBetLevel());
	}

	_initMaxBetLevel()
	{
		this._fMaxBetLevel_num = APP.playerController.info.possibleBetLevels ? Math.max.apply(null, APP.playerController.info.possibleBetLevels): 1;

		return this._fMaxBetLevel_num;
	}

	get _supportedButtonsCount ()
	{
		return 3;
	}

	destroy()
	{
		this._fMaxBetLevel_num = null;

		super.destroy();
	}
}

export default GUReturnToGameDialogView;