import GUDialogView from '../GUDialogView';
import { APP } from '../../../../../unified/controller/main/globals';
import I18 from '../../../../../unified/controller/translations/I18';
import GUSLobbyNonActiveBattlegroundDialogButton from './game/GUSLobbyNonActiveBattlegroundDialogButton';
import GUSLobbyActiveGameDialogButton from './game/GUSLobbyActiveGameDialogButton';

class GUSLobbyBattlegroundNotEnoughPlayersDialogView extends GUDialogView
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
		this._buttonsContainer.position.set(0, 0);
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
		let lFrame_g = this.addChild(APP.library.getSprite("dialogs/battleground/not_enough_players/frame"));
		this._baseContainer.addChild(lFrame_g);
		//...DIALOG BASE

		//LOGO...
		let logo = I18.generateNewCTranslatableAsset("TABattlegroundDialogLogo");
		logo.position.set(0, -143);
		logo.scale.set(0.85);
		this._baseContainer.addChild(logo);
		//...LOGO

		//TEXTS...
		this._messageContainer.destroyChildren();

		//TEXTS...
		let msg = I18.generateNewCTranslatableAsset("TABattlegroundNotEnoughtPlayersDialogText");
		msg.position.set(0, -25);
		msg.scale.set(1.2);
		this._messageContainer.addChild(msg);
		//...TEXTS

	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				lButton_btn = new GUSLobbyNonActiveBattlegroundDialogButton("dialogs/battleground/not_enough_players/no_active_button", "TABattlegroundButtonChangeWorldBuyIn", undefined, undefined, GUSLobbyNonActiveBattlegroundDialogButton.BUTTON_TYPE_ACCEPT, false);
				lButton_btn.isCancelButton = true;
				break;
			case 1:
				lButton_btn = new GUSLobbyActiveGameDialogButton("dialogs/battleground/not_enough_players/active_button", "TABattlegroundContinueWaiting", undefined, undefined, GUSLobbyActiveGameDialogButton.BUTTON_TYPE_CANCEL, false, true);
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
		var lYOffset_num = 70;

		switch(aIntButtonId_int)
		{
			case 0:
				lXOffset_num = -90;
				break;
			case 1:
				lXOffset_num = 90;
				break;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	get _supportedButtonsCount ()
	{
		return 2;
	}

	deactivateOkButton()
	{
		this.okButton.enabled = false;
		if (this.okButton.disabledView) this.okButton.disabledView.visible = false;
		this.okButton.alpha = 0.7;
	}

	activateOkButton()
	{
		this.okButton.enabled = true;
		this.okButton.alpha = 1;
	}
}

export default GUSLobbyBattlegroundNotEnoughPlayersDialogView;