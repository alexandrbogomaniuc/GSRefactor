import DialogView from '../../DialogView';
import I18 from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Sprite from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ActiveGameDialogButton from './ActiveGameDialogButton';
import GameDialogButton from './GameDialogButton';
import Button from '../../../../../../ui/LobbyButton';
import BattlegroundRefundIndicatorView from './BattlegroundRefundIndicatorView';

class GameBattlegroundNoWeaponsFiredDialogView extends DialogView
{
	constructor()
	{
		super();
	}

	//INIT VIEW...
	_initDialogView()
	{
		super._initDialogView();

		this._overlayContainer.visible = false;
		this._baseContainer.position.set(0, 0);
		this._buttonsContainer.position.set(-1, 35.5);
		this._messageContainer.position.set(-6, 15);
		this._commentContainer.position.set(0, -54);
		this._refundContainer.position.set(-6, 15);

		this._fViewStateId_int = GameBattlegroundNoWeaponsFiredDialogView.VIEW_STATE_ID_DEFAULT;
	}

	_addDialogBase()
	{
		this._commentContainer = this.addChild(new Sprite);
		this._refundContainer = this.addChild(new Sprite);

		//OVERLAY...
		let lOverlay_g = new PIXI.Graphics();
		lOverlay_g.beginFill(0x000000).drawRect(0, 0, 960, 540).endFill();
		lOverlay_g.alpha = 0.8;;
		lOverlay_g.position.set(-960 / 2, -540 / 2);
		this._baseContainer.addChild(lOverlay_g);
		//...OVERLAY

		//DIALOG BASE...
		let lFrame_g = this.addChild(new Sprite());

		let lFrame_img = lFrame_g.addChild(APP.library.getSprite("dialogs/battleground/frame"));
		lFrame_img.y = -25;
		lFrame_g.scale.set(1.067, 1.075);

	
		let lLine_spr = lFrame_g.addChild(APP.library.getSprite("dialogs/battleground/separate_line"));
		lLine_spr.position.set(5, -75);
		lLine_spr.scale.set(3, 1);
		this._baseContainer.addChild(lFrame_g);
		//...DIALOG BASE

		//LOGO...
		let logo = I18.generateNewCTranslatableAsset("TABattlegroundDialogLogo");
		logo.position.set(-1, -165.5);
		this._baseContainer.addChild(logo);
		//...LOGO

		//TEXTS...
		this._messageContainer.destroyChildren();

		let msg = I18.generateNewCTranslatableAsset("TABattlegroundNoWeaponsFiredDialogText");
		msg.position.set(6, -118);
		this._messageContainer.addChild(msg);

		let comment = I18.generateNewCTranslatableAsset("TABattlegroundNoWeaponsFiredDialogAllPlayersText");
		//msg.position.set(6, -69);
		this._commentContainer.addChild(comment);
		//...TEXTS

		//REFUND...
		let rfnd = I18.generateNewCTranslatableAsset("TABattlegroundNoWeaponsFiredDialogRefundText");
		rfnd.position.set(-37, -23);
		this._refundContainer.addChild(rfnd);

		//REFUND INDICATOR...
		let l_briv = new BattlegroundRefundIndicatorView();
		l_briv.position.set(rfnd.x + rfnd.getBounds().width / 2 - 10, -38);
		this._fRefundIndicatorView_briv = l_briv;
		this._refundContainer.addChild(l_briv);
		//...REFUND INDICATOR

		rfnd = I18.generateNewCTranslatableAsset("TABattlegroundNoWeaponsFiredDialogRefundIsText");
		rfnd.position.set(6, 2);
		this._refundContainer.addChild(rfnd);
		//...REFUND
	}

	__retreiveDialogButtonViewInstance (aIntId_int)
	{
		let lButton_btn;
		switch (aIntId_int)
		{
			case 0:
				let lReturnToLobbyCaptionId_str = APP.isCAFMode ? "TABattlegroundButtonNWsFBackToLobby" : "TABattlegroundButtonChangeWorldBuyIn";
				lButton_btn = new GameDialogButton("dialogs/battleground/not_enough_players/no_active_button", "dialogs/battleground/not_enough_players/no_active_button", lReturnToLobbyCaptionId_str,  undefined, undefined, ActiveGameDialogButton.BUTTON_TYPE_CANCEL, false);
				lButton_btn.isCancelButton = true;
				break;
			case 1:
				lButton_btn = new ActiveGameDialogButton("dialogs/battleground/no_weapons_fired/active_button", "TABattlegroundNoWeaponsFiredDialogPlayAgainButton", undefined, undefined,  ActiveGameDialogButton.BUTTON_TYPE_ACCEPT, false, true);
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
		var lYOffset_num = 0;

		switch (aIntButtonId_int)
		{
			case 0:
				lXOffset_num = -94;
				lYOffset_num = 36;
				break;
			case 1:
				lXOffset_num = 100;
				lYOffset_num = 36;
				break;
		}

		aButtonView_domdb.position.set(lXOffset_num, lYOffset_num);
	}
	//...INIT VIEW

	updateRefundIndicator(aValue_num)
	{
		this._fRefundIndicatorView_briv.applyValue(aValue_num);
	}

	showRefundView(aShowRefundView)
	{
		if(aShowRefundView)
		{
			this._refundContainer.visible = true;
			this._commentContainer.position.set(0, -54);
		}
		else
		{
			this._refundContainer.visible = false;
			this._commentContainer.position.set(0, -36);
		}
	}

	get _supportedButtonsCount ()
	{
		return 2;
	}
}

export default GameBattlegroundNoWeaponsFiredDialogView;