/** create ending for nouns.
 * @param {number} nCount - count for noun.
 * @param {string} langId - current language id.
 * @param {string[]} args - various ending form for noun.
 * @example
 * example 1: line, lines (1 line, 2 lines).
 * example 2: линия, линии, линий (1 линия, 2 линии, 5 линий).
 */
function createEnding(nCount, langId, args)
{
	if (args == null || args.length == 0 || nCount < 0)
	{
		return "";
	}

	var sEnding = "";
	
	if (args.length === 4)
	{
		//czech
		if (nCount < 1 && nCount > 0)
		{
			return args[3];
		}
		
		if(nCount == 1)
		{
			return args[0];
		}
		
		if(nCount == 2 || nCount == 3 || nCount == 4)
		{
			return args[1];
		}
		
		return args[2];
	}
	else if (args.length === 3)
	{
		if (nCount < 1 && langId != 'ro')
		{
			return args[2];
		}

		if (langId === 'ro')
		{
			if (nCount === 1)
			{
				return args[0];
			}
			else if (2 <= nCount & nCount <= 19 || nCount === 0)
			{
				return args[1];
			}
			else
			{
				return args[2];
			}
		}

		nCount = nCount % 100;
		if (10 <= nCount && nCount <= 20 && langId != 'cz' &&  langId !='sk')
		{
			return args[2];
		}
		nCount = nCount % 10;
		switch(nCount)
		{
			case 1:
				sEnding = args[0];
				break;
			case 2:
			case 3:
			case 4:
				sEnding = args[1];
				break;
			default:	
				sEnding = args[2];
		}
	}

	else if (args.length === 2)
	{
		if (nCount == 1)
		{
			sEnding = args[0];
		}
		else
		{
			sEnding = args[1];
		}
	}
	else if (args.length === 1)
	{
		sEnding = args[0];
	}
	return sEnding;
}

export {createEnding}