import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import GameplayInfo from '../../../../model/gameplay/GameplayInfo';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../config/AtlasConfig';

const GREEN_LIGHT_WIDTH = 16;

class BetsListItem extends Sprite
{
	static get ITEM_HEIGHT ()       { return 20; }
	static get ITEM_INDENT ()       { return 3; }
	static get ITEM_MARGIN_RIGHT()  { return 6; }

	static get MULTIPLIER_MAX_WIDTH() { return 40; } /*enough for 9999.99x without scaling*/
	static get BET_WIN_MAX_WIDTH() { return 76; } /*minimal required*/

	update(aPlayerName_str, aBetValue_num, aIsCurrentPlayer_bl, aPayout_num, aMultiplier_num)
	{
		this._fPlayerFullName_str = aPlayerName_str !== undefined ? aPlayerName_str : "";
		this._fPlayerName_tf.text = this._fPlayerFullName_str;
		this._fRegularBase_gr.visible = !aIsCurrentPlayer_bl;
		this._fCurrentPlayerBase_gr.visible = aIsCurrentPlayer_bl;

		if (aMultiplier_num !== undefined)
		{
			console.log("bet list multiplier num " + aMultiplier_num);
			let formatedMultiplier =  GameplayInfo.formatMultiplier(aMultiplier_num).replace("x", "");

			let formatValue = APP.currencyInfo.i_formatNumber(formatedMultiplier*100,false,false,2);

			this._fMultiplier_tf.text = formatValue + "x";

			// [OWL] TODO: apply changes for alll systems without any conditions
			if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
			{
				this._fWin_tf.text = APP.currencyInfo.i_formatNumber(aPayout_num, true, APP.isBattlegroundGame, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
			}
			else
			{
				this._fWin_tf.text = APP.currencyInfo.i_formatNumber(aPayout_num, true, APP.isBattlegroundGame, 2, undefined, false);
			}

			this._fMultiplier_tf.visible = true;
			this._fWin_tf.visible = true;
			this._fBet_tf.visible = false;
			this._fIsEjectedGreenLight_sprt.visible = true;
		}
		else
		{
			let lFormattedValue_str;
			if (aBetValue_num !== undefined && APP.appParamsInfo.restrictCoinFractionLength !== undefined)
			{
				lFormattedValue_str = APP.currencyInfo.i_formatNumber(aBetValue_num, true, APP.isBattlegroundGame, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
			}
			else if (aBetValue_num !== undefined)
			{
				lFormattedValue_str = APP.currencyInfo.i_formatNumber(aBetValue_num, true, APP.isBattlegroundGame, 2, undefined, false);
			}
			else
			{
				lFormattedValue_str = ""
			}
			this._fBet_tf.text = lFormattedValue_str;
			
			this._fMultiplier_tf.visible = false;
			this._fWin_tf.visible = false;
			this._fIsEjectedGreenLight_sprt.visible = false;
			this._fBet_tf.visible = true;
		}
	}

	updateItemWidth(aItemWidth_num)
	{
		this._fItemWidth_num = aItemWidth_num;

		this._refillBase();
		
		let lFreeWidth_num = aItemWidth_num - BetsListItem.ITEM_MARGIN_RIGHT - BetsListItem.MULTIPLIER_MAX_WIDTH - BetsListItem.ITEM_INDENT * 3; // 226-40-12=174
		let lBetWinWidth_num = Math.max(BetsListItem.BET_WIN_MAX_WIDTH /*min*/, Math.floor(lFreeWidth_num * 0.43));
		let lNameWidth_num = lFreeWidth_num - lBetWinWidth_num - GREEN_LIGHT_WIDTH;

		this._fPlayerName_tf.textFormat = this._getPlayerTextFormat(lNameWidth_num);
		this._fPlayerName_tf.text = this._fPlayerFullName_str;

		this._fMultiplier_tf.position.x = lNameWidth_num + GREEN_LIGHT_WIDTH + BetsListItem.ITEM_INDENT * 2;

		this._fBet_tf.maxWidth = this._fWin_tf.maxWidth = lBetWinWidth_num;
		this._fBet_tf.position.x = this._fWin_tf.position.x = aItemWidth_num - BetsListItem.ITEM_MARGIN_RIGHT;
	}

	_refillBase()
	{
		this._fRegularBase_gr.clear();
		this._fRegularBase_gr.beginFill(this.globalId % 2 == 0 ? 0x000000 : 0x141828).drawRoundedRect(0, 0, this._fItemWidth_num, BetsListItem.ITEM_HEIGHT, 5).endFill();

		this._fCurrentPlayerBase_gr.clear();
		this._fCurrentPlayerBase_gr.beginFill(this.globalId % 2 == 0 ? 0x1a190f : 0x47452a).drawRoundedRect(0, 0, this._fItemWidth_num, BetsListItem.ITEM_HEIGHT, 5).endFill();
	}

	clear()
	{
		this.update(undefined, undefined);
	}

	showGreenLight(aIsEjected_bl)
	{
		this._fIsEjectedGreenLight_sprt.visible = aIsEjected_bl;
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
		
		this._refillBase();
	}

	constructor(aId_int)
	{
		super();

		this._fId_int = aId_int;
		this._fGlobalId_int = aId_int;

		this._fRegularBase_gr = this.addChild(new PIXI.Graphics);
		this._fCurrentPlayerBase_gr = this.addChild(new PIXI.Graphics);
		this._fCurrentPlayerBase_gr.visible = false;

		this._fPlayerName_tf = this.addChild(new TextField(this._getPlayerTextFormat()));
		this._fPlayerName_tf.anchor.set(0, 1);
		this._fPlayerName_tf.position.set(BetsListItem.ITEM_INDENT + 12, BetsListItem.ITEM_HEIGHT - 3);
		this._fPlayerFullName_str = "";
		
		this._fMultiplier_tf = this.addChild(new TextField(this._getMultiplierTextFormat()));
		this._fMultiplier_tf.anchor.set(0, 1);
		this._fMultiplier_tf.maxWidth = BetsListItem.MULTIPLIER_MAX_WIDTH;
		this._fMultiplier_tf.position.y = BetsListItem.ITEM_HEIGHT - 4;
		this._fMultiplier_tf.visible = false;
		
		this._fBet_tf = this.addChild(new TextField(this._getBetTextFormat()));
		this._fBet_tf.anchor.set(1, 1);
		this._fBet_tf.position.y = BetsListItem.ITEM_HEIGHT - 3;

		this._fWin_tf = this.addChild(new TextField(this._getWinTextFormat()));
		this._fWin_tf.anchor = this._fBet_tf.anchor;
		this._fWin_tf.position = this._fBet_tf.position;
		this._fWin_tf.visible = false;

		//GREEN LIGHT...
		let l_sprt = this._fIsEjectedGreenLight_sprt = new Sprite;
		l_sprt.textures = [BetsListItem.getGreenLightTextures()[0]];
		l_sprt.position.set(8, BetsListItem.ITEM_HEIGHT/2);
		l_sprt.visible = false;
		this.addChild(l_sprt);
		//...GREEN LIGHT
	}

	_getPlayerTextFormat(aOptShortLength_num)
	{
		aOptShortLength_num = aOptShortLength_num || 95;
		return {
			fontFamily: "fnt_nm_roboto_medium",
			fontSize: 11,
			shortLength: aOptShortLength_num,
			fill: 0xffffff
		};
	}

	_getBetTextFormat()
	{
		return {
			fontFamily: "fnt_nm_roboto_medium",
			fontSize: 11,
			fill: 0xffffff
		};
	}

	_getWinTextFormat()
	{
		return {
			fontFamily: "fnt_nm_roboto_medium",
			fontSize: 11,
			fill: 0x26ee21
		};
	}

	_getMultiplierTextFormat()
	{
		return {
			fontFamily: "fnt_nm_roboto_medium",
			fontSize: 9,
			fill: 0x26ee21
		};
	}

	destroy()
	{
		this.parent && this.parent.removeChild(this);

		super.destroy();
	}
}

BetsListItem.getGreenLightTextures = function()
{
	if (!BetsListItem.green_light_textures)
	{
		BetsListItem.green_light_textures = [];

		BetsListItem.green_light_textures = AtlasSprite.getFrames([APP.library.getAsset('game/gameplay_assets')], [AtlasConfig.GameplayAssets], 'greenactive');
		BetsListItem.green_light_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return BetsListItem.green_light_textures;
}

export default BetsListItem;