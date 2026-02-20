import Sprite from '../base/display/Sprite';
import ScrollableContainer from './ScrollableContainer';

/**
 * Base container to be scrolled horizontally.
 * @class
 * @augments ScrollableContainer
 * @inheritdoc
 */
class HorizontalScrollableContainer extends ScrollableContainer {

	constructor()
	{
		super();
	}

	moveTo(aValue_num)
	{
		this.x = this.initPosition.x - aValue_num;		
	}
}

export default HorizontalScrollableContainer