import AppearanceView from './AppearanceView';
import { ENEMIES, FRAME_RATE } from '../../../../../../../shared/src/CommonConstants';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DeathFxAnimation from '../../../../../main/animation/death/DeathFxAnimation';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

const SMOKE_CONTAINER_TYPES = { BACK: "back", FRONT: "front"}
const LANDING_SMOKES_SETTING = [
	{containerType: SMOKE_CONTAINER_TYPES.BACK, posDelta: {x: 0 - 25, y: 0}, scaleMult: 1.6},
	{containerType: SMOKE_CONTAINER_TYPES.BACK, posDelta: {x: 9 - 25, y: -55}, scaleMult: 0.4},
	{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -20 - 25, y: -11.5}, scaleMult: 0.7},
	{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 34 - 25, y: 48}, scaleMult: 0.4},
	{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -61 - 25, y: 38}, scaleMult: 0.4},
	{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -78 - 25, y: 36}, scaleMult: 0.4},
	{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -81 - 25, y: 52}, scaleMult: 0.4},
	{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 42 - 25, y: 65}, scaleMult: 0.4},
	{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -16 - 25, y: 63}, scaleMult: 0.4}
];

const APPEARING_SMOKES_SETTING = [
	{appearingIntroDelay: 29 * FRAME_RATE, posDelta: {x: -12 - 25, y: -110 - 40}, scaleMult: 0.4},
	{appearingIntroDelay: 32 * FRAME_RATE, posDelta: {x: 9 - 25, y: -101 - 40}, scaleMult: 0.4},
	{appearingIntroDelay: 34 * FRAME_RATE, posDelta: {x: 1 - 25, y: -132 - 40}, scaleMult: 0.4},
	{appearingIntroDelay: 36 * FRAME_RATE, posDelta: {x: 14 - 25, y: -110 - 40}, scaleMult: 0.4},
	{appearingIntroDelay: 37 * FRAME_RATE, posDelta: {x: -1 - 25, y: -136 - 40}, scaleMult: 0.4},
];

class ApeAppearanceView extends AppearanceView
{
	constructor(aViewContainerInfo_obj)
	{
		super();

		this._fViewContainerInfo_obj = aViewContainerInfo_obj;
		this._fSmokes_arr = [];
		this._fAppearingSmokesTimers_arr_t = [];
	}

	//INIT...
	get _captionPosition()
	{
		return { x:0, y:-4 };
	}

	get _bossType()
	{
		return ENEMIES.ApeBoss;
	}
	//...INIT

	_startAppearing(aZombieView_e)
	{
		this._fBossZombie_e = aZombieView_e;
		this._fBossZombie_e && this._fBossZombie_e.showBossAppearance();

		DeathFxAnimation.initTextures();

		this.emit(AppearanceView.EVENT_APPEARING_STARTED);

		this._fBossZombie_e.once("EVENT_ON_APE_LANDED", this._onApeLanded, this);
		this._startYellowScreenAnimation();
	}

	_onApeLanded()
	{
		APP.gameScreen.gameField.shakeTheGround();
		this._onTimeToStartCaptionAnimation();
		this._showLandingSmokes();
	}

	_showLandingSmokes()
	{
		if (!APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater)
		{
			return;
		}

		this._fBackSmokesContainer = this._fViewContainerInfo_obj.container.addChild(new Sprite);
		this._fBackSmokesContainer.zIndex = this._fViewContainerInfo_obj.circleZIndex;

		this._fFrontSmokesContainer = this.addChild(new Sprite);

		let lApe_e = this._fBossZombie_e;
		let lBackBasePosition_obj = lApe_e.parent.localToLocal(lApe_e.x, lApe_e.y, this._fBackSmokesContainer);
		let lFrontBasePosition_obj = lApe_e.parent.localToLocal(lApe_e.x, lApe_e.y, this._fFrontSmokesContainer);
		
		for (let i=0; i<LANDING_SMOKES_SETTING.length; i++)
		{
			let lSmokeSetting_obj = LANDING_SMOKES_SETTING[i];
			let lSmokeContainer_spr = lSmokeSetting_obj.containerType === SMOKE_CONTAINER_TYPES.BACK ? this._fBackSmokesContainer : this._fFrontSmokesContainer;
			let lBasePos_spr = lSmokeSetting_obj.containerType === SMOKE_CONTAINER_TYPES.BACK ? lBackBasePosition_obj : lFrontBasePosition_obj;

			this._playSmokeEffect(lSmokeContainer_spr, lBasePos_spr, lSmokeSetting_obj.posDelta, lSmokeSetting_obj.scaleMult);
		}
		
		for (let i=0; i<LANDING_SMOKES_SETTING.length; i++)
		{
			let lSmokeSetting_obj = LANDING_SMOKES_SETTING[i];
			let lSmokeContainer_spr = lSmokeSetting_obj.containerType === SMOKE_CONTAINER_TYPES.BACK ? this._fBackSmokesContainer : this._fFrontSmokesContainer;
			let lBasePos_spr = lSmokeSetting_obj.containerType === SMOKE_CONTAINER_TYPES.BACK ? lBackBasePosition_obj : lFrontBasePosition_obj;

			this._playSmokeEffect(lSmokeContainer_spr, lBasePos_spr, lSmokeSetting_obj.posDelta, lSmokeSetting_obj.scaleMult);
		}

		this._cleareTimers();
		for (let i = 0; i < APPEARING_SMOKES_SETTING.length; i++)
		{
			let lSmokeSetting_obj = APPEARING_SMOKES_SETTING[i];
			let lSmokeContainer_spr = this._fFrontSmokesContainer;
			let lBasePos_spr = lFrontBasePosition_obj;

			let l_t = new Timer(()=>this._playSmokeEffect(lSmokeContainer_spr, lBasePos_spr, lSmokeSetting_obj.posDelta, lSmokeSetting_obj.scaleMult), lSmokeSetting_obj.appearingIntroDelay);
			this._fAppearingSmokesTimers_arr_t.push(l_t);
		}
	}

	_cleareTimers()
	{
		if(this._fAppearingSmokesTimers_arr_t && this._fAppearingSmokesTimers_arr_t.length)
		{
			while (this._fAppearingSmokesTimers_arr_t.length)
			{
				let l_t = this._fAppearingSmokesTimers_arr_t.shift();
				l_t.destructor();
			}
		}
	}

	_playSmokeEffect(aContainer_spr, aBasePosition_obj, aPositionDelta_obj = {x: 0, y: 0}, aScaleMult_num = 1)
	{
		let lEffect_spr = aContainer_spr.addChild(Sprite.createMultiframesSprite(DeathFxAnimation.textures["smokePuff"], 3));
		lEffect_spr.blendMode = PIXI.BLEND_MODES.SCREEN;
		lEffect_spr.scale.set(2 * aScaleMult_num);
		lEffect_spr.position.set(aBasePosition_obj.x + aPositionDelta_obj.x, aBasePosition_obj.y + aPositionDelta_obj.y);
		lEffect_spr.animationSpeed = 24/60;
		lEffect_spr.play();
		lEffect_spr.once('animationend', (e) => {
			let lSmokeIndex_num = this._fSmokes_arr.indexOf(e.target);
			if (lSmokeIndex_num >= 0)
			{
				this._fSmokes_arr.splice(lSmokeIndex_num, 1);
			}
			e.target.destroy();

			if (!this._fSmokes_arr.length)
			{
				this._onAppearingCompleted();
			}
		});

		this._fSmokes_arr.push(lEffect_spr);

		return lEffect_spr;
	}

	_onTimeToStartCaptionAnimation()
	{
		this.emit(AppearanceView.EVENT_ON_TIME_TO_START_CAPTION_ANIMATION, {captionPosition: this._captionPosition, startDelay: 5*FRAME_RATE});
	}

	destroy()
	{
		this._cleareTimers();
		super.destroy();
	}
}

export default ApeAppearanceView