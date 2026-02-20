import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GameButton from '../../../../ui/GameButton';
import Button from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import ScreenKeyboardSquareButton from './ScreenKeyboardSquareButton';
import ScreenKeyboardControlButton from './ScreenKeyboardControlButton';
import PointerSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/PointerSprite';

const SQUARE_BUTTONS_X_POS_ARR = [42, 117, 190];
const SQUARE_BUTTONS_Y_START_VALUE = 70;

var _INSTANCE = null;

class ScreenKeyboard extends PointerSprite
{
    static get EVENT_ON_KEY_PRESSED() { return "EVENT_ON_KEY_PRESSED" }

    static get KEYBOARD_TYPES() 
    {
        return {
                NUMERIC:    "NUMERIC",
                TEXT:       "TEXT"
        }
    }

    static get instance()
    {
        return _INSTANCE || (_INSTANCE = new ScreenKeyboard());
    }

    get keyboardType()
    {
        return this._fKeyboardType_str;
    }

    constructor()
    {
        super();

        if (!!_INSTANCE)
        {
            throw new Error ("ScreenKeyboard instance already exists!");
            return;
        }
        
        this._fButtonsContainer_bc = null;
        this._fButtonsContainerBase_gr = null;
        this._fRowNow_num = 1;
        this._fButtonYOffset_num = -35;
        this._fButtonXOffset_num = -16;
        this._fDigitButtonBaseWidth_num = 65;
        this._fDigitButtonBaseHeight_num = 62;
        this._fDigitButtonsPositionX_num = 26;
        this._fDigitButtonsPositionY_num = 6;

        this._fKeyboardType_str = ScreenKeyboard.KEYBOARD_TYPES.NUMERIC;
        
        this.__init();
    }

    __init()
    {
        let lButtonsContainer_bc = this._fButtonsContainer_bc = this.addChild( new Sprite );
        
        lButtonsContainer_bc.position.set(0, 40);
        let lButtonsContainerBase_gr = this._fButtonsContainerBase_gr = new PIXI.Graphics();

        let lButtonsContainerXOffset = 0;
        let lButtonsContainerYOffset = 0;
        let lButtonsContainerBaseWidth_num = 230;
        let lButtonsContainerBaseHeight_num = 343;
        lButtonsContainerBase_gr.lineStyle(1, 0xffffff, 1).beginFill(0x171b2d, 1).drawRoundedRect(
            lButtonsContainerXOffset, 
            lButtonsContainerYOffset,
            lButtonsContainerBaseWidth_num,
            lButtonsContainerBaseHeight_num,
            3);

        this.updateBase(lButtonsContainerBase_gr);

        this.on("pointerclick", this._onBaseClicked, this);

        this._getButtonsRows();
        this._getControllButtonsRow();
    }

    _getButtonsRows()
    {
        for (let i = 1; i <= 12; i++)
        {
            if (i === 10)
            {
                this._drawBackSpaceFigure();
            }
            else
            {
                let lBase = this._getSquareButtonBase();
                let lSqaureButton_gb = this._fButtonsContainer_bc.addChild(new ScreenKeyboardSquareButton(lBase, `${i}`, true));
                let lButtonPosition_arr = this._getSquareButtonPosition(i);
                lSqaureButton_gb.position.set(lButtonPosition_arr[0], lButtonPosition_arr[1]);
                lSqaureButton_gb.on("pointerclick", this._onKeyboardButtonClicked, this);
            }
        }
    }

    _getControllButtonsRow()
    {
        let lButtonWidth_num = 103;
        let lButtonHeight_num = 38; 
        let lCancelButtonBase_gr = new PIXI.Graphics().beginFill(0xc12d26).drawRoundedRect(
            -lButtonWidth_num/2,
            -lButtonHeight_num/2, 
            lButtonWidth_num,
            lButtonHeight_num,
            3).endFill();
        let lCancelButton_gb = this._fButtonsContainer_bc.addChild(new ScreenKeyboardControlButton("CANCEL", lCancelButtonBase_gr, "TACancelKeyboardButton", true));
        lCancelButton_gb.position.set(62, 272);
        lCancelButton_gb.on("pointerclick", this._onKeyboardButtonClicked, this);

        let lOkButtonBase_gr = new PIXI.Graphics().beginFill(0xefc033).drawRoundedRect(
            -lButtonWidth_num/2,
            -lButtonHeight_num/2, 
            lButtonWidth_num,
            lButtonHeight_num,
            3).endFill();
        let lOkayButton_gb = this._fButtonsContainer_bc.addChild(new ScreenKeyboardControlButton("OK", lOkButtonBase_gr, "TAOKKeyboardButton", true));
        lOkayButton_gb.position.set(170, 272);
        lOkayButton_gb.on("pointerclick", this._onKeyboardButtonClicked, this);
    }

    _getSquareButtonBase()
    {
        let lSquareButtonBase_gr = new PIXI.Graphics().lineStyle(1, 0x3f445b, 1).beginFill(0x272e4e).drawRoundedRect(
            -33,
            -29, 
            this._fDigitButtonBaseWidth_num,
            this._fDigitButtonBaseHeight_num,
            4).endFill();


        return lSquareButtonBase_gr;
    }

    _onKeyboardButtonClicked(e)
    {
        e.stopPropagation();
        //DEBUGGING CODE BLOCK...
        let lTargetButton_gb = e.target;

        this.emit(ScreenKeyboard.EVENT_ON_KEY_PRESSED, {keyCode: lTargetButton_gb.keyCode, keyValue: lTargetButton_gb.keyValue});
        //...DEBUGGING CODE BLOCK
    }

    _onBaseClicked(e)
    {
        e.stopPropagation();
    }

    _drawBackSpaceFigure()
    {
        let lLineToDraw_gr = new PIXI.Graphics().
            lineStyle(1, 0xffffff, 1).
            beginFill(0x272e4e).
            drawPolygon(
                [
                    -35, 5,
                    -25, -14,
                    12, -14,
                    12, 20,
                    -24, 20,
                    -35, 5
                    ]);

        let lSqaureButtonBase_gr = new PIXI.Graphics().lineStyle(1, 0x3f445b, 1).beginFill(0x272e4e).drawRoundedRect(
            -43,
            -29, 
            this._fDigitButtonBaseWidth_num,
            this._fDigitButtonBaseHeight_num,
            4).endFill();
        lSqaureButtonBase_gr.addChild(lLineToDraw_gr);
        let lSqaureButton_gb =  this._fButtonsContainer_bc.addChild(new ScreenKeyboardSquareButton(lSqaureButtonBase_gr, '10', true));
        let lButtonPosition_arr = this._getSquareButtonPosition(10);
        lSqaureButton_gb.position.set(lButtonPosition_arr[0] + 10, lButtonPosition_arr[1]);
        lSqaureButton_gb.on("pointerclick", this._onKeyboardButtonClicked, this);
    }

    _getSquareButtonPosition(aButtonNumber_num)
    {
        let lButtonNum = aButtonNumber_num;
        let lButtonPositionX_num;
        if (lButtonNum === 1 ||  lButtonNum === 4 || lButtonNum === 7 || lButtonNum === 10)
        {
            lButtonPositionX_num = SQUARE_BUTTONS_X_POS_ARR[0];
        }
        else if (lButtonNum === 2 || lButtonNum === 5 || lButtonNum === 8 || lButtonNum === 11)
        {
            lButtonPositionX_num = SQUARE_BUTTONS_X_POS_ARR[1];
        }
        else
        {
            lButtonPositionX_num = SQUARE_BUTTONS_X_POS_ARR[2];
        }

        let lButtonPositionY_num;

        if (lButtonNum >= 1 && lButtonNum <= 3)
        {
            lButtonPositionY_num = 0;
        }
        else if (lButtonNum >= 4 && lButtonNum <= 6)
        {
            lButtonPositionY_num = SQUARE_BUTTONS_Y_START_VALUE;
        }
        else if (lButtonNum >= 7 && lButtonNum <= 9)
        {
            lButtonPositionY_num = SQUARE_BUTTONS_Y_START_VALUE * 2;
        }
        else
        {
            lButtonPositionY_num = SQUARE_BUTTONS_Y_START_VALUE * 3;
        }

        return [lButtonPositionX_num, lButtonPositionY_num];
    }
}

export default ScreenKeyboard;