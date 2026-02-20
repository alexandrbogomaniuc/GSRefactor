import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import TreasureAwardingAnimation from '../../../main/animation/treasures/TreasureAwardingAnimation';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class TreasuresView extends SimpleUIView
{
	static get TREASURE_AWARD_ANIMATION_COMPLETED()			{ return "TREASURE_AWARD_ANIMATION_COMPLETED"; }
	static get EVENT_ON_TREASURE_GEM_DROP()					{ return TreasureAwardingAnimation.EVENT_ON_TREASURE_GEM_DROP; }

	addToContainerIfRequired(aTreasuresContainerInfo_obj)
	{
		this._addToContainerIfRequired(aTreasuresContainerInfo_obj);
	}

	showTreasureById(aGemId_num, lStartPosition_obj, aFinalPosition_obj)
	{
		this._showTreasureById(aGemId_num, lStartPosition_obj, aFinalPosition_obj)
	}

	clearAllTreasures()
	{
		this._clearAllTreasures();
	}

	constructor()
	{
		super();

		this._fTreasureAwardingAnimations_taa_arr = [];
	}

	_addToContainerIfRequired(aTreasuresContainerInfo_obj)
	{
		if (this.parent)
		{
			return;
		}

		aTreasuresContainerInfo_obj.container.addChild(this);
		this.zIndex = aTreasuresContainerInfo_obj.zIndex;
	}

	_showTreasureById(aGemId_num, lStartPosition_obj, aFinalPosition_obj)
	{
		const lTreasureAwardingAnimation_taa = this.addChild(new TreasureAwardingAnimation(aGemId_num));
		lTreasureAwardingAnimation_taa.once(TreasureAwardingAnimation.EVENT_ON_TREASURE_GEM_DROP, this.emit, this);
		lTreasureAwardingAnimation_taa.startAnimation();
		lTreasureAwardingAnimation_taa.position = lStartPosition_obj;

		const lTreasurePositionSeq_arr = [
			{ tweens: [], duration: 46 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: lTreasureAwardingAnimation_taa.position.x - 3.5 }, { prop: 'position.y', to: lTreasureAwardingAnimation_taa.position.y - 2.5 }], duration: 4 * FRAME_RATE },
			{ tweens: [{ prop: 'position.x', to: aFinalPosition_obj.x }, { prop: 'position.y', to: aFinalPosition_obj.y }], duration: 10 * FRAME_RATE, ease: Easing.exponential.easeIn },
		];
		const l_seq = Sequence.start(lTreasureAwardingAnimation_taa, lTreasurePositionSeq_arr);
		l_seq.once(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, this._onTreasureAwardAnimationCompleted, this);

		this._fTreasureAwardingAnimations_taa_arr.push(lTreasureAwardingAnimation_taa);
	}

	_onTreasureAwardAnimationCompleted(aEvent_obj)
	{
		const lTreasureAwardingAnimation_taa = aEvent_obj.target.obj;
		this.emit(TreasuresView.TREASURE_AWARD_ANIMATION_COMPLETED, { id: lTreasureAwardingAnimation_taa.id });
		const lIndex_int = this._fTreasureAwardingAnimations_taa_arr.indexOf(lTreasureAwardingAnimation_taa);
		if (~lIndex_int)
		{
			this._fTreasureAwardingAnimations_taa_arr.splice(lIndex_int, 1);
		}
		APP.soundsController.play("quests_treasure_collect");
		lTreasureAwardingAnimation_taa.destroy();
	}

	_clearAllTreasures()
	{
		this._fTreasureAwardingAnimations_taa_arr && this._fTreasureAwardingAnimations_taa_arr.forEach(a_taa =>
		{
			Sequence.destroy(Sequence.findByTarget(a_taa));
			a_taa.destroy();
		});
	}

	destroy()
	{
		this._clearAllTreasures();

		super.destroy();

		this._fTreasureAwardingAnimations_taa_arr = null;
	}
}

export default TreasuresView;