//Andrey Nazarov 12/23/2005
function TripleCalendar(_dayCtrl, _monthCtrl, _yearCtrl) {
	this.dayCtrl = _dayCtrl;
	this.monthCtrl = _monthCtrl;
	this.yearCtrl = _yearCtrl;
	this.currentDate = this.dayCtrl.options[this.dayCtrl.selectedIndex].text;
	var this_ = this;	

	this.monthCtrl.onChange = function() {
		this_.populate(-1);
	};

	this.monthCtrl.onchange = function() {
		this_.populate(-1);
	};

	this.dayCtrl.onfocus = function() {
//		this_.populate(-1);
	};

	this.dayCtrl.onchange = function() {
		this_.changeDate(this_.dayCtrl.selectedIndex);
	};

	this.yearCtrl.onchange = function() {
		this_.populate(-1);
	};

	this.changeDate = function(day) {
		this_.currentDate=day+1;
		this_.populate(-1);
	};

	this.populate = function(day) {
		var months = new Array(1,2,3,4,5,6,7,8,9,10,11,12);
		var timeA = new Date(this.yearCtrl.options[this_.yearCtrl.selectedIndex].text, 
			months[this_.monthCtrl.selectedIndex],1);
		var timeDifference = timeA - 79200000; //86400000;
		var timeB = new Date(timeDifference);
		var daysInMonth = timeB.getDate();
		if (day<0) {
			day=this_.currentDate;
		} else {
			this_.currentDate=day;
		}

		if (this_.currentDate > daysInMonth) {
			day=this_.currentDate=daysInMonth;
		}
		for(var i = 0; i < this_.dayCtrl.length; i++) {
			this_.dayCtrl.options[0] = null;
		}
		for (var i = 0; i < daysInMonth; i++) {
			this_.dayCtrl.options[i] = new Option(i+1);
		}
		if(day>0) { 
			this_.dayCtrl.options[day-1].selected = true;
		}
	}

    this.getDate = function() {
        var months = new Array(1,2,3,4,5,6,7,8,9,10,11,12);
        return new Date(this.yearCtrl.options[this_.yearCtrl.selectedIndex].text,
			months[this_.monthCtrl.selectedIndex], this_.dayCtrl.selectedIndex + 1);
    }
}
