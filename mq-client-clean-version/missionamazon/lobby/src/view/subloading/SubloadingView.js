import SimpleUIView from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PreloadingSpinner from '../../../../../common/PIXI/src/dgphoenix/gunified/view/custom/PreloadingSpinner';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class SubloadingView extends SimpleUIView {
	
	constructor()
	{
		super();

		this.waitScreen = null;
	}

	i_showLoadingScreen()
	{
		this.waitScreen = APP.lobbyWaitScreen.addChild(new Sprite());
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

export default SubloadingView;