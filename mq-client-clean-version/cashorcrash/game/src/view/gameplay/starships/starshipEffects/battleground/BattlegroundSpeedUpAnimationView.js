import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MTimeLine from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasSprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../../config/AtlasConfig';
import GameplayController from '../../../../../controller/gameplay/GameplayController';
import BattlegroundStarshipCrashExplosionView from './BattlegroundStarshipCrashExplosionView';
import StarshipBaseView from '../../StarshipBaseView';

class BattlegroundSpeedUpAnimationView extends Sprite
{
    constructor(aShip_Id = 0, aStarshipView)
    {
        super();
        this._fExplosionView_sprt = null;
        this._fExplosionFramesAnimation_rcfav = null;
        this._fpreTakeOffAnimationBTG = null;

        this._fStarshipView = aStarshipView;
        this._fShipId = aShip_Id;
        let l_gpv = APP.gameController.view;

        this._fBody_sprt = this._generateStarshipBlurBody(this._fShipId);
		this._fBody_sprt.scale.set(0.4);
        this._fBody_sprt.alpha = 0.84;
		this.addChild(this._fBody_sprt);

        //FLAME...
        let lExplosion_sprt = new Sprite();
        let lExplosionView_sprt = this._fExplosionView_sprt = Sprite.createMultiframesSprite(BattlegroundStarshipCrashExplosionView.getFlameExplosionTextures());
        lExplosion_sprt.addChild(lExplosionView_sprt);
        lExplosionView_sprt.anchor.set(0.5, 0.5);
        lExplosionView_sprt.visible = false;
        lExplosionView_sprt.angle = 180;
        lExplosionView_sprt.position.set(0, 85);
        lExplosionView_sprt.scale.set(0.63);
        lExplosionView_sprt.blendMode = PIXI.BLEND_MODES.ADD;
        lExplosionView_sprt.loop = false;

        this._fExplosionFramesAnimation_rcfav = this.addChild(lExplosion_sprt);
        //...FLAME

        let lRG_sprt = this._fRG_sprt = this.addChild(APP.library.getSprite("game/battleground/redglow_add"));
		lRG_sprt.position.set(-20, 70);
        lRG_sprt.anchor.set(0.34);
        lRG_sprt.alpha = 0;
		lRG_sprt.blendMode = PIXI.BLEND_MODES.ADD;

        let l_fsprt = this._fGlowFrame_sprt = APP.library.getSprite("game/frame_fx_round_final");
        l_fsprt.anchor.set(0);
        l_fsprt.scale.set(4.45, 4);

        let lWhiteArea = this._fWhiteArea_pgs = new PIXI.Graphics();
        lWhiteArea.beginFill(0xffffff);
        lWhiteArea.drawRect(0,0,l_fsprt.width,l_fsprt.height);
        lWhiteArea.endFill();
        lWhiteArea.alpha = 0;
        l_gpv.addChild(lWhiteArea);

        if(APP.layout.isPortraitOrientation)
        {
            l_fsprt.position.set(l_fsprt.height,0);
            lWhiteArea.position.set(l_fsprt.height,0);
            l_fsprt.angle = 90;
            lWhiteArea.angle = 90;
        }
        this._fWhiteArea_pgs.visible = false;
        this._fGlowFrame_sprt.visible = false;
        l_fsprt.alpha = 0;
        l_fsprt.blendMode = PIXI.BLEND_MODES.ADD;
        l_gpv.addChild(l_fsprt);

        let lptoa_mtl = new MTimeLine();

        lptoa_mtl.addAnimation(
			lRG_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				1,
				[0.6, 2],
				[0, 6]
			]);

        lptoa_mtl.addAnimation(
			lRG_sprt,
			MTimeLine.SET_SCALE,
			0.3,
			[
				1,
				[7, 8, MTimeLine.EASE_IN]
			]);

        lptoa_mtl.addAnimation(
            lWhiteArea,
            MTimeLine.SET_ALPHA,
            0,
            [
                [0.33, 2],
                [0, 9, MTimeLine.EASE_IN_OUT]
            ]);

        lptoa_mtl.addAnimation(
            lExplosionView_sprt,
            MTimeLine.SET_ALPHA,
            1,
            [
                14,
                [0, 1]
            ]);
        
        this._fpreTakeOffAnimationBTG = lptoa_mtl;

        let lfgf_mtl = new MTimeLine();

        lfgf_mtl.addAnimation(
            l_fsprt,
            MTimeLine.SET_ALPHA,
            0.48,
            [
				[0.52, 2],
				[0.48, 2]
			]);

        this._fForegroundFrameAnimation = lfgf_mtl;

        APP.layout.on('orientationchange', this.onOrientationChanged, this);

        this.visible = false;
    }

    adjust()
    {
        let l_gpi = APP.gameController.gameplayController.info;
        let l_ri = l_gpi.roundInfo;

		if (!l_ri.isRoundPlayActive || !l_gpi.allEjectedTime)
		{
			this.visible = false;
			this._resetAnimations();
		}
        else
		{
			this.visible = true;
            if (!this._fForegroundFrameAnimation.isPlaying() && this._fExplosionView_sprt.currentFrame == 0)
            {
                this._fForegroundFrameAnimation.playLoop();
            }
		}
    }

    activateListeners()
    {
        this._fStarshipView.on(StarshipBaseView.EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED, this._resetAnimations, this);
        APP.gameController.gameplayController.on(GameplayController.EVENT_ON_ALL_ASTRONAUTS_EJECTED_BTG, this._onPreTakeOffAnimationRequired, this);
    }

    deactivate()
    {
        this._fStarshipView.off(StarshipBaseView.EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED, this._resetAnimations, this);
        APP.gameController.gameplayController.off(GameplayController.EVENT_ON_ALL_ASTRONAUTS_EJECTED_BTG, this._onPreTakeOffAnimationRequired, this);

        this._resetAnimations();
    }

    _generateStarshipBlurBody(aShip_Id)
	{
		let l_sprt = new Sprite;
        l_sprt.textures = [BattlegroundSpeedUpAnimationView.getStarhipBlurTextures()[aShip_Id]];

		return l_sprt;
	}

    _resetAnimations()
    {
        this.visible = false;
        this._fpreTakeOffAnimationBTG.stop();
        this._fpreTakeOffAnimationBTG.reset();
        this._fForegroundFrameAnimation.stop();
        this._fGlowFrame_sprt.alpha = 0;
        this._fExplosionView_sprt.gotoAndStop(0);
        this._fExplosionView_sprt.visible = false;
        this._fWhiteArea_pgs.visible = false;
        this._fGlowFrame_sprt.visible = false;
    }

    _onPreTakeOffAnimationRequired()
    {
        if(this.visible)
        {
            return;
        }
        this._fpreTakeOffAnimationBTG.play();
        this._fStarshipView.startWiggleStarshipSpeedUp();
        this._fForegroundFrameAnimation.playLoop();
        this._fExplosionView_sprt.visible = true;
        this._fWhiteArea_pgs.visible = true;
        this._fGlowFrame_sprt.visible = true;
        this._fExplosionView_sprt.gotoAndPlay(0);
        this.visible = true;
    }

    onOrientationChanged(e)
    {
        let l_fsprt = this._fGlowFrame_sprt;
        let lWhiteArea = this._fWhiteArea_pgs;
        if(APP.layout.isPortraitOrientation)
        {
            l_fsprt.position.set(l_fsprt.height,0);
            lWhiteArea.position.set(l_fsprt.height,0);
            l_fsprt.angle = 90;
            lWhiteArea.angle = 90;
        }
        else
        {
            l_fsprt.position.set(0,0);
            lWhiteArea.position.set(0,0);
            l_fsprt.angle = 0;
            lWhiteArea.angle = 0;
        }
    }

    destroy()
    {
        this._fStarshipView = null;
        this._fShipId = null;
        this._fRG_sprt && this._fRG_sprt.destroy();
        this._fRG_sprt = null;
        this._fBody_sprt && this._fBody_sprt.destroy();
        this._fBody_sprt = null;
        this._fExplosionView_sprt && this._fExplosionView_sprt.destroy();
        this._fExplosionView_sprt = null;
        this._fGlowFrame_sprt && this._fGlowFrame_sprt.destroy();
        this._fGlowFrame_sprt = null;
        this._fWhiteArea_pgs && this._fWhiteArea_pgs.destroy();
        this._fWhiteArea_pgs = null;
        this._fExplosionFramesAnimation_rcfav && this._fExplosionFramesAnimation_rcfav.destroy();
        this._fExplosionFramesAnimation_rcfav = null;

        this._fpreTakeOffAnimationBTG && this._fpreTakeOffAnimationBTG.destroy();
        this._fpreTakeOffAnimationBTG = null;
        this._fForegroundFrameAnimation && this._fForegroundFrameAnimation.destroy();
        this._fForegroundFrameAnimation = null;

        this._fStarshipView.off(StarshipBaseView.EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED, this._onExplosionStarted, this);
        APP.gameController.gameplayController.off(GameplayController.EVENT_ON_ALL_ASTRONAUTS_EJECTED_BTG, this._onPreTakeOffAnimationRequired, this);
        APP.layout.off('orientationchange', this.onOrientationChanged, this);
    }
}

BattlegroundSpeedUpAnimationView.getStarhipBlurTextures = function()
{
	if (!BattlegroundSpeedUpAnimationView.starship_blur_textures)
	{
		BattlegroundSpeedUpAnimationView.starship_blur_textures = [];

		BattlegroundSpeedUpAnimationView.starship_blur_textures = AtlasSprite.getFrames([APP.library.getAsset('game/battleground/ship_explosion/ShipsBlur')], [AtlasConfig.ShipsBlur], 'starshipBlur');
		BattlegroundSpeedUpAnimationView.starship_blur_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return BattlegroundSpeedUpAnimationView.starship_blur_textures;
}

export default BattlegroundSpeedUpAnimationView