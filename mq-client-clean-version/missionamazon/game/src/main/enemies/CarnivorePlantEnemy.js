import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { AtlasSprite, Sprite } from "../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import { APP } from "../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import ProfilingInfo from "../../../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo";
import { FRAME_RATE } from "../../../../shared/src/CommonConstants";
import AtlasConfig from "../../config/AtlasConfig";
import { STATE_IDLE } from "./Enemy";
import PlantEnemy from "./PlantEnemy";

class CarnivorePlantEnemy extends PlantEnemy
{
	i_interruptPreDeathAnimationAndInstantDestroy()
	{
		this._fSpawnTimer_t && this._fSpawnTimer_t.destructor();
		this._fSpawnTimer_t = null;

		this._fSpawnSmoke_spr && this._fSpawnSmoke_spr.removeAllListeners();
		this._fSpawnSmoke_spr && this._fSpawnSmoke_spr.destroy();
		this._fSpawnSmoke_spr = null;

		this._fMouthSmoke_spr && this._fMouthSmoke_spr.destroy();
		this._fMouthSmoke_spr = null;

		this._fDeathAnimationsCount_num = null;
		this._fIsExpectedInstantDestroy_bl = true;

		this._playDeathFxAnimation(true);
	}

	i_playPreDeathAnimation()
	{
		if (this._fIsPreDeathAnimationPlaying_bl || this._fIsExpectedInstantDestroy_bl)
		{
			return;
		}

		this._fSpawnTimer_t && this._fSpawnTimer_t.destructor();
		this._fSpawnTimer_t = null;

		this._fIsPreDeathAnimationPlaying_bl = true;

		super.i_playPreDeathAnimation();

		if (APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM))
		{
			return;
		}

		if(!this.spineView)
		{
			return;
		}

		let lTrack_obj = this.spineView.view.state.tracks[0];
		this.spineView.on("update", () => {
			if (lTrack_obj.trackTime <= 0.5*lTrack_obj.animationEnd)
			{
				if(this.spineView)
				{
					this.spineView.off("update");
				}
				this._fDeathAnimationsCount_num++;
				this._playSpawnGroundSmoke();
			}
		});
		this.spineView.on("reverseAnimationCompleted", ()=>{
			this.emit(PlantEnemy.EVENT_ON_ENEMY_START_DYING);
			this._fDeathAnimationInProgress_bl = true;
		});
	}

	constructor(params)
	{
		super(params);

		this._fIsExpectedInstantDestroy_bl = null;
		this._fIsPreDeathAnimationPlaying_bl = null;
	}

	__startGrowAnimation()
	{
		if (this._fIsExpectedInstantDestroy_bl)
		{
			return;
		}

		if (APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM))
		{
			super.__startGrowAnimation();
			return;
		}
		this._playSpawnGroundSmoke();

		this._fSpawnTimer_t = new Timer(super.__startGrowAnimation.bind(this), 20*FRAME_RATE);
	}

	__startIdleAnimations()
	{
		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._startIdleMouthSmoke();
		}
	}

	_playSpawnGroundSmoke()
	{
		this._fSpawnSmoke_spr = this.container.addChild(new Sprite());
		this._fSpawnSmoke_spr.zIndex = 5;
		this._fSpawnSmoke_spr.scale.set(2.5);
		this._fSpawnSmoke_spr.anchor.set(.5, .6);

		let l_t = AtlasSprite.getFrames([APP.library.getAsset('enemies/plants/spawn_smoke')], [AtlasConfig.CarnivorePlantSpawnSmoke], "");
		this._fSpawnSmoke_spr.textures = l_t;

		if (this.spineView && this.spineView.i_isAnimationReverse || !this.spineView)
		{
			this.shadow.visible = false;
		}

		this._fSpawnSmoke_spr.on('animationend', ()=>{
			this._fDeathAnimationsCount_num--;
			this._fSpawnSmoke_spr && this._fSpawnSmoke_spr.destroy();

			if (
				!this._fIsExpectedInstantDestroy_bl &&
				(this.spineView && this.spineView.i_isAnimationReverse || !this.spineView)
				)
			{
				this.destroy();
			}
		});
		this._fSpawnSmoke_spr.play();
	}

	_startIdleMouthSmoke()
	{
		if (
			!this.spineView
			|| (this.state != STATE_IDLE)
		)
		{
			return
		}

		this._fMouthSmoke_spr = this.container.addChild(new Sprite());
		this._fMouthSmoke_spr.textures = AtlasSprite.getFrames([APP.library.getAsset('enemies/plants/mouth_smoke')], [AtlasConfig.CarnivorePlantMouthSmoke], "");
		this._fMouthSmoke_spr.zIndex = 5;
		this._fMouthSmoke_spr.visible = false;
		this._fMouthSmoke_spr.scale.set(1.6);
		this._fMouthSmoke_spr.position.set(0, -85);
		
		this._fMouthSmoke_spr.on('animationend', ()=>{
			if(this._fMouthSmoke_spr)
			{
				this._fMouthSmoke_spr.stop();
				this._fMouthSmoke_spr.visible = false;
			}
		});

		let lSmokeStamp_obj = {
			function: ()=>{
				this._fMouthSmoke_spr.gotoAndPlay(0);
				this._fMouthSmoke_spr.visible = true;
			},
			timeStamps: 0.9,
			percentDelta: 0.017,
			animationName: STATE_IDLE
		};
		this.spineView.addCallFunctionAtStamps(lSmokeStamp_obj);
	}

	//override
	_getHitRectWidth()
	{
		return 100;
	}
	
	//override
	_getHitRectHeight()
	{
		return 140;
	}

	destroy(purely = false)
	{
		this._fSpawnTimer_t && this._fSpawnTimer_t.destructor();
		this._fSpawnTimer_t = null;

		this._fSpawnSmoke_spr && this._fSpawnSmoke_spr.removeAllListeners();
		this._fSpawnSmoke_spr && this._fSpawnSmoke_spr.destroy();
		this._fSpawnSmoke_spr = null;

		this._fMouthSmoke_spr && this._fMouthSmoke_spr.destroy();
		this._fMouthSmoke_spr = null;

		this._fDeathAnimationsCount_num = null;

		this._fIsExpectedInstantDestroy_bl = null;
		this._fIsPreDeathAnimationPlaying_bl = null;

		super.destroy(purely);
	}
}

export default CarnivorePlantEnemy;