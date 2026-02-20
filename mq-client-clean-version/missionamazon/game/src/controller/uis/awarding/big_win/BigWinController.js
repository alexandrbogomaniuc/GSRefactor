import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import BigWinInfo from '../../../../model/uis/awarding/big_win/BigWinInfo';
import BigWinView from '../../../../view/uis/awarding/big_win/BigWinView';

class BigWinController extends SimpleUIController {

	static get EVENT_ON_ANIMATION_COMPLETED() 	{ return BigWinView.EVENT_ON_ANIMATION_COMPLETED; }
	static get EVENT_ON_ANIMATION_INTERRUPTED() { return BigWinView.EVENT_ON_ANIMATION_INTERRUPTED;}
	static get EVENT_ON_COIN_LANDED() 			{ return BigWinView.EVENT_ON_COIN_LANDED; }
	static get EVENT_ON_BIG_WIN_AWARD_COUNTED() { return BigWinView.EVENT_ON_BIG_WIN_AWARD_COUNTED; }
	static get EVENT_NEED_MUTE_BG_SOUND_ON_BIG_WIN_PLAYING() { return BigWinView.EVENT_NEED_MUTE_BG_SOUND_ON_BIG_WIN_PLAYING;}
	static get EVENT_NEED_FADE_BACK_BG_SOUND_ON_BIG_WIN_PLAYING() { return BigWinView.EVENT_NEED_FADE_BACK_BG_SOUND_ON_BIG_WIN_PLAYING;}
	
	static get EVENT_BIG_WIN_PRESETNTATION_STARTED() { return BigWinView.EVENT_BIG_WIN_PRESETNTATION_STARTED; }

	i_startAnimation()
	{
		this._startAnimation();
	}

	i_interrupt()
	{
		this._interrupt();
	}

	constructor(aTotalWin_num, aShotStake_num, aParentContainer_sprt, aOptPowerUpMult_int=1)
	{
		super(new BigWinInfo(aTotalWin_num, aShotStake_num, aOptPowerUpMult_int), new BigWinView(aParentContainer_sprt));
	}

	__initControlLevel()
	{
		super.__initControlLevel();
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		this.view.on(BigWinView.EVENT_ON_ANIMATION_COMPLETED, this._onAnimationCompleted, this);
		this.view.on(BigWinView.EVENT_ON_COIN_LANDED, this._onCoinLanded, this);
		this.view.on(BigWinView.EVENT_ON_BIG_WIN_AWARD_COUNTED, this._onBigWinAwardCounted, this);
		
		this.view.on(BigWinView.EVENT_BIG_WIN_PRESETNTATION_STARTED, this.emit, this);
		this.view.on(BigWinView.EVENT_NEED_MUTE_BG_SOUND_ON_BIG_WIN_PLAYING, this.emit, this);
		this.view.on(BigWinView.EVENT_NEED_FADE_BACK_BG_SOUND_ON_BIG_WIN_PLAYING, this.emit, this);
	}

	_onCoinLanded(aEvent_obj)
	{
		this.emit(BigWinController.EVENT_ON_COIN_LANDED, aEvent_obj);
		this.info && (this.info.notLandedWin = this.info.notLandedWin - aEvent_obj.money);
	}

	_onBigWinAwardCounted(aEvent_obj)
	{
		this.emit(BigWinController.EVENT_ON_BIG_WIN_AWARD_COUNTED, aEvent_obj);
		this.info.uncountedWin = this.info.uncountedWin - aEvent_obj.money;
	}

	_startAnimation()
	{
		this.view.i_startAnimation();
	}

	_onAnimationCompleted()
	{
		this.emit(BigWinController.EVENT_ON_ANIMATION_COMPLETED);
	}

	_interrupt()
	{
		this.view && this.view.i_interrupt();

		this.emit(BigWinController.EVENT_ON_ANIMATION_INTERRUPTED, {uncountedWin: this.info.uncountedWin, notLandedWin: this.info.notLandedWin }); // to update WIN field and transfer uncountedWin to ammo
	}

	_clear()
	{
		this.view.i_clear();
		this.info.i_clear();
	}

	destroy()
	{
		super.destroy();
	}
}

export default BigWinController;