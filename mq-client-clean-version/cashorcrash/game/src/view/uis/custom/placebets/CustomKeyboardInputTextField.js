import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import InputText from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/InputText';
import CustomInputCursor from './CustomInputCursor';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ScreenKeyboard from './ScreenKeyboard';
import { DecimalPartValidator } from '../../../../ui/BetInputField';

class CustomKeyboardInputTextField extends Sprite 
{
	static get EVENT_ON_BLUR()				{ return InputText.EVENT_ON_BLUR; }
	static get EVENT_ON_FOCUS()				{ return InputText.EVENT_ON_FOCUS; }
	static get EVENT_ON_VALUE_CHANGED()		{ return InputText.EVENT_ON_VALUE_CHANGED; }
	static get EVENT_ON_CLICK()				{ return InputText.EVENT_ON_CLICK; }

	set enabled(aValue_bl)
	{
		if (aValue_bl === this._fEnabled_bl)
		{
			return;
		}

		this._fEnabled_bl = !!aValue_bl;
		if (this._fEnabled_bl)
		{
			this._addEventListeners();
		}
		else
		{
			this._removeEventListeners();
		}
	}

	get enabled()
	{
		return this._fEnabled_bl;
	}

	setValue(aValue_str, aOptApplyFilter_bl = true)
	{
		let lNewFilteredValue_str = "";
		if (aOptApplyFilter_bl)
		{
			lNewFilteredValue_str = this._filterValue(aValue_str);
		}
		else
		{
			lNewFilteredValue_str = aValue_str;
		}

		if (lNewFilteredValue_str === this._fTextValue_str)
		{
			return;
		}

		this._fTextValue_str = lNewFilteredValue_str;

		this._updateTextView();

		this.emit(CustomKeyboardInputTextField.EVENT_ON_VALUE_CHANGED, {value: this.getValue()});
	}

	getValue()
	{
		return this._fTextValue_str;
	}

	updateFontColor(aColor_int)
	{
		let lParams_obj = this.getParams();
		lParams_obj.fontColor = aColor_int;
		
		this.setParams(lParams_obj); 
	}

	updateFontSize(aFontSize_int)
	{
		let lParams_obj = this.getParams();
		lParams_obj.fontSize = aFontSize_int;
		
		this.setParams(lParams_obj); 
	}

	autoBlur()
	{
		this.blur();
	}

	focus(pointerX = null)
	{
		if (this.hasFocus)
		{
			return;
		}

		this.hasFocus = true;
		this._fPrevValue_str = this.getValue();

		this._fTextValue_str = "";
		this._updateTextView();

		this._fCursorView_cic.show();
		this._fCursorView_cic.startBlink();

		this._fCustomKeyboard_kb.show();
		
		APP.gameScreenView.gameViewContainer.on("pointerclick", this._onGameScreenViewClicked, this);
		
		this.emit(CustomKeyboardInputTextField.EVENT_ON_FOCUS);
	}

	blur()
	{
		if (!this.hasFocus)
		{
			return;
		}

		this.hasFocus = false;

		this._fCursorView_cic.hide();
		this._fCustomKeyboard_kb.hide();

		APP.gameScreenView.gameViewContainer.off("pointerclick", this._onGameScreenViewClicked, this);

		this.emit(CustomKeyboardInputTextField.EVENT_ON_BLUR);
	}

	constructor(inputParams, aCustomKeyboard_kb = ScreenKeyboard.instance, aOptIgnoreDecimalFilters_bl=false)
	{
		super();

		this._fCustomKeyboard_kb = aCustomKeyboard_kb;
		this._fCustomKeyboard_kb.on(ScreenKeyboard.EVENT_ON_KEY_PRESSED, this._onKeyboardKeyPressed, this);

		this._fTextValue_str = "";
		this._fPrevValue_str = "";
		this._fTextContainer_sprt = null;
		this._fTextView_tf = null;
		this._fCursorView_cic = null;

		this.hasFocus = false;
		
		this._fEnabled_bl = false;
		
		this.fontSize = null;
		this.fontFamily = null;
		this.fontColor = null;
		this.textAlign = null;
		this.maxTextLength = null;
		this.inputWidth = null;
		this.inputHeight = null;
		this.acceptableChars = null;
		this.clipOnFocusLost = true;
		
		this._init(inputParams);

		this.enabled = true;
		this._fIgnoreDecimalFilters_bl = aOptIgnoreDecimalFilters_bl;
	}

	_init(inputParams)
	{
		this._fTextContainer_sprt = this.addChild(new Sprite);
		this._fTextView_tf = this._fTextContainer_sprt.addChild(new TextField());

		this._fCursorView_cic = this.addChild(new CustomInputCursor());
		this._fCursorView_cic.hide();

		this.setParams(inputParams);
	}

	setParams(inputParams)
	{
		this.fontSize = inputParams.fontSize || 14;
		this.fontFamily = inputParams.fontFamily || "sans-serif";
		this.fontColor = inputParams.fontColor || 0x000000;
		this.textAlign = inputParams.textAlign || "left";
		this.inputWidth = inputParams.width || 150;
		this.inputHeight = inputParams.height || this.fontSize;
		this.acceptableChars = inputParams.acceptableChars || null;
		this.maxTextLength = inputParams.maxTextLength || 0;
		this.clipOnFocusLost = inputParams.clipOnFocusLost !== undefined ? inputParams.clipOnFocusLost : true;

		let lTextStyle_obj = this._fTextView_tf.getStyle() || {};
		lTextStyle_obj.fontFamily = this.fontFamily;
		lTextStyle_obj.fontSize = this.fontSize;
		lTextStyle_obj.fill = this.fontColor;
		this._fTextView_tf.textFormat = lTextStyle_obj;
		
		this._updateHitArea();
		this._applyAlign();
		this._updateCursorView();

		this._updateTextView();
	}

	getParams()
	{
		let lParams_obj =
		{
			fontSize: this.fontSize,
			fontFamily: this.fontFamily,
			fontColor: this.fontColor,
			textAlign: this.textAlign,
			width: this.inputWidth,
			height: this.inputHeight,
			acceptableChars: this.acceptableChars,
			maxTextLength: this.maxTextLength,
			clipOnFocusLost: this.clipOnFocusLost
		}

		return lParams_obj;
	}

	_updateHitArea()
	{
		let lWidth_num = this.inputWidth;
		let lHeight_num = this.inputHeight;

		let lY_num = -lHeight_num / 2;

		let lX_num = -lWidth_num / 2;		
		if (this.textAlign == "left")
		{
			lX_num = 0;
		}
		else if (this.textAlign == "right")
		{
			lX_num = -lWidth_num;
		}

		this.hitArea = new PIXI.Rectangle(lX_num, lY_num, lWidth_num, lHeight_num);

		//DEBUG...
		// this._gr = this._gr || this.addChildAt(new PIXI.Graphics, 0);
		// this._gr.clear();
		// this._gr.beginFill(0xff0000, 0.4).drawRect(this.hitArea.x, this.hitArea.y, this.hitArea.width, this.hitArea.height).endFill();
		//...DEBUG
	}

	_applyAlign()
	{
		let l_sprt = this._fTextContainer_sprt;

		if (!l_sprt)
		{
			return;
		}
		
		let lContentBounds_r = l_sprt.getBounds();
		let lX_num = 0;
		let lY_num = -lContentBounds_r.height/2;

		switch (this.textAlign)
		{
			case "left":
				lX_num = 0;
				break;
			case "right":
				lX_num = -lContentBounds_r.width;
				break;
			case "center":
				lX_num = -lContentBounds_r.width/2;
				break;
		}

		l_sprt.position.set(lX_num, lY_num);
	}

	_updateTextView()
	{
		let lPrintableText_str = this._clipText(this._fTextValue_str);

		this._fTextContainer_sprt.scale.x = 1;

		this._fTextView_tf.text = lPrintableText_str;

		this._applyAlign();

		this._updateCursorPosition();

		let lPrintableTextWidth_num = this._calculateStringWidth(lPrintableText_str);
		if (lPrintableTextWidth_num > this._textAreaWidth)
		{
			this._fTextContainer_sprt.scale.x = this._textAreaWidth/lPrintableTextWidth_num;
			lPrintableTextWidth_num = this._textAreaWidth;
		}
	}

	_clipText(aSourceValue_str)
	{
		if (!this.hasFocus && !this.clipOnFocusLost)
		{
			return aSourceValue_str;
		}

		let lBaseClipText_str = aSourceValue_str;
		let lClippedText_str = lBaseClipText_str;
		let lLen_int = lBaseClipText_str.length;
		for (let i=0; i<lLen_int; i++)
		{
			lClippedText_str = lBaseClipText_str.substring(i);
			let lCurPrintableTextWidth_num = this._calculateStringWidth(lClippedText_str);
			if (lCurPrintableTextWidth_num <= this._textAreaWidth)
			{
				break;
			}
		}

		return lClippedText_str;
	}

	get _textAreaWidth()
	{
		return this.inputWidth;
	}

	_updateCursorView()
	{
		this._fCursorView_cic.cursorHeight = this.inputHeight;
		this._fCursorView_cic.cursorColor = 0xffffff;
	}

	_updateCursorPosition()
	{
		let lPrintableText_str = this._fTextView_tf.text || "";
		let lCursorPosIndex_int = lPrintableText_str.length || 0;

		let lBeforeCursorText_str = lPrintableText_str.substring(0, lCursorPosIndex_int);
		let lBeforeCursorTextWidth_num = this._calculateStringWidth(lBeforeCursorText_str);

		this._fCursorView_cic.x = this._fTextContainer_sprt.x + lBeforeCursorTextWidth_num;
	}

	_calculateStringWidth(text)
	{
		text = text || "";
		let lMult_num = APP.isMobile ? 0.5 : 1;
		return PIXI.TextMetrics.measureText(text, this._fTextView_tf.style, this._fTextView_tf.style.wordWrap).width * lMult_num;
	}

	_isCharacterAcceptable(char)
	{
		if (!DecimalPartValidator.validate(char) && !this._fIgnoreDecimalFilters_bl)
		{
			return false;
		}

		if (this.acceptableChars == null){
			return true;
		}

		let isAccept = !!~this.acceptableChars.indexOf(char);
		
		isAccept = !!~this.acceptableChars.indexOf(char.toUpperCase());
		isAccept = isAccept || !!~this.acceptableChars.indexOf(char.toLowerCase());

		return isAccept;
	}

	_filterValue(aValue_str)
	{
		if (!aValue_str || !aValue_str.length)
		{
			return "";
		}

		let lFilteredValue_str = "";
		for (let i=0; i<aValue_str.length; i++)
		{
			let lChar_str = aValue_str[i];
			if (this._isCharacterAcceptable(lChar_str))
			{
				lFilteredValue_str += lChar_str;
			}
		}

		if (lFilteredValue_str.length > this.maxTextLength)
		{
			lFilteredValue_str = lFilteredValue_str.substring(0, this.maxTextLength);
		}
		
		return lFilteredValue_str;
	}

	_addEventListeners()
	{
		this.on("pointerclick", this._onPointerClick, this);
	}

	_removeEventListeners()
	{
		this.off("pointerclick", this._onPointerClick, this);
	}

	_onPointerClick(event)
	{
		if (this.hasFocus)
		{
			event.stopPropagation();
		}

		this.focus();

		this.emit(CustomKeyboardInputTextField.EVENT_ON_CLICK);
	}

	_onGameScreenViewClicked(event)
	{
		this.blur();
	}

	_onKeyboardKeyPressed(event)
	{
		if (this.hasFocus)
		{
			this._handleInput(event.keyCode, event.keyValue);
		}
	}

	_handleInput(aKeyCode_int, aKeyValue_str)
	{
		let lCurValue_str = this.getValue();
		if (aKeyCode_int === 27) // ESC
		{
			this._revertInput();
			this.blur();
		}
		else if (aKeyCode_int === 13) // Enter
		{
			this.blur();
		}
		else if (aKeyCode_int === 8) // Backspace
		{
			if (!!lCurValue_str && !!lCurValue_str.length)
			{
				this.setValue(lCurValue_str.substring(0, lCurValue_str.length-1));
			}
		}
		else
		{
			let lInputChar_str = aKeyValue_str;
			
			if (this._isCharacterAcceptable(lInputChar_str))
			{
				if (this._fCustomKeyboard_kb.keyboardType === ScreenKeyboard.KEYBOARD_TYPES.NUMERIC)
				{
					let lCurDotIndex_int = lCurValue_str.indexOf(".");
					let lPossibleDemicalsAmount_int = 2;
					if (lCurDotIndex_int >= 0
						&& (
								lInputChar_str == "."
								|| (lCurValue_str.length-1) - lCurDotIndex_int >= 2
							)
						)
					{
						lInputChar_str = "";
					}
					
				}
				this.setValue(lCurValue_str + lInputChar_str);
			}
		}
	}

	_revertInput()
	{
		this.setValue(this._fPrevValue_str);
	}

	destroy()
	{
		super.destroy();
	}
}

export default CustomKeyboardInputTextField;