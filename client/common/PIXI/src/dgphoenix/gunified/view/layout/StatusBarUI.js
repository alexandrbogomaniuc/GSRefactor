import { APP } from '../../../unified/controller/main/globals';
import {default as BaseUI} from '../../../unified/view/layout/StatusBarUI';

const DEFAULT_WIDTH = 300;
const DEFAULT_HEIGHT = 15;
const INDENT = 5;

const PLACEHOLDER_NAME = '/NAME/';
const PLACEHOLDER_VERSION = '/VERSION/';
const PLACEHOLDER_DATE = '/DATE/';
const PLACEHOLDER_FPS = '/FPS/';

const TEMPLATE_FPS = `. FPS: ${PLACEHOLDER_FPS}`;
const TEMPLATE_MESSAGE = `Game: ${PLACEHOLDER_NAME}. Version: ${PLACEHOLDER_VERSION}. Date: ${PLACEHOLDER_DATE}${TEMPLATE_FPS}`;

class StatusBarUI extends BaseUI 
{

	constructor(layout, name, version, date) 
	{
		super(layout);

		this.baseMessage = TEMPLATE_MESSAGE.replace(PLACEHOLDER_NAME, name).replace(PLACEHOLDER_VERSION, version).replace(PLACEHOLDER_DATE, date);

		this.text = document.createElement('div');
		Object.assign(this.text.style, {
			position:'absolute',
			fontFamily:'serif',
			fontSize:'10px',
			color:'#ffffff',
			whiteSpace:'nowrap',
			top:INDENT+'px',
			left:INDENT+'px',
			padding:'1px',
			paddingLeft:'4px',
			transformOrigin:'left'
		});
		this.container.appendChild(this.text);

		this.base = document.createElement('canvas');
		this.container.appendChild(this.base);
	}

	updateStatusBar(refreshView, fps)
	{
		if (refreshView)
		{
			this.text.innerText = fps ? this.baseMessage.replace(PLACEHOLDER_FPS, "60") : this.baseMessage.replace(TEMPLATE_FPS, "");
			this.drawStatusBarBg(this.text.scrollWidth ? this.text.scrollWidth + 3 : undefined);
		}

		this.text.innerText = fps ? this.baseMessage.replace(PLACEHOLDER_FPS, fps) : this.baseMessage.replace(TEMPLATE_FPS, "");
	}

	drawStatusBarBg(width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT)
	{
		let ctx = this.base.getContext('2d');
		ctx.clearRect(0,0,this.base.width,this.base.height);

		this.base.width = width + INDENT*2;
		this.base.height = height + INDENT*2;
		
		ctx.fillStyle = 'rgba(0,0,0,0.4)';
		this.drawRoundedRect(ctx, INDENT + 0.5, INDENT, width + 1,height + 3, ~~(height*0.64));
		ctx.fill();

		let strokeGradient = ctx.createLinearGradient(INDENT, 0, width, 0);
		strokeGradient.addColorStop(0, "#ff7900");
		strokeGradient.addColorStop(0.5, "#ffff00");
		strokeGradient.addColorStop(1, "#ff7900");
		ctx.strokeStyle = strokeGradient;
		ctx.lineWidth = 1.5;
		this.drawRoundedRect(ctx, INDENT, INDENT, width, height, ~~(height*0.64));
		ctx.fill();
		ctx.stroke();
	}

	drawRoundedRect(ctx, x, y, width, height, radius)
	{
		ctx.beginPath();
		ctx.moveTo(x+radius, y);
		ctx.lineTo(x+width-radius, y);
		ctx.quadraticCurveTo(x+width, y, x+width, y+radius);
		ctx.lineTo(x+width, y+height-radius);
		ctx.quadraticCurveTo(x+width, y+height, x+width-radius, y+height);
		ctx.lineTo(x+radius, y+height);
		ctx.quadraticCurveTo(x, y+height, x, y+height-radius);
		ctx.lineTo(x, y+radius);
		ctx.quadraticCurveTo(x, y, x+radius, y);
		ctx.closePath();
	}
}

export default StatusBarUI;