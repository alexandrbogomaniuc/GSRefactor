import BaseSlider from './BaseSlider';

/**
 * Horizontall slider.
 * @class
 * @augments BaseSlider
 * @inheritdoc
 */
class HorizontalSlider extends BaseSlider
{
	constructor(backAsset, trackAsset, btnAsset, stepsCount = 20, buttonIndent = 2, holdInterval, aOptDiscreteTrackScale_bl = false)
	{
		super(backAsset, trackAsset, btnAsset, stepsCount, buttonIndent, false, holdInterval, aOptDiscreteTrackScale_bl);
	}

	__updateButtonsPositions()
	{
		if (!this.bottomBtn) return;

		this.bottomBtn.position.x = (this.back.getLocalBounds().width * this.back.scale.x - this.bottomBtn.getLocalBounds().width * this.bottomBtn.scale.x)/2 - this.buttonIndent;
		this.topBtn.position.x = -this.bottomBtn.position.x;
		this.topBtn.scale.x = -1;
	}

	__onPointerDown(e)
	{
		let localMouse = this.toLocal(e.data.global);
		let trackH = this.track.getHeight();
		if(localMouse.y + trackH/2 >= this.track.y && localMouse.y + trackH/2 <= this.track.y + trackH)
		{
			this.track.handleDown(e, true);
		}
	}

	__calculateRelativeSizes()
	{
		let lBtnWidth_num = this.topBtn ? this.topBtn.getLocalBounds().width * this.topBtn.scale.x : 0;

		this.trackSpace = this.back.getLocalBounds().width * this.back.scale.x - (lBtnWidth_num + this.buttonIndent)*2;
		this.freeSpace = this.trackSpace - this.track.getWidth();

		this.trackEdges = {top: -this.freeSpace/2, bottom: this.freeSpace/2};
		super.__calculateRelativeSizes();
	}
}

export default HorizontalSlider;