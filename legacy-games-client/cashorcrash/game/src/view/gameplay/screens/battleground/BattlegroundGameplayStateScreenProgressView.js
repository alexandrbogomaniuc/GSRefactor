import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MTimeLine from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import BattlegroundGameplayStateScreenProgressPayoutsView from './BattlegroundGameplayStateScreenProgressPayoutsView';
import GameplayStateScreenProgressView from '../GameplayStateScreenProgressView';

class BattlegroundGameplayStateScreenProgressView extends Sprite
{
	static get EVENT_ON_EJECT_INITIATED()		{ return GameplayStateScreenProgressView.EVENT_ON_EJECT_INITIATED };
	static get EVENT_ON_EJECT_ALL_INITIATED()	{ return GameplayStateScreenProgressView.EVENT_ON_EJECT_ALL_INITIATED };
	
	constructor()
	{
		super();

		this._fPayoutsView_gssppsv = null;

		//CONTENT...
		this._addContent();
		//...CONTENT

		//INTRO ANIMATION...
		let l_mt = new MTimeLine();
		l_mt.addAnimation(
			this,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 25]
			]);

		l_mt.addAnimation(
			APP.gameController.gameplayController.view.ejectButtonsContainer,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 25]
			]);

		this._fIntroAnimation_mtl = l_mt;
		//...INTRO ANIMATION
	}

	updateArea()
	{
		this._fPayoutsView_gssppsv.updateArea();
	}

	adjust(aStartTime_num)
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_gpv = APP.gameController.gameplayController.view;
		let lCurGameplayTime_num = l_gpi.gameplayTime;

		let lMultiplier_num = l_gpi.serverMultiplierValue;	
		
		let lIntro_mtl = this._fIntroAnimation_mtl;
		lIntro_mtl.windToMillisecond(lCurGameplayTime_num-aStartTime_num);

		let lMasterPlayer_gpi = l_gpi.gamePlayersInfo.masterPlayerInfo;
		let lMasterBets_bi_arr = !!lMasterPlayer_gpi ? lMasterPlayer_gpi.activeBets : null;
		
		let lPayoutsView_gssppsv = this._fPayoutsView_gssppsv;
		if (lMasterBets_bi_arr && !!lMasterBets_bi_arr.length)
		{
			lPayoutsView_gssppsv.adjust(lMasterBets_bi_arr);
			lPayoutsView_gssppsv.visible = true;
			l_gpv.ejectButtonsContainer.visible = true;
		}
		else
		{
			lPayoutsView_gssppsv.adjust(null);
			lPayoutsView_gssppsv.visible = false;
			l_gpv.ejectButtonsContainer.visible = false;
		}
	}

	_addContent()
	{
		let lPayoutsView_gssppsv = this._fPayoutsView_gssppsv = new BattlegroundGameplayStateScreenProgressPayoutsView;
		lPayoutsView_gssppsv.on(BattlegroundGameplayStateScreenProgressPayoutsView.EVENT_ON_EJECT_INITIATED, this.emit, this);
		lPayoutsView_gssppsv.on(BattlegroundGameplayStateScreenProgressPayoutsView.EVENT_ON_EJECT_ALL_INITIATED, this.emit, this);
		this.addChild(lPayoutsView_gssppsv);
	}
}

export default BattlegroundGameplayStateScreenProgressView;