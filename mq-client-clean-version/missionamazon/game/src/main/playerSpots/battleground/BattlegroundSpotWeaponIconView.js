import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { WEAPONS } from '../../../../../shared/src/CommonConstants';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';

const ICON_WEAPONS_IDS =
[
	WEAPONS.RICOCHET,
	WEAPONS.ARTILLERYSTRIKE,
	WEAPONS.CRYOGUN,
	WEAPONS.FLAMETHROWER,
	WEAPONS.INSTAKILL,
	WEAPONS.RICOCHET_POWER_UP,
	WEAPONS.ARTILLERYSTRIKE_POWER_UP,
	WEAPONS.CRYOGUN_POWER_UP,
	WEAPONS.FLAMETHROWER_POWER_UP,
	WEAPONS.INSTAKILL_POWER_UP
];

const ASSETS_NAMES =
[
	"player_spot/battleground/icons/ricochet",
	"player_spot/battleground/icons/artillery_strike",
	"player_spot/battleground/icons/cryogun",
	"player_spot/battleground/icons/flamethrower",
	"player_spot/battleground/icons/plasma",
	"player_spot/battleground/icons/ricochet_highlight",
	"player_spot/battleground/icons/artillery_strike_highlight",
	"player_spot/battleground/icons/cryogun_highlight",
	"player_spot/battleground/icons/flamethrower_highlight",
	"player_spot/battleground/icons/plasma_highlight"
];

class BattlegroundSpotWeaponIconView extends Sprite
{
	constructor()
	{
		super();


		this._fContentContainer_s = this.addChild(new Sprite())
		this._fIconsContainer_s = this._fContentContainer_s.addChild(new Sprite())
		this._fIntroAnimation_mtl = null;
		this._fOutroAnimation_mtl = null;
		this._fRechargeAnimation_mtl = null;
		this._fIsRequired_bl = false;
		this._fIconImages_s_arr = [];
		this._fWeaponId_int = undefined;
		this._fRemainingShotsCount_int = 0;
		this._fBlick_s = null;
		this._fBlueFlash_s = null;


		this._fContentContainer_s.visible = false;
		//ADDING ALL AVAILABLE SW IMAGES...
		for( let i = 0; i < ICON_WEAPONS_IDS.length; i++ )
		{
			let l_s = APP.library.getSpriteFromAtlas(ASSETS_NAMES[i]);
			l_s.anchor.set(0.5, 0.5);
			l_s.visible = false;

			this._fIconImages_s_arr[i] = l_s;
			this._fIconsContainer_s.addChild(l_s);
		}
		//...ADDING ALL AVAILABLE SW IMAGES

		//BLICK...
		let l_s = APP.library.getSpriteFromAtlas("player_spot/battleground/icons/fx/blick");
		l_s.anchor.set(0.5, 0.5);
		l_s.alpha = 0;
		this._fBlick_s = this._fIconsContainer_s.addChild(l_s);
		//...BLICK

		//BLUE FLASH...
		l_s = APP.library.getSpriteFromAtlas("player_spot/battleground/icons/fx/blue_flash");
		l_s.anchor.set(0.5, 0.5);
		l_s.scale.set(0, 0);
		l_s.blendMode = PIXI.BLEND_MODES.ADD;
		this._fBlueFlash_s = this._fContentContainer_s.addChild(l_s);
		//...BLUE FLASH


		//ANIMATIONS...
		//INTRO...
		let l_mtl = new MTimeLine();

		l_mtl.callFunctionAtFrame(
			this._fContentContainer_s.show,
			1,
			this._fContentContainer_s);

		l_mtl.addAnimation(
			this._fIconsContainer_s,
			MTimeLine.SET_SCALE,
			0,
			[
				10,
				[0.68, 3],
				[1, 2],
				[0.87, 5],
				[1, 13],
			]);

		l_mtl.addAnimation(
			this._fBlick_s,
			MTimeLine.SET_ALPHA,
			1,
			[
				10,
				[0, 10],
			]);

		l_mtl.addAnimation(
			this._fBlueFlash_s,
			MTimeLine.SET_SCALE,
			0,
			[
				[1.48, 3],
				[0, 15],
			]);

		l_mtl.addAnimation(
			this._fBlueFlash_s,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			36,
			[
				[48, 18],
			]);

		this._fIntroAnimation_mtl = l_mtl;
		//...INTRO


		//OUTRO...
		l_mtl = new MTimeLine();

		l_mtl.addAnimation(
			this._fIconsContainer_s,
			MTimeLine.SET_SCALE,
			1,
			[
				[1.25, 10],
				[0, 10],
			]);

		l_mtl.callFunctionAtFrame(
			this.drop,
			20,
			this);

		this._fOutroAnimation_mtl = l_mtl;
		//...OUTRO

		//RECHARGE...
		l_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fBlick_s,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 7],
				[0, 7],
			]);

		l_mtl.addAnimation(
			this._fIconsContainer_s,
			MTimeLine.SET_SCALE,
			1,
			[
				[1.149, 5],
				[1, 13],
			]);

		this._fRechargeAnimation_mtl = l_mtl;
		//...RECHARGE
		//...ANIMATIONS
	}

	setRemainingShotsCount(aRemainingShotsCount_int)
	{
		this._fRemainingShotsCount_int = aRemainingShotsCount_int;
	}

	getRemainingShotsCount()
	{
		return this._fRemainingShotsCount_int;
	}

	drop()
	{
		this._fRemainingShotsCount_int = 0;
		this._fIsRequired_bl = false;
		this._fContentContainer_s.visible = false;
		this._resetAnimations();
	}

	canBeReused()
	{
		return !this._fContentContainer_s.visible && !this._fIsRequired_bl;
	}

	isRequired()
	{
		return this._fIsRequired_bl;
	}

	setIsRequired(aIsRequired_bl)
	{
		this._fIsRequired_bl = aIsRequired_bl;
	}


	_resetAnimations()
	{
		this._fIntroAnimation_mtl.stop();
		this._fOutroAnimation_mtl.stop();
		this._fRechargeAnimation_mtl.stop();
	}

	startIntroAnimation(aWeaponId_int, aShotsCount_int)
	{
		this._resetAnimations();

		this._fRemainingShotsCount_int = aShotsCount_int;
		this._fWeaponId_int = aWeaponId_int;
		this._fIsRequired_bl = true;

		//DISPLAYING RQUIRED WEAPON IMAGE...
		for( let i = 0; i < ICON_WEAPONS_IDS.length; i++ )
		{
			this._fIconImages_s_arr[i].visible = (ICON_WEAPONS_IDS[i] === aWeaponId_int);
		}
		//...DISPLAYING RQUIRED WEAPON IMAGE

		this._fIntroAnimation_mtl.play();
	}

	tryToPlayRechargeAnimation()
	{
		if(
			this._fRechargeAnimation_mtl.isPlaying() ||
			this._fIntroAnimation_mtl.isPlaying() ||
			this._fOutroAnimation_mtl.isPlaying()
			)
		{
			return;
		}

		this._resetAnimations();
		this._fRechargeAnimation_mtl.play();
	}

	startOutroAnimation()
	{
		this._resetAnimations();
		this._fOutroAnimation_mtl.play();
	}

	getWeaponId()
	{
		return this._fWeaponId_int;
	}

	isOutroAnimationInProcess()
	{
		return this._fOutroAnimation_mtl.isPlaying();
	}

	skipIntroIfPlaying()
	{
		if(this._fIntroAnimation_mtl.isPlaying())
		{
			this._fIntroAnimation_mtl.windToEnd();
		}
	}
	
	destroy()
	{
		this._fIntroAnimation_mtl && this._fIntroAnimation_mtl.destroy();
		this._fIntroAnimation_mtl = null;
		
		this._fOutroAnimation_mtl && this._fOutroAnimation_mtl.destroy();
		this._fOutroAnimation_mtl = null;
		
		this._fRechargeAnimation_mtl && this._fRechargeAnimation_mtl.destroy();
		this._fRechargeAnimation_mtl = null;
		
		this._fIntroAnimation_mtl = null;
		this._fOutroAnimation_mtl = null;
		this._fRechargeAnimation_mtl = null;
		this._fIsRequired_bl = false;
		this._fIconImages_s_arr = null;
		this._fWeaponId_int = null;
		this._fRemainingShotsCount_int = null;
		this._fBlick_s = null;
		this._fBlueFlash_s = null;
		this._fHighLevelWeaponView_sprt = null;

		super.destroy();
	}
}

export default BattlegroundSpotWeaponIconView;