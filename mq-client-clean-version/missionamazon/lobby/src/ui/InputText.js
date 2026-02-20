import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';

let globalInputs = [];

class InputText extends Sprite 
{
	static get EVENT_ON_BLUR()				{ return "onBlur"; }
	static get EVENT_ON_FOCUS()				{ return "onFocus"; }
	static get EVENT_ON_VALUE_CHANGED()		{ return "onValueChanged"; }

	get value()
	{
		return Number(this.hiddenHTMLInput.value);
	}

	set enabled(aValue_bl)
	{
		this._enabled = aValue_bl;
	}

	get enabled()
	{
		return this._enabled;
	}

	constructor(inputData)
	{
		super();

		this.hiddenHTMLInput = null;
		this.pixiTextContainer = null;
		this.inputBackground = null;
		this.textCursor = null;
		this.textSelection = null;
		this.curInputIndex = null;
		this.cursorBlinkInterval = null;

		this.fontSize = null;
		this.fontFamily = null;
		this.fontColor = null;
		this.fontWeight = null;
		this.fontStyle = null;
		this.textAlign = null;
		this.offsetX = null;
		this.offsetY = null;
		this.readonly = false;
		this.maxTextLength = null;
		this.padding = null;
		this.borderWidth = null;
		this.inputWidth = null;
		this.inputHeight = null;
		this.textCenter = null;
		this.backImgSrc = null;
		this.selectionColor = null;
		this.placeHolder = null;
		this.hiddenValue = null;
		this.visibleValue = null;
		this.acceptableChars = null;
		this.caseSensitive = false;
		this.maxTextLength = null;
		this.selectYShift = 0;
		this.caretYShift = 0;
		this.selectAddHeight = 0;
		this.backgroundColor = null;
		this.dropShadowColor = null;
		this.dropShadowAngle = null;
		this.dropShadowBlur = null;
		this.dropShadowDistance = null;

		this.specCodes = [8, 9, 13, 16, 17, 35, 36, 37, 38, 39, 40, 46, 190];
		this.isCursor = false;
		this.cursorPos = 0;
		this.hasFocus = false;
		this.selection = [0, 0];
		this.wasOver = false;
		this.leftEdge = 0;
		this.rightEdge = 0;
		this.ctrlHolded = false;
		this.shiftHolded = false;
		this.selectionCursorStart = 0;
		this.isPointerDown = false;
		this.selectionUpdated = false;
		this.selectionStart = 0;
		this.pointerUpInside = false;

		this._fEnabled_bl = false;
		this._pointerDownInside = false;

		this._isiOS = navigator.userAgent.match(/ipad|iphone/i);
		this._isAndroid = navigator.userAgent.toLowerCase().indexOf("android") > -1;

		this.init(inputData);
	}

	destroy()
	{
		let self = this;
		let inputs = globalInputs;
		let currentInputIndex = inputs.indexOf(self);

		if (currentInputIndex != -1)
		{
			inputs.splice(currentInputIndex, 1);
		}

		if (self.hasFocus)
		{
			self.blur();
		}

		document.body.removeChild(self.hiddenHTMLInput);

		super.destroy();
	}

	setText(text){
		this.hiddenHTMLInput.value = this.hiddenValue = text;
	}

	setValue(aValue_str){
		aValue_str = this.inputFilter(aValue_str);
		aValue_str = this.filter(aValue_str);
		
		this.hiddenHTMLInput.value = aValue_str || "";
		this.textSelection.width = 0;

		this.hiddenValue = this.hiddenHTMLInput.value;

		if (this.hasFocus){
			this.onfocus(this);
		} else {
			this.onblur(this);
		}

		this.updateEdges();
		this.renderText();
	}

	getValue(){
		return this.hiddenHTMLInput.value;
	}

	updateFont(fontName)
	{
		this.fontFamily = fontName;

		if (this.pixiTextContainer)
		{
			let txtStyle = this.pixiTextContainer.getStyle() || {};
			txtStyle.fontFamily = fontName;
			this.pixiTextContainer.textFormat = txtStyle;
		}
	}

	init(inputData)
	{
		this.initGlobalInputText();
		this.setParams(inputData);
		this.initElements();
		this.updateEdges();
		this.renderText();

		this.hiddenHTMLInput.addEventListener("keydown", this.keydown.bind(this));
		this.hiddenHTMLInput.addEventListener("keyup", this.keyup.bind(this));
		this.hiddenHTMLInput.addEventListener("input", this.input.bind(this));

		if (this._isAndroid)
		{
			this.hiddenHTMLInput.addEventListener("blur", (e) => {
				if (this._pointerDownInside)
				{
					this._pointerDownInside = false;
					this.hiddenHTMLInput.focus();
				}
			});
		}

		if (this._isiOS)
		{
			this.hiddenHTMLInput.addEventListener("blur", this.blur.bind(this));
		}

		if(!!window.PointerEvent)
		{
			window.addEventListener("pointerup", this.autoBlur.bind(this), true);
		}
		else
		{
			window.addEventListener("touchend", this.autoBlur.bind(this), true);
			window.addEventListener("mouseup", this.autoBlur.bind(this), true);
		}

		window.addEventListener("dblclick", this._onDblclick.bind(this), true);

		this._enabled = true;
	}

	_onDblclick()
	{
		if (this.hasFocus)
		{
			this.selectText();
			this.cursorPos = this.selection[1];
		}
	}

	initGlobalInputText(){
		globalInputs.push(this);
		this.curInputIndex = globalInputs.length - 1;
	}

	setParams(inputData){
		this.fontSize = inputData.fontSize || 14;
		this.fontFamily = inputData.fontFamily || "Arial";
		this.fontColor = inputData.fontColor || 0x000000;
		this.fontWeight = inputData.fontWeight || "normal";
		this.fontStyle = inputData.fontStyle || "normal";
		this.textAlign = inputData.textAlign || "left";
		this.offsetX = inputData.offsetX || 0;
		this.offsetY = inputData.offsetY || -2;
		this.readonly = inputData.readonly || false;
		this.maxTextLength = inputData.maxlength || null;
		this.padding = (inputData.padding >= 0 ? inputData.padding : 3);
		this.borderWidth = inputData.borderWidth >= 0 ? inputData.borderWidth : 1;
		this.inputWidth = (inputData.width || 150) - this.padding - this.borderWidth;
		this.inputHeight = inputData.height || this.fontSize;
		this.textCenter = inputData.center || (this.inputWidth - this.padding) / 2;
		this.backImgSrc = inputData.backgroundImage || null;
		this.selectionColor = inputData.selectionColor || 0x000000;
		this.placeHolder = inputData.placeHolder || "";
		this.hiddenValue = (inputData.value || this.placeHolder) + "";
		this.acceptableChars = inputData.acceptableChars || null;
		this.caseSensitive = !!inputData.caseSensitive || false;
		this.maxTextLength = inputData.maxTextLength || 0;
		this.visibleValue = this.hiddenValue;
		this.selectYShift = inputData.selectYShift || 0;
		this.caretYShift = inputData.caretYShift || 0;
		this.selectAddHeight = (inputData.selectAddHeight == undefined ? 0 : inputData.selectAddHeight)
		this.backgroundColor = inputData.backgroundColor || 0xffffff;
		this.stroke = inputData.stroke || null,
		this.strokeThickness = inputData.strokeThickness || null,
		this.dropShadow = inputData.dropShadow || null,
		this.dropShadowColor = inputData.dropShadowColor || null,
		this.dropShadowAngle = inputData.dropShadowAngle || null,
		this.dropShadowBlur = inputData.dropShadowBlur || null,
		this.dropShadowDistance = inputData.dropShadowDistance || null,
		this.dropShadowAlpha = inputData.dropShadowAlpha || null
	}

	initElements(){
		let style = {
			align: this.textAlign,
			fontFamily: this.fontFamily,
			fontSize: this.fontSize,
			fill: this.fontColor,
			stroke: this.stroke,
			strokeThickness: this.strokeThickness,
			fontWeight: this.fontWeight,
			dropShadow: this.dropShadow,
			dropShadowColor: this.dropShadowColor,
			dropShadowAngle: this.dropShadowAngle,
			dropShadowBlur: this.dropShadowBlur,
			dropShadowDistance: this.dropShadowDistance,
			dropShadowAlpha: this.dropShadowAlpha
		};
		
		if (this.backImgSrc)
		{
			this.inputBackground = new Sprite(APP.library.getAsset(this.backImgSrc));
			this.addChild(this.inputBackground);
		}
		else
		{
			let width = this.inputWidth + this.padding + this.borderWidth;
			this.inputBackground = new PIXI.Rectangle(-width / 2, this.inputHeight / 2, width, this.inputHeight);
		}

		this.textCursor = new PIXI.Graphics();
		this.textSelection = new PIXI.Graphics();

		this.addChild(this.textSelection);

		this.pixiTextContainer = this.addChild(new TextField(style));
		this.pixiTextContainer.text = "";
		this.pixiTextContainer.anchor.set(0.5, 0.5);

		this.addChild(this.textCursor);

		this.hiddenHTMLInput = document.createElement("input");
		this.hiddenHTMLInput.type = "text";
		this.hiddenHTMLInput.style.position = "absolute";
		this.hiddenHTMLInput.style.opacity = 0;
		this.hiddenHTMLInput.style.pointerEvents = "none";
		this.hiddenHTMLInput.style.zIndex = 0;
		this.hiddenHTMLInput.style.textIndent = "-9999999em";
		this.hiddenHTMLInput.style.transform = "scale(0)";

		this.updateHiddenInput();
		if (this.maxTextLength){
			this.hiddenHTMLInput.maxLength = this.maxTextLength;
		}
		document.body.appendChild(this.hiddenHTMLInput);
		this.hiddenHTMLInput.value = this.hiddenValue;

		this.prerenderGraphics();
	}

	set _enabled(aValue_bl)
	{
		if (aValue_bl === this._fEnabled_bl)
		{
			return;
		}

		this._fEnabled_bl = !!aValue_bl;
		if (this._fEnabled_bl)
		{
			this.addEventListeners();
		}
		else
		{
			this.removeEventListeners();
		}
	}

	get enabled()
	{
		return this._fEnabled_bl;
	}

	addEventListeners()
	{
		let bounds = null;

		if (this.inputBackground.getBounds)
		{
			bounds = this.inputBackground.getBounds();
		}
		else
		{
			bounds = this.inputBackground;
		}

		this.hitArea = new PIXI.Rectangle(-bounds.width/2, -bounds.height/2, bounds.width, bounds.height);

		this.on("pointerdown", this.onPointerdown, this);
		this.on("pointerup", this.onPointerup, this);
		this.on("pointerupoutside", this.onPointerup, this);
		this.on("pointermove", this.onPointermove, this);
	}

	removeEventListeners()
	{
		this.hitArea = null;

		this.off("pointerdown", this.onPointerdown, this);
		this.off("pointerup", this.onPointerup, this);
		this.off("pointerupoutside", this.onPointerup, this);
		this.off("pointermove", this.onPointermove, this);
	}

	autoBlur()
	{
		if (APP.isMobile)
		{
			this.pointerUpInside = false;

			if (this.hasFocus && !this.isPointerDown)
			{
				this.hideKeyboard();
				this.blur();
			}
		}
		else
		{
			if (this.pointerUpInside)
			{
				this.pointerUpInside = false;
				return;
			}
			else if (this.hasFocus && !this.isPointerDown)
			{
				this.blur();
			}
		}
	}

	hideKeyboard()
	{
		setTimeout(function()
		{
			var field = document.createElement('input');
			field.setAttribute('type', 'text');
			field.setAttribute('style', 'position:absolute; top: 0px; -webkit-transform: translateY(-9999px); -webkit-user-modify: read-write-plaintext-only; left:0px;');
			document.body.appendChild(field);

			field.onfocus = function()
			{
				setTimeout(function()
				{
					field.setAttribute('style', 'display:none;');
					setTimeout(function()
					{
						document.body.removeChild(field);
						document.body.focus();
					}, 14);
				}, 200);
			};

			field.focus();
		}, 50);
	}

	updateEdges(prevHiddenValue = null){
		let hiddenLength = this.hiddenValue.length;
		let visibleLength = this.clipText().length;
		let cursorPos = this.cursorPos;

		if (this.selectionCursorStart <= this.cursorPos){
			cursorPos = this.selection[1];
		}

		if (prevHiddenValue){
			if (this.rightEdge != prevHiddenValue.length){
				let prevLeftLen = prevHiddenValue.substr(0, this.leftEdge).replace(/,/g, "").length;
				let newLeftLen = this.hiddenValue.substr(0, this.leftEdge).replace(/,/g, "").length;

				this.leftEdge -= (newLeftLen - prevLeftLen);
				this.rightEdge = this.leftEdge + visibleLength;
			} else {
				this.rightEdge = hiddenLength;
				this.leftEdge = hiddenLength - visibleLength;
			}
			
		} else if (hiddenLength == visibleLength){
			this.leftEdge = 0;
			this.rightEdge = visibleLength;
		} else if (cursorPos >= this.leftEdge + visibleLength){
			this.rightEdge = cursorPos;
			this.leftEdge = cursorPos - visibleLength;
		} else if (cursorPos <= this.leftEdge){
			this.leftEdge = cursorPos;
			this.rightEdge = this.leftEdge + visibleLength;
		}
	}

	focus(pointerX = null){
		this.emit(InputText.EVENT_ON_FOCUS);

		// if (this._isiOS)
		// {
		// 	console.log("IOS focus");
		// }

		let oldHiddenValue = this.hiddenValue;
		this.selectionCursorStart = null;

		if (!this.hasFocus){
			this.onfocus(this);

			let inputs = globalInputs;
			for (let i = 0; i < inputs.length; ++i){
				if (inputs[i].hasFocus){
					inputs[i].blur();
				}
			}
		}

		if (!this.selectionUpdated){
			this.selection = [0, 0];
			this.textSelection.width = 0;
		} else {
			this.selectionUpdated = false;
		}

		this.hasFocus = true;
		if (this.readonly){
			this.hiddenHTMLInput.readOnly = true;
		} else {
			this.hiddenHTMLInput.readOnly = false;
			let cursorPos = pointerX ? this.getClickPos(pointerX) : this.clipTextWithEdges().length;
			this.cursorPos = cursorPos;

			if (this.placeHolder === this.hiddenValue){
				this.hiddenValue = "";
				this.hiddenHTMLInput.value = "";
			}

			this.setUpCursor();
		}

		let hasSelection = (this.selection[0] > 0 || this.selection[1] > 0);
		this.hiddenHTMLInput.focus();
		this.hiddenHTMLInput.selectionStart = hasSelection ? this.selection[0] : this.cursorPos;
		this.hiddenHTMLInput.selectionEnd = hasSelection ? this.selection[1] : this.cursorPos;
		this.updateEdges(oldHiddenValue);

		return this.renderText();
	}

	blur(){
		this.emit(InputText.EVENT_ON_BLUR);

		// if (this._isiOS)
		// {
		// 	console.log("IOS blur");
		// }

		let oldHiddenValue = this.hiddenValue;

		this.onblur(this);

		if (this.cursorBlinkInterval){
			clearInterval(this.cursorBlinkInterval);
		}

		this.hasFocus = false;
		this.isCursor = false;
		this.selectionCursorStart = null;
		this.selection = [0, 0];

		this.textSelection.width = 0;
		this.hiddenHTMLInput.blur();

		if (this.hiddenValue === ""){
			this.hiddenValue = this.placeHolder;
		}

		this.updateEdges(oldHiddenValue);

		return this.renderText();
	}

	setUpCursor(){
		if (this.cursorBlinkInterval){
			clearInterval(this.cursorBlinkInterval);
		}

		this.cursorBlinkInterval = setInterval(this.cursorUpdate.bind(this), 500);
	}

	cursorUpdate(){
		this.isCursor = !this.isCursor;
		this.renderText();
	}

	keydown(e){
		e = e || window.event;

		let keyCode = e.which;

		if (!this.isCharacterAcceptable(e.key)  && !this.ctrlHolded && !~this.specCodes.indexOf(e.keyCode)){
			e.preventDefault();
			return;
		}

		if (this.hasFocus){
			window.focus();
			this.hiddenHTMLInput.focus();

			if (this.readonly || !this.hasFocus){
				return;
			}

			this.onkeydown(e);

			let oldHiddenValue = null;
			if (keyCode == 8 || keyCode == 46 || (this.acceptableChars != null && !!~this.acceptableChars.indexOf(e.key))){
				oldHiddenValue = this.hiddenValue;
			} else if (keyCode === 65 && (e.ctrlKey || e.metaKey)){
				this.selectText();
				e.preventDefault();
				this.renderText();
				return;
			} else if (keyCode === 17 || e.metaKey || e.ctrlKey){
				this.ctrlHolded = true;
				return;
			}

			if (keyCode === 13){
				this.blur();
				e.preventDefault();
				this.onsubmit(e);
			} else if (keyCode === 9){
				e.preventDefault();
				
				if (globalInputs.length > 1){
					this.blur();
					setTimeout(this.focusNext.bind(this), 10);
				}
			}

			this.updateEdges(oldHiddenValue);
			this.hiddenValue = this.hiddenHTMLInput.value;
			this.cursorPos = this.hiddenHTMLInput.selectionStart;
			if (keyCode == 16){
				this.shiftHolded = true;
				if (this.selection[0] == this.selection[1]){
					this.selectionCursorStart = this.cursorPos;
				}
				return;
			}

			if (!this.shiftHolded){
				this.textSelection.width = 0;
			}
		}

		return this.renderText();
	}

	keyup(e){
		if (e.keyCode == 17){
			this.ctrlHolded = false;
		}

		let oldHiddenValue = null;

		if (e.keyCode == 8 || e.keyCode == 46 || this.isCharacterAcceptable(e.key)){
			oldHiddenValue = this.hiddenValue;
		}

		if (this.hiddenValue != this.hiddenHTMLInput.value){
			let newLen = this.hiddenHTMLInput.value.length;
			let newSelection = [this.hiddenHTMLInput.selectionStart, this.hiddenHTMLInput.selectionEnd];
			let extraParam = {selection: null};
			this.hiddenHTMLInput.value = this.inputFilter(this.hiddenHTMLInput.value, extraParam);
			let div = newLen - this.hiddenHTMLInput.value.length;
			this.hiddenHTMLInput.selectionStart = newSelection[0] - div;
			this.hiddenHTMLInput.selectionEnd = newSelection[1] - div;
			if (extraParam.selection != null){
				this.hiddenHTMLInput.selectionStart = extraParam.selection;
				this.hiddenHTMLInput.selectionEnd = extraParam.selection;
			}
		}

		if (e.keyCode == 16){
			this.shiftHolded = false;
		}

		this.hiddenValue = this.hiddenHTMLInput.value;
		this.cursorPos = this.hiddenHTMLInput.selectionStart;
		this.selection = [this.hiddenHTMLInput.selectionStart, this.hiddenHTMLInput.selectionEnd];

		this.updateEdges(oldHiddenValue);
		this.renderText();

		if (this.hasFocus){
			this.onkeyup(e);
		}
	}

	isCharacterAcceptable(char){
		if (this.acceptableChars == null){
			return true;
		}

		let isAccept = !!~this.acceptableChars.indexOf(char);
		if (this.caseSensitive)
		{
			return isAccept;
		}

		isAccept = !!~this.acceptableChars.indexOf(char.toUpperCase());
		isAccept = isAccept || !!~this.acceptableChars.indexOf(char.toLowerCase());

		return isAccept;
	}

	input(){
		let value = this.hiddenHTMLInput.value;
		this.hiddenHTMLInput.value = this.filter(value);
		this.textSelection.width = 0;
	}

	focusNext(){
		let inputs = globalInputs;
		let next = (inputs[this.curInputIndex + 1]) ? this.curInputIndex + 1 : 0;
		inputs[next].focus();
	}

	filter(aValue_str){
		let filteredValue = "";
		for (let i = 0; i < aValue_str.length; ++i){
			if (this.isCharacterAcceptable(aValue_str[i])){
				filteredValue += aValue_str[i];
			}
		}

		if (this.maxTextLength != 0){
			let dotPos = this.hiddenValue.indexOf(".");
			let oldLen = ~dotPos ? dotPos :  this.hiddenValue.length;
			dotPos = filteredValue.indexOf(".");
			let newLen = ~dotPos ? dotPos :  filteredValue.length;
			if (newLen > this.maxTextLength){
				if (oldLen == this.maxTextLength){
					filteredValue = this.hiddenValue;
				} else {
					filteredValue = filteredValue.substring(0, this.maxTextLength);
					if (dotPos >= 0)
					{
						filteredValue += filteredValue.substring(dotPos);
					}
				}
			}
		}

		return filteredValue;
	}

	click(e){
		let pointerPos = this.getPointerPos(e);

		this.isCursor = true;
		this.renderText();

		if (this.isPointerDown){
			this.isPointerDown = false;
			this.click(e, this);
			return this.focus(pointerPos.x);
		}
	}

	onPointermove(e){
		let localMouse = this.toLocal(e.data.global);
		let isHit = this.hitArea.contains(localMouse.x, localMouse.y);

		if (!isHit){
			this.onRollout();
		}
		else{
			this.onRollover();
		}

		if (this.isPointerDown){
			let pointerPos = this.getPointerPos(e);
			let pointerX = pointerPos.x;

			if (this.hasFocus && this.selectionStart >= 0){
				let currentPos = this.getClickPos(pointerX);
				this.cursorPos = currentPos;
				let start = Math.min(this.selectionStart, currentPos);
				let end = Math.max(this.selectionStart, currentPos);

				if (this.selection[0] !== start || this.selection[1] !== end){
					this.selection = [start, end];
					this.renderText();
				}
			}

			this.tryMoveSelection(this.getClickPos(pointerX));
		}
	}

	onRollover(){
		APP.stage.renderer.view.style.cursor = "text";
		this.wasOver = true;
	}

	onRollout(){
		if (this.wasOver){
			APP.stage.renderer.view.style.cursor = "inherit";
			this.wasOver = false;
		}
	}

	onPointerdown(e){
		let pointerPos = this.getPointerPos(e);
		this.isPointerDown = true;

		this._pointerDownInside = true;

		this.selectionStart = this.getClickPos(pointerPos.x);
		this.cursorPos = this.selectionStart;

		this.focus(pointerPos.x);
	}

	onPointerup(e){
		this.pointerUpInside = true;

		let pointerPos = this.getPointerPos(e);

		let isSelection = this.getClickPos(pointerPos.x) !== this.selectionStart;
		if (this.hasFocus && this.selectionStart >= 0 && isSelection){
			this.selectionUpdated = true;
			this.selectionStart = null;
			this.renderText();
		} else {
			this.selectionStart = null;
		}

		this.click(e, this);
	}

	onTextChanged(){
		this.emit(InputText.EVENT_ON_VALUE_CHANGED, {value: this.getValue()});
	}

	selectText(range){
		range = range || [0, this.hiddenValue.length];
		this.selection = [range[0], range[1]];

		setTimeout(this.select.bind(this), 1);
	}

	select(){
		this.hiddenHTMLInput.selectionStart = this.selection[0];
		this.hiddenHTMLInput.selectionEnd = this.selection[1];
		this.renderText();
	}

	renderText(){
		let clipedText = this.clipTextWithEdges();
		let textWidth = this.getTextWidth(clipedText);

		let centerOffset = 0;
		switch (this.textAlign){
			case "center":
			centerOffset = 0;
			break;
			case "left":
			centerOffset = textWidth / 2;
			break;
			case "right":
			centerOffset = -textWidth / 2;
			break;
		}

		let offsetX = this.getTextWidth(clipedText.substring(this.selection[0] - this.leftEdge));
		let selectSubstr = clipedText.substring(this.selection[0] - this.leftEdge, this.selection[1] - this.leftEdge);
		let selectWidth = this.getTextWidth(selectSubstr);

		if (selectWidth != this.textSelection.width){
			let selectionX = textWidth / 2 - offsetX + centerOffset;

			this.textSelection.position.x = selectionX;
			this.textSelection.width = selectWidth;
		}

		if (!this.isCursor){
			this.textCursor.visible = false;
		} else {
			let cursorX = this.getTextWidth(clipedText.substring(0, this.cursorPos - this.leftEdge)) - textWidth / 2;

			if (selectWidth > 0 && this.selectionCursorStart && this.selectionCursorStart <= this.cursorPos){
				cursorX += selectWidth;
			}

			this.textCursor.position.x = cursorX;
			this.textCursor.visible = true;
		}

		clipedText = (clipedText === "" && this.placeHolder) ? this.placeHolder : clipedText;
		if (this.visibleValue != clipedText){
			this.visibleValue = clipedText;
			this.pixiTextContainer.text = this.visibleValue;

			this.onTextChanged();
		}

		return this;
	}

	prerenderGraphics(){
		let textHeight = this.pixiTextContainer.height + this.selectAddHeight;

		this.textSelection.beginFill(this.selectionColor);
		this.textSelection.drawRect(0, -textHeight / 2, 1, textHeight);
		this.textSelection.endFill();
		this.textSelection.width = 0;

		this.textCursor.beginFill(this.fontColor);
		this.textCursor.drawRect(0, -this.fontSize / 2, 1, this.fontSize);
		this.textCursor.endFill();
		this.textCursor.cacheAsBitmap = true;

		return this;
	}

	clipText(aValue_str){
		aValue_str = aValue_str || this.hiddenValue;

		let textWidth = this.getTextWidth(aValue_str);
		let fillPer = textWidth / (this.inputWidth - (this.padding + this.borderWidth));
		let clipedText = fillPer > 1 ? aValue_str.substr(-1 * Math.floor(aValue_str.length / fillPer)) : aValue_str;

		return clipedText + "";
	}

	clipTextWithEdges(aValue_str){
		aValue_str = aValue_str || this.hiddenValue;
		let clipedText = aValue_str.substr(this.leftEdge, this.rightEdge - this.leftEdge);

		return clipedText + "";
	}

	getTextWidth(text){
		text = text || "";
		return APP.isMobile? PIXI.TextMetrics.measureText(text, this.pixiTextContainer.style, this.pixiTextContainer.style.wordWrap).width /2 : PIXI.TextMetrics.measureText(text, this.pixiTextContainer.style, this.pixiTextContainer.style.wordWrap).width ;
	}

	updateHiddenInput(){
		this.hiddenHTMLInput.style.left = this.x + "px";
		this.hiddenHTMLInput.style.top = this.y + "px";
		this.hiddenHTMLInput.style.width = (this.inputWidth + this.padding * 2) + "px";
		this.hiddenHTMLInput.style.height = this.inputHeight + "px";
	}

	getClickPos(pointerX){
		let text = this.clipTextWithEdges();
		if (this.hiddenValue === this.placeHolder){
			text = "";
		}
		let totalWidth = 0;
		let position = text.length;

		pointerX += this.getTextWidth(text) / 2;

		if (pointerX < this.getTextWidth(text)){
			for (let i = 0; i < text.length; ++i){
				totalWidth += this.getTextWidth(text[i]);
				if (totalWidth >= (pointerX + this.getTextWidth(text[i])/3)){
					position = i;
					break;
				}
			}
		}

		return position + this.leftEdge;
	}

	getPointerPos(e){
		return e.data.local;
	}

	tryMoveSelection (position){
		let isHiddenTexts = this.hiddenValue.length > this.clipTextWithEdges().length;

		if (position == this.leftEdge && isHiddenTexts && this.leftEdge != 0){
			--this.leftEdge;
			--this.rightEdge;
		} else if (position == this.rightEdge && isHiddenTexts && this.rightEdge != this.hiddenValue.length){
			++this.leftEdge;
			++this.rightEdge;
		}
	}

	onkeyup(e){
	}

	onkeydown(e){
	}

	onsubmit(){
	}

	onfocus(){
	}

	onblur(){
	}

	inputFilter(value){
		return value;
	}
}

export default InputText;