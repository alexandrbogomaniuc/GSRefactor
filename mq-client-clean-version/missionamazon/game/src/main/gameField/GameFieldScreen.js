import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Z_INDEXES } from '../GameField';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Ticker from '../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Ticker';
import TextField from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';

class GameFieldScreen extends Sprite
{
	get stepsTrailContainer()
	{
		return this._fStepsTrailContainer_sprt;
	}

	removeAllSteps()
	{
		this._fStepsTrailContainer_sprt.destroyChildren();
	}

	constructor()
	{
		super();

		this._initContainers();
	}

	_initContainers()
	{
		this._fStepsTrailContainer_sprt = this.addChild(new Sprite);
		this._fStepsTrailContainer_sprt.zIndex = Z_INDEXES.STEPS_TRAIL;

		//debug...
		// this._startDebugSafeAreas();
		//...debug
		
	}

	//DEBUG...
	// _startDebugSafeAreas()
	// {
	// 	let lTextStyle_obj = {
	// 		fontFamily: "fnt_nm_barlow_semibold",
	// 		fontSize: 12,
	// 		align: "center",
	// 		fill: 0X0000ff
	// 	};

	// 	let areaSafeStartPos = new PIXI.Point(584.5, 195);

	// 	let startPos = this.startPos = new PIXI.Point(areaSafeStartPos.x, areaSafeStartPos.y);
	// 	let ccc = this.ccc = window.cc = this.addChild(new Sprite);
	// 	ccc.position.set(startPos.x, startPos.y);
	// 	ccc.zIndex = this.ccc.y;

	// 	let gr = this.ccc.addChild(new PIXI.Graphics)
	// 	gr.beginFill(0xff00ff, 0.5).drawCircle(0, 0, 4).endFill();

	// 	// let spineView = this.ccc.addChild(APP.spineLibrary.getSprite('enemies/scorpion/GiantScorpion180'));
	// 	// spineView.scale.set(0.2); // 0.4*getScaleCoefficient

	// 	// let spineView = this.ccc.addChild(APP.spineLibrary.getSprite('enemies/mummy_warrior/MummyWarrior0'));
	// 	// spineView.scale.set(0.552);

	// 	// let spineView = this.ccc.addChild(APP.spineLibrary.getSprite('enemies/mummy_small_white/MummySmallWhite180'));
	// 	// spineView.scale.set(0.4*1.1);

	// 	let spineView = this.ccc.addChild(APP.spineLibrary.getSprite('enemies/mummy_god_green/MummyGodGreen0'));
	// 	spineView.scale.set(0.4*1.1);

	// 	spineView.view.skeleton.setToSetupPose();
	// 	spineView.view.autoUpdate = true;
	// 	spineView.view.state.setAnimation(0, "0_walk", true);
		
	// 	let safeArea = this.addChild(new Sprite);
	// 	safeArea.position.set(areaSafeStartPos.x, areaSafeStartPos.y);
	// 	safeArea.zIndex = 10;
		
	// 	let safeAreaTopGr = safeArea.addChild(new PIXI.Graphics);
	// 	safeAreaTopGr.beginFill(0x00ff00, 0.7).drawRect(-1, -400, 2, 400).endFill();
	// 	safeAreaTopGr.beginFill(0x0000ff, 1).drawCircle(0, 0, 3).endFill();
	// 	safeAreaTopGr.rotation = Utils.gradToRad(60);
	// 	let tfTop = safeArea.addChild(new TextField(lTextStyle_obj));
	// 	tfTop.position.set(-70, -10);
	// 	tfTop.text = "(" + areaSafeStartPos.x + "," + areaSafeStartPos.y + ")";

	// 	let safeDownDist = startPos.y-areaSafeStartPos.y;
	// 	let safeAreaBottomGr = safeArea.addChild(new PIXI.Graphics);
	// 	safeAreaBottomGr.y = safeDownDist;
	// 	safeAreaBottomGr.beginFill(0x00ff00, 0.7).drawRect(-1, -400, 2, 400).endFill();
	// 	safeAreaBottomGr.beginFill(0x0000ff, 1).drawCircle(0, 0, 3).endFill();
	// 	safeAreaBottomGr.rotation = Utils.gradToRad(60);
	// 	let tfBottom = safeArea.addChild(new TextField(lTextStyle_obj));
	// 	tfBottom.position.set(-70, -10+safeAreaBottomGr.y);
	// 	tfBottom.text = "(" + areaSafeStartPos.x + "," + (areaSafeStartPos.y+safeAreaBottomGr.y) + ")";
		
	// 	window.addEventListener("keydown", keyDownHandler, false);

	// 	function keyDownHandler(keyCode)
	// 	{
	// 		if (keyCode.keyCode == 107) //+
	// 		{
	// 			startMovement();
	// 		}
	// 		else if (keyCode.keyCode == 109) //-
	// 		{
	// 			resetMovement();
	// 		}
	// 	}
		
	// 	function startMovement()
	// 	{
	// 		let dist = 400;
	// 		let dx = dist*Math.cos(Utils.gradToRad(-30));
	// 		let dy = dist*Math.sin(Utils.gradToRad(-30));

	// 		let endPos = new PIXI.Point(startPos.x+dx, startPos.y+dy);

	// 		ccc.moveTo(endPos.x, endPos.y, 10000)
	// 	}

	// 	function resetMovement()
	// 	{
	// 		ccc.removeTweens();

	// 		ccc.position.set(startPos.x, startPos.y);

	// 		safeDownDist = startPos.y-areaSafeStartPos.y;
	// 		safeAreaBottomGr.y = safeDownDist;

	// 		tfTop.text = "(" + areaSafeStartPos.x + "," + areaSafeStartPos.y + ")";
	// 		tfBottom.text = "(" + areaSafeStartPos.x + "," + (areaSafeStartPos.y+safeAreaBottomGr.y) + ")";
	// 		tfBottom.y = -10+safeAreaBottomGr.y;
	// 	}

	// 	Ticker.on('tick', updateZ, this);

	// 	function updateZ()
	// 	{
	// 		ccc.zIndex = this.ccc.y;
	// 	}
	// }	
	//...DEBUG
}

export default GameFieldScreen;