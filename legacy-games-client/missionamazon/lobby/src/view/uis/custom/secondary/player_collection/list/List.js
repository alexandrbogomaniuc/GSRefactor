import Sprite from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

const ITEMS_DIRECTION = {
	VERTICAL: 	"vertical",
	HORIZONTAL: "horizontal"
};

const ITEMS_DISTANCE = 6;

class List extends Sprite
{
	static get EVENT_ON_ITEM_ADDED()		{ return "onListItemAdded"; }
	static get EVENT_ON_ITEM_REMOVED()		{ return "onListItemRmoved"; }
	static get EVENT_ON_ITEM_MOVED()		{ return "onListItemMoved"; }
	static get EVENT_ON_CLEARED()			{ return "onListCleared"; }
	
	constructor()
	{
		super();

		this._itemsContainer = null;
		this._direction = ITEMS_DIRECTION.VERTICAL;
		this._data = null;
		this._items = null;
		this._mask = null;
		
		this._init();
	}

	set data (listItemInfos)
	{
		this.clear();

		this._data = [];
		this._items = [];

		listItemInfos = listItemInfos instanceof Array ? listItemInfos : [listItemInfos];

		for (let i=0; i<listItemInfos.length; i++)
		{
			let listItemInfo = listItemInfos[i];
			if (!listItemInfo)
			{
				continue;
			}

			this.addItem(listItemInfo);
		}
	}

	get data()
	{
		return this._data;
	}

	get borders()
	{
		return this.getBounds();
	}

	get localBorders()
	{
		return this.getLocalBounds();
	}

	get stepDelay()
	{
		return 150;
	}

	addItem(listItemInfo)
	{
		this._data = this._data || [];
		this._items = this._items || [];

		this._data.push(listItemInfo);

		let itemIndex = this._data.length-1;
		let item = this._generateNewItemInstance(listItemInfo);
		
		this._items.push(item);

		let itemPos = this._getItemPosition(itemIndex);
		item.position.set(itemPos.x, itemPos.y)

		item.zIndex = itemIndex;
		this._itemsContainer.addChild(item);

		this.emit(List.EVENT_ON_ITEM_ADDED);

		return item;
	}

	hasItem(listItemInfo)
	{
		if (!this._data || !this._data.length)
		{
			return false;
		}

		let itemIndex = this._data.indexOf(listItemInfo);
		
		return (itemIndex >= 0);
	}

	updateItem(itemInfo)
	{
		if (!this.hasItem(itemInfo))
		{
			return;
		}

		let itemIndex = this._data.indexOf(itemInfo);
		this._getItem(itemIndex).update();
	}

	removeItem(listItemInfo, animateNextItems = false)
	{
		if (!this._data || !this._data.length)
		{
			throw new Error("Can't remove item from empty list.");
		}

		let itemIndex = this._data.indexOf(listItemInfo);
		if (itemIndex < 0)
		{
			throw new Error("Can't find item in the list.");
		}

		this._data.splice(itemIndex, 1);
		this._getItem(itemIndex).destroy();
		this._items.splice(itemIndex, 1);

		let delayIndex = 0;
		for (let i=itemIndex; i<this._data.length; i++)
		{
			let item = this._getItem(i);
			let itemPos = this._getItemPosition(i);

			itemPos = this._correctItemTargetPos(i, itemPos);

			item.targetPosition = itemPos.clone();
			this._moveItemTo(item, itemPos, animateNextItems, this._calculateItemMoveDelay(delayIndex));
			delayIndex++;
		}
		
		this.emit(List.EVENT_ON_ITEM_REMOVED);
	}

	getItem(itemInfo)
	{
		if (!this._data || !this._data.length)
		{
			return null;
		}

		let itemIndex = this._data.indexOf(itemInfo);
		if (itemIndex < 0)
		{
			throw new Error("Can't find item in the list.");
		}

		return this._getItem(itemIndex);
	}

	_correctItemTargetPos(itemIndex, itemPos)
	{
		return itemPos;
	}

	clear()
	{
		while (this._data && this._data.length)
		{
			this.removeItem(this._data[0]);
		}
		this._data = null;
		this._items = null;

		this.emit(List.EVENT_ON_CLEARED);
	}

	_init()
	{
		this._itemsContainer = this.addChild(new Sprite());
	}

	_generateNewItemInstance(itemInfo)
	{
		throw new Error("Method must be overridden is subclass.");
	}

	_getItem(index)
	{
		return this._items[index];
	}

	_getItemPosition(itemIndex)
	{
		let pos = new PIXI.Point(0, 0);

		if (itemIndex == 0)
		{
			return pos;
		}

		let prevIcon = this._getItem(itemIndex-1);
		let prevIconPosition = prevIcon.targetPosition || prevIcon.position;
		switch (this._direction)
		{
			case ITEMS_DIRECTION.VERTICAL:
				pos.y = prevIconPosition.y + prevIcon.borders.height;
				pos.y += this._itemsDistance;
				break;
			case ITEMS_DIRECTION.HORIZONTAL:
				pos.x = prevIconPosition.x + prevIcon.borders.width;
				pos.x += this._itemsDistance;
				break;
		}
		
		return pos;
	}

	_moveItemTo(item, newPosition, animateNextItems, delay)
	{
		item.position.set(newPosition.x, newPosition.y);
		item.targetPosition = null;

		this._onItemMoved();
	}

	_calculateItemMoveDelay(delayIndex)
	{
		return (delayIndex*this.stepDelay);
	}

	_onItemMoved()
	{
		this.emit(List.EVENT_ON_ITEM_MOVED);
	}

	get _itemsDistance()
	{
		return ITEMS_DISTANCE;
	}

	_getItemIndex (itemInfo)
	{
		if (!this._data || !this._data.length)
		{
			return -1;
		}

		return this._data.indexOf(itemInfo);
	}

	destroy()
	{
		this.clear();

		this._itemsContainer = null;
		this._direction = undefined;
		this._data = null;
		this._items = null;
		this._mask = null;
		
		super.destroy();
	}
}

export {List, ITEMS_DIRECTION}