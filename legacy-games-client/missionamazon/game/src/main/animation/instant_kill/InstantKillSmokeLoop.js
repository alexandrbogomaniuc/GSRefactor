import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { AtlasSprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasConfig from './../../../config/AtlasConfig';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';

let _smoke_textures = null;
function _getSmokeTextures()
{
	if (_smoke_textures) return _smoke_textures;

	_smoke_textures = AtlasSprite.getFrames([APP.library.getAsset("weapons/InstantKill/smoke_loop")], [AtlasConfig.InstantKillSmokeAtlas], "");
	return _smoke_textures;
}

class InstantKillSmokeLoop extends Sprite 
{

	constructor(aScaleX_num, aScaleY_num)
	{
		super();

		this.smoke = this.addChild(new Sprite);
		this.smoke.textures = _getSmokeTextures();
		this.smoke.blendMode = PIXI.BLEND_MODES.ADD;
		this.smoke.scale.set(0,0);
		this.smoke.play();
		this._finishScaleX_num = aScaleX_num ? aScaleX_num: 1;
		this._finishScaleY_num = aScaleY_num ? aScaleY_num: 1;

		this.startSmoke();
	}

	startSmoke()
	{
		var sequence = [
			{
				tweens: [{ prop: 'scale.x', to: this._finishScaleX_num }, { prop: 'scale.y', to: this._finishScaleY_num }],
				duration: 10 * FRAME_RATE
			}
		]
		Sequence.start(this.smoke, sequence);
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this.smoke));
		this.smoke && this.smoke.destroy();
		this.smoke = null;

		super.destroy();
	}
}

export default InstantKillSmokeLoop;