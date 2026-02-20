import SimpleInfo from '../../../unified/model/base/SimpleInfo';
import { APP } from '../../../unified/controller/main/globals';

class GUPendingOperationInfo extends SimpleInfo
{
	constructor(aParentInfo_usi)
	{
		super(undefined, aParentInfo_usi);

		this._fIsPendingOperationHandlingSupported_bl = false;

		this._fIsPendingOperationInProgress_bl = this.__isInitialPendingOperationInProgress;
		this._fIsFatalPendingOperation_bl = false;
		this._fIsOperationStatusTrackingOn_bl = this.__isInitialOperationStatusTrackingOn;
	}

	get isPendingOperationInProgress()
	{
		return this._fIsPendingOperationInProgress_bl;
	}

	set isPendingOperationInProgress(value)
	{
		this._fIsPendingOperationInProgress_bl = value;
	}

	get isPendingOperationProgressStatusDefined()
	{
		return this._fIsPendingOperationInProgress_bl !== undefined;
	}

	get isPendingOperationStatusCheckInProgress()
	{
		return this.isPendingOperationHandlingSupported
				&& (!this.isPendingOperationProgressStatusDefined || this.isPendingOperationInProgress);
	}

	resetPendingOperationStatus()
	{
		this._fIsPendingOperationInProgress_bl = undefined;
	}

	get isPendingOperationHandlingSupported()
	{
		return this._fIsPendingOperationHandlingSupported_bl;
	}

	set isPendingOperationHandlingSupported(value)
	{
		this._fIsPendingOperationHandlingSupported_bl = value;
	}

	get isFatalPendingOperation()
	{
		return this._fIsFatalPendingOperation_bl;
	}

	set isFatalPendingOperation(value)
	{
		this._fIsFatalPendingOperation_bl = value;
	}

	get isOperationStatusTrackingOn()
	{
		return this._fIsOperationStatusTrackingOn_bl;
	}

	set isOperationStatusTrackingOn(value)
	{
		this._fIsOperationStatusTrackingOn_bl = value;
	}

	get __isInitialOperationStatusTrackingOn()
	{
		return true;
	}

	get __isInitialPendingOperationInProgress()
	{
		return false;
	}

	destroy()
	{
		this._fIsPendingOperationHandlingSupported_bl = undefined;

		this._fIsPendingOperationInProgress_bl = undefined;
		this._fIsFatalPendingOperation_bl = undefined;
		this._fIsOperationStatusTrackingOn_bl = undefined;

		super.destroy();
	}
}

export default GUPendingOperationInfo