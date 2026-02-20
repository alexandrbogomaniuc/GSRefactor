import EventDispatcher from '../events/EventDispatcher';
import { APP } from '../../controller/main/globals';
import SoundLoader from '../interaction/resources/loaders/SoundLoader';

class USystem extends EventDispatcher
{
    static _NON_PRODUCTION_ENVIRONMENTS_DISTINGUISHING_CAPABILITY_ENABLED = true;

    static i_URL_SCHEME_FILE = "file:";
    static i_URL_SCHEME_HTTP = "http:";
    static i_URL_SCHEME_HTTPS = "https:";

    static _LOCAL_STORAGE_SUPPORTED = (function ()
		{
			try
			{
				return Boolean(window.localStorage);
			}
			catch (aException_obj)
			{
				return false;
			}
		})();

    constructor()
    {
        super();
    }

    _getClientApplicationExVersionString()
    {
        var lClientApplicationName_str = APP.name;
        var lRet_str = (lClientApplicationName_str === undefined) ? "na" : lClientApplicationName_str;
        var lClientApplicationVersionString_str = APP.version;
        lRet_str += "/" + (lClientApplicationVersionString_str === undefined ? "na" : lClientApplicationVersionString_str);
        return lRet_str;
    }

    i_getUserAgentString()
    {
        return this._fUserAgentString_str === undefined ? (this._fUserAgentString_str = this._getUserAgentStringNt()) : this._fUserAgentString_str;
    }

    _getUserAgentStringNt()
    {
        let lRet_str = this._fUserAgentString_str = navigator.userAgent;
        return lRet_str;
    }

    _isSimulatedUserAgentSuspicion()
    {
        if(APP.profilingController.info.isVfxProfileValueLowerOrGreater)
        {
            return true;
        }
        return false;
    }

    _generateEXIFriendlyCurrentPlatformsDescriptor()
    {
        let lPlatformDescriptor_obj = {};
        lPlatformDescriptor_obj.platform_category = "browser";
        lPlatformDescriptor_obj.platform_id = window["getPlatformInfo"].os.name;
        lPlatformDescriptor_obj.platform_id = window["getPlatformInfo"].version;
        return lPlatformDescriptor_obj;
    }

    _generateEXIFriendlyRuntimeCapabilitiesDescriptor()
    {
        var lCapsDescriptor_obj = {};

        var lAudioCapsDescriptor_obj = lCapsDescriptor_obj.audio = {};
        lAudioCapsDescriptor_obj.web_audio_capable = SoundLoader._WEB_AUDIO_ENABLED;

        var lLoadingCapsDescriptor_obj = lCapsDescriptor_obj.loading = {};
        lLoadingCapsDescriptor_obj.image_cors_capable = "true"; //html5 games - lImageCorsSupported_bl = (Math.random() > 0.5);

        var lHistoryCapsDescriptor_obj = lCapsDescriptor_obj.history = {};
        lHistoryCapsDescriptor_obj.local_storage_capable = USystem._LOCAL_STORAGE_SUPPORTED;

        var lViewportCapsDescriptor_obj = lCapsDescriptor_obj.viewport = {};
        lViewportCapsDescriptor_obj.accurate_dpr_capable = (this._estimateDevicePixelsCountPerTheViewportOne() !== undefined);
        lViewportCapsDescriptor_obj.visibility_monitoring_capable = !document.hidden;

        var lInteractionCapsDescriptor_obj = lCapsDescriptor_obj.interaction = {};
        var lPointerInteractionAPIsInUse_str = "";
        (!!('ontouchstart' in window)) && (lPointerInteractionAPIsInUse_str += (lPointerInteractionAPIsInUse_str ? "&" : "") + "touch");
        (!('ontouchstart' in window)) && (lPointerInteractionAPIsInUse_str += (lPointerInteractionAPIsInUse_str ? "&" : "") + "mouse");
        window.navigator.pointerEnabled && (lPointerInteractionAPIsInUse_str += (lPointerInteractionAPIsInUse_str ? "&" : "") + "pointer");
        lInteractionCapsDescriptor_obj.api_in_use = lPointerInteractionAPIsInUse_str;

        return lCapsDescriptor_obj;
    }

    _estimateDevicePixelsCountPerTheViewportOne()
    {
        var lRet_num = window.devicePixelRatio;
        if (lRet_num === undefined)
        {
            var lDeviceDPI_num;
            var lLogicalDPI_num;
            var lWindowScreen_obj = window.screen;
            if (
                //IE10- compatible implementation
                    (lDeviceDPI_num = lWindowScreen_obj.deviceXDPI) !== undefined
                    && (lLogicalDPI_num = lWindowScreen_obj.logicalXDPI) !== undefined
                )
            {
                lRet_num = lDeviceDPI_num / lLogicalDPI_num;
            }
            else
            {
                return undefined;
            }
        }
        return lRet_num;
    }

    _generateEXIFriendlyViewportSizeDescriptor()
    {

    }

    _generateEXIFriendlyProfilesDescriptor()
    {

    }

    _generatePlatformInfoJSONStringToBeLoggedToDataBase()
    {
        var lLoggingDescriptor_obj = {};
        lLoggingDescriptor_obj.logger = this._getClientApplicationExVersionString();
        lLoggingDescriptor_obj.user_agent = {};
        lLoggingDescriptor_obj.user_agent.ua_value = this.i_getUserAgentString();
        lLoggingDescriptor_obj.user_agent.simulated_ua_suspicion = this._isSimulatedUserAgentSuspicion();
        lLoggingDescriptor_obj.determined_platforms = this._generateEXIFriendlyCurrentPlatformsDescriptor();
        lLoggingDescriptor_obj.runtime_capabilities = this._generateEXIFriendlyRuntimeCapabilitiesDescriptor();
        lLoggingDescriptor_obj.viewport_size = this._generateEXIFriendlyViewportSizeDescriptor();
        lLoggingDescriptor_obj.profiles = this._generateEXIFriendlyProfilesDescriptor();
        var lRet_str = JSON.stringify(lLoggingDescriptor_obj, undefined, "/t");
        return lRet_str;
    }

} export default USystem