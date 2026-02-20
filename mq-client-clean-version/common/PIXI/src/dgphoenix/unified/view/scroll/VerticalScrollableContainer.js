import Sprite from '../base/display/Sprite';
import ScrollableContainer from './ScrollableContainer';

/**
 * Base container to be scrolled vertically.
 * @class
 * @augments ScrollableContainer
 * @inheritdoc
 */
class VerticalScrollableContainer extends ScrollableContainer {

	constructor()
	{
		super();
	}

	moveTo(aValue_num)
	{
		this.y = this.initPosition.y - aValue_num;
	}

	_getValidStep(aStep_num, aHeightItem_num)
    {
        let lValidStep_num;
        if ((aStep_num % aHeightItem_num === 0) || aStep_num === 0)
        {
            lValidStep_num = aStep_num;
        }
        else 
        {
            lValidStep_num = (Math.trunc(aStep_num / aHeightItem_num) + 1) * aHeightItem_num;
        }
        return lValidStep_num;
    }
}

export default VerticalScrollableContainer