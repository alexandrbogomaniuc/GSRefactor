import { ENEMIES, FRAME_RATE } from '../../../../../../../shared/src/CommonConstants';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import Sequence from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import AppearanceView from './AppearanceView';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DeathFxAnimation from '../../../../../main/animation/death/DeathFxAnimation';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { ENEMY_DIRECTION } from '../../../../../config/Constants';

const SMOKE_CONTAINER_TYPES = { BACK: "back", FRONT: "front"}
const SMOKES_SETTINGS = {
	[ENEMY_DIRECTION.LEFT_UP]: 		[
										{containerType: SMOKE_CONTAINER_TYPES.BACK, posDelta: {x: 10, y: -66}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.BACK, posDelta: {x: -15, y: 0}, scaleMult: 0.7},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -145, y: -45}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 85, y: 25}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 110, y: 30}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -95, y: -25}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -80, y: 15}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 95, y: 0}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -20, y: 40}, scaleMult: 0.4}
									],
	[ENEMY_DIRECTION.LEFT_DOWN]: 	[
										{containerType: SMOKE_CONTAINER_TYPES.BACK, posDelta: {x: 10, y: -66}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.BACK, posDelta: {x: -15, y: 0}, scaleMult: 0.7},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -145, y: -45}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 85, y: 25}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 110, y: 30}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -95, y: -25}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -80, y: 15}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 95, y: 0}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -20, y: 40}, scaleMult: 0.4}
									],
	[ENEMY_DIRECTION.RIGHT_UP]: 	[
										{containerType: SMOKE_CONTAINER_TYPES.BACK, posDelta: {x: -10, y: -66}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.BACK, posDelta: {x: 15, y: 0}, scaleMult: 0.7},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 145, y: -45}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -85, y: 25}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -110, y: 30}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 95, y: -25}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 80, y: 15}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -95, y: 0}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 20, y: 40}, scaleMult: 0.4}
									],
	[ENEMY_DIRECTION.RIGHT_DOWN]: 	[
										{containerType: SMOKE_CONTAINER_TYPES.BACK, posDelta: {x: -10, y: -66}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.BACK, posDelta: {x: 15, y: 0}, scaleMult: 0.7},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 145, y: -45}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -85, y: 25}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -110, y: 30}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 95, y: -25}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 80, y: 15}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: -95, y: 0}, scaleMult: 0.4},
										{containerType: SMOKE_CONTAINER_TYPES.FRONT, posDelta: {x: 20, y: 40}, scaleMult: 0.4}
									]
}

class SpiderAppearanceView extends AppearanceView
{
	constructor(aViewContainerInfo_obj)
	{
		super();

		this._fViewContainerInfo_obj = aViewContainerInfo_obj;
		this._fSmokes_arr = [];
	}

	//INIT...
	get _captionPosition()
	{
		return { x:0, y:-4 };
	}

	get _bossType()
	{
		return ENEMIES.SpiderBoss;
	}
	//...INIT

	//ANIMATION..
	get _bossAppearanceSequences()
	{
		return [
					[
						{ tweens:[{prop:"y", to:0}], duration:8*FRAME_RATE, ease:Easing.sine.easeInOut }
					],
					[
						{ tweens:[],							duration: 4 * FRAME_RATE },
						{ tweens:[{prop: "scale.x", to: 1}],	duration: 3 * FRAME_RATE, ease:Easing.sine.easeInOut },
						{ tweens:[{prop: "scale.y", to: 0.9}],	duration: 2 * FRAME_RATE, ease:Easing.sine.easeInOut },
						{ tweens:[{prop: "scale.y", to: 1.04}],	duration: 3 * FRAME_RATE, ease:Easing.sine.easeInOut },
						{ tweens:[{prop: "scale.y", to: 1}],	duration: 3 * FRAME_RATE, ease:Easing.sine.easeInOut }
					]
				];
	}

	get _bossAppearanceInit()
	{
		return {x: 0, y: -300, scale: {x: 0.8, y: 1}};
	}

	_onAppearingIntroTime()
	{
		super._onAppearingIntroTime();

		DeathFxAnimation.initTextures();
	}

	get _appearingCulminationTime()
	{
		return 1 * FRAME_RATE;
	}

	_onAppearingCulminated()
	{
		super._onAppearingCulminated();

		this._fBossZombie_e.once("EVENT_ON_SPIDER_LANDED", this._onSpiderLanded, this);
	}

	_onSpiderLanded(event)
	{
		this._showLandingSmokes();

		APP.gameScreen.gameField.shakeTheGround();
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

		let spider = this._fBossZombie_e;
		let spiderDirection = spider.direction;
		let backBasePosition = spider.parent.localToLocal(spider.x, spider.y, this._fBackSmokesContainer);
		let frontBasePosition = spider.parent.localToLocal(spider.x, spider.y, this._fFrontSmokesContainer);
		
		let smokesSettings = SMOKES_SETTINGS[spiderDirection];
		for (let i=0; i<smokesSettings.length; i++)
		{
			let smokeSettings = smokesSettings[i];
			let smokeContainer = smokeSettings.containerType === SMOKE_CONTAINER_TYPES.BACK ? this._fBackSmokesContainer : this._fFrontSmokesContainer;
			let basePos = smokeSettings.containerType === SMOKE_CONTAINER_TYPES.BACK ? backBasePosition : frontBasePosition;

			this._playSmokeEffect(smokeContainer, basePos, smokeSettings.posDelta, smokeSettings.scaleMult);
		}
	}

	_playSmokeEffect(container, basePosition, positionDelta = {x: 0, y: 0}, scaleMult = 1)
	{
		let effect = container.addChild(Sprite.createMultiframesSprite(DeathFxAnimation.textures["smokePuff"], 3));
		effect.blendMode = PIXI.BLEND_MODES.SCREEN;
		effect.scale.set(2*scaleMult);
		effect.position.set(basePosition.x+positionDelta.x, basePosition.y+positionDelta.y);
		effect.animationSpeed = 24/60;
		effect.play();
		effect.once('animationend', (e) => {
			let smokeIndex = this._fSmokes_arr.indexOf(e.target);
			if (smokeIndex >= 0)
			{
				this._fSmokes_arr.splice(smokeIndex, 1);
			}
			e.target.destroy();

			if (!this._fSmokes_arr.length)
			{
				this._onAppearingCompleted();
			}
		});

		this._fSmokes_arr.push(effect);

		return effect;
	}

	get _completionDelay()
	{
		return 25*FRAME_RATE;
	}

	_onTimeToStartCaptionAnimation()
	{
		this.emit(AppearanceView.EVENT_ON_TIME_TO_START_CAPTION_ANIMATION, {captionPosition: this._captionPosition, startDelay: 5*FRAME_RATE});
	}
	//...ANIMATION
}

export default SpiderAppearanceView;