import BossEnemy from './BossEnemy';
import { DIRECTION } from './Enemy';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import PathTween from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/PathTween';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

const SPAWN_POINT = {x: 1052, y: -100}
const SHADOW_ALPHA = 1;

export const STATE_CALL 	= 'call';

class ApeBossEnemy extends BossEnemy 
{
	static get EVENT_ON_APE_LANDED ()	{ return "EVENT_ON_APE_LANDED" }

	//override
	_invalidateStates()
	{
		this._onAppearingSuspicion();
		super._invalidateStates();
	}

	//APPEARANCE...
	_onAppearingSuspicion()
	{
		if (Utils.isEqualPoints(this.trajectory.points[0], this.trajectory.points[1]) &&
			!this._fIsLasthand_bl)
		{
			this._fCryogunsController_cgs.i_isEnemyFrozen(this.id) && this._unfreeze();
			this._fIsEnableFreeze_bl = false;
		}
		else
		{
			this._fIsEnableFreeze_bl = true;
		}
	}

	_startAppearing()
	{
		this._animateApiaring();

		let lAppearingTime_num = this.trajectory.points[2].time - this.trajectory.points[0].time;
		if(!this._fTotalAppearingTimer_t)
		{
			this._fTotalAppearingTimer_t = new Timer(this._finishAppearing.bind(this), lAppearingTime_num);
		}

		this.shadow.alpha = 0;
		this.shadow.tint = 0x000000;
		this.shadow.fadeTo(SHADOW_ALPHA, 12*FRAME_RATE, Easing.sine.easeOut);
		this.shadow.visible = true;
	}

	_finishAppearing()
	{
		this._fIsAppearingInProgress = false;
		this._fIsEnableFreeze_bl = true;

		this._fTotalAppearingTimer_t && this._fTotalAppearingTimer_t.destructor();
		this._fTotalAppearingTimer_t = null;
	}

	_freeze(aIsAnimated_bl = true)
	{
		if (!this._fIsEnableFreeze_bl)
		{
			return;
		}
		super._freeze(aIsAnimated_bl);
	}

	setStayIfPossible()
	{
		if (this.isFrozen)
		{
			return;
		}

		this.setStay();
	}

	_animateApiaring()
	{
		this.position = SPAWN_POINT;
		let lLandingPoint_p = this.trajectory.points[0];

		this._fPosTween_pt = new PathTween(this, [{x: SPAWN_POINT.x, y: SPAWN_POINT.y}, {x: SPAWN_POINT.x - 300, y: SPAWN_POINT.y + 150}, {x: lLandingPoint_p.x, y: lLandingPoint_p.y}], true);
		this._fPosTween_pt.start(12 * FRAME_RATE, Easing.cubic.easeIn, () => { 
			//onfinish
			this._onLanding();
		});
	}

	_onLanding()
	{
		this._fIsAppearingInProgress = true;

		this.emit(ApeBossEnemy.EVENT_ON_APE_LANDED);

		this.changeTextures(STATE_CALL);
		this.stateListener = {complete: () =>{
			this.spineView && this.spineView.stop();
			this.setStayIfPossible();
			this._fIsAppearingInProgress = false;
		}};

		if (this.spineView && this.spineView.view.state && this.spineView.view.state)
		{
			this.spineView.view.state.addListener(this.stateListener);
		}
	}

	_calculateDirection()
	{
		return ApeBossEnemy.getDirection(this.angle, this._fIsAppearingInProgress);
	}

	//override
	_generateBossAppearanceMask()
	{
		return null;
	}

	static getDirection(angle, aIsAppearing_bl)
	{
		let direction = DIRECTION.LEFT_DOWN;
		if (angle > Math.PI*2) angle -= Math.PI*2;

		if (angle > 0) direction = DIRECTION.RIGHT_DOWN;
		if (angle > Math.PI/2) direction = DIRECTION.LEFT_DOWN;
		if (angle > Math.PI) direction = DIRECTION.LEFT_UP;
		if (angle > Math.PI*3/2) direction = DIRECTION.RIGHT_UP;

		if(aIsAppearing_bl)
		{
			direction = DIRECTION.LEFT_DOWN;
		}

		return direction
	}

	showBossAppearance()
	{
		if (this.container) this.container.visible = true;
		this._startAppearing();
	}

	//override
	get turnPostfix()
	{
		return this.isHealthStateWeak ? "_weak_turn" : "_walk_turn";
	}

	//override
	getSpineSpeed()
	{
		let lBaseSpeed_num = 0;
		if(this._fIsAppearingInProgress)
		{
			lBaseSpeed_num = 0.211;
		}
		else
		{
			lBaseSpeed_num = this.isHealthStateWeak ? 0.12 : 0.064;
		}
		
		return this.speed * this.getScaleCoefficient() * lBaseSpeed_num;
	}

	//override
	changeZindex()
	{
		super.changeZindex();
		switch (this.direction)
		{
			case DIRECTION.LEFT_DOWN:
			case DIRECTION.LEFT_UP:
				this.zIndex += 15;
				break;
			case DIRECTION.RIGHT_UP:
				this.zIndex += 25;
				break;
			case DIRECTION.RIGHT_DOWN:
				this.zIndex += 75;
				break;
		}
	}

	//override
	changeShadowPosition()
	{
		//do nothing
	}

	//override
	getScaleCoefficient()
	{
		return 1.6;
	}

	//override
	getLocalCenterOffset()
	{
		let pos = new PIXI.Point();
		switch (this.direction)
		{
			case DIRECTION.LEFT_DOWN:
				pos.x = -45;
				pos.y = -25;
				break;
			case DIRECTION.LEFT_UP:
				pos.x = -45;
				pos.y = -45;
				break;
			case DIRECTION.RIGHT_DOWN:
				pos.x = 25;
				pos.y = -25;
				break;
			case DIRECTION.RIGHT_UP:
				pos.x = 20;
				pos.y = -45;
				break;
		}
		let scale = this.getScaleCoefficient();
		pos.x *= scale;
		pos.y *= scale;
		return pos;
	}

	//override
	_getHitRectHeight()
	{
		return 180;
	}

	//override
	_getHitRectWidth()
	{
		return 200;
	}

	//override
	_calcWalkAnimationName(aDirection_str)
	{
		let lWalkAnimationSuffix_str = this.isHealthStateWeak ? 'weak' : "walk";
		return super._calcWalkAnimationName(aDirection_str, lWalkAnimationSuffix_str);
	}


	destroy()
	{
		this._fTotalAppearingTimer_t && this._fTotalAppearingTimer_t.destructor();
		this._fTotalAppearingTimer_t = null;

		PathTween.destroy(PathTween.findByTarget(this));

		super.destroy();
	}
}

export default ApeBossEnemy;