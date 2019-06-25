
package eu.researchalps.etranslation;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.researchalps.etranslation package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _TranslateResponse_QNAME = new QName("http://cef.dgt.ec.europa.eu", "translateResponse");
    private final static QName _Translate_QNAME = new QName("http://cef.dgt.ec.europa.eu", "translate");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.researchalps.etranslation
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Input }
     * 
     */
    public Input createInput() {
        return new Input();
    }

    /**
     * Create an instance of {@link Translate }
     * 
     */
    public Translate createTranslate() {
        return new Translate();
    }

    /**
     * Create an instance of {@link Translate.Arg0 }
     * 
     */
    public Translate.Arg0 createTranslateArg0() {
        return new Translate.Arg0();
    }

    /**
     * Create an instance of {@link Input.CallerInformation }
     * 
     */
    public Input.CallerInformation createInputCallerInformation() {
        return new Input.CallerInformation();
    }

    /**
     * Create an instance of {@link Input.DocumentToTranslateBase64 }
     * 
     */
    public Input.DocumentToTranslateBase64 createInputDocumentToTranslateBase64() {
        return new Input.DocumentToTranslateBase64();
    }

    /**
     * Create an instance of {@link Input.TargetLanguages }
     * 
     */
    public Input.TargetLanguages createInputTargetLanguages() {
        return new Input.TargetLanguages();
    }

    /**
     * Create an instance of {@link Input.Destinations }
     * 
     */
    public Input.Destinations createInputDestinations() {
        return new Input.Destinations();
    }

    /**
     * Create an instance of {@link TranslateResponse }
     * 
     */
    public TranslateResponse createTranslateResponse() {
        return new TranslateResponse();
    }

    /**
     * Create an instance of {@link Translate.Arg0 .CallerInformation }
     * 
     */
    public Translate.Arg0 .CallerInformation createTranslateArg0CallerInformation() {
        return new Translate.Arg0 .CallerInformation();
    }

    /**
     * Create an instance of {@link Translate.Arg0 .DocumentToTranslateBase64 }
     * 
     */
    public Translate.Arg0 .DocumentToTranslateBase64 createTranslateArg0DocumentToTranslateBase64() {
        return new Translate.Arg0 .DocumentToTranslateBase64();
    }

    /**
     * Create an instance of {@link Translate.Arg0 .TargetLanguages }
     * 
     */
    public Translate.Arg0 .TargetLanguages createTranslateArg0TargetLanguages() {
        return new Translate.Arg0 .TargetLanguages();
    }

    /**
     * Create an instance of {@link Translate.Arg0 .Destinations }
     * 
     */
    public Translate.Arg0 .Destinations createTranslateArg0Destinations() {
        return new Translate.Arg0 .Destinations();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TranslateResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link TranslateResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://cef.dgt.ec.europa.eu", name = "translateResponse")
    public JAXBElement<TranslateResponse> createTranslateResponse(TranslateResponse value) {
        return new JAXBElement<TranslateResponse>(_TranslateResponse_QNAME, TranslateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Translate }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Translate }{@code >}
     */
    @XmlElementDecl(namespace = "http://cef.dgt.ec.europa.eu", name = "translate")
    public JAXBElement<Translate> createTranslate(Translate value) {
        return new JAXBElement<Translate>(_Translate_QNAME, Translate.class, null, value);
    }

}
