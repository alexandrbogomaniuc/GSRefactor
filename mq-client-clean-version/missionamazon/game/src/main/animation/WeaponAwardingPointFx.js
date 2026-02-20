import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class WeaponAwardingPointFx extends Sprite {

    constructor() {
        super();

        this._fSmoke_sprt = null;
        this._fFlares_sprt_arr = null;

        this._init();
    }

    _init()
    {
    	this._addSmoke(0, 0);
    	this._addFlares();
    }

    _addFlares()
    {
    	for (let i=0; i<4; i++)
    	{
			let x = 0;
    		let y = -10 * i;
    		let maxScale = 1.5 + i * 0.2;
    		let delaySequence = [
    			{
    				tweens: [],
    				duration: i * FRAME_RATE,
    				onfinish: () => this._addFlare(x, y, maxScale)
    			}
    		];
    		Sequence.start(this, delaySequence);
    	}
    }

    _addFlare(x, y, maxScale)
    {
    	let lFlare_sprt = this.addChild(APP.library.getSpriteFromAtlas("common/weapon_awarding_flare"));
    	lFlare_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;

    	lFlare_sprt.position.set(x, y);

    	lFlare_sprt.scale.set(0);
    	lFlare_sprt.scaleTo(maxScale, 2*FRAME_RATE, null, () => {
    		lFlare_sprt.scaleTo(0, 14 * FRAME_RATE)
    	});

    	lFlare_sprt.rotation = Utils.gradToRad(2.6);
    	lFlare_sprt.rotateTo(Utils.gradToRad(32.6), 16 * FRAME_RATE);
    }

    _addSmoke(x, y)
	{
		this._fSmoke_sprt = this.addChild(APP.library.getSprite('blend/smoke'));
		this._fSmoke_sprt.position.set(x, y);
		this._fSmoke_sprt.scale.set(0);
		this._fSmoke_sprt.alpha = 1;
		this._fSmoke_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;

		let alphaSequence = [
			{
				tweens: [],
				duration: 8 * FRAME_RATE
			},
			{
				tweens:[ {prop:"alpha", to: 0}],
				duration: 12 * FRAME_RATE,
				onfinish: () => {
					this._fSmoke_sprt = null;
				}
			}
		];

		this._fSmoke_sprt.scaleTo(1, 20 * FRAME_RATE);

		Sequence.start(this._fSmoke_sprt, alphaSequence);
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));
		Sequence.destroy(Sequence.findByTarget(this._fSmoke_sprt));
		super.destroy();
	}
}
export default WeaponAwardingPointFx;