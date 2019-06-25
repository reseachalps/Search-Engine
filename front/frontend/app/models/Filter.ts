/**
 * Filter model
 */

export class Filter{
    op: string; // filter operator (all, any, none)
    values: Array<string>; // the filter values (an array of objects
    type: string; // the filter type (kind, geo,..) - Optionnal

    constructor(op: string, values: Array<string>, type?:string) {
        this.op = op;
        this.values = values;
        this.type = type;
    }
}