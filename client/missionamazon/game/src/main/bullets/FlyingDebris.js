import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Bullet from './Bullet';
import PathTween from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/PathTween';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import {Z_INDEXES} from '../GameField';

const LAST_OFFSET_DIST = 30;
const DISTANCE_EPS = 10;

class FlyingDebris extends Bullet {

	constructor(params, points, callback) {
		super(params, points, callback);

		this.shadow = null;
		this._fBaseSprite_sprt;
	}

	//override
	getSpeed(){
		return Utils.random(0.8, 2, true);
	}

	//override
	addFire(){

		let lDebrisSprite_sprt = this._initBaseSprite();
		this._fBaseSprite_sprt = lDebrisSprite_sprt;
		this.fire = this.addChild(lDebrisSprite_sprt);
		this.fire.zIndex = 2;

		let lRandomScale_num = this._getRandomScale();
		this.fire.scale.set(this._getBaseScale() * lRandomScale_num);
		//this.fire.rotation = this.fireRotation;

		this.shadow = this.addChild(new Sprite());
		this.shadow.view = this.shadow.addChild(APP.library.getSprite('shadow'));
		this.shadow.view.alpha = 0.8;
		this.shadow.view.anchor.set(0.45, 0.5);
		this.shadow.view.scale.x = this.fire.scale.x * 0.6;
		this.shadow.view.scale.y = this.fire.scale.y * 0.7;
		this.shadow.zIndex = 1;
	}

	_initBaseSprite()
	{
		let n = Utils.random(0, 3);

		let lDebrisSprite_sprt = APP.library.getSpriteFromAtlas('weapons/GrenadeGun/debris/debris_' + n);
		lDebrisSprite_sprt.rotation = Utils.random(0, Math.PI*2, true);
		return lDebrisSprite_sprt;
	}

	_getBaseScale()
	{
		return 1;
	}

	_getRandomScale()
	{
		return Utils.random(0.9, 1.2, true);
	}

	//override
	showDefaultTrajectory(callback){
		if (this.targetEnemy)
		{
			this._startFollowTargetTrajectory(callback);
		}
		else
		{
			this._startSimpleTrajectory(callback);
		}
	}

	_startSimpleTrajectory(callback)
	{
		this.fire.rotation = Math.PI/6;;

		let len = Math.sqrt(Math.pow(this.startPos.x - this.endPos.x, 2) + Math.pow(this.startPos.y - this.endPos.y, 2));
		let time = 500 + len / this.speed;

		let startX = this.x;
		let startY = this.y;
		let endX = this.endPos.x;
		let endY = this.endPos.y;

		let rx = (this.x + endX) / 2;
		let lHeight_num = this._fParams_obj.maxHeight != null ? this._fParams_obj.maxHeight : 300;
		let ry = (this.y + endY) / 2 - (lHeight_num + 100*this.pointsArray.length);

		let t = new PathTween(this, [{x: this.x, y: this.y}, {x: rx, y: ry}, {x: endX, y: endY}], true);
		t.start(time, Easing.linear.easeOut,
			(e) => {	// finish
				this.shadow.y = 0;
				this.shadow.alpha = 1;

				this._bulletAngle = Math.atan2(this.endPos.x - this.startPos.x, this.endPos.y - this.startPos.y) + Math.PI/2;
				this.endPos = {x: endX, y: endY };
				this._finalize(callback);
			},
			(e) => { // onchange
				let dy = endY - this.y;
				if (e.position < 0.5) {
					dy = startY - this.y;
				}
				if (e.position > 0.3 && e.position < 0.7 || dy < 0){
					this.shadow.alpha = 0;
				} else {
					this.shadow.alpha = 1 - dy/100;
				}
				this.shadow.y = dy;
				if (this.pathCount++ == 0 && e.position < 0.5) {
					this.zIndex = Z_INDEXES.BULLET;
				} else {
					this._updateZIndex();
				}
			}
		);

		//this.scaleTo(0.6, time);
		let n = this.pointsArray.length;
		let sign = n % 2 ? 1 : -1;
		this.fire.rotateTo(this.fire.rotation + Math.PI*2 * sign , time);
	}

	_startFollowTargetTrajectory(callback)
	{
		let len = Math.sqrt(Math.pow(this.x - this.endPos.x, 2) + Math.pow(this.y - this.endPos.y, 2));
		let time = len / this.speed;
		this.shadow.alpha = 0;

		this.on('tick', (e) => {
			let dist = Math.sqrt(Math.pow(this.x - this.endPos.x, 2) + Math.pow(this.y - this.endPos.y, 2));
			if (this.targetEnemy)
			{
				this.endPos = this.targetEnemy ? this.targetEnemy.getGlobalPosition() : this.endPos;
				if (this.randomOffset)
				{
					this.endPos.x += this.randomOffset.x;
					this.endPos.y += this.randomOffset.y;
				}
			}

			let step = e.delta * this.speed;
			if (dist < DISTANCE_EPS || step > dist)
			{
				this.removeAllListeners();
				this.position.set(this.endPos.x, this.endPos.y);
				this._finalize(callback);
				return;
			}

			this._bulletAngle = Math.atan2(this.endPos.x - this.x, this.endPos.y - this.y) + Math.PI/2;
			this.fireRotation = -this._bulletAngle - Math.PI/2;
			this.fire.rotation = this.fireRotation;

			let angle = this._bulletAngle - Math.PI;
			this.x = this.x + Math.cos(angle)*step;
			this.y = this.y - Math.sin(angle)*step;
			this.fire.rotation += Math.PI/180;
			this._updateZIndex();
		});
	}

	_updateZIndex() {
		this.zIndex = this.y + this.fire.y;
	}

	_finalize(callback)
	{
		this.emit('showReboundSmoke', {x: this.x, y: this.y});
		if (callback)
		{
			callback(this, this.endPos, this._bulletAngle);
		}
		if (this.targetEnemy)
		{
			this._prepareForFinalRebound();
		}
		else
		{
			this._startFinalRebound();
		}
	}

	_prepareForFinalRebound()
	{
		this._bulletAngle = this._bulletAngle - Math.PI;

		let currentFootPoint = this.targetEnemy.getCurrentFootPointPosition();

		let lLastOffsetDistanceMult_num = this._fParams_obj.lastOffsetDistanceMult != null ? this._fParams_obj.lastOffsetDistanceMult : 1;

		this.endPos.x = this.endPos.x + currentFootPoint.x - Math.cos(this._bulletAngle)*LAST_OFFSET_DIST * lLastOffsetDistanceMult_num;
		this.endPos.y = this.endPos.y + currentFootPoint.y + Math.sin(this._bulletAngle)*LAST_OFFSET_DIST * lLastOffsetDistanceMult_num;

		this.moveTo(this.endPos.x, this.endPos.y, 150, Easing.quadratic.easeIn, () => {this._startFinalRebound()}, () => { this._updateZIndex(); });
	}

	_startFinalRebound()
	{
		let lLastOffsetDistanceMult_num = this._fParams_obj.lastOffsetDistanceMult != null ? this._fParams_obj.lastOffsetDistanceMult : 1;

		this.endPos.x = this.endPos.x - Math.cos(this._bulletAngle)*LAST_OFFSET_DIST * lLastOffsetDistanceMult_num;
		this.endPos.y = this.endPos.y + Math.sin(this._bulletAngle)*LAST_OFFSET_DIST * lLastOffsetDistanceMult_num;

		let lMinReboundHeight_num = this._fParams_obj.minReboundHeight != null ? this._fParams_obj.minReboundHeight : 30;
		let lMaxReboundHeight_num = this._fParams_obj.maxReboundHeight != null ? this._fParams_obj.maxReboundHeight : 50;

		let lLastRotationDelta_num = this._fParams_obj.lastRotationDelta != null ? this._fParams_obj.lastRotationDelta : Math.PI*2 - Utils.random(0, Math.PI*4, true);

		let sequence = [
			{
				tweens: [
					{prop: 'fire.y', to: -Utils.random(lMinReboundHeight_num, lMaxReboundHeight_num, true)},
					{prop: 'fire.rotation', to: this.fire.rotation + Math.PI - Utils.random(0, Math.PI*2, true)},
					{prop: 'shadow.alpha', to: 0}
				],
				duration: 200 + Utils.random(0, 100),
				ease: Easing.sine.easeOut,
				onchange: () => {this._updateZIndex();}
			},
			{
				tweens: [
					{prop: 'fire.y', to: 0, ease: Easing.bounce.easeOut},
					{prop: 'fire.rotation', to: this.fire.rotation + lLastRotationDelta_num},
					{prop: 'shadow.alpha', to: 1}
				],
				duration: 400 + Utils.random(0, 100),
				onchange: () => {this._updateZIndex();}
			},
			{
				tweens: [],
				duration: 1500
			},
			{
				tweens: [
					{prop: 'alpha', to: 0}
				],
				duration: 700,
				ease: Easing.quadratic.easeOut,
				onfinish: (e) => {
					this.destroy();
				}
			}
		];
		Sequence.start(this, sequence);

		this.moveTo(this.endPos.x, this.endPos.y, 600, Easing.quadratic.easeOut, null, () => { this._updateZIndex(); });
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));
		PathTween.destroy(PathTween.findByTarget(this));

		this.shadow = null;

		super.destroy();
	}

}

export default FlyingDebris;