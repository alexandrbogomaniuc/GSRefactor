/**
* @description Composite HitArea. Allows to specify hitArea that consists of several zones.
*/
class CompositeHitArea {
	constructor() {
		this.areas = [];
	}

	/** add new zone */
	add(area) {
		this.areas.push(area);
	}

	/** remove zone */
	remove(area) {
		var ix = this.areas.indexOf(area);
		if(ix >= 0) this.areas.splice(ix, 1);
	}

	/** remove all zones */
	clear() {
		this.areas = [];
	}

	/** checks if hitArea includes the point */
	contains(x, y) {
		for(let area of this.areas) {
			if(area.contains(x, y)) return true;
		}

		return false;
	}
}

export default CompositeHitArea;