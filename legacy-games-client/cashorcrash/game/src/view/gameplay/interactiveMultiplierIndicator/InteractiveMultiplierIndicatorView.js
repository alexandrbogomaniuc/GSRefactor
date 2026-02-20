import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import InteractiveMultiplierIndicatorLabelView from './InteractiveMultiplierIndicatorLabelView';
import Button from '../../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';
import DialogController from '../../../controller/uis/custom/dialogs/DialogController';

const DASH = {width: 7, height: 2, space: 5};

class InteractiveMultiplierIndicatorView extends Button
{
	constructor()
	{
		super();

		this._fLastMouseOverGlobalPosition_p = undefined;
		this._fAxisContainer_sprt = null;
		this._fLine_gr = null;
		this._fIndicatorView_imilv = null;
		this._fCurrentMultiplierValueOnAxis_num = undefined;
		this._fIsOver_bl = false;
		this._fIsPointerMoveTurnOff_bl = false;

		this._updateDimntions();

		this._fAxisContainer_sprt = this.addChild(new Sprite);

		this._fLine_gr = this._fAxisContainer_sprt.addChild(new PIXI.Graphics);
		this._redrawAxisLine();
		
		//INDICATOR...
		let l_imilv = new InteractiveMultiplierIndicatorLabelView();
		this._fAxisContainer_sprt.addChild(l_imilv);
		l_imilv.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x, DASH.height+1);
		this._fIndicatorView_imilv = l_imilv;
		//...INDICATOR

		this.updateArea();
		this._addPointerMoveListener();
		
		let dialogsController = APP.dialogsController;
		dialogsController.on(DialogController.EVENT_DIALOG_ACTIVATED, this._removePointerMoveListener, this);
		dialogsController.on(DialogController.EVENT_DIALOG_DEACTIVATED, this._addPointerMoveListener, this);
	}

	_addPointerMoveListener()
	{
		this.on("pointermove", this._onPointerMove, this);
		this._fIsPointerMoveTurnOff_bl = false;		
	}
	
	_removePointerMoveListener()
	{
		this.off("pointermove", this._onPointerMove, this);
		this._fIsPointerMoveTurnOff_bl = true;
	}

	_updateDimntions()
	{
		this.setHitArea(new PIXI.Rectangle(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height));
	}
	
	adjust()
	{
		let lAxisContainer_sprt = this._fAxisContainer_sprt;
		let lIndicatorView_imilv = this._fIndicatorView_imilv;

		if (!this._fIsOver_bl || !this._fLastMouseOverGlobalPosition_p || this._fIsPointerMoveTurnOff_bl)
		{
			lAxisContainer_sprt.visible = false;
			return;
		}

		let l_gpv = APP.gameController.gameplayController.view;
		let lGraphView_rgv = l_gpv.graphView;
		let lLocalGraphCoordinate_p = l_gpv.parent.globalToLocal(this._fLastMouseOverGlobalPosition_p.x, this._fLastMouseOverGlobalPosition_p.y);

		let lMultiplier_num = lGraphView_rgv.getVisuallyMatchingMultiplierValue(lLocalGraphCoordinate_p.y);
		this._fCurrentMultiplierValueOnAxis_num = lMultiplier_num;

		lAxisContainer_sprt.visible = lMultiplier_num >= 1;
		lAxisContainer_sprt.y = this._fLastMouseOverGlobalPosition_p.y;

		lIndicatorView_imilv.setValue(lMultiplier_num);
		lIndicatorView_imilv.x = this._fLastMouseOverGlobalPosition_p.x + 20;

		let lIndicatorViewBounds_r = lIndicatorView_imilv.getBounds();
		let lIndicatorRightBorder_num = lIndicatorViewBounds_r.x + lIndicatorViewBounds_r.width;
		let lZoneRightBorder_num = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width;
		if (lIndicatorRightBorder_num > lZoneRightBorder_num)
		{
			lIndicatorView_imilv.x -= lIndicatorRightBorder_num - lZoneRightBorder_num;
		}
	}

	updateArea()
	{
		this._updateDimntions();

		this._redrawAxisLine();
	}

	_redrawAxisLine()
	{
		let lHitArea_r = this.getHitArea();
		let l_gr = this._fLine_gr;

		l_gr.position.set(lHitArea_r.x, lHitArea_r.y);

		l_gr.cacheAsBitmap = false;
		l_gr.clear();

		let lDashWidth_num = DASH.width;
		let lDashHeight_num = DASH.height;
		let lSpaceWidth_num = DASH.space;
		let lStepWidth_num = lDashWidth_num + lSpaceWidth_num;
		let lStepsCount_int = Math.trunc(lHitArea_r.width / lStepWidth_num) + 1;

		l_gr.beginFill(0xFFFFFF, 0.5);
		for( let i = 0; i < lStepsCount_int; i++ )
		{
			l_gr.drawRect(i * lStepWidth_num, -lDashHeight_num/2, lDashWidth_num, lDashHeight_num);
		}
		l_gr.endFill();
	}

	_onPointerMove(event)
	{
		if (Utils.isPointInsideRect(this.getHitArea(), event.data.local))
		{
			this._updateLastMouseOverGlobalPosition(event.data.global);
		}
		else
		{
			this._updateLastMouseOverGlobalPosition(undefined);
		}
	}

	handleOver()
	{
		this._fIsOver_bl = true;
	}

	handleOut()
	{
		this._fIsOver_bl = false;
	}

	_updateLastMouseOverGlobalPosition(pos)
	{
		if (!pos)
		{
			this._fLastMouseOverGlobalPosition_p = undefined;
			return;
		}

		this._fLastMouseOverGlobalPosition_p = new PIXI.Point(pos.x, pos.y);
	}
}

export default InteractiveMultiplierIndicatorView;