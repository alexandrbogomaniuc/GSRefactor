import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';

class BigWinsView extends SimpleUIView {

	addToContainerIfRequired(aBigWinContainerInfo_obj)
	{
		this._addToContainerIfRequired(aBigWinContainerInfo_obj);
	}

	updateZIndex()
	{
		if (this._fBigWinsZindex_num === undefined)
		{
			return;
		}

		this.zIndex = this._fBigWinsZindex_num;
	}

	constructor()
	{
		super();
	}

	_addToContainerIfRequired(aBigWinContainerInfo_obj)
	{
		if (this.parent)
		{
			return;
		}

		this._fBigWinsZindex_num = aBigWinContainerInfo_obj.zIndex;

		aBigWinContainerInfo_obj.container.addChild(this);
		this.zIndex = aBigWinContainerInfo_obj.zIndex;
	}
}

export default BigWinsView;