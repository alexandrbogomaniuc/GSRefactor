import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class EnemyIndicatorsInfo extends SimpleUIInfo
{
	constructor(aInfo_obj)
	{
		super();

		this._fTypeId_num = null;
		this._fEnergy_num = null;
		this._fId_num = null;
		this._fSkin_num = null;
		this._fFullEnergy_num = null;
		this._fEnemyName_str = null;

		this.typeId = aInfo_obj.typeId;
		this.energy = aInfo_obj.energy;
		this.fullEnergy = aInfo_obj.fullEnergy;
		this.id = aInfo_obj.id;
		this.skin = aInfo_obj.skin;
		this.enemyName = aInfo_obj.enemyName;
	}

	set fullEnergy(afullEnergy_num)
	{
		this._fFullEnergy_num = afullEnergy_num;
	}

	get fullEnergy()
	{
		//DEBUG...
		//return 100;
		//...DEBUG
		return this._fFullEnergy_num;
	}

	set skin(aSkin_num)
	{
		this._fSkin_num = aSkin_num;
	}

	get skin()
	{
		return this._fSkin_num;
	}

	set typeId(aTypeId_obj)
	{
		this._fTypeId_num = aTypeId_obj;
	}

	get typeId()
	{
		return this._fTypeId_num;
	}

	set energy(aEnergy_num)
	{
		this._fEnergy_num = aEnergy_num;
	}

	get energy()
	{
		//DEBUG...
		//return Math.round(Math.random() * this.fullEnergy);
		//...DEBUG
		return this._fEnergy_num;
	}

	set id(aId_num)
	{
		this._fId_num = aId_num;
	}

	get id()
	{
		return this._fId_num;
	}

	set enemyName(value)
	{
		this._fEnemyName_str = value;
	}

	get enemyName()
	{
		return this._fEnemyName_str;
	}

	destroy()
	{
		super.destroy();

		this._fTypeId_num = null;
		this._fEnergy_num = null;
		this._fId_num = null;
		this._fSkin_num = null;
		this._fEnemyName_str = null;
	}
}

export default EnemyIndicatorsInfo;