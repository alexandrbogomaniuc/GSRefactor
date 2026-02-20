import { APP } from '../../../unified/controller/main/globals';
import NumberValueFormat from '../../../unified/view/custom/values/NumberValueFormat';
import SUPPORTED_CURRENCIES from './currencies_descriptor.json';

class CurrencyInfo
{
	static _fCompatibleCurrenciesIds_obj = {};
	static _fSupportedCurrenciesDescriptor_obj = {};
	static _fSupportedCurrenciesInitialized_bl = false;

	static get _CURRENCY_ID_DEFAULT()	{return "VIRTUAL_DEFAULT_CURRENCY";} //virtual default currency; it is necessary to be sure the value is not minified
	static get _CURRENCY_FORMAT_VALUE_PATTERN() {return "<value>";}
	static get _DEFAULT_THOUSAND_DISCHARGE_SUFFIXES() {
		return {
			"1": "K",
			"2": "M",
			"3": "B"
		};
	}

	static _addCompatibleCurrencyIdMapping(aCompatibleCurrencyId_str, aCurrencyId_str)
	{
		CurrencyInfo._checkCurrencyId(aCurrencyId_str);
		CurrencyInfo._checkCurrencyId(aCompatibleCurrencyId_str);

		if (!CurrencyInfo._fSupportedCurrenciesDescriptor_obj[aCurrencyId_str])
		{
			throw Error("Currency id is not supported: " + aCurrencyId_str);
		}

		if (CurrencyInfo._fSupportedCurrenciesInitialized_bl)
		{
			throw Error("Supported currencies have already been initialized");
		}

		if (CurrencyInfo._fCompatibleCurrenciesIds_obj[aCompatibleCurrencyId_str])
		{
			throw Error("Already added: " + aCompatibleCurrencyId_str);
		}

		CurrencyInfo._fCompatibleCurrenciesIds_obj[aCompatibleCurrencyId_str] = aCurrencyId_str;
	}

	static _addCurrencyDescriptor(aCurrencyId_str, aCurrencyDecimalPrecision_int, aCurrencyFormat_str, aThousandDishargeSuffixes_obj)
	{
		CurrencyInfo._checkCurrencyId(aCurrencyId_str);
		if (
				isNaN(aCurrencyDecimalPrecision_int)
				|| aCurrencyDecimalPrecision_int < 0
			)
		{
			throw Error("Invalid currency decimal precision: " + aCurrencyDecimalPrecision_int);
		}

		if (aCurrencyDecimalPrecision_int > 2)
		{
			//the reason for this restriction introduction is the fact some implementations need a capability to determine a minimum supported value in currency in strict coordination with the server side to avoid unpredictable result when attempt to bet less than the minimum (e.g. the minimum limit in table games when not specified by the server explicitly); at the same time it has been concluded to do not restrict if the value is less than the actual value on the server (e.g. 0 for the TKN on the client but usual 2 on the server side) due to this kind of discrepancy does not seem to be critical unlike the previously described case
			throw Error("Currency decimal precision value must not exceed the value used by the server side: " + aCurrencyDecimalPrecision_int + "/2");
		}

		if (
				typeof aCurrencyFormat_str !== 'string'
				|| aCurrencyFormat_str.indexOf(CurrencyInfo._CURRENCY_FORMAT_VALUE_PATTERN) === -1
			)
		{
			throw Error("Invalid currency format: " + aCurrencyFormat_str);
		}

		if (CurrencyInfo._fSupportedCurrenciesDescriptor_obj[aCurrencyId_str])
		{
			throw Error("Already in use: " + aCurrencyId_str);
		}

		let lCurrencyDescriptor_obj = {
			i_fCurrencyId_str: aCurrencyId_str,
			i_fCurrencyDecimalPrecision_int: aCurrencyDecimalPrecision_int,
			i_fCurrencyFormat_str: aCurrencyFormat_str,
			i_fThousandDishargeSuffixes_obj: aThousandDishargeSuffixes_obj
		};
		CurrencyInfo._fSupportedCurrenciesDescriptor_obj[aCurrencyId_str] = lCurrencyDescriptor_obj;
	}

	static _checkCurrencyId(aCurrencyId_str, aOptIgnoreUppercaseCheck_bl)
	{
		if (
				typeof aCurrencyId_str !== 'string'
				|| (!aOptIgnoreUppercaseCheck_bl && aCurrencyId_str !== aCurrencyId_str.toUpperCase())
				|| aCurrencyId_str === ""
			)
		{
			throw Error("Invalid currency id: " + aCurrencyId_str);
		}
	}

	constructor()
	{
		this._initSupportedCurrenciesDescriptor();
	}
	
	i_setCurrencyId(aCurrencyId_str)
	{
		CurrencyInfo._checkCurrencyId(aCurrencyId_str, true);

		if (!CurrencyInfo._fSupportedCurrenciesInitialized_bl)
		{
			throw Error("Supported currencies are not initialized yet!");
		}

		if (typeof aCurrencyId_str !== 'string')
		{
			throw Error("The currency id must be string code like 'XXX'!");
		}

		aCurrencyId_str = aCurrencyId_str.toUpperCase();
		let lCorrectedCurrencyId_str = CurrencyInfo._fCompatibleCurrenciesIds_obj[aCurrencyId_str];
		if (lCorrectedCurrencyId_str)
		{
			aCurrencyId_str = lCorrectedCurrencyId_str;
		}

		this._fCurrencyId_str = aCurrencyId_str;
	}

	i_getCurrencyId()
	{
		return this._getCurrencyId();
	}

	i_resetCurrency()
	{
		this._fCurrencyId_str = undefined;
	}

	i_getCurrencyDecimalPrecision()
	{
		if (APP.isBattlegroundGame)
		{
			return 0;
		}
		return this._getCurrencyDescriptor().i_fCurrencyDecimalPrecision_int;
	}

	i_getCurrencyAbsPrecision()
	{
		return Math.pow(10, -this.i_getCurrencyDecimalPrecision());
	}

	i_getCurrencySymbol()
	{
		return this._getCurrencyFormatPattern().replace(CurrencyInfo._CURRENCY_FORMAT_VALUE_PATTERN, "").trim();
	}

	i_isCurrencySymbolDefined()
	{
		let l_str = this.i_getCurrencySymbol();
		return !!l_str && !!l_str.length;
	}

	i_getValueNumber(aValue_str, aOptThrowException_bl = false)
	{
		let lCurrencyFormat_str = this._getCurrencyFormatPattern().replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
		let lNumberRegExp_str = this.i_getCurrencyDecimalPrecision() > 0 ?
			`(\\d+(?:\\.\\d{1,${this.i_getCurrencyDecimalPrecision()}})?)` :
			'(\\d+)';
		let lCurrencyRegExp_str = '^' + lCurrencyFormat_str.replace(CurrencyInfo._CURRENCY_FORMAT_VALUE_PATTERN, lNumberRegExp_str) + '$'
		let lCurrencyRegExp_re = RegExp(lCurrencyRegExp_str);
		let lMatch_str_arr = aValue_str.match(lCurrencyRegExp_re);
		if (!lMatch_str_arr)
		{
			if (aOptThrowException_bl)
			{
				throw Error("Value string does not match current format pattern: " + aValue_str);
			}
			return NaN;
		}
		return lMatch_str_arr && +lMatch_str_arr[1];
	}

	i_formatString(aValue_str)
	{
		return this._getCurrencyFormatPattern().replace(CurrencyInfo._CURRENCY_FORMAT_VALUE_PATTERN, aValue_str);
	}

	i_formatNumber(aValue_num, aOptIncludeCurrencyMark_bl=true, aOptForceIncludeCurrencyMark_bl, aOptDecimalPrecision_num, aOptStartValueForShortForm_num, aOptDiscardDecimalPart_bl=false, aOptThrowExceptionIfExtraDigitsCount_bl=true, aOptCutDecimalNulls_bl=false, aOptAmountOfAllowedDecimals_int=0)
	{
		if (aOptDecimalPrecision_num === undefined || aOptDecimalPrecision_num === null || aOptDecimalPrecision_num > 2)
		{
			aOptDecimalPrecision_num = this.i_getCurrencyDecimalPrecision();
		}

		if (aOptAmountOfAllowedDecimals_int > 0)
		{
			aOptAmountOfAllowedDecimals_int = Math.min(aOptAmountOfAllowedDecimals_int, this.i_getCurrencyDecimalPrecision());
		}

		let lVal_str = NumberValueFormat.formatMoney(aValue_num, true, aOptDecimalPrecision_num, aOptThrowExceptionIfExtraDigitsCount_bl, aOptCutDecimalNulls_bl, aOptAmountOfAllowedDecimals_int);

		if (aOptStartValueForShortForm_num !== undefined && aOptStartValueForShortForm_num !== null && aOptStartValueForShortForm_num >= 0)
		{
			lVal_str = this._addDischargeSuffix(lVal_str, aOptStartValueForShortForm_num, aOptDecimalPrecision_num);
		}

		if (
				this.__isCurrencyMarkExcludeRequired()
				&& !aOptForceIncludeCurrencyMark_bl
			)
		{
			aOptIncludeCurrencyMark_bl = false;
		}

		lVal_str = aOptIncludeCurrencyMark_bl ? this.i_formatString(lVal_str) : lVal_str;

		if (aOptDiscardDecimalPart_bl)
		{
			let lDecimalDelimeterIndex_int = lVal_str.indexOf(NumberValueFormat.DECIMAL_DELIMETER);

			if (lDecimalDelimeterIndex_int >= 0)
			{
				lVal_str = lVal_str.substring(0, lDecimalDelimeterIndex_int);
			}
		}
		return lVal_str;
	}

	__isCurrencyMarkExcludeRequired()
	{
		return APP.isBattlegroundGame;
	}

	i_formatIncomeWithPlusSign(aValue_num, aOptIncludeCurrencyMark_bl, aOptForceIncludeCurrencyMark_bl, aOptDecimalPrecision_num, aOptStartValueForShortForm_num, aOptDiscardDecimalPart_bl=false, aOptThrowExceptionIfExtraDigitsCount_bl=true, aOptCutDecimalNulls_bl=false, aOptAmountOfAllowedDecimals_int=0, aOptUseSpaceCharBetween_bl=true)
	{
		let lPlusSign_str = aOptUseSpaceCharBetween_bl ? "+ " : "+";
		return lPlusSign_str + this.i_formatNumber(aValue_num, aOptIncludeCurrencyMark_bl, aOptForceIncludeCurrencyMark_bl, aOptDecimalPrecision_num, aOptStartValueForShortForm_num, aOptDiscardDecimalPart_bl, aOptThrowExceptionIfExtraDigitsCount_bl, aOptCutDecimalNulls_bl, aOptAmountOfAllowedDecimals_int);
	}

	i_formatInterval(aFirstValue_num, aSecondValue_num, aOptIncludeCurrencyMark_bl, aOptForceIncludeCurrencyMark_bl, aOptDecimalPrecision_num, aOptStartValueForShortForm_num, aOptDiscardDecimalPart_bl=false, aOptThrowExceptionIfExtraDigitsCount_bl=true, aOptCutDecimalNulls_bl=false, aOptAmountOfAllowedDecimals_int=0)
	{
		let lResult_str = 
							this.i_formatNumber(aFirstValue_num, aOptIncludeCurrencyMark_bl, aOptForceIncludeCurrencyMark_bl, aOptDecimalPrecision_num, aOptStartValueForShortForm_num, aOptDiscardDecimalPart_bl, aOptThrowExceptionIfExtraDigitsCount_bl, aOptCutDecimalNulls_bl, aOptAmountOfAllowedDecimals_int)
							+ ' - '
							+ this.i_formatNumber(aSecondValue_num, aOptIncludeCurrencyMark_bl, aOptForceIncludeCurrencyMark_bl, aOptDecimalPrecision_num, aOptStartValueForShortForm_num, aOptDiscardDecimalPart_bl, aOptThrowExceptionIfExtraDigitsCount_bl, aOptCutDecimalNulls_bl, aOptAmountOfAllowedDecimals_int);

		return lResult_str;
	}

	_initSupportedCurrenciesDescriptor()
	{
		let lExternalCurrenciesDescriptors_obj_arr = SUPPORTED_CURRENCIES.supported_currencies;

		CurrencyInfo._addCurrencyDescriptor(CurrencyInfo._CURRENCY_ID_DEFAULT, 2, CurrencyInfo._CURRENCY_FORMAT_VALUE_PATTERN, CurrencyInfo._DEFAULT_THOUSAND_DISCHARGE_SUFFIXES);

		for (let i = 0; i < lExternalCurrenciesDescriptors_obj_arr.length; i++)
		{
			let lExternalCurrenciesDescriptor_obj = lExternalCurrenciesDescriptors_obj_arr[i];
			let lCurrencyId_str = lExternalCurrenciesDescriptor_obj.currency_id;
			let lCurrencyDecimalPrecision_int = lExternalCurrenciesDescriptor_obj.currency_decimal_precision;
			let lCurrencyFormat_str = lExternalCurrenciesDescriptor_obj.currency_format;
			let lThousandDischargeSuffixes_obj = lExternalCurrenciesDescriptor_obj.thousand_discharge_suffixes;
			CurrencyInfo._addCurrencyDescriptor(lCurrencyId_str, lCurrencyDecimalPrecision_int, lCurrencyFormat_str, lThousandDischargeSuffixes_obj);

			let lCurrencyCompatibilityId_str = lExternalCurrenciesDescriptor_obj.currency_compatibility_id;
			if (lCurrencyCompatibilityId_str !== undefined)
			{
				CurrencyInfo._checkCurrencyId(lCurrencyCompatibilityId_str, true);

				lCurrencyCompatibilityId_str = lCurrencyCompatibilityId_str.toUpperCase(); //it has been concluded to uppercase here due to the values itself may be in foreign language
				CurrencyInfo._addCompatibleCurrencyIdMapping(lCurrencyCompatibilityId_str, lCurrencyId_str);
			}
	
			for (let j in lExternalCurrenciesDescriptor_obj)
			{
				if (
						(j === "currency_id")
						|| (j === "currency_format")
						|| (j === "currency_compatibility_id")
						|| (j === "note") //optional note is usually intended to note something directly in the descriptor (a little inefficient due to increases its size to be loaded but that is only the way currently to put some important 'in-place' comments)
						|| (j === "currency_decimal_precision")
						|| (j === "thousand_discharge_suffixes")
					)
				{
				}
				else
				{
					throw Error("Unsupported currency entity: " + j + "/" + lCurrencyId_str);
				}
			}
			
		}		

		CurrencyInfo._fSupportedCurrenciesInitialized_bl = true;
	}

	_addDischargeSuffix(aInitString_str, aStartValue_num, aDecimalPrecision_num) //default is 1K coins (not cents)
	{
		let lValue_num;
		try
		{
			lValue_num = Number(aInitString_str.replaceAll(',', ''));
		}
		catch(e)
		{
			lValue_num = Number(aInitString_str.replace(/,/g, '')); //for ie browser
		}

		let lResult_str = aInitString_str;

		if (isNaN(lValue_num))
		{
			throw Error(`The value ${aInitString_str} cannot be converted to a number.`);
		}

		if (lValue_num >= aStartValue_num)
		{
			let lLogOfTen_num = Math.min(Math.floor(Math.log10(lValue_num)/3), 3); //max discharge is B = 1000^3
			let lCorrection_num =  Math.pow(10, aDecimalPrecision_num);

			lValue_num = (lValue_num / Math.pow(1000, lLogOfTen_num)); // get short form of number (as 1.23456 from 1234.56)
			lValue_num = Math.floor(lValue_num * lCorrection_num) / lCorrection_num; // correct the precision

			let lSuffixes_obj = this._getCurrencyThousandDischargeSuffixes();

			if (lLogOfTen_num === 3)
			{
				lResult_str = lValue_num.toString() + lSuffixes_obj[3];
			}
			else if (lLogOfTen_num === 2)
			{
				lResult_str = lValue_num.toString() + lSuffixes_obj[2];
			}
			else if (lLogOfTen_num === 1)
			{
				lResult_str = lValue_num.toString() + lSuffixes_obj[1];
			}
		}
		
		return lResult_str;
	}

	_getCurrencyId()
	{		
		return this._fCurrencyId_str;
	}

	_getCurrencyDescriptor()
	{
		if (!CurrencyInfo._fSupportedCurrenciesInitialized_bl)
		{
			CurrencyInfo._initSupportedCurrenciesDescriptor();
		}

		let lCurrencyId_str = this._fCurrencyId_str;

		if (lCurrencyId_str === undefined || !CurrencyInfo._fSupportedCurrenciesDescriptor_obj[lCurrencyId_str])
		{
			lCurrencyId_str = CurrencyInfo._CURRENCY_ID_DEFAULT;
		}

		return CurrencyInfo._fSupportedCurrenciesDescriptor_obj[lCurrencyId_str];
	}

	_getCurrencyFormatPattern()
	{
		return this._getCurrencyDescriptor().i_fCurrencyFormat_str;
	}

	_getCurrencyThousandDischargeSuffixes()
	{
		return this._getCurrencyDescriptor().i_fThousandDishargeSuffixes_obj || CurrencyInfo._DEFAULT_THOUSAND_DISCHARGE_SUFFIXES;
	}

	destroy()
	{
		this._fCurrencyId_str = null;

		super.destroy();
	}
}

export default CurrencyInfo;