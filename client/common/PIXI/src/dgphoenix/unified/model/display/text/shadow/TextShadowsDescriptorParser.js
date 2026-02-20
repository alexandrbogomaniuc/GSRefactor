import SimpleXMLParser from '../../../xml/SimpleXMLParser';
import TextShadowsDescriptor from './TextShadowsDescriptor';
import TextShadowDescriptorParser from './TextShadowDescriptorParser';

/**
 * Parses shadows descriptor (XML element).
 * @class
 * @example
 * Example of shadows xml descriptor:
 * <shadows>
 * 	<shadow color="0x000000" distance="4" angle="90" alpha="0.8" blur="3"/>
 * </shadows>
 */
class TextShadowsDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} textShadowsDescriptorElement - XML element.
	 */
	constructor (textShadowsDescriptorElement)
	{
		this._shadowsDescriptor = null;

		this._initTextShadowsDescriptorParser(textShadowsDescriptorElement);
	}

	/**
	 * Shadows descriptor instance.
	 * @type {TextShadowsDescriptor}
	 */
	get shadowsDescriptor ()
	{
		return this._shadowsDescriptor;
	}

	_initTextShadowsDescriptorParser (textShadowsDescriptorElement)
	{
		if (textShadowsDescriptorElement)
		{
			this._parse(textShadowsDescriptorElement);
		}
	}

	_parse(textShadowsDescriptorElement)
	{
		this._shadowsDescriptor = new TextShadowsDescriptor();
		
		var lShadowsDescriptors_e_arr = SimpleXMLParser.getTags(textShadowsDescriptorElement.childNodes, "shadow");
		var lCount_int = lShadowsDescriptors_e_arr.length;
		for (var i = 0; i < lCount_int; i++)
		{
			this._shadowsDescriptor.addTextShadowDescriptor(new TextShadowDescriptorParser(lShadowsDescriptors_e_arr[i]).textShadowDescriptor);
		}
	}
}

export default TextShadowsDescriptorParser