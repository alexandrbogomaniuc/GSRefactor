import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import NumberValueFormat from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';

class BigWinPayoutView extends Sprite
{
	static get EVENT_ON_VALUE_COUNTING_COMPLETED()				{return "onValueCountingCompleted";}

	showPayout(aPayoutValue_num, aCountingDuration_num=0)
	{
		this._fPayoutValue_num = aPayoutValue_num;
		this._fPayoutValue_str = aPayoutValue_num;
		
		this._fCountingDuration_num = ~~aCountingDuration_num;
		
		this._fCurPayout_obj.value = (this._fCountingDuration_num > 0) ? 0 : this._fPayoutValue_num;

		this._formatValues();
		this._updateValues();
		this._alignValue();

		if (this._fCountingDuration_num > 0)
		{
			this._startCounting();
		}
		else
		{
			this._onCountingCompleted();
		}
	}

	updatePayout(aPayoutValue_num)
	{
		Sequence.destroy(Sequence.findByTarget(this._fCurPayout_obj));

		this._fPayoutValue_str = aPayoutValue_num;
		this._fCurPayout_obj.value = aPayoutValue_num;

		this._formatValues();
		this._updateValues();
		this._alignValue();
	}

	constructor()
	{
		super();

		this._fPayoutValue_num = 0;
		this._fPayoutValue_str = 0;
		this._fDecimalValue_str = null;
		this._fMainText_tf = null;
		this._fDecimicalText_tf = null;
		this._fContainer_sprt = null;

		this._fCountingDuration_num = 0;
		
		this._fCurPayout_obj = {};
		this._fCurPayout_obj.value = 0;

		this._initField();
	}

	get Container_sprt()
	{
		return this._fContainer_sprt;
	}

	_formatValues()
	{
		this._fPayoutValue_str = APP.currencyInfo.i_formatNumber(this._fCurPayout_obj.value, false);
		let lDelimIndex_int = this._fPayoutValue_str.indexOf('.');

		if (lDelimIndex_int !== -1) // not BG mode
		{
			this._fDecimalValue_str = this._fPayoutValue_str.substring(lDelimIndex_int).substr(0, 3);
			this._fPayoutValue_str = this._fPayoutValue_str.substring(0, lDelimIndex_int);
		}
	}

	_initField()
	{
		this._fContainer_sprt = this.addChild(new Sprite());

		this._generateMainField();
		this._generateDecimicalField();

		this._alignValue();
	}

	_alignValue()
	{
		let lMainWidth_num = this._fMainText_tf.getBounds().width;

		this._fDecimicalText_tf.position.set(lMainWidth_num, -10);

		let lContainerWidth_num = this._fContainer_sprt.getBounds().width;
		this._fContainer_sprt.position.x = -lContainerWidth_num/2;
	}

	_generateMainField()
	{
		this._fMainText_tf = this._fContainer_sprt.addChild(new TextField(this._getStyle(84)));
		this._fMainText_tf.text = this._fPayoutValue_str;
		this._fMainText_tf.anchor.set(0, 0.5);
	}

	_generateDecimicalField()
	{
		this._fDecimicalText_tf = this._fContainer_sprt.addChild(new TextField(this._getStyle(57)));
		this._fDecimicalText_tf.text = this._fDecimalValue_str;
		this._fDecimicalText_tf.anchor.set(0, 0.5);
	}

	_updateValues()
	{
		this._fMainText_tf.text = this._fPayoutValue_str;
		this._fDecimicalText_tf.text = this._fDecimalValue_str;
	}

	_getStyle(aFontSize_num)
	{
		let lFontFamily_str = "fnt_nm_arial_currency";
		if (APP.fonts.isGlyphsSupported("fnt_nm_amerika", APP.currencyInfo.i_getCurrencySymbol()))
		{
			lFontFamily_str = "fnt_nm_amerika";
		}

		return {
			fontFamily: lFontFamily_str,
			fontSize: aFontSize_num,
			align: "left",
			fill: [0xffffff, 0xfdfdfd, 0xb5b5b5, 0x949494],
			fillGradientType: PIXI.TEXT_GRADIENT.LINEAR_VERTICAL,
			fillGradientStops: [0.04, 0.43, 0.6, 0.96],
			stroke: 0x5b5b5b,
			strokeThickness: 2,
			dropShadow: true,
			dropShadowColor: 0x242323,
			dropShadowAngle: 0.78,
			dropShadowDistance: 2,
			dropShadowBlur: 2,
			letterSpacing: 1.7,
			padding: 8
		};
	}

	_startCounting()
	{
		let l_seq = [
			{tweens: [	{prop: "value", to: this._fPayoutValue_num, onchange: ()=> {this._onCountingValueUpdate();} } ],	duration: this._fCountingDuration_num }
		];

		Sequence.start(this._fCurPayout_obj, l_seq);
	}

	_onCountingValueUpdate()
	{
		let lPrevSceleX = this.scale.x;
		let lPrevSceleY = this.scale.y;

		this.scale.set(1, 1);

		this._formatValues();
		this._updateValues();
		this._alignValue();

		this.scale.set(lPrevSceleX, lPrevSceleY);

		if (this._fCurPayout_obj.value == this._fPayoutValue_num)
		{
			this._onCountingCompleted();
		}
	}

	_onCountingCompleted()
	{
		this.emit(BigWinPayoutView.EVENT_ON_VALUE_COUNTING_COMPLETED);
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this._fCurPayout_obj));
		this._fCurPayout_obj = null;

		super.destroy();

		this._fPayoutValue_str = null;
		this._fPayoutValue_num = null;
		this._fDecimalValue_str = null;
		this._fMainText_tf = null;
		this._fDecimicalText_tf = null;
		this._fContainer_sprt = null;
		this._fCountingDuration_num = null;
	}
}

export default BigWinPayoutView;