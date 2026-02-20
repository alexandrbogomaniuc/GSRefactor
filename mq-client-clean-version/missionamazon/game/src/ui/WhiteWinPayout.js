import WinPayout from './WinPayout';

class WhiteWinPayout extends WinPayout
{
	//override
	_getAssetName()
	{
		return "awards/white_numbers";
	}
}

export default WhiteWinPayout;