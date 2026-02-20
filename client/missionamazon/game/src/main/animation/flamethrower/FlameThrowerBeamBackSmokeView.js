import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AtlasConfig from '../../../config/AtlasConfig';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';

const SMOKES_AMOUNT = 5;
const SMOKES_STEP = 135;

class FlameThrowerBeamBackSmokeView extends Sprite 
{
	constructor()
	{
		super();
		
		this._initView();
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));

		super.destroy();
	}

	startAnimation()
	{
		this._startAnimation();
	}

	_initView()
	{
		for (let i=0; i<SMOKES_AMOUNT; i++)
		{
			let smoke_sprt = this.addChild(APP.library.getSpriteFromAtlas('weapons/FlameThrower/flame_back_smoke'));
			smoke_sprt.anchor.set(0, 0.5);
			smoke_sprt.x = -SMOKES_STEP*i;
		}

		this.alpha = 0;
	}

	_startAnimation()
	{
		let seq = [
			{
				tweens: [],
				duration: 2*2*16.7
			},
			{
				tweens: [ { prop: 'alpha', to: 1 } ],
				duration: 4*2*16.7
			},
			{
				tweens: [],
				duration: 23*2*16.7
			},
			{
				tweens: [ { prop: 'alpha', to: 0} ],
				duration: 2*2*16.7,
				onfinish: ()=> {
					this.destroy();
				}
			}
		];
		Sequence.start(this, seq);

		this.moveTo(135, 0, 50*2*16.7);
	}

}

export default FlameThrowerBeamBackSmokeView;