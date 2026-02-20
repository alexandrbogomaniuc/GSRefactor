import SimpleInfo from "../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo";

export const WAIT_MESSAGE_TYPES = {
	BET_LEVEL_CHANGE_REQUEST: 		"BET_LEVEL_CHANGE_REQUEST",
	SIT_OUT_REQUEST: 				"SIT_OUT_REQUEST",
	FIRST_ENEMY_CREATION: 			"FIRST_ENEMY_CREATION",
	ROUND_RESULT_SCREEN: 			"ROUND_RESULT_SCREEN",
	STATE_PLAY_END: 				"STATE_PLAY_END",
	STATE_QUALIFY_END: 				"STATE_QUALIFY_END",
	STATE_WAIT_END: 				"STATE_WAIT_END",
	BUY_IN_REQUEST: 				"BUY_IN_REQUEST",
	RE_BUY_REQUEST: 				"RE_BUY_REQUEST",
	SHOT:							"SHOT",
	SIT_IN_REQUEST: 				"SIT_IN_REQUEST"
};

class GamePleaseWaitDialogInfo extends SimpleInfo
{
	constructor()
	{
		super();
	}
}

export default GamePleaseWaitDialogInfo;