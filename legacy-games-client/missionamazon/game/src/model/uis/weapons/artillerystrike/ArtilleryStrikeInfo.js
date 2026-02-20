import SimpleUIInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class ArtilleryStrikeInfo extends SimpleUIInfo {

	constructor(aShotData_obj)
	{
		super();

		this._fShotData_obj = aShotData_obj;
	}

	get shotData()
	{
		return this._fShotData_obj;
	}

	get rid()
	{
		return this.shotData.rid;
	}

	get seatId()
	{
		return this.shotData.seatId;
	}

	get origX()
	{
		return this.shotData.x;
	}

	get origY()
	{
		return this.shotData.y;
	}

	get requestEnemyId()
	{
		return this.shotData.requestEnemyId;
	}

	get affectedEnemies()
	{
		return this.shotData.affectedEnemies;
	}
}

export default ArtilleryStrikeInfo;