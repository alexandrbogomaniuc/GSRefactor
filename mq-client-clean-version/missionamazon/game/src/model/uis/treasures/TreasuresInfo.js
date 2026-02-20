import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo'

class TreasuresInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._fCurrentGemsAmount_int_arr = [0, 0, 0, 0, 0];
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

	get currentGemsAmount()
	{
		return this._fCurrentGemsAmount_int_arr;
	}

	set currentGemsAmount(aValue_arr)
	{
		this._fCurrentGemsAmount_int_arr = aValue_arr;
	}

	clear()
	{
		this._fCurrentGemsAmount_int_arr = [0, 0, 0, 0, 0];
	}

	destroy()
	{
		super.destroy();

		this._fCurrentGemsAmount_int_arr = null;
	}
}

export default TreasuresInfo