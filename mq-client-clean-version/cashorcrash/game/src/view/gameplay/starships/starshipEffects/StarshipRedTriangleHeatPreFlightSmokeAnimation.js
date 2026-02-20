import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import StarshipHeatPreFlightSmokeAnimation from './StarshipHeatPreFlightSmokeAnimation';

class StarshipRedTriangleHeatPreFlightSmokeAnimation extends Sprite
{
	constructor()
	{
		super();
		
		let l_arr = this._fSmokes_arr = [];

		let l_shpfsa = this.addChild(new StarshipHeatPreFlightSmokeAnimation);
		l_arr.push(l_shpfsa);

		l_shpfsa = this.addChild(new StarshipHeatPreFlightSmokeAnimation);
		l_shpfsa.position.set(-32, -12);
		l_shpfsa.scale.set(0.5);
		l_arr.push(l_shpfsa);

		l_shpfsa = this.addChild(new StarshipHeatPreFlightSmokeAnimation);
		l_shpfsa.position.set(38, -12);
		l_shpfsa.scale.set(0.5);
		l_arr.push(l_shpfsa);
	}

	get duration()
	{
		let lDuration_num = 0;
		for (let i = 0; i < this._fSmokes_arr.length; i++)
		{
			if (this._fSmokes_arr[i].duration > lDuration_num)
			{
				lDuration_num = this._fSmokes_arr[i].duration;
			}
		}
		return lDuration_num;
	}

	adjust(aAnimDuration_num)
	{
		for (let i=0; i<this._fSmokes_arr.length; i++)
		{
			this._fSmokes_arr[i].adjust(aAnimDuration_num);
		}
	}
}

export default StarshipRedTriangleHeatPreFlightSmokeAnimation;