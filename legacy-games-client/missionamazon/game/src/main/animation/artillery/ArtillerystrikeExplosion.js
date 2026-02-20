import GrenadeEffectsAnimation from '../grenade/GrenadeEffectsAnimation';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class ArtillerystrikeExplosion extends GrenadeEffectsAnimation {

	constructor(aOptIsBig_bl = false)
	{
		super();
		this._fIsBigExplosion_bl = aOptIsBig_bl;

		if (!aOptIsBig_bl)
		{
			this.scale.set(0.8);
		}
		else
		{
			this.scale.set(1.6);
		}
	}

	//override
	_addPillar()
	{
		//nothing to do
	}

	//override
	_addGrenadeBlast()
	{
		let lGrads_num_arr = [0, 250, 180, 120, 60];
		for (let i=0; i<lGrads_num_arr.length; i++)
		{
			let lGrad_num = lGrads_num_arr[i];
			let grenadeBlast = super._addGrenadeBlast();
			grenadeBlast.rotation = Utils.gradToRad(lGrad_num);
			grenadeBlast.zIndex = grenadeBlast.zIndex + i;
			if (!this._fIsBigExplosion_bl && i == 0)
			{
				grenadeBlast.once('animationend', (e) => {
					this.emit('animationFinish');
					this.destroy();
				})
			}
		}
	}

	//override
	_addGrenadeBlast2() {
	}

	//override
	_addGlow() {
		let glow = this.addChild(APP.library.getSprite('common/crate_glow'));
		glow.zIndex = 100-11;
		glow.blendMode = PIXI.BLEND_MODES.ADD;
		glow.scale.set(0);

		let sequence = [
			{
				tweens: [
					{prop: 'scale.x', to: 1.54*2},
					{prop: 'scale.y', to: 1.54*2}
				],
				duration: 2*2*16.6
			},
			{
				tweens: [],
				duration: 2*2*16.6
			},
			{
				tweens: [
					{prop: 'scale.x', to: 3.73*2},
					{prop: 'scale.y', to: 3.73*2}
				],
				duration: 1*2*16.6
			},
			{
				tweens: [
					{prop: 'scale.x', to: 0},
					{prop: 'scale.y', to: 0}
				],
				duration: 3*2*16.6,
				onfinish: (e) => {
					glow.destroy();
				}
			}
		]

		let seq = Sequence.start(glow, sequence, 4*2*16.6);
		this._addSequence(seq);
	}

	//override
	_addGlow2() {

	}

	//override
	_addDimmer() {

	}

	//override
	_addGroundSmoke(params, container)
	{
		if (this._fIsBigExplosion_bl)
		{
			super._addGroundSmoke(params, container);
		}
	}

	//override
	_addBurstSmoke(params, container)
	{

	}
}

export default ArtillerystrikeExplosion;