import GameplayDialogView from '../GameplayDialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ActiveGameDialogButton from './ActiveGameDialogButton';
import BattlegroundCountDownView from './BattlegroundCountDownView';

class BattlegroundCountDownDialogView extends GameplayDialogView
{
	constructor()
	{
		super();
	}

	//INIT VIEW...
	_initDialogView()
	{
		super._initDialogView();

		let lCenterX = 0;
		let lCenterY = -20;

		this._baseContainer.position.set(lCenterX, lCenterY);
		this._messageContainer.position.set(lCenterX - 8, lCenterY + 15);
	}
	
	_addDialogBase()
	{
		this._fBackContainer_sprt = this.addChild(APP.library.getSprite("gameplay_dialogs/battleground/count_down/count_down_frame_dialog"));
		this._fBackContainer_sprt.scale.set(1.5);
		this._baseContainer.addChild(this._fBackContainer_sprt);
		this._fBackContainer_sprt.y = -10;

		//LOGO...
		let logo = I18.generateNewCTranslatableAsset("TABattlegroundCountDownLogo");
		logo.scale.set(1.5);
		logo.position.set(-350, -270);
		this._messageContainer.addChild(logo);
		//...LOGO

		//TEXTS...
		let msg = I18.generateNewCTranslatableAsset(APP.isCAFMode ? "TACafPrivateBattlegroundWaitingForHost" : "TABattlegroundStartsIn");
		APP.isCAFMode ? 0 : msg.scale.set(1.5);
		msg.position.set(APP.isCAFMode ? 8 : 8, APP.isCAFMode ? -27: -40);
		this._messageContainer.addChild(msg);
		//...TEXTS

		if(!APP.isCAFMode)
		{
			//COUNTER...
			let l_bcdv = new BattlegroundCountDownView();
			l_bcdv.position.set(1, 20);
		
			this._baseContainer.addChild(l_bcdv);
			this._fCounter = l_bcdv;
			//...COUNTER
		}
	}

	updateIfTutorial()
	{
		let lIsTutorialDisplayed_bl = APP.gameScreen.gameField.battlegroundTutorialController.isTutorialDisplayed;
		if (lIsTutorialDisplayed_bl)
		{
			if(APP.isMobile)
			{
				let lSpot = APP.gameScreen.gameField.spot;
				if(lSpot)
				{
					let lYOffset = lSpot.isBottom ? 20 : -20;
					this.scale.set(0.8);
					this._bgContainer.scale.set(this._bgContainer.scale*1.25);
					this.position.set(0, -lYOffset);
					this._bgContainer.position.set(0, lYOffset);
				}
			}
			else
			{
				this.scale.set(0.5);
				this._bgContainer.scale.set(this._bgContainer.scale*2);
			}
		}
		else if (!APP.gameScreen.gameField.battlegroundTutorialController.isTutorialDisplayed)
		{
			this.scale.set(1);
			this.position.set(0, 0);
			this._bgContainer.position.set(0, 0);
		}
	}

	updateTimeIndicator(aTimeLeft_str)
	{
		if(APP.isCAFMode) return;
		
		this._fCounter.applyValue(aTimeLeft_str);
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				let l_ta = APP.isCAFMode ? "TABattlegroundCancelButton" : "TABattlegroundBackToBuyIn";
				let lAssetBg_a = APP.isCAFMode ? "gameplay_dialogs/battleground/count_down/active_btn" : "gameplay_dialogs/battleground/count_down/back_to_lobby_button";
				lButton_btn = new ActiveGameDialogButton(lAssetBg_a, l_ta, undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_CANCEL, false);
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
				lYOffset_num -= 2;
				break;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	_deactivateCancelButton()
	{
		if(this.cancelButton)
		{
			this.cancelButton.enabled = false;
			this.cancelButton.alpha = 0.3;
		}
	}

	_activateCancelButton()
	{
		if(this.cancelButton)
		{
			this.cancelButton.enabled = true;
			this.cancelButton.alpha = 1;
		}
	}

	get _supportedButtonsCount ()
	{
		return 1;
	}
}

export default BattlegroundCountDownDialogView;