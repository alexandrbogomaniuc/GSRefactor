import BackgroundTilesetBaseClassView from './BackgroundTilesetBaseClassView';
import BackgroundTilePurpleSmokeView from "./tiles/BackgroundTilePurpleSmokeView";
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayBackgroundView from './GameplayBackgroundView';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

const MAX_SMOKES_AMOUNT = 3;

class BackgroundPurpleSmokesView extends BackgroundTilesetBaseClassView
{
	constructor()
	{
		super();

		this._fSmokes_arr = [];

		this._fBasePathLen_num = Math.sqrt(this.getTileWidth()*this.getTileWidth() + this.getTileHeight()*this.getTileHeight());
		this._fBaseAngle_num = 150;
	}

	getTileWidth()
	{
		return 1400;
	}

	getTileHeight()
	{
		return 1100;
	}
	//...OVERRIDE

	//OVERRIDE...
	generateTileView(aIndex_int)
	{
		let l_btrav = new BackgroundTilePurpleSmokeView(aIndex_int);

		return l_btrav;
	}
	//...OVERRIDE

	//OVERRIDE...
	expandIfRequired(aWidthInPixels_num, aHeightInPixels_num)
	{
		let l_arr = this._fSmokes_arr;
		for (let i=l_arr.length; i<MAX_SMOKES_AMOUNT; i++)
		{
			let lSmokeView_btrav = this.generateTileView(i);

			let lContainer_sprt = this;
			lContainer_sprt.addChild(lSmokeView_btrav);

			let lSmokeDescr_obj = {};
			lSmokeDescr_obj.smoke = lSmokeView_btrav;
			lSmokeDescr_obj.initialXOffset = this.getTileWidth();
			lSmokeDescr_obj.initialYOffset = -this.getTileHeight();
			lSmokeDescr_obj.startDelay = 0;
			lSmokeDescr_obj.speedBasedDuration = 8600;

			if (i == 0)
			{
				lSmokeDescr_obj.initialYOffset += 380;
			}
			else if (i == 1)
			{
				lSmokeDescr_obj.initialYOffset += 280;
				lSmokeDescr_obj.initialXOffset += 230;
				lSmokeDescr_obj.startDelay = 2800;
				lSmokeDescr_obj.speedBasedDuration = 6000;
			}
			else if (i == 2)
			{
				lSmokeDescr_obj.initialXOffset += -250;
				lSmokeDescr_obj.startDelay = 5000;
				lSmokeDescr_obj.speedBasedDuration = 4000;
			}

			l_arr.push(lSmokeDescr_obj);
		}
	}
	//...OVERRIDE

	//OVERRIDE...
	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_gpv = APP.gameController.gameplayController.view;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lCurGameplayTime_num = l_gpi.gameplayTime;

		let lCurRoundDuration_num = l_gpi.multiplierRoundDuration;
		
		let lMultiplierDelta_num = (l_gpi.multiplierValue - l_gpi.minMultiplierValue);
		let lParalaxSpeedMultiplier = (1-Math.min(0.3, lMultiplierDelta_num*0.05));

		this.expandIfRequired(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height);

		for (let i=0; i<this._fSmokes_arr.length; i++)
		{
			let lSmokeDescr_obj = this._fSmokes_arr[i];

			let l_btrav = lSmokeDescr_obj.smoke;

			if (l_gpv.foregroundContainer && i%2 === 0 && l_btrav.parent === this)
			{
				l_gpv.foregroundContainer.addChild(l_btrav);
			}

			let lSmokeIndex_num = l_btrav.smokeIndex;

			let lIndexRandomValue_num = l_gpi.getPseudoRandomValue(lSmokeIndex_num);
			
			let lPathXLen_num = Math.abs(lSmokeDescr_obj.initialXOffset);
			let lPathYLen_num = Math.abs(lSmokeDescr_obj.initialYOffset);
			let lPathLen_num = Math.sqrt(lPathXLen_num*lPathXLen_num + lPathYLen_num*lPathYLen_num)+300;

			let lPathPercent_num = 0;
			if (lCurRoundDuration_num > lSmokeDescr_obj.startDelay)
			{
				let lSmokeMovePathDuration_num = Math.trunc(lSmokeDescr_obj.speedBasedDuration * (lPathLen_num/this._fBasePathLen_num)) * lParalaxSpeedMultiplier;
				let lSmokeCurCyclePathDuration_num = (lCurRoundDuration_num-lSmokeDescr_obj.startDelay)%lSmokeMovePathDuration_num;

				lPathPercent_num = lSmokeCurCyclePathDuration_num/lSmokeMovePathDuration_num;
			}

			let lAngle_num = this._fBaseAngle_num + 10*lIndexRandomValue_num;
			let lPassedPath_num = lPathPercent_num * lPathLen_num;
			let lOffsetX_num = lPassedPath_num * Math.cos(Utils.gradToRad(lAngle_num));
			let lOffsetY_num = lPassedPath_num * Math.sin(Utils.gradToRad(lAngle_num));

			let lBaseX_num = lSmokeDescr_obj.initialXOffset;
			let lBaseY_num = lSmokeDescr_obj.initialYOffset;

			let lTargetX_num = lBaseX_num + lOffsetX_num;
			let lTargetY_num = lBaseY_num + lOffsetY_num;

			if (l_btrav.parent !== this)
			{
				let lPos_p = this.localToLocal(lTargetX_num, lTargetY_num, l_btrav.parent);
				lTargetX_num = lPos_p.x;
				lTargetY_num = lPos_p.y;
			}

			l_btrav.position.set(lTargetX_num, lTargetY_num);

			l_btrav.visible = true;
		}

		this.position.y = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;
	}
	//...OVERRIDE
}
export default BackgroundPurpleSmokesView;