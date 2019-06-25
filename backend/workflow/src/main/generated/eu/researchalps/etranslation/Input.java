
package eu.researchalps.etranslation;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="priority" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="external-reference" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="caller-information"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="application" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="institution" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="department-number" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="document-to-translate-base64" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="content" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/&gt;
 *                   &lt;element name="format" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="file-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="document-to-translate-path" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="text-to-translate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="source-language" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="target-languages"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="target-language" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="domain" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="output-format" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="with-quality-estimate" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="requester-callback" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="error-callback" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="destinations" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="email-destination" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                   &lt;element name="http-destination" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                   &lt;element name="ftp-destination" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                   &lt;element name="sftp-destination" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "priority",
    "externalReference",
    "callerInformation",
    "documentToTranslateBase64",
    "documentToTranslatePath",
    "textToTranslate",
    "sourceLanguage",
    "targetLanguages",
    "domain",
    "outputFormat",
    "withQualityEstimate",
    "requesterCallback",
    "errorCallback",
    "destinations"
})
@XmlRootElement(name = "input")
public class Input {

    @XmlElement(defaultValue = "0")
    protected Integer priority;
    @XmlElement(name = "external-reference")
    protected String externalReference;
    @XmlElement(name = "caller-information", required = true)
    protected Input.CallerInformation callerInformation;
    @XmlElement(name = "document-to-translate-base64")
    protected Input.DocumentToTranslateBase64 documentToTranslateBase64;
    @XmlElement(name = "document-to-translate-path")
    protected String documentToTranslatePath;
    @XmlElement(name = "text-to-translate")
    protected String textToTranslate;
    @XmlElement(name = "source-language", required = true)
    protected String sourceLanguage;
    @XmlElement(name = "target-languages", required = true)
    protected Input.TargetLanguages targetLanguages;
    protected String domain;
    @XmlElement(name = "output-format")
    protected String outputFormat;
    @XmlElement(name = "with-quality-estimate", defaultValue = "false")
    protected boolean withQualityEstimate;
    @XmlElement(name = "requester-callback")
    protected String requesterCallback;
    @XmlElement(name = "error-callback")
    protected String errorCallback;
    protected Input.Destinations destinations;

    /**
     * Obtient la valeur de la propriété priority.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Définit la valeur de la propriété priority.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPriority(Integer value) {
        this.priority = value;
    }

    /**
     * Obtient la valeur de la propriété externalReference.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalReference() {
        return externalReference;
    }

    /**
     * Définit la valeur de la propriété externalReference.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalReference(String value) {
        this.externalReference = value;
    }

    /**
     * Obtient la valeur de la propriété callerInformation.
     * 
     * @return
     *     possible object is
     *     {@link Input.CallerInformation }
     *     
     */
    public Input.CallerInformation getCallerInformation() {
        return callerInformation;
    }

    /**
     * Définit la valeur de la propriété callerInformation.
     * 
     * @param value
     *     allowed object is
     *     {@link Input.CallerInformation }
     *     
     */
    public void setCallerInformation(Input.CallerInformation value) {
        this.callerInformation = value;
    }

    /**
     * Obtient la valeur de la propriété documentToTranslateBase64.
     * 
     * @return
     *     possible object is
     *     {@link Input.DocumentToTranslateBase64 }
     *     
     */
    public Input.DocumentToTranslateBase64 getDocumentToTranslateBase64() {
        return documentToTranslateBase64;
    }

    /**
     * Définit la valeur de la propriété documentToTranslateBase64.
     * 
     * @param value
     *     allowed object is
     *     {@link Input.DocumentToTranslateBase64 }
     *     
     */
    public void setDocumentToTranslateBase64(Input.DocumentToTranslateBase64 value) {
        this.documentToTranslateBase64 = value;
    }

    /**
     * Obtient la valeur de la propriété documentToTranslatePath.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentToTranslatePath() {
        return documentToTranslatePath;
    }

    /**
     * Définit la valeur de la propriété documentToTranslatePath.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentToTranslatePath(String value) {
        this.documentToTranslatePath = value;
    }

    /**
     * Obtient la valeur de la propriété textToTranslate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextToTranslate() {
        return textToTranslate;
    }

    /**
     * Définit la valeur de la propriété textToTranslate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextToTranslate(String value) {
        this.textToTranslate = value;
    }

    /**
     * Obtient la valeur de la propriété sourceLanguage.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceLanguage() {
        return sourceLanguage;
    }

    /**
     * Définit la valeur de la propriété sourceLanguage.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceLanguage(String value) {
        this.sourceLanguage = value;
    }

    /**
     * Obtient la valeur de la propriété targetLanguages.
     * 
     * @return
     *     possible object is
     *     {@link Input.TargetLanguages }
     *     
     */
    public Input.TargetLanguages getTargetLanguages() {
        return targetLanguages;
    }

    /**
     * Définit la valeur de la propriété targetLanguages.
     * 
     * @param value
     *     allowed object is
     *     {@link Input.TargetLanguages }
     *     
     */
    public void setTargetLanguages(Input.TargetLanguages value) {
        this.targetLanguages = value;
    }

    /**
     * Obtient la valeur de la propriété domain.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Définit la valeur de la propriété domain.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDomain(String value) {
        this.domain = value;
    }

    /**
     * Obtient la valeur de la propriété outputFormat.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * Définit la valeur de la propriété outputFormat.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputFormat(String value) {
        this.outputFormat = value;
    }

    /**
     * Obtient la valeur de la propriété withQualityEstimate.
     * 
     */
    public boolean isWithQualityEstimate() {
        return withQualityEstimate;
    }

    /**
     * Définit la valeur de la propriété withQualityEstimate.
     * 
     */
    public void setWithQualityEstimate(boolean value) {
        this.withQualityEstimate = value;
    }

    /**
     * Obtient la valeur de la propriété requesterCallback.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequesterCallback() {
        return requesterCallback;
    }

    /**
     * Définit la valeur de la propriété requesterCallback.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequesterCallback(String value) {
        this.requesterCallback = value;
    }

    /**
     * Obtient la valeur de la propriété errorCallback.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorCallback() {
        return errorCallback;
    }

    /**
     * Définit la valeur de la propriété errorCallback.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorCallback(String value) {
        this.errorCallback = value;
    }

    /**
     * Obtient la valeur de la propriété destinations.
     * 
     * @return
     *     possible object is
     *     {@link Input.Destinations }
     *     
     */
    public Input.Destinations getDestinations() {
        return destinations;
    }

    /**
     * Définit la valeur de la propriété destinations.
     * 
     * @param value
     *     allowed object is
     *     {@link Input.Destinations }
     *     
     */
    public void setDestinations(Input.Destinations value) {
        this.destinations = value;
    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="application" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="institution" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="department-number" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "application",
        "username",
        "institution",
        "departmentNumber"
    })
    public static class CallerInformation {

        @XmlElement(required = true)
        protected String application;
        @XmlElement(required = true)
        protected String username;
        protected String institution;
        @XmlElement(name = "department-number")
        protected String departmentNumber;

        /**
         * Obtient la valeur de la propriété application.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getApplication() {
            return application;
        }

        /**
         * Définit la valeur de la propriété application.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setApplication(String value) {
            this.application = value;
        }

        /**
         * Obtient la valeur de la propriété username.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUsername() {
            return username;
        }

        /**
         * Définit la valeur de la propriété username.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUsername(String value) {
            this.username = value;
        }

        /**
         * Obtient la valeur de la propriété institution.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getInstitution() {
            return institution;
        }

        /**
         * Définit la valeur de la propriété institution.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInstitution(String value) {
            this.institution = value;
        }

        /**
         * Obtient la valeur de la propriété departmentNumber.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDepartmentNumber() {
            return departmentNumber;
        }

        /**
         * Définit la valeur de la propriété departmentNumber.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDepartmentNumber(String value) {
            this.departmentNumber = value;
        }

    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="email-destination" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
     *         &lt;element name="http-destination" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
     *         &lt;element name="ftp-destination" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
     *         &lt;element name="sftp-destination" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "emailDestination",
        "httpDestination",
        "ftpDestination",
        "sftpDestination"
    })
    public static class Destinations {

        @XmlElement(name = "email-destination")
        protected List<String> emailDestination;
        @XmlElement(name = "http-destination")
        protected List<String> httpDestination;
        @XmlElement(name = "ftp-destination")
        protected List<String> ftpDestination;
        @XmlElement(name = "sftp-destination")
        protected List<String> sftpDestination;

        /**
         * Gets the value of the emailDestination property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the emailDestination property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEmailDestination().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getEmailDestination() {
            if (emailDestination == null) {
                emailDestination = new ArrayList<String>();
            }
            return this.emailDestination;
        }

        /**
         * Gets the value of the httpDestination property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the httpDestination property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getHttpDestination().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getHttpDestination() {
            if (httpDestination == null) {
                httpDestination = new ArrayList<String>();
            }
            return this.httpDestination;
        }

        /**
         * Gets the value of the ftpDestination property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the ftpDestination property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getFtpDestination().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getFtpDestination() {
            if (ftpDestination == null) {
                ftpDestination = new ArrayList<String>();
            }
            return this.ftpDestination;
        }

        /**
         * Gets the value of the sftpDestination property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the sftpDestination property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSftpDestination().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getSftpDestination() {
            if (sftpDestination == null) {
                sftpDestination = new ArrayList<String>();
            }
            return this.sftpDestination;
        }

    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="content" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/&gt;
     *         &lt;element name="format" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="file-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "content",
        "format",
        "fileName"
    })
    public static class DocumentToTranslateBase64 {

        @XmlElement(required = true)
        protected byte[] content;
        @XmlElement(required = true)
        protected String format;
        @XmlElement(name = "file-name")
        protected String fileName;

        /**
         * Obtient la valeur de la propriété content.
         * 
         * @return
         *     possible object is
         *     byte[]
         */
        public byte[] getContent() {
            return content;
        }

        /**
         * Définit la valeur de la propriété content.
         * 
         * @param value
         *     allowed object is
         *     byte[]
         */
        public void setContent(byte[] value) {
            this.content = value;
        }

        /**
         * Obtient la valeur de la propriété format.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFormat() {
            return format;
        }

        /**
         * Définit la valeur de la propriété format.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFormat(String value) {
            this.format = value;
        }

        /**
         * Obtient la valeur de la propriété fileName.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Définit la valeur de la propriété fileName.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFileName(String value) {
            this.fileName = value;
        }

    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="target-language" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "targetLanguage"
    })
    public static class TargetLanguages {

        @XmlElement(name = "target-language")
        protected List<String> targetLanguage;

        /**
         * Gets the value of the targetLanguage property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the targetLanguage property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTargetLanguage().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getTargetLanguage() {
            if (targetLanguage == null) {
                targetLanguage = new ArrayList<String>();
            }
            return this.targetLanguage;
        }

    }

}
