import BaseSlider from './BaseSlider';

/**
 * VerticalSlider slider.
 * @class
 * @augments BaseSlider
 * @inheritdoc
 */
class VerticalSlider extends BaseSlider
{
	constructor(backAsset, trackAsset, btnAsset, stepsCount = 20, buttonIndent=2, holdInterval, aOptDiscreteTrackScale_bl = false)
	{
		super(backAsset, trackAsset, btnAsset, stepsCount, buttonIndent, true, holdInterval, aOptDiscreteTrackScale_bl);
	}

	__updateButtonsPositions()
	{
		if (!this.bottomBtn) return;

		this.bottomBtn.position.y = (this.back.getLocalBounds().height * this.back.scale.y - this.bottomBtn.getLocalBounds().height * this.bottomBtn.scale.y)/2 - this.buttonIndent;
		this.topBtn.position.y = -this.bottomBtn.position.y;
		this.topBtn.scale.y = -1;
	}

	__onPointerDown(e)
	{
		let localMouse = this.toLocal(e.data.global);
		let trackW = this.track.getWidth();
		if(localMouse.x + trackW/2 >= this.track.x && localMouse.x + trackW/2 <= this.track.x + trackW)
		{
			this.track.handleDown(e, true);
		}
		this.emit("trackPointerDown");
	}

	__calculateRelativeSizes()
	{
		let lBtnHeight_num = this.topBtn ? this.topBtn.getBounds().height : 0;
		this.trackSpace = this.back.getLocalBounds().height * this.back.scale.y - (lBtnHeight_num + this.buttonIndent)*2;
		this.freeSpace = this.trackSpace - this.track.getHeight();

		this.trackEdges = {top: -this.freeSpace/2, bottom: this.freeSpace/2};
		super.__calculateRelativeSizes();
	}
}

export default VerticalSlider;