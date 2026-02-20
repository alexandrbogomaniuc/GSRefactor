import { APP } from '../../controller/main/globals';
import PointerSprite from './PointerSprite';
import I18 from '../../controller/translations/I18';
import { Filters } from '../base/display/Filters';
import Sprite from '../base/display/Sprite';
import TextField from '../base/display/TextField';
import CTranslatableAsset from '../translations/CTranslatableAsset';

/**
 * Button.
 * @class
 * @extends PointerSprite
 */
class Button extends PointerSprite
{
	static get BUTTON_TYPE_ACCEPT()	{return "buttonTypeAccept";}
	static get BUTTON_TYPE_CANCEL()	{return "buttonTypeCancel";}
	static get BUTTON_TYPE_COMMON()	{return "buttonTypeCommon";}

	static get EVENT_ON_HOLDED()	{return "EVENT_ON_HOLDED";}
	static get EVENT_ON_UNHOLDED()	{return "EVENT_ON_UNHOLDED";}	

	/**
	 * @constructor
	 * @param {PIXI.Texture|PIXI.Graphics|Sprite|String} baseAssetName - Asset name or base view.
	 * @param {string} [captionId] - Translatable asset id used for button caption.
	 * @param {boolean} [cursorPointer] - Use pointer cursor or not.
	 * @param {boolean} [noDefaultSound] - Is sound on press supported or not.
	 * @param {Point} [anchorPoint] - Custom anchor.
	 * @param {string} [buttonType] - Button type.
	 */
	constructor(baseAssetName, captionId = undefined, cursorPointer = false, noDefaultSound = false, anchorPoint = null, buttonType = Button.BUTTON_TYPE_COMMON)
	{
		super(baseAssetName, cursorPointer, anchorPoint);

		this._buttonType = buttonType;

		this.captionId = captionId;
		this.noDefaultSound = !!noDefaultSound;

		this._fHolded_bln = false;
		this._enabled = false;
		this.enabled = true;

		this._fLeftBorder_num = 0;
		this._fTopBorder_num = 0;
	}

	/**
	 * Indicates whether the button is held down or not.
	 * @type {boolean}
	 */
	get isDownHolded()
	{
		return this._fHolded_bln;
	}

	/**
	 * Translatable asset id used for button caption. 
	 * @type {string}
	 */
	get captionId()
	{
		return this._captionId; 
	}

	/**
	 * Set caption asset id.
	 */
	set captionId(value)
	{
		if (this._captionId !== value)
		{
			this._captionId = value;
			
			if (!!value)
			{
				this.updateCaptionView(I18.generateNewCTranslatableAsset(value));
			}
			else
			{
				this.updateCaptionView(null);
			}
		}
	}

	/**
	 * Text field representing caption.
	 */
	get captionTf()
	{
		if (!this.caption)
		{
			return null;
		}

		if (this.caption instanceof TextField)
		{
			return this.caption;
		}

		return this.caption.assetContent;
	}

	set captionTf(a_tf)
	{
		if (!a_tf)
		{
			return;
		}
	}

	/**
	 * Update caption view.
	 * @param {Sprite|CTranslatableAsset} aView_sprt 
	 */
	updateCaptionView(aView_sprt)
	{
		if (this.caption)
		{
			this.caption.destroy();
			this.caption = null;
		}

		if (!!aView_sprt)
		{
			this.caption = aView_sprt;
			this.holder.addChild(this.caption);

			if (aView_sprt instanceof CTranslatableAsset)
			{
				this._captionId = aView_sprt.descriptor.assetId;
			}
		}

		this._updateDisabledView();
		this._updateHitArea();
	}

	/**
	 * Set caption position.
	 * @param {number} x 
	 * @param {number} y 
	 */
	setCaptionPosition(x = 0, y = 0)
	{
		this.caption && this.caption.position.set(x, y);
	}

	/**
	 * Enable or disable button if mode changed.
	 * @param {boolean} value
	 */
	set enabled(value)
	{
		value = !!value;

		if (value === this._enabled)
		{
			return;
		}

		if (value)
		{
			this.setEnabled();
		}
		else
		{
			this.setDisabled();
		}
	}

	/**
	 * Indicates whether button is enabled or not.
	 */
	get enabled()
	{
		return this._enabled;
	}

	_updateBaseView(baseAssetName)
	{
		super._updateBaseView(baseAssetName);

		this._updateDisabledView();
		this._updateHitArea();
	}

	/**
	 * Disable button.
	 */
	setDisabled()
	{
		this._enabled = false;
		
		this._addDisabledViewIfPossible();
		
		this.handleUp();

		this.off("pointerdown", this.handleDown, this);
		this.off("pointerup", this.handleUp, this);
		this.off("pointerupoutside", this.handleUpOutside, this);
		this.off("mouseover", this.handleOver, this);
		this.off("mouseout", this.handleOut, this);

		APP.off("contextmenu", this.handleUpOutside, this);
		APP.off("lobbycontextmenu", this.handleUpOutside, this);
		APP.off("gamecontextmenu", this.handleUpOutside, this);

		super.setDisabled();
	}

	_addDisabledViewIfPossible()
	{
		let stageRenderer = this.stageRenderer;
		if (!stageRenderer)
		{
			let curTopNotAddedParent = this;
			while (!!curTopNotAddedParent.parent)
			{
				curTopNotAddedParent = curTopNotAddedParent.parent;
			}

			curTopNotAddedParent.once("added", this._onTargetAddedToScene, this);
			return;
		}

		if (!this._enabled)
		{
			this._addDisabledView();
		}		
	}

	_addDisabledView()
	{
		let lDisabledView_sprt = this.disabledView;
		this.holder.addChild(lDisabledView_sprt);

		lDisabledView_sprt.visible = true;
	}

	_updateDisabledView()
	{
		let lDisabledView_sprt = this._fDisabledView_sprt;
		if (!lDisabledView_sprt)
		{
			return;
		}

		lDisabledView_sprt.textures = [PIXI.Texture.EMPTY];
		lDisabledView_sprt.destroyChildren();
		lDisabledView_sprt.addChild( this._initDisabledView() );

		if (!!this.caption && this.holder.getChildIndex(this._fDisabledView_sprt) < this.holder.getChildIndex(this.caption))
		{
			this.holder.swapChildren(this._fDisabledView_sprt, this.caption);
		}
	}

	_onTargetAddedToScene(event)
	{
		this._addDisabledViewIfPossible();
	}

	/**
	 * Button disabled view.
	 * @type {Sprite}
	 */
	get disabledView()
	{
		return this._fDisabledView_sprt || (this._fDisabledView_sprt = this._initDisabledView());
	}

	_initDisabledView()
	{
		if (this.captionTf && this.captionTf.text !== undefined && this.captionTf.text.length > 0)
		{
			this.captionTf.visible = true;
		}

		this._calcBorders();
		
		let appStageRenderer = APP.stage.renderer;
		
		switch (appStageRenderer.type)
		{
			case PIXI.RENDERER_TYPE.WEBGL:
				return this._initWebGLDisabledView();
				break;
			case PIXI.RENDERER_TYPE.CANVAS:
				return this._initCanvasDisabledView();
				break;
		}
		throw new Error (`${rendererType} is unknown renderer type`);
		
	}

	_initCanvasDisabledView()
	{
		if (APP.stage.isWebglContextLost)
		{
			return new Sprite;
		}

		let appStageRenderer = APP.stage.renderer;
		var l_rt = this.holder.getBounds();
		var lOrigX_num = this.holder.position.x;
		var lOrigY_num = this.holder.position.y;
		this.holder.position.set(l_rt.width/2,l_rt.height/2);
		
		var l_txtr = PIXI.RenderTexture.create({ width: l_rt.width, height: l_rt.height, scaleMode: PIXI.SCALE_MODES.LINEAR, resolution: 3 });
		appStageRenderer.render(this.holder, { renderTexture: l_txtr });
		let lSource_sprt = new Sprite();
		lSource_sprt.textures = [l_txtr];

		let lCanvas_cnvs = appStageRenderer.plugins.extract.canvas(lSource_sprt);
		l_txtr.destroy(true);

		let lPixelData_obj = Filters.getPixelsFromCanvas(lCanvas_cnvs);
		var idata = Filters.grayscale(lPixelData_obj);

		let c = Filters.getCanvas(idata.width, idata.height);
		var ctx = c.getContext('2d');
		ctx.putImageData(idata, 0, 0);
		let texture = PIXI.Texture.from(c, {scaleMode: PIXI.SCALE_MODES.LINEAR});
		let sprt = new Sprite();
		sprt.textures = [texture];

		this.holder.position.set(lOrigX_num, lOrigY_num);

		return sprt;
	}

	_initWebGLDisabledView()
	{
		if (APP.stage.isWebglContextLost)
		{
			return new Sprite;
		}

		var appStageRenderer = APP.stage.renderer;
		
		this.holder.filters = [this.grayFilter];

		var l_rt = this.holder.getBounds();
		var lOrigX_num = this.holder.position.x;
		var lOrigY_num = this.holder.position.y;
		l_rt.height *=1.11; //Correction to avoid cutting bottom of sprite on render
		this.holder.position.set(l_rt.width/2,l_rt.height/2);
		
		var l_txtr = PIXI.RenderTexture.create({ width: l_rt.width, height: l_rt.height, scaleMode: PIXI.SCALE_MODES.LINEAR, resolution: 3});
		appStageRenderer.render(this.holder, { renderTexture: l_txtr });
		
		let l_sprt = new Sprite();
		l_sprt.textures = [l_txtr];
		
		this.holder.filters = null;
		this.holder.position.set(lOrigX_num, lOrigY_num);
		
		if (this.stageRenderer.type == PIXI.RENDERER_TYPE.CANVAS)
		{
			let lCanvas_cnvs = appStageRenderer.plugins.extract.canvas(l_sprt);
			let texture = PIXI.Texture.from(lCanvas_cnvs, {scaleMode: PIXI.SCALE_MODES.LINEAR});

			l_sprt.textures = [texture];

			l_txtr.destroy(true);
		}
		
		return l_sprt;
	}

	/**
	 * Button grey filter.
	 * @type {PIXI.Filter}
	 */
	get grayFilter()
	{
		return this._fGrayFilter_f || (this._fGrayFilter_f = this._initGrayFilter());
	}

	_initGrayFilter()
	{
		let colorMatrix = [
				//R  G  B  A
				1, 0, 0, 0,
				0, 1, 0, 0,
				0, 0, 1, 0,
				0, 0, 0, 1
			];
		let filter = new PIXI.filters.ColorMatrixFilter();
		filter.matrix = colorMatrix;
		filter.resolution = 4;
		filter.greyscale(0.2, false);
		return filter;
	}

	_calcBorders()
	{
		let leftBorder = 0;
		let topBorder = 0;
		
		if (!this.holder || !this.holder.children)
		{
			return;
		}

		for (let i=0; i<this.holder.children.length; i++)
		{
			let lCurChld = this.holder.children[i];
			let lCurChldLocBnds = lCurChld.getLocalBounds();
			let lCurChldLeftPos = lCurChld.x + lCurChldLocBnds.x*lCurChld.scale.x;
			let lCurChldTopPos = lCurChld.y + lCurChldLocBnds.y*lCurChld.scale.y;

			if (lCurChld.visible)
			{
				if (lCurChldLeftPos < leftBorder)
				{
					leftBorder = lCurChldLeftPos;
				}

				if (lCurChldTopPos < topBorder)
				{
					topBorder = lCurChldTopPos;
				}				
			}
		}

		this._fLeftBorder_num = leftBorder;
		this._fTopBorder_num = topBorder;
	}

	/**
	 * Enable button.
	 */
	setEnabled()
	{
		if (this._fDisabledView_sprt)
		{
			this._fDisabledView_sprt.visible = false;
		}

		this._enabled = true;
		this.holder.filters = null;

		this.on("pointerdown", this.handleDown, this);
		this.on("pointerup", this.handleUp, this);
		this.on("pointerupoutside", this.handleUpOutside, this);
		this.on("mouseover", this.handleOver, this);
		this.on("mouseout", this.handleOut, this);
		this.on("mouseupoutside", this.handleUpOutside, this);
		APP.on("contextmenu", this.handleUpOutside, this);
		APP.on("lobbycontextmenu", this.handleUpOutside, this);
		APP.on("gamecontextmenu", this.handleUpOutside, this);
		
		super.setEnabled();
	}

	handleDown(e) 
	{
		this._tryPlaySound();
		this.holder.scale.set(0.95);
		this._fHolded_bln = true;

		this.emit(Button.EVENT_ON_HOLDED);
	}

	_tryPlaySound() //Should be overriden
	{
		return;
	}

	handleUpOutside(e) 
	{
		this.handleUp(e);
	}

	handleUp(e) 
	{
		if (this.parent)
		{
			this.holder.scale.set(1);
		}
		this._fHolded_bln = false;

		this.emit(Button.EVENT_ON_UNHOLDED);
	}

	handleOver(e) 
	{
	}

	handleOut(e) 
	{
	}

	/**
	 * Destroy instance.
	 */
	destroy()
	{
		if (this.holder)
		{
			this.holder.filters = null;
		}
		
		this._fGrayFilter_f = null;
		this._fHolded_bln = null;

		APP.off("contextmenu", this.handleUpOutside, this);
		super.destroy();
	}

	applyRoundedCornersHitArea()
	{
		if(this._fSpecificHitArea_obj)
		{
			return;
		}

		let bounds = this.holder.getLocalBounds();
		let lScaleFactor_num = 1.01;
		let scaleX = this.holder.scale.x * lScaleFactor_num;
		let scaleY = this.holder.scale.y * lScaleFactor_num;

		let lX_num = -bounds.width/2 * scaleX;
		let lY_num = -bounds.height/2 * scaleY;
		let lWidth_num = bounds.width * scaleX;
		let lHeight_num = bounds.height * scaleY;

		let lRadius_num = bounds.height * scaleY * 0.5;
		let lStep_int = 15;


		//POLYGON...
		let lPoints_pt_arr = [];

		//LEFT CIRCLE HALF...
		let lCenterX_num = lX_num + lRadius_num;
		let lCenterY_num = lY_num + lRadius_num;

		for( let i = 0; i < 180 / lStep_int; i++ )
		{
			let lAngle_num = Math.PI/180 * i * lStep_int + Math.PI / 2;

			let lPoint_pt = new PIXI.Point(
				lCenterX_num + lRadius_num * Math.cos(lAngle_num),
				lCenterY_num + lRadius_num * Math.sin(lAngle_num));

			lPoints_pt_arr.push(lPoint_pt);
		}

		lPoints_pt_arr.push(new PIXI.Point(lCenterX_num, lCenterY_num - lRadius_num));
		//...LEFT CIRCLE HALF


		//RIGHT CIRCLE HALF...
		lCenterX_num = lX_num + lWidth_num - lRadius_num;
		lCenterY_num = lY_num + lRadius_num;

		for( let i = 0; i < 180 / lStep_int; i++ )
		{
			let lAngle_num = Math.PI/180 * i * lStep_int - Math.PI / 2;

			let lPoint_pt = new PIXI.Point(
				lCenterX_num + lRadius_num * Math.cos(lAngle_num),
				lCenterY_num + lRadius_num * Math.sin(lAngle_num));

			lPoints_pt_arr.push(lPoint_pt);
		}

		lPoints_pt_arr.push(new PIXI.Point(lCenterX_num, lCenterY_num + lRadius_num));
		//...RIGHT CIRCLE HALF

		let lPolygon_p = new PIXI.Polygon(lPoints_pt_arr);

		this._fSpecificHitArea_obj = lPolygon_p
		//...POLYGON

		this._updateHitArea();

		/*
		//DEBUG...
		this.hitZoneDebugVisibleShape = this.addChild(new PIXI.Graphics());
		this.hitZoneDebugVisibleShape.clear()
			.beginFill(0xFF00FF, 0.75)
			.drawPolygon(lPolygon_p);
		this.hitZoneDebugVisibleShape.zIndex = 300000;

		this.hitZoneDebugVisibleShape.endFill();
		//...DEBUG
		*/
		
	}

	//override
	_updateHitArea()
	{
		if (this.cursorPointer)
		{
			let bounds = this.holder.getLocalBounds();
			//WHEN CLICKED ON THE EDGE, SHOULD PROCEED IN SPITE OF DOWN SCALE...
			let scaleX = 1;
			let scaleY = 1;
			//...WHEN CLICKED ON THE EDGE, SHOULD PROCEED IN SPITE OF DOWN SCALE
			this.hitArea = this._fSpecificHitArea_obj || new PIXI.Rectangle(-bounds.width/2 * scaleX, -bounds.height/2 * scaleY, bounds.width * scaleX, bounds.height * scaleY);
			this.buttonMode = true;
		}
	}
}

export default Button;