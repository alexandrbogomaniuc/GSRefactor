import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

class GameSettingsInfo extends SimpleInfo {

	constructor()
	{
		super();
		this._fWeaponsSavingAllowed_bl = undefined;
	}

	get weaponsSavingAllowed()
	{
		return this._fWeaponsSavingAllowed_bl;
	}

	set weaponsSavingAllowed(aValue_bl)
	{
		this._fWeaponsSavingAllowed_bl = aValue_bl;
	}

	get isBuyInNotMandatoryInRound()
	{
		return this.weaponsSavingAllowed == true;
	}
}

export default GameSettingsInfo;