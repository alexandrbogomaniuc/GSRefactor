import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AlignDescriptor from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';
import NonWobblingTextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/NonWobblingTextField';
import GameplayInfo from '../../../model/gameplay/GameplayInfo';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
//import TextShadowDescriptor from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/text/shadow/TextShadowDescriptor';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../config/AtlasConfig';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';

class EjectableUnitView extends Sprite {
	static get EVENT_ON_EJECT_STARTED() { return "EVENT_ON_EJECT_STARTED"; }
	static get EVENT_ON_LANDED() { return "EVENT_ON_LANDED"; }

	constructor(isBattlegroundGame) {
		super();
		this._fBetId_str = null;

		this._fContentContainer_sprt = null;
		this._fBody_sprt = null;
		this._fEjectAnimation_mtl = null;
		this._fEjectNicknameAlphaAnimation_mtl = null;
		this._fConstantLoopAnimation_mt = null;
		this._fCrownAnimation_mt = null;
		this._fHidingAnimation_mt = null;
		this._fMultiplierTextField_nwtf = null;
		this._fNicknameTextField_tf = null;
		this._fPayoutTextField_nwtf = null;


		this._fTransparent = false;
		this._fStartHidingAnimTime = undefined;
		this._viewInited = false;
		this._isBattlegroundGame = isBattlegroundGame;
		//...HIDING BTG
		//...ANIMATIONS

		// this._fBody_sprt.scale.set(0);
	}


	initView() {
		//CONTENT CONTAINER...
		if(this._viewInited) return;
		this._viewInited = true;
		let l_sprt = new Sprite();
		let lContainer_sprt = this._fContentContainer_sprt = this.addChild(l_sprt);
		//...CONTENT CONTAINER

		//SHADOW...
		/*this._fShadow_sprt = lContainer_sprt.addChild(new Sprite);
		this._fShadow_sprt.textures = [EjectableUnitView.getUnitTextures()[1]];
		this._fShadow_sprt.anchor.set(0.47, 0.58);
		if (APP.isBattlegroundGame) {
			this._fShadow_sprt.scale.set(2);
		}*/
		//...SHADOW
		//BODY...
		this._fBody_sprt = lContainer_sprt.addChild(new Sprite);
		this._fBody_sprt.textures = [EjectableUnitView.getUnitTextures()[0]];
		//...BODY
		//CROWN...
		this._fCrown_sprt = lContainer_sprt.addChild(APP.library.getSprite("game/crown"));
		this._fCrown_sprt.position.set(0, -23);
		this._fCrown_sprt.scale.set(0.5);
		this._fCrown_sprt.visible = false;
		//...CROWN

		//TEXT FIELDS...
		//MULTIPLIER...
		let lMultiplierTextField_nwtf = this._fMultiplierTextField_nwtf = lContainer_sprt.addChild(new NonWobblingTextField());
		lMultiplierTextField_nwtf.fontName = "fnt_nm_myriad_pro_bold";
		lMultiplierTextField_nwtf.fontSize = 16;
		lMultiplierTextField_nwtf.fontColor = 0xffffff;
		lMultiplierTextField_nwtf.setAlign(AlignDescriptor.CENTER, AlignDescriptor.MIDDLE);
		lMultiplierTextField_nwtf.maxWidth = 70;
		lMultiplierTextField_nwtf.position.set(0, -23);
		lMultiplierTextField_nwtf.letterSpace = -2;
		// irelevant overkill being that bakcground is already black
		//lMultiplierTextField_nwtf.shadowDescriptor = new TextShadowDescriptor(0x000000, 1, 2, 60, 1);
		if (APP.isBattlegroundGame) {
			lMultiplierTextField_nwtf.visible = false;
		}
		//...MULTIPLIER

		//NICKNAME...
		let lNickname_ta = I18.generateNewCTranslatableAsset("TABattlegroundAstronautsSitinNickname");
		let lNicknameFormat_obj = Object.assign({}, lNickname_ta.textFormat, { shortLength: 90 });
		let lNicknameTextField_tf = this._fNicknameTextField_tf = lContainer_sprt.addChild(new TextField(lNicknameFormat_obj));
		lNicknameTextField_tf.position.set(0, 23);
		lNicknameTextField_tf.anchor.set(0.5, 0.5);
		lNicknameTextField_tf.text = lNickname_ta.text;
		if (!APP.isBattlegroundGame) {
			lNicknameTextField_tf.visible = false;
		}
		//...NICKNAME

		//PAYOUT...
		let lPayoutTextField_nwtf = this._fPayoutTextField_nwtf = lContainer_sprt.addChild(new NonWobblingTextField());
		lPayoutTextField_nwtf.fontName = "fnt_nm_myriad_pro_bold";
		lPayoutTextField_nwtf.fontSize = 15;
		lPayoutTextField_nwtf.fontColor = 0x13b70f;
		lPayoutTextField_nwtf.setAlign(AlignDescriptor.CENTER, AlignDescriptor.MIDDLE);
		lPayoutTextField_nwtf.maxWidth = 70;
		lPayoutTextField_nwtf.position.set(0, -38);
		lPayoutTextField_nwtf.letterSpace = -2;
		// irrelevant overkill being that background is already black
		//lPayoutTextField_nwtf.shadowDescriptor = new TextShadowDescriptor(0x000000, 1, 2, 60, 1);
		if (APP.isBattlegroundGame) {
			lPayoutTextField_nwtf.visible = false;
		}
		//...PAYOUT
		//...TEXT FIELDS

		//ANIMATIONS...
		//EJECT...
		let l_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fBody_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				[1.5, 12, MTimeLine.EASE_IN_OUT],
				[1, 12, MTimeLine.EASE_IN_OUT],
			]);

		l_mtl.addAnimation(
			this._fBody_sprt,
			MTimeLine.SET_Y,
			0,
			[
				[-50, 12, MTimeLine.EASE_IN_OUT],
				[0, 12, MTimeLine.EASE_IN_OUT],
			]);

		l_mtl.addAnimation(
			this._fMultiplierTextField_nwtf,
			MTimeLine.SET_ALPHA,
			0,
			[
				12,
				[1, 12],
			]);

		l_mtl.addAnimation(
			this._fPayoutTextField_nwtf,
			MTimeLine.SET_ALPHA,
			0,
			[
				12,
				[1, 12],
			]);
		// overkill being that background is already black 
		/*l_mtl.addAnimation(
			this._fShadow_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				12,
				[1, 12],
			]);
			*/

		l_mtl.callFunctionAtFrame(this._onEjectStarted, 1, this);
		l_mtl.callFunctionAtFrame(this._onLanded, 24, this);

		this._fEjectAnimation_mtl = l_mtl;

		this._fEjectAnimationTotalDuration_num = this._fEjectAnimation_mtl.getTotalDurationInMilliseconds();
		this._fLastAdjustEjectAnimTime_num = undefined;

		l_mtl = new MTimeLine();

		l_mtl.addAnimation(
			this._fNicknameTextField_tf,
			MTimeLine.SET_ALPHA,
			0,
			[
				12,
				[1, 12],
			]);

		this._fEjectNicknameAlphaAnimation_mtl = l_mtl;
		//...EJECT

		//IDLE LOOP...
		l_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fBody_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			-15,
			[
				[15, 40],
				[-15, 40],
			]);

		this._fConstantLoopAnimation_mt = l_mtl;
		//...IDLE LOOP

		//CROWN...
		l_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fCrown_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				12,
				[1, 12],
			]);

		l_mtl.addAnimation(
			this._fCrown_sprt,
			MTimeLine.SET_SCALE_Y,
			0.5,
			[
				24,
				[1, 6],
				[0.75, 6],
			]);

		l_mtl.addAnimation(
			this._fCrown_sprt,
			MTimeLine.SET_SCALE_X,
			0.5,
			[
				24,
				[1, 6],
				[0.75, 6],
			]);
		this._fCrownAnimation_mt = l_mtl;
		//...CROWN

		//HIDING BTG...
		l_mtl = new MTimeLine();

		if (APP.isBattlegroundGame) {
			l_mtl.addAnimation(
				this._fBody_sprt,
				MTimeLine.SET_ALPHA,
				1,
				[
					[0.4, 12],
				]);


			l_mtl.addAnimation(
				this._fPayoutTextField_nwtf,
				MTimeLine.SET_ALPHA,
				1,
				[
					[0.4, 12],
				]);

			l_mtl.addAnimation(
				this._fMultiplierTextField_nwtf,
				MTimeLine.SET_ALPHA,
				1,
				[
					[0.4, 12],
				]);


			l_mtl.addAnimation(
				this._fNicknameTextField_tf,
				MTimeLine.SET_ALPHA,
				1,
				[
					[0.4, 12],
				]);
		} else {
			l_mtl.addAnimation(
				this._fBody_sprt,
				MTimeLine.SET_ALPHA,
				1,
				[
					[0, 75],
				]);


			l_mtl.addAnimation(
				this._fPayoutTextField_nwtf,
				MTimeLine.SET_ALPHA,
				1,
				[
					[0, 75],
				]);

			l_mtl.addAnimation(
				this._fMultiplierTextField_nwtf,
				MTimeLine.SET_ALPHA,
				1,
				[
					[0, 75],
				]);


			l_mtl.addAnimation(
				this._fNicknameTextField_tf,
				MTimeLine.SET_ALPHA,
				1,
				[
					[0, 75],
				]);

		}

		this._fHidingAnimation_mt = l_mtl;
	}

	set transparent(aTransparent) {
		if (this._fTransparent === aTransparent) {
			return;
		}
		this._fTransparent = aTransparent;
		if (aTransparent === true) {
			this._fHidingAnimation_mt.windToMillisecond(0);
		}
		else {
			this._fStartHidingAnimTime = undefined;
		}
	}

	get transparent() {
		return this._fTransparent;
	}

	set hideTime(aTime) {
		this._fStartHidingAnimTime = aTime;
	}

	get hideTime() {
		return this._fStartHidingAnimTime;
	}

	drop() {

		this.visible = false;
		this._fLastAdjustEjectAnimTime_num = undefined;
		if (this._viewInited && this._fBetId_str != null) {
			this._fHidingAnimation_mt.windToMillisecond(0);
			this._fEjectAnimation_mtl.windToMillisecond(0);
			this._fEjectNicknameAlphaAnimation_mtl.windLoopToMillisecond(0);
			this._fConstantLoopAnimation_mt.windToMillisecond(0);
			this.position.set(-50, -50);
			this._fCrown_sprt.visible = false;
		}
		this._fBetId_str = null;

	}

	adjust(aBetInfo_bi) {
		if (!this._viewInited) return;
		this.visible = true;

		let l_gpi = APP.gameController.gameplayController.info;
		let lCurMultiplierRoundDuration_num = l_gpi.multiplierRoundDuration;
		let lEjectedBetDuration_num = aBetInfo_bi.ejectTime - l_gpi.multiplierChangeFlightStartTime;

		let lCurEjectAnimationTime_num = lCurMultiplierRoundDuration_num - lEjectedBetDuration_num;
		let lRoundInfo_ri = l_gpi.roundInfo;

		if (lRoundInfo_ri.isRoundEndTimeDefined) {
			lCurEjectAnimationTime_num += l_gpi.outOfRoundDuration;
		}

		this._fLastAdjustEjectAnimTime_num = lCurEjectAnimationTime_num
		this._fEjectAnimation_mtl.windToMillisecond(lCurEjectAnimationTime_num);
		this._fCrownAnimation_mt.windToMillisecond(lCurEjectAnimationTime_num);

		if (this._fStartHidingAnimTime) {
			let lCurHidingAnimationTime = lCurMultiplierRoundDuration_num - this._fStartHidingAnimTime + l_gpi.multiplierChangeFlightStartTime;
			this._fHidingAnimation_mt.windToMillisecond(lCurHidingAnimationTime);
		}
		else {
			this._fHidingAnimation_mt.windToMillisecond(0);
			this._fEjectNicknameAlphaAnimation_mtl.windToMillisecond(lCurEjectAnimationTime_num);
		}

		this._fConstantLoopAnimation_mt.windLoopToMillisecond(
			lCurMultiplierRoundDuration_num,
			lEjectedBetDuration_num);

		if (this._fBetId_str == aBetInfo_bi.betId) {
			return;
		}
		this._fBetId_str = aBetInfo_bi.betId;

		let lEjectMult_num = aBetInfo_bi.multiplier;
		let formatedMultiplier = GameplayInfo.formatMultiplier(lEjectMult_num).replace("x", "");
		let formatedCurrency = APP.currencyInfo.i_formatNumber(formatedMultiplier*100,false,false,2);
		this._fMultiplierTextField_nwtf.text = formatedCurrency + "x";
		let lPayout_num = aBetInfo_bi.isBetWinDefined ? aBetInfo_bi.betWin : aBetInfo_bi.betAmount * lEjectMult_num;

		// [OWL] TODO: apply changes for alll systems without any conditions
		if (APP.appParamsInfo.restrictCoinFractionLength !== undefined) {
			if (!aBetInfo_bi.isBetWinDefined) {
				lPayout_num = +lPayout_num.toFixed(0);
			}
			this._fPayoutTextField_nwtf.text = APP.currencyInfo.i_formatIncomeWithPlusSign(lPayout_num, true, APP.isBattlegroundGame, 2, undefined, false, true, true, 0, false);
		}
		else {
			if (!aBetInfo_bi.isBetWinDefined) {
				lPayout_num = Math.floor(lPayout_num);
			}

			this._fPayoutTextField_nwtf.text = ("+" + APP.currencyInfo.i_formatNumber(lPayout_num, true, APP.isBattlegroundGame, 2, undefined, false));
		}

		this._fNicknameTextField_tf.text = aBetInfo_bi.playerName;

		this.zIndex = (APP.isBattlegroundGame && aBetInfo_bi.isMasterBet) ? 1 : 0;
		if (APP.isBattlegroundGame) {
			let _unitScale = aBetInfo_bi.isMasterBet ? 1.2 : 1;
			this._fContentContainer_sprt.scale.set(_unitScale, _unitScale);
		}

	}

	setKingOfTheHill(aValue_bl) {
		if (!this._viewInited) return;
		this._fCrown_sprt.visible = aValue_bl;
	}

	_onEjectStarted() {
		if (!this._viewInited) return;
		if (this.visible && this._fLastAdjustEjectAnimTime_num <= this._fEjectAnimationTotalDuration_num / 2) {
			this.emit(EjectableUnitView.EVENT_ON_EJECT_STARTED);
		}
	}

	_onLanded() {
		if (!this._viewInited) return;
		if (this.visible && this._fLastAdjustEjectAnimTime_num <= this._fEjectAnimationTotalDuration_num) {
			this.emit(EjectableUnitView.EVENT_ON_LANDED);
		}
	}

	destroy() {
		this._fBetId_str = null;

		this._fContentContainer_sprt = null;
		this._fBody_sprt = null;
		this._fEjectAnimation_mtl = null;
		this._fEjectNicknameAlphaAnimation_mtl = null;
		this._fConstantLoopAnimation_mt = null;
		this._fCrownAnimation_mt = null;
		this._fHidingAnimation_mt = null;
		this._fMultiplierTextField_nwtf = null;
		this._fNicknameTextField_tf = null;
		this._fPayoutTextField_nwtf = null;

		//CONTENT CONTAINER...
		let l_sprt = null
		let lContainer_sprt = null;
		//...CONTENT CONTAINER

		//SHADOW...
		this._fShadow_sprt = null;

		//...SHADOW
		//BODY...
		this._fBody_sprt = null;

		//CROWN...
		this._fCrown_sprt = null;

		//...CROWN

		//TEXT FIELDS...
		//MULTIPLIER...
		this._fMultiplierTextField_nwtf = null;


		//NICKNAME...
		this._fNicknameTextField_tf = null;


		//PAYOUT...
		this._fPayoutTextField_nwtf = null;

	}


}
export default EjectableUnitView;

EjectableUnitView.getUnitTextures = function () {
	if (!EjectableUnitView.unit_textures) {
		EjectableUnitView.unit_textures = [];

		EjectableUnitView.unit_textures = AtlasSprite.getFrames([APP.library.getAsset('game/gameplay_assets')], [AtlasConfig.GameplayAssets], 'ejectable_unit');
		EjectableUnitView.unit_textures.sort(function (a, b) { if (a._atlasName > b._atlasName) return 1; else return -1 });
	}

	return EjectableUnitView.unit_textures;
}