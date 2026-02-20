import SimpleUIInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo'

class BossModeHPBarInfo extends SimpleUIInfo
{
	constructor(aOptId_obj, aOptParentInfo_usi)
	{
		super(aOptId_obj, aOptParentInfo_usi);

		this._fFullHealth_num = null;
		this._fCurrentHealth_num = null;
		this._fName_str = null;
		this._fId_num = null;
	}

	get fullHealth()
	{
		return this._fFullHealth_num;
	}

	set fullHealth(aVal_num)
	{
		this._fFullHealth_num = aVal_num;
	}

	get currentHealth()
	{
		return this._fCurrentHealth_num;
	}

	set currentHealth(aVal_num)
	{
		this._fCurrentHealth_num = aVal_num;
	}

	get name()
	{
		return this._fName_str;
	}

	set name(aVal_str)
	{
		this._fName_str = aVal_str;
	}

	get id()
	{
		return this._fId_num;
	}

	set id(aVal_num)
	{
		this._fId_num = aVal_num;
	}

	destroy()
	{
		super.destroy();

		this._fFullHealth_num = null;
		this._fCurrentHealth_num = null;
		this._fName_str = null;
		this._fId_num = null;
	}
}

export default BossModeHPBarInfo