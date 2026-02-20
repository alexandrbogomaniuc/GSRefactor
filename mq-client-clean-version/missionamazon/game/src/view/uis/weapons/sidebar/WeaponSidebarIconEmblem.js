import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { FRAME_RATE, WEAPONS } from '../../../../../../shared/src/CommonConstants';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DesaturatingSprite from './DesaturatingSprite';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import EmblemRim from './EmblemRim';

const POSITIONS = {
	[WEAPONS.FLAMETHROWER]: {
		center: {x: 66.5/2, y: 85.5/2 },
		body: 	{x: -1/2, y: 37/2 },
		bg: 	{x: 11/2, y: 28/2 },
		fx: 	{x: 29/2, y: 0/2 }
	},
	[WEAPONS.INSTAKILL]: {
		center: {x: 70.5/2, y: 145.5/2},
		body: 	{x: 0/2, 	y: 101/2,	xDeactivate: 0/2 + 10 },
		bg: 	{x: 15/2, 	y: 88/2},
		fx: 	{x: 83/2, 	y: 0/2, 	xDeactivate: 83/2 + 10}
	},
	[WEAPONS.CRYOGUN]: {
		center: {x: 70/2, y: 136/2},
		body: 	{x: 8/2, 	y: 101/2},
		bg: 	{x: 14/2, 	y: 81/2},
		fx: 	{x: 0/2, 	y: 0/2}
	},
	[WEAPONS.ARTILLERYSTRIKE]: {
		center: {x: 55.5/2, y: 63.5/2},
		body: 	{x: 27/2, 	y: 5/2, 	zIndex: 3},
		bg: 	{x: 0/2, 	y: 6/2, 	zIndex: 1},
		fx: 	{x: 7/2, 	y: 0/2, 	zIndex: 2}
	},
	[WEAPONS.RICOCHET]: {
		center: {x: 90.5/2, y: 135.5/2},
		body: 	{x: 16/2, y: 106/2 },
		bg: 	{x: 32/2 , y: 78/2 },
		fx: 	{x: 0, y: 0 }
	},
}

const ACTIVATION_ANIMATION_DURATION = 10 * FRAME_RATE;

class WeaponSidebarIconEmblem extends Sprite {

	set active(aValue_bl)
	{
		if (this._fIsActive_bl != aValue_bl)
		{
			this._fIsActive_bl = aValue_bl;
			this._invalidateActiveState();
		}
	}

	get active()
	{
		return this._fIsActive_bl;
	}

	set isSelected(aValue_bl)
	{
		if (this._fIsSelected_bl != aValue_bl)
		{
			this._fIsSelected_bl = aValue_bl;
			this._invalidateSelectionState();
		}
	}

	get isSelected()
	{
		return this._fIsSelected_bl;
	}

	set isAnimating(aValue_bl)
	{
		this._fIsAnimating_bl = aValue_bl;
	}

	get isAnimating()
	{
		return this._fIsAnimating_bl;
	}

	get weaponId()
	{
		return this._fWeaponId_int;
	}

	startBackToWeaponAnimation()
	{
		this._startBackToWeaponAnimation();
	}

	interruptBackToWeaponAnimation()
	{
		this._interruptBackToWeaponAnimation();
	}

	constructor(aWeaponId_int)
	{
		super();
		this._fWeaponId_int = aWeaponId_int;
		this._fIsAnimating_bl = false;
		this._fIsSelected_bl = false;

		this._fBg_ds = null;
		this._fBody_ds = null;
		this._fFx_ds = null;

		this._fRim_er = null;
		this._fRimGlow_sprt = null;

		this._fIsActive_bl = false;

		this._initView();
		this._invalidateActiveState();
		this._invalidateSelectionState();

		// let gr = new PIXI.Graphics();
		// gr.beginFill(0xff0000, 0.5);
		// gr.drawCircle(0, 0, 40);
		// this.addChild(gr);

		//DEBUG...
		// this.on("pointerclick", () => {
		// 	this.active = !this.active;
		// }, this);
		//...DEBUG
	}

	_initView()
	{

		this._createBg();
		this._createRim();
		this._createRimGlow();
		this._createBody();
		this._createFx();

		this.pivot.set(this.positionsObj.center.x, this.positionsObj.center.y);
	}

	get basePath()
	{
		let lBasePath_str;
		switch (this._fWeaponId_int)
		{
			case WEAPONS.INSTAKILL:
				lBasePath_str = "weapons/sidebar/plasma/plasma_";
				break;
			case WEAPONS.ARTILLERYSTRIKE:
				lBasePath_str = "weapons/sidebar/artillerystrike/artillerystrike_";
				break;
			case WEAPONS.FLAMETHROWER:
				lBasePath_str = "weapons/sidebar/flamethrower/flamethrower_";
				break;
			case WEAPONS.CRYOGUN:
				lBasePath_str = "weapons/sidebar/cryogun/cryogun_";
				break;
			case WEAPONS.RICOCHET:
				lBasePath_str = "weapons/sidebar/ricochetgun/laser_";
				break;
			default:
				throw new Error("No assets for weaponId = " + this._fWeaponId_int);
				break;
		}
		return lBasePath_str;
	}

	get positionsObj()
	{
		return POSITIONS[this._fWeaponId_int];
	}

	_createBg()
	{
		this._fBg_ds = this.addChild(new DesaturatingSprite(this.basePath + "bg", this.basePath + "bg_desaturated"));
		this._fBg_ds.position.set(this.positionsObj.bg.x, this.positionsObj.bg.y);
		if (this.positionsObj.bg.zIndex != null)
		{
			this._fBg_ds.zIndex = this.positionsObj.bg.zIndex;
		}
	}

	_createRim()
	{
		this._fRim_er = this.addChild(new EmblemRim(this.weaponId));
		this._fRim_er.position.set(this.positionsObj.center.x, this.positionsObj.center.y);
	}

	_createBody()
	{
		this._fBody_ds = this.addChild(new DesaturatingSprite(this.basePath + "body", this.basePath + "body_desaturated"));
		this._fBody_ds.position.set(this.positionsObj.body.x, this.positionsObj.body.y);
		if (this.positionsObj.body.zIndex != null)
		{
			this._fBody_ds.zIndex = this.positionsObj.body.zIndex;
		}
	}

	_createFx()
	{
		this._fFx_ds = this.addChild(new DesaturatingSprite(this.basePath + "fx", this.basePath + "fx_desaturated"));
		this._fFx_ds.position.set(this.positionsObj.fx.x, this.positionsObj.fx.y);
		if (this.positionsObj.fx.zIndex != null)
		{
			this._fFx_ds.zIndex = this.positionsObj.fx.zIndex;
		}
	}

	_createRimGlow()
	{
		let lRimGlow = this._fRimGlow_sprt = this.addChild(APP.library.getSpriteFromAtlas("weapons/sidebar/rim_glow"));
		lRimGlow.scale.set(2);
		lRimGlow.anchor.set(0.48, 0.5);
		lRimGlow.position.set(this.positionsObj.center.x, this.positionsObj.center.y);
		lRimGlow.blendMode = PIXI.BLEND_MODES.ADD;
		lRimGlow.visible = false;
	}

	_invalidateActiveState()
	{
		if (this.active)
		{
			this._activate();
		}
		else
		{
			this._deactivate();
		}
	}

	_activate()
	{
		this._fBg_ds.i_saturate(!this.isAnimating);
		this._fBody_ds.i_saturate(!this.isAnimating);
		this._fFx_ds.i_saturate(!this.isAnimating);

		this._resetSequences();
		if (this.isAnimating)
		{
			let fxSeq = [
				{
					tweens: [ { prop: "alpha", to: 1 } ],
					duration: ACTIVATION_ANIMATION_DURATION
				}
			];
			Sequence.start(this._fFx_ds, fxSeq);

			//move if needed
			if (this.positionsObj.body.xDeactivate != null)
			{
				let moveBodySeq = [
					{
						tweens: [ { prop: "x", to: this.positionsObj.body.x }],
						duration: ACTIVATION_ANIMATION_DURATION
					}
				];
				Sequence.start(this._fBody_ds, moveBodySeq);
			}
			if (this.positionsObj.fx.xDeactivate != null)
			{
				let moveFxSeq = [
					{
						tweens: [ { prop: "x", to: this.positionsObj.fx.x }],
						duration: ACTIVATION_ANIMATION_DURATION
					}
				];
				Sequence.start(this._fFx_ds, moveFxSeq);
			}
		}
		else
		{
			this._fFx_ds.alpha = 1;
			this._fFx_ds.position.set(this.positionsObj.fx.x, this.positionsObj.fx.y);
			this._fBody_ds.position.set(this.positionsObj.body.x, this.positionsObj.body.y);
		}
	}

	_deactivate()
	{
		this._fBg_ds.i_desaturate(!this.isAnimating);
		this._fBody_ds.i_desaturate(!this.isAnimating);
		this._fFx_ds.i_desaturate(!this.isAnimating);

		this._resetSequences();
		this._interruptBackToWeaponAnimation();
		if (this.isAnimating)
		{
			let fxSeq = [
				{
					tweens: [ { prop: "alpha", to: 0 } ],
					duration: ACTIVATION_ANIMATION_DURATION
				}
			];
			Sequence.start(this._fFx_ds, fxSeq);

			//move back if any
			if (this.positionsObj.body.xDeactivate != null)
			{
				let moveBodySeq = [
					{
						tweens: [ { prop: "x", to: this.positionsObj.body.xDeactivate }],
						duration: ACTIVATION_ANIMATION_DURATION
					}
				];
				Sequence.start(this._fBody_ds, moveBodySeq);
			}
			if (this.positionsObj.fx.xDeactivate != null)
			{
				let moveFxSeq = [
					{
						tweens: [ { prop: "x", to: this.positionsObj.fx.xDeactivate }],
						duration: ACTIVATION_ANIMATION_DURATION
					}
				];
				Sequence.start(this._fFx_ds, moveFxSeq);
			}
		}
		else
		{
			this._fFx_ds.alpha = 0;
			if (this.positionsObj.fx.xDeactivate != null)
			{
				this._fFx_ds.position.set(this.positionsObj.fx.xDeactivate, this.positionsObj.fx.y);
			}
			if (this.positionsObj.body.xDeactivate != null)
			{
				this._fBody_ds.position.set(this.positionsObj.body.xDeactivate, this.positionsObj.body.y);
			}
		}
	}

	_resetSequences()
	{
		this._fFx_ds && Sequence.destroy(Sequence.findByTarget(this._fFx_ds));
		this._fBody_ds && Sequence.destroy(Sequence.findByTarget(this._fBody_ds));
	}

	_invalidateSelectionState()
	{
		if (this.isSelected)
		{
			this._fRim_er.i_activate();
		}
		else
		{
			this._fRim_er.i_deactivate();
		}
	}

	_startBackToWeaponAnimation()
	{
		this._interruptBackToWeaponAnimation();

		let lRimGlow = this._fRimGlow_sprt;
		lRimGlow.visible = true;
		lRimGlow.alpha = 1;

		lRimGlow.fadeTo(0, 16*FRAME_RATE, undefined, () => { lRimGlow.visible = false; } );
	}

	_interruptBackToWeaponAnimation()
	{
		let lRimGlow = this._fRimGlow_sprt;
		lRimGlow.removeTweens();

		lRimGlow.visible = false;
	}

}

export default WeaponSidebarIconEmblem;