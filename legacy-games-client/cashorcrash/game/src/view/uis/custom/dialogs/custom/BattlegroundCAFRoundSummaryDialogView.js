import DialogView from '../DialogView';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ActiveGameDialogButton from './game/ActiveGameDialogButton';

class BattlegroundCAFRoundSummaryDialogView extends DialogView
{
	constructor()
	{
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
		this._messageContainer.position.set(-6, 17);
	}

	_addDialogBase()
	{	
		//OVERLAY...
		let lOverlay_g = new PIXI.Graphics();
		lOverlay_g.beginFill(0x000000).drawRect(0, 0, 960, 540).endFill();
		lOverlay_g.alpha = 0.8;;
		lOverlay_g.position.set(-960 / 2, -540 / 2);
		this._baseContainer.addChild(lOverlay_g);
		//...OVERLAY

		//DIALOG BASE...
		let lFrame_g = lFrame_g = this._baseContainer.addChild(APP.library.getSprite("dialogs/battleground/frame"));
		lFrame_g.scale.set(1.155, 0.707);
		//...DIALOG BASE

		//LOGO...
		let logo = I18.generateNewCTranslatableAsset("TABattlegroundDialogLogo");
		logo.position.set(-1, -113);
		this._baseContainer.addChild(logo);
		//...LOGO

		//TEXTS...
		this._messageContainer.destroyChildren();

		//TEXTS...
		let msg = I18.generateNewCTranslatableAsset("TACafPrivateBattlegroundRoundAlreadyStarted");
		msg.position.set(6, -50);
		this._messageContainer.addChild(msg);
		//...TEXTS
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				lButton_btn = new ActiveGameDialogButton("dialogs/battleground/confirm_buy_in/return_to_lobby_button", "TABattlegroundBackToLobbyButtonCaption", undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_CANCEL, false);
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
				lYOffset_num = 10;
				break;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	get _supportedButtonsCount ()
	{
		return 1;
	}
}

export default BattlegroundCAFRoundSummaryDialogView;