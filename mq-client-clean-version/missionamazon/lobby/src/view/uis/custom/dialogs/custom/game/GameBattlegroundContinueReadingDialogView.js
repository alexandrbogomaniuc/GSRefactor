import DialogView from '../../DialogView';
import I18 from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AlignDescriptor from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';
import TextField from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import NumberValueFormat from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import ActiveGameDialogButton from './../game/ActiveGameDialogButton';
import NonActiveBattlegroundDialogButton from './../game/NonActiveBattlegroundDialogButton';

import Sprite from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class GameBattlegroundContinueReadingDialogView extends DialogView
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
	}
	
	_addDialogBase()
	{

		//OVERLAY...
		let lOverlay_g = new PIXI.Graphics();
		lOverlay_g.beginFill(0x000000).drawRect(0, 0, 960, 540).endFill();
		lOverlay_g.alpha = 0.5;
		lOverlay_g.position.set(-960 / 2, -540 / 2);
		this._baseContainer.addChild(lOverlay_g);
		//...OVERLAY

		//DIALOG BASE...
		let lFrame_g = this.addChild(APP.library.getSprite("dialogs/battleground/frame"));
		lFrame_g.scale.set(0.90);
		this._baseContainer.addChild(lFrame_g);
		//...DIALOG BASE

		//LOGO...
		let logo = I18.generateNewCTranslatableAsset("TABattlegroundDialogLogo");
		logo.position.set(0, -121);
		logo.scale.set(0.76);
		this._baseContainer.addChild(logo);
		//...LOGO

		//TEXTS...
		this._messageContainer.destroyChildren();

		//TEXTS...
		let msg1 = I18.generateNewCTranslatableAsset("TABattlegroundRoundBeginsDialogText");
		msg1.position.set(-25, -65);
		this._messageContainer.addChild(msg1);

		let msg2 = I18.generateNewCTranslatableAsset("TABattlegroundContinueReadingDialogText");
		msg2.position.set(0, -38);
		this._messageContainer.addChild(msg2);

		let msg3 = I18.generateNewCTranslatableAsset("TABattlegroundDoingNothingText");
		msg3.position.set(0, 8);
		this._messageContainer.addChild(msg3);
		//...TEXTS

		this._fTimer_tf = this.addChild(new TextField(this._timerStyle));
		this._fTimer_tf.position.set(50, -75);
		this._messageContainer.addChild(this._fTimer_tf);
		this.updateTimeIndicator();
	}

	updateTimeIndicator(aFormatedValue_str = "00:00")
	{
		if(this._fTimer_tf)
		{
			if(!(aFormatedValue_str == "00:00") || !(aFormatedValue_str == "--:--"))
			{
				this._fTimer_tf.text = aFormatedValue_str;
			}
		}
	}

	get _timerStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 18,
			align: "left",
			fill: 0xfccc32,//0xfccc32 : 0xffffff
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 2,
			dropShadowAlpha: 1
		};

		return lStyle_obj;
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				lButton_btn = new NonActiveBattlegroundDialogButton("dialogs/battleground/not_enough_players/no_active_button", "TABattlegroundDialogButtonYes", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_ACCEPT);
				lButton_btn.isOkButton = true;
				break;
			case 1:
				lButton_btn = new ActiveGameDialogButton("dialogs/battleground/not_enough_players/active_button", "TABattlegroundDialogButtonNo", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_CANCEL);
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

		switch(aIntButtonId_int)
		{
			case 0:
				lXOffset_num = -80;
				break;
			case 1:
				lXOffset_num = 80;
				break;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
		aButtonView_domdb.scale.set(0.85);
	}
	//...INIT VIEW

	deactivateOkButton()
	{
		if(this.okButton)
		{
			this.okButton.enabled = false;
			if(this.okButton.disabledView) this.okButton.disabledView.visible = false;
			this.okButton.alpha = 0.7;
		}
	}

	activateOkButton()
	{
		if(this.okButton)
		{
			this.okButton.enabled = true;
			this.okButton.alpha = 1;
		}
	}

	get _supportedButtonsCount ()
	{
		return 2;
	}
}

export default GameBattlegroundContinueReadingDialogView