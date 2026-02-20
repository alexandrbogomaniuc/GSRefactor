import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class BackgroundTileBaseClassView extends Sprite
{
	constructor()
	{
		super();
	}

	getTileWidth()
	{
		let lLocalBounds_r = this.getLocalBounds();

		return lLocalBounds_r ? lLocalBounds_r.width : 0;
	}

	getTileHeight()
	{
		let lLocalBounds_r = this.getLocalBounds();

		return lLocalBounds_r ? lLocalBounds_r.height : 0;
	}
}
export default BackgroundTileBaseClassView;