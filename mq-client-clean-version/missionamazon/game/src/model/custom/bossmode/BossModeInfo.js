import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo'

class BossModeInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._fBossNumberShots_int = 0;
		this._fBossSummoned_bl = false;
		// In cases when player joins mid-round or unpauses, we don't know wheather
		// boss has been summoned or not until we get bossNumberShots from room info
		this._fBossSummonedCheckRequired_bl = true;
	}

	get bossNumberShots()
	{
		return this._fBossNumberShots_int;
	}

	set bossNumberShots(aValue_num)
	{
		this._fBossNumberShots_int = aValue_num;
		if (this.bossSummonedCheckRequired)
		{
			this.bossSummoned = (aValue_num > 0);
		}
	}

	get bossSummoned()
	{
		return this._fBossSummoned_bl;
	}

	set bossSummoned(aValue_bl)
	{
		this._fBossSummoned_bl = !!aValue_bl;
	}

	get bossSummonedCheckRequired()
	{
		return this._fBossSummonedCheckRequired_bl;
	}

	set bossSummonedCheckRequired(aValue_bl)
	{
		this._fBossSummonedCheckRequired_bl = !!aValue_bl;
	}

	destroy()
	{
		super.destroy();

		this._fBossNumberShots_int = null;
		this._fBossSummoned_bl = null;
		this._fBossSummonedCheckRequired_bl = null;
	}
}

export default BossModeInfo