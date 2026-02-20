import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import NumberValueFormat from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import TreasuresSimpleCrate from '../../../../ui/TreasuresSimpleCrate';
import ContentItemInfo from '../../../../model/uis/content/ContentItemInfo';
import EnemyTreasuresAwardView from '../../../../view/uis/treasures/EnemyTreasuresAwardView';
import Crate from '../../../../ui/Crate';

const GEMS = [
	{
		id: 0
	},
	{
		id: 1
	},
	{
		id: 2
	},
	{
		id: 3
	}
];

class BossGemsAwardsView extends Sprite
{
	static get BOSS_GEM_LANDED()			{return "BOSS_GEM_LANDED";}
	static get EVENT_ON_GEM_ACHIEVED_PANEL(){return TreasuresSimpleCrate.EVENT_ON_GEM_ACHIEVED_PANEL;}

	constructor(aGems_int_arr)
	{
		super();

		this._fGems_arr_int = aGems_int_arr;
		this._fAwardQueue_int_arr = [];

		this._setAwardQueue();
		this._animateAwards();
	}

	_animateAwards()
	{
		let crate = new TreasuresSimpleCrate({x: 0, y: 0}, this._crateContentItems(), this, true);
		crate.on(TreasuresSimpleCrate.ON_GEM_LANDED, this._onFinishAward, this);
		crate.on(TreasuresSimpleCrate.EVENT_ON_GEM_ACHIEVED_PANEL, this._onGemAchievedPanel, this);

		this._crate = this.addChild(crate);
	}

	_onGemAchievedPanel(e)
	{
		let lData_obj = {gemId: e.gemId, position: {x: e.landGlobalX, y: e.landGlobalY}};

		this.emit(BossGemsAwardsView.EVENT_ON_GEM_ACHIEVED_PANEL, {data: lData_obj});
	}

	_onFinishAward(e)
	{
		let lData_obj = {gemId: e.gemId, position: {x: e.landGlobalX, y: e.landGlobalY}};

		this.emit(BossGemsAwardsView.BOSS_GEM_LANDED, {data: lData_obj});
	}

	_crateContentItems()
	{
		let l_arr = [];

		for (let i = 0; i < this._fAwardQueue_int_arr.length; i++)
		{
			let lGemAssetId_num = GEMS[this._fAwardQueue_int_arr[i]].id;
			let itemInfo = new ContentItemInfo(ContentItemInfo.TYPE_TREASURE, APP.playerController.info.seatId, null, undefined, EnemyTreasuresAwardView.getBossGemAssetTexture(lGemAssetId_num), null, null, lGemAssetId_num, EnemyTreasuresAwardView.getBossGemGlowAssetTexture(lGemAssetId_num));
			l_arr.push(itemInfo);
		}

		return l_arr;
	}

	_setAwardQueue()
	{
		if(this._fGems_arr_int)
		{
			for (let i = 0; i < this._fGems_arr_int.length; i++)
			{
				for (let j = 0; j < this._fGems_arr_int[i]; j++)
				{
					this._fAwardQueue_int_arr.push(i);
				}
			}
		}
	}

	destroy()
	{
		super.destroy();

		this._fGems_arr_int = null;
		this._fAwardQueue_int_arr = null;
	}
}

export default BossGemsAwardsView;