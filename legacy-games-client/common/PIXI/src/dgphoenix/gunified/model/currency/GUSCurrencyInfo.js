import { APP } from '../../../unified/controller/main/globals';
import CurrencyInfo from './CurrencyInfo';

class GUSCurrencyInfo extends CurrencyInfo
{
	constructor()
	{
		super();
	}

	__isCurrencyMarkExcludeRequired()
	{
		return super.__isCurrencyMarkExcludeRequired() || APP.tournamentModeController.info.isTournamentMode;
	}
}

export default GUSCurrencyInfo;