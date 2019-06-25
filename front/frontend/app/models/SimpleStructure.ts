/**
 * Simplified structure model, used for children and parents
 */
export class SimpleStructure{
    id: string;
    kind: string;
    label: string;
    logo: string;
    acronym: Array<string>;
    isPublic: boolean;

    constructor(id?: string, kind?: string, label?: string, logo?: string, acronym?: Array<string>, isPublic?: boolean) {
        this.id = id;
        this.kind = kind;
        this.label = label;
        this.logo = logo;
        this.acronym = acronym;
    }
}