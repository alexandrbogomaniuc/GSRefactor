import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import PointerSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/PointerSprite';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import WeaponSidebarIconEmblem from './WeaponSidebarIconEmblem';
import WeaponSidebarIconEmblemDefault from './WeaponSidebarIconEmblemDefault';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import FreeShotsLabelView from './FreeShotsLabelView';

const STATE_FREE_SW = 0;
const STATE_PAID_SW = 1;
const STATE_BONUS = 2;

const STATES = [STATE_FREE_SW, STATE_PAID_SW, STATE_BONUS];

class WeaponSidebarIcon extends PointerSprite {

	static get EVENT_WEAPON_SIDEBAR_ICON_CLICKED () { return "EVENT_WEAPON_SIDEBAR_ICON_CLICKED"; }
	static get EVENT_WEAPON_SIDEBAR_ICON_DEFAULT_DISAPPEARED () { return WeaponSidebarIconEmblemDefault.EVENT_ON_DISAPPEARED; }

	static get i_STATE_FREE_SW() 	{ return STATE_FREE_SW; }
	static get i_STATE_PAID_SW() 	{ return STATE_PAID_SW; }
	static get i_STATE_BONUS() 	{ return STATE_BONUS; }

	i_updatePrice(aPrice_num)
	{
		this._updatePrice(aPrice_num);
	}

	i_updateState(aNewState_int)
	{
		this._updateState(aNewState_int);
	}

	i_updateFreeShots(aFreeShots_int)
	{
		this._updateFreeShots(aFreeShots_int);
	}

	set freeShots(aVal_bln)
	{
		this._fFreeShots_bln = aVal_bln;

		this._invalidateView();
	}

	set isAnimating(aValue_bl)
	{
		this._fIsAnimating_bl = aValue_bl;
		if (this._fEmblem_wsie)
		{
			this._fEmblem_wsie.isAnimating = aValue_bl;
		}
	}

	get isAnimating()
	{
		return this._fIsAnimating_bl;
	}

	set active(aValue_bl)
	{
		if (this._fIsActive_bl != aValue_bl)
		{
			this._fIsActive_bl = aValue_bl;
			this._invalidateActiveState();
		}
	}

	get active()
	{
		return this._fIsActive_bl;
	}

	get weaponId()
	{
		return this._fWeaponId_int;
	}

	set isSelected(aValue_bl)
	{
		if (this._fIsSelected_bl != aValue_bl)
		{
			this._fIsSelected_bl = aValue_bl;
			this._invalidateView();
			this._checkSelectionAnimation();
		}
	}

	get isSelected()
	{
		return this._fIsSelected_bl;
	}

	get currentState()
	{
		return this._fCurrentState_int;
	}

	get price()
	{
		return this._fPrice_num;
	}

	get weaponPrice()
	{
		return this._fDefaultPrice_num;
	}

	get freeShots()
	{
		return this._fFreeShots_int;
	}

	set delayDefaultIcon(aValue_bl)
	{
		this._fDelayDefaultIcon = aValue_bl;
	}

	get delayDefaultIcon()
	{
		return this._fDelayDefaultIcon;
	}

	interruptTransitions()
	{
		this._interruptTransitions();
	}

	constructor(aWeaponId_int, aWeaponPrice_num) {
		super();

		this._fDefaultPrice_num = aWeaponPrice_num;
		this._fCurrentState_int = STATE_PAID_SW;
		this._fIsActive_bl = false;
		this._fIsSelected_bl = false;
		this._fIsAnimating_bl = false;
		this._fDelayDefaultIcon = false;

		this._fWeaponId_int = aWeaponId_int;
		this._fPrice_num = null;
		this._fFreeShots_int = null;

		this._fEmblem_wsie = null;
		this._fBackToDefaultEmblem_wsied = null;

		this._fPriceLabel_tf = null;
		this._fFreeShotsLabelView_fslv = null;
		this._fLabelTextFormat_obj = null;

		this._fDelayedPrice_num = undefined;
		this._isClicked = true;

		this._init();
	}

	_init()
	{
		this._initEmblem();
		this._initBackToDefaultEmblem();

		this._initButtonBehaviour();

		this._invalidateView();

	}

	_initButtonBehaviour()
	{
		//set cursor to default over the sidebar icon
		var cursorController = APP.currentWindow.cursorController;
		this.mouseover = () => cursorController.setOverRestrictedZone(true);
		this.mouseout = () => cursorController.setOverRestrictedZone(false);

		this.setHitArea(new PIXI.Circle(0, 0, 30));
		this.setEnabled();

		this.on("pointerdown", (e)=>e.stopPropagation(), this);
		this.on("pointerclick", this._onClicked, this);
	}

	_initEmblem()
	{
		this._fEmblem_wsie = this.addChild(new WeaponSidebarIconEmblem(this.weaponId));
		this._fEmblem_wsie.isAnimating = this.isAnimating;
	}

	_initBackToDefaultEmblem()
	{
		this._fBackToDefaultEmblem_wsied = this.addChild(new WeaponSidebarIconEmblemDefault());
		this._fBackToDefaultEmblem_wsied.on(WeaponSidebarIconEmblemDefault.EVENT_ON_DISAPPEARED, this._onBackToDefaultEmblemDisappearCompleted, this);
	}

	//LABELS...
	_initPriceLabel()
	{
		this._fPriceLabel_tf = this.addChild(new TextField(this._labelTextFormat));
		this._fPriceLabel_tf.anchor.set(0.5, 0.5);
		this._fPriceLabel_tf.position.set(0, 20);
		this._fPriceLabel_tf.maxWidth = 70;

		return this._fPriceLabel_tf;
	}

	_initFreeShotsLabelView()
	{
		this._fFreeShotsLabelView_fslv = this.addChild(new FreeShotsLabelView());
		this._fFreeShotsLabelView_fslv.position.set(0, 20);
		return this._fFreeShotsLabelView_fslv;

	}

	get _freeShotsLabelView()
	{
		return this._fFreeShotsLabelView_fslv || this._initFreeShotsLabelView();
	}

	get _priceLabel()
	{
		return this._fPriceLabel_tf || this._initPriceLabel();
	}

	get _labelTextFormat()
	{
		return this._fLabelTextFormat_obj || this._initLabelTextFormat();
	}

	_initLabelTextFormat()
	{
		let l_ta = I18.generateNewCTranslatableAsset(this._freeShotsLabelViewTranslatableAssetName);
		this._fLabelTextFormat_obj = l_ta.textFormat;
		return this._fLabelTextFormat_obj;
	}

	get _freeShotsLabelViewTranslatableAssetName()
	{
		return "TAWeaponsSidebarIconFreeShotsLabel";
	}
	//...LABELS

	_updatePrice(aPrice_num)
	{
		if (this._fBackToDefaultEmblem_wsied.isDisappearInProgress || this.delayDefaultIcon)
		{
			this._fDelayedPrice_num = aPrice_num;
			return;
		}

		this._fDelayedPrice_num = undefined;

		this._fPrice_num = aPrice_num;
		this._priceLabel.text = APP.currencyInfo.i_formatNumber(this._fPrice_num, true);

		this._fBackToDefaultEmblem_wsied.updateView();
	}

	_updateState(aNewState_int)
	{
		if (STATES.indexOf(aNewState_int) < 0)
		{
			throw new Error("Unsupported sidebar icon state: " + aNewState_int);
		}
		let lIsFRBMode_bl = APP.currentWindow.gameFrbController.info.frbMode;
		if (aNewState_int === STATE_FREE_SW && !lIsFRBMode_bl)
		{
			this.delayDefaultIcon = false;
		}
		this._fCurrentState_int = aNewState_int;
		this._invalidateView();
	}

	_updateFreeShots(aFreeShots_int)
	{
		this._fFreeShots_int = aFreeShots_int;
		this._updateFreeShotsLabel(aFreeShots_int);
	}

	_updateFreeShotsLabel(aFreeShots_int)
	{
		let lIsAnimationNeeded_bl = this.isSelected;
		this._freeShotsLabelView.i_updateFreeShotsCount(aFreeShots_int, lIsAnimationNeeded_bl);
	}

	_invalidateView(aIsBackToWeaponAnim_bl=false)
	{
		let lIsFRBMode_bl = APP.currentWindow.gameFrbController.info.frbMode;
		let lDefHideRequired_bl = (this._fCurrentState_int == STATE_FREE_SW && !lIsFRBMode_bl) || !this.isSelected;
		let lDefDisappearAnimRequired_bl = lDefHideRequired_bl && this.active && this._fBackToDefaultEmblem_wsied.visible && !this._fBackToDefaultEmblem_wsied.isDisappearInProgress;
		let lDefDisappearInProgress = this._fBackToDefaultEmblem_wsied.visible && this._fBackToDefaultEmblem_wsied.isDisappearInProgress;

		if (lDefDisappearAnimRequired_bl)
		{
			this._fBackToDefaultEmblem_wsied.startDisappearAnimation();
			return;
		}

		if (lDefDisappearInProgress && this.delayDefaultIcon)
		{
			this.delayDefaultIcon = false;
			this.interruptTransitions();
		}

		switch (this._fCurrentState_int)
		{
			case STATE_FREE_SW:
				if (!lIsFRBMode_bl)
				{
					this._fBackToDefaultEmblem_wsied.hide();
					this._fEmblem_wsie.visible = true;
				}

				this._priceLabel.visible = false;

				this._freeShotsLabelView.show();
				break;
			case STATE_PAID_SW:
				this._freeShotsLabelView.hide();
				if (this.isSelected)
				{
					if (!this.delayDefaultIcon)
					{
						this._fBackToDefaultEmblem_wsied.show();
						this._fEmblem_wsie.visible = false;
					}
				}
				else
				{
					this._fBackToDefaultEmblem_wsied.hide();
					this._fEmblem_wsie.visible = true;
				}
				this._priceLabel.visible = true;
				break;
			case STATE_BONUS:
				if (this.isSelected)
				{
					if (!this.delayDefaultIcon)
					{
						this._fBackToDefaultEmblem_wsied.show();
						this._fEmblem_wsie.visible = false;
						this._priceLabel.visible = false;
					}
				}
				else
				{
					this._fBackToDefaultEmblem_wsied.hide();
					this._fEmblem_wsie.visible = true;
					this._priceLabel.visible = !this._fFreeShots_bln;
				}
				break;
		}

		if (aIsBackToWeaponAnim_bl)
		{
			this._fEmblem_wsie.startBackToWeaponAnimation();
		}

		this._invalidateActiveState();
		this._invalidateSelectionState();
	}

	_onBackToDefaultEmblemDisappearCompleted(event)
	{
		this._invalidateView(true);

		if (this._fDelayedPrice_num !== undefined)
		{
			this._updatePrice(this._fDelayedPrice_num);
		}

		this.emit(WeaponSidebarIcon.EVENT_WEAPON_SIDEBAR_ICON_DEFAULT_DISAPPEARED);
	}

	_invalidateActiveState()
	{
		this._fEmblem_wsie.active = this.active;
	}

	_invalidateSelectionState()
	{
		this._fEmblem_wsie.isSelected = this.isSelected;
	}

	_onClicked(e)
	{
		e.stopPropagation();
		this._isClicked = true;
		if (!this.active) return;
		this.emit(WeaponSidebarIcon.EVENT_WEAPON_SIDEBAR_ICON_CLICKED, {weaponId: this.weaponId});
	}


	set isClicked(aValue_bl)
	{
		this._isClicked = aValue_bl;
	}

	get isClicked()
	{
		return this._isClicked;
	}

	get _isWeaponSwitchAnimationInProgress()
	{
		let lSelfSequences_arr = Sequence.findByTarget(this);
		return lSelfSequences_arr && lSelfSequences_arr.length > 0;
	}

	_checkSelectionAnimation()
	{
		this._resetAnimation();
		if (this.isSelected && this.isAnimating || this._fBackToDefaultEmblem_wsied.isDisappearInProgress)
		{
			let selectionSeq;

			if (this._fBackToDefaultEmblem_wsied.isDisappearInProgress)
			{
				selectionSeq = [
					{
						tweens: [],
						duration: 9 * FRAME_RATE
					},
					{
						tweens: [
							{prop: "scale.x", to: 1.148},
							{prop: "scale.y", to: 1.148}
						],
						ease: Easing.sine.easeInOut,
						duration: 4 * FRAME_RATE
					},
					{
						tweens: [
							{prop: "scale.x", to: 1.1},
							{prop: "scale.y", to: 1.1}
						],
						ease: Easing.sine.easeInOut,
						duration: 3 * FRAME_RATE
					},
					{
						tweens: [
							{prop: "scale.x", to: 1},
							{prop: "scale.y", to: 1}
						],
						ease: Easing.sine.easeInOut,
						duration: 3 * FRAME_RATE
					}
				];
			}
			else
			{
				selectionSeq = [
					{
						tweens: [
							{prop: "scale.x", to: 1.1},
							{prop: "scale.y", to: 1.1}
						],
						ease: Easing.sine.easeInOut,
						duration: 3 * FRAME_RATE
					},
					{
						tweens: [
							{prop: "scale.x", to: 1.148},
							{prop: "scale.y", to: 1.148}
						],
						ease: Easing.sine.easeInOut,
						duration: 3 * FRAME_RATE
					},
					{
						tweens: [
							{prop: "scale.x", to: 1},
							{prop: "scale.y", to: 1}
						],
						ease: Easing.sine.easeInOut,
						duration: 4 * FRAME_RATE
					},
					{
						tweens: [
							{prop: "scale.x", to: 0.973},
							{prop: "scale.y", to: 0.973}
						],
						ease: Easing.sine.easeInOut,
						duration: 3 * FRAME_RATE
					},
					{
						tweens: [
							{prop: "scale.x", to: 1},
							{prop: "scale.y", to: 1}
						],
						ease: Easing.sine.easeInOut,
						duration: 10 * FRAME_RATE
					}
				];
			}

			Sequence.start(this, selectionSeq);
		}
	}

	_resetAnimation()
	{
		Sequence.destroy(Sequence.findByTarget(this));
		this.scale.set(1);
	}

	_interruptTransitions()
	{
		this._resetAnimation();

		if (this._fBackToDefaultEmblem_wsied.isDisappearInProgress)
		{
			this._onBackToDefaultEmblemDisappearCompleted();
		}

		this._fBackToDefaultEmblem_wsied.interruptTransitions();
		this._fEmblem_wsie.interruptBackToWeaponAnimation();

	}
}

export default WeaponSidebarIcon;