import { APP } from '../../controller/main/globals';
import Sprite from '../base/display/Sprite';

/**
 * Interactive Sprite.
 * @class
 * @extends Sprite
 */
class PointerSprite extends Sprite 
{
	/**
	 * @constructor
	 * @param {PIXI.Texture|PIXI.Graphics|Sprite|String} baseAssetName - Asset name or base view.
	 * @param {boolean} [cursorPointer=false] - Use pointer cursor or not.
	 * @param {Point} [anchorPoint=null] - Custom anchor for base view.
	 */
	constructor(baseAssetName, cursorPointer = false, anchorPoint = null) 
	{
		super();

		this.holder = this.addChild(new Sprite());
		this._baseView = null;
		
		this.cursorPointer = cursorPointer;
		this.interactive = false;

		this._updateBaseView(baseAssetName);
		if (anchorPoint)
		{
			this._baseView.anchor.set(anchorPoint.x, anchorPoint.y);
		}
	}

	/** Enable interactivity. */
	setEnabled()
	{
		this.interactive = true;
		this.buttonMode = this.cursorPointer;
	}

	/** Disable interactivity. */
	setDisabled()
	{
		this.interactive = false;
		this.buttonMode = false;
	}

	/**
	 * Set custom hit area.
	 * @param {PIXI.Rectangle} aHitArea_obj 
	 */
	setHitArea(aHitArea_obj)
	{
		this._fSpecificHitArea_obj = aHitArea_obj;
		this.hitArea = this._fSpecificHitArea_obj;
	}

	/**
	 * Hit area.
	 * @returns {PIXI.Rectangle}
	 */
	getHitArea()
	{
		return this._fSpecificHitArea_obj || this.hitArea;
	}

	/**
	 * Base view.
	 * @type {Sprite}
	 */
	get baseView()
	{
		return this._baseView;
	}

	update()
	{
	}

	/**
	 * Update base view.
	 * @param {PIXI.Texture|PIXI.Graphics|Sprite|String} baseAssetName 
	 */
	updateBase(baseAssetName)
	{
		this._updateBaseView(baseAssetName);
	}

	_updateBaseView(baseAssetName)
	{
		if (!baseAssetName)
		{
			return;
		}

		let sprite;
		if (baseAssetName instanceof PIXI.Texture)
		{
			sprite = new Sprite();
			sprite.texture = baseAssetName;
		}
		else if (baseAssetName instanceof PIXI.Graphics)
		{
			sprite = this._baseView || new Sprite();
			sprite.destroyChildren();
			sprite.addChild(baseAssetName);
		}
		else if (baseAssetName instanceof Sprite)
		{
			sprite = this._baseView || new Sprite();
			sprite.destroyChildren();
			sprite.addChild(baseAssetName);
		}
		else
		{
			sprite = APP.library.getSprite(baseAssetName);
		}

		if (this._baseView)
		{
			this._baseView = sprite;
		}
		else
		{
			this._baseView = this.holder.addChild(sprite);
		}

		this._updateHitArea();
	}

	_updateHitArea()
	{
		if (this.cursorPointer)
		{
			let bounds = this.holder.getLocalBounds();
			let scaleX = this.holder.scale.x;
			let scaleY = this.holder.scale.y;
			this.hitArea = this._fSpecificHitArea_obj || new PIXI.Rectangle(-bounds.width/2 * scaleX, -bounds.height/2 * scaleY, bounds.width * scaleX, bounds.height * scaleY);
			this.buttonMode = true;
		}
	}

	/**
	 * Destroy instance.
	 */
	destroy()
	{
		this._baseView = null;
		this._fSpecificHitArea_obj = null;
		this.holder = null;

		super.destroy();
	}
}

export default PointerSprite;