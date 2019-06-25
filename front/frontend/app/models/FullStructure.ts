import {SimpleStructure} from "./SimpleStructure";
import {StructureDetails} from "./StructureDetails";

/**
 * Complete Structure model
 */
export class FullStructure{
    id: string;
    structure: StructureDetails;
    children: Array<SimpleStructure>;
    parents: Array<SimpleStructure>;
    websites: any;
    projects: Array<any>;

    constructor(id?: string, structure?: StructureDetails, children?: Array<SimpleStructure>, parents?: Array<SimpleStructure>, websites?: any, projects?: Array<any>) {
        this.id = id;
        this.structure = structure ? new StructureDetails(structure.acronyms ,structure.activities ,structure.address ,structure.companyType ,structure.createdDate ,structure.creationYear ,structure.finance ,structure.history ,structure.id ,structure.institutions ,structure.kind ,structure.label ,structure.lastUpdated ,structure.leaders ,structure.level ,structure.links ,structure.logo ,structure.nature ,structure.parent ,structure.relations ,structure.spinoffs ,structure.tags ,structure.type) : undefined;
        this.children = children;
        this.parents = parents || [];
        this.websites = websites;
        this.projects = projects;
    }

    /**
     * Check whether the structure is public or not
     * @returns {boolean}
     */
    isRNSR(): boolean{
        return this.structure.kind === 'RNSR';
    }

    /**
     * Will get the structure labels from its institutions
     * @returns {Array} of ids (UMR XXX, UMR YYY,...)
     */
    getIdNumbers(): Array<any> {
        return this.structure.getIdNumbers();
    }
}