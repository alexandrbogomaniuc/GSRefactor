import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';
import MAPS_ASSETS from '../../../config/maps_assets.json';

const TOTAL_MAPS_COUNT = 2;
const MAPS_WALKING_ZONES = {
	"1": [
		0, 385,
		60, 385,
		64, 244,
		14, 169,
		82, 123,
		218, 223,
		337, 217,
		350, 205,
		425, 110,
		545, 70,
		712, 121,
		773, 62,
		869, 91,
		849, 161,
		960, 195,
		960, 540,
		0, 540

	],
	"2": [
		0, 275,
		360, 125,
		485, 95,
		544, 125,
		645, 99,
		748, 48,
		859, 103,
		786, 166,
		790, 246,
		790, 245,
		825, 310,
		960, 310,
		960, 540,
		0, 540
	],
	"3": [
		0, 250,
		450, 90,
		605, 110,
		700, 70,
		700, 0,
		960, 0,
		960, 540,
		0, 540
	]
};

class MapsInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._fMapId_int = undefined;
		this._fNextMapId_int = undefined;
		this._fAdditionalRestrictionZoneActivated_bl = false;
	}

	/*returns Array*/
	static getMapAssets(aMapId_int)
	{
		return MAPS_ASSETS[aMapId_int];
	}

	indexCalc(aValue_int)
	{
		let lArg_str = aValue_int + "";
		let lMultiplier_int = Math.pow(10, (lArg_str.length - 1));
		let lOffset_int = Math.trunc(aValue_int / lMultiplier_int) * lMultiplier_int;
		let lMapId_int = aValue_int - lOffset_int;
		
		return lMapId_int;
	}

	set mapId(aValue_int)
	{
		//TEMP SOLUTION...
		let lMapId_int = this.indexCalc(aValue_int);

		if(lMapId_int >= 100)
		{
			lMapId_int = this.indexCalc(lMapId_int);
		}

		if (isNaN(lMapId_int) || lMapId_int < 1 || lMapId_int > TOTAL_MAPS_COUNT)
		{
			throw new Error(`MapId ${lMapId_int} is out of range. The value in range [1, ${TOTAL_MAPS_COUNT}] is expected.`);
		}

		this._fMapId_int = lMapId_int;
		//...TEMP SOLUTION
	}

	get totalMapsCount()
	{
		return TOTAL_MAPS_COUNT;
	}

	get mapId()
	{
		return this._fMapId_int;
	}

	set nextMapId(aValue_int)
	{
		//TEMP SOLUTION...
		let lMapId_int = this.indexCalc(aValue_int);

		if(lMapId_int >= 100)
		{
			lMapId_int = this.indexCalc(lMapId_int);
		}

		if (isNaN(lMapId_int) || lMapId_int < 1 || lMapId_int > TOTAL_MAPS_COUNT)
		{
			throw new Error(`MapId ${lMapId_int} is out of range. The value in range [1, ${TOTAL_MAPS_COUNT}] is expected.`);
		}

		aValue_int = lMapId_int;
		//...TEMP SOLUTION

		if (isNaN(aValue_int) || aValue_int < 1 || aValue_int > TOTAL_MAPS_COUNT)
		{
			throw new Error(`NextMapId ${aValue_int} is out of range. The value in range [1, ${TOTAL_MAPS_COUNT}] is expected.`);
		}

		this._fNextMapId_int = aValue_int;
	}

	get nextMapId()
	{
		return this._fNextMapId_int;
	}

	get currentMapWalkingZone()
	{
		return MAPS_WALKING_ZONES[this.mapId]; //array of points
	}

	destroy()
	{
		this._fMapId_int = undefined;
		this._fNextMapId_int = undefined;

		super.destroy();
	}
}

export default MapsInfo;
export { MAPS_ASSETS }