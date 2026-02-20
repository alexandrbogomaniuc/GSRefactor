import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import DeathFxAnimation from '../animation/death/DeathFxAnimation';
import BossDeathFxAnimation from '../../view/uis/custom/bossmode/death/BossDeathFxAnimation';
import { BOMB_RADIUS, WHITE_FILTER, ENEMY_DIRECTION } from '../../config/Constants';
import { WEAPONS, ENEMIES, ENEMY_TYPES, FRAME_RATE, isBossEnemy } from '../../../../shared/src/CommonConstants';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import CryogunsController from '../../controller/uis/weapons/cryogun/CryogunsController';
import CommonEffectsManager from '../CommonEffectsManager';
import CryogunsEffectsManager from '../../view/uis/weapons/cryogun/CryogunsEffectsManager';
import EnemyIndicatorsController from '../../controller/uis/enemies/EnemyIndicatorsController';
import GameDebuggingController from '../../controller/debug/GameDebuggingController';
import GameScreen from '../GameScreen';
import EnemyIndicatorsView from './../../view/uis/enemies/EnemyIndicatorsView';
import { Z_INDEXES } from '../GameField';
import SpecialAreasMap from '../specialAreas/SpecialAreasMap';
import EnemyShadow from './shadows/EnemyShadow';
import InstantKillMarker from '../animation/instant_kill/InstantKillMarker';

const HIT_BOUNCE_TIME = 70;
const BOMB_BOUNCE_TIME = 50;
const TINT_COLOR = 0x405883;
const TINT_INTENCITY = 0.1;

const DIRECTION_LEFT = 'turnLeft';
const DIRECTION_RIGHT = 'turnRight';

const Y_INDICATOR_OFFSET = 5;

export const SPINE_SCALE = 0.4;
export const TURN_ANIM_TIME = 1000;

export const STATE_STAY 	= 'stay';
export const STATE_WALK 	= 'walk';
export const STATE_IMPACT 	= 'impact';
export const STATE_DEATH 	= 'death';
export const STATE_TURN 	= 'turn';
export const STATE_IDLE		= 'idle'; // for plants
export const STATE_GROW		= 'grow'; // for plants

export const DIRECTION = ENEMY_DIRECTION;

export const TURN_DIRECTION = {
	CCW: DIRECTION_LEFT,
	CW: DIRECTION_RIGHT
};

const ENEMIES_SCALES =
{

	[ENEMIES.Jaguar]:						1.1,
	[ENEMIES.Skullbreaker]:					1.2,
	[ENEMIES.SkullboneOrb]:					1.2,
	[ENEMIES.SnakeStraight]:				1.1,
	[ENEMIES.Scorpion]:						0.65,
	// [ENEMIES.Witch]:						1,
	// [ENEMIES.Jumper]:					1,
	// [ENEMIES.Exploder]:					1,
	// [ENEMIES.SkullboneRunner]:			1,

	[ENEMIES.RedAnt]:						0.19,
	[ENEMIES.BlackAnt]:						0.19,
	[ENEMIES.Wasp]:							0.35,
	[ENEMIES.YellowWasp]:					0.35,
	[ENEMIES.VioletWasp]:					0.35,
	[ENEMIES.Firefly]:						0.35,

	[ENEMIES.Goblin]:						0.55,
	[ENEMIES.HobGoblin]:					0.55,
	[ENEMIES.GreenImp]:						0.2,
	[ENEMIES.Ogre]:							1.512,
	[ENEMIES.PoisonPlantMaroonViolet]:		0.55,
	[ENEMIES.PoisonPlantYellowPurple]:		0.55,
	[ENEMIES.CarnivorePlantRed]:			0.43,
	[ENEMIES.CarnivorePlantGreen]:			0.43,

	[ENEMIES.BlueOrbArtillerystrike]:		1.8,
	[ENEMIES.BlueOrbFlamethrower]:			1.45,
	[ENEMIES.BlueOrbCryogun]:				1.45,
	[ENEMIES.BlueOrbLaser]:					1.3,
	[ENEMIES.BlueOrbPlasma]:				1.45,

	[ENEMIES.Dragon]:						1
}

class Enemy extends Sprite
{
	static get EVENT_ON_ENEMY_START_DYING()					{ return "onEnemyStartDying"; }
	static get EVENT_ON_DEATH_ANIMATION_STARTED() 			{ return "deathAnimationStarted"; }
	static get EVENT_ON_DEATH_ANIMATION_BONES_FELL_DOWN() 	{ return "deathAnimationBonesFellDown"; }
	static get EVENT_ON_DEATH_ANIMATION_FLARE()		 		{ return "deathAnimationFlare"; }
	static get EVENT_ON_DEATH_ANIMATION_CRACK()		 		{ return "deathAnimationCrack"; }
	static get EVENT_ON_DEATH_ANIMATION_OUTRO_STARTED() 	{ return "deathAnimationOutroStarted"; }
	static get EVENT_ON_DEATH_ANIMATION_COMPLETED() 		{ return "deathAnimationCompleted";}
	static get EVENT_ON_DEATH_ANIMATION_EXPLOSION_STARTED() { return "deathAnimationExplosionStarted";}
	static get EVENT_ON_ENEMY_VIEW_REMOVING() 				{ return "onEnemyViewRemoving"; }
	static get EVENT_ON_HV_ENEMY_READY_TO_GO() 				{ return "onHvEnemyReadyToGo"; }
	static get EVENT_ON_ENEMY_RIGHT_CLICK()					{ return "onEnemyRightClick"; }
	static get EVENT_ON_ENEMY_CLICK()						{ return "onEnemyClick"; }
	static get EVENT_ON_ENEMY_ADD_TRAJECTORY_POINT()		{ return "onEnemyAddTrajectoryPoint"; }
	static get EVENT_ON_ENEMY_PAUSE_WALKING() 				{ return "onEnemyPauseWalking"; }
	static get EVENT_ON_ENEMY_RESUME_WALKING() 				{ return "onEnemyResumeWalking"; }
	static get EVENT_ON_ENEMY_FREEZE() 						{ return "onEnemyFreeze";}
	static get EVENT_ON_ENEMY_UNFREEZE() 					{ return "onEnemyUnfreeze";}
	static get EVENT_ON_ENEMY_DESTROY()						{ return "onEnemyDestroy"; }
	static get EVENT_ON_GROUNDSHAKE()						{ return "onBossGroundshake"; }

	static get EVENT_ON_ENEMY_IS_HIDDEN()					{ return "onEnemyIsHidden"; }

	static get EVENT_ON_TIME_TO_EXPLODE_COINS()				{ return BossDeathFxAnimation.EVENT_ON_TIME_TO_EXPLODE_COINS; }

	static get EVENT_ON_DEATH_COIN_AWARD()					{ return DeathFxAnimation.EVENT_ON_DEATH_COIN_AWARD }

	static getEnemyDefaultScaleCoef(aEnemyName_str)
	{
		return ENEMIES_SCALES[aEnemyName_str] || 1;
	}

	static i_isLongDeathAnimationSupported(aTypeId_num)
	{
		switch (aTypeId_num)
		{
			case ENEMY_TYPES.OGRE:
				return true;
			default:
				return false;
		}
	}

	set deathReason(aValue_num)
	{
		this._fDeathReason_num = aValue_num;
	}

	get isEnemyLockedForTarget()
	{
		return false;
	}

	get deathReason()
	{
		return this._fDeathReason_num;
	}

	set energy(aValue_num)
	{
		this._fEnergy_num = aValue_num;
		if (this._fEnergy_num < 0) this._fEnergy_num = 0;
	}

	get energy()
	{
		if (this._fEnergy_num > this.fullEnergy)
		{
			return this.fullEnergy;
		}
		return this._fEnergy_num;
	}

	get isDeathActivated()
	{
		return this._fIsDeathActivated_bl;
	}

	set fullEnergy(aValue_num)
	{
		this._fFullEnergy_num = aValue_num;
	}

	get fullEnergy()
	{
		return this._fFullEnergy_num;
	}

	set lastPointTimeInterval(durationValue)
	{
		this._lastPointTimeInterval = durationValue;
	}

	get lastPointTimeInterval()
	{
		return this._lastPointTimeInterval;
	}

	set childHvEnemyId(aEnemyId_num)
	{
		this._fChildHvEnemyId_num = aEnemyId_num;
	}

	get childHvEnemyId()
	{
		return this._fChildHvEnemyId_num;
	}

	get hasChildHvEnemy()
	{
		return !isNaN(this.childHvEnemyId) && this.childHvEnemyId > -1;
	}

	get isDeathInProgress()
	{
		return this._deathInProgress;
	}

	get isDeathOutroAnimationStarted()
	{
		return this._deathOutroAnimationStarted;
	}

	get isBonesFellDown()
	{
		return this._fBonesFellDown_bl;
	}

	get isStayState()
	{
		return this.state == STATE_STAY;
	}

	get isWalkState()
	{
		return this.state == STATE_WALK;
	}

	get isTurnState()
	{
		return this.state == STATE_TURN;
	}

	get isUndefinedState()
	{
		return this.state == undefined;
	}

	get isFireDenied()
	{
		return this._fIsFireDenied_bl;
	}

	set isFireDenied(aValue_bl)
	{
		this._fIsFireDenied_bl = aValue_bl;
	}

	get trajectoryPositionChangeInitiated()
	{
		return this._fIsTrajectoryPositionChangeInitiated_bl;
	}

	set trajectoryPositionChangeInitiated(aValue_bl)
	{
		this._fIsTrajectoryPositionChangeInitiated_bl = aValue_bl;
	}

	get crosshairsOffsetPosition()
	{
		let lOffset_pt = {x: 0, y: 0};

		let lContainerPos_pt = this.container.position;

		lOffset_pt.x += lContainerPos_pt.x;
		lOffset_pt.y += lContainerPos_pt.y;

		return lOffset_pt;
	}

	get isFrozen()
	{
		return this._fIsFrozen_bl;
	}

	i_playPreDeathAnimation()
	{
		this.destroy();
	}

	i_hitHiglightUpdate()
	{
		this._hitHiglightUpdate();
	}

	playHitHighlightAnimation(time)
	{
		this._playHitHighlightAnimation(time);
	}

	hideEnemyEffectsBeforeDeathIfRequired()
	{
		return;
	}

	__onBeforeNewTrajectoryApplied(aTrajectory_obj)
	{

	}

	updateTrajectory(aTrajectory_obj)
	{
		this.__onBeforeNewTrajectoryApplied(aTrajectory_obj);

		this.trajectory = aTrajectory_obj;
		this.speed = aTrajectory_obj.speed || this.speed;
		this.currentTrajectorySpeed = this.speed;

		this._validateTrajectoryPoints();

		this.changeView();
	}

	_validateTrajectoryPoints()
	{
		let lPoints = this.trajectory.points;
		for (let i=0; i<lPoints.length; i++)
		{
			let lPoint = lPoints[i];
			if (i == 0)
			{
				lPoint.isStartPoint = true;
				continue;
			}

			if (i == lPoints.length-1)
			{
				lPoint.isEndPoint = true;
				continue;
			}

			lPoint.isRealTurnPoint = false;

			let lPrevPoint = lPoints[i-1];
			let lNextPoint = lPoints[i+1];

			let lPrevAngle = !isNaN(lPrevPoint.angle) ? lPrevPoint.angle : undefined;
			if (isNaN(lPrevAngle))
			{
				for (let j=i-1; j>=0; j--)
				{
					if (!Utils.isEqualPoints(lPoints[j], lPoint))
					{
						lPrevAngle = Math.atan2(lPoint.y - lPoints[j].y, lPoint.x - lPoints[j].x);
						break;
					}
				}

				if (isNaN(lPrevAngle))
				{
					lPoint.isRealTurnPoint = false;
					continue;
				}
			}

			let lNextAngle = !isNaN(lNextPoint.angle) ? lNextPoint.angle : undefined;
			if (isNaN(lNextAngle))
			{
				if (Utils.isEqualPoints(lNextPoint, lPoint))
				{
					lPoint.isRealTurnPoint = false;
					continue;
				}

				lNextAngle = Math.atan2(lNextPoint.y - lPoint.y, lNextPoint.x - lPoint.x);
			}

			lPrevAngle = GameScreen.normalizeAngle(lPrevAngle);
			lNextAngle = GameScreen.normalizeAngle(lNextAngle);


			if (this._isTrajectoryTurnPointCondition(lPrevAngle, lNextAngle))
			{
				lPoint.isRealTurnPoint = true;
			}
		}
	}

	_isTrajectoryTurnPointCondition(aPrevAngle, aNextAngle)
	{
		return this._calculateDirection(aPrevAngle) !== this._calculateDirection(aNextAngle)
	}

	updateLife(aVal_num)
	{
		this.life = aVal_num;
	}

	get invulnerable()
	{
		return this._fInvulnerable_bl;
	}

	set invulnerable(aValue_bl)
	{
		this._fInvulnerable_bl = aValue_bl;
	}

	get curAnimationFrameTime()
	{
		return undefined;
	}

	get curAnimationState()
	{
		return this._curAnimationState;
	}

	constructor(params)
	{
		super();

		this.params = params;

		this._fIgnoreCollisionsBodyPartsNames_str_arr = this.__generateIgnoreCollisionsBodyPartsNames();
		this._fPreciseCollisionBodyPartsNames_str_arr = this.__generatePreciseCollisionBodyPartsNames();

		this._fPrevUpdateAccurateTime_num = undefined;
		this._fInvulnerable_bl = false;
		this._curAnimationState = undefined;
		this._fIsUndetectableSpineBodyBounds_bl = false;
		this._fSpecialAreasMap_sam = APP.gameScreen.gameField.specialAreasMap;

		//DEBUG...

		this.tintColor = undefined;
		this.tintIntencity = undefined;
		this.shadowTintColor = undefined;
		//if (APP.currentWindow.mapsController.info.mapId === 1)
		{
			this._fGameDebuggingController_gdc = APP.gameDebuggingController;
			this._fGameDebuggingController_gdc.on(GameDebuggingController.i_EVENT_TINT_UPDATE, this._onDebugTintUpdated, this);
			this._fGameDebuggingController_gdc.on(GameDebuggingController.i_EVENT_SHADOW_TINT_UPDATE, this._onDebugShadowTintUpdated, this);
			this.tintColor = APP.gameDebuggingController.debugTintColor;
			this.tintIntensity = APP.gameDebuggingController.debugTintIntencity;
			this.shadowTintColor = APP.gameDebuggingController.debugShadowTintColor;
		}
		//...DEBUG

		this._fIsOnSpawnActionRequired_bl = true;
		this.parentEnemyId = params.parentEnemyId;
		this.typeId = params.typeId;
		this.name = params.name;
		this.id = params.id;
		this.radius = params.radius;
		this.life = 1;
		this.angle = params.angle;
		this.speed = params.trajectory.speed || params.speed; //2,4,8
		this.currentTrajectorySpeed = this.speed;
		this.awardedPrizes = params.awardedPrizes;
		this.prizes = params.prizes;
		this._fEnergy_num = params.energy;
		this._fFullEnergy_num = params.fullEnergy;
		this.boss = params.boss;
		this.skin = params.skin;

		this.startPosition = params.startPosition;
		this.trajectory = params.trajectory;

		this._validateTrajectoryPoints();
		this.swarmType = params.swarmType;

		this.upateCurrentNearbyTrajectoryPoints(params.prevTurnPoint, params.nextTurnPoint);
		this._fLastPosition_obj = null;

		this._fBossHitScreamTimer_t = null;

		this._parseCustomParams(params);

		this._lastPointTimeInterval = undefined;
		this.lastPointTimeInterval = params.lastPointTimeInterval || undefined;

		this.instaMark = null;

		this.initialDirection = null;

		this.container = this.addChild(new Sprite);
		this.container.zIndex = 1;

		this.imageName = this.getImageName(this.name);
		this.isEnded = false;
		this.direction = this._calculateDirection();
		this.turnDirection = DIRECTION_LEFT;

		this.state = null;
		this._fIsImpactAnimationInProgress_bl = false;
		this.spineSpeed = this.getSpineSpeed();

		this.hitBounceDelta = null;
		this.bombBounceDelta = null;
		this.currentHitBounceDelta = null;
		this.currentBombBounceDelta = null;

		this.bossAppearanceDelta = null;
		this.bossAppearanceMask = null;

		this._deathInProgress = false;
		this._deathOutroAnimationStarted = false;
		this._fBonesFellDown_bl = false;

		this.deathFxAnimation = null;

		this.view = null;
		this.viewPos = null;
		this.spineView = null;
		this._fCurSpineName_str = undefined;
		this.spineViewPos = null;
		this.shadow = null;
		this.footPoint = null;

		this.stepTimers = null;
		this._stepsAmount = undefined;

		this._vibrateTimer = null;
		this._fBossWalkTimer_t = null;

		this.hvTopContainer = null;
		this.hvBottomContainer = null;
		this.hvAppearingEffects = null;
		this._risingSequence = null;
		this._fChildHvEnemyId_num = undefined;
		this.isVibrating_bl = false;

		this._fIsFireDenied_bl = false;
		this._fIsTrajectoryPositionChangeInitiated_bl = false;

		this._fBossAppearanceInProgress_bln = false;
		this._fFreezAfterLasthand_bl = null;

		this.setSpineViewPos();
		this.setViewPos();

		this.addShadow();
		this.addFootPoint();
		this.changeShadowPosition();
		this.changeFootPointPosition();
		this.changeInstaMarkPosition();

		let lShowAppearanceEffect_bl = !params.allowUpdatePosition;
		this._fIsLasthand_bl = params.isLasthand;
		this._createView(lShowAppearanceEffect_bl);
		if (params.needShowSound) this.playAppearSound();

		this._fIsDeathActivated_bl = false;
		this._fDeathAnimationInProgress_bl = false;

		this._fCryogunsController_cgs = APP.currentWindow.cryogunsController;
		this._fCryogunsInfo_csi = this._fCryogunsController_cgs.info;

		this._fCryogunsController_cgs.on(CryogunsController.EVENT_FREEZE_ENEMY, this._onCryogunsFreezeEnemy, this);
		this._fCryogunsController_cgs.on(CryogunsController.EVENT_UNFREEZE_ENEMY, this._onCryogunsUnfreezeEnemy, this);
		this._fIsFrozen_bl = false;
		this._fFreezeBaseContainer_sprt = null;
		this._fFreezeCover_sprt = null;
		this._fFreezeMask_sprt = null;
		this._fFreezeGround_sprt = null;
		this._fLastFreezeTimeMarker_num = undefined;

		this._fHitRectangle_r = null;
		this._fDeathReason_num = null;

		this._gameScreen = APP.currentWindow;
		this._gameField = this._gameScreen.gameField;

		this._firstPoint = null;

		this._fPauseWalkingTimeMarker_num = undefined;

		this._addSpecialEffectsIfRequired(this._fIsLasthand_bl);

		this._invalidateStates();

		this._fEnemyIndicatorsController_eic = this._initEnemyIndicatorsController();
		this._fEnemyIndicatorsView_eiv = null;
		this.updateIndicatorsPosition();
		this._initEnemyIndicatorsView();
	}

	get isLasthand()
	{
		return !!this._fIsLasthand_bl;
	}

	_onTimeToTintEnemy()
	{
 		if (!this.isBoss)
 		{
			this.tintColor = TINT_COLOR;
			this.tintIntensity = TINT_INTENCITY;

 			this._updateTint();
 		}
	}

	//virtual protected
	_updateTint()
	{
	}

	//virtual protected
	_untint()
	{
	}

	_onDebugTintUpdated()
	{
	}

	_onDebugShadowTintUpdated()
	{
		this.shadowTintColor = APP.gameDebuggingController.debugShadowTintColor;
		this._rerenderShadow();
	}

	_parseCustomParams(params)
	{
	}

	//virtual
	_addSpecialEffectsIfRequired(aOptIsLasthand)
	{
	}

	//ENEMY INDICATORS...
	_initEnemyIndicatorsView()
	{
		this._fEnemyIndicatorsView_eiv = this.addChild(this.enemyIndicatorsController.view);
		this._fEnemyIndicatorsView_eiv.zIndex = 201;
	}

	get enemyIndicatorsController()
	{
		return this._fEnemyIndicatorsController_eic || (this._fEnemyIndicatorsController_eic = this._initEnemyIndicatorsController());
	}

	_initEnemyIndicatorsController()
	{
		let lInfo_obj =
		{
			typeId: this.typeId,
			energy: this.energy,
			fullEnergy: this.fullEnergy,
			id: this.id,
			skin: this.skin,
			enemyName: this.name
		};

		let lView_eiv = this._getEnemyIndicatorView();

		let l_eic = this._getEnemyIndicatorController(lInfo_obj, lView_eiv);
		l_eic.i_init();

		l_eic.on(EnemyIndicatorsController.EVENT_ON_ENERGY_UPDATED, this._onEnergyUpdated, this);
		this._fEnemyIndicatorsController_eic = l_eic;

		return l_eic;
	}

	_getEnemyIndicatorController(info, view)
	{
		return new EnemyIndicatorsController(info, view);
	}

	_getEnemyIndicatorView()
	{
		return new EnemyIndicatorsView();
	}

	_onEnergyUpdated(data)
	{
		let lNewEnergy_num = data.energy;
		this.energy = lNewEnergy_num;

		this.emit(Enemy.EVENT_ON_ENEMY_ENERGY_UPDATED, {energy: data.energy, damage: data.damage});
	}

	updateIndicatorsPosition()
	{
		let offset = this.getEnemyIndicatorsViewPosOffset();

		this.enemyIndicatorsController.view.position.x = offset.x;
		this.enemyIndicatorsController.view.position.y = offset.y;
	}

	getEnemyIndicatorsViewPosOffset()
	{
		let lOffset = {x: 0, y: 0};

		let lIndicatorsHeight_num = this.enemyIndicatorsController.view.getBounds().height;

		if (this.spineView && this.spineView.view && !this.isBoss)
		{
			let lBounds_obj = this.spineView.view.getLocalBounds();
			let lX_num = lBounds_obj.x * this.spineView.scale.x + this.spineViewPos.x;
			let lY_num = lBounds_obj.y * this.spineView.scale.y + this.spineViewPos.y;
			let lW_num = lBounds_obj.width * this.spineView.scale.x;

			lOffset.x = lX_num + lW_num/2;
			lOffset.y = lY_num - lIndicatorsHeight_num/2 - Y_INDICATOR_OFFSET;
		}

		return lOffset;
	}
	//...ENEMY INDICATORS

	addInstaMark(aIsFast_bl)
	{
		if (this.instaMark) return; // to prevent spawning multiple marks (in this case only one will be destroyed, and rest will stay)
		let lLocCenterOffset_obj = this.getLocalCenterOffset();

		let mark = this.instaMark = this.addChild(new InstantKillMarker(aIsFast_bl));
		mark.position.set(lLocCenterOffset_obj.x, lLocCenterOffset_obj.y);
	}

	removeInstaMark()
	{
		this.instaMark && this.instaMark.destroy();
		this.instaMark = null;
	}

	_invalidateStates()
	{
		this._freezeSuspicionAfterCreation();
	}

	isTargetable()
	{
		//is invulnerable
		if(this.invulnerable)
		{
			return false;
		}

		//is dead
		if(this.life === 0)
		{
			return false;
		}

		if(this._fDeathAnimationInProgress_bl)
		{
			return false;
		}

		let lX_num = this.position.x;
		let lY_num = this.position.y;

		if(!this.isFlyingEnemy())
		{
			lX_num += this.shadow.position.x;
			lY_num += this.shadow.position.y;
		}

		//is out of screen
		if(
			lX_num < 0 ||
			lY_num < 0 ||
			lX_num > 960 ||
			lY_num > 540
			)
		{

			return false;
		}

		if(!this.isFlyingEnemy())
		{
			//no click when inside targeting-restricted area
			if(
				this._fSpecialAreasMap_sam.isSpecialArea(
					SpecialAreasMap.AREA_ID_TARGETING_FORBIDDEN,
					lX_num,
					lY_num
					)
				)
			{
				return false;
			}
		}

		return true;
	}


	isBehindWall()
	{
		//flying mobs are never behind walls
		if(this.isFlyingEnemy())
		{
			return false;
		}

		return (

				this._fSpecialAreasMap_sam.isSpecialArea(
								SpecialAreasMap.AREA_ID_BEHIND_WALLS,
								this.position.x + this.shadow.position.x,
								this.position.y + this.shadow.position.y)
				);
	}

	//RIGHT CLICK DETECTION...
	onPointerRightClick(aX_num, aY_num)
	{
		return (
			this.isTargetable() &&
			this.isCollision(aX_num, aY_num));
	}

	onPointerClick(aX_num, aY_num)
	{
		return this.onPointerRightClick(aX_num, aY_num);
	}
	//...RIGHT CLICK DETECTION

	getImageName(name)
	{
		let imageName;
		switch (name)
		{
			case ENEMIES.Jaguar:
				imageName = 'enemies/jaguar/Jaguar';
				break;
			case ENEMIES.Skullbreaker:
			imageName = 'enemies/skullbreaker/Skullbreaker';
				break;
			case ENEMIES.SkullboneOrb:
			imageName = 'enemies/armedwarrior/EnemyHeavy';
			break;
			case ENEMIES.Witch:
				imageName = 'enemies/witch/Witch';
			break;
			case ENEMIES.SnakeStraight:
				imageName = 'enemies/snake/Snake';
			break;
			case ENEMIES.Exploder:
			case ENEMIES.BabyFrog:
				imageName = 'enemies/frog/Frog';
			break;
			case ENEMIES.Scorpion:
				imageName = 'enemies/scorpion/Scorpion';
			break;

			case ENEMIES.Jumper:
				imageName = 'enemies/jumper/Skullbreaker';
			break;
			case ENEMIES.RedAnt:
				imageName = 'enemies/red_ant/RedAnt';
				break;
			case ENEMIES.BlackAnt:
				imageName = 'enemies/black_ant/BlackAnt';
				break;
			case ENEMIES.SpiderBoss:
				imageName = 'enemies/spider/Spider';
			break;
			case ENEMIES.GolemBoss:
				imageName = 'enemies/golem/Golem';
			break;
			case ENEMIES.ApeBoss:
				imageName = 'enemies/ape/Ape';
			break;
			case ENEMIES.PoisonPlantMaroonViolet:
				imageName = 'enemies/plants/PoisonPlantMaroonViolet';
				break;
			case ENEMIES.PoisonPlantYellowPurple:
				imageName = 'enemies/plants/PoisonPlantYellowPurple';
				break;
			case ENEMIES.CarnivorePlantRed:
				imageName = 'enemies/plants/CarnivorePlantRed';
				break;
			case ENEMIES.CarnivorePlantGreen:
				imageName = 'enemies/plants/CarnivorePlantGreen';
				break;
			case ENEMIES.GreenImp:
				imageName = 'enemies/imp/green/Imp';
				break;
			case ENEMIES.Wasp:
				imageName = 'enemies/wasp/Wasp';
				break;
			case ENEMIES.YellowWasp:
				imageName = 'enemies/wasp_yellow/Wasp';
				break;
			case ENEMIES.VioletWasp:
				imageName = 'enemies/wasp_violet/Wasp';
				break;
			case ENEMIES.Firefly:
				imageName = 'enemies/firefly/Firefly';
				break;
			default: throw new Error('imageName is undefined for ' + name);
		}

		return imageName;
	}

	playDeathSound()
	{
		let soundName = '';
		let randomSoundIndex = 1;
		let lPlaySound_bln = Boolean(Math.random() > 0.5);
		// let lowProfile = APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM);
		if (lPlaySound_bln)
		{
			switch (this.name)
			{
				case ENEMIES.Jaguar:
					soundName = "death_jaguar";
					break;
				case ENEMIES.Jumper:
					soundName = "death_jumper";
					break;
				case ENEMIES.Witch:
					soundName = "death_shaman";
					break;
				case ENEMIES.Skullbreaker:
				case ENEMIES.SkullboneOrb:
					soundName = "death_skullbreaker";
					break;
				case ENEMIES.SnakeStraight:
					soundName = "death_straight_snake";
					break;
			}
		}
		if (soundName)
		{
			APP.soundsController.play(soundName);
		}
	}

	playAppearSound()
	{
		let soundName = "";
		switch (this.name)
		{
			case ENEMIES.Jaguar:
				soundName = "spawn_jaguar";
			break;
		}
		let randomSoundIndex = 1;
		// let isLowProfile = APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM);

		if (soundName)
		{
			APP.soundsController.play(soundName);
		}
	}

	getSpineSpeed()
	{
		let speed = 1;
		return speed;
	}

	upateCurrentNearbyTrajectoryPoints(prevTurnPoint, nextTurnPoint)
	{
		let lLastPrevPoint = this.prevTurnPoint;
		let lLastNextPoint = this.nextTurnPoint;

		this.prevTurnPoint = prevTurnPoint;
		this.nextTurnPoint = nextTurnPoint;

		this._updateCurrentTrajectorySpeed(lLastPrevPoint, this.prevTurnPoint, lLastNextPoint, this.nextTurnPoint);

	}

	_updateCurrentTrajectorySpeed(aLastPrevPoint, aNewPrevPoint, aLastNextPoint, aNewNextPoint)
	{
		if (aLastPrevPoint && aLastNextPoint && aNewPrevPoint && aNewNextPoint)
		{
			if (
						!Utils.isEqualPoints(aLastPrevPoint, aNewPrevPoint)
						|| aLastPrevPoint.time !== aNewPrevPoint.time
						|| !Utils.isEqualPoints(aLastNextPoint, aNewNextPoint)
						|| aLastNextPoint.time !== aNewNextPoint.time
						|| this.currentTrajectorySpeed == this.speed
					)
			{
				this.currentTrajectorySpeed = this._calcCurrentTrajectorySpeed();
			}

		}
		else
		{
			this.currentTrajectorySpeed = this.speed;
		}
	}

	_calcCurrentTrajectorySpeed()
	{
		if (!!this.prevTurnPoint && !!this.nextTurnPoint)
		{
			let lDeltaTime = this.nextTurnPoint.time - this.prevTurnPoint.time;
			let lDistance = Utils.getDistance(this.nextTurnPoint, this.prevTurnPoint)

			if (~~lDistance == 0)
			{
				return this.speed;
			}

			let lCurTrajectorySpeed = +(lDistance/lDeltaTime*1000/11.45).toFixed(2);

			return lCurTrajectorySpeed;
		}
	}

	setViewPos()
	{
		let pos = {x: 0, y: 0};
		this.viewPos = pos;
	}

	setSpineViewPos()
	{
		let pos = {x: 0, y: 0};
		this.spineViewPos = pos;
	}

	static getDirection(angle)
	{

		//return DIRECTION.RIGHT_UP;

		let direction = DIRECTION.RIGHT_DOWN;
		if (angle > Math.PI*2) angle -= Math.PI*2;
		if (angle < 0) angle = 2*Math.PI - angle;

		if (angle > 0) direction = DIRECTION.RIGHT_DOWN;
		if (angle > Math.PI/2) direction = DIRECTION.LEFT_DOWN;
		if (angle > Math.PI) direction = DIRECTION.LEFT_UP;
		if (angle > Math.PI*3/2) direction = DIRECTION.RIGHT_UP;

		return direction
	}

	getTurnDirection(direction)
	{
		let turnDirection;

		switch (this.direction)
		{
			case DIRECTION.RIGHT_DOWN:
				turnDirection = (direction == DIRECTION.RIGHT_UP) ? DIRECTION_LEFT : DIRECTION_RIGHT;
			break;
			case DIRECTION.RIGHT_UP:
				turnDirection = (direction == DIRECTION.LEFT_UP) ? DIRECTION_LEFT : DIRECTION_RIGHT;
			break;
			case DIRECTION.LEFT_UP:
				turnDirection = (direction == DIRECTION.LEFT_DOWN) ? DIRECTION_LEFT : DIRECTION_RIGHT;
			break;
			case DIRECTION.LEFT_DOWN:
				turnDirection = (direction == DIRECTION.RIGHT_DOWN) ? DIRECTION_LEFT : DIRECTION_RIGHT;
			break;
		}

		return turnDirection;
	}

	_calculateDirection(aOptAngle_num=undefined)
	{
		let lAngle_num = aOptAngle_num !== undefined ? aOptAngle_num : this.angle;

		return Enemy.getDirection(lAngle_num);
	}

	addShadow()
	{
		this.shadow = this.container.addChild(this._createShadow());
	}

	_createShadow()
	{
		return (new EnemyShadow());
	}

	_rerenderShadow()
	{
		let lTintColor_hex = this.shadowTintColor;
		if (lTintColor_hex == undefined) return;
		try
		{
			let sprite = this.shadow.view;
			sprite.convertToHeaven();
			sprite.tint = lTintColor_hex;
			sprite.color.dark[0] = sprite.color.light[0];
			sprite.color.dark[1] = sprite.color.light[1];
			sprite.color.dark[2] = sprite.color.light[2];
			sprite.color.invalidate();
		}
		catch (e)
		{
			console.log(e);
		}

	}

	_generateShadowView()
	{
		return APP.library.getSprite('shadow');
	}

	addFootPoint()
	{
		this.footPoint = this.container.addChild(new PIXI.Graphics());
		this.footPoint.clear().beginFill(0x000000, 0.01).drawCircle(0, 0, 4);
		this.footPoint.position.set(30, 120);
		this.footPoint.zIndex = 10;
	}

	get isBodyOutOfScreen()
	{
		return false;
	}

	_createView(aShowAppearanceEffect_bl)
	{
		this._initView();

		if (this.isHighVolatility)
		{
			this._initHVView(aShowAppearanceEffect_bl);
			// this._initHVValueView();
		}
	}

	get isHighVolatility()
	{
		return false;
	}

	_initView()
	{
		this.view = this.container.addChild(new Sprite());
		this.view.scale.set(this.getViewScale());
		this.view.position.set(this.viewPos.x, this.viewPos.y);
		this.view.alpha = 0;
		this.view.animationSpeed = 1;
		this.view.zIndex = 3;


		this.state = STATE_STAY;
		this._curAnimationState = STATE_WALK;

		this.container.addChild(this._generateSpineView(this.imageName));

		this.spineView.scale.set(SPINE_SCALE);
		this._fCurrentAnimationName_str = "Walk";
		this.spineView.setAnimationByName(0, "Walk", true);
		this.spineView.play();
		this.spineView.view.state.timeScale = this.spineSpeed;
		this.spineView.position.set(this.spineViewPos.x, this.spineViewPos.y);
		this.spineView.zIndex = 3;
	}

	_updateHitPointerRectangle()
	{
		let rect = this._calcHitPointerRectangle();

		//DEBUG...
		// if (this.hitPointerRectangle && this.hitPointerRectangle.parent)
		// {
		// 	this.hitPointerRectangle.destroy();
		// }
		// this.hitPointerRectangle = this.container.addChild(new PIXI.Graphics());
		// this.hitPointerRectangle.clear().beginFill(0x00ff00, 0.2).drawRect(rect.x, rect.y, rect.width, rect.height).endFill();
		// this.hitPointerRectangle.beginFill(0x0000ff, 0.5).drawCircle(this.getLocalCenterOffset().x, this.getLocalCenterOffset().y, 3).endFill();
		// this.hitPointerRectangle.zIndex = 300000;
		//...DEBUG

		this.interactive = true;
		this.hitArea = rect;
	}

	_calcHitPointerRectangle()
	{
		let lFootPoint_pt = this.getCurrentFootPointPosition();
		let lWidth_num = this._getHitRectWidth();
		let lHeight_num = this._getHitRectHeight();

		let lRect_r = null;

		if(this._fHitRectangle_r)
		{
			lRect_r = this._fHitRectangle_r;
		}
		else
		{
			lRect_r = new PIXI.Rectangle(0, 0, 0, 0);
			this._fHitRectangle_r = lRect_r;
		}

		lRect_r.x = lFootPoint_pt.x + this.getLocalCenterOffset().x - lWidth_num/2;
		lRect_r.y = lFootPoint_pt.y + this.getLocalCenterOffset().y - lHeight_num/2;
		lRect_r.width = lWidth_num;
		lRect_r.height = lHeight_num;

		return lRect_r;
	}

	_getHitRectWidth()
	{
		let hitWidth = 0;
		return hitWidth;
	}

	_getHitRectHeight()
	{
		let hitHeight = 0;
		return hitHeight;
	}

	_getApproximateWidth()
	{
		let coef = this.isScarab ? 3 : 2;
		return this._getHitRectWidth() * coef;
	}

	_getApproximateHeight()
	{
		let coef = this.isScarab ? 3 : 2;
		return this._getHitRectHeight() * coef;
	}

	_initHVView(aShowAppearanceEffect_bl = false)
	{
		if (!this.hvTopContainer)
		{
			this.hvTopContainer = this.container.addChild(new Sprite);
			this.hvTopContainer.zIndex = 11;
		}
		if (!this.hvBottomContainer)
		{
			this.hvBottomContainer = this.container.addChild(new Sprite);
			this.hvBottomContainer.zIndex = 1;
		}

		if (aShowAppearanceEffect_bl)
		{
			this._showHVAppearanceEffect(this.name);
		}
	}

	_showHVAppearanceEffect(aEnemyName_str)
	{
		//add effects
		this.changeFootPointPosition();
		this.direction = this._calculateDirection();

		this.hvAppearingEffects = this._generateHvAppearingEffectsView(aEnemyName_str);
		this.hvAppearingEffects.once(Sprite.EVENT_ON_DESTROYING, this._onHvAppearanceEffectDestroying, this);
		this.hvAppearingEffects.zIndex = 100;

		//rising
		this.spineView && this.spineView.stop();
		this.container.scale.y = 0;

		let dy = this.getCurrentFootPointPosition().y;

		let lRisingSequence_seq = [
			{tweens: [], duration: 528},
			{tweens: [{prop: "scale.y", to: 1,
						onchange: (e) => {this.container.y = dy * (1 - e.value);}}
					],
					duration: 858,
					onfinish: this._onHVEnemyReadyToGo.bind(this)
			}
		];

		this._risingSequence = Sequence.start(this.container, lRisingSequence_seq);
	}

	get _isHVRisingUpInProgress()
	{
		return !!this._risingSequence;
	}

	_destroyRisingSequence()
	{
		if (!this._risingSequence)
		{
			return;
		}

		this._risingSequence.destructor();
		this._risingSequence = null;
	}

	_forceHVRisingUpIfRequired()
	{
		if (this._risingSequence)
		{
			this._destroyRisingSequence();

			this.container.scale.y = 1;
			this.container.y = 0;

			this._onHVEnemyReadyToGo();
		}
	}

	_generateHvAppearingEffectsView(aEnemyName_str)
	{
		return new Sprite;
	}

	_onHVEnemyReadyToGo()
	{
		this._destroyRisingSequence();

		if (!this.isFrozen && !this._deathInProgress)
		{
			if (this.isWalkState)
			{
				this.spineView && this.spineView.play();
			}
			else
			{
				this.setWalk();
			}
		}

		this.emit(Enemy.EVENT_ON_HV_ENEMY_READY_TO_GO);
	}

	_onHvAppearanceEffectDestroying(event)
	{
		this.hvAppearingEffects = null;
	}

	getViewScale()
	{
		let scale = 1;
		return scale;
	}

	getStepTimers()
	{
		let timers = [];

		this._stepsAmount = 0;

		return timers;
	}

	getScaleCoefficient()
	{
		return Enemy.getEnemyDefaultScaleCoef(this.name);
	}

	changeSpineView(type)
	{
		if (type == STATE_DEATH)
		{
			return;
		}

		let x = this.spineViewPos.x;
		let y = this.spineViewPos.y;
		let name = this.imageName;
		let animationName = '';
		let animationLoop = true;
		let scale = SPINE_SCALE;

		if (this.spineView)
		{
			if (this.spineView.view && this.spineView.view.state)
			{
				this.spineView.view.state.onComplete = null;

				if (this.spineView.view.state.tracks && this.spineView.view.state.tracks[0])
				{
					this.spineView.view.state.tracks[0].onComplete = null;
				}
			}
			this.spineView.destroy();
		}

		let scaleCoefficient = this.getScaleCoefficient();
		scale *= scaleCoefficient;

		if (this.direction != DIRECTION.LEFT_DOWN)
		{
			name += this.direction.substr(3);
		}

		switch(type)
		{
			case STATE_STAY:
			case STATE_WALK:
			case STATE_IMPACT:
				animationName = 'Walk';
			break;
		}

		this.state = type;
		if (type !== STATE_STAY)
		{
			this._curAnimationState = type;
		}

		this.container.addChild(this._generateSpineView(name));

		this.spineView.scale.set(scale);
		this._fCurrentAnimationName_str = animationName;
		this.spineView.setAnimationByName(0, animationName, animationLoop);

		if (this.state == STATE_STAY)
		{
			this.spineView.stop();
		}
		else
		{
			this.spineView.play();
		}

		this.spineView.position.set(x, y);
		this.spineView.zIndex = 3;
		this.spineView.view.state.timeScale = this.spineSpeed;
	}

	_onSpineViewGenerated()
	{
	}

	_generateSpineView(spineName)
	{
		let lSpineView = this.spineView = APP.spineLibrary.getSprite(spineName);
		this._fCurSpineName_str = spineName;
		this._addSpineMixes();
		this._onSpineViewGenerated();

		return lSpineView;
	}

	windSpineAnimationTo(aFrameTime_num)
	{
		if(!this.spineView)
		{
			return;
		}

		this.spineView.view.state.tracks[0].trackTime = aFrameTime_num;
	}

	_addSpineMixes()
	{
		this.spineView.setAnimationsDefaultMixDuration(this._defaultSpineMix);

		this._addSpineCustomAnimationsMixes();
	}

	_addSpineCustomAnimationsMixes()
	{
		let lCustomSpineTransitions_arr = this._customSpineTransitionsDescr;
		if (!lCustomSpineTransitions_arr || !lCustomSpineTransitions_arr.length)
		{
			return;
		}

		let prefix = this._getSpineAnimDefaultPrefix();

		for (let i=0; i<lCustomSpineTransitions_arr.length; i++)
		{
			let customTransition = lCustomSpineTransitions_arr[i];

			let fromName = this._getSpineMixAnimName(customTransition.from, prefix);
			let toName = this._getSpineMixAnimName(customTransition.to, prefix);
			let transDuration = customTransition.duration; // duration in sec

			if (this.spineView.hasAnimation(fromName) && this.spineView.hasAnimation(toName))
			{
				this.spineView.setAnimationMix(fromName, toName, transDuration);
			}
		}
	}

	get customSpineTransitions()
	{
		let lCustomSpineTransitions_arr = this._customSpineTransitionsDescr;
		if (!lCustomSpineTransitions_arr || !lCustomSpineTransitions_arr.length)
		{
			return null;
		}

		let prefix = this._getSpineAnimDefaultPrefix();

		let customTransitions = [];
		for (let i=0; i<lCustomSpineTransitions_arr.length; i++)
		{
			let customTransition = lCustomSpineTransitions_arr[i];

			let fromName = this._getSpineMixAnimName(customTransition.from, prefix);
			let toName = this._getSpineMixAnimName(customTransition.to, prefix);
			let transDuration = customTransition.duration; // duration in sec

			customTransitions.push({fromName: fromName, toName: toName, transDuration: transDuration})
		}

		return customTransitions;
	}

	_getSpineAnimDefaultPrefix()
	{
		let spineName = this._fCurSpineName_str;
		let lMatchedDirection_arr = spineName.match(/\d+/ig);
		let prefix = (lMatchedDirection_arr && lMatchedDirection_arr.length) ? lMatchedDirection_arr[0] + "_" : "";

		return prefix;
	}

	_getSpineMixAnimName(animNameTemplate, prefix)
	{
		let animName = animNameTemplate;
		animName = animName.replace("<PREFIX>", prefix);

		return animName;
	}

	get _defaultSpineMix()
	{
		return 0;
	}

	get _customSpineTransitionsDescr()
	{
		// returns array of custom spine transitions,
		// format of single transition: {from: <ANIM_NAME>, from: <ANIM_NAME>, duration: <TRANSITION_DURATION_IN_SEC>},
		// ANIM_NAME - spine animation name; optional <ANIM_NAME> placeholder will be replaced with current spine anim direction prefix ("0_", "90_", "180_", "270_"),
		// example: {from: "idle", to: "walk", duration: 0.2} or {from: "<PREFIX>idle", to: "<PREFIX>walk", duration: 0.2}

		return null;
	}

	changeTextures(type, noChangeFrame)
	{
		this.view.alpha = (type == STATE_TURN) ? 1 : 0;

		if (type == STATE_DEATH)
		{
			this.changeSpineView(type);
			return;
		}

		this.spineView.alpha = (type == STATE_IMPACT || type == STATE_WALK || type == STATE_STAY ) ? 1 : 0;

		if (this.spineView && this.spineView.alpha > 0)
		{
			this.changeSpineView(type);
		}
		else
		{
			if (this.view.alpha == 0)
			{
				this.view.alpha = !this.view.alpha*1;
				this.spineView.alpha = !this.spineView.alpha*1;
			}

			let textures, speedCoefficient = 1;

			let scale = this.getViewScale();
			this.view.textures = textures;
			this.view.scale.set(scale);
			this.view.animationSpeed = 24 * speedCoefficient / (TURN_ANIM_TIME / this.view.textures.length);

			if (noChangeFrame)
			{
				this.view.play();
			}
			else
			{
				this.view.gotoAndPlay(0);
			}

			this.changeShadowPosition();
			this.changeInstaMarkPosition();
		}
	}

	changeShadowPosition()
	{
		let x = 18, y = 50, scale = 1;

		this.shadow.position.set(x, y);
		this.shadow.scale.set(scale);
	}

	changeFootPointPosition()
	{
		let x = 0, y = 0;

		this.footPoint.position.set(x, y);

		this._updateHitPointerRectangle();
	}

	changeInstaMarkPosition()
	{
		if (this.instaMark)
		{
			let pos = this.getLocalCenterOffset();
			this.instaMark.position.set(pos.x, pos.y);
		}
	}

	getCurrentFootPointPosition()
	{
		return this.footPoint.position;
	}

	getCurrentGlobalFootPointPosition()
	{
		let lCurFootPointPos_p = this.getCurrentFootPointPosition();

		return new PIXI.Point(this.position.x + lCurFootPointPos_p.x, this.position.y + lCurFootPointPos_p.y);
	}

	getColor()
	{
		let color = '0x';
		switch (this.typeId)
		{
			case 3:
			{
				color += 'bbcd00';
				break;
			}
			default:
			{
				color += 'aabbaa';
				break;
			}
		}
		return color;
	}

	checkBackDirection(direction)
	{
		let check = (this.direction == DIRECTION.LEFT_DOWN && direction == DIRECTION.RIGHT_UP)
		|| (this.direction == DIRECTION.LEFT_UP && direction == DIRECTION.RIGHT_DOWN)
		|| (this.direction == DIRECTION.RIGHT_UP && direction == DIRECTION.LEFT_DOWN)
		|| (this.direction == DIRECTION.RIGHT_DOWN && direction == DIRECTION.LEFT_UP);

		return check;
	}

	changeView()
	{
		if (this.checkInitialTurn())
		{
			return;
		}
		let direction = this._calculateDirection();
		if (direction != this.direction)
		{
			this.turnDirection = this.getTurnDirection(direction);
			let checkBackDirection = this.checkBackDirection(direction);
			if (!checkBackDirection)
			{
				this.changeTextures(STATE_TURN, false);
				this.direction = direction;
				this.view.removeListener('animationend');
				this.view.once("animationend", (e) =>{
					this.endTurn();
				});
			}
			else
			{
				if (this.state == STATE_DEATH) return;

				this.direction = direction;
				this.changeTextures(this.state, true);
				if (this.spineView) this.spineView.view.state.timeScale = this.getSpineSpeed();
				this.changeShadowPosition();
				this.changeFootPointPosition();
				this.changeInstaMarkPosition();
			}
		}

		this._fPrevUpdateAccurateTime_num = APP.gameScreen.accurateCurrentTime;
	}

	checkInitialTurn(direction)
	{
		//  we don't have to play turn spritesheet animation wneh the enemy first comes into screen - just do it instantly
		if (!this.initialDirection)
		{
			let direction = this._calculateDirection();
			this.initialDirection = this.direction = direction;
			this.turnDirection = this.getTurnDirection(direction);
			this.endTurn(true /*initial*/);
			return true;
		}
		return false;
	}

	endTurn(aInitial_bl = false)
	{
		this.view && this.view.removeListener('animationend');

		if (this._fIsDeathActivated_bl) // we were waiting for endTurn to proceed with the Death animation
		{
			this.setDeath();
			return;
		}
		if (this.state == STATE_DEATH) return;

		this.changeTextures(this.state, true);
		if (this.spineView) this.spineView.view.state.timeScale = this.getSpineSpeed();
		this.changeShadowPosition();
		this.changeFootPointPosition();
		this.changeInstaMarkPosition();
	}


	isFlyingEnemy()
	{
		switch (this.name)
		{
			case ENEMIES.Wasp:
			case ENEMIES.YellowWasp:
			case ENEMIES.VioletWasp:
			case ENEMIES.Firefly:
			case ENEMIES.BlueOrbArtillerystrike:
			case ENEMIES.BlueOrbCryogun:
			case ENEMIES.BlueOrbFlamethrower:
			case ENEMIES.BlueOrbLaser:
			case ENEMIES.BlueOrbPlasma:
				return true;
			default:
				return false;
		}
	}

	//override
	changeZindex()
	{
		this.zIndex = this.y + this.footPoint.y;

		if (this.isFlyingEnemy())
		{
			this.zIndex += 1000;
		}
		else if (
			this.name === ENEMIES.RedAnt
			|| this.name === ENEMIES.BlackAnt
		)
		{
			this.zIndex -= 540;
		}
		else if (this.name === ENEMIES.SnakeStraight)
		{
			this.zIndex -= 640;
		}
	}

	//CRYOGUN EFFECT...
	_freezeSuspicionAfterCreation()
	{
		if (this._fCryogunsController_cgs.i_isEnemyFrozen(this.id))
		{
			this._freezeIfRequired(false);
			this._fFreezAfterLasthand_bl = true;
		}
	}

	_freezeIfRequired(aIsAnimated_bl = true)
	{
		if (!this._fIsDeathActivated_bl)
		{
			//non-animated freezing means this is lasthand - we must check first points then to make sure we need to freeze him
			if (aIsAnimated_bl || Utils.isEqualPoints(this.trajectory.points[0], this.trajectory.points[1]))
			{
				this._freeze(aIsAnimated_bl);
			}
		}
	}

	_freeze(aIsAnimated_bl = true)
	{

		if (!this._fIsFrozen_bl)
		{
			this._destroyFrozenSprites(); //just for sure
			this._fLastFreezeTimeMarker_num = Date.now();
			this.setStay();
			//do not move enemy while it stays
			this.currentBombBounceDelta && this.currentBombBounceDelta.sequence && this.currentBombBounceDelta.sequence.pause();
			this._fIsFrozen_bl = true;

			this._forceHVRisingUpIfRequired();

			//don't add freeze effect for Edge and IE
			if (APP.isPixiHeavenLibrarySupported)
			{
				this._addFreezeEffect(aIsAnimated_bl);
			}

			this.emit(Enemy.EVENT_ON_ENEMY_FREEZE, {enemyId: this.id});
		}

		this._fLastPosition_obj = null;
	}

	_addFreezeEffect(aIsAnimated_bl = true)
	{
		this._fFreezeBaseContainer_sprt = this.container.addChild(new Sprite);
		this._fFreezeBaseContainer_sprt.zIndex = 50;

		//freeze mask...
		let lMask_sprt = this._generateFreezeMask();
		lMask_sprt.zIndex = 10;
		this._fFreezeBaseContainer_sprt.addChild(lMask_sprt);
		this._fFreezeMask_sprt = lMask_sprt;
		//...freeze mask

		this._addFreezeCover();

		if (aIsAnimated_bl)
		{
			this._fFreezeBaseContainer_sprt.alpha = 0;
			let lAlphaSequence_seq = [
				{
					tweens: [],
					duration: 5*2*16.7,
					onfinish: () => {
						APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater && this._addFreezeSmokes();
					}
				},
				{
					tweens: [
						{ prop: 'alpha', to: 1 }
					],
					duration: 10*2*16.7,
					ease: Easing.sine.easeIn
				}
			];
			Sequence.start(this._fFreezeBaseContainer_sprt, lAlphaSequence_seq);
		}

		//freeze ground...
		this._addFreezeGround(aIsAnimated_bl);
		//...freeze ground
	}

	_generateFreezeMask(aResolution_num)
	{
		//position and rotation should be set to 0 for correct texture generation
		let lRememberStats_obj = {
			containerRotation: this.container.rotation,
			spineRotation: this.spineView.rotation,
			spinePosX: this.spineView.position.x,
			spinePosY: this.spineView.position.y
		};

		this.container.rotation = 0;
		this.spineView.rotation = 0;
		this.spineView.position.x = 0;
		this.spineView.position.y = 0;

		let lSpineBounds_obj = this.spineView.view.getBounds();
		let lSpineLocBounds_obj = this.spineView.view.getLocalBounds();

		this.spineView.view.x = -lSpineLocBounds_obj.x*this.spineView.view.scale.x;
		this.spineView.view.y = -lSpineLocBounds_obj.y*this.spineView.view.scale.y;

		this.spineView.view.filters = [WHITE_FILTER];

		let lResulution_num = isNaN(aResolution_num) ? (APP.isMobile ? 1 : 2) : aResolution_num;
		var l_txtr = PIXI.RenderTexture.create({width: lSpineLocBounds_obj.width*this.spineView.view.scale.x, height: lSpineLocBounds_obj.height*this.spineView.view.scale.y, scaleMode: PIXI.SCALE_MODES.NEAREST, resolution: lResulution_num});
		APP.stage.renderer.render(this.spineView.view, l_txtr);

		let lMask_sprt = new Sprite();
		lMask_sprt.anchor.set(0, 0);
		lMask_sprt.textures = [l_txtr];
		lMask_sprt.scale.set(this.spineView.view.scale.x*this.spineView.scale.x, this.spineView.view.scale.y*this.spineView.scale.y);
		lMask_sprt.convertToHeaven();

		this.spineView.view.filters = null;

		this.spineView.view.x = 0;
		this.spineView.view.y = 0;

		let lLocalSpineBounds_obj = this.spineView.view.getLocalBounds();
		lLocalSpineBounds_obj.x *= this.spineView.scale.x;
		lLocalSpineBounds_obj.y *= this.spineView.scale.y;
		lLocalSpineBounds_obj.width *= this.spineView.scale.x;
		lLocalSpineBounds_obj.height *= this.spineView.scale.y;

		lMask_sprt.position.set(lLocalSpineBounds_obj.x + this.spineViewPos.x, lLocalSpineBounds_obj.y + this.spineViewPos.y);

		//after texture generated return position and rotation to old values
		this.container.rotation = lRememberStats_obj.containerRotation;
		this.spineView.rotation = lRememberStats_obj.spineRotation;
		this.spineView.position.x = lRememberStats_obj.spinePosX;
		this.spineView.position.y = lRememberStats_obj.spinePosY;
		lRememberStats_obj = null;

		return lMask_sprt;
	}

	_addFreezeCover()
	{
		let lFreezeCover_sprt = new PIXI.heaven.Sprite(APP.library.getSprite('weapons/Cryogun/Freeze').textures[0]);

		lFreezeCover_sprt.anchor.set(0.5, 0.5);
		let centerPos = this.getLocalCenterOffset();
		lFreezeCover_sprt.position.set(centerPos.x, centerPos.y);
		let lFreezeBounds_obj = lFreezeCover_sprt.getBounds();
		let lFreezeCoverScaleY_num =  (this._getApproximateHeight()) / lFreezeBounds_obj.height;
		let lFreezeCoverScaleX_num = (this._getApproximateWidth()) / lFreezeBounds_obj.width;
		let lMaxScale_num = Math.max(lFreezeCoverScaleX_num, lFreezeCoverScaleY_num);
		lFreezeCover_sprt.scale.set(lMaxScale_num, lMaxScale_num);

		this._fFreezeBaseContainer_sprt.addChild(lFreezeCover_sprt);
		this._fFreezeCover_sprt = lFreezeCover_sprt;


		this._fFreezeCover_sprt.zIndex = this._fFreezeMask_sprt.zIndex+1;

		this._fFreezeCover_sprt.maskSprite = this._fFreezeMask_sprt;
		this._fFreezeCover_sprt.pluginName = 'batchMasked';
		this._fFreezeMask_sprt.alpha = 0.28;
	}

	_addFreezeSmokes()
	{
		let pos = this.getLocalCenterOffset();
		this._addFreezeSmoke(pos.x + 5, pos.y + 10, /*0xA8F2FF*/0xCBEAEF);
		this._addFreezeSmoke(pos.x - 5, pos.y + 20);
	}

	_addFreezeSmoke(x, y, aTint_int = undefined)
	{
		let smoke = this._fFreezeBaseContainer_sprt.addChild(new Sprite);
		smoke.position.set(x, y);
		smoke.zIndex = 100;
		smoke.textures = CommonEffectsManager.getDieSmokeUnmultTextures();
		smoke.anchor.set(0.57, 0.81);
		smoke.scale.set(2);
		if (aTint_int !== undefined)
		{
			smoke.tint = aTint_int;
		}
		smoke.on('animationend', () => {
			smoke.destroy();
		})
		smoke.play();
	}

	_addFreezeGround(aIsAnimated_bl = true)
	{
		if (!this.isFreezeGroundAvailable) return;

		this._fFreezeGround_sprt = APP.library.getSprite('weapons/Cryogun/freeze_ground');
		this._fFreezeGround_sprt.anchor.set(146/283, 82/195);
		this._fFreezeGround_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fFreezeGround_sprt.scale.set(this._getFreezeGroundScaleCoef());
		this.shadow.addChild(this._fFreezeGround_sprt);
		if (aIsAnimated_bl)
		{
			this._fFreezeGround_sprt.alpha = 0;
			this._fFreezeGround_sprt.fadeTo(1, 15*2*16.7);
		}
	}

	_getFreezeGroundScaleCoef()
	{
		let coef = 1;

		return coef;
	}

	_unfreezeIfRequired()
	{
		if (this._fIsFrozen_bl && this.isStayState)
		{
			this._unfreeze();
		}
	}

	_unfreeze(aIsAnimated_bl = true)
	{
		this._fIsFrozen_bl = false;

		if (aIsAnimated_bl)
		{
			this._explodeIce();
		}

		this._resumeAfterUnfreeze();

		this.emit(Enemy.EVENT_ON_ENEMY_UNFREEZE, {enemyId: this.id});
	}

	_explodeIce()
	{
		let lIceExplodeSprite_sprt = this.container.addChild(new Sprite);
		let pos = this.getLocalCenterOffset();
		lIceExplodeSprite_sprt.position.set(pos.x, pos.y);
		lIceExplodeSprite_sprt.scale.set(this._getExplodeIceScaleCoefficient());
		lIceExplodeSprite_sprt.anchor.set(0.5, 0.5);
		lIceExplodeSprite_sprt.zIndex = 200;
		lIceExplodeSprite_sprt.textures = CryogunsEffectsManager.getIceExplodeTextures();
		lIceExplodeSprite_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		lIceExplodeSprite_sprt.on('animationend', () => {
			lIceExplodeSprite_sprt.destroy();
		})
		lIceExplodeSprite_sprt.play();
	}

	_getExplodeIceScaleCoefficient()
	{
		return this.getScaleCoefficient();
	}

	_resumeAfterUnfreeze()
	{
		this._destroyFrozenSprites();

		if (this.spineView && this.spineView.view && this.spineView.view.state && !this._fIsImpactAnimationInProgress_bl)
		{
			this._resumeSpineAnimationAfterUnfreeze();
		}
		else
		{
			this.setWalk();
		}

		//complete the bomb rebouncing restore
		if (this.currentBombBounceDelta && this.currentBombBounceDelta.sequence && this.currentBombBounceDelta.sequence.paused)
		{
			this.currentBombBounceDelta.sequence.resume();
		}
	}

	_resumeSpineAnimationAfterUnfreeze()
	{
		if (this._fFreezAfterLasthand_bl)
		{
			this.setWalk();
			this._fFreezAfterLasthand_bl = false;
		}
		this.spineView.play();
	}

	_destroyFrozenSprites()
	{
		if (this._fFreezeBaseContainer_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fFreezeBaseContainer_sprt));
		}
		this._fFreezeCover_sprt && this._fFreezeCover_sprt.destroy();
		this._fFreezeCover_sprt = null;
		this._fFreezeMask_sprt && this._fFreezeMask_sprt.destroy({children: true, texture: true, baseTexture: true});
		this._fFreezeMask_sprt = null;
		this._fFreezeBaseContainer_sprt && this._fFreezeBaseContainer_sprt.destroy();
		this._fFreezeBaseContainer_sprt = null;

		if (this._fFreezeGround_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fFreezeGround_sprt));
			this._fFreezeGround_sprt.destroy();
		}
		this._fFreezeGround_sprt = null;
	}

	_restoreStateBeforeFreeze()
	{
		this.setWalk();
	}
	//...CRYOGUN EFFECT

	setStay()
	{
		this._destroyBossWalkTimer();
		this.view && this.view.removeListener('animationend');
		this.changeTextures(STATE_STAY);
	}

	setWalk()
	{
		this._destroyBossWalkTimer();
		if (this._fIsFrozen_bl || this._isHVRisingUpInProgress)
		{
			return;
		}
		else
		{
			this._destroyFrozenSprites();
		}

		this.view && this.view.removeListener('animationend');
		this.changeTextures(STATE_WALK);

		this.__onWalk()
	}

	continueStayedStateAnim()
	{
		if (this.isStayState && !!this._curAnimationState)
		{
			this.state = this._curAnimationState;
		}
	}

	__onWalk()
	{

	}

	get currentHitBounce()
	{
		return this.currentHitBounceDelta;
	}

	get currentBombBounce()
	{
		return this.currentBombBounceDelta;
	}

	get isFreezeGroundAvailable()
	{
		return true;
	}

	get isRunner()
	{
		return false;
	}

	get isFastTurnEnemy()
	{
		return this.name == ENEMIES.Wasp
				|| this.name == ENEMIES.YellowWasp
				|| this.name == ENEMIES.VioletWasp
				|| this.name == ENEMIES.Firefly;
		// 	return this.isRunner;
	}

	get isScarab()
	{
		return false;
	}

	get isLocust()
	{
		return false;
	}

	get isCritter()
	{
		return 	this.name == ENEMIES.RedAnt
				|| this.name == ENEMIES.BlackAnt;
	}

	get isBoss()
	{
		return isBossEnemy(this.typeId);
	}

	get isBombEnemy()
	{
		return false;
	}

	showHitBounce(angle, weaponId, aOptHitDuration_num)
	{
		if (this.isFrozen)
		{
			//don't do hit bounce for frozen enemies - it looks non-natural
			return;
		}

		if (weaponId == WEAPONS.RICOCHET || weaponId == WEAPONS.RICOCHET_POWER_UP)
		{
			this.startVibration(aOptHitDuration_num/3); // aOptHitDuration_num is too long
			return;
		}

		let hitBounceDistance = this.getHitBounceDistance(weaponId) * this.getHitBounceMultiplier();

		if (isNaN(hitBounceDistance))
		{
			throw new Error('hitBounceDistance error! hitBounceDistance = " + hitBounceDistance + ", weaponId = ' + weaponId + ", angle = " + angle);
		}

		let dx = hitBounceDistance * Math.cos(-angle - Math.PI),
			dy = hitBounceDistance * Math.sin(-angle - Math.PI);

		if (!this.hitBounceDelta)
		{
			this.hitBounceDelta = {x: dx, y: dy};

			let seq = [
				{
					tweens: [
						{prop: "x", to: this.hitBounceDelta.x*1.2}, //increased jitter of enemies by 20% (1.2)
						{prop: "y", to: this.hitBounceDelta.y*1.2}
					],
					duration: HIT_BOUNCE_TIME/5
					//ease: /*Easing.quadratic.easeIn*/ Easing.linear.easeIn,
				},
				{
					tweens: [
						{prop: "x", to: 0},
						{prop: "y", to: 0}
					],
					duration: HIT_BOUNCE_TIME,
					//ease: /*Easing.quadratic.easeOut*/Easing.linear.easeOut,
					onfinish: (e) => {
						this.resetHit();
					}
				}
			]

			this.currentHitBounceDelta = {x: 0, y: 0};
			Sequence.start(this.currentHitBounceDelta, seq);
		}
/*
		if (this.isBoss && (Math.random() > 0.8) && !this._fBossHitScreamTimer_t)
		{
			let soundIndex = APP.isMobile || APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM) ? 5 : Utils.random(1,5);
			let soundName = 'mq_boss_hit_' + soundIndex;
			APP.soundsController.play(soundName);

			this._fBossHitScreamTimer_t = new Timer(()=>{
				this._fBossHitScreamTimer_t && this._fBossHitScreamTimer_t.destructor();
				this._fBossHitScreamTimer_t = null;
			}, 1000);
		}*/
	}

	showBombBounce(angle, dist, mapZonePoints)
	{
		if (dist > BOMB_RADIUS || this._fIsFrozen_bl || this._isHVRisingUpInProgress || this._fBossAppearanceInProgress_bln)
		{
			return;
		}
		let distPercentage = Math.min(dist/BOMB_RADIUS, 1);
		let distance = this.getBombBounceDistance() * this.getHitBounceMultiplier();
		let step = (1 - distPercentage) * distance;

		let dx = step * Math.cos(-angle - Math.PI),
			dy = step * Math.sin(-angle - Math.PI);


		let potentialPoint = {x: dx, y: dy};
		if (this.currentBombBounceDelta)
		{
			potentialPoint.x += this.currentBombBounceDelta.x;
			potentialPoint.y += this.currentBombBounceDelta.y;
		}
		potentialPoint.x += this.getGlobalPosition().x;
		potentialPoint.y += this.getGlobalPosition().y;

		let mapZonePolygon = mapZonePoints ? new PIXI.Polygon(mapZonePoints) : null;
		if (mapZonePolygon && !Utils.isPointInsidePolygon(potentialPoint, mapZonePolygon))
		{
			return;
		}

		this.bombBounceDelta = {x: dx, y: dy};
		if (this.currentBombBounceDelta)
		{
			this.bombBounceDelta.x += this.currentBombBounceDelta.x;
			this.bombBounceDelta.y += this.currentBombBounceDelta.y;
			// remove previous sequence
			this.currentBombBounceDelta.sequence.destructor();
		}
		else
		{
			this.currentBombBounceDelta = {x: 0, y: 0};
		}

		let seq = [
			{
				tweens: [],
				duration: distPercentage * 500
			},
			{
				tweens: [
					{prop: "x", to: this.bombBounceDelta.x},
					{prop: "y", to: this.bombBounceDelta.y}
				],
				duration: BOMB_BOUNCE_TIME,
				/*ease: Easing.quadratic.easeIn,*/
				onfinish: (e) => {
					let pt = {x: this.x + this.container.x, y: this.y + this.container.y};
					pt.y += this.getCurrentFootPointPosition().y;
					pt.x += this.getCurrentFootPointPosition().x;
					this.emit(Enemy.EVENT_ON_ENEMY_ADD_TRAJECTORY_POINT, {enemyId: this.id, x: pt.x, y: pt.y});
					this.resetBombBounce();
				}
			}
		]

		this.currentBombBounceDelta.sequence = Sequence.start(this.currentBombBounceDelta, seq);
	}

	getBombBounceDistance()
	{
		return Utils.random(30, 40, true);
	}

	prepareForBossAppearance()
	{
		this.container.visible = false;
		this._fBossAppearanceInProgress_bln = true;
	}

	_onBossAppear()
	{
		this._fBossAppearanceInProgress_bln = false;
		this._resetBossAppearanceMask();

		this._destroyBossWalkTimer();
	}

	_destroyBossWalkTimer()
	{
		this._fBossWalkTimer_t && this._fBossWalkTimer_t.destructor();
		this._fBossWalkTimer_t = null;
	}

	resetBossAppearance()
	{
		if (this.bossAppearanceDelta && this.bossAppearanceDelta.sequences)
		{
			while (this.bossAppearanceDelta.sequences.length)
			{
				this.bossAppearanceDelta.sequences.pop().destructor();
			}
		}
		this.bossAppearanceDelta = null;

		this._resetBossAppearanceMask();

		if (this.container)
		{
			this.container.visible = true;
		}
	}

	_resetBossAppearanceMask()
	{
		if (this.container)
		{
			this.container.mask = null;
		}

		if (this.bossAppearanceMask)
		{
			this.bossAppearanceMask.destroy();
		}
		this.bossAppearanceMask = null;
		if (this.shadow) this.shadow.visible = true;
	}

	showBossAppearance(aSequence_arr, aInitialParams_obj)
	{
		if (this.isBoss)
		{
			this.resetBossAppearance();

			let lMask_gr = this._generateBossAppearanceMask();
			this.container.mask = lMask_gr;
			this.bossAppearanceMask = lMask_gr;
			this.shadow.visible = false;

			this._applyBossStartParams(aInitialParams_obj);

			let n1 = aSequence_arr.length;
			let n2 = aSequence_arr[n1 - 1].length;

			aSequence_arr[n1 - 1][n2 - 1].onfinish = this._onBossAppear.bind(this);

			this.bossAppearanceDelta.sequences = [];
			for (let i = 0; i < aSequence_arr.length; i++)
			{
				this.bossAppearanceDelta.sequences.push(Sequence.start(this.bossAppearanceDelta, aSequence_arr[i]));
			}
		}

		if (this.container) this.container.visible = true;
	}

	_generateBossAppearanceMask()
	{
		let lMask_gr = this.addChild(new PIXI.Graphics());
		let lSpineBounds_rt = this.spineView.view.getLocalBounds();
		let lY_num = lSpineBounds_rt.y * this.spineView.scale.y + this.spineViewPos.y;
		let lHeight_num = lSpineBounds_rt.height * this.spineView.scale.y;
		lMask_gr.beginFill(0x00FF00).drawRect(-480, lY_num, 960, lHeight_num).endFill();

		return lMask_gr;
	}

	_applyBossStartParams(aInitialParams_obj)
	{
		this.bossAppearanceDelta = {x:aInitialParams_obj.x, y:aInitialParams_obj.y, rotation:aInitialParams_obj.rotation};

		this.updateOffsets();
		this.updateRotation();
	}

	updateRotation()
	{
		let angle = 0;
		if (this.bossAppearanceDelta !== null && this.bossAppearanceDelta.rotation !== undefined)
		{
			angle += this.bossAppearanceDelta.rotation;
		}
		this.container.rotation = angle;
	}

	_resetOffsets()
	{
		this.resetHit();
		this.resetBombBounce();
	}

	resetHit()
	{
		Sequence.destroy(Sequence.findByTarget(this.currentHitBounceDelta));
		this.hitBounceDelta = null;
		this.currentHitBounceDelta = null;
	}

	resetBombBounce()
	{
		this.bombBounceDelta = null;
		this.currentBombBounceDelta = null;
	}

	_pauseWalking()
	{
		if (!isNaN(this._fPauseWalkingTimeMarker_num)) return; // already paused

		this._fPauseWalkingTimeMarker_num = Date.now();

		this.emit(Enemy.EVENT_ON_ENEMY_PAUSE_WALKING, {enemyId: this.id});
	}

	_resumeWalking()
	{
		let delta = 0;
		if (!isNaN(this._fPauseWalkingTimeMarker_num))
		{
			let lPauseWalkingEndTimeMarker_num = (this._fLastFreezeTimeMarker_num !== undefined && this._fLastFreezeTimeMarker_num >= this._fPauseWalkingTimeMarker_num) ? this._fLastFreezeTimeMarker_num : Date.now();
			delta = lPauseWalkingEndTimeMarker_num - this._fPauseWalkingTimeMarker_num;
			delta = this._correctResumedWalkTimeDelta(delta);
			this._fPauseWalkingTimeMarker_num = undefined;
		}

		let pt = {x: this.x, y: this.y};
		pt.y += this.getCurrentFootPointPosition().y;
		pt.x += this.getCurrentFootPointPosition().x;
		this.emit(Enemy.EVENT_ON_ENEMY_ADD_TRAJECTORY_POINT, {enemyId: this.id, x: pt.x, y: pt.y, timeOffset: -delta});

		this.emit(Enemy.EVENT_ON_ENEMY_RESUME_WALKING, {enemyId: this.id, timeOffset: delta});
	}

	_correctResumedWalkTimeDelta(delta)
	{
		return delta;
	}

	setDeath(aIsInstantKill_bl = false, aPlayerWin_obj = null)
	{
		this._destroyBossWalkTimer();
		this.life = 0;

		if (this.isFrozen)
		{
			this._unfreeze();
		}
	}

	setDeathFramesAnimation(aIsInstantKill_bl = false, aPlayerWin_obj = null)
	{
		this._deathInProgress = true;

		this.view && this.view.removeListener('animationend');

		if (this.isBoss)
		{
			if (aIsInstantKill_bl)
			{
				this._playDeathFxAnimation(aIsInstantKill_bl);
			}
			else
			{
				this.spineView.stop();
				this._playBossDeathFxAnimation();
			}
		}
		else
		{
			if (!aIsInstantKill_bl)
			{
				this.playDeathSound();
			}

			this._forceHVRisingUpIfRequired();

			this._playDeathFxAnimation(aIsInstantKill_bl);
		}

		this.emit(Enemy.EVENT_ON_DEATH_ANIMATION_STARTED);
	}

	__getDeathAnimationPointX()
	{
		return this.footPoint.x;
	}

	__getDeathAnimationPointY()
	{
		return this.footPoint.y;
	}

	__getDeathAnimationContainer()
	{
		return this.container;
	}

	_playDeathFxAnimation(aIsInstantKill_bl)
	{
		if (this.spineView)
		{
			Sequence.destroy(Sequence.findByTarget(this.spineView));
			this.spineView.destroy();
			this.spineView = null;
			this.emit(Enemy.EVENT_ON_ENEMY_IS_HIDDEN);
			this._fCurSpineName_str = undefined;
		}

		if (this.shadow)
		{
			Sequence.destroy(Sequence.findByTarget(this.shadow));
			this.shadow.view && Sequence.destroy(Sequence.findByTarget(this.shadow.view));
			this.shadow && this.shadow.destroy();
			this.shadow = null;
		}

		this.deathFxAnimation = this.__getDeathAnimationContainer().addChild(this._generateDeathFxAnimation());

		this.deathFxAnimation.position.set(
			this.__getDeathAnimationPointX(),
			this.__getDeathAnimationPointY());
		this.deathFxAnimation.scale.set(this._deathFxScale);

		this._validatezIndexOnDeath();

		if (this.isCritter || aIsInstantKill_bl)
		{
			this.deathFxAnimation.playIntro(false/*fast variant*/, true /*skip outro*/);
		}
		else
		{
			this.deathFxAnimation.playIntro(this.hasChildHvEnemy /*fast variant*/);
		}
		this.deathFxAnimation.once(DeathFxAnimation.EVENT_ANIMATION_COMPLETED, (e) => {
			this.onDeathFxAnimationCompleted();
		});
		this.deathFxAnimation.once(DeathFxAnimation.EVENT_ON_DEATH_COIN_AWARD, this.emit, this);

		if (!this.isCritter && !aIsInstantKill_bl)
		{
			this.deathFxAnimation.once(DeathFxAnimation.EVENT_OUTRO_STARTED, (e) => {
				this.onDeathFxOutroStarted();
			});
		}
		this.deathFxAnimation.zIndex = 20;
	}

	_validatezIndexOnDeath()
	{
		this.zIndex = Z_INDEXES.GROUNDBURN;
	}

	_generateDeathFxAnimation()
	{
		return new DeathFxAnimation();
	}

	get _deathFxScale()
	{
		switch (this.name)
		{
			case ENEMIES.Goblin:
			case ENEMIES.HobGoblin:
				return 0.4;
			case ENEMIES.PoisonPlantMaroonViolet:
			case ENEMIES.PoisonPlantYellowPurple:
				return 0.9;
			case ENEMIES.CarnivorePlantRed:
			case ENEMIES.CarnivorePlantGreen:
				return 0.7;
		}

		return 0.7;
	}

	_playBossDeathFxAnimation(aPlayerWin_obj)
	{
		let lPlaySound_bln = !!(aPlayerWin_obj.playerWin !== null && aPlayerWin_obj.playerWin !== undefined && aPlayerWin_obj.playerWin > 0)
		this.deathFxAnimation = this._getBossDeathFxAnimationInstance(aPlayerWin_obj);
		this.deathFxAnimation.zIndex = 20;
		this.deathFxAnimation.once(BossDeathFxAnimation.EVENT_FLARE_STARTED, () => {
			this.emit(Enemy.EVENT_ON_DEATH_ANIMATION_FLARE);
		});
		this.deathFxAnimation.on(BossDeathFxAnimation.EVENT_CRACK_STARTED, () => {
			this.emit(Enemy.EVENT_ON_DEATH_ANIMATION_CRACK, {playSound: lPlaySound_bln});
		});
		this.deathFxAnimation.once(BossDeathFxAnimation.EVENT_OUTRO_STARTED, this.onBossDeathFxOutroStarted, this);
		this.deathFxAnimation.once(BossDeathFxAnimation.EVENT_ANIMATION_COMPLETED, this.onDeathFxAnimationCompleted, this);
		this.deathFxAnimation.once(BossDeathFxAnimation.EVENT_ON_TIME_TO_EXPLODE_COINS, this._onTimeToExplodeCoins, this);
		this.deathFxAnimation.playIntro();
	}

	_getBossDeathFxAnimationInstance(aPlayerWin_obj)
	{
		return new BossDeathFxAnimation(this.spineView, this.container, aPlayerWin_obj, this.direction, this.name);
	}

	onDeathFxAnimationCompleted()
	{
		this._deathInProgress = false;
		this._deathOutroAnimationStarted = false;
		this.emit(Enemy.EVENT_ON_DEATH_ANIMATION_COMPLETED);
		this.destroy();
	}

	_onTimeToExplodeCoins(event)
	{
		this.emit(Enemy.EVENT_ON_TIME_TO_EXPLODE_COINS, {position: this.getGlobalPosition(), isCoPlayerWin: event.isCoPlayerWin});
	}

	onDeathFxOutroStarted()
	{
		this.showDeathOutroFx();
	}

	onBossDeathFxOutroStarted()
	{
		this.showDeathOutroFx();
	}

	showDeathOutroFx()
	{
		this.spineView && this.spineView.destroy();
		this.shadow && this.shadow.destroy();

		this._deathOutroAnimationStarted = true;

		try
		{
			this.emit(Enemy.EVENT_ON_DEATH_ANIMATION_OUTRO_STARTED, {position: this.getGlobalPosition()});
		}
		catch (err)
		{

		}
	}

	getHitRectangle()
	{
		let rect = new PIXI.Rectangle();
		rect.width = this.isBoss ? 20 : 10;
		rect.height = Math.abs(this.getCurrentFootPointPosition().y) / (this.isBoss ? 1.2 : 2);
		return rect;
	}

	updateSpineAnimation()
	{
		//override
	}

	updateMembersPosition()
	{
		//override
	}

	_playHitHighlightAnimation(aTime_num, aOptIntensity_num)
	{
		this._fHitHighlightTimer_t && this._fHitHighlightTimer_t.destructor();
		this._fHitHighlightTimer_t = null;

		this._fHitHighlightIntensitySeq && this._fHitHighlightIntensitySeq.destructor();
		this._fHitHighlightIntensitySeq = null;

		aTime_num = aTime_num || 1 * FRAME_RATE;

		if (this.spineView)
		{
			this.spineView.filters = null;
		}
		if (this.view)
		{
			this.view.filters = null;
		}

		this._hitHighlightInProgress = true;
		aOptIntensity_num = aOptIntensity_num || this._baseGitHighlightFilterIntensity;

		this._fHitHighlightTimer_t = new Timer(()=>{
			this._endHitHighlightAnimation();
		}, aTime_num);

		let lHighlight_seq = [
			{tweens: [{prop: "intensity.value", to: aOptIntensity_num}], ease: Easing.sine.easeIn, duration: aTime_num/2},
			{tweens: [{prop: "intensity.value", to: 0}], ease: Easing.quadratic.easeOut, duration: aTime_num/2}
		];
		this._fHitHighlightIntensitySeq = Sequence.start(this._hitHighlightFilterIntensity, lHighlight_seq);
	}

	_endHitHighlightAnimation()
	{
		this._hitHighlightInProgress = false;
		this._setDefaultFilters()
	}

	_setDefaultFilters()
	{
		if (this.spineView)
		{
			this.spineView.filters = null;
		}
		if (this.view)
		{
			this.view.filters = null;
		}
	}

	get _hitHighlightVertexShader()
	{
		return `
		attribute vec2 aVertexPosition;
		attribute vec2 aTextureCoord;
		uniform mat3 projectionMatrix;
		varying vec2 vTextureCoord;

		void main(void)
		{
			gl_Position = vec4((projectionMatrix * vec3(aVertexPosition, 1.0)).xy, 0.0, 1.0);
			vTextureCoord = aTextureCoord;
		}`;
	}

	get _hitHighlightFragmentShader()
	{
		return `
		varying vec2 vTextureCoord;
		uniform sampler2D uSampler;
		uniform float intensity;

		void main(void)
		{
			vec4 c = texture2D(uSampler, vTextureCoord);

			if (c.a < 0.7)
			{
				return;
			}

			vec4 result;
			float add = intensity;
			result.r = c.r+add;
			result.g = c.g-add*3.0;
			result.b = c.b-add*3.0;
			result.a = c.a;
			gl_FragColor = result;
		}`;
	}

	get _hitHighlightFilter()
	{
		if (!this._fHitHighlightFilter)
		{
			this._fHitHighlightFilter = new PIXI.Filter(this._hitHighlightVertexShader, this._hitHighlightFragmentShader, {intensity: this._intensity});
		}
		return this._fHitHighlightFilter;
	}

	get _baseGitHighlightFilterIntensity()
	{
		return 0.3;
	}

	get _intensity()
	{
		this.intensity = this._hitHighlightFilterIntensity.intensity;
		return this.intensity;
	}

	get _hitHighlightFilterIntensity()
	{
		if (!this._fHitHighlightFilterIntensity)
		{
			this._fHitHighlightFilterIntensity = {intensity: {type: 'f', value: this._baseGitHighlightFilterIntensity}};
		}
		return this._fHitHighlightFilterIntensity;
	}

	_hitHiglightUpdate()
	{
		if (this._hitHighlightInProgress && this.spineView)
		{
			this._hitHighlightFilter.uniforms.intensity = this._hitHighlightFilterIntensity.intensity.value;
			this.spineView.filters = [this._hitHighlightFilter];
		} 
	}

	tick(delta)
	{
		this._validateFreezing();
		this.updateSpineAnimation();
		this.updateOffsets();
		this.updateIndicatorsPosition();
		this.updateMembersPosition();
		this._hitHiglightUpdate();

		if(this._fIsOnSpawnActionRequired_bl)
		{
			this._fIsOnSpawnActionRequired_bl = false;

			if(
				this.trajectory &&
				this.trajectory.points &&
				this.trajectory.points[0] &&
				(APP.gameScreen.currentTime > this.trajectory.points[0].time + 1000)
				|| this.isLasthand
				)
			{
				this._fIsLasthand_bl = false;
				return;
			}

			this.__onSpawn();
		}

		//DEBUG...
		//this._drawColliders();
		//...DEBUG
	}

	//COLLISIONS...
	__generatePreciseCollisionBodyPartsNames()
	{
		return [

			];
	}

	__generateIgnoreCollisionsBodyPartsNames()
	{
		return [
			"shadow"
			];
	}

	__isSimpleCollisionEnemy()
	{
		return false;
	}

	isCollision(aX_num, aY_num)
	{
		if(
			!this.container.visible ||
			!this.spineView ||
			!this.spineView.view ||
			!this.spineView.view.skeleton
			||
				(
					!this.spineView.view._localBoundsRect &&
					!this.spineView.view._bounds
				)
			)
		{
			return false;
		}

		let lSpineView_s = this.spineView.view;
		let scaleCoefficient = this.getScaleCoefficient() * 0.4;
		let lMeshes_obj_arr = lSpineView_s.skeleton.drawOrder;

		let lPoinerX_num = (aX_num - this.position.x - this.container.position.x) / scaleCoefficient;
		let lPoinerY_num = (aY_num - this.position.y - this.container.position.y) / scaleCoefficient;


		//BODY BOUNDS...
		let lSpineViewBounds_obj = lSpineView_s._localBoundsRect || lSpineView_s._bounds;;
		let lBoundsLeftX_num = lSpineViewBounds_obj.left;
		let lBoundsRightX_num = lSpineViewBounds_obj.right;
		let lBoundsTopY_num = lSpineViewBounds_obj.top;
		let lBoundsBottomY_num = lSpineViewBounds_obj.bottom;


		if(
			Math.abs(lBoundsLeftX_num) === Infinity ||
			Math.abs(lBoundsRightX_num) === -Infinity ||
			Math.abs(lBoundsTopY_num) === Infinity ||
			Math.abs(lBoundsBottomY_num) === -Infinity
			)
		{
			this._fIsUndetectableSpineBodyBounds_bl = true;
		}
		//...BODY BOUNDS

		//CHECK IF POINT INSIDE SPINE RECTANGLE BOUNDS...
		if(
			!this._fIsUndetectableSpineBodyBounds_bl
			&&
			(
				lPoinerX_num < lBoundsLeftX_num ||
				lPoinerX_num > lBoundsRightX_num ||
				lPoinerY_num < lBoundsTopY_num ||
				lPoinerY_num > lBoundsBottomY_num
				)
			)
		{
			return false; //not even close, no collision
		}
		//...CHECK IF POINT INSIDE SPINE RECTANGLE BOUNDS

		//CHECK ELIIPSE INSIDE SPINE RECTANGLE BOUNDS IF ONLY SIMPLE CHECK IS REQUIRED...
		if(this.__isSimpleCollisionEnemy())
		{
			let lW_num = (lBoundsRightX_num - lBoundsLeftX_num) / 2;
			let lH_num = (lBoundsBottomY_num - lBoundsTopY_num) / 2;

			return(
					this._isPointInsideEllipse(
						lPoinerX_num,
						lPoinerY_num,
						lBoundsLeftX_num + lW_num,
						lBoundsTopY_num + lH_num,
						lW_num,
						lH_num)
					);
		}
		//...CHECK ELIIPSE INSIDE SPINE RECTANGLE BOUNDS IF ONLY SIMPLE CHECK IS REQUIRED


		//CHECK EACH BODY PART...
		for( let i = 0; i < lMeshes_obj_arr.length; i++ )
		{
			if(!lMeshes_obj_arr[i].currentMesh)
			{
				continue; //no mesh = no collision for this body part
			}

			let lBodyPartName_str = lMeshes_obj_arr[i].currentMeshName;

			if(!this._isCollisionRequired(lBodyPartName_str))
			{
				continue; //body part excluded
			}

			let lVertices_num_arr = lMeshes_obj_arr[i].currentMesh.vertices;
			let lBone_obj = lMeshes_obj_arr[i].bone;

			//CHECK IF IS INSIDE BODY PART BOUNDS...
			let lLeftX_num = 100000;
			let lRightX_num = -100000;
			let lTopY_num = 100000;
			let lBottomY_num = -100000;
			let lFinalVerticeIndex_int = 0;

			for( let j = 0; j < lVertices_num_arr.length; j+=2 )
			{
				let lX_num = lVertices_num_arr[j];
				let lY_num = lVertices_num_arr[j + 1];

				if(lX_num < lLeftX_num)
				{
					lLeftX_num = lX_num;

					if(j > lFinalVerticeIndex_int)
					{
						lFinalVerticeIndex_int = j;
					}
				}
				if(lX_num > lRightX_num)
				{
					lRightX_num = lX_num;

					if(j > lFinalVerticeIndex_int)
					{
						lFinalVerticeIndex_int = j;
					}
				}

				if(lY_num < lTopY_num)
				{
					lTopY_num = lY_num;

					if(j > lFinalVerticeIndex_int)
					{
						lFinalVerticeIndex_int = j;
					}
				}

				if(lY_num > lBottomY_num)
				{
					lBottomY_num = lY_num;

					if(j > lFinalVerticeIndex_int)
					{
						lFinalVerticeIndex_int = j;
					}
				}
			}

			if(
				lPoinerX_num < lLeftX_num ||
				lPoinerX_num > lRightX_num ||
				lPoinerY_num < lTopY_num ||
				lPoinerY_num > lBottomY_num
				)
			{
				continue; //not even inside bounds, skip this body part
			}
			//...CHECK IF IS INSIDE BODY PART BOUNDS

			if(!this._isPreciseCollisionRequired(lBodyPartName_str))
			{
				//CHECK ZONE AS AN ELLIPSE...
				let lWidth_num = (lRightX_num - lLeftX_num);
				let lHeight_num = (lBottomY_num - lTopY_num);
				let lCenterX_num = lLeftX_num + lWidth_num / 2;
				let lCenterY_num = lTopY_num + lHeight_num / 2;


				if(
					this._isPointInsideEllipse(
						lPoinerX_num,
						lPoinerY_num,
						lCenterX_num,
						lCenterY_num,
						lWidth_num / 2,
						lHeight_num / 2)
					)
				{
					return true; //collision found!
				}
				//...CHECK ZONE AS AN ELLIPSE
			}
			else
			{
				//CHECK IF IS INSIDE POLYGON...
				if(this._isPointInsidePolygon(lPoinerX_num, lPoinerY_num, lVertices_num_arr, lFinalVerticeIndex_int))
				{
					return true; //collision found!
				}
				//CHECK IF IS INSIDE POLYGON
			}
		}
		//...CHECK EACH BODY PART

		return false;
	}

	_isPreciseCollisionRequired(aBodyPartName_str)
	{
		let l_str_arr = this._fPreciseCollisionBodyPartsNames_str_arr;

		for( let i = 0; i < l_str_arr.length; i++ )
		{
			if(aBodyPartName_str.includes(l_str_arr[i]))
			{
				return true;
			}
		}

		return false;
	}

	_isCollisionRequired(aBodyPartName_str)
	{
		let l_str_arr = this._fIgnoreCollisionsBodyPartsNames_str_arr;

		for( let i = 0; i < l_str_arr.length; i++ )
		{
			if(aBodyPartName_str.includes(l_str_arr[i]))
			{
				return false;
			}
		}

		return true;
	}

	_isPointInsideEllipse(aX_num, aY_num, aCenterX_num, aCenterY_num, aWidth_num, aHeight_num)
	{
		return ((aX_num - aCenterX_num) ** 2 / aWidth_num ** 2 + (aY_num-aCenterY_num) ** 2 / aHeight_num ** 2) <= 1;
	}

	_isPointInsidePolygon(aX_num, aY_num, aVertices_int_arr, aOptFinalVerticeIndex_int)
	{
		let lIsInside_bl = false;

		let lFinalVerticeIndex_int = aOptFinalVerticeIndex_int === undefined ? aVertices_int_arr.length - 2 : aOptFinalVerticeIndex_int;


		let j = lFinalVerticeIndex_int;

		for (let i = 0; i <= lFinalVerticeIndex_int; i+= 2)
		{
			if(i > 0)
			{
				j = i - 2;
			}

			let lXi_num = aVertices_int_arr[i];
			let lYi_num = aVertices_int_arr[i + 1];

			let lXj_num = aVertices_int_arr[j];
			let lYj_num = aVertices_int_arr[j + 1];

			if (
					(
						(lYi_num > aY_num) !== (lYj_num > aY_num)
					)
					&&
					(
						aX_num < (lXj_num - lXi_num) * (aY_num - lYi_num) / (lYj_num - lYi_num) + lXi_num
					)
				)
			{
				lIsInside_bl = !lIsInside_bl;
			}
		}

		return lIsInside_bl;
	}

	//DEBUG...
	_drawColliders()
	{
		if(
			!this.container.visible ||
			!this.spineView ||
			!this.spineView.view ||
			!this.spineView.view.skeleton
			||
				(
					!this.spineView.view._localBoundsRect &&
					!this.spineView.view._bounds
				)
			)
		{
			return false;
		}

		let lSpineView_s = this.spineView.view;
		let scaleCoefficient = this.getScaleCoefficient() * 0.4;
		let lMeshes_obj_arr = lSpineView_s.skeleton.drawOrder;
		let lContainer = this._debugContainer || (this._debugContainer = new Sprite);

		lContainer.zIndex = 10000000;

		if (!lContainer.parent && APP.gameScreen)
		{
			this.addChild(lContainer);
		}

		lContainer.destroyChildren();

		//BODY BOUNDS...
		let lSpineViewBounds_obj = lSpineView_s._localBoundsRect || lSpineView_s._bounds;
		let lBoundsLeftX_num = lSpineViewBounds_obj.left;
		let lBoundsRightX_num = lSpineViewBounds_obj.right;
		let lBoundsTopY_num = lSpineViewBounds_obj.top;
		let lBoundsBottomY_num = lSpineViewBounds_obj.bottom;

		let g = new PIXI.Graphics();
		g.beginFill(0X0000FF, 0.5);

		g.drawRect(
			(lBoundsLeftX_num) * scaleCoefficient + this.container.position.x,
			(lBoundsTopY_num) * scaleCoefficient + this.container.position.y,
			(lBoundsRightX_num - lBoundsLeftX_num) * scaleCoefficient,
			(lBoundsBottomY_num - lBoundsTopY_num) * scaleCoefficient);
		g.endFill();

		lContainer.addChild(g);
		//...BODY BOUNDS


		//SIMPLE COLLISION MODE...
		if(this.__isSimpleCollisionEnemy())
		{
			let lW_num = (lBoundsRightX_num - lBoundsLeftX_num) / 2 * scaleCoefficient;
			let lH_num = (lBoundsBottomY_num - lBoundsTopY_num) / 2 * scaleCoefficient;

			g.beginFill(0X00FF00, 0.5);
			g.drawEllipse(
				(lBoundsLeftX_num) * scaleCoefficient + lW_num + this.container.position.x,
				(lBoundsTopY_num) * scaleCoefficient + lH_num + this.container.position.y,
				lW_num,
				lH_num)
			g.endFill();

			return;
		}
		//...SIMPLE COLLISION MODE


		//BODY PARTS...
		for( let i = 0; i < lMeshes_obj_arr.length; i++ )
		{
			if(!lMeshes_obj_arr[i].currentMesh)
			{
				continue;
			}
			let lName_str = lMeshes_obj_arr[i].currentMeshName;

			if(!this._isCollisionRequired(lName_str))
			{
				continue;
			}

			let lVertices_num_arr = lMeshes_obj_arr[i].currentMesh.vertices;

			//BODY PART BOUNDS...
			let lLeftX_num = 100000;
			let lRightX_num = -100000;
			let lTopY_num = 100000;
			let lBottomY_num = -100000;


			for( let j = 0; j < lVertices_num_arr.length; j+=2 )
			{
				let lX_num = lVertices_num_arr[j];
				let lY_num = lVertices_num_arr[j + 1];

				if(lX_num < lLeftX_num) lLeftX_num = lX_num;
				if(lX_num > lRightX_num) lRightX_num = lX_num;
				if(lY_num < lTopY_num) lTopY_num = lY_num;
				if(lY_num > lBottomY_num) lBottomY_num = lY_num;
			}
			//...BODY PART BOUNDS


			if(this._isPreciseCollisionRequired(lName_str))
			{

				//BODY PART POLYGON...
				g.beginFill(0x00FF00, 1);
				let ver = [];



				for( let j = 0; j < lVertices_num_arr.length; j+=2 )
				{

					let lX_num = (lVertices_num_arr[j]) * scaleCoefficient + this.container.position.x;
					let lY_num = (lVertices_num_arr[j + 1]) * scaleCoefficient + this.container.position.y;


					ver.push(lX_num, lY_num);
				}

				g.drawPolygon(ver);
				g.endFill();
				//...BODY PART POLYGON
			}
			else
			{
				//BODY PART BOUNDS...
				let lW_num = (lRightX_num - lLeftX_num) * scaleCoefficient * 0.5;
				let lH_num = (lBottomY_num - lTopY_num) * scaleCoefficient * 0.5

				g.beginFill(0X00FF00, 0.35);
				g.drawEllipse(
					(lLeftX_num) * scaleCoefficient + lW_num + this.container.position.x,
					(lTopY_num) * scaleCoefficient + lH_num + this.container.position.y,
					lW_num,
					lH_num)
				g.endFill();
				//...BODY PART BOUNDS
			}
		}
		//...BODY PARTS
	}
	//...DEBUG
	//...COLLISIONS

	__onSpawn()
	{

	}

	_isInTimeOfOwnTrajectrory()
	{
		return(
			this.trajectory &&
			this.trajectory.points &&
			APP.gameScreen.currentTime - this.trajectory.points[0].time >= 0);
	}


	_validateFreezing()
	{
		if (this.isFrozen && this.state == STATE_STAY)
		{
			if (this._fLastPosition_obj)
			{
				if (
					(this._fLastPosition_obj.x !== this.position.x) &&
					(this._fLastPosition_obj.y !== this.position.y) &&
					this._isInTimeOfOwnTrajectrory()
					)
				{
					this._unfreeze();
				}
			}
			this._fLastPosition_obj = {x: this.position.x, y: this.position.y};
		}
	}

	getPositionOffsetX()
	{
		return 0;
	}

	getPositionOffsetY()
	{
		return 0;
	}

	updateOffsets()
	{
		this.enemyIndicatorsController.view.pivot.set(0, 0);

		let lOffset_obj = this._getOffset();

		this.container.position.set(lOffset_obj.x, lOffset_obj.y);
	}

	_getOffset()
	{
		let dx = 0, dy = 0;

		if (this.currentHitBounce !== null)
		{
			dx += this.currentHitBounce.x;
			dy += this.currentHitBounce.y;
		}

		if (this.currentBombBounce !== null)
		{
			dx += this.currentBombBounce.x;
			dy += this.currentBombBounce.y;
		}

		if (this.isVibrating_bl)
		{
			dx += 2.5 - Utils.random(0, 5);
			dy += 2.5 - Utils.random(0, 5);
		}

		if (this.bossAppearanceDelta !== null)
		{
			dx += this.bossAppearanceDelta.x;
			dy += this.bossAppearanceDelta.y;
		}

		return {x: dx, y: dy};
	}

	//CRYOGUNS...
	_onCryogunsFreezeEnemy(aEvent_obj)
	{
		let lEnemyId_int = aEvent_obj.enemyId;
		let lIsAnimated_bl = aEvent_obj.isAnimated;
		if (this.id === lEnemyId_int)
		{
			this._freezeIfRequired(lIsAnimated_bl);
		}
	}

	_onCryogunsUnfreezeEnemy(aEvent_obj)
	{
		let lEnemyId_int = aEvent_obj.enemyId;
		if (this.id === lEnemyId_int)
		{
			this._unfreezeIfRequired();
		}
	}
	//...CRYOGUNS

	destroy(purely = false)
	{
		//INDICATORS...
		this.removeChild(this._fEnemyIndicatorsView_eiv);
		this._fEnemyIndicatorsView_eiv = null;

		if (this._fEnemyIndicatorsController_eic)
		{
			this._fEnemyIndicatorsController_eic.off(EnemyIndicatorsController.EVENT_ON_ENERGY_UPDATED, this._onEnergyUpdated, this);
			this._fEnemyIndicatorsController_eic.destroy();
		}
		this._destroyBossWalkTimer();

		this._fEnemyIndicatorsController_eic = null;
		//...INDICATORS

		this._fHitHighlightTimer_t && this._fHitHighlightTimer_t.destructor();
		this._fHitHighlightTimer_t = null;

		this._fHitHighlightIntensitySeq && this._fHitHighlightIntensitySeq.destructor();
		this._fHitHighlightIntensitySeq = null;

		Sequence.destroy(Sequence.findByTarget(this.currentHitBounceDelta));
		Sequence.destroy(Sequence.findByTarget(this.currentBombBounceDelta));

		this.emit(Enemy.EVENT_ON_ENEMY_DESTROY, {id: this.id});
		if (this._destroyed)
		{
			return;
		}

		/*APP.off("tick", this._tickFunc);*/
		if (!purely)
		{
			let lEnemyPosition_pt = this.getGlobalPosition();
			lEnemyPosition_pt.x += this.getCurrentFootPointPosition().x;
			lEnemyPosition_pt.y += this.getCurrentFootPointPosition().y;

			this.emit(Enemy.EVENT_ON_ENEMY_VIEW_REMOVING, {position: lEnemyPosition_pt, angle: this.angle});
		}

		this.resetBossAppearance();

		this.container && Sequence.destroy(Sequence.findByTarget(this.container));
		if (this.currentBombBounceDelta)
		{
			this.currentBombBounceDelta.sequence && this.currentBombBounceDelta.sequence.destructor();
			this.currentBombBounceDelta.sequence = null;
			this.currentBombBounceDelta = null;
		}

		if (this.shadow)
		{
			Sequence.destroy(Sequence.findByTarget(this.shadow));
			this.shadow.view && Sequence.destroy(Sequence.findByTarget(this.shadow.view));
		}

		this.name = undefined;
		this.initialDirection = null;

		this.container = null;

		this.id = undefined;
		this.typeId = undefined;
		this.imageName = undefined;
		this.radius = undefined;
		this.life = undefined;
		this.isEnded = false;
		this.angle = undefined;
		this.direction = undefined;
		this.turnDirection = undefined;
		this.speed = undefined;
		this.currentTrajectorySpeed = undefined;
		this._lastPointTimeInterval = undefined;
		this._fLastPosition_obj = null;
		this.swarmType = null;
		this._fDeathReason_num = null;

		this.awardedPrizes = null;
		this.prizes = undefined;

		this.boss = false;
		this.skin = undefined;

		this.state = null;
		this.spineSpeed = undefined;

		this.trajectory = null;

		this.hitBounceDelta = null;
		this.bombBounceDelta = null;
		this.currentHitBounceDelta = null;

		this._deathInProgress = false;
		this._deathOutroAnimationStarted = false;
		this.deathFxAnimation = null;
		this._fBonesFellDown_bl = false;

		this._vibrateTimer && this._vibrateTimer.destructor();
		this._vibrateTimer = null;

		this._fIsFireDenied_bl = false;
		this._fIsTrajectoryPositionChangeInitiated_bl = false;

		this.view = null;
		this.viewPos = null;
		this.spineViewPos = null;
		this.shadow = null;
		this.footPoint = null;

		this._fBossHitScreamTimer_t && this._fBossHitScreamTimer_t.destructor();
		this._fBossHitScreamTimer_t = null;

		if (this.spineView)
		{
			if (this.spineView.view && this.spineView.view.state)
			{
				this.spineView.view.state.onComplete = null;
				if (this.spineView.view.state.tracks && this.spineView.view.state.tracks[0])
				{
					this.spineView.view.state.tracks[0].onComplete = null;
				}
			}
		}

		Sequence.destroy(Sequence.findByTarget(this.spineView));
		this.spineView = null;
		this._fCurSpineName_str = undefined;

		this.stepTimers = null;
		this._stepsAmount = undefined;

		this.hvTopContainer = null;
		this.hvBottomContainer = null;

		this.hvAppearingEffects && this.hvAppearingEffects.destroy();
		this.hvAppearingEffects = null;

		this._fFreezAfterLasthand_bl = null;

		this._destroyRisingSequence();

		this._fChildHvEnemyId_num = undefined;
		this.isVibrating_bl = false;

		this.removeInstaMark();

		this._fBossAppearanceInProgress_bln = null;

		this._fCryogunsController_cgs.off(CryogunsController.EVENT_FREEZE_ENEMY, this._onCryogunsFreezeEnemy, this);
		this._fCryogunsController_cgs.off(CryogunsController.EVENT_UNFREEZE_ENEMY, this._onCryogunsUnfreezeEnemy, this);
		this._fIsFrozen_bl = false;
		this._destroyFrozenSprites();

		this._fGameDebuggingController_gdc && this._fGameDebuggingController_gdc.off(GameDebuggingController.i_EVENT_TINT_UPDATE, this._onDebugTintUpdated, this);
		this._fGameDebuggingController_gdc && this._fGameDebuggingController_gdc.off(GameDebuggingController.i_EVENT_SHADOW_TINT_UPDATE, this._onDebugShadowTintUpdated, this);
		this._fGameDebuggingController_gdc = null;

		this._fIsImpactAnimationInProgress_bl = undefined;
		this._fPauseWalkingTimeMarker_num = undefined;

		this._fPrevUpdateAccurateTime_num = undefined;
		this._curAnimationState = undefined;

		this._gameScreen = null;
		this._gameField = null;

		this.params = null;

		this._hitHighlightInProgress = undefined;
		super.destroy();
	}

	get isDestroyed()
	{
		return this._destroyed;
	}

	get isWasp()
	{
		return (this.typeId === ENEMY_TYPES.WASP);
	}

	isRedTargetMarkerRejected()
	{
		return false;
	}

	getHitBounceDistance(weaponId)
	{
		switch (weaponId)
		{
			case WEAPONS.DEFAULT:
			case WEAPONS.HIGH_LEVEL:
				return 10;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				return 25;
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				return 30;
		}
		return 0;
	}

	getHitBounceMultiplier()
	{
		let mult = 1;
		return mult * Math.sqrt(Math.max(this.speed/4, 1));
	}

	startVibration(aHitDuration_num)
	{
		this.isVibrating_bl = true;

		if (this._vibrateTimer)
		{
			this._vibrateTimer && this._vibrateTimer.destructor();
		}

		this._vibrateTimer = new Timer(this.resetVibration.bind(this), aHitDuration_num);
	}

	resetVibration()
	{
		this.isVibrating_bl = false;
	}

	getCenterPosition()
	{
		let pos = {x: 0, y: 0};
		pos.x = this.getGlobalPosition().x + this.getLocalCenterOffset().x + this._getOffset().x;
		pos.y = this.getGlobalPosition().y + this.getLocalCenterOffset().y + this._getOffset().y;
		return pos;
	}

	getLocalCenterOffset()
	{
		let pos = {x: 0, y: 0};
		return pos;
	}

	toString()
	{
		return `Enemy {id: ${this.id}, typeId: ${this.typeId}, name: ${this.name}}`;
	}
}

export default Enemy;