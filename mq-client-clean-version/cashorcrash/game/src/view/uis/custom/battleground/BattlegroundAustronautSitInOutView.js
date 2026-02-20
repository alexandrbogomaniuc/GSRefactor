import MTimeLine from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../config/AtlasConfig';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';

const TRAMPOLINE_JUMP_FRAME_INDEX = 9;
const ASTRONAUT_PRE_LANDING_FRAME_INDEX = 30;
const ASTRONAUT_SIT_IN_COMPLETION_FRAME_INDEX = 34;
const ASTRONAUT_SIT_OUT_STARTED_FRAME_INDEX = 0;

class BattlegroundAustronautSitInOutView extends Sprite
{	
	static get EVENT_ON_ASTRONAUTS_ANIMATION_PRE_LANDING()			{ return "EVENT_ON_ASTRONAUTS_ANIMATION_PRE_LANDING"};
	static get EVENT_ON_ASTRONAUTS_ANIMATION_SITIN_COMPLETION()		{ return "EVENT_ON_ASTRONAUTS_ANIMATION_SITIN_COMPLETION"};
	static get EVENT_ON_ASTRONAUT_TRAMPOLINE ()						{ return "EVENT_ON_ASTRONAUT_TRAMPOLINE"; }
	static get EVENT_ON_ASTRONAUTS_ANIMATION_SITOUT_STARTED ()		{ return "EVENT_ON_ASTRONAUTS_ANIMATION_SITOUT_STARTED"; }

    constructor()
	{
		super();

		this._fTimelineIn_mtl = null;
        this._fTimelineOut_mtl = null;
        this._fActiveTimeline_mtl = null;
        this._fCurAdjustMilliseconds_num = undefined;
        this._fIsTrampolineJumpAlreadyOccurred_bl = false;
        this._fIsSitInPreLandingAlreadyOccurred_bl = false;
        this._fIsSitInCompletionAlreadyOccurred_bl = false;
        this._fIsSitOutStartedAlreadyOccurred_bl = false;
        this._fSeatId_str = undefined;
		this._fBodyRotationAnim_ma = null;
		this.isCafForced = false;

		this._fMainContainer_sprt = this.addChild(new Sprite);
		this._fMainContainer_sprt.x = 635;
		this._fMainContainer_sprt.y = 190;

		this._fAstronautContainer_sprt = this._fMainContainer_sprt.addChild(new Sprite);
		this._fAstronautContainer_sprt.alpha = 0;

		this._fBody_sprt = this._fAstronautContainer_sprt.addChild(new Sprite);
		this._fBody_sprt.textures = [BattlegroundAustronautSitInOutView.getUnitTextures()[0]];
		this._fBody_sprt.scale.set(-1, 1);

		let lNickname_ta = this._fNicknameTemplate_ta = I18.generateNewCTranslatableAsset("TABattlegroundAstronautsSitinNickname");
		let lNicknameFormat_obj = Object.assign({}, lNickname_ta.textFormat, {shortLength: 90});
		let lNickname_tf = this._fAstronautContainer_sprt.addChild(new TextField(lNicknameFormat_obj));

		this._fNickname_tf = lNickname_tf;
		lNickname_tf.position.set(0, -25);
		lNickname_tf.anchor.set(0.5, 0.5);

		this._fTopLightParticle_spr = this._fAstronautContainer_sprt.addChild(APP.library.getSprite("game/battleground/light_particle"));

		let lOpticalFlare_spr = this._fOpticalFlare_spr = this.addChild(APP.library.getSprite("game/battleground/optical_flare_blue"));
		lOpticalFlare_spr.scale.set(2.09, 2.023);
		lOpticalFlare_spr.anchor.set(0,0);
		lOpticalFlare_spr.position.set(0,0);
		lOpticalFlare_spr.blendMode = PIXI.BLEND_MODES.ADD;

		this._addAnimationIn();
        this._addAnimationOut();

		this.visible = false;
	}

	get sitInAnimationDuration()
	{
		return this._fTimelineIn_mtl.getTotalDurationInMilliseconds();
	}

	get sitOutAnimationDuration()
	{
		return this._fTimelineOut_mtl.getTotalDurationInMilliseconds();
	}

	_addAnimationIn()
	{
		let l_mtl = new MTimeLine();

		l_mtl.addAnimation(
			this._astronautJumpIn,
			MTimeLine.EXECUTE_METHOD,
			0,
			[
				[34, 34]
			],
			this
		);

		l_mtl.addAnimation(
			this._fAstronautContainer_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				[1.42, 15]
			],
			this);

		
		l_mtl.addAnimation(
			this._fBody_sprt,
			MTimeLine.SET_ALPHA,
			1,
			[
				34,
				[0, 1]
			],
			this);

		l_mtl.addAnimation(
			this._fNickname_tf,
			MTimeLine.SET_ALPHA,
			1,
			[
				34,
				[0, 1]
			],
			this);

		l_mtl.addAnimation(
			this._fTopLightParticle_spr,
			MTimeLine.SET_SCALE,
			2.76,
			[
				[1.58, 25]
			],
			this);

		l_mtl.addAnimation(
			this._fTopLightParticle_spr,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0, 25]
			],
			this);

		l_mtl.addAnimation(
			this._fOpticalFlare_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				[0.51, 3],
				[0, 32],
			],
			this);

		l_mtl.callFunctionAtFrame(this._onTrampolineJump, TRAMPOLINE_JUMP_FRAME_INDEX, this);
		l_mtl.callFunctionAtFrame(this._onAstronautPreLanding, ASTRONAUT_PRE_LANDING_FRAME_INDEX, this);
		l_mtl.callFunctionAtFrame(this._onSitInAnimationCompletion, ASTRONAUT_SIT_IN_COMPLETION_FRAME_INDEX, this);
		l_mtl.callFunctionOnFinish(this._onAnimationInCompleted, this);

		this._fTimelineIn_mtl = l_mtl;
	}

    _addAnimationOut()
    {
		console.log("add animation out")
        let l_mtl = new MTimeLine();

        l_mtl.addAnimation(
            this._astronautJumpOut,
            MTimeLine.EXECUTE_METHOD,
            0,
            [
                [27, 27]
            ],
            this
        );

        l_mtl.addAnimation(
            this._fAstronautContainer_sprt,
            MTimeLine.SET_SCALE,
            0,
            [
                [1.42, 8]
            ],
            this);

        l_mtl.addAnimation(
            this._fBody_sprt,
            MTimeLine.SET_ROTATION_IN_DEGREES,
            -28.6,
            [
                [360, 27]
            ],
            this);

        l_mtl.addAnimation(
            this._fBody_sprt,
            MTimeLine.SET_ALPHA,
            1,
            [
                27,
                [0, 1]
            ],
            this);

        l_mtl.addAnimation(
            this._fNickname_tf,
            MTimeLine.SET_ALPHA,
            1,
            [
                27,
                [0, 1]
            ],
            this);

        l_mtl.callFunctionAtFrame(this._onAstronautOutStarted, ASTRONAUT_SIT_OUT_STARTED_FRAME_INDEX, this);
        l_mtl.callFunctionOnFinish(this._onAnimationOutCompleted, this);

        this._fTimelineOut_mtl = l_mtl;
    }

	drop()
	{
		if(this.isCafForced){
			return;
		}
		
		this.visible = false;

		this._fTimelineIn_mtl.stop();
		this._fTimelineIn_mtl.windToMillisecond(0);

      	this._fTimelineOut_mtl.stop();
        this._fTimelineOut_mtl.windToMillisecond(0);

        this._fActiveTimeline_mtl = null;
        this._fCurAdjustMilliseconds_num = undefined;
        
        this._fIsTrampolineJumpAlreadyOccurred_bl = false;
        this._fIsSitInPreLandingAlreadyOccurred_bl = false;
        this._fIsSitInCompletionAlreadyOccurred_bl = false;
        this._fIsSitOutStartedAlreadyOccurred_bl = false;

        this._fSeatId_str = undefined;

		let l_ma = this._fBodyRotationAnim_ma;
		if (l_ma)
		{
			this._fTimelineIn_mtl.removeAnimation(l_ma);
		}
		this._fBodyRotationAnim_ma = null;
		this._fBody_sprt.rotation = 0;
	}

	get isDropped()
	{
		return !this.visible;
	}

	get isSitInAnimationInProgress()
	{
		return this._fActiveTimeline_mtl === this._fTimelineIn_mtl;
	}

	get isSitOutAnimationInProgress()
	{
		return this._fActiveTimeline_mtl === this._fTimelineOut_mtl;
	}

	get seatId()
	{
		return this._fSeatId_str;
	}

	updateArea()
	{
        let lIsPortraitOrientation_bl = APP.layout.isPortraitOrientation;

		this._fMainContainer_sprt.x = lIsPortraitOrientation_bl ? 376 : 635;
        this._fOpticalFlare_spr.x = lIsPortraitOrientation_bl ? -300 : 0;
	}

	adjustSitInView(aSeatId_str, aMasterBet_bl, aDurationInMs_num)
	{
		this._fSeatId_str = aSeatId_str;
		this._configureView(true, aSeatId_str, aMasterBet_bl);
		this._fActiveTimeline_mtl = this._fTimelineIn_mtl;

		this._fCurAdjustMilliseconds_num = aDurationInMs_num;
		this._fTimelineIn_mtl.windToMillisecond(aDurationInMs_num);

		this.visible = true;
	}

    adjustSitOutView(aSeatId_str, isMaster, aDurationInMs_num )
    {
		
    	this._fSeatId_str = aSeatId_str;
        this._configureView(false, aSeatId_str,isMaster);
        this._fActiveTimeline_mtl = this._fTimelineOut_mtl;

		this._fCurAdjustMilliseconds_num = aDurationInMs_num;
		this._fTimelineOut_mtl.windToMillisecond(aDurationInMs_num);

        this.visible = true;
    }

	_configureView(aIsInAnimation_bl, aSeatId_str, aMasterBet_bl=false)
	{

		
		if(aMasterBet_bl == true){
			this._player = true;
		}else{
			this._player = false;
		}

		this._fNickname_tf.text = this._fNicknameTemplate_ta.text.replace("/VALUE/", aSeatId_str);

		this._fOpticalFlare_spr.visible = !!aIsInAnimation_bl && !!aMasterBet_bl;

        this._fTopLightParticle_spr.visible = !!aIsInAnimation_bl;

		this._fAstronautContainer_sprt.alpha = 1;
		
		let l_mtl = this._fTimelineIn_mtl;

		if (!this._fBodyRotationAnim_ma)
		{
			if (aMasterBet_bl)
			{
				this._fBodyRotationAnim_ma = l_mtl.addAnimation(
					this._fBody_sprt,
					MTimeLine.SET_ROTATION_IN_DEGREES,
					-28.6,
					[
						[-19, 12],
						[-360, 21]
					],
					this);
			}
			else
			{
				this._fBodyRotationAnim_ma = l_mtl.addAnimation(
					this._fBody_sprt,
					MTimeLine.SET_ROTATION_IN_DEGREES,
					-28.6,
					[
						[-2.1, 33]
					],
					this);
			}
		}

		this.updateArea();
	}

	_astronautJumpIn(aFrame_num)
	{
		if(this._player){
			this._fBody_sprt.scale.set(-1.2, 1.2);
		}else{
			this._fBody_sprt.scale.set(-1, 1);
		}
		let x = this._fAstronautContainer_sprt.position.x;
		let y = this._fAstronautContainer_sprt.position.y;
		if (aFrame_num < 9)
		{
			x = 50 - aFrame_num * 8.75;
			y = (6/245)*(x-50)*(x-50) - 120;
		}
		else if (aFrame_num >= 9 && aFrame_num < 12)
		{
			x = -20;
			y += 2;
		}
		else if (aFrame_num >= 12 && aFrame_num < 20)
		{
			x = -20 - (aFrame_num - 12) * 12.5;
			y = 0.01*(x+120)*(x+120) - 100;
		}
		else if (aFrame_num >= 20)
		{
			x = -120 - (aFrame_num - 20) * 8;
			y = (5/288)*(x+120)*(x+120) - 100;
		}

		this._fAstronautContainer_sprt.position.set(x, y);
	}

    _astronautJumpOut(aFrame_num)
    {
		if(this._player){
			this._fBody_sprt.scale.set(-1.2, 1.2);
		}else{
			this._fBody_sprt.scale.set(-1, 1);
		}
        let x = this._fAstronautContainer_sprt.position.x;
        let y = this._fAstronautContainer_sprt.position.y;
        if (aFrame_num < 9)
        {
            x = -240 + aFrame_num * 15;
            y = (1/144)*(x+120)*(x+120) + 100;
        }
        else if (aFrame_num >= 9 && aFrame_num < 27)
        {
            x = -120 + (aFrame_num - 9) * (20/3);
            y = (3/144)*(x+120)*(x+120) + 100;
        }
        this._fAstronautContainer_sprt.position.set(x, y);
    }

	_onTrampolineJump()
	{
		let lCurAdlustFrameIndex_int = this._fTimelineIn_mtl.convertMillisecondToFrameIndex(this._fCurAdjustMilliseconds_num);
		if (this._fIsTrampolineJumpAlreadyOccurred_bl || lCurAdlustFrameIndex_int > TRAMPOLINE_JUMP_FRAME_INDEX+5)
		{
			return;
		}

		this._fIsTrampolineJumpAlreadyOccurred_bl = true;
		this.emit(BattlegroundAustronautSitInOutView.EVENT_ON_ASTRONAUT_TRAMPOLINE);
	}

	_onAstronautPreLanding()
	{
		let lCurAdlustFrameIndex_int = this._fTimelineIn_mtl.convertMillisecondToFrameIndex(this._fCurAdjustMilliseconds_num);
		if (this._fIsSitInPreLandingAlreadyOccurred_bl || lCurAdlustFrameIndex_int > ASTRONAUT_PRE_LANDING_FRAME_INDEX+2)
		{
			return;
		}
		
		this._fIsSitInPreLandingAlreadyOccurred_bl = true;
		this.emit(BattlegroundAustronautSitInOutView.EVENT_ON_ASTRONAUTS_ANIMATION_PRE_LANDING);
	}

	_onSitInAnimationCompletion()
	{
		let lCurAdlustFrameIndex_int = this._fTimelineIn_mtl.convertMillisecondToFrameIndex(this._fCurAdjustMilliseconds_num);
		if (this._fIsSitInCompletionAlreadyOccurred_bl || lCurAdlustFrameIndex_int > ASTRONAUT_SIT_IN_COMPLETION_FRAME_INDEX+2)
		{
			return;
		}
		
		this._fIsSitInCompletionAlreadyOccurred_bl = true;
		this.emit(BattlegroundAustronautSitInOutView.EVENT_ON_ASTRONAUTS_ANIMATION_SITIN_COMPLETION);
	}

	_onAnimationInCompleted()
	{		
		this.drop();
	}

	_onAstronautOutStarted()
	{
		let lCurAdlustFrameIndex_int = this._fTimelineOut_mtl.convertMillisecondToFrameIndex(this._fCurAdjustMilliseconds_num);
		if (this._fIsSitOutStartedAlreadyOccurred_bl || lCurAdlustFrameIndex_int > ASTRONAUT_SIT_OUT_STARTED_FRAME_INDEX+2)
		{
			return;
		}
		this._fIsSitOutStartedAlreadyOccurred_bl = true;
		this.emit(BattlegroundAustronautSitInOutView.EVENT_ON_ASTRONAUTS_ANIMATION_SITOUT_STARTED);
	}

    _onAnimationOutCompleted()
    {
        this.drop();
    }

	_destroyAnimation()
	{
		
		if (this._fTimelineIn_mtl)
		{
			this._fTimelineIn_mtl.stop();
			this._fTimelineIn_mtl.destroy();
		}
		this._fTimelineIn_mtl = null;

        if (this._fTimelineOut_mtl)
        {
            this._fTimelineOut_mtl.stop();
            this._fTimelineOut_mtl.destroy();
        }
        this._fTimelineOut_mtl = null;
		
        this._fActiveTimeline_mtl = null;
        this._fCurAdjustMilliseconds_num = undefined;
        this._fSeatId_str = undefined;
        
        this._fIsTrampolineJumpAlreadyOccurred_bl = undefined;
        this._fIsSitInPreLandingAlreadyOccurred_bl = undefined;
        this._fIsSitInCompletionAlreadyOccurred_bl = undefined;
        this._fIsSitOutStartedAlreadyOccurred_bl = undefined;

		this._fBodyRotationAnim_ma = null;

		this._fOpticalFlare_spr = null;
		this._fTopLightParticle_spr = null;
		this._fNickname_tf = null;
		this._fNicknameTemplate_ta = null;
		this._fBody_sprt = null;
		this._fAstronautContainer_sprt = null;
		this._fMainContainer_sprt = null;
		this._player = false;

	}

	destroy()
	{
		this._destroyAnimation();
		super.destroy();
	}

}

export default BattlegroundAustronautSitInOutView;

BattlegroundAustronautSitInOutView.getUnitTextures = function()
{
	if (!BattlegroundAustronautSitInOutView.unit_textures)
	{
		BattlegroundAustronautSitInOutView.unit_textures = [];

		BattlegroundAustronautSitInOutView.unit_textures = AtlasSprite.getFrames([APP.library.getAsset('game/gameplay_assets')], [AtlasConfig.GameplayAssets], 'ejectable_unit');
		BattlegroundAustronautSitInOutView.unit_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return BattlegroundAustronautSitInOutView.unit_textures;
}