import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import Button from '../../../ui/GameButton';
import AutoTargetingSwitcherInfo from '../../../model/uis/targeting/AutoTargetingSwitcherInfo';

const INITIAL_SCALE = 1.2;

const POSITION = {x: 960 - 95, y: 540/2};
const POSITION_SMALL_DEVICE = {x: 960 - 110, y: 540/2};

class AutoTargetingSwitcherView extends SimpleUIView {	

	static get EVENT_ON_CLICK() { return 'EVENT_ON_CLICK'; }

	constructor()
	{
		super();

		this._fBack_grph = null;
		this._fButton_btn = null;
		this._fState_str = undefined;

		this._createView();	
	}

	i_addToContainerIfRequired(aContainerInfo_obj)
	{
		if (this.parent)
		{
			return;
		}

		aContainerInfo_obj.container.addChild(this);
		this.zIndex = aContainerInfo_obj.zIndex;
	}

	i_disable()
	{
		this._disable();
	}

	i_enable()
	{
		this._enable();
	}

	i_updateState(aState_str)
	{
		this._setState(aState_str);
	}

	_createView()
	{
		this._fBack_grph = new PIXI.Graphics();
		/*this._fBack_grph.clear()
			.beginFill(0x013f68, 1)
			.drawRoundedRect(-42*INITIAL_SCALE/2, -42*INITIAL_SCALE/2, 43*INITIAL_SCALE, 42*INITIAL_SCALE, 5*INITIAL_SCALE)
			.endFill();*/
		this._fBack_grph.beginFill(0x000000, 0.7)
			.drawRoundedRect(-40*INITIAL_SCALE/2, -40*INITIAL_SCALE/2, 41*INITIAL_SCALE, 40*INITIAL_SCALE, 5*INITIAL_SCALE);
		this.addChild(this._fBack_grph);

		this._fButton_btn = new Button('TargetCrosshair_RED', undefined, true);
		this._fButton_btn.scale.set(INITIAL_SCALE);
		this._fButton_btn.on("pointerdown", (e) => {
			e.stopPropagation()
		});
		this._fButton_btn.on('pointerclick', (e) => {
			e.stopPropagation();
		});
		this._fButton_btn.on('pointerup', (e) => {
			e.stopPropagation();
			this._onButtonClick(e);
		})

		this.addChild(this._fButton_btn);

		if ( !navigator.userAgent.match(/Tablet|iPad/i) )
		{
			this.position.set(POSITION_SMALL_DEVICE.x, POSITION_SMALL_DEVICE.y);
			this.scale.set(1.4);
		}
		else
		{
			this.position.set(POSITION.x, POSITION.y);
		}
	}

	_enable()
	{
		this._fButton_btn.enabled = true;
	}

	_disable()
	{
		this._fButton_btn.enabled = false;
	}

	_setState(aState_str)
	{
		this._fState_str = aState_str;
		switch (this._fState_str)
		{
			case AutoTargetingSwitcherInfo.STATE_ON:
				this._startAnimation();
				break;
			case AutoTargetingSwitcherInfo.STATE_OFF:
				this._stopAnimation();
				break;
		}
	}

	_startAnimation()
	{
		let mult = INITIAL_SCALE;
		let sequence = [
			{
				tweens: [
					{prop: 'scale.x', to: 0.8 * mult},
					{prop: 'scale.y', to: 0.8 * mult}					
				],
				duration: 100
			},
			{
				tweens: [
					{prop: 'scale.x', to: 1.2 * mult},
					{prop: 'scale.y', to: 1.2 * mult}
				],
				duration: 100
			},
			{
				tweens: [
					{prop: 'scale.x', to: 1 * mult},
					{prop: 'scale.y', to: 1 * mult}
				],
				duration: 100
			},
			{
				tweens: [],
				duration: 100,
				onfinish: (e) => {
					this._startAnimation();
				}
			}
		];
		Sequence.start(this._fButton_btn, sequence);
	}

	_stopAnimation()
	{
		Sequence.destroy(Sequence.findByTarget(this._fButton_btn));
		this._fButton_btn.scale.set(INITIAL_SCALE);
	}

	_onButtonClick(e)
	{
		this.emit(AutoTargetingSwitcherView.EVENT_ON_CLICK);
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this._fButton_btn));
		super.destroy();
	}

}

export default AutoTargetingSwitcherView