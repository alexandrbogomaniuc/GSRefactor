import BackgroundTilesetBaseClassView from './BackgroundTilesetBaseClassView';
import BackgroundTileGroundView from './tiles/BackgroundTileGroundView';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class BackgroundTilesetGroundView extends BackgroundTilesetBaseClassView
{
	constructor()
	{
		super(2);
	}

	//override
	generateTileView(aTemplateTileIndex_int)
	{
		switch(aTemplateTileIndex_int)
		{
			case 0:
				return new BackgroundTileGroundView(BackgroundTileGroundView.GROUND_SKIN_ID_ANTENNAS);
			case 1:
				return new BackgroundTileGroundView(BackgroundTileGroundView.GROUND_SKIN_ID_DESERT);
		}
	}

	//override
	getTilesetHeight()
	{
		return 150;
	}

	//override
	getInitialPositionY()
	{
		return GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;
	}

	//override
	getOffsetPerMultiplierX()
	{
		return -75;
	}

	//override
	getOffsetPerMultiplierY()
	{
		return 100;
	}

	//OVERRIDE...
	getOffsetPerPreLaunchX()
	{
		return 0;
	}

	getOffsetPerPreLaunchY()
	{
		return 270;
	}
	//...OVERRIDE
}
export default BackgroundTilesetGroundView;