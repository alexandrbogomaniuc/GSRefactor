import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

class HvEnemyInfo extends SimpleInfo{
	constructor(aEnemyData_obj)
	{
		super();

		this._fEnemyId_num = undefined;
		this._fEnemyData_obj = null;
		this._fParentEnemyId_num = undefined;
		this._fTimeCreated_num = 0; // the time when NewEnemy server message received
		this._fTimeAdded_num = undefined; // the time when the enemy has been added to the scene and ready to go

		this._fParentEnemyLastPosition_pt = null;
		this._fParentEnemyLastAngle_num = undefined;

		this.enemyId = aEnemyData_obj.id;
		this.enemyData = aEnemyData_obj;
		this._fParentEnemyId_num = aEnemyData_obj.parentEnemyId;
		this.timeCreated = (new Date()).getTime();
	}

	set enemyId(aEnemyId_num)
	{
		this._fEnemyId_num = aEnemyId_num;
	}

	get enemyId()
	{
		return this._fEnemyId_num;
	}

	set enemyData(aEnemyData_obj)
	{
		this._fEnemyData_obj = aEnemyData_obj;
	}

	get enemyData()
	{
		return this._fEnemyData_obj;
	}

	set parentEnemyId(aParentEnemyId_int)
	{
		this._fParentEnemyId_num = aParentEnemyId_int;
	}

	get parentEnemyId()
	{
		return this._fParentEnemyId_num;
	}

	set timeCreated(aTimeCreated_num)
	{
		this._fTimeCreated_num = aTimeCreated_num;
	}

	get timeCreated()
	{
		return this._fTimeCreated_num;
	}

	set timeAdded(aTimeAdded_num)
	{
		this._fTimeAdded_num = aTimeAdded_num;
	}

	get timeAdded()
	{
		return this._fTimeAdded_num;
	}

	set parentEnemyLastPosition(aValue_pt)
	{
		this._fParentEnemyLastPosition_pt = aValue_pt;
	}

	get parentEnemyLastPosition()
	{
		return this._fParentEnemyLastPosition_pt;
	}

	set parentEnemyLastAngle(aValue_num)
	{
		this._fParentEnemyLastAngle_num = aValue_num;
	}

	get parentEnemyLastAngle()
	{
		return this._fParentEnemyLastAngle_num;
	}

	destroy()
	{
		this._fEnemyId_num = undefined;
		this._fEnemyData_obj = null;
		this._fParentEnemyId_num = undefined;
		this._fTimeCreated_num = undefined;
		this._fTimeAdded_num = undefined;

		this.enemyId = undefined;
		this.enemyData = null;
		this.timeCreated = undefined;

		super.destroy();
	}

}

export default HvEnemyInfo;