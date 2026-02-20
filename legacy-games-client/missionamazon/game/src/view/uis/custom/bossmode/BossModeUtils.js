import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';

class BossModeUtils
{
	static startAnimationSequence(aTarget_sprt, aSequence_obj_arr, aInitial_obj = {}, aDelay_num = 0, aOptOnComplete_fnc = null)
	{
		Sequence.destroy(Sequence.findByTarget(aTarget_sprt));

		if (aInitial_obj.x !== undefined) aTarget_sprt.position.x = aInitial_obj.x;
		if (aInitial_obj.y !== undefined) aTarget_sprt.position.y = aInitial_obj.y;
		if (aInitial_obj.scale !== undefined) aTarget_sprt.scale.set(aInitial_obj.scale);
		if (aInitial_obj.alpha !== undefined) aTarget_sprt.alpha = aInitial_obj.alpha;

		let lSequence_s, lSequenceDuration_num = 0;
		if (aSequence_obj_arr.length)
		{
			for (var i = 0; i < aSequence_obj_arr.length; i++)
			{
				let l_s = Sequence.start(aTarget_sprt, aSequence_obj_arr[i], aDelay_num);
				
				if (aOptOnComplete_fnc)
				{
					let lDuration_num = aDelay_num;
					for (var j = 0; j < aSequence_obj_arr[i].length; j++)
					{
						lDuration_num += (aSequence_obj_arr[i][j].duration || 0);
					}
					if (lDuration_num > lSequenceDuration_num)
					{
						lSequence_s = l_s;
					}
				}
			}
		}

		if (aOptOnComplete_fnc && lSequence_s)
		{
			lSequence_s.on("finish", aOptOnComplete_fnc);
		}
	}

	static generateWiggleSequence(aParams_obj)
	{
		let lXOffset_num = aParams_obj.x || 0;
		let lYOffset_num = aParams_obj.y || 0;
		let lPeriod_num = aParams_obj.period || 300;
		let lDuration_num = aParams_obj.duration || 300;

		let lSequence_s = [];
		let lTotalDuration_num = 0;
		while (lTotalDuration_num < lDuration_num)
		{
			let lCurrentDuration_num = Math.min(lPeriod_num, lDuration_num - lTotalDuration_num);
			let lTweens_arr = [];
			if (aParams_obj.x)
			{
				lTweens_arr.push({prop:"x", to:aParams_obj.x*(2*Math.random()-1)});
			}
			if (aParams_obj.y)
			{
				lTweens_arr.push({prop:"y", to:aParams_obj.y*(2*Math.random()-1)});
			}
			if (aParams_obj.alpha)
			{
				let lFromAlpha_num = aParams_obj.from || 0;
				let lToAlpha_num = aParams_obj.to || 1;
				lTweens_arr.push({prop:"alpha", to:lFromAlpha_num+Math.random()*(lToAlpha_num-lFromAlpha_num)});
			}
			lSequence_s.push({ tweens:lTweens_arr, duration:lCurrentDuration_num, ease:Easing.sine.easeInOut });
			lTotalDuration_num += lCurrentDuration_num;
		}
		return lSequence_s;
	}
}

export default BossModeUtils;