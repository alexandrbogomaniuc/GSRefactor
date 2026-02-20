import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TorchFxAnimation from '../TorchFxAnimation';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import PathTween from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/PathTween';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing'
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { ENEMIES } from '../../../../../shared/src/CommonConstants';
import MapsView from '../../../view/uis/maps/MapsView';

const RANDOM_RADIUS = 70;
const MIN_SCALE = 0.2;
const MAX_SCALE = 0.45;

class FlameDebris extends Sprite {

	static get EVENT_ON_ANIMATION_END()		{ return 'EVENT_ON_ANIMATION_END'; }

	constructor()
	{
		super();
		this._fTorch_sprt = null;
		this._flameGlow_sprt = null;
		this.once('added', () => { this._createView() });
	}

	_createView()
	{
		TorchFxAnimation.initTextures();
		
		let lTorch_sprt = this.addChild(new Sprite);
		lTorch_sprt.zIndex = 1;
		lTorch_sprt.textures = TorchFxAnimation.textures.torch;
		lTorch_sprt.anchor.set(0.5, 1);
		
		lTorch_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lTorch_sprt.play();

		lTorch_sprt.scale.set(Utils.random(MIN_SCALE, MAX_SCALE, true), Utils.random(MIN_SCALE, MAX_SCALE, true));		

		this._fTorch_sprt = lTorch_sprt;

		
		let lDestinationPoint_pt = this._calculateEndPosition();
		//moving
		let endX = lDestinationPoint_pt.x;
		let endY = lDestinationPoint_pt.y;
		let rx = (this.x + endX) / 2;
		let ry = (this.y + endY) / 2 - Utils.random(20, 40);
		let lPathTween_pt  = new PathTween(this, [{x: this.x, y: this.y}, {x: rx, y: ry}, {x: endX, y: endY}], true);
		let time = Utils.random(3*2*16.7, 8*2*16.7);
		lPathTween_pt.start(time, Easing.quadratic.easeOut,
			() => { 
				//onfinish
				this._onMovingCompleted();
			},
			() => {
				//onchange
				this.zIndex = this.y;
			}
		)
	}

	_calculateEndPosition()
	{
		let lDestinationPoint_pt = new PIXI.Point(this.x + Utils.random(-RANDOM_RADIUS, RANDOM_RADIUS), this.y + Utils.random(-RANDOM_RADIUS, RANDOM_RADIUS));
		return lDestinationPoint_pt;
	}

	_onMovingCompleted()
	{
		let lTorch_sprt = this._fTorch_sprt;
		this._addFlameGlow(lTorch_sprt.scale.x, lTorch_sprt.scale.y);

		let lFinalScaleY_num = Math.min(0.3, lTorch_sprt.scale.y);
		let idleDuration = 750 + Utils.random(-200, 200);

		let seq = [
			{
				tweens: [],
				duration: idleDuration,
				onfinish: () => {
					this._flameGlow_sprt.fadeTo(0, 10*2*16.7, undefined, () => {this._onAnimationCompleted();});
				}
			},
			{
				tweens: [
					{ prop: "scale.y", to: lFinalScaleY_num },
					{ prop: "alpha", to: 0 }
				],
				duration: 8*2*16.7
			}
		]

		Sequence.start(lTorch_sprt, seq);
	}

	get _currentMapId()
	{
		return APP.currentWindow.mapsController.info.mapId;
	}

	_addFlameGlow(scaleX, scaleY)
	{
		let lFlameGlow_sprt = this._flameGlow_sprt = this.addChild(APP.library.getSprite('common/crate_glow'));
		lFlameGlow_sprt.zIndex = 0;
		lFlameGlow_sprt.anchor.set(0.48, 0.88);
		lFlameGlow_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lFlameGlow_sprt.scale.set(0.5*scaleX, 0.5*scaleY);
		lFlameGlow_sprt.alpha = 0.5;
	}

	_onAnimationCompleted()
	{
		this.emit(FlameDebris.EVENT_ON_ANIMATION_END);
	}

	destroy()
	{
		this.removeAllListeners();

		PathTween.destroy(PathTween.findByTarget(this));
		Sequence.destroy(Sequence.findByTarget(this._fTorch_sprt));

		this._fTorch_sprt && this._fTorch_sprt.destroy();
		this._fTorch_sprt = null;

		super.destroy();
		
	}
}

export default FlameDebris;