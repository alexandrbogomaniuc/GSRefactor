import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

export const Z_INDEXES = {
					SHADOW : 0,
					LOGO : 2
				};

class GamePreloaderLogoView extends Sprite
{
	constructor()
	{
		super();

		this._initView();
	}

	_initView()
	{
		this._addLogo();
	}

	_addLogo()
	{
		let logo = this.addChild(I18.generateNewCTranslatableAsset('TALobbyPreloaderLogo'));
		logo.zIndex = Z_INDEXES.LOGO;
	}

	destroy()
	{
		super.destroy();
	}
}

export default GamePreloaderLogoView;