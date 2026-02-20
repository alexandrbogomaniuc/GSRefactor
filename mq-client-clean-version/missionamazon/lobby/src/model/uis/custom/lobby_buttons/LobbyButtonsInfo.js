import SimpleInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

class LobbyButtonsInfo extends SimpleInfo
{
	registerButton(aButton_b)
	{
		this._registerButton(aButton_b);
	}

	unregisterButton(aButton_b)
	{
		this._unregisterButton(aButton_b);
	}

	constructor()
	{
		super();

		this._fLobbyButtons_b_arr = [];
	}

	_registerButton(aButton_b)
	{
		this._fLobbyButtons_b_arr.push(aButton_b);
	}

	_unregisterButton(aButton_b)
	{
		const lIndex_int = this._fLobbyButtons_b_arr.indexOf(aButton_b);
		if (~lIndex_int)
		{
			this._fLobbyButtons_b_arr.splice(lIndex_int, 1);
		}
	}

	destroy()
	{
		super.destroy();

		this._fLobbyButtons_b_arr = [];
	}
}

export default LobbyButtonsInfo;