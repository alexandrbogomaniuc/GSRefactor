import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GameplayInfo from '../../../model/gameplay/GameplayInfo';
import AlignDescriptor from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/align/AlignDescriptor';
import NonWobblingTextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/NonWobblingTextField';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class GameplayStateScreenEndView extends Sprite
{
	constructor()
	{
		super();

		this._fMult_tf = null;

		//CONTENT...
		this._addContent();
		//...CONTENT
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;

		let lMultiplier_num = l_gpi.serverMultiplierValue;

		let l_tf = this._fMult_tf;
		l_tf.text = GameplayInfo.formatMultiplier(lMultiplier_num);
	}

	_addContent()
	{
		let l_gpi = APP.gameController.gameplayController.info;

		let lContainer_sprt = this.addChild(new Sprite);

		let lLabelAsset_ta = I18.generateNewCTranslatableAsset("TARoundOverLabel");
		lContainer_sprt.addChild(lLabelAsset_ta);
		lLabelAsset_ta.position.set(0, 50);

		let l_tf = this._fMult_tf = lContainer_sprt.addChild(new NonWobblingTextField());
		l_tf.fontName = "fnt_nm_barlow_bold";
		l_tf.fontSize = 100;
		l_tf.fontColor = 0xff0000;
		l_tf.setAlign(AlignDescriptor.CENTER, AlignDescriptor.MIDDLE);
		l_tf.letterSpace = -5;
		l_tf.maxWidth = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width;

		if (l_gpi.isPreLaunchFlightRequired)
		{
			lContainer_sprt.position.y = 60;
		}
	}
}

export default GameplayStateScreenEndView;