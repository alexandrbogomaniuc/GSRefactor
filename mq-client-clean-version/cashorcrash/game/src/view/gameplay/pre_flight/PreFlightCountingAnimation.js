import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import PreFlightCountingDigitAnimation from './PreFlightCountingDigitAnimation';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const COUNTING_DESCRIPTOR = 
[
	{ digit: PreFlightCountingDigitAnimation.SUPPORTED_VALUES.THREE,	startTimeOffset: 0 },
	{ digit: PreFlightCountingDigitAnimation.SUPPORTED_VALUES.TWO,		startTimeOffset: 1000 },
	{ digit: PreFlightCountingDigitAnimation.SUPPORTED_VALUES.ONE,		startTimeOffset: 2000 },
	{ digit: PreFlightCountingDigitAnimation.SUPPORTED_VALUES.GO,		startTimeOffset: 3000 }
]

class PreFlightCountingAnimation extends Sprite
{
	constructor()
	{
		super();

		this._fDigitAnimations_pfcda_arr = [];
	}

	_getDigitAnimation(aDigitIndex_int)
	{
		if (!!this._fDigitAnimations_pfcda_arr[aDigitIndex_int])
		{
			return this._fDigitAnimations_pfcda_arr[aDigitIndex_int]
		}

		let lDigitDescriptor_obj = COUNTING_DESCRIPTOR[aDigitIndex_int];
		let lDigitAnimation_pfcda = this.addChildAt(new PreFlightCountingDigitAnimation(lDigitDescriptor_obj.digit), 0);

		this._fDigitAnimations_pfcda_arr[aDigitIndex_int] = lDigitAnimation_pfcda;

		return lDigitAnimation_pfcda;
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lCurGameplayTime_num = l_gpi.gameplayTime;
		let lBetsInfo_bsi = l_gpi.gamePlayersInfo.betsInfo;

		if (lRoundInfo_ri.isRoundPauseState || lRoundInfo_ri.isRoundPlayState)
		{
			let lAnimPreStartDuration_num = COUNTING_DESCRIPTOR[COUNTING_DESCRIPTOR.length-1].startTimeOffset;
			let lCurAnimDuration_num = lAnimPreStartDuration_num - (lRoundInfo_ri.roundStartTime - l_gpi.gameplayTime);
			
			if (APP.isBattlegroundGame)
			{
				lCurAnimDuration_num += l_gpi.preLaunchFlightDuration;
			}

			for (let i=0; i<COUNTING_DESCRIPTOR.length; i++)
			{
				let lCurDescr_obj = COUNTING_DESCRIPTOR[i];
				let lCurDigitAnim_pfcda = this._getDigitAnimation(i);
				if (lCurAnimDuration_num >= lCurDescr_obj.startTimeOffset)
				{
					lCurDigitAnim_pfcda.adjust(lCurAnimDuration_num-lCurDescr_obj.startTimeOffset);
					lCurDigitAnim_pfcda.visible = true;
				}
				else
				{
					lCurDigitAnim_pfcda.visible = false;
				}
			}

			this.visible = true;
		}
		else
		{
			this.visible = false;
		}
	}

	updateArea()
	{
		if (APP.layout.isPortraitOrientation)
		{
			this.scale.set(0.82, 0.82);
		}
		else
		{
			this.scale.set(1, 1);
		}
	}
}
export default PreFlightCountingAnimation;