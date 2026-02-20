import { APP } from '../../controller/main/globals';
import EventDispatcher from '../../controller/events/EventDispatcher';

let SPINNER_SIZE = 32; //in px
let SPINNER_RAY_LENGTH = 10; //in px
let SPINNER_RAY_WEIGHT = 3.5; //in px
let SPINNER_RAYS_COLORS = ['#000000', '#232323', '#464646', '#696969', '#8c8c8c', '#afafaf', '#d2d2d2', '#f5f5f5'];
let SPINNER_RAYS_AMOUNT = SPINNER_RAYS_COLORS.length;
let SPINNER_FREQUENCY = 67;//in ms

/**
 * Preloader view that is shown while preloader assets are loading.
 * @class
 * @augments EventDispatcher
 */
class PreLoaderUI extends EventDispatcher 
{

	constructor(layout) 
	{
		super();
		
		this.layout = layout;
		this.container = layout.getLayer('preparepreloader');

		this.createLayout();
		this.addListeners();
	}

	removeAllEventListeners(){
		super.removeAllEventListeners();
		this.removeListeners();
	}

	removeListeners() {
		this.layout.off('fitlayout', this.fitLayout);
	}

	addListeners() {
		this.removeListeners();
		this.layout.on('fitlayout', this.fitLayout, this);
	}

	get spinnerCanvas()
	{
		return this.spinner;
	}

	destructor() {
		super.destructor();

		this.stopSpinnerUpdate();

		this.layout = null;
		this.container = null;
	}

	createLayout() 
	{
		this.back = this.container.appendChild(document.createElement('div'));
		this.back.style.position = 'absolute';
		this.back.style.left = '50%';
		this.back.style.top = '50%';
		this.back.style.backgroundColor = `#000000`;

		this.spinner = this.back.appendChild(document.createElement('canvas'));
		this.spinner.id = 'spinner';
		this.spinner.width = SPINNER_SIZE;
		this.spinner.height = SPINNER_SIZE;
		this.spinner.style.cssText = 'position:relative; top:50%; left:50%;';
		this.spinnerContext = this.spinner.getContext('2d');
		this.spinnerContext.globalAlpha = 0.5;

		this._currentRotationPos = 0;
		this._updateSpinnerTimeout = 0;
		
		this.startSpinnerUpdate();

		this.fitLayout();
	}

	startSpinnerUpdate ()
	{
		this.updateSpinner();
	}

	stopSpinnerUpdate ()
	{
		clearTimeout(this._updateSpinnerTimeout);
	}

	updateSpinner ()
	{
		this._currentRotationPos++;
		if (this._currentRotationPos >= SPINNER_RAYS_AMOUNT)
		{
			this._currentRotationPos = 0;
		}

		this.drawSpinner(this._currentRotationPos);

		this._updateSpinnerTimeout = setTimeout(this.updateSpinner.bind(this), SPINNER_FREQUENCY);
	}

	drawSpinner (aShift_int)
	{
		this.spinnerContext.clearRect(0, 0, SPINNER_SIZE, SPINNER_SIZE);
		
		var lInitPoint_num = SPINNER_SIZE / 2;
		var lBeginRadius_num = lInitPoint_num - SPINNER_RAY_LENGTH;
		var lEndRadius_num = lBeginRadius_num + SPINNER_RAY_LENGTH - SPINNER_RAY_WEIGHT;
		var lAngleStep_num = 2 * Math.PI / SPINNER_RAYS_AMOUNT;
		
		this.spinnerContext.lineWidth = SPINNER_RAY_WEIGHT;
		this.spinnerContext.lineCap = 'round';
		
		for (var i = 0; i < SPINNER_RAYS_AMOUNT; i++)
		{
			var lColorIndex_int = i - aShift_int;
			while (lColorIndex_int < 0)
			{
				lColorIndex_int += SPINNER_RAYS_AMOUNT;
			}
			
			var lCos_num = Math.cos(lAngleStep_num * i);
			var lSin_num = Math.sin(lAngleStep_num * i);
			this.spinnerContext.beginPath();
			this.spinnerContext.moveTo(lInitPoint_num + lCos_num * lBeginRadius_num, lInitPoint_num + lSin_num * lBeginRadius_num);
			this.spinnerContext.lineTo(lInitPoint_num + lCos_num * lEndRadius_num, lInitPoint_num + lSin_num * lEndRadius_num);
			this.spinnerContext.strokeStyle = SPINNER_RAYS_COLORS[lColorIndex_int];
			this.spinnerContext.stroke();
		}
	}

	fitLayout() 
	{
		var scale = APP.layout.scale;

		let w = 960 * scale;
		let h = 540 * scale;
		this.back.style.width = w + 'px';
		this.back.style.marginLeft = -w / 2 + 'px';
		this.back.style.height = h + 'px';
		this.back.style.marginTop = -h / 2 + 'px';
	}
}

export default PreLoaderUI;