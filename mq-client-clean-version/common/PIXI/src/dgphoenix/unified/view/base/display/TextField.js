import { APP } from '../../../controller/main/globals';

export const DEFAULT_SYSTEM_FONT = 'sans-serif'

const __BaseClass = PIXI.Text;

/**
 * Text field.
 * @class
 * @augments PIXI.Text
 */
class TextField extends __BaseClass
{

	/**
	 * Replace placeholder with string value.
	 */
	substitutePlaceholder(aPlaceholder_str, aValue)
	{
		let lFinalText_str = this.text.replace(aPlaceholder_str, aValue + "");
		this.text = lFinalText_str;
		this._refresh();
	}

	/**
	 * Refresh text view.
	 */
	refresh()
	{
		this._refresh();
	}

	constructor(textFormat, aOptFixBlinkin_bln = true)
	{
		super('');

		let lPlatformInfo_obj = window.getPlatformInfo ? window.getPlatformInfo() : {};
		this._fIsMobile_bln = lPlatformInfo_obj.mobile;
		this._fExtraScale_num = this._fIsMobile_bln ? 0.5 : 1;
		this._fFixBlinkin_bln = aOptFixBlinkin_bln;

		this.textFormat = textFormat;
		this.scale.set(this._fExtraScale_num);

		this._cached = false;
		this.resolution = APP.layout.bitmapScale;
		this._fMaxWidth_num = 0;
		this._fMaxHeight_num = 0;

		this._checkFontWeight();
	}

	_checkFontWeight()
	{
		if ( (this.style.fontWeight !== 'normal') && APP.isDebugMode )
		{
			console.warn
				(
					" Font: '" + this.style.fontFamily 
					+ "' fontWeight: '" + this.style.fontWeight 
					+ "'\nText has the fontWeight parameter set in translation_descriptor ('bold') for CTranslatableAsset or elsewhere for TextField. \nThere may be problems displaying the font in different browsers."
				)
		}
	}

	/**
	 * Text style.
	 * @returns {Object}
	 */
	getStyle()
	{
		let lStyle_obj = {
			align: this.style.align,
			breakWords: this.style.breakWords,
			dropShadow: this.style.dropShadow,
			dropShadowAlpha: this.style.dropShadowAlpha,
			dropShadowAngle: this.style.dropShadowAngle,
			dropShadowBlur: this.style.dropShadowBlur,
			dropShadowColor: this.style.dropShadowColor,
			dropShadowDistance: this.style.dropShadowDistance * this._fExtraScale_num,
			fill: this.style.fill,
			fillGradientStops: this.style.fillGradientStops,
			fillGradientType: this.style.fillGradientType,
			fontFamily: this.style.fontFamily,
			fontSize: this.style.fontSize * this._fExtraScale_num,
			fontStyle: this.style.fontStyle,
			fontVariant: this.style.fontVariant,
			fontWeight: this.style.fontWeight,
			leading: this.style.leading,
			letterSpacing: this.style.letterSpacing * this._fExtraScale_num,
			lineHeight: this.style.lineHeight * this._fExtraScale_num,
			lineJoin: this.style.lineJoin,
			miterLimit: this.style.miterLimit,
			padding: this.style.padding * this._fExtraScale_num,
			stroke: this.style.stroke,
			strokeThickness: this.style.strokeThickness * this._fExtraScale_num,
			textBaseline: this.style.textBaseline,
			trim: this.style.trim,
			whiteSpace: this.style.whiteSpace,
			wordWrap: this.style.wordWrap,
			wordWrapWidth: this.style.wordWrapWidth
		};

		return lStyle_obj;
	}

	/**
	 * Is mobile text field.
	 */
	get isMobile()
	{
		return this._fIsMobile_bln;
	}

	/**
	 * Base scale on mobile devices.
	 */
	get mobileScale()
	{
		return this._fExtraScale_num;
	}

	/**
	 * Set max text width.
	 * @param {number} aValue_num
	 */
	set maxWidth(aValue_num)
	{
		if (aValue_num === undefined || aValue_num < 0)
		{
			return;
		}

		this._fMaxWidth_num = aValue_num;
		this._applyMaxBounds();
	}

	/**
	 * Max text width.
	 */
	get maxWidth()
	{
		return this._fMaxWidth_num;
	}

	/**
	 * Set max text height.
	 * @param {number} aValue_num
	 */
	set maxHeight(aValue_num)
	{
		if (aValue_num === undefined || aValue_num < 0)
		{
			return;
		}

		this._fMaxHeight_num = aValue_num;
		this._applyMaxBounds();
	}

	/**
	 * Set text string.
	 * @param {string} aValue_str
	 */
	set text(aValue_str)
	{
		if (this._fShortLength_num !== undefined)
		{
			if(this._useMiddleTrim === true)
			{
				aValue_str = this._createShortenedFormMidleTrim(aValue_str);
			}else{
				aValue_str = this._createShortenedForm(aValue_str);
			}
			
		}

		super.text = aValue_str;

		this._applyMaxBounds();
	}

	/**
	 * Text string.
	 * @type {string}
	 */
	get text()
	{
		// don't remove getter override
		return super.text;
	}

	/**
	 * Max text height.
	 * @type {number}
	 */
	get maxHeight()
	{
		return this._fMaxHeight_num;
	}

	/**
	 * Increase font size for mobile devices.
	 * @param {number} amount - Additional font size.
	 */
	increaseFontSizeByIfMobile(amount)
	{
		if (this._fIsMobile_bln)
		{
			let lNewFontSize_num = this.style.fontSize * this._fExtraScale_num + amount;
			this.textFormat = {
				fontSize: lNewFontSize_num
			};
		}
	}

	/**
	 * Cache view (reject redraw).
	 */
	cache()
	{
		this._cached = true;
	}

	/**
	 * Uncache view (allow redraw).
	 */
	uncache()
	{
		this._cached = false;
	}

	/**
	 * Destroy instance.
	 */
	destroy()
	{
		this._fIsMobile_bln = undefined;
		this._fExtraScale_num = undefined;

		this.textFormat = null;
		
		this._cached = undefined;
		
		this._fMaxWidth_num = undefined;
		this._fMaxHeight_num = undefined;
		this._fFixBlinkin_bln = undefined;
		this._useMiddleTrim = undefined;

		super.destroy();
	}

	/** Text bounds. */
	get textBounds()
	{
		let lLocalBounds_obj = this.getLocalBounds();
		let lBounds_obj = {x: lLocalBounds_obj.x, y: lLocalBounds_obj.y, width: lLocalBounds_obj.width, height: lLocalBounds_obj.height};

		if (this._fIsMobile_bln)
		{
			lBounds_obj.width = lBounds_obj.width * this._fExtraScale_num;
			lBounds_obj.height = lBounds_obj.height * this._fExtraScale_num;
			lBounds_obj.x = lBounds_obj.x * this._fExtraScale_num;
			lBounds_obj.y = lBounds_obj.y * this._fExtraScale_num;
		}
		return lBounds_obj;
	}

	/**
	 * Update text format.
	 */
	set textFormat(aFormat_obj)
	{
		if (aFormat_obj)
		{
			if (this._fIsMobile_bln)
			{
				aFormat_obj.fontSize && (aFormat_obj.fontSize /= this._fExtraScale_num);
				aFormat_obj.strokeThickness && (aFormat_obj.strokeThickness /= this._fExtraScale_num);
				aFormat_obj.dropShadowDistance && (aFormat_obj.dropShadowDistance /= this._fExtraScale_num);
				aFormat_obj.lineHeight && (aFormat_obj.lineHeight /= this._fExtraScale_num);
				aFormat_obj.letterSpacing && (aFormat_obj.letterSpacing /= this._fExtraScale_num);
				aFormat_obj.padding && (aFormat_obj.padding /= this._fExtraScale_num);
			}
			Object.assign(this.style, aFormat_obj);
			if (this._fIsMobile_bln)
			{
				aFormat_obj.fontSize && (aFormat_obj.fontSize *= this._fExtraScale_num);
				aFormat_obj.strokeThickness && (aFormat_obj.strokeThickness *= this._fExtraScale_num);
				aFormat_obj.dropShadowDistance && (aFormat_obj.dropShadowDistance *= this._fExtraScale_num);
				aFormat_obj.lineHeight && (aFormat_obj.lineHeight *= this._fExtraScale_num);
				aFormat_obj.letterSpacing && (aFormat_obj.letterSpacing *= this._fExtraScale_num);
				aFormat_obj.padding && (aFormat_obj.padding *= this._fExtraScale_num);
			}

			if (aFormat_obj.shortLength !== undefined)
			{
				this._fShortLength_num = APP.isMobile ? aFormat_obj.shortLength * 2 : aFormat_obj.shortLength;
				if(aFormat_obj.useMiddleTrim === true)
				{
					this._useMiddleTrim = true;
				}
			}
		}
		else
		{
			this.style = null;
		}

		this._refresh();
	}

	/**
	 * Update font color.
	 * @param {number} aColor_int 
	 */
	updateFontColor(aColor_int)
	{
		let lParams_obj = this.getStyle();
		lParams_obj.fill = aColor_int;
		
		this.textFormat = lParams_obj;
	}

	_generateFillStyle(style, lines) 
	{
		if (!Array.isArray(style.fill)) 
		{
			return style.fill;
		}

		// cocoon on canvas+ cannot generate textures, so use the first colour instead
		if (navigator.isCocoonJS) {
			return style.fill[0];
		}

		// the gradient will be evenly spaced out according to how large the array is.
		// ['#FF0000', '#00FF00', '#0000FF'] would created stops at 0.25, 0.5 and 0.75
		var gradient = void 0;
		var totalIterations = void 0;
		var currentIteration = void 0;
		var stop = void 0;

		var width = this.canvas.width / this.resolution;
		var height = this.canvas.height / this.resolution;

		// make a copy of the style settings, so we can manipulate them later
		var fill = style.fill.slice();
		var fillGradientStops = style.fillGradientStops.slice();

		// wanting to evenly distribute the fills. So an array of 4 colours should give fills of 0.25, 0.5 and 0.75
		if (!fillGradientStops.length) {
			var lengthPlus1 = fill.length + 1;

			for (var i = 1; i < lengthPlus1; ++i) {
				fillGradientStops.push(i / lengthPlus1);
			}
		}

		// stop the bleeding of the last gradient on the line above to the top gradient of the this line
		// by hard defining the first gradient colour at point 0, and last gradient colour at point 1
		fill.unshift(style.fill[0]);
		fillGradientStops.unshift(0);

		fill.push(style.fill[style.fill.length - 1]);
		fillGradientStops.push(1);

		let gradient_X0;
		let gradient_Y0;
		let gradient_X1;
		let gradient_Y1;

		if (
				style.fillGradientType === PIXI.TEXT_GRADIENT.LINEAR_VERTICAL
				|| (style.linearGradientPoints && style.linearGradientPoints[0] === style.linearGradientPoints[2])
			) 
		{
			// start the gradient at the top center of the canvas, and end at the bottom middle of the canvas
			gradient_X0 = style.linearGradientPoints ? style.linearGradientPoints[0] * this.resolution : width / 2;
			gradient_Y0 = style.linearGradientPoints && (style.linearGradientPoints[1] !== style.linearGradientPoints[3]) ? style.linearGradientPoints[1] * this.resolution : 0;
			gradient_X1 = style.linearGradientPoints ? style.linearGradientPoints[2] * this.resolution : width / 2;
			gradient_Y1 = style.linearGradientPoints && (style.linearGradientPoints[1] !== style.linearGradientPoints[3]) ? style.linearGradientPoints[3] * this.resolution : height;
			gradient = this.context.createLinearGradient(gradient_X0, gradient_Y0, gradient_X1, gradient_Y1);

			// we need to repeat the gradient so that each individual line of text has the same vertical gradient effect
			// ['#FF0000', '#00FF00', '#0000FF'] over 2 lines would create stops at 0.125, 0.25, 0.375, 0.625, 0.75, 0.875
			totalIterations = (fill.length + 1) * lines.length;
			currentIteration = 0;
			for (var _i2 = 0; _i2 < lines.length; _i2++) {
				currentIteration += 1;
				for (var j = 0; j < fill.length; j++) {
					if (typeof fillGradientStops[j] === 'number') {
						stop = fillGradientStops[j] / lines.length + _i2 / lines.length;
					} else {
						stop = currentIteration / totalIterations;
					}
					gradient.addColorStop(stop, fill[j]);
					currentIteration++;
				}
			}
		} else {
			// start the gradient at the center left of the canvas, and end at the center right of the canvas
			gradient_X0 = style.linearGradientPoints ? style.linearGradientPoints[0] * this.resolution : 0;
			gradient_Y0 = style.linearGradientPoints ? style.linearGradientPoints[1] * this.resolution : height / 2;
			gradient_X1 = style.linearGradientPoints ? style.linearGradientPoints[2] * this.resolution : width;
			gradient_Y1 = style.linearGradientPoints ? style.linearGradientPoints[3] * this.resolution : height / 2;
			gradient = this.context.createLinearGradient(gradient_X0, gradient_Y0, gradient_X1, gradient_Y1);

			// can just evenly space out the gradients in this case, as multiple lines makes no difference
			// to an even left to right gradient
			totalIterations = fill.length + 1;
			currentIteration = 1;

			for (var _i3 = 0; _i3 < fill.length; _i3++) {
				if (typeof fillGradientStops[_i3] === 'number') {
					stop = fillGradientStops[_i3];
				} else {
					stop = currentIteration / totalIterations;
				}
				gradient.addColorStop(stop, fill[_i3]);
				currentIteration++;
			}
		}

		return gradient;
	};

	/**
	 * Redraw text view.
	 * @param {boolean} respectDirty 
	 */
	updateText(respectDirty)
	{
		if (this._cached)
		{
			return;
		}

		super.updateText(respectDirty);
	}

	/**
	 * Update font size.
	 * @param {number} aFontSize_int 
	 */
	updateFontSize(aFontSize_int)
	{
		let lParams_obj = this.getStyle();
		lParams_obj.fontSize = aFontSize_int;
		
		this.textFormat = lParams_obj;
	}

	_refresh()
	{
		if (this._cached)
		{
			this.uncache();
			this.updateText();
			this.cache();
		}
	}

	_onTextureUpdate()
	{
		let newScale = {x: 0, y: 0};
		newScale.x = this._calcScaleX(this._texture.orig.width);
		newScale.y = this._calcScaleY(this._texture.orig.height);
		
		super._onTextureUpdate();

		this.scale.x = newScale.x;
		this.scale.y = newScale.y;
	}

	_applyMaxBounds()
	{
		if (!this._style || !this._text)
		{
			return;
		}

		var measured = PIXI.TextMetrics.measureText(this._text, this._style, this._style.wordWrap);
		var cWidth = Math.max(1, measured.width);
		var cHeight = Math.max(1, measured.height);

		this.scale.x = this._calcScaleX(cWidth);
		this.scale.y = this._calcScaleY(cHeight);
	}
	// middle trim 
	_createShortenedFormMidleTrim(aText_str)
	{
		let lLeftPart_str = aText_str.slice(0, aText_str.length/2);
		let lRightPart_str = aText_str.slice(aText_str.length/2);
		let lShortText_str = lLeftPart_str + lRightPart_str;

		let lMetrics_obj = PIXI.TextMetrics.measureText(lShortText_str, this._style, false);

		if (lMetrics_obj.width <= this._fShortLength_num)
		{
			return lShortText_str;
		}

		while (lMetrics_obj.width > this._fShortLength_num && lLeftPart_str.length > 1 && lRightPart_str.length > 1)
		{
			lLeftPart_str = lLeftPart_str.slice(0, -1);
			lRightPart_str = lRightPart_str.slice(1);
			lShortText_str = lLeftPart_str + '..' + lRightPart_str;
			lMetrics_obj = PIXI.TextMetrics.measureText(lShortText_str, this._style, false);
		}

		return lShortText_str;
	}
	// end trim 
	_createShortenedForm(aText_str)
	{
		let lLeftPart_str = aText_str;
		let lShortText_str = lLeftPart_str;

		let lMetrics_obj = PIXI.TextMetrics.measureText(lShortText_str, this._style, false);

		if (lMetrics_obj.width <= this._fShortLength_num)
		{
			return lShortText_str;
		}

		const tree_dots_str = "...";


		while (lMetrics_obj.width > this._fShortLength_num && lLeftPart_str.length>1)
		{
			lLeftPart_str = lLeftPart_str.slice(0, -1);
			lShortText_str = lLeftPart_str + tree_dots_str;
			lMetrics_obj = PIXI.TextMetrics.measureText(lShortText_str, this._style, false);
		}

		return lShortText_str;
	}

	_calcScaleX(aTextureWidth_num)
	{
		let scaleX = 1;
		if(this._fMaxWidth_num && (aTextureWidth_num * this._fExtraScale_num) > this._fMaxWidth_num)
		{
			scaleX = (this._fMaxWidth_num / (aTextureWidth_num * this._fExtraScale_num)) * this._fExtraScale_num;
		}
		else
		{
			scaleX = this._fExtraScale_num;
		}

		return scaleX;
	}

	_calcScaleY(aTextureHeight_num)
	{
		let scaleY = 1;
		if(this._fMaxHeight_num && (aTextureHeight_num * this._fExtraScale_num) > this._fMaxHeight_num)
		{
			scaleY = (this._fMaxHeight_num / (aTextureHeight_num * this._fExtraScale_num)) * this._fExtraScale_num;
		}
		else
		{
			scaleY = this._fExtraScale_num;
		}

		return scaleY;
	}
}

export default TextField