function HashMap(obj)
{
    this.length = 0;
    this.items = {};

    for (let p in obj) {
        if (obj.hasOwnProperty(p)) {
            this.items[p] = obj[p];
            this.length++;
        }
    }

    this.set = function(key, value)
    {
        let previous = undefined;
        if (this.has(key)) previous = this.items[key];
        else this.length++;

        this.items[key] = value;
        return previous;
    };

    this.get = function(key)
    {
        return this.has(key) ? this.items[key] : null;
    };

    this.has = function(key)
    {
        return this.items.hasOwnProperty(key);
    };
   
    this.remove = function(key)
    {
        if (this.has(key)) {
            let previous = this.items[key];
            this.length--;
            delete this.items[key];
            return previous;
        }
        else {
            return undefined;
        }
    };

    this.keys = function()
    {
        let keys = [];
        for (let k in this.items) {
            if (this.has(k)) {
                keys.push(k);
            }
        }
        return keys;
    };

    this.values = function()
    {
        let values = [];
        for (let k in this.items) {
            if (this.has(k)) {
                values.push(this.items[k]);
            }
        }
        return values;
    };

    this.each = function(fn) {
        for (let k in this.items) {
            if (this.has(k)) {
                fn(k, this.items[k]);
            }
        }
    };

    this.clear = function()
    {
        this.items = {};
        this.length = 0;
    }
}
