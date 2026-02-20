import EnemyShadow from './EnemyShadow';
import { ENEMIES } from '../../../../../shared/src/CommonConstants';
import SpineEnemy from './../SpineEnemy';
import Enemy from './../Enemy';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { STATE_WALK, STATE_IMPACT, STATE_DEATH, STATE_TURN, STATE_STAY, SPINE_SCALE, DIRECTION, TURN_DIRECTION } from './../Enemy';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class SpineEnemyShadow extends EnemyShadow {

	i_update()
	{
		if (this._fNotStoped_bl) this._update();
	}

	i_stopSpinePlaying()
	{
		this.spineView && this.spineView.stop();
		let timeScale = this._fSpineEnemy_se.spineSpeed;
		this.spineView && (this.spineView.view.state.timeScale = timeScale);
		this._fNotStoped_bl = false;
	}

	i_startSpinePlaying()
	{
		let timeScale = this._fSpineEnemy_se.spineSpeed;
		this.spineView && (this.spineView.view.state.timeScale = timeScale);
		this._fNotStoped_bl = true;
	}

	constructor(aSpineEnemy_se)
	{
		super(aSpineEnemy_se.name);
		this._fSpineEnemy_se = aSpineEnemy_se;
		this.spineView = null;

		this._fCurrentDirection_str = undefined;
		this._fCurrentAnimationName_str = undefined;

		this._fPureSpine_bl = undefined;

		this._lastImageName = undefined;
		this._fNotStoped_bl = true;

		this._init();
	}

	_init()
	{
		this._fSpineEnemy_se.on(Enemy.EVENT_ON_ENEMY_FREEZE, this._onEnemyFreeze, this);
		this._fSpineEnemy_se.on(Enemy.EVENT_ON_ENEMY_UNFREEZE, this._onEnemyUnfreeze, this);
		this._createSpineView();
	}

	//override
	_createView()
	{
		//empty
	}

	_createSpineView()
	{
		try
		{
			this.view = this.addChild(this._generateShadowView());
		}
		catch (e)
		{
			//console.info(e);
		}
	}

	_generateShadowView()
	{
		switch (this.enemyName)
		{
			case ENEMIES.SnakeStraight:
			case ENEMIES.ApeBoss:
				if (APP.stage.renderer.type == PIXI.RENDERER_TYPE.WEBGL)
				{
					return this._createSpineAnimation();
				}
				else
				{
					return new Sprite();
				}
		}

		return super._generateShadowView();
	}

	get enemyName()
	{
		return this._fSpineEnemy_se ? this._fSpineEnemy_se.params.name : undefined;
	}

	_createSpineAnimation()
	{
		this._fPureSpine_bl = true;
		this._update();
		return this.spineView;
	}

	_update()
	{
		if (!this._fSpineEnemy_se.spineView) return;

		if (this._fPureSpine_bl)
		{
			let lEnemyCurrentAnimationName_str = this._fSpineEnemy_se.currentAnimationName || this._fSpineEnemy_se.getWalkAnimationName();
			let lEnemyCurrentDirection_str = this._fSpineEnemy_se.direction;
			let timeScale = this._fSpineEnemy_se.spineSpeed;

			if (this._fCurrentAnimationName_str !== lEnemyCurrentAnimationName_str
				 || this._fCurrentDirection_str !== lEnemyCurrentDirection_str)
			{
				let lNewImageName_str = this._fSpineEnemy_se._fCurSpineName_str;

				if (lNewImageName_str !== this._lastImageName)
				{
					this._destroySpine();
					this.spineView = APP.spineLibrary.getSprite(lNewImageName_str);
					this._lastImageName = lNewImageName_str;
				}
				
				this.spineView.setAnimationsDefaultMixDuration(this._fSpineEnemy_se.spineView.view.stateData.defaultMix);

				let enemyCustomTransitions = this._fSpineEnemy_se.customSpineTransitions;
				if (enemyCustomTransitions && enemyCustomTransitions.length)
				{
					for (let i=0; i<enemyCustomTransitions.length; i++)
					{
						let enemyCustomTransition = enemyCustomTransitions[i];
						let fromName = enemyCustomTransition.fromName;
						let toName = enemyCustomTransition.toName;
						let transDuration = enemyCustomTransition.transDuration;

						this.spineView.setAnimationMix(fromName, toName, transDuration);
					}
				}
				this._fSpineEnemy_se.spineView.untint();
				let loop = this.enemyName !== ENEMIES.Jumper && !this._fSpineEnemy_se.isTurnState;
				this.spineView.setAnimationByName(0, lEnemyCurrentAnimationName_str, loop);

				this._fCurrentAnimationName_str = lEnemyCurrentAnimationName_str;
				this._fCurrentDirection_str = lEnemyCurrentDirection_str;

				this._adjust();

				this.view = this.addChild(this.spineView);
				this.spineView.view.state.timeScale = timeScale; //spineSpeed
			}

			this.spineView.stop();
			let lEnemyAnimationCurrentPosition_num = this._fSpineEnemy_se.spineView._lastUpdateDelta;
			this.spineView.updatePosition(lEnemyAnimationCurrentPosition_num);
		}
	}

	_adjust()
	{
		let pos = this._fSpineEnemy_se._getSpineViewOffset();

		let lBaseScale_num = SPINE_SCALE * this._fSpineEnemy_se.getScaleCoefficient();
		let blurFilter;
		let alphaFilter;
		let lBrightnessFilter_cmf;

		let resolution = APP.stage.renderer.resolution;

		switch (this.enemyName)
		{
			case ENEMIES.SnakeStraight:
				this.spineView.scale.set(lBaseScale_num*2.2, lBaseScale_num*2.2);
				this.spineView.position.set(pos.x + 3, pos.y + 3);
				this.spineView.alpha = 0.3;
				blurFilter = new PIXI.filters.BlurFilter();
				blurFilter.blurY = 2;
				blurFilter.blurX = 2;
				blurFilter.resolution = resolution;
				lBrightnessFilter_cmf = new PIXI.filters.ColorMatrixFilter();
				lBrightnessFilter_cmf.resolution = resolution;
				lBrightnessFilter_cmf.brightness(0);
				this.spineView.filters = [lBrightnessFilter_cmf, blurFilter];
				break;
			case ENEMIES.ApeBoss:
				this.spineView.scale.set(lBaseScale_num, -lBaseScale_num);
				switch (this._fSpineEnemy_se.direction)
				{
					case DIRECTION.RIGHT_UP:
						this.spineView.skew.set(Utils.gradToRad(10), Utils.gradToRad(-10));
						break;
					case DIRECTION.LEFT_DOWN:
						this.spineView.skew.set(0, Utils.gradToRad(-20));
						break;
					case DIRECTION.LEFT_UP:
						pos.y -= 15;
						this.spineView.skew.set(Utils.gradToRad(-10), 0);
						break;
					case DIRECTION.RIGHT_DOWN:
						this.spineView.skew.set(0, Utils.gradToRad(20));
						break;
				}
				this.spineView.position.set(pos.x, pos.y);
				blurFilter = new PIXI.filters.BlurFilter();
				blurFilter.blurY = 5;
				blurFilter.blurX = 5;
				blurFilter.resolution = resolution;
				alphaFilter = new PIXI.filters.AlphaFilter(0.3);
				alphaFilter.resolution = resolution;
				lBrightnessFilter_cmf = new PIXI.filters.ColorMatrixFilter();
				lBrightnessFilter_cmf.resolution = resolution;
				lBrightnessFilter_cmf.brightness(0);
				this.spineView.filters = [lBrightnessFilter_cmf, blurFilter, alphaFilter];
				break;
			default:
				break;
		}
	}

	_destroySpine()
	{
		if (this.spineView)
		{
			this.spineView.stop();
			this.spineView.destroy();
			this.spineView = null;
			this._lastImageName = undefined;
		}
	}

	_onEnemyFreeze(aEvent_ue)
	{
		this.spineView && this.spineView.stop();
	}

	_onEnemyUnfreeze(aEvent_ue)
	{
		this.spineView && this.spineView.play();
	}

	destroy()
	{
		this._fSpineEnemy_se && this._fSpineEnemy_se.off(Enemy.EVENT_ON_ENEMY_FREEZE, this._onEnemyFreeze, this);
		this._fSpineEnemy_se && this._fSpineEnemy_se.off(Enemy.EVENT_ON_ENEMY_UNFREEZE, this._onEnemyUnfreeze, this);

		this._destroySpine();
		this._fSpineEnemy_se = null;

		super.destroy();
	}
}

export default SpineEnemyShadow;