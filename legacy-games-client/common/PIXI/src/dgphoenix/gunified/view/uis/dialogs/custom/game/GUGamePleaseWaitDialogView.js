import GUDialogView from '../../GUDialogView';

const SPINNER_RAYS_AMOUNT = 7;
const SPINNER_RAY_LENGTH = 10; //in px
const SPINNER_RAY_WEIGHT = 3.5; //in px
const SPINNER_FREQUENCY = 90;//in ms
const SPINNER_ANGLE_STEP = 2 * Math.PI / SPINNER_RAYS_AMOUNT;

class GUGamePleaseWaitDialogView extends GUDialogView
{
	constructor()
	{
		super();

		this._fCurrentRotationPos_num = 0;
		this._fIsDialogActivated_bl = null;
		this._updateSpinnerTimeout = null;
	}

	activateDialog()
	{
		if (!this._spinner || this._fIsDialogActivated_bl)
		{
			return;
		}

		this._fIsDialogActivated_bl = true;
		this.updateSpinner();
	}

	deactivateDialog()
	{
		this._fIsDialogActivated_bl = false;
		clearTimeout(this._updateSpinnerTimeout);
	}

	_initDialogView()
	{
		this._createSpinner();
	}

	_createSpinner()
	{
		this._spinner = this.addChild(new PIXI.Sprite());
		
		for (var i = 0; i < SPINNER_RAYS_AMOUNT; i++)
		{
			let line = new PIXI.Graphics();
			this._spinner.addChild(line);
	
			line.beginFill(0xf5f5f5, 1);
			line.drawRect(5, -SPINNER_RAY_WEIGHT/2, SPINNER_RAY_LENGTH, SPINNER_RAY_WEIGHT);
			line.alpha = 1 - i / (SPINNER_RAYS_AMOUNT-1);
			line.endFill();
			line.rotation  = SPINNER_ANGLE_STEP * i;
		}
	}

	updateSpinner()
	{
		this._fCurrentRotationPos_num += SPINNER_ANGLE_STEP;
		if (this._fCurrentRotationPos_num >= 2 * Math.PI)
		{
			this._fCurrentRotationPos_num -= 2 * Math.PI;
		}

		this._spinner.rotation = this._fCurrentRotationPos_num;

		if (this._fIsDialogActivated_bl)
		{
			this._updateSpinnerTimeout = setTimeout(this.updateSpinner.bind(this), SPINNER_FREQUENCY);
		}		
	}
}

export default GUGamePleaseWaitDialogView;