import BaseBetsController from './BaseBetsController';

/**
 * @class
 * @extends BaseBetsController
 * @inheritdoc
 */
class BetsController extends BaseBetsController
{
	static get EVENT_ON_BET_REJECTED() 							{ return BaseBetsController.EVENT_ON_BET_REJECTED};
	static get EVENT_ON_BETS_ACCEPTED() 						{ return BaseBetsController.EVENT_ON_BETS_ACCEPTED };
	static get EVENT_ON_BET_CONFIRMED() 						{ return BaseBetsController.EVENT_ON_BET_CONFIRMED };
	static get EVENT_ON_BET_NOT_CONFIRMED() 					{ return BaseBetsController.EVENT_ON_BET_NOT_CONFIRMED };
	static get EVENT_ON_ALL_PLAYER_BETS_REJECTED_BY_SERVER()	{ return BaseBetsController.EVENT_ON_ALL_PLAYER_BETS_REJECTED_BY_SERVER };
	static get EVENT_ON_ALL_PLAYER_BETS_CONFIRMED_BY_SERVER()	{ return BaseBetsController.EVENT_ON_ALL_PLAYER_BETS_CONFIRMED_BY_SERVER };
	static get EVENT_ON_BET_CANCEL_REJECTED() 					{ return BaseBetsController.EVENT_ON_BET_CANCEL_REJECTED };
	static get EVENT_ON_BET_CANCEL_INITIATED() 					{ return BaseBetsController.EVENT_ON_BET_CANCEL_INITIATED };
	static get EVENT_ON_CANCEL_ALL_BETS_INITIATED() 			{ return BaseBetsController.EVENT_ON_CANCEL_ALL_BETS_INITIATED };
	static get EVENT_ON_BET_CANCELLED() 						{ return BaseBetsController.EVENT_ON_BET_CANCELLED };
	static get EVENT_ON_OUTDATED_BET_REMOVED() 					{ return BaseBetsController.EVENT_ON_OUTDATED_BET_REMOVED };
	static get EVENT_ON_BETS_CLEARED() 							{ return BaseBetsController.EVENT_ON_BETS_CLEARED };
	static get EVENT_ON_BETS_UPDATED() 							{ return BaseBetsController.EVENT_ON_BETS_UPDATED };
	static get EVENT_ON_BET_LIMITS_UPDATED() 					{ return BaseBetsController.EVENT_ON_BET_LIMITS_UPDATED };
	
	static get EVENT_ON_CANCEL_AUTOEJECT_INITIATED()			{ return BaseBetsController.EVENT_ON_CANCEL_AUTOEJECT_INITIATED };
	static get EVENT_ON_CRASH_CANCEL_AUTOEJECT_REJECTED() 		{ return BaseBetsController.EVENT_ON_CRASH_CANCEL_AUTOEJECT_REJECTED };
	static get EVENT_ON_CRASH_CANCEL_AUTOEJECT_CONFIRMED() 		{ return BaseBetsController.EVENT_ON_CRASH_CANCEL_AUTOEJECT_CONFIRMED };

	static get EVENT_ON_EDIT_AUTO_EJECT_INITIATED() 			{ return BaseBetsController.EVENT_ON_EDIT_AUTO_EJECT_INITIATED };
	static get EVENT_ON_EDIT_AUTOEJECT_REJECTED() 				{ return BaseBetsController.EVENT_ON_EDIT_AUTOEJECT_REJECTED };
	static get EVENT_ON_EDIT_AUTOEJECT_CONFIRMED() 				{ return BaseBetsController.EVENT_ON_EDIT_AUTOEJECT_CONFIRMED };

	constructor(aOptInfo_usi, aOptParentController_usc)
	{
		super(aOptInfo_usi, aOptParentController_usc);
	}

	__initControlLevel()
	{
		super.__initControlLevel();
	}
}

export default BetsController;