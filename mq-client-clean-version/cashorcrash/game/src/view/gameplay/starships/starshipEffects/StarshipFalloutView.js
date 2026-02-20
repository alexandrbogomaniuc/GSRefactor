import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MTimeLine from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import AsteroidExplosionView from '../../asteroidExplosion/AsteroidExplosionView';
import GameplayBackgroundView from '../../background/GameplayBackgroundView';
import StarshipsPoolView from '../StarshipsPoolView';

const ASTEROIDS_HIT_RADIUS = 60;

class StarshipFalloutView extends Sprite
{
	constructor(aTextures_arr)
	{
		super();

		this._fBodyParts_sprt_arr = [];
		this._fLightedBodyParts_sprt_arr = [];
		this._fBodyPartsDirectionAngles_num_arr = [];
		this._fBodyPartsInitialPositionX_num_arr = [];
		this._fBodyPartsInitialPositionY_num_arr = [];
		this._fFalloutAnimation_rctl = null;
		this._fAsteroidExplosionView = null;
		this._fWhiteFilter_f = null;
		this._fFalloutAnimationTotalDuration_num = undefined;

		let l_sprt_arr = this._fBodyParts_sprt_arr;
		let l_lighted_sprt_arr = this._fLightedBodyParts_sprt_arr;

		//BODY PARTS...
		for( let i = 0; i < 4; i++ )
		{
			let l_sprt = new Sprite;
			l_sprt.textures = aTextures_arr;
			l_sprt.scale.set(0.4);
			l_sprt_arr[i] = this.addChild(l_sprt);
		}
		//...BODY PARTS

		//WHITE BODY PARTS...
		let lWhiteShipView_sprt = this.addChild(new Sprite);
		lWhiteShipView_sprt.textures = aTextures_arr;
		lWhiteShipView_sprt.filters = [this.whiteFilter];
		lWhiteShipView_sprt.anchor.set(0, 0);

		let l_rt = lWhiteShipView_sprt.getBounds();
		var l_txtr = PIXI.RenderTexture.create({width: l_rt.width, height: l_rt.height, scaleMode: PIXI.SCALE_MODES.NEAREST, resolution: 2});
		APP.stage.renderer.render(lWhiteShipView_sprt, { renderTexture: l_txtr});
		lWhiteShipView_sprt.textures = [l_txtr];
		lWhiteShipView_sprt.anchor.set(0.5, 0.5);

		lWhiteShipView_sprt.filters = null;

		let lBodyLocBounds_r = l_sprt_arr[0].getLocalBounds();
		let lHalfBodyWidth_num = lBodyLocBounds_r.width * 0.5;
		let lHalfBodyHeight_num = lBodyLocBounds_r.height * 0.5;

		for (let i = 0; i < 4; i++)
		{
			let l_sprt = new Sprite;
			l_sprt.textures = lWhiteShipView_sprt.textures;
			l_sprt.scale.set(0.4);
			l_lighted_sprt_arr[i] = this.addChild(l_sprt);
		}
		
		lWhiteShipView_sprt.visible = false;
		//...WHITE BODY PARTS

		if (!!GameplayBackgroundView.TILESET_ASTEROIDS)
		{
			this._fAsteroidExplosionView = this.addChild(new AsteroidExplosionView(0, 0));
		}

		//TOP LEFT PART...
		let l_sprt = l_sprt_arr[0];
		let lMask_gr = l_sprt.addChild(new PIXI.Graphics);
		lMask_gr.position.set(-lHalfBodyWidth_num, -lHalfBodyHeight_num);

		lMask_gr.beginFill(0xff0000, 1).drawRect(0, lHalfBodyHeight_num * 0.5, lHalfBodyWidth_num, lHalfBodyHeight_num * 0.5)
										.drawRect(lHalfBodyWidth_num * 0.5, 0, lHalfBodyWidth_num * 0.5, lHalfBodyHeight_num)
										.drawEllipse(0, 0, lHalfBodyWidth_num, lHalfBodyHeight_num)
										.endFill();
		l_sprt.mask = lMask_gr;
		this._fBodyPartsDirectionAngles_num_arr.push(Math.PI*3/4);
		//...TOP LEFT PART

		//TOP RIGHT PART...
		l_sprt = l_sprt_arr[1];
		lMask_gr = l_sprt.addChild(new PIXI.Graphics);
		lMask_gr.position.set(-lHalfBodyWidth_num, -lHalfBodyHeight_num);

		lMask_gr.beginFill(0x00ff00, 1).drawRect(lHalfBodyWidth_num, lHalfBodyHeight_num * 0.5, lHalfBodyWidth_num, lHalfBodyHeight_num * 0.5)
										.drawRect(lHalfBodyWidth_num, 0, lHalfBodyWidth_num * 0.5, lHalfBodyHeight_num)
										.drawEllipse(lHalfBodyWidth_num*2, 0, lHalfBodyWidth_num, lHalfBodyHeight_num)
										.endFill();
		l_sprt.mask = lMask_gr;
		this._fBodyPartsDirectionAngles_num_arr.push(Math.PI/4);
		//...TOP RIGHT PART

		//BOTTOM LEFT PART...
		l_sprt = l_sprt_arr[2];
		lMask_gr = l_sprt.addChild(new PIXI.Graphics);
		lMask_gr.position.set(-lHalfBodyWidth_num, -lHalfBodyHeight_num);

		lMask_gr.beginFill(0x0000ff, 1).drawRect(lHalfBodyWidth_num * 0.5, lHalfBodyHeight_num, lHalfBodyWidth_num * 0.5, lHalfBodyHeight_num)
										.drawRect(0, lHalfBodyHeight_num, lHalfBodyWidth_num, lHalfBodyHeight_num * 0.5)
										.drawEllipse(0, lHalfBodyHeight_num*2, lHalfBodyWidth_num, lHalfBodyHeight_num)
										.endFill();
		l_sprt.mask = lMask_gr;
		this._fBodyPartsDirectionAngles_num_arr.push(-Math.PI*3/4);
		//...BOTTOM LEFT PART

		//BOTTOM RIGHT PART...
		l_sprt = l_sprt_arr[3];
		lMask_gr = l_sprt.addChild(new PIXI.Graphics);
		lMask_gr.position.set(-lHalfBodyWidth_num, -lHalfBodyHeight_num);

		lMask_gr.beginFill(0xcccccc, 1).drawRect(lHalfBodyWidth_num, lHalfBodyHeight_num, lHalfBodyWidth_num * 0.5, lHalfBodyHeight_num)
										.drawRect(lHalfBodyWidth_num, lHalfBodyHeight_num, lHalfBodyWidth_num, lHalfBodyHeight_num * 0.5)
										.drawEllipse(lHalfBodyWidth_num*2, lHalfBodyHeight_num*2, lHalfBodyWidth_num, lHalfBodyHeight_num)
										.endFill();
		l_sprt.mask = lMask_gr;
		this._fBodyPartsDirectionAngles_num_arr.push(-Math.PI/4);
		//...BOTTOM RIGHT PART

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//LIGHTED TOP LEFT PART...
		l_sprt = l_lighted_sprt_arr[0];
		lMask_gr = l_sprt.addChild(new PIXI.Graphics);
		lMask_gr.position.set(-lHalfBodyWidth_num, -lHalfBodyHeight_num);

		lMask_gr.beginFill(0xff0000, 1).drawRect(0, lHalfBodyHeight_num * 0.5, lHalfBodyWidth_num, lHalfBodyHeight_num * 0.5)
										.drawRect(lHalfBodyWidth_num * 0.5, 0, lHalfBodyWidth_num * 0.5, lHalfBodyHeight_num)
										.drawEllipse(0, 0, lHalfBodyWidth_num, lHalfBodyHeight_num)
										.endFill();
		l_sprt.mask = lMask_gr;
		//...LIGHTED TOP LEFT PART

		//LIGHTED TOP RIGHT PART...
		l_sprt = l_lighted_sprt_arr[1];
		lMask_gr = l_sprt.addChild(new PIXI.Graphics);
		lMask_gr.position.set(-lHalfBodyWidth_num, -lHalfBodyHeight_num);

		lMask_gr.beginFill(0x00ff00, 1).drawRect(lHalfBodyWidth_num, lHalfBodyHeight_num * 0.5, lHalfBodyWidth_num, lHalfBodyHeight_num * 0.5)
										.drawRect(lHalfBodyWidth_num, 0, lHalfBodyWidth_num * 0.5, lHalfBodyHeight_num)
										.drawEllipse(lHalfBodyWidth_num*2, 0, lHalfBodyWidth_num, lHalfBodyHeight_num)
										.endFill();
		l_sprt.mask = lMask_gr;
		//...LIGHTED TOP RIGHT PART

		//LIGHTED BOTTOM LEFT PART...
		l_sprt = l_lighted_sprt_arr[2];
		lMask_gr = l_sprt.addChild(new PIXI.Graphics);
		lMask_gr.position.set(-lHalfBodyWidth_num, -lHalfBodyHeight_num);

		lMask_gr.beginFill(0x0000ff, 1).drawRect(lHalfBodyWidth_num * 0.5, lHalfBodyHeight_num, lHalfBodyWidth_num * 0.5, lHalfBodyHeight_num)
										.drawRect(0, lHalfBodyHeight_num, lHalfBodyWidth_num, lHalfBodyHeight_num * 0.5)
										.drawEllipse(0, lHalfBodyHeight_num*2, lHalfBodyWidth_num, lHalfBodyHeight_num)
										.endFill();
		l_sprt.mask = lMask_gr;
		//...LIGHTED BOTTOM LEFT PART

		//LIGHTED BOTTOM RIGHT PART...
		l_sprt = l_lighted_sprt_arr[3];
		lMask_gr = l_sprt.addChild(new PIXI.Graphics);
		lMask_gr.position.set(-lHalfBodyWidth_num, -lHalfBodyHeight_num);

		lMask_gr.beginFill(0xcccccc, 1).drawRect(lHalfBodyWidth_num, lHalfBodyHeight_num, lHalfBodyWidth_num * 0.5, lHalfBodyHeight_num)
										.drawRect(lHalfBodyWidth_num, lHalfBodyHeight_num, lHalfBodyWidth_num, lHalfBodyHeight_num * 0.5)
										.drawEllipse(lHalfBodyWidth_num*2, lHalfBodyHeight_num*2, lHalfBodyWidth_num, lHalfBodyHeight_num)
										.endFill();
		l_sprt.mask = lMask_gr;
		//...LIGHTED BOTTOM RIGHT PART

		for( let i = 0; i < l_sprt_arr.length; i++ )
		{
			this._fBodyPartsInitialPositionX_num_arr[i] = l_sprt_arr[i].x;
			this._fBodyPartsInitialPositionY_num_arr[i] = l_sprt_arr[i].y;
		}

		//FALLOUT ANIMATION...
		let l_rctl = new MTimeLine();

		if (!APP.isBattlegroundGame)
		{
			l_rctl.addAnimation(
				this.falloutBodyParts,
				MTimeLine.EXECUTE_METHOD,
				0,
				[
					[150, 25, MTimeLine.EASE_OUT]
				],
				this);

			l_rctl.addAnimation(
				this.rotateBodyParts,
				MTimeLine.EXECUTE_METHOD,
				0,
				[
					[360, 25]
				],
				this);

		l_rctl.addAnimation(
			this.scaleBodyParts,
			MTimeLine.EXECUTE_METHOD,
			0.4,
			[
				10,
				[0, 15, MTimeLine.EASE_IN]
			],
			this);

			l_rctl.addAnimation(
				this.alphaBodyParts,
				MTimeLine.EXECUTE_METHOD,
				0,
				[
					6, 
					[1, 24]
				],
				this);
		}
		else
		{
			l_rctl.addAnimation(
				this.falloutBodyParts,
				MTimeLine.EXECUTE_METHOD,
				0,
				[
					[500, 38]
				],
				this);

			l_rctl.addAnimation(
				this.rotateBodyParts,
				MTimeLine.EXECUTE_METHOD,
				0,
				[
					[1000, 29]
				],
				this);

			l_rctl.addAnimation(
				this.scaleBodyParts,
				MTimeLine.EXECUTE_METHOD,
				0.4,
				[
					11,
					[0, 18]
				],
				this);
		}

		l_rctl.addAnimation(
			this.alphaLightedAsteroidParts,
			MTimeLine.EXECUTE_METHOD,
			1, 
			[
				12,
				[0, 19]
			],
			this);

		this._fFalloutAnimation_rctl = l_rctl;

		this._fFalloutAnimationTotalDuration_num = this._fFalloutAnimation_rctl.getTotalDurationInMilliseconds();
		//...FALLOUT ANIMATION	
	}

	get whiteFilter()
	{
		return this._fWhiteFilter_f || (this._fWhiteFilter_f = this._initWhiteFilter());
	}

	_initWhiteFilter()
	{
		let lWhiteFilter_cmf = new PIXI.filters.ColorMatrixFilter();
		lWhiteFilter_cmf.matrix = [0,0,0,0,255, 0,0,0,0,255, 0,0,0,0,255, 0,0,0,1,0];

		return lWhiteFilter_cmf;
	}


	falloutBodyParts(aDistance_num)
	{
		let l_sprt_arr = this._fBodyParts_sprt_arr;
		let l_lighted_sprt_arr = this._fLightedBodyParts_sprt_arr;

		for( let i = 0; i < l_sprt_arr.length; i++ )
		{
			l_sprt_arr[i].position.set(
					this._fBodyPartsInitialPositionX_num_arr[i] + aDistance_num * Math.cos(this._fBodyPartsDirectionAngles_num_arr[i]),
					this._fBodyPartsInitialPositionY_num_arr[i] + aDistance_num * Math.sin(this._fBodyPartsDirectionAngles_num_arr[i]));

			l_lighted_sprt_arr[i].position.set(l_sprt_arr[i].position.x, l_sprt_arr[i].position.y);
		}
	}

	alphaBodyParts(aAlhpa_num)
	{
		let l_sprt_arr = this._fBodyParts_sprt_arr;

		for (let i = 0; i < l_sprt_arr.length; i++)
		{
			l_sprt_arr[i].alpha = aAlhpa_num;
		}
	}

	alphaLightedAsteroidParts(aAlhpa_num)
	{
		let l_sprt_arr = this._fLightedBodyParts_sprt_arr;

		for (let i = 0; i < l_sprt_arr.length; i++)
		{
			l_sprt_arr[i].alpha = aAlhpa_num;
		}
	}

	rotateBodyParts(aRotationInDegrees_num)
	{
		let l_sprt_arr = this._fBodyParts_sprt_arr;
		let lRotationInDegrees_num = aRotationInDegrees_num;
		let l_lighted_sprt_arr = this._fLightedBodyParts_sprt_arr;

		for( let i = 0; i < l_sprt_arr.length; i++ )
		{
			lRotationInDegrees_num *= -1;

			l_sprt_arr[i].rotation = Utils.gradToRad(lRotationInDegrees_num);
			l_lighted_sprt_arr[i].rotation = Utils.gradToRad(lRotationInDegrees_num);
		}
	}

	scaleBodyParts(aScale_num)
	{
		let l_sprt_arr = this._fBodyParts_sprt_arr;
		let l_lighted_sprt_arr = this._fLightedBodyParts_sprt_arr;

		for( let i = 0; i < l_sprt_arr.length; i++ )
		{
			l_sprt_arr[i].scale.set(aScale_num);
			l_lighted_sprt_arr[i].scale.set(aScale_num);
		}
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_ri = l_gpi.roundInfo;
		let l_rsv = StarshipsPoolView.STARSHIP;
		let l_brasv = GameplayBackgroundView.TILESET_ASTEROIDS;

		if (l_ri.isRoundPlayActive)
		{
			this._resetAnimation();
			this.visible = false;
		}
		else
		{
			let lOutOfRoundDuration_num = l_gpi.outOfRoundDuration;
			let lTotalAnimationDuration_num = this._fFalloutAnimationTotalDuration_num;
			if (this._fAsteroidExplosionView && this._fAsteroidExplosionView.getTotalAnimationDuration() > lTotalAnimationDuration_num)
			{
				lTotalAnimationDuration_num = this._fAsteroidExplosionView.getTotalAnimationDuration();
			}

			this.visible = lOutOfRoundDuration_num > 0 && lOutOfRoundDuration_num <= lTotalAnimationDuration_num;

			if (this.visible)
			{
				if (!this._fFalloutAnimation_rctl.isPlaying() && !this._fFalloutAnimation_rctl.isCompleted())
				{
					this._fFalloutAnimation_rctl.playFromMillisecond(lOutOfRoundDuration_num);
				}

				if (this._fAsteroidExplosionView)
				{
					let lTargetAsteroid_btrav = null;
					let lAsteroids_arr = l_brasv.asteroids;
					let lPotentialHitAsteroids_arr = [];
					let lHitRect_r = new PIXI.Rectangle(l_rsv.position.x-ASTEROIDS_HIT_RADIUS*0.5, l_rsv.position.y-ASTEROIDS_HIT_RADIUS, ASTEROIDS_HIT_RADIUS*1.5, ASTEROIDS_HIT_RADIUS*2);
					for (let i=0; i<lAsteroids_arr.length; i++)
					{
						let lCurAsteroid = lAsteroids_arr[i];
						if (Utils.isPointInsideRect(lHitRect_r, lCurAsteroid.position))
						{
							lPotentialHitAsteroids_arr.push(lCurAsteroid);
						}
					}

					if (!!lPotentialHitAsteroids_arr.length)
					{
						lTargetAsteroid_btrav = lPotentialHitAsteroids_arr[0];
						for (let i=1; i<lPotentialHitAsteroids_arr.length; i++)
						{
							let lCurAsteroid = lAsteroids_arr[i];
							if (Utils.getDistance(l_rsv.position, lCurAsteroid.position) < Utils.getDistance(l_rsv.position, lTargetAsteroid_btrav.position))
							{
								lTargetAsteroid_btrav = lCurAsteroid;
							}
						}
					}
					
					if (lTargetAsteroid_btrav)
					{
						lTargetAsteroid_btrav.visible = false;
						let lLocalAsteroidPos_p = lTargetAsteroid_btrav.parent.localToLocal(lTargetAsteroid_btrav.position.x, lTargetAsteroid_btrav.position.y, this);
						this._fAsteroidExplosionView.position.set(lLocalAsteroidPos_p.x, lLocalAsteroidPos_p.y);
					}
					else
					{
						let lLocalRightTopPos_p = l_rsv.parent.localToLocal(l_rsv.position.x, l_rsv.position.y, this);
						this._fAsteroidExplosionView.position.set(lLocalRightTopPos_p.x - ASTEROIDS_HIT_RADIUS*0.5, lLocalRightTopPos_p.y - ASTEROIDS_HIT_RADIUS*0.5);
					}

					this._fAsteroidExplosionView.adjust();
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
		this._fFalloutAnimation_rctl.reset();

		this._fAsteroidExplosionView && this._fAsteroidExplosionView.deactivate();
	}
}
export default StarshipFalloutView;