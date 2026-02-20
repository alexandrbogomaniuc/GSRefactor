import { APP } from '../../../../unified/controller/main/globals';
import Sprite from '../../../../unified/view/base/display/Sprite';
import I18 from '../../../../unified/controller/translations/I18';
import * as Easing from '../../../../unified/model/display/animation/easing';
import Button from '../../../../unified/view/ui/Button';
import GUSLobbyBulletRangeCostIndicatorView from './GUSLobbyBulletRangeCostIndicatorView';
import Sequence from '../../../../unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../unified/controller/time/Ticker';

class GUSLobbyRoomButton extends Sprite
{
	static get EVENT_LOBBY_ROOM_STAKE_SELECTED()					{return "onLobbyRoomStakeSelected";}

	static get INIT_X()							{return 96;}
	static get VISIBLE_OFFSET_X()				{return 170;}
	static get INVISIBLE_OFFSET_X()				{return 240;}
	static get VISIBLE_AMOUNT()					{return 5;}

	updateStake(aStake_num, aCurrentSkin_num)
	{
		this._updateStake(aStake_num, aCurrentSkin_num);
	}

	disableRoomBtn()
	{
		if (!this._fButton_b.enabled) return;
		// see https://jira.dgphoenix.com/browse/MQPRT-12
		// 'interactive' value is invalidating after any listeners removing, and can be set to 'true' on the higher level (see Sprite.js, removeListener function)
		// so we must avoid situations where removing listeners (off) happens after 'interactive' was set to 'false' by ourselves

		this.__setBulletCostIndicatorDisabledView();

		this._disableButtonInteraction();
		this._disableRoomBtn();

		this._fBulletCostIndicatorView_bciv && (this._fBulletCostIndicatorView_bciv.visible = false);
	}

	enableRoomBtn()
	{
		if (this._fButton_b.enabled) {
			return;	
		}

		this.__setBulletCostIndicatorEnabledView();
		this._fBulletCostIndicatorView_bciv && (this._fBulletCostIndicatorView_bciv.visible = true);

		this._enableRoomBtn();
		this._enableButtonInteraction();
	}

	get enabled()
	{
		return this._fButton_b.enabled;
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

		this._fContentContainer_s = this.addChild(new Sprite());
		this._addBack();
		
		this._fCardsContainer_s = this._fContentContainer_s.addChild(new Sprite());	
		this._fCards_s_arr = [];
		this._fButtonStake_num = null;
		this._fBulletCostIndicatorView_bciv = null;
		this._fButton_b = null;
		this._fStakeIndicator_lbci = null;
		this._fNotEnoughMoneyCapture_cta = null;
		this._fPosition_int = aPersonalPosition_int;

		this._enableButtonInteraction();
	}

	_addBack()
	{
	}

	__setBulletCostIndicatorDisabledView()
	{
		// revision 234779, https://jira.dgphoenix.com/browse/MQRR-152
		if (this._fBulletCostIndicatorView_bciv && !window.isWebGLSupported)
		{
			this._fBulletCostIndicatorView_bciv.position.set(80, 163);
		}
	}

	__setBulletCostIndicatorEnabledView()
	{
		// revision 234779, https://jira.dgphoenix.com/browse/MQRR-152
		if (this._fBulletCostIndicatorView_bciv && !window.isWebGLSupported)
		{
			this._fBulletCostIndicatorView_bciv.position.set(0, 83);
		}
	}

	get __texturesAmount()
	{
		return 5;
	}

	_getCard(aSkinId_int)
	{
		if (!this._fCards_s_arr[aSkinId_int])
		{
			let l_btn = this._initButton(aSkinId_int);
			this._fCards_s_arr[aSkinId_int] = l_btn;
		}

		return this._fCards_s_arr[aSkinId_int];
	}

	_initButton(aSkinId_int)
	{
		let l_btn = this._fCardsContainer_s.addChild(this.__provideButtonInstance(aSkinId_int));

		this._addNotEnoughMoneyCapture(aSkinId_int);

		l_btn.holder.addChild(this._stakeIndicator);

		this._enableRoomBtn();

		return l_btn;
	}

	__provideButtonInstance(aSkinId_int)
	{
		let lBaseAsset_str = this.__cardTextureNamePrefix + this.__cardTextureId(aSkinId_int);
		return new Button(lBaseAsset_str, null, true);
	}

	__cardTextureId(aSkinId_int)
	{
		return aSkinId_int;
	}

	get __cardTextureNamePrefix()
	{
		// must be overridden
		return undefined;
	}

	_addNotEnoughMoneyCapture(aCurrentSkin_num)
	{
		if (APP.isBattlegroundGame)
		{
			return; //prevent "BATTLEGROUND" caption overlap
		}

		this._fNotEnoughMoneyCapture_cta = this._fContentContainer_s.addChild(I18.generateNewCTranslatableAsset(this.__notEnoughMoneyCaptureAssetId));

		let lPos_p = this.__getNotEnoughMoneyCapturePosition(aCurrentSkin_num);
		this._fNotEnoughMoneyCapture_cta.position.set(lPos_p.x, lPos_p.y);
		this._fNotEnoughMoneyCapture_cta.visible = false;
	}

	get __notEnoughMoneyCaptureAssetId()
	{
		// must be overridden
		return undefined;
	}

	__getNotEnoughMoneyCapturePosition(aCurrentSkin_num)
	{
		return new PIXI.Point(0, 115);
	}

	_disableRoomBtn()
	{
		this._mouseOut();

		this._fButton_b && (this._fButton_b.enabled = false);
		this._fNotEnoughMoneyCapture_cta && (this._fNotEnoughMoneyCapture_cta.visible = true);

		this.off("pointerclick", this._onRoomBtnClicked, this);
		this.off("pointerdown", this._onRoomBtnDown, this);
		this.off("mouseover", this._mouseOver, this);
		this.off("mouseout", this._mouseOut, this);
	}

	_enableRoomBtn()
	{
		if (this._fButton_b)
		{
			this._fButton_b.enabled = true;

			this._tryToShowMouseOver();
		}

		this._fNotEnoughMoneyCapture_cta && (this._fNotEnoughMoneyCapture_cta.visible = false);

		this.on("pointerclick", this._onRoomBtnClicked, this);
		this.on("pointerdown", this._onRoomBtnDown, this);
		this.on("mouseover", this._mouseOver, this);
		this.on("mouseout", this._mouseOut, this);
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
		this._fButton_b.scaleTo(1.05, 1*FRAME_RATE);
	}

	_mouseOut()
	{
		this._fButton_b.scaleTo(1, 1*FRAME_RATE);
	}

	_onRoomBtnClicked()
	{
		this.emit(GUSLobbyRoomButton.EVENT_LOBBY_ROOM_STAKE_SELECTED, {value: this.stake})
	}

	_onRoomBtnDown()
	{
		this._addDocumentEventListeners();
	}

	pointerupHandler()
	{
		this._fButton_b.handleUp();
		
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

	get _stakeIndicator()
	{
		return this._fBulletCostIndicatorView_bciv || (this._fBulletCostIndicatorView_bciv = this.__provideLobbyBulletRangeCostIndicatorInstance());
	}

	__provideLobbyBulletRangeCostIndicatorInstance()
	{
		return new GUSLobbyBulletRangeCostIndicatorView();
	}

	_updateStake(aStake, aCurrentSkin_num)
	{
		this._fButtonStake_num = aStake;

		this._updateStakeView(aCurrentSkin_num);

		let lSkinId_int = this.__getInternalSkinId(aCurrentSkin_num);
		let lCard_b = this._getCard(lSkinId_int);
		lCard_b.visible = true;

		this._fButton_b = lCard_b;
	}

	__getInternalSkinId(aCurrentSkin_num)
	{
		return aCurrentSkin_num;
	}

	_updateStakeView(aCurrentSkin_num)
	{
		this._stakeIndicator.updateStake(this._fButtonStake_num);
		
		let lStakePos_obj = this.__getStakeIndicatorPosition(aCurrentSkin_num);
		this._stakeIndicator.position.set(lStakePos_obj.x, lStakePos_obj.y);

		let lContPos_obj = this.__getContentContainerPosition(aCurrentSkin_num);
		this._fContentContainer_s.position.set(lContPos_obj.x, lContPos_obj.y);
	}

	__getStakeIndicatorPosition(aCurrentSkin_num)
	{
		// must be overridden
		return new PIXI.Point(0, 0);
	}

	__getContentContainerPosition(aCurrentSkin_num)
	{
		return new PIXI.Point(0, 0);
	}

	_updatePosition(aIndex_int, aSkipAnimation_bl = false, aIsReverseGrow_bl = true)
	{
		let lX_num = this.__calculatePositionOffsetX(aIndex_int, aIsReverseGrow_bl);
		
		this.removeTweens();
		
		if (aSkipAnimation_bl)
		{
			this.position.set(lX_num, 0);
		}
		else
		{
			this.moveTo(lX_num, 0, 300, Easing.sine.easeInOut);
		}
	}

	__calculatePositionOffsetX(aIndex_int, aIsReverseGrow_bl)
	{
		let lX_num = GUSLobbyRoomButton.INIT_X;
		let lIndexOffset_int = this._fPosition_int - aIndex_int;

		if (lIndexOffset_int < 0)
		{
			lX_num -= GUSLobbyRoomButton.INVISIBLE_OFFSET_X * (lIndexOffset_int < -1 ? 2 : 1);
		}
		else if (lIndexOffset_int < GUSLobbyRoomButton.VISIBLE_AMOUNT)
		{
			lX_num += lIndexOffset_int * (GUSLobbyRoomButton.VISIBLE_OFFSET_X);
		}
		else
		{
			lX_num += (GUSLobbyRoomButton.VISIBLE_AMOUNT - 1) * (GUSLobbyRoomButton.VISIBLE_OFFSET_X) + GUSLobbyRoomButton.INVISIBLE_OFFSET_X * (lIndexOffset_int >= GUSLobbyRoomButton.VISIBLE_AMOUNT ? 2 : 1);
		}

		return lX_num;
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
			if (this._fCards_s_arr[i])
			{
				this._fButton_b.on("mouseover", this._mouseOver, this);
				this._fButton_b.on("mouseout", this._mouseOut, this);
			}
		}
	}

	destroy()
	{
		this._fContentContainer_s = null;
		this._fCardsContainer_s = null;
		this._fButtonStake_num = null;
		this._fBulletCostIndicatorView_bciv = null;
		this._fButton_b = null;
		this._fStakeIndicator_lbci = null;
		this._fDisaibledViewCorrection_num = null;

		this._disableButtonInteraction();

		this._fCards_s_arr = null;

		super.destroy();
	}
}

export default GUSLobbyRoomButton;