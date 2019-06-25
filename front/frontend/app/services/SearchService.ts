const NUTS_LABELS = JSON.parse(require<string>('../assets/nuts-labels.json'));

export class SearchService {
    private http:ng.IHttpService;
    private log:ng.ILogService;
    private location:ng.ILocationService;
    private filter:ng.IFilterService;
    private nomenclatures: any; // nomenclatures as a map of map
    private nomenclaturesArray: any = {}; // nomenclatures as a map of arrays

    public lastSearch: any;

    constructor($http, $log, $location, $filter){
        "ngInject";
        this.http = $http;
        this.log = $log;
        this.location = $location;
        this.filter = $filter;
        this.nomenclatures = this.getNomenclature()
    }

    public search(params: any) : any {
        return this.http({
            data: params,
            method: 'POST',
            url: '/api/structures/search/'
        });
    }

    public getTagCloud(params: any) {
        return this.http({
            data: params,
            method: 'POST',
            url: '/api/structures/search/tagcloud'
        });
    }

    /**
    * return the export link
    * params: full search URL
    * request: search request
    */
    public generateExportLink(params:any, request: any):any{
        return '/api/structures/search/export?rp='+this.encode(params)+'&r='+this.encode(JSON.stringify(request));
    }

    private encode(message:any):any{
            return message ? btoa(encodeURIComponent(message)):'';
    }

    public runSearch(query:any):void {
        var q = this.serializeQuery(query);
        this.location.search(q);
    }

    /**
     * Turns a query object into a JSON string
     * @param query
     * @returns a JSONed string
     */
    private serializeQuery(query:any):any {
        var q = query;
        angular.forEach(q, function (values, key) {
            if (values && !Array.isArray(values)) {
                values = [values];
            }
            if (values) {
                values = values.map(function (el) {
                    // detect if this is a filter or a string (there is probably a better way)
                    return (angular.isDefined(el.op) || angular.isDefined(el.min) || angular.isDefined(el.max) || Array.isArray(el)) ? JSON.stringify(el) : el;
                });
            }
            q[key] = values;
        });
        return q;
    }

    /**
     * Turns a JSON string from the search params into an object
     * @param query : $location.search() params
     * @returns a query object
     */
    public deserializeQuery(query:any):any {
        var q = {};
        angular.forEach(query, function (values, key) {
            if (key != "query" && key != "page") {
                q[key] = [].concat(values || []).map(function (el) {
                    return (el.toString().charAt(0) === '{' || el.toString().charAt(0) === '[') ? JSON.parse(el) : el;
                });
            } else {
                q[key] = values;
            }
        });
        return q;
    }
    /**
     *
     * @param stateParams needs to be given in argument, because the service takes a non-updated $stateParams
     */
    public buildSearchParams = function(stateParams) {
        const search = {
            query: stateParams.query, // Text query
            page: stateParams.page,
            publicEntity: this.parseFilters(stateParams.publicEntity),
            urbanUnit: this.parseFilters(stateParams.urbanUnit),
            departements: this.parseFilters(stateParams.departements),
            erc: this.parseFilters(stateParams.erc),
            domaine: this.parseFilters(stateParams.domaine),
            projects: this.parseFilters(stateParams.projects),
            calls: this.parseFilters(stateParams.calls),
            badges: this.parseFilters(stateParams.badges),
            countries: this.parseFilters(stateParams.countries),
            institutions: this.parseFilters(stateParams.institutions),
            sources: this.parseFilters(stateParams.sources),
            ids: this.parseFilters(stateParams.ids),
            nuts: this.parseFilters(stateParams.nuts),
        };
        this.lastSearch = search;
        return search;
    };

    /**
     * Turns a json string into an object if needed
     * @param s
     * @returns {*}
     */
    private parseFilters = (s) => {
        if (s) {
            return [].concat(s).map(function (el) {
                return (el.toString().charAt(0) === '{') ? JSON.parse(el) : el;
            })[0];
        } else {
            return s;
        }
    };

    public getGeoPositions = () : ng.IHttpPromise<any> => {
        return this.http({
            data: this.lastSearch || {},
            method: 'POST',
            url: '/api/structures/search/georesults'
        });
    };

    /**
     * Warning - This is meant to be used only in a routing/resolve context. To get a label from the nomenclature, use getLabelForFilter or getLabelsForFilters
     * @returns {any}
     */
    public getNomenclature = () : ng.IPromise<any> => {
        if(this.nomenclatures){
            return this.nomenclatures;
        } else {
            return this.http.get('api/nomenclatures').then(data => {
                this.nomenclatures = data.data;
                this.nomenclatures["NUTS"] = NUTS_LABELS;
                // converting the map of map into a map of array (used for the themes filter search -- See searchTheme method)
                for(var n in this.nomenclatures){
                    this.nomenclaturesArray[n] = Object.keys(this.nomenclatures[n]).map(key => {return {key:key,label:this.nomenclatures[n][key]}})
                }
                return this.nomenclatures;
            });
        }
    };

    /**
     * Get a single label base on a group and a key
     */
    public getLabelForFilter = (group, key) : string => {
        if(!this.nomenclatures){
            console.error('Error - Nomenclatures hasn\'t been set in SearchService. Looking for : '+ group+'.'+key);
            return;
        }
        const nomenclature = this.nomenclatures[group.toUpperCase()];
        if (angular.isUndefined(nomenclature)){
            console.error('Cannot get ' + group.toUpperCase() + ' nomenclature');
            return;
        }
        return nomenclature[key];
    };

    /**
     * Will replace the label attribute of all the values by the correct label from the nomenclature
     * @param values
     * @param filterId
     */
    public getLabelsForFilter(values:any, filterId:String):string[] {
        return values.map(value => {
            if(value.hasOwnProperty('label')){
                value.label = this.getLabelForFilter(filterId.toUpperCase(), value.value);
            } else {
                value = this.getLabelForFilter(filterId.toUpperCase(), value);
            }
            return value;
        });
    }

    /**
     * Search method for the Themes filter
     */
    public searchTheme(theme: string, label: string) {
        const group = this.nomenclaturesArray[theme.toUpperCase()];
        if (angular.isUndefined(group)){
            console.error('Cannot get ' + theme.toUpperCase() + ' nomenclature');
            return;
        }
        return this.filter('filter')(group, {label:label}); // we search only on the label
    }

    public searchProject(query: string) {
        return this.http.post('/api/project/search', query).then((data) => {
            return data.data;
        });
    }

    public searchCall(query: string) {
        return this.filter('filter')(this.nomenclaturesArray.CALLS,query);
    }
}