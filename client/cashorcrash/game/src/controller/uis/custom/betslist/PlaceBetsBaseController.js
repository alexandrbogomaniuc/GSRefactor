import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import CrashAPP from '../../../../CrashAPP';
import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import PlaceBetsView from '../../../../view/uis/custom/placebets/PlaceBetsView';

class PlaceBetsBaseController extends SimpleUIController
{
	static get EVENT_ON_PLACE_BETS ()					{ return PlaceBetsView.EVENT_ON_PLACE_BETS; }
	static get EVENT_ON_CANCEL_BET ()					{ return PlaceBetsView.EVENT_ON_CANCEL_BET; }
	static get EVENT_ON_EJECT_INITIATED()				{ return PlaceBetsView.EVENT_ON_EJECT_INITIATED; };
	static get EVENT_ON_CANCEL_AUTO_EJECT_INITIATED()	{ return PlaceBetsView.EVENT_ON_CANCEL_AUTO_EJECT_INITIATED; };
	static get EVENT_ON_EDIT_AUTO_EJECT_INITIATED()		{ return PlaceBetsView.EVENT_ON_EDIT_AUTO_EJECT_INITIATED; };

	init()
	{
		super.init();
	}

	//INIT...
	constructor(...args)
	{
		super(...args);
	}

	__initControlLevel()
	{
		super.__initControlLevel();

        APP.on(CrashAPP.EVENT_ON_CURRENCY_INFO_UPDATED, this._onCurrencyInfoUpdated, this);
        APP.on(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this);
	}

    __initViewLevel()
    {
        super.__initViewLevel();

        this.view.on(PlaceBetsView.EVENT_ON_PLACE_BETS, this.emit, this);
        this.view.on(PlaceBetsView.EVENT_ON_CANCEL_BET, this.emit, this);
		this.view.on(PlaceBetsView.EVENT_ON_EJECT_INITIATED, this.emit, this);
        this.view.on(PlaceBetsView.EVENT_ON_CANCEL_AUTO_EJECT_INITIATED, this.emit, this);
        this.view.on(PlaceBetsView.EVENT_ON_EDIT_AUTO_EJECT_INITIATED, this.emit, this);
    }
	//...INIT

	_onTickTime(event)
	{
		this.validate();
	}
	
	_onCurrencyInfoUpdated()
	{
		this.view._setCurrencySymbols();
	}

	validate()
	{
		this.view && this.view.validate();
	}
}

export default PlaceBetsBaseController