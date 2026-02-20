import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import GameplayStateScreenProgressPayoutsView from '../GameplayStateScreenProgressPayoutsView';
import BattlegroundGameplayStateScreenProgressSinglePayoutView from './BattlegroundGameplayStateScreenProgressSinglePayoutView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class BattlegroundGameplayStateScreenProgressPayoutsView extends Sprite
{
	static get EVENT_ON_EJECT_INITIATED()		{ return GameplayStateScreenProgressPayoutsView.EVENT_ON_EJECT_INITIATED };
	
	constructor()
	{
		super();

		this._fPayout_bgsspspv = null;

		//CONTENT...
		this._addContent();
		//...CONTENT
	}

	updateArea()
	{
		this._fPayout_bgsspspv.updateArea();
	}

	adjust(aMasterBets_bi_arr)
	{
		let lBetInfo_bi = (!!aMasterBets_bi_arr && !!aMasterBets_bi_arr.length) ?  aMasterBets_bi_arr[0] : null;
		let l_bgsspspv = this._fPayout_bgsspspv;

		if (lBetInfo_bi && lBetInfo_bi.isConfirmedMasterBet)
		{
			l_bgsspspv.adjust(lBetInfo_bi);

			l_bgsspspv.visible = true;
		}
		else
		{
			l_bgsspspv.visible = false;
			l_bgsspspv.ejectButton.visible = false;
		}
	}

	_addContent()
	{
		this._fPayout_bgsspspv = this._addSinglePayout(0);
	}

	_addSinglePayout(aBetIndex_int)
	{		
		let l_bgsspspv = new BattlegroundGameplayStateScreenProgressSinglePayoutView(aBetIndex_int);
		l_bgsspspv.on(BattlegroundGameplayStateScreenProgressSinglePayoutView.EVENT_ON_EJECT_INITIATED, this.emit, this);
		l_bgsspspv.position.set(0, 26);

		this.addChild(l_bgsspspv);

		return l_bgsspspv;
	}
}

export default BattlegroundGameplayStateScreenProgressPayoutsView;