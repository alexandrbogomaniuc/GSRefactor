import Sprite from '../base/display/Sprite';
import { APP } from '../../controller/main/globals';

/**
 * Base container to be scrolled in any direction.
 * @class
 * @augments Sprite
 * @inheritdoc
 */
class ScrollableContainer extends Sprite {

	static get EVENT_ON_CONTENT_UPDATED() {return "EVENT_ON_CONTENT_UPDATED";}
	
	constructor()
	{
		super();

		this._fInitPosition_pt = null;
	}

	/**
	 * Move container to initial position.
	 */
	resetPosition()
	{
		this.position.set(this.initPosition.x, this.initPosition.y);
	}

	/**
	 * Reset initial position.
	 */
	resetInitPosition()
	{
		this._fInitPosition_pt = null;
	}
	
	/**
	 * Move container.
	 * @param {number} aValue_num - Step length in pixels.
	 * @abstract
	 */
	moveTo(aValue_num)
	{
		throw new Error("ScrollableContainer :: moveTo >> calling abstract method");
	}

	/**
	 * Initial position of container.
	 */
	get initPosition()
	{
		if (!this._fInitPosition_pt)
		{
			this._fInitPosition_pt = {x: this.position.x, y: this.position.y};	
		}
		return this._fInitPosition_pt;
	}

	/**
	 * Current width of container.
	 * @type {number}
	 */
	get measuredWidth()
	{
		return this.getBounds().width;
	}

	/**
	 * Current height of container.
	 * @type {number}
	 */
	get measuredHeight()
	{
		return this.getBounds().height;
	}
}

export default ScrollableContainer
