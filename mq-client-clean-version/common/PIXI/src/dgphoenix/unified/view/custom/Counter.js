import { Utils } from '../../model/Utils';
import NumberValueFormat from './values/NumberValueFormat';
import Tween from '../../controller/animation/Tween';
import EventDispatcher from '../../controller/events/EventDispatcher';

/**
 * Counter of number value.
 * @class
 * @augments EventDispatcher
 */
class Counter extends EventDispatcher
{
	static get EVENT_COUNTING_STARTED()		{return "onCountingStarted"};
	static get EVENT_COUNTING_TICK()		{return "onCountingTick"};
	static get EVENT_COUNTING_INTERRUPTED()	{return "onCountingInterrupted"};
	static get EVENT_COUNTING_COMPLETED()	{return "onCountingCompleted"};

	/**
	 * Start value counting.
	 * @param {number} aValue_num - Target value.
	 * @param {number} aTime_num - Counting duration in milliseconds.
	 * @param {Function} aEase_fnc - Easing function.
	 * @param {Function} aOnComplete_fnc - Counting complete handler.
	 * @param {Function} aOnChange_fnc - Counting step handler.
	 */
	startCounting(aValue_num, aTime_num, aEase_fnc = null, aOnComplete_fnc = null, aOnChange_fnc = null)
	{
		this._startCounting(aValue_num, aTime_num, aEase_fnc, aOnComplete_fnc, aOnChange_fnc);
	}

	/**
	 * Update target counting value and adjust current counter value to fit new target value.
	 * @param {number} aValue_num
	 */
	updateCounting(aValue_num)
	{
		this._updateCounting(aValue_num);
	}

	/**
	 * Interrupt counting and reset values.
	 */
	stopCounting()
	{
		this._stopCounting();
	}

	/**
	 * Force completion of counting and reset values.
	 */
	finishCounting()
	{
		this._finishCounting();
	}
	
	/**
	 * Checks if value is counting now.
	 * @returns {boolean}
	 */
	inProgress()
	{
		return this._fInProgress;
	}

	/**
	 * Update target counting value.
	 * @param {number} aValue_num
	 */
	set endValue(aValue_num)
	{
		this._fEndValue_num = aValue_num;
	}

	/**
	 * Get target counting value.
	 * @type {number}
	 */
	get endValue()
	{
		return this._fEndValue_num;
	}

	/**
	 * Update current counting value.
	 * @param {number} aValue_num
	 */
	set progress(aValue_num)
	{
		this._setProgress(aValue_num);
	}

	/**
	 * Get current counting value.
	 * @type {number}
	 */
	get progress()
	{
		return this._fProgress_num;
	}

	/**
	 * Set rounding precision.
	 * @param {number} aValue_num - Rounding precision.
	 */
	set roundRate(aValue_num)
	{
		this._fCustomRound_num = Math.pow(10, aValue_num);
	}

	constructor(aSetter_obj, aGetter_obj, aIsRealis_bl = null)
	{
		super();

		this._fisRealis_bl = aIsRealis_bl;

		if (!aSetter_obj)
		{
			throw new Error(`Setter/Getter is absent today`);
		}
		if (!aGetter_obj)
		{
			aGetter_obj = aSetter_obj;
		}

		if (aSetter_obj.callback)
		{
			this._fSetCallback_fnc = aSetter_obj.callback;
		}
		else if (aSetter_obj.target && aSetter_obj.method)
		{
			this._fSetTarget_obj = aSetter_obj.target;
			this._fSetMethod_str = aSetter_obj.method;
		}
		else
		{
			throw new Error(`Invalid custom setter`);
		}

		if (aGetter_obj.callback)
		{
			this._fGetCallback_fnc = aGetter_obj.callback;
		}
		else if (aGetter_obj.target && aGetter_obj.method)
		{
			this._fGetTarget_obj = aGetter_obj.target;
			this._fGetMethod_str = aGetter_obj.method;
		}
		else
		{
			throw new Error(`Invalid custom getter`);
		}
	}

	_startCounting(aValue_num, aTime_num, aEase_fnc = null, aOnComplete_fnc = null, aOnChange_fnc = null)
	{
		this._clear();

		if (aTime_num > 0)
		{
			this._fStartValue_num = +this._getValue();
			this._fEndValue_num = aValue_num;
			this._fOnComplete_fnc = aOnComplete_fnc;
			this._fProgress_num = 0;
			this._fInProgress = true;
			this._updateRoundIfRequired();

			this._fTween_tw = new Tween(this, "progress", 0, 1, aTime_num, aEase_fnc);
			this._fTween_tw.on(Tween.EVENT_ON_FINISHED, this._onCountingCompleted, this);
			if (aOnChange_fnc)
			{
				this._fTween_tw.on(Tween.EVENT_ON_CHANGE, aOnChange_fnc);
			}
			this._fTween_tw.play();
			this.emit(Counter.EVENT_COUNTING_STARTED);
		}
		else
		{
			this._setValue(aValue_num);
			aOnComplete_fnc && aOnComplete_fnc();
			this.emit(Counter.EVENT_COUNTING_COMPLETED);
		}
	}

	_updateCounting(aValue_num)
	{
		if (this._fInProgress)
		{
			let lDifference_num = this._fEndValue_num - aValue_num;
			this._fStartValue_num += lDifference_num;
			this._fEndValue_num += lDifference_num;
		}
	}

	_stopCounting()
	{
		if (this._fInProgress)
		{
			this._clear();
			this.emit(Counter.EVENT_COUNTING_INTERRUPTED);
		}
	}

	_finishCounting()
	{
		if (this._fTween_tw && this._fInProgress)
		{
			this._fTween_tw.finish();
			this._clear();
		}
	}

	_onCountingCompleted()
	{
		let l_fnc = this._fOnComplete_fnc;
		this._clear();
		l_fnc && l_fnc();
		this.emit(Counter.EVENT_COUNTING_COMPLETED);
	}

	_updateRoundIfRequired()
	{
		if (!this._fCustomRound_num)
		{
			let lDecimalsCount_int = NumberValueFormat.decimalPlaces(this._getValue());
			if (this._fEndValue_num)
			{
				lDecimalsCount_int = Math.min(NumberValueFormat.decimalPlaces(this._fEndValue_num), lDecimalsCount_int);
			}
			this._fRound_num = Math.pow(10, lDecimalsCount_int);
		}
	}

	_setProgress(aValue_num)
	{
		let lTargetValue_num = this._fStartValue_num + aValue_num * (this._fEndValue_num - this._fStartValue_num);
		let lRound_num = this._fCustomRound_num || this._fRound_num;
		if (lRound_num !== undefined && !this._fisRealis_bl)
		{
			lTargetValue_num = Math.floor(lTargetValue_num * lRound_num) / lRound_num;
		}
		this._setValue(lTargetValue_num);
		this._fProgress_num = aValue_num;
	}

	_setValue(aValue_num)
	{
		if (this._fSetCallback_fnc)
		{
			this._fSetCallback_fnc(aValue_num);
		}
		else if (this._fSetTarget_obj && this._fSetMethod_str)
		{
			this._fSetTarget_obj[this._fSetMethod_str] = aValue_num;;
		}
	}

	_getValue()
	{
		if (this._fGetCallback_fnc)
		{
			return this._fGetCallback_fnc();
		}
		else if (this._fGetTarget_obj && this._fGetMethod_str)
		{
			return this._fGetTarget_obj[this._fGetMethod_str];
		}
	}

	_clear()
	{
		this._fTween_tw && this._fTween_tw.destructor();
		this._fTween_tw = null;

		this._fInProgress = false;
		this._fProgress_num = undefined;
		this._fStartValue_num = undefined;
		this._fEndValue_num = undefined;
		this._fOnComplete_fnc = null;
	}

	/**
	 * Destroy counter instance.
	 */
	destroy()
	{
		this._fSetCallback_fnc = null;
		this._fSetTarget_obj = null;
		this._fSetMethod_str = undefined;

		this._fGetCallback_fnc = null;
		this._fGetTarget_obj = null;
		this._fGetMethod_str = undefined;

		this._fRound_num = undefined;
		this._fCustomRound_num = undefined;

		this._fisRealis_bl = null;

		this._clear();
	}
}

export default Counter;