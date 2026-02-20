import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';

class GrenadeGroundBurn extends Sprite 
{
	static get EVENT_ON_ANIMATION_COMPLETED () {return "EVENT_ON_ANIMATION_COMPLETED";}

	constructor(aInitialAlpha_num = 0.7, aIsArtilerryHit_bl = false)
	{
		super();

		this._fInitialAlpha_num = aInitialAlpha_num;
		this._groundburn = null;

		this._fIsArtilleryHit = aIsArtilerryHit_bl;

		this.once('added', (e) => {this._onAdded();});
	}

	_onAdded()
	{
		this._showEffect();
	}

	_showEffect()
	{
		let groundburn = this._groundburn = this._fIsArtilleryHit ? this.addChild(APP.library.getSpriteFromAtlas( "weapons/ArtilleryStrike/groundburn")) : this.addChild(APP.library.getSpriteFromAtlas("common/groundburn"));
		groundburn.scale.set(2);
		groundburn.blendMode = PIXI.BLEND_MODES.MULTIPLY;
		groundburn.alpha = 0;

		let groundburnSequence = [
			{
				tweens: [],
				duration: 4*2*16.6,
				onfinish: (e) => {groundburn.alpha = this._fInitialAlpha_num;}
			},
			{
				tweens: [],
				duration: 80*2*16.6
			},
			{
				tweens: [
							{prop: "alpha", to: 0}
						],
				duration: 25*2*16.6,
				ease: Easing.cubic.cubicIn,
				onfinish: (e) => {
					this._onAnimationCompleted();
				}
			}
		];

		Sequence.start(groundburn, groundburnSequence);	
	}

	_onAnimationCompleted()
	{
		this.emit(GrenadeGroundBurn.EVENT_ON_ANIMATION_COMPLETED);
	}

	destroy()
	{
		if (this._groundburn)
		{
			Sequence.destroy(Sequence.findByTarget(this._groundburn));
			this._groundburn = null;
		}

		super.destroy();
	}
}

export default GrenadeGroundBurn;