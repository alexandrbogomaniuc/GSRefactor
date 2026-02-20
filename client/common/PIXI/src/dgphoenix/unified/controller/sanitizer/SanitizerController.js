import SimpleController from '../base/SimpleController';
import sanitizeHtml from 'sanitize-html';

/**
 * @class
 * @extends SimpleController
 * @classdesc Serves to clean data of invalid values 
 */
class SanitizerController extends SimpleController {

	constructor()
	{
		super ();
	}

	init() 
	{
		super.init();
	}

	/**
	 * Cleans GET params
	 * @param {object} aParam 
	 * @returns {object} Cleaned params with proper values
	 */
	sanitizer(aParam)
	{
		let lResult = {};
		for (var key in aParam) {
			let lNewValue = sanitizeHtml(aParam[key]);
			if (lNewValue != aParam[key])
			{
				console.log("Key: "+key+" value: "+aParam[key]+"  sanitizer: "+lNewValue);
			}
			lResult[key] = lNewValue;
		  }

		return lResult;
	}
}

export default SanitizerController;