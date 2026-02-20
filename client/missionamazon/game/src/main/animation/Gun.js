import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

const STATES = 	{
					STATE_IDLE: 0,
					STATE_RELOAD: 1,
					STATE_SHOT: 2
				}

class Gun extends Sprite {

	static get EVENT_ON_RELOADED() { return 'EVENT_ON_RELOADED' };
	static get EVENT_ON_SHOT_COMPLETED() { return 'EVENT_ON_SHOT_COMPLETED' };	
	static get EVENT_ON_RESET() { return 'EVENT_ON_RESET' };
	
	idle()
	{
		this.state = STATES.STATE_IDLE;
	}

	reload()
	{
		this.state = STATES.STATE_RELOAD;
	}

	get isCharged()
	{
		return false;
	}

	shot()
	{
		this.state = STATES.STATE_SHOT;
	}

	reset()
	{
		this.emit(Gun.EVENT_ON_RESET);
		/*TODO [os]: override in subclass*/
	}

	get isIdleState()
	{
		return this.state === STATES.STATE_IDLE;
	}

	get isReloadState()
	{
		return this.state === STATES.STATE_RELOAD;
	}

	get isShotState()
	{
		return this.state === STATES.STATE_SHOT;
	}

	set state(aState_int)
	{
		if (this._fState_int !== aState_int)
		{
			this._fState_int = aState_int;
			this._invalidateState();
		}
	}

	get state()
	{
		return this._fState_int;
	}

	constructor()
	{
		super();
		this._fState_int = undefined;
	}

	_invalidateState()
	{
		switch (this._fState_int)
		{
			case STATES.STATE_IDLE:
				this._initIdleState();
				break;
			case STATES.STATE_RELOAD:
				this._initReloadState();
				break;
			case STATES.STATE_SHOT:
				this._initShotState();
				break;
		}
	}

	_initIdleState()
	{
		throw new Error('Gun :: _initIdleState >> is an abstract method');
	}

	_initReloadState()
	{
		throw new Error('Gun :: _initReloadState >> is an abstract method');
	}

	_initShotState()
	{
		throw new Error('Gun :: _initShotState >> is an abstract method');
	}
}

export default Gun;