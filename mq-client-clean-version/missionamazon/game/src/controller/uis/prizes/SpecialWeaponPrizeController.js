import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SpecialWeaponPrizeView from '../../../view/uis/prizes/SpecialWeaponPrizeView';
import SpecialWeaponPrizeInfo from '../../../model/uis/prizes/SpecialWeaponPrizeInfo';
import GameScreen from '../../../main/GameScreen';

class SpecialWeaponPrizeController extends SimpleUIController {

	static get i_EVENT_ON_ANIMATION_COMPLETED() { return SpecialWeaponPrizeView.i_EVENT_ON_ANIMATION_COMPLETED; }
	static get i_EVENT_ON_ANIMATION_INTERRUPTED() { return SpecialWeaponPrizeView.i_EVENT_ON_ANIMATION_INTERRUPTED; }

	i_startAnimation()
	{
		this._startAnimation();
	}

	constructor(aUniqueId_int, aSpecialWeaponId_int, aSeatId_int, aStartPosition_pt)
	{
		super(new SpecialWeaponPrizeInfo(aUniqueId_int, aSpecialWeaponId_int, aSeatId_int, aStartPosition_pt), new SpecialWeaponPrizeView());
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._fGameScreen_gs = APP.currentWindow;
		this._fGameScreen_gs.on(GameScreen.EVENT_ON_PLAYER_REMOVED, this._onPlayerRemoved, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lAwardingContainerInfo_obj = APP.currentWindow.gameField.awardingContainerInfo;		
		this.view.i_init(lAwardingContainerInfo_obj);

		this.view.once(SpecialWeaponPrizeView.i_EVENT_ON_ANIMATION_COMPLETED, this._onAnimationCompleted, this);
	}

	_onAnimationCompleted()
	{
		this.emit(SpecialWeaponPrizeController.i_EVENT_ON_ANIMATION_COMPLETED, {uniqueId: this.info.uniqueId});
	}

	_startAnimation()
	{
		this.view.i_startAnimation();
	}

	_onPlayerRemoved(e)
	{
		let seatId = e.seatId;
		if (seatId == this.info.seatId)
		{
			this._interruptAnimation();
		}
	}

	_interruptAnimation()
	{
		this.view.destroy();
		this.emit(SpecialWeaponPrizeController.i_EVENT_ON_ANIMATION_INTERRUPTED, {uniqueId: this.info.uniqueId});
	}

	destroy()
	{
		this.removeAllListeners();
		this._fGameScreen_gs.off(GameScreen.EVENT_ON_PLAYER_REMOVED, this._onPlayerRemoved, this);
		super.destroy();
	}

}

export default SpecialWeaponPrizeController;