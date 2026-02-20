import Sprite from './Sprite';
import AlignDescriptor from '../../../model/display/align/AlignDescriptor';
import TextField from './TextField';


const DEFAULT_FONT_NAME = 'sans-serif';
const DEFAULT_FONT_SIZE = 12;
const DEFAULT_FONT_COLOR = 0xffffff;

const TEMPLATE_CHAR = "0";
const DEFAULT_THIN_CHARS = ":,.";

/**
 * Text field with fixed equal space for each symbol.
 * Can be defined thin symbols for that will be set shorter width (ex.: "." or ",").
 * Helpful to avoid wobbling of text on updates, but .
 * But it is assumed wide free space next to thin symbols (ex. "1").
 * @class
 */
class NonWobblingTextField extends Sprite
{
	constructor()
	{
		super();

		this._fContentContainer_sprt = this.addChild(new Sprite);

		this._fFontName_str = DEFAULT_FONT_NAME;
		this._fFontSize_num = DEFAULT_FONT_SIZE;
		this._fFontColor_num = DEFAULT_FONT_COLOR;
		this._fShadowDescriptor_shd = null;

		this._fMaxWidth_num = undefined;
		this._fHorizontalAlign_str = AlignDescriptor.LEFT;
		this._fVerticalAlign_str = AlignDescriptor.TOP;
		this._fLetterSpace_num = 0;

		this._fTextValue_str = "";
		this._fTextFields_tf_arr = [];
		this._fTextLocalWidth_num = 0;

		this._fTemplate_tf = new TextField();
		this._fTemplate_tf.text = TEMPLATE_CHAR;
		this._fTemplateChar_str = TEMPLATE_CHAR;
		this._fPotentialTemplateChars_str = undefined;

		this._fThinChars_str = DEFAULT_THIN_CHARS;
	}

	/**
	 * Actual text width.
	 * @type {number}
	 */
	get textWidth()
	{
		return this._fTextLocalWidth_num * this._fContentContainer_sprt.scale.x;
	}

	/**
	 * Update font name.
	 * @param {string} - Font name.
	 */
	set fontName (aValue_str)
	{
		if (aValue_str === this._fFontName_str)
		{
			return;
		}

		this._fFontName_str = aValue_str;

		if (this.isPotentialTemplateCharsDefined)
		{
			this.updateTemplateChar(this._fPotentialTemplateChars_str);
		}

		this._updateTextView();
	}

	/**
	 * Font name.
	 * @type {string}
	 */
	get fontName()
	{
		return this._fFontName_str;
	}

	/**
	 * Update font size.
	 * @param {string} aValue_num
	 */
	set fontSize (aValue_num)
	{
		if (aValue_num === this._fFontSize_num)
		{
			return;
		}

		this._fFontSize_num = aValue_num;

		if (this.isPotentialTemplateCharsDefined)
		{
			this.updateTemplateChar(this._fPotentialTemplateChars_str);
		}

		this._updateTextView();
	}

	/**
	 * Font size.
	 * @type {string}
	 */
	get fontSize()
	{
		return this._fFontSize_num;
	}

	/**
	 * Update font color.
	 * @param {number} aValue_int
	 */
	set fontColor (aValue_int)
	{
		if (aValue_int === this._fFontColor_num)
		{
			return;
		}

		this._fFontColor_num = aValue_int;

		if (this.isPotentialTemplateCharsDefined)
		{
			this.updateTemplateChar(this._fPotentialTemplateChars_str);
		}

		this._updateTextView();
	}

	/**
	 * Font color.
	 * @type {number}
	 */
	get fontColor()
	{
		return this._fFontColor_num;
	}

	/**
	 * Set text align.
	 * @param {string} aHorizontalAlign_str 
	 * @param {string} aVerticalAlign_str 
	 */
	setAlign(aHorizontalAlign_str=AlignDescriptor.LEFT, aVerticalAlign_str=AlignDescriptor.TOP)
	{
		this._fHorizontalAlign_str = (""+aHorizontalAlign_str) || AlignDescriptor.LEFT;
		this._fVerticalAlign_str = (""+aVerticalAlign_str) || AlignDescriptor.TOP;

		this._alignContent();
	}

	/**
	 * Vertical align type.
	 * @type {string}
	 */
	get verticalAlign()
	{
		return this._fVerticalAlign_str;
	}

	/**
	 * Horizontal align type.
	 * @type {string}
	 */
	get horizontalAlign()
	{
		return this._fHorizontalAlign_str;
	}

	/**
	 * Update max text width.
	 * @param {number}
	 */
	set maxWidth(value)
	{
		this._fMaxWidth_num = +value || undefined;

		this._writeIntoBounds();
		this._alignContent();
	}

	/**
	 * Max text width.
	 * @type {number}
	 */
	get maxWidth()
	{
		return this._fMaxWidth_num;
	}

	/**
	 * Set letter space width.
	 * @param {number}
	 */
	set letterSpace (aValue_num)
	{
		if (aValue_num === this._fLetterSpace_num)
		{
			return;
		}

		this._fLetterSpace_num = +aValue_num || 0;

		if (this.isPotentialTemplateCharsDefined)
		{
			this.updateTemplateChar(this._fPotentialTemplateChars_str);
		}

		this._updateTextView();
	}

	/**
	 * Letter space width.
	 */
	get letterSpace()
	{
		return this._fLetterSpace_num;
	}

	/**
	 * Update text.
	 * @param {string} value
	 */
	set text(value="")
	{
		if (value === this._fTextValue_str)
		{
			return;
		}

		this._fTextValue_str = value;
		
		this._updateTextView();
	}

	/**
	 * Text width.
	 * @type {number}
	 */
	get text()
	{
		return this._fTextValue_str;
	}

	/**
	 * Define thin chars.
	 * @param {string} value
	 */
	set thinChars(value="")
	{
		if (value === this._fThinChars_str)
		{
			return;
		}

		this._fThinChars_str = ""+value || DEFAULT_THIN_CHARS;

		if (this.isPotentialTemplateCharsDefined)
		{
			this.updateTemplateChar(this._fPotentialTemplateChars_str);
		}

		this._updateTextView();
	}

	/**
	 * Thin chars.
	 * @type {string}
	 */
	get thinChars()
	{
		return this._fThinChars_str;
	}

	/**
	 * Set shadow descriptor.
	 * @param {TextShadowDescriptor} value
	 */
	set shadowDescriptor(value)
	{
		if (value === this._fShadowDescriptor_shd)
		{
			return;
		}

		this._fShadowDescriptor_shd = value;

		if (this.isPotentialTemplateCharsDefined)
		{
			this.updateTemplateChar(this._fPotentialTemplateChars_str);
		}

		this._updateTextView();
	}

	/**
	 * Shadow descriptor.
	 * @type {TextShadowDescriptor}
	 */
	get shadowDescriptor()
	{
		return this._fShadowDescriptor_shd;
	}

	/**
	 * Template symbol. Used to get symbols width
	 * @type {string}
	 */
	get templateChar()
	{
		return this._fTemplateChar_str;
	}

	/**
	 * Update template symbol.
	 * @param {string[]} aPossibleChars_str 
	 */
	updateTemplateChar(aPossibleChars_str=TEMPLATE_CHAR)
	{
		let lCurTemplateChar_str = this._fTemplateChar_str;

		if (aPossibleChars_str === lCurTemplateChar_str)
		{
			return;
		}

		this._fPotentialTemplateChars_str = aPossibleChars_str;

		let lTxt_str = aPossibleChars_str;
		let lNewTemplateChar_str = lCurTemplateChar_str;
	
		let lTemplate_tf = this._fTemplate_tf;
		lTemplate_tf.text = lCurTemplateChar_str;
		
		let lCurTemplateCharWidth_num = lTemplate_tf.textBounds.width;

		for (let i=0;  i<lTxt_str.length; i++)
		{
			let lCurPossibleChar_str = lTxt_str[i];
			lTemplate_tf.text = lCurPossibleChar_str;

			if (lTemplate_tf.textBounds.width > lCurTemplateCharWidth_num)
			{
				lNewTemplateChar_str = lCurPossibleChar_str;
			}
		}

		lTemplate_tf.text = this._fTemplateChar_str = lNewTemplateChar_str;

		if (lNewTemplateChar_str !== lCurTemplateChar_str)
		{
			return;
		}

		this._updateTextView();
	}

	/**
	 * Indicates whether potential template symbols are defined or not.
	 */
	get isPotentialTemplateCharsDefined()
	{
		return this._fPotentialTemplateChars_str !== undefined;
	}

	_updateTextView()
	{
		this._clear();

		let lTxt_str = this._fTextValue_str;
		let lX_num = 0;
		let lTextWidth_num = 0;
		for (let i=0; i<lTxt_str.length; i++)
		{
			let lCurChar_str = lTxt_str[i];

			let lTemplate_tf = this._fTemplate_tf;
			lTemplate_tf.text = this._isThinChar(lCurChar_str) ? this._thinTemplateChar : this.templateChar;
			this._applyTextStyle(lTemplate_tf);

			let lChar_tf = this._getTextField(i);
			lChar_tf.text = lCurChar_str;
			lChar_tf.visible = true;

			this._applyTextStyle(lChar_tf);

			let lTemplateCharWidth_num = lTemplate_tf.textBounds.width;
			let lCurCharWidth_num = lChar_tf.textBounds.width;

			lChar_tf.x = lX_num + this.letterSpace*i + (lTemplateCharWidth_num - lCurCharWidth_num)/2;
			lX_num += lTemplateCharWidth_num;

			lTextWidth_num += lTemplateCharWidth_num;
		}

		this._fTextLocalWidth_num = lTextWidth_num;

		this._writeIntoBounds();
		this._alignContent();
	}

	_isThinChar(aChar_str)
	{
		return this._fThinChars_str.indexOf(aChar_str) >= 0;
	}

	get _thinTemplateChar()
	{
		return this._fThinChars_str[0];
	}

	_applyTextStyle(aTarget_tf)
	{
		let charStyle = aTarget_tf.getStyle();
		charStyle.fontFamily = this.fontName;
		charStyle.fontSize = this.fontSize;
		charStyle.fill = this.fontColor;

		let lShadowDescr_shd = this.shadowDescriptor;
		if (!!lShadowDescr_shd)
		{
			charStyle.dropShadow = true;
			charStyle.dropShadowColor = lShadowDescr_shd.color.toCSSString(true);
			charStyle.dropShadowAlpha = lShadowDescr_shd.color.normalizedAlpha;
			charStyle.dropShadowBlur = lShadowDescr_shd.blurRadius;
			charStyle.dropShadowAngle = lShadowDescr_shd.shadowAngle*Math.PI/180;
			charStyle.dropShadowDistance = lShadowDescr_shd.shadowDistance;
			if (lShadowDescr_shd.isShadowAlphaDefined)
			{
				charStyle.dropShadowAlpha = lShadowDescr_shd.shadowAlpha;
			}
		}

		aTarget_tf.textFormat = charStyle;
	}

	_alignContent()
	{
		let l_sprt = this._fContentContainer_sprt;
		let lContentBounds_r = l_sprt.getLocalBounds();
		let lX_num = 0;
		let lY_num = 0;
		let lScaleX_num = l_sprt.scale.x;
		let lScaleY_num = l_sprt.scale.y;

		switch (this._fHorizontalAlign_str)
		{
			case AlignDescriptor.LEFT:
				lX_num = 0;
				break;
			case AlignDescriptor.CENTER:
				lX_num = -this._fTextLocalWidth_num/2;
				break;
			case AlignDescriptor.RIGHT:
				lX_num = -this._fTextLocalWidth_num;
				break;
		}

		switch (this._fVerticalAlign_str)
		{
			case AlignDescriptor.TOP:
				lY_num = lContentBounds_r.y;
				break;
			case AlignDescriptor.MIDDLE:
				lY_num = -(lContentBounds_r.height-lContentBounds_r.y)/2;
				break;
			case AlignDescriptor.BOTTOM:
				lY_num = -(lContentBounds_r.height-lContentBounds_r.y);
				break;
		}

		l_sprt.position.set(lX_num*lScaleX_num, lY_num*lScaleY_num);
	}

	_writeIntoBounds()
	{
		let lMaxWidth_num = this._fMaxWidth_num;
		if (lMaxWidth_num === undefined || this._fTextLocalWidth_num === 0)
		{
			return;
		}

		let l_sprt = this._fContentContainer_sprt;
		
		let lScaleX_num = 1;
		if (this._fTextLocalWidth_num > lMaxWidth_num)
		{
			lScaleX_num = lMaxWidth_num / this._fTextLocalWidth_num;
		}

		l_sprt.scale.x = lScaleX_num;
	}

	_clear()
	{
		let lFields_arr = this._fTextFields_tf_arr;
		for (let i=0; i<lFields_arr.length; i++)
		{
			lFields_arr[i].visible = false;
		}
	}

	_getTextField(aIndex_int)
	{
		if (aIndex_int >= this._fTextFields_tf_arr.length)
		{
			return this._generateTextField();
		}

		return this._fTextFields_tf_arr[aIndex_int];
	}

	_generateTextField()
	{
		let lStyle_obj = {
							fontFamily: this.fontName,
							fontSize: this.fontSize,
							fill: this.fontColor
						};

		let l_tf = this._fContentContainer_sprt.addChild(new TextField(lStyle_obj));

		this._fTextFields_tf_arr.push(l_tf);

		return l_tf;
	}

	/**
	 * Destroy instance.
	 */
	destroy()
	{
		this._fContentContainer_sprt = null;

		this._fFontName_str = undefined;
		this._fFontSize_num = undefined;
		this._fFontColor_num = undefined;
		this._fShadowDescriptor_shd = undefined;

		this._fMaxWidth_num = undefined;
		this._fHorizontalAlign_str = undefined;
		this._fVerticalAlign_str = undefined;
		this._fLetterSpace_num = undefined;

		this._fTextValue_str = undefined;
		this._fTextFields_tf_arr = null;

		this._fTemplate_tf = null;
		
		this._fThinChars_str = undefined;

		super.destroy();
	}
}

export default NonWobblingTextField