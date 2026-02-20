import EternalSmoke from '../EternalSmoke';

class EternalPlazmaSmoke extends EternalSmoke {
	constructor(indigoSmoke = false, indigoBlendMode = "normal"){
		super(indigoSmoke, indigoBlendMode);
	}

	__getSmokeAssetName(aIndigoSmoke)
	{
		return aIndigoSmoke ? 'weapons/InstantKill/plasmasmoke_indigo' : 'weapons/InstantKill/plasmasmoke';
	}
}

export default EternalPlazmaSmoke;