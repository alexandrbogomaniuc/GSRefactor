import SliderButton from './SliderButton';
import SliderTrack from './SliderTrack';
import { APP } from '../../controller/main/globals';
import Sprite from '../base/display/Sprite';

/**
 * Base slider view for scrollbar
 * @class
 * @extends Sprite
 */
class BaseSlider extends Sprite
{
	static get EVENT_ON_SCROLL_MULTIPLIER_UPDATED() {return "EVENT_ON_SCROLL_MULTIPLIER_UPDATED";}

	/**
	 * @constructor
	 * @param {PIXI.Graphics|Sprite} backAsset - Slider background asset.
	 * @param {PIXI.Graphics|String} trackAsset - Track graphics view or bitmap asset name.
	 * @param {String} btnAsset - Slider arrow button bitmap asset name.
	 * @param {number} stepsCount - Amount of steps enough to scroll to the end of scrollable container.
	 * @param {number} [buttonIndent=2] - Indent for arrow button.
	 * @param {boolean} isVertical - Is vertical slider or not.
	 * @param {number|null} holdInterval - Time interval (in milliseconds) for slider steps while arrow button is pressed.
	 * @param {boolean} aOptDiscreteTrackScale_bl - Is descrete track scale mode (track size is equal to the step size) or not.
	 */
	constructor(backAsset, trackAsset, btnAsset, stepsCount = 20, buttonIndent = 2, isVertical, holdInterval, aOptDiscreteTrackScale_bl = false)
	{
		super();

		this.backAsset = backAsset;
		this.trackAsset = trackAsset;
		this.btnAsset = btnAsset;
		this.isVertical = isVertical;

		this.stepsCount = stepsCount;

		this.buttonIndent = buttonIndent;

		this.trackEdges = null;
		this.scrollStep = null;
		this.trackSpace = 0;
		this.freeSpace = 0;	
		this._fScrollMultiplier_num = 1;

		this._fIsDiscreteTrackScale_bl = aOptDiscreteTrackScale_bl;

		this.addComponents(isVertical, holdInterval);
	}

	/**
	 * Set track scale.
	 * @param {number} aScale_num 
	 */
	setTrackScale(aScale_num)
	{
		let lScale_num = 1;
		if (this.isVertical)
		{
			lScale_num = (this.trackSpace / this.track.getOrigHeight()) * aScale_num;
		}
		else
		{
			lScale_num = (this.trackSpace / this.track.getOrigWidth()) * aScale_num;
		}

		this.track.setScale(lScale_num);
		this.__calculateRelativeSizes();
		this.calculateStepSizes();
		this.track.setEdges(this.trackEdges);
	}

	/**
	 * Set steps multiplier.
	 * @param {number} aValue_num
	 */
	set scrollMultiplier(aValue_num)
	{
		let lPrevValue_num = this._fScrollMultiplier_num;
		this._fScrollMultiplier_num = aValue_num;

		if (lPrevValue_num !== this._fScrollMultiplier_num)
		{
			this.emit(BaseSlider.EVENT_ON_SCROLL_MULTIPLIER_UPDATED);
		}
	}

	/**
	 * Steps multiplier (one slider movement can include several steps).
	 */
	get scrollMultiplier()
	{
		return this._fScrollMultiplier_num;
	}

	/**
	 * Update slider view.
	 * @param {PIXI.Graphics|Sprite} backAsset - Slider background asset.
	 * @param {PIXI.Graphics|String} trackAsset - Track graphics view or bitmap asset name.
	 */
	updateView(backAsset, trackAsset)
	{
		if (backAsset && this.backAsset instanceof PIXI.Graphics)
		{
			this.backAsset = backAsset;
			this.back.getChildAt(0).destroy();

			this.back.addChildAt(this.backAsset, 0);
		}
		
		this.trackAsset = trackAsset;
		this.track.updateView(trackAsset);
		this._fScrollMultiplier_num = 1;

		this.__updateButtonsPositions();
		this.__calculateRelativeSizes();

		this.track.setEdges(this.trackEdges);
	}

	/**
	 * @param {boolean} isVertical 
	 * @param {number} holdInterval 
	 * @private
	 */
	addComponents(isVertical, holdInterval)
	{
		if (this.backAsset instanceof PIXI.Graphics)
		{
			this.back = this.addChild(new Sprite());
			this.back.addChild(this.backAsset);
		}
		else
		{
			this.back = this.addChild(APP.library.getSprite(this.backAsset));
		}

		this.track = this.back.addChild(new SliderTrack(this.trackAsset, isVertical));
		this.track.on("positionUpdated", this.handleTrackMove, this);
		this.track.on("trackReleased", this.onTrackReleased, this);
		this.track.on("trackCkick", this.onTrackClick, this);

		this._updateDiscreteTrackScale();

		if (this.btnAsset)
		{
			this.bottomBtn = this.addChild(new SliderButton(this.btnAsset, holdInterval));
			this.topBtn = this.addChild(new SliderButton(this.btnAsset, holdInterval));
		}

		this.__updateButtonsPositions();
		this.__calculateRelativeSizes();

		this.track.setEdges(this.trackEdges);
		this.moveTop();

		this.bottomBtn && this.bottomBtn.on("action", this.onTrackDown, this);
		this.topBtn && this.topBtn.on("action", this.onTrackUp, this);

		this.back.on("pointerdown", this.__onPointerDown, this);
		this.back.on("pointerupoutside", this.onPointerUp, this);
	}

	_updateDiscreteTrackScale()
	{
		if (this._fIsDiscreteTrackScale_bl)
		{
			let lScale_num = 1;
			if (this.stepsCount < 1)
			{
				lScale_num = 0;
			}
			else if (this.isVertical)
			{
				lScale_num = (this.back.getLocalBounds().height * this.back.scale.y / this.stepsCount) / this.track.getOrigHeight();
			}
			else
			{
				lScale_num = (this.back.getLocalBounds().width * this.back.scale.x / this.stepsCount) / this.track.getOrigWidth();
			}
			this.track.setScale(lScale_num);
		}
	}

	__updateButtonsPositions()
	{
	}

	__onPointerDown(e)
	{
	}

	__calculateRelativeSizes()
	{
		this.calculateStepSizes();
	}

	onPointerUp(e)
	{
		this.track.handleUp(e);
	}

	calculateStepSizes(){
		if (this.stepsCount < 1)
		{
			this.scrollStep = 0;
			this.visible = false;
		}
		else
		{
			this.scrollStep = this.freeSpace / this.stepsCount;
			this.visible = true;
		}
	}

	handleTrackMove(data)
	{
		let val = this.scrollStep !== 0 ? Math.round((data.pos + this.trackEdges.bottom) / this.scrollStep) : 0;
		this.emit("trackUpdated", {value: val});
	}

	onTrackReleased()
	{
		let val = Math.round((this.track.sliderPosition + this.trackEdges.bottom) / this.scrollStep);
		let normalizedPos = val*this.scrollStep - this.trackEdges.bottom;
		this.track.moveTrack(normalizedPos, false, true);
	}

	onTrackClick()
	{
		this.emit("trackCkick");
	}

	onTrackDown()
	{
		this.track.moveTrack(this.track.sliderPosition + this.scrollStep * this.scrollMultiplier);
	}

	onTrackUp()
	{
		this.track.moveTrack(this.track.sliderPosition - this.scrollStep * this.scrollMultiplier);
	}

	onTrackDownStep(value)
	{
		this.track.moveTrack(this.track.sliderPosition + this.scrollStep * value);
	}

	onTrackUpStep(value)
	{
		this.track.moveTrack(this.track.sliderPosition - this.scrollStep * value);
	}
	
	/** Move slider forward. */
	moveUp(needEmit = false)
	{
		this.track.moveTrack(this.track.sliderPosition - this.scrollStep * this.scrollMultiplier, needEmit);
	}

	/** Move slider back. */
	moveDown(needEmit = false)
	{
		this.track.moveTrack(this.track.sliderPosition + this.scrollStep * this.scrollMultiplier, needEmit);
	}

	/** Move slider to top position. */
	moveTop(needEmit = false)
	{
		this.track.moveTrack(this.trackEdges.top, needEmit);
	}

	/** Move slider to bottom position. */
	moveBottom(needEmit = false)
	{
		this.track.moveTrack(this.trackEdges.bottom, needEmit);
	}

	/**
	 * Move slider to target position.
	 * @param {number} pos - Target slider position.
	 * @param {boolean} [needEmit=false] - Should SliderTrack#positionUpdated event be emitted or not.
	 * @param {boolean} [savePercent=false] - Should current slider position (in percents) be kept or not.
	 */
	moveTo(pos, needEmit = false, savePercent=false)
	{
		this.track.moveTrack(this.trackEdges.top + pos*this.scrollStep, needEmit, false, savePercent);		
	}

	/**
	 * Update slider steps amount.
	 * @param {number} stepsCount - Amount of steps enough to scroll to the end of scrollable container.
	 */
	update(stepsCount, savePercent=false)
	{
		if (stepsCount == this.stepsCount) return;

		let oldScrollStep = this.scrollStep;
		this.stepsCount = stepsCount;
		this.calculateStepSizes();
		this._updateDiscreteTrackScale();

		let oldRelativePosition = (this.track.sliderPosition + this.trackEdges.bottom) / (oldScrollStep == 0 ? 1 : oldScrollStep);
		let newPosition = (oldRelativePosition * this.scrollStep) - this.trackEdges.bottom;

		this.track.moveTrack(newPosition, true, false, false);
	}
}

export default BaseSlider