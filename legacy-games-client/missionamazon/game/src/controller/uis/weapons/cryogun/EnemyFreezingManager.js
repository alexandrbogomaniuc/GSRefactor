import EventDispatcher from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/events/EventDispatcher';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

class EnemyFreezingManager extends EventDispatcher {

	static get EVENT_ON_FREEZING_TIMER_COMPLETED() { return 'EVENT_ON_FREEZING_TIMER_COMPLETED' }

	i_activateFreezing(aFreezeTime_num, aIsAnimated_bl = true)
	{
		this._activateFreezing(aFreezeTime_num, aIsAnimated_bl);
	}

	i_deactivateFreezeing()
	{
		//immediately
		this._deactivateFreezing();
	}

	constructor(aEnemyId_int)
	{
		super();
		this._fEnemyId_int = aEnemyId_int;
		this._fFreezeTime_num = undefined;
		this._fIsAnimated_bl = undefined;

		this._fFreezingTimer_t = null;
	}

	_activateFreezing(aFreezeTime_num, aIsAnimated_bl = true)
	{
		//destroy previous freezing timer if any
		if (this._fFreezingTimer_t)
		{
			this._fFreezingTimer_t.destructor();
		}

		this._fFreezeTime_num = aFreezeTime_num;
		this._fIsAnimated_bl = aIsAnimated_bl;

		this._fFreezingTimer_t = new Timer(this._deactivateFreezing.bind(this), this._fFreezeTime_num);

	}

	_deactivateFreezing()
	{
		this._fFreezingTimer_t && this._fFreezingTimer_t.destructor();
		this._fFreezingTimer_t = null;

		this.emit(EnemyFreezingManager.EVENT_ON_FREEZING_TIMER_COMPLETED, {enemyId: this._fEnemyId_int});
	}

	destroy()
	{
		this._fFreezingTimer_t && this._fFreezingTimer_t.destructor();
		this._fFreezingTimer_t = null;

		this.removeAllListeners();
	}
}

export default EnemyFreezingManager