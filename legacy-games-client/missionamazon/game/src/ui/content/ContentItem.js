import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PrizeView from './PrizeView';
import Timer from "../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer";
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import PathTween from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/PathTween';
import ContentItemInfo from '../../model/uis/content/ContentItemInfo';
import { GREYSCALE_FILTER } from '../../config/Constants';
import { FRAME_RATE, WEAPONS} from '../../../../shared/src/CommonConstants';
import { Sequence } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import FreeShotsCounterView from './FreeShotsCounterView';
import WeaponAwardingPointFx from '../../main/animation/WeaponAwardingPointFx';

const MAX_SCALE = 0.13;
const BASE_SCALE = 10;
const AMMO_COUNTER_OFFSET = 170;

class ContentItem extends Sprite
{
	static get ON_CONTENT_ANIMATION_STARTED()	{return "onContentAnimationStarted";}
	static get ON_CONTENT_APPEARED()			{return "onContentAppeared";}
	static get ON_CONTENT_LANDED()				{return "onContentLanded";}
	static get ON_CONTENT_LANDING()				{return "onContentLanding";}

	constructor(itemInfo)
	{
		super();

		this._itemInfo = null;
		this._contentContainer = null;
		this._prizeView = null;

		this._fPrizeGlow_sprt = null;

		this._startTimer = null;
		this._blockIndex = undefined;

		this._topPositionDelta = null;

		this._fSparklesContainer_sprt = null;
		this._fWeaponAwardingPointFx_wapf = null;
		this._fFlare_sprt = null;

		this._animationSpeed = 1;
		this._masterSeatId = APP.gameScreen.gameField.seatId;

		this._fFreeShotsCounterView_fscv = null;

		this._initContent(itemInfo);
	}

	destroy()
	{
		if (this._contentContainer)
		{
			PathTween.destroy(PathTween.findByTarget(this._contentContainer));
			Sequence.destroy(Sequence.findByTarget(this._contentContainer));
		}

		this._itemInfo = null;
		this._contentContainer = null;
		this._prizeView = null;

		if (this._fFlare_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fFlare_sprt));
			this._fFlare_sprt.destroy();
		}
		this._fFlare_sprt = null;

		if (this._fPrizeGlow_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fPrizeGlow_sprt));
			this._fPrizeGlow_sprt.destroy();
		}
		this._fPrizeGlow_sprt = null;

		this._blockIndex = undefined;
		this._animationSpeed = undefined;

		this._topPositionDelta = null;

		if (this._fSparklesContainer_sprt)
		{
			for (let lSparkle_sprt of this._fSparklesContainer_sprt.children)
			{
				Sequence.destroy(Sequence.findByTarget(lSparkle_sprt));
			}
			this._fSparklesContainer_sprt.destroy();
		}
		this._fSparklesContainer_sprt = null;

		if (this._fWeaponAwardingPointFx_wapf)
		{
			this._fWeaponAwardingPointFx_wapf.destroy();
		}
		this._fWeaponAwardingPointFx_wapf = null;

		this._masterSeatId = null;

		this._removeStartTimer();

		if (this._fFreeShotsCounterView_fscv)
		{
			this._fFreeShotsCounterView_fscv.off(Sprite.EVENT_ON_DESTROYING, this._onFlyingAmmoCounterDestroying, true);
			this._fFreeShotsCounterView_fscv.destroy();
			this._fFreeShotsCounterView_fscv = null;
		}

		super.destroy();
	}

	startAnimation(topPositionDelta, aShowFXs_bln, animationSpeed)
	{
		if (animationSpeed)
		{
			this._animationSpeed = 1 / animationSpeed;
		}

		if (Boolean(aShowFXs_bln))
		{
			this._addBackFXs();
		}

		this._addPrizeView();

		this._contentContainer.scale.set(0);
		this._topPositionDelta = topPositionDelta;

		this._startTimer = new Timer(this._onStartTimerCompleted.bind(this), 250);
		if (this._itemInfo.assetType === ContentItemInfo.TYPE_WEAPON) {
			this._tryToShowAmmoCounter();
		}
	}

	get info()
	{
		return this._itemInfo;
	}

	get blockIndex()
	{
		return this._blockIndex;
	}

	set blockIndex(value)
	{
		this._blockIndex = value;
	}

	getBaseScale()
	{
		return BASE_SCALE;
	}

	getMaxScale()
	{
		return MAX_SCALE;
	}

	getAmmoCounterOffset()
	{
		return AMMO_COUNTER_OFFSET;
	}	

	_initContent(itemInfo)
	{
		this._itemInfo = itemInfo;

		this._contentContainer = this.addChild(new Sprite());
		this._contentContainer.scale.set(0.8);
	}

	//FX...
	_addBackFXs()
	{
		//nothing to do
	}
	//...FX

	//PRIZE...
	_addPrizeView()
	{
		let itemInfo = this._itemInfo;
		let assetName = itemInfo.assetName;
		let assetTexture = itemInfo.assetTexture;
		let lAssetTextureGlow_txtr = itemInfo.assetTextureGlow;
		let lAssetGlowItemDescriptor_obj = itemInfo.assetGlowItemDescriptor;

		if (assetName !== undefined)
		{
			let assetAnchor = itemInfo.assetAnchor || undefined;
			this._prizeView = this._contentContainer.addChild(new PrizeView(assetName, assetAnchor, assetTexture));

			if (itemInfo.isWeaponType && itemInfo.weaponId == WEAPONS.HIGH_LEVEL)
			{
				const STRIPES_POSITIONS_Y = [6, -2, -10];
				for (let i = 0; i < 3; i++)
				{
					let lNormalStripe_spr = this._prizeView.addChild(APP.library.getSpriteFromAtlas("battleground/powerup/stripe_normal"));
					lNormalStripe_spr.position.set(11, STRIPES_POSITIONS_Y[i]);
				}

				let lHighLevelWeaponSkin = APP.playerController.info.getTurretSkinId(itemInfo.awardedWeapon.nextBetLevel);
				let lWeaponView = APP.library.getSpriteFromAtlas(`weapons/DefaultGun/turret_${lHighLevelWeaponSkin}/turret`);
				lWeaponView.scale.set(0.3);
				lWeaponView.position.set(-10, 0);

				this._prizeView.addChild(lWeaponView);
			}
			
			if (
				itemInfo.isWeaponType && itemInfo.weaponId !== WEAPONS.HIGH_LEVEL 	//always for usual weapon type
				|| APP.profilingController.info.isVfxProfileValueMediumOrGreater 	//for others depends on VFX
			) 
			{
				if (lAssetTextureGlow_txtr)
				{
					this._fPrizeGlow_sprt = this._prizeView.addChild(new Sprite());
					this._fPrizeGlow_sprt.texture = lAssetTextureGlow_txtr;
					this._fPrizeGlow_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
					this._fPrizeGlow_sprt.alpha = 0;
				}
				else if (lAssetGlowItemDescriptor_obj)
				{
					if(lAssetGlowItemDescriptor_obj.assetName != "battleground/powerup/background")
					{
						this._fPrizeGlow_sprt = this._prizeView.addChild(APP.library.getSprite(lAssetGlowItemDescriptor_obj.assetName));
					}else{
						this._fPrizeGlow_sprt = this._prizeView.addChild(APP.library.getSpriteFromAtlas(lAssetGlowItemDescriptor_obj.assetName));
					}
					const lAnchor_obj = lAssetGlowItemDescriptor_obj.anchor || assetAnchor;
					this._fPrizeGlow_sprt.anchor.set(lAnchor_obj.x, lAnchor_obj.y);
					this._fPrizeGlow_sprt.scale.set(lAssetGlowItemDescriptor_obj.scale || 1);

					this._fPrizeGlow_sprt.blendMode = PIXI.BLEND_MODES.ADD;
					this._fPrizeGlow_sprt.alpha = 0;
				}
			}
			this._prizeView.visible = true;

			this._wrapPrizeViewIfRequired();
		}
	}

	_wrapPrizeViewIfRequired()
	{
		let itemInfo = this._itemInfo;
		if (!isNaN(itemInfo.playerSeatId) && !itemInfo.isMasterSeat)
		{
			if(itemInfo.weaponId == WEAPONS.HIGH_LEVEL)
			{
				this._prizeView.visible = false;
			}
			else
			{
				this._prizeView.filters = [GREYSCALE_FILTER];
				this._prizeView.scale.set(this._prizeCoPlayerScale);
			}
		}
	}

	get _prizeCoPlayerScale()
	{
		return 0.7;
	}
	//...PRIZE

	_tryToShowAmmoCounter()
	{
		if (!this._itemInfo.isMasterSeat || this.info.awardedWeapon.id == WEAPONS.HIGH_LEVEL)
		{
			return;
		}

		let content = this._contentContainer;
		let initialX = content.x;
		let initialY = content.y;
		let topXDistance = this._topPositionDelta.x;
		let topYDistance = this._topPositionDelta.y;

		const lAwardedShots_int = this.info.awardedWeapon.shots;
		let lFreeShotsCounterView_fscv = content.addChild(new FreeShotsCounterView(lAwardedShots_int));
		this._fFreeShotsCounterView_fscv = lFreeShotsCounterView_fscv;
		lFreeShotsCounterView_fscv.once(Sprite.EVENT_ON_DESTROYING, this._onFlyingAmmoCounterDestroying, this);
		lFreeShotsCounterView_fscv.i_startFlying(new PIXI.Point(0, AMMO_COUNTER_OFFSET - 100));

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			let lGlobalPos_obj = this.localToGlobal(content.position.x, content.position.y);
			this._showWeaponsAwardingFx(lGlobalPos_obj.x, lGlobalPos_obj.y);
		}
	}

	_showWeaponsAwardingFx(x, y)
	{
		this._fWeaponAwardingPointFx_wapf = this._getSparklesContainer().container.addChild(new WeaponAwardingPointFx());
		this._fWeaponAwardingPointFx_wapf.zIndex = this._getSparklesContainer().zIndex;
		this._fWeaponAwardingPointFx_wapf.position.set(x, y);
	}

	_onFlyingAmmoCounterDestroying()
	{
		this._fFreeShotsCounterView_fscv = null;
	}

	_onStartTimerCompleted()
	{
		this._removeStartTimer();

		this._animate();
	}

	_removeStartTimer()
	{
		if (!this._startTimer)
		{
			return;
		}

		this._startTimer.destructor();
		this._startTimer = null;
	}

	_animate()
	{
		let content = this._contentContainer;
		let initialX = content.x;
		let initialY = content.y;
		let topXDistance = this._topPositionDelta.x;
		let topYDistance = this._topPositionDelta.y;
		var lAwardSound_str ;

		if (this._itemInfo.assetType === ContentItemInfo.TYPE_WEAPON)
		{
			this._animateSequence(content, initialX+topXDistance, initialY+topYDistance);
		}
		else
		{
			this._animateKeySequence(content, initialX+topXDistance, initialY+topYDistance);
		}

		if (this.info.assetType == ContentItemInfo.TYPE_WEAPON)
		{
			lAwardSound_str = "weapon_drop";
		}

		if (APP.soundsController.isSoundPlaying(lAwardSound_str))
		{
			APP.soundsController.stop(lAwardSound_str);
		}

		if (this._isMasterPlayer)
		{
			APP.soundsController.play(lAwardSound_str, false);
		}

		let contentGlobalPos = content.parent.localToGlobal(content.position.x, content.position.y);
		this.emit(ContentItem.ON_CONTENT_ANIMATION_STARTED, {landGlobalX: contentGlobalPos.x, landGlobalY: contentGlobalPos.y, playerSeatId:this._itemInfo.playerSeatId});
	}

	get _isMasterPlayer()
	{
		return this._itemInfo.playerSeatId === APP.playerController.info.seatId;
	}

	_animateKeySequence(aContent_sprt, x, y)
	{
		aContent_sprt.rotation = Utils.gradToRad(-274);

		aContent_sprt.scaleTo(1, 7*FRAME_RATE * this._animationSpeed);

		aContent_sprt.rotateTo(Utils.gradToRad(-7), 20*FRAME_RATE * this._animationSpeed, Easing.quintic.easeOut, () =>
			aContent_sprt.rotateTo(Utils.gradToRad(-19), 8*FRAME_RATE * this._animationSpeed, Easing.linear.easeInOut, () =>
				aContent_sprt.rotateTo(Utils.gradToRad(-5), 7*FRAME_RATE * this._animationSpeed, Easing.linear.easeInOut, () => this._onItemAppeared())
			)
		);

		aContent_sprt.moveTo(x, y - 18, 6*FRAME_RATE * this._animationSpeed, Easing.linear.easeInOut, () => {
			aContent_sprt.moveTo(x, y + 5, 5*FRAME_RATE * this._animationSpeed, Easing.linear.easeInOut, () => {
				aContent_sprt.moveTo(x, y, 12*FRAME_RATE * this._animationSpeed, Easing.linear.easeInOut)
					}
				);
			}
		);

		if (this._fPrizeGlow_sprt)
		{
			this._fPrizeGlow_sprt.alpha = 1;

			let lGlowSeq_arr = [];
			if (this._itemInfo.isWeaponType)
			{
				lGlowSeq_arr = [
					{tweens: [{prop:"alpha", to: 0.14}], duration: 	18*FRAME_RATE},
					{tweens: [{prop:"alpha", to: 0.70}], duration: 	4*FRAME_RATE},
					{tweens: [{prop:"alpha", to: 0.50}], duration: 	9*FRAME_RATE}
				];
			}
			else {
				lGlowSeq_arr = [
					{tweens: [],							duration: 7*FRAME_RATE},
					{tweens: [{prop: 'alpha', to: 0}],		duration: 9*FRAME_RATE},
				];
			}

			Sequence.start(this._fPrizeGlow_sprt, lGlowSeq_arr);
		}


		if (APP.profilingController.info.isVfxProfileValueLowerOrGreater)
		{
			let lFlare_sprt = this._fFlare_sprt = aContent_sprt.addChild(APP.library.getSprite("round_result/flare"));
			lFlare_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
			lFlare_sprt.position.set(-24, -18);
			lFlare_sprt.scale.set(2);

			let lFlareSeq_arr = [
				{tweens: [{prop: 'scale.x', to: 2},		{prop: 'scale.y', to: 2}],		duration: 4*FRAME_RATE},
				{tweens: [{prop: 'scale.x', to: 0.62},	{prop: 'scale.y', to: 0.62}],	duration: 9*FRAME_RATE}
			];

			Sequence.start(lFlare_sprt, lFlareSeq_arr, 7*FRAME_RATE);
		}
	}

	//for AWARDED WEAPONS only
	_animateSequence(content, x, y)
	{
		content.scale.set(0);
		let scaleSequence = [
			{
				tweens: [
					{ prop: "scale.x", to: BASE_SCALE * 0.1},
					{ prop: "scale.y", to: BASE_SCALE * 0.1},
				],
				duration: FRAME_RATE * 3,
				ease: Easing.sine.easeInOut
			},
			{
				tweens: [],
				duration: FRAME_RATE * 4
			},
			{
				tweens: [
					{ prop: "scale.x", to: BASE_SCALE * 0.111},
					{ prop: "scale.y", to: BASE_SCALE * 0.111},
				],
				duration: FRAME_RATE * 7,
				ease: Easing.sine.easeInOut
			},
			{
				tweens: [
					{ prop: "scale.x", to: BASE_SCALE * MAX_SCALE},
					{ prop: "scale.y", to: BASE_SCALE * MAX_SCALE},
				],
				duration: FRAME_RATE * 4,
				ease: Easing.sine.easeInOut,
				onfinish: () => this._onItemAppeared()
			},
			{
				tweens: [
					{ prop: "scale.x", to: BASE_SCALE * 0.02},
					{ prop: "scale.y", to: BASE_SCALE * 0.02},
				],
				duration: FRAME_RATE * 8,
				ease: Easing.sine.easeInOut
			}
		];
		Sequence.start(content, scaleSequence);

		content.rotation = Utils.gradToRad(60 + 8.6);
		let rotationSequence = [
			{
				tweens: [ { prop: "rotation", to: Utils.gradToRad(0.4 + 8.6)}],
				duration: 5 * FRAME_RATE,
				ease: Easing.sine.easeInOut
			},
			{
				tweens: [ { prop: "rotation", to: Utils.gradToRad(3 + 8.6)}],
				duration: 10 * FRAME_RATE,
				ease: Easing.sine.easeInOut
			},
			{
				tweens: [ { prop: "rotation", to: Utils.gradToRad(-20 + 8.6)}],
				duration: 7 * FRAME_RATE,
				ease: Easing.sine.easeInOut
			}
		];
		Sequence.start(content, rotationSequence);

		let offset = {x: 0, y: 0};
		offset.x = this.position.x > 960/2 ? 50 : -50;
		offset.y = -70;

		content.moveTo(x + offset.x, y + offset.y, 8 * FRAME_RATE * this._animationSpeed, Easing.sine.easeOut);

		if (this._fPrizeGlow_sprt)
		{
			this._fPrizeGlow_sprt.alpha = 1;

			let lGlowSeq_arr = [];
			if (this._itemInfo.isWeaponType)
			{
				lGlowSeq_arr = [
					{tweens: [{prop:"alpha", to: 0.14}], duration: 	9*FRAME_RATE},
					{tweens: [{prop:"alpha", to: 0.70}], duration: 	2*FRAME_RATE},
					{tweens: [{prop:"alpha", to: 0.50}], duration: 	5*FRAME_RATE}
				];
			}
			else {
				lGlowSeq_arr = [
					{tweens: [],							duration: 7*FRAME_RATE},
					{tweens: [{prop: 'alpha', to: 0}],		duration: 9*FRAME_RATE},
				];
			}
			Sequence.start(this._fPrizeGlow_sprt, lGlowSeq_arr);
			if (APP.profilingController.info.isVfxProfileValueLowerOrGreater)
			{
				this._fFreeShotsCounterView_fscv && this._fFreeShotsCounterView_fscv.i_startGlowSequence(lGlowSeq_arr);
			}
		}
	}

	_onItemAppeared()
	{
		this.emit(ContentItem.ON_CONTENT_APPEARED);

		this._tryToStartFinalAnimation();
	}

	_tryToStartFinalAnimation()
	{
		this._startFinalAnimation();
	}

	_startFinalAnimation()
	{
		let content = this._contentContainer;

		let lScaleTo_num = 0.22;
		let lScaleTime_num = 9*FRAME_RATE;
		let flyTime = 10*FRAME_RATE;
		let rotateTime = 6*FRAME_RATE;
		let rotation = 125;

		let rx = Utils.random(content.x, endX);
		let ry = (content.y - endY < 0) ? content.y - 150 : content.y + 150;

		if (this._itemInfo.assetType !== ContentItemInfo.TYPE_WEAPON)
		{
			lScaleTo_num = 0.3;
			lScaleTime_num = 12*FRAME_RATE;
			flyTime = 12*FRAME_RATE;
			rotateTime = 12*FRAME_RATE;
			rotation = 360;

			rx = 0;
			ry = 0;

			if (this._fPrizeGlow_sprt)
			{
				let lGlowSeq_arr = [
					{tweens: [{prop: 'alpha', to: 0.9}],	duration: 6*FRAME_RATE},
					{tweens: [],							duration: 4*FRAME_RATE},
					{tweens: [{prop: 'alpha', to: 0}],		duration: 6*FRAME_RATE, ease: Easing.sine.easeOut},
				];

				Sequence.start(this._fPrizeGlow_sprt, lGlowSeq_arr);
			}

			if(APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater)
			{
				let lGlobalPos_obj = this.localToGlobal(content.position.x, content.position.y);
				this._animateSparkles(lGlobalPos_obj.x, lGlobalPos_obj.y + 50);
			}

			content.rotateTo(Utils.gradToRad(rotation), rotateTime * this._animationSpeed, Easing.linear.easeInOut);

			content.scaleTo(1, 1*FRAME_RATE, Easing.linear.easeIn, () => {
					content.scaleTo(lScaleTo_num, lScaleTime_num * this._animationSpeed, Easing.linear.easeInOut);
				}
			);
		}

		let lFinalPos_obj = this._finalLocalPosition;
		let endX = lFinalPos_obj.x;
		let endY = lFinalPos_obj.y;
		let t = new PathTween(content, [{x: content.x, y: content.y}, {x: rx, y: ry}, {x: endX, y: endY}], true);

		t.start(flyTime * this._animationSpeed, Easing.cubic.easeIn,
					() => this._onMovedToFinalPoint()
				);
		//this._fFreeShotsCounterView_fscv && this._fFreeShotsCounterView_fscv.i_startFinalMove(flyTime * this._animationSpeed, lFinalPos_obj, Easing.cubic.easeIn);

		this.emit(ContentItem.ON_CONTENT_LANDING);
	}

	_animateSparkles(x, y)
	{
		this._fSparklesContainer_sprt = this._getSparklesContainer().container.addChild(new Sprite());
		this._fSparklesContainer_sprt.zIndex = this._getSparklesContainer().zIndex;
		this._fSparklesContainer_sprt.position.set(x, y);

		let k = 0;
		for (let i = 0; i < 16; ++i)
		{
			let lPos_obj = {x: -18*i, y: 5*i};

			let lSparkle_sprt = this._fSparklesContainer_sprt.addChild(this._generateSprkle());
			lSparkle_sprt.position.set(lPos_obj.x, lPos_obj.y);
			lSparkle_sprt.alpha = 0;

			let lSeq_arr = [
				{tweens: [{prop: 'position.x', to: lPos_obj.x - 9}, {prop: 'position.y', to: lPos_obj.y + 2.5}, {prop: "alpha", to: 0.9}], ease: Easing.quadratic.easeIn, duration: 1*FRAME_RATE},
				{tweens: [{prop: 'position.x', to: lPos_obj.x - 18}, {prop: 'position.y', to: lPos_obj.y + 5}, {prop: "alpha", to: 0}], ease: Easing.quadratic.easeOut, duration: 3*FRAME_RATE, onfinish: ()=>{
				lSparkle_sprt.destroy();
				if (++k >= 40)
				{
					this._fSparklesContainer_sprt.destroy();
				}
			}}];

			Sequence.start(lSparkle_sprt, lSeq_arr, (3 + i)*FRAME_RATE);
		}
	}

	_generateSprkle()
	{
		let lSparkle_sprt = APP.library.getSprite("quests/sparkles");
		lSparkle_sprt.scale.set(0.2);
		lSparkle_sprt.rotation = Utils.gradToRad(Utils.random(0, 360));
		lSparkle_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lSparkle_sprt.tint = 0xffeecc;

		return lSparkle_sprt;
	}

	_getSparklesContainer()
	{
		return {container: APP.gameScreen.gameField.awardingContainerInfo.keySparklesContainer, zIndex: APP.gameScreen.gameField.awardingContainerInfo.zIndex}
	}

	get _finalLocalPosition()
	{
		let globalPos = new PIXI.Point(1300, 300); // out of the screen

		let lIsMaster_bln = !!(this._itemInfo.playerSeatId == this._masterSeatId);
		let playerSeat = APP.currentWindow.gameField.getSeat(this._itemInfo.playerSeatId);
		if (!playerSeat)
		{
			let playerSpot = APP.currentWindow.gameField.spot;

			if (playerSpot && playerSpot.id === this._itemInfo.playerSeatId)
			{
				playerSeat = playerSpot;
			}
		}

		if (playerSeat)
		{
			if (this._itemInfo.isWeaponType)
			{
				if (playerSeat.isMaster)
				{
					globalPos = APP.currentWindow.gameField.getWeaponContentLandingPosition(this._itemInfo.weaponId);
				}
				else
				{
					globalPos = playerSeat.parent.localToGlobal(playerSeat.spotVisualCenterPoint);
				}
			}
		}

		return this.globalToLocal(globalPos.x, globalPos.y);
	}

	_onMovedToFinalPoint()
	{
		let content = this._contentContainer;
		content.scale.set(0);

		let contentGlobalPos = content.parent.localToGlobal(content.position.x, content.position.y);
		this.emit(ContentItem.ON_CONTENT_LANDED, {landGlobalX: contentGlobalPos.x, landGlobalY: contentGlobalPos.y, playerSeatId:this._itemInfo.playerSeatId, blockIndex:this.blockIndex});
	}
}

export default ContentItem