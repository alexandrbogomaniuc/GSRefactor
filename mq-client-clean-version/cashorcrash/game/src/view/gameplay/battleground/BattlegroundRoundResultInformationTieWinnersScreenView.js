import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { BitmapText } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasSprite  from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../config/AtlasConfig';

class BattlegroundRoundResultInformationTieWinnersScreenView extends Sprite
{
	constructor()
	{
		super();

		this._fContainer_sprt = null;
		this._fRoundResultTieTwoPlayersContainer_spr = null;
		this._fRoundResultTieManyPlayersContainer_spr = null;
		this._fNicknameTieOne_ta = null;
		this._fNicknameTieTwo_ta = null;
		this._fWinTieManyPlayersCountValue_ta = null;
		this._fWinTieManyValue_ta = null;
		this._fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset("roboto/bmp_roboto_medium")], [AtlasConfig.BmpRobotoMedium], "");
		this._fTextures_tx_map_full = AtlasSprite.getMapFrames([APP.library.getAsset("scorescoreboard_font/scoreboard_font")], [AtlasConfig.ScoreBoardFont], "");
		this._addContent();
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_ri = l_gpi.roundInfo;
		let lRoundWinners_arr = l_ri.battlegroundRoundWinners;
		if(!!lRoundWinners_arr)
		{
			this._tieLabel_ta.write(lRoundWinners_arr.length  + " WINNERS");
			this._tieLabel_ta.x = ((this._tieLabel_ta.textWidth * this._tieLabel_ta.scale.x )/ 2) *-1;
		}

		if (!!lRoundWinners_arr && lRoundWinners_arr.length === 2)
		{
			this.visible = true;
			this._fRoundResultTieTwoPlayersContainer_spr.visible = true;
			this._fRoundResultTieManyPlayersContainer_spr.visible = false;

			this._fNicknameTieOne_ta.write(lRoundWinners_arr[0]);
			this._fNicknameTieTwo_ta.write(lRoundWinners_arr[1]);

			this._fWinTieValue_ta.write(APP.currencyInfo.i_formatNumber(l_ri.battlegroundRoundWinValue, true, APP.isBattlegroundGame, 2, undefined, false) + "");


			const tieOneNickWidth = this._fNicknameTieOne_ta.textWidth * this._fNicknameTieOne_ta.scale.x;
			const tileTwoNickWidth = this._fNicknameTieTwo_ta.textWidth * this._fNicknameTieTwo_ta.scale.x;

			this._fNicknameTieOne_ta.x  = (tieOneNickWidth + 25) *-1;
			this._fNicknameTieTwo_ta.x = 50;
			this._crownOne.position.x = this._fNicknameTieOne_ta.x  - 25;
			this._crownTwo.position.x = this._fNicknameTieTwo_ta.x  - 25;

			let dif = 0; 


			this._fNicknameTieOne_ta.x  = (tieOneNickWidth + 25) *-1;
			this._fNicknameTieTwo_ta.x = 50;
			this._crownOne.position.x = this._fNicknameTieOne_ta.x  - 25;
			this._crownTwo.position.x = this._fNicknameTieTwo_ta.x  - 25;


			if(tieOneNickWidth > tileTwoNickWidth)
			{
				dif = (tieOneNickWidth - tileTwoNickWidth)/2;

				this._fNicknameTieOne_ta.x+= dif;
				this._fNicknameTieTwo_ta.x+= dif;
				this._crownOne.position.x+= dif;
				this._crownTwo.position.x+= dif;


			}else if(tileTwoNickWidth > tieOneNickWidth)
			{
				dif = (tileTwoNickWidth - tieOneNickWidth)/2;
				this._fNicknameTieOne_ta.x-= dif;
				this._fNicknameTieTwo_ta.x-= dif;
				this._crownOne.position.x-= dif;
				this._crownTwo.position.x-= dif;
			}

		}
		else if (!!lRoundWinners_arr && lRoundWinners_arr.length > 2)
		{
			this.visible = true;
			const offsetx = 10;
			this._fRoundResultTieManyPlayersContainer_spr.visible = true;
			this._fRoundResultTieTwoPlayersContainer_spr.visible = false;
			this._fWinTieManyPlayersCountValue_ta.write(lRoundWinners_arr.length + "");
			this._fWinTieManyPlayersCountValue_ta.x = this._playersWonLabel_ta.x -  (this._fWinTieManyPlayersCountValue_ta.textWidth * this._fWinTieManyPlayersCountValue_ta.scale.x) - offsetx ;
			this._fWinTieManyValue_ta.write(APP.currencyInfo.i_formatNumber(l_ri.battlegroundRoundWinValue, true, APP.isBattlegroundGame, 2, undefined, false)+"") ;
			this._fWinTieManyValue_ta.x = this._playersWonLabel_ta.x + (this._playersWonLabel_ta.textWidth * this._playersWonLabel_ta.scale.x) + offsetx;
		}
		else
		{
			this.visible = false;
		}	
	}

	_addContent()
	{
		this._fContainer_sprt = this.addChild(new Sprite());

		let lRoundResultRoundOverLabel_ta = this._tieLabel_ta =  this._fContainer_sprt.addChild(new BitmapText(this._fTextures_tx_map_full, "", 3));
		lRoundResultRoundOverLabel_ta.position.set(0, -58);
		lRoundResultRoundOverLabel_ta.scale.set(1.8, 1.8);

		let lRoundResultRoundOverTieLabel_ta = this._fContainer_sprt.addChild(APP.library.getSprite("labels/its_a_tie"));
		lRoundResultRoundOverTieLabel_ta.position.set(0, 0);

		let lCrown_spr = this._fContainer_sprt.addChild(APP.library.getSprite("game/battleground/round_result_crown"));
		lCrown_spr.position.set(0, 48);

		let l_gr = this._fContainer_sprt.addChild(new PIXI.Graphics).beginFill(0xe0b636, 1).drawRect(-87, 72, 174, 1.5).endFill();

		this._initTieTwoPlayers();
		this._initTieManyPlayers();
	}

	_initTieTwoPlayers()
	{
		this._fRoundResultTieTwoPlayersContainer_spr = this._fContainer_sprt.addChild(new Sprite());
		this._fRoundResultTieTwoPlayersContainer_spr.position.set(0, 100);

		this._crownOne = this._fRoundResultTieTwoPlayersContainer_spr.addChild(APP.library.getSprite("game/battleground/round_result_crown_tie"));
		this._crownOne.position.set(-220, -2);

		let lNicknameTieOne_ta = this._fNicknameTieOne_ta = this._fRoundResultTieTwoPlayersContainer_spr.addChild(new BitmapText(this._fTextures_tx_map_full,"", 4));
		lNicknameTieOne_ta.position.set(-154, 0);
	

		this._crownTwo = this._fRoundResultTieTwoPlayersContainer_spr.addChild(APP.library.getSprite("game/battleground/round_result_crown_tie"));
		this._crownTwo.position.set(8, -2);

		let lNicknameTieTwo_ta = this._fNicknameTieTwo_ta = this._fRoundResultTieTwoPlayersContainer_spr.addChild(new BitmapText(this._fTextures_tx_map_full,"", 4));
		lNicknameTieTwo_ta.position.set(74, 0);

		let l_gr = this._fRoundResultTieTwoPlayersContainer_spr.addChild(new PIXI.Graphics).beginFill(0xe0b636, 1).drawRect(-131, 26, 262, 1.5).endFill();

		let lEachWins_ta = this._eachWinsLabel_ta = this._fRoundResultTieTwoPlayersContainer_spr.addChild(new BitmapText(this._fTextures_tx_map_full, "", 4));
		lEachWins_ta.position.set(-166, 48);
		lEachWins_ta.write("EACH WINS");

		let lWinTieValue_ta = this._fWinTieValue_ta = this._fRoundResultTieTwoPlayersContainer_spr.addChild(new BitmapText(this._fTextures_tx_map, "", -22));
		lWinTieValue_ta.position.set(10, 50);
		lWinTieValue_ta.scale.set(0.65, 0.65);
		lWinTieValue_ta.addTint(0xf4d425);
	}

	_initTieManyPlayers()
	{
		this._fRoundResultTieManyPlayersContainer_spr = this._fContainer_sprt.addChild(new Sprite());
		this._fRoundResultTieManyPlayersContainer_spr.position.set(0, 100);

		let lPlayersCountValue_ta = this._fWinTieManyPlayersCountValue_ta = this._fRoundResultTieManyPlayersContainer_spr.addChild(new BitmapText(this._fTextures_tx_map_full, "", 4));
		lPlayersCountValue_ta.position.set(-182, 6);
		lPlayersCountValue_ta.addTint(0xf4d425);
		//lPlayersCountValue_ta.scale.set(0.65,0.65);

		let lPlayersWon_ta = this._playersWonLabel_ta = this._fRoundResultTieManyPlayersContainer_spr.addChild(new BitmapText(this._fTextures_tx_map_full, "", 2));
		lPlayersWon_ta.position.set(-137, 6);
		lPlayersWon_ta.write("PLAYERS WON");

		let lWinTieManyValue_ta = this._fWinTieManyValue_ta = this._fRoundResultTieManyPlayersContainer_spr.addChild(new BitmapText(this._fTextures_tx_map_full, "", 4));
		lWinTieManyValue_ta.position.set(56, 6);
		lWinTieManyValue_ta.addTint(0xf4d425);
	}
}

export default BattlegroundRoundResultInformationTieWinnersScreenView;