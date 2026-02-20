import GUDialogView from '../../GUDialogView';
import Sprite from '../../../../../../unified/view/base/display/Sprite';
import { APP } from '../../../../../../unified/controller/main/globals';
import Tween from '../../../../../../unified/controller/animation/Tween';
import I18 from '../../../../../../unified/controller/translations/I18';
import GUSLobbyBattlegroundRefundIndicatorView from './GUSLobbyBattlegroundRefundIndicatorView';
import GUSLobbyActiveGameDialogButton from './GUSLobbyActiveGameDialogButton';
import GUSLobbyGameDialogButton from './GUSLobbyGameDialogButton';

class GUSGameBattlegroundNoWeaponsFiredDialogView extends GUDialogView
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
		this._buttonsContainer.position.set(-1, 35.5);
		this._messageContainer.position.set(-6, 15);
		this._commentContainer.position.set(0, -54);
		this._refundContainer.position.set(-6, 15);
		this._spinner.position.set(0, 0);
		this.tween;
	}

	showWaitLayer()
	{
		this._overlayContainer.visible = true;
		this._baseContainer.visible = false;
		this._buttonsContainer.visible = false;
		this._messageContainer.visible = false;
		this._commentContainer.visible = false;
		this._refundContainer.visible = false;
		this._spinner.rotation = 0;
		this.tween.play();
	}

	hideWaitLayer()
	{
		this._overlayContainer.visible = false;
		this._baseContainer.visible = true;
		this._buttonsContainer.visible = true;
		this._messageContainer.visible = true;
		this._commentContainer.visible = true;
		this._refundContainer.visible = true;

		this.tween.stop();
	}

	_addDialogBase()
	{
		this._commentContainer = this.addChild(new Sprite);
		this._refundContainer = this.addChild(new Sprite);

		//WAIT CONTENT...
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
		lOverlay_g.alpha = 0.8;
		lOverlay_g.position.set(-960 / 2, -540 / 2);
		this._baseContainer.addChild(lOverlay_g);
		//...OVERLAY

		//DIALOG BASE...
		let lFrame_g = this.addChild(APP.library.getSprite("dialogs/battleground/no_weapons_fired/frame"));
		lFrame_g.position.set(18, 0);
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
		let l_briv = new GUSLobbyBattlegroundRefundIndicatorView();
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
				lButton_btn = new GUSLobbyGameDialogButton("dialogs/battleground/not_enough_players/no_active_button", "dialogs/battleground/not_enough_players/no_active_button", "TABattlegroundButtonChangeWorldBuyIn",  undefined, undefined, GUSLobbyGameDialogButton.BUTTON_TYPE_CANCEL, false);
				lButton_btn.isCancelButton = true;
				break;
			case 1:
				lButton_btn = new GUSLobbyActiveGameDialogButton("dialogs/battleground/no_weapons_fired/active_button", "TABattlegroundNoWeaponsFiredDialogPlayAgainButton", undefined, undefined,  GUSLobbyActiveGameDialogButton.BUTTON_TYPE_ACCEPT, false, true);
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

export default GUSGameBattlegroundNoWeaponsFiredDialogView;