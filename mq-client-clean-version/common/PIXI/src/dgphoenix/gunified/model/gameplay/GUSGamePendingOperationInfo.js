import GUSPendingOperationInfo from './GUSPendingOperationInfo';

class GUSGamePendingOperationInfo extends GUSPendingOperationInfo
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

export default GUSGamePendingOperationInfo