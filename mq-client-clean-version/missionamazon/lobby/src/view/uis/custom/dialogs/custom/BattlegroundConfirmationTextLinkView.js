import GUIndicatorView from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/view/ui/GUIndicatorView';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import NumberValueFormat from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import * as FEATURES from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class BattlegroundConfirmationTextLinkView extends GUIndicatorView
{
	static get EVENT_BATTLEGROUND_RULES_CLICKED () { return "EVENT_BATTLEGROUND_RULES_CLICKED" };

	getBounds()
	{
		return this._fValueContainer_sprt.getBounds();
	}

	//INIT...
	_initIndicatorView()
	{
		this._fLink_tf = null;
		this._fValueContainer_sprt.position.set(0, -4);

		super._initIndicatorView();

		if (FEATURES.IE)
		{
			this._fValueContainer_sprt.pivot.set(-4, -2);
		}
	}
	//...INIT

	//VALUE...
	_initValueView()
	{
		super._initValueView();
		this._fValueView_tf.anchor.set(0.5, 0.5);

		this._initLinkIndicatorPart();
	}

	_getValueTextFormat()
	{
		let format = {
			fontFamily: "fnt_nm_barlow_regular",
			fontSize: 14,
			fill: 0xFFFFFF,
		};
		return format;
	}

	_getValueMaxWidth()
	{
		return 0;
	}
	
	_formatValue(aValue_num)
	{
		return APP.currencyInfo.i_formatNumber(aValue_num, false);
	}

	get _maxWidth()
	{
		return 310;
	}

	//override
	_applyFormattedValue()
	{
		this._fValueView_tf.text = I18.getTranslatableAssetDescriptor("TABattlegroundYouAgreeToGameRulesPart1")._textDescriptor._content.text + " ";
		this._fLink_tf.text = I18.getTranslatableAssetDescriptor("TABattlegroundYouAgreeToGameRulesPart2")._textDescriptor._content.text;
		this._onValueChanged();
	}

	_onValueChanged()
	{
		if (this._fLink_tf)
		{
			let lValueWidth_num = this._fValueView_tf.width;
			this._fLink_tf.position.x = lValueWidth_num * 0.5;
		}

		this._fValueContainer_sprt.scale.x = 1;
		let lWidth_num = this._fValueContainer_sprt.getBounds().width;

		if (lWidth_num > this._maxWidth)
		{
			this._fValueContainer_sprt.scale.x = this._maxWidth / lWidth_num;
		}
		super._onValueChanged();

		if (this._fLink_tf)
		{
			let lOffsetX_num = this._fLink_tf.width / 2;

			this._fLink_tf.position.x -= lOffsetX_num;
			this._fValueView_tf.position.x -= lOffsetX_num;

			//UNDERLINE LINK...
			let lUnderline_g = this._fValueContainer_sprt.addChild(new PIXI.Graphics());
			let lWidth_num = this._fLink_tf.width;
			let lHeight_num = this._fLink_tf.height;
			lUnderline_g.beginFill(0xfccc32).drawRect(
				this._fLink_tf.position.x,
				this._fLink_tf.position.y + lHeight_num / 2 - 1,
				lWidth_num,
				1).endFill();
			//...UNDERLINE LINK
		}
		
	}
	//...VALUE

	//CURRENCY SYMBOL...
	_initLinkIndicatorPart()
	{

		let l_tf = new TextField(this._getLinkTextFormat());

		l_tf.anchor.set(0, 0.5);
		l_tf.text = "";


		this._fLink_tf = this._fValueContainer_sprt.addChild(l_tf);
		this._fLink_tf.interactive = true;
		this._fLink_tf.buttonMode = true;
		this._fLink_tf.defaultCursor = "crosshair";
		this._fLink_tf.on("click", this._onLinkClick, this);
		this._fLink_tf.on("touchend", this._onLinkClick, this);
		this._fValueContainer_sprt.pivot.set(0, 0);
		return l_tf;
	}


	_isInsideLinkArea(aX_num, aY_num)
	{
		let lWidth_num = this._fLink_tf.width;
		let lHeight_num = this._fLink_tf.height;

		return(
			aX_num >= this._fLink_tf.position.x * this._fValueContainer_sprt.scale.x &&
			aX_num <= (this._fLink_tf.position.x + lWidth_num) * this._fValueContainer_sprt.scale.x &&
			aY_num >= -lHeight_num / 2 &&
			aY_num <= this._fLink_tf.position.y + lHeight_num / 2);
	}

	_onLinkClick(e)
	{
		this.emit(BattlegroundConfirmationTextLinkView.EVENT_BATTLEGROUND_RULES_CLICKED, this);
	}

	_getLinkTextFormat()
	{
		let format = {
			fontFamily: "fnt_nm_barlow_regular",
			fontSize: 14,
			fill: 0xfccc32,
		};
		return format;
	}
	//...CURRENCY SYMBOL
}

export default BattlegroundConfirmationTextLinkView;