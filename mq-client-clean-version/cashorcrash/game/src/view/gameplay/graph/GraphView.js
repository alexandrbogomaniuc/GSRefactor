import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MultiplierRulerView from './MultiplierRulerView';
import TimeRulerView from './TimeRulerView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class GraphView extends Sprite
{
	constructor()
	{
		super();

		this._updateDepencencyArea();

		this._fTargetMultiplier_num = undefined;
		this._fTargetTimeMillis_int = undefined;

		this._fMultiplierRulerView_mrv = null;
		this._fTimeRulerView_trv = null; 
		this._fTimeLine_mt = null;

		//MULTIPLIER RULER...
		let l_mrv = new MultiplierRulerView();
		this._fMultiplierRulerView_mrv = this.addChild(l_mrv);
		//...MULTIPLIER RULER

		//TIME RULER...
		let l_trv = new TimeRulerView();
		this._fTimeRulerView_trv = this.addChild(l_trv);
		//...TIME RULER

		this.updateArea();
	}

	adjust()
	{
		this.setMultiplierAdjustmentCoordinateY(0);  /*TODO [os]: temporary here*/

		this._fTimeRulerView_trv.adjust();
		this._fMultiplierRulerView_mrv.adjust();
	}

	updateArea()
	{
		this._updateDepencencyArea();

		let l_mrv = this._fMultiplierRulerView_mrv;
		l_mrv.position.set(GraphView.DEPENDENCY_AREA_BORDER_RIGHT_X, GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y);
		l_mrv.updateArea();

		let l_trv = this._fTimeRulerView_trv;
		l_trv.position.set(0, GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y);
		l_trv.updateArea();
	}

	_updateDepencencyArea()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_gpv = APP.gameController.gameplayController.view;
		let lIsPortraitMode_bl = APP.layout.isPortraitOrientation;

		//DEPENDENCY AREA...
		GraphView.TURN_DISTANCE_X = 60;
		GraphView.DEPENDENCY_AREA_BORDER_LEFT_X = l_gpi.isPreLaunchFlightRequired ? l_gpv.getStarshipLaunchX()+GraphView.TURN_DISTANCE_X : 0;
		GraphView.DEPENDENCY_AREA_BORDER_RIGHT_X = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width - (l_gpi.isPreLaunchFlightRequired ? Math.floor(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width/2) : 125);
		GraphView.DEPENDENCY_AREA_BORDER_TOP_Y = l_gpi.isPreLaunchFlightRequired ? 204 : 51;
		GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height - (lIsPortraitMode_bl ? 40 : 60);
		GraphView.DEPENDENCY_AREA_WIDTH = GraphView.DEPENDENCY_AREA_BORDER_RIGHT_X - GraphView.DEPENDENCY_AREA_BORDER_LEFT_X;
		GraphView.DEPENDENCY_AREA_HEIGHT = GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y - GraphView.DEPENDENCY_AREA_BORDER_TOP_Y;

		GraphView.DEPENDENCY_AREA_FLIGHT_DISTANCE = Utils.getDistance({x: l_gpv.getStarshipLaunchX(), y: l_gpv.getStarshipLaunchY()},
																		{x: GraphView.DEPENDENCY_AREA_BORDER_RIGHT_X, y: GraphView.DEPENDENCY_AREA_BORDER_TOP_Y});
		//...DEPENDENCY AREA
	}

	setMultiplierAdjustmentCoordinateY(aY_num)
	{
		this._fMultiplierRulerView_mrv.setAdjustmentCoordinateY(aY_num)
	}

	getCorrespondentCoordinateX(aMillisecondIndex_int)
	{
		return this._fTimeRulerView_trv.getCorrespondentCoordinateX(aMillisecondIndex_int);
	}

	getMatchingCoordinateYAccordingMockapInitialSettings(aMultiplier_num)
	{
		return this._fMultiplierRulerView_mrv.getMatchingCoordinateYAccordingMockapInitialSettings(aMultiplier_num);
	}

	getCorrespondentCoordinateY(aMultiplier_num)
	{
		return this._fMultiplierRulerView_mrv.getTargetMultiplierCoordinateY(aMultiplier_num);
	}

	getVisuallyMatchingMultiplierValue(aY_num)
	{
		return this._fMultiplierRulerView_mrv.getVisuallyMatchingMultiplierValue(aY_num);
	}
	
	getVisuallyMatchingMillisecondIndex(aX_num)
	{
		return this._fTimeRulerView_trv.getVisuallyMatchingMillisecondIndex(aX_num);
	}
}

export default GraphView;
