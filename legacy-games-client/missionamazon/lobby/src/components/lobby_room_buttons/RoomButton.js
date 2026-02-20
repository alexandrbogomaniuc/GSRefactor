import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Button from '../../ui/LobbyButton';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import LobbyBulletRangeCostIndicatorView from '../../view/uis/custom/indicators/LobbyBulletRangeCostIndicatorView';
import RoomIndicatorButton from './RoomIndicatorButton';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import LobbyAPP from './../../LobbyAPP';
import { FRAME_RATE} from '../../../../shared/src/CommonConstants';

const CARDS_Y_OFFSETS =
[
	2,
	2,
	9,
	5,
	-11,
	4
];
const STAKE_POS =
[
	{x: -2, y: 76},
	{x: -2, y: 76},
	{x: 0, y: 70},
	{x: 0, y: 74},
	{x: 0, y: 90},
	{x: -8, y: 76}
];
const BET_CAPTURE_POS = 18;

class RoomButtom extends Sprite
{
	static get EVENT_LOBBY_ROOM_STAKE_SELECTED()					{return "onLobbyRoomStakeSelected";}

	static get EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK()			{return "onRoomWeaponsIndicatorClick"};

	static get INIT_X()							{return 86;}
	static get VISIBLE_OFFSET_X()				{return 140;}
	static get INVISIBLE_OFFSET_X()				{return 340;}
	static get VISIBLE_AMOUNT()					{return 6;}

	static get SKIN_ID_TIN()	{ return 0 }
	static get SKIN_ID_IRON()	{ return 1 }
	static get SKIN_ID_BRONZE()	{ return 2 }
	static get SKIN_ID_LEAD()	{ return 3 }
	static get SKIN_ID_SILVER()	{ return 4 }
	static get SKIN_ID_GOLD()	{ return 5 }

	updateStake(aStake_num, aCurrentSkin_num)
	{
		this._updateStake(aStake_num, aCurrentSkin_num);
	}

	updateIndicators(aWeaponsValue_num)
	{
		this._updateIndicators(aWeaponsValue_num);
	}

	updateWeaponsIndicator(aWeaponsValue_num)
	{
		this._updateWeaponsIndicator(aWeaponsValue_num);
	}

	disableRoomBtn()
	{
		if (!this._fButton_b.enabled) return;
		// see https://jira.dgphoenix.com/browse/MQPRT-12
		// 'interactive' value is invalidating after any listeners removing, and can be set to 'true' on the higher level (see Sprite.js, removeListener function)
		// so we must avoid situations where removing listeners (off) happens after 'interactive' was set to 'false' by ourselves

		if (this._fBulletCostIndicatorView_bciv && !window.isWebGLSupported)
		{
			this._fBulletCostIndicatorView_bciv.position.set(80, 163);
		}

		this._disableButtonInteraction();
		this._disableRoomBtn();

		this._fBulletCostIndicatorView_bciv && (this._fBulletCostIndicatorView_bciv.visible = false);
	}

	enableRoomBtn()
	{
		if (this._fButton_b.enabled) {
			return;
		}

		if (this._fBulletCostIndicatorView_bciv)
		{
			if (!window.isWebGLSupported)
			{
				this._fBulletCostIndicatorView_bciv.position.set(0, 83);
			}

			this._fBulletCostIndicatorView_bciv.visible = true;
		}

		this._enableRoomBtn();
		this._enableButtonInteraction();
	}

	get enabled()
	{
		return this._fButton_b.enabled;
	}

	updateOverState(lobbyScreenVisible_bl)
	{
		if (lobbyScreenVisible_bl)
		{
			this._tryToShowMouseOver();
		}
		else
		{
			this._mouseOut();
		}
	}

	get stake()
	{
		return this._fButtonStake_num;
	}

	updatePosition(aIndex_int, aSkipAnimation_bl, aIsReverseGrow_bl)
	{
		this._updatePosition(aIndex_int, aSkipAnimation_bl, aIsReverseGrow_bl);
	}

	constructor(aPersonalPosition_int)
	{
		super();

		this._fCards_s_arr = [];
		this._fContentContainer_s = this.addChild(new Sprite());
		this._fCardsContainer_s = this._fContentContainer_s.addChild(new Sprite());
		this._fButtonStake_num = null;
		this._fBulletCostIndicatorView_bciv = null;
		this._fButton_b = null;
		this._fStateIndicator_lbci = null;
		this._fNotEnoughMoneyCapture_cta = null;
		this._fPosition_int = aPersonalPosition_int;

		this._fWeaponsIndicator_b = null;

		// this._initStateIndicator();
		this._addRoomIndicators();
		this._enableButtonInteraction();

		APP.on(LobbyAPP.EVENT_ON_SOME_BONUS_STATE_CHANGED, this._onSomeBonusStateChanged, this);
	}


	_getCard(aSkinId_int)
	{
		if(!this._fCards_s_arr[aSkinId_int])
		{
			let lShadow_sprt = this._fCardsContainer_s.addChild(APP.library.getSprite("lobby/button_incons/shadow_circle"));
			lShadow_sprt.position.set(0,-10);
			let l_btn = this._fCardsContainer_s.addChild(new Button("lobby/button_incons/lobby_room_level_" + (aSkinId_int + 1), null, true));

			l_btn.on("pointerclick", this._onRoomBtnClicked, this);
			l_btn.on("pointerdown", this._onRoomBtnDown, this);
			l_btn.on("mouseover", this._mouseOver, this);
			l_btn.on("mouseout", this._mouseOut, this);

			this._setSkinButton(aSkinId_int);
			l_btn.holder.addChild(this._fStateIndicator);
			l_btn.scale.set(0.9,0.9)

			this._fCards_s_arr[aSkinId_int] = l_btn;
			if(aSkinId_int == 0 && !APP.appParamsInfo.isBattlegroundGame)
			{
				setTimeout(()=>this._onRoomBtnClicked(),0);
			}
		}

		return this._fCards_s_arr[aSkinId_int];
	}

	_onSomeBonusStateChanged()
	{
		if (this._fWeaponsIndicator_b)
		{
			this._fWeaponsIndicator_b.visible = !!APP.isKeepSWModeActive;
		}
	}

	_addNotEnoughMoneyCapture(aCurrentSkin_num)
	{

		if(APP.battlegroundController.info.isBattlegroundMode())
		{
			return; //prevent "BATTLEGROUND" caption overlap
		}

		this._fNotEnoughMoneyCapture_cta = this._fContentContainer_s.addChild(I18.generateNewCTranslatableAsset('TALobbyRoomBtnNotEnoughMoneyCapture'));
		this._fNotEnoughMoneyCapture_cta.position.set(STAKE_POS[aCurrentSkin_num].x, STAKE_POS[aCurrentSkin_num].y + 45);
		this._fNotEnoughMoneyCapture_cta.visible = false;
	}

	_addRoomIndicators()
	{
		this._fWeaponsIndicator_b = this._fContentContainer_s.addChild(new RoomIndicatorButton("common_btn_fire_settings"));
		this._fWeaponsIndicator_b.on("pointerdown", this._onRoomInsicatorBtnDown, this);
		this._fWeaponsIndicator_b.on("pointerclick", this._onRoomWeaponsIndicatorClick, this);
		this._fWeaponsIndicator_b.on("mouseover", this._onWeaponsIndicatorMouseover, this);
		this._fWeaponsIndicator_b.on("mouseout", this._onWeaponsIndicatorMouseout, this);
		APP.isMobile && this._fWeaponsIndicator_b.setHitArea(new PIXI.Rectangle(-22 * 1.5, -9 * 2, 44 * 1.5, 18 * 2));
		this._fWeaponsIndicator_b.position.set(52, -61);

		if (!APP.isKeepSWModeActive)
		{
			this._fWeaponsIndicator_b.visible = false;
		}
	}

	_onRoomWeaponsIndicatorClick()
	{
		this.emit(RoomButtom.EVENT_LOBBY_ROOM_WEAPONS_INDICATOR_CLICK, {value: this.stake})
	}

	_onWeaponsIndicatorMouseover()
	{
		this._fWeaponsIndicator_b.setSelectedView(true);
	}

	_onWeaponsIndicatorMouseout()
	{
		this._fWeaponsIndicator_b.setBaseView(true);
	}

	_disableRoomBtn()
	{
		this._mouseOut();
		this._fButton_b && (this._fButton_b.enabled = false);
		this._fNotEnoughMoneyCapture_cta && (this._fNotEnoughMoneyCapture_cta.visible = true);
	}

	_enableRoomBtn()
	{
		if (this._fButton_b)
		{
			this._fButton_b.enabled = true;

			this._tryToShowMouseOver();
		}
		this._fNotEnoughMoneyCapture_cta && (this._fNotEnoughMoneyCapture_cta.visible = false);
	}

	_tryToShowMouseOver()
	{
		if (!this._fButton_b)
		{
			return;
		}

		let lLocalMouse_obj = this._fButton_b.toLocal(APP.stage.renderer.plugins.interaction.mouse.global);
		let lIsHit_bln = this._fButton_b.hitArea.contains(lLocalMouse_obj.x, lLocalMouse_obj.y);

		if (lIsHit_bln && this._fButton_b.enabled)
		{
			this._mouseOver();
		}
	}

	_mouseOver()
	{
		let lSequenceScale_arr = [
			{tweens: [{ prop: "scale.x", to: 0.95 }, { prop: "scale.y", to: 0.95 }], duration: 1 * FRAME_RATE},
			];
		Sequence.start(this._fButton_b, lSequenceScale_arr );

		this._fWeaponsIndicator_b.setSelectedView();
	}

	_mouseOut()
	{
		let lSequenceScale_arr = [
			{tweens: [{ prop: "scale.x", to: 0.9 }, { prop: "scale.y", to: 0.9 }], duration: 1 * FRAME_RATE},
			];
		Sequence.start(this._fButton_b, lSequenceScale_arr );

		this._fWeaponsIndicator_b.setBaseView();
	}

	_onRoomBtnClicked()
	{
		this.emit(RoomButtom.EVENT_LOBBY_ROOM_STAKE_SELECTED, {value: this.stake})
	}

	_onRoomBtnDown()
	{
		this._fWeaponsIndicator_b.scale.set(0.95);
		this._fWeaponsIndicator_b.position.x = 52 - 3;
		this._fWeaponsIndicator_b.position.y = -61 + 3;

		this._addDocumentEventListeners();
	}

	pointerupHandler()
	{
		this._fButton_b.handleUp();

		this._fWeaponsIndicator_b.handleUp();

		this._fWeaponsIndicator_b.scale.set(1);
		this._fWeaponsIndicator_b.position.x = 52;
		this._fWeaponsIndicator_b.position.y = -61;

		this._removeEventListeners();
	}

	_onRoomInsicatorBtnDown()
	{
		this._addDocumentEventListeners();
	}

	_addDocumentEventListeners()
	{
		if(!!window.PointerEvent)
		{
			document.addEventListener("pointerup", this.pointerupHandler.bind(this));
		}
		else
		{
			document.addEventListener("mouseup", this.pointerupHandler.bind(this));
			document.addEventListener("touchend", this.pointerupHandler.bind(this));
		}
	}

	_removeEventListeners()
	{
		if(!!window.PointerEvent)
		{
			document.removeEventListener("pointerup", this.pointerupHandler.bind(this));
		}
		else
		{
			document.removeEventListener("mouseup", this.pointerupHandler.bind(this));
			document.removeEventListener("touchend", this.pointerupHandler.bind(this));
		}
	}

	_initStateIndicator()
	{
		this._fBulletCostIndicatorView_bciv = new LobbyBulletRangeCostIndicatorView();

		return this._fBulletCostIndicatorView_bciv;
	}

	get _fStateIndicator()
	{
		return this._fBulletCostIndicatorView_bciv || (this._fBulletCostIndicatorView_bciv = this._initStateIndicator());
	}

	_updateStake(aStake, aCurrentSkin_num)
	{
		this._fButtonStake_num = aStake;
		var lSkinId_int = null;

		//SKIN VALIDATION...
		switch (aCurrentSkin_num) {
			case 0:
				lSkinId_int = RoomButtom.SKIN_ID_TIN;
				break;
			case 1:
				lSkinId_int = RoomButtom.SKIN_ID_IRON;
				break;
			case 2:
				lSkinId_int = RoomButtom.SKIN_ID_BRONZE;
				break;
			case 3:
				lSkinId_int = RoomButtom.SKIN_ID_LEAD;
				break;
			case 4:
				lSkinId_int = RoomButtom.SKIN_ID_SILVER;
				break;
			case 5:
				lSkinId_int = RoomButtom.SKIN_ID_GOLD;
				break;
			default:
				lSkinId_int = RoomButtom.SKIN_ID_GOLD;
				break;
		}

		let lCard_b = this._getCard(lSkinId_int);
		lCard_b.visible = true;

		this._fButton_b = lCard_b;
		//...SKIN VALIDATION
	}

	_setSkinButton(aCurrentSkin_num)
	{

		var lStakePos_obj = STAKE_POS[aCurrentSkin_num];

		this._fStateIndicator.updateStake(this._fButtonStake_num);
		this._fStateIndicator.position.set(lStakePos_obj.x, lStakePos_obj.y + (aCurrentSkin_num == 4 ? -1 : 0));

		this._fContentContainer_s.position.y = CARDS_Y_OFFSETS[aCurrentSkin_num];
		this._addNotEnoughMoneyCapture(aCurrentSkin_num)
	}

	_updateIndicators(aWeaponsValue_num)
	{
		this._fWeaponsIndicator_b && this._fWeaponsIndicator_b.update(aWeaponsValue_num);
	}

	_updateWeaponsIndicator(aWeaponsValue_num)
	{
		this._fWeaponsIndicator_b && this._fWeaponsIndicator_b.update(aWeaponsValue_num);
	}

	_updatePosition(aIndex_int, aSkipAnimation_bl = false, aIsReverseGrow_bl = true)
	{
		let lX_num = RoomButtom.INIT_X;
		let lIndexOffset_int = this._fPosition_int - aIndex_int;

		if (lIndexOffset_int < 0)
		{
			lX_num -= RoomButtom.INVISIBLE_OFFSET_X * (lIndexOffset_int < -1 ? 2 : 1);
		}
		else if (lIndexOffset_int < RoomButtom.VISIBLE_AMOUNT)
		{
			lX_num += lIndexOffset_int * (RoomButtom.VISIBLE_OFFSET_X);
		}
		else
		{
			lX_num += (RoomButtom.VISIBLE_AMOUNT - 1) * (RoomButtom.VISIBLE_OFFSET_X) + RoomButtom.INVISIBLE_OFFSET_X * (lIndexOffset_int >= RoomButtom.VISIBLE_AMOUNT ? 2 : 1);
		}

		//Offset for gold icon
		if(!aIsReverseGrow_bl && lIndexOffset_int != 0)
		{
			lX_num += 10;
		}
		else if(aIsReverseGrow_bl && lIndexOffset_int === 5)
		{
			lX_num += 30;
		}else if(aIsReverseGrow_bl && lIndexOffset_int === 4){
			lX_num += 5;
		}

		if(lIndexOffset_int == 0){
			lX_num += 5;
		}

		/*if(lIndexOffset_int == 5){
		
			lX_num += 20;
		}*/
		this.removeTweens();
		if (aSkipAnimation_bl) this.position.set(lX_num, 0);

		else this.moveTo(lX_num, 0, 300, Easing.sine.easeInOut);
	}

	_disableButtonInteraction()
	{
		for( let i = 0; i < this._fCards_s_arr.length; i++ )
		{
			if(this._fCards_s_arr[i])
			{
				this._fCards_s_arr[i].off("mouseover", this._mouseOver, this);
				this._fCards_s_arr[i].off("mouseout", this._mouseOut, this);
			}
		}
	}

	_enableButtonInteraction()
	{
		for( let i = 0; i < this._fCards_s_arr.length; i++ )
		{
			if(this._fCards_s_arr[i])
			{
				this._fButton_b.on("mouseover", this._mouseOver, this);
				this._fButton_b.on("mouseout", this._mouseOut, this);
			}
		}
	}

	destroy()
	{
		APP.off(LobbyAPP.EVENT_ON_SOME_BONUS_STATE_CHANGED, this._onSomeBonusStateChanged, this);

		this._fButtonStake_num = null;
		this._fBulletCostIndicatorView_bciv = null;
		this._fButton_b = null;
		this._fStateIndicator_lbci = null;
		this._fWeaponsIndicator_b = null;

		this._disableButtonInteraction();

		super.destroy();
	}
}

export default RoomButtom;