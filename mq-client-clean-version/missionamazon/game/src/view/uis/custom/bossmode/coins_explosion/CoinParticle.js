import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class CoinParticle extends PIXI.Container
{
	static get EVENT_ON_DEATH()		{return "onDeath";}

	updateParticle(aDt_num)
	{
		this._updateParticle(aDt_num);
	}

	play()
	{
		this._fAnim_sprt && this._fAnim_sprt.play();
	}

	stop()
	{
		this._fAnim_sprt && this._fAnim_sprt.stop();
	}

	resurrect(aDistanation_num, aLifeTime_num, aDeathTime_num)
	{
		this._fCurrentTime_num = 0;
		this._fAnim_sprt.alpha = 1;
		this._fAnim_sprt.position.set(0, 0);

		this._fDistanation_num = aDistanation_num;
		this._fLifeTime_num = aLifeTime_num;
		this._fDeathTime_num = aDeathTime_num;
	}

	constructor(aTextures_arr, aDistanation_num, aLifeTime_num, aDeathTime_num)
	{
		super();

		this._fCurrentTime_num = 0;

		this._fDistanation_num = aDistanation_num;
		this._fLifeTime_num = aLifeTime_num;
		this._fDeathTime_num = aDeathTime_num;

		this._fAnim_sprt = this.addChild(new Sprite());
		this._fAnim_sprt.rotation = Math.random()*12.56637;
		this._fAnim_sprt.scale.set(2);
		this._fAnim_sprt.textures = aTextures_arr;
	}

	_updateParticle(aDt_num)
	{
		if (this._fCurrentTime_num >= this._fDeathTime_num)
		{
			this._fAnim_sprt.alpha = 0;
			this.emit(CoinParticle.EVENT_ON_DEATH, {target: this});
			return;
		}

		if (this._fCurrentTime_num >= this._fLifeTime_num)
		{
			let lAlphaPercent_num = 1 - (this._fCurrentTime_num - this._fLifeTime_num) / (this._fDeathTime_num - this._fLifeTime_num);
			this._fAnim_sprt.alpha = lAlphaPercent_num;
		}

		let lDist_num = this._fDistanation_num * (this._fCurrentTime_num / this._fDeathTime_num);
		this._fAnim_sprt.position.x = lDist_num;

		this._fCurrentTime_num += aDt_num;
	}

	destroy()
	{
		this._fAnim_sprt.destroy();

		super.destroy();

		this._fAnim_sprt = null;
		this._fDistanation_obj = null;
		this._fLifeTime_num = null;
		this._fDeathTime_num = null;
	}
}

export default CoinParticle;