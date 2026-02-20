import GUPendingOperationInfo from './GUPendingOperationInfo';

class GUSPendingOperationInfo extends GUPendingOperationInfo
{
	constructor(aParentInfo_usi)
	{
		super(aParentInfo_usi);
	}

	get __isInitialOperationStatusTrackingOn()
	{
		return false;
	}
}

export default GUSPendingOperationInfo