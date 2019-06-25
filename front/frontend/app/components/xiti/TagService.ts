
export class TagService {

    private _tag;

    constructor(){
        //this._tag = new ATInternet.Tracker.Tag();
    }


    public getTag = function() {
        return this._tag;
    };
    public pageSend = function(obj) {
        // this._tag.page.set(obj);
        // this._tag.dispatch();
    };
    public clickSend = function(obj) {
        // this._tag.clickListener.send(obj);
    };
}