import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import * as FEATURES from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/features'

class TimeBlock extends Sprite
{
	static get EVENT_TIME_SYNC_REQUEST()			{ return "onTimeSyncRequest"; }

	hideSeptum()
	{
		if (!this._fSeptum_grphc) return;
		this._fSeptum_grphc.visible = false;
	}

	showSeptum()
	{
		if (!this._fSeptum_grphc) return;
		this._fSeptum_grphc.visible = true;
	}

	sync()
	{
		this._fSyncTimer_tmr.start();
	}

	set time(aVal_num)
	{
		this._fTimeFromServer_num = aVal_num;
	}

	get time()
	{
		return this._fTimeFromServer_num;
	}

	constructor(aTimeFromServer_num, aTimerFrequency_num)
	{
		super();

		this._fTimeFromServer_num = aTimeFromServer_num;
		this._fTimerFrequency_num = aTimerFrequency_num * 1000;
		this._fTimeOddTick_bln = false;

		this._fTimeViewHours_tf = null;
		this._fTimeViewColon_tf = null;
		this._fTimeViewMinutes_tf = null;

		this._fIntervalTimer_tmr = null;
		this._fSyncTimer_tmr = null;

		this._fSeptum_grphc = null;

		this._init();
	}

	_init()
	{
		let lTime_str = this._getTime();

		this._fTimeViewHours_tf = new TextField(this._timeTextFormat);
		this._fTimeViewHours_tf.anchor.set(0.5, 0.46);
		this._fTimeViewHours_tf.position.set(-8, 0);
		this._fTimeViewHours_tf.text = lTime_str.substring(0, 2);
		this.addChild(this._fTimeViewHours_tf);

		this._fTimeViewColon_tf = new TextField(this._timeTextFormat);
		this._fTimeViewColon_tf.anchor.set(0.5, 0.46);
		this._fTimeViewColon_tf.text = ":";
		this._fTimeViewColon_tf.position.set(0, -1);
		this.addChild(this._fTimeViewColon_tf);

		this._fTimeViewMinutes_tf = new TextField(this._timeTextFormat);
		this._fTimeViewMinutes_tf.anchor.set(0.5, 0.46);
		this._fTimeViewMinutes_tf.position.set(8, 0);
		this._fTimeViewMinutes_tf.text = lTime_str.substring(3, 5);
		this.addChild(this._fTimeViewMinutes_tf);

		if (FEATURES.IE)
		{
			this._fTimeViewHours_tf.y = this._fTimeViewMinutes_tf.y = 2;
			this._fTimeViewColon_tf.y = 1;
		}

		this._fIntervalTimer_tmr = new Timer(this._onIntervalTimeout.bind(this), 1000, true);
		this._fIntervalTimer_tmr.start();

		this._fSyncTimer_tmr = new Timer(this._onTimeSyncRequest.bind(this), this._fTimerFrequency_num, true);
		this._fSyncTimer_tmr.start();

		if (!APP.isMobile)
		{
			let lTimeIcon_sprt = this.addChild(APP.library.getSprite("common_time_icon"));
			lTimeIcon_sprt.position.set(-28, 0);

			this._addSeptum();
		}
		else
		{
			this._fTimeViewHours_tf.position.x = -10;
			this._fTimeViewMinutes_tf.position.x = 10;
		}
	}

	_addSeptum()
	{
		this._fSeptum_grphc = this.addChild(new PIXI.Graphics());
		this._fSeptum_grphc.beginFill(0x4e4e4e).drawRect(-1, -9, 1, 18).endFill();
		this._fSeptum_grphc.position.set(-45, 0);
	}

	_onIntervalTimeout()
	{
		let lTime_str = this._getTime(true);

		this._fTimeViewHours_tf.text = lTime_str.substring(0,2);

		if (lTime_str.indexOf(":") != -1)
		{
			this._fTimeViewColon_tf.visible = true;
		}
		else
		{
			this._fTimeViewColon_tf.visible = false;
		}

		this._fTimeViewMinutes_tf.text = lTime_str.substring(3, 5);
	}

	_getTime(aOnTick_bl = false)
	{
		if (aOnTick_bl)
		{
			this._fTimeOddTick_bln = !this._fTimeOddTick_bln;
		}

		let date = new Date();
		if (this._fTimeFromServer_num !== undefined)
		{
			date.setTime(this._fTimeFromServer_num);
		}
		date.setTime(date.getTime() + (this._fTimeOffset_num !== undefined ? this._fTimeOffset_num : -date.getTimezoneOffset()) * 60000);

		return	this._formatTimePart(date.getUTCHours()) +
				(this._fTimeOddTick_bln ? ':' : ' ') +
				this._formatTimePart(date.getUTCMinutes());
	}

	_formatTimePart(aValue_int)
	{
		var lRet_str = String(aValue_int);
		lRet_str = lRet_str.length < 2 ? new Array(3 - lRet_str.length).join("0") + lRet_str : lRet_str;
		return lRet_str;
	}

	_onTimeSyncRequest()
	{
		this._fSyncTimer_tmr.pause();
		this.emit(TimeBlock.EVENT_TIME_SYNC_REQUEST);
	}

	get _timeTextFormat()
	{
		return {
			fontFamily: "fnt_nm_barlow",
			fontSize: APP.isMobile ? 13 : 10.5,
			align: "left",
			fill: 0xffffff
		};
	}

	destroy()
	{
		if (this._fSyncTimer_tmr) this._fSyncTimer_tmr.destructor();
		if (this._fIntervalTimer_tmr) this._fIntervalTimer_tmr.destructor();

		super.destroy();

		this._fTimeViewHours_tf = null;
		this._fTimeViewColon_tf = null;
		this._fTimeViewMinutes_tf = null;

		this._fSyncTimer_tmr = null;
		this._fIntervalTimer_tmr = null;

		this._fTimeFromServer_num = null;
		this._fTimeOddTick_bln = null;
		this._fTimeOffset_num = null;

		this._fSeptum_grphc = null;
	}
}

export default TimeBlock