import SimpleSoundController from './SimpleSoundController';

/**
 * @class
 * @extends SimpleSoundController
 * @classdesc Class for looped music playing. Supports only 1 sound instance for looped playing.
 */
class SimpleSoundLoopController extends SimpleSoundController
{
	//CL INTERFACE...
	static i_EVENT_SOUND_PLAYING_STARTING 	= SimpleSoundController.i_EVENT_SOUND_PLAYING_STARTING;
	static i_EVENT_SOUND_PLAYING_STARTED	= SimpleSoundController.i_EVENT_SOUND_PLAYING_STARTED;
	static i_EVENT_SOUND_PLAYING_COMPLETION = SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETION;
	static i_EVENT_SOUND_PLAYING_COMPLETED 	= SimpleSoundController.i_EVENT_SOUND_PLAYING_COMPLETED;
	//...CL INTERFACE

	//IL CONSTRUCTION...
	constructor(aSoundInfo_ussi)
	{
		super(aSoundInfo_ussi);
		
		//IL IMPLEMENTATION...
		this._fVolumeValueCounter_guvc = null;
		this._fOnVolumeChangingCompleteHandler_func = null;

		this._initUSimpleSoundLoopController();
	}
	//...IL CONSTRUCTION

	//ILI INIT...
	_initUSimpleSoundLoopController()
	{
	}
	//...ILI INIT

	//SOUND PLAYING CONTROL...
	__startSoundPlaying()
	{
		let lInfo_ussi = this.__getInfo();
		if (!lInfo_ussi) return;

		if (lInfo_ussi.i_isPlayingStatePlaying())
		{
		}
		else if (lInfo_ussi.i_isPlayingStatePaused())
		{
			this.i_resumePlaying();
		}
		else
		{
			var lChannels_usschc_arr = this.__getChannels();
			var lChannel_usschc;
			if (lChannels_usschc_arr && lChannels_usschc_arr.length > 0)
			{
				lChannel_usschc = lChannels_usschc_arr[0];
			}
			
			if (!lChannel_usschc)
			{
				lChannel_usschc = this.__generateNewSoundChannelController();
			}
			this._fLastActivatedChannel_usschc = lChannel_usschc;
			lChannel_usschc.i_startPlaying();
		}
	}

	__maximumChannelsAmount()
	{
		return 1;
	}

	__disposeCurrentSoundChannel(aChannels_usschc)
	{
		// must be empty for loop sound
	}
	//...SOUND PLAYING CONTROL

	//SWITCH FROM EMULATION TO REAL PLAYING...
	__cancelEmulationMode()
	{
		let lInfo_ussi = this.__getInfo();
		if (!lInfo_ussi) return;

		let isPlaying = lInfo_ussi.i_isPlayingStatePlaying();
		let isPaused = lInfo_ussi.i_isPlayingStatePaused();

		this.i_stopPlaying();

		super.__cancelEmulationMode();

		if (isPlaying || isPaused)
		{
			this.i_startPlaying();
		}

		if (isPaused)
		{
			this.i_pausePlaying();
		}
	}
	//...SWITCH FROM EMULATION TO REAL PLAYING
}

export default SimpleSoundLoopController;