import BackgroundTileBaseClassView from '../BackgroundTileBaseClassView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import BackgroundTilesetKaktusesView from '../BackgroundTilesetKaktusesView';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class BackgroundTileKaktuses1View extends BackgroundTileBaseClassView
{
	constructor()
	{
		super();

		//KAKTUS 0...
		let l_rcdo = new Sprite;
		l_rcdo.textures = [BackgroundTilesetKaktusesView.getKaktusTextures()[0]];
		l_rcdo.anchor.set(1, 1);
		l_rcdo.scale.x = -1;
		l_rcdo.position.x = 29;
		this.addChild(l_rcdo);
		//...KAKTUS 0

		//KAKTUS 1...
		l_rcdo = new Sprite;
		l_rcdo.textures = [BackgroundTilesetKaktusesView.getKaktusTextures()[1]];
		l_rcdoanchor.set(1, 1);
		l_rcdo.scale.x = -1;
		l_rcdo.position.x = 210;
		this.addChild(l_rcdo);
		//...KAKTUS 1

		//KAKTUS 2...
		l_rcdo = new Sprite;
		l_rcdo.textures = [BackgroundTilesetKaktusesView.getKaktusTextures()[2]];
		l_rcdo.anchor.set(1, 1);
		l_rcdo.scale.x = -1;
		l_rcdo.position.x = 503;
		this.addChild(l_rcdo);
		//...KAKTUS 2
	}

	//override
	getTileWidth()
	{
		return 694;
	}
}
export default BackgroundTileKaktuses1View;