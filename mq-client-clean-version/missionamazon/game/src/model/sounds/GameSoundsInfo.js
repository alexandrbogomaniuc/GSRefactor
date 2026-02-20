import SoundsInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/sounds/SoundsInfo';

class GameSoundsInfo extends SoundsInfo
{
	//IL CONSTRUCTION...
	constructor()
	{
		super();

		this._lobbySoundsLoadingOccurred = false;
		
		this._initGSoundsInfo();
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...
	set lobbySoundsLoadingOccurred(value)
	{
		this._lobbySoundsLoadingOccurred = value;
	}

	get lobbySoundsLoadingOccurred()
	{
		return this._lobbySoundsLoadingOccurred;
	}
	//...IL INTERFACE

	//ILI INIT...
	_initGSoundsInfo ()
	{
	}
	//...ILI INIT
}

export default GameSoundsInfo;