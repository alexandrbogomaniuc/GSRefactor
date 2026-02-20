import { Sprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { FRAME_RATE } from './../../../../../../shared/src/CommonConstants';
import PrizesController from './../../../../controller/uis/prizes/PrizesController';
import PowerUpMultiplierView from './PowerUpMultiplierView';

class PowerUpMultiplierAnimation extends Sprite
{
	static get EVENT_ON_ANIMATION_ENDED() { return "onPowerUpMultiplierAnimationEnded"; }

	get rid()
	{
		return this._fRid_num;
	}

	get enemyId()
	{
		return this._fEnemyId_num;
	}

	constructor(aCurrentPowerUpMultiplier_num, aEnemyId_num, aEnemyName_str, aRid_num, aOptIsBottom_bl=false)
	{
		super();

		this._fRid_num = aRid_num;
		this._fMult_num = aCurrentPowerUpMultiplier_num;
		this._fMultiplierContainer_sprt = null;
		this._fMultiplier_chmv = null;
		this._fEnemyId_num = aEnemyId_num;

		this._fIsBottom_bl = aOptIsBottom_bl;

		APP.gameScreen.prizesController.on(PrizesController.i_EVENT_ON_TIME_TO_SHOW_CASH_PRIZES, this._onTimeToShowCash, this);

		this._init();
	}

	_init()
	{
		this._initMultiplier();
	}

	_initMultiplier()
	{
		this._fMultiplierContainer_sprt = this.addChild(new Sprite());

		this._fMultiplier_chmv = new PowerUpMultiplierView();
		this._fMultiplier_chmv.value = this._fMult_num;

		this._fMultiplierContainer_sprt.addChild(this._fMultiplier_chmv);
	}

	_onTimeToShowCash(aEvent_obj)
	{
		const lData_arr = aEvent_obj.data;

		for (let lData_obj of lData_arr)
		{
			if (lData_obj.hitData.enemy && this._fEnemyId_num === lData_obj.hitData.enemy.id)
			{
				if (APP.gameScreen.prizesController)
				{
					APP.gameScreen.prizesController.off(PrizesController.i_EVENT_ON_TIME_TO_SHOW_CASH_PRIZES, this._onTimeToShowCash, this);
				}

				this._startMultiplier(lData_obj.awardStartPosition);
				return;
			}
		}
	}

	_startMultiplier(aFinalGlobalPos_obj)
	{
		let lFinalLocalPos_obj = { x: 0, y: 0 };
		if (aFinalGlobalPos_obj)
		{
			lFinalLocalPos_obj = this.globalToLocal(aFinalGlobalPos_obj.x, aFinalGlobalPos_obj.y);
		}
		lFinalLocalPos_obj.y -= 60;

		let lAward_ca = APP.currentWindow.awardingController.getAwardByRid(this._fRid_num, this._fEnemyId_num);
		if (lAward_ca)
		{
			let lOffset_obj = lAward_ca.offscreenOffset;
			if (lOffset_obj)
			{
				lFinalLocalPos_obj.x += lOffset_obj.x;
				lFinalLocalPos_obj.y += lOffset_obj.y;
			}
		}

		let lMultSeq_arr = [
			{ tweens: [
				{ prop: 'scale.x', to: 1.2},
				{ prop: 'scale.y', to: 1.2},
			], duration: 12 * FRAME_RATE},
			{ tweens: [
				{ prop: 'position.x', to: lFinalLocalPos_obj.x },
				{ prop: 'position.y', to: lFinalLocalPos_obj.y+80},
				{ prop: 'scale.x', to: 1.5},
				{ prop: 'scale.y', to: 1.5},
			], duration: 12 * FRAME_RATE, ease: Easing.quadratic.easeInOut},
			{ tweens: [{ prop: 'scale.x', to: 2.6 }, { prop: 'scale.y', to: 2.6 }], duration: 4 * FRAME_RATE },
			{ tweens: [
				{ prop: 'scale.x', to: 1 }, 
				{ prop: 'scale.y', to: 1 }, 
				{ prop: 'position.y', to: lFinalLocalPos_obj.y }
			], duration: 8 * FRAME_RATE, onfinish: () => {
				this._fMultiplierContainer_sprt && this._fMultiplierContainer_sprt.destroy();
				this._fMultiplierContainer_sprt = null;
				this._onAnimationEnded();
			}
			},
		];

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._showWhiteFlareAnimation();
		}

		Sequence.start(this._fMultiplierContainer_sprt, lMultSeq_arr);
	}

	_showWhiteFlareAnimation()
	{
		let lFlare_spr = this.addChild(APP.library.getSprite("powerup_multiplicator/white_flare"));
		lFlare_spr.scale.set(0, 0);

		let lPositionY_num = this._fIsBottom_bl ? -22 : 26;
		lFlare_spr.position.set(48, lPositionY_num);
		lFlare_spr.blendMode = PIXI.BLEND_MODES.SCREEN;
		
		this._fWhiteFlare_spr = lFlare_spr;
		let lFlareScale_seq = [
			{ 
				tweens: [
					{ prop: 'scale.x', to: 4},
					{ prop: 'scale.y', to: 4},
				], duration: 5 * FRAME_RATE
			},
			{ 
				tweens: [
					{ prop: 'scale.x', to: 0},
					{ prop: 'scale.y', to: 0},
					{ prop: 'rotation', from: 0, to: 2}
				], duration: 7 * FRAME_RATE, onfinish: () => {
					Sequence.destroy(Sequence.findByTarget(this._fWhiteFlare_spr));
					this._fWhiteFlare_spr && this._fWhiteFlare_spr.destroy();
					this._fWhiteFlare_spr = null;
				}
			}
		];

		Sequence.start(this._fWhiteFlare_spr, lFlareScale_seq);
	}

	_onAnimationEnded()
	{
		this.emit(PowerUpMultiplierAnimation.EVENT_ON_ANIMATION_ENDED);
	}

	destroy()
	{
		if (APP.gameScreen.prizesController)
		{
			APP.gameScreen.prizesController.off(PrizesController.i_EVENT_ON_TIME_TO_SHOW_CASH_PRIZES, this._onTimeToShowCash, this);
		}

		Sequence.destroy(Sequence.findByTarget(this._fWhiteFlare_spr));
		this._fWhiteFlare_spr && this._fWhiteFlare_spr.destroy();
		this._fWhiteFlare_spr = null;

		Sequence.destroy(Sequence.findByTarget(this._fMultiplierContainer_sprt));
		this._fMultiplierContainer_sprt && this._fMultiplierContainer_sprt.destroy();
		this._fMultiplierContainer_sprt = null;

		super.destroy();

		this._fWin_num = null;
		this._fMult_num = null;
		this._fMultiplierContainer_sprt = null;
		this._fMultiplier_chmv = null;
		this._fEnemyId_num = null;
		this._fIsBottom_bl = null;
	}
}

export default PowerUpMultiplierAnimation;