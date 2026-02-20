import SimpleController from '../base/SimpleController';
import CustomerspecInfo from '../../model/preloading/CustomerspecInfo';
import { APP } from '../main/globals';
import CustomerspecLoader from '../interaction/resources/loaders/CustomerspecLoader';
import Queue from '../interaction/resources/loaders/Queue';

const CUSTOMERSPEC_XML_FILENAME = "customerspec_descriptor.xml";

class CustomerspecController extends SimpleController
{
	static get EVENT_CUSTOMERSPEC_LOADED()			{return "customerspecLoaded";}
	static get EVENT_CUSTOMERSPEC_LOAD_ERROR()		{return "customerspecLoadError";}

	load()
	{
		this._load();
	}

	constructor()
	{
		super(new CustomerspecInfo());

		this._fFullDescriptorPath_str = null;
	}

	get customerspecDescriptorPath()
	{
		if (this._fFullDescriptorPath_str) return this._fFullDescriptorPath_str;

		let lCustomerspecDescriptorURL_str = APP.appParamsInfo.customerspecDescriptorUrl;
		if (lCustomerspecDescriptorURL_str == null) return null;

		let lCustomerspecDescriptorFullURL_str = lCustomerspecDescriptorURL_str + CUSTOMERSPEC_XML_FILENAME;

		if (APP.isDebugMode)
		{
			let lLobbyPath_str = APP.appParamsController.info.lobbyPath;
			if (lLobbyPath_str)
			{
				lCustomerspecDescriptorFullURL_str = lLobbyPath_str + "assets/_debug/settings/" + CUSTOMERSPEC_XML_FILENAME;
			}
		}

		console.log("customerspec_descriptor path:", lCustomerspecDescriptorFullURL_str);

		return (this._fFullDescriptorPath_str = lCustomerspecDescriptorFullURL_str);
	}

	_load()
	{
		//DEBUG!
		/*this.emit(CustomerspecController.EVENT_CUSTOMERSPEC_LOADED);
		return;*/

		let lCustomerspecDescriptorPath_str = this.customerspecDescriptorPath;

		if (!lCustomerspecDescriptorPath_str)
		{
			this.emit(CustomerspecController.EVENT_CUSTOMERSPEC_LOAD_ERROR);
			return;
		}

		let lLoadingQueue_q = new Queue();

		lLoadingQueue_q.add(
			new CustomerspecLoader(lCustomerspecDescriptorPath_str)
		);
		lLoadingQueue_q.once('fileload', this._onDescriptorLoaded, this);

		lLoadingQueue_q.load();
	}

	_onDescriptorLoaded(aEvent_obj)
	{
		let lLoadedData_obj = aEvent_obj.item;

		if (lLoadedData_obj.key == this.customerspecDescriptorPath)
		{
			this._parseDescriptor(lLoadedData_obj.data);

			this.emit(CustomerspecController.EVENT_CUSTOMERSPEC_LOADED);
		}
		else
		{
			this.emit(CustomerspecController.EVENT_CUSTOMERSPEC_LOAD_ERROR);
		}
	}

	_parseDescriptor(aDescriptorDataXML)
	{
		let lInfo_ci = this.info;

		//BRAND...
		let lBrandTag = aDescriptorDataXML.getElementsByTagName(CustomerspecInfo.TAG_BRAND)[0];

		if (lBrandTag)
		{
			let lBrandEnableAttribute = lBrandTag.getAttribute(CustomerspecInfo.ATTRIBUTE_ENABLE);
			let lEnableBrand_bln = (lBrandEnableAttribute == "true");

			let lBrandPriorityAttribute = lBrandTag.getAttribute(CustomerspecInfo.ATTRIBUTE_PRIORITY);
			let lBrandPriority = Number(lBrandPriorityAttribute);

			lInfo_ci.brand = {enable: lEnableBrand_bln, priority: lBrandPriority};
		}
		//...BRAND
	}

	destroy()
	{
		this._fFullDescriptorPath_str = null;

		super.destroy();
	}
}

export default CustomerspecController;