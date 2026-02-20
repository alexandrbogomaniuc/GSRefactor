import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { COOP_FIRE_FX_ALPHA } from '../../../../../../shared/src/CommonConstants';
import { Z_INDEXES } from '../../../../main/GameField';
import ShotResultsUtil from '../../../../main/ShotResultsUtil';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import RicochetLaserRay from '../../../../main/animation/ricochet_laser/RicochetLaserRay';

class RicochetLasersView extends SimpleUIView {
	
	static get EVENT_ON_BEAM_BASIC_ANIMATION_COMPLETED() 	{ return 'EVENT_ON_BEAM_BASIC_ANIMATION_COMPLETED'; }

	constructor()
	{
		super();

		this._fMasterLasersContainer_sprt = null;
		this._fCoplayersLasersContainer_sprt = null;

		this._ricochet_lasers = [];
	}

	get _mainContainer ()
	{
		return APP.currentWindow.gameField.lasersContainerInfo;
	}

	get _gf ()
	{
		return APP.gameScreen.gameField;
	}

	//PUBLIC...
	i_init()
	{
		this._init();
	}

	i_showFire(data, aTargetEnemyId_int, aStartPos_pt, aEndPos_pt, callback)
	{
		this._showFire(data, aTargetEnemyId_int, aStartPos_pt, aEndPos_pt, callback);
	}

	i_clearAll()
	{
		this._clearAll();
	}
	//...PUBLIC

	//PRIVATE...
	_init()
	{
	}

	_initContainersIfRequired()
	{
		if (this._fMasterLasersContainer_sprt)
		{
			return;
		}

		let lContainer_sprt = this._gf._ricochetLasersContainer;

		this._fMasterLasersContainer_sprt = lContainer_sprt.addChild(new Sprite);
		this._fMasterLasersContainer_sprt.zIndex = Z_INDEXES.MAIN_PLAYER_CONTAINER_EFFECTS;

		this._fCoplayersLasersContainer_sprt = lContainer_sprt.addChild(new Sprite);
		this._fCoplayersLasersContainer_sprt.zIndex = Z_INDEXES.PLAYERS_CONTAINER_EFFECTS;
	}

	_showFire(data, aTargetEnemyId_int, aStartPos_pt, aEndPos_pt, callback)
	{
		this._initContainersIfRequired();

		this._showLasers(data, aTargetEnemyId_int, aStartPos_pt, aEndPos_pt, callback);
	}

	_showLasers(data, aTargetEnemyId_int, aStartPos_pt, aEndPos_pt, callback)
	{
		let lTargetSeatId_num = data.seatId;
		let seat = this._gf.getSeat(lTargetSeatId_num, true);
		let player = this._gf.getPlayerBySeatId(lTargetSeatId_num);
		let weaponSpotView = seat.weaponSpotView;
		
		let lIsMasterShot_bl = ShotResultsUtil.isMasterShot(data);
		let laserContainer = lIsMasterShot_bl ? this._fMasterLasersContainer_sprt : this._fCoplayersLasersContainer_sprt;

		let params = [];
		params.push( {basePoint: aStartPos_pt, seatId: lTargetSeatId_num} );
		params.push( {basePoint: aEndPos_pt} );

		for (let affectedEnemy of data.affectedEnemies)
		{
			if (ShotResultsUtil.isFakeEnemy(affectedEnemy))
			{
				continue;
			}

			let lPoint_pt = laserContainer.globalToLocal(affectedEnemy.data.x, affectedEnemy.data.y);
			let lParam_obj = { enemyId: affectedEnemy.enemyId, basePoint: {x: lPoint_pt.x, y: lPoint_pt.y} };

			if (affectedEnemy.enemyId == aTargetEnemyId_int)
			{
				params.splice(2, 0, lParam_obj);
			}
			else
			{
				params.push(lParam_obj);
			}
		}

		var laserRays = [];
		let lHitDuration_num = RicochetLaserRay.FULL_APPEARANCE_TIME / params.length;
		for (let i=0; i<params.length - 1; i++)
		{
			let laserRay = laserContainer.addChild(new RicochetLaserRay(i, i*100, lHitDuration_num, params[i], params[i+1], data));
			laserRay.zIndex = 100-i;
			laserRay.alpha = lIsMasterShot_bl ? 1 : COOP_FIRE_FX_ALPHA;

			this._ricochet_lasers.push(laserRay);
			laserRays.push(laserRay);

			laserRay.once(RicochetLaserRay.EVENT_DISAPPEARED, this._onRicochetLaserRayDisappeared, this);

			if (i == params.length - 2)
			{
				laserRay.once(RicochetLaserRay.EVENT_ADDED, (e) => {
					callback.call(this, laserContainer.localToGlobal(e.finalPosition.x, e.finalPosition.y), e.angle);
				})

				laserRay.once(RicochetLaserRay.EVENT_READY_FOR_SAFE_REMOVE, (e) => {
					for (let laserRay of laserRays)
					{
						laserRay.disappear();
					}

					this.emit(RicochetLasersView.EVENT_ON_BEAM_BASIC_ANIMATION_COMPLETED, {shotData: laserRay.shotData});
				})
			}
		}
	}

	get hasNotReadyMasterBeams()
	{
		let lMainSpotLaserRaysInProcess_rlr_arr = this._ricochet_lasers.filter((aLaserRay_rlr) => {
			return ShotResultsUtil.isMasterShot(aLaserRay_rlr.shotData) && !aLaserRay_rlr.isReadyForSafeRemove; //only not ready laser rays from main spot
		});

		return lMainSpotLaserRaysInProcess_rlr_arr.length > 0;
	}

	_onRicochetLaserRayDisappeared(event)
	{
		let laserRay = event.target;

		laserRay.off(RicochetLaserRay.EVENT_DISAPPEARED, this._onRicochetLaserRayDisappeared, this, true);
		this._removeRicochetLaserRay(laserRay);
	}

	_removeRicochetLaserRay(laserRay)
	{
		if (!laserRay)
		{
			return;
		}

		let lIndex_int = this._ricochet_lasers.indexOf(laserRay);
		if (~lIndex_int)
		{
			this._ricochet_lasers.splice(lIndex_int, 1);
		}

		laserRay.destroy();
	}

	_clearAll()
	{
		while (this._ricochet_lasers && this._ricochet_lasers.length)
		{
			this._ricochet_lasers.pop().destroy();
		}
		this._ricochet_lasers = [];
	}
	//...PRIVATE
}

export default RicochetLasersView;