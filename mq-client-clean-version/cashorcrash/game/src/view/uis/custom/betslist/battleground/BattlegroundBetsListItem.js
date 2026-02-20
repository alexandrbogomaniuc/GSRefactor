import { Sprite } from "../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display"
import { APP } from "../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import TextField from "../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField";
import GameplayInfo from "../../../../../model/gameplay/GameplayInfo";
import AtlasConfig from "../../../../../config/AtlasConfig";
import { BitmapText } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasSprite  from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import NonWobblingTextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/NonWobblingTextField';
import AlignDescriptor from "../../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor";

const NOT_EJECTED_VALUE = '----';

class BattlegroundBetsListItem extends Sprite
{
    static get ITEM_HEIGHT ()                                   { return 20; }
    static get ITEM_HEIGHT_PORTRAIT_PLAY_STATE ()               { return 33; }
    static get ITEM_HEIGHT_PORTRAIT_WAIT_STATE ()               { return 24; }
    static get ITEM_INDENT ()                                   { return 0; }
    static get ITEM_MARGIN_RIGHT()                              { return 6; }

    static get DISTANCE_MAX_WIDTH() { return 40; }
    static get BET_EJECTED_TIME_MAX_WIDTH() { return 76; } /*minimal required*/

    update(aBetInfo_bi, aIsWinner_bl)
    {
        this._fContentContainer_sprt.visible = true;

        let lPlayerName_str = this._fPlayerFullName_str = aBetInfo_bi.playerName || undefined;
        this._fPlayerName_tf.text = lPlayerName_str || "";

        this._fPositionNum_tf.visible = lPlayerName_str !== undefined;
        this._fPositionNum_tf.write((1 + this.globalId) + "");

        let lIsEjected_bl = this._fIsEjected_bl = aBetInfo_bi.isEjected;
        this._fIsWinnerItem_bl = aIsWinner_bl;

        if (lPlayerName_str === undefined)
        {
            this._fDistance_tf.visible = false;
            this._fEjectedTime_tf.visible = false;
        }
        else
        {
            this._fDistance_tf.write( lIsEjected_bl ? GameplayInfo.formatMultiplier(aBetInfo_bi.multiplier) : NOT_EJECTED_VALUE);
            this._fDistance_tf.visible = true;

            let l_gpi = APP.gameController.gameplayController.info;
            let lEjectedBetDuration_num = aBetInfo_bi.ejectTime - l_gpi.multiplierChangeFlightStartTime;
            this._fEjectedTime_tf.write( lIsEjected_bl ? GameplayInfo.formatTime(lEjectedBetDuration_num) + 's' : NOT_EJECTED_VALUE);
            //this._fEjectedTime_tf.write("0000");
            this._fEjectedTime_tf.visible = true;
        }

        this._validateViewFormat();
    }

    updateLayout(aItemWidth_num, aItemHeight_num)
    {
        this._fItemWidth_num = aItemWidth_num;
        this._fItemHeight_num = aItemHeight_num;
        
        let lFreeWidth_num = aItemWidth_num - BattlegroundBetsListItem.ITEM_MARGIN_RIGHT - BattlegroundBetsListItem.DISTANCE_MAX_WIDTH - BattlegroundBetsListItem.ITEM_INDENT * 3;
        let lEjectedTimeWidth_num = Math.max(BattlegroundBetsListItem.BET_EJECTED_TIME_MAX_WIDTH /*min*/, Math.floor(lFreeWidth_num * 0.43));
        let lNameWidth_num = lFreeWidth_num - lEjectedTimeWidth_num - 16;

        this._fEjectedTime_tf.maxWidth = lEjectedTimeWidth_num;
        this._fEjectedTime_tf.position.x = aItemWidth_num - 50;

        this._fDistance_tf.position.x = lNameWidth_num + 25 + BattlegroundBetsListItem.ITEM_INDENT * 2;

        this._validateViewFormat();
    }

    clear()
    {
        this._fIsWinnerItem_bl = false;
        this._fIsEjected_bl = false;

        this._validateBaseView();

        this._fContentContainer_sprt.visible = false;
    }

    _validateViewFormat()
    {
        this._validateBaseView();
        this._validateCrownView();

        this._updateFieldsFormatting();

        this._updateTextFieldsPositions();
    }

    _validateBaseView()
    {
        this._fRegularBase_gr.clear();
        this._fWinnerBase_gr.clear();

        if (this._fIsWinnerItem_bl)
        {
            this._fWinnerBase_gr.beginFill(0x1c2238).drawRoundedRect(0, 0, this._fItemWidth_num, this._fItemHeight_num, 5).endFill();
        }
        else
        {
            this._fRegularBase_gr.beginFill(this.globalId % 2 == 0 ? 0x141828 : 0x000000).drawRoundedRect(0, 0, this._fItemWidth_num, this._fItemHeight_num, 5).endFill();
        }
    }

    _validateCrownView()
    {
        let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;

        let lIsCrownRequired_bl = this._fIsWinnerItem_bl;

        if (lIsCrownRequired_bl)
        {
            let lCrownScale_num = lIsPortraitMode_bl ? 0.9 : 0.75;
            let lCrownX_num = 115;
            let lCrownY_num = lIsPortraitMode_bl
                                    ? BattlegroundBetsListItem.ITEM_HEIGHT_PORTRAIT_PLAY_STATE/2
                                    : BattlegroundBetsListItem.ITEM_HEIGHT/2;

            this._fCrown_spr.scale.set(lCrownScale_num);
            this._fCrown_spr.position.set(lCrownX_num, lCrownY_num);
            this._fCrown_spr.visible = true;
        }
        else
        {
            this._fCrown_spr.visible = false;
        }
    }

    get globalId()
    {
        let lGID = this._fGlobalId_int;
        if (!lGID) lGID = this._fId_int;
        return lGID;
    }

    set globalId(value_int)
    {
        this._fGlobalId_int = value_int;
    }

    constructor(aId_int)
    {
        super();

        this._fId_int = aId_int;
        this._fGlobalId_int = aId_int;

        this._fIsEjected_bl = false;
        this._fIsWinnerItem_bl = false;

        let lBase_sprt = this._fBaseContainer_sprt = this.addChild(new Sprite);
        let lCont_sprt = this._fContentContainer_sprt = this.addChild(new Sprite);

        this._fRegularBase_gr = lBase_sprt.addChild(new PIXI.Graphics);
        this._fWinnerBase_gr = lBase_sprt.addChild(new PIXI.Graphics);

          /**
         * 		let lMultiplierTextField_nwtf = this._fMultiplierTextField_nwtf = lContainer_sprt.addChild(new NonWobblingTextField());
                lMultiplierTextField_nwtf.fontName = "fnt_nm_myriad_pro_bold";
                lMultiplierTextField_nwtf.fontSize = 16;
                lMultiplierTextField_nwtf.fontColor = 0xffffff;
                lMultiplierTextField_nwtf.setAlign(AlignDescriptor.CENTER, AlignDescriptor.MIDDLE);
                lMultiplierTextField_nwtf.maxWidth = 70;
                lMultiplierTextField_nwtf.position.set(0, -23);
                lMultiplierTextField_nwtf.letterSpace = -2;
         */

        this._fPlayerName_tf = lCont_sprt.addChild(new TextField(this._getPlayerTextFormat(68)));
        this._fPlayerName_tf.visible = true;
        this._fPlayerFullName_str = "";

        const letterSpace = 3;

        this._fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset("scorescoreboard_font/scoreboard_font")], [AtlasConfig.ScoreBoardFont], "");
       
        this._fDistance_tf = lCont_sprt.addChild(new BitmapText(this._fTextures_tx_map, "", letterSpace));
        this._fDistance_tf.scale.set(0.37, 0.37);
        
        this._fDistance_tf.visible = false;

        this._fEjectedTime_tf = lCont_sprt.addChild(new BitmapText(this._fTextures_tx_map, "", letterSpace));
        this._fEjectedTime_tf.position.y = BattlegroundBetsListItem.ITEM_HEIGHT - 10;
        this._fEjectedTime_tf.visible = false;
        this._fEjectedTime_tf.scale.set(0.37, 0.37);

        this._fCrown_spr = lCont_sprt.addChild(APP.library.getSprite("game/crown"));
        this._fCrown_spr.scale.set(0.75);
        this._fCrown_spr.visible = false;

        this._fPositionNum_tf = lCont_sprt.addChild(new BitmapText(this._fTextures_tx_map, "", letterSpace));
        this._fPositionNum_tf.scale.set(0.37, 0.37);
        this._fPositionNum_tf.position.set(4, BattlegroundBetsListItem.ITEM_HEIGHT/2 + 1);
        this._fPositionNum_tf.visible = false;
    }    

    _updateFieldsFormatting()
    {
        let lFieldsFontSize_num;
        let lItemHeight_num = this._fItemHeight_num;

        if (lItemHeight_num === BattlegroundBetsListItem.ITEM_HEIGHT_PORTRAIT_PLAY_STATE)
        {
            lFieldsFontSize_num = 14;
        }
        else if (lItemHeight_num === BattlegroundBetsListItem.ITEM_HEIGHT_PORTRAIT_WAIT_STATE)
        {
            lFieldsFontSize_num = 13;
        }
        else
        {
            lFieldsFontSize_num = 11;
        }

        this._fPlayerName_tf.updateFontSize(lFieldsFontSize_num);
       // this._fDistance_tf.updateFontSize(lFieldsFontSize_num);
        //this._fEjectedTime_tf.updateFontSize(lFieldsFontSize_num);
        //this._fPositionNum_tf.updateFontSize(lFieldsFontSize_num);

        let lEjectedTimeIndicatorColor_int = this._fIsWinnerItem_bl ? 0x26ee21 : (this._fIsEjected_bl ? 0xbc363a : 0xffffff);
        this._fEjectedTime_tf.addTint(lEjectedTimeIndicatorColor_int);

        let lDistanceIndicatorColor_int = this._fIsWinnerItem_bl ? 0x26ee21 : 0xffffff;
        this._fDistance_tf.addTint(lDistanceIndicatorColor_int);
    }

    _updateTextFieldsPositions()
    {
        let lIsPortraitMode_bi = APP.layout.isPortraitOrientation;       

        if (lIsPortraitMode_bi)
        {
			if (this._fItemHeight_num === BattlegroundBetsListItem.ITEM_HEIGHT_PORTRAIT_PLAY_STATE)
            {
                const y_pos_int = 18;
                this._fDistance_tf.position.y = y_pos_int;
                this._fPlayerName_tf.position.set(27, y_pos_int-7);
                this._fEjectedTime_tf.position.y = y_pos_int;
                this._fPositionNum_tf.position.set(BattlegroundBetsListItem.ITEM_INDENT + 4, y_pos_int);
            }
            else
            {
                const y_pos_int = 14;
                this._fDistance_tf.position.y = y_pos_int;
                this._fPlayerName_tf.position.set(27, y_pos_int-7);
                this._fEjectedTime_tf.position.y = y_pos_int;
                this._fPositionNum_tf.position.set(4, y_pos_int);
            }
        }
        else
        {
			this._fDistance_tf.position.y = BattlegroundBetsListItem.ITEM_HEIGHT - 9;
            this._fPlayerName_tf.position.set(27, 3);
			this._fEjectedTime_tf.position.y = BattlegroundBetsListItem.ITEM_HEIGHT - 10;
			this._fPositionNum_tf.position.set(4, BattlegroundBetsListItem.ITEM_HEIGHT/2 + 1);
        }
    }

    _getPlayerTextFormat(aOptShortLength_num)
    {
        aOptShortLength_num = aOptShortLength_num || 95;
        return {
            fontFamily: "fnt_nm_barlow_semibold",
            fontSize: 11,
            shortLength: aOptShortLength_num,
            fill: 0xffffff
        };
    }

    
    destroy()
    {
        this._fIsWinnerItem_bl = undefined;
        this._fIsEjected_bl = undefined;
        this._fPlayerFullName_str = "";

        this._fBaseContainer_sprt = null;
        this._fContentContainer_sprt = null;

        this._fRegularBase_gr = null;
        this._fWinnerBase_gr = null;

        this._fPlayerName_tf = null;
        this._fDistance_tf = null;
        this._fEjectedTime_tf = null;
        this._fCrown_spr = null;
        this._fPositionNum_tf = null;

        super.destroy();
    }
}
export default BattlegroundBetsListItem;