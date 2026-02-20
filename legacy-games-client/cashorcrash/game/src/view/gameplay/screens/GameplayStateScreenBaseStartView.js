import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class GameplayStateScreenBaseStartView extends Sprite
{
	static get EVENT_ON_TIMER_VALUE_UPDATED ()					{ return "EVENT_ON_TIMER_VALUE_UPDATED"; }

	constructor()
	{
		super();

		this._fShipContainer_sprt = null;

		 /*TODO [os]: move common implementation from BattlegroundGameplayStateScreenStartView/BattlegroundGameplayStateScreenStartView to base class*/
	}

	updateArea()
	{
		// must be overridden
	}

	get shipContainer()
	{
		return this._fShipContainer_sprt;
	}

	get startScreenViewEndTime()
	{
		// must be overridden
		return undefined;
	}

	adjust()
	{
		// must be overridden
	}
}

export default GameplayStateScreenBaseStartView;