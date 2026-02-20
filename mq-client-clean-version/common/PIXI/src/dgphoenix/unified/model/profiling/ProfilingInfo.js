import SimpleInfo from '../base/SimpleInfo';

/**
 * Base info to specify application profiles.
 * @class
 * @extends SimpleInfo
 */
class ProfilingInfo extends SimpleInfo {

	//VFX...
	/**
	 * VFX profile property name.
	 * @static
	 */
	static get i_VFX_LEVEL_PROFILE () { return 'vfx_level_profile'; }
	/**
	 * VFX supported values.
	 * @static
	 */
	static get i_VFX_LEVEL_PROFILE_VALUES () {
		return {
			LOW: 	'low',
			LOWER: 	'lower',
			MEDIUM: 'medium',
			HIGH: 	'high'
		}
	};
	//...VFX

	//CPU...
	/**
	 * CPU profile property name.
	 * @static
	 */
	static get i_CPU_PERFORMANCE_PROFILE () { return 'cpu_performance_profile'; }
	/**
	 * CPU supported values.
	 * @static
	 */
	static get i_CPU_PERFORMANCE_PROFILE_VALUES() {
		return {
			VERY_LOW: 	'very_low',
			LOW: 		'low',
			LOWER: 		'lower',
			MEDIUM: 	'medium',
			HIGHER: 	'higher',
			HIGH: 		'high'
		}
	}
	//...CPU

	//ALL PROFILES...
	/**
	 * Supported profiles.
	 * @static
	 */
	static get i_PROFILES_POSSIBLE_VALUES () {
		return {
			[ProfilingInfo.i_VFX_LEVEL_PROFILE]: 		ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES,
			[ProfilingInfo.i_CPU_PERFORMANCE_PROFILE]: 	ProfilingInfo.i_CPU_PERFORMANCE_PROFILE_VALUES
		}
	};

	/**
	 * Weights of supported profiles.
	 * @static
	 */
	static get i_PROFILES_VALUES_WEIGHTS () {
		return {
				[ProfilingInfo.i_VFX_LEVEL_PROFILE]: {
					[ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_VFX_LEVEL_PROFILE].LOW]: 		0,
					[ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_VFX_LEVEL_PROFILE].LOWER]: 	1,
					[ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_VFX_LEVEL_PROFILE].MEDIUM]: 	2,
					[ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_VFX_LEVEL_PROFILE].HIGH]: 	3
				},
				[ProfilingInfo.i_CPU_PERFORMANCE_PROFILE]: {
					[ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_CPU_PERFORMANCE_PROFILE].VERY_LOW]: 	0,
					[ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_CPU_PERFORMANCE_PROFILE].LOW]: 		1,
					[ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_CPU_PERFORMANCE_PROFILE].LOWER]: 		2,
					[ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_CPU_PERFORMANCE_PROFILE].MEDIUM]: 	3,
					[ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_CPU_PERFORMANCE_PROFILE].HIGHER]: 	4,
					[ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_CPU_PERFORMANCE_PROFILE].HIGH]: 		5,
				}
			}
	}

	/**
	 * Gets weight of profile property value.
	 * @param {string} aProfileId_str - Profile property name.
	 * @param {string} aProfileValue_str - Profile value.
	 * @returns {number}
	 * @static 
	 */
	static i_getProfileValueWeight (aProfileId_str, aProfileValue_str)
	{
		return ProfilingInfo.i_PROFILES_VALUES_WEIGHTS[aProfileId_str] ? ProfilingInfo.i_PROFILES_VALUES_WEIGHTS[aProfileId_str][aProfileValue_str] : undefined;
	}

	/**
	 * Gets value of profile property by weight.
	 * @param {string} aProfileId_str 
	 * @param {number} aProfileWeight_num 
	 * @returns {string}
	 * @static
	 */
	static i_getProfileValueByWeight (aProfileId_str, aProfileWeight_num)
	{
		let lProfileValue_str = undefined;
		let profileWeights = ProfilingInfo.i_PROFILES_VALUES_WEIGHTS[aProfileId_str];
		
		if (profileWeights)
		{
			for (let profileValue in profileWeights)
			{
				if (profileWeights[profileValue] === aProfileWeight_num)
				{
					return profileValue;
				}
			}
		}

		return lProfileValue_str;
	}
	//...ALL PROFILES

	constructor() 
	{
		super();

		this._fProfilesObj_obj = null;
	}

	set profiles(aProfilesObj_obj)
	{
		this._fProfilesObj_obj = aProfilesObj_obj;
	}

	/**
	 * Application profiles instance.
	 * @type {Object}
	 */
	get profiles()
	{
		return this._fProfilesObj_obj;
	}

	/**
	 * Indicates whether profiles are defined or not.
	 * @type {boolean}
	 */
	get isProfilesDefined()
	{
		return !!this._fProfilesObj_obj;
	}

	/**
	 * Gets profile value.
	 * @param {string} aProfileId_str
	 * @param {boolean} aIsDynamicProfileValueRequired_bl 
	 * @returns {string}
	 */
	getProfileValue(aProfileId_str, aIsDynamicProfileValueRequired_bl=false)
	{
		let lProfileValue_str = this._fProfilesObj_obj ? this._fProfilesObj_obj[aProfileId_str] : undefined;

		return lProfileValue_str
	}

	//FUNCTIONS...
	/**
	 * Checks if current profile value is greater or equal to compared value.
	 * @param {string} aProfileId_str 
	 * @param {string} aProfileValueToCompare_str 
	 * @param {boolean} [aIsDynamicProfileValueRequired_bl=false] 
	 * @returns {boolean}
	 */
	i_isProfileValueGreaterThanOrEqualTo (aProfileId_str, aProfileValueToCompare_str, aIsDynamicProfileValueRequired_bl = false)
	{
		if (!aProfileValueToCompare_str)
		{
			throw new Error('Unknown profile value to compate: ' + aProfileValueToCompare_str);
		}
		let lProfileCurrentValue_str = this.getProfileValue(aProfileId_str, aIsDynamicProfileValueRequired_bl);
		let lProfileValueWeight_int = ProfilingInfo.i_getProfileValueWeight(aProfileId_str, lProfileCurrentValue_str);

		let lComparableWeight_int = ProfilingInfo.i_getProfileValueWeight(aProfileId_str, aProfileValueToCompare_str);
		return lProfileValueWeight_int >= lComparableWeight_int;
	}

	/**
	 * Checks if current profile value is greater then compared value.
	 * @param {string} aProfileId_str 
	 * @param {string} aProfileValueToCompare_str 
	 * @param {boolean} [aIsDynamicProfileValueRequired_bl=false] 
	 * @returns {boolean}
	 */
	i_isProfileValueGreaterThan (aProfileId_str, aProfileValueToCompare_str, aIsDynamicProfileValueRequired_bl=false)
	{
		if (!aProfileValueToCompare_str)
		{
			throw new Error('Unknown profile value to compate: ' + aProfileValueToCompare_str);
		}
		let lProfileCurrentValue_str = this.getProfileValue(aProfileId_str, aIsDynamicProfileValueRequired_bl);
		let lProfileValueWeight_int = ProfilingInfo.i_getProfileValueWeight(aProfileId_str, lProfileCurrentValue_str);

		let lComparableWeight_int = ProfilingInfo.i_getProfileValueWeight(aProfileId_str, aProfileValueToCompare_str);
		return lProfileValueWeight_int > lComparableWeight_int;
	}

	/**
	 * Checks if current profile value is less or equal to compared value.
	 * @param {string} aProfileId_str 
	 * @param {string} aProfileValueToCompare_str 
	 * @param {boolean} [aIsDynamicProfileValueRequired_bl=false] 
	 * @returns {boolean}
	 */
	i_isProfileValueLessThanOrEqualTo (aProfileId_str, aProfileValueToCompare_str, aIsDynamicProfileValueRequired_bl=false)
	{
		if (!aProfileValueToCompare_str)
		{
			throw new Error('Unknown profile value to compate: ' + aProfileValueToCompare_str);
		}
		let lProfileCurrentValue_str = this.getProfileValue(aProfileId_str, aIsDynamicProfileValueRequired_bl);
		let lProfileValueWeight_int = ProfilingInfo.i_getProfileValueWeight(aProfileId_str, lProfileCurrentValue_str);

		let lComparableWeight_int = ProfilingInfo.i_getProfileValueWeight(aProfileId_str, aProfileValueToCompare_str);

		return lProfileValueWeight_int <= lComparableWeight_int;
	}

	/**
	 * Checks if current profile value is less then compared value.
	 * @param {string} aProfileId_str 
	 * @param {string} aProfileValueToCompare_str 
	 * @param {boolean} [aIsDynamicProfileValueRequired_bl=false] 
	 * @returns {boolean}
	 */
	i_isProfileValueLessThan (aProfileId_str, aProfileValueToCompare_str, aIsDynamicProfileValueRequired_bl=false)
	{
		if (!aProfileValueToCompare_str)
		{
			throw new Error('Unknown profile value to compate: ' + aProfileValueToCompare_str);
		}
		let lProfileCurrentValue_str = this.getProfileValue(aProfileId_str, aIsDynamicProfileValueRequired_bl);
		let lProfileValueWeight_int = ProfilingInfo.i_getProfileValueWeight(aProfileId_str, lProfileCurrentValue_str);

		let lComparableWeight_int = ProfilingInfo.i_getProfileValueWeight(aProfileId_str, aProfileValueToCompare_str);
		return lProfileValueWeight_int < lComparableWeight_int;
	}
	//...FUNCTIONS

	//VFX...
	/**
	 * Dynamic VFX profile value. Currently this property is the same as just vfx profile value.
	 * @type {string}
	 */
	get vfxDynamicProfileValue()
	{
		return this.getProfileValue(ProfilingInfo.i_VFX_LEVEL_PROFILE, true);
	}

	/**
	 * VFX profile value.
	 * @type {string}
	 */
	get vfxProfileValue()
	{
		return this.getProfileValue(ProfilingInfo.i_VFX_LEVEL_PROFILE);
	}

	/**
	 * Checks if VFX profile value is medium or greater.
	 * @type {boolean}
	 */
	get isVfxProfileValueMediumOrGreater()
	{
		let lProfileName_str = ProfilingInfo.i_VFX_LEVEL_PROFILE;
		let lComparingProfileValue_str = ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_VFX_LEVEL_PROFILE].MEDIUM;
		return this.i_isProfileValueGreaterThanOrEqualTo(lProfileName_str, lComparingProfileValue_str);
	}

	/**
	 * Checks if VFX profile value is lower or greater.
	 * @type {boolean}
	 */
	get isVfxProfileValueLowerOrGreater()
	{
		let lProfileName_str = ProfilingInfo.i_VFX_LEVEL_PROFILE;
		let lComparingProfileValue_str = ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_VFX_LEVEL_PROFILE].LOWER;
		return this.i_isProfileValueGreaterThanOrEqualTo(lProfileName_str, lComparingProfileValue_str);
	}

	/**
	 * Checks if VFX profile value is medium or greater.
	 * @type {boolean}
	 */
	get isVfxDynamicProfileValueMediumOrGreater()
	{
		let lProfileName_str = ProfilingInfo.i_VFX_LEVEL_PROFILE;
		let lComparingProfileValue_str = ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_VFX_LEVEL_PROFILE].MEDIUM;
		return this.i_isProfileValueGreaterThanOrEqualTo(lProfileName_str, lComparingProfileValue_str, true);
	}

	/**
	 * Checks if VFX profile value is less then compared value.
	 * @type {boolean}
	 */
	isVfxProfileValueLessThan(aProfileValue_str, aIsDynamicProfileValueRequired_bl=false)
	{
		let lComparableWeight_int = ProfilingInfo.i_getProfileValueWeight(ProfilingInfo.i_VFX_LEVEL_PROFILE, aProfileValue_str);
		if (lComparableWeight_int === undefined)
		{
			throw new Error('Undefined VFX profile to compare: ' + aProfileValue_str);
		}
		return this.i_isProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE, aProfileValue_str, aIsDynamicProfileValueRequired_bl);
	}
	//...VFX

	//CPU...
	/**
	 * CPU profile value.
	 * @type {string}
	 */
	get cpuProfileValue()
	{
		return this.getProfileValue(ProfilingInfo.i_CPU_PERFORMANCE_PROFILE);
	}

	/**
	 * Checks if CPU profile value is medium or greater then compared value.
	 * @type {boolean}
	 */
	get isCpuPerformanceProfileValueMediumOrGreater()
	{
		let lProfileName_str = ProfilingInfo.i_CPU_PERFORMANCE_PROFILE;
		let lComparingProfileValue_str = ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_CPU_PERFORMANCE_PROFILE].MEDIUM;
		return this.i_isProfileValueGreaterThanOrEqualTo(lProfileName_str, lComparingProfileValue_str);
	}

	/**
	 * Checks if CPU profile value is low or less.
	 * @type {boolean}
	 */
	get isCpuPerformanceProfileValueLowOrLess()
	{
		let lProfileName_str = ProfilingInfo.i_CPU_PERFORMANCE_PROFILE;
		let lComparingProfileValue_str = ProfilingInfo.i_PROFILES_POSSIBLE_VALUES[ProfilingInfo.i_CPU_PERFORMANCE_PROFILE].LOW;
		return this.i_isProfileValueLessThanOrEqualTo(lProfileName_str, lComparingProfileValue_str);
	}
	//...CPU

}

export default ProfilingInfo