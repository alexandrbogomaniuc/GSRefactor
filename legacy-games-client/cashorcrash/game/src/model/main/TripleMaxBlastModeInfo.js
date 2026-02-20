import SimpleInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

class TripleMaxBlastModeInfo extends SimpleInfo
{
	static get PARAM_IS_TRIPLE_MAX_BLAST_MODE()					{return "isTripleMaxBlast"};

	constructor()
	{
		super();

		this._fIsTripleMaxBlastMode_bl = false;
	}

	set isTripleMaxBlastMode(value)
	{
		this._fIsTripleMaxBlastMode_bl = value;
	}

	get isTripleMaxBlastMode()
	{
		return this._fIsTripleMaxBlastMode_bl;
	}

	destroy()
	{
		this._fIsTripleMaxBlastMode_bl = undefined;

		super.destroy();
	}
}
export default TripleMaxBlastModeInfo;