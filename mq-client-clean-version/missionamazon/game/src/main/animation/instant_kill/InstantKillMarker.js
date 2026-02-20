import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import EternalPlazmaSmoke from './EternalPlazmaSmoke';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { AtlasSprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasConfig from './../../../config/AtlasConfig';
import InstantKillSmokeLoop from './InstantKillSmokeLoop';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';

let _marker_textures = null;
function _getMarkerTextures()
{
	if (_marker_textures) return _marker_textures;

	_marker_textures = AtlasSprite.getFrames([APP.library.getAsset("weapons/InstantKill/plasma_marker")], [AtlasConfig.InstantKillMarkerAtlas], "");
	return _marker_textures;
}

class InstantKillMarker extends Sprite 
{
	constructor(aIsFast_bl = false)
	{
		super();

		this.marker = null;
		this.eternalPlazmaSmoke = null;
		this.indigoSmoke = null;
		this.smoke = null;
		this.timer = null;

		APP.profilingController.info.isVfxProfileValueMediumOrGreater && this.startIndigoSmoke();
		APP.profilingController.info.isVfxProfileValueMediumOrGreater && this.startLoopSmoke();

		this.marker = this.addChild(new Sprite());

		this.marker.textures = _getMarkerTextures();
		this.marker.blendMode = PIXI.BLEND_MODES.ADD;
		this.marker.scale.set(2);
		this.marker.play();

		this.zIndex = 100000;
		this.scale.set(0.1);
		let duration = aIsFast_bl ? 80 : 400;
		this.scaleTo(0.8, duration);

		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this.eternalPlazmaSmoke = this.addChild(new EternalPlazmaSmoke());
			this.eternalPlazmaSmoke.scale.set(2);
		}
	}

	startTimerLoopSmoke()
	{
		this.timer = new Timer(this.createView.startLoopSmoke(this), 5 * FRAME_RATE);
	}

	startLoopSmoke()
	{
		this.smoke = this.addChild(new InstantKillSmokeLoop(4.8, 7.2));
	}


	destroy()
	{
		this.eternalPlazmaSmoke && this.eternalPlazmaSmoke.destroy();
		this.marker && this.marker.destroy();
		this.smoke && this.smoke.destroy();

		this.timer && this.timer.destructor();
		this.timer = null;

		this.marker = null;
		this.eternalPlazmaSmoke = null;
		this.indigoSmoke = null;
		this.smoke = null;

		super.destroy();
	}

	startIndigoSmoke()
	{
		this.indigoSmoke = this.addChild(new EternalPlazmaSmoke(true));
		this.indigoSmoke.scale.set(0);
		this.indigoSmoke.scaleXTo(2*1.63, 10*2*16.6);
		this.indigoSmoke.scaleYTo(2*2.45, 10*2*16.6);
	}	
}

export default InstantKillMarker;