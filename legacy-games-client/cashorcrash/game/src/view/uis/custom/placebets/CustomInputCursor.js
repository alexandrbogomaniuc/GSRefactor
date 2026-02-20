import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { FRAME_RATE } from '../../../../config/Constants';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';

class CustomInputCursor extends Sprite
{
	constructor(aHeight_num, aColor_int)
	{
		super();

		this._fHeight_num = aHeight_num || 10;
		this._fColor_int = aColor_int || 0xffffff;
		this._fBlink_seq = null;

		this._fViewContainer_sprt = this.addChild(new Sprite);
		this._fView_gr = this._fViewContainer_sprt.addChild(new PIXI.Graphics);

		this._updateView();
	}

	set cursorHeight(value)
	{
		this._fHeight_num = value;

		this._updateView();
	}

	get cursorHeight()
	{
		return this._fHeight_num;
	}

	set cursorColor(value)
	{
		this._fColor_int = value;

		this._updateView();
	}

	get cursorColor()
	{
		return this._fColor_int;
	}

	startBlink()
	{
		if (this._fBlink_seq)
		{
			return;
		}

		this._fViewContainer_sprt.alpha = 1;
		this._fBlink_seq = Sequence.start(this._fViewContainer_sprt, this._blinkSeq);
	}

	stopBlink()
	{
		this._fBlink_seq && this._fBlink_seq.destructor();
		this._fBlink_seq = null;

		this._fView_gr.alpha = 1;
	}

	hide()
	{
		super.hide();

		this.stopBlink();
	}

	_updateView()
	{
		this._fView_gr.cacheAsBitmap = false;
		this._fView_gr.clear();

		this._fView_gr.beginFill(this._fColor_int).drawRect(-0.5, -this._fHeight_num/2, 1, this._fHeight_num).endFill();
	}

	get _blinkSeq()
	{
		let lSeq_arr = [
			{tweens: [{prop: 'alpha', to: 0}],	duration: 1*FRAME_RATE},
			{tweens: [],						duration: 15*FRAME_RATE},
			{tweens: [{prop: 'alpha', to: 1}],	duration: 1*FRAME_RATE},
			{tweens: [], 						duration: 15*FRAME_RATE, onfinish:  () => this._onBlinkCycleCompleted()}
		];

		return lSeq_arr
	}

	_onBlinkCycleCompleted()
	{
		this._fBlink_seq && this._fBlink_seq.destructor();
		this._fBlink_seq = null;

		this.startBlink();
	}

	destroy()
	{
		this._fBlink_seq && this._fBlink_seq.destructor();
		this._fBlink_seq = null;

		this._fHeight_num = undefined;
		this._fColor_int = undefined;
		this._fView_gr = null;

		super.destroy();
	}
}

export default CustomInputCursor;