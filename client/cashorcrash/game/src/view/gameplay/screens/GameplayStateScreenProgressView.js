import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayInfo from '../../../model/gameplay/GameplayInfo';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import AlignDescriptor from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';
import NonWobblingTextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/NonWobblingTextField';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GameplayStateScreenProgressPayoutsView from './GameplayStateScreenProgressPayoutsView';
import MultiplierRulerView from '../graph/MultiplierRulerView';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

const PRE_LAUNCH_MODE_SCALE = 0.8;
class GameplayStateScreenProgressView extends Sprite
{
	static get EVENT_ON_EJECT_INITIATED()		{ return GameplayStateScreenProgressPayoutsView.EVENT_ON_EJECT_INITIATED };
	static get EVENT_ON_EJECT_ALL_INITIATED()	{ return GameplayStateScreenProgressPayoutsView.EVENT_ON_EJECT_ALL_INITIATED };
	
	constructor()
	{
		super();

		this._fMult_tf = null;
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

		let l_gpi = APP.gameController.gameplayController.info;
		if (l_gpi.isPreLaunchFlightRequired)
		{
			l_mt.addAnimation(
			this._fMult_tf,
			MTimeLine.SET_SCALE,
			0,
			[
				[0, 25],
				[PRE_LAUNCH_MODE_SCALE, 15]
			]);
		}

		this._fIntroAnimation_mtl = l_mt;
		//...INTRO ANIMATION
	}

	updateArea()
	{
		this._fPayoutsView_gssppsv.updateArea();
		this._fMult_tf.maxWidth = this._fullAreaWidth;
	}

	get _fullAreaWidth()
	{
		return GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width - MultiplierRulerView.RULER_VISUAL_WIDTH;
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
		if (lMasterBets_bi_arr)
		{
			lMasterBets_bi_arr = lMasterBets_bi_arr.filter(betInfo => betInfo.isConfirmedMasterBet);
		}

		let lPayoutsView_gssppsv = this._fPayoutsView_gssppsv;
		if (lMasterBets_bi_arr && !!lMasterBets_bi_arr.length)
		{
			lPayoutsView_gssppsv.adjust();
			lPayoutsView_gssppsv.visible = true;
			l_gpv.ejectButtonsContainer.visible = true;
		}
		else
		{
			// DEBUG...
			// lPayoutsView_gssppsv.adjust();
			// lPayoutsView_gssppsv.visible = true;
			// l_gpv.ejectButtonsContainer.visible = true;
			// ...DEBUG

			lPayoutsView_gssppsv.visible = false;
			l_gpv.ejectButtonsContainer.visible = false;
		}

		let l_tf = this._fMult_tf;
		l_tf.text = GameplayInfo.formatMultiplier(lMultiplier_num);

		if (!l_gpi.isPreLaunchFlightRequired)
		{
			l_tf.position.y = lPayoutsView_gssppsv.visible ? -60 : 0;
		}
	}

	_addContent()
	{
		let lContainer_sprt = this.addChild(new Sprite);
		let l_tf;

		l_tf = this._fMult_tf = lContainer_sprt.addChild(new NonWobblingTextField);
		l_tf.fontName = "fnt_nm_barlow_bold";
		l_tf.fontSize = 100;
		l_tf.fontColor = 0xffffff;
		l_tf.setAlign(AlignDescriptor.CENTER, AlignDescriptor.MIDDLE);
		l_tf.letterSpace = -5;
		l_tf.maxWidth = this._fullAreaWidth;
		
		let lPayoutsView_gssppsv = this._fPayoutsView_gssppsv = new GameplayStateScreenProgressPayoutsView;
		lPayoutsView_gssppsv.on(GameplayStateScreenProgressPayoutsView.EVENT_ON_EJECT_INITIATED, this.emit, this);
		lPayoutsView_gssppsv.on(GameplayStateScreenProgressPayoutsView.EVENT_ON_EJECT_ALL_INITIATED, this.emit, this);
		lContainer_sprt.addChild(lPayoutsView_gssppsv);

		let l_gpi = APP.gameController.gameplayController.info;
		if (l_gpi.isPreLaunchFlightRequired)
		{
			l_tf.position.y = -41;
			lContainer_sprt.position.y = 95;
		}
		else
		{
			lContainer_sprt.position.y = 0;
		}
	}
}

export default GameplayStateScreenProgressView;