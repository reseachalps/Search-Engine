import {Filter} from "../models/Filter";
import {SearchService} from "./SearchService";

export class FiltersService {

    private log:ng.ILogService;
    private http:ng.IHttpService;
    private SearchService:SearchService;

    constructor($log, $http, SearchService){
        "ngInject";
        this.log = $log;
        this.http = $http;
        this.SearchService = SearchService;
    }

    /**
     * Returns the displayable label for a filter
     * @param filter
     * @returns {string}
     */
    public getFilterLabel(filter:Filter):string {
        switch(filter.type) {
            case 'publicEntity': {
                return this.getLabelForKind(filter);
            }
            case 'institutions': {
                filter.values = this.SearchService.getLabelsForFilter(filter.values, 'INSTITUTIONS');
                return this.getLabelDefault('Institutions', filter);
            }
            case 'urbanUnit': {
                return this.getLabelDefault('Urban units', filter);
            }
            case 'countries': {
                return this.getLabelDefault('Countries', filter);
            }
            case 'nuts': {
                filter.values = this.SearchService.getLabelsForFilter(filter.values, 'NUTS');
                return this.getLabelDefault('NUTS Region', filter);
            }
            case 'sources': {
                return this.getLabelDefault('Sources', filter);
            }
            case 'departements': {
                filter.values = this.SearchService.getLabelsForFilter(filter.values, 'DEPARTEMENTS');
                return this.getLabelDefault('Departments', filter);
            }
            case 'erc': {
                filter.values = this.SearchService.getLabelsForFilter(filter.values, 'ERC');
                return this.getLabelDefault('ERC', filter);
            }
            case 'domaine': {
                filter.values = this.SearchService.getLabelsForFilter(filter.values, 'DOMAINE');
                return this.getLabelDefault('Fields', filter);
            }
            case 'projects': {
                return 'Project : ';
            }
            case 'calls': {
                return this.getLabelDefault('Call for project', filter);
            }
            case 'badges': {
                filter.values = this.SearchService.getLabelsForFilter(filter.values, 'BADGE');
                return this.getLabelDefault('Tags', filter);
            }
            case 'ids': {
                return "Geographical selection";
            }
        }
        this.log.error('Unknown filter type, cannot display label for type : '+filter.type);
    }

    /**
     * Kind filter (private, public)
     */
    private getLabelForKind(filter:Filter):string {
        return this.kindLabelsDic[filter.values[0].toLowerCase()] + ' organizations';
    }

    private kindLabelsDic =  {
        'true': 'public',
        'false': 'private',
        'undefined': 'not available'
    };

    /**
     * Default label constructor for most of the filters
     */
    private getLabelDefault(type:string, filter:Filter): string {
        return type + ': ' + this.buildLabel(filter);
    }

    private buildLabel(filter):string {
        let values=filter.values;
        let op=(filter.op=="any")? "or":"and";
        let label = '';
        if(values.length === 1){
            label += values[0];
        } else {
            label += values.slice(0, -1).join(', ');

            if (values.length > 4) {
                let remaining = values.length - 4;
                label += "... ("+op+" " + remaining + " other";
                if (remaining > 1) {
                    label += 's';
                }
                label += ')';
            } else {
                label +=  " "+op+" "+ values[values.length - 1];
            }
        }
        return label;
    }

    getProjectName(projectId:String):any {
        return this.http.get('/api/project/'+projectId);
    }
}