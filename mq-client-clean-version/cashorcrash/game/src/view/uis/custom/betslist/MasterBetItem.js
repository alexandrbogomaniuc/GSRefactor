import { APP } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import { Sprite } from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import TextField from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField";
import GameplayInfo from "../../../../model/gameplay/GameplayInfo";
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import AtlasConfig from "../../../../config/AtlasConfig";
import { BitmapText } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasSprite  from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';

const NOT_EJECTED_VALUE = '----';

class MasterBetItem extends Sprite
{
    static get ITEM_HEIGHT ()                                   { return 20; }
    static get ITEM_HEIGHT_PORTRAIT_PLAY_STATE ()               { return 33; }
    static get ITEM_HEIGHT_PORTRAIT_WAIT_STATE ()               { return 24; }
    static get ITEM_INDENT ()                                   { return 13; }
    static get ITEM_MARGIN_RIGHT()                              { return 6; }

    static get MULTIPLIER_MAX_WIDTH() { return 40; }
    static get BET_WIN_MAX_WIDTH() { return 76; } /*minimal required*/

    update(aDistance_num, aTime_num)
    {
        if (aDistance_num === undefined)
        {
            this._fDistance_tf.write(NOT_EJECTED_VALUE);
            this._fEjectedTime_tf.write(NOT_EJECTED_VALUE);
        }
        else
        {
            this._fDistance_tf.write(GameplayInfo.formatMultiplier(aDistance_num));
            this._fEjectedTime_tf.write(aTime_num + 's');
        }
    }

    updateSize(aItemWidth_num, aItemHeight_num)
    {
        if (this._fItemWidth_num === aItemWidth_num && this._fItemHeight_num === aItemHeight_num)
        {
            return;
        }

        this._fItemHeight_num = aItemHeight_num;
        this._fItemWidth_num = aItemWidth_num;

        this._refillBase();
        this._validateTextFields();
    }

    _refillBase()
    {
        let lBase_gr = this._fRegularBase_gr;
        
        lBase_gr.cacheAsBitmap = false;

        lBase_gr.clear();
        lBase_gr.beginFill(0xf4d425, 1).drawRoundedRect(0, 0, this._fItemWidth_num, this._fItemHeight_num, 5).endFill();
    }

    constructor()
    {
        super();

        this._fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset("scorescoreboard_font/scoreboard_font")], [AtlasConfig.ScoreBoardFont], "");

        this._fRegularBase_gr = this.addChild(new PIXI.Graphics);

        let lPlayerName_ta = this._fPlayerNameEjected_ta = this.addChild(new BitmapText(this._fTextures_tx_map, "", 5));
        lPlayerName_ta.position.set(MasterBetItem.ITEM_INDENT, MasterBetItem.ITEM_HEIGHT*0.5);
        lPlayerName_ta.write("YOU");
        lPlayerName_ta.scale.set(0.37, 0.37);  
        lPlayerName_ta.addTint(0x000000);   

        
        let lDistance_tf = this._fDistance_tf = this.addChild(new BitmapText(this._fTextures_tx_map, "", 5));
        lDistance_tf.scale.set(0.37, 0.37);
        lDistance_tf.position.set(110, MasterBetItem.ITEM_HEIGHT - 11);
        lDistance_tf.addTint(0x000000);

        let lEjectedTime_tf = this._fEjectedTime_tf = this.addChild(new BitmapText(this._fTextures_tx_map, "", 5));
        lEjectedTime_tf.scale.set(0.37, 0.37);
        lEjectedTime_tf.position.set(200, MasterBetItem.ITEM_HEIGHT - 11);
        lEjectedTime_tf.addTint(0x000000);

        this._fItemHeight_num = null;
        this._fItemWidth_num = null;
    }

    _validateTextFields()
    {
        /*let lFieldsFontSize_num = 11;
        let lItemHeight_num = this._fItemHeight_num;

        let lNamePostfix_str = '';
        if (lItemHeight_num === MasterBetItem.ITEM_HEIGHT_PORTRAIT_PLAY_STATE)
        {
            lNamePostfix_str = 'Portrait';
            lFieldsFontSize_num = 14;
            this._fDistance_tf.position.y = MasterBetItem.ITEM_HEIGHT_PORTRAIT_PLAY_STATE - 9;
            this._fEjectedTime_tf.position.set(238, MasterBetItem.ITEM_HEIGHT_PORTRAIT_PLAY_STATE - 9);
        }
        else if (lItemHeight_num === MasterBetItem.ITEM_HEIGHT_PORTRAIT_WAIT_STATE)
        {
            lNamePostfix_str = 'PortraitWaitState';
            lFieldsFontSize_num = 13;
            this._fDistance_tf.position.y = MasterBetItem.ITEM_HEIGHT - 1;
            this._fEjectedTime_tf.position.set(238, MasterBetItem.ITEM_HEIGHT - 1);
        }
        else
        {
            lFieldsFontSize_num = 11;
            this._fDistance_tf.position.y = MasterBetItem.ITEM_HEIGHT - 4;
            this._fEjectedTime_tf.position.set(240, MasterBetItem.ITEM_HEIGHT - 4);
        }

        this._fPlayerNameEjected_ta.position.set(MasterBetItem.ITEM_INDENT, lItemHeight_num*0.5);
        this._fPlayerNameNotEjected_ta.position.x = this._fPlayerNameEjected_ta.position.x;
        this._fPlayerNameNotEjected_ta.position.y = this._fPlayerNameEjected_ta.position.y;

        let lNameEjectedAssetId_str = 'TABattlegroundYOUEjectedItemCaption'+lNamePostfix_str;
        let lNameNotEjectedAssetId_str = 'TABattlegroundYOUNotEjectedItemCaption'+lNamePostfix_str;

        if (this._fPlayerNameEjected_ta.descriptor.assetId !== lNameEjectedAssetId_str)
        {
            //this._fPlayerNameEjected_ta.setAssetDescriptor(I18.getTranslatableAssetDescriptor(lNameEjectedAssetId_str));
        }

       if (this._fPlayerNameNotEjected_ta.descriptor.assetId !== lNameNotEjectedAssetId_str)
        {
            this._fPlayerNameNotEjected_ta.setAssetDescriptor(I18.getTranslatableAssetDescriptor(lNameNotEjectedAssetId_str));
        }
        
        this._fDistance_tf.updateFontSize(lFieldsFontSize_num);
        this._fEjectedTime_tf.updateFontSize(lFieldsFontSize_num);
        */
    }

    _getValuesTextFormat()
    {
        return {
            fontFamily: "fnt_nm_roboto_medium",
            fontSize: 11,
            fill: 0x000000
        };
    }

    destroy()
    {
        this.parent && this.parent.removeChild(this);

        super.destroy();
    }
}
export default MasterBetItem;