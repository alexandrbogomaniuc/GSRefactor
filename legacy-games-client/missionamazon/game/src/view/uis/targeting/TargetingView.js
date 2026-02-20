import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Crosshairs from '../../../main/Crosshairs';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Enemy from '../../../main/enemies/Enemy';
import WitchEnemy from '../../../main/enemies/WitchEnemy';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { isPointInsideRect } from '../../../model/collisions/CollisionTools';
import { ENEMY_TYPES } from '../../../../../shared/src/CommonConstants'

class TargetingView extends SimpleUIView {

	static get EVENT_ON_TARGET_VIEW_DESTROYED()	{return 'EVENT_ON_TARGET_VIEW_DESTROYED'};
	static get EVENT_CROSSHAIRS_HIDDEN() {return 'EVENT_CROSSHAIRS_HIDDEN'}

	constructor()
	{
		super();
		this._fCrosshairs_c = null;
		this._fTargetEnemy_enm = null;
		this._fLastCrosshairsPos_p = null;
		this._fIsRedrawRequired_bl = false;
	}

	addToContainerIfRequired(targetingContainerInfo)
	{
		if (this.parent || !targetingContainerInfo || !targetingContainerInfo.container)
		{
			return;
		}

		targetingContainerInfo.container.addChild(this);
		this.zIndex = targetingContainerInfo.zIndex;
	}

	updateTarget(aTargetEnemy_enm)
	{	
		this._stopListeningTarget();
		this._fTargetEnemy_enm = aTargetEnemy_enm;	
		if (this._fTargetEnemy_enm)
		{			
			this._startListeningTarget();
		}
		else
		{	
			this._hideCrosshairs();
		}
	}

	_startListeningTarget()
	{
		this._fTargetEnemy_enm.on(Enemy.EVENT_ON_ENEMY_START_DYING, this._onTargetEnemyStartDying, this);
		this._fTargetEnemy_enm.on(Enemy.EVENT_ON_ENEMY_VIEW_REMOVING, this._onTargetEnemyViewRemoving, this);
		this._fTargetEnemy_enm.on(WitchEnemy.EVENT_ON_WITCH_TELEPORT, this._onTargetEnemyViewRemoving, this);
		
		this._fIsRedrawRequired_bl = true;
	}

	_stopListeningTarget(e)
	{
		this._fTargetEnemy_enm && this._fTargetEnemy_enm.off(Enemy.EVENT_ON_ENEMY_START_DYING, this._onTargetEnemyStartDying, this);
		this._fTargetEnemy_enm && this._fTargetEnemy_enm.off(Enemy.EVENT_ON_ENEMY_VIEW_REMOVING, this._onTargetEnemyViewRemoving, this);
		this._fTargetEnemy_enm && this._fTargetEnemy_enm.off(WitchEnemy.EVENT_ON_WITCH_TELEPORT, this._onTargetEnemyViewRemoving, this);
		
		this._fIsRedrawRequired_bl = false;
	}

	_onTargetEnemyStartDying(e)
	{
		this._resetTarget();
	}

	_onTargetEnemyViewRemoving(e)
	{
		this._resetTarget();
	}

	_resetTarget()
	{
		this.updateTarget(null);
		this.emit(TargetingView.EVENT_ON_TARGET_VIEW_DESTROYED);
	}

	tick()
	{
		if (!this._fIsRedrawRequired_bl)
		{
			return;
		}

		let lEnemy_enm = this._fTargetEnemy_enm;

		if (
			lEnemy_enm &&
			lEnemy_enm.parent &&
			!lEnemy_enm.isRedTargetMarkerRejected() &&
			lEnemy_enm.isTargetable()
			)
		{
			let lEnemyPos_pt = lEnemy_enm.getCenterPosition();
			let lCrosshairsOffsetPosition_pt = lEnemy_enm.crosshairsOffsetPosition;
			let lCrosshairsPos_obj = {x: lEnemyPos_pt.x + lCrosshairsOffsetPosition_pt.x, y:  lEnemyPos_pt.y + lCrosshairsOffsetPosition_pt.y};
			
			let lCrosshairsPosX_num = lEnemyPos_pt.x + lCrosshairsOffsetPosition_pt.x;
			let lCrosshairsPosY_num = lEnemyPos_pt.y + lCrosshairsOffsetPosition_pt.y;

			if (//check if the aim is not on the screen except case, when targeting boss
				//https://jira.dgphoenix.com/browse/MA-209
				!lEnemy_enm.typeId === ENEMY_TYPES.BOSS &&
					(lCrosshairsPosX_num < 0 ||
					lCrosshairsPosY_num < 0 ||
					lCrosshairsPosX_num >= 960 ||
					lCrosshairsPosY_num >= 540)
				)
			{
				this.emit(TargetingView.EVENT_CROSSHAIRS_HIDDEN);
			}

			this.crosshairs.position.set(lCrosshairsPosX_num, lCrosshairsPosY_num);
			
			this._fLastCrosshairsPos_p = new PIXI.Point(lCrosshairsPosX_num, lCrosshairsPosY_num);

			this._showCrosshairs();
		}
		else
		{
			this._resetTarget();
		}
	}

	_hideCrosshairs()
	{
		this.crosshairs.visible = false;
	}

	_showCrosshairs()
	{
		this.crosshairs.visible = true;
	}

	get lastCrosshairsPos()
	{
		let lCrosshairs_c = this._fCrosshairs_c;
		if (!lCrosshairs_c)
		{
			return null;
		}

		if (lCrosshairs_c.parent && lCrosshairs_c.visible)
		{
			return lCrosshairs_c.parent.localToGlobal(lCrosshairs_c.position.x, lCrosshairs_c.position.y);
		}

		return this._fLastCrosshairsPos_p || null;
	}

	get crosshairs()
	{
		return this._fCrosshairs_c || this._initCrosshairs();
	}

	_initCrosshairs()
	{
		return (this._fCrosshairs_c = this.addChild(new Crosshairs()));
	}	

	destroy()
	{
		this._fCrosshairs_c && this._fCrosshairs_c.destroy();
		this._fCrosshairs_c = null;

		this._fTargetEnemy_enm && this._fTargetEnemy_enm.off(Enemy.EVENT_ON_ENEMY_START_DYING, this._onTargetEnemyStartDying, this);
		this._fTargetEnemy_enm && this._fTargetEnemy_enm.off(Enemy.EVENT_ON_ENEMY_VIEW_REMOVING, this._onTargetEnemyViewRemoving, this);
		this._fTargetEnemy_enm && this._fTargetEnemy_enm.off(WitchEnemy.EVENT_ON_WITCH_TELEPORT, this._onTargetEnemyViewRemoving, this);
		this._fTargetEnemy_enm = null;
		this._fLastCrosshairsPos_p = null;
		this._fIsRedrawRequired_bl = undefined;

		super.destroy();

	}
}

export default TargetingView
