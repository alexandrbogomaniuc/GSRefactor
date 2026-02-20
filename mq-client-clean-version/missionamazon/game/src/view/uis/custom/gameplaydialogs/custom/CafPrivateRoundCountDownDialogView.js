import GameplayDialogView from '../GameplayDialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ActiveGameDialogButton from './ActiveGameDialogButton';
import BattlegroundCountDownView from './BattlegroundCountDownView';
import MTimeLine from "../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine";

class CafPrivateRoundCountDownDialogView extends GameplayDialogView
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
		let lCenterY = 0;

		this._baseContainer.position.set(lCenterX, lCenterY);
		this._messageContainer.position.set(lCenterX - 8, lCenterY + 15);
	}

	_addDialogBase()
	{
		let lOverlay_g = new PIXI.Graphics();
        lOverlay_g.beginFill(0x000000).drawRect(0, 0, 960, 540).endFill();
        lOverlay_g.alpha = 0.5;
        lOverlay_g.position.set(-960 / 2, -540 / 2);
        this._overlayContainer = this._baseContainer.addChild(lOverlay_g);

		this._fBackContainer_sprt = this.addChild(APP.library.getSprite("gameplay_dialogs/battleground/count_down/count_down_frame"));
		this._baseContainer.addChild(this._fBackContainer_sprt);

		//LOGO...
		let logo = I18.generateNewCTranslatableAsset("TABattlegroundRoundResultLogo");
		logo.position.set(-1, -113);
		this._logo = this._messageContainer.addChild(logo);
		//...LOGO

		//TEXTS...
		let msg = "";
		
		if(APP.isCAFMode)
		{
			this._fBackContainer_sprt.visible = true;
			msg = I18.generateNewCTranslatableAsset("TACafPrivateRoundEnds");
			this._logo.visible = true; 
			this._overlayContainer.visible = false;
		}else{
			this._fBackContainer_sprt.visible = false;
			msg = I18.generateNewCTranslatableAsset("TAPVPRoundEnds");
			this._logo.visible = false; 
			this._overlayContainer.visible = true;
		}
		
		
		msg.position.set(1, -61);
		this._messageContainer.addChild(msg);
		//...TEXTS

		//COUNTER...
		let l_bcdv = new BattlegroundCountDownView();
		l_bcdv.position.set(1, 0);

		this._baseContainer.addChild(l_bcdv);
		this._fCounter = l_bcdv;
		//...COUNTER

		//BLINKING TIMELINE...
		let l_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fCounter,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0, 3],
				5,
				[1, 3],
				5,
			]);

		this._fBlinkingTimerTimeline_mtl = l_mtl;
		//...BLINKING TIMELINE
	}

	updateTimeIndicator(aTimeLeft_str)
	{
		this._fCounter.applyValue(aTimeLeft_str);
	}

	startBlinkingAnimation()
	{
		if(
			this._fBlinkingTimerTimeline_mtl &&
			!this._fBlinkingTimerTimeline_mtl.isPlaying()
			)
		{
			this._fBlinkingTimerTimeline_mtl.playLoop();
		}
	}

	stopBlinkingAnimation()
	{
		if(
			this._fBlinkingTimerTimeline_mtl &&
			this._fBlinkingTimerTimeline_mtl.isPlaying()
			)
		{
			this._fBlinkingTimerTimeline_mtl.stop();
		}
			
		this._fCounter.alpha = 1;
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				lButton_btn = new ActiveGameDialogButton("gameplay_dialogs/battleground/count_down/back_to_lobby_button", "TABattlegroundBackToLobbyButtonCaption", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_CANCEL, false);
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
				lYOffset_num -= 22;
				break;
		}
		aButtonView_domdb.caption.position.set(0, -3);
		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	get _supportedButtonsCount ()
	{
		return 1;
	}
}

export default CafPrivateRoundCountDownDialogView;