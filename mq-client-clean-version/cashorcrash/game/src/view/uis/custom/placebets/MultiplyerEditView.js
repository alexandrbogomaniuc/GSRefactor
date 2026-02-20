import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import GameButton from '../../../../ui/GameButton';
import InputText from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/InputText';
import CustomKeyboardInputTextField from './CustomKeyboardInputTextField';
import StepArrowsView from './StepArrowsView';

class MultiplyerEditView extends Sprite
{	
	static get EVENT_ON_MULTIPLIER_FOCUS_LOST ()				{ return "EVENT_ON_MULTIPLIER_FOCUS_LOST"; }
	static get EVENT_ON_MULTIPLIER_VALUE_RESET ()				{ return "EVENT_ON_MULTIPLIER_VALUE_RESET"; }
	static get EVENT_ON_MULTIPLIER_VALUE_UPDATED ()				{ return "EVENT_ON_MULTIPLIER_VALUE_UPDATED"; }
	

	get enabled()
	{
		return this._fIsEnabled_bl;
	}

	set enabled(value)
	{
		if (this._fIsEnabled_bl === value)
		{
			return;
		}

		this._fIsEnabled_bl = value;

		if (this._fIsEnabled_bl)
		{
			this._fMultiplyerEditView_tf.enabled = true;
			this._fAutoEjectResetButton_gb.enabled = true;
			this._validateArrowBtns();
		}
		else
		{
			this._fMultiplyerEditView_tf.enabled = false;
			this._fAutoEjectResetButton_gb.enabled = false;
			this._validateArrowBtns();
		}
	}

	allowAutoEjectReset()
	{
		this._fAutoEjectResetButton_gb.enabled = true;
	}

	updateLayout(aRowWidth_num)
	{
		this._fRowWidth_num = aRowWidth_num;
		this._updateLayoutSettings();
	}

	unfocus()
	{
		this._fMultiplyerEditView_tf.hasFocus && this._fMultiplyerEditView_tf.autoBlur();
	}

	set multiplierValue(value)
	{
		this._applyAutoEjectMultiplier(value);
		this._validateAutoEjectMultiplierValue();

		this.emit(MultiplyerEditView.EVENT_ON_MULTIPLIER_VALUE_UPDATED);
	}

	get multiplierValue()
	{
		return this._fSelectedAutoEjectMultiplier_int;
	}

	handleDeniedAutoEject(aAutoEjectMultValue_num)
	{
		if (+this._clearedAutoEjectMultiplierEnteredValue === aAutoEjectMultValue_num)
		{
			this.unfocus();
			this._applyMultViewStyle(false);
		}
	}

	//INIT...
	constructor(aRowWidth_num)
	{
		super();

		this._fRowWidth_num = aRowWidth_num;

        this._fCurrentBet_num = 1;
        this._fMultiplyerEditView_tf = null;
        this._fSelectedAutoEjectMultiplier_int = undefined;
		this._fIsEnabled_bl = false;
		this._fIsAutoEjectResetAllowed_bl = false;

		this._fBetsInfo_bsi = APP.gameController.gameplayController.gamePlayersController.betsController.info;

		this._fMultiplierOFFMessage_ta = this.addChild(I18.generateNewCTranslatableAsset("TAAutoCashoutOFF"));
		this._fMultiplierOFFMessage_ta.visible = true;

		this._fMultiplyerEditView_tf = this.addChild(this._generateInputField());
		this._fMultiplyerEditView_tf.setValue("OFF");
		this._fMultiplyerEditView_tf.on(InputText.EVENT_ON_VALUE_CHANGED, this._onAutoEjectMultiplierValueChanged, this);
		this._fMultiplyerEditView_tf.on(InputText.EVENT_ON_BLUR, this._onAutoEjectMultiplierFocusLost, this);
		this._fMultiplyerEditView_tf.on(InputText.EVENT_ON_FOCUS, this._onAutoEjectMultiplierFocus, this);
		this._validateAutoEjectMultiplierValue();

		this._fStepArrowsView_srsv = this.addChild(new StepArrowsView);
		this._fStepArrowsView_srsv.on(StepArrowsView.EVENT_ON_DOWN_STEP, this._onDownStep, this);
		this._fStepArrowsView_srsv.on(StepArrowsView.EVENT_ON_UP_STEP, this._onUpStep, this);
		
		let lAutoEjectResetButtonBase_gr = new PIXI.Graphics();
		let lAutoEjectResetButton_gb = this._fAutoEjectResetButton_gb = this.addChild(new GameButton(lAutoEjectResetButtonBase_gr, undefined, true));
		lAutoEjectResetButton_gb.grayFilter.greyscale(0.4, false);
		lAutoEjectResetButton_gb.on("pointerclick", this._onAutoEjectMultiplierValueReset, this);
		
		this._updateLayoutSettings();

		this.enabled = true;

		this._fFocusOnScrollableContainer = null;
	}

	_generateAutoEjectResetBtnLabel()
	{
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;
		let lFontSize_num = lIsPortraitMode_bl ? 28 : 16;
		let lAutoEjectResetLabel_tf = new TextField({fontFamily:"fnt_nm_barlow_bold", fontSize:lFontSize_num, fill:0xFFFFFF});
		lAutoEjectResetLabel_tf.anchor.set(0.5, 0.5);
		lAutoEjectResetLabel_tf.text = "X";

		return lAutoEjectResetLabel_tf;
	}

	_generateInputField()
	{
		let lInputConfig_obj = this._getTextEditFormat();

		let lInput_tf = APP.isMobile ? new CustomKeyboardInputTextField(lInputConfig_obj, undefined, true) : new InputText(lInputConfig_obj);

		return lInput_tf
	}
	//...INIT

	_updateLayoutSettings()
	{
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;
		let lRowContentOffsetX_num = lIsPortraitMode_bl ? 3 : 3;

		let lMultiplyerEditViewX_num = 0;
		let lMultiplyerEditViewY_num = lIsPortraitMode_bl ? 10 : 10;
		this._fMultiplyerEditView_tf.position.set(lMultiplyerEditViewX_num, lMultiplyerEditViewY_num);
		
		let lMultiplierOFFMessageCaptionAssetId_str = lIsPortraitMode_bl ? "TAAutoCashoutOFFPortrait" : "TAAutoCashoutOFF";
		this._fMultiplierOFFMessage_ta.position.set(lMultiplyerEditViewX_num, lMultiplyerEditViewY_num);
		this._fMultiplierOFFMessage_ta.setAssetDescriptor(I18.getTranslatableAssetDescriptor(lMultiplierOFFMessageCaptionAssetId_str));

		let lResetButtonBaseWidth_num = lIsPortraitMode_bl ? 43 : 26;
		let lResetButtonBaseHeight_num = lIsPortraitMode_bl ? 40 : lResetButtonBaseWidth_num;
		let lResetButtonBase_gr = new PIXI.Graphics().beginFill(0x242b44, 1).drawRoundedRect(-lResetButtonBaseWidth_num/2, -lResetButtonBaseHeight_num/2, lResetButtonBaseWidth_num, lResetButtonBaseHeight_num, 5).endFill();
		let lAutoEjectResetButton_gb = this._fAutoEjectResetButton_gb;
		let lResetButtonX_num = lIsPortraitMode_bl ? this._fRowWidth_num/2 - lRowContentOffsetX_num - lResetButtonBaseWidth_num/2 : this._fRowWidth_num - lRowContentOffsetX_num - lResetButtonBaseWidth_num/2;
		let lResetButtonY_num = lIsPortraitMode_bl ? 0 : 4;
		lAutoEjectResetButton_gb.updateBase(lResetButtonBase_gr);
		lAutoEjectResetButton_gb.updateCaptionView(this._generateAutoEjectResetBtnLabel());
		lAutoEjectResetButton_gb.position.set(lResetButtonX_num, lResetButtonY_num);
		
		let lInputAreaRightBorder_num = lIsPortraitMode_bl ? 118 : 158;
		let inputData = this._fMultiplyerEditView_tf.getParams();
		inputData.width = ~~((lInputAreaRightBorder_num - this._fMultiplyerEditView_tf.position.x)/this._fMultiplyerEditView_tf.scale.x);
		inputData.fontSize = lIsPortraitMode_bl ? 20 : this._getTextEditFormat().fontSize;
		inputData.height = inputData.fontSize+4;
		this._fMultiplyerEditView_tf.setParams(inputData); 
		this._fMultiplyerEditView_tf.blur();

		this._fStepArrowsView_srsv.updateLayout();
		this._fStepArrowsView_srsv.position.set(lInputAreaRightBorder_num + this._fStepArrowsView_srsv.getBounds().width/2, lResetButtonY_num);
	}

	_onAutoEjectMultiplierValueChanged()
	{
		let lEnteredValue_bl = this._clearedAutoEjectMultiplierEnteredValue;
		this._applyAutoEjectMultiplier(lEnteredValue_bl);
	}

	_onAutoEjectMultiplierValueReset()
	{
		this._applyAutoEjectMultiplier(0);
		this._validateAutoEjectMultiplierValue();

		this.emit(MultiplyerEditView.EVENT_ON_MULTIPLIER_VALUE_RESET);
	}

	get _clearedAutoEjectMultiplierEnteredValue()
	{
		let lValue_str = this._fMultiplyerEditView_tf.getValue();
		lValue_str = lValue_str.replace(/OFF/gi, '');

		return lValue_str;
	}

	_applyAutoEjectMultiplier(aValue_str)
	{
		this._fSelectedAutoEjectMultiplier_int = !aValue_str ? undefined : +aValue_str;


		let lIsValidAutoEjectMult_bl = this._fBetsInfo_bsi.isValidAutoEjectMultiplier(this._fSelectedAutoEjectMultiplier_int);

		this._applyMultViewStyle(lIsValidAutoEjectMult_bl);
		this._validateArrowBtns();
	}

	_applyMultViewStyle(aIsValidValue_bl = true)
	{
		let lFontColor_int = aIsValidValue_bl ? 0xFFFFFF : 0xFF0000;

		this._fMultiplyerEditView_tf.updateFontColor(lFontColor_int);
	}
	
	_onAutoEjectMultiplierFocus()
	{
		let lValue_str = this._clearedAutoEjectMultiplierEnteredValue;

		this._fMultiplyerEditView_tf.setValue( lValue_str );
		this._fMultiplierOFFMessage_ta.visible = false;
	}

	_onAutoEjectMultiplierFocusLost()
	{
		let lNewValue_str = this._clearedAutoEjectMultiplierEnteredValue;

		if (!this._fBetsInfo_bsi.isValidAutoEjectMultiplier(+lNewValue_str))
		{
			lNewValue_str = "";
		}

		this.multiplierValue = +lNewValue_str;
	}

	_validateAutoEjectMultiplierValue()
	{
		if (this.multiplierValue === undefined)
		{
			this._fMultiplyerEditView_tf.setValue("OFF");
			this._fMultiplierOFFMessage_ta.visible = true;
		}
		else
		{
			
			this._fMultiplyerEditView_tf.setValue( APP.currencyInfo.i_formatNumber( this.multiplierValue*100,false,false,2));
			this._fMultiplierOFFMessage_ta.visible = false;
		}
		this._validateArrowBtns();
	}

	_validateArrowBtns()
	{
		if (!this._fStepArrowsView_srsv)
		{
			return;
		}

		let lIsDownStepEnabled_bl = false;
		let lIsUpStepEnabled_bl = false;
		let lValue_num = this.multiplierValue;
		
		if (lValue_num !== undefined && lValue_num > this._fBetsInfo_bsi.minAutoEjectMultiplier)
		{
			lIsDownStepEnabled_bl = this.enabled;
		}

		if (lValue_num === undefined || !this._fBetsInfo_bsi.isMaxAutoEjectMultiplierDefined || lValue_num < this._fBetsInfo_bsi.maxAutoEjectMultiplier)
		{
			lIsUpStepEnabled_bl = this.enabled;
		}

		this._fStepArrowsView_srsv.updateEnableState(lIsDownStepEnabled_bl, lIsUpStepEnabled_bl);
	}

	get multiplierStepValue()
	{
		return 0.5;
	}

	_onDownStep()
	{
		let lNewValue_num;
		let lMultiplierNotDefined_bl = this.multiplierValue === undefined || isNaN(this.multiplierValue);
		if (lMultiplierNotDefined_bl)
		{
			lNewValue_num = 1.01;
		}
		else
		{
			lNewValue_num = this.multiplierValue - this.multiplierStepValue;
			if (lNewValue_num <= 1.01) lNewValue_num = 1.01;
		}
		
		this.multiplierValue = lNewValue_num;
	}

	_onUpStep()
	{
		let lNewValue_num;
		let lMultiplierNotDefined_bl = this.multiplierValue === undefined || isNaN(this.multiplierValue);
		if (lMultiplierNotDefined_bl)
		{
			lNewValue_num = 1.01;
		}
		else
		{
			lNewValue_num = this.multiplierValue + this.multiplierStepValue;
		}
		
		this.multiplierValue = lNewValue_num;
	}
	
	_getTextEditFormat()
	{
		return {
			fontColor: 0xffffff,
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 16,
			width: 200,
			height: 20,
			selectionColor: 0x9d5525,
			textAlign: "left",
			maxTextLength: 13,
			borderWidth: 1,
			padding: 1,
			acceptableChars: "0123456789.",
			clipOnFocusLost: false
		}
	}
}

export default MultiplyerEditView;