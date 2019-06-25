package eu.researchalps.db.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Number associated with a research structure from RNSR.
 * It can store, nomber of employess, ratios..
 */
public class StructureFinance {
    /**
     * number of employees  (range limits : 1-10-20-50-100) (min/max)
     *
     * @deprecated use employeeField instead
     */
    @Deprecated
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer employeesCategory;
    /**
     * enseignants-chercheurs ratio (range limits : 10-25-50)
     *
     * @deprecated use ecField instead
     */
    @Deprecated
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Double ecRatio;

    @Deprecated
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer hdr;
    /**
     * Domains ordered by number of employees (desc).
     * FR: Liste des disciplines ordonnées de manière décroissante par effectif
     */
    private List<Domain> domainRatios;
    /**
     * Paying structures (liste des établissements payeurs ordonnées de manière décroissante par effectif)
     */
    private List<PayingStructure> researchersPayroll;

    /**
     * number of employees as a complete string field to be displayed as is in the interface
     */
    private String employeesField;
    /**
     * enseignants-chercheurs ratio as a complete string field to be displayed as is in the interface
     */
    private String ecField;
    /**
     * number of hdr persons as a complete string field to be displayed as is in the interface
     */
    private String hdrField;


    public List<PayingStructure> getResearchersPayroll() {
        return researchersPayroll;
    }

    public List<Domain> getDomainRatios() {
        return domainRatios;
    }


    public String getEmployeesField() {
        if (employeesField == null) {
            return employeesCategory != null ? EmployeeRange.findRangeForValue(employeesCategory).toDisplayString() : null;
        } else
            return employeesField;
    }

    public String getEcField() {
        if (ecField == null) {
            return ecRatio != null ? EcRange.findRangeForValue(ecRatio * 100).toDisplayString() : null;
        } else
            return ecField;
    }

    public String getHdrField() {
        if (hdrField == null) {
            return hdr != null ? HdrRange.findRangeForValue(hdr).toDisplayString() : null;
        } else
            return hdrField;
    }

    @Deprecated
    public static class EmployeeRange extends Range<Integer> {
        public static final EmployeeRange[] values = {new EmployeeRange(1, 10), new EmployeeRange(11, 20),
                new EmployeeRange(21, 50), new EmployeeRange(51, 100), new EmployeeRange(101, null)};

        public EmployeeRange(Integer min, Integer max) {
            super(min, max);
        }

        public String toDisplayString() {
            if (max != null)
                return "" + min + " - " + max + " employés";
            else
                return "> " + min + " employés";
        }

        public static EmployeeRange findRangeForValue(Integer value) {
            if (value == null) return null;
            EmployeeRange result = values[0];
            for (int i = 1; i < values.length; i++) {
                EmployeeRange r = values[i];
                if (r.min <= value)
                    result = r;
                else
                    break;
            }
            return result;
        }
    }

    @Deprecated
    public static class HdrRange extends Range<Integer> {
        public static final HdrRange[] values = {new HdrRange(0, 0), new HdrRange(1, 2),
                new HdrRange(3, 5), new HdrRange(6, 10), new HdrRange(10, null)};

        public HdrRange(Integer min, Integer max) {
            super(min, max);
        }

        public String toDisplayString() {
            if (max != null)
                return "HDR : " + min + " - " + max + " personnes";
            else
                return "HDR : + de " + min + " personnes";
        }

        public static HdrRange findRangeForValue(Integer value) {
            if (value == null) return null;
            HdrRange result = values[0];
            for (int i = 1; i < values.length; i++) {
                HdrRange r = values[i];
                if (r.min <= value)
                    result = r;
                else
                    break;
            }
            return result;
        }
    }

    @Deprecated
    public static class EcRange extends Range<Integer> {
        public static final EcRange[] values = {new EcRange(0, 10), new EcRange(10, 25),
                new EcRange(25, 50), new EcRange(50, 100)};

        public EcRange(Integer min, Integer max) {
            super(min, max);
        }

        public String toDisplayString() {
            return "" + min + " - " + max + " % de chercheurs et enseignants-chercheurs";
        }

        public static EcRange findRangeForValue(Double value) {
            if (value == null) return null;
            EcRange result = values[0];
            for (int i = 1; i < values.length; i++) {
                EcRange r = values[i];
                if (r.min <= value)
                    result = r;
                else
                    break;
            }
            return result;
        }
    }

}
