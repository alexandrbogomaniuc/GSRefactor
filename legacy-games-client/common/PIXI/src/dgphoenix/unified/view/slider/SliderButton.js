import Button from '../ui/Button';
import { APP } from '../../controller/main/globals';

/**
 * Arrow button of slider. Triggers slider movement.
 */
class SliderButton extends Button 
{
	/**
	 * @constructor
	 * @param {string} baseAssetName - Asset name for button view.
	 * @param {number} holdInterval - Time interval (in milliseconds) for click action while button is pressed.
	 */
	constructor(baseAssetName, holdInterval = 150) 
	{
		super(baseAssetName, null, true, true);

		this.holded = false;
		this.holdStep = holdInterval;
		this.holdIntervalCounter = this.holdStep;
	}

	handleClick()
	{
		return;
	}

	handleDown(e)
	{
		super.handleDown(e);
		e && e.stopPropagation();

		if (this.holdStep !== null){
			this.holded = true;
			APP.ticker.on("tick", this.handleHold, this)
		}
		this.emit("action");
	}

	handleUp(e)
	{
		super.handleUp(e);

		if (this.holdStep !== null){
			APP.ticker.off("tick", this.handleHold, this);
			this.holded = false;
			this.holdIntervalCounter = this.holdStep;
		}
	}

	handleOut()
	{
		super.handleOut();
		this.handleUp();
	}

	handleHold(e)
	{
		this.holdIntervalCounter -= e.delta;

		if (this.holdIntervalCounter <= 0)
		{
			this.holdIntervalCounter = this.holdStep;
			this.emit("action");
		}
	}
}

export default SliderButton;