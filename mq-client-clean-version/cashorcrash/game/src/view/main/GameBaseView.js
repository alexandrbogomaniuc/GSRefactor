import SimpleUIView from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const GAME_VIEW_SETTINGS = 
{
	BAP_ROW_HEIGHT: undefined,
	BAP_HEIGHT: undefined,
	GAMEPLAY_ZONE: undefined
}

class GameBaseView extends SimpleUIView
{
	// mockup size:
	// gameplay: 683*503
	// bet/list area: 180*513
	// bottom panel: screen_width*42

	get gameplayView()
	{
		return this._fGameplayView_gpv || (this._fGameplayView_gpv = this._generateGameplayView())
	}
	
	get roundDetailsView()
	{
		return this._fRoundDetailsView_rdsv || (this._fRoundDetailsView_rdsv = this._generateRoundDetailsView())
	}

	get betsListView()
	{
		return this._fBetsListView_bslv || (this._fBetsListView_bslv = this._generateBetsListView())
	}

	updateArea()
	{
		this._updateArea();
	}

	validate()
	{
		
	}

	get placeBetsView()
	{
		return this._fPlaceBetsView_pbsv || (this._fPlaceBetsView_pbsv = this._generatePlaceBetsView())
	}

	constructor()
	{
		super();

		this._fGameplayView_gpv = null;
		this._fRoundDetailsView_rdsv = null;
		this._fBetsListView_bslv = null;
		this._fPlaceBetsView_pbsv = null;
	}

	__init()
	{
		super.__init();

		this._calculateGameZones();

		APP.gameScreenView.gameViewContainer.addChild(this);

		this._updateArea();
	}

	_calculateGameZones()
	{
		// should be overridden
	}

	_updateArea()
	{
		this._calculateGameZones();
	}

	_generateGameplayView()
	{
		// should be overridden
		return null;
	}

	_generateRoundDetailsView()
	{
		// should be overridden
		return null;
	}

	_generateBetsListView()
	{
		// should be overridden
		return null;
	}

	_generatePlaceBetsView()
	{
		// should be overridden
		return null;
	}
}

export { GAME_VIEW_SETTINGS };
export default GameBaseView;