import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class InfoPanelInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._fRoomId_num = null;
		this._fRoundId_num = null;
		this._fCostPerBullet_num = null;
		this._fMessageNickname_str = null;
		this._fMessageEnemy_str = null;
		this._latency_float = null;
	}

	set roomId(aVal_num)
	{
		this._fRoomId_num = aVal_num;
	}

	get roomId()
	{
		return this._fRoomId_num;
	}

	set roundId(aVal_num)
	{
		this._fRoundId_num = aVal_num;
	}

	get roundId()
	{
		return this._fRoundId_num;
	}

	set cpb(aVal_num)
	{
		this._fCostPerBullet_num = aVal_num;
	}

	get cpb()
	{
		return this._fCostPerBullet_num;
	}

	set messageNickname(aVal_str)
	{
		this._fMessageNickname_str = aVal_str;
	}

	get messageNickname()
	{
		return this._fMessageNickname_str;
	}

	set messageEnemy(aVal_str)
	{
		this._fMessageEnemy_str = aVal_str;
	}

	get messageEnemy()
	{
		return this._fMessageEnemy_str;
	}

	set latency(aVal_float)
	{
		this._latency_float = aVal_float;
	}

	get latency()
	{
		return this._latency_float || 0;
	}


	destroy()
	{
		super.destroy();

		this._fRoomId_num = null;
		this._fRoundId_num = null;
		this._fCostPerBullet_num = null;
		this._fMessageNickname_str = null;
		this._fMessageEnemy_str = null;
		this._latency_float = null;
	}
}

export default InfoPanelInfo;