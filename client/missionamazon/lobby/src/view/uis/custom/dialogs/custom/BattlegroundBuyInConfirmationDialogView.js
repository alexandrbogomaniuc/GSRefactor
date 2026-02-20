import DialogView from '../DialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ActiveGameDialogButton from './game/ActiveGameDialogButton';
import BattlegroundConfirmationBuyInIndicatorView from './BattlegroundConfirmationBuyInIndicatorView';
import Tween from "../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Tween";
import BattlegroundPvPListItem from './BattlegroundPvPListItem';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import BattlegroundCountDownView from '../../../../../../../game/src/view/uis/custom/gameplaydialogs/custom/BattlegroundCountDownView';
export const BTN_TYPE = 
{
	BTN_TYPE_RETURN_TO_LOBBY: 		'BTN_RETURN_TO_LOBBY',
	BTN_TYPE_RETURN_TO_SCOREBOARD: 	'BTN_RETURN_TO_SCOREBOARD'
}

class BattlegroundBuyInConfirmationDialogView extends DialogView
{
	constructor()
	{
		super();
	}
	
	//INIT VIEW...
	_initDialogView()
	{
		this._activeOkButtonCaption = "TABattlegroundJoinButtonCaptionBlack";
		super._initDialogView();
		
		this._overlayContainer.position.set(0, 0);
		this._overlayContainer.visible = false;
		this._baseContainer.position.set(0, 0);
		this._buttonsContainer.position.set(-1, 10.5);
		this._messageContainer.position.set(-6, 52);
		this._spinner.position.set(0, 0);
		this.tween;
		
		this._fViewStateId_int = BattlegroundBuyInConfirmationDialogView.VIEW_STATE_ID_DEFAULT;
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


	showPleaseWait()
	{
		this._activeOkButtonCaption = "TABattlegroundPreparingRoundButtonCaption";
		this.okButton.captionId = this._activeOkButtonCaption;
	}

	hidePleaseWait()
	{
		this._activeOkButtonCaption = "TABattlegroundJoinButtonCaptionBlack";
		this.okButton.captionId = this._activeOkButtonCaption;
	}

	
	disableBetButton()
	{
		this.okButton.enabled = false;
		this.okButton.alpha = 0.5;
	}

	enableBetButton()
	{
		this.okButton.enabled = true;
		this.okButton.alpha = 1;
	}

	_addDialogBase()
	{	
		this._totalPlayers = 6;
		this._playerCells = [];
		this._fTimeToStart_int = null;

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
		let lFrame_g = this.addChild(APP.library.getSprite("dialogs/battleground/frame"));
		lFrame_g.scale.set(1.22, 1.163);

		this._baseContainer.addChild(lFrame_g);
		//...DIALOG BASE

		//LOGO...
		let logo = I18.generateNewCTranslatableAsset("TABattlegroundDialogLogo");
		logo.position.set(-3, -165.5);
		this._baseContainer.addChild(logo);
		//...LOGO

		//TEXTS...
		this._messageContainer.destroyChildren();
		this._usersContainer = this.addChild(new Sprite());
		this._usersContainer.position.set(-100,-72);

		for (let i = 0; i < this._totalPlayers; i++)
		{
			this._addItem(i);
		}

		this._btgCounter = this.addChild(new BattlegroundCountDownView());
		this._btgCounter.applyValue("--:--:--");
		this._btgCounter.scale.set(0.6,0.6);
		this._btgCounter.position.set(130,7);
	}

	_addItem(aIndex_int)
	{
		let lItemData_obj = { index: aIndex_int };

		let lItem_bppsli = this._usersContainer.addChild(new BattlegroundPvPListItem(lItemData_obj));
		lItem_bppsli.y = aIndex_int*27.5;
		this._playerCells.push(lItem_bppsli);
	}

	updateTimeToStart(aTimeToStart_int)
	{
		if(aTimeToStart_int)
		{
			this._fTimeToStart_int = aTimeToStart_int;
		}else{
			this._fTimeToStart_int = undefined;
		}
		
		this._btgCounter.applyValue(this.getFormattedTimeToStart(false));
	}

	getFormattedTimeToStart(aOptIsHHRequired_bl)
	{
		if(this._fTimeToStart_int === undefined)
		{
			return aOptIsHHRequired_bl ? "--:--:--" : "--:--"
		}

		let lSecondsCount_int = Math.round(this.getTimeToStartInMillis() / 1000);

		let hh = Math.floor(lSecondsCount_int / 60 / 60);
		let mm = Math.floor(lSecondsCount_int / 60 - hh * 60);
		let ss = lSecondsCount_int % 60;

		let ssPrefix_str = ss < 10 ? "0" : "";
		let mmPrefix_str = mm < 10 ? "0" : "";
		let hhPrefix_str = hh < 10 ? "0" : "";

		if(aOptIsHHRequired_bl)
		{
			return hhPrefix_str + hh + ":" + mmPrefix_str + mm + ":" + ssPrefix_str + ss;
		}

		return mmPrefix_str + mm + ":" + ssPrefix_str + ss;
	}

	getTimeToStartInMillis()
	{
		if(this._fTimeToStart_int === undefined)
		{
			return undefined;
		}

		let lDelta_num = this._fTimeToStart_int - Date.now();

		if(lDelta_num < 0)
		{
			return 0;
		}

		return lDelta_num;
	}

	updateObservers(observers)
	{
		for(let i=0; i<this._playerCells.length; i++)
		{
			const cell = this._playerCells[i];
			const dataFeed = observers[i];
			if(dataFeed)
			{	
				cell.update(dataFeed, i+1);
			}else
			{
				cell.empty();
			}
		}
	}


	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				let l_ta = this._activeOkButtonCaption;
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
		var lXOffset_num = 125;
		var lYOffset_num = 0;
		
		switch (aIntButtonId_int)
		{
			case 0:
				lYOffset_num = -68;
				break;
			case 1:
				lYOffset_num = 54;
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
		//this._fBuyInCostIndicatorView_bcbiiv.applyValue(aValue_num);
	}
}

export default BattlegroundBuyInConfirmationDialogView;