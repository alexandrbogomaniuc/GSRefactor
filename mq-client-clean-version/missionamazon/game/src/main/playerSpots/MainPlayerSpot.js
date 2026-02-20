import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { WEAPONS } from '../../../../shared/src/CommonConstants';
import TextField from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';

import WeaponsQueueController from '../../controller/uis/weapons/WeaponsQueueController';
import PlayerSpot from './PlayerSpot';
import I18 from '../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GameStateController from './../../controller/state/GameStateController';
import PlayerSpotButton from './PlayerSpotButton';
import PlayerSpotTurretButton from './PlayerSpotTurretButton';
import GameField from './../GameField';
import GameScreen from './../GameScreen';
import ShotResponsesController from './../../controller/custom/ShotResponsesController';
import MainPLayerSpotHitZone from './MainPLayerSpotHitZone';
import Weapon from './Weapon';

class MainPlayerSpot extends PlayerSpot
{
	static get EVENT_CHANGE_WEAPON() { return PlayerSpot.EVENT_CHANGE_WEAPON; }
	static get EVENT_RELOAD_REQUIRED() { return "onPlayerSpotReloadRequired"; }
	static get EVENT_AMMO_UPDATED() { return "onAmmoUpdated"; }
	static get EVENT_ON_WEAPON_SELECTED() { return WeaponsQueueController.EVENT_ON_WEAPON_SELECTED; }
	static get EVENT_ON_BET_MULTIPLIER_CHANGED() { return "onBetMultiplierChanged"; }
	static get EVENT_ON_BET_UPDATE_REQUIRED() { return "onBetUpdateRequired"; }
	static get EVENT_ON_CHANGE_WEAPON_TO_DEFAULT_REQUIRED() { return "onChangeWeaponToDefaultRequired"; }
	static get EVENT_ON_ROTATE_GUN_TO_ZERO() { return "rotateGunToZero"; }

	get rotationBlocked()
	{
		return this._fRotationBlocked_bln;
	}

	onBetChangeConfirmed(aMultiplier_num)
	{
		this._onBetChangeConfirmed(aMultiplier_num);
	}

	onBetChangeNotConfirmed()
	{
		this._onBetChangeNotConfirmed();
	}

	get currentBetMultiplier()
	{
		return this._fCurrentMult_num;
	}

	updateAmmo(aAmmo_int, aWaitBuyIn_bln)
	{
		this._updateAmmo(aAmmo_int, aWaitBuyIn_bln);
	}

	onPlusButtonClicked(aEmulateClickAnimation_bln, aWaitingBetLevelChange_bln)
	{
		this._onPlusButtonClicked(aEmulateClickAnimation_bln, aWaitingBetLevelChange_bln);
	}

	onMinusButtonClicked(aEmulateClickAnimation_bln, aWaitingBetLevelChange_bln)
	{
		this._onMinusButtonClicked(aEmulateClickAnimation_bln, aWaitingBetLevelChange_bln);
	}

	isPlusButtonClickedAllowed()
	{
		return this._fPlusButton_b.enabled && this._fCurrentMultId_num < 4;
	}

	isMinusButtonClickedAllowed()
	{
		return this._fMinusButton_b.enabled && this._fCurrentMultId_num > 0;
	}

	resetWaitingBetLevelChange()
	{
		this._fIsWaitingBetLevelChange_bl = false;
	}

	constructor(aPlayer_obj, aPosition_obj)
	{
		super(aPlayer_obj, aPosition_obj);

		this._tournamentModeInfo = APP.tournamentModeController.info;
		this._fIsWaitingBetLevelChange_bl = null;
		this._fNeedRotateGunToZeroAfterGunShotCompleted_bl = null;
	}

	get _gunCenter()
	{
		if (!this._isBottom)
		{
			return { x: 0, y: 8 };
		}
		return { x: 0, y: -8 };
	}

	get _possibleBetLevels()
	{
		return APP.playerController.info.possibleBetLevels;
	}

	get betLevelPlusButtonParameters()
	{
		return {
				width: this._fPlusButton_b.buttonWidth, 
				height: this._fPlusButton_b.buttonHeigth, 
				x: this._getBetLevelPlusButtonOffset().x,
				y: this._getBetLevelPlusButtonOffset().y
			};
	}

	get betLevelMinusButtonParameters()
	{
		return {
				width: this._fMinusButton_b.buttonWidth, 
				height: this._fMinusButton_b.buttonHeigth, 
				x: this._getBetLevelMinusButtonOffset().x,
				y: this._getBetLevelMinusButtonOffset().y
			};
	}

	_init()
	{
		this._fRicochetInfo_ri = null;
		this._fReloadRequiredSent_bln = false;
		this._fGameStateInfo_gsi = APP.currentWindow.gameStateController.info;

		super._init();

		this._weaponQueueController.i_init();

		this._fWeaponsInfo_wsi = APP.currentWindow.weaponsController.i_getInfo();
		this._fFireSettingsInfo_fssi = APP.currentWindow.fireSettingsController.i_getInfo();

		this._initMain();

		this.on("pointermove", this._onPointerMove, this);

		APP.currentWindow.on(GameScreen.EVENT_ON_TARGETING, this._onTargetingEnemy, this);
		APP.currentWindow.gameStateController.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
		APP.currentWindow.gameField.on(GameField.EVENT_ON_RICOCHET_BULLETS_UPDATED, this._onRicochetBulletsUpdated, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_SHOT_TRIGGERED, this._onShotTriggered, this);
		APP.currentWindow.shotResponsesController.on(ShotResponsesController.EVENT_ON_SERVER_SHOT_RESPONSE, this._onShotResponse, this);
	}

	_initMain()
	{
		this._addTurretButton();

		this._addStakeButtons();
		this._addStakeValue();
		this._addFreeStake();
	}

	__setRestrictedZone()
	{
	}

	_changeWeapon(aWeaponId_int, aCurrentDefaultWeaponId_int, aIsSkipAnimation_bl = false, aIsNewAwardedLevelUp_bl = false)
	{
		super._changeWeapon(aWeaponId_int, aCurrentDefaultWeaponId_int, aIsSkipAnimation_bl, aIsNewAwardedLevelUp_bl);
		this._invalidateView();
	}

	_addTurretButton()
	{
		this._fTurretButton_b = this.addChild(new PlayerSpotTurretButton());
		this._fTurretButton_b.position.set(90, 0);
		this._fTurretButton_b.on("pointerdown", (e) => e.stopPropagation(), this);
		this._fTurretButton_b.on("pointerclick", this._onTurretClicked, this);

		if (this.currentWeaponId == WEAPONS.DEFAULT)
		{
			this._fTurretButton_b.enabled = false;
		}

		if (this._isBottom)
		{
			this._fTurretButton_b.position.y = 2;
		}
		else
		{
			this._fTurretButton_b.position.y = -3;
		}
	}

	_onTurretClicked(e)
	{
		e.stopPropagation();

		this._fTurretButton_b.enabled = false;
		this.emit(MainPlayerSpot.EVENT_ON_CHANGE_WEAPON_TO_DEFAULT_REQUIRED);
	}

	_addStakeValue()
	{
		this._fStakeValue_tf = this.addChild(new TextField(this._stakeStyle));
		this._fStakeValue_tf.position.set(48, 19.5);
		this._fStakeValue_tf.maxWidth = 60;
		this._fStakeValue_tf.anchor.set(0.5, 0.5);
		this._fCurrentMultId_num = this._possibleBetLevels.indexOf(APP.playerController.info.betLevel);
		if (this._fCurrentMultId_num < 0)
		{
			this._fCurrentMultId_num = 0;
		}
		this._fPrevMultId_num = this._fCurrentMultId_num;
		this._fCurrentMult_num = this._possibleBetLevels[this._fCurrentMultId_num];

		this._invalidateShotPrice();

		if (!this._isBottom)
		{
			this._fStakeValue_tf.position.y = -21;
		}

		this._fStakeValue_tf.on('pointerdown', (e)=>e.stopPropagation(), this);

		this._validateButtonsEnable();
	}

	_addStakeButtons()
	{
		this._fPlusButton_b = this.addChild(new PlayerSpotButton("player_spot/bet_level/btn_plus_glow", "player_spot/bet_level/btn_plus_disabled"));
		this._fPlusButton_b.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fPlusButton_b.position.set(this._getBetLevelPlusButtonOffset().x, this._getBetLevelPlusButtonOffset().y);
		this._fPlusButton_b.zIndex = PlayerSpotButton.BET_LEVEL_BUTTON_Z_INDEX;
		this._fPlusButton_b.on('pointerdown', (e)=>e.stopPropagation(), this);

		this._fMinusButton_b = this.addChild(new PlayerSpotButton("player_spot/bet_level/btn_minus_glow", "player_spot/bet_level/btn_minus_disabled", false));
		this._fMinusButton_b.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fMinusButton_b.position.set(this._getBetLevelMinusButtonOffset().x, this._getBetLevelMinusButtonOffset().y);
		this._fMinusButton_b.zIndex = PlayerSpotButton.BET_LEVEL_BUTTON_Z_INDEX;
		this._fMinusButton_b.on('pointerdown', (e)=>e.stopPropagation(), this);

		if (!this._isBottom)
		{
			this._fPlusButton_b.position.y = 3;
			this._fMinusButton_b.position.y = 3;
		}
	}

	_getBetLevelMinusButtonOffset()
	{
		return {x: -49, y: -3.5}
	}

	_getBetLevelPlusButtonOffset()
	{
		return {x: 48, y: -3.5}
	}

	_addFreeStake()
	{
		this._fFreeCaption_ta = this.addChild(I18.generateNewCTranslatableAsset("TAPlayerSpotFree"));
		this._fFreeCaption_ta.position.set(47, 19.5);
		this._fFreeCaption_ta.visible = false;
		this._fFreeCaption_ta.on('pointerdown', (e)=>e.stopPropagation(), this);

		if (!this._isBottom)
		{
			this._fFreeCaption_ta.position.y = -21;
		}
	}

	_invalidateView()
	{
		// Check free shots
		const lWeaponsInfo_wsi = this._fWeaponsInfo_wsi;
		const lCurrentFreeShots_int = lWeaponsInfo_wsi.i_getWeaponFreeShots(lWeaponsInfo_wsi.currentWeaponId);
		const lIsFree_bln = (
			(lCurrentFreeShots_int > 0 && lWeaponsInfo_wsi.isFreeWeaponsQueueActivated)
			|| lWeaponsInfo_wsi.isFreeWeaponsQueueActivated
		)
			|| this._isFRBMode;

		if (lIsFree_bln)
		{
			this._fStakeValue_tf.visible = false;
			this._fFreeCaption_ta.visible = true;
		}
		else
		{
			this._fStakeValue_tf.visible = true;
			this._fFreeCaption_ta.visible = false;
		}

		this._validateButtonsEnable();
		this._invalidateShotPrice();
	}

	_onPlusButtonClicked(aEmulateClickAnimation_bln, aWaitingBetLevelChange_bln)
	{
		if (aEmulateClickAnimation_bln)
		{
			this._fPlusButton_b.startGlowAnimation();
		}

		if (!aWaitingBetLevelChange_bln)
		{
			if (this._fCurrentMultId_num < 4)
			{
				this._fPrevMultId_num = this._fCurrentMultId_num;
				++this._fCurrentMultId_num;
				if (this._fCurrentMultId_num > 4)
				{
					this._fCurrentMultId_num = 0;
				}
				this._fCurrentMult_num = this._possibleBetLevels[this._fCurrentMultId_num];

				this._onStakeValueUpdateRequred();
			}

			this._fIsWaitingBetLevelChange_bl = false;
		}
		else
		{
			this._fIsWaitingBetLevelChange_bl = true;
		}

		this._validateButtonsEnable();
	}

	_onMinusButtonClicked(aEmulateClickAnimation_bln, aWaitingBetLevelChange_bln)
	{
		if (aEmulateClickAnimation_bln)
		{
			this._fMinusButton_b.startGlowAnimation();
		}

		if (!aWaitingBetLevelChange_bln)
		{
			if (this._fCurrentMultId_num > 0)
			{
				this._fPrevMultId_num = this._fCurrentMultId_num;
				--this._fCurrentMultId_num;
				if (this._fCurrentMultId_num < 0)
				{
					this._fCurrentMultId_num = 0;
					return;
				}
				this._fCurrentMult_num = this._possibleBetLevels[this._fCurrentMultId_num];

				this._onStakeValueUpdateRequred();
				this._fIsWaitingBetLevelChange_bl = false;
			}
		}
		else
		{
			this._fIsWaitingBetLevelChange_bl = true;
		}

		this._validateButtonsEnable();
	}

	_onStakeValueUpdateRequred()
	{
		this._fUpdateSent_bln = true;
		this._fPlusButton_b.interactive = false;
		this._fMinusButton_b.interactive = false;
		this._fTurretButton_b.enabled = false;

		this.emit(MainPlayerSpot.EVENT_ON_BET_UPDATE_REQUIRED, { id: this._fCurrentMultId_num, multiplier: this._fCurrentMult_num });
	}

	_onBetChangeConfirmed(aMultiplier_num)
	{
		this._fIsWaitingBetLevelChange_bl = false;
		this._fUpdateSent_bln = false;
		this._fCurrentMult_num = aMultiplier_num;
		this._fCurrentMultId_num = this._possibleBetLevels.indexOf(this._fCurrentMult_num);

		this.emit(MainPlayerSpot.EVENT_ON_BET_MULTIPLIER_CHANGED, { id: this._fCurrentMultId_num + 1, multiplier: this._fCurrentMult_num, weaponUpdateAllowed: true });

		APP.soundsController.play("turret_change");

		this._validateButtonsEnable();
		this._invalidateShotPrice();

		this._updateAmmo();
	}

	_onGameStateChanged()
	{
		this._validateButtonsEnable();
	}

	_onRicochetBulletsUpdated()
	{
		this._validateButtonsEnable();
	}

	_onShotTriggered()
	{
		this._validateButtonsEnable();
	}

	_onShotResponse()
	{
		this._validateButtonsEnable();
	}

	get ricochetInfo()
	{
		return this._fRicochetInfo_ri || (this._fRicochetInfo_ri = APP.gameScreen.gameField.ricochetController.info);
	}

	_validateButtonsEnable()
	{
		if (!APP.gameScreen.gameField.isRoundStatePlay || this._fIsWaitingBetLevelChange_bl)
		{
			this._fPlusButton_b.enabled = false;
			this._fMinusButton_b.enabled = false;
			this._fTurretButton_b.enabled = false;
		}
		else if (this._fUpdateSent_bln)
		{
			this._fPlusButton_b.interactive = false;
			this._fMinusButton_b.interactive = false;
			this._fTurretButton_b.enabled = false;
		}
		else
		{
			let wsInteractionController = APP.webSocketInteractionController;
			const lCurrentFreeShots_int = this._fWeaponsInfo_wsi.i_getWeaponFreeShots(this._fWeaponsInfo_wsi.currentWeaponId);
			const lIsFree_bln = this.currentWeaponId !== WEAPONS.DEFAULT && lCurrentFreeShots_int > 0;
			const lIsRicochetBulletsExist_bl = this.ricochetInfo.existingMasterBulletsCount > 0;

			const lBetChangeEnabled_bln = !lIsFree_bln && !this._isFRBMode
											&& !lIsRicochetBulletsExist_bl
											&& !wsInteractionController.hasUnrespondedShots
											&& !wsInteractionController.hasDelayedShots
											&& !this._fWeaponsInfo_wsi.isFreeWeaponsQueueActivated;

			const lAllowDisable_bln = (
				!lIsRicochetBulletsExist_bl &&
				!wsInteractionController.hasUnrespondedShots &&
				!wsInteractionController.hasDelayedShots
			);

			if (this._fCurrentMultId_num >= 4)
			{
				this._fPlusButton_b.enabled = false;
			}
			else
			{
				if (lBetChangeEnabled_bln)
				{
					this._fPlusButton_b.interactive = true;
					this._fPlusButton_b.enabled = true;
				}
				else
				{
					this._fPlusButton_b.interactive = false;
					this._fPlusButton_b.enabled = !(lAllowDisable_bln
													|| lIsFree_bln
													|| this._isFRBMode
													|| this._fWeaponsInfo_wsi.isFreeWeaponsQueueActivated
													);
				}
			}

			if (this._fCurrentMultId_num <= 0)
			{
				this._fMinusButton_b.enabled = false;
			}
			else
			{
				if (lBetChangeEnabled_bln)
				{
					this._fMinusButton_b.interactive = true;
					this._fMinusButton_b.enabled = true;
				}
				else
				{
					this._fMinusButton_b.interactive = false;
					this._fMinusButton_b.enabled = !(lAllowDisable_bln
													|| lIsFree_bln
													|| this._isFRBMode
													|| this._fWeaponsInfo_wsi.isFreeWeaponsQueueActivated
													);
				}
			}

			if (this.currentWeaponId == WEAPONS.DEFAULT)
			{
				this._fTurretButton_b.enabled = false;
				this._fTurretButton_b.active = false;
			}
			else
			{
				this._fTurretButton_b.enabled = !(APP.gameScreen.gameField.isWeaponChangeInProcess 
													|| lIsFree_bln && !this._isFRBMode
													|| this._fWeaponsInfo_wsi.isFreeWeaponsQueueActivated && !this._isFRBMode
												);

				this._fTurretButton_b.active = this._fTurretButton_b.enabled;
			}
		}
	}

	get _isFRBMode()
	{
		return APP.currentWindow.gameFrbController.info.frbMode;
	}

	get _isBonusMode()
	{
		return APP.currentWindow.gameBonusController.info.isActivated;
	}

	_onBetChangeNotConfirmed()
	{
		this._fIsWaitingBetLevelChange_bl = false;
		this._fCurrentMultId_num = this._fPrevMultId_num;
		this._fCurrentMult_num = this._possibleBetLevels[this._fCurrentMultId_num];
		this._fUpdateSent_bln = false;

		this._validateButtonsEnable();
	}

	_invalidateShotPrice()
	{
		let lBet_num = this._fWeaponsInfo_wsi.i_getWeaponShotPrice(this._fWeaponsInfo_wsi.currentWeaponId);
		this._fStakeValue_tf.text = APP.currencyInfo.i_formatNumber(lBet_num, true);

		if (this._fTurretButton_b)
		{
			this._fTurretButton_b.updateView();
		}
	}

	get _stakeStyle()
	{
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 10,
			align: "center",
			fill: 0xffffff
		}

		return lStyle_obj;
	}

	get _nicknameStyle()
	{
		let lFontFamily_str = "fnt_nm_barlow_semibold";
		if (!APP.fonts.isGlyphsSupported(lFontFamily_str, this._fPlayer_obj.nickname))
		{
			lFontFamily_str = "sans-serif";
		}

		let lStyle_obj = {
			fontFamily: lFontFamily_str,
			fontSize: 9.5,
			align: "left",
			fill: 0xffffff
		}

		return lStyle_obj;
	}

	//override
	_getWeaponsBackPosition()
	{
		if(!this._isBottom)
		{
			return {x: -0.5, y: 6.5};
		}

		return { x: -0.5, y: -6 };
	}

	//override
	_getWeaponsBackScale()
	{
		return { x: 1, y: 1 };
	}

	_addBack()
	{
		this._fBack_sprt = this.addChild(APP.library.getSpriteFromAtlas("player_spot/back"));

		let lHitZoneFirstBottomOffsetY_num  = 0;
		let lHitZoneSecondBottomOffsetY_num  = 0;
		let lHitZoneCircleRadiusOffset_num  = 0;

		if (!this._isBottom)
		{
			this._fBack_sprt.scale.y = -1;
			lHitZoneFirstBottomOffsetY_num  = -4.5;
			lHitZoneSecondBottomOffsetY_num  = 13;
			lHitZoneCircleRadiusOffset_num  = 2;
		}
		
		let lHitZone1 = this.addChild(new MainPLayerSpotHitZone(MainPLayerSpotHitZone.TYPE_HIT_ZONE_CIRCLE, {x: -1, y: -7.5 + lHitZoneSecondBottomOffsetY_num, width: 29.5 + lHitZoneCircleRadiusOffset_num, height: 29.5 + lHitZoneCircleRadiusOffset_num}));
		let lHitZone2 = this.addChild(new MainPLayerSpotHitZone(MainPLayerSpotHitZone.TYPE_HIT_ZONE_CIRCLE, {x: -89.5, y: 2.5 + lHitZoneFirstBottomOffsetY_num, width: 25.5, height: 25.5}));
		let lHitZone3 = this.addChild(new MainPLayerSpotHitZone(MainPLayerSpotHitZone.TYPE_HIT_ZONE_CIRCLE, {x: 89.5, y: 2.5 + lHitZoneFirstBottomOffsetY_num, width: 25.5, height: 25.5}));
		let lHitZone4 = this.addChild(new MainPLayerSpotHitZone(MainPLayerSpotHitZone.TYPE_HIT_ZONE_RECT, {x: -94, y: -23 + lHitZoneFirstBottomOffsetY_num, width: 188, height: 50.5}));

		lHitZone1.zIndex = lHitZone2.zIndex = lHitZone3.zIndex = lHitZone4.zIndex = 27023; //Z_INDEXES.BET_LEVEL_BUTTON_HIT_AREA + 2

		let lSpotBounds_obj = this._fBack_sprt.getBounds();
		this._widthEdge = (lSpotBounds_obj.width - 8) / 2;
		this._heightEdge = (lSpotBounds_obj.height - 30) / 2;
	}

	_addNickname()
	{
		let lNickname_tf = super._addNickname();
		lNickname_tf.anchor.set(0, 0.5);
		lNickname_tf.position.set(-68.5, 19.5);

		if (!this._isBottom)
		{
			lNickname_tf.position.set(-68.5, -21);
		}
	}

	get _maxNicknameWidth()
	{
		return 44;
	}

	_addAvatar()
	{
		this._fAvatar_pa = super._addAvatar();
		this._fAvatar_pa.position.set(-90, 4);
		this._fAvatar_pa.scale.set(0.37);

		if (!this._isBottom)
		{
			this._fAvatar_pa.position.set(-90, -1);
		}

		this._refreshPlayer();
	}

	_updateAmmo(aAmmo_int, aWaitBuyIn_bln)
	{
		let lIsSpecWeaponChosen_bln = !!(this._fWeaponsInfo_wsi.currentWeaponId !== WEAPONS.DEFAULT);
		let lIsFrb_bln = APP.currentWindow.gameFrbController.info.frbMode;

		let lPlayerInfo_pi = APP.playerController.info;
		let lBetLevel_num = lPlayerInfo_pi.betLevel * lPlayerInfo_pi.currentStake;
		let lAmmoLimit_num = lPlayerInfo_pi.balance < lPlayerInfo_pi.currentStake || this._fGameStateInfo_gsi.extraBuyInAvailable ? lBetLevel_num : lPlayerInfo_pi.stakesLimit * lPlayerInfo_pi.betLevel;
		let lAmmoCost_num = lIsFrb_bln ? 0 : this._fWeaponsInfo_wsi.ammo * lPlayerInfo_pi.currentStake;
		let lIsBulletSpentWeaponChoosed_bln = !lIsSpecWeaponChosen_bln;

		let lTotalRealAmmoAmount_num = this._fWeaponsInfo_wsi.realAmmo + lPlayerInfo_pi.pendingAmmo;
		lTotalRealAmmoAmount_num = Number(lTotalRealAmmoAmount_num.toFixed(2));

		let lTotalAmmoAmount_num = Math.floor(lTotalRealAmmoAmount_num);

		if (
			!lIsFrb_bln &&
			!APP.currentWindow.gameField.roundResultActive &&
			!APP.currentWindow.gameField.roundResultActivationInProgress &&
			this._fWeaponsInfo_wsi.ammo < lAmmoLimit_num &&
			lTotalAmmoAmount_num < lAmmoLimit_num &&
			!this.reloadRequiredSent &&
			!aWaitBuyIn_bln &&
			(lPlayerInfo_pi.balance + lAmmoCost_num) > 0 &&
			lPlayerInfo_pi.balance >= lPlayerInfo_pi.currentStake &&
			lIsBulletSpentWeaponChoosed_bln &&
			!APP.currentWindow.gameBonusController.info.isActivated && /* in cash bonus mode auto BuyIn should never occur */
			!this._tournamentModeInfo.isTournamentMode /* in tournament mode auto BuyIn should never occur */
		)
		{
			this.reloadRequiredSent = true;
			this.emit(MainPlayerSpot.EVENT_RELOAD_REQUIRED);
		}
		else
		{
			this.emit(MainPlayerSpot.EVENT_AMMO_UPDATED);
			this.reloadRequiredSent = false;
		}

		this._invalidateView();
	}

	set reloadRequiredSent(aValue_bln)
	{
		this._fReloadRequiredSent_bln = aValue_bln;
	}

	get reloadRequiredSent()
	{
		return this._fReloadRequiredSent_bln;
	}

	get _weaponQueueController()
	{
		return this._fWeaponQueueController_wqc || (this._fWeaponQueueController_wqc = this._initWeaponQueueController());
	}

	_initWeaponQueueController()
	{
		let l_wqc = new WeaponsQueueController(this);
		l_wqc.on(WeaponsQueueController.EVENT_ON_WEAPON_SELECTED, this.emit, this);

		return l_wqc;
	}

	_onPointerMove(e)
	{
		let lIsPointerPushed_bl = APP.isMobile ? false : APP.currentWindow.gameField.isPointerPushed;
		let lIsShotState_bl = this._weaponSpotView && this._weaponSpotView.gun && this._weaponSpotView.gun.isShotState;
		let lIsReloadState_bl = this._weaponSpotView && this._weaponSpotView.gun && this._weaponSpotView.gun.isReloadState;

		if (this._weaponSpotView && !lIsPointerPushed_bl && !APP.currentWindow.gameField.isAutoFireEnabled)
		{
			let widthEdge = this._widthEdge;
			let heightEdge = this._heightEdge;

			if (Math.abs(e.data.local.x) < widthEdge && Math.abs(e.data.local.y) < heightEdge)
			{
				if (!this._fRotationBlocked_bln)
				{
					if (lIsShotState_bl || lIsReloadState_bl)
					{
						if (!this._fNeedRotateGunToZeroAfterGunShotCompleted_bl)
						{
							this._fNeedRotateGunToZeroAfterGunShotCompleted_bl = true;
							this._weaponSpotView.gun.once(Weapon.EVENT_ON_GUN_SHOT_COMPLETED, this._rotateGunToZero, this);
						}
					}
					else
					{
						this._rotateGunToZero();
					}
				}
			}
			else
			{
				if (this._fNeedRotateGunToZeroAfterGunShotCompleted_bl)
				{
					this._fNeedRotateGunToZeroAfterGunShotCompleted_bl = false;
					this._weaponSpotView.gun.off(Weapon.EVENT_ON_GUN_SHOT_COMPLETED, this._rotateGunToZero, this);
				}
				
				this._fRotationBlocked_bln = false;
			}
		}
	}

	_onTargetingEnemy()
	{
		this._fRotationBlocked_bln = false;
	}

	_rotateGunToZero()
	{
		this._fNeedRotateGunToZeroAfterGunShotCompleted_bl = false;
		this._weaponSpotView.gun.off(Weapon.EVENT_ON_GUN_SHOT_COMPLETED, this._rotateGunToZero, this);
		this.emit(MainPlayerSpot.EVENT_ON_ROTATE_GUN_TO_ZERO);
		this._fRotationBlocked_bln = true;
	}

	destroy()
	{
		this.off("pointermove", this._onPointerMove, this);
		
		APP.currentWindow.off(GameScreen.EVENT_ON_TARGETING, this._onTargetingEnemy, this);
		APP.currentWindow.gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
		APP.currentWindow.gameField.off(GameField.EVENT_ON_RICOCHET_BULLETS_UPDATED, this._onRicochetBulletsUpdated, this);
		APP.currentWindow.off(GameScreen.EVENT_ON_SHOT_TRIGGERED, this._onShotTriggered, this);
		APP.currentWindow.shotResponsesController.off(ShotResponsesController.EVENT_ON_SERVER_SHOT_RESPONSE, this._onShotResponse, this);

		this._fWeaponQueueController_wqc && this._fWeaponQueueController_wqc.destroy();

		super.destroy();

		this._fWeaponQueueController_wqc = null;
		this._fReloadRequiredSent_bln = undefined;
		this._fCurrentMult_num = null;
		this._fCurrentMultId_num = null;
		this._fStakeValue_tf = null;
		this._fWeaponsInfo_wsi = null;
		this._fFireSettingsInfo_fssi = null;
		this._fGameStateInfo_gsi = null;
		this._fPlusButton_b = null;
		this._fMinusButton_b = null;
		this._fUpdateSent_bln = null;
		this._fBack_sprt = null;
		this._fRotationBlocked_bln = null;
		this._fIsWaitingBetLevelChange_bl = null;
		this._fNeedRotateGunToZeroAfterGunShotCompleted_bl = null;
	}
}

export default MainPlayerSpot;
