import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import CloudsPool from './CloudsPool';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const CLOUD_POOLS_DESCRIPTORS =
[
	{
		x: 0.5,
		y: 1.1,
		angle: 0,
		scale: 1.25,
		alpha:  0.5,
		delayInFrames: 30
	},
	{
		x: 0.5,
		y: 0.25,
		angle: 0,
		scale: 1,
		alpha: 0.25,
		delayInFrames: 60
	},
	{
		x: 0,
		y: 0.5,
		angle: Math.PI/2,
		scale: 2,
		alpha: 1,
		delayInFrames: 0
	},
	{
		x: 1,
		y: 0.5,
		angle: -Math.PI/2,
		scale: 2,
		alpha: 1,
		delayInFrames: 30
	}
]

const CLOUD_POOLS_COUNT = CLOUD_POOLS_DESCRIPTORS.length;

class TransitionView extends SimpleUIView
{
	static get TRANSITION_VIEW_STATE_ID_IVALID() { return -1; }
	static get TRANSITION_VIEW_STATE_ID_INTRO() { return 0; }
	static get TRANSITION_VIEW_STATE_ID_THICKEN() { return 1; }
	static get TRANSITION_VIEW_STATE_ID_LOOP() 	{ return 2; }
	static get TRANSITION_VIEW_STATE_ID_OUTRO() { return 3; }

	static get EVENT_ON_TRANSITION_INTRO_COMPLETED() { return "EVENT_ON_TRANSITION_INTRO_COMPLETED"; }

	initOnScreen(containerInfo)
	{
		containerInfo.container.addChild(this);

		this.zIndex = containerInfo.zIndex;
		this.position.set(-960/2, -540/2);
	}

	constructor()
	{
		super();

		this._fStateId_int = undefined;
		this._fFramesCount_num = 0;
		this._fIsLoop_bl = false;
		this._fCloudsPools_cp_arr = [];
		this._fIsNeedEventOnTransitionIntroCompleted_bl = null;

		let lOverlay_g = new PIXI.Graphics();
		lOverlay_g.beginFill(0x888888).drawRect(0, 0, 960, 540).endFill();
		lOverlay_g.alpha = 0;
		this._fOverlay_g = this.addChild(lOverlay_g);

		for( let i = 0; i < CLOUD_POOLS_COUNT; i++ )
		{
			let lDescriptor_obj = CLOUD_POOLS_DESCRIPTORS[i];
			let lCloudsPool_cp = new CloudsPool();

			lCloudsPool_cp.position.set(
				lDescriptor_obj.x * 960,
				lDescriptor_obj.y * 540);

			lCloudsPool_cp.scale.x = lDescriptor_obj.scale;
			lCloudsPool_cp.scale.y = lDescriptor_obj.scale;

			lCloudsPool_cp.alpha = lDescriptor_obj.alpha;

			lCloudsPool_cp.rotation = lDescriptor_obj.angle;

			this._fCloudsPools_cp_arr[i] = this.addChild(lCloudsPool_cp); 
		}

		/*
		//DEBUG...
		let l_html = window.document.createElement('div');

		l_html.id = "ID1";
		l_html.style.color = "white";
		l_html.style.position = "absolute";
		l_html.style.left = "0px";
		l_html.style.top = "0px";
		l_html.style.margin = "10px";
		l_html.style.width = "100px";
		l_html.style.height = "100px";
		l_html.style["background-color"] = "#7e5ad1";
		l_html.style["z-index"] = "999";

		l_html.style["text-align"] = "center";
		l_html.style["vertical-align"] = "middle";
		l_html.style["line-height"] = "100px";
		l_html.style["font-family"] = "calibri";
		l_html.style["font-weight"] = "bold";
		l_html.style["font-size"] = "80px";
		l_html.style["border-radius"] = "20px";
		l_html.style["cursor"] = "pointer";
		l_html.style["opacity"] = "1";

		l_html.innerText = "I";

		l_html.addEventListener("click", this.setStateId.bind(this, TransitionView.TRANSITION_VIEW_STATE_ID_INTRO));

		document.body.appendChild(l_html);

		l_html = window.document.createElement('div');
		l_html.id = "ID2";
		l_html.style.color = "white";
		l_html.style.position = "absolute";
		l_html.style.left = "110px";
		l_html.style.top = "0px";
		l_html.style.margin = "10px";
		l_html.style.width = "100px";
		l_html.style.height = "100px";
		l_html.style["background-color"] = "#7e5ad1";
		l_html.style["z-index"] = "999";

		l_html.style["text-align"] = "center";
		l_html.style["vertical-align"] = "middle";
		l_html.style["line-height"] = "100px";
		l_html.style["font-family"] = "calibri";
		l_html.style["font-weight"] = "bold";
		l_html.style["font-size"] = "80px";
		l_html.style["border-radius"] = "20px";
		l_html.style["cursor"] = "pointer";
		l_html.style["opacity"] = "1";

		l_html.innerText = "T";

		l_html.addEventListener("click", this.setStateId.bind(this, TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN));

		document.body.appendChild(l_html);

		l_html = window.document.createElement('div');
		l_html.id = "ID3";
		l_html.style.color = "white";
		l_html.style.position = "absolute";
		l_html.style.left = "220px";
		l_html.style.top = "0px";
		l_html.style.margin = "10px";
		l_html.style.width = "100px";
		l_html.style.height = "100px";
		l_html.style["background-color"] = "#7e5ad1";
		l_html.style["z-index"] = "999";

		l_html.style["text-align"] = "center";
		l_html.style["vertical-align"] = "middle";
		l_html.style["line-height"] = "100px";
		l_html.style["font-family"] = "calibri";
		l_html.style["font-weight"] = "bold";
		l_html.style["font-size"] = "80px";
		l_html.style["border-radius"] = "20px";
		l_html.style["cursor"] = "pointer";
		l_html.style["opacity"] = "1";

		l_html.innerText = "L";
		l_html.addEventListener("click", this.setStateId.bind(this, TransitionView.TRANSITION_VIEW_STATE_ID_LOOP));
		document.body.appendChild(l_html);


		l_html = window.document.createElement('div');
		l_html.id = "ID4";
		l_html.style.color = "white";
		l_html.style.position = "absolute";
		l_html.style.left = "330px";
		l_html.style.top = "0px";
		l_html.style.margin = "10px";
		l_html.style.width = "100px";
		l_html.style.height = "100px";
		l_html.style["background-color"] = "#7e5ad1";
		l_html.style["z-index"] = "999";

		l_html.style["text-align"] = "center";
		l_html.style["vertical-align"] = "middle";
		l_html.style["line-height"] = "100px";
		l_html.style["font-family"] = "calibri";
		l_html.style["font-weight"] = "bold";
		l_html.style["font-size"] = "80px";
		l_html.style["border-radius"] = "20px";
		l_html.style["cursor"] = "pointer";
		l_html.style["opacity"] = "1";

		l_html.innerText = "O";
		l_html.addEventListener("click", this.setStateId.bind(this, TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO));
		document.body.appendChild(l_html);
		//...DEBUG
		*/
		
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		this.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_IVALID);
		this.drop();
	}

	get stateId()
	{
		return this._fStateId_int;
	}

	setStateId(aStateId_int)
	{
		if(this._fStateId_int === aStateId_int)
		{
			return;
		}

		if(this.uiInfo && this.uiInfo.blockChangeState)
		{
			return;
		}

		if(
			this._fStateId_int === TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN &&
			aStateId_int === TransitionView.TRANSITION_VIEW_STATE_ID_LOOP
			)
		{
			return;
		}

		if(
			this._fStateId_int === TransitionView.TRANSITION_VIEW_STATE_ID_IVALID &&
			aStateId_int === TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO
			)
		{
			return;
		}

		if(
			aStateId_int === TransitionView.TRANSITION_VIEW_STATE_ID_IVALID &&
			!APP.currentWindow.gameStateController.info.gameState
			)
		{
			this.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO);
			return;
		}

		this._fStateId_int = aStateId_int;
		
		switch(aStateId_int)
		{
			case TransitionView.TRANSITION_VIEW_STATE_ID_IVALID:
			{
				this._fIsNeedEventOnTransitionIntroCompleted_bl = false;
				this.drop();
			}
			break;
			case TransitionView.TRANSITION_VIEW_STATE_ID_INTRO:
			{
				this._fIsNeedEventOnTransitionIntroCompleted_bl = true;
				this._fOverlay_g.alpha = 0;
				this.drop();
				this._setLoopMode(true);
				this.visible = true;
			}
			break;
			case TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN:
			{
				this._setLoopMode(true);
				this.visible = true;
			}
			break;
			case TransitionView.TRANSITION_VIEW_STATE_ID_LOOP:
			{
				this.drop();
				this._setLoopMode(true, true);
				this._fOverlay_g.alpha = 1;
				for( let i = 0; i < 60; i++ )
				{
					this.tick();
				}
				this.visible = true;
			}
			break;
			case TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO:
			{
				this._fFramesCount_num = 0;
				this._setLoopMode(false);
				this.visible = true;
			}
			break;
		}
	}

	_setLoopMode(aIsLoop_bl, aOptIsInstant_bl = false)
	{
		this._fIsLoop_bl = aIsLoop_bl;

		for( let i = 0; i < CLOUD_POOLS_COUNT; i++ )
		{
			this._fCloudsPools_cp_arr[i].setLoopMode(aIsLoop_bl, aOptIsInstant_bl);
		}
	}


	tick(delta = 17)
	{
		if(this._fStateId_int === TransitionView.TRANSITION_VIEW_STATE_ID_IVALID)
		{
			return;
		}

		let lTimeMultiplier_num = delta / 17 * 0.6;

		this._fFramesCount_num+= lTimeMultiplier_num;

		for( let i = 0; i < CLOUD_POOLS_COUNT; i++ )
		{
			if(this._fFramesCount_num > CLOUD_POOLS_DESCRIPTORS[i].delayInFrames)
			{
				this._fCloudsPools_cp_arr[i].update(lTimeMultiplier_num);
			}
		}

		switch(this._fStateId_int)
		{
			case TransitionView.TRANSITION_VIEW_STATE_ID_INTRO:
			{
				if(
					this._fOverlay_g.alpha < 0.6 &&
					this._fFramesCount_num > 10
					)
				{
					this._fOverlay_g.alpha += 0.035 * lTimeMultiplier_num;
				}

				if ((this._fOverlay_g.alpha) >= 0.6 && this._fFramesCount_num > 30 && this._fIsNeedEventOnTransitionIntroCompleted_bl)
				{
					this._fIsNeedEventOnTransitionIntroCompleted_bl = false;
					this.emit(TransitionView.EVENT_ON_TRANSITION_INTRO_COMPLETED);
				}
			}
			break;
			case TransitionView.TRANSITION_VIEW_STATE_ID_THICKEN:
			{
				if(
					this._fOverlay_g.alpha < 1 &&
					this._fFramesCount_num > 10
					)
				{
					this._fOverlay_g.alpha += 0.025 * lTimeMultiplier_num;
				}
			}
			break;
			case TransitionView.TRANSITION_VIEW_STATE_ID_LOOP:
			{
				this._fOverlay_g.alpha = 1;
			}
			break;
			case TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO:
			{
				if(this._fOverlay_g.alpha > 0)
				this._fOverlay_g.alpha -= 0.01 * lTimeMultiplier_num;

				if(!this._isVisible())
				{
					this._fStateId_int = TransitionView.TRANSITION_VIEW_STATE_ID_IVALID;
					this.drop();
				}
			}
			break;
		}
	}

	_isVisible()
	{
		if(this._fOverlay_g.alpha > 0)
		{
			return true;
		}

		for( let i = 0; i < CLOUD_POOLS_COUNT; i++ )
		{
			if(this._fCloudsPools_cp_arr[i].isVisible())
			{
				return true;
			}
		}

		return false;
	}

	drop()
	{
		this._fOverlay_g.alpha = 0;
		this._fFramesCount_num = 0;

		for( let i = 0; i < CLOUD_POOLS_COUNT; i++ )
		{
			this._fCloudsPools_cp_arr[i].drop();
		}

		this.visible = false;
	}

	destroy()
	{
		this._fOverlay_g.destroy();

		for( let i = 0; i < CLOUD_POOLS_COUNT; i++ )
		{
			this._fCloudsPools_cp_arr[i].destroy();
		}

		this._fCloudsPools_cp_arr = null;
		this._fIsNeedEventOnTransitionIntroCompleted_bl = null;

		super.destroy();
	}
	
	/*
	//DEBUG...
	keyDownHandler(e)
	{
		switch(e.keyCode)
		{
			case 49:
				this.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_INTRO);
				break;
			case 50:
				this.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_LOOP);
				break;
			case 51:
				this.setStateId(TransitionView.TRANSITION_VIEW_STATE_ID_OUTRO);
				break;
		}
	}
	//...DEBUG
	*/
}

export default TransitionView;