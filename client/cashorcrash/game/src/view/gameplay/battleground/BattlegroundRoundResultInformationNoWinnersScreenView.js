import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasConfig from '../../../config/AtlasConfig';
import { BitmapText } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasSprite  from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';

class BattlegroundRoundResultInformationNoWinnersScreenView extends Sprite
{
	constructor()
	{
		super();

		this._fContainer_sprt = null;
		this._fNoOneEjectRefundContainer_spr = null;
		this._fRefundValue_ta = null;

		this._addContent();
	}

	get isRefundMode()
	{
		return this.visible && this._fNoOneEjectRefundContainer_spr.visible;
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_ri = l_gpi.roundInfo;
		let lRoomInfo_ri = l_gpi.roomInfo;

		let lRoundWinners_arr = l_ri.battlegroundRoundWinners;
		if (!lRoundWinners_arr || !lRoundWinners_arr.length)
		{
			this.visible = true;

			let lRefundValue_num = l_ri.refundValue;
			if (lRefundValue_num > 0)
			{
				this._fNoOneEjectRefundContainer_spr.visible = true;

				this._fRefundValue_ta.write(APP.currencyInfo.i_formatNumber(lRefundValue_num, true, APP.isBattlegroundGame, 2, undefined, false));

				let lRakePercent_num = lRoomInfo_ri.rakePercent;
				let lRakeValue_str = lRakePercent_num !== undefined ? `(${lRakePercent_num}%)` : '';
			}
			else
			{
				this._fNoOneEjectRefundContainer_spr.visible = false;
			}
		}
		else
		{
			this.visible = false;
		}
	}

	_addContent()
	{
		this._fContainer_sprt = this.addChild(new Sprite());
		let l_gr = this._fContainer_sprt.addChild(new PIXI.Graphics).beginFill(0xe0b636, 1).drawRect(-131, 21, 262, 1.5).endFill();
		this._initNoOneEjectRefundBuyInScreen();
	}

	_initNoOneEjectRefundBuyInScreen()
	{
		this._fNoOneEjectRefundContainer_spr = this._fContainer_sprt.addChild(new Sprite());
		this._fNoOneEjectedMessage = APP.library.getSprite("messages/no_one_ejected");
		this._fNoOneEjectRefundContainer_spr.addChild(this._fNoOneEjectedMessage);
		this._fNoOneEjectedMessage.position.set(4,34);
		this._fNoOneEjectedMessage.scale.set(1.15,1.15);
		this._fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset("roboto/bmp_roboto_medium")], [AtlasConfig.BmpRobotoMedium], "");
		this._fRefundValue_ta = this._fNoOneEjectRefundContainer_spr.addChild(new BitmapText(this._fTextures_tx_map, "", -14));
		this._fRefundValue_ta.position.set(40, 85);
		this._fRefundValue_ta.scale.set(0.5,0.5);
		this._fRefundValue_ta.addTint(0xf4d425);
	}
}

export default BattlegroundRoundResultInformationNoWinnersScreenView;