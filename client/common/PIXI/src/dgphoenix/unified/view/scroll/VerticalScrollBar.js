import ScrollBar from "./ScrollBar";
import VerticalSlider from '../slider/VerticalSlider';

/**
 * Vertical scrollbar.
 * @class
 * @augments ScrollBar
 * @inheritdoc
 */
class VerticalScrollBar extends ScrollBar
{

	constructor()
	{
		super();
	}

	_updateSlider()
	{
		let stepsCount = 0;
		if (this.visibleArea)
		{
			stepsCount = Math.max(0, this.scrollableContainer.measuredHeight - this.visibleArea.height);
			if (stepsCount == 0)
			{
				this.__onDraggebleContainerPulled();
			}
		}
		this.slider.update(stepsCount, true);

		this._updateSliderScale();
	}

	_updateSliderScale()
	{
		let lScale_num = this.scrollableContainer.measuredHeight ? (this.visibleArea.height / this.scrollableContainer.measuredHeight) : 0;
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
				stepsCount = this.scrollableContainer.measuredHeight - this.visibleArea.height;
			}
			else
			{
				stepsCount = 0;
			}
		}
		this._fSlider_s = new VerticalSlider(backAsset, trackAsset, btnAsset, stepsCount, buttonIndent, holdInterval, aOptDiscreteTrackScale_bl);
		this._fSlider_s.on("trackUpdated", this._updateListPosition, this);

		this._updateSliderScale();
	}

	__getDragParameter()
	{
		return "y";
	}

	_scrollContainer(aValue_num)
	{
		if (this.visibleArea && this.scrollableContainer.measuredHeight < this.visibleArea.height)
			return;

		let lNewY_num = this.scrollableContainer.y + aValue_num;
		let lVisibleAreaHeight_num = this.visibleArea ? this.visibleArea.height : this.scrollableContainer.measuredHeight;
		if (lNewY_num + this.scrollableContainer.measuredHeight - lVisibleAreaHeight_num < 0 || lNewY_num > 0)
			return;
		this.scrollableContainer.moveTo(-lNewY_num);
	}

	_scrollContainerTo(aValue_num)
	{
		this.scrollableContainer.moveTo(aValue_num);
		this.slider.moveTo(aValue_num); 
	}
}

export default VerticalScrollBar