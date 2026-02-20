import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import CommonEffectsManager from '../../CommonEffectsManager';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class FlameThrowerGunFireEffect extends Sprite {

	static get EVENT_ON_ANIMATION_END()		{ return 'EVENT_ON_ANIMATION_END'; }

	constructor(weaponScale)
	{
		super();

		this._fWeaponScale = weaponScale ? weaponScale: 1;

		this._createView();
	}

	_createView()
	{
		this._createDieSmokes();
	}

	_createDieSmokes()
	{	
		this._addDieSmoke(new PIXI.Point(-4*this._fWeaponScale, 23*this._fWeaponScale), new PIXI.Point(0.15*this._fWeaponScale, 0.16*this._fWeaponScale), 260+18, PIXI.BLEND_MODES.SCREEN);
		this._addDieSmoke(new PIXI.Point(2*this._fWeaponScale, 14*this._fWeaponScale), new PIXI.Point(0.15*this._fWeaponScale, 0.16*this._fWeaponScale), 270+18, PIXI.BLEND_MODES.SCREEN);
		this._addDieSmoke(new PIXI.Point(4*this._fWeaponScale, 23*this._fWeaponScale), new PIXI.Point(0.15*this._fWeaponScale, 0.16*this._fWeaponScale), 67+18, PIXI.BLEND_MODES.SCREEN);
		this._addDieSmoke(new PIXI.Point(-2*this._fWeaponScale, 14*this._fWeaponScale), new PIXI.Point(0.15*this._fWeaponScale, 0.16*this._fWeaponScale), 57+18, PIXI.BLEND_MODES.SCREEN);

		let lSmoke_sprt = this._addDieSmoke(undefined, new PIXI.Point(0.4, 0.5), 0+18, PIXI.BLEND_MODES.SCREEN, -1);
		lSmoke_sprt.on('animationend', () => {
			this._onAnimationCompleted();
		});
	}

	_addDieSmoke(position, scaleMultiplier, rotationInGrad, blendMode=PIXI.BLEND_MODES.NORMAL, startFrame=0, totalFrames=undefined)
	{
		let lSmoke_sprt = Sprite.createMultiframesSprite(CommonEffectsManager.getDieSmokeUnmultTextures(), startFrame, totalFrames);
		this.addChild(lSmoke_sprt);

		lSmoke_sprt.animationSpeed = 30/60;
		lSmoke_sprt.anchor.set(0.57, 0.81);
		lSmoke_sprt.scale.set(2 * scaleMultiplier.x, 2 * scaleMultiplier.y);
		if (position !== undefined)
		{
			lSmoke_sprt.position.set(position.x, position.y);
		}
		
		if (rotationInGrad !== undefined)
		{
			lSmoke_sprt.rotation = Utils.gradToRad(rotationInGrad);
		}

		lSmoke_sprt.on('animationend', () => {
			lSmoke_sprt.destroy();
		});

		lSmoke_sprt.play();

		return lSmoke_sprt;
	}

	_onAnimationCompleted()
	{
		this.emit(FlameThrowerGunFireEffect.EVENT_ON_ANIMATION_END);
		this.destroy();
	}

	destroy()
	{
		this.removeAllListeners();

		this._fWeaponScale = null;

		super.destroy();
	}
}

export default FlameThrowerGunFireEffect;