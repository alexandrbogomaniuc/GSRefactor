import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE, WEAPONS, COOP_FIRE_FX_ALPHA } from '../../../../../../shared/src/CommonConstants';
import ArtilleryGrenade from '../../../../main/bullets/ArtilleryGrenade';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import ArtilleryMissile from './ArtilleryMissile';
import ArtilleryGrenadeSmokes from './ArtilleryGrenadeSmokes';

const MISSILES_INTERVAL = 3*FRAME_RATE;
const MISSILE_SPEED = 1; //px in ms

class ArtilleryStrikeView extends SimpleUIView {

	static get EVENT_ON_ARTILLERY_GRENADE_LANDED() 			{ return 'EVENT_ON_ARTILLERY_GRENADE_LANDED'; }	
	static get EVENT_ON_TIME_TO_START_ARTILLERY_STRIKES() 	{ return 'EVENT_ON_TIME_TO_START_ARTILLERY_STRIKES'; }
	static get EVENT_ON_ALL_MISSILES_COMPLETED() 			{ return 'EVENT_ON_ALL_MISSILES_COMPLETED'; }
	static get EVENT_ON_FULL_ANIMATION_COMPLETED() 			{ return 'EVENT_ON_FULL_ANIMATION_COMPLETED'; }
	static get EVENT_ON_STRIKE_MISSILE_HIT() 				{ return 'EVENT_ON_STRIKE_MISSILE_HIT'; }

	i_throwArtilleryGrenade(lWeaponId_num, startPos, endPos, middlePos = null)
	{
		this._throwArtilleryGrenade(lWeaponId_num, startPos, endPos, middlePos);
	}

	i_startArtilleryMissiles(aTargetEnemiesIds_int_arr)
	{
		this._startArtilleryMissiles(aTargetEnemiesIds_int_arr);
	}

	constructor()
	{
		super();

		this._gameScreen = APP.currentWindow;

		this._fArtilleryGrenade_sprt = null;
		this._fTimers_tmr_arr = [];
		this._fArtilleryMissiles_am_arr = [];
		this._fArtilleryMissilesCounter_int = 0;
		this._fArtilleryGrenadeSmokes_agss = null;
	}

	get _grenadeContainer()
	{
		return this._gameScreen.gameField.artilleryStrikeEffectContainer.grenadeContainer;
	}

	get _strikeContainer()
	{
		return this._gameScreen.gameField.artilleryStrikeEffectContainer.strikeContainer;
	}

	get _mainZIndex()
	{
		return this._gameScreen.gameField.artilleryStrikeEffectContainer.zIndex;
	}

	_throwArtilleryGrenade(lWeaponId_num, startPos, endPos, middlePos = null)
	{
		let points = [startPos, endPos];

		if (middlePos)
		{
			points = [startPos, middlePos, endPos];
		}

		let bulletProps = {
			typeId: lWeaponId_num
		};

		this._fArtilleryGrenade_sprt = new ArtilleryGrenade(bulletProps, points, ()=>this._onArtilleryGrenadeLanded(), this.uiInfo.rid);

		this._grenadeContainer.addChild(this._fArtilleryGrenade_sprt);
		this._fArtilleryGrenade_sprt.zIndex = this._mainZIndex;

		this._fArtilleryGrenade_sprt.position.set(startPos.x, startPos.y);

		if (this.uiInfo.rid == -1)
		{
			this._fArtilleryGrenade_sprt.alpha = COOP_FIRE_FX_ALPHA;
		}
	}

	_onArtilleryGrenadeLanded()
	{
		this.emit(ArtilleryStrikeView.EVENT_ON_ARTILLERY_GRENADE_LANDED);
		this._startSmoke();
	}

	_startSmoke()
	{
		this._fArtilleryGrenadeSmokes_agss = this._grenadeContainer.addChild(new ArtilleryGrenadeSmokes());
		this._fArtilleryGrenadeSmokes_agss.position.set(this._fArtilleryGrenade_sprt.x, this._fArtilleryGrenade_sprt.y);
		this._fArtilleryGrenadeSmokes_agss.zIndex = this._fArtilleryGrenade_sprt.zIndex;
		this._fArtilleryGrenadeSmokes_agss.once(ArtilleryGrenadeSmokes.EVENT_ON_ANIMATION_END, this._onSmokesAnimationEnd, this);

		if (this.uiInfo.rid == -1)
		{
			this._fArtilleryGrenadeSmokes_agss.alpha = COOP_FIRE_FX_ALPHA;
		}
		this._onTimeToStartArtilleryStrikes();
	}

	_onTimeToStartArtilleryStrikes()
	{
		this._fStrikesStartTimer_tmr && this._fStrikesStartTimer_tmr.destructor();
		this._fStrikesStartTimer_tmr = null;
		this.emit(ArtilleryStrikeView.EVENT_ON_TIME_TO_START_ARTILLERY_STRIKES);
	}

	_onSmokesAnimationEnd()
	{
		this._fArtilleryGrenadeSmokes_agss && this._fArtilleryGrenadeSmokes_agss.destroy();
		this._fArtilleryGrenadeSmokes_agss = null;	
	}

	_startArtilleryMissiles(aTargetEnemiesIds_int_arr)
	{
		for (let i = 0; i < aTargetEnemiesIds_int_arr.length; ++i)
		{
			let lTargetEnemyId_int = aTargetEnemiesIds_int_arr[i];
			let lInterval_num = MISSILES_INTERVAL * i + Utils.random(-FRAME_RATE, FRAME_RATE);
			let lTimer_tmr = new Timer(this._launchAnotherArtilleryMissile.bind(this, lTargetEnemyId_int), lInterval_num);
			this._fArtilleryMissilesCounter_int++;
			this._fTimers_tmr_arr.push(lTimer_tmr);
		}
	}

	_launchAnotherArtilleryMissile(aTargetEnemyId_int)
	{
		let endPos = this._gameScreen.gameField.getEnemyPosition(aTargetEnemyId_int, true /*feet pos*/);
		if (!endPos)
		{
			endPos = new PIXI.Point();
			APP.logger.i_pushWarning(`ArtilleryStrike. Missile target error :: no enemy position found for ${aTargetEnemyId_int}.`);
			console.log("[Y] ArtilleryStrike missile target error :: no enemy position found for " + aTargetEnemyId_int);
		}
		let deltaRand = Math.random()*50 - 25;
		let startPos = new PIXI.Point(endPos.x - deltaRand, -10);
		let distance = Utils.getDistance(startPos, endPos);
		let speed = MISSILE_SPEED;
		let time = distance/speed;

		let lArtilleryMissile_am = new ArtilleryMissile();

		this._strikeContainer.addChild(lArtilleryMissile_am);
		lArtilleryMissile_am.scale.set(1.3);
		lArtilleryMissile_am.zIndex = endPos.y;
		lArtilleryMissile_am.rotation = Math.PI - Math.asin(deltaRand/distance);
		// if (APP.currentWindow.gameStateController.info.isBossSubround)
		// {
		// 	lArtilleryMissile_am.zIndex = 1040;
		// }

		lArtilleryMissile_am.position.set(startPos.x, startPos.y);
		this._fArtilleryMissiles_am_arr.push(lArtilleryMissile_am);
		
		lArtilleryMissile_am.moveTo(endPos.x, endPos.y, time, Easing.quadratic.easeIn, ()=>{
			this._onAnotherArtilleryMissileHit(endPos.x, endPos.y);
			lArtilleryMissile_am.i_hideMissile();
		});

		lArtilleryMissile_am.once(ArtilleryMissile.EVENT_ON_ANIMATION_END, this._onAnotherArtilleryMissileAnimationCompleted, this);

		if (this.uiInfo.rid == -1)
		{
			lArtilleryMissile_am.alpha = COOP_FIRE_FX_ALPHA;
		}
	}

	_onAnotherArtilleryMissileHit(x, y)
	{
		this._fArtilleryMissilesCounter_int--;

		let isFinal = this._fArtilleryMissilesCounter_int <= 0;
		this.emit(ArtilleryStrikeView.EVENT_ON_STRIKE_MISSILE_HIT, {x: x, y: y, rid: this.uiInfo.rid, isFinal: isFinal});

		if (this._fArtilleryMissilesCounter_int <= 0)
		{
			this._onAllArtilleryMissilesHit();
		}
	}

	_onAnotherArtilleryMissileAnimationCompleted(aEvent_obj)
	{
		let lArtilleryMissile_am = aEvent_obj.target;
		let lIndex_int = this._fArtilleryMissiles_am_arr.indexOf(lArtilleryMissile_am);
		if (~lIndex_int)
		{
			this._fArtilleryMissiles_am_arr.splice(lIndex_int, 1);
			lArtilleryMissile_am.destroy();
		}
		else
		{
			throw new Error ('No ArtilleryMissile sprite found!');
		}

		if (this._fArtilleryMissiles_am_arr.length === 0)
		{
			this._fullAnimationCompletionSuspicion();
		}
	}

	_onAllArtilleryMissilesHit()
	{
		this.emit(ArtilleryStrikeView.EVENT_ON_ALL_MISSILES_COMPLETED);
		this._fadeOutGrenade();
	}

	_onAllArtilleryMissilesAnimationCompleted()
	{
		this._fullAnimationCompletionSuspicion();
	}

	_fadeOutGrenade()
	{
		this._fArtilleryGrenadeSmokes_agss && this._fArtilleryGrenadeSmokes_agss.fadeTo(0, 13*FRAME_RATE);
		this._fArtilleryGrenade_sprt.fadeTo(0, 13*FRAME_RATE, null, ()=>{
			this._fArtilleryGrenade_sprt.destroy();
			this._fArtilleryGrenade_sprt = null;
			this._fullAnimationCompletionSuspicion();
		})
	}

	_fullAnimationCompletionSuspicion()
	{
		if ((!this._fArtilleryMissiles_am_arr || this._fArtilleryMissiles_am_arr.length === 0) && !this._fArtilleryGrenade_sprt) 
		{
			this.emit(ArtilleryStrikeView.EVENT_ON_FULL_ANIMATION_COMPLETED);
		}
	}

	destroy()
	{
		this.removeAllListeners();

		this._fStrikesStartTimer_tmr && this._fStrikesStartTimer_tmr.destructor();
		this._fStrikesStartTimer_tmr = null;

		this._fArtilleryGrenade_sprt && this._fArtilleryGrenade_sprt.destroy();
		this._fArtilleryGrenade_sprt = null;

		this._fArtilleryGrenadeSmokes_agss && this._fArtilleryGrenadeSmokes_agss.destroy();
		this._fArtilleryGrenadeSmokes_agss = null;

		while (this._fArtilleryMissiles_am_arr.length > 0)
		{
			let lArtilleryMissile_am = this._fArtilleryMissiles_am_arr.pop();
			lArtilleryMissile_am.destroy();
		}
		this._fArtilleryMissiles_am_arr = null;

		while (this._fTimers_tmr_arr.length > 0)
		{
			let lTimer_tmr = this._fTimers_tmr_arr.pop();
			lTimer_tmr.destructor();
		}
		this._fTimers_tmr_arr = null;

		super.destroy();
	}
}

export default ArtilleryStrikeView;