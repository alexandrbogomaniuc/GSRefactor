import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

class TimeBlock extends Sprite
{
	static get BLOCK_WIDTH()
	{
		return 75;
	}

	set time(aVal_num)
	{
		this._fTimeFromServer_num = aVal_num;

		this._updateTimeView(true);
	}

	get time()
	{
		return this._fTimeFromServer_num;
	}
	
	constructor(aTimeFromServer_num, aTimerFrequency_num, aOptTimerOffset_num)
	{
		super();

		this._fContentContainer_sprt = null;
		this._fTimeFromServer_num = aTimeFromServer_num;
		this._fTimerFrequency_num = aTimerFrequency_num * 1000;
		this._fTimeOffset_num = aOptTimerOffset_num !== undefined ? aOptTimerOffset_num : -(new Date()).getTimezoneOffset();
		this._fTimeOddTick_bln = false;

		this._fTimeViewHours_tf = null;
		this._fTimeViewColon_tf = null;
		this._fTimeViewMinutes_tf = null;

		this._fIntervalTimer_tmr = null;

		this._init();
	}

	_init()
	{
		// DEBUG...
		// this.addChild(new PIXI.Graphics).beginFill(0x00ffff).drawRect(-TimeBlock.BLOCK_WIDTH/2, -10, TimeBlock.BLOCK_WIDTH, 20).endFill();
		// this.addChild(new PIXI.Graphics).beginFill(0xff0000).drawRect(-1, -7, 2, 14).endFill();
		// ...DEBUG

		let lContainer_sprt = this._fContentContainer_sprt = this.addChild(new Sprite);

		let lTime_str = this._getTime(true);

		this._fTimeViewHours_tf = new TextField(this._timeTextFormat);
		this._fTimeViewHours_tf.anchor.set(0.5, 0.5);
		this._fTimeViewHours_tf.position.set(-11, 0);
		this._fTimeViewHours_tf.text = lTime_str.substring(0,2);
		lContainer_sprt.addChild(this._fTimeViewHours_tf);

		this._fTimeViewColon_tf = new TextField(this._timeTextFormat);
		this._fTimeViewColon_tf.anchor.set(0.5, 0.5);
		this._fTimeViewColon_tf.text = ":";
		this._fTimeViewColon_tf.position.set(0, -1);
		lContainer_sprt.addChild(this._fTimeViewColon_tf);

		this._fTimeViewMinutes_tf = new TextField(this._timeTextFormat);
		this._fTimeViewMinutes_tf.anchor.set(0.5, 0.5);
		this._fTimeViewMinutes_tf.position.set(11, 0);
		this._fTimeViewMinutes_tf.text = lTime_str.substring(3, 5);
		lContainer_sprt.addChild(this._fTimeViewMinutes_tf);

		this._fIntervalTimer_tmr = new Timer(this._onIntervalTimeout.bind(this), 1000, true);
		this._fIntervalTimer_tmr.start();

		if (APP.isMobile)
		{
			this._fTimeViewHours_tf.position.x = -13;
			this._fTimeViewMinutes_tf.position.x = 13;
		}
		else
		{
			let lTimeIcon_sprt = lContainer_sprt.addChild(APP.library.getSprite("common_time_icon"));
			lTimeIcon_sprt.position.set(-30, 0);
		}

		this._alignContent();
	}

	_onIntervalTimeout()
	{
		this._updateTimeView(false);
	}

	_updateTimeView(aForced_bl=false)
	{
		let lTime_str = this._getTime(aForced_bl);

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

	_getTime(aForced_bl = false)
	{
		if (!aForced_bl)
		{
			this._fTimeOddTick_bln = !this._fTimeOddTick_bln;
		}

		let date = new Date();
		if (this._fTimeFromServer_num !== undefined)
		{
			date.setTime(this._fTimeFromServer_num);
		}
		date.setTime(date.getTime() + this._fTimeOffset_num * 60000);

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

	get _timeTextFormat()
	{
		return {
			fontFamily: "fnt_nm_barlow",
			fontSize: APP.isMobile ? 18 : 14,
			align: "left",
			fill: 0xffffff
		};
	}

	_alignContent()
	{
		let lContainer_sprt = this._fContentContainer_sprt;
		let lContainerBounds_r = lContainer_sprt.getBounds();
		let lGlobalX_num = lContainerBounds_r.x;
		let lLocalX_num = this.globalToLocal(lGlobalX_num, 0).x;

		lContainer_sprt.x = -lLocalX_num - lContainerBounds_r.width/2;
	}

	destroy()
	{
		if (this._fIntervalTimer_tmr) this._fIntervalTimer_tmr.destructor();

		super.destroy();

		this._fContentContainer_sprt = null;
		this._fTimeViewHours_tf = null;
		this._fTimeViewColon_tf = null;
		this._fTimeViewMinutes_tf = null;

		this._fIntervalTimer_tmr = null;

		this._fTimeFromServer_num = null;
		this._fTimeOddTick_bln = null;
		this._fTimeOffset_num = null;
	}
}

export default TimeBlock