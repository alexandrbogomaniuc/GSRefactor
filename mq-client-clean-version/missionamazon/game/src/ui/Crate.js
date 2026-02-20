import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import * as Easing from '../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import AtlasConfig from '../config/AtlasConfig';
import AtlasSprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import Sequence from '../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import ContentItem from './content/ContentItem';
import ContentItemInfo from '../model/uis/content/ContentItemInfo';
import Timer from "../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer";
import DeathFxAnimation from '../main/animation/death/DeathFxAnimation';
import { FRAME_RATE } from '../../../shared/src/CommonConstants';

class Crate extends Sprite
{
	static initMaskedTextures()
	{
	}

	static get ON_CRATE_WEAPON_REVEAL() 				{return "onCrateWeaponReveal";}
	static get ON_CRATE_DISAPPEARED() 					{return "onCrateDisappeared";}
	static get ON_CRATE_LANDED() 						{return "onCrateLanded"};
	static get ON_CRATE_CONTENT_LANDED() 				{return ContentItem.ON_CONTENT_LANDED;}
	static get ON_CRATE_CONTENT_APPEARED() 				{return ContentItem.ON_CONTENT_APPEARED;}	
	static get ON_CRATE_CONTENT_ANIMATION_STARTED() 	{return ContentItem.ON_CONTENT_ANIMATION_STARTED;}

	static get DEFAULT_DELTA_X() {return 	-80;}
	static get DEFAULT_DELTA_Y() {return 	 15;}

	static get MAX_SIMULTANEOUS_ITEMS_AMOUNT() 		{return   12;}
	static get CONTENT_ITEMS_DISTANCE() 			{return  120;}
	static get CONTENT_ITEMS_BOTTOM_ROW_DISTANCE() 	{return -110;}

	static get CRATE_TYPE() 	{return "base";}

	get isIntroInProgress()
	{
		return this._fIntroAnimationInprogress_bl;
	}

	get isOutroInProgress()
	{
		return this._fOutroAnimationInprogress_bl;
	}

	get finalPoint()
	{
		return this._crateFinalPoint;
	}

	get contentItems()
	{
		return this._fContentItems_arr;
	}

	constructor(targetEnemyPos, contentItems_arr, contentItemsContainer)
	{
		super();

		this._itemsOrder = null;
		this._currentBlockIndex = -1;
		this._blockRevealTimer = null;
		this._contentItemsContainer = contentItemsContainer || null;
		this._contentItemsStack_arr = [];
		this._fCrateContainer_sprt = null;
		this._fCrateSmokeContainer_sprt = null;
		this._fCrateView_sprt = null;
		this._fContentItems_arr = contentItems_arr;

		this._streakTextures = null;

		this._revealWeaponBlast = null;
		this._glowView = null;

		this._initItemsOrder(contentItems_arr);

		this._setTargetPosition(targetEnemyPos);
		
		this._fIntroAnimationInprogress_bl = false;
		this._fOutroAnimationInprogress_bl = false;

		this.once('added', this._onAdded, this);
	}

	_setTargetPosition(targetEnemyPos)
	{
		this.x = targetEnemyPos.x;
		this.y = targetEnemyPos.y;
	}

	calculateCrateFinalPoint(targetEnemyPos)
	{
		return this._calculateCrateFinalPoint(targetEnemyPos);
	}

	_initItemsOrder(contentItems_arr)
	{
		let totalItemsAmount = contentItems_arr ? contentItems_arr.length : 0;
		let orderBlocksAmount = Math.ceil(totalItemsAmount/this.constructor.MAX_SIMULTANEOUS_ITEMS_AMOUNT);

		if (orderBlocksAmount === 1)
		{
			this._itemsOrder = [contentItems_arr];
			return;
		}

		this._itemsOrder = [];

		let lastBlockItemsAmount = this.constructor.MAX_SIMULTANEOUS_ITEMS_AMOUNT - ((this.constructor.MAX_SIMULTANEOUS_ITEMS_AMOUNT*orderBlocksAmount) - totalItemsAmount);
		let additionalLastBlockItemsAmount = 0;
		if (lastBlockItemsAmount < this.constructor.MAX_SIMULTANEOUS_ITEMS_AMOUNT/2)
		{
			additionalLastBlockItemsAmount = (this.constructor.MAX_SIMULTANEOUS_ITEMS_AMOUNT/2) - lastBlockItemsAmount;
		}

		let queuedItemsAmount = 0;
		for (let i=0; i<orderBlocksAmount; i++)
		{
			let blockLength = this.constructor.MAX_SIMULTANEOUS_ITEMS_AMOUNT;
			if (i === orderBlocksAmount-2)
			{
				blockLength -= additionalLastBlockItemsAmount;
			}
			else if (i === orderBlocksAmount-1)
			{
				blockLength += additionalLastBlockItemsAmount;
			}

			let startIndex = i === 0 ? 0 : queuedItemsAmount;
			let endIndex = startIndex + blockLength;
			let blockItems = contentItems_arr.slice(startIndex, endIndex);

			this._itemsOrder.push(blockItems);

			queuedItemsAmount += blockLength;
		}
	}

	_calculateCrateFinalPoint(targetEnemyPos)
	{
		let crateDeltaPoint = new PIXI.Point(this.constructor.DEFAULT_DELTA_X, this.constructor.DEFAULT_DELTA_Y);

		let contentMostLeftBottomPoint = new PIXI.Point(0, this.constructor.CONTENT_ITEMS_BOTTOM_ROW_DISTANCE);
		let contentMostRightTopPoint = new PIXI.Point(0, this.constructor.CONTENT_ITEMS_BOTTOM_ROW_DISTANCE);
		let blockItems;
		let item;
		for (let i=0; i<this._itemsOrder.length; i++)
		{
			blockItems = this._itemsOrder[i];
			for (let j=0; j<blockItems.length; j++)
			{
				item = blockItems[j];
				let itemDistancePoint = this._getContentItemTopDistancePoint(j, blockItems.length);
				if (itemDistancePoint.x < contentMostLeftBottomPoint.x)
				{
					contentMostLeftBottomPoint.x = itemDistancePoint.x;
				}

				if (itemDistancePoint.y > contentMostLeftBottomPoint.y)
				{
					contentMostLeftBottomPoint.y = itemDistancePoint.y;
				}

				if (itemDistancePoint.x > contentMostRightTopPoint.x)
				{
					contentMostRightTopPoint.x = itemDistancePoint.x;
				}

				if (itemDistancePoint.y < contentMostRightTopPoint.y)
				{
					contentMostRightTopPoint.y = itemDistancePoint.y;
				}
			}
		}

		let crateContentContainer = this._contentItemsContainer || this.parent;
		let contentPosition = new PIXI.Point(this.position.x, this.position.y);
		contentPosition = this.parent.localToLocal(contentPosition.x, contentPosition.y, crateContentContainer);

		contentMostLeftBottomPoint.x += contentPosition.x+crateDeltaPoint.x-this.constructor.CONTENT_ITEMS_DISTANCE/2;
		contentMostLeftBottomPoint.y += contentPosition.y+crateDeltaPoint.y+this.constructor.CONTENT_ITEMS_DISTANCE/2;
		contentMostRightTopPoint.x += contentPosition.x+crateDeltaPoint.x+this.constructor.CONTENT_ITEMS_DISTANCE/2;
		contentMostRightTopPoint.y += contentPosition.y+crateDeltaPoint.y-this.constructor.CONTENT_ITEMS_DISTANCE/2;

		let globalLeftBottom = crateContentContainer.localToGlobal(contentMostLeftBottomPoint)
		let globalRightTop = crateContentContainer.localToGlobal(contentMostRightTopPoint)

		let deltaX = 0;
		if (globalLeftBottom.x < 0)
		{
			deltaX = 0-globalLeftBottom.x;
		}
		else if (globalRightTop.x > 960)
		{
			deltaX = 960-globalRightTop.x;
		}
		crateDeltaPoint.x += deltaX;
		if (Math.abs(crateDeltaPoint.x) < Math.abs(this.constructor.DEFAULT_DELTA_X))
		{
			crateDeltaPoint.x = deltaX > 0 ? targetEnemyPos.x + Math.abs(this.constructor.DEFAULT_DELTA_X) : targetEnemyPos.x - Math.abs(this.constructor.DEFAULT_DELTA_X);
		}
		
		let deltaY = 0;
		if (globalLeftBottom.y > (540+this.constructor.CONTENT_ITEMS_BOTTOM_ROW_DISTANCE))
		{
			deltaY = (540+this.constructor.CONTENT_ITEMS_BOTTOM_ROW_DISTANCE)-globalLeftBottom.y;
		}
		else if (globalRightTop.y < 0)
		{
			deltaY = 0-globalRightTop.y;
		}
		crateDeltaPoint.y += deltaY;
		if (Math.abs(crateDeltaPoint.y) < Math.abs(this.constructor.DEFAULT_DELTA_Y))
		{
			crateDeltaPoint.y = deltaY > 0 ? targetEnemyPos.y + Math.abs(this.constructor.DEFAULT_DELTA_Y) : targetEnemyPos.y - Math.abs(this.constructor.DEFAULT_DELTA_Y);
		}
		
		return crateDeltaPoint;
	}

	_onAdded(e)
	{
		this.crate;
		this._crateFinalPoint = this._calculateCrateFinalPoint(new PIXI.Point(this.position.x, this.position.y));
		this._onBoxLanding();
		this._onBoxLanded();
	}

	get crate()
	{
		return this._fCrateView_sprt || (this._fCrateView_sprt = this._initCrateView());
	}

	_initCrateView()
	{
		this._fCrateSmokeContainer_sprt = this.addChild(new Sprite());;

		this._fCrateContainer_sprt = this.addChild(new Sprite());
		this._fCrateContainer_sprt.position.set(0, 0);
		this._fCrateContainer_sprt.alpha = 0;
		this._fCrateContainer_sprt.scale.set(0.1);

		return this._fCrateContainer_sprt.addChild(new Sprite());
	}

	_onBoxLanding()
	{
		this.emit(Crate.ON_CRATE_LANDED, {crateType: this.constructor.CRATE_TYPE});
	}

	_onBoxLanded()
	{
		this._fIntroAnimationInprogress_bl = false;
		this._startContentAnimation();
	}

	_startContentAnimation()
	{
		this._revealContent();
	}

	_revealContent()
	{
		this._currentBlockIndex = -1;
		this._revealNextBlockIfPossible();
		
		this.emit(Crate.ON_CRATE_WEAPON_REVEAL);
	}

	_revealNextBlockIfPossible()
	{
		this._removeBlockRevealTimer();

		this._currentBlockIndex++;

		if (this._currentBlockIndex >= this._itemsOrder.length)
		{
			return;
		}

		this._revealBlock();
	}

	_removeBlockRevealTimer()
	{
		if (!this._blockRevealTimer)
		{
			return;
		}

		this._blockRevealTimer.destructor();
		this._blockRevealTimer = null;
	}

	_revealBlock()
	{
		this.crate;
		let blockRedSmokeAdded = false;
		for (let i=0; i<this._currentBlock.length; i++)
		{
			let topDistancePoint = this._addContentItemView(i);

			let tg = Math.abs(topDistancePoint.x) / Math.abs(topDistancePoint.y);
			let angleRadians = Math.atan(tg);
			let multiplier = topDistancePoint.x < 0 ? -1 : 1;
			let weaponFxContainer = this._fCrateContainer_sprt.addChild(new Sprite());
			weaponFxContainer.rotation = angleRadians*multiplier;

			this._addRevealWeaponStreak(weaponFxContainer);

			if (!blockRedSmokeAdded)
			{
				blockRedSmokeAdded = true;
			}
		}

		this._blockRevealTimer = new Timer(this._revealNextBlockIfPossible.bind(this), 1300);
	}

	get _currentBlock()
	{
		return this._itemsOrder[this._currentBlockIndex];
	}

	_startFadeCrate()
	{
		this._fOutroAnimationInprogress_bl = true;
		this._fCrateContainer_sprt.fadeTo(0, this._crateDisappearingDuration, Easing.sine.easeIn, () => this._onCrateDisappeared());
	}

	get _crateDisappearingDuration()
	{
		return 500;
	}

	_onCrateDisappeared()
	{
		this._fOutroAnimationInprogress_bl = false;
		this.emit(Crate.ON_CRATE_DISAPPEARED);
	}

	_addContentItemView(itemIndex)
	{
		let animSpeed = this._currentBlock[itemIndex].assetType == ContentItemInfo.TYPE_WEAPON ? 2 : 1;

		let crateContentContainer = this._contentItemsContainer || this.parent;
		let crateContent = crateContentContainer.addChild(this._generateContentItemInstance(this._currentBlock[itemIndex]));
		crateContent.zIndex = 26000;
		this._contentItemsStack_arr.push(crateContent);

		let contentPosition = new PIXI.Point(this.position.x, this.position.y);
		contentPosition = this.parent.localToLocal(contentPosition.x, contentPosition.y, crateContentContainer);
		
		crateContent.position.set(contentPosition.x+this._crateFinalPoint.x, contentPosition.y+this._crateFinalPoint.y);
		crateContent.blockIndex = this._currentBlockIndex;
		crateContent.once(ContentItem.ON_CONTENT_LANDING, this._onCrateContentLanding, this);
		crateContent.once(ContentItem.ON_CONTENT_APPEARED, this.emit, this);
		crateContent.once(ContentItem.ON_CONTENT_LANDED, this._onCrateContentLanded, this);
		crateContent.once(ContentItem.ON_CONTENT_ANIMATION_STARTED, this._onCrateAnimationStarted, this);
		
		let topDistanceDelta = this._getContentItemTopDelta(itemIndex, this._currentBlock.length, crateContent);
		this._startContentItemAnimation(crateContent, topDistanceDelta, this._showContentFx, animSpeed);

		return topDistanceDelta;
	}

	_startContentItemAnimation(crateContent, topDistanceDelta, showContentFx, animSpeed)
	{
		crateContent.startAnimation(topDistanceDelta, showContentFx, animSpeed);
	}

	_generateContentItemInstance(itemInfo)
	{
		return new ContentItem(itemInfo);
	}

	_getContentItemTopDelta(itemIndex, blockLength, targetContentItem)
	{
		return this._getContentItemTopDistancePoint(itemIndex, blockLength);
	}

	get _showContentFx()
	{
		return false;
	}

	_getContentItemTopDistancePoint(itemIndex, blockLength)
	{
		let blockItemsAmount = blockLength;
		let itemsDistance = this.constructor.CONTENT_ITEMS_DISTANCE;
		let topBaseDistance = this.constructor.CONTENT_ITEMS_BOTTOM_ROW_DISTANCE;

		let rowsAmount = this._getBlockRowsAmount(blockItemsAmount);
		let bottomRowItemsAmount = rowsAmount == 1 ? blockItemsAmount : Math.ceil(blockItemsAmount/2);
		let isBottomRowItem = itemIndex < bottomRowItemsAmount;
		let itemRowItemsAmount = isBottomRowItem ? bottomRowItemsAmount : blockItemsAmount - bottomRowItemsAmount;
		let normalizeditemIndex = isBottomRowItem ? itemIndex : itemIndex%bottomRowItemsAmount;

		let itemYDistance = isBottomRowItem ? topBaseDistance : topBaseDistance-itemsDistance*2/3;
		
		let rowLeftDelta = Math.floor((itemRowItemsAmount-1)/2)*itemsDistance;
		if (itemRowItemsAmount%2 === 0)
		{
			rowLeftDelta += itemsDistance/2;
		}
		let itemXDistance = normalizeditemIndex * itemsDistance - rowLeftDelta;

		let point = new PIXI.Point(itemXDistance, itemYDistance);
		return point;
	}

	_getBlockRowsAmount(blockLength)
	{
		return blockLength > 4 ? 2 : 1;
	}

	_onCrateContentLanding(e)
	{
		if (this._currentBlockIndex >= this._itemsOrder.length-1)
		{
			this._startFadeCrate();
		}
	}

	_onCrateContentLanded(e)
	{
		let lTarget_ci = e.target;
		let lIndex_int = this._contentItemsStack_arr.indexOf(lTarget_ci);
		if (~lIndex_int)
		{
			this._contentItemsStack_arr.splice(lIndex_int, 1);
		}

		lTarget_ci.destroy();

		this.emit(Crate.ON_CRATE_CONTENT_LANDED, {landGlobalX: e.landGlobalX, landGlobalY: e.landGlobalY, playerSeatId: e.playerSeatId, blockIndex: e.blockIndex, lastBlockIndex:(this._itemsOrder.length-1)});
	}

	_onCrateAnimationStarted(e)
	{
		this.emit(Crate.ON_CRATE_CONTENT_ANIMATION_STARTED, {landGlobalX: e.landGlobalX, landGlobalY: e.landGlobalY, playerSeatId: e.playerSeatId});
	}

	//WEAPON STREAK...
	_addRevealWeaponStreak(weaponFxContainer)
	{
		let streak = weaponFxContainer.addChild(new Sprite());
		streak.scale.set(2);
		streak.textures = this.streakTextures;
		streak.blendMode = PIXI.BLEND_MODES.ADD;
		streak.rotation = -Math.PI/2;
		streak.position.set(-4, -90);
		streak.play();
		streak.once('animationend', (e) => { e.target.destroy(); });
	}

	get streakTextures()
	{
		if (!this._streakTextures)
		{
			this._streakTextures = [PIXI.Texture.EMPTY, PIXI.Texture.EMPTY];
			let view_textures = this._streakViewTextures;
			if (view_textures && view_textures.length)
			{
				this._streakTextures = this._streakTextures.concat(view_textures);
			}
		}
		return this._streakTextures;
	}

	get _streakViewTextures()
	{
		return null;
	}
	//...WEAPON STREAK

	_animateOpenedCrate()
	{
		let sequence = [
			{
				tweens: [{prop: "y", to: this._crateFinalPoint.y+13}],
				duration: 2*FRAME_RATE
			},
			{
				tweens: [],
				duration: 1*FRAME_RATE
			},
			{
				tweens: [{prop: "y", to: this._crateFinalPoint.y}],
				duration: 3*FRAME_RATE
			}
		];
		Sequence.start(this._fCrateContainer_sprt, sequence);

		this._addCrateGlowAnimation();
	}

	_addCrateGlowAnimation()
	{
		let glowView = new Sprite(APP.library.getSprite('common/crate_glow'));
		glowView.blendMode = PIXI.BLEND_MODES.ADD;
		glowView.scale.set(2);

		let glow = this._fCrateContainer_sprt.addChild(new Sprite());
		glow.addChild(glowView);

		this._glowView = glow;
		
		glow.position.set(0, 30);
		glow.alpha = 0;
		let sequence = [
			{
				tweens: [],
				duration: 1*FRAME_RATE
			},
			{
				tweens: [{prop: "alpha", to: 0.7}],
				duration: 3*FRAME_RATE
			},
			{
				tweens: [],
				duration: 2*FRAME_RATE
			},
			{
				tweens: [{prop: "alpha", to: 0}],
				duration: 7*FRAME_RATE
			}
		];
		Sequence.start(glow, sequence);
	}

	destroy()
	{
		if (this._fCrateContainer_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fCrateContainer_sprt));
		}

		if (this._revealWeaponBlast)
		{
			Sequence.destroy(Sequence.findByTarget(this._revealWeaponBlast));
		}

		if (this._glowView)
		{
			Sequence.destroy(Sequence.findByTarget(this._glowView));
		}

		if (this._fCrateView_sprt)
		{
			this._fCrateView_sprt.destroy()
			this._fCrateView_sprt = null;
		}

		this._removeBlockRevealTimer();
		while (this._contentItemsStack_arr.length)
		{
			this._contentItemsStack_arr.shift().destroy();
		}
		this._contentItemsStack_arr = null;

		if (this._itemsOrder)
		{
			while (this._itemsOrder.length)
			{
				let itemsInfos = this._itemsOrder.pop();
				if (!itemsInfos || !itemsInfos.length)
				{
					continue;
				}

				while (itemsInfos.length)
				{
					let itemInfo = itemsInfos.pop();
					itemInfo && itemInfo.destroy();
				}
				
			}
			this._itemsOrder = null;
		}

		this._fContentItems_arr = null;

		this._fCrateSmokeContainer_sprt = null;
		
		this._currentBlockIndex = undefined;
		this._blockRevealTimer = null;
		this._contentItemsContainer = null;
		
		this._streakTextures = null;

		this._revealWeaponBlast = null;
		this._glowView = null;

		this._fIntroAnimationInprogress_bl = undefined;
		this._fOutroAnimationInprogress_bl = undefined;

		super.destroy();
	}
}

export default Crate;