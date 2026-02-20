import ProfilingInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const DYNAMIC_FVX_PROFILE_STEP = 10; // total animations amount required to make one vfx profile decrease step

const FPS_LOW_PROFILE_TRESHOLD = 10;
const FPS_LOWER_PROFILE_TRESHOLD = 20;
const FPS_HIGH_PROFILE_TRESHOLD = 35;

class GameProfilingInfo extends ProfilingInfo {

	constructor()
	{
		super();

		this._fGameOptimizationInfo_goi = null;
	}

	//VFX...
	getProfileValue(aProfileId_str, aIsDynamicProfileValueRequired_bl = false)
	{
		let profileValue = super.getProfileValue(aProfileId_str);

		if (aIsDynamicProfileValueRequired_bl)
		{
			// !!! NOTE: dynamic profile value should NOT be Greater then computed profile value!!!
			profileValue = this._reduceProfileValueIfRequired(aProfileId_str, profileValue);
		}

		return profileValue;
	}

	_reduceProfileValueIfRequired(aProfileId_str, aActualProfileValue_str)
	{
		let profileValue = aActualProfileValue_str;

		if (aProfileId_str === ProfilingInfo.i_VFX_LEVEL_PROFILE)
		{
			profileValue = this._reduceVFXProfileValueIfRequired(aActualProfileValue_str);
		}

		return profileValue;
	}

	_reduceVFXProfileValueIfRequired(aActualProfileValue_str)
	{
		let profileValue = aActualProfileValue_str;
		let curProfileIndex = ProfilingInfo.i_getProfileValueWeight(ProfilingInfo.i_VFX_LEVEL_PROFILE, profileValue);
		
		if (curProfileIndex <= 0)
		{
			return profileValue;
		}

		var lastFPS = APP.lastFps;

		if (lastFPS <= FPS_LOW_PROFILE_TRESHOLD)
		{
			curProfileIndex = 0;
		}
		else if (lastFPS < FPS_HIGH_PROFILE_TRESHOLD)
		{
			let lGameOptimizationinfo_goi = this._gameOptimizationinfo;
			let lArtilleryStrikesInfo_gassi = this._artilleryStrikesInfo;

			let curBulletsAmount = lGameOptimizationinfo_goi.currentShotsAmount;
			let curExplosionsAmount = lGameOptimizationinfo_goi.currentGrenadeExplosionsNumber
										+ lGameOptimizationinfo_goi.currentCeilingDustAnimationsNumber
										+ lGameOptimizationinfo_goi.currentFlameThrowerHitExplosionsNumber
										+ lArtilleryStrikesInfo_gassi.activeArtilleryStrikesCounter;

			let curAwardsAmount = lGameOptimizationinfo_goi.currentAwardsAmount;
			let curTreasureAwardsAmount = lGameOptimizationinfo_goi.currentTreasuresAnimationsAmount;

			let curAnimationsAmount = curBulletsAmount + curExplosionsAmount + curAwardsAmount + curTreasureAwardsAmount;

			let indexDecreaseDelta_num = Math.floor(curAnimationsAmount/DYNAMIC_FVX_PROFILE_STEP);
			curProfileIndex -= indexDecreaseDelta_num;
			if (curProfileIndex <= 0)
			{
				curProfileIndex = 0;
			}

			if (lastFPS <= FPS_LOWER_PROFILE_TRESHOLD)
			{
				curProfileIndex = Math.min(curProfileIndex, 1);
			}
		}

		profileValue = ProfilingInfo.i_getProfileValueByWeight(ProfilingInfo.i_VFX_LEVEL_PROFILE, curProfileIndex);		

		return profileValue;
	}

	get _gameOptimizationinfo()
	{
		return this._fGameOptimizationInfo_goi || (this._fGameOptimizationInfo_goi = this._initGameOptimizationInfo());
	}

	_initGameOptimizationInfo()
	{
		return APP.currentWindow.gameOptimizationController.info;
	}

	get _artilleryStrikesInfo()
	{
		return this._fArtilleryStrikesInfo_gassi || (this._fArtilleryStrikesInfo_gassi = this._initArtilleryStrikesInfo());
	}

	_initArtilleryStrikesInfo()
	{
		return APP.currentWindow.artilleryStrikesController.info;
	}
}

export default GameProfilingInfo;