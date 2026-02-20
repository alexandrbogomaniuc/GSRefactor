import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import EternalPlazmaSmoke from './EternalPlazmaSmoke';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { AtlasSprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasConfig from './../../../config/AtlasConfig';

let _spinner_textures = null;
function _getSpinnerTextures()
{
	if (_spinner_textures) return _spinner_textures;

	_spinner_textures = AtlasSprite.getFrames([APP.library.getAsset("weapons/InstantKill/plasma_spinner")], [AtlasConfig.InstantKillSpinnerAtlas], "");
	return _spinner_textures;
}

class InstantKillSpinner extends Sprite
{
	constructor()
	{
		super();
		
		this.eternalPlazmaSmoke = null;
		this.spinner = null;
		this.flare = null;
		this.flareSequence = null;

		this.createView();
	}

	static get EVENT_SPINNER_ANIMATION_FINISHED()
	{
		return "onSpinnerAnimationFinished";
	}

	createView(){
		this.eternalPlazmaSmoke = this.addChild(new EternalPlazmaSmoke(true /*indigoSmoke*/));
		this.eternalPlazmaSmoke.scale.set(2);

		this.spinner = this.addChild(new Sprite);
		this.spinner.textures = _getSpinnerTextures();
		this.spinner.blendMode = PIXI.BLEND_MODES.ADD;
		this.spinner.scale.set(2);
		this.spinner.play();
		this.spinner.on('animationend', (e) => {this.emit(InstantKillSpinner.EVENT_SPINNER_ANIMATION_FINISHED);});

		this.flare = this.addChild(APP.library.getSpriteFromAtlas("weapons/InstantKill/marker_flare"));
		this.flare.blendMode = PIXI.BLEND_MODES.ADD;
		this.flare.scale.set(2*1.86);
		this.flareSequence = [
				{
					tweens: [
						{prop: "scale.x", to: 0},
						{prop: "scale.y", to: 0}
					],
					duration: 3*2*16.6					
				},
				{
					tweens: [
					],
					duration: 6*2*16.6
				},
				{
					tweens: [
						{prop: "scale.x", to: 1.86*2},
						{prop: "scale.y", to: 1.86*2}
					],
					duration: 3*2*16.6,
					onfinish: () => {this.startFlareAnimation();}
				}
			]
		this.startFlareAnimation();
	}

	startFlareAnimation(){
		Sequence.start(this.flare, this.flareSequence);
	}

	destroy()
	{
		if (this.flare)
		{
			Sequence.destroy(Sequence.findByTarget(this.flare));
		}
		
		this.eternalPlazmaSmoke = null;
		this.spinner = null;
		this.flare = null;
		this.flareSequence = null;

		super.destroy();
	}
}

export default InstantKillSpinner;