// Component stylesheet
import {directive} from "../../decorators/directive";
import {TagService} from "./TagService";

interface TagClickComponentScope extends ng.IScope
{
    name: any
    chapter1: any
    chapter2: any
    chapter3: any
    level2: any
    action: any
}

@directive('TagService')
export class TagClickDirective implements ng.IDirective {

    public scope:any;
    public link:any;
    public template:string;

    constructor(private TagService: TagService) {
        this.scope = {
            name: '@tagName',
            chapter1: '@tagChapter1',
            chapter2: '@tagChapter2',
            chapter3: '@tagChapter3',
            level2: '@tagLevel2',
            action: '@tagAction'
        };
        this.link = (scope:TagClickComponentScope, element:any[]):void => {
            var obj = this._addPropertiesToObject(scope, element[0]);
            TagService.clickSend(obj);
        }
    }

    private _setDeclaredProperty = function(obj, scope, key) {
    if(typeof scope[key] !== 'undefined') {
        obj[key] = scope[key];
    }
};
    private _addPropertiesToObject = function(scope, elem){
    var obj = {
        elem: elem,
        name: scope.name,
        level2: scope.level2,
        type: scope.action
    };
    this._setDeclaredProperty(obj, scope, 'chapter1');
    this._setDeclaredProperty(obj, scope, 'chapter2');
    this._setDeclaredProperty(obj, scope, 'chapter3');
    return obj;
};
}
