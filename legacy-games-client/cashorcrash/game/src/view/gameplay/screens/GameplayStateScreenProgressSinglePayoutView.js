import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayInfo from '../../../model/gameplay/GameplayInfo';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import NonWobblingTextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/NonWobblingTextField';
import { MAX_MASTER_BETS_AMOUNT } from '../../../model/gameplay/bets/BetsInfo';
import GameplayStateScreenProgressPayoutsView from './GameplayStateScreenProgressPayoutsView';
import AlignDescriptor from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';
import GameButton from '../../../ui/GameButton';
import MultiplierRulerView from '../graph/MultiplierRulerView';
import Button from '../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

const DEFAULT_MAX_WIDTH = 100;

class GameplayStateScreenProgressSinglePayoutView extends Sprite
{
	static get EVENT_ON_EJECT_INITIATED()		{ return 'EVENT_ON_EJECT_INITIATED' };

	static calcMaxWidth()
	{
		GameplayStateScreenProgressSinglePayoutView.MAX_WIDTH = DEFAULT_MAX_WIDTH;

		let lFullAreaWidth_num = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width - MultiplierRulerView.RULER_VISUAL_WIDTH;
		let lCalculatedMaxWidth = ~~(lFullAreaWidth_num/MAX_MASTER_BETS_AMOUNT - GameplayStateScreenProgressPayoutsView.PAYOUTS_DISTANCE*(MAX_MASTER_BETS_AMOUNT-1));
		if (lCalculatedMaxWidth < DEFAULT_MAX_WIDTH)
		{
			GameplayStateScreenProgressSinglePayoutView.MAX_WIDTH = lCalculatedMaxWidth;
		}
	}

	constructor(aBetIndex_int)
	{
		super();

		GameplayStateScreenProgressSinglePayoutView.calcMaxWidth();

		this._fBetIndex_int = aBetIndex_int;
		this._fPayoutLabel_ta = null;
		this._fPayout_tf = null;
		this._fPayoutContainer_sprt = null;
		this._fCurBetInfo_bi = null;
		this._fContainer_sprt = null;

		//CONTENT...
		this._addContent();
		//...CONTENT
	}

	get betIndex()
	{
		return this._fBetIndex_int;
	}

	get curBetInfo()
	{
		return this._fCurBetInfo_bi;
	}

	get ejectButton()
	{
		return this._fEject_btn;
	}

	adjust(aBetInfo_bi)
	{
		let l_gpc = APP.gameController.gameplayController;
		let l_gpi = l_gpc.info;
		let l_gpv = l_gpc.view;
		let l_bsc = l_gpc.gamePlayersController.betsController;

		let lMultiplier_num = l_gpi.serverMultiplierValue;
		let lPayout_num = aBetInfo_bi.isBetWinDefined ?  aBetInfo_bi.betWin : aBetInfo_bi.betAmount * lMultiplier_num;
		
		// [OWL] TODO: apply changes for alll systems without any conditions
		if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
		{
			if (!aBetInfo_bi.isBetWinDefined)
			{
				lPayout_num = +lPayout_num.toFixed(0);
			}
			lPayout_num = APP.currencyInfo.i_formatNumber(lPayout_num, false, false, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
		}
		else
		{
			if (!aBetInfo_bi.isBetWinDefined)
			{
				lPayout_num = Math.floor(lPayout_num);
			}
			lPayout_num = APP.currencyInfo.i_formatNumber(lPayout_num, false, false, 2, undefined, false);
		}


		if(APP.currencyInfo.i_getCurrencySymbol().toUpperCase() === "QC")
		{
			let spliter = lPayout_num.split(".");
			lPayout_num = spliter[0];
			this._fPayout_tf.text = lPayout_num + " " + APP.freeMoneySign;
		}else{
			this._fPayout_tf.text = "$" + lPayout_num.replaceAll(/ /g, '');
		}

		
		let lPayoutContainer_sprt = this._fPayoutContainer_sprt;
		lPayoutContainer_sprt.scale.x = 1;
		let lCurPayoutWidth_num = this._fPayout_tf.position.x + this._fPayout_tf.textWidth;
		if (lCurPayoutWidth_num > GameplayStateScreenProgressSinglePayoutView.MAX_WIDTH)
		{
			lPayoutContainer_sprt.scale.x = GameplayStateScreenProgressSinglePayoutView.MAX_WIDTH / lCurPayoutWidth_num;
		}
		lPayoutContainer_sprt.position.x = - lPayoutContainer_sprt.scale.x * lCurPayoutWidth_num * 0.5;

		let lAutoEjectAtLabelAsset_ta = this._fAutoEjectAtLabelAsset_ta;
		if (aBetInfo_bi.isAutoEject && !aBetInfo_bi.isBetWinDefined)
		{
			let lAutoEjectMult_num = aBetInfo_bi.autoEjectMultiplier;
			let formatEjetMult_num =  GameplayInfo.formatMultiplier(lAutoEjectMult_num).replace("x","");
			lAutoEjectAtLabelAsset_ta.text = this._fAutoEjectTextTemplate_str.replace("/AUTO_EJECT_MULT/", APP.currencyInfo.i_formatNumber(formatEjetMult_num * 100,false,false,2) + "x");
			lAutoEjectAtLabelAsset_ta.visible = true;
		}
		else
		{
			lAutoEjectAtLabelAsset_ta.visible = false;
		}

		let lPayoutCollectedLabelAsset_ta = this._fPayoutLabel_ta;
		lPayoutCollectedLabelAsset_ta.visible = !!aBetInfo_bi.isBetWinDefined;

		let lEject_btn = this._fEject_btn;
		lEject_btn.visible = !aBetInfo_bi.isBetWinDefined;

		let lIsBetEjectInProgress_bl = l_bsc.isBetCancelInProgress(aBetInfo_bi.betId);
		lEject_btn.enabled = !aBetInfo_bi.isEjected && l_gpi.roundInfo.isRoundPlayActive && !lIsBetEjectInProgress_bl && !l_gpi.gamePlayersInfo.isMasterPlayerLeaveRoomTriggered;

		let lContainer_sprt = this._fContainer_sprt;
		let lEjectPos_p = lContainer_sprt.localToLocal(0, 54, l_gpv.ejectButtonsContainer);

		lEject_btn.position.set(lEjectPos_p.x, lEjectPos_p.y);

		this._fCurBetInfo_bi = aBetInfo_bi;

	}

	_addContent()
	{
		let l_gpv = APP.gameController.gameplayController.view;
		let lContainer_sprt = this._fContainer_sprt = this.addChild(new Sprite);

		let lPayoutLabelAsset_ta = I18.generateNewCTranslatableAsset("TAPayoutAstronautLabel");
		lContainer_sprt.addChild(lPayoutLabelAsset_ta);
		
		let lTemplate_str = lPayoutLabelAsset_ta.text;
		lPayoutLabelAsset_ta.text = lTemplate_str.replace("/ASTRONAUT_NUM/", (this._fBetIndex_int+1));

		this._fPayoutContainer_sprt = lContainer_sprt.addChild(new Sprite);
		

		let l_tf = this._fPayout_tf = this._fPayoutContainer_sprt.addChild(new NonWobblingTextField);
		l_tf.fontName = "fnt_nm_barlow_bold";
		l_tf.fontSize = 23;
		l_tf.fontColor = 0xffffff;
		l_tf.setAlign(AlignDescriptor.LEFT, AlignDescriptor.MIDDLE);
		l_tf.position.x = 0;

		this._fPayoutContainer_sprt.position.set(0, 20);

		let lAutoEjectAtLabelAsset_ta = this._fAutoEjectAtLabelAsset_ta = I18.generateNewCTranslatableAsset("TAPayoutAutoEjectAtLabel");
		lContainer_sprt.addChild(lAutoEjectAtLabelAsset_ta);
		this._fAutoEjectTextTemplate_str = lAutoEjectAtLabelAsset_ta.text;
		lAutoEjectAtLabelAsset_ta.position.set(0, 37);
		
		let lPayoutCollectedLabelAsset_ta = this._fPayoutLabel_ta = I18.generateNewCTranslatableAsset("TAPayoutCollectedLabel");
		lContainer_sprt.addChild(lPayoutCollectedLabelAsset_ta);
		lPayoutCollectedLabelAsset_ta.position.set(0, 37);

		let l_gr = new PIXI.Graphics();
		l_gr = new PIXI.Graphics().beginFill(0xefc033).drawRoundedRect(-35, -9, 70, 18, 3).endFill();
		
		let lEject_btn = this._fEject_btn = l_gpv.ejectButtonsContainer.addChild(new GameButton(l_gr, "TAPayoutEjectButtonLabel", true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lEject_btn.visible = false;
		
		lEject_btn.on("pointerclick", this._onEjectBtnClicked, this);
	}

	_onEjectBtnClicked(event)
	{
		if (!this._fCurBetInfo_bi)
		{
			throw new Error('Attempt to eject for undefined bet');
			return;
		}

		this.emit(GameplayStateScreenProgressSinglePayoutView.EVENT_ON_EJECT_INITIATED, {betInfo: this._fCurBetInfo_bi});
	}
}

export default GameplayStateScreenProgressSinglePayoutView;