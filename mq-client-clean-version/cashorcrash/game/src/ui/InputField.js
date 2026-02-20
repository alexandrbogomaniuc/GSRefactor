import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import InputText from '../ui/InputText';

class InputField extends InputText
{
	static get EVENT_ON_BLUR()				{ return InputText.EVENT_ON_BLUR; }
	static get EVENT_ON_FOCUS()				{ return InputText.EVENT_ON_FOCUS; }
	static get EVENT_ON_VALUE_CHANGED()		{ return InputText.EVENT_ON_VALUE_CHANGED; }

	constructor(background = null, config = null)
	{
		var config = config || {
			backgroundImage: background,
			fontColor: 0xffffff,
			fontFamily: "fnt_nm_barlow",
			fontSize: 12,
			width: 100,
			height: 58,
			selectionColor: 0x9d5525,
			textAlign: "center",
			offsetY: -4,
			selectYShift: -1,
			maxTextLength: 17,
			acceptableChars: "abcdefghijklmnopqrstuvwxyz0123456789"
		};

		super(config);
	}
}

export default InputField;