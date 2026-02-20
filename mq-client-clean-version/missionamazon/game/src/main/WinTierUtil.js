const WIN_RATIO_1 			= 0;
const WIN_RATIO_2 			= 10;
const WIN_RATIO_3 			= 20;

export const WIN_TIER_1			= 2;//Small (2 coins to show)
export const WIN_TIER_2			= 3;//Medium (3 coins to show)
export const WIN_TIER_3			= 5;//Big (5 coins to show)

class WinTierUtil
{
	static get WIN_TIERS()
	{
		return {
			TIER_SMALL: WIN_TIER_1,
			TIER_MEDIUM: WIN_TIER_2,
			TIER_BIG: WIN_TIER_3
		}
	}

	static calcWinCoins(aMoneyValue_num, aCurrentStake_num)
	{
		if (!aCurrentStake_num || aCurrentStake_num === 0 || isNaN(aCurrentStake_num))
		{
			throw new Error('Current stake is ' + aCurrentStake_num);
		}

		let lWinRatio_num = aMoneyValue_num/aCurrentStake_num;

		let lCoins_num = 0;
		if (lWinRatio_num >= WIN_RATIO_3)
		{
			lCoins_num = 15;
		}
		else if (lWinRatio_num >= WIN_RATIO_2)
		{
			lCoins_num = 10;
		}
		else
		{
			lCoins_num = 5;
		}

		return lCoins_num;
	}

	static calcWinTier(aMoneyValue_num, aCurrentStake_num)
	{
		if (!aCurrentStake_num || aCurrentStake_num === 0 || isNaN(aCurrentStake_num))
		{
			throw new Error('Current stake is ' + aCurrentStake_num);
		}

		let lWinRatio_num = aMoneyValue_num/aCurrentStake_num;

		if (lWinRatio_num >= WIN_RATIO_3)
		{
			return WIN_TIER_3;
		}
		else if (lWinRatio_num >= WIN_RATIO_2)
		{
			return WIN_TIER_2;
		}
		else if (lWinRatio_num > WIN_RATIO_1)
		{
			return WIN_TIER_1;
		}

		return 0;
	}
}

export default WinTierUtil