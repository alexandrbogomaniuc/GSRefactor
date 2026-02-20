import SimpleController from '../../../base/SimpleController';

/**
 * @class
 * @classdesc Base controller for FRB/Cash bonus
 */
class BonusController extends SimpleController {

	static get EVENT_ON_BONUS_STATE_CHANGED() 		{ return 'EVENT_ON_BONUS_STATE_CHANGED'; }
}

export default BonusController;