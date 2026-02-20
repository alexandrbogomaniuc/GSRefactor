//help to interprete the Shot response
class ShotResultsUtil {

	//@returns unique id-s
	static extractNonFakeEnemies(data)
	{
		let lNonFakeEnemyIds_int_arr = [];
		let lAffectedEnemies_obj_arr = data.affectedEnemies;
		for (let lEnemy_obj of lAffectedEnemies_obj_arr)
		{
			if (lEnemy_obj.enemyId >= 0)
			{
				lNonFakeEnemyIds_int_arr.push(lEnemy_obj.enemyId);
			}
		}
		lNonFakeEnemyIds_int_arr = lNonFakeEnemyIds_int_arr.filter(function(value, index, self){
			return self.indexOf(value) === index;
		});
		return lNonFakeEnemyIds_int_arr;
	}

	static getFirstNonFakeEnemy(data)
	{
		let lAffectedEnemiesUnique_int_arr = ShotResultsUtil.extractNonFakeEnemies(data);
		return lAffectedEnemiesUnique_int_arr[0];
	}

	static extractTargetEnemyId(data)
	{
		return data.requestEnemyId;
	}

	static excludeFakeEnemies(affectedEnemies)
	{
		let lFakeEnemies_arr = [];
		let lRealEmemies_arr = [];
		for (let obj of affectedEnemies)
		{
			if (obj.enemyId !== -10)
			{
				lRealEmemies_arr.push(obj)
			}
			else
			{
				lFakeEnemies_arr.push(obj);
			}
		}

		let lRealEnemiesAmount_int = lRealEmemies_arr.length;
		if (lRealEnemiesAmount_int)
		{
			for (let i = 0; i < lFakeEnemies_arr.length; i++)
			{
				let lRealEnemy_obj = lRealEmemies_arr[Math.floor(Math.random()*lRealEnemiesAmount_int)];
				let lRealEnemyId_int = lRealEnemy_obj.data && lRealEnemy_obj.data.enemy ? lRealEnemy_obj.data.enemy.id : lRealEnemy_obj.enemyId;
				
				let lFakeEnemy_obj = lFakeEnemies_arr[i];
				if (lFakeEnemy_obj.data && lFakeEnemy_obj.data.enemy)
				{
					lFakeEnemy_obj.data.enemy.id = lRealEnemyId_int;
				}
				lFakeEnemy_obj.enemyId = lRealEnemyId_int;
				lFakeEnemy_obj.data.killed = lRealEnemy_obj.data.killed;
			}
		}
		return affectedEnemies;
	}

	static isMasterShot(dataOrRid)
	{
		let rid = typeof dataOrRid === 'object' ? dataOrRid.rid : dataOrRid;
		return rid > -1;
	}

	static isFakeEnemy(dataOrEnemyId)
	{
		let lEnemyId_num = dataOrEnemyId;

		if (typeof dataOrEnemyId === 'object')
		{
			lEnemyId_num = dataOrEnemyId.enemy ? dataOrEnemyId.enemy.id : dataOrEnemyId.enemyId;
		}
		
		return lEnemyId_num == -10;
	}
}

export default ShotResultsUtil;