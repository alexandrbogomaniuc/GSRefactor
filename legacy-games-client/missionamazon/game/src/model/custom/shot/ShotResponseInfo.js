import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

class ShotResponseInfo extends SimpleInfo {

	constructor(data, requestData = null)
	{
		super();
		this._fData_obj = data;
		this._fRequestData_obj = requestData;

		this._fClass_str = data.class;
	}

	get data() {
		return this._fData_obj;
	}

	get requestData()
	{
		return this._fRequestData_obj;
	}

	get responseClass() {
		return this._fClass_str;
	}

	get isHit()
	{
		return this.responseClass === "Hit";
	}

	get isMiss()
	{
		return this.responseClass === "Miss";
	}

	get enemyId()
	{
		return this.data.enemyId;
	}

	set lastResult(aValue_bl)
	{
		this.data.lastResult = aValue_bl;
	}

	get lastResult()
	{
		return Boolean(this.data.lastResult);
	}

	get win()
	{
		return this.data.win;
	}

	set win(aValue_num)
	{
		this.data.win = aValue_num;
	}

	get score()
	{
		return this.data.score;
	}

	set score(aValue_num)
	{
		this.data.score = aValue_num;
	}

	get damage()
	{
		return this.data.damage;
	}

	set damage(aValue_num)
	{
		this.data.damage = aValue_num;
	}

	get killed()
	{
		return Boolean(this.data.killed);
	}

	set killed(aValue_bl)
	{
		this.data.killed = aValue_bl;
	}

	get awardedPrizes()
	{
		return this.data.awardedPrizes;
	}

	get awardedWeaponId()
	{
		return this.data.awardedWeaponId;
	}

	get awardedWeaponShots()
	{
		return this.data.awardedWeaponShots;
	}

	set hitResultBySeats(aValue_obj)
	{
		this.data.hitResultBySeats = aValue_obj;
	}

	get hitResultBySeats()
	{
		return this.data.hitResultBySeats;
	}

	get multiplierPay()
	{
		return this.data.multiplierPay;
	}

	get chMult()
	{
		return this.data.chMult;
	}

	get enemiesInstantKilled()
	{
		return this.data.enemiesInstantKilled;
	}

	set enemiesInstantKilled(aValue_obj)
	{
		this.data.enemiesInstantKilled = aValue_obj;
	}

	set killBonusPay(aValue_num)
	{
		this.data.killBonusPay = aValue_num;
	}

	get killBonusPay()
	{
		return this.data.killBonusPay;
	}

	get effects()
	{
		return this.data.effects;
	}

	toString() {
		let str = "";
		if (this.data) {
			for (let prop in this.data) {
				str += prop + ": " + this.data[prop] + "\n";
			}
		}
		str = "{ " + str + " }";
		return str;
	}

	clear() {
		this._fData_obj = null;
		this._fClass_str = null;
	}

	destroy() {
		this.clear();
		super.destroy();
	}
}

export default ShotResponseInfo