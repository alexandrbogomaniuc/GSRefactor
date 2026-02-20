import GUSPendingOperationInfo from '../../../../../common/PIXI/src/dgphoenix/gunified/model/gameplay/GUSPendingOperationInfo';

class GamePendingOperationInfo extends GUSPendingOperationInfo
{
	constructor(aParentInfo_usi)
	{
		super(aParentInfo_usi);
	}
	
	get __isInitialPendingOperationInProgress()
	{
		return false;
	}

	destroy()
	{
		super.destroy();
	}
}

export default GamePendingOperationInfo