import TextField from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField";
import GameButton from "../../../../ui/GameButton";
import I18 from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18";

class ScreenKeyboardSquareButton extends GameButton
{
    constructor(aBase, aKeyValue_str, cursorPointer, noDefaultSound, anchorPoint, buttonType, useDefaultGreyscaleForDisabledState)
    {
        super(aBase, undefined, cursorPointer, noDefaultSound, anchorPoint, buttonType, useDefaultGreyscaleForDisabledState);

        this._fKeyVal_str = null;
        this._fKeyCode_num = null;
        
        this.keyValue = aKeyValue_str;
        this._setButtonCaption();
    }

    get keyValue()
    {
        return this._fKeyVal_str;
    }

    get keyCode()
    {
        return this._fKeyCode_num;
    }

    set keyValue(aVal)
    {
        let lValue_str;
        switch (aVal)
        {
            case '10':
                lValue_str = 'X';
                break;
            case '11':
                lValue_str = '0';
                break;
            case '12':
                lValue_str = '.';
                break;
            default:
                lValue_str = aVal;
        }
        this._fKeyVal_str = lValue_str;
        this._fKeyCode_num = lValue_str === 'X' ? 8 : lValue_str.charCodeAt();
    }

    _setButtonCaption()
    {
        let lSquareButtonLabelConfig_obj = this._getTextFieldParams();
        lSquareButtonLabelConfig_obj.fontSize = this._fKeyVal_str === 'X' ? 30 : 48;

        let lSquareButtonLabel_tf = new TextField(lSquareButtonLabelConfig_obj);
        
        switch (this._fKeyVal_str)
        {
            case '1':
                lSquareButtonLabel_tf.position.x = -9;
                break;
            case '7':
                lSquareButtonLabel_tf.position.x = -12;
                break;
            case '.':
                lSquareButtonLabel_tf.position.x = -6;
                break;
            default:
                lSquareButtonLabel_tf.position.x = -14;
        }
        
        lSquareButtonLabel_tf.position.y = this._fKeyVal_str != 'X' ? -25 : -14;
        lSquareButtonLabel_tf.text = this._fKeyVal_str;

        if (this._fKeyVal_str !== 'X')
        {
            this.updateCaptionView(lSquareButtonLabel_tf);
        }
        else
        {
            let lKeyboardResetButtonCaption_ta = I18.generateNewCTranslatableAsset("TAKeyboardResetButtonLabel");
            lKeyboardResetButtonCaption_ta.position.set(-3, 0);
            this.updateCaptionView(lKeyboardResetButtonCaption_ta);
        }
    }

    _getTextFieldParams()
    {
        return {
			fill: 0xffffff,
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 48,
			lineHeight: 57,
            padding: 5
		};
    }
}
export default ScreenKeyboardSquareButton;