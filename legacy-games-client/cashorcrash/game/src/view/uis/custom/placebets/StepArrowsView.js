import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameButton from '../../../../ui/GameButton';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Button from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { FRAME_RATE } from '../../../../config/Constants';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

const DIRECTIONS = {DOWN: "DOWN", UP: "UP"};
const MAX_CLICK_INTERVAL = 200;

class StepArrowsView extends Sprite
{
	static get EVENT_ON_DOWN_STEP() 	{ return 'EVENT_ON_DOWN_STEP' }
	static get EVENT_ON_UP_STEP() 		{ return 'EVENT_ON_UP_STEP' }

	updateLayout()
	{
		this._updateLayoutSettings();
	}

	updateEnableState(aDownStepEnabled_bl, aUpStepEnabled_bl)
	{
		this._fDownArrow_gb.enabled = aDownStepEnabled_bl;
		this._fUpArrow_gb.enabled = aUpStepEnabled_bl;
	}

	//INIT...
	constructor()
	{
		super();

		this._fDirectionType_str = undefined;
		
		let lDownArrow_gb = this._fDownArrow_gb = this.addChild(new GameButton(new PIXI.Graphics(), undefined, true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lDownArrow_gb.grayFilter.greyscale(0.15, false);
		lDownArrow_gb.on(Button.EVENT_ON_HOLDED, this._onDownArrowBtnHolded, this);
		lDownArrow_gb.on(Button.EVENT_ON_UNHOLDED, this._onDownArrowBtnUnHolded, this);
		
		let lUpArrow_gb = this._fUpArrow_gb = this.addChild(new GameButton(new PIXI.Graphics(), undefined, true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lUpArrow_gb.grayFilter.greyscale(0.15, false);
		lUpArrow_gb.on(Button.EVENT_ON_HOLDED, this._onUpArrowBtnHolded, this);
		lUpArrow_gb.on(Button.EVENT_ON_UNHOLDED, this._onUpArrowBtnUnHolded, this);
		
		this._updateLayoutSettings();
	}

	_updateLayoutSettings()
	{
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;

		let lStepArrowWidth_num = lIsPortraitMode_bl ? 42 : 25;
		let lStepArrowHeight_num = lIsPortraitMode_bl ? 38 : lStepArrowWidth_num;
		let lArrowOffsetX_num = lIsPortraitMode_bl ? 3.5 : 2;
		let lArrowFontSize_num = lIsPortraitMode_bl ? 22 : this._getWhiteTextFormat.fontSize;

		let lUpArrowBase_gr = new PIXI.Graphics();
		lUpArrowBase_gr.lineStyle(1, 0x53555f).beginFill(0x000000, 0.01).drawRoundedRect(-lStepArrowWidth_num/2, -lStepArrowHeight_num/2, lStepArrowWidth_num, lStepArrowHeight_num, 5).endFill();
		this._fUpArrow_gb.position.set(lStepArrowWidth_num/2+lArrowOffsetX_num, 0);
		this._fUpArrow_gb.updateCaptionView(this._generateUpArrowLabel());
		this._fUpArrow_gb.updateBase(lUpArrowBase_gr);

		let lDownArrowBase_gr = new PIXI.Graphics();
		lDownArrowBase_gr.lineStyle(1, 0x53555f).beginFill(0x000000, 0.01).drawRoundedRect(-lStepArrowWidth_num/2, -lStepArrowHeight_num/2, lStepArrowWidth_num, lStepArrowHeight_num, 5).endFill();
		this._fDownArrow_gb.position.set(-lStepArrowWidth_num/2-lArrowOffsetX_num, 0);
		this._fDownArrow_gb.updateCaptionView(this._generateDownArrowLabel());
		this._fDownArrow_gb.updateBase(lDownArrowBase_gr);
	}

	_generateDownArrowLabel()
	{	
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;
		let lCaption_ta = I18.generateNewCTranslatableAsset(lIsPortraitMode_bl ? "TAButtonDownArrowLabelPortrait" : "TAButtonDownArrowLabel");
		lCaption_ta.rotation = Math.PI/2;
		return lCaption_ta;
	}

	_generateUpArrowLabel()
	{	
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;
		let lCaption_ta = I18.generateNewCTranslatableAsset(lIsPortraitMode_bl ? "TAButtonUpArrowLabelPortrait" : "TAButtonUpArrowLabel");
		lCaption_ta.rotation = Math.PI/2;
		return lCaption_ta;
	}

	_getWhiteTextFormat()
	{
		return {
			fontFamily: "fnt_nm_roboto_medium",
			fontSize: 16,
			align: "left",
			fill: 0xFFFFFF
		};
	}

	_onDownArrowBtnHolded(event)
	{
		this._fDirectionType_str = DIRECTIONS.DOWN;

		this._emitStep();

		this._startStepsSequence(8*FRAME_RATE);
	}

	_onDownArrowBtnUnHolded(event)
	{
		this._resetStepsSequence();
		this._resetDirectionType();
	}

	_onUpArrowBtnHolded(event)
	{
		this._fDirectionType_str = DIRECTIONS.UP;

		this._emitStep();

		this._startStepsSequence(8*FRAME_RATE);
	}

	_onUpArrowBtnUnHolded(event)
	{
		this._resetStepsSequence();
		this._resetDirectionType();
	}

	_resetDirectionType()
	{
		this._fDirectionType_str = undefined;
	}

	_startStepsSequence(aDuration_num=10*FRAME_RATE)
	{
		this._resetStepsSequence();

		let lSeq = [
			{tweens: [],	duration: aDuration_num, onfinish: () => { this._onSeqCycleCompleted(); } },
		];

		Sequence.start(this, lSeq);
	}

	_onSeqCycleCompleted()
	{
		this._emitStep();

		this._startStepsSequence(2*FRAME_RATE);
	}

	_resetStepsSequence()
	{
		Sequence.destroy(Sequence.findByTarget(this));
	}

	_emitStep()
	{
		let lEventType_str = undefined;
		switch (this._fDirectionType_str)
		{
			case DIRECTIONS.DOWN:
				lEventType_str = StepArrowsView.EVENT_ON_DOWN_STEP;
				break;
			case DIRECTIONS.UP:
				lEventType_str = StepArrowsView.EVENT_ON_UP_STEP;
				break;
		}

		if (lEventType_str !== undefined)
		{
			this.emit(lEventType_str);
		}
	}
}

export default StepArrowsView;