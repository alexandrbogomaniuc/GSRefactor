import SimpleInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

class ArtilleryStrikesInfo extends SimpleInfo {

	i_clearAll()
	{
		this._clearAll();
	}

	i_increaseActiveArtilleryStrikesCounter()
	{
		this._fActiveArtilleryStrikesCounter_int++;
	}

	i_decreaseActiveArtilleryStrikesCounter()
	{
		this._fActiveArtilleryStrikesCounter_int--;
	}

	get activeArtilleryStrikesCounter()
	{
		return this._fActiveArtilleryStrikesCounter_int;
	}

	constructor() {
		super();

		this._fActiveArtilleryStrikesCounter_int = 0;
	}

	_clearAll()
	{
		this._fActiveArtilleryStrikesCounter_int = 0;
	}
}

export default ArtilleryStrikesInfo;