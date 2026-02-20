import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { DropShadowFilter } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';
import BattlegroundPrivatePlayersListItem from './BattlegroundPrivatePlayersListItem';
import VerticalScrollableContainer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/scroll/VerticalScrollableContainer';


const ITEMS_AMOUNT = 6;

class BattlegroundPrivatePlayersListView extends VerticalScrollableContainer
{
	static get EVENT_ON_ITEM_KICK_CLICKED () { return "EVENT_ON_ITEM_KICK_CLICKED" };
	static get EVENT_ON_CANCEL_KICK_CLICKED () { return "EVENT_EVENT_ON_CANCEL_KICK_CLICKEDON_INVITE_CLICKED" };
	static get EVENT_ON_INVITE_CLICKED () { return "EVENT_ON_INVITE_CLICKED" };

	update(aData_obj_arr, reinviteCount)
	{
		if(!aData_obj_arr) return;
		if(aData_obj_arr.length!=this._fItems_bppsli_arr.length)
		{

			
			for( var s =0; s< this._fItems_bppsli_arr.length; s++){
				const noLongerValiditem = this._fItems_bppsli_arr[s];
				noLongerValiditem.destroy();
			}
			this._fItems_bppsli_arr = [];

			for( var i =0; i< aData_obj_arr.length; i++){
				this._addItem(i);
			}

		}
		this._update(aData_obj_arr, reinviteCount);
	}


	get measuredWidth() {
		return 200;
	}

	get measuredHeight() {
		var length = this._fItems_bppsli_arr.length-1;
		if(length<0) return 0;
		return this._fItems_bppsli_arr[length].y + 27.5;
	}



	constructor()
	{
		super();

		this._fItems_bppsli_arr = [];

		this._init();
	}

	_init()
	{
		/*for (let i = 0; i < ITEMS_AMOUNT; i++)
		{
			this._addItem(i);
		}*/
	}

	_addItem(aIndex_int)
	{
		let lItemData_obj = { index: aIndex_int };

		let lItem_bppsli = this.addChild(new BattlegroundPrivatePlayersListItem(lItemData_obj));
		lItem_bppsli.on(BattlegroundPrivatePlayersListItem.EVENT_ON_KICK_CLICKED, this._onItemKickClicked, this);
		lItem_bppsli.on(BattlegroundPrivatePlayersListItem.EVENT_ON_CANCEL_KICK_CLICKED, this._onCancelKickClicked, this);
		lItem_bppsli.on(BattlegroundPrivatePlayersListItem.EVENT_ON_INVITE_CLICKED, this._onInviteClicked, this);
		

		lItem_bppsli.y = aIndex_int*27.5;

		this._fItems_bppsli_arr.push(lItem_bppsli);
	}

	_onInviteClicked(aEvent_e)
	{
	
		let lItem_bppsli = aEvent_e.target;
		this.emit(BattlegroundPrivatePlayersListView.EVENT_ON_INVITE_CLICKED, {nickname: lItem_bppsli.nickName});
	}

	_update(aData_obj_arr, reinviteCount)
	{
		if (!aData_obj_arr)
		{
			aData_obj_arr = [];
		}
		
		for (let i = 0; i < aData_obj_arr.length; i++)
		{
			let lItemData_obj = aData_obj_arr[i] || { isKicked: true };
			lItemData_obj.index = i;
			let listItem = this._fItems_bppsli_arr[i];
			listItem.update(lItemData_obj,reinviteCount);
			listItem.isOddPosition(i%2 === 0);
		}
	}

	_onItemKickClicked(aEvent_e)
	{
		console.log("basick kick friend clicked")
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