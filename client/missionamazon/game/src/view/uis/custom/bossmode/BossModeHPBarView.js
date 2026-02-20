import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import { ENEMIES } from '../../../../../../shared/src/CommonConstants';
import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from './../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../..//common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';

class BossModeHPBarView extends SimpleUIView
{
	update()
	{
		this._update();
	}

	//INIT...
	constructor()
	{
		super();

		this._fBarContainer_sprt = null;
		this._fProgressBar_sprt = null;
		this._fNameCaption_ta = null;
		this._fHPValueField_tf = null;
		this._flasthand = false;

		this._fHPChangingValueSequence = null;
		this._fHealthBarRemove_seq = null;
	}

	__init()
	{
		super.__init();

		this._initBar();
	}

	_initBar()
	{
		this._fBarContainer_sprt = this.addChild(new Sprite());

		this._fBarContainer_sprt.addChild(APP.library.getSpriteFromAtlas("boss_mode/hp_bar/back"));

		this._fProgressBar_sprt = this._fBarContainer_sprt.addChild(APP.library.getSpriteFromAtlas("boss_mode/hp_bar/bar"));
		this._fProgressBar_sprt.position.set(1, 169.5 - 17);
		this._fProgressBar_sprt.anchor.set(0.5, 1);

		this._fBarContainer_sprt.scale.set(0.8);
		this._fBarContainer_sprt.position.set(APP.isMobile ? - 20 : -35, -5);
	}

	_update()
	{
		this._updateBar();
		this._updateName();
		this._updateValue();
	}
	
	onBossDeath()
	{
		if(this._fHealthBarRemove_seq)
		{
			return;
		}
		var lSequence_arr = 
		[
			{
				tweens: [{ prop: "position.x", to: this.position.x + 260 }],
				duration: 2 * 31 * FRAME_RATE,
				onfinish: () => {
					this.emit('animationend');
				}
			}
		]
		this._fHealthBarRemove_seq = Sequence.start(this._fBarContainer_sprt, lSequence_arr);
	}

	_updateBar()
	{
		let lCurrentHealth_num = this.uiInfo.currentHealth;
		let lFullHealth_num = this.uiInfo.fullHealth;
		let lProgress_num = lCurrentHealth_num / lFullHealth_num;
		
		if(lProgress_num >= this._fProgressBar_sprt.scale.y)
		{
			return;
		}

		if(this._flasthand)
		{
			this._fHPChangingValueSequence && this._fHPChangingValueSequence.destructor();

			let seq = [
				{
					tweens: [
						{prop:"scale.y", to: lProgress_num}
					],
					duration: 10*FRAME_RATE,
					ease: Easing.quadratic.easeOut
				}
			];
			this._fHPChangingValueSequence = Sequence.start(this._fProgressBar_sprt, seq);
		}
		else
		{
			this._fProgressBar_sprt.scale.y = lProgress_num
			this._flasthand = true;
		}
	}

	_updateName()
	{
		if (this._fNameCaption_ta) return;

		let lName_str = this.uiInfo.name;

		this._fNameCaption_ta = this._fBarContainer_sprt.addChild(I18.generateNewCTranslatableAsset(this._getBossId(lName_str)));
		this._fNameCaption_ta.position.set(2, 180);

		if (APP.isMobile)
		{
			this._fNameCaption_ta.position.set(2, 170);
		}
	}

	_updateValue()
	{
		let lCurrentHealth_num = this.uiInfo.currentHealth;
	}

	_getBossId(aBossName_str)
	{
		let lCaptions_obj = 'TABossModeCaption';
		if (APP.isMobile)
		{
			lCaptions_obj = 'TABossModeCaptionMobile';
		}

		return lCaptions_obj;
	}

	destroy()
	{
		super.destroy();

		this._fHPChangingValueSequence && this._fHPChangingValueSequence.destructor();
		this._fHPChangingValueSequence = null;

		this._fHealthBarRemove_seq && this._fHealthBarRemove_seq.destructor();
		this._fHealthBarRemove_seq = null;

		this._flasthand = null;
		this._fBarContainer_sprt = null;
		this._fProgressBar_sprt = null;
		this._fNameCaption_ta = null;
		this._fHPValueField_tf = null;
	}
}

export default BossModeHPBarView;