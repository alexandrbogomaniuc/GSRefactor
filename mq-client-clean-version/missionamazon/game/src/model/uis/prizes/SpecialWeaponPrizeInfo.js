import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class SpecialWeaponPrizeInfo extends SimpleUIInfo {

	constructor(aUniqueId_int, aSpecialWeaponId_int, aSeatId_int, aStartPosition_pt)
	{
		super();

		this._fUniqueId_int = aUniqueId_int;
		this._fSpecialWeaponId_int = aSpecialWeaponId_int;
		this._fSeatId_int = aSeatId_int;
		this._fStartPosition_pt = aStartPosition_pt;

	}

	get uniqueId()
	{
		return this._fUniqueId_int;
	}

	get specialWeaponId()
	{
		return this._fSpecialWeaponId_int;
	}

	get seatId()
	{
		return this._fSeatId_int;
	}

	get isMasterSeat()
	{
		return (this.seatId === APP.playerController.info.seatId);
	}

	get startPosition()
	{
		return this._fStartPosition_pt;
	}

}

export default SpecialWeaponPrizeInfo;