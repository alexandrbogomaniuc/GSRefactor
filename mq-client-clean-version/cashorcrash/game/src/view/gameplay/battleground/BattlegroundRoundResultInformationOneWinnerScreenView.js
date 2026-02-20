import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import{BitmapText } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasSprite  from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../config/AtlasConfig';


class BattlegroundRoundResultInformationOneWinnerScreenView extends Sprite
{
	constructor()
	{
		super();

		this._fContainer_sprt = null;
		this._fWinnerNameRow_sprt = null;
		this._fRoundResultNickName_tf = null;
		this._fRoundResultWinValue_ta = null;
		this._addContent();
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_ri = l_gpi.roundInfo;

		let lRoundWinners_arr = l_ri.battlegroundRoundWinners;
		if (!!lRoundWinners_arr && lRoundWinners_arr.length === 1)
		{
			this.visible = true;

			let lFormattedValue_str;
			// [OWL] TODO: apply changes for alll systems without any conditions
			if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
			{
				lFormattedValue_str = APP.currencyInfo.i_formatNumber(l_ri.battlegroundRoundWinValue, true, APP.isBattlegroundGame, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
			}
			else
			{
				lFormattedValue_str = APP.currencyInfo.i_formatNumber(l_ri.battlegroundRoundWinValue, true, APP.isBattlegroundGame, 2, undefined, false);
			}

			this._fRoundResultWinValue_ta.write(lFormattedValue_str);

			this._fRoundResultNickName_tf.write(lRoundWinners_arr[0]);

			this._fWinnerNameRow_sprt.position.x = -this._fWinnerNameRow_sprt.getBounds().width*0.5;
		}
		else
		{
			this.visible = false;
		}
	}

	_addContent()
	{
		const _fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset("scorescoreboard_font/scoreboard_font")], [AtlasConfig.ScoreBoardFont], "");


		this._fContainer_sprt = this.addChild(new Sprite());

		let lRoundResultRoundOverLabel_ta = this._fContainer_sprt.addChild(new BitmapText(_fTextures_tx_map, "", 5));
		
		lRoundResultRoundOverLabel_ta.scale.set(1.5, 1.5);
		lRoundResultRoundOverLabel_ta.write("ROUND OVER!");
		lRoundResultRoundOverLabel_ta.position.set(((lRoundResultRoundOverLabel_ta.textWidth * lRoundResultRoundOverLabel_ta.scale.x)/2)*-1, -48);

		this._fContainer_sprt.addChild(new PIXI.Graphics).beginFill(0xe0b636, 1).drawRect(-131, 21, 262, 1.5).endFill();

		let lWinnerNameRow_sprt = this._fWinnerNameRow_sprt = this._fContainer_sprt.addChild(new Sprite);
		let lCrown_spr = lWinnerNameRow_sprt.addChild(APP.library.getSprite("game/battleground/round_result_crown"));
		lCrown_spr.anchor.set(0, 0.5);
		lCrown_spr.position.set(0, -4);

		let lNickname_tf = this._fRoundResultNickName_tf = lWinnerNameRow_sprt.addChild(new BitmapText(_fTextures_tx_map, "", 5));
		lNickname_tf.position.set(lCrown_spr.getBounds().width + 5, -2);
		lNickname_tf.scale.set(1.3,1.3);
		lNickname_tf.write("");

		let lRoundResultWinsLabel_ta = this._fContainer_sprt.addChild(new BitmapText(_fTextures_tx_map, "", 5));
		lRoundResultWinsLabel_ta.position.set(-120, 46);
		lRoundResultWinsLabel_ta.write("WINS");
		lRoundResultWinsLabel_ta.scale.set(1.4, 1.4);

		let lWinValue_ta = this._fRoundResultWinValue_ta = this._fContainer_sprt.addChild(new BitmapText(_fTextures_tx_map, "", 5));
		lWinValue_ta.position.set(10, 46);
		lWinValue_ta.scale.set(1.3,1.3);
		lWinValue_ta.addTint(0xffc700);
		
	}


}

export default BattlegroundRoundResultInformationOneWinnerScreenView;