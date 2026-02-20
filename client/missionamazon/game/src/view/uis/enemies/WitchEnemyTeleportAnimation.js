import { Sprite } from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DeathFxAnimation from '../../../main/animation/death/DeathFxAnimation';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../config/AtlasConfig';

let teleport_textures = null;
function initTeleportTextures()
{
	if (!teleport_textures)
	{
		let lTextures_arr = AtlasSprite.getFrames(APP.library.getAsset("enemies/witch/teleport/teleport"), AtlasConfig.WitchTeleport, "");
		teleport_textures = lTextures_arr;
	}
}

class WitchEnemyTeleportAnimation extends Sprite
{
	static get EVENT_ON_ANIMATION_ENDED()		{return "EVENT_ON_ANIMATION_ENDED";}
	
	startAnimation()
	{
		this._startAnimation();
	}

	pauseAnimation()
	{
		APP.profilingController.info.isVfxProfileValueMediumOrGreater && this._fPuff_spr && this._fPuff_spr.stop();
		this._fTeleport_sprt && this._fTeleport_sprt.stop();
		if (this._fPuff_spr) this._fPuff_spr.visible = false;
		if (this._fTeleport_sprt) this._fTeleport_sprt.visible = false;
	}

	resumeAnimation()
	{
		APP.profilingController.info.isVfxProfileValueMediumOrGreater && this._fPuff_spr && this._fPuff_spr.play();
		this._fTeleport_sprt && this._fTeleport_sprt.play();
		if (this._fPuff_spr) this._fPuff_spr.visible = true;
		if (this._fTeleport_sprt) this._fTeleport_sprt.visible = true;
	}

	constructor()
	{
		super();
		
		DeathFxAnimation.initTextures();
		initTeleportTextures();

		this._fPuff_spr = null;
		this._fTeleport_sprt = null;
		this._fPuffInProgress_bl = false;
		this._fTeleportInProgress_bl = false;

		this._initAnimation();
	}

	_initAnimation()
	{
		APP.profilingController.info.isVfxProfileValueMediumOrGreater && this._initPuff();
		this._initTeleport();
	}

	_initTeleport()
	{
		this._fTeleport_sprt = this.addChild(new Sprite());
		this._fTeleport_sprt.scale.set(2);
		this._fTeleport_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fTeleport_sprt.position.set(4, 10);
		this._fTeleport_sprt.textures = teleport_textures;
		this._fTeleport_sprt.on('animationend', () => {
			this._fTeleportInProgress_bl = false;
			this._fTeleport_sprt && this._fTeleport_sprt.destroy();
			this._fTeleport_sprt = null;
			this._checkAnimationEnd();
		});
	}

	_initPuff()
	{
		this._fPuff_spr = this.addChild(Sprite.createMultiframesSprite(DeathFxAnimation.textures["smokePuff"], 3));
		this._fPuff_spr.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fPuff_spr.position.set(10 + 4 , -70 + 10);
		this._fPuff_spr.scale.set(2.88);
		this._fPuff_spr.once('animationend', () => {
			this._fPuffInProgress_bl = false;
			this._fPuff_spr.destroy();
			this._fPuff_spr = null;
			this._checkAnimationEnd();
		});
	}

	_startAnimation()
	{
		this._fPuffInProgress_bl = true;
		this._fTeleportInProgress_bl = true;
		APP.profilingController.info.isVfxProfileValueMediumOrGreater && this._fPuff_spr && this._fPuff_spr.play();
		this._fTeleport_sprt && this._fTeleport_sprt.play();
	}

	_checkAnimationEnd()
	{
		if (!this._fPuffInProgress_bl && !this._fTeleportInProgress_bl)
		{
			this.emit(WitchEnemyTeleportAnimation.EVENT_ON_ANIMATION_ENDED);
		}
	}

	destroy()
	{
		this._fPuff_spr && this._fPuff_spr.destroy();
		this._fPuff_spr = null;

		this._fTeleport_sprt && this._fTeleport_sprt.destroy();
		this._fTeleport_sprt = null;

		super.destroy();
	}
}

export default WitchEnemyTeleportAnimation;