import QueueItem from './QueueItem';
import AJAXLoader from './AJAXLoader';
import { WEBAUDIO_SUPPORT, BROKEN_ANDROID, FIREFOX, IOS } from '../../../../view/layout/features';
import { APP } from '../../../main/globals';

let EXT = new Map();
EXT.set('mp3', 'audio/mpeg');
EXT.set('ogg', 'audio/ogg');
// EXT.set('m4a', 'audio/m4a');
// EXT.set('aac', 'audio/aac');
// EXT.set('wav', 'audio/wav');
EXT.set('mp4', 'audio/mp4');

let extensionCheck = new RegExp(`\\.(${Array.from(EXT.keys()).join('|')})$`); // XXX: magic!

let LEGACY_MODE = !WEBAUDIO_SUPPORT;

/**
 * Order by support and return ordered array
 * @param {String|Array} src
 * @return {Array}
 * @private
 */
function orderSource(src) {
	if (src instanceof Array) {
		// Select best matching format. This can become more complex in future
		src.sort(function (a, b) {
			// float supported extension to top
			[, a] = (extensionCheck.exec(a) || []);
			[, b] = (extensionCheck.exec(b) || []);

			if(a && b) {
				if(SoundLoader.i_PREFFERED_FORMATS.indexOf(a) == -1 || SoundLoader.i_PREFFERED_FORMATS.indexOf(b) == -1)
				{
					return 0;
				}
				return SoundLoader.i_PREFFERED_FORMATS.indexOf(a) - SoundLoader.i_PREFFERED_FORMATS.indexOf(b)
			}
			return 0;
		});
		
		if (BROKEN_ANDROID)
		{
			src.sort((a, b)=>{
				if (a.includes("high") && b.includes("low"))
				{
					return 1; // for LOW to be first
				}
				else if (b.includes("high") && a.includes("low"))
				{
					return -1; // for LOW to be first
				}
				return 0;
			});
		}
		
		return src;
	}

	return src ? [src] : [];
}
	
/**
 * @class
 * @inheritDoc
 * @extends QueueItem
 * @classdesc Sound loader
 */
class SoundLoader extends QueueItem {

	static _fPrefferedFormats_str_arr = null;
	static _fCanPlayCheckBasedAudioFormatsIdsPreferenceSequence_str_arr = [];

	static i_AUDIO_FORMAT_MP4 = 'mp4';
	static i_AUDIO_FORMAT_MP3 = 'mp3';
	static i_AUDIO_FORMAT_OGG = 'ogg';
	static i_AUDIO_FORMAT_WAV = 'wav';

	static get i_PREFFERED_FORMATS()
	{
		return SoundLoader._fPrefferedFormats_str_arr || SoundLoader.choosePrefferedFormats();
	}

	static choosePrefferedFormats()
	{
		let lPrefferableFormats_str_arr = [];
		
		try
		{
			if (SoundLoader._WEB_AUDIO_ENABLED())
			{
				this._handleCanPlayCheck(["probably"], [SoundLoader.i_AUDIO_FORMAT_WAV]);
				this._handleCanPlayCheck(["maybe"], [SoundLoader.i_AUDIO_FORMAT_WAV]);
				this._handleCanPlayCheck(["probably"], []);
				this._handleCanPlayCheck(["maybe"], []);
			}
			else //no web audio support; possibly html audio
			{
				this._handleCanPlayCheck(["maybe", "probably"], []); //allowing to prefer items for HTMLAudio from the base preferring sequence ignoring if the items are even 'maybe' only
			}
			lPrefferableFormats_str_arr = SoundLoader._fCanPlayCheckBasedAudioFormatsIdsPreferenceSequence_str_arr;
			
		}
		catch (error)
		{
			console.error(error);
		};

		// clear the unplayable formats
		let lPath_str = APP.contentPathURLsProvider.soundsPath;
		lPrefferableFormats_str_arr.forEach((el, id, ar)=>{
			let l_sl = new SoundLoader(`${lPath_str}/sample.${el}`);
			l_sl.on('error', (event)=>{
				ar.indexOf(el) > -1 && ar.splice(ar.indexOf(el), 1);
				l_sl.destructor();
			});

			l_sl.on('success', ()=>{
				l_sl.destructor();
			})
			l_sl.load(false);
		});

		return SoundLoader._fPrefferedFormats_str_arr = lPrefferableFormats_str_arr;
	}

	static _handleCanPlayCheck(aCanPlayResults_str_arr, aDeniedAudioFormats_str_arr)
	{
		if (
				SoundLoader._fCanPlayHandler_htmlae === null //comparing with null is important!
				|| !window.Audio //the Audio interface may be inaccessible in some cases, e.g. "The Media Foundation platform is disabled when the system is running in Safe Mode."; in this case nothing is going to be preferred (Web Audio is assumed to be inaccessible as well in this case)
			) 
		{
			return;
		}

		if (!SoundLoader._fCanPlayHandler_htmlae)
		{
			try
			{
				SoundLoader._fCanPlayHandler_htmlae = new Audio();
			}
			catch (aError_err)
			{
				//the above call may raise exception in at least Edge on the Windows 10 N/KN (special versions of windows without the media software pre-installed, https://stackoverflow.com/questions/47459812/javascript-new-audio-throw-not-implemented-on-window-10-n-on-ie11-and-edge); in this case nothing is going to be preferred (Web Audio is assumed to be inaccessible as well in this case)
				//there was also registed issue like "The Media Foundation platform is disabled when the system is running in Safe Mode."
				SoundLoader._fCanPlayHandler_htmlae = null;
				return;
			}
		}
		var l_htmlae = SoundLoader._fCanPlayHandler_htmlae; 

		var lAudioFormats_str_arr = SoundLoader._getSoundFormat()
		
		for (var i = 0; i < lAudioFormats_str_arr.length; i++)
		{
			var lAudioFormatId_str = lAudioFormats_str_arr[i];
			if (
					aCanPlayResults_str_arr.indexOf(l_htmlae.canPlayType('audio/' + lAudioFormatId_str)) >= 0
					&& aDeniedAudioFormats_str_arr.indexOf(lAudioFormatId_str) < 0 //remove any format, for example wav
					&& SoundLoader._fCanPlayCheckBasedAudioFormatsIdsPreferenceSequence_str_arr.indexOf(lAudioFormatId_str) < 0
				)
			{
				SoundLoader._fCanPlayCheckBasedAudioFormatsIdsPreferenceSequence_str_arr.push(lAudioFormatId_str);
			}
		}
	}

	static _getSoundFormat()
	{
		if (SoundLoader._WEB_AUDIO_ENABLED())
		{
			if (FIREFOX)
			{
				return [
							SoundLoader.i_AUDIO_FORMAT_OGG, //The Ogg Vorbis format is top preferred for Firefox as more friendly according to https://developer.mozilla.org/en-US/docs/Web/HTML/Supported_media_formats (furthermore, on Android the MP4 and MP3 may have unacceptable long decoding time, tested on Android 4.4.2/Firefox 55).
							SoundLoader.i_AUDIO_FORMAT_MP4,
							SoundLoader.i_AUDIO_FORMAT_MP3
						];
			}
			else
			{
				return [
							SoundLoader.i_AUDIO_FORMAT_MP4,
							SoundLoader.i_AUDIO_FORMAT_OGG,
							SoundLoader.i_AUDIO_FORMAT_MP3
						];
			}
		}
		else //no web audio support
		{
			return [
						SoundLoader.i_AUDIO_FORMAT_MP3, //seems mp3 is a little more friendly for entire HTMLAudio buffering (not sure it is definetely true, the note is empiric by analizing HTMLAudio::buffered property for different audio formats in different browsers)
						SoundLoader.i_AUDIO_FORMAT_OGG,
						SoundLoader.i_AUDIO_FORMAT_MP4
					];
		}
	}

	static _WEB_AUDIO_ENABLED()
	{
		return Boolean(SoundLoader._getNativeWebAudioContext(true));
	}

	static _getNativeWebAudioContext(aOptAllowNull_bl)
	{
		if (SoundLoader._fNativeWebAudioContext_ac)
		{
			return SoundLoader._fNativeWebAudioContext_ac;
		}
		else if (SoundLoader._fNativeWebAudioContext_ac === null)
		{
			if (aOptAllowNull_bl)
			{
				return null;
			}
		}
		
		var lAudioContext_ac = null;
		var lSuccess_bl = false;
		try
		{
			//return null; //uncomment to disable Web Audio deeply

			var lNativeWebAudioContextClass_func = SoundLoader._getNativeWebAudioContextClass(true);
			if (!lNativeWebAudioContextClass_func)
			{
				return null; //the finally block below will finish the work
			}
			
			var lAudioContext_ac;
			try
			{
				//Chrome may raise "Failed to construct 'AudioContext': The number of hardware contexts provided (6) is greater than or equal to the maximum bound (6)." (https://developer.mozilla.org/en-US/docs/Web/API/AudioContext/AudioContext) when the game is launched as nested content and external content instantiates the audio contexts as well - it has been concluded to do not halt entire application but to handle softly instead (the issue seems not simply reproducible in Desktop Chrome 69)
				lAudioContext_ac = new lNativeWebAudioContextClass_func();
				if (!lAudioContext_ac)
				{
					//some remote loogs from MacOS seem points the returned value may be null
					return null;
				}
			}
			catch (aError_obj)
			{
				return null; //the finally block below will finish the work
			}

			var lAudioBuffer_ab;
			if (IOS) // sometimes the audio is distorted on iOS. It seems the known bug, when audio context is created with wrong sample rate (http://www.html5gamedevs.com/topic/19156-ios-92-sound-broken/). There is the possible solution applied. (https://github.com/Jam3/ios-safe-audio-context/blob/master/index.js).
			{
				if (!lAudioContext_ac.createBuffer)
				{
					return null; //the finally block below will finish the work
				}
				lAudioBuffer_ab = lAudioContext_ac.createBuffer(1, 1, 44100); //a default sample rate value for audio buffer. It is needed to force next audio context creation to be with right default system sample rate value.
			}

			if (!lAudioContext_ac.createBufferSource)
			{
				return null; //the finally block below will finish the work
			}
			var lAudioBufferSourceNode_absn = lAudioContext_ac.createBufferSource();
			if (
					!lAudioBufferSourceNode_absn //according to LIVE logs it might actually be undefined (Chrome 86, FF65, MacOS Safari 14 - not sure what actually causes that to happen in modern enough browsers)
					|| !lAudioBufferSourceNode_absn.start //earlier draft version has no method, e.g. in iOS6
				) 
			{
				return null; //the finally block below will finish the work
			}

			if (IOS)
			{
				lAudioBufferSourceNode_absn.buffer = lAudioBuffer_ab;
				if (!lAudioBufferSourceNode_absn.connect)
				{
					return null; //the finally block below will finish the work
				}
				lAudioBufferSourceNode_absn.connect(lAudioContext_ac.destination);
				lAudioBufferSourceNode_absn.start(0);
				if (!lAudioBufferSourceNode_absn.disconnect)
				{
					return null; //the finally block below will finish the work
				}
				lAudioBufferSourceNode_absn.disconnect();
				if (lAudioContext_ac.close) // in old WebAudio API draft standard there is no close method of audio context (example iPad4 - iOS 7.0.4) - https://www.w3.org/TR/2013/WD-webaudio-20131010/.
				{
					lAudioContext_ac.close();
					lAudioContext_ac = null;
				}

				//seems there is no reason to handle in try...catch like the above instantiation of the audio context due to iOS is not expected to be affected to the Chrome browser limitation
				lAudioContext_ac = new lNativeWebAudioContextClass_func(); //re-instantiation is required for the iOS hack to work properly
			}

			lSuccess_bl = true;
		}
		finally
		{
			if (!lSuccess_bl)
			{
				if (lAudioContext_ac)
				{
					lAudioContext_ac.close && lAudioContext_ac.close();
					lAudioContext_ac = null;
				}
			}

			SoundLoader._fNativeWebAudioContext_ac = lAudioContext_ac; //may be null here
		}

		return lAudioContext_ac;
	}

	static _getNativeWebAudioContextClass(aOptNullAllowed_bl)
	{
			//uncomment the below item to simulate no support of WebAudio
			// return undefined;

			return window.AudioContext || window.webkitAudioContext || null;
		}

	constructor(src, context = null) {
		super(src);
		this.context = context;
	}

	destructor() {
		super.destructor();
		this.context = null;
	}

	prepareData(src) {
		this._sources = orderSource(src);
		
		if (LEGACY_MODE) {
			let item = document.createElement('audio');
			item.autoplay = false;
			item.controls = false;

			if (FIREFOX)
			{
				item.preload = "none";
				item.type = "application/ogg";
			}
			else
			{
				item.preload = "auto";
			}

			item.src = this._sources[0];

			for (let url of this._sources) { // let browser select better source
				let s = item.appendChild(document.createElement('source'));
				s.src = url;
				if (FIREFOX)
				{
					s.type = "application/ogg";
				}
			}
			return item;
		}
	}

	prepareKey(src) {
		return orderSource(src);
	}

	cached() {
		// get by any of alternate URLs
		let item = null;
		for (let src of this._sources) {
			item = super.cached(src);
			if (item) {
				return item;
			}
		}
	}

	cache() {
		// cache each of alternate URLs
		let item = this.data;
		for (let src of this._sources) {
			super.cache(src);
		}
	}

	_legacyLoad(cache) {
		let self = this, fn = (e) => {
			if (!self.complete) {
				self.data.removeEventListener('canplay', fn);
				self.data.removeEventListener('canplaythrough', fn);
				self.data.removeEventListener('error', fn);
				self.data.removeEventListener('stalled', fn);
				if (e.type == 'error' || e.type == 'stalled') {
					self._status = 'error';
					self._statusMessage = 'Error loading audio';
				}
				self.completeLoad(cache);
			}
		};

		this.data.addEventListener('canplay', fn);
		this.data.addEventListener('canplaythrough', fn);
		this.data.addEventListener('error', fn);
		this.data.addEventListener('stalled', fn);
		this.data.load();
	}

	load(cache = true) 
	{
		super.load(cache);
		if (!this.complete) 
		{
			if (this._sources.length) 
			{
				let src = this._sources.shift();
				let self = this;
				
				createjs.Sound.on("fileload", (e) => {
					if (self.name != e.id) return;
					self.key = e.src;
					self._sources = [e.src];
					self._statusMessage = 'OK';
					self.completeLoad(cache);
				});

				
				createjs.Sound.on("fileerror", (e) => {
					if (self.name != e.id) return;
					self._status = 'error';
					this.completeLoad(true);
				});
				
				let absoluteUrl = this.generateAbsoluteURL(src);
				let loader = createjs.Sound.registerSound(absoluteUrl, this.name);
				
				if (loader === true && createjs.Sound.loadComplete(absoluteUrl))
				{
					self.key = absoluteUrl;
					self._sources = [absoluteUrl];
					self._statusMessage = 'OK';
					self.completeLoad(cache);
					return;
				}
				
			}
			else {
				// not completed, no more sources
				this._status = 'error';
				this._statusMessage = 'All provided sources are invalid!';
				this.completeLoad(cache);
			}
		}
	}
}

export default SoundLoader;