import CriticalHitMultiplierView from './CriticalHitMultiplierView'

class CriticalHitMultiplierGlowView extends CriticalHitMultiplierView
{
	constructor()
	{
		super();
	}

	_getAssetName()
	{
		return "critical_hit/critical_numbers_glow";
	}

	destroy()
	{
		super.destroy();
	}
}

export default CriticalHitMultiplierGlowView