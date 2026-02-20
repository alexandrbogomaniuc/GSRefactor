import Sprite from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class Carousel extends Sprite
{
	constructor(content)
	{
		super();

		this._fContent_qdil = content;
		this._fPrevButton_btn = null;
		this._fNextButton_btn = null;
		this._fCurrentStepIndex_int = 0;
		this._fInitialX_num = 0;
		
		this._init();
	}

	_init()
	{
		this._addBase();
		this._addContent();
		this._addButtons();
	}

	_addBase()
	{
	}

	_addContent()
	{
		let contentContainer = this.addChild(new Sprite())
		contentContainer.position.set(this._contentContainerPosition.x, this._contentContainerPosition.y);
		
		contentContainer.addChild(this._fContent_qdil);
		this._fInitialX_num = this._fContent_qdil.x;

		let maskArea = 	this._visibleArea;
		let mask = contentContainer.addChild(new PIXI.Graphics());
		mask.beginFill(0x000000).drawRect(maskArea.x, maskArea.y, maskArea.width, maskArea.height).endFill();
		this._fContent_qdil.mask = mask;
	}

	get _contentContainerPosition()
	{
		return new PIXI.Point(0, 0);
	}

	get _visibleArea()
	{
		return new PIXI.Rectangle(0, 0, 100, 100);
	}

	_addButtons()
	{
		let prevBtn = this._fPrevButton_btn = this._generatePrevButtonView();
		prevBtn.on("pointerclick", this._onPrevBtnClicked, this);
		this.addChild(prevBtn);
		
		let nextBtn = this._fNextButton_btn = this._generateNextButtonView();
		nextBtn.on("pointerclick", this._onNextBtnClicked, this);
		this.addChild(nextBtn);

		this._validateArrowButtons();
	}

	get _scrollStep()
	{
		return this._visibleArea.width;
	}

	_validateArrowButtons()
	{
		let stepsAmount = this._stepsAmount;
		
		if (stepsAmount == 0)
		{
			this._disablePrevButton();
			this._disableNextButton();
			return;
		}

		if (this._fCurrentStepIndex_int == 0)
		{
			this._disablePrevButton();
		}
		else
		{
			this._enablePrevButton();
		}

		
		if (stepsAmount <= 0)
		{
			this._disableNextButton();
			return;
		}

		let isLastStep = (this._fCurrentStepIndex_int == (stepsAmount));
		if (isLastStep)
		{
			this._disableNextButton();
		}
		else
		{
			this._enableNextButton();
		}
	}

	_enablePrevButton()
	{
		this._fPrevButton_btn.setEnabled();
	}

	_disablePrevButton()
	{
		this._fPrevButton_btn.setDisabled();
	}

	_enableNextButton()
	{
		this._fNextButton_btn.setEnabled();
	}

	_disableNextButton()
	{
		this._fNextButton_btn.setDisabled();
	}

	_generatePrevButtonView()
	{
		throw new Error("Method must be overridden!");
	}

	_generateNextButtonView()
	{
		throw new Error("Method must be overridden!");
	}

	_onPrevBtnClicked(event)
	{
		this._moveToStep(-1);
		this._validateArrowButtons();
	}

	_onNextBtnClicked(event)
	{
		this._moveToStep(1);
		this._validateArrowButtons();
	}

	get _stepsAmount()
	{
		let contentWidth = this._fContent_qdil ? this._fContent_qdil.localBorders.width * this._fContent_qdil.scale.x : 0;
		if (contentWidth > 0)
		{
			contentWidth -= this._visibleArea.width+this._visibleArea.x+this._fInitialX_num;
		}
		return Math.round(contentWidth/this._scrollStep);
	}

	get _speed()
	{
		return 200;
	}

	_moveToStep(distance, forced=false)
	{
		this._fCurrentStepIndex_int += distance;

		if (this._fCurrentStepIndex_int < 0)
		{
			this._fCurrentStepIndex_int = 0;
		}
		else if (this._fCurrentStepIndex_int > this._stepsAmount)
		{
			this._fCurrentStepIndex_int = this._stepsAmount;
		}

		let newX = -this._fCurrentStepIndex_int * this._scrollStep;
		if (this._fCurrentStepIndex_int === 0)
		{
			newX = this._fInitialX_num;
		}

		if (forced)
		{
			this._fContent_qdil.x = newX;
		}
		else
		{
			this._fContent_qdil.moveTo(newX, this._fContent_qdil.y, this._speed);
		}
	}

	destroy()
	{
		this._fContent_qdil = null;
		this._fPrevButton_btn = null;
		this._fNextButton_btn = null;
		this._fCurrentStepIndex_int = undefined;

		super.destroy();
	}
}

export default Carousel