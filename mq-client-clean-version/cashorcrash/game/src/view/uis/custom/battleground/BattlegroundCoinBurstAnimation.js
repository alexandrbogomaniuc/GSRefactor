import BattlegroundWinCoinBaseAnimation from './BattlegroundWinCoinBaseAnimation';

const COINS_CONFIG = [
	{
		posStart: {x: (1547 - 1547) / 2, y: (842 - 842) / 2}, posEnd: {x: (2719 - 1547) / 2, y: (438 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.53,
		rotationStart: 29, rotationEnd: 53,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 0
	},
	{
		posStart: {x: (1551 - 1547) / 2, y: (856 - 842) / 2}, posEnd: {x: (365 - 1547) / 2, y: (1068 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.4,
		rotationStart: 22, rotationEnd: 77,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 2
	},
	{
		posStart: {x: (1551 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (2727 - 1547) / 2, y: (1348 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.54,
		rotationStart: 23, rotationEnd: 95,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 1
	},
	{
		posStart: {x: (1451 - 1547) / 2, y: (858 - 842) / 2}, posEnd: {x: (547 - 1547) / 2, y: (514 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.48,
		rotationStart: 32, rotationEnd: 1,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 4
	},
	{
		posStart: {x: (1451 - 1547) / 2, y: (832 - 842) / 2}, posEnd: {x: (2669 - 1547) / 2, y: (568 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.56,
		rotationStart: 20, rotationEnd: -1,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 3
	},
	{
		posStart: {x: (1451 - 1547) / 2, y: (866 - 842) / 2}, posEnd: {x: (513 - 1547) / 2, y: (818 - 842) / 2},
		scaleStart: 0.22, scaleEnd: 0.45,
		rotationStart: -21, rotationEnd: -38,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 1
	},
	{
		posStart: {x: (1731 - 1547) / 2, y: (821 - 842) / 2}, posEnd: {x: (951 - 1547) / 2, y: (1562 - 842) / 2},
		scaleStart: 0.38, scaleEnd: 0.45,
		rotationStart: 12, rotationEnd: 20,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 3
	},
	{
		posStart: {x: (1716 - 1547) / 2, y: (820 - 842) / 2}, posEnd: {x: (2137 - 1547) / 2, y: (1556 - 842) / 2},
		scaleStart: 0.42, scaleEnd: 0.43,
		rotationStart: 13, rotationEnd: -36,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 4
	},
	{
		posStart: {x: (1731 - 1547) / 2, y: (831 - 842) / 2}, posEnd: {x: (613 - 1547) / 2, y: (1542 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.47,
		rotationStart: -16, rotationEnd: 147,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 2
	},
	{
		posStart: {x: (1744 - 1547) / 2, y: (856 - 842) / 2}, posEnd: {x: (869 - 1547) / 2, y: (236 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.46,
		rotationStart: 16, rotationEnd: -53,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 0
	},
	{
		posStart: {x: (1517 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (2413 - 1547) / 2, y: (260 - 842) / 2},
		scaleStart: 0.36, scaleEnd: 0.51,
		rotationStart: 34, rotationEnd: -29,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 3
	},
	{
		posStart: {x: (1513 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (517 - 1547) / 2, y: (1178 - 842) / 2},
		scaleStart: 0.19, scaleEnd: 0.53,
		rotationStart: -19, rotationEnd: 27,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 1
	},
	{
		posStart: {x: (1531 - 1547) / 2, y: (871 - 842) / 2}, posEnd: {x: (2475 - 1547) / 2, y: (1634 - 842) / 2},
		scaleStart: 0.4, scaleEnd: 0.55,
		rotationStart: -23, rotationEnd: 20,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 2
	},
	{
		posStart: {x: (1502 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (2673 - 1547) / 2, y: (1506 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.49,
		rotationStart: -18, rotationEnd: 147,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 3
	},
	{
		posStart: {x: (1542 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (2273 - 1547) / 2, y: (144 - 842) / 2},
		scaleStart: 0.38, scaleEnd: 0.45,
		rotationStart: 19, rotationEnd: -53,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 5
	},
	{
		posStart: {x: (1483 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (2717 - 1547) / 2, y: (838 - 842) / 2},
		scaleStart: 0.22, scaleEnd: 0.43,
		rotationStart: -16, rotationEnd: 27,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 0
	},
	{
		posStart: {x: (1485 - 1547) / 2, y: (856 - 842) / 2}, posEnd: {x: (2687 - 1547) / 2, y: (1118 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.49,
		rotationStart: -21, rotationEnd: 53,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 4
	},
	{
		posStart: {x: (1530 - 1547) / 2, y: (864 - 842) / 2}, posEnd: {x: (361 - 1547) / 2, y: (1408 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.47,
		rotationStart: 48, rotationEnd: -77,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 2
	},
	{
		posStart: {x: (1493 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (1911 - 1547) / 2, y: (1652 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.47,
		rotationStart: 14, rotationEnd: 95,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 4
	},
	{
		posStart: {x: (1533 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (1167 - 1547) / 2, y: (1710 - 842) / 2},
		scaleStart: 0.43, scaleEnd: 0.53,
		rotationStart: -16, rotationEnd: 1,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 3
	},
	{
		posStart: {x: (1591 - 1547) / 2, y: (870 - 842) / 2}, posEnd: {x: (1801 - 1547) / 2, y: (1752 - 842) / 2},
		scaleStart: 0.39, scaleEnd: 0.56,
		rotationStart: 21, rotationEnd: -1,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 2
	},
	{
		posStart: {x: (1581 - 1547) / 2, y: (846 - 842) / 2}, posEnd: {x: (1281 - 1547) / 2, y: (1686 - 842) / 2},
		scaleStart: 0.33, scaleEnd: 0.6,
		rotationStart: 27, rotationEnd: -38,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 5
	},
	{
		posStart: {x: (1609 - 1547) / 2, y: (860 - 842) / 2}, posEnd: {x: (1167 - 1547) / 2, y: (154 - 842) / 2},
		scaleStart: 0.49, scaleEnd: 0.54,
		rotationStart: -14, rotationEnd: 20,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 6
	},
	{
		posStart: {x: (1578 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (2001 - 1547) / 2, y: (152 - 842) / 2},
		scaleStart: 0.26, scaleEnd: 0.4,
		rotationStart: -13, rotationEnd: -36,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 3
	},
	{
		posStart: {x: (1581 - 1547) / 2, y: (826 - 842) / 2}, posEnd: {x: (1689 - 1547) / 2, y: (1606 - 842) / 2},
		scaleStart: 0.39, scaleEnd: 0.53,
		rotationStart: 22, rotationEnd: 147,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 6
	},
	{
		posStart: {x: (1610 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (1521 - 1547) / 2, y: (136 - 842) / 2},
		scaleStart: 0.2, scaleEnd: 0.56,
		rotationStart: -14, rotationEnd: -33,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 5
	},
	{
		posStart: {x: (1572 - 1547) / 2, y: (857 - 842) / 2}, posEnd: {x: (1865 - 1547) / 2, y: (108 - 842) / 2},
		scaleStart: 0.53, scaleEnd: 0.43,
		rotationStart: 14, rotationEnd: -6,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 3
	},
	{
		posStart: {x: (1579 - 1547) / 2, y: (819 - 842) / 2}, posEnd: {x: (489 - 1547) / 2, y: (210 - 842) / 2},
		scaleStart: 0.18, scaleEnd: 0.5,
		rotationStart: -17, rotationEnd: 46,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 5
	},
	{
		posStart: {x: (1573 - 1547) / 2, y: (840 - 842) / 2}, posEnd: {x: (2687 - 1547) / 2, y: (242 - 842) / 2},
		scaleStart: 0.38, scaleEnd: 0.49,
		rotationStart: 21, rotationEnd: 46,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 8
	},
	{
		posStart: {x: (1605 - 1547) / 2, y: (860 - 842) / 2}, posEnd: {x: (2267 - 1547) / 2, y: (1794 - 842) / 2},
		scaleStart: 0.2, scaleEnd: 0.46,
		rotationStart: 18, rotationEnd: 93,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 7
	},
	{
		posStart: {x: (1595 - 1547) / 2, y: (842 - 842) / 2}, posEnd: {x: (781 - 1547) / 2, y: (1748 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.5,
		rotationStart: -13, rotationEnd: -28,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 4
	},
	{
		posStart: {x: (1574 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (2593 - 1547) / 2, y: (1802 - 842) / 2},
		scaleStart: 0.22, scaleEnd: 0.4,
		rotationStart: 20, rotationEnd: 16,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 7
	},
	{
		posStart: {x: (1621 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (1943 - 1547) / 2, y: (1590 - 842) / 2},
		scaleStart: 0.26, scaleEnd: 0.45,
		rotationStart: 22, rotationEnd: 53,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 10
	},
	{
		posStart: {x: (1581 - 1547) / 2, y: (834 - 842) / 2}, posEnd: {x: (1113 - 1547) / 2, y: (1564 - 842) / 2},
		scaleStart: 0.25, scaleEnd: 0.38,
		rotationStart: -18, rotationEnd: -77,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 9
	},
	{
		posStart: {x: (1568 - 1547) / 2, y: (862 - 842) / 2}, posEnd: {x: (2299 - 1547) / 2, y: (1592 - 842) / 2},
		scaleStart: 0.36, scaleEnd: 0.55,
		rotationStart: -14, rotationEnd: 95,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 6
	},
	{
		posStart: {x: (1589 - 1547) / 2, y: (894 - 842) / 2}, posEnd: {x: (2737 - 1547) / 2, y: (888 - 842) / 2},
		scaleStart: 0.2, scaleEnd: 0.4,
		rotationStart: 29, rotationEnd: -1,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 7
	},
	{
		posStart: {x: (1577 - 1547) / 2, y: (829 - 842) / 2}, posEnd: {x: (711 - 1547) / 2, y: (214 - 842) / 2},
		scaleStart: 0.1, scaleEnd: 0.4,
		rotationStart: -13, rotationEnd: 20,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 9
	},
	{
		posStart: {x: (1603 - 1547) / 2, y: (868 - 842) / 2}, posEnd: {x: (2737 - 1547) / 2, y: (1174 - 842) / 2},
		scaleStart: 0.2, scaleEnd: 0.54,
		rotationStart: -12, rotationEnd: 147,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 8
	},
	{
		posStart: {x: (1589 - 1547) / 2, y: (868 - 842) / 2}, posEnd: {x: (469 - 1547) / 2, y: (1568 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.52,
		rotationStart: 27, rotationEnd: -33,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 6
	},
	{
		posStart: {x: (1564 - 1547) / 2, y: (863 - 842) / 2}, posEnd: {x: (1021 - 1547) / 2, y: (156 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.57,
		rotationStart: -7, rotationEnd: -6,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 9
	},
	{
		posStart: {x: (1603 - 1547) / 2, y: (830 - 842) / 2}, posEnd: {x: (823 - 1547) / 2, y: (1638 - 842) / 2},
		scaleStart: 0.17, scaleEnd: 0.47,
		rotationStart: 27, rotationEnd: 1,
		curvePoint: {x: 0, y: -150},
		duration: 19, delay: 2
	},
	{
		posStart: {x: (1576 - 1547) / 2, y: (863 - 842) / 2}, posEnd: {x: (405 - 1547) / 2, y: (722 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.4,
		rotationStart: 19, rotationEnd: -38,
		curvePoint: {x: 0, y: -150},
		duration: 19, delay: 2
	},
	{
		posStart: {x: (1606 - 1547) / 2, y: (863 - 842) / 2}, posEnd: {x: (2657 - 1547) / 2, y: (272 - 842) / 2},
		scaleStart: 0.2, scaleEnd: 0.35,
		rotationStart: 5, rotationEnd: -36,
		curvePoint: {x: 0, y: -150},
		duration: 19, delay: 4
	},
	{
		posStart: {x: (1591 - 1547) / 2, y: (821 - 842) / 2}, posEnd: {x: (2521 - 1547) / 2, y: (162 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.54,
		rotationStart: 29, rotationEnd: 46,
		curvePoint: {x: 0, y: -150},
		duration: 19, delay: 3
	},
	{
		posStart: {x: (1591 - 1547) / 2, y: (844 - 842) / 2}, posEnd: {x: (1447 - 1547) / 2, y: (1738 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.42,
		rotationStart: -25, rotationEnd: 46,
		curvePoint: {x: 0, y: -150},
		duration: 19, delay: 5
	},
	{
		posStart: {x: (1605 - 1547) / 2, y: (838 - 842) / 2}, posEnd: {x: (1425 - 1547) / 2, y: (1618 - 842) / 2},
		scaleStart: 0.1, scaleEnd: 0.41,
		rotationStart: 21, rotationEnd: 93,
		curvePoint: {x: 0, y: -150},
		duration: 19, delay: 4
	},
	{
		posStart: {x: (1579 - 1547) / 2, y: (813 - 842) / 2}, posEnd: {x: (2721 - 1547) / 2, y: (656 - 842) / 2},
		scaleStart: 0.2, scaleEnd: 0.49,
		rotationStart: 17, rotationEnd: -28,
		curvePoint: {x: 0, y: -150},
		duration: 19, delay: 5
	},
	{
		posStart: {x: (1589 - 1547) / 2, y: (826 - 842) / 2}, posEnd: {x: (369 - 1547) / 2, y: (582 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.47,
		rotationStart: -34, rotationEnd: 16,
		curvePoint: {x: 0, y: -150},
		duration: 19, delay: 6
	},
	{
		posStart: {x: (1585 - 1547) / 2, y: (822 - 842) / 2}, posEnd: {x: (861 - 1547) / 2, y: (1508 - 842) / 2},
		scaleStart: 0.4, scaleEnd: 0.43,
		rotationStart: -7, rotationEnd: -6,
		curvePoint: {x: 0, y: -150},
		duration: 21, delay: 7
	},
	{
		posStart: {x: (1587 - 1547) / 2, y: (818 - 842) / 2}, posEnd: {x: (2341 - 1547) / 2, y: (1600 - 842) / 2},
		scaleStart: 0.2, scaleEnd: 0.39,
		rotationStart: 17, rotationEnd: -28,
		curvePoint: {x: 0, y: -150},
		duration: 24, delay: 8
	},
	{
		posStart: {x: (1607 - 1547) / 2, y: (850 - 842) / 2}, posEnd: {x: (745 - 1547) / 2, y: (1582 - 842) / 2},
		scaleStart: 0.3, scaleEnd: 0.37,
		rotationStart: -34, rotationEnd: 16,
		curvePoint: {x: 0, y: -150},
		duration: 21, delay: 9
	}
];

class BattlegroundCoinBurstAnimation extends BattlegroundWinCoinBaseAnimation
{
	constructor()
	{
		super(COINS_CONFIG);
	}
}

export default BattlegroundCoinBurstAnimation;