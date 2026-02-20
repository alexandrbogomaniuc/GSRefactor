import ScrollBar from "./ScrollBar";
import HorizontalSlider from '../slider/HorizontalSlider';

/**
 * Horizontall scrollbar.
 * @class
 * @augments ScrollBar
 * @inheritdoc
 */
class HorizontalScrollBar extends ScrollBar {

	constructor() {
		super();
	}

	_updateSlider()
	{
		let stepsCount = 0;
		if (this.visibleArea)
		{
			stepsCount = Math.max(0, this.scrollableContainer.measuredWidth - this.visibleArea.width);
		}
		this.slider.update(stepsCount);	

		this._updateSliderScale();	
	}

	_updateSliderScale()
	{
		let lScale_num = this.scrollableContainer.measuredWidth ? (this.visibleArea.width / this.scrollableContainer.measuredWidth) : 0;
		if (lScale_num > 1) lScale_num = 0; // To prevent enlargement
		this.slider.track.setScale(1); // hotfix: calculation based on this.back.getLocalBounds() was wrong, because this.back contains track element, thus it counts last track size when calculate freeSpace
		this.slider.setTrackScale(lScale_num);		
	}

	//override
	__initSlider(backAsset, trackAsset, btnAsset, stepsCount, buttonIndent=2, holdInterval, aOptDiscreteTrackScale_bl = false)
	{
		if (!stepsCount)
		{
			if (this.visibleArea)
			{
				stepsCount = this.scrollableContainer.measuredWidth - this.visibleArea.width;
			}
			else
			{
				stepsCount = 0;
			}
		}
		this._fSlider_s = new HorizontalSlider(backAsset, trackAsset, btnAsset, stepsCount, buttonIndent, holdInterval, aOptDiscreteTrackScale_bl);
		this._fSlider_s.on("trackUpdated", this._updateListPosition, this);

		this._updateSliderScale();
	}
	
	__getDragParameter()
	{
		return "x";
	}

	_scrollContainer(aValue_num)
	{
		if (this.visibleArea && this.scrollableContainer.measuredWidth < this.visibleArea.width)
			return;

		let lNewX_num = this.scrollableContainer.x + aValue_num;
		let lVisibleAreaWidth_num = this.visibleArea ? this.visibleArea.width : this.scrollableContainer.measuredWidth;
		if (lNewX_num + this.scrollableContainer.measuredWidth - lVisibleAreaWidth_num < 0 || lNewX_num > 0)
			return;
		this.scrollableContainer.moveTo(-lNewX_num);
	}
}

export default HorizontalScrollBar