import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import BattlegroundFinalCountingInfo from '../../../model/uis/final_counting/BattlegroundFinalCountingInfo';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

const ASSETS = [
	{name: "3", translation: false},
	{name: "2", translation: false},
	{name: "1", translation: false},
	{name: "TABattlegroundFinalCountingBattle", translation: true},
];

class BattlegroundFinalCountingView extends SimpleUIView
{
	static get EVENT_ON_COUNT_FINISHED()									{ return "EVENT_ON_COUNT_FINISHED"; }
	static get EVENT_ON_BATTLEGROUND_FINAL_COUNTING_NEXT_COUNT_STARTED()	{ return "EVENT_ON_BATTLEGROUND_FINAL_COUNTING_NEXT_COUNT_STARTED"; }

	initOnScreen(containerInfo)
	{
		containerInfo.container.addChild(this);

		this.zIndex = containerInfo.zIndex;
		this.position.set(0, 0);
	}

	reset()
	{
		this._reset();
	}

	startCountingAnimation(aNumber_int)
	{
		this._nextNumberAnimation(aNumber_int);
	}

	constructor()
	{
		super();

		this._fContainerNumber_spr_arr = [];
		this._fNumber_spr_arr = [];
		this._fNumberGlow_spr_arr = [];

		this.init()
	}

	init()
	{
		for (let i = 0; i < BattlegroundFinalCountingInfo.NUMBER_COUNT; i++)
		{
			this._fContainerNumber_spr_arr[i] = this.addChild(new Sprite());
			this._fContainerNumber_spr_arr[i].scale.set(0, 0);

			let l_sprt = this._getNumberSprite(i);
			l_sprt = this._getNumberGlowSprite(i);
		}
	}

	_getNumberSprite(aNumber_int)
	{
		return this._fNumber_spr_arr[aNumber_int] || (this._fNumber_spr_arr[aNumber_int] = this._initNumberSprite(aNumber_int));
	}

	_initNumberSprite(aNumber_int)
	{
		let l_sprt = null;

		if (ASSETS[aNumber_int].translation)
		{
			l_sprt = this._fContainerNumber_spr_arr[aNumber_int].addChild(I18.generateNewCTranslatableAsset(ASSETS[aNumber_int].name));
		}
		else
		{
			l_sprt = this._fContainerNumber_spr_arr[aNumber_int].addChild(APP.library.getSpriteFromAtlas("final_count/"+ASSETS[aNumber_int].name));
		}
		
		return l_sprt;
	}

	_getNumberGlowSprite(aNumber_int)
	{
		return this._fNumberGlow_spr_arr[aNumber_int] || (this._fNumberGlow_spr_arr[aNumber_int] = this._initNumberGlowSprite(aNumber_int));
	}

	_initNumberGlowSprite(aNumber_int)
	{
		let l_sprt = null;

		if (ASSETS[aNumber_int].translation)
		{
			l_sprt = this._fContainerNumber_spr_arr[aNumber_int].addChild(I18.generateNewCTranslatableAsset(ASSETS[aNumber_int].name+"Glow"));
		}
		else
		{
			l_sprt = this._fContainerNumber_spr_arr[aNumber_int].addChild(APP.library.getSpriteFromAtlas("final_count/"+ASSETS[aNumber_int].name+"_glow"));
		}
		
		return l_sprt;
	}

	_nextNumberAnimation(aNumber_int)
	{
		if (aNumber_int >= BattlegroundFinalCountingInfo.NUMBER_COUNT)
		{
			APP.logger.i_pushWarning(`Final Count. Not correct value of a nubmer: ${aNumber_int}`);
			console.error("Not correct value of a nubmer: "+aNumber_int); 
			return;
		}

		let lNumber_sprt = this._getNumberSprite(aNumber_int);
		lNumber_sprt.alpha = 0;

		let lNumberGlow_sprt = this._getNumberGlowSprite(aNumber_int);
		lNumberGlow_sprt.alpha = 1;

		let lContainerNumbet_sprt = this._fContainerNumber_spr_arr[aNumber_int];
		lContainerNumbet_sprt.scale.set(0, 0);
		
		let lSequenceContainerScale_arr = [
			{
				tweens: [
							{ prop: "scale.x", to: 1 }, 
							{ prop: "scale.y", to: 1 }
						],
				duration: 7 * FRAME_RATE,
				ease: Easing.sine.easeOut
			},
			{
				tweens: [],	duration: 20 * FRAME_RATE 
			},
			{
				tweens: [
							{ prop: "scale.x", to: 1.211 }, 
							{ prop: "scale.y", to: 1.211 }],
				duration: 2 * FRAME_RATE,
				ease: Easing.sine.easeIn
			},
			{
				tweens: [
							{ prop: "scale.x", to: 0 },
							{ prop: "scale.y", to: 0 }],
				duration: 9 * FRAME_RATE,
				ease: Easing.sine.easeOut,
				onfinish: () => {
					this._animationCompleted(aNumber_int);
				}
			}
		];

		let lSequenceNumberAlpha_arr = [
			{
				tweens: [
							{ prop: "alpha", to: 1 }
						],
				duration: 4 * FRAME_RATE,
				ease: Easing.sine.easeIn
			}
		];

		let lSequenceGlowAlpha_arr = [
			{
				tweens: [],	duration: 4 * FRAME_RATE 
			},
			{
				tweens: [
							{ prop: "alpha", to: 0 }
						],	
				duration: 3 * FRAME_RATE,
				ease: Easing.sine.easeOut 
			},
			{
				tweens: [],	duration: 19 * FRAME_RATE 
			},
			{
				tweens: [
							{ prop: "alpha", to: 1 }
						],	
				duration: 3 * FRAME_RATE,
				ease: Easing.sine.easeIn
			},
		];

		Sequence.start(lContainerNumbet_sprt, lSequenceContainerScale_arr);
		Sequence.start(lNumber_sprt, lSequenceNumberAlpha_arr);
		Sequence.start(lNumberGlow_sprt, lSequenceGlowAlpha_arr);

		this.emit(BattlegroundFinalCountingView.EVENT_ON_BATTLEGROUND_FINAL_COUNTING_NEXT_COUNT_STARTED);
	}

	_animationCompleted(aCountingNumber_num)
	{
		this.emit(BattlegroundFinalCountingView.EVENT_ON_COUNT_FINISHED, {countingNumber: aCountingNumber_num});
	}

	_reset()
	{
		for (let i = 0; i < this._fContainerNumber_spr_arr.length; i++)
		{
			Sequence.destroy(Sequence.findByTarget(this._fContainerNumber_spr_arr[i]));
			this._fContainerNumber_spr_arr[i].scale.set(0, 0);
		}

		for (let i = 0; i < this._fNumber_spr_arr.length; i++)
		{
			Sequence.destroy(Sequence.findByTarget(this._fNumber_spr_arr[i]));
			this._fNumber_spr_arr[i].alpha = 0;
		}

		for (let i = 0; i < this._fNumberGlow_spr_arr.length; i++)
		{
			Sequence.destroy(Sequence.findByTarget(this._fNumberGlow_spr_arr[i]));
			this._fNumberGlow_spr_arr[i].alpha = 0;
		}
	}


	destroy()
	{
		super.destroy();

		this._reset();
		this._fNumber_spr_arr = null;
		this._fNumberGlow_spr_arr = null;
		this._fContainerNumber_spr_arr = null;

	}
}

export default BattlegroundFinalCountingView;