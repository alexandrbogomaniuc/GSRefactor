import PlayerInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';

class GamePlayerInfo extends PlayerInfo 
{
	constructor(aOptId_obj, aOptParentInfo_usi)
	{
		super(aOptId_obj, aOptParentInfo_usi);
	}

	getEnemyPayouts(enemyTypeId)
	{
		let lEnemiesPayouts = this.paytable.enemyPayouts;

		for (let i=0; i<lEnemiesPayouts.length; i++)
		{
			let lEnemyPayouts = lEnemiesPayouts[i];
			if (+lEnemyPayouts.idEnemy == enemyTypeId)
			{
				return lEnemyPayouts.prize;
			}
		}

		return null;
	}
}

export default GamePlayerInfo;