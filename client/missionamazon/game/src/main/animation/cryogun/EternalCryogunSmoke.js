import EternalSmoke from '../EternalSmoke';

class EternalCryogunSmoke extends EternalSmoke {
	constructor(indigoSmoke = false, indigoBlendMode = "normal"){
		super(indigoSmoke, indigoBlendMode);
	}

	__getSmokeAssetName(aIndigoSmoke)
	{
		return aIndigoSmoke ? 'weapons/Cryogun/plasmasmoke_indigo' : 'weapons/Cryogun/plasmasmoke';
	}
}

export default EternalCryogunSmoke;