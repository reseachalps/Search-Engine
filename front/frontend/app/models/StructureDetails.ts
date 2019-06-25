export class StructureDetails{

    public acronyms: any;
    public activities: any[];
    public address: any;
    public companyType: any;
    public createdDate: any;
    public creationYear: number;
    public finance: any;
    public history: any[];
    public id: any;
    public institutions: any[];
    public kind: string;
    public label: string;
    public lastUpdated: Date;
    public leaders: any[];
    public level: number;
    public links: any[];
    public logo: string;
    public nature: string;
    public parent: any[];
    public relations: any[];
    public spinoffs: any;
    public tags: any;
    public type: any;

    constructor(acronyms?:any, activities?:any[], address?:any, companyType?:any, createdDate?:any, creationYear?:number, finance?:any, history?:any[], id?:any, institutions?:any[], kind?:string, label?:string, lastUpdated?:Date, leaders?:any[], level?:number, links?:any[], logo?:string, nature?:string, parent?:any[], relations?:any[], spinoffs?:any, tags?:any, type?:any) {
        this.acronyms = acronyms;
        this.activities = activities;
        this.address = address;
        this.companyType = companyType;
        this.createdDate = createdDate;
        this.creationYear = creationYear;
        this.finance = finance;
        this.history = history;
        this.id = id;
        this.institutions = institutions;
        this.kind = kind;
        this.label = label;
        this.lastUpdated = lastUpdated;
        this.leaders = leaders;
        this.level = level;
        this.links = links;
        this.logo = logo;
        this.nature = nature;
        this.parent = parent;
        this.relations = relations;
        this.spinoffs = spinoffs;
        this.tags = tags;
        this.type = type;
    }

    /**
     * Will get the structure labels from its institutions
     * @returns {Array} of ids (UMR XXX, UMR YYY,...)
     */
    getIdNumbers(): Array<string> {
        let codes = this.institutions ? this.institutions
            .map(inst => (inst.code && inst.code.normalized) || "")
            .filter(x => x.length > 0) : [];
        return [...new Set(codes)]; // guaranty uniqueness
    }
}