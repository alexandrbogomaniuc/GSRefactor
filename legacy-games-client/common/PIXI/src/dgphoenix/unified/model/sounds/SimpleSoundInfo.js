class SimpleSoundInfo
{
	//CL INTERFACE...
	static i_PLAYING_STATE_ID_INITIAL 		= -1;	//initial playing state or canceled playing state (cancelling occurs when trying to stop starting sound playing)
	static i_PLAYING_STATE_ID_STARTING 		= 0;	//occurs whan starting the sound playing from some of the final state (undefined, interrupted, completed)
	static i_PLAYING_STATE_ID_PLAYING 		= 1;	//occurs when the sound playing is really in progress (after starting or completing (e.g. looping restarting) state)
	static i_PLAYING_STATE_ID_PAUSED 		= 2;	//currently paused state is never used; this is for future purposes only
	static i_PLAYING_STATE_ID_INTERRUPTED 	= 3;	//occurs when the playing or paused sound is stopped
	static i_PLAYING_STATE_ID_COMPLETION 	= 4;	//occurs before syncronously before the real playing completion; you can prevent real completion and silently restart playing from this state (e.g. for the playing loop implementation)
	static i_PLAYING_STATE_ID_COMPLETED 	= 5;	//occurs immediately after the completion state if restart has not been applied
	//...CL INTERFACE

	//IL CONSTRUCTION...
	constructor(aSoundDescriptor_usms, aSoundName_str, aOptIsLooped_bln)
	{
		//IL IMPLEMENTATION...
		this._fSoundDescriptor_usms = null;
		this._fSoundName_str = null;
		this._fIsLooped_bln = false;
		this._fCurVolume_num = 1;
		this._fIndependentVolumeFactor_num = 1; /*to manipulate with volume without changing default or common volume*/
		this._fSoundOn_bl = true;
		this._fPlayingStateId_int = SimpleSoundInfo.i_PLAYING_STATE_ID_INITIAL;
		// this._fPlayingStateMachine_ussm = null;
		this._fWasPausedBeforeChangeVisibility_bl = false;
		this._fIsSoundEmulationMode_bl = false;
		this._fFadeMultiplier_num = 1;

		this._initUSimpleSoundInfo(aSoundDescriptor_usms, aSoundName_str, aOptIsLooped_bln);
	}
	//...IL CONSTRUCTION
	
	//IL INTERFACE...
	/**
	 * Gets sound descriptor.
	 * @returns {SoundMetrics}
	 */
	i_getSoundDescriptor()
	{
		return this._fSoundDescriptor_usms;
	}

	//SOUND PLAYING STATE...
	/**
	 * Set actual sound state.
	 * @param {number} aStateId_int 
	 */
	i_setPlayingStateId(aStateId_int)
	{
		// this._fPlayingStateMachine_ussm.i_setMachineStateId(aStateId_int);
		this._fPlayingStateId_int = aStateId_int;
	}

	/**
	 * Gets actual sound state.
	 * @returns {number}
	 */
	i_getPlayingStateId()
	{
		return this._fPlayingStateId_int;
	}

	/**
	 * Checks if current sound state is i_PLAYING_STATE_ID_INITIAL.
	 * @returns {boolean}
	 */
	i_isPlayingStateInitial()
	{
		return this._fPlayingStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_INITIAL;
	}

	/**
	 * Checks if current sound state is i_PLAYING_STATE_ID_STARTING.
	 * @returns {boolean}
	 */
	i_isPlayingStateStarting()
	{
		return this._fPlayingStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_STARTING;
	}

	/**
	 * Checks if current sound state is i_PLAYING_STATE_ID_PLAYING.
	 * @returns {boolean}
	 */
	i_isPlayingStatePlaying()
	{
		return this._fPlayingStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_PLAYING;
	}

	/**
	 * Checks if current sound state is i_PLAYING_STATE_ID_PAUSED.
	 * @returns {boolean}
	 */
	i_isPlayingStatePaused()
	{
		return this._fPlayingStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_PAUSED;
	}

	/**
	 * Checks if current sound state is i_PLAYING_STATE_ID_INTERRUPTED.
	 * @returns {boolean}
	 */
	i_isPlayingStateInterrupted()
	{
		return this._fPlayingStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_INTERRUPTED;
	}
	
	/**
	 * Checks if current sound state is i_PLAYING_STATE_ID_COMPLETION.
	 * @returns {boolean}
	 */
	i_isPlayingStateCompletion()
	{
		return this._fPlayingStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_COMPLETION;
	}

	/**
	 * Checks if current sound state is i_PLAYING_STATE_ID_COMPLETED.
	 * @returns {boolean}
	 */
	i_isPlayingStateCompleted()
	{
		return this._fPlayingStateId_int === SimpleSoundInfo.i_PLAYING_STATE_ID_COMPLETED;
	}
	//...SOUND PLAYING STATE

	/**
	 * Sound volume fade multiplier.
	 * @type {number}
	 */
	get fadeMultiplier()
	{
		return this._fFadeMultiplier_num || 0;
	}

	/**
	 * Set sound volume fade multiplier.
	 * @param {number}
	 */
	set fadeMultiplier(aFadeMultiplier_num)
	{
		this._fFadeMultiplier_num = aFadeMultiplier_num;
	}

	/**
	 * Update sound volume on/off state.
	 * @param {boolean} aSoundOn_bl - Is sound on or not.
	 */
	i_setSoundOn(aSoundOn_bl)
	{
		this._fSoundOn_bl = aSoundOn_bl;
	}

	/**
	 * Checks if sound volume is on.
	 * @returns {boolean}
	 */
	i_isSoundOn()
	{
		return this._fSoundOn_bl;
	}

	/**
	 * Sound name.
	 * @returns {string}
	 */
	i_getSoundName()
	{
		return this._fSoundName_str;
	}

	/**
	 * Update sound loop state (should sound playing be looped or not).
	 * @param {boolean} aIsLooped_bln 
	 */
	i_setLoop(aIsLooped_bln)
	{
		this._fIsLooped_bln = aIsLooped_bln;
	}
	
	/**
	 * Checks if sound playing is looped.
	 * @returns {boolean}
	 */
	i_isLoop()
	{
		return this._fIsLooped_bln;
	}
	
	/**
	 * Set sound volume.
	 * @param {number} aVolume_num 
	 */
	i_setVolume(aVolume_num)
	{
		this._fCurVolume_num = aVolume_num;
	}
	
	/**
	 * Soumd volume.
	 * @returns {number}
	 */
	i_getVolume()
	{
		return this._fCurVolume_num;
	}

	/**
	 * Set additional sound volume multiplier.
	 * @param {number} aMultiplier_num 
	 */
	i_multiplyVolume(aMultiplier_num)
	{
		this._fIndependentVolumeFactor_num = aMultiplier_num;
	}

	/**
	 * Additional sound volume multiplier value.
	 * @returns {number}
	 */
	i_getIndependentVolumeFactor()
	{
		return this._fIndependentVolumeFactor_num;
	}
	
	/**
	 * Checks is sound settings were saved before change of page visibility.
	 * @returns {boolean}
	 */
	i_getWasPausedBeforeChangeVisibility()
	{
		return this._fWasPausedBeforeChangeVisibility_bl;
	}
	
	/**
	 * Update state of sound settings: saved before change of page visibility or not.
	 * @param {boolean} aWasPausedBeforeChangeVisibility_bl 
	 */
	i_setWasPausedBeforeChangeVisibility(aWasPausedBeforeChangeVisibility_bl)
	{
		this._fWasPausedBeforeChangeVisibility_bl = aWasPausedBeforeChangeVisibility_bl;
	}

	/**
	 * Update sound emulation playing state: on or off.
	 * @param {boolean} aValue_bl 
	 */
	i_setSoundEmulationMode(aValue_bl)
	{
		this._fIsSoundEmulationMode_bl = aValue_bl;
	}

	/**
	 * Checks if sound emulation playing is on or off.
	 * @returns {boolean}
	 */
	i_isSoundEmulationMode()
	{
		return this._fIsSoundEmulationMode_bl;
	}

	/**
	 * Destroy sound instance.
	 */
	i_destroy()
	{
		this._fSoundDescriptor_usms = null;
		this._fSoundName_str = null;
		this._fIsLooped_bln = undefined;
		this._fCurVolume_num = undefined;
		this._fIndependentVolumeFactor_num = undefined;
		this._fSoundOn_bl = undefined;
		this._fPlayingStateId_int = undefined;
		this._fWasPausedBeforeChangeVisibility_bl = undefined;
		this._fIsSoundEmulationMode_bl = undefined;
		this._fFadeMultiplier_num = undefined;
	}
	//...IL INTERFACE

	//ILI INIT...
	_initUSimpleSoundInfo(aSoundDescriptor_usms, aSoundName_str, aOptIsLooped_bln)
	{
		this._fSoundDescriptor_usms = aSoundDescriptor_usms;
		this._fSoundName_str = aSoundName_str;
		this._fIsLooped_bln = !!aOptIsLooped_bln;
	}
	//...ILI INIT
}

export default SimpleSoundInfo;