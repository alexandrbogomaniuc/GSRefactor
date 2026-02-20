import BackgroundTilesetBaseClassView from './BackgroundTilesetBaseClassView';
import BackgroundTileSunsetView from './tiles/BackgroundTileSunsetView';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class BackgroundTilesetSunsetView extends BackgroundTilesetBaseClassView
{
	constructor()
	{
		super(1);
	}

	//OVERRIDE...
	generateTileView(aTemplateTileIndex_int)
	{
		switch(aTemplateTileIndex_int)
		{
			case 0: return new BackgroundTileSunsetView();
		}
	}
	//...OVERRIDE

	//OVERRIDE...
	getTilesetHeight()
	{
		return 689.5;
	}
	//...OVERRIDE

	//OVERRIDE...
	getInitialPositionY()
	{
		return GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;
	}
	//...OVERRIDE

	//OVERRIDE...
	getOffsetPerMultiplierX()
	{
		return -50;
	}
	//...OVERRIDE

	//OVERRIDE...
	getOffsetPerMultiplierY()
	{
		return 225;
	}
	//...OVERRIDE

	//OVERRIDE...
	getOffsetPerPreLaunchX()
	{
		return 0;
	}

	getOffsetPerPreLaunchY()
	{
		return 450;
	}
	//...OVERRIDE
}
export default BackgroundTilesetSunsetView;