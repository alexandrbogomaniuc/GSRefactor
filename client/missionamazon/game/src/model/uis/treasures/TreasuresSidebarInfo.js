import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo'

class TreasuresSidebarInfo extends SimpleInfo
{
	constructor()
	{
		super();

		this._fCurrentGemsAmount_int_arr = [0, 0, 0, 0, 0];
		this._fQuestCompletedAnimationPlay_bl_arr = [false, false, false, false, false];
	}

	clear()
	{
		this._fQuestCompletedAnimationPlay_bl_arr = [false, false, false, false, false];
	}

	doNowQuestCompetedAnimationPlayByGemId(aGemId_num)
	{
		return this._fQuestCompletedAnimationPlay_bl_arr[aGemId_num];
	}

	setQuestCompetedAnimationPlayByGemId(aGemId_num)
	{
		this._fQuestCompletedAnimationPlay_bl_arr[aGemId_num] = true;
	}

	setQuestCompetedAnimationFreeByGemId(aGemId_num)
	{
		this._fQuestCompletedAnimationPlay_bl_arr[aGemId_num] = false;
	}

	getGemAmountById(aGemId_num)
	{
		return this._fCurrentGemsAmount_int_arr[aGemId_num];
	}

	clearGemAmountById(aGemId_num)
	{
		this._fCurrentGemsAmount_int_arr[aGemId_num] = 0;
	}

	increaseGemsAmountById(aGemId_num)
	{
		this._fCurrentGemsAmount_int_arr[aGemId_num]++;
	}

	decreaseGemsAmountById(aGemId_num)
	{
		this._fCurrentGemsAmount_int_arr[aGemId_num]--;
	}

	get currentGemsAmount()
	{
		return this._fCurrentGemsAmount_int_arr;
	}

	set currentGemsAmount(aValue_arr)
	{
		this._fCurrentGemsAmount_int_arr = aValue_arr;
	}

	i_getPrices()
	{
		return this._fPrices_obj;
	}

	/**
	 * Currently this array is reversed.
	 * @param {Object} aPrices_obj
	 */
	set prices(aPrices_obj)
	{
		this._fPrices_obj = {};

		let lLength_num = Object.keys(aPrices_obj).length;
		for (let i = 0; i < lLength_num; i++)
		{
			this._fPrices_obj[i] = aPrices_obj[lLength_num-i-1];
		}
	}

	destroy()
	{
		super.destroy();
	}
}

export default TreasuresSidebarInfo