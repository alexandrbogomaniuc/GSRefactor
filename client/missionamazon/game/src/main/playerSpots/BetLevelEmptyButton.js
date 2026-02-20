import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Button from '../../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';

// This button is an empty container, it handles the click on the hit area of the bet levels
// It is necessary so that other containers do not interfere with the click of the bet level

class BetLevelEmptyButton extends Button {

	static get EVENT_BET_LEVEL_EMPTY_BUTTON_CLICK () { return "EVENT_BET_LEVEL_EMPTY_BUTTON_CLICK"; }
	static get EVENT_BET_LEVEL_EMPTY_BUTTON_RESTRICTED_ZONE () { return "EVENT_BET_LEVEL_EMPTY_BUTTON_RESTRICTED_ZONE"; }

	constructor(aWidth_num, aHeight_num) {
		super();

		this._fButton_b = null;
		this._fButtonWidth_num = aWidth_num;
		this._fButtonHeight_num = aHeight_num;

		this._isRestrictedZone_bl = null;

		//set cursor to default over the empty button
		this.mouseover = () => this.setOverRestrictedZone(true);
		this.mouseout = () => this.setOverRestrictedZone(false);

		this._init()
		this._initButtonBehaviour();
	}

	get betLevelEmptyButtonRestrictedZone()
	{
		return this._isRestrictedZone_bl;
	}

	_init()
	{
		//DEBUG... 
		// Needed to highlight buttons
		// this._fButton_b = this.addChild(new PIXI.Graphics()); 
		// this._fButton_b.beginFill(0xFF0000).drawCircle(0, 0, this._fButtonWidth_num / 2).endFill();
		// this._fButton_b.alpha = 0.2;
		//...DEBUG
	}

	setOverRestrictedZone(aRestricted_bl)
	{
		this._isRestrictedZone_bl = aRestricted_bl;
		var cursorController = APP.currentWindow.cursorController;
		cursorController.setOverRestrictedZone(aRestricted_bl);
		
		this.emit(BetLevelEmptyButton.EVENT_BET_LEVEL_EMPTY_BUTTON_RESTRICTED_ZONE, {isRestricted: aRestricted_bl});
	}

	_initButtonBehaviour()
	{
		this.setHitArea(new PIXI.Circle(0, 0, this._fButtonWidth_num / 2));
		this.setEnabled();

		this.on("pointerdown", (e)=>e.stopPropagation(), this);
		this.on("pointerclick", this._onClicked, this);
	}

	_onClicked(e)
	{
		e.stopPropagation();
		this.emit(BetLevelEmptyButton.EVENT_BET_LEVEL_EMPTY_BUTTON_CLICK);
	}

	destroy()
	{
		this.off("pointerdown", (e)=>e.stopPropagation(), this);
		this.off("pointerclick", this._onClicked, this);

		this.mouseover = null;
		this.mouseout = null;
		this._fButton_b = null;
		this._fButtonWidth_num = null;
		this._fButtonHeight_num = null;
		this._isRestrictedZone_bl = null;

		super.destroy();
	}

}

export default BetLevelEmptyButton;