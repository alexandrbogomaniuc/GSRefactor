import GameplayBackgroundBaseClassView from './GameplayBackgroundBaseClassView';
import BackgroundTilesetSpaceView from './BackgroundTilesetSpaceView';
import BackgroundTilesetSunsetView from './BackgroundTilesetSunsetView';
import BackgroundTilesetGroundView from './BackgroundTilesetGroundView';
import BackgroundTilesetKaktusesView from './BackgroundTilesetKaktusesView';
import BackgroundTilesetPlanetsView from './BackgroundTilesetPlanetsView';
import BackgroundRandomBigPlanetsView from './BackgroundRandomBigPlanetsView';
import BackgroundRandomSmallPlanetsView from './BackgroundRandomSmallPlanetsView';
import BackgroundRandomAsteroidsView from './BackgroundRandomAsteroidsView';
import BackgroundSummonedAsteroidsView from './BackgroundSummonedAsteroidsView';
import BackgroundPurpleSmokesView from './BackgroundPurpleSmokesView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class GameplayBackgroundView extends GameplayBackgroundBaseClassView
{
	static generateTilesets()
	{
		GameplayBackgroundView.TILESET_SPACE 				= new BackgroundTilesetSpaceView();
		GameplayBackgroundView.TILESET_SMALL_PLANETS 		= new BackgroundRandomSmallPlanetsView();
		GameplayBackgroundView.TILESET_BIG_PLANETS 			= new BackgroundRandomBigPlanetsView();
		GameplayBackgroundView.TILESET_SUNSET 				= new BackgroundTilesetSunsetView();
		GameplayBackgroundView.TILESET_GROUND 				= new BackgroundTilesetGroundView();
		GameplayBackgroundView.TILESET_KAKTUSES 			= new BackgroundTilesetKaktusesView();

		if (APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode || APP.isBattlegroundGame)
		{
			GameplayBackgroundView.TILESET_ASTEROIDS		= new BackgroundRandomAsteroidsView();
			if( APP.isBattlegroundGame)
			{
				GameplayBackgroundView.TILESET_ASTEROIDS_BTG = new BackgroundSummonedAsteroidsView();
			}
			
			GameplayBackgroundView.TILESET_PURPLE_SMOKES	= new BackgroundPurpleSmokesView();
		}
		else
		{
			GameplayBackgroundView.TILESET_TAKE_OFF_PLANETS		= new BackgroundTilesetPlanetsView();
		}
	}

	adjustRandomElements()
	{
		if (GameplayBackgroundView.TILESET_TAKE_OFF_PLANETS)
		{
			GameplayBackgroundView.TILESET_TAKE_OFF_PLANETS.setRandomPlanets();
			GameplayBackgroundView.TILESET_SMALL_PLANETS.setRandomPlanets([
																		GameplayBackgroundView.TILESET_TAKE_OFF_PLANETS.smallPlanetIndex, 
																		GameplayBackgroundView.TILESET_TAKE_OFF_PLANETS.bigPlanetIndex
																	]);

			GameplayBackgroundView.TILESET_BIG_PLANETS.setRandomPlanets([
																		GameplayBackgroundView.TILESET_TAKE_OFF_PLANETS.smallPlanetIndex, 
																		GameplayBackgroundView.TILESET_TAKE_OFF_PLANETS.bigPlanetIndex,
																		GameplayBackgroundView.TILESET_SMALL_PLANETS.currentPlanetIndex
																	]);
		}
		else
		{
			GameplayBackgroundView.TILESET_SMALL_PLANETS.setRandomPlanets();
			GameplayBackgroundView.TILESET_BIG_PLANETS.setRandomPlanets([ GameplayBackgroundView.TILESET_SMALL_PLANETS.currentPlanetIndex ]);
		}
	}

	constructor()
	{
		GameplayBackgroundView.generateTilesets();

		super();
	}
}
export default GameplayBackgroundView;