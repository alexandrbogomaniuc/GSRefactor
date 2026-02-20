import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';


const VALUE_TEMPLATE = "/VALUE/";
const VALUE_UNDEFINED = "-";


class RoundResultTextIndicatorView extends Sprite
{
	get value()
	{
		return this._fNumberValue_num || 0;
	}

	get width()
	{
		return this._fTextField_tf && this._fTextField_tf.getBounds().width;
	}
	
	set value(aValue_num)
	{
		this._fNumberValue_num = aValue_num;
		this._fTextField_tf.text = aValue_num !== undefined ? (this._fUseValueTemplate_bl ? this._fTextTemplate_str.replace(VALUE_TEMPLATE, aValue_num) : aValue_num) : VALUE_UNDEFINED;
	}

	set maxWidth(aValue_num)
	{
		this._fTextField_tf.maxWidth = aValue_num;
	}

	constructor(aBaseTextField_str, aUseValueTemplate_bl, aXAnchor_num = 0, aYAnchor_num = 0)
	{
		super();

		this._fTextField_tf = aBaseTextField_str;
		this._fTextTemplate_str = aBaseTextField_str.text;
		this._fUseValueTemplate_bl = aUseValueTemplate_bl;
		this._fNumberValue_num = undefined;

		this._fTextField_tf.anchor.set(aXAnchor_num, aYAnchor_num);
		this._fTextField_tf.text = VALUE_UNDEFINED;
		this.addChild(this._fTextField_tf);
	}

	destroy()
	{
		this._fTextField_tf && this._fTextField_tf.destroy();
		this._fTextField_tf = null;

		this._fTextTemplate_str = undefined;
		this._fNumberValue_num = undefined;
		this._fUseValueTemplate_bl = false;

		super.destroy();
	}
}

export default RoundResultTextIndicatorView;