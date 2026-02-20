import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

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
		let logo;

		if (APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode)
		{
			logo = this.addChild(I18.generateNewCTranslatableAsset('TAPreloaderTripleMaxBlastLogo'));
			logo.scaleTo(0.75);
		}
		else if (APP.isBattlegroundGame)
		{
			logo = this.addChild(I18.generateNewCTranslatableAsset('TAPreloaderMaxBlastChampionsLogo'));
			logo.scaleTo(0.75);
		}
		else
		{
			logo = this.addChild(I18.generateNewCTranslatableAsset('TAPreloaderLogo'));

			if (I18.currentLocale === 'zh' || I18.currentLocale === 'zh-cn')
			{
				let lPortraitModeLogo = this.addChild(I18.generateNewCTranslatableAsset('TAPreloaderLogoPortrait'));
				if (APP.layout.isPortraitOrientation)
				{
					logo.visible = false;
				}
				else
				{
					lPortraitModeLogo.visible = false;
				}
			}
		}
	}

	destroy()
	{
		super.destroy();
	}
}

export default GamePreloaderLogoView;