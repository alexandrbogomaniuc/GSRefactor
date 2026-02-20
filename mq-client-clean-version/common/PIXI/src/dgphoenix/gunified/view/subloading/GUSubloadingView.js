import SimpleUIView from '../../../unified/view/base/SimpleUIView';
import PreloadingSpinner from '../custom/PreloadingSpinner';
import Sprite from '../../../unified/view/base/display/Sprite';

class GUSubloadingView extends SimpleUIView
{
	constructor()
	{
		super();

		this.waitScreen = null;
	}

	i_showLoadingScreen()
	{
		this.waitScreen = this.__waitScreenContainer.addChild(new Sprite());
		this.waitScreen.spinner = this.waitScreen.addChild(new PreloadingSpinner(2100, 110));
		this.waitScreen.spinner.position.y = -51;
		this.waitScreen.spinner.startAnimation();
	}

	i_hideLoadingScreen()
	{
		if (this.waitScreen)
		{
			this.waitScreen && this.waitScreen.parent && this.waitScreen.parent.removeChild(this);
			this.waitScreen && this.waitScreen.destroy();
			this.waitScreen = null;
		}
	}

	get __waitScreenContainer()
	{
		//must be overridden
		return null;
	}

	destroy()
	{
		if (this.waitScreen)
		{
			this.waitScreen && this.waitScreen.parent && this.waitScreen.parent.removeChild(this);
			this.waitScreen && this.waitScreen.destroy();
			this.waitScreen = null;
		}

		super.destroy();
	}
}

export default GUSubloadingView;