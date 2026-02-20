import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import SimpleUIView from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';

class EnemyIndicatorsView extends SimpleUIView
{
	setCurrentEnergy(aEnergy_num)
	{

	}

	updateHp(aDemage_num)
	{
		this._updateHp(aDemage_num);
	}

	constructor()
	{
		super();

		this._fEnemyIndicatorsContainer_spr = this.addChild(new Sprite());
		this._fIsDisabled_bl = null;
	}

	__init()
	{
		super.__init();

		this._initView();
	}

	_initView()
	{
		//DEBUG show enemy ID...
		let lStyle_obj = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 11.6,
			fill: 0Xff0000
		};
		let tf = this._id_tf = new TextField(lStyle_obj);
		tf.text = this.uiInfo.id;
		this.addChild(tf);
		tf.zIndex = 100;
		tf.x = - 20;
		tf.y = 0;
		tf.visible = !!window.SEIDs;
		//...DEBUG
	}

	//debug...
	toggleIdsIndicator()
	{
		this._id_tf.visible = !!window.SEIDs;
	}
	//...debug


	destroy()
	{
		super.destroy();
		
		this._fEnemyIndicatorsContainer_spr && this._fEnemyIndicatorsContainer_spr.destroy();
		this._fEnemyIndicatorsContainer_spr = null;

		this._fIsDisabled_bl = null;
	}
}

export default EnemyIndicatorsView;
