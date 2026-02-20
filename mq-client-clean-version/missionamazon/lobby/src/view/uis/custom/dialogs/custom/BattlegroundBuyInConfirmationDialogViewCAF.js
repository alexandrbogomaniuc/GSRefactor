import DialogView from '../DialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ActiveGameDialogButton from './game/ActiveGameDialogButton';
import BattlegroundConfirmationBuyInIndicatorView from './BattlegroundConfirmationBuyInIndicatorView';
import Tween from "../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Tween";

export const BTN_TYPE = 
{
	BTN_TYPE_RETURN_TO_LOBBY: 		'BTN_RETURN_TO_LOBBY',
	BTN_TYPE_RETURN_TO_SCOREBOARD: 	'BTN_RETURN_TO_SCOREBOARD'
}

class BattlegroundBuyInConfirmationDialogViewCAF extends DialogView
{
	constructor()
	{
		console.log("is caf buy in confirmation VIEW");
		super();
	}
	
	//INIT VIEW...
	_initDialogView()
	{
		super._initDialogView();
		
		this._overlayContainer.position.set(0, 0);
		this._overlayContainer.visible = false;
		this._baseContainer.position.set(0, 0);
		this._buttonsContainer.position.set(-1, 10.5);
		this._messageContainer.position.set(-6, APP.isCAFMode ? 17 : 52);
		this._spinner.position.set(0, 0);
		this.tween;

		this._fViewStateId_int = BattlegroundBuyInConfirmationDialogViewCAF.VIEW_STATE_ID_DEFAULT;
	}

	showWaitLayer()
	{
		this._overlayContainer.visible = true;
		this._baseContainer.visible = false;
		this._buttonsContainer.visible = false;
		this._messageContainer.visible = false;

		this._spinner.rotation = 0;
		this.tween.play();
	}

	hideWaitLayer()
	{
		this._overlayContainer.visible = false;
		this._baseContainer.visible = true;
		this._buttonsContainer.visible = true;
		this._messageContainer.visible = true;

		this.tween.stop();
	}

	changeButtons(btnType)
	{
		this._fBtnType = btnType;
		this._destroyButtons();
		this._addButtons();
	}

	_addDialogBase()
	{	
		//WAIT CONTENT...
		this._fBtnType = BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY;
		//OVERLAY...

		let lWaitOverlay_g = new PIXI.Graphics();
		lWaitOverlay_g.beginFill(0x000000).drawRect(0, 0, 960, 540).endFill();
		lWaitOverlay_g.alpha = 0.8;;
		lWaitOverlay_g.position.set(-960 / 2, -540 / 2);
		this._overlayContainer.addChild(lWaitOverlay_g);
		//...OVERLAY

		//SPINNER...
		let myCanvas = APP.preLoader.spinnerCanvas;
		this._spinner = new PIXI.Sprite(PIXI.Texture.from(myCanvas));
		this._spinner.anchor.set(0.5);
		this._overlayContainer.addChild(this._spinner);

		this.tween = new Tween(this._spinner, 'rotation', 0, Math.PI * 2, 1000);
		this.tween.autoRewind = true;
		//...SPINNER
		//...WAIT CONTENT

		//OVERLAY...
		let lOverlay_g = new PIXI.Graphics();
		lOverlay_g.beginFill(0x000000).drawRect(0, 0, 960, 540).endFill();
		lOverlay_g.alpha = 0.8;;
		lOverlay_g.position.set(-960 / 2, -540 / 2);
		this._baseContainer.addChild(lOverlay_g);
		//...OVERLAY

		//DIALOG BASE...
		let lFrame_g = null;
		if (APP.isCAFMode)
		{
			lFrame_g = this.addChild(APP.library.getSprite("dialogs/battleground/frame"));
			lFrame_g.scale.set(1.155, 0.707);
		}
		else
		{
			lFrame_g = this.addChild(APP.library.getSprite("dialogs/battleground/frame"));
			lFrame_g.scale.set(1.155, 1.163);
		}

		this._baseContainer.addChild(lFrame_g);
		//...DIALOG BASE

		//LOGO...
		let logo = I18.generateNewCTranslatableAsset("TABattlegroundDialogLogo");
		logo.position.set(APP.isCAFMode ? -1: -3, APP.isCAFMode ? -113 : -165.5);
		this._baseContainer.addChild(logo);
		//...LOGO

		//TEXTS...
		this._messageContainer.destroyChildren();

		if (!APP.isCAFMode)
		{
			//TEXTS...
			let msg = I18.generateNewCTranslatableAsset("TABattlegroundRoomBuyIn");
			msg.position.set(-62, -118);
			this._messageContainer.addChild(msg);
			//...TEXTS
			
			//...SEPARATE
			let lSeparate_spr = this._baseContainer.addChild(APP.library.getSprite("dialogs/battleground/separate_line"));
			lSeparate_spr.position.set(0, -20);
			lSeparate_spr.scale.set(2, 1);
			//SEPARATE...

			//BUY-IN COST INDICATOR...
			let l_bcbiiv = new BattlegroundConfirmationBuyInIndicatorView();
			l_bcbiiv.position.set(114, -113.5);
			this._fBuyInCostIndicatorView_bcbiiv = l_bcbiiv;
			this._messageContainer.addChild(l_bcbiiv);
			//...BUY-IN COST INDICATOR
		}
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				let l_ta = APP.isCAFMode ? "TABattlegroundReadyButtonCaption" : "TABattlegroundJoinButtonCaption";
				lButton_btn = new ActiveGameDialogButton("lobby/battleground/active_btn", l_ta, undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_ACCEPT, false, true);
				lButton_btn.isOkButton = true;
				break;
			case 1:
				switch (this._fBtnType)
				{
					case BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY:
						lButton_btn = new ActiveGameDialogButton("dialogs/battleground/confirm_buy_in/return_to_lobby_button", "TABattlegroundBackToLobbyButtonCaption", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_CANCEL, false);
						break;
					case BTN_TYPE.BTN_TYPE_RETURN_TO_SCOREBOARD:
						lButton_btn = new ActiveGameDialogButton("dialogs/battleground/confirm_buy_in/return_to_lobby_button","TABattlegroundBackToResults", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_CANCEL, false);
						break;
					default:
						throw new Error (`Unsupported button type: ${this._fBtnType}`)
				}
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


		switch (aIntButtonId_int)
		{
			case 0:
				lYOffset_num = APP.isCAFMode ? -32 : 26;
				break;
			case 1:
				lYOffset_num = APP.isCAFMode ? 17 : 75;
				break;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	deactivateOkButton()
	{
		if(this.okButton)
		{

			this.okButton.enabled = false;
			this.okButton.alpha = 0.3;
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

	updateBuyInCostIndicator(aValue_num)
	{
		this._fBuyInCostIndicatorView_bcbiiv.applyValue(aValue_num);
	}
}

export default BattlegroundBuyInConfirmationDialogViewCAF;