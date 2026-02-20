import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';

class ArtilleryCanisterSmoke extends Sprite {

	static get EVENT_ON_ANIMATION_END() 		{ return 'EVENT_ON_ANIMATION_END'; }
	static get TYPE_WHITE() 					{ return 0; }
	static get TYPE_GREEN() 					{ return 1; }

	constructor(aTypeId_int = 0, aDelay_num = 0)
	{
		super();

		this._fTypeId_int = aTypeId_int;

		this._fSmokes_sprt_arr = [];

		
		this._fFadeOutTimer_tmr = null;

		this._fCreationTimer_tmr = new Timer(this._createView.bind(this), aDelay_num);
	}

	_createView()
	{
		let lSmokeAssetName_str = this._getSmokeAssetName();

		let lPositionsY_num_arr = [235, 165, 95, 10, -90];
		
		//smoke1...
		let smoke1 = this.addChild(APP.library.getSpriteFromAtlas(lSmokeAssetName_str));
		smoke1.anchor.set(0.5, 1);
		smoke1.scale.set(2*0.2);
		smoke1.position.set(0, lPositionsY_num_arr[2]);

		let seq1 = [
			{
				tweens: [
					{ prop: 'y', to:  lPositionsY_num_arr[3] },
					{ prop: 'scale.x', to: 2*0.23 },
					{ prop: 'scale.y', to: 2*0.3 }
				],
				duration: 50*2*16.7
			},
			{
				tweens: [
					{ prop: 'y', to:  lPositionsY_num_arr[4] },					
					{ prop: 'scale.y', to: 2*0.5 }
				],
				duration: 74*2*16.7,
				onfinish: _=> {
					smoke1.destroy();
				}
			}			
		];

		Sequence.start(smoke1, seq1);
		this._fSmokes_sprt_arr.push(smoke1);
		//...smoke1

		//smoke2...
		let smoke2 = this.addChild(APP.library.getSpriteFromAtlas(lSmokeAssetName_str));
		smoke2.anchor.set(0.5, 1);
		smoke2.scale.set(2*0.2);
		smoke2.position.set(0,  lPositionsY_num_arr[1]);

		let seq2 = [
			{
				tweens: [
					{ prop: 'y', to:  lPositionsY_num_arr[2] }
				],
				duration: 34*2*16.7
			},
			{
				tweens: [
					{ prop: 'y', to:  lPositionsY_num_arr[3] },
					{ prop: 'scale.x', to: 2*0.23 },
					{ prop: 'scale.y', to: 2*0.3 }
				],
				duration: 50*2*16.7
			},
			{
				tweens: [
					{ prop: 'y', to:  lPositionsY_num_arr[4] },
					{ prop: 'scale.x', to: 2*0.23 },
					{ prop: 'scale.y', to: 2*0.5 }
				],
				duration: 74*2*16.7,
				onfinish: _=> {
					smoke2.destroy();
				}
			}
		];

		Sequence.start(smoke2, seq2);
		this._fSmokes_sprt_arr.push(smoke2);
		//...smoke2

		//smoke3...
		let smoke3 = this.addChild(APP.library.getSpriteFromAtlas(lSmokeAssetName_str));
		smoke3.anchor.set(0.5, 1);
		smoke3.scale.set(2*0.2);
		smoke3.position.set(0,  lPositionsY_num_arr[0]);

		let seq3 = [
			{
				tweens: [
					{ prop: 'y', to:  lPositionsY_num_arr[1] }
				],
				duration: 34*2*16.7
			},
			{
				tweens: [
					{ prop: 'y', to:  lPositionsY_num_arr[2] }
				],
				duration: 34*2*16.7
			},
			{
				tweens: [
					{ prop: 'y', to:  lPositionsY_num_arr[3] },
					{ prop: 'scale.x', to: 2*0.23 },
					{ prop: 'scale.y', to: 2*0.3 }
				],
				duration: 29*2*16.7
			},
			{
				tweens: [
					{ prop: 'y', to:  lPositionsY_num_arr[4] },
					{ prop: 'scale.x', to: 2*0.23 },
					{ prop: 'scale.y', to: 2*0.5 }
				],
				duration: 58*2*16.7,
				onfinish: _=> {
					smoke3.destroy();
				}
			}
		];

		Sequence.start(smoke3, seq3);
		this._fSmokes_sprt_arr.push(smoke3);
		//...smoke3

		this.alpha = 0;
		this.fadeTo(1, 10*2*16.7);

		this._fFadeOutTimer_tmr = new Timer(this._onStartFadeOut.bind(this), 55*2*16.7);

	}

	_onStartFadeOut()
	{
		this.fadeTo(0, 19*2*16.7, null, _=> {
			this._onAnimationEnd();
		})
	}

	_getSmokeAssetName()
	{
		switch (this._fTypeId_int)
		{
			case ArtilleryCanisterSmoke.TYPE_GREEN:
				return  'weapons/ArtilleryStrike/SmokeGrenadeTrail_unmult_green_blurred';
		}
		//case ArtilleryCanisterSmoke.TYPE_WHITE:
		return 'weapons/ArtilleryStrike/SmokeGrenadeTrail_unmult_blurred';
	}

	_onAnimationEnd()
	{
		this.emit(ArtilleryCanisterSmoke.EVENT_ON_ANIMATION_END);
	}

	destroy()
	{
		this._fFadeOutTimer_tmr && this._fFadeOutTimer_tmr.destructor();
		this._fFadeOutTimer_tmr = null;

		this._fCreationTimer_tmr && this._fCreationTimer_tmr.destructor();
		this._fCreationTimer_tmr = null;

		while (this._fSmokes_sprt_arr.length > 0)
		{
			let lSmoke_sprt = this._fSmokes_sprt_arr.pop();
			Sequence.destroy(Sequence.findByTarget(lSmoke_sprt));
		}

		this._fSmokes_sprt_arr = null;

		super.destroy();
	}
}

export default ArtilleryCanisterSmoke;