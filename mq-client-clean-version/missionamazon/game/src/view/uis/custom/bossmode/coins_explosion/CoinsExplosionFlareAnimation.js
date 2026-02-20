import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sequence } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import { FRAME_RATE } from './../../../../../../../shared/src/CommonConstants';
import { ENEMIES } from '../../../../../../../shared/src/CommonConstants';

class CoinsExplosionFlareAnimation extends Sprite
{
	startAnimation()
	{
		this._startAnimation();
	}

	constructor(aBossType_str)
	{
		super();

		this._fFlare_sprt = null;
		this._fBossType_str = aBossType_str;
	}

	_startAnimation()
	{
		this._fFlare_sprt = this.addChild(this._getFlare());

		let lSeq_arr = [
			{tweens: [], duration: 10*FRAME_RATE},
			{tweens: [{prop: 'alpha', to: 0}], duration: 5*FRAME_RATE, onfinish: () => {
				this._fFlare_sprt && this._fFlare_sprt.destroy();
				this._fFlare_sprt = null;
			}}
		];

		Sequence.start(this._fFlare_sprt, lSeq_arr);
	}

	_getFlare()
	{
		let flashAssetName = "boss_mode/death_flash";
		let lFlare_sprt = APP.library.getSpriteFromAtlas(flashAssetName);
		lFlare_sprt.scale.set(2.5);
		lFlare_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		return lFlare_sprt;
	}

	destroy()
	{
		this._fFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fFlare_sprt));

		super.destroy();

		this._fFlare_sprt = null;
	}
}

export default CoinsExplosionFlareAnimation;