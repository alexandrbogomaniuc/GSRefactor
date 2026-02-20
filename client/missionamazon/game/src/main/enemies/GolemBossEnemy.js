import BossEnemy, { STATE_CALL } from './BossEnemy';
import { DIRECTION } from './Enemy';

class GolemBossEnemy extends BossEnemy {

	//override
	get turnPostfix()
	{
		return this.isHealthStateWeak ? "_weak_turn" : "_turn";
	}

	//override
	getSpineSpeed()
	{
		let lBaseSpeed_num = this.isHealthStateWeak ? 0.25 : 0.20;
		return this.speed * this.getScaleCoefficient() * lBaseSpeed_num;
	}

	//override
	changeZindex()
	{
		super.changeZindex();
		switch (this.direction)
		{
			case DIRECTION.LEFT_DOWN:
			case DIRECTION.LEFT_UP:
				this.zIndex += 17;
				break;
			case DIRECTION.RIGHT_UP:
				this.zIndex += 25;
				break;
			case DIRECTION.RIGHT_DOWN:
				this.zIndex += 60;
				break;
		}
	}

	//override
	changeShadowPosition()
	{
		let x = 0, y = 0, scale = 1.4, alpha = 1;

		this.shadow.position.set(x, y);
		this.shadow.scale.set(scale);
		this.shadow.alpha = alpha;
	}

	//override
	getScaleCoefficient()
	{
		return 1.5*1.15;
	}

	//override
	getLocalCenterOffset()
	{
		let pos = new PIXI.Point();
		switch (this.direction)
		{
			case DIRECTION.LEFT_DOWN:
				pos.x = -10;
				pos.y = -55;
				break;
			case DIRECTION.LEFT_UP:
				pos.x = 0;
				pos.y = -55;
				break;
			case DIRECTION.RIGHT_DOWN:
				pos.x = 20;
				pos.y = -55;
				break;
			case DIRECTION.RIGHT_UP:
				pos.x = 0;
				pos.y = -55;
				break;
		}
		let scale = this.getScaleCoefficient();
		pos.x *= scale;
		pos.y *= scale;
		return pos;
	}

	//override
	_getHitRectHeight()
	{
		return 120 * this.getScaleCoefficient();
	}

	//override
	_getHitRectWidth()
	{
		return 80 * this.getScaleCoefficient();
	}

	//override
	_calcWalkAnimationName(aDirection_str)
	{
		let lWalkAnimationSuffix_str = this.isHealthStateWeak ? 'weak' : "walk";
		return super._calcWalkAnimationName(aDirection_str, lWalkAnimationSuffix_str);
	}

	//override
	get _customSpineTransitionsDescr()
	{
		return [
			{from: "<PREFIX>midlife", to: "<PREFIX>walk", duration: 0.4},
			{from: "<PREFIX>walk", to: "<PREFIX>midlife", duration: 0.4}
		];
	}

	//override
	_generateBossAppearanceMask()
	{
		let lMask_gr = this.addChild(new PIXI.Graphics());
		let lSpineBounds_rt = this.spineView.view.getLocalBounds();
		let lY_num = lSpineBounds_rt.y * this.spineView.scale.y + this.spineViewPos.y - 15;
		let lHeight_num = lSpineBounds_rt.height * this.spineView.scale.y;
		let lX_num = -480;
		lMask_gr.beginFill(0x00FF00).drawRect(lX_num, lY_num, 960, lHeight_num).endFill();

		let poly = [new PIXI.Point(-112, 	lY_num +  lHeight_num + 13 ),
			new PIXI.Point(-65, 	lY_num +  lHeight_num + 25),
			new PIXI.Point(23, 		lY_num +  lHeight_num),
			new PIXI.Point(37, 		lY_num +  lHeight_num + 13),
			new PIXI.Point(77, 		lY_num +  lHeight_num + 13),
			new PIXI.Point(105, 	lY_num +  lHeight_num + 5),
			new PIXI.Point(127, 	lY_num +  lHeight_num + 6),

			new PIXI.Point(127, 	lY_num + lHeight_num),
			new PIXI.Point(-112, 	lY_num + lHeight_num)];
		lMask_gr.beginFill(0x00ff00).drawPolygon(poly).endFill();

		return lMask_gr;
	}

	//override
	showBossAppearance(aSequence_arr, aInitialParams_obj)
	{
		super.showBossAppearance(aSequence_arr, aInitialParams_obj);

		this._playAppearanceMidlife();
	}

	_playAppearanceMidlife()
	{
		this.changeTextures(STATE_CALL);

		this.stateListener = {complete: () =>{
			this.spineView && this.spineView.stop();
			this.setStay();
		}};


		if (this.spineView && this.spineView.view.state && this.spineView.view.state)
		{
			this.spineView.view.state.timeScale = this.getSpineSpeed() * 0.55; // low speed midlife
			this.spineView.view.state.addListener(this.stateListener);
		}
	}

	_unfreeze(aIsAnimated_bl = true)
	{
		super._unfreeze(aIsAnimated_bl);

		if (!this._fBossAppearanceInProgress_bln && !this._fWeakTransitionInProgress_bl)
		{
			this.setStay(); //to prevent midlife animation when unfreeze right after appearance
		}
	}
}

export default GolemBossEnemy;