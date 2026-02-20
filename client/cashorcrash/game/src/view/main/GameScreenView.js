import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class GameScreenView extends Sprite
{
	constructor()
	{
		super();

		this._fGameContainer_sprt = this.addChild(new Sprite);
		
		this._fBottomPanelContainer_sprt = this.addChild(new Sprite);

		this._fSubloadingContainer_sprt = this.addChild(new Sprite);
		this._fSecondaryContainer_sprt = this.addChild(new Sprite);
	}

	get gameViewContainer()
	{
		return this._fGameContainer_sprt;
	}

	get subloadingContainer()
	{
		return this._fSubloadingContainer_sprt;
	}

	get secondaryScreenContainer()
	{
		return this._fSecondaryContainer_sprt;
	}

	get bottomPanelContainer()
	{
		return this._fBottomPanelContainer_sprt;
	}
}
export default GameScreenView;