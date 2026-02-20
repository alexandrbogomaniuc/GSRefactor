import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DialogView from '../DialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Button from '../../../../../ui/LobbyButton';
import GameDialogButton from './game/GameDialogButton';

class ReturnToGameDialogView extends DialogView
{
	constructor()
	{
		super();

		this._fMaxBetLevel_num = null;
	}

	setMessage(messageId, aOptRoomId_int = undefined, aOptBulletCost_num = undefined)
	{
		this._setMessage(messageId, aOptRoomId_int, aOptBulletCost_num);
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
		let lBack_spr = this._baseContainer.addChild(APP.library.getSprite("dialogs/back"));

		let lIcon_spr = lBack_spr.addChild(APP.library.getSprite("dialogs/force_sit_out/icon"));
		lIcon_spr.position.set(-294, -174.5);
		lIcon_spr.scale.x = -1;

		let line = lBack_spr.addChild(APP.library.getSprite("dialogs/line"));
		line.scale.x = 0.66;
		line.position.set(0, 32);
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
		let lMaxBulletCost_num = aOptBulletCost_num * this._fMaxBetLevel;

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

	get _fMaxBetLevel()
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
	}
}

export default ReturnToGameDialogView;