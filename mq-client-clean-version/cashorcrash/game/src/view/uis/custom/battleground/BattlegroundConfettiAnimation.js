import { Sprite } from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import { APP } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import AtlasSprite from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite";
import AtlasConfig from "../../../../config/AtlasConfig";
import MTimeLine from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine";

class BattlegroundConfettiAnimation extends Sprite
{
	constructor()
    {
        super();
       
        let lConfettiFlyContainer_sprt = this._fConfettiFlContainer_sprt = this.addChild(new Sprite());

        //1...
        let lConfettiContainer1_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle1_sprt = lConfettiContainer1_sprt.addChild(this._generateLightParticleView());
        let lConfetti_sprt = lConfettiContainer1_sprt.addChild(new Sprite());
        lConfetti_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];
        //...1

        //2...
        let lConfettiContainer2_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle2_sprt = lConfettiContainer2_sprt.addChild(this._generateLightParticleView());
        let lConfetti2_sprt = lConfettiContainer2_sprt.addChild(new Sprite());
        lConfetti2_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[1]];
        //...2

        //3...
        let lConfettiContainer3_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        lConfettiContainer3_sprt.scale.set(0.88);
        let lLightParticle3_sprt = lConfettiContainer3_sprt.addChild(this._generateLightParticleView());
        let lConfetti3_sprt = lConfettiContainer3_sprt.addChild(new Sprite());
        lConfetti3_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[2]];;
        //...3

        //4...
        let lConfettiContainer4_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle4_sprt = lConfettiContainer4_sprt.addChild(this._generateLightParticleView());
        let lConfetti4_sprt = lConfettiContainer4_sprt.addChild(new Sprite());
        lConfetti4_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[3]];;
        //...4

        //5...
        let lConfettiContainer5_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle5_sprt = lConfettiContainer5_sprt.addChild(this._generateLightParticleView());
        let lConfetti5_sprt = lConfettiContainer5_sprt.addChild(new Sprite());
        lConfetti5_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];;
        //...5

        //6...
        let lConfettiContainer6_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        lConfettiContainer6_sprt.scale.set(0.88)
        let lLightParticle6_sprt = lConfettiContainer6_sprt.addChild(this._generateLightParticleView());
        let lConfetti6_sprt = lConfettiContainer6_sprt.addChild(new Sprite());
        lConfetti6_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[2]];;
        //...6

        //7...
        let lConfettiContainer7_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle7_sprt = lConfettiContainer7_sprt.addChild(this._generateLightParticleView());
        let lConfetti7_sprt = lConfettiContainer7_sprt.addChild(new Sprite());
        lConfetti7_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[1]];
        let lConfetti7Flash_sprt = lConfettiContainer7_sprt.addChild(APP.library.getSprite("game/battleground/star_glow"));
        //...7

        //8...
        let lConfettiContainer8_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle8_sprt = lConfettiContainer8_sprt.addChild(this._generateLightParticleView());
        let lConfetti8_sprt = lConfettiContainer8_sprt.addChild(new Sprite());
        lConfetti8_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];;
        //...8

        let l_catl = new MTimeLine();

        l_catl.addAnimation(
            lConfettiContainer1_sprt,
            MTimeLine.SET_X,
            -100,
            [
                [-88, 7],
                [-115, 39],
                [-100, 6]
            ]);

        l_catl.addAnimation(
            lConfettiContainer1_sprt,
            MTimeLine.SET_Y,
            -251.5,
            [
                [265.5, 53]
            ]);

        l_catl.addAnimation(
            lConfetti_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                [176.7, 53]
            ]);

        l_catl.addAnimation(
            lConfetti_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle1_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                6,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);

        l_catl.addAnimation(
            lConfettiContainer3_sprt,
            MTimeLine.SET_X,
            -10,
        [
                1,
                [-240, 46],
                [-225, 5]
            ]);

        l_catl.addAnimation(
            lConfettiContainer3_sprt,
            MTimeLine.SET_Y,
            -251.5,
            [
                1,
                [265.5, 53]
            ]);

        l_catl.addAnimation(
            lConfetti3_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            22.1,
            [
                1,
                [62.2, 26],
                [29.9, 27]
            ]);

        l_catl.addAnimation(
            lConfetti3_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                1,
                [-1, 10],
                [1, 10]
                [-1, 10],
                [1, 10],
                [-1, 10],
                [-0.568, 3]
            ]);
        
        l_catl.addAnimation(
            lLightParticle3_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                13,
                [1, 2],
                [0, 3],
                2,
                [1, 2],
                [0, 3],
                21,
                [1, 2],
                [0, 3]
            ]);

        l_catl.addAnimation(
            lConfettiContainer2_sprt,
            MTimeLine.SET_X,
            40,
            [
                1,
                [-30, 46],
                [-15, 5]
            ]);

        l_catl.addAnimation(
            lConfettiContainer2_sprt,
            MTimeLine.SET_Y,
            -251.5,
            [
                1,
                [265.5, 53]
            ]);

        l_catl.addAnimation(
            lConfetti2_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            88.3,
            [
                1,
                [92.3, 2],
                [-100, 15],
                [100, 17],
                [-100, 17],
            ]);

        l_catl.addAnimation(
            lConfetti2_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                1,
                [-1, 17],
                [1, 17]
                [-1, 17]
            ]);
        
        l_catl.addAnimation(
            lLightParticle3_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                3,
                [1, 2],
                [0, 3],
                5,
                [1, 2],
                [0, 3],
                13,
                [1, 2],
                [0, 3],
                10,
                [1, 2],
                [0, 3]
            ]);

        l_catl.addAnimation(
            lConfettiContainer4_sprt,
            MTimeLine.SET_X, 
            0,
            [
                6,
                [-300, 45],
                [-250, 13]
            ]);

        l_catl.addAnimation(
            lConfettiContainer4_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                6,
                [265.5, 58]
            ]);
        
        l_catl.addAnimation(
            lConfetti4_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            -127.8,
            [
                6,
                [116.4, 40],
                [85.1, 18]
            ]);

        l_catl.addAnimation(
            lConfetti4_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                6, 
                [-1, 32],
                [1, 32]
            ]);

        l_catl.addAnimation(
            lConfettiContainer5_sprt,
            MTimeLine.SET_X,
            160,
            [
                9,
                [190, 25],
                [165, 24]
            ]);

        l_catl.addAnimation(
            lConfettiContainer5_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                9,
                [265.5, 49]
            ]);

        l_catl.addAnimation(
            lConfetti5_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                9,
                [176.7, 49]
            ]);

        l_catl.addAnimation(
            lConfetti5_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                9,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle5_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                15,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);

        l_catl.addAnimation(
            lConfettiContainer6_sprt,
            MTimeLine.SET_X,
            170,
            [
                10,
                [60, 29],
                [77, 4]
            ]);

        l_catl.addAnimation(
            lConfettiContainer6_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                10,
                [265.5, 33]
            ]);

        l_catl.addAnimation(
            lConfetti6_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            22.1,
            [
                10,
                [62.2, 26],
                [29.9, 27]
            ]);

        l_catl.addAnimation(
            lConfetti6_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                10,
                [-1, 10],
                [1, 10]
                [-1, 10],
                [1, 10],
                [-1, 10],
                [-0.568, 3]
            ]);
        
        l_catl.addAnimation(
            lLightParticle6_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                23,
                [1, 2],
                [0, 3],
                2,
                [1, 2],
                [0, 3],
                21,
                [1, 2],
                [0, 3]
            ]);

        l_catl.addAnimation(
            lConfettiContainer7_sprt,
            MTimeLine.SET_X,
            120,
            [
                13,
                [170, 38],
            ]);

        l_catl.addAnimation(
            lConfettiContainer7_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                13,
                [265.5, 38]
            ]);

        l_catl.addAnimation(
            lConfetti7_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            88.3,
            [
                13,
                [92.3, 2],
                [-100, 15],
                [100, 17]
            ]);

        l_catl.addAnimation(
            lConfetti7_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                13,
                [-1, 17],
                [1, 17]
                [-1, 17]
            ]);

        l_catl.addAnimation(
            lConfetti7Flash_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                22,
                [1, 1],
                [0, 2]
            ]);

        l_catl.addAnimation(
            lConfetti7Flash_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                22,
                [22, 3]
            ]);
        
        l_catl.addAnimation(
            lLightParticle7_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                13,
                [1, 2],
                [0, 3],
                5,
                [1, 2],
                [0, 3]
            ]);

        //
        l_catl.addAnimation(
            lConfettiContainer8_sprt,
            MTimeLine.SET_X,
            40,
            [
                26,
                [45, 35],
                [65, 14]
            ]);

        l_catl.addAnimation(
            lConfettiContainer8_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                26,
                [265.5, 49]
            ]);

        l_catl.addAnimation(
            lConfetti8_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                26,
                [176.7, 49]
            ]);

        l_catl.addAnimation(
            lConfetti8_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                26,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle1_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                32,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);

        
        //9...
        let lConfettiContainer9_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle9_sprt = lConfettiContainer9_sprt.addChild(this._generateLightParticleView());
        let lConfetti9_sprt = lConfettiContainer9_sprt.addChild(new Sprite());
        lConfetti9_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[2]];;
        //...9

        l_catl.addAnimation(
            lConfettiContainer9_sprt,
            MTimeLine.SET_X,
            25,
            [
                28,
                [-10, 12],
                [30, 13]
            ]);

        l_catl.addAnimation(
            lConfettiContainer9_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                28,
                [265.5, 25]
            ]);

        l_catl.addAnimation(
            lConfetti9_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            22.1,
            [
                29,
                [62.2, 26]
            ]);

        l_catl.addAnimation(
            lConfetti9_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                29,
                [-1, 10],
                [1, 10]
                [-1, 10]
            ]);
        
        l_catl.addAnimation(
            lLightParticle9_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                39,
                [1, 2],
                [0, 3],
                2,
                [1, 2],
                [0, 3],
            ]);

        //10...
        let lConfettiContainer10_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle10_sprt = lConfettiContainer10_sprt.addChild(this._generateLightParticleView());
        let lConfetti10_sprt = lConfettiContainer10_sprt.addChild(new Sprite());
        lConfetti10_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[3]];
        let lConfetti10Flash_sprt = lConfettiContainer10_sprt.addChild(APP.library.getSprite("game/battleground/star_glow"));
        //...10

        l_catl.addAnimation(
            lConfettiContainer10_sprt,
            MTimeLine.SET_X,
            180,
            [
                44,
                [195, 27],
                [190, 27]
            ]);

        l_catl.addAnimation(
            lConfettiContainer10_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                44,
                [265.5, 54]
            ]);

        l_catl.addAnimation(
            lConfetti10_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            -127.8,
            [
                50,
                [116.4, 40],
                [85.1, 18]
            ]);

        l_catl.addAnimation(
            lConfetti10Flash_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                63,
                [1, 1],
                [0, 2],
                22,
                [1, 1],
                [0, 2]
            ]);

        l_catl.addAnimation(
            lConfetti10Flash_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                63,
                [22, 3],
                [0, 1],
                21,
                [22, 3]
            ]);

        l_catl.addAnimation(
            lConfetti10_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                50, 
                [-1, 32],
                [1, 32]
            ]);

        //11...
        let lConfettiContainer11_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle11_sprt = lConfettiContainer11_sprt.addChild(this._generateLightParticleView());
        let lConfetti11_sprt = lConfettiContainer11_sprt.addChild(new Sprite());
        lConfetti11_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[1]];;
        //...11

        l_catl.addAnimation(
            lConfettiContainer11_sprt,
            MTimeLine.SET_X,
            120,
            [
                45,
                [130, 13],
                [150, 17],
                [170, 11]
            ]);

        l_catl.addAnimation(
            lConfettiContainer11_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                45,
                [265.5, 41]
            ]);

        l_catl.addAnimation(
            lConfetti11_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            88.3,
            [
                46,
                [92.3, 2],
                [-100, 15],
                [100, 17],
                [-100, 17],
            ]);

        l_catl.addAnimation(
            lConfetti11_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                46,
                [-1, 17],
                [1, 17]
                [-1, 17]
            ]);
        
        l_catl.addAnimation(
            lLightParticle11_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                48,
                [1, 2],
                [0, 3],
                5,
                [1, 2],
                [0, 3],
                13,
                [1, 2],
                [0, 3],
                10,
                [1, 2],
                [0, 3]
            ]);

        //12...
        let lConfettiContainer12_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle12_sprt = lConfettiContainer12_sprt.addChild(this._generateLightParticleView());
        let lConfetti12_sprt = lConfettiContainer12_sprt.addChild(new Sprite());
        lConfetti12_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];
        //...12

        l_catl.addAnimation(
            lConfettiContainer12_sprt,
            MTimeLine.SET_X,
            -60,
            [
                48,
                [-50, 7],
                [-45, 30],
                [-35, 19]
            ]);

        l_catl.addAnimation(
            lConfettiContainer12_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                48,
                [265.5, 56]
            ]);

        l_catl.addAnimation(
            lConfetti12_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                48,
                [176.7, 53]
            ]);

        l_catl.addAnimation(
            lConfetti12_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                48,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle12_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                54,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);
        
        //13...
        let lConfettiContainer13_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle13_sprt = lConfettiContainer13_sprt.addChild(this._generateLightParticleView());
        let lConfetti13_sprt = lConfettiContainer13_sprt.addChild(new Sprite());
        lConfetti13_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[3]];
        //...13

        l_catl.addAnimation(
            lConfettiContainer13_sprt,
            MTimeLine.SET_X,
            -50,
            [
                49,
                [-50, 5],
                [-280, 48],
                [-260, 17]
            ]);

        l_catl.addAnimation(
            lConfettiContainer13_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                49,
                [265.5, 70]
            ]);

        l_catl.addAnimation(
            lConfetti13_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                49,
                [176.7, 53]
            ]);

        l_catl.addAnimation(
            lConfetti13_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            -127.8,
            [
                55,
                [116.4, 40],
                [85.1, 18]
            ]);

        l_catl.addAnimation(
            lConfetti13_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                55, 
                [-1, 32],
                [1, 32]
            ]);

        //14...
        let lConfettiContainer14_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle14_sprt = lConfettiContainer14_sprt.addChild(this._generateLightParticleView());
        let lConfetti14_sprt = lConfettiContainer14_sprt.addChild(new Sprite());
        lConfetti14_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];
        //...14

        l_catl.addAnimation(
            lConfettiContainer14_sprt,
            MTimeLine.SET_X,
            280,
            [
                49,
                [290, 10],
                [280, 41],
                [285, 14]
            ]);

        l_catl.addAnimation(
            lConfettiContainer14_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                49,
                [265.5, 65]
            ]);

        l_catl.addAnimation(
            lConfetti14_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                49,
                [176.7, 53]
            ]);

        l_catl.addAnimation(
            lConfetti14_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                49,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle14_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                55,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);

        //15...
        let lConfettiContainer15_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle15_sprt = lConfettiContainer15_sprt.addChild(this._generateLightParticleView());
        let lConfetti15_sprt = lConfettiContainer15_sprt.addChild(new Sprite());
        lConfetti15_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];
        //...15

        l_catl.addAnimation(
            lConfettiContainer15_sprt,
            MTimeLine.SET_X,
            -200,
            [
                54,
                [-240, 15],
                [-200, 14]
            ]);

        l_catl.addAnimation(
            lConfettiContainer15_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                54,
                [265.5, 29]
            ]);

        l_catl.addAnimation(
            lConfetti15_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                54,
                [176.7, 29]
            ]);

        l_catl.addAnimation(
            lConfetti15_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                54,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle15_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                60,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);

        //16...
        let lConfettiContainer16_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle16_sprt = lConfettiContainer16_sprt.addChild(this._generateLightParticleView());
        let lConfetti16_sprt = lConfettiContainer16_sprt.addChild(new Sprite());
        lConfetti16_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];
        //...16

        l_catl.addAnimation(
            lConfettiContainer16_sprt,
            MTimeLine.SET_X,
            -230,
            [
                57,
                [-300, 29],
                [-290, 20]
            ]);

        l_catl.addAnimation(
            lConfettiContainer16_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                57,
                [265.5, 49]
            ]);

        l_catl.addAnimation(
            lConfetti16_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                57,
                [176.7, 29]
            ]);

        l_catl.addAnimation(
            lConfetti16_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                57,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle16_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                63,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);

         //17...
        let lConfettiContainer17_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle17_sprt = lConfettiContainer17_sprt.addChild(this._generateLightParticleView());
        let lConfetti17_sprt = lConfettiContainer17_sprt.addChild(new Sprite());
        lConfetti17_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];
        let lConfetti17Flash_sprt = lConfettiContainer17_sprt.addChild(APP.library.getSprite("game/battleground/star_glow"));
        //...17

        l_catl.addAnimation(
            lConfettiContainer17_sprt,
            MTimeLine.SET_X,
            -210,
            [
                64,
                [-200, 4],
                [-290, 79],
                [-270, 6]
            ]);

        l_catl.addAnimation(
            lConfettiContainer17_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                64,
                [265.5, 89]
            ]);

        l_catl.addAnimation(
            lConfetti17_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                64,
                [176.7, 29]
            ]);

        l_catl.addAnimation(
            lConfetti17_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                64,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lConfetti17Flash_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                84,
                [1, 1],
                [0, 2]
            ]);
        
        
        l_catl.addAnimation(
            lConfetti17_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                84,
                [28, 3]
            ]);

        l_catl.addAnimation(
            lLightParticle17_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                70,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);

        //18...
        let lConfettiContainer18_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle18_sprt = lConfettiContainer18_sprt.addChild(this._generateLightParticleView());
        let lConfetti18_sprt = lConfettiContainer18_sprt.addChild(new Sprite());
        lConfetti18_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[1]];
        let lConfetti18Flash_sprt = lConfettiContainer18_sprt.addChild(APP.library.getSprite("game/battleground/star_glow"));
        //...18

        l_catl.addAnimation(
            lConfettiContainer18_sprt,
            MTimeLine.SET_X,
            250,
            [
                66,
                [300, 17],
                [290, 16],
                [260, 14]
            ]);

        l_catl.addAnimation(
            lConfettiContainer18_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                67,
                [265.5, 47]
            ]);

        l_catl.addAnimation(
            lConfetti18_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            88.3,
            [
                67,
                [92.3, 2],
                [-100, 15],
                [100, 17],
                [-100, 17],
            ]);

        l_catl.addAnimation(
            lConfetti18_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                67,
                [-1, 17],
                [1, 17]
                [-1, 17]
            ]);

        l_catl.addAnimation(
            lConfetti18Flash_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                92,
                [1, 1],
                [0, 2]
            ]);

        l_catl.addAnimation(
            lConfetti18Flash_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                92,
                [28, 3]
            ]);
        
        l_catl.addAnimation(
            lLightParticle18_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                70,
                [1, 2],
                [0, 3],
                5,
                [1, 2],
                [0, 3],
                13,
                [1, 2],
                [0, 3],
                10,
                [1, 2],
                [0, 3]
            ]);

         //19...
        let lConfettiContainer19_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle19_sprt = lConfettiContainer19_sprt.addChild(this._generateLightParticleView());
        let lConfetti19_sprt = lConfettiContainer19_sprt.addChild(new Sprite());
        lConfetti19_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];
        //...17

        l_catl.addAnimation(
            lConfettiContainer19_sprt,
            MTimeLine.SET_X,
            215,
            [
                70,
                [225, 20],
                [233, 16],
                [220, 25]
            ]);

        l_catl.addAnimation(
            lConfettiContainer19_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                70,
                [265.5, 61]
            ]);

        l_catl.addAnimation(
            lConfetti19_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                70,
                [176.7, 29]
            ]);

        l_catl.addAnimation(
            lConfetti19_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                70,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle19_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                70,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);

         //20...
        let lConfettiContainer20_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle20_sprt = lConfettiContainer20_sprt.addChild(this._generateLightParticleView());
        let lConfetti20_sprt = lConfettiContainer20_sprt.addChild(new Sprite());
        lConfetti20_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[1]];
        //...20

        l_catl.addAnimation(
            lConfettiContainer20_sprt,
            MTimeLine.SET_X,
            0,
            [
                76,
                [-20, 30],
                [-19, 6]
            ]);

        l_catl.addAnimation(
            lConfettiContainer20_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                76,
                [265.5, 36]
            ]);

        l_catl.addAnimation(
            lConfetti20_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            88.3,
            [
                77,
                [92.3, 2],
                [-100, 15],
                [100, 17],
                [-100, 17],
            ]);

        l_catl.addAnimation(
            lConfetti20_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                77,
                [-1, 17],
                [1, 17]
                [-1, 17]
            ]);
        
        l_catl.addAnimation(
            lLightParticle20_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                80,
                [1, 2],
                [0, 3],
                5,
                [1, 2],
                [0, 3],
                13,
                [1, 2],
                [0, 3],
                10,
                [1, 2],
                [0, 3]
            ]);

        //21...
        let lConfettiContainer21_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle21_sprt = lConfettiContainer21_sprt.addChild(this._generateLightParticleView());
        let lConfetti21_sprt = lConfettiContainer21_sprt.addChild(new Sprite());
        lConfetti21_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[3]];
        //...21

        l_catl.addAnimation(
            lConfettiContainer21_sprt,
            MTimeLine.SET_X,
            284,
            [
                77,
                [296, 19],
                [304, 24],
                [299, 22]
            ]);

        l_catl.addAnimation(
            lConfettiContainer21_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                77,
                [265.5, 64]
            ]);

        l_catl.addAnimation(
            lConfetti21_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            -127.8,
            [
                83,
                [116.4, 40],
                [85.1, 18]
            ]);

        l_catl.addAnimation(
            lConfetti21_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                83, 
                [-1, 32],
                [1, 32]
            ]);

         //22...
        let lConfettiContainer22_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle22_sprt = lConfettiContainer22_sprt.addChild(this._generateLightParticleView());
        let lConfetti22_sprt = lConfettiContainer22_sprt.addChild(new Sprite());
        lConfetti22_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[1]];
        //...22

        l_catl.addAnimation(
            lConfettiContainer22_sprt,
            MTimeLine.SET_X,
            10,
            [
                88,
                [50, 30],
                [63, 13]
            ]);

        l_catl.addAnimation(
            lConfettiContainer22_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                88,
                [265.5, 43]
            ]);

        l_catl.addAnimation(
            lConfetti22_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                87,
                [176.7, 29]
            ]);

        l_catl.addAnimation(
            lConfetti22_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                87,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle22_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                87,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);

         //23...
        let lConfettiContainer23_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle23_sprt = lConfettiContainer23_sprt.addChild(this._generateLightParticleView());
        let lConfetti23_sprt = lConfettiContainer23_sprt.addChild(new Sprite());
        lConfetti23_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];
        let lConfetti23Flash_sprt = lConfettiContainer23_sprt.addChild(APP.library.getSprite("game/battleground/star_glow"));
        //...23

        l_catl.addAnimation(
            lConfettiContainer23_sprt,
            MTimeLine.SET_X,
            -77,
            [
                87,
                [-72, 10],
                [-68, 29],
                [-57, 21]
            ]);

        l_catl.addAnimation(
            lConfettiContainer23_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                87,
                [265.5, 61]
            ]);

        l_catl.addAnimation(
            lConfetti23_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            88.3,
            [
                88,
                [92.3, 2],
                [-100, 15],
                [100, 17],
                [-100, 17],
            ]);

        l_catl.addAnimation(
            lConfetti23_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                88,
                [-1, 17],
                [1, 17]
                [-1, 17]
            ]);

        l_catl.addAnimation(
            lConfetti23Flash_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                109,
                [1, 1],
                [0, 2]
            ]);

        l_catl.addAnimation(
            lConfetti23Flash_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                109,
                [28, 3]
            ]);
        
        l_catl.addAnimation(
            lLightParticle23_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                91,
                [1, 2],
                [0, 3],
                5,
                [1, 2],
                [0, 3],
                13,
                [1, 2],
                [0, 3],
                10,
                [1, 2],
                [0, 3]
            ]);

        //24...
        let lConfettiContainer24_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle24_sprt = lConfettiContainer24_sprt.addChild(this._generateLightParticleView());
        let lConfetti24_sprt = lConfettiContainer24_sprt.addChild(new Sprite());
        lConfetti24_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[3]];
        //...24

        l_catl.addAnimation(
            lConfettiContainer24_sprt,
            MTimeLine.SET_X,
            -320,
            [
                92,
                [270, 56],
                [300, 11]
            ]);

        l_catl.addAnimation(
            lConfettiContainer24_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                92,
                [265.5, 67]
            ]);

        l_catl.addAnimation(
            lConfetti24_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            -127.8,
            [
                98,
                [116.4, 40],
                [85.1, 18]
            ]);

        l_catl.addAnimation(
            lConfetti24_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                98, 
                [-1, 32],
                [1, 32]
            ]);

         //25...
        let lConfettiContainer25_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle25_sprt = lConfettiContainer25_sprt.addChild(this._generateLightParticleView());
        let lConfetti25_sprt = lConfettiContainer25_sprt.addChild(new Sprite());
        lConfetti25_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[1]];
        //...25

        l_catl.addAnimation(
            lConfettiContainer25_sprt,
            MTimeLine.SET_X,
            -340,
            [
                92,
                [-320, 7],
                [-350, 24],
                [-340, 6]
            ]);

        l_catl.addAnimation(
            lConfettiContainer25_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                92,
                [265.5, 37]
            ]);

        l_catl.addAnimation(
            lConfetti25_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                93,
                [176.7, 29]
            ]);

        l_catl.addAnimation(
            lConfetti25_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                93,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle25_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                93,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);


         //26...
        let lConfettiContainer26_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle26_sprt = lConfettiContainer26_sprt.addChild(this._generateLightParticleView());
        let lConfetti26_sprt = lConfettiContainer26_sprt.addChild(new Sprite());
        lConfetti26_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];        
        let lConfetti26Flash_sprt = lConfettiContainer26_sprt.addChild(APP.library.getSprite("game/battleground/star_glow"));
        //...26

        l_catl.addAnimation(
            lConfettiContainer26_sprt,
            MTimeLine.SET_X,
            322,
            [
                98,
                [330, 10],
                [310, 30],
                [325, 16]
            ]);

        l_catl.addAnimation(
            lConfettiContainer26_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                98,
                [265.5, 57]
            ]);

        l_catl.addAnimation(
            lConfetti26_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            88.3,
            [
                99,
                [92.3, 2],
                [-100, 15],
                [100, 17],
                [-100, 17],
            ]);

        l_catl.addAnimation(
            lConfetti26_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                99,
                [-1, 17],
                [1, 17]
                [-1, 17]
            ]);

        l_catl.addAnimation(
            lConfetti26Flash_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                107,
                [1, 1],
                [0, 2]
            ]);

        l_catl.addAnimation(
            lConfetti26Flash_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                107,
                [28, 3]
            ]);
        
        l_catl.addAnimation(
            lLightParticle26_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                102,
                [1, 2],
                [0, 3],
                5,
                [1, 2],
                [0, 3],
                13,
                [1, 2],
                [0, 3],
                10,
                [1, 2],
                [0, 3]
            ]); // +3 

        //27...
        let lConfettiContainer27_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle27_sprt = lConfettiContainer27_sprt.addChild(this._generateLightParticleView());
        let lConfetti27_sprt = lConfettiContainer27_sprt.addChild(new Sprite());
        lConfetti27_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[1]];
        //...27

        l_catl.addAnimation(
            lConfettiContainer27_sprt,
            MTimeLine.SET_X,
            179,
            [
                101,
                [211, 16],
                [221, 22],
                [299, 12]
            ]);

        l_catl.addAnimation(
            lConfettiContainer27_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                101,
                [265.5, 51]
            ]);

        l_catl.addAnimation(
            lConfetti27_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            0,
            [
                102,
                [176.7, 29]
            ]);

        l_catl.addAnimation(
            lConfetti27_sprt,
            MTimeLine.SET_SCALE_Y,
            1, 
            [
                102,
                [-1, 19],
                [1, 19] 
            ]);

        l_catl.addAnimation(
            lLightParticle27_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                102,
                [1, 2],
                [0, 3],
                16,
                [1, 2],
                [0, 3],
            ]);

        //28...
        let lConfettiContainer28_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle28_sprt = lConfettiContainer28_sprt.addChild(this._generateLightParticleView());
        let lConfetti28_sprt = lConfettiContainer28_sprt.addChild(new Sprite());
        lConfetti28_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[3]];
        //...28

        l_catl.addAnimation(
            lConfettiContainer28_sprt,
            MTimeLine.SET_X,
            -342,
            [
                101,
                [-77, 60],
                [-87, 16]
            ]);

        l_catl.addAnimation(
            lConfettiContainer28_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                101,
                [265.5, 67]
            ]);

        l_catl.addAnimation(
            lConfetti28_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            -127.8,
            [
                107,
                [116.4, 40],
                [85.1, 18]
            ]);

        l_catl.addAnimation(
            lConfetti28_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                107, 
                [-1, 32],
                [1, 32]
            ]);

        //29...
        let lConfettiContainer29_sprt = lConfettiFlyContainer_sprt.addChild(new Sprite());
        let lLightParticle29_sprt = lConfettiContainer29_sprt.addChild(this._generateLightParticleView());
        let lConfetti29_sprt = lConfettiContainer29_sprt.addChild(new Sprite());
        lConfetti29_sprt.textures = [BattlegroundConfettiAnimation.getConfettiTextures()[0]];
        //...29

        l_catl.addAnimation(
            lConfettiContainer29_sprt,
            MTimeLine.SET_X,
            -328,
            [
                103,
                [-359, 30],
                [-333, 20]
            ]);

        l_catl.addAnimation(
            lConfettiContainer29_sprt,
            MTimeLine.SET_Y,
            -265.5,
            [
                103,
                [265.5, 51]
            ]);

        l_catl.addAnimation(
            lConfetti29_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            88.3,
            [
                105,
                [92.3, 2],
                [-100, 15],
                [100, 17],
                [-100, 17],
            ]);

        l_catl.addAnimation(
            lConfetti29_sprt,
            MTimeLine.SET_SCALE_Y,
            1,
            [
                105,
                [-1, 17],
                [1, 17]
                [-1, 17]
            ]);
        
        l_catl.addAnimation(
            lLightParticle29_sprt,
            MTimeLine.SET_ALPHA,
            0,
            [
                107,
                [1, 2],
                [0, 3],
                5,
                [1, 2],
                [0, 3],
                13,
                [1, 2],
                [0, 3],
                10,
                [1, 2],
                [0, 3]
            ]);

        this._fConfettiAnimation = l_catl;

        this._fConfettiAnimationDuration_num = this._fConfettiAnimation.getTotalDurationInMilliseconds();

        this.visible = false;
    }

    _generateLightParticleView()
	{
		let l_sprt = APP.library.getSprite("game/battleground/light_particle");
        l_sprt.scale.set(0.73);

        return l_sprt;
	}

   adjust(aStartTime_num)
    {
        let l_gpi = APP.gameController.gameplayController.info;
        let lCurGameplayTime_num = l_gpi.gameplayTime;

        if (lCurGameplayTime_num >= aStartTime_num)
        {
            this.visible = true;

            if (!this._fConfettiAnimation.isPlaying() && !this._fConfettiAnimation.isCompleted())
            {
                this._fConfettiAnimation.playFromMillisecond(lCurGameplayTime_num - aStartTime_num);
            }
        }
        else
        {
            this.drop();
        }
    }

    getTotalDuration()
    {
        return this._fConfettiAnimationDuration_num;
    }

    drop()
    {
    	this._fConfettiAnimation.reset();

		this.visible = false;
    }

    get isDropped()
	{
		return !this.visible;
	}

    destroy()
    {
    	this._fConfettiAnimation && this._fConfettiAnimation.stop();
        this._fConfettiAnimation = null;
        this._fConfettiAnimationDuration_num = undefined;

        this._fConfettiFlContainer_sprt = null;

        super.destroy();
    }
}
export default BattlegroundConfettiAnimation;

BattlegroundConfettiAnimation.getConfettiTextures = function()
{
	if (!BattlegroundConfettiAnimation.confetti_textures)
	{
		BattlegroundConfettiAnimation.confetti_textures = [];

		BattlegroundConfettiAnimation.confetti_textures = AtlasSprite.getFrames([APP.library.getAsset('game/battleground/confetti')], [AtlasConfig.Confetti], 'confetti');
		BattlegroundConfettiAnimation.confetti_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return BattlegroundConfettiAnimation.confetti_textures;
}