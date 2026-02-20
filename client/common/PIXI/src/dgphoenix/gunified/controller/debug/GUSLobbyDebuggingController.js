import SimpleController from '../../../unified/controller/base/SimpleController';
import { APP } from '../../../unified/controller/main/globals';
import { LOBBY_MESSAGES } from '../external/GUSExternalCommunicator';

class GUSLobbyDebuggingController extends SimpleController
{
	constructor()
	{
		super();
	}

	//override
	__init()
	{
		super.__init();

		window.setTint = function (aTintColor_hex, aTintIntencity_num)
		{
			APP.externalCommunicator.sendExternalMessage(
				LOBBY_MESSAGES.DEBUG_MESSAGE,
				{
					message: 'tint',
					data:
					{
						tintColor: aTintColor_hex,
						tintIntensity: aTintIntencity_num
					}
				}
			);
		}

		window.resetTint = function ()
		{
			window.setTint(0xffffff, 1);
		}

		window.setShadowTint = function (aTintColor_hex)
		{
			APP.externalCommunicator.sendExternalMessage(
				LOBBY_MESSAGES.DEBUG_MESSAGE,
				{ message: 'shadowTint', data: { tintColor: aTintColor_hex } }
			);
		}

		window.resetShadowTint = function ()
		{
			window.setShadowTint(0x000000);
		}
	}


}

export default GUSLobbyDebuggingController;