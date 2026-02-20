import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Counter from '../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/Counter';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AtlasConfig from '../../../config/AtlasConfig';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import PathTween from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/PathTween';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import * as FEATURES from '../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';

import GameStateController from '../../../controller/state/GameStateController'
import ProfileAvatar from '../../../ui/profile/ProfileAvatar';
import RoundResultButton from './RoundResultButton';
import RoundResultTextIndicatorView from './indicators/RoundResultTextIndicatorView';
import RoundResultPayoutsIndicatorView from './indicators/RoundResultPayoutsIndicatorView';
import GameBonusController from '../../../controller/uis/custom/bonus/GameBonusController';
import PreloadingSpinner from '../../../../../../common/PIXI/src/dgphoenix/gunified/view/custom/PreloadingSpinner';

const COUNTING_DURATION_PARTIAL = 18 * FRAME_RATE;
const COINS_TIME_RANGE = 3 * FRAME_RATE;
const END_POSITION_COIN_RANGE = { x: 30, y: 30 };

class RoundResultScreenView extends SimpleUIView
{
	static get EVENT_ON_NEXT_ROUND_CLICKED()							{ return "onNextRoundClicked"; }
	static get EVENT_ON_BACK_TO_LOBBY_ROUND_RESULT_BUTTON_CLICKED()		{ return "onBackToLobbyRoundResultBtnClicked"; }

	static get ON_COINS_ANIMATION_STARTED()								{ return "onCoinsAnimationStarted"; }
	static get ON_COINS_ANIMATION_COMPLETED()							{ return "onCoinsAnimationCompleted"; }

	show(aSkipAnimation_bl = false)
	{
		this._show(aSkipAnimation_bl);
	}

	refreshPlayerStatsValues(playerStats)
	{
	}

	stopAllAnimation()
	{
		this._stopAllAnimation();
	}

	validateCaptions()
	{
		this._validateCaptions();
	}

	showWaitingCaption()
	{
		this._showWaitingCaption();
	}

	hideWaitingCaption()
	{
		this._hideWaitingCaption();
	}

	constructor()
	{
		super();

		this._fPlayerNickname_tf = null;

		this._fTotalDamageValue_rrtiv = null;
		this._fTotalDamageCounter_c = null;
		this._fTotalKillsCountValue_rrtiv = null;
		this._fTotalFreeShotsCountValue_rrtiv = null;
		this._fBulletsFiredCountValue_rrtiv = null;
		this._fBulletsFiredCounter_c = null;
		this._fQuestPayoutsIndicatorView_rrbpiv = null;
		this._fQuestPayoutsCounter_c = null;

		this._fWeaponsPayoutsIndicatorView_rrbpiv = null;
		this._fWeaponsPayoutsCounter_c = null;

		this._fAvatarView_pa = null;
		this._fQuestsCompletedCaption_cta = null;
		this._fQuestPayoutsCaption_cta = null;
		this._fWeaponsPayoutsCaption_cta = null;

		this._nextRoundBtn = null;
		this._exitRoomBtn = null;
		this._coinTextures = null;
		this._fSequences_arr = []
		this._fCoinsAnimationDelay_t = null;
		this._fGraphicLines_arr = [];

		this._showWaitingCaption_bl = null;

		if(APP.isMobile) 
		{
			this.scale.set(1.3);
			this.position.set(0, 12);
		}
	}

	__init()
	{
		super.__init();

		this._addTransparentBack();

		if (this.uiInfo.isActiveScreenMode)
		{
			this._addBack();
			this._addGraphics();
			this._addCaptions();
			this._addNickname();
			this._fAvatarCountainer_spr = this.addChild(new Sprite());
			this._addPlayersList();
			this._addValues();

			APP.currentWindow.gameStateController.on(GameStateController.EVENT_ON_PLAYER_SEAT_STATE_CHANGED, this._onPlayerSeatStateChanged, this);
			APP.currentWindow.gameBonusController.on(GameBonusController.EVENT_ON_BONUS_STATE_CHANGED, this._onBonusStatusChanged, this);
		}
	}

	_addTransparentBack()
	{
		let lTransparentBack_grphc = this.addChild(new PIXI.Graphics());

		lTransparentBack_grphc.beginFill(0x000000, this.uiInfo.isActiveScreenMode ? 0.45 : 0.01).drawRect(-480, -284, 960, 560).endFill();
		lTransparentBack_grphc.interactive = true;
		lTransparentBack_grphc.buttonMode = false;
	}

	_addBack()
	{
		this._addBackLines();
	}

	_addBackLines()
	{

		this._addGraphicLine(-100.5, 51.5);
		this._addGraphicLine(-210.5, 51.5);


		if (this._compensationNeeded)
		{
			this._addGraphicLine(-100.5, 121.5);
			this._addGraphicLine(-210.5, 121.5);
		}
		else
		{
			this._addGraphicLine(-155.5, 121.5);
		}
	}

	_removeBackLines()
	{
		if (this._fGraphicLines_arr)
		{
			for (let line of this._fGraphicLines_arr)
			{
				line.destroy();
			}
		}
		this._fGraphicLines_arr = [];
	}

	_addGraphicLine(aX, aY)
	{
		let lLine_grphc = this.addChild(new PIXI.Graphics());
		lLine_grphc.beginFill(0xffffff).drawRect(-1, -20, 1, 40).endFill();
		lLine_grphc.position.set(aX, aY);
		lLine_grphc.alpha = 0.2;
		this._fGraphicLines_arr.push(lLine_grphc);
	}

	_addCaptions()
	{
		let lTitleCaption_cta = this.addChild(I18.generateNewCTranslatableAsset("TARoundResultsTitle"));
		lTitleCaption_cta.position.set(-264, -171.5);

		let lTotalDamageCaption_cta = this.addChild(I18.generateNewCTranslatableAsset("TARoundResultTotalDamageCaption"));
		lTotalDamageCaption_cta.position.set(-88, -17);

		let lTotalFreeShotsCaption_cta = this.addChild(I18.generateNewCTranslatableAsset("TARoundResultTotalFreeShotsCaption"));
		lTotalFreeShotsCaption_cta.position.set(-264, 34.5);

		let lTotalKillsCaption_cta = this.addChild(I18.generateNewCTranslatableAsset("TARoundResultTotalKillsCaption"));
		lTotalKillsCaption_cta.position.set(-154.5, 34.5);

		let lBulletsFiredCaption_cta = this.addChild(I18.generateNewCTranslatableAsset("TARoundResultBulletsFiredCaption"));
		lBulletsFiredCaption_cta.position.set(-49, 34.5);

		this._fQuestsCompletedCaption_cta = this.addChild(I18.generateNewCTranslatableAsset("TARoundResultQuestsCompletedCaption"));
		this._fQuestsCompletedCaption_cta.position.set(-264, 100);

		this._fQuestPayoutsCaption_cta = this.addChild(I18.generateNewCTranslatableAsset("TARoundResultQuestPayoutsCaption"));
		this._fQuestPayoutsCaption_cta.position.set(-154, 100);

		if (this._compensationNeeded)
		{
			this._fWeaponsPayoutsCaption_cta = this.addChild(I18.generateNewCTranslatableAsset("TARoundResultUnusedBonusCaption"));
			this._fWeaponsPayoutsCaption_cta.position.set(-49, 100);
		}
	}

	get _weaponsBonusCaption()
	{
		return (this._isFrbMode || this._isCashBonusMode) ? "TARoundResultSWBonusCaption" : "TARoundResultUnusedBonusCaption";
	}

	_addNickname()
	{
		this._fPlayerNickname_tf = this.addChild(new TextField(this._nicknameStyle));
		this._fPlayerNickname_tf.maxWidth = 130;
		this._fPlayerNickname_tf.anchor.set(0.5, 0.5);
		this._fPlayerNickname_tf.position.set(-231.5, 3);
		this._updateNicknameText("-");
	}

	_updateNicknameText(value)
	{
		let fontName = "sans-serif";
		if (APP.fonts.isGlyphsSupported(this._nicknameStyle.fontFamily, value))
		{
			fontName = this._nicknameStyle.fontFamily;
		}
		
		let txtStyle = this._fPlayerNickname_tf.getStyle() || {};
		txtStyle.fontFamily = fontName;
		this._fPlayerNickname_tf.textFormat = txtStyle;
		
		this._fPlayerNickname_tf.text = value;
	}

	_addGraphics()
	{
		this._playerAvatar;
	}

	get _playerAvatar()
	{
		return this._fAvatarView_pa || (this._fAvatarView_pa = this._initAvatar());
	}

	_initAvatar()
	{
		let lAvatarData_obj = this.uiInfo && this.uiInfo.playerAvatarData;

		if (lAvatarData_obj)
		{
			this._fAvatarView_pa = this._fAvatarCountainer_spr.addChild(new ProfileAvatar(null, lAvatarData_obj));
			this._fAvatarView_pa.position.set(-230.5, -66.5);
		}

		return this._fAvatarView_pa;
	}

	_updateAvatar()
	{
		let lAvatarData_obj = this.uiInfo.playerAvatarData;

		if (this._playerAvatar && lAvatarData_obj)
		{
			this._playerAvatar.update(lAvatarData_obj);
		}
	}

	_onNextRoundButtonClicked()
	{
		this.emit(RoundResultScreenView.EVENT_ON_NEXT_ROUND_CLICKED);
	}

	_onExitRoomButtonClicked()
	{
		this.emit(RoundResultScreenView.EVENT_ON_BACK_TO_LOBBY_ROUND_RESULT_BUTTON_CLICKED);
	}

	_addValues()
	{
		this._fTotalDamageValue_rrtiv = this.addChild(new RoundResultTextIndicatorView(new TextField(this._partialValuesStyle), false, 0.5, 0.5));
		this._fTotalDamageValue_rrtiv.position.set(-91, 3);
		this._fTotalDamageCounter_c = new Counter({ target: this._fTotalDamageValue_rrtiv, method: "value" });

		this._fTotalFreeShotsCountValue_rrtiv = this.addChild(new RoundResultTextIndicatorView(new TextField(this._partialValuesStyle), false, 0.5, 0.5));
		this._fTotalFreeShotsCountValue_rrtiv.maxWidth = 40;
		this._fTotalFreeShotsCountValue_rrtiv.position.set(-263, 50);
		this._fTotalFreeShotsCounter_c = new Counter({ target: this._fTotalFreeShotsCountValue_rrtiv, method: "value" });

		this._fTotalKillsCountValue_rrtiv = this.addChild(new RoundResultTextIndicatorView(new TextField(this._partialValuesStyle), false, 0.5, 0.5));
		this._fTotalKillsCountValue_rrtiv.maxWidth = 40;
		this._fTotalKillsCountValue_rrtiv.position.set(-154, 50);
		this._fTotalKillsCounter_c = new Counter({ target: this._fTotalKillsCountValue_rrtiv, method: "value" });

		this._fBulletsFiredCountValue_rrtiv = this.addChild(new RoundResultTextIndicatorView(new TextField(this._partialValuesStyle), false, 0.5, 0.5));
		this._fBulletsFiredCountValue_rrtiv.maxWidth = 40;
		this._fBulletsFiredCountValue_rrtiv.position.set(-49, 50);
		this._fBulletsFiredCounter_c = new Counter({ target: this._fBulletsFiredCountValue_rrtiv, method: "value" });

		this._fQuestsCompletedCountValue_rrtiv = this.addChild(new RoundResultTextIndicatorView(new TextField(this._partialValuesStyle), false, 0.5, 0.5));
		this._fQuestsCompletedCountValue_rrtiv.maxWidth = 40;
		this._fQuestsCompletedCountValue_rrtiv.position.set(-263, 121.5);
		this._fQuestsCompletedCountCounter_c = new Counter({ target: this._fQuestsCompletedCountValue_rrtiv, method: "value" });

		this._fQuestPayoutsCounter_c = new Counter({ target: this.questsPayoutsIndicatorView, method: "indicatorValue" });

		if (this._compensationNeeded)
		{
			this._fWeaponsPayoutsCounter_c = new Counter({ target: this.weaponsPayoutsIndicatorView, method: "indicatorValue" });
		}
	}

	get questsPayoutsIndicatorView()
	{
		return this._fQuestPayoutsIndicatorView_rrbpiv || (this._fQuestPayoutsIndicatorView_rrbpiv = this._initQuestPayoutsIndicatorView());
	}

	_initQuestPayoutsIndicatorView()
	{
		let l_rrqpiv = this.addChild(new RoundResultPayoutsIndicatorView()/* RoundResultQuestPayoutsIndicatorView() */);
		l_rrqpiv.position.set(-150, 121.5);
		return l_rrqpiv;
	}

	get weaponsPayoutsIndicatorView()
	{
		return this._fWeaponsPayoutsIndicatorView_rrbpiv || (this._fWeaponsPayoutsIndicatorView_rrbpiv = this._initWeaponsPayoutsIndicatorView());
	}

	_initWeaponsPayoutsIndicatorView()
	{
		let l_rrqpiv = this.addChild(new RoundResultPayoutsIndicatorView());
		l_rrqpiv.position.set(-47, 121.5);
		return l_rrqpiv;
	}

	_updateValues(aSkipAnimation_bl = false)
	{
		let l_rrsi = this.uiInfo;
		if (l_rrsi && l_rrsi.isActiveScreenMode)
		{
			let nickName = l_rrsi.playerNickname !== undefined ? l_rrsi.playerNickname : "-";
			this._updateNicknameText(nickName);

			!APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater && (aSkipAnimation_bl = true)

			if (aSkipAnimation_bl)
			{
				this._fTotalDamageCounter_c && this._fTotalDamageCounter_c.stopCounting();
				this._fTotalDamageValue_rrtiv.value = l_rrsi.totalDamage;

				this._fTotalKillsCounter_c && this._fTotalKillsCounter_c.stopCounting();
				this._fTotalKillsCountValue_rrtiv.value = l_rrsi.totalKillsCount;

				this._fTotalFreeShotsCounter_c && this._fTotalFreeShotsCounter_c.stopCounting();
				this._fTotalFreeShotsCountValue_rrtiv.value = l_rrsi.totalFreeShotsCount;

				this._fBulletsFiredCounter_c && this._fBulletsFiredCounter_c.stopCounting();
				this._fBulletsFiredCountValue_rrtiv.value = l_rrsi.bulletsFiredCount;

				this._fQuestsCompletedCountCounter_c && this._fQuestsCompletedCountCounter_c.stopCounting();
				this._fQuestsCompletedCountValue_rrtiv.value = l_rrsi.questsCompletedCount;

				this._fQuestPayoutsCounter_c && this._fQuestPayoutsCounter_c.stopCounting();
				this.questsPayoutsIndicatorView.indicatorValue = l_rrsi.questsPayouts;

				if (this._compensationNeeded)
				{
					this._fWeaponsPayoutsCounter_c && this._fWeaponsPayoutsCounter_c.stopCounting();
					this.weaponsPayoutsIndicatorView.indicatorValue = l_rrsi.weaponsSurplus;
				}

				this._resetCoinDelay(true);
			}
			else
			{
				this._fTotalDamageValue_rrtiv.value = 0;

				this._fTotalKillsCountValue_rrtiv.value = 0;
				this._fTotalFreeShotsCountValue_rrtiv.value = 0;
				this._fBulletsFiredCountValue_rrtiv.value = 0;
				this._fQuestsCompletedCountValue_rrtiv.value = 0;
				this.questsPayoutsIndicatorView.indicatorValue = 0;

				if (this._compensationNeeded)
				{
					this.weaponsPayoutsIndicatorView.indicatorValue = 0;
				}

				this._startCountingBlock1();
			}
		}
	}

	_onePulseBeatAnimation(container)
	{
		let sequence = [
			{
				tweens: [
					{ prop: "scale.x", to: 1 },
					{ prop: "scale.y", to: 1 },
				],
				duration: 0.3 * COUNTING_DURATION_PARTIAL,
				ease: Easing.back.easeIn
			},
			{
				tweens: [
					{ prop: "scale.x", to: 1.4 },
					{ prop: "scale.y", to: 1.4 },
				],
				duration: 0.2 * COUNTING_DURATION_PARTIAL,
				ease: Easing.back.easeIn
			},
			{
				tweens: [
					{ prop: "scale.x", to: 0.8 },
					{ prop: "scale.y", to: 0.8 },
				],
				duration: 0.2 * COUNTING_DURATION_PARTIAL,
				ease: Easing.back.easeIn
			},
			{
				tweens: [
					{ prop: "scale.x", to: 1 },
					{ prop: "scale.y", to: 1 },
				],
				duration: 0.3 * COUNTING_DURATION_PARTIAL,
				ease: Easing.back.easeOut
			}
		];
		Sequence.start(container, sequence);
	}

	_startCoinsAnimation(aValueCounter_rrtiv)
	{
		let coin = this.addChild(new Sprite());
		coin.textures = this.coinTextures;
		coin.blendMode = PIXI.BLEND_MODES.ADD;
		coin.position.set(aValueCounter_rrtiv.position.x, aValueCounter_rrtiv.position.y);
		coin.animationSpeed = 0.8;
		coin.scale.set(0.8);

		coin.play();

		let _fDropPath_arr = this._getCoinTrace(aValueCounter_rrtiv, aValueCounter_rrtiv.position.x, aValueCounter_rrtiv.position.y);

		let lPathTween_ptw = new PathTween(coin, _fDropPath_arr, true, false);
		lPathTween_ptw.start(15 * FRAME_RATE, Easing.quadratic.easeOut, this._destroyCoin.bind(coin));
	}

	_destroyCoin()
	{
		this.stop();
		this.destroy();
	}

	_addSequence(aSeq)
	{
		this._fSequences_arr.push(aSeq);
	}

	_removeSequences()
	{
		if (!this._fSequences_arr)
		{
			return;
		}

		while (this._fSequences_arr.length)
		{
			this._fSequences_arr.pop().destructor();
		}
	}

	_getCoinTrace(aValueCounter_rrtiv, x, y)
	{
		let startPosition = aValueCounter_rrtiv.position;

		let intermediatePosition = { x: 20 + x + this._getRandomArbitary(50), y: y - 111 };
		let endPosition = { x: -86 + this._getRandomArbitary(END_POSITION_COIN_RANGE.x), y: -92 + this._getRandomArbitary(END_POSITION_COIN_RANGE.y) };

		return [startPosition, intermediatePosition, endPosition];
	}

	_getRandomArbitary(range)
	{
		return Math.floor(Math.random() * 2 * range + 1) - range;
	}

	get coinTextures()
	{
		if (!this._coinTextures)
		{
			this._coinTextures = AtlasSprite.getFrames([APP.library.getAsset("round_result/coin_atlas")], AtlasConfig.Coin, "");
		}
		return this._coinTextures;
	}

	_startCountingBlock1()
	{
		this._startCountingBlock2();
		this._fTotalDamageCounter_c.startCounting(this.uiInfo.totalDamage, COUNTING_DURATION_PARTIAL * 6);

		this._resetCoinDelay();

		this.emit(RoundResultScreenView.ON_COINS_ANIMATION_STARTED);
	}

	_startCountingBlock2()
	{
		this._fTotalKillsCounter_c.startCounting(this.uiInfo.totalKillsCount, COUNTING_DURATION_PARTIAL, null, this._startCountingBlock3.bind(this));

		this._resetCoinDelay();
		this.emit(RoundResultScreenView.ON_COINS_ANIMATION_COMPLETED);

		if (this.uiInfo.totalKillsCount != 0)
			this._fCoinsAnimationDelay_t = new Timer(this._startCoinsAnimation.bind(this, this._fTotalKillsCountValue_rrtiv), COINS_TIME_RANGE, true);
	}

	_startCountingBlock3()
	{
		this._fTotalFreeShotsCounter_c.startCounting(this.uiInfo.totalFreeShotsCount, COUNTING_DURATION_PARTIAL, null, this._startCountingBlock4.bind(this));

		this._resetCoinDelay();
		this.emit(RoundResultScreenView.ON_COINS_ANIMATION_COMPLETED);

		if (this.uiInfo.totalFreeShotsCount != 0)
		{
			this.emit(RoundResultScreenView.ON_COINS_ANIMATION_STARTED);
			this._fCoinsAnimationDelay_t = new Timer(this._startCoinsAnimation.bind(this, this._fTotalFreeShotsCountValue_rrtiv), COINS_TIME_RANGE, true);
		}
	}

	_startCountingBlock4()
	{
		this._resetCoinDelay();
		this.emit(RoundResultScreenView.ON_COINS_ANIMATION_COMPLETED);

		this._fBulletsFiredCounter_c.startCounting(
			this.uiInfo.bulletsFiredCount,
			COUNTING_DURATION_PARTIAL,
			null,
			this._startCountingBlock5.bind(this)
		);

		if (this.uiInfo.bulletsFiredCount != 0)
		{
			this.emit(RoundResultScreenView.ON_COINS_ANIMATION_STARTED);
			this._fCoinsAnimationDelay_t = new Timer(this._startCoinsAnimation.bind(this, this._fBulletsFiredCountValue_rrtiv), COINS_TIME_RANGE, true);
		}
	}

	_startCountingBlock5()
	{
		if (!this._compensationNeeded)
		{
			return;
		}

		this._resetCoinDelay();
		this.emit(RoundResultScreenView.ON_COINS_ANIMATION_COMPLETED);

		this._fWeaponsPayoutsCounter_c.startCounting(
			this.uiInfo.weaponsSurplus,
			COUNTING_DURATION_PARTIAL,
			null,
			() =>	{
					this._resetCoinDelay();
					this.emit(RoundResultScreenView.ON_COINS_ANIMATION_COMPLETED);
					}
		);

		if (this.uiInfo.weaponsSurplus != 0)
		{
			this.emit(RoundResultScreenView.ON_COINS_ANIMATION_STARTED);
			this._fCoinsAnimationDelay_t = new Timer(this._startCoinsAnimation.bind(this, this._fWeaponsPayoutsIndicatorView_rrbpiv), COINS_TIME_RANGE, true);
		}
	}

	_resetCoinDelay()
	{
		this._fCoinsAnimationDelay_t && this._fCoinsAnimationDelay_t.destructor();
		this._fCoinsAnimationDelay_t = null;
	}

	get _nicknameStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow",
			fontSize: 11,
			align: "center",
			fill: 0xffffff
		};

		return lStyle_obj;
	}

	get _partialValuesStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 20,
			align: "center",
			fill: 0xfccc32
		};

		return lStyle_obj;
	}

	_show(aSkipAnimation_bl = false)
	{
		if (this.uiInfo.isActiveScreenMode)
		{
			this._updateAvatar();
			this._updateValues(aSkipAnimation_bl);
		}

		this.visible = true;
	}

	_showWaitingCaption()
	{
		if (!this.uiInfo.isActiveScreenMode)
		{
			return;
		}
		this._showWaitingCaption_bl = true;

		if (!this._fWaitingCaption_spr)
		{
			let lCaption_cta = this._fWaitingCaption_spr = this.addChild(I18.generateNewCTranslatableAsset('TARoundResultWaitingForNewRoundCaption'));
			lCaption_cta.position.set(125, 127);
			
			let lSpinner_cta = this._fWaitingCaption_spr.spinner = this._fWaitingCaption_spr.addChild(new PreloadingSpinner(2100, 110));
			lSpinner_cta.position.x = 146;
			lSpinner_cta.scale.set(0.6);
			lSpinner_cta.startAnimation();
		}

		this._fWaitingCaption_spr.visible = true;
	}

	_hideWaitingCaption()
	{
		if (this._fWaitingCaption_spr)
		{
			this._fWaitingCaption_spr.visible = false;
		}
	}

	get _isFrbMode()
	{
		return APP.currentWindow.gameFrbController.info.frbMode;
	}

	get _isCashBonusMode()
	{
		return APP.currentWindow.gameBonusController.info.isActivated;
	}

	_stopAllAnimation()
	{
		if (!this.uiInfo.isActiveScreenMode)
		{
			return;
		}

		this._fTotalDamageCounter_c && this._fTotalDamageCounter_c.stopCounting();
		this._fBulletsFiredCounter_c && this._fBulletsFiredCounter_c.stopCounting();
		this._fWeaponsPayoutsCounter_c && this._fWeaponsPayoutsCounter_c.stopCounting();

		this._fCoinsAnimationDelay_t && this._fCoinsAnimationDelay_t.destructor();

		this.emit(RoundResultScreenView.ON_COINS_ANIMATION_COMPLETED);

		this._updateValues(true);
	}

	_onPlayerSeatStateChanged(aEvent_obj)
	{

	}

	_onBonusStatusChanged(aEvent_obj)
	{
		this._validateCaptions();
	}

	get _playerInfo()
	{
		return APP.playerController.info;
	}

	_validateCaptions()
	{
		if (!this.uiInfo.isActiveScreenMode)
		{
			return;
		}

		if (this._compensationNeeded)
		{
			if (!this._fWeaponsPayoutsCaption_cta)
			{
				let lNewSWBonusCaptionAssetId = this._weaponsBonusCaption;
				this._fWeaponsPayoutsCaption_cta = this.addChild(I18.generateNewCTranslatableAsset(lNewSWBonusCaptionAssetId));
			}
			else
			{
				this._fWeaponsPayoutsCaption_cta.visible = true;
			}
			this._fWeaponsPayoutsCaption_cta.position.set(-49, 100);

			if (!this._fWeaponsPayoutsCounter_c)
			{
				this._fWeaponsPayoutsCounter_c = new Counter({ target: this.weaponsPayoutsIndicatorView, method: "indicatorValue" });
			}
			else
			{
				this.weaponsPayoutsIndicatorView.visible = true;
			}
		}
		else
		{
			if (this._fWeaponsPayoutsCaption_cta)
			{
				this._fWeaponsPayoutsCaption_cta.visible = false;
			}

			if (this._fWeaponsPayoutsCounter_c)
			{
				this.weaponsPayoutsIndicatorView.visible = false;
			}
		}

		this._removeBackLines();
		this._addBackLines();

		if (!this._compensationNeeded)
		{
			return;
		}

		let lNewSWBonusCaptionAssetId = this._weaponsBonusCaption;
		let lCaptionIndex = this.getChildIndex(this._fWeaponsPayoutsCaption_cta);
		let lCaptionPos = new PIXI.Point(this._fWeaponsPayoutsCaption_cta.x, this._fWeaponsPayoutsCaption_cta.y);
		
		this._fWeaponsPayoutsCaption_cta.destroy();
		this._fWeaponsPayoutsCaption_cta = this.addChildAt(I18.generateNewCTranslatableAsset(lNewSWBonusCaptionAssetId), lCaptionIndex);
		this._fWeaponsPayoutsCaption_cta.position.set(lCaptionPos.x, lCaptionPos.y);
	}

	get _compensationNeeded()
	{
		return !APP.currentWindow.isKeepSWModeActive || this._isFrbMode || this._isCashBonusMode;
	}

	destroy()
	{
		APP.currentWindow.gameStateController.off(GameStateController.EVENT_ON_PLAYER_SEAT_STATE_CHANGED, this._onPlayerSeatStateChanged, this);
		APP.currentWindow.gameBonusController.off(GameBonusController.EVENT_ON_BONUS_STATE_CHANGED, this._onBonusStatusChanged, this);

		super.destroy();

		this._fPlayerNickname_tf = null;

		this._fTotalDamageValue_rrtiv && this._fTotalDamageValue_rrtiv.destroy();
		this._fTotalDamageValue_rrtiv = null;
		this._fTotalDamageCounter_c && this._fTotalDamageCounter_c.destroy();
		this._fTotalDamageCounter_c = null;

		this._fTotalKillsCountValue_rrtiv && this._fTotalKillsCountValue_rrtiv.destroy();
		this._fTotalKillsCountValue_rrtiv = null;
		this._fTotalKillsCounter_c && this._fTotalKillsCounter_c.destroy();
		this._fTotalKillsCounter_c = null;

		this._fTotalFreeShotsCountValue_rrtiv && this._fTotalFreeShotsCountValue_rrtiv.destroy();
		this._fTotalFreeShotsCountValue_rrtiv = null;
		this._fTotalFreeShotsCounter_c && this._fTotalFreeShotsCounter_c.destroy();
		this._fTotalFreeShotsCounter_c = null;
		
		this._fBulletsFiredCountValue_rrtiv && this._fBulletsFiredCountValue_rrtiv.destroy();
		this._fBulletsFiredCountValue_rrtiv = null;
		this._fBulletsFiredCounter_c && this._fBulletsFiredCounter_c.destroy();
		this._fBulletsFiredCounter_c = null;

		this._fWeaponsPayoutsIndicatorView_rrbpiv && this._fWeaponsPayoutsIndicatorView_rrbpiv.destroy();
		this._fWeaponsPayoutsIndicatorView_rrbpiv = null;
		this._fWeaponsPayoutsCounter_c && this._fWeaponsPayoutsCounter_c.destroy();
		this._fWeaponsPayoutsCounter_c = null;

		this._fQuestsCompletedCountValue_rrtiv && this._fQuestsCompletedCountValue_rrtiv.destroy();
		this._fQuestsCompletedCountValue_rrtiv = null;
		this._fQuestsCompletedCountCounter_c && this._fQuestsCompletedCountCounter_c.destroy();
		this._fQuestsCompletedCountCounter_c = null;

		this._fQuestPayoutsIndicatorView_rrbpiv && this._fQuestPayoutsIndicatorView_rrbpiv.destroy();
		this._fQuestPayoutsIndicatorView_rrbpiv = null;
		this._fQuestPayoutsCounter_c && this._fQuestPayoutsCounter_c.destroy();
		this._fQuestPayoutsCounter_c = null;

		this._fQuestsCompletedCaption_cta = null;
		this._fQuestPayoutsCaption_cta = null;

		this._fWeaponsPayoutsCaption_cta = null;

		this._fAvatarView_pa = null;
		this._fAvatarCountainer_spr = null;

		this._nextRoundBtn = null;
		this._fGraphicLines_arr = null;

		this._showWaitingCaption_bl = null;

		this._resetCoinDelay();
		this._removeSequences();

		this._coinTextures = null;
	}
}

export default RoundResultScreenView;
