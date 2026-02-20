import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MissEffect from './MissEffect';
import MissEffectLevel1 from './MissEffectLevel1';
import MissEffectLevel2 from './MissEffectLevel2';
import MissEffectLevel3 from './MissEffectLevel3';
import MissEffectLevel4 from './MissEffectLevel4';
import MissEffectLevel5 from './MissEffectLevel5';
import { COOP_FIRE_FX_ALPHA } from '../../../../shared/src/CommonConstants';

class MissEffectsPool extends Sprite
{
	constructor()
	{
		super();

		this._fMissEffects_me_arr_arr = [];
		this._fLayers_s_arr = [];

		//ADDING LAYER FOR EACH MISS EFFECT TYPE...
		for (let i = 0; i < MissEffect.TYPES_COUNT; i++)
		{
			this._fMissEffects_me_arr_arr[i] = [];
			this._fLayers_s_arr[i] = this.addChild(new Sprite());
		}
		//...ADDING LAYER FOR EACH MISS EFFECT TYPE
	}

	getNextEffect(aTypeId_int, aOptIsMasterEffect_bl = true)
	{
		let l_me_arr_arr = this._fMissEffects_me_arr_arr;
		let lTypeId_int = aTypeId_int;

		//SEARCING IF ANY IS AVAILABLE TO REUSE...
		for (let i = 0; i < l_me_arr_arr[lTypeId_int].length; i++)
		{
			if (l_me_arr_arr[lTypeId_int][i].canBeReused())
			{
				if (!aOptIsMasterEffect_bl && l_me_arr_arr[lTypeId_int][i])
				{
					l_me_arr_arr[lTypeId_int][i].alpha = COOP_FIRE_FX_ALPHA;
				}
				else
				{
					l_me_arr_arr[lTypeId_int][i].alpha = 1;
				}
				return l_me_arr_arr[lTypeId_int][i];
			}
		}
		//...SEARCING IF ANY IS AVAILABLE TO REUSE

		//NEW MISS EFFECT...
		let l_me = null;

		switch (lTypeId_int)
		{
			case MissEffect.TYPE_ID_LEVEL_1:
				l_me = new MissEffectLevel1();
				break;
			case MissEffect.TYPE_ID_LEVEL_2:
				l_me = new MissEffectLevel2();
				break;
			case MissEffect.TYPE_ID_LEVEL_3:
				l_me = new MissEffectLevel3();
				break;
			case MissEffect.TYPE_ID_LEVEL_4:
				l_me = new MissEffectLevel4();
				break;
			case MissEffect.TYPE_ID_LEVEL_5:
				l_me = new MissEffectLevel5();
				break;
			default:
				l_me = new MissEffectLevel1();
				break;
		}

		if (!aOptIsMasterEffect_bl)
		{
			l_me.alpha = COOP_FIRE_FX_ALPHA;
		}

		l_me_arr_arr[lTypeId_int].push(l_me);
		this._fLayers_s_arr[lTypeId_int].addChild(l_me);

		return l_me;
		//...NEW MISS EFFECT
	}


	addMissEffect(aTypeId_int, aX_num, aY_num, aOptIsMasterEffect_bl = true)
	{
		this.getNextEffect(aTypeId_int, aOptIsMasterEffect_bl).reset(aX_num, aY_num);
	}

	drop()
	{
		let l_me_arr_arr = this._fMissEffects_me_arr_arr;

		for (let i = 0; i < MissEffect.TYPES_COUNT; i++)
		{
			for (let j = 0; j < l_me_arr_arr[i].length; j++)
			{
				l_me_arr_arr[i][j].drop();
			}
		}
	}

	destroy()
	{
		let l_me_arr_arr = this._fMissEffects_me_arr_arr;

		for (let i = 0; i < MissEffect.TYPES_COUNT; i++)
		{
			for (let j = 0; j < l_me_arr_arr[i].length; j++)
			{
				l_me_arr_arr[i][j].destroy();
			}
		}

		super.destroy();
	}
}

export default MissEffectsPool;