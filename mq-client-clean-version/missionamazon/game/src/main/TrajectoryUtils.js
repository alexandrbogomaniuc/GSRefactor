
class TrajectoryUtils {

	static extractTrajectoryTotalDuration(aPoints_pt_arr)
	{
		return TrajectoryUtils.extractTrajectoryDuration(aPoints_pt_arr, 0);
	}

	static extractTrajectoryDuration(aPoints_pt_arr, aIgnorePointsFromEnd_int = 0)
	{
		let n = aPoints_pt_arr.length;
		let lLastPointIndex_int = n - 1 - aIgnorePointsFromEnd_int;
		if (lLastPointIndex_int < 0)
		{
			lLastPointIndex_int = 0;
		}
		let timeDelta = aPoints_pt_arr[lLastPointIndex_int].time - aPoints_pt_arr[0].time;
		return timeDelta;
	}

	static getPrevTrajectoryPoint(aTrajectory_obj, aCurrentTime_num)
	{
		if(!aTrajectory_obj || !aTrajectory_obj.points || !aTrajectory_obj.points.length) return null;

		let lCurrentDate_d = aCurrentTime_num;
		for(let i = 1; i < aTrajectory_obj.points.length; i++)
		{
			if(aTrajectory_obj.points[i].time >= lCurrentDate_d)
			{
				return aTrajectory_obj.points[i - 1];
			}
		}

		return null;
	}

	static getPrevTrajectoryPointIndex(aTrajectory_obj, aCurrentTime_num)
	{
		if(!aTrajectory_obj || !aTrajectory_obj.points || !aTrajectory_obj.points.length) return null;

		let lCurrentDate_d = aCurrentTime_num;
		for(let i = 1; i < aTrajectory_obj.points.length; i++)
		{
			if(aTrajectory_obj.points[i].time >= lCurrentDate_d)
			{
				return (i - 1);
			}
		}
		return null;
	}

}

export default TrajectoryUtils;