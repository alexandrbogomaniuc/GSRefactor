import { APP } from '../../../../unified/controller/main/globals';
import I18 from '../../../../unified/controller/translations/I18';
import Sprite from '../../../../unified/view/base/display/Sprite';
import Sequence from '../../../../unified/controller/animation/Sequence';
import * as Easing from '../../../../unified/model/display/animation/easing';
import Timer from '../../../../unified/controller/time/Timer';

class GUSTooltipView extends Sprite
{
	static get EVENT_ON_TIP_SHOWN() { return "onTipShown"; }
	static get EVENT_ON_TIP_HIDED() { return "onTipHided"; }

	get id()
	{
		return this._fId_num;
	}

	showTip()
	{
		this._showTip();
	}

	hideTip()
	{
		this._hideTip();
	}

	//INIT...
	constructor(aCaptionAsset_str, aTipId_num, aArrowDir_str, aTipDurationMs_num)
	{
		super();

		this._fId_num = aTipId_num;
		this._fArrowDir_str = aArrowDir_str;
		this._fCaptionAsset_str = aCaptionAsset_str;
		this._fBack_spr = null;
		this._fArrow_sprt = null;
		this._fTipDurationMs_num = aTipDurationMs_num;
		this._fShowTimer_tmr = null;

		this._init();
	}

	_init()
	{
		this._createCaption();
		this._addBack();
		this._addArrow();
		this._addCaption();

		this.interactive = true;
		this.buttonMode = false;
	}

	_createCaption()
	{
		this._fCaption_ta = I18.generateNewCTranslatableAsset(this._fCaptionAsset_str);
	}

	_addBack()
	{
	}

	_addArrow()
	{
	}

	_addCaption()
	{
	}

	_showTip()
	{
		this.visible = true;
		this.scale.set(0);

		let visible = [{
			tweens: [{ prop: "scale.x", to: 1 }, { prop: "scale.y", to: 1 }],
			duration: 300,
			ease: Easing.back.easeOut,
			onfinish: () =>
			{
				this._fShowTimer_tmr = new Timer(() => { this._onShowTimerCompleted(); }, this._fTipDurationMs_num);
			}
		}];

		Sequence.start(this, visible);

		this.emit(GUSTooltipView.EVENT_ON_TIP_SHOWN, { id: this._fId_num });
	}

	_onShowTimerCompleted()
	{
		this._hideTip();
	}

	_hideTip()
	{
		this._fShowTimer_tmr && this._fShowTimer_tmr.destructor();
		this._fShowTimer_tmr = null;

		let hide = [{
			tweens: [{ prop: "scale.x", to: 0 }, { prop: "scale.y", to: 0 }],
			duration: 100,
			ease: Easing.linear.easeIn,
			onfinish: () =>
			{
				this.visible = false;

				this.emit(GUSTooltipView.EVENT_ON_TIP_HIDED, { id: this._fId_num });
			}
		}];

		Sequence.start(this, hide);
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));

		this._fId_num = null;
		this._fArrowDir_str = null;
		this._fCaptionAsset_str = null;
		this._fBack_spr = null;
		this._fArrow_sprt = null;

		this._fShowTimer_tmr && this._fShowTimer_tmr.destructor();
		this._fShowTimer_tmr = null;

		super.destroy();
	}
}

export default GUSTooltipView;