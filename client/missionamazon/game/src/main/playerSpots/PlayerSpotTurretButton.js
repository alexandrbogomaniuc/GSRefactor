import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PointerSprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/ui/PointerSprite';
import { GREYSCALE_FILTER } from '../../config/Constants';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';


class PlayerSpotTurretButton extends PointerSprite
{
	set enabled(aValue_bl)
	{
		if (aValue_bl)
		{
			this.setEnabled();
		}
		else
		{
			this.setDisabled();
		}
	}

	get enabled()
	{
		return !!this.interactive;
	}

	set active(aValue_bl)
	{
		if (this._fIsActive_bl != aValue_bl)
		{
			this._fIsActive_bl = aValue_bl;
			
			if (aValue_bl)
			{
				this._startAppearAnimation();
			}
			else
			{
				if (this._fArrow_sprt.scale.x >= 0.9)
				{
					this._startDisappearAnimation();
				}
			}

			this._invalidateBoby();
		}
	}


	get active()
	{
		return this._fIsActive_bl;
	}

	get mainPlayerSpot()
	{
		return APP.gameScreen.gameField.spot;
	}

	updateView()
	{
		this._invalidateBoby();
	}

	constructor()
	{
		super();

		this._fIsActive_bl = false;
		this._fBody_sprt = null;
		this._fArrow_sprt = null;
		this._fDisappearSeq = null;

		this._init();

		//set cursor to default over the sidebar icon
		var cursorController = APP.currentWindow.cursorController;
		this.mouseover = () => cursorController.setOverRestrictedZone(true);
		this.mouseout = () => cursorController.setOverRestrictedZone(false);
	}

	_init()
	{
		let turretButtonBase = this.addChild(APP.library.getSpriteFromAtlas("player_spot/turret_btn_base"));

		this._fBody_sprt = this.addChild(new Sprite);
		
		let lArrow_sprt = this._fArrow_sprt = this.addChild(APP.library.getSpriteFromAtlas("weapons/sidebar/back_arrow"));
		lArrow_sprt.position.y = -2;
		lArrow_sprt.scale.set(0.9);
		
		this._initButtonBehaviour();

		this._invalidateBoby();
	}

	_initButtonBehaviour()
	{
		this.setHitArea(new PIXI.Circle(0, 0, 22));
		this.setEnabled();

		this.on("pointerdown", (e)=>e.stopPropagation(), this);
	}

	setEnabled()
	{
		super.setEnabled();

		this.filters = null;
	}

	setDisabled()
	{
		super.setDisabled();

		this.filters = [GREYSCALE_FILTER];
	}

	_startAppearAnimation()
	{
		Sequence.destroy(Sequence.findByTarget(this._fArrow_sprt));

		this._fArrow_sprt.visible = true;
		this._fArrow_sprt.scale.set(0);

		let lSeq = [
			{ tweens: [ { prop: "scale.x", to: 1.1*0.9 }, { prop: "scale.y", to: 1.1*0.9 }], duration: 6*FRAME_RATE },
			{ tweens: [ { prop: "scale.x", to: 1.2*0.9 }, { prop: "scale.y", to: 1.2*0.9 }], duration: 5*FRAME_RATE },
			{ tweens: [ { prop: "scale.x", to: 1.0*0.9 }, { prop: "scale.y", to: 1.0*0.9 }], duration: 4*FRAME_RATE }
		];

		Sequence.start(this._fArrow_sprt, lSeq);
	}

	_startDisappearAnimation()
	{
		this.filters = null;

		Sequence.destroy(Sequence.findByTarget(this._fArrow_sprt));

		let lSeq = [
			{ tweens: [ { prop: "scale.x", to: 1.2 }, { prop: "scale.y", to: 1.2 }], duration: 4*FRAME_RATE },
			{ tweens: [ { prop: "scale.x", to: 1.1 }, { prop: "scale.y", to: 1.1 }], duration: 5*FRAME_RATE },
			{ tweens: [ { prop: "scale.x", to: 0.2 }, { prop: "scale.y", to: 0.2 }], duration: 4*FRAME_RATE, onfinish: () => { this._onDisappearAnimationCompleted(); } }
		];

		this._fDisappearSeq = Sequence.start(this._fArrow_sprt, lSeq);
	}

	_onDisappearAnimationCompleted()
	{
		Sequence.destroy(Sequence.findByTarget(this._fArrow_sprt));
		this._fDisappearSeq = null;

		this._invalidateBoby();
	}

	_invalidateBoby()
	{
		this._fBody_sprt.destroyChildren();
		this._fBody_sprt.rotation = 0;

		if (this._fIsActive_bl || !!this._fDisappearSeq)
		{
			let lBack = this._fBody_sprt.addChild(APP.library.getSpriteFromAtlas("player_spot/turret_btn_back"));
			lBack.alpha = 0.85;

			let lBetLevel_num = APP.playerController.info.betLevel;
			let lPlayerInfo_pi = APP.playerController.info;

			let lViewType_int = lPlayerInfo_pi.getTurretSkinId(lPlayerInfo_pi.betLevel);
			let lAssetName_str = "weapons/DefaultGun/turret_"+lViewType_int+"/turret";
			
			let lView = this._fBody_sprt.addChild(APP.library.getSpriteFromAtlas(lAssetName_str));
			lView.scale.set(0.4)

			if (lViewType_int == 1)
			{
				lView.anchor.set(0.5, 0.585);
			}

			this._fBody_sprt.rotation = Utils.gradToRad(20);
			this.zIndex = 27024;													//Z_INDEXES.WEAPON_ON_SPOT - 1

			if(this.mainPlayerSpot) 
			{
				this.mainPlayerSpot.weaponSpotView.parent.zIndex = 27025;			//Z_INDEXES.BET_LEVEL_BUTTON_HIT_AREA + 1
			}
		}
		else
		{
			
			this._fBody_sprt.addChild(APP.library.getSpriteFromAtlas("player_spot/turret_icon"));
			this._fArrow_sprt.visible = false;

			if (!this.enabled)
			{
				this.setDisabled();
			}
			this.zIndex = 0;

			if(this.mainPlayerSpot) 
			{
				this.mainPlayerSpot.weaponSpotView.parent.zIndex = 27022;			//Z_INDEXES.WEAPON_ON_SPOT
			}
		}
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this._fArrow_sprt));
		this._fDisappearSeq = null;

		this._fIsActive_bl = undefined;
		this._fBody_sprt = null;
		this._fArrow_sprt = null;

		super.destroy();
	}
}

export default PlayerSpotTurretButton