import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GraphView from './GraphView';
import GameplayInfo from '../../../model/gameplay/GameplayInfo';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../config/AtlasConfig';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class MultiplierRulerView extends Sprite
{
	constructor()
	{
		super();

		this._updateDimensions();

		this._fContentContainer_sprt = null;
		this._fGraphics_g = null;
		this._fTextFields_rctfv_arr = [];
		this._fAdjustmentCoordinateY_num = undefined;
		this._fCurrentMultiplierValueOnTopOfRuler_num = this.getInitialMultiplierValueOnTopOfRulerAccordingMockap();
		this._fCurAdjustedMultiplier_num = undefined;
		this._fShadow_sprt = null;

		//BACKGROUND CONTAINER...
		this._addBackground();
		//...BACKGROUND CONTAINER

		//CONENT CONTAINER...
		this._fContentContainer_sprt = this.addChild(new Sprite);
		this._fLines_gr = this._fContentContainer_sprt.addChild(new PIXI.Graphics());
		//...CONTENT CONTAINER

		this.updateArea();
	}

	_updateDimensions()
	{
		MultiplierRulerView.RULER_WIDTH = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width - GraphView.DEPENDENCY_AREA_BORDER_RIGHT_X;
		MultiplierRulerView.RULER_HEIGHT = GraphView.DEPENDENCY_AREA_HEIGHT;

		MultiplierRulerView.RULER_VISUAL_X_OFFSET = MultiplierRulerView.RULER_WIDTH-60;
		MultiplierRulerView.RULER_VISUAL_WIDTH = MultiplierRulerView.RULER_WIDTH - MultiplierRulerView.RULER_VISUAL_X_OFFSET;
		MultiplierRulerView.RULER_VISUAL_Y_OFFSET = 0;
		MultiplierRulerView.RULER_VISUAL_HEIGHT = GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y;
	}

	_addBackground()
	{
		//DEBUG...
		// this.addChild(new PIXI.Graphics).beginFill(0x00ff00, 0.5).drawRect(MultiplierRulerView.RULER_VISUAL_X_OFFSET, MultiplierRulerView.RULER_VISUAL_Y_OFFSET, MultiplierRulerView.RULER_VISUAL_WIDTH, -MultiplierRulerView.RULER_VISUAL_HEIGHT).endFill();
		// this.addChild(new PIXI.Graphics).beginFill(0x0000ff, 0.5).drawRect(0, 0, MultiplierRulerView.RULER_WIDTH, -MultiplierRulerView.RULER_HEIGHT).endFill();
		//...DEBUG

		let lShadow_sprt = this._fShadow_sprt = this.addChild(new Sprite);
		lShadow_sprt.textures = [MultiplierRulerView.getShadowTextures()[0]];
		lShadow_sprt.anchor.set(0, 0.9);
	}

	//DEFAULT SETTINGS...
	getInitialMultiplierValueOnTopOfRulerAccordingMockap()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		return l_gpi.isPreLaunchFlightRequired ? 1.2 : 2.5;
	}

	getInitialMultiplierStepAccordingMockap()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		return l_gpi.isPreLaunchFlightRequired ? 0.1 : 0.1;
	}

	getStepSizeBreakPointScale()
	{
		return 0.5;
	}
	//...DEFAULT SETTINGS


	getTextFieldView(aIndex_int)
	{
		let l_rctfv_arr = this._fTextFields_rctfv_arr;

		if(!l_rctfv_arr[aIndex_int])
		{
			let lStyle_obj = {
				fontFamily: "fnt_nm_roboto_bold",
				fontSize: 11,
				fill: 0xe4e4e6,
				align: "left"
			};

			let l_rctfv = new TextField(lStyle_obj);
			l_rctfv.anchor.set(0, 0.5);
			l_rctfv.position.x = 30;
			l_rctfv.maxWidth = 26;
			l_rctfv_arr[aIndex_int] = l_rctfv;
			this._fContentContainer_sprt.addChild(l_rctfv);
		}

		return l_rctfv_arr[aIndex_int];
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lMinGameplayMultiplier_num = l_gpi.minMultiplierValue;
		let lCurGameplayMultiplier_num = l_gpi.multiplierValue || lMinGameplayMultiplier_num;

		if (this._fCurAdjustedMultiplier_num === lCurGameplayMultiplier_num)
		{
			return;
		}
		
		let lMultiplierStep_num = this.getInitialMultiplierStepAccordingMockap();
		let lInitialStepsCount_num = (this.getInitialMultiplierValueOnTopOfRulerAccordingMockap() - lMinGameplayMultiplier_num) / lMultiplierStep_num;
		let lStepsCount_num = lInitialStepsCount_num;
		let lInitialStepSizeInPixels_num = MultiplierRulerView.RULER_HEIGHT / lStepsCount_num;
		let lStepSizeInPixels_num = lInitialStepSizeInPixels_num;
		
		let lDesiredRulerCoordinateY_num = -(lCurGameplayMultiplier_num - lMinGameplayMultiplier_num) / lMultiplierStep_num * lStepSizeInPixels_num;
		let lAdjustmentCoordinateY_num = this._fAdjustmentCoordinateY_num;
		let lDownscaleY_num = lAdjustmentCoordinateY_num / lDesiredRulerCoordinateY_num;

		if(lDownscaleY_num > 1)
		{
			lDownscaleY_num = 1;
		}
		
		if(isNaN(lStepsCount_num))
		{
			return;
		}

		lStepSizeInPixels_num *= lDownscaleY_num;
		lStepsCount_num = MultiplierRulerView.RULER_HEIGHT / lStepSizeInPixels_num;

		let lStepSizeInPixelsBreakPoint_num = lInitialStepSizeInPixels_num * this.getStepSizeBreakPointScale();
		let lOverflow_num = lStepSizeInPixelsBreakPoint_num / lStepSizeInPixels_num;
		let lSkipStepsCount_int = Math.trunc(lOverflow_num) + 1;

		let lFinalMultiplierStep_num = lMultiplierStep_num * lSkipStepsCount_int;
		let lFinalMultiplierStepSizeInPixels_num = lStepSizeInPixels_num * lSkipStepsCount_int;
		let lFinalStepsCount_num = Math.trunc(lStepsCount_num / lSkipStepsCount_int)+1;

		let lFilledVisualRulerHeight_num = lFinalMultiplierStepSizeInPixels_num*(lFinalStepsCount_num-1);
		let lEmptyVisualRulerHeight_num = MultiplierRulerView.RULER_VISUAL_HEIGHT - lFilledVisualRulerHeight_num;
		let lAdditionalVisualRulerStepsAmount_num = Math.trunc(lEmptyVisualRulerHeight_num/lFinalMultiplierStepSizeInPixels_num)+1;

		//REFILL...
		this.refillRuler(
			lFinalMultiplierStep_num,
			lFinalMultiplierStepSizeInPixels_num,
			lFinalStepsCount_num + lAdditionalVisualRulerStepsAmount_num);
		//...REFILL

		this._fCurrentMultiplierValueOnTopOfRuler_num = lMultiplierStep_num * lStepsCount_num + lMinGameplayMultiplier_num;

		this._fCurAdjustedMultiplier_num = lCurGameplayMultiplier_num;
	}

	updateArea()
	{
		this._updateDimensions();

		this._fContentContainer_sprt.position.set(MultiplierRulerView.RULER_VISUAL_X_OFFSET, MultiplierRulerView.RULER_VISUAL_Y_OFFSET);

		let lShadow_sprt = this._fShadow_sprt;
		let llShadowLocBounds_r = lShadow_sprt.getLocalBounds();
		lShadow_sprt.scale.set(MultiplierRulerView.RULER_WIDTH/llShadowLocBounds_r.width, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height/llShadowLocBounds_r.height*1.2);

		lShadow_sprt.position.set(0, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height - MultiplierRulerView.RULER_VISUAL_HEIGHT)

		this._fCurAdjustedMultiplier_num = undefined;
	}

	clearRuler()
	{
		for( let i = 0; i < this._fTextFields_rctfv_arr.length; i++ )
		{
			if(this._fTextFields_rctfv_arr[i])
			{
				this._fTextFields_rctfv_arr[i].visible = false;
			}
		}

		this._fLines_gr.clear();
	}

	refillRuler( aMultiplierStep_num, aMultiplierStepSizeInPixels_num, aStepsCount_int )
	{
		this.clearRuler();

		let lFontSize_num = 16;
		let lTextFieldMaximalHeight_num = lFontSize_num;

		let lMultiplierStep_num = aMultiplierStep_num;
		let lMultiplierStepSizeInPixels_num = aMultiplierStepSizeInPixels_num;
		let lStepsCount_num = aStepsCount_int;

		// //PLACING INDICATORS...
		for( let i = 0; i < lStepsCount_num; i++ )
		{
			let lIsEven_bl = i % 2 === 0;

			let lY_num = -i * lMultiplierStepSizeInPixels_num;
			let l_rctfv = this.getTextFieldView(i);
			l_rctfv.text = GameplayInfo.formatMultiplier(1 + i * lMultiplierStep_num);

			l_rctfv.alpha = lIsEven_bl ? 1 : 0.5;

			let txtStyle = l_rctfv.getStyle() || {};
			txtStyle.fontSize = lIsEven_bl ? 11 : 9;
			
			if (l_rctfv.text.length > 8)
			{
				txtStyle.fontSize *= 0.6;
			}
			else if (l_rctfv.text.length > 10)
			{
				txtStyle.fontSize *= 0.5;
			}

			l_rctfv.textFormat = txtStyle;

			l_rctfv.position.y = lY_num;
			l_rctfv.visible = true;
		}
		//...PLACING INDICATORS

		//DRAWING GRAPHICS...
		//WHITE LINES...
		this._fLines_gr.beginFill(0xe4e4e6, 1);

		for( let i = 0; i < lStepsCount_num; i++ )
		{
			let lIsEven_bl = i % 2 === 0; 
			let lLength_num = lIsEven_bl ? 20 : 10;

			let lY_num = -i * lMultiplierStepSizeInPixels_num;

			let lOffsetX_num = lIsEven_bl ? 5 : 13;
			this._fLines_gr.drawRect(
				lOffsetX_num,
				lY_num - 0.75,
				lLength_num,
				1.5);
		}
		//...WHITE LINES
		
		//PIXI TEXTURES CACHING MIP MAP DEBUG...
		this._fLines_gr.drawRect(
				0,
				0,
				MultiplierRulerView.RULER_WIDTH,
				0.001);
		//...PIXI TEXTURES CACHING MIP MAP DEBUG

		this._fLines_gr.endFill();

		this._fLines_gr.beginFill(0xe4e4e6, 0.5);
		//GRAY LINES...
		let lSubStepsCount_int = 3; 
		let lSubsteSizeInPixels_num = lMultiplierStepSizeInPixels_num / (lSubStepsCount_int + 1);

		for( let i = 1; i < lStepsCount_num; i++ )
		{
			for( let j = 1; j <= lSubStepsCount_int; j++ )
			{
				let lY_num = - i * lMultiplierStepSizeInPixels_num + j * lSubsteSizeInPixels_num;
				this._fLines_gr.drawRect(
					16,
					lY_num - 0.75,
					7,
					1.5);
			}
		}
		//...GRAY LINES
		this._fLines_gr.endFill();

		//...DRAWING GRAPHICS
	}


	getMatchingCoordinateYAccordingMockapInitialSettings(aMultiplier_num)
	{
	 	let l_gpi = APP.gameController.gameplayController.info;
	 	let l_gpv = APP.gameController.gameplayController.view;
		let lMultiplierMinimalValue_num = l_gpi.minMultiplierValue;
		let lMultiplierStep_num = this.getInitialMultiplierStepAccordingMockap();		
		let lStepsCount_num = (this.getInitialMultiplierValueOnTopOfRulerAccordingMockap() - lMultiplierMinimalValue_num) / lMultiplierStep_num;
		let lStepSizeInPixels_num = MultiplierRulerView.RULER_HEIGHT / lStepsCount_num;
		let lRulerCoordinateY_num = (aMultiplier_num - lMultiplierMinimalValue_num) / lMultiplierStep_num * lStepSizeInPixels_num;
		
		if(lRulerCoordinateY_num > MultiplierRulerView.RULER_HEIGHT)
		{
			lRulerCoordinateY_num = MultiplierRulerView.RULER_HEIGHT;
		}

		let lResultY_num = GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y - lRulerCoordinateY_num;

		if (aMultiplier_num < lMultiplierMinimalValue_num)
		{
			let lPreLaunchHeight_num = l_gpv.getStarshipLaunchY() - GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y;
			let lDeltaY_num = aMultiplier_num*lPreLaunchHeight_num;
			
			lResultY_num = l_gpv.getStarshipLaunchY() - lDeltaY_num;
		}

		//DEBUG...
		// if(!this._fDebug_g)
		// {
		// 	this._fDebug_g = this.addChild(new PIXI.Graphics());
		// }

		// let l_g = this._fDebug_g;
		// let l_rgv = APP.gameController.gameplayController.view;

		// l_g.clear();
		// l_g.beginFill(0xFF00FF);
		// l_g.drawRect(
		// 	MultiplierRulerView.RULER_WIDTH,
		// 	-lRulerCoordinateY_num,
		// 	-l_rgv.getBounds().width,
		// 	-0.5,
		// 	1 );


		// l_g.endFill();
		//...DEBUG

		return lResultY_num;
	}

	getTargetMultiplierCoordinateY(aMultiplier_num)
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_gpv = APP.gameController.gameplayController.view;
		let lRulerDelta_num = this._fCurrentMultiplierValueOnTopOfRuler_num - l_gpi.minMultiplierValue;
		let lGivenMultiplierDelta_num = aMultiplier_num - l_gpi.minMultiplierValue;
		let lScaleY_num = lGivenMultiplierDelta_num / lRulerDelta_num;

		let lResultY_num = GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y - MultiplierRulerView.RULER_HEIGHT * lScaleY_num;

		let lMultiplierMinimalValue_num = l_gpi.minMultiplierValue;
		if (aMultiplier_num < lMultiplierMinimalValue_num)
		{
			let lPreLaunchHeight_num = l_gpv.getStarshipLaunchY() - GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y;
			let lDeltaY_num = aMultiplier_num*lPreLaunchHeight_num;
			
			lResultY_num = l_gpv.getStarshipLaunchY() - lDeltaY_num;
		}

		return lResultY_num;
	}

	getVisuallyMatchingMultiplierValue(aY_num)
	{
	 	let l_gpi = APP.gameController.gameplayController.info;
		let lMultiplierDelta_num = this._fCurrentMultiplierValueOnTopOfRuler_num - l_gpi.minMultiplierValue;
		let lDeltaY_num = GraphView.DEPENDENCY_AREA_BORDER_BOTTOM_Y - aY_num;
		let lRatio_num = lDeltaY_num / MultiplierRulerView.RULER_HEIGHT;

		return lMultiplierDelta_num * lRatio_num + l_gpi.minMultiplierValue;
	}

	setAdjustmentCoordinateY(aY_num)
	{
		this._fAdjustmentCoordinateY_num = -(MultiplierRulerView.RULER_HEIGHT - aY_num);
	}
}

export default MultiplierRulerView;

MultiplierRulerView.getShadowTextures = function()
{
	if (!MultiplierRulerView.shadow_textures)
	{
		MultiplierRulerView.shadow_textures = [];

		MultiplierRulerView.shadow_textures = AtlasSprite.getFrames([APP.library.getAsset('game/gameplay_assets')], [AtlasConfig.GameplayAssets], 'ruler_shadow');
		MultiplierRulerView.shadow_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return MultiplierRulerView.shadow_textures;
}