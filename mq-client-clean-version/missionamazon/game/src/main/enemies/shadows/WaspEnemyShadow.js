import EnemyShadow from './EnemyShadow'

class WaspEnemyShadow extends EnemyShadow {

	//override
	_createView()
	{
		super._createView();
		this.view.alpha = 0;
		this.view.fadeTo(0.5, 10*2*16.6);
	}
}

export default WaspEnemyShadow;