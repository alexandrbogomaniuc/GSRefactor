import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

const WIDTH = 3;

class ScrollBarView extends Sprite
{
	static get EVENT_ON_SCROLL () { return "EVENT_ON_SCROLL" };

	constructor()
	{
		super();

		this._fScrollProgressInPixels_num = 0;
		this._fTotalScrollHeightInPixels_num = 1000;
		this._fVisibleHeightInPixels_num = 0;

		this._fHolder_g = null;
		this._fButton_g = null;

		this._fButtonClickY_num = undefined;

		this.initGrphics();
	}

	initGrphics()
	{
		this._fHolder_g = new PIXI.Graphics();
		this._fHolder_g.beginFill(0x484848).drawRect(0, 0, 1, 1).endFill();
		this._fHolder_g.position.set(0, 0);

		this.addChild(this._fHolder_g);

		this._fButton_g = new PIXI.Graphics();
		this._fButton_g.beginFill(0xffca13).drawRect(0, 0, 1, 1).endFill();
		this._fButton_g.position.set(0, 0);
		this._fButton_g.interactive = true;
		this._fButton_g.buttonMode = true;
		this._fButton_g.defaultCursor = "crosshair";

		this._fButton_g.on('mousedown', this._onScrollBarButtonMouseDown.bind(this));
		document.addEventListener('mouseup', this._onMouseUp.bind(this));
		//document.addEventListener('mousemove', this._onMouseMove.bind(this));
		//this.on("mousemove", this._onLinkMouseMove, this);
		this.addChild(this._fButton_g);
	}

	_onScrollBarButtonMouseDown(event)
	{
		this._fButtonClickY_num = event.data.local.y + this._fVisibleHeightInPixels_num / 2 - this._fButton_g.position.y;
	}

	_onMouseUp()
	{
		this._fButtonClickY_num = undefined;
	}

	onMouseMove(aMouseX_num, aMouseY_num)
	{
		if(this._fButtonClickY_num === undefined)
		{
			return;
		}

		this._fButton_g.y = aMouseY_num - this._fButtonClickY_num;

		let lBorderBottomY_num = this._fVisibleHeightInPixels_num - this._fButton_g.height;
		let lBorderTopY_num = 1;


		if(this._fButton_g.y > lBorderBottomY_num)
		{
			this._fButton_g.y = lBorderBottomY_num;
		}
		else if(this._fButton_g.y < lBorderTopY_num)
		{
			this._fButton_g.y = lBorderTopY_num;
		}

		//let lProgress_num = this._fScrollProgressInPixels_num / lTotalScrollHeight_num;

		let lScrollProgressInPixels_num = (this._fButton_g.y / this._fVisibleHeightInPixels_num) * this._fTotalScrollHeightInPixels_num;




		//this._fButton_g.y = lProgress_num * lVisibleHeight_num;

		this.emit(ScrollBarView.EVENT_ON_SCROLL, {scroll: lScrollProgressInPixels_num});

	}


	setVisibleHeightInPixels(aHeight_num)
	{
		this._fVisibleHeightInPixels_num = aHeight_num;
		this.adjust();
	}

	setTotalScrollHeightInPixels(aHeight_num)
	{
		this._fTotalScrollHeightInPixels_num = aHeight_num;
		this.adjust();
	}

	setScrollProgressInPixels(aProgress_num)
	{
		this._fScrollProgressInPixels_num = aProgress_num;
		this.adjust();
	}

	adjust()
	{
		if(this._fButtonClickY_num !== undefined)
		{
			return;
		}

		let lVisibleHeight_num = this._fVisibleHeightInPixels_num;
		let lTotalScrollHeight_num = this._fTotalScrollHeightInPixels_num;

		if(lVisibleHeight_num >= lTotalScrollHeight_num)
		{
			this.visible = false;
			return;
		}

		this.visible = true;

		this._fHolder_g.scale.set(WIDTH, lVisibleHeight_num);
		this._fButton_g.scale.set(WIDTH, lVisibleHeight_num * lVisibleHeight_num / lTotalScrollHeight_num);
		
		let lProgress_num = this._fScrollProgressInPixels_num / lTotalScrollHeight_num;
		this._fButton_g.y = lProgress_num * lVisibleHeight_num;
	}
}

export default ScrollBarView;