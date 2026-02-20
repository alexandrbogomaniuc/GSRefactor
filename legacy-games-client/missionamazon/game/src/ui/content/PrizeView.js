import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class PrizeView extends Sprite
{
	constructor(cratePrizeAssetName, optCratePrizeAssetAnchor, optPrizeAssetTexture)
	{
		super();

		this._cratePrizeAssetName = cratePrizeAssetName;
		this._prizeAssetTexture = optPrizeAssetTexture;
		this._cratePrizeAssetAnchor = optCratePrizeAssetAnchor;

		this._viewContainer = this.addChild(new Sprite());
		this._prizeView = null;

		this._initPrizeView();
	}

	_initPrizeView()
	{
		let view = null;
		
		if (this._cratePrizeAssetName)
		{
			view = this._viewContainer.addChild(APP.library.getSpriteFromAtlas(this._cratePrizeAssetName));
		}
		else
		{
			view = this._viewContainer.addChild(new Sprite);
			view.texture = this._prizeAssetTexture;
		}

		if (this._cratePrizeAssetAnchor !== undefined)
		{
			view.anchor.set(this._cratePrizeAssetAnchor.x, this._cratePrizeAssetAnchor.y);
		}
		
		this._prizeView = view;
	}

	destroy()
	{
		this._cratePrizeAssetName = undefined;
		this._prizeAssetTexture = null;
		this._cratePrizeAssetAnchor = null;

		this._viewContainer = null;
		this._prizeView = null;

		super.destroy();
	}
}

export default PrizeView