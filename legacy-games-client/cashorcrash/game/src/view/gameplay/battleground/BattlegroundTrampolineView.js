import MTimeLine from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine";
import { AtlasSprite, Sprite } from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import { APP } from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import AtlasConfig from "../../../config/AtlasConfig";

class BattlegroundTrampolineView extends Sprite
{
	get flamesInfo()
	{
		return [
			{x: -49, y: 45, angle: 10, z: 2},
			{x: 60, y: 43, angle: 8, z: 2},
			{x: -55, y: 0, angle: 10, z: -1},
			{x: 55, y: 0, angle: 8, z: -1}
		];
	}

	trampolineJump()
	{
		this._fTrampolineSprite_sprt.gotoAndPlay(0);
	}
	
	constructor()
	{
		super();

		this._fTrampolineContainer_sprt = this.addChild(new Sprite);
		this._fTrampolineSprite_sprt = this._fTrampolineContainer_sprt.addChild(Sprite.createMultiframesSprite(BattlegroundTrampolineView.getTrampolineTextures()));
		this._fTrampolineSprite_sprt.loop = false;

		this._fFlameSprite_sprt_arr = [];
		for (let i = 0; i < this.flamesInfo.length; i++)
		{
			let lFlame_sprt = this._fTrampolineContainer_sprt.addChild(Sprite.createMultiframesSprite(BattlegroundTrampolineView.getFlameTextures()));
			lFlame_sprt.blendMode = PIXI.BLEND_MODES.ADD;
			lFlame_sprt.position.set(this.flamesInfo[i].x, this.flamesInfo[i].y);
			lFlame_sprt.zIndex = this.flamesInfo[i].z;
			lFlame_sprt.angle = this.flamesInfo[i].angle;
			lFlame_sprt.loop = true;
			this._fFlameSprite_sprt_arr.push(lFlame_sprt);

			let lGlow_sprt = this._fTrampolineContainer_sprt.addChild(new Sprite(APP.library.getAsset('game/battleground/trampoline_glow')));
			lGlow_sprt.blendMode = PIXI.BLEND_MODES.ADD;
			lGlow_sprt.position.set(this.flamesInfo[i].x, this.flamesInfo[i].y);
			lGlow_sprt.zIndex = this.flamesInfo[i].z - 1;
			lGlow_sprt.scale.set(2, 4);
		}

		let lGlow_sprt = this._fTrampolineContainer_sprt.addChild(new Sprite(APP.library.getAsset('game/battleground/trampoline_glow')));
		lGlow_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lGlow_sprt.position.set(0, 20);
		lGlow_sprt.zIndex = -2;
		lGlow_sprt.scale.set(4, 2);

		let l_mtl = this._fWiggleAnimation_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fTrampolineContainer_sprt,
			MTimeLine.SET_X,
			0,
			[
				[1, 24],
				[-1, 48],
				[0, 24]
			],
			this
		);

		l_mtl.addAnimation(
			this._fTrampolineContainer_sprt,
			MTimeLine.SET_Y,
			0.5,
			[
				[1, 12],
				[-1, 48],
				[0.5, 36]
			],
			this
		);

		l_mtl.addAnimation(
			this._fTrampolineContainer_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[1, 18],
				[-1, 36],
				[0, 18]
			],
			this
		);

		this._fWiggleCache_obj = {};
	}

	startIdleAnimation()
	{
		for (let i = 0; i < this._fFlameSprite_sprt_arr.length; i++)
		{
			this._fFlameSprite_sprt_arr[i].play();
		}

		this._fWiggleAnimation_mtl.playLoop();
	}
}

export default BattlegroundTrampolineView;

BattlegroundTrampolineView.getTrampolineTextures = function()
{
	if (!BattlegroundTrampolineView.trampoline_textures)
	{
		BattlegroundTrampolineView.trampoline_textures = [];
		BattlegroundTrampolineView.trampoline_textures = AtlasSprite.getFrames([APP.library.getAsset('game/battleground/trampoline')],
			[AtlasConfig.Trampoline], '');
	}

	let obj = {};
	BattlegroundTrampolineView.trampoline_textures.map((val) => {return obj[val._atlasName] = val;});
	let animationTextures = [];
	animationTextures.push(
		obj.middle,
		obj.bottom1,
		obj.bottom2,
		obj.bottom2,
		obj.bottom2,
		obj.bottom2,
		obj.bottom1,
		obj.middle,
		obj.top,
		obj.top,
		obj.middle
	);

	return animationTextures;
}

BattlegroundTrampolineView.getFlameTextures = function()
{
	if (!BattlegroundTrampolineView.flame_textures)
	{
		BattlegroundTrampolineView.flame_textures = [];
		BattlegroundTrampolineView.flame_textures = AtlasSprite.getFrames([APP.library.getAsset('game/battleground/trampoline_flame')],
			[AtlasConfig.TrampolineFlame], '');
		BattlegroundTrampolineView.flame_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return BattlegroundTrampolineView.flame_textures;
}