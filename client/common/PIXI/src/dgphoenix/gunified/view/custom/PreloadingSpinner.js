import Spinner from '../../../unified/view/custom/Spinner';

const RECT_COUNT = 8;

class PreloadingSpinner extends Spinner
{
	constructor(duration = 3800, delay = 100) 
	{
		super(duration, delay);
	}

	createView() 
	{
		for (var i = 0; i < RECT_COUNT; ++i)
		{
			this.container.addChild(this.getRect(i));
		}
	}

	getRect(index)
	{
		var rect = null;
		if (!this.sourceRect)
		{
			this.sourceRect = new PIXI.Graphics();
			this.sourceRect.beginFill(0xEEDDB9, 1);
			this.sourceRect.drawRoundedRect(-2, 10.5, 4, 14, 1.5);
			this.sourceRect.endFill();
			rect = this.sourceRect;
		}
		else
		{
			rect = this.sourceRect.clone();
		}

		rect.rotation = Math.PI * (index/4);

		return rect;
	}
}

export default PreloadingSpinner;