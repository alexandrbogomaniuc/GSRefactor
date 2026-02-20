import Sprite from '../../../unified/view/base/display/Sprite';
import Tween from '../../../unified/controller/animation/Tween';

class GUSLobbyGameLaunchUI extends Sprite 
{
	constructor(appPreLoader)
	{
		super();

		this._back = null;
		this._spinner = null;
		this.appPreLoader = appPreLoader;

		this.createView();
		this.hide();
	}

	createView() 
	{
		this.drawBack();
		this.drawSpinner();
	}

	drawBack()
	{
		this._back = new PIXI.Graphics();
		this.addChild(this._back);

		this._back.beginFill(0x000000, 1);
		this._back.drawRect(-480, -280, 960, 560);
		this._back.endFill();

		this._back.cacheAsBitmap = true;
	}

	drawSpinner()
	{
		let myCanvas = this.appPreLoader.spinnerCanvas;
		this._spinner = new PIXI.Sprite(PIXI.Texture.from(myCanvas));
		this._spinner.anchor.set(0.5);
		this._spinner.scale.set(0.5);

		this.addChild(this._spinner);
	}

	startAnimations()
	{
		this._spinner.rotation = 0;

		this.tween = new Tween(this._spinner, 'rotation', 0, Math.PI * 2, 1000);
		this.tween.autoRewind = true;
		this.tween.play();
	}

	stopAnimations()
	{
		if (!this.tween)
		{
			return;
		}
		this.tween.stop();
		this.tween.destructor();
		this.tween = null;
	}

	hide()
	{
		this.stopAnimations();

		super.hide();
	}

	show()
	{
		this.startAnimations();

		super.show();
	}
}

export default GUSLobbyGameLaunchUI;