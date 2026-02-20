import BackgroundTileBaseClassView from '../BackgroundTileBaseClassView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class BackgroundTileRandomAsteroidView extends BackgroundTileBaseClassView
{
	constructor(aIndex_int)
	{
		super();
		let l_rcdo = this._fAsteroidView_sprt = APP.library.getSprite("game/asteroids/asteroid_" + (Math.floor(Math.random() * 7)));
		l_rcdo.anchor.set(0.5, 0.5);
		this.addChild(l_rcdo);
		this.rotatable = (Math.round(Math.random() * 10) % 2) == 0; 
		if(this.rotatable)
		{
			this.rotationDirection = Math.floor(Math.random() * 2);
		}
		this._fRandomIndex_int = aIndex_int;
	}

	get randomIndex()
	{
		return this._fRandomIndex_int;
	}

	randomize(aOffset_num=0)
	{
		this.removeChild(this._fAsteroidView_sprt);
		let l_rcdo = this._fAsteroidView_sprt = APP.library.getSprite("game/asteroids/asteroid_" + (Math.floor(Math.random() * 7)));
		
		this.addChild(l_rcdo);
		this.rotatable = (Math.round(Math.random() * 10) % 2) == 0;
		if(this.rotatable)
		{
			l_rcdo.anchor.set(0.5, 0.5);
			this.rotationDirection = Math.floor(Math.random() * 2);
		}else{
			l_rcdo.anchor.set(0.5, Math.random());
		}
	}
}
export default BackgroundTileRandomAsteroidView;