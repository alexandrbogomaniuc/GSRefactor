import PointerSprite from '../ui/PointerSprite';
import { APP } from '../../controller/main/globals';

const MIN_SLIDER_SCALE = 0.3;

/**
 * @typedef {Object} TrackEdges
 * @property {number} top - Start edge.
 * @property {number} bottom - End edge.
 */

/**
 * Track view of slider.
 * @class
 * @extends PointerSprite
 */
class SliderTrack extends PointerSprite 
{
	/**
	 * @constructor
	 * @param {PIXI.Graphics|String} baseAssetName - Track graphics view or bitmap asset name.
	 * @param {boolean} isVertical - Is vertical slider type or not. 
	 * @param {TrackEdges} edges - Track movement interval.
	 */
	constructor(baseAssetName, isVertical, edges) 
	{
		super(null, true);

		this._addBase(baseAssetName);
		this._updateHitArea();

		this.isVertical = isVertical;
		this.holded = false;
		this.edges = edges || {top: -10, bottom: 10};
		this._topShiftPercent = undefined;

		if (isVertical)
		{
			this.slideParameter = "y";
		}
		else
		{
			this.slideParameter = "x";
		}

		/**
		 * Distance between current track position and mouse down position.
		 * @type {number}
		 */
		this.cursorShift = 0;

		this.on("pointerdown", this.handleDown, this);
		this.on("pointerup", this.handleUp, this);
		this.on("pointerupoutside", this.handleUp, this);
		this.on("pointerclick", this.handleClick, this);

		this.on("pointermove", this.handleMove, this);

		APP.on("contextmenu", this.handleUp, this);
	}

	/**
	 * Update track view.
	 * @param {PIXI.Graphics|String} baseAssetName 
	 */
	updateView(baseAssetName)
	{
		this._baseView && this._baseView.destroy();

		this._addBase(baseAssetName);
		this.setScale(1);
		
		this.edges.top = 0;
		this.edges.bottom = 0;
		this._topShiftPercent = undefined;

		this.position.set(0, 0);
	}

	_addBase(baseAssetName)
	{
		if (baseAssetName instanceof PIXI.Graphics)
		{
			this._baseView = this.holder.addChild(baseAssetName);
			this._fBaseOrigWidth_num = baseAssetName.width;
			this._fBaseOrigHeight_num = baseAssetName.height;
		}
		else if (baseAssetName)
		{
			this._baseView = this.holder.addChild(APP.library.getSprite(baseAssetName));
		}
	}

	/**
	 * Current track position.
	 */
	get sliderPosition()
	{
		return this.position[this.slideParameter];
	}

	setEdges(edges)
	{
		this._topShiftPercent = !!this.edges ? Math.abs(this.edges.top - this.position[this.slideParameter]) / Math.abs(this.edges.top - this.edges.bottom) : undefined;
		
		let lPrevEdgeTop_num = this.edges.top;
		let lPrevEdgeBottom_num = this.edges.bottom;

		this.edges.top = edges.top;
		this.edges.bottom = edges.bottom;

		if (lPrevEdgeTop_num !== this.edges.top || lPrevEdgeBottom_num !== this.edges.bottom)
		{
			this.validatePosition(true);
		}
	}

	/**
	 * Set track view scale.
	 * @param {number} scale 
	 */
	setScale(scale)
	{
		if (this.isVertical)
		{
			this._baseView.scale.y = Math.max(MIN_SLIDER_SCALE, scale);
		}
		else
		{
			this._baseView.scale.x = Math.max(MIN_SLIDER_SCALE, scale);
		}

		this._updateHitArea();
	}

	/**
	 * Hit area height.
	 * @returns {number}
	 */
	getHeight()
	{
		return this.hitArea.height;
	}

	/**
	 * Hit area width.
	 * @returns {number}
	 */
	getWidth()
	{
		return this.hitArea.width;
	}

	/**
	 * Track view height.
	 * @returns {number}
	 */
	getOrigHeight()
	{
		if (this._baseView.texture)
		{
			return this._baseView.texture.height;
		}

		if (!isNaN(this._fBaseOrigHeight_num))
		{
			return this._fBaseOrigHeight_num;
		}

		return this._baseView.getLocalBounds().height * this._baseView.scale.y;
	}

	/**
	 * Track view width.
	 * @returns {number}
	 */
	getOrigWidth()
	{
		if (this._baseView.texture)
		{
			return this._baseView.texture.width;
		}

		if (!isNaN(this._fBaseOrigWidth_num))
		{
			return this._fBaseOrigWidth_num;
		}

		return this._baseView.getLocalBounds().width * this._baseView.scale.x;
	}

	handleDown(e, center)
	{
		e.stopPropagation && e.stopPropagation();

		this.holded = true;
		if (center)
		{
			this.removeCursorShift();
			this.moveTrack(this.parent.toLocal(e.data.global)[this.slideParameter]);
		}
		else
		{
			this.createCursorShift(this.parent.toLocal(e.data.global)[this.slideParameter]);
		}

		if(!!document.PointerEvent)
		{
			document.addEventListener("pointerup", this._onDocumentMouseUp.bind(this));
		}
		else
		{
			document.addEventListener("touchend", this._onDocumentMouseUp.bind(this));
			document.addEventListener("mouseup", this._onDocumentMouseUp.bind(this));
		}

		this.emit("trackCkick");
	}
	
	_onDocumentMouseUp(e)
	{
		if (this.holded)
		{
			this.handleUp(e);
		}
	}

	handleUp(e)
	{
		if (!this.holded) return;

		e.stopPropagation && e.stopPropagation();

		this.emit("trackReleased");

		this.holded = false;
		this.removeCursorShift();

		if(!!document.PointerEvent)
		{
			document.removeEventListener("pointerup", this._onDocumentMouseUp.bind(this));
		}
		else
		{
			document.removeEventListener("touchend", this._onDocumentMouseUp.bind(this));
			document.removeEventListener("mouseup", this._onDocumentMouseUp.bind(this));
		}
	}

	createCursorShift(cursor)
	{
		this.cursorShift = this.position[this.slideParameter] - cursor;
	}

	removeCursorShift()
	{
		this.cursorShift = 0;
	}

	handleMove(e)
	{
		if (this.holded)
		{
			this.moveTrack(this.parent.toLocal(e.data.global)[this.slideParameter])
		}
	}

	handleClick(e)
	{
		this.moveTrack(this.parent.toLocal(e.data.global)[this.slideParameter]);
	}

	/**
	 * Move track to target position.
	 * @param {number} pos - Target position.
	 * @param {boolean} [needEmit=true] - Should SliderTrack#positionUpdated event be emitted or not.
	 * @param {boolean} [noShift=false] - Should cursorShift be counted in target position or not.
	 * @param {boolean} [saveTopPosPercent_bln=false] - Should current track position (in percents) be kept or not.
	 */
	moveTrack(pos, needEmit = true, noShift = false, saveTopPosPercent_bln=false)
	{
		if (!noShift){
			pos += this.cursorShift;
		}

		if (pos < this.edges.top)
		{
			pos = this.edges.top;
		}
		else if (pos > this.edges.bottom)
		{
			pos = this.edges.bottom;
		}

		if (saveTopPosPercent_bln)
		{
			this._topShiftPercent = this._topShiftPercent || 0;
			let coef = this.edges.top < 0 ? 1 : -1;
			let percentedDistance = Math.abs(this.edges.top - this.edges.bottom)*this._topShiftPercent;
			pos = this.edges.top + coef*percentedDistance;
		}

		this.position[this.slideParameter] = pos;

		if (needEmit)
		{
			this.emit("positionUpdated", {pos: this.position[this.slideParameter]});
		}
	}

	/**
	 * Sync track view position with slider current position.
	 * @param {boolean} [saveTopPosPercent_bln=false] - Should current track position (in percents) be kept or not. 
	 */
	validatePosition(saveTopPosPercent_bln = false)
	{
		this.moveTrack(this.sliderPosition, true, false, saveTopPosPercent_bln);
	}

	/**
	 * Destroy instance.
	 */
	destroy()
	{
		APP.off("contextmenu", this.handleUp, this);
		this._topShiftPercent = undefined;

		super.destroy();
	}
}

export default SliderTrack;