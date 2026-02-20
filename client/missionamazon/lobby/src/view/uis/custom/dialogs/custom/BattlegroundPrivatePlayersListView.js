import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { DropShadowFilter } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';
import BattlegroundPrivatePlayersListItem from './BattlegroundPrivatePlayersListItem';

const ITEMS_AMOUNT = 6;

class BattlegroundPrivatePlayersListView extends Sprite
{
	static get EVENT_ON_ITEM_KICK_CLICKED () { return "EVENT_ON_ITEM_KICK_CLICKED" };
	static get EVENT_ON_CANCEL_KICK_CLICKED () { return "EVENT_EVENT_ON_CANCEL_KICK_CLICKEDON_INVITE_CLICKED" };

	update(aData_obj_arr, reinviteCountData)
	{
		this._update(aData_obj_arr,reinviteCountData);
	}

	constructor()
	{
		super();

		this._fItems_bppsli_arr = [];

		this._init();
	}

	_init()
	{
		for (let i = 0; i < ITEMS_AMOUNT; i++)
		{
			this._addItem(i);
		}
	}

	_addItem(aIndex_int)
	{
		let lItemData_obj = { index: aIndex_int };

		let lItem_bppsli = this.addChild(new BattlegroundPrivatePlayersListItem(lItemData_obj));
		lItem_bppsli.on(BattlegroundPrivatePlayersListItem.EVENT_ON_KICK_CLICKED, this._onItemKickClicked, this);
		lItem_bppsli.on(BattlegroundPrivatePlayersListItem.EVENT_ON_CANCEL_KICK_CLICKED, this._onCancelKickClicked, this);

		lItem_bppsli.y = aIndex_int*27.5;

		this._fItems_bppsli_arr.push(lItem_bppsli);
	}

	_update(aData_obj_arr, reinviteCountData)
	{
		if (!aData_obj_arr)
		{
			aData_obj_arr = [];
		}
		
		for (let i = 0; i < ITEMS_AMOUNT; i++)
		{
			let lItemData_obj = aData_obj_arr[i] || { isKicked: true };
			lItemData_obj.index = i;

			this._fItems_bppsli_arr[i].update(lItemData_obj, reinviteCountData);
		}
	}

	_onItemKickClicked(aEvent_e)
	{
		let lItem_bppsli = aEvent_e.target;
		this.emit(BattlegroundPrivatePlayersListView.EVENT_ON_ITEM_KICK_CLICKED, {nickname: lItem_bppsli.nickName})
	}

	_onCancelKickClicked(aEvent_e)
	{
		let lItem_bppsli = aEvent_e.target;
		this.emit(BattlegroundPrivatePlayersListView.EVENT_ON_CANCEL_KICK_CLICKED, {nickname: lItem_bppsli.nickName})
	}

	destroy()
	{
		super.destroy();
	}
}

export default BattlegroundPrivatePlayersListView;