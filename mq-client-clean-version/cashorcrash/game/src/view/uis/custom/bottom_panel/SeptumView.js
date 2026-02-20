import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

const DEFAULT_COLOR = 0x4e4e4e;

class SeptumView extends Sprite
{
	constructor(aWidth_num, aHeight_num, aOptColor_int=undefined)
	{
		super();

		this._fWidth_num = aWidth_num;
		this._fHeight_num = aHeight_num;
		this._fColor_num = isNaN(aOptColor_int) ? DEFAULT_COLOR : aOptColor_int;

		this._fSeptum_gr = this.addChild(new PIXI.Graphics);

		this._reBuild();
	}

	set septumColor(value)
	{
		if (value === this._fColor_num)
		{
			return;
		}

		this._fColor_num = value;

		this._reBuild();
	}

	get septumColor()
	{
		return this._fColor_num;
	}

	set septumWidth(value)
	{
		if (value === this._fWidth_num)
		{
			return;
		}

		this._fWidth_num = value;

		this._reBuild();
	}

	get septumWidth()
	{
		return this._fWidth_num;
	}

	set septumHeight(value)
	{
		if (value === this._fHeight_num)
		{
			return;
		}

		this._fHeight_num = value;

		this._reBuild();
	}

	get septumHeight()
	{
		return this._fHeight_num;
	}

	_reBuild()
	{
		let l_gr = this._fSeptum_gr;
		l_gr.cacheAsBitmap = false;
		l_gr.clear();

		let lColor_int = this._fColor_num;
		let lWidth_num = this._fWidth_num;
		let lHeight_num = this._fHeight_num;

		l_gr.beginFill(lColor_int).drawRect(-lWidth_num/2, -lHeight_num/2, lWidth_num, lHeight_num).endFill();
	}
}

export default SeptumView