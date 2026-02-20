import SimpleXMLParser from '../../../xml/SimpleXMLParser';
import LGFillDescriptor from './LGFillDescriptor';
import PointDescriptorParser from '../../geom/PointDescriptorParser';
import KeyColorDescriptorParser from './KeyColorDescriptorParser';

/**
 * Parser of linear gradient descriptor (XML element).
 * @class
 * 
 * @example
 * Linear gradient descriptor to be parsed:
 * <fill type="linear_gradient" repeat="lines">
 * 	<from x="0" y="0"/>
 * 	<to x="0" y="1"/>
 * 	<key position="0" color="0xffffff"/>
 * 	<key position="1" color="0xd7d7d7"/>
 * </fill>
 */
class LGFillDescriptorParser
{
	/**
	 * @constructor
	 * @param {*} lgFillDescriptorElement - Fill XML element.
	 */
	constructor(lgFillDescriptorElement)
	{
		this._lgFillDescriptor = null;

		this._initLGFillDescriptorParser(lgFillDescriptorElement);
	}

	/**
	 * Linear Gradient fill descriptor.
	 * @type {LGFillDescriptor}
	 * @readonly
	 */
	get lgFillDescriptor()
	{
		return this._lgFillDescriptor;
	}

	_initLGFillDescriptorParser(lgFillDescriptorElement)
	{
		if (lgFillDescriptorElement)
		{
			this._parse(lgFillDescriptorElement);
		}
	}

	_parse(lgFillDescriptorElement)
	{
		var lPointFrom_up = new PointDescriptorParser(SimpleXMLParser.getTag(lgFillDescriptorElement, "from", 0, true)).pointDescriptor;
		var lPointTo_up = new PointDescriptorParser(SimpleXMLParser.getTag(lgFillDescriptorElement, "to", 0, true)).pointDescriptor;
		var l_ulgfd = new LGFillDescriptor(lPointFrom_up, lPointTo_up);

		var lKeyColors_e_arr = SimpleXMLParser.getTags(lgFillDescriptorElement, "key");
		var lKeysCount_int = lKeyColors_e_arr.length;
		for (var i = 0; i < lKeysCount_int; i++)
		{
			l_ulgfd.addKeyColor(new KeyColorDescriptorParser(lKeyColors_e_arr[i]).keyColorDescriptor);
		}
		
		var lRepeatType_str = SimpleXMLParser.getAttributeValue(lgFillDescriptorElement, "repeat", false);
		if (lRepeatType_str)
		{
			l_ulgfd.repeatType = lRepeatType_str;
		}

		this._lgFillDescriptor = l_ulgfd;
	}
}

export default LGFillDescriptorParser;