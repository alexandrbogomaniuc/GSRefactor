import Sprite from '../../../../../unified/view/base/display/Sprite';
import { APP } from '../../../../../unified/controller/main/globals';
import TextField from '../../../../../unified/view/base/display/TextField';
import Timer from '../../../../../unified/controller/time/Timer';

class GUSLobbyCPanelTimeBlock extends Sprite
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

		let lHoursParams_obj = this.__hoursViewParams;
		this._fTimeViewHours_tf = new TextField(this.__timeTextFormat);
		this._fTimeViewHours_tf.anchor.set(lHoursParams_obj.anchor.x, lHoursParams_obj.anchor.y);
		this._fTimeViewHours_tf.position.set(lHoursParams_obj.position.x, lHoursParams_obj.position.y);
		this._fTimeViewHours_tf.text = lTime_str.substring(0,2);
		this.addChild(this._fTimeViewHours_tf);

		let lColonParams_obj = this.__colonViewParams;
		this._fTimeViewColon_tf = new TextField(this.__timeTextFormat);
		this._fTimeViewColon_tf.anchor.set(lColonParams_obj.anchor.x, lColonParams_obj.anchor.y);
		this._fTimeViewColon_tf.text = ":";
		this._fTimeViewColon_tf.position.set(lColonParams_obj.position.x, lColonParams_obj.position.y);
		this.addChild(this._fTimeViewColon_tf);

		let lMinutesParams_obj = this.__minutesViewParams;
		this._fTimeViewMinutes_tf = new TextField(this.__timeTextFormat);
		this._fTimeViewMinutes_tf.anchor.set(lMinutesParams_obj.anchor.x, lMinutesParams_obj.anchor.y);
		this._fTimeViewMinutes_tf.position.set(lMinutesParams_obj.position.x, lMinutesParams_obj.position.y);
		this._fTimeViewMinutes_tf.text = lTime_str.substring(3, 5);
		this.addChild(this._fTimeViewMinutes_tf);

		this._fIntervalTimer_tmr = new Timer(this._onIntervalTimeout.bind(this), 1000, true);
		this._fIntervalTimer_tmr.start();

		this._fSyncTimer_tmr = new Timer(this._onTimeSyncRequest.bind(this), this._fTimerFrequency_num, true);
		this._fSyncTimer_tmr.start();

		if (!APP.isMobile)
		{
			let lIconParams_obj = this.__iconViewParams;
			let lTimeIcon_sprt = this.addChild(APP.library.getSprite(this.__iconAssetName));
			lTimeIcon_sprt.position.set(lIconParams_obj.position.x, lIconParams_obj.position.y);

			this._addSeptum();
		}
	}

	get __hoursViewParams()
	{
		return { anchor: {x: 0.5, y: 0.46}, position: {x: (APP.isMobile ? -10 : -8), y: 0} }
	}

	get __minutesViewParams()
	{
		return { anchor: {x: 0.5, y: 0.46}, position: {x: (APP.isMobile ? 10 : 8), y: 0} }
	}

	get __colonViewParams()
	{
		return { anchor: {x: 0.5, y: 0.46}, position: {x: 0, y: -1} }
	}

	get __iconViewParams()
	{
		return { position: {x: -28, y: 0} }
	}

	get __iconAssetName()
	{
		// must be overridden
		return undefined;
	}

	get __septumViewParams()
	{
		return { position: {x: -45, y: 0}, width: 1, height: 18, color: 0x4e4e4e }
	}

	_addSeptum()
	{
		let lSeptumViewParams_obj = this.__septumViewParams;
		this._fSeptum_grphc = this.addChild(new PIXI.Graphics());
		this._fSeptum_grphc.beginFill(lSeptumViewParams_obj.color).drawRect(-lSeptumViewParams_obj.width, -lSeptumViewParams_obj.height/2, lSeptumViewParams_obj.width, lSeptumViewParams_obj.height).endFill();
		this._fSeptum_grphc.position.set(lSeptumViewParams_obj.position.x, lSeptumViewParams_obj.position.y);
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
		this.emit(GUSLobbyCPanelTimeBlock.EVENT_TIME_SYNC_REQUEST);
	}

	get __timeTextFormat()
	{
		return {
			fontFamily: "fnt_nm_cmn_barlow",
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

export default GUSLobbyCPanelTimeBlock