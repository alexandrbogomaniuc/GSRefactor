import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class AutoTargetingSwitcherInfo extends SimpleUIInfo {

	static get STATE_ON() { return 'on'; }
	static get STATE_OFF() { return 'off'; }

	constructor() {
		super();

		this._fState_str = AutoTargetingSwitcherInfo.STATE_OFF;
	}

	get isOn()
	{
		return this._fState_str === AutoTargetingSwitcherInfo.STATE_ON;
	}

	get isOff()
	{
		return this._fState_str === AutoTargetingSwitcherInfo.STATE_OFF;
	}

	switchState()
	{
		if (this._fState_str === AutoTargetingSwitcherInfo.STATE_OFF)
		{
			this._fState_str = AutoTargetingSwitcherInfo.STATE_ON;
		}
		else
		{
			this._fState_str = AutoTargetingSwitcherInfo.STATE_OFF;
		}
	}

	get state()
	{
		return this._fState_str;
	}
}

export default AutoTargetingSwitcherInfo;