import {Utils} from '../Utils';

let _xmlSerializer = null;

let _XML_ENCODED_APOS_ENTITY		= "&apos;";
let _XML_ENCODED_QUOT_ENTITY		= "&quot;";
let _XML_ENCODED_GT_ENTITY			= "&gt;";
let _XML_ENCODED_LT_ENTITY			= "&lt;";
let _XML_ENCODED_AMP_ENTITY			= "&amp;";

let _XML_DECODED_APOS_ENTITY		= "'";
let _XML_DECODED_QUOT_ENTITY		= "\"";
let _XML_DECODED_GT_ENTITY			= ">";
let _XML_DECODED_LT_ENTITY			= "<";
let _XML_DECODED_AMP_ENTITY			= "&";

let _XML_ENCODED_APOS_ENTITY_SEARCH_PATTERN = new RegExp(_XML_ENCODED_APOS_ENTITY, "g");
let _XML_ENCODED_QUOT_ENTITY_SEARCH_PATTERN = new RegExp(_XML_ENCODED_QUOT_ENTITY, "g");
let _XML_ENCODED_GT_ENTITY_SEARCH_PATTERN = new RegExp(_XML_ENCODED_GT_ENTITY, "g");
let _XML_ENCODED_LT_ENTITY_SEARCH_PATTERN = new RegExp(_XML_ENCODED_LT_ENTITY, "g");
let _XML_ENCODED_AMP_ENTITY_SEARCH_PATTERN = new RegExp(_XML_ENCODED_AMP_ENTITY, "g");

let _XML_DECODED_APOS_ENTITY_SEARCH_PATTERN = new RegExp(_XML_DECODED_APOS_ENTITY, "g");
let _XML_DECODED_QUOT_ENTITY_SEARCH_PATTERN = new RegExp(_XML_DECODED_QUOT_ENTITY, "g");
let _XML_DECODED_GT_ENTITY_SEARCH_PATTERN = new RegExp(_XML_DECODED_GT_ENTITY, "g");
let _XML_DECODED_LT_ENTITY_SEARCH_PATTERN = new RegExp(_XML_DECODED_LT_ENTITY, "g");
let _XML_DECODED_AMP_ENTITY_SEARCH_PATTERN = new RegExp(_XML_DECODED_AMP_ENTITY, "g");

/**
 * XML parser.
 * @class
 */
class SimpleXMLParser
{
	/**
	 * Get XMLSerializer.
	 * @static
	 */
	static get xmlSerializer()
	{
		if (!_xmlSerializer)
		{
			_xmlSerializer = new XMLSerializer();
		}

		return _xmlSerializer;
	}

	static get _XML_SERIALIZED_ELEMENT_ATOMICITY_CHECKING_PROVIDER() { return new RegExp("^<.+/>$"); }
	static get _XML_SERIALIZED_ELEMENT_INNER_CONTENT_RETRIEVING_PROVIDER() { return new RegExp("^<.+?>([\\s\\S]*)</.+?>$"); }

	/**
	 * Get XML tag.
	 * @param {*} aTags_obj 
	 * @param {string} aTagName_str 
	 * @param {number} aOptTagIndex_int 
	 * @param {boolean} aOptThrowExceptionIfNotExists_bl 
	 * @param {number} aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int 
	 * @returns  {Element} XML tag.
	 * @static
	 */
	static getTag (aTags_obj, aTagName_str, aOptTagIndex_int, aOptThrowExceptionIfNotExists_bl, aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int)
	{
		aOptTagIndex_int = aOptTagIndex_int === undefined ? 0 : aOptTagIndex_int;
		var lElement_e = SimpleXMLParser.getTags(aTags_obj, aTagName_str, aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int)[aOptTagIndex_int];
		if (aOptThrowExceptionIfNotExists_bl && !lElement_e)
		{
			throw new Error(`Tag does not exist: '${aTagName_str}'; index= ${aOptTagIndex_int}`);
		}
		return lElement_e;
	}

	/**
	 * Get XML tags.
	 * @param {*} aTags_obj 
	 * @param {string} aTagName_str 
	 * @param {number} aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int 
	 * @returns {Element[]}
	 */
	static getTags (aTags_obj, aTagName_str, aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int)
	{
		var lNodes_nl;
		if (aTags_obj instanceof NodeList)
		{
			lNodes_nl = aTags_obj;
		}
		else if (aTags_obj instanceof Element)
		{
			lNodes_nl = aTags_obj.childNodes;
		}
		else
		{
			throw new Error(`Invalid tags argument: '${aTags_obj}'; `);
		}

		if (!(typeof aTagName_str === 'string'))
		{
			throw new Error(`Invalid name argument: '${aTagName_str}'; `);
		}

		if (
				aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int !== undefined
				&&
				(
					!Utils.isInt(aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int)
					|| aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int < 0
				)
			)
		{
			throw new Error(`Invalid count argument: '${aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int}'; `);
		}

		var lTags_e_arr = new Array();
		var lElementsCount_int = lNodes_nl.length;
		for (var i = 0; i < lElementsCount_int; i++)
		{
			var lNode_n = lNodes_nl[i];
			if (
					(lNode_n instanceof Element)
					&& (lNode_n.tagName === aTagName_str)
				)
			{
				lTags_e_arr.push(lNode_n);
			}
		}

		var lTagsCount_int = lTags_e_arr.length;
		if (
				aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int !== undefined
				&& lTags_e_arr.length !== aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int
			)
		{
			throw new Error(`Unexpected tags count: '${aTagName_str}' : ${lTagsCount_int} / ${aOptThrowIfTotalNamedTagsCountIsNotEqualTo_int}`);
		}

		return lTags_e_arr;
	}

	/**
	 * Get XML tag attribute value.
	 * @param {Element} aElement_e 
	 * @param {string} aAttributeName_str 
	 * @param {boolean} aOptThrowIfNotExists_bl 
	 * @returns {string}
	 */
	static getAttributeValue (aElement_e, aAttributeName_str, aOptThrowIfNotExists_bl)
	{
		if (!(aElement_e instanceof Element))
		{
			throw new Error(`Invalid element arg: '${aElement_e}'; `);
		}
		
		if (!(typeof aAttributeName_str === 'string'))
		{
			throw new Error(`Invalid name arg: '${aAttributeName_str}'; `);
		}

		var lAttribute_obj = aElement_e.attributes[aAttributeName_str];
		if (!lAttribute_obj)
		{
			if (aOptThrowIfNotExists_bl)
			{
				throw new Error(`Attribute does not exist: '${aAttributeName_str}'; `);
			}
			return undefined;
		}
		var lValue_str = lAttribute_obj.value;
		return lValue_str;
	}

	/**
	 * Get XML element inner text.
	 * @param {Element} aElement_e 
	 * @returns {string}
	 */
	static getInnerXMLText (aElement_e)
	{
		if (!(aElement_e instanceof Element))
		{
			throw new Error(`Invalid arg: '${aElement_e}'; `);
		}

		//note: {Element}.innerHTML may work as well but there are potential issues (e.g. case-sensitivity specific) so explicit implementation is in use
		var lXMLText_str = SimpleXMLParser.xmlSerializer.serializeToString(aElement_e);
		if (SimpleXMLParser._XML_SERIALIZED_ELEMENT_ATOMICITY_CHECKING_PROVIDER.exec(lXMLText_str))
		{
			return "";
		}
		var lResult_str_arr = SimpleXMLParser._XML_SERIALIZED_ELEMENT_INNER_CONTENT_RETRIEVING_PROVIDER.exec(lXMLText_str);
		if (!lResult_str_arr || lResult_str_arr.length !== 2)
		{
			throw new Error('Inner XML element content retrieving failure;');
		}
		var lInnerXMLText_str = lResult_str_arr[1];
		return lInnerXMLText_str;
	}

	/**
	 * Decode XML inner text entities.
	 * @param {string} aText_str - Source XML inner text.
	 * @returns {string}
	 */
	static decodeXMLTextStrongEntities (aText_str)
	{
		if (!(typeof aText_str === 'string'))
		{
			throw new Error(`Invalid arg: ${aText_str}`);
		}
		return aText_str.replace(_XML_ENCODED_APOS_ENTITY_SEARCH_PATTERN, _XML_DECODED_APOS_ENTITY)
						.replace(_XML_ENCODED_QUOT_ENTITY_SEARCH_PATTERN, _XML_DECODED_QUOT_ENTITY)
						.replace(_XML_ENCODED_GT_ENTITY_SEARCH_PATTERN, _XML_DECODED_GT_ENTITY)
						.replace(_XML_ENCODED_LT_ENTITY_SEARCH_PATTERN, _XML_DECODED_LT_ENTITY)
						.replace(_XML_ENCODED_AMP_ENTITY_SEARCH_PATTERN, _XML_DECODED_AMP_ENTITY); //the &amp; -> & decoding must be addressed last to avoid unexpected potentially possible new entities generation
	}

}

export default SimpleXMLParser;
