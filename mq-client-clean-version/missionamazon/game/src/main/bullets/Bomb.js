import Bullet from './Bullet';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import PathTween from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/PathTween';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import Timer from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import {Z_INDEXES} from '../GameField';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import PlayerSpot from '../playerSpots/PlayerSpot';

class Bomb extends Bullet {

	static get EVENT_ON_BOMB_LANDED() {return 'EVENT_ON_BOMB_LANDED';}

	constructor(params, points, callback)
	{
		super(params, points, callback);

		this.bombThrown = null;
		this.shadow = null;
	}

	//override
	getSpeed(){
		return 5;
	}

	_initFireRotation()
	{
		this.fire.rotation = Math.PI/6;
	}

	_finalizeFireRotation(aTime_num)
	{
		let n = this.pointsArray.length;
		let sign = n % 2 ? 1 : -1;
		this.fire.rotateTo(this.fire.rotation + Math.PI*2 * sign , aTime_num);
	}

	showDefaultTrajectory(callback){
		this._initFireRotation();

		let len = Math.sqrt(Math.pow(this.startPos.x - this.endPos.x, 2) + Math.pow(this.startPos.y - this.endPos.y, 2));
		let time = 500 + len / this.speed;

		let startX = this.x;
		let startY = this.y;
		let endX = this.endPos.x;
		let endY = this.endPos.y;
		
		let rx = (this.x + endX) / 2;
		let ry = (this.y + endY) / 2 - (300 + 100*this.pointsArray.length);

		let t = new PathTween(this, [{x: this.x, y: this.y}, {x: rx, y: ry}, {x: endX, y: endY}], true);		
		t.start(time, null,
			(e) => {	// finish
				this.shadow.y = 0;
				this.shadow.alpha = 1;
				
				this._bulletAngle = Math.atan2(this.endPos.x - this.startPos.x, this.endPos.y - this.startPos.y) + Math.PI/2;
				this.endPos = {x: endX, y: endY };
				if (this.pointsArray.length === 0)
				{
					this._startBombDebris(callback);
				}
				else
				{
					this.startPos = Utils.clone(this.endPos);
					this.endPos = this.pointsArray.pop();
					this.showDefaultTrajectory(callback);
				}
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
					this._updateBombZIndex();
				}
			}
		);

		this.scaleTo(this._finalScale, time);
		this._finalizeFireRotation(time);
	}

	get _finalScale()
	{
		return 0.6;
	}

	_updateBombZIndex() {
		// bomb should always be above all
		//this.zIndex = this.y + this.fire.y;
	}
	
	_startBombDebris(callback) {
		this.disappear();
		this.emit(Bomb.EVENT_ON_BOMB_LANDED, {pos: this.endPos});
		callback(this, this.endPos, this._bulletAngle);
	}

	/*override*/
	addFire()
	{
	}

	/*override*/
	destroy(){
		this.bombThrown = null;

		PathTween.destroy(PathTween.findByTarget(this));
		
		this.shadow = null;
		
		super.destroy();
	}
}

export default Bomb