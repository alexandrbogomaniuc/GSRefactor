import SimpleInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class PendingOperationInfo extends SimpleInfo
{
	constructor(aParentInfo_usi)
	{
		super(undefined, aParentInfo_usi);

		this._fIsPendingOperationInProgress_bl = false;
		this._fIsFatalPendingOperation_bl = false;
	}

	get isPendingOperationInProgress()
	{
		return this._fIsPendingOperationInProgress_bl;
	}

	set isPendingOperationInProgress(value)
	{
		this._fIsPendingOperationInProgress_bl = value;
	}

	get isPendingOperationHandlingSupported()
	{
		return true;
	}

	get isFatalPendingOperation()
	{
		return this._fIsFatalPendingOperation_bl;
	}

	set isFatalPendingOperation(value)
	{
		this._fIsFatalPendingOperation_bl = value;
	}

	destroy()
	{
		this._fIsPendingOperationInProgress_bl = undefined;
		this._fIsFatalPendingOperation_bl = undefined;

		super.destroy();
	}
}

export default PendingOperationInfo