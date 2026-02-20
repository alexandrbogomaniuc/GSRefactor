import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayInfo from '../../../model/gameplay/GameplayInfo';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { MAX_MASTER_BETS_AMOUNT } from '../../../model/gameplay/bets/BetsInfo';
import GameplayStateScreenProgressSinglePayoutView from './GameplayStateScreenProgressSinglePayoutView';
import GameButton from '../../../ui/GameButton';
import Button from '../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';

class GameplayStateScreenProgressPayoutsView extends Sprite
{
	static get EVENT_ON_EJECT_INITIATED()		{ return GameplayStateScreenProgressSinglePayoutView.EVENT_ON_EJECT_INITIATED };
	static get EVENT_ON_EJECT_ALL_INITIATED()	{ return 'EVENT_ON_EJECT_ALL_INITIATED' };
	
	constructor()
	{
		super();

		GameplayStateScreenProgressPayoutsView.PAYOUTS_DISTANCE = 25;

		this._fContainer_sprt = null;
		this._fPayoutsContainer_sprt = null;
		this._fPayoutsLabel_ta = null;
		this._fPayouts_gsspspv_arr = [];

		//CONTENT...
		this._addContent();
		//...CONTENT
	}

	updateArea()
	{
		GameplayStateScreenProgressSinglePayoutView.calcMaxWidth();
	}

	adjust()
	{
		let l_gpv = APP.gameController.gameplayController.view;
		let l_gpi = APP.gameController.gameplayController.info;
		let lContainer_sprt = this._fContainer_sprt

		let lMasterPlayer_gpi = l_gpi.gamePlayersInfo.masterPlayerInfo;
		let lMasterBets_bi_arr = !!lMasterPlayer_gpi ? lMasterPlayer_gpi.activeBets : [];
		lMasterBets_bi_arr = lMasterBets_bi_arr || [];

		let lVisibleEjectsAmount_int = 0;
		let lEnabledEjectsAmount_int = 0;
		let lActiveIndexes_num_arr = [];
		for (let i=0; i<lMasterBets_bi_arr.length; i++)
		{
			let lBetInfo_bi = lMasterBets_bi_arr[i];
			let lBetIndex_num = lBetInfo_bi.betIndex;
			
			let l_gsspspv = this._fPayouts_gsspspv_arr[lBetIndex_num];
			l_gsspspv.adjust(lBetInfo_bi);

			if (l_gsspspv.ejectButton.visible)
			{
				lVisibleEjectsAmount_int++;
			}

			if (l_gsspspv.ejectButton.enabled)
			{
				lEnabledEjectsAmount_int++;
			}
			
			lActiveIndexes_num_arr.push(lBetIndex_num);
		}

		let lEjectAll_btn = this._fEjectAll_btn;
		lEjectAll_btn.visible = lVisibleEjectsAmount_int > 0;
		lEjectAll_btn.enabled = lEnabledEjectsAmount_int > 0;

		if (lEjectAll_btn.visible)
		{
			let lLocEjectAllY_num = l_gpi.isPreLaunchFlightRequired ? 105 : 120;
			let lEjectPos_p = lContainer_sprt.localToLocal(0, lLocEjectAllY_num, l_gpv.ejectButtonsContainer);
			lEjectAll_btn.position.set(lEjectPos_p.x, lEjectPos_p.y);
		}

		// DEBUG...
		// lActiveIndexes_num_arr = [0, 1, 2]
		// ...DEBUG

		let lCurActiveCounter_num = 0;
		let lRightBorder_num = 0;
		for (let i=0; i<this._fPayouts_gsspspv_arr.length; i++)
		{
			let l_gsspspv = this._fPayouts_gsspspv_arr[i];

			if (lActiveIndexes_num_arr.indexOf(l_gsspspv.betIndex) >= 0)
			{
				l_gsspspv.visible = true;
				let lWidth_num = GameplayStateScreenProgressSinglePayoutView.MAX_WIDTH;
				l_gsspspv.position.x = lWidth_num/2 + (lWidth_num+GameplayStateScreenProgressPayoutsView.PAYOUTS_DISTANCE)*lCurActiveCounter_num;

				lCurActiveCounter_num++;
				lRightBorder_num = l_gsspspv.position.x + lWidth_num/2;
			}
			else
			{
				l_gsspspv.visible = false;
				l_gsspspv.ejectButton.visible = false;
			}
		}

		let lPayoutsContainer_sprt = this._fPayoutsContainer_sprt;
		lPayoutsContainer_sprt.position.x = -lRightBorder_num/2;
	}

	_addContent()
	{
		let l_gpv = APP.gameController.gameplayController.view;
		let lContainer_sprt = this._fContainer_sprt = this.addChild(new Sprite);
		let lPayoutsContainer_sprt = this._fPayoutsContainer_sprt = lContainer_sprt.addChild(new Sprite);
		// DEBUG...
		// lPayoutsContainer_sprt.addChild(new PIXI.Graphics).beginFill(0x0000ff, 0.5).drawRect(0, 0, 100, 100).endFill();
		// ...DEBUG

		let lPayoutLabelAsset_ta = this._fPayoutsLabel_ta = I18.generateNewCTranslatableAsset("TACurrentPayoutLabel");
		lContainer_sprt.addChild(lPayoutLabelAsset_ta);
		lPayoutLabelAsset_ta.position.set(0, 0);

		for (let i=0; i<MAX_MASTER_BETS_AMOUNT; i++)
		{
			let l_gsspspv = this._addSinglePayout(i);

			this._fPayouts_gsspspv_arr.push(l_gsspspv);
		}

		let l_gr = new PIXI.Graphics().beginFill(0xc12d26).drawRoundedRect(-55, -11, 110, 22, 3).endFill();
		let lEjectAll_btn = this._fEjectAll_btn = l_gpv.ejectButtonsContainer.addChild(new GameButton(l_gr, "TAPayoutEjectAllRemainingButtonLabel", true, false, null, Button.BUTTON_TYPE_COMMON, true));
		lEjectAll_btn.enabled = false;
		lEjectAll_btn.visible = false;
		lEjectAll_btn.on("pointerclick", this._onEjectAllBtnClicked, this);
	}

	_addSinglePayout(aBetIndex_int)
	{
		let lPayoutsContainer_sprt = this._fPayoutsContainer_sprt
		
		let l_gsspspv = new GameplayStateScreenProgressSinglePayoutView(aBetIndex_int);
		l_gsspspv.on(GameplayStateScreenProgressSinglePayoutView.EVENT_ON_EJECT_INITIATED, this.emit, this);
		l_gsspspv.position.set(0, 26);

		lPayoutsContainer_sprt.addChild(l_gsspspv);

		return l_gsspspv;
	}

	_onEjectAllBtnClicked(event)
	{
		this.emit(GameplayStateScreenProgressPayoutsView.EVENT_ON_EJECT_ALL_INITIATED, {});
	}
}

export default GameplayStateScreenProgressPayoutsView;