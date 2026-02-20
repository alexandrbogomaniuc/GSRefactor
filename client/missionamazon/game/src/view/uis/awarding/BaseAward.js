import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';


class BaseAward extends Sprite
{
	static get EVENT_AWARD_COUNTED()			{ return "onBaseAwardCounted"; }
	static get EVENT_ANIMATION_COMPLETED()		{ return "onBaseAwardAnimationCompleted"; }


	showAwarding(aValue_num, aParams_obj)
	{
		this._fUncountedValue_num = aValue_num;
		this._fSeatId_int = aParams_obj.seatId;
		this._fIsBoss_bl = aParams_obj.isBoss;
		this._showAwarding(aValue_num, aParams_obj);
	}

	get uncountedValue()
	{
		return this._fUncountedValue_num;
	}

	get awardType()
	{
		return this._fAwardType_int;
	}

	set currentStake(aValue_num)
	{
		this._fCurrentStake_num = aValue_num;
	}

	get currentStake()
	{
		return this._fCurrentStake_num;
	}

	constructor(aGameField_sprt, aAwardType_int)
	{
		super();

		this._fGameField_sprt = aGameField_sprt;
		this._fAwardType_int = aAwardType_int;
		this._fUncountedValue_num = undefined;
		this._fCurrentStake_num = undefined;
		this._fTimer_t = null;
		this._fSeatId_int = null;
	}

	_showAwarding(aValue_num, aCurrentStake_num, aParams_obj)
	{
		this._startContinuousDelay();
	}

	_startContinuousDelay()
	{
		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = new Timer(this._continueAwarding.bind(this), this._waitingTime);
	}

	get _waitingTime()
	{
		return 700;
	}

	get _defaultPoint()
	{
		return new PIXI.Point(480, 540);
	}

	_continueAwarding()
	{
		this._completeAwarding();
	}

	_completeAwarding()
	{
		this.emit(BaseAward.EVENT_ANIMATION_COMPLETED);
		this.destroy();
	}

	_onAwardCounted(aValue_num)
	{
		if (this._fUncountedValue_num !== undefined && aValue_num > 0)
		{
			this._fUncountedValue_num = Math.max(this._fUncountedValue_num - aValue_num, 0);
		}
		this.emit(BaseAward.EVENT_AWARD_COUNTED, this._generateAwardCountedEventData(aValue_num));
	}

	_generateAwardCountedEventData(aValue_num)
	{
		return {value:aValue_num, seatId: this._fSeatId_int, isBoss: this._fIsBoss_bl}
	}

	_calcDropPosition(aDropDistance_num, aParams_obj)
	{
		let lDropPosition_pt = new PIXI.Point();
		let lStartX_num = aParams_obj.start ? aParams_obj.start.x : 0;
		let lStartY_num = aParams_obj.start ? aParams_obj.start.y : 0;

		if (aParams_obj.previous)
		{
			let lOffsetX_num = lStartX_num - aParams_obj.previous.x;
			let lOffsetY_num = lStartY_num - aParams_obj.previous.y;
			
			let lDistanceToprevTurnPosition_num = Math.sqrt(Math.pow(lOffsetX_num, 2) + Math.pow(lOffsetY_num, 2));
			if (lDistanceToprevTurnPosition_num > aDropDistance_num)
			{
				lDropPosition_pt.x = lStartX_num - (aDropDistance_num / lDistanceToprevTurnPosition_num) * lOffsetX_num;
				lDropPosition_pt.y = lStartY_num - (aDropDistance_num / lDistanceToprevTurnPosition_num) * lOffsetY_num;
			}
			else
			{
				lDropPosition_pt.x = aParams_obj.previous.x;
				lDropPosition_pt.y = aParams_obj.previous.y;
			}
		}
		else
		{
			lDropPosition_pt.x = lStartX_num;
			lDropPosition_pt.y = lStartY_num;
		}

		if (aParams_obj.footPoint)
		{
			lDropPosition_pt.x += aParams_obj.footPoint.x;
			lDropPosition_pt.y += aParams_obj.footPoint.y;
		}

		return lDropPosition_pt;
	}

	_placeOnGameField(aObject_sprt, aPosition_pt)
	{
		if (this._fGameField_sprt)
		{
			this._fGameField_sprt.addChild(aObject_sprt);
			aObject_sprt.position.set(aPosition_pt.x, aPosition_pt.y);
			aObject_sprt.zIndex = aPosition_pt.y;
		}
		return aObject_sprt;
	}

	destroy()
	{
		this._fTimer_t && this._fTimer_t.destructor();
		this._fTimer_t = null;
		
		this._fGameField_sprt = null;
		this._fUncountedValue_num = undefined;

		this._fSeatId_int = null;

		super.destroy();
	}
}

export default BaseAward;