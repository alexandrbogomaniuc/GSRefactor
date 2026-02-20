import SimpleInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

class ShotResponsesInfo extends SimpleInfo {

	get currentShotResponses() {
		return this._fCurrentShotResponses_sri_arr;
	}

	constructor() {
		super();
		this._fCurrentShotResponses_sri_arr = null;
	}

	addShotResponse(aShotResponseInfo_sri) {
		if (this._fCurrentShotResponses_sri_arr == null)
		{
			this._fCurrentShotResponses_sri_arr = [];
		}
		this._fCurrentShotResponses_sri_arr.push(aShotResponseInfo_sri);
	}

	clear() {
		while (this._fCurrentShotResponses_sri_arr && this._fCurrentShotResponses_sri_arr.length > 0)
		{
			this._fCurrentShotResponses_sri_arr.pop().destroy();
		}
		this._fCurrentShotResponses_sri_arr = null;
	}

	destroy() {
		this.clear();
		super.destroy();
	}
}

export default ShotResponsesInfo;