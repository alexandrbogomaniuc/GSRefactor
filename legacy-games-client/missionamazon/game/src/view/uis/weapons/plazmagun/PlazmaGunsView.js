import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { COOP_FIRE_FX_ALPHA } from '../../../../../../shared/src/CommonConstants';
import { Z_INDEXES } from '../../../../main/GameField';
import ShotResultsUtil from '../../../../main/ShotResultsUtil';
import InstantKillExplosion from '../../../../main/animation/instant_kill/InstantKillExplosion';
import PlazmaGunBeam from '../../../../main/animation/instant_kill/PlazmaGunBeam';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class PlazmaGunsView extends SimpleUIView {

	
	static get EVENT_ON_BEAM_TARGET_PRE_ACHIEVED() 		 	{ return PlazmaGunBeam.EVENT_ON_TARGET_PRE_ACHIEVED; }
	static get EVENT_ON_BEAM_ANIMATION_COMPLETED() 		 	{ return PlazmaGunBeam.EVENT_ON_ANIMATION_COMPLETED; }
	static get EVENT_ON_BEAM_BASIC_ANIMATION_COMPLETED() 	{ return PlazmaGunBeam.EVENT_ON_BASIC_ANIMATION_COMPLETED; }

	constructor()
	{
		super();

		this._fBeams_ftb_arr = [];
		this._fAddBeams_ftb_arr = [];
		this._fExplosions_ike_arr = [];
	}

	get _mainContainer ()
	{
		return this._gf.plazmagunEffectContainer;
	}

	get _gf ()
	{
		return APP.currentWindow.gameField;
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
		this._mainContainer.container.addChild(this);
		this.zIndex = this._mainContainer.zIndex;
	}
	
	_showFire(data, aTargetEnemyId_int, aStartPos_pt, aEndPos_pt, callback)
	{
		//create beam
		this._createBeam(data, aTargetEnemyId_int, aStartPos_pt, aEndPos_pt, callback);

		//create explosion
		this._showExplosion(data, aStartPos_pt, aEndPos_pt);
	}

	//BEAM...
	_createBeam(data, aTargetEnemyId_int, aStartPos_pt, aEndPos_pt, callback)
	{
		//create beam
		let lBeam_pgb = new PlazmaGunBeam(data, aTargetEnemyId_int, callback);

		lBeam_pgb.on(PlazmaGunBeam.EVENT_ON_ANIMATION_COMPLETED, this._onBeamAnimationCompleted, this);
		lBeam_pgb.on(PlazmaGunBeam.EVENT_ON_BASIC_ANIMATION_COMPLETED, this._onBeamBasicAnimationCompleted, this);
		lBeam_pgb.on(PlazmaGunBeam.EVENT_ON_TARGET_PRE_ACHIEVED, this._onBeamTargetPreAchieved, this);
		lBeam_pgb.on(PlazmaGunBeam.EVENT_ON_TARGET_ACHIEVED, this._onBeamTargetAchieved, this);
		lBeam_pgb.i_shoot(aStartPos_pt, aEndPos_pt);
		
		this.addChild(lBeam_pgb);

		if (!ShotResultsUtil.isMasterShot(data))
		{
			lBeam_pgb.alpha = COOP_FIRE_FX_ALPHA;
		}

		this._fBeams_ftb_arr.push(lBeam_pgb);
	}

	_onBeamTargetPreAchieved(aEvent_obj)
	{
		let lBeam_pgb = aEvent_obj.target;

		let lBeamShotData_obj = lBeam_pgb.shotData;
		let lBeamTargetEnemyId_num = lBeam_pgb.targetEnemyId;
		const lStartPos_obj = this._gf.getEnemyPosition(lBeamTargetEnemyId_num);
		
		let lAddProjectilesAffectedEnemies_obj_arr = this._definePlasmaAddProjectilesEndPositions(lBeamTargetEnemyId_num, lStartPos_obj, lBeamShotData_obj.affectedEnemies);

		for (let i = 0; i < lAddProjectilesAffectedEnemies_obj_arr.length; i++)
		{
			let lEndPos_obj = lAddProjectilesAffectedEnemies_obj_arr[i];
			this._createAddBeam(lBeamShotData_obj, lStartPos_obj, lEndPos_obj);
		}

		this.emit(PlazmaGunsView.EVENT_ON_BEAM_TARGET_PRE_ACHIEVED, {targetEnemyId: lBeam_pgb.targetEnemyId});
	}

	_onBeamTargetAchieved(aEvent_obj)
	{
	}

	_onBeamAnimationCompleted(aEvent_obj)
	{
		let lBeam_pgb = aEvent_obj.target;
		let lIndex_int = this._fBeams_ftb_arr.indexOf(lBeam_pgb);
		if (~lIndex_int)
		{
			this._fBeams_ftb_arr.splice(lIndex_int, 1);
			lBeam_pgb.destroy();
		}
	}

	_onBeamBasicAnimationCompleted(aEvent_obj)
	{
		let lBeam_pgb = aEvent_obj.target;
		this.emit(PlazmaGunsView.EVENT_ON_BEAM_BASIC_ANIMATION_COMPLETED, {shotData: lBeam_pgb.shotData});
	}
	//...BEAM

	//ADD BEAMS...
	_definePlasmaAddProjectilesEndPositions(aStartEnemyId_int, aStartPos_obj, aAffectedEnemies_obj_arr)
	{
		let lEndPositions_arr = [];
		
		for (let i = 0; i < aAffectedEnemies_obj_arr.length; i++)
		{
			let lCurAffectedEnemy_obj = aAffectedEnemies_obj_arr[i];
			
			if (lCurAffectedEnemy_obj.enemyId == aStartEnemyId_int)
			{
				continue;
			}
			
			const lAffectedEnemy_e = this._gf.getExistEnemy(lCurAffectedEnemy_obj.enemyId);
			let lEndPos_obj = (lAffectedEnemy_e && lAffectedEnemy_e.parent) ? lAffectedEnemy_e.getCenterPosition() : this._gf.enemiesLastPositions[lCurAffectedEnemy_obj.enemyId];

			if (!aStartPos_obj || !lEndPos_obj)
			{
				continue;
			}

			const lEnemiesDistance_num = Utils.getDistance(aStartPos_obj, lEndPos_obj);

			if (lEnemiesDistance_num < 60)
			{
				continue;
			}

			lEndPos_obj = {x: lEndPos_obj.x, y: lEndPos_obj.y, distance: lEnemiesDistance_num, angle: Utils.getAngle(aStartPos_obj, lEndPos_obj)};

			lEndPositions_arr.push(lEndPos_obj);
		}

		let lSectorAngle_num = 45;
		let lSectorsAmount_int = Math.ceil(360/lSectorAngle_num);
		let lSectorsEndPositions_arr = [];
		for (let i=0; i<lSectorsAmount_int; i++)
		{
			let lSectorFromAngle_num = lSectorAngle_num*i;
			let lSectorToAngle_num = Math.min(lSectorFromAngle_num + lSectorAngle_num, 360);
			if (lSectorFromAngle_num >= 180)
			{
				lSectorFromAngle_num -= 360;
				lSectorToAngle_num -= 360;
			}

			let lSectorPositions_arr = [];
			for (let j=0; j<lEndPositions_arr.length; j++)
			{
				let lEndPos = lEndPositions_arr[j];
				let lAngle_num = lEndPos.angle;
				if (lAngle_num >= Utils.gradToRad(lSectorFromAngle_num) && lAngle_num <= Utils.gradToRad(lSectorToAngle_num))
				{
					lSectorPositions_arr.push(lEndPos);
					lEndPositions_arr.splice(j, 1);
					j--;
				}
			}
			lSectorsEndPositions_arr[i] = lSectorPositions_arr;
		}

		for (let i=0; i<lSectorsEndPositions_arr.length; i++)
		{
			let lSectorPositions_arr = lSectorsEndPositions_arr[i];
			if (lSectorPositions_arr && lSectorPositions_arr.length)
			{
				let lMaxDistPos = lSectorPositions_arr[0];
				for (let j=1; j<lSectorPositions_arr.length; j++)
				{
					if (lSectorPositions_arr[j].distance > lMaxDistPos.distance)
					{
						lMaxDistPos = lSectorPositions_arr[j];
					}
				}

				lEndPositions_arr.push(lMaxDistPos);
			}
		}

		return lEndPositions_arr;
	}

	_createAddBeam(aShotData_obj, aStartPos_pt, aEndPos_pt)
	{
		//create beam
		let lBeam_pgb = new PlazmaGunBeam(null);

		lBeam_pgb.once(PlazmaGunBeam.EVENT_ON_ANIMATION_COMPLETED, this._onAddBeamAnimationCompleted, this);
		lBeam_pgb.i_shoot(aStartPos_pt, aEndPos_pt);
		
		this.addChild(lBeam_pgb);

		if (!ShotResultsUtil.isMasterShot(aShotData_obj))
		{
			lBeam_pgb.alpha = COOP_FIRE_FX_ALPHA;
		}

		this._fAddBeams_ftb_arr.push(lBeam_pgb);
	}

	_onAddBeamAnimationCompleted(aEvent_obj)
	{
		let lBeam_pgb = aEvent_obj.target;
		
		let lIndex_int = this._fAddBeams_ftb_arr.indexOf(lBeam_pgb);
		if (~lIndex_int)
		{
			this._fAddBeams_ftb_arr.splice(lIndex_int, 1);
			lBeam_pgb.destroy();
		}
	}
	//...ADD BEAMS

	//EXPLOSION...
	_showExplosion(data, aStartPos_pt, aEndPos_pt)
	{
		let lParent_sprt = this._mainContainer.container;
		let lIsCoopPlayerShot_bl = !ShotResultsUtil.isMasterShot(data);

		var lensFlareParent = lParent_sprt.addChild(new Sprite);
		lensFlareParent.zIndex = Z_INDEXES.PLAZMA_LENS_FLARE;
		lensFlareParent.position.set(aEndPos_pt.x, aEndPos_pt.y);
		
		var lExplosion_ike = lParent_sprt.addChild(new InstantKillExplosion(lensFlareParent));
		lIsCoopPlayerShot_bl && (lExplosion_ike.alpha = COOP_FIRE_FX_ALPHA);

		lExplosion_ike.position.set(aEndPos_pt.x, aEndPos_pt.y);
		lExplosion_ike.zIndex = aEndPos_pt.y + 100;
		lExplosion_ike.once(InstantKillExplosion.EVENT_ON_READY_FOR_DESTROY, this._onExplosionEnd, this);
		
		this._fExplosions_ike_arr.push(lExplosion_ike);
	}

	_onExplosionEnd(aEvent_obj)
	{
		let lExplosion_ike = aEvent_obj.target;
		let lIndex_int = this._fExplosions_ike_arr.indexOf(lExplosion_ike);
		if (~lIndex_int)
		{
			this._fExplosions_ike_arr.splice(lIndex_int, 1);
			lExplosion_ike.destroy();
		}
	}
	//...EXPLOSION

	_clearAll()
	{
		for (let lBeam_pgb of this._fBeams_ftb_arr)
		{
			lBeam_pgb.destroy();
		}
		this._fBeams_ftb_arr = [];

		for (let lBeam_pgb of this._fAddBeams_ftb_arr)
		{
			lBeam_pgb.destroy();
		}
		this._fAddBeams_ftb_arr = [];

		for (let lExplosion_ike of this._fExplosions_ike_arr)
		{
			lExplosion_ike.destroy();
		}
		this._fExplosions_ike_arr = [];
	}
	//...PRIVATE
}

export default PlazmaGunsView;