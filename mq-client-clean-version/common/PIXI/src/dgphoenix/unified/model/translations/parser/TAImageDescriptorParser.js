import SimpleXMLParser from '../../xml/SimpleXMLParser';
import TAImageDescriptor from '../data/TAImageDescriptor';

/**
 * Translatable asset image descriptor parser.
 * @class
 */
class TAImageDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} imageDescriptorElement - XML element
	 */
	constructor(imageDescriptorElement)
	{
		this._imageDescriptor = null;

		this._initTAImageDescriptorParser(imageDescriptorElement);
	}

	/**
	 * @type {TAImageDescriptor}
	 */
	get imageDescriptor()
	{
		return this._imageDescriptor;
	}

	_initTAImageDescriptorParser(imageDescriptorElement)
	{
		var lImageURL_str = SimpleXMLParser.getAttributeValue(imageDescriptorElement, "url", true);
		
		this._imageDescriptor = new TAImageDescriptor(lImageURL_str);
	}
}

export default TAImageDescriptorParser