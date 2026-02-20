import { DIRECTION } from './Enemy';
import SpineEnemy from './SpineEnemy';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
const TRIPPLE_SHADOWS_POSITIONS = {			
	[DIRECTION.LEFT_DOWN]: {
		shadow1: 	{x: 0, 		y: 0},
		shadow2: 	{x: -5, 	y: -70},
		shadow3: 	{x: 110, 	y: -15}
	},
	[DIRECTION.RIGHT_DOWN]: { //done
		shadow1: 	{x: 0,	 	y: 0},
		shadow2: 	{x: 30, 	y: -70},
		shadow3: 	{x: -110, 	y: 20}
	},
	[DIRECTION.RIGHT_UP]: { //done
		shadow1: 	{x: 0,	 	y: 0},
		shadow2: 	{x: -5, 	y: -70},
		shadow3: 	{x: 110, 	y: 10}
	},
	[DIRECTION.LEFT_UP]: { //done
		shadow1: 	{x: 0,	 	y: 0},
		shadow2: 	{x: -30, 	y: -70},
		shadow3: 	{x: -110, 	y: 20}
	}
}

class WaspEnemy extends SpineEnemy
{
	constructor(params)
	{
		super(params);
		this.shadow2 = null;
		this.shadow3 = null;
		this._fWaspWiggleSeq_s = null;
		this._fWaspWiggleXYSeq_s = null;
		this._fWaspRotationWiggleSeq_s = null;
		this._fWaspGapSeq_s = null;
		this._startWaspWiggleMovement();
	}

	//override...
	getSpineSpeed()
	{
		let lSpeed_num = 0.11;
		let lSpineSpeed_num = lSpeed_num * this.currentTrajectorySpeed;

		return lSpineSpeed_num;
	}

	//override
	_addFreezeGround(aIsAnimated_bl = true)
	{
		//do nothing
	}

	_startWaspWiggleMovement()
	{
		this._fWiggleOffset_obj = {x: 0, y: 0, y2: 0, rotation: 0};
		this._fGapOffset_obj = {x: 0, y: 0};

		this._startNextWaspWiggle(Utils.random(0, 250));
		this._startNextWaspXYWiggle(Utils.random(0, 120));
		this._startNextWaspRotationWiggle(Utils.random(0, 5));
	}

	_startNextWaspWiggle(aDelay_num = 0)
	{
		let l_seq = [
			{tweens: [{ prop: 'y2', to: Utils.getRandomWiggledValue(0, 50) }], duration: Utils.random(30, 50)*FRAME_RATE, ease: Easing.sine.easeIn},
			{tweens: [{ prop: 'y2', to: 0 }], duration: Utils.random(30, 50)*FRAME_RATE, ease: Easing.sine.easeOut, onfinish: ()=>{
				this._fWaspWiggleSeq_s  && this._fWaspWiggleSeq_s .destructor();
				this._startNextWaspWiggle();
			}}
		];

		this._fWaspWiggleSeq_s = Sequence.start(this._fWiggleOffset_obj, l_seq, (Utils.random(3, 10)+aDelay_num)*FRAME_RATE);
	}

	_startNextWaspXYWiggle(aDelay_num = 0)
	{
		let l_seq = [
			{tweens: [{ prop: 'y', to: Utils.getRandomWiggledValue(0, 13) }, { prop: 'x', to: Utils.getRandomWiggledValue(0, 13) }], duration: Utils.random(40, 50)*FRAME_RATE, ease: Easing.sine.easeIn},
			{tweens: [{ prop: 'y', to: 0 }, { prop: 'x', to: 0 }], duration: Utils.random(40, 50)*FRAME_RATE, ease: Easing.sine.easeOut, onfinish: ()=>{
				this._fWaspWiggleXYSeq_s && this._fWaspWiggleXYSeq_s.destructor();
				this._startNextWaspXYWiggle();
			}}
		];

		this._fWaspWiggleXYSeq_s = Sequence.start(this._fWiggleOffset_obj, l_seq, (Utils.random(3, 10)+aDelay_num)*FRAME_RATE);
	}

	_startNextWaspRotationWiggle(aDelay_num = 0)
	{
		let l_seq = [
			{tweens: [{ prop: 'rotation', to: Utils.getRandomWiggledValue(0, 0.43) }], duration: Utils.random(20, 30)*FRAME_RATE, ease: Easing.sine.easeIn},
			{tweens: [{ prop: 'rotation', to: 0 }], duration: Utils.random(20, 30)*FRAME_RATE, ease: Easing.sine.easeOut, onfinish: ()=>{
				this._fWaspRotationWiggleSeq_s && this._fWaspRotationWiggleSeq_s.destructor();
				this._startNextWaspRotationWiggle();
			}}
		];

		this._fWaspRotationWiggleSeq_s = Sequence.start(this._fWiggleOffset_obj, l_seq, (Utils.random(3, 10)+aDelay_num)*FRAME_RATE);
	}

	//override
	changeSpineView(type)
	{
		super.changeSpineView(type);
		this._turnShadow();
	}

	//override
	get isFastTurnEnemy()
	{
		return true;
	}

	//override
	_resumeAfterUnfreeze()
	{
		super._resumeAfterUnfreeze();
		if (!this._fIsFrozen_bl)
		{
			this._fWaspWiggleSeq_s && this._fWaspWiggleSeq_s.resume();
			this._fWaspWiggleXYSeq_s && this._fWaspWiggleXYSeq_s.resume();
			this._fWaspGapSeq_s && this._fWaspGapSeq_s.resume();
			this._fWaspRotationWiggleSeq_s && this._fWaspRotationWiggleSeq_s.resume();
		}
	}

	//override
	updateOffsets()
	{
		this.enemyIndicatorsController.view.pivot.set(0, 0);
		let lOffset_obj = this._getOffset();
		if (!this._fIsFrozen_bl)
		{
			if (this._fWiggleOffset_obj)
			{
				this.enemyIndicatorsController.view.pivot.x -= this._fWiggleOffset_obj.x;
				this.enemyIndicatorsController.view.pivot.y -= this._fWiggleOffset_obj.y;
				this.enemyIndicatorsController.view.pivot.y -= this._fWiggleOffset_obj.y2;
			}

			if (this._fGapOffset_obj)
			{
				this.enemyIndicatorsController.view.pivot.x -= this._fGapOffset_obj.x;
				this.enemyIndicatorsController.view.pivot.y -= this._fGapOffset_obj.y;
			}

			this.container.children[1].position.set(lOffset_obj.x, lOffset_obj.y);
			if (lOffset_obj.rotation && this.container.children[1]) 
			{
				this.container.children[1].rotation = lOffset_obj.rotation; //rotate witout shadow
			}
		}
	}

	//override
	_getHitRectWidth()
	{
		return 62;
	}
	//override
	_getHitRectHeight()
	{
		return 62;
	}
	
		//override
		addShadow()
		{
			super.addShadow();
			let lPositions = TRIPPLE_SHADOWS_POSITIONS[this.direction];
			this.shadow2 = this.shadow.addChild(this._createShadow());
			this.shadow3 = this.shadow.addChild(this._createShadow());
			this.shadow2.position.set(lPositions.shadow2.x, lPositions.shadow2.y);
			this.shadow3.position.set(lPositions.shadow3.x, lPositions.shadow3.y);
		}
		
		_turnShadow(aDirection = this.direction)
		{
			if (!this.shadow2)
			{
				if (this.shadow.children[1])
				{
					this.shadow2 = this.shadow.children[1];
					this.shadow3 = this.shadow.children[2];
				}
				else 
				{
					this.addShadow()
				}
			}
			let lPositions = TRIPPLE_SHADOWS_POSITIONS[aDirection];
			this.shadow2.position.set(lPositions.shadow2.x, lPositions.shadow2.y);
			this.shadow3.position.set(lPositions.shadow3.x, lPositions.shadow3.y);
		}

	//override
	_freeze(aIsAnimated_bl = true)
	{
		if (!this._fIsFrozen_bl)
		{
			this._fWaspWiggleSeq_s && this._fWaspWiggleSeq_s.pause();
			this._fWaspWiggleXYSeq_s && this._fWaspWiggleXYSeq_s.pause();
			this._fWaspGapSeq_s && this._fWaspGapSeq_s.pause();
			this._fWaspRotationWiggleSeq_s && this._fWaspRotationWiggleSeq_s.pause();
		}

		super._freeze(aIsAnimated_bl);
		
		if (this._fFreezeBaseContainer_sprt && this.container.children[1]) 
		{
			this._fFreezeBaseContainer_sprt.position.set(this.container.children[1].position.x, this.container.children[1].position.y);
			this._fFreezeBaseContainer_sprt.rotation = this.container.children[1].rotation;
		}
	}

	//override
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

		if (this._fGapOffset_obj)
		{
			dx += this._fGapOffset_obj.x;
			dy += this._fGapOffset_obj.y;
		}
		if (this._fWiggleOffset_obj) // for wasps
		{
			dx += this._fWiggleOffset_obj.x;
			dy += this._fWiggleOffset_obj.y;
			dy += this._fWiggleOffset_obj.y2;
			let dr = this._fWiggleOffset_obj.rotation;
			return {x: dx, y: dy, rotation: dr};
		}

		return {x: dx, y: dy};
	}

	//override
	get isCritter()
	{
		return true;
	}

	//override
	destroy(purely = false)
	{
		this.shadow2 = null;
		this.shadow3 = null;
		this.ShadowPositionChange_seq = null;
		this.ShadowPositionChange2_seq = null;

		if (this.view)
		{
			Sequence.destroy(Sequence.findByTarget(this.view));
		}

		if (this._fWiggleOffset_obj)
		{
			Sequence.destroy(Sequence.findByTarget(this._fWiggleOffset_obj));
			this._fWiggleOffset_obj = null;
		}

		if (this._fGapOffset_obj)
		{
			Sequence.destroy(Sequence.findByTarget(this._fGapOffset_obj));
			this._fGapOffset_obj = null;
		}
		this._fWaspWiggleSeq_s = null;
		this._fWaspWiggleXYSeq_s = null;
		this._fWaspGapSeq_s = null;

		super.destroy(purely);
	}
}

export default WaspEnemy;