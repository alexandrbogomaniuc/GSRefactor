import GUSPendingOperationInfo from './GUSPendingOperationInfo';

class GUSLobbyPendingOperationInfo extends GUSPendingOperationInfo
{
	constructor(aParentInfo_usi)
	{
		super(aParentInfo_usi);

		this._fIsRoomPendingOperationInProgress_bl = undefined;
	}
	
	get isRoomPendingOperationInProgress()
	{
		return this._fIsRoomPendingOperationInProgress_bl;
	}

	set isRoomPendingOperationInProgress(value)
	{
		this._fIsRoomPendingOperationInProgress_bl = value;
	}

	get isRoomPendingOperationProgressStatusDefined()
	{
		return this.isRoomPendingOperationInProgress !== undefined;
	}

	get __isInitialPendingOperationInProgress()
	{
		return undefined;
	}

	destroy()
	{
		this._fIsRoomPendingOperationInProgress_bl = undefined;

		super.destroy();
	}
}

export default GUSLobbyPendingOperationInfo