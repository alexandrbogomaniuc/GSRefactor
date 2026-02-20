import TextField from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField";
import GameButton from "../../../../ui/GameButton";

class ScreenKeyboardControlButton extends GameButton
{
    constructor(aKeyVal_str, baseAssetName, captionId, cursorPointer, noDefaultSound, anchorPoint, buttonType)
    {
        super(baseAssetName, captionId, cursorPointer, noDefaultSound, anchorPoint, buttonType);
        
        this._fKeyCode_num = null;
        this._fKeyVal_str = null;

        this.keyValue = aKeyVal_str;
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
        this._fKeyVal_str = aVal;
        if (this._fKeyVal_str === 'CANCEL')
        {
            this._fKeyCode_num = 27;
        }
        else if (this._fKeyVal_str === 'OK')
        {
            this._fKeyCode_num = 13;
        }
    }
}

export default ScreenKeyboardControlButton;