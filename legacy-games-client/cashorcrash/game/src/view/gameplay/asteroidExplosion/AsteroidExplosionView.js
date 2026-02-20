import { Sprite } from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import MTimeLine from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine";
import { APP } from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { AtlasSprite } from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import AtlasConfig from "../../../config/AtlasConfig";

class AsteroidExplosionView extends Sprite
{
	constructor(aRocketPartsPositionX, aRocketPartsPositionY)
	{
		super();

		this._fAsteroidParts_sprt_arr = [];
		this._fAsteroidPartsDirectionAngles_num_arr = [];
		this._fAsteroidPartsInitialPositionX_num_arr = [];
		this._fAsteroidPartsInitialPositionY_num_arr = [];
		this._fWhiteAsteroidParts_sprt_arr= [];
		this._fExplosionAnimation_rctl = null;
		this._fExplosionAnimationTotalDuration_num = undefined;

		let lRocketPartXCoordinate_num = aRocketPartsPositionX;
		let lRocketPartYCoordinate_num = aRocketPartsPositionY;
		let l_sprt_arr = this._fAsteroidParts_sprt_arr;
		let l_white_sprt_arr = this._fWhiteAsteroidParts_sprt_arr;	

		//TWO TOP ASTEROID PARTS...

		let l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/layer_4");
		this.addChild(l_sprt);
		l_sprt_arr.push(l_sprt);
		this._fAsteroidPartsDirectionAngles_num_arr.push(Math.PI*5/6);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/layer_5");
		this.addChild(l_sprt);
		l_sprt_arr.push(l_sprt);
		this._fAsteroidPartsDirectionAngles_num_arr.push(Math.PI*3/4);
		//...TWO TOP ASTEROID PARTS...

		//RIGHT ASTEROID PARTS...
		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/layer_3");
		this.addChild(l_sprt);
		l_sprt_arr.push(l_sprt);
		this._fAsteroidPartsDirectionAngles_num_arr.push(Math.PI*3/2);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/layer_3");
		this.addChild(l_sprt);
		l_sprt_arr.push(l_sprt);
		this._fAsteroidPartsDirectionAngles_num_arr.push(Math.PI*11/6);
		//...RIGHT ASTEROID PARTS

		//BOTTOM ASTEROID PARTS...
		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/layer_2");
		this.addChild(l_sprt);
		l_sprt_arr.push(l_sprt);
		this._fAsteroidPartsDirectionAngles_num_arr.push(Math.PI*2);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/layer_2");
		this.addChild(l_sprt);
		l_sprt_arr.push(l_sprt);
		this._fAsteroidPartsDirectionAngles_num_arr.push(Math.PI/3);
		//...BOTTOM ASTEROID PARTS

		//LEFT ASTEROID PARTS...
		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/layer_5");
		this.addChild(l_sprt);
		l_sprt_arr.push(l_sprt);
		this._fAsteroidPartsDirectionAngles_num_arr.push(Math.PI/2);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/layer_4");
		this.addChild(l_sprt);
		l_sprt_arr.push(l_sprt);
		this._fAsteroidPartsDirectionAngles_num_arr.push(Math.PI*5/4);
		//...LEFT ASTEROID PARTS

		//WHITE PARTS...
		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/lighted_layer_4");
		this.addChild(l_sprt);
		l_white_sprt_arr.push(l_sprt);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/lighted_layer_4");
		this.addChild(l_sprt);
		l_white_sprt_arr.push(l_sprt);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/lighted_layer_5");
		this.addChild(l_sprt);
		l_white_sprt_arr.push(l_sprt);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/lighted_layer_3");
		this.addChild(l_sprt);
		l_white_sprt_arr.push(l_sprt);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/lighted_layer_3");
		this.addChild(l_sprt);
		l_white_sprt_arr.push(l_sprt);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/lighted_layer_2");
		this.addChild(l_sprt);
		l_white_sprt_arr.push(l_sprt);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/lighted_layer_2");
		this.addChild(l_sprt);
		l_white_sprt_arr.push(l_sprt);

		l_sprt = APP.library.getSprite("game/asteroid_and_parts/parts/lighted_layer_5");
		this.addChild(l_sprt);
		l_white_sprt_arr.push(l_sprt);
		//...WHITE PARTS

		for (let i = 0; i < l_sprt_arr.length; i++)
		{
			this._fAsteroidPartsInitialPositionX_num_arr[i] = lRocketPartXCoordinate_num;
			this._fAsteroidPartsInitialPositionY_num_arr[i] = lRocketPartYCoordinate_num;
			l_sprt_arr[i].visible = true;
		}

		//DEBUG...
		// this.addChild(new PIXI.Graphics).beginFill(0xff0000).drawCircle(0, 0, 8).endFill();
		// for (let i = 0; i < l_sprt_arr.length; i++)
		// {
		// 	this.addChild(new PIXI.Graphics).beginFill(0x00ff00).drawCircle(this._fAsteroidPartsInitialPositionX_num_arr[i], this._fAsteroidPartsInitialPositionY_num_arr[i], 5).endFill();
		// }
		//...DEBUG

		//EXPLOSION ANIMATION...
		let l_aecl = new MTimeLine();
		l_aecl.addAnimation(
			this.falloutAsteroidParts,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				5,
				[400, 30, MTimeLine.EASE_OUT]
			],
			this);
		
		l_aecl.addAnimation(
			this.rotateAsteroidParts,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				[360, 30]
			],
			this);
		
		l_aecl.addAnimation(
			this.scaleAsteroidParts,
			MTimeLine.EXECUTE_METHOD,
			0.5,
			[
				11,
				[0, 18, MTimeLine.EASE_IN]
			],
			this);

		l_aecl.addAnimation(
			this.scaleFasterAsteroidParts,
			MTimeLine.EXECUTE_METHOD,
			0.5,
			[
				11,
				[0, 10, MTimeLine.EASE_IN]
			],
			this);

		l_aecl.addAnimation(
			this.alphaAsteroidParts,
			MTimeLine.EXECUTE_METHOD,
			1,
			[
				11,
				[0, 19]
			],
			this);

		l_aecl.addAnimation(
			this.alphaWhiteAsteroidParts,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				6, 
				[1, 24]
			],
			this);

		this._fExplosionAnimation_rctl = l_aecl;

		this._fExplosionAnimationTotalDuration_num = this._fExplosionAnimation_rctl.getTotalDurationInMilliseconds();
		//...EXPLOSION ANIMTION
	}

	getTotalAnimationDuration()
	{
		return this._fExplosionAnimationTotalDuration_num;
	}

	falloutAsteroidParts(aDistance_num)
	{
		let l_sprt_arr = this._fAsteroidParts_sprt_arr;
		
		let lWhiteAsteroidParts_sprt_arr = this._fWhiteAsteroidParts_sprt_arr;

		for (let i = 0; i < l_sprt_arr.length; i++)
		{
			l_sprt_arr[i].position.set(
				this._fAsteroidPartsInitialPositionX_num_arr[i] + aDistance_num * Math.cos(this._fAsteroidPartsDirectionAngles_num_arr[i]),
				this._fAsteroidPartsInitialPositionY_num_arr[i] + aDistance_num * Math.sin(this._fAsteroidPartsDirectionAngles_num_arr[i]));

			lWhiteAsteroidParts_sprt_arr[i].position.set(l_sprt_arr[i].position.x, l_sprt_arr[i].position.y);
		}
	}

	rotateAsteroidParts(aRotationInDegrees_num)
	{
		let l_sprt_arr = this._fAsteroidParts_sprt_arr;
		let lWhiteAsteroidParts_sprt_arr = this._fWhiteAsteroidParts_sprt_arr;
		let lRotationInDegrees_num = aRotationInDegrees_num;

		for (let i = 0; i < l_sprt_arr.length; i++){
			lRotationInDegrees_num *= -1;

			l_sprt_arr[i].rotation = Utils.gradToRad(lRotationInDegrees_num);
			lWhiteAsteroidParts_sprt_arr[i].rotation = Utils.gradToRad(lRotationInDegrees_num);
		}
	}

	scaleAsteroidParts(aScale_num)
	{
		let l_sprt_arr = this._fAsteroidParts_sprt_arr;
		let lWhiteAsteroidParts_sprt_arr = this._fWhiteAsteroidParts_sprt_arr;

		for (let i = 0; i < l_sprt_arr.length; i++)
		{
			if (i === 4 || i === 5)
			{
				continue;
			}
			l_sprt_arr[i].scale.set(aScale_num);
			lWhiteAsteroidParts_sprt_arr[i].scale.set(aScale_num);
		}
	}

	scaleFasterAsteroidParts(aScale_num)
	{
		let l_sprt_arr = this._fAsteroidParts_sprt_arr;
		let lWhiteAsteroidParts_sprt_arr = this._fWhiteAsteroidParts_sprt_arr;

		for (let i = 4; i < 6; i++)
		{
			l_sprt_arr[i].scale.set(aScale_num);
			lWhiteAsteroidParts_sprt_arr[i].scale.set(aScale_num);
		}
	}

	alphaAsteroidParts(aAlhpa_num)
	{
		let l_sprt_arr = this._fAsteroidParts_sprt_arr;

		for (let i = 0; i < l_sprt_arr.length; i++)
		{
			l_sprt_arr[i].alpha = aAlhpa_num;
		}
	}
	
	alphaWhiteAsteroidParts(aAlhpa_num)
	{
		let l_sprt_arr = this._fWhiteAsteroidParts_sprt_arr;
		for (let i = 0; i < l_sprt_arr.length; i++)
		{
			l_sprt_arr[i].alpha = aAlhpa_num;
		}
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_ri = l_gpi.roundInfo;

		if (l_ri.isRoundPlayActive)
		{
			this._resetAnimation();
			this.visible = false;
		}
		else
		{
			let lOutOfRoundDuration_num = l_gpi.outOfRoundDuration;
			let lTotalAnimationDuration_num = this._fExplosionAnimationTotalDuration_num;
			
			this.visible = lOutOfRoundDuration_num > 0 && lOutOfRoundDuration_num <= lTotalAnimationDuration_num;

			if (this.visible)
			{
				if (!this._fExplosionAnimation_rctl.isPlaying() && !this._fExplosionAnimation_rctl.isCompleted())
				{
					this._fExplosionAnimation_rctl.playFromMillisecond(lOutOfRoundDuration_num);
				}
			}
			else
			{
				this._resetAnimation();
			}
		}
	}

	deactivate()
	{
		this._resetAnimation();
	}

	_resetAnimation()
	{
		this._fExplosionAnimation_rctl.reset();
	}
}
export default AsteroidExplosionView;