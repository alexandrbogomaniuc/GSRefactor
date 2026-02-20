import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameField from '../../main/GameField';
import RicochetInfo from '../../model/custom/RicochetInfo';
import RicochetBullet from './../../main/bullets/RicochetBullet';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import GameScreen from './../../main/GameScreen';
import { WEAPONS } from '../../../../shared/src/CommonConstants';

class RicochetController extends SimpleController
{
	static get EVENT_ON_RICOCHET_BULLETS_UPDATED()					{return "onRicochetBulletsUpdated";}
	static get EVENT_ON_RICOCHET_BULLETS_PAYBACK_REQUIRED()			{return "onRicochetBulletsPaybackRequired";}

	constructor()
	{
		super(new RicochetInfo());

		this._fAvailableSpace_obj = null;
		this._fGameStateInfo_gsi = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.gameScreen.gameField.on(GameField.EVENT_ON_RICOCHET_BULLET_FLY_OUT, this._onBulletFlyOut, this);
		APP.gameScreen.gameField.on(GameField.EVENT_ON_CLEAR_ROOM_STARTED, this._onRoomClear, this);
		APP.gameScreen.gameField.on(GameField.EVENT_ON_CLEAR_BULLETS_BY_SEAT_ID, this._onClearBulletsBySeatId, this);
		APP.gameScreen.gameField.on(GameField.EVENT_ON_MASTER_SEAT_ADDED, this._onMasterSeatAdded, this);
		APP.gameScreen.gameField.on(GameField.EVENT_ON_BULLET_PLACE_NOT_ALLOWED, this._onBulletPlaceNotAllowed, this);

		APP.gameScreen.on(GameScreen.EVENT_ON_CO_PLAYER_COLLISION_OCCURED, this._onCoPlayerCollisionOccured, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_PLAYER_ADDED, this._onPlayerSitIn, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_PLAYER_REMOVED, this._onPlayerSitOut, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_BET_LEVEL_CHANGE_CONFIRMED, this._onBetLevelChangeConfirmed, this);
	}

	__initModelLevel()
	{
		let masterSeatId = APP.gameScreen.gameField.seatId
		let limit = APP.playerController.info.bulletsLimit;
		this.info.masterSeatId = masterSeatId;
		this.info.bulletsLimit = limit;
	}

	_onMasterSeatAdded(event)
	{
		this.info.masterSeatId = event.seatId;
	}

	_onBulletFlyOut(e)
	{
		let bullet = e.bullet;
		this.info.addBullet(bullet);
		this._onPlayerRicochetBulletsUpdated();
	}

	onBulletDestroy(aBullet_rb)
	{
		this.info.removeBullet(aBullet_rb);
		this._onPlayerRicochetBulletsUpdated();
	}


	_onBulletPlaceNotAllowed(e)
	{
		let bulletId = e.bulletId;
		let lBullet_rb = this.info.getBulletByBulletId(bulletId);
		if (lBullet_rb)
		{
			lBullet_rb.setIsRequired(false);
			lBullet_rb.setDeniedByServer();
			lBullet_rb.disableRicochet();
		}

	}

	_onRoomClear()
	{
		this._backAmmoForCurrentBullets();

		this.info.removeAllBullets();

		this._onPlayerRicochetBulletsUpdated();
	}

	_onClearBulletsBySeatId(event)
	{
		let seatId = event.seatId;

		if (seatId === this.info.masterSeatId)
		{
			this._backAmmoForCurrentBullets();
		}

		let removedBullets = this.info.removeBulletsBySeatId(seatId);
		for (let bullet of removedBullets)
		{
			bullet.disableRicochet();
		}

		if (seatId === this.info.masterSeatId)
		{
			this._onPlayerRicochetBulletsUpdated();
		}
	}

	onBulletMove(aBullet_rb)
	{
		let bullet = aBullet_rb;
		let bulletPos = aBullet_rb.position;

		let pointsToCheck = bullet.localWallsCollisionPoints;
		let changeDirAllowed = bullet.isChangeDirectionAllowed;

		for (let point of pointsToCheck)
		{
			let pointToCheck = new PIXI.Point(bulletPos.x + point.x, bulletPos.y + point.y);
			let startPoint = new PIXI.Point(bullet.startPos.x + point.x, bullet.startPos.y + point.y);
			let newPointToMove = this._checkForCollision(pointToCheck, startPoint, changeDirAllowed);
			if (newPointToMove)
			{
				if (changeDirAllowed)
				{
					newPointToMove.x -= point.x;
					newPointToMove.y -= point.y;
					if (newPointToMove.intersectPoint)
					{
						newPointToMove.intersectPoint.x -= point.x;
						newPointToMove.intersectPoint.y -= point.y;
					}
					this._changeBulletDirection(bullet, newPointToMove);
				}
				else
				{
					bullet.setIsRequired(false); // Destroy bullet if new direction not needed and it run out of bounds
				}

				return;
			}
		}
	}

	_changeBulletDirection(bullet, newPointToMove)
	{
		if (newPointToMove.intersectPoint)
		{
			bullet.position.set(newPointToMove.intersectPoint.x, newPointToMove.intersectPoint.y);
		}

		bullet.startBulletFly({x: bullet.x, y: bullet.y}, newPointToMove);
	}

	_checkForCollision(pointToCheck, startPoint, changeDirectionAllowed = true)
	{
		let sizes = this._availableSpace;
		let newPointToMove = null;
		let hitPoints = null;

		if (pointToCheck.x > sizes.right) hitPoints = [new PIXI.Point(sizes.right, sizes.bottom), new PIXI.Point(sizes.right, sizes.top)];
		else if (pointToCheck.x < sizes.left) hitPoints = [new PIXI.Point(sizes.left, sizes.top), new PIXI.Point(sizes.left, sizes.bottom)];
		else if (pointToCheck.y < sizes.top) hitPoints = [new PIXI.Point(sizes.right, sizes.top), new PIXI.Point(sizes.left, sizes.top)];
		else if (pointToCheck.y > sizes.bottom) hitPoints = [new PIXI.Point(sizes.left, sizes.bottom), new PIXI.Point(sizes.right, sizes.bottom)];

		if (hitPoints)
		{
			if (changeDirectionAllowed) // If false, do not search for new direction
			{
				newPointToMove = this._getNewPointToMoveFromCollision(startPoint, pointToCheck, hitPoints);
				return newPointToMove;
			}
			else
			{
				return true;
			}
		}

		return false;
	}

	// If collision occured, find crossed line and new bullet direction
	_getNewPointToMoveFromCollision(startPoint, currentPoint, hitPoints)
	{
		let p1 = hitPoints[0];
		let p2 = hitPoints[1];

		if (p1 && p2)
		{
			// Debug wall highlight...
			// let mapContainer = APP.gameScreen.gameField.screen;
			// let hitLine = mapContainer.addChild(new PIXI.Graphics());
			// hitLine.lineStyle(2, 0xff0000, 0.7).drawPolygon([p1, p2]);
			// let tId = setInterval(()=>{if (hitLine.alpha <= 0) clearTimeout(tId); else hitLine.alpha -= 0.03}, 30);
			// ...Debug wall highlight

			let intersectPoint = Utils.getIntersectionBetweenTwoLines(p1, p2, startPoint, currentPoint) || {x: 0, y: 0}; // Need for shifting point from current position outside of bounds
			currentPoint = {x: (intersectPoint.x), y: (intersectPoint.y)};

			let a1 = Math.atan2(p2.y - p1.y, p2.x - p1.x);
			let a2 = Math.atan2(currentPoint.y - startPoint.y, currentPoint.x - startPoint.x);
			let a = a1 * 2 - a2; // Calculate angle between bullet trajectory and crossed line
			while (a < 0) a+= Math.PI*2;
			while (a >= Math.PI*2) a-= Math.PI*2;
			a = +(a.toFixed(5)); // Round angle to closest integer value
			intersectPoint.x = +(intersectPoint.x.toFixed(1));
			intersectPoint.y = +(intersectPoint.y.toFixed(1));
			let dist = Utils.getDistance(startPoint, currentPoint);
			let newPoint = {x: currentPoint.x + Math.cos(a)*dist, y: currentPoint.y + Math.sin(a)*dist, intersectPoint: intersectPoint};
			newPoint.x = +(newPoint.x.toFixed(1));
			newPoint.y = +(newPoint.y.toFixed(1));
			return this._moveOutOfBoundsIfRequired(newPoint);
		}

		return {x: APP.config.size.width/2, y: APP.config.size.height/2};
	}

	_moveOutOfBoundsIfRequired(point)
	{
		let sizes = this._availableSpace;
		if (point.x >= sizes.right) point.x -= 1;
		if (point.x <= sizes.left) point.x += 1;
		if (point.y <= sizes.top) point.x += 0.56;
		if (point.y >= sizes.bottom) point.x -= 0.56;
		return point;
	}

	get _availableSpace()
	{
		if (!this._fAvailableSpace_obj)
		{
			let globalView = APP.config.size;
			this._fAvailableSpace_obj = {
				left: 0,
				right: globalView.width,
				top: 0 + 16,
				bottom: globalView.height - 11
			}
		}

		return this._fAvailableSpace_obj;
	}

	_onCoPlayerCollisionOccured(event)
	{
		let bulletId = event.bulletId;
		let bullet = this.info.removeBulletByBulletId(bulletId);
		if (bullet)
		{
			bullet.setIsRequired(false);
		}
	}

	_onPlayerSitIn(event)
	{
		let seatId = event.seatId;

		if (seatId === this.info.masterSeatId)
		{
			this._backAmmoForCurrentBullets();
		}

		let removedBullets = this.info.removeBulletsBySeatId(seatId);
		for (let bullet of removedBullets)
		{
			bullet.disableRicochet();
		}

		if (seatId === this.info.masterSeatId)
		{
			this._onPlayerRicochetBulletsUpdated();
		}
	}

	_onPlayerSitOut(event)
	{
		let seatId = event.seatId;

		if (seatId === this.info.masterSeatId)
		{
			this._backAmmoForCurrentBullets();
		}

		let removedBullets = this.info.removeBulletsBySeatId(seatId);
		for (let bullet of removedBullets)
		{
			bullet.disableRicochet();
		}

		if (seatId === this.info.masterSeatId)
		{
			this._onPlayerRicochetBulletsUpdated();
		}
	}

	_backAmmoForCurrentBullets()
	{
		let bulletsCount = this.info.activeMasterBulletsAmount;
		if (
				bulletsCount > 0
				&& (this._gameStateInfo.isPlayState || this._isFrbMode)
			)
		{
			let bullets = this.info.getMasterBullets();
			let activeBulletsAmnt;
			for (let bullet of bullets)
			{
				if (bullet.isActive) 
				{
					activeBulletsAmnt = activeBulletsAmnt || {};

					let lWeaponId_num = bullet.weaponId;
					let lActiveWeaponBullets = activeBulletsAmnt[lWeaponId_num] || {};
					activeBulletsAmnt[lWeaponId_num] = lActiveWeaponBullets;
					let l_int = lActiveWeaponBullets.bulletsAmount || 0;
					lActiveWeaponBullets.bulletsAmount = ++l_int;
					lActiveWeaponBullets.betLevel = bullet.bulletInitialBetLevel; 	// in btg mode ricochet bullet can update its level/skin during flight, 
																					// but it is necessary to return the bullet of the level with which it was fired
				}
			}

			for (let weaponId in activeBulletsAmnt)
			{
				if (weaponId == WEAPONS.DEFAULT)
				{
					this.emit(RicochetController.EVENT_ON_RICOCHET_BULLETS_PAYBACK_REQUIRED, {currentBullets: activeBulletsAmnt[weaponId].bulletsAmount, weaponId: weaponId, betLevel: activeBulletsAmnt[weaponId].betLevel});
				}
				else
				{
					for (let i=0; i<activeBulletsAmnt[weaponId].bulletsAmount; i++)
					{
						this.emit(RicochetController.EVENT_ON_RICOCHET_BULLETS_PAYBACK_REQUIRED, {currentBullets: 1, weaponId: weaponId, betLevel: activeBulletsAmnt[weaponId].betLevel});
					}
				}
			}
		}
	}

	get _isFrbMode()
	{
		return APP.currentWindow.gameFrbController.info.frbMode;
	}

	get _gameStateInfo()
	{
		return this._fGameStateInfo_gsi || (this._fGameStateInfo_gsi = APP.gameScreen.gameStateController.info);
	}

	_onPlayerRicochetBulletsUpdated()
	{
		this.emit(RicochetController.EVENT_ON_RICOCHET_BULLETS_UPDATED, {currentBullets: this.info.existingMasterBulletsCount});
	}

	_onBetLevelChangeConfirmed(aEvent_obj)
	{
		let lSeatId_int = aEvent_obj.seatId;
		let lNewBetLevel_int = aEvent_obj.betLevel;
		let lPlayerInfo_pi = APP.playerController.info;
		let lPossibleBetLevels = lPlayerInfo_pi.possibleBetLevels;

		if (APP.isBattlegroundGame && lPossibleBetLevels.indexOf(lNewBetLevel_int) == (lPossibleBetLevels.length-1))
		{
			let lSeatRicochetBullets = this.info.getBulletsBySeatId(lSeatId_int, true, WEAPONS.HIGH_LEVEL);
			for (let lSeatRicochetBullet of lSeatRicochetBullets)
			{
				let lNewSkinId = lPlayerInfo_pi.getTurretSkinId(lNewBetLevel_int);
				lSeatRicochetBullet.updateSkin(lNewSkinId);
			}
		}
	}

	destroy()
	{
		APP.gameScreen.gameField.off(GameField.EVENT_ON_RICOCHET_BULLET_FLY_OUT, this._onBulletFlyOut, this);
		APP.gameScreen.gameField.off(GameField.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomClear, this);
		APP.gameScreen.gameField.off(GameField.EVENT_ON_CLEAR_BULLETS_BY_SEAT_ID, this._onClearBulletsBySeatId, this);
		APP.gameScreen.gameField.off(GameField.EVENT_ON_MASTER_SEAT_ADDED, this._onMasterSeatAdded, this);

		APP.gameScreen.off(GameScreen.EVENT_ON_CO_PLAYER_COLLISION_OCCURED, this._onCoPlayerCollisionOccured, this);
		APP.gameScreen.off(GameScreen.EVENT_ON_PLAYER_ADDED, this._onPlayerSitIn, this);
		APP.gameScreen.off(GameScreen.EVENT_ON_PLAYER_REMOVED, this._onPlayerSitOut, this);

		super.destroy();

		this._fAvailableSpace_obj = null;
	}
}

export default RicochetController;