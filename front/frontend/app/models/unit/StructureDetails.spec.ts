import {StructureDetails} from "../StructureDetails";

describe("StructureDetails", () => {

    let struc = new StructureDetails();

    it('should get the idenfication numbers', function () {
        struc.institutions = [
            {
                "code": {
                    "normalized": "UMR 7326"
                }
            },
            {
                "code": {
                    "normalized": "UMR 7326"
                }
            },
            {
                "code": {
                    "normalized": "UMR 7265"
                }
            },
            {
                "code": {
                    "normalized": "Dept"
                }
            },
            {
                "code": {
                    "normalized": ""
                }
            },
            {
                "code": {
                    "normalized": "32"
                }
            }
        ];
        expect(struc.getIdNumbers()).toEqual(["UMR 7326", "UMR 7265", "Dept", "32"]);
    });

    it('should return an empty array if institutions is null', function () {
        struc.institutions = null;
        expect(struc.getIdNumbers()).toEqual([]);
    });
});
