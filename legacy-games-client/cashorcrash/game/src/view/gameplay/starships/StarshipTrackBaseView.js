import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayInfo from '../../../model/gameplay/GameplayInfo';
import StarshipsPoolView from './StarshipsPoolView';
import { Bezier } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/math/Bezier';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import GraphView from '../graph/GraphView';

class StarshipTrackBaseView extends Sprite
{
	constructor()
	{
		super();

		this._fLines_gr = this.addChild(new PIXI.Graphics());
		this._fCurAdjustedRoundDuration_num = undefined;
		this._fTrackCurvePoints_p_arr = null;
		this._fTrackMultiplierBasedPoints_p_arr = null;
	}

	//DEFAULT SETTINGS...
	getColor()
	{
		return 0xFF6600;
	}

	getTileWidth()
	{
		return this.getStepWidth();
	}

	getStepWidth()
	{
		return 75;
	}

	getLineThickness()
	{
		return 5;
	}

	getLineMinimalThickness()
	{
		return this.getLineThickness();
	}

	getLineWobbling()
	{
		return 3;
	}

	getLineMinimalWobbling()
	{
		return this.getLineWobbling();
	}

	getDownscaleXSpeed()
	{
		return 1000;
	}

	getTileOutroMillisecondsCount()
	{
		return 30000;
	}
	//...DEFAULT SETTINGS

	get trackLength()
	{
		return this.getTrackDistance();
	}

	getTrackDistance(aFrom_p=undefined, aTo_p=undefined)
	{
		if (!this._fTrackMultiplierBasedPoints_p_arr || !this._fTrackMultiplierBasedPoints_p_arr.length)
		{
			return 0;
		}

		let lTrackFirstPoint_p = this._fTrackMultiplierBasedPoints_p_arr[0];
		let lTrackLastPoint_p = this._fTrackMultiplierBasedPoints_p_arr[this._fTrackMultiplierBasedPoints_p_arr.length-1];

		let lFrom_p = aFrom_p || lTrackFirstPoint_p;
		if (lFrom_p.x < lTrackFirstPoint_p.x)
		{
			lFrom_p = lTrackFirstPoint_p;
		}
		let lTo_p = aTo_p || lTrackLastPoint_p;
		if (lTo_p.x > lTrackLastPoint_p.x)
		{
			lTo_p = lTrackLastPoint_p;
		}

		let l_num = 0;
		for (let i=0; i<this._fTrackMultiplierBasedPoints_p_arr.length; i++)
		{
			let lCurPoint_p = this._fTrackMultiplierBasedPoints_p_arr[i];
			if (lCurPoint_p.x <= lFrom_p.x)
			{
				continue;
			}

			let lPrevPoint_p = this._fTrackMultiplierBasedPoints_p_arr[i-1];
			if (lPrevPoint_p.x <= lFrom_p.x)
			{
				if (lCurPoint_p.x < lTo_p.x)
				{
					l_num += Utils.getDistance(lFrom_p, lCurPoint_p);
				}
				else
				{
					l_num += Utils.getDistance(lFrom_p, lTo_p);
					break;
				}
			}
			else if (lCurPoint_p.x >= lTo_p.x)
			{
				l_num += Utils.getDistance(lPrevPoint_p, lTo_p);
				break;
			}
			else
			{
				l_num += Utils.getDistance(lPrevPoint_p, lCurPoint_p);
			}
		}

		return l_num;
	}

	getPointInDistance(aBasePoint_p, aDistance_num)
	{
		if (!this._fTrackMultiplierBasedPoints_p_arr || !this._fTrackMultiplierBasedPoints_p_arr.length)
		{
			return aBasePoint_p;
		}

		let lTrackFirstPoint_p = this._fTrackMultiplierBasedPoints_p_arr[0];
		let lTrackLastPoint_p = this._fTrackMultiplierBasedPoints_p_arr[this._fTrackMultiplierBasedPoints_p_arr.length-1];

		if (aBasePoint_p.x < lTrackFirstPoint_p.x)
		{
			aBasePoint_p = lTrackFirstPoint_p;
		}

		let lMaxPointTrackDistance_num = this.getTrackDistance(aBasePoint_p);
		if (aDistance_num >= lMaxPointTrackDistance_num)
		{
			return lTrackLastPoint_p;
		}

		let lDistance_num = aDistance_num;

		let lTargetPoint_p = aBasePoint_p;
		let lRightOfBasePoint_p = lTrackLastPoint_p;
		for (let i=1; i<this._fTrackMultiplierBasedPoints_p_arr.length; i++)
		{
			if (this._fTrackMultiplierBasedPoints_p_arr[i].x < aBasePoint_p.x)
			{
				continue;
			}

			lRightOfBasePoint_p = this._fTrackMultiplierBasedPoints_p_arr[i];
			let lCurBasePoint_p = aBasePoint_p;
			for (let j=i; j<this._fTrackMultiplierBasedPoints_p_arr.length-1; j++)
			{
				let lDistToRightOfBasePoint_num = Utils.getDistance(lCurBasePoint_p, lRightOfBasePoint_p);
				if (lDistance_num > lDistToRightOfBasePoint_num)
				{
					lDistance_num -= lDistToRightOfBasePoint_num;
					lCurBasePoint_p = lRightOfBasePoint_p;
					lRightOfBasePoint_p = this._fTrackMultiplierBasedPoints_p_arr[j+1];
					continue;
				}

				let lAnglePointsDistance_num = Utils.getDistance(lCurBasePoint_p, lRightOfBasePoint_p);
				let lCos_num = Math.abs(lRightOfBasePoint_p.x - lCurBasePoint_p.x) / lAnglePointsDistance_num;
				let lSin_num = Math.abs(lRightOfBasePoint_p.y - lCurBasePoint_p.y) / lAnglePointsDistance_num;

				lTargetPoint_p.x = lCurBasePoint_p.x+lDistance_num*lCos_num;
				lTargetPoint_p.y = lCurBasePoint_p.y-lDistance_num*lSin_num;
				break;
			}

			break;
		}

		return lTargetPoint_p;
	}

	getCurvePoint(aMillisecondIndex_int)
	{
		if (!this._fTrackCurvePoints_p_arr)
		{
			return null;
		}

		let l_gpi = APP.gameController.gameplayController.info;

		let lRoundMillisecondIndex_int = l_gpi.multiplierRoundDuration;
		let lPreLaunchFlightDuration_int = l_gpi.preLaunchFlightDuration;
		let lFlightMillisecondIndex_int = lRoundMillisecondIndex_int + lPreLaunchFlightDuration_int;

		let lCurveLength_num = Bezier.getCurveLen(this._fTrackCurvePoints_p_arr);
		let lTileTrackPathLength_num = lCurveLength_num * aMillisecondIndex_int/lFlightMillisecondIndex_int;
		
		return Bezier.getCurvePoint(this._fTrackCurvePoints_p_arr, lTileTrackPathLength_num);
	}

	getCrashCurvePoint(aMultiplier_num)
	{
		if (!this._fTrackMultiplierBasedPoints_p_arr)
		{
			return null;
		}

		let l_gpi = APP.gameController.gameplayController.info;

		let lGivenMultiplierDelta_num = aMultiplier_num - l_gpi.minMultiplierValue;
		let lCurrentMultiplierDelta_num = l_gpi.multiplierValue - l_gpi.minMultiplierValue;

		let lCurveLength_num = Bezier.getCurveLen(this._fTrackMultiplierBasedPoints_p_arr);

		let lTileTrackPathLength_num = lCurveLength_num * lGivenMultiplierDelta_num/lCurrentMultiplierDelta_num;
		
		return Bezier.getCurvePoint(this._fTrackMultiplierBasedPoints_p_arr, lTileTrackPathLength_num);
	}

	getBTGCurvePoint(aMultiplier_num)
	{
		if (!this._fTrackCurvePoints_p_arr)
		{
			return null;
		}

		let l_gpi = APP.gameController.gameplayController.info;
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;

		let lGivenMultiplierDelta_num = aMultiplier_num - l_gpi.minMultiplierValue;
		let lCurrentMultiplierDelta_num = l_gpi.multiplierValue - l_gpi.minMultiplierValue;

		let lCurveLength_num = Bezier.getCurveLen(this._fTrackCurvePoints_p_arr);

		let lPaddingTrack = lIsPortraitMode_bl ? 30 : 90;

		let lTileTrackPathLength_num = lPaddingTrack + (lCurveLength_num - lPaddingTrack) * lGivenMultiplierDelta_num/lCurrentMultiplierDelta_num;
		
		return Bezier.getCurvePoint(this._fTrackCurvePoints_p_arr, lTileTrackPathLength_num);
	}

	getCurveVisualMatchingMillisecondIndex(aX_num)
	{
		if (!this._fTrackCurvePoints_p_arr)
		{
			return undefined;
		}

		let l_gpi = APP.gameController.gameplayController.info;

		let lRoundMillisecondIndex_int = l_gpi.multiplierRoundDuration;
		let lPreLaunchFlightDuration_int = l_gpi.preLaunchFlightDuration;
		let lFlightMillisecondIndex_int = lRoundMillisecondIndex_int + lPreLaunchFlightDuration_int;

		let lCurveLength_num = Bezier.getCurveLen(this._fTrackCurvePoints_p_arr);
		let lTileTrackPathLength_num = lCurveLength_num * aMillisecondIndex_int/lFlightMillisecondIndex_int;

		return Bezier.getCurvePoint(this._fTrackCurvePoints_p_arr, lTileTrackPathLength_num);
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_gpv = APP.gameController.gameplayController.view;
		let l_rsv = StarshipsPoolView.STARSHIP;
		let lGraphView_rgv = l_gpv.graphView;

		let lRoundMillisecondIndex_int = l_gpi.multiplierRoundDuration;
		let lPreLaunchFlightDuration_int = l_gpi.preLaunchFlightDuration;
		let lFlightMillisecondIndex_int = lRoundMillisecondIndex_int + lPreLaunchFlightDuration_int;

		if (APP.isBattlegroundGame && l_gpv.battlegroundYouWonView.isAnimationInProgress)
		{
			this._fCurAdjustedRoundDuration_num = undefined;
			this._fLines_gr.clear();
			return;
		}

		if (this._fCurAdjustedRoundDuration_num === lRoundMillisecondIndex_int)
		{
			return;
		}

		this._fLines_gr.clear();

		if (lRoundMillisecondIndex_int === 0)
		{
			return;
		}

		if (lPreLaunchFlightDuration_int > 0)
		{
			let lEndPoint_p = new PIXI.Point(l_rsv.position.x, l_rsv.position.y);
			let lDirection_num = l_rsv.rotation + Math.PI/2 - Utils.gradToRad(7);
			let lDistance_num = GraphView.DEPENDENCY_AREA_FLIGHT_DISTANCE + 10;
			let lStartPoint_p = new PIXI.Point(lEndPoint_p.x + lDistance_num*Math.cos(lDirection_num), lEndPoint_p.y + lDistance_num*Math.sin(lDirection_num));
			
			this._fTrackCurvePoints_p_arr = Bezier.getCurve([{x: lStartPoint_p.x, y:lStartPoint_p.y},
																{x: lStartPoint_p.x+Math.abs(lEndPoint_p.x-lStartPoint_p.x)*0.35, y: lStartPoint_p.y - Math.abs(lEndPoint_p.y-lStartPoint_p.y)*0.55},
																{x: lEndPoint_p.x, y:lEndPoint_p.y}
															]);
		}

		this._fTrackMultiplierBasedPoints_p_arr = [];
		
		let lMillisecondsPerTileCount_int = 1000;
		let lMaximalTilesCount_int = 300;
		let lTilesCount_int = lFlightMillisecondIndex_int / lMillisecondsPerTileCount_int;
		let lTrackUpscaleX_num = lTilesCount_int / lMaximalTilesCount_int;
		let lLineThicknessScale_num = (1 - lFlightMillisecondIndex_int / 180000) || 1;
		
		if(lLineThicknessScale_num < 0.75)
		{
			lLineThicknessScale_num = 0.75;
		}
		lLineThicknessScale_num = 1;

		let lLineThicknessSideDistance_num = this.getLineThickness()/2*lLineThicknessScale_num;

		if(lTrackUpscaleX_num < 1)
		{
			lTrackUpscaleX_num = 1;
		}

		if(lTilesCount_int > lMaximalTilesCount_int)
		{
			lTilesCount_int = lMaximalTilesCount_int;
		}

		let lStarshipActualMillisecondIndex_int = lGraphView_rgv.getVisuallyMatchingMillisecondIndex(l_rsv.position.x);
		let lScaleX_num = lStarshipActualMillisecondIndex_int / lFlightMillisecondIndex_int || 1;
		
		let lLineWobblingOffsetTemplate_num = this.getLineWobbling();

		lTilesCount_int = Math.trunc(lTilesCount_int) + 1;		
		
		let lSecMult_num = (lFlightMillisecondIndex_int%lMillisecondsPerTileCount_int)/lMillisecondsPerTileCount_int;
		let lIsEvenSec_bl = (~~(lFlightMillisecondIndex_int/1000)%2) === 0;

		let lPrevPoligonNeighbor_arr = null;
		let lTrackCurveLength_num = !!this._fTrackCurvePoints_p_arr ? Bezier.getCurveLen(this._fTrackCurvePoints_p_arr) : undefined;
		
		const lPreLaunchTrackFillTotalDuration_num = 500;
		this.alpha = Math.min(lRoundMillisecondIndex_int/lPreLaunchTrackFillTotalDuration_num, 1);

		let lPrevNeighborBasePoint_p = null;
		for( let i = 0; i <= lTilesCount_int + 2; i++ )
		{
			let lTileMillisecondIndex_int = i * lMillisecondsPerTileCount_int * lTrackUpscaleX_num;

			if (lTileMillisecondIndex_int > lFlightMillisecondIndex_int)
			{
				lTileMillisecondIndex_int = lFlightMillisecondIndex_int;
			}

			let lLineWobblingOffset_num = 0;
			if (i % 2 !== 0)
			{
				lLineWobblingOffset_num = lLineWobblingOffsetTemplate_num * Math.min(i/lTilesCount_int, 1);

				if (lIsEvenSec_bl)
				{
					lLineWobblingOffset_num = lLineWobblingOffset_num - lLineWobblingOffset_num * lSecMult_num;
				}
				else
				{
					lLineWobblingOffset_num = lLineWobblingOffset_num * lSecMult_num;
				}
			}

			let lTargetCurvePoint_p = null;
			if (!!this._fTrackCurvePoints_p_arr)
			{
				let lTileTrackPathLength_num = lTrackCurveLength_num * lTileMillisecondIndex_int/lFlightMillisecondIndex_int;
				lTargetCurvePoint_p = Bezier.getCurvePoint(this._fTrackCurvePoints_p_arr, lTileTrackPathLength_num);
			}

			let lTargetPointX_num = lTargetCurvePoint_p ? lTargetCurvePoint_p.x : lGraphView_rgv.getCorrespondentCoordinateX(lTileMillisecondIndex_int) * lScaleX_num;

			let lMultiplier_num = l_gpi.calculateMultiplier(lTileMillisecondIndex_int);
			let lMultiplierY_num = lGraphView_rgv.getCorrespondentCoordinateY(lMultiplier_num);
			
			let lPrevTrackMultBasedPoint_p = !!this._fTrackMultiplierBasedPoints_p_arr.length ? this._fTrackMultiplierBasedPoints_p_arr[this._fTrackMultiplierBasedPoints_p_arr.length-1] : null;

			if (!!lPrevTrackMultBasedPoint_p)
			{
				let lDeltaY_num = Math.abs(lMultiplierY_num - lPrevTrackMultBasedPoint_p.y);

				if (lDeltaY_num < (lLineWobblingOffset_num*2))
				{
					lLineWobblingOffset_num *= lDeltaY_num/(lLineWobblingOffset_num*2);
				}
			}

			let lTargetMultiplierBasedY_num = lTargetCurvePoint_p ? lTargetCurvePoint_p.y : lMultiplierY_num;
			let lTargetPointY_num = lTargetMultiplierBasedY_num + lLineWobblingOffset_num;

			if (!!lPrevNeighborBasePoint_p)
			{
				let lStepDeltaX_num = Math.abs(lPrevNeighborBasePoint_p.x - lTargetPointX_num);
				let lStepDeltaY_num = Math.abs(lPrevNeighborBasePoint_p.y - lTargetPointY_num);

				let lFinalAngle_num = Math.PI/2-Math.atan2(lStepDeltaX_num, lStepDeltaY_num);
				let lFinalDX_num = Math.sin(lFinalAngle_num) * lLineThicknessSideDistance_num;
				let lFinalDY_num = Math.cos(lFinalAngle_num) * lLineThicknessSideDistance_num;

				if (!lPrevPoligonNeighbor_arr)
				{
					lPrevPoligonNeighbor_arr = [ 
													lPrevNeighborBasePoint_p.x-lFinalDX_num, lPrevNeighborBasePoint_p.y - lFinalDY_num,
													lPrevNeighborBasePoint_p.x+lFinalDX_num, lPrevNeighborBasePoint_p.y + lFinalDY_num
												];
				}

				let lPolygon_arr = [
					lPrevPoligonNeighbor_arr[0], lPrevPoligonNeighbor_arr[1],
					lTargetPointX_num-lFinalDX_num, lTargetPointY_num - lFinalDY_num,
					lTargetPointX_num+lFinalDX_num, lTargetPointY_num + lFinalDY_num,
					lPrevPoligonNeighbor_arr[2], lPrevPoligonNeighbor_arr[3]
				]

				this._fLines_gr.beginFill(this.getColor()).drawPolygon(lPolygon_arr).endFill();

				lPrevPoligonNeighbor_arr = lPolygon_arr.slice(2, 6);
			}

			lPrevNeighborBasePoint_p = new PIXI.Point(lTargetPointX_num, lTargetPointY_num);

			this._fTrackMultiplierBasedPoints_p_arr.push(new PIXI.Point(lTargetPointX_num, lTargetMultiplierBasedY_num));
		}
		
		this._fCurAdjustedRoundDuration_num = lRoundMillisecondIndex_int;
	}

	updateArea()
	{
		this._fCurAdjustedRoundDuration_num = undefined;
	}
}

export default StarshipTrackBaseView