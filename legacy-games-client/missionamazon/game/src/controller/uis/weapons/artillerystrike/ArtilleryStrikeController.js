import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import ArtilleryStrikeInfo from '../../../../model/uis/weapons/artillerystrike/ArtilleryStrikeInfo';
import ArtilleryStrikeView from '../../../../view/uis/weapons/artillerystrike/ArtilleryStrikeView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import ShotResultsUtil from '../../../../main/ShotResultsUtil';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import GameSoundsController from '../../../sounds/GameSoundsController';
import SimpleSoundController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/sounds/SimpleSoundController';
import { isPointInsideRect } from '../../../../model/collisions/CollisionTools';

const MAX_MISSILES_AMOUNT = 15;

class ArtilleryStrikeController extends SimpleUIController
{
	static get EVENT_ON_MAIN_STRIKE_ANIMATION_COMPLETED() { return 'EVENT_ON_MAIN_STRIKE_ANIMATION_COMPLETED'; }
	static get EVENT_ON_ALL_MAIN_MISSILES_HIT() { return 'EVENT_ON_ALL_MAIN_MISSILES_HIT'; }
	static get EVENT_ON_FULL_ANIMATION_COMPLETED() { return ArtilleryStrikeView.EVENT_ON_FULL_ANIMATION_COMPLETED; }
	static get EVENT_ON_STRIKE_MISSILE_HIT() { return ArtilleryStrikeView.EVENT_ON_STRIKE_MISSILE_HIT; }
	static get EVENT_ON_ARTILLERY_GRENADE_LANDED() { return ArtilleryStrikeView.EVENT_ON_ARTILLERY_GRENADE_LANDED; }

	constructor(data, callback)
	{
		super(new ArtilleryStrikeInfo(data), new ArtilleryStrikeView());

		this._fShotCallback_func = callback;

		this._gameScreen = null;
	}

	__init()
	{
		super.__init();

		this._gameScreen = APP.currentWindow;

		this._fMapsController_msc = APP.currentWindow.mapsController;
		this._fMapsInfo_msi = this._fMapsController_msc.info;

		this._fHitMissilesCounter_int = 0;
		this._fArtilleryStrikesFlyingSound_snd = null;

		this._startFire();
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		this.view.once(ArtilleryStrikeView.EVENT_ON_ARTILLERY_GRENADE_LANDED, this._onArtilleryGrenadeLanded, this);
		this.view.once(ArtilleryStrikeView.EVENT_ON_TIME_TO_START_ARTILLERY_STRIKES, this._onTimeToStartArtilleryStrikes, this);
		this.view.once(ArtilleryStrikeView.EVENT_ON_ALL_MISSILES_COMPLETED, this._onAllMissilesCompleted, this);
		this.view.once(ArtilleryStrikeView.EVENT_ON_FULL_ANIMATION_COMPLETED, this._onFullAnimationCompleted, this);
		this.view.on(ArtilleryStrikeView.EVENT_ON_STRIKE_MISSILE_HIT, this._onSomeStrikeMissileHit, this);
	}

	_startFire()
	{
		let startPos = this._gameScreen.gameField.getGunPosition(this.info.seatId);
		let endPos = new PIXI.Point(this.info.origX, this.info.origY);
		let middlePos = null;

		if (this.info.rid !== -1)
		{
			//my shot
			let targetEnemyId = this.info.requestEnemyId;
			let enemyPos = this._gameScreen.gameField.getEnemyPosition(targetEnemyId, true /*foot position*/);

			let mapZonePoints = this._fMapsInfo_msi.currentMapWalkingZone;
			let mapZonePolygon = mapZonePoints ? new PIXI.Polygon(mapZonePoints) : null;

			if (mapZonePolygon && !Utils.isPointInsidePolygon(endPos, mapZonePolygon))
			{
				middlePos = new PIXI.Point(endPos.x, endPos.y);
				endPos = enemyPos;
			}
		}
		else
		{
			//opponent's shot
			let targetEnemyId = ShotResultsUtil.getFirstNonFakeEnemy(this.info.shotData);
			let lastPos = this._gameScreen.gameField.getEnemyPosition(targetEnemyId, true /*foot position*/);

			if (lastPos)
			{
				endPos = lastPos;
			}
		}

		const lWeaponId_num = this.info.shotData.usedSpecialWeapon;
		this.view.i_throwArtilleryGrenade(lWeaponId_num, startPos, endPos, middlePos);
	}

	_onArtilleryGrenadeLanded(aEvent_obj)
	{
		this.emit(ArtilleryStrikeController.EVENT_ON_ARTILLERY_GRENADE_LANDED);
		if (this.info.rid !== -1)
		{
			this.emit(ArtilleryStrikeController.EVENT_ON_MAIN_STRIKE_ANIMATION_COMPLETED);
		}
		this._playSmokeSound();
	}

	_onSomeStrikeMissileHit(aEvent_obj)
	{
		let isFirst = ++this._fHitMissilesCounter_int === 1;
		let lEvent_obj = Object.assign({ isFirst: isFirst }, aEvent_obj);

		if (isFirst)
		{
			APP.gameScreen.gameField.shakeTheGround("artillery");
		}

		this.emit(ArtilleryStrikeController.EVENT_ON_STRIKE_MISSILE_HIT, lEvent_obj);
	}

	//SOUNDS...
	_playSmokeSound()
	{
		this._playSound('bomb_smoking');
	}

	_playArtilleryStrikesFlyingSound()
	{
		let lArtilleryStrikesFlyingSound_snd = this._fArtilleryStrikesFlyingSound_snd = this._playSound('artillery_strikes_flying');
		lArtilleryStrikesFlyingSound_snd && lArtilleryStrikesFlyingSound_snd.once(SimpleSoundController.i_EVENT_SOUND_DESTROYING, this._onArtilleryStrikesFlyingSoundDestroying, this);
	}

	_onArtilleryStrikesFlyingSoundDestroying(aEvent_obj)
	{
		this._fArtilleryStrikesFlyingSound_snd = null;
	}

	_destroyArtilleryStrikesFlyingSound()
	{
		if (this._fArtilleryStrikesFlyingSound_snd)
		{
			this._fArtilleryStrikesFlyingSound_snd.off(SimpleSoundController.i_EVENT_SOUND_DESTROYING, this._onArtilleryStrikesFlyingSoundDestroying, this, true);
			this._fArtilleryStrikesFlyingSound_snd.i_destroy();
			this._fArtilleryStrikesFlyingSound_snd = null;
		}
	}

	_playSound(aSoundName_str)
	{
		let lIsMainPlayerShot_bl = this.info.rid >= 0;
		let lVolume_num = lIsMainPlayerShot_bl ? GameSoundsController.MAIN_PLAYER_VOLUME : GameSoundsController.OPPONENT_WEAPON_VOLUME;
		return APP.soundsController.play(aSoundName_str, false, lVolume_num, !lIsMainPlayerShot_bl);
	}
	//...SOUNDS

	_onTimeToStartArtilleryStrikes(aEvent_obj)
	{
		const lAffectedEnemies_obj_arr = ShotResultsUtil.excludeFakeEnemies(this.info.affectedEnemies);

		lAffectedEnemies_obj_arr.sort(a => {
			if (a.data.class == "Hit")
			{
				return -1;
			}
			else
			{
				return 1;
			}
		});

		const lAffectedEnemiesInfo_obj_arr = lAffectedEnemies_obj_arr.map(function (obj)
		{
			return {id: obj.enemyId, class: obj.data.class};
		});

		const lAffectedEnemiesIds_obj_arr = this._defineMissilesTargetEnemies(lAffectedEnemiesInfo_obj_arr);

		this.view.i_startArtilleryMissiles(lAffectedEnemiesIds_obj_arr);
		this._playArtilleryStrikesFlyingSound();
	}

	_defineMissilesTargetEnemies(lAffectedEnemiesInfo_obj_arr)
	{
		const lHits_obj_arr = lAffectedEnemiesInfo_obj_arr.filter(info => info.class == "Hit");

		let lMissilesTargetEnemies_int_arr = [];
		let lScreenBlockWidth_num = APP.config.size.width / MAX_MISSILES_AMOUNT;
		let lDuplicateEnemyId_int = undefined;

		for (let i = 0; i < MAX_MISSILES_AMOUNT; i++)
		{
			let lBlockRect = new PIXI.Rectangle(i * lScreenBlockWidth_num, 0, lScreenBlockWidth_num, APP.config.size.height);

			for (let j = 0; j < lAffectedEnemiesInfo_obj_arr.length; j++)
			{
				let lAffectedEnemy_obj = lAffectedEnemiesInfo_obj_arr[j];
				let lAffectedEnemyPosition_p = this._gameScreen.gameField.getEnemyPosition(lAffectedEnemy_obj.id, true /*feet pos*/);
				if (isNaN(lDuplicateEnemyId_int))
				{
					lDuplicateEnemyId_int = lAffectedEnemy_obj.id;
				}

				if (lAffectedEnemyPosition_p && isPointInsideRect(lBlockRect, lAffectedEnemyPosition_p) || lAffectedEnemy_obj.class == "Hit")
				{
					lMissilesTargetEnemies_int_arr[i] = lAffectedEnemy_obj.id;

					lAffectedEnemiesInfo_obj_arr.splice(j, 1);
					break;
				}
			}
		}

		const lAdditionlHitMissiles_int_arr = [];
		for (let i = 0; i < lMissilesTargetEnemies_int_arr.length; i++)
		{
			for (let j = 0; j < lHits_obj_arr.length; j++)
			{
				if (lMissilesTargetEnemies_int_arr[i] == lHits_obj_arr[j].id)
				{
					lAdditionlHitMissiles_int_arr.push(lHits_obj_arr[j].id, lHits_obj_arr[j].id);
				}
			}
		}
		lMissilesTargetEnemies_int_arr = lAdditionlHitMissiles_int_arr.concat(lMissilesTargetEnemies_int_arr);

		for (let i = 0; lMissilesTargetEnemies_int_arr.length > MAX_MISSILES_AMOUNT; i++)
		{
			lMissilesTargetEnemies_int_arr.pop();
		}

		for (let i = 0; i < MAX_MISSILES_AMOUNT; i++)
		{
			if (isNaN(lMissilesTargetEnemies_int_arr[i]))
			{
				if (lAffectedEnemiesInfo_obj_arr.length > 0)
				{
					lMissilesTargetEnemies_int_arr[i] = lAffectedEnemiesInfo_obj_arr.pop().id;
					continue;
				}
			}
		}

		for (let i = 0; i < MAX_MISSILES_AMOUNT; i++)
		{
			if (isNaN(lMissilesTargetEnemies_int_arr[i]))
			{
				if (isNaN(lDuplicateEnemyId_int))
				{
					throw new Error(`Cannot determine missile's target enemy id.`);
				}
				else
				{
					lMissilesTargetEnemies_int_arr[i] = lDuplicateEnemyId_int;
				}
			}
		}

		return lMissilesTargetEnemies_int_arr;
	}

	_onAllMissilesCompleted()
	{
		//stop sound of flying
		this._destroyArtilleryStrikesFlyingSound();
		if (this.info.rid !== -1)
		{
			this.emit(ArtilleryStrikeController.EVENT_ON_ALL_MAIN_MISSILES_HIT);
		}
		this._fShotCallback_func.call(null);
	}

	_onFullAnimationCompleted()
	{
		this.emit(ArtilleryStrikeController.EVENT_ON_FULL_ANIMATION_COMPLETED);
	}

	destroy()
	{
		this.removeAllListeners();
		this._destroyArtilleryStrikesFlyingSound();

		super.destroy();
	}
}

export default ArtilleryStrikeController;