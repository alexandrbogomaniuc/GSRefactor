import DialogView from '../DialogView';
import MTimeLine from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { Utils } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

const SPINNER_RAYS_AMOUNT = 7;
const SPINNER_RAY_LENGTH = 10; //in px
const SPINNER_RAY_WEIGHT = 3.5; //in px
const SPINNER_FREQUENCY = 90;//in ms
const SPINNER_ANGLE_STEP = +(360 / SPINNER_RAYS_AMOUNT).toFixed(2);

class PleaseWaitDialogView extends DialogView
{
	constructor()
	{
		super();		
	}

	validateSpinner()
	{
		if (this.uiInfo.isActive && this.uiInfo.isPresented)
		{
			this._startSpinnerRotation();
		}
		else
		{
			this._stopSpinnerRotation();

			this._spinner.rotation = 0;
			this._fCurrentRotationPos_num = 0;
		}
	}

	_initDialogView()
	{
		super._initDialogView();

		this._createSpinner();
	}

	_addDialogBase()
	{
		// no need to add base
	}

	get _supportedButtonsCount ()
	{
		return 0;
	}

	_createSpinner()
	{
		this._spinner = this._messageContainer.addChild(new PIXI.Sprite());
		
		for (var i = 0; i < SPINNER_RAYS_AMOUNT; i++)
		{
			let line = new PIXI.Graphics();
			this._spinner.addChild(line);
	
			line.beginFill(0xf5f5f5, 1);
			line.drawRect(5, -SPINNER_RAY_WEIGHT/2, SPINNER_RAY_LENGTH, SPINNER_RAY_WEIGHT);
			line.alpha = 1 - i / (SPINNER_RAYS_AMOUNT-1);
			line.endFill();
			line.rotation  = Utils.gradToRad(SPINNER_ANGLE_STEP * i);

			let lStepAngle_num = SPINNER_ANGLE_STEP*(i+1);
		}

		this._fSpinnerTimer_t = null;
		this._fCurrentRotationPos_num = 0;
	}

	_startSpinnerRotation()
	{
		if (!this._fSpinnerTimer_t)
		{
			this._fSpinnerTimer_t = new Timer(this._onSpinnerUpdateTime.bind(this), SPINNER_FREQUENCY, true);
		}
	}

	_stopSpinnerRotation()
	{
		if (!!this._fSpinnerTimer_t)
		{
			this._fSpinnerTimer_t.destructor();
			this._fSpinnerTimer_t = null;
		}
	}

	_onSpinnerUpdateTime()
	{
		this._updateSpinner();
	}

	_updateSpinner()
	{
		this._fCurrentRotationPos_num += SPINNER_ANGLE_STEP;
		if (this._fCurrentRotationPos_num >= 360)
		{
			this._fCurrentRotationPos_num -= 360;
		}

		this._spinner.rotation = Utils.gradToRad(this._fCurrentRotationPos_num);
	}
}

export default PleaseWaitDialogView;