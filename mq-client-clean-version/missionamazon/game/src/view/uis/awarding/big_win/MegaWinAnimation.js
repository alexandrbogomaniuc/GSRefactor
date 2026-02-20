import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Timer from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import AtlasConfig from './../../../../config/AtlasConfig';
import { AtlasSprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import BigWinAnimation from './BigWinAnimation';
import HugeWinAnimation from './HugeWinAnimation';
import { FRAME_RATE } from '../../../../../../shared/src/CommonConstants';

let _particle_textures = null;
function _generateParticlesTextures()
{
	if (_particle_textures) return

	_particle_textures = AtlasSprite.getFrames([APP.library.getAsset("big_win/particles/big_win_particle_1"), APP.library.getAsset("big_win/particles/big_win_particle_2"), APP.library.getAsset("big_win/particles/big_win_particle_3")], [AtlasConfig.BigWinParticles1, AtlasConfig.BigWinParticles2, AtlasConfig.BigWinParticles3], "");
}

class MegaWinAnimation extends HugeWinAnimation
{
	constructor(aPayoutValue_num)
	{
		super(aPayoutValue_num);

		_generateParticlesTextures();

		this._fMegaBurst1_sprt = null;
		this._fMegaBurst2_sprt = null;
		this._fBurst1Timer_t = null;
		this._fBurst2Timer_t = null;
	}

	_startAnimation()
	{
		super._startAnimation();

		this._startMegaBurstAnimation();
	}

	get _burstAsset()
	{
		return "big_win/mega_win_burst";
	}

	_startMegaBurstAnimation()
	{
		this._fMegaBurst1_sprt = this.addChild(new Sprite());
		this._fMegaBurst1_sprt.scale.set(2);
		this._fMegaBurst1_sprt.position.set(0, -30);
		this._fMegaBurst1_sprt.textures = _particle_textures;
		this._fMegaBurst1_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		this._fMegaBurst1_sprt.on('animationend', () => {
			this._fMegaBurst1_sprt && this._fMegaBurst1_sprt.destroy();
			this._fMegaBurst1_sprt = null;

			this._validateEnding();
		});
		this._fMegaBurst1_sprt.visible = false;
		this._fBurst1Timer_t = new Timer(()=>{
			this._fMegaBurst1_sprt.visible = true;
			this._fMegaBurst1_sprt.play();
		}, 4*FRAME_RATE);

		this._fMegaBurst2_sprt = this.addChild(new Sprite());
		this._fMegaBurst2_sprt.scale.set(2);
		this._fMegaBurst2_sprt.position.set(0, -30);
		this._fMegaBurst2_sprt.textures = _particle_textures;
		this._fMegaBurst2_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		this._fMegaBurst2_sprt.on('animationend', () => {
			this._fMegaBurst2_sprt && this._fMegaBurst2_sprt.destroy();
			this._fMegaBurst2_sprt = null;

			this._validateEnding();
		});
		this._fMegaBurst2_sprt.visible = false;
		this._fBurst2Timer_t = new Timer(()=>{
			this._fMegaBurst2_sprt.visible = true;
			this._fMegaBurst2_sprt.play();
		}, 33*FRAME_RATE);
	}

	_startCoinsExplodeAnimation()
	{
		this._startNextExplosion(6*FRAME_RATE, {x: 0, y: 30});
		this._startNextExplosion(13*FRAME_RATE, {x: -39, y: 30});
		this._startNextExplosion(17*FRAME_RATE, {x: 0, y: 30});
		this._startNextExplosion(24*FRAME_RATE, {x: -39, y: 30});
		this._startNextExplosion(35*FRAME_RATE, {x: -39, y: 30});
		this._startNextExplosion(47*FRAME_RATE, {x: 0, y: 30});
		this._startNextExplosion(57*FRAME_RATE, {x: -39, y: 30});
		this._startNextExplosion(59*FRAME_RATE, {x: 0, y: 30}, true);
	}

	get _captionGlowAsset()
	{
		return "TAMegaWinGlowCaption";
	}

	get _payoutGlowColor()
	{
		return 0xfb6b6b;
	}

	get _coinsAmount()
	{
		return 40;
	}

	get _captionAsset()
	{
		return "TAMegaWinCaption";
	}

	get _glowAsset()
	{
		return "big_win/glow_mega_win";
	}

	get _circleAssetIn()
	{
		return "big_win/circle_mega_win";
	}

	get _circleAssetOut()
	{
		return "big_win/circle_mega_win";
	}

	get _isSomethingAnimating()
	{
		return	super._isSomethingAnimating ||
				this._fMegaBurst1_sprt ||
				this._fMegaBurst2_sprt;
	}

	destroy()
	{
		this._fBurst1Timer_t && this._fBurst1Timer_t.destructor();
		this._fBurst2Timer_t && this._fBurst2Timer_t.destructor();

		super.destroy();

		this._fMegaBurst1_sprt = null;
		this._fMegaBurst2_sprt = null;
		this._fBurst1Timer_t = null;
		this._fBurst2Timer_t = null;
	}
}

export default MegaWinAnimation;