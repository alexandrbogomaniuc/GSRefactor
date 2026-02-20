import { APP } from '../../../../../unified/controller/main/globals';
import Sprite from '../../../../../unified/view/base/display/Sprite';
import * as Easing from '../../../../../unified/model/display/animation/easing';
import Sequence from '../../../../../unified/controller/animation/Sequence';
import Timer from "../../../../../unified/controller/time/Timer";

class GUSPreloaderTooltip extends Sprite
{	
	static get END_OF_HIDE_ANIMATION() 	{return "endHideAnimation";}

	show()
	{
		this._showTip();
	}

	get id()
	{
		return this._fTooltipID;
	}

	constructor(aTooltipID)
	{
		super();

		this._fTooltipID = aTooltipID;

		this.visible = false;
		this.alpha = 0;

		this.__init();

		this._addContent();
	}

	__init()
	{
	}

	_addContent()
	{
	}

	_showTip()
	{
		this.visible = true;

		let show = [{
			tweens: [{ prop: "alpha", to: 1 }],
			duration: 800,
			ease: Easing.linear.easeOut,
			onfinish: () =>
			{
				this._fIntervalTimer_tmr = new Timer(this._hideTip.bind(this), 2000, true);
				this._fIntervalTimer_tmr.start();
			}
		}];

		Sequence.start(this, show);
	}

	_hideTip()
	{
		if (this._fIntervalTimer_tmr) this._fIntervalTimer_tmr.destructor();

		let hide = [{
			tweens: [{ prop: "alpha", to: 0 }],
			duration: 800,
			ease: Easing.linear.easeOut,
			onfinish: () =>
			{
				this.visible = false;
				this.emit(GUSPreloaderTooltip.END_OF_HIDE_ANIMATION);
			}
		}];

		Sequence.start(this, hide);
	}

	destroy() 
	{
		Sequence.destroy(Sequence.findByTarget(this));
		
		super.destroy();

		if (this._fIntervalTimer_tmr) this._fIntervalTimer_tmr.destructor();
		this._fTooltipID = null;
	}
}

export default GUSPreloaderTooltip;