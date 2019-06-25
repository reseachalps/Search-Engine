// Component stylesheet
import {directive} from "../../decorators/directive";
import {TagService} from "./TagService";

interface TagPageComponentScope extends ng.IScope
{
    tagData: any
}

@directive('TagService')
export class TagPageDirective implements ng.IDirective {

    public scope:any;
    public link:any;
    public template:string;

    constructor(private TagService: TagService) {
        this.scope = {
            tagData : "="
        };
        this.link = (scope:TagPageComponentScope):void => {
            TagService.pageSend(scope.tagData);
        }
    }
}
