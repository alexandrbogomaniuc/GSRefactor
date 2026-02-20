import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import GameOptimizationInfo from '../../model/custom/GameOptimizationInfo';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameField from '../../main/GameField';
import GameScreen from '../../main/GameScreen';
import AwardingController from '../uis/awarding/AwardingController';
import FlameThrowersController from '../uis/weapons/flamethrower/FlameThrowersController';

class GameOptimizationController extends SimpleController {

	constructor()
	{
		super(new GameOptimizationInfo());
	}

	__init()
	{
		super.__init();

		this._gameScreen = APP.currentWindow;
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);
		this._gameScreen.once(GameScreen.EVENT_ON_READY, this._onGameScreenReady, this);	
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);
	}

	_onGameScreenReady()
	{
		this._gameScreen.gameField.on(GameField.EVENT_ON_BULLET_FLY_TIME, this._onBulletFlyTime, this);
		this._gameScreen.gameField.on(GameField.EVENT_ON_BULLET_TARGET_TIME, this._onBulletTargetTime, this);

		this._gameScreen.gameField.on(GameField.EVENT_ON_CEILING_DUST_ANIMATION_STARTED, this._onCeilingDustAnimationStarted, this);
		this._gameScreen.gameField.on(GameField.EVENT_ON_CEILING_DUST_ANIMATION_COMPLETED, this._onCeilingDustAnimationCompleted, this);

		this._gameScreen.gameField.on('grenadeExplosionAnimationStarted', this._onGrenadeExplosionAnimationStarted, this);
		this._gameScreen.gameField.on(GameField.EVENT_ON_GRENADE_EXPLOSION_ANIMATION_COMPLETED, this._onGrenadeExplosionAnimationCompleted, this);

		let flameThrowersController = APP.currentWindow.flameThrowersController;
		flameThrowersController.on(FlameThrowersController.EVENT_ON_HIT_ANIMATION_STARTED, this._onFlameThrowerHitAnimationStarted, this);
		flameThrowersController.on(FlameThrowersController.EVENT_ON_HIT_ANIMATION_COMPLETED, this._onFlameThrowerHitAnimationCompleted, this);

		let awardingController = APP.currentWindow.awardingController;
		awardingController.on(AwardingController.EVENT_ON_AWARD_ANIMATION_STARTED, this._onAwardAnimationStarted, this);
		awardingController.on(AwardingController.EVENT_ON_AWARD_ANIMATION_INTERRUPTED, this._onAwardAnimationInterrupted, this);
		awardingController.on(AwardingController.EVENT_ON_AWARD_ANIMATION_COMPLETED, this._onAwardAnimationCompleted, this);
		awardingController.on(AwardingController.EVENT_ON_ALL_ANIMATIONS_COMPLETED, this._onAwardAllAnimationsCompleted, this);
	}

	_onRoomPaused()
	{
		this._clearAll();
	}

	_onRoomFieldCleared()
	{
		this._clearAll();
	}

	_onCeilingDustAnimationStarted()
	{
		this.info.i_increaseCeilingDustAnimationsNumber();
	}

	_onCeilingDustAnimationCompleted()
	{
		this.info.i_decreaseCeilingDustAnimationsNumber();
	}

	_onGrenadeExplosionAnimationStarted()
	{
		this.info.i_increaseGrenadeExplosionsNumber();
	}

	_onGrenadeExplosionAnimationCompleted()
	{
		this.info.i_decreaseGrenadeExplosionsNumber();
	}

	_onFlameThrowerHitAnimationStarted()
	{
		this.info.i_increaseFlameThrowerHitExplosionsNumber();
	}

	_onFlameThrowerHitAnimationCompleted()
	{
		this.info.i_decreaseFlameThrowerHitExplosionsNumber();
	}

	_clearAll()
	{
		this.info.i_clearAll();
	}

	//bullets...
	_onBulletFlyTime()
	{
		this.info.i_increaseCurrentShotsAmount();
	}

	_onBulletTargetTime()
	{
		this.info.i_decreaseCurrentShotsAmount();
	}
	//...bullets

	//awards...
	_onAwardAnimationStarted()
	{
		this.info.i_increaseCurrentAwardsAmount();
	}

	_onAwardAnimationInterrupted()
	{
		this.info.i_decreaseCurrentAwardsAmount();
	}

	_onAwardAnimationCompleted()
	{
		this.info.i_decreaseCurrentAwardsAmount();
	}

	_onAwardAllAnimationsCompleted()
	{
		this.info.i_resetCurrentAwardsAmount();
	}
	//...awards
}

export default GameOptimizationController;