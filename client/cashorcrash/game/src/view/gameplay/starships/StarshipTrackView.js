import StarshipTrackBaseView from './StarshipTrackBaseView';

class StarshipTrackView extends StarshipTrackBaseView
{
	constructor()
	{
		super();
	}

	//override
	getColor()
	{
		return 0xf8ef21;
	}

	//override
	getStepWidth()
	{
		return 100;
	}

	//override
	getLineThickness()
	{
		return 4;
	}

	//override
	getLineMinimalThickness()
	{
		return this.getLineThickness();
	}

	//override
	getLineWobbling()
	{
		return 5;
	}

	//override
	getLineMinimalWobbling()
	{
		return 4;
	}

	//override
	getDownscaleXSpeed()
	{
		return 1000;
	}

	//override
	getTileOutroMillisecondsCount()
	{
		return 30000;
	}
	//...OVERRIDE
}

export default StarshipTrackView