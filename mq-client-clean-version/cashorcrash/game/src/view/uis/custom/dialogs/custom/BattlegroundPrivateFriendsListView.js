import BattlegroundPrivatateFriendsListItem from './BattlegroundPrivatateFriendsListItem';
import VerticalScrollableContainer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/VerticalScrollableContainer';

let ITEMS_AMOUNT = 0;

class BattlegroundPrivateFriendsListView extends VerticalScrollableContainer {
	static get EVENT_ON_INVITE_FRIEND_CLICKED() { return "EVENT_ON_INVITE_FRIEND_CLICKED" };

	update(aData_obj_arr) {
		return this._update(aData_obj_arr);
	}

	constructor() {
		super();
		this._fItems_bppsli_arr = [];
	}

	get measuredWidth() {
		return 200;
	}

	get measuredHeight() {
		let lastHeight_fl = -1000;
		for (let i = 0; i < this._fItems_bppsli_arr.length; i++) {
			const itemInstance = this._fItems_bppsli_arr[i];
			const itemInsstance_y = itemInstance.y;

			if (itemInstance.visible && itemInsstance_y > lastHeight_fl) {
				lastHeight_fl = itemInstance.y
			}
		}
		return lastHeight_fl + 27.5;
	}


	_addItem(aIndex_int) {
		console.log("adding item")
		let lItemData_obj = { index: aIndex_int };

		let lItem_bppsli = this.addChild(new BattlegroundPrivatateFriendsListItem(lItemData_obj));
		lItem_bppsli.on(BattlegroundPrivatateFriendsListItem.EVENT_ON_INVITE_CLICKED, this._onItemInviteClicked, this);

		lItem_bppsli.y = aIndex_int * 27.5;

		this._fItems_bppsli_arr.push(lItem_bppsli);
	}


	_update(aData_obj_arr) {
		let sizeChanged = false;
		const previousItemAmount = Number(ITEMS_AMOUNT);
		ITEMS_AMOUNT = Number(aData_obj_arr.length);

		if (aData_obj_arr.length == 0) {
			for (let n = 0; n < this._fItems_bppsli_arr.length; n++) {
				this._fItems_bppsli_arr[n].visible = false;
			}

			if (previousItemAmount != ITEMS_AMOUNT) {
				sizeChanged = true;
			}
			return sizeChanged;
		};

		if (previousItemAmount != ITEMS_AMOUNT) {
			sizeChanged = true;
			for (let i = 0; i < ITEMS_AMOUNT; i++) {
				const lItemData_obj = aData_obj_arr[i];
				lItemData_obj.index = i;
				if (i + 1 > this._fItems_bppsli_arr.length) {
					this._addItem(i);
				}
				const item = this._fItems_bppsli_arr[i];
				if (item.visible != true) {
					this._fItems_bppsli_arr[i].visible = true;
				}
				item.update(lItemData_obj);

			}

			for (let n = 0; n < this._fItems_bppsli_arr.length; n++) {
				if (n >= ITEMS_AMOUNT) {
					this._fItems_bppsli_arr[n].visible = false;
				}
			}

		} else {
			for (let i = 0; i < ITEMS_AMOUNT; i++) {
				const lItemData_obj = aData_obj_arr[i];
				const item = this._fItems_bppsli_arr[i];
				item.update(lItemData_obj);
			}
		}
		return sizeChanged;
	}

	_onItemInviteClicked(aEvent_e) {
		console.log("invite friend is indead clicked")
		let lItem_bppsli = aEvent_e.target;
		this.emit(BattlegroundPrivateFriendsListView.EVENT_ON_INVITE_FRIEND_CLICKED, { nickname: lItem_bppsli.nickName })
	}

	destroy() {
		super.destroy();
	}
}

export default BattlegroundPrivateFriendsListView;