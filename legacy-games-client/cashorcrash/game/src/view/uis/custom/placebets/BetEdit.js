import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import BetInputField from '../../../../ui/BetInputField';
import CustomKeyboardInputTextField from './CustomKeyboardInputTextField';
import StepArrowsView from './StepArrowsView';

class BetEdit extends Sprite {
	get enabled() {
		return this._fIsEnabled_bl;
	}

	get isValueCorrect() {
		return this._fIsValueCorrect_bl;
	}

	set enabled(value) {
		if (this._fIsEnabled_bl === !!value) {
			return;
		}

		this._fIsEnabled_bl = !!value;

		if (this._fIsEnabled_bl) {
			this._fBetEdit_tf.enabled = true;
			this._validateArrowBtns();

		}
		else {
			this._fBetEdit_tf.enabled = false;
			this._validateArrowBtns();
		}
	}

	set arrowsXOffset(aXOffset_num) {
		this._fArrowsXOffset = aXOffset_num;
	}

	get arrowsXOffset() {
		return this._fArrowsXOffset;
	}

	updateLayout() {
		this._updateLayoutSettings();
	}

	unfocus() {
		this._fBetEdit_tf.hasFocus && this._fBetEdit_tf.autoBlur();
	}

	//INIT...
	constructor() {
		super();

		this._fCurrentAppliedBet_num = 1;
		this._fBetEdit_tf = null;
		this._fIsEnabled_bl = false;

		this._fBetsInfo_bsi = APP.gameController.gameplayController.gamePlayersController.betsController.info;

		this._fBetEdit_tf = this.addChild(this._generateInputField());
		this._fBetEdit_tf.on(BetInputField.EVENT_ON_VALUE_CHANGED, this._onBetEditValueChanged, this);
		this._fBetEdit_tf.on(BetInputField.EVENT_ON_BLUR, this._onBetEditFocusLost, this);
		this._fBetEdit_tf.on(BetInputField.EVENT_ON_FOCUS, this._onBetEditFocus, this);
		this._fBetEdit_tf.enabled = false;

		this._fStepArrowsView_srsv = this.addChild(new StepArrowsView);
		this._fStepArrowsView_srsv.on(StepArrowsView.EVENT_ON_DOWN_STEP, this._onDownStep, this);
		this._fStepArrowsView_srsv.on(StepArrowsView.EVENT_ON_UP_STEP, this._onUpStep, this);

		this._updateLayoutSettings();

		this.setBetValue(this._fBetsInfo_bsi.betMinimalCentsCount);

		this.enabled = true;

		this._fFocusOnScrollableContainer = null;

		this._fArrowsXOffset = null;
	}

	_generateInputField() {
		let lInputConfig_obj = this._getTextEditFormat();
		let lInput_tf = APP.isMobile ? new CustomKeyboardInputTextField(lInputConfig_obj) : new BetInputField(lInputConfig_obj);

		return lInput_tf
	}
	//...INIT

	_updateLayoutSettings() {
		this._fStepArrowsView_srsv.updateLayout();

		this._updateBetPositionSettings();
	}

	_updateBetPositionSettings() {
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;

		let lBetEditX_num = 0;
		let lBetEditY_num = lIsPortraitMode_bl ? 10 : 10;
		this._fBetEdit_tf.position.set(lBetEditX_num, lBetEditY_num);

		let lInputAreaRightBorder_num = lIsPortraitMode_bl ? 200 : 90;
		let inputData = this._fBetEdit_tf.getParams();
		inputData.width = ~~((lInputAreaRightBorder_num - this._fBetEdit_tf.position.x) / this._fBetEdit_tf.scale.x);
		inputData.fontSize = lIsPortraitMode_bl ? 28 : this._getTextEditFormat().fontSize;
		inputData.height = inputData.fontSize + 4;
		this._fBetEdit_tf.setParams(inputData);

		let lArrowsOffsetX_num = lIsPortraitMode_bl ? 4 : 0;
		let lArrowsX_num = lInputAreaRightBorder_num + this._fStepArrowsView_srsv.getBounds().width / 2 + lArrowsOffsetX_num;
		if (this.arrowsXOffset) {
			lArrowsX_num -= this.arrowsXOffset;
		}
		let lArrowsY_num = lIsPortraitMode_bl ? 9 : 4;
		this._fStepArrowsView_srsv.position.set(lArrowsX_num, lArrowsY_num);

	}

	get currentAppliedBetValue() {
		return this._fCurrentAppliedBet_num;
	}

	get currentEnteredValue() {
		return this._enteredBetCents;
	}

	updateBetLimits() {
		let lMinBetCents_num = this._fBetsInfo_bsi.betMinimalCentsCount;
		let lMaxBetCents_num = this._fBetsInfo_bsi.betMaximalCentsCount;
		let lCurBetCents_num = this.currentEnteredValue;

		if (lCurBetCents_num < lMinBetCents_num) {
			this.setBetValue(lMinBetCents_num);
		}
		else if (lCurBetCents_num > lMaxBetCents_num) {
			this.setBetValue(lMinBetCents_num);
		}
	}

	_setCurrencySymbol() {
		this.setBetValue(this._enteredBetCents);
		this._updateBetPositionSettings();
	}


	_onBetEditFocusLost() {
		let lEnteredBetCents_num = this._enteredBetCents;
		if (!this._fBetsInfo_bsi.isValidBetValue(lEnteredBetCents_num)) {
			let toSetValue = this._fBetsInfo_bsi.betMinimalCentsCount;
			if (lEnteredBetCents_num > this._fBetsInfo_bsi.betMaximalCentsCount) {
				toSetValue = this._fBetsInfo_bsi.betMaximalCentsCount;
			}
			this._fBetEdit_tf.setValue(APP.currencyInfo.i_formatNumber(toSetValue) + "");
			this._fBetEdit_tf.updateFontColor(0xFFFFFF);
			this._fCurrentAppliedBet_num = this._fBetsInfo_bsi.betMinimalCentsCount;
			this._validateArrowBtns();
		}

		this.setBetValue(this._enteredBetCents, false);

	}

	_onBetEditFocus() {
		this.setBetValue(this._enteredBetCents, true);
	}

	_onBetEditValueChanged() {
		let lEnteredBetCents_num = this._enteredBetCents;

		if (this._fBetsInfo_bsi.isValidBetValue(lEnteredBetCents_num)) {
			this._fBetEdit_tf.updateFontColor(0xFFFFFF);
			this._fCurrentAppliedBet_num = lEnteredBetCents_num;
		}
		else {
			this._fBetEdit_tf.updateFontColor(0xFF0000);
		}

		this._validateArrowBtns();
	}

	get _enteredBetCents() {
		let lEnteredValue_str = this._fBetEdit_tf.getValue();
		//hotfix...
		//APP.currencyInfo.i_getValueNumber crashes on aValue_str.match(lCurrencyRegExp_re), because it does not take into account thousands separator
		//as a result method returns NaN and it's not possible to place more then 999
		lEnteredValue_str = lEnteredValue_str.replace(/,/g, '');
		lEnteredValue_str = lEnteredValue_str.replace(/MC/g, '');
		lEnteredValue_str = lEnteredValue_str.replace(/QC/g, '');
		lEnteredValue_str = lEnteredValue_str.replace(/PM/g, '');
		lEnteredValue_str = lEnteredValue_str.replace(/USD/g, '');
		lEnteredValue_str = lEnteredValue_str.replace('$', '');
		//...hotfix
		let lCurrencyDecimalFactor_num = Math.pow(10, APP.currencyInfo.i_getCurrencyDecimalPrecision());
		let lEnteredBetCents_num = 0;
		if (isNaN(lEnteredValue_str)) {
			lEnteredBetCents_num = +(APP.currencyInfo.i_getValueNumber(lEnteredValue_str) * lCurrencyDecimalFactor_num).toFixed(0);
		}
		else {
			lEnteredBetCents_num = +(((+lEnteredValue_str) * lCurrencyDecimalFactor_num).toFixed(0));
		}

		return lEnteredBetCents_num;
	}

	setBetValue(aBetValue_num, aOptApplyFilter_bl) {
		if (aOptApplyFilter_bl === undefined) {
			aOptApplyFilter_bl = this._fBetEdit_tf.hasFocus;
		}

		let lBetInCents_num = +aBetValue_num;

		let lIsValidValue_bl = this._fBetsInfo_bsi.isValidBetValue(lBetInCents_num);

		this._fCurrentAppliedBet_num = lBetInCents_num;

		if (lIsValidValue_bl) {
			let lBetValue;
			if (APP.appParamsInfo.restrictCoinFractionLength !== undefined) {
				lBetValue = APP.currencyInfo.i_formatNumber(lBetInCents_num, true, APP.isBattlegroundGame, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
			}
			else {
				lBetValue = APP.currencyInfo.i_formatNumber(lBetInCents_num, true, APP.isBattlegroundGame, 2);
			}

			/*if (!aOptApplyFilter_bl)
			{
				
				lBetValue = APP.currencyInfo.i_formatString(lBetValue);
			}*/

			this._fBetEdit_tf.setValue(lBetValue, aOptApplyFilter_bl);
		}
		this._validateArrowBtns()
	}

	_validateArrowBtns() {
		let lIsDownStepEnabled_bl = false;
		let lIsUpStepEnabled_bl = false;
		let lValue_num = this._enteredBetCents;
		if (lValue_num > this._fBetsInfo_bsi.betMinimalCentsCount) {
			lIsDownStepEnabled_bl = this.enabled;
		}

		if (lValue_num < this._fBetsInfo_bsi.betMaximalCentsCount) {
			lIsUpStepEnabled_bl = this.enabled;
		}

		this._fStepArrowsView_srsv.updateEnableState(lIsDownStepEnabled_bl, lIsUpStepEnabled_bl);
	}

	_onDownStep() {
		let lNewValue_num = this._fCurrentAppliedBet_num - this._fBetsInfo_bsi.centsCountPerBetStep;
		if (lNewValue_num < this._fBetsInfo_bsi.betMinimalCentsCount) {
			lNewValue_num = this._fBetsInfo_bsi.betMinimalCentsCount;
		}
		this.setBetValue(lNewValue_num);
	}

	_onUpStep() {
		let lNewValue_num = this._fCurrentAppliedBet_num + this._fBetsInfo_bsi.centsCountPerBetStep;
		if (lNewValue_num > this._fBetsInfo_bsi.betMaximalCentsCount) {
			lNewValue_num = this._fBetsInfo_bsi.betMaximalCentsCount;
		}
		this.setBetValue(lNewValue_num);
	}

	_getTextEditFormat() {
		return {
			fontColor: 0xffffff,
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 16,
			width: 120,
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

export default BetEdit;