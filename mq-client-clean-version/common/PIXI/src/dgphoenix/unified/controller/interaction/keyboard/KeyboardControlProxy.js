import EventDispatcher from '../../events/EventDispatcher';

/**
 * Base class to handle keyboard key events
 */
class KeyboardControlProxy extends EventDispatcher
{
	static get i_EVENT_BUTTON_CLICKED()		{ return "onButtonClicked"; }
	static get i_EVENT_BUTTON_SPACE_UP()	{ return "onButtonSpaceUp"; }

	constructor()
	{
		super();

		this._fIsSpaceDown_bl = false;
		this._fIsLeftDown_bl = false;
		this._fIsRightDown_bl = false;
		this._fIsUpDown_bl = false;
		this._fIsDownDown_bl = false;

 		window.addEventListener("keydown", (e) => this.keyDownHandler(e));
 		window.addEventListener("keyup", (e) => this.keyUpHandler(e));
 		window.addEventListener("blur", (e) => this.blurHandler(e));
	}

	keyDownHandler(e)
	{
		this.emit(KeyboardControlProxy.i_EVENT_BUTTON_CLICKED, {code: e.code});
		switch (e.code)
		{
			case "Space":
				this._fIsSpaceDown_bl = true;
			break;
			case "ArrowLeft":
			case "KeyA":
				this._fIsLeftDown_bl = true;
			break;
			case "ArrowRight":
			case "KeyD":
				this._fIsRightDown_bl = true;
			break;
			case "ArrowUp":
			case "KeyW":
				this._fIsUpDown_bl = true;
			break;
			case "ArrowDown":
			case "KeyS":
				this._fIsDownDown_bl = true;
			break;
		}
	}

	keyUpHandler(e)
	{
		this.emit(KeyboardControlProxy.i_EVENT_BUTTON_SPACE_UP , {code: e.code});
		switch (e.code)
		{
			case "Space":
				this._fIsSpaceDown_bl = false;
			break;
			case "ArrowLeft":
			case "KeyA":
				this._fIsLeftDown_bl = false;
			break;
			case "ArrowRight":
			case "KeyD":
				this._fIsRightDown_bl = false;
			break;
			case "ArrowUp":
			case "KeyW":
				this._fIsUpDown_bl = false;
			break;
			case "ArrowDown":
			case "KeyS":
				this._fIsDownDown_bl = false;
			break;
		}
	}

	get isSpaceDown()
	{
		return this._fIsSpaceDown_bl;
	}

	get isLeftDown()
	{
		return this._fIsLeftDown_bl;
	}

	get isRightDown()
	{
		return this._fIsRightDown_bl;
	}

	get isUpDown()
	{
		return this._fIsUpDown_bl;
	}

	get isDownDown()
	{
		return this._fIsDownDown_bl;
	}

	blurHandler(e)
	{
		this._fIsSpaceDown_bl = false;
		this._fIsLeftDown_bl = false;
		this._fIsRightDown_bl = false;
		this._fIsUpDown_bl = false;
		this._fIsDownDown_bl = false;
	}
}

export default KeyboardControlProxy