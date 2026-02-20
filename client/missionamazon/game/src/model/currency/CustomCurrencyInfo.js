import GUSCurrencyInfo from "../../../../../common/PIXI/src/dgphoenix/gunified/model/currency/GUSCurrencyInfo";
import { APP } from "../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
class CustomCurrencyInfo extends GUSCurrencyInfo
{
    constructor()
	{
		super();
	}


    i_formatNumber(aValue_num, aOptIncludeCurrencyMark_bl = true, aOptForceIncludeCurrencyMark_bl, aOptDecimalPrecision_num, aOptStartValueForShortForm_num, aOptDiscardDecimalPart_bl = false, aOptThrowExceptionIfExtraDigitsCount_bl = true, aOptCutDecimalNulls_bl = false, aOptAmountOfAllowedDecimals_int = 0) {
        const originalValue_str = super.i_formatNumber(aValue_num, aOptIncludeCurrencyMark_bl, aOptForceIncludeCurrencyMark_bl, aOptDecimalPrecision_num, aOptStartValueForShortForm_num, aOptDiscardDecimalPart_bl = false, aOptThrowExceptionIfExtraDigitsCount_bl, aOptCutDecimalNulls_bl, aOptAmountOfAllowedDecimals_int);
        const no_zero_values_str = originalValue_str.replaceAll(".00","");
        const mcTypeCurrency_bul = no_zero_values_str.indexOf("MC")>-1;
        const qcTypeCurrency_bul = no_zero_values_str.indexOf("QC")>-1;
        const dolarTypeCurrency_bul = no_zero_values_str.indexOf("$")>-1;
        const noCurrency = !mcTypeCurrency_bul && !qcTypeCurrency_bul && !dolarTypeCurrency_bul;

        if(noCurrency)
        {
            return no_zero_values_str;
        }


        let noCurrency_str = "";
        let finalValue_str = "";
        

        if(dolarTypeCurrency_bul)
        {
            noCurrency_str = no_zero_values_str.replaceAll("$","");
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            finalValue_str = "$"+noCurrency_str;
        }

        if(mcTypeCurrency_bul)
        {
            noCurrency_str = no_zero_values_str.replaceAll("MC","");
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            finalValue_str = "$"+noCurrency_str;
        }

        if(qcTypeCurrency_bul)
        {
            noCurrency_str = no_zero_values_str.replaceAll("QC","");
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            finalValue_str = noCurrency_str  + " "+ APP.freeMoneySign;
        }
        return finalValue_str;
    }

    i_formatString(aValue_str)
    {
        let originalValue_str = super.i_formatString(aValue_str); 
        const no_zero_values_str = originalValue_str.replaceAll(".00","");
        const mcTypeCurrency_bul = no_zero_values_str.indexOf("MC")>-1;
        const qcTypeCurrency_bul = no_zero_values_str.indexOf("QC")>-1;
        const dolarTypeCurrency_bul = no_zero_values_str.indexOf("$")>-1;
        const noCurrency = !mcTypeCurrency_bul && !qcTypeCurrency_bul && !dolarTypeCurrency_bul;

        if(noCurrency)
        {
            return no_zero_values_str;
        }


        let noCurrency_str = "";
        let finalValue_str = "";
        

        if(dolarTypeCurrency_bul)
        {
            noCurrency_str = no_zero_values_str.replaceAll("$","");
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            finalValue_str = "$"+noCurrency_str;
        }

        if(mcTypeCurrency_bul)
        {
            noCurrency_str = no_zero_values_str.replaceAll("MC","");
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            finalValue_str = "$"+noCurrency_str;
        }

        if(qcTypeCurrency_bul)
        {
            noCurrency_str = no_zero_values_str.replaceAll("QC","");
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            noCurrency_str = noCurrency_str.replaceAll(/ /g,'');
            finalValue_str = noCurrency_str + " "+ APP.freeMoneySign;
        }
    
        return finalValue_str;
    }
}


export default CustomCurrencyInfo;
