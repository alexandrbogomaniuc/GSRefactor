import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class TargetingInfo extends SimpleUIInfo {

	constructor() {
		super();

		this._fTargetEnemyId_int = null;
		this._fIsPaused_bl = false;
		this._fIsLookingForATarget_bl = false;
	}

	i_resetTarget()
	{
		this.targetEnemyId = null;
	}

	set targetEnemyId(aTargetEnemyId_int)
	{
		this._fTargetEnemyId_int = aTargetEnemyId_int;
	}

	get targetEnemyId()
	{
		return this._fTargetEnemyId_int;
	}
	
	get isTargetEnemyDefined()
	{
		return this._fTargetEnemyId_int !== null;
	}

	get isActive()
	{
		return (this.isTargetEnemyDefined || this.lookingForATarget) && !this.isPaused;
	}

	get isActiveTargetPaused()
	{
		return (this.isTargetEnemyDefined || this.lookingForATarget) && this.isPaused;
	}

	set isPaused(aValue_bl)
	{
		this._fIsPaused_bl = aValue_bl;
	}

	get isPaused()
	{
		return this._fIsPaused_bl;
	}

	get lookingForATarget()
	{
		return this._fIsLookingForATarget_bl;
	}

	set lookingForATarget(aValue_bl)
	{
		this._fIsLookingForATarget_bl = aValue_bl;
	}

	destroy()
	{
		this._fTargetEnemyId_int = null;
		this._fIsPaused_bl = undefined;
		
		super.destroy();
	}
}

export default TargetingInfo