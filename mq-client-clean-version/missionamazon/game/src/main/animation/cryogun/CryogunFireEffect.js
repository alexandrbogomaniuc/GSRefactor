import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import CommonEffectsManager from '../../CommonEffectsManager';

class CryogunFireEffect extends Sprite {

	static get EVENT_ON_ANIMATION_END() { return 'animationend'};

	constructor()
	{
		super();
		this._fDieSmoke_sprt = null;
		this.once('added', (e) => {this._onAdded();});
	}

	_onAdded()
	{
		this._showEffect();
	}

	_showEffect()
	{
		let smoke = this.addChild(new Sprite);		
		smoke.textures = CommonEffectsManager.getDieSmokeUnmultTextures();
		smoke.anchor.set(0.57, 0.81);
		smoke.once('animationend', (e) => {
			smoke.destroy();
			this.emit(CryogunFireEffect.EVENT_ON_ANIMATION_END);
			this.destroy();
		})
		smoke.scale.set(this._baseScale.x*1.32, this._baseScale.y*1.32);
		smoke.blendMode = PIXI.BLEND_MODES.SCREEN;
		smoke.play();
		this._fDieSmoke_sprt = smoke;	
	}

	get _baseScale()
	{
		return {x: 2, y: 2};
	}

	destroy()
	{
		this._fDieSmoke_sprt && this._fDieSmoke_sprt.destroy();
		this._fDieSmoke_sprt = null;
		super.destroy();
	}
}

export default CryogunFireEffect;