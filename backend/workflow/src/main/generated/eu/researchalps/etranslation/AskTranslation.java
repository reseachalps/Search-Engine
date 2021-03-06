package eu.researchalps.etranslation;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.3.0
 * 2019-02-20T18:32:42.920+01:00
 * Generated source version: 3.3.0
 *
 */
@WebService(targetNamespace = "http://cef.dgt.ec.europa.eu", name = "AskTranslation")
@XmlSeeAlso({ObjectFactory.class})
public interface AskTranslation {

    @WebMethod
    @Action(input = "http://cef.dgt.ec.europa.eu/AskTranslation/translateRequest", output = "http://cef.dgt.ec.europa.eu/AskTranslation/translateResponse")
    @RequestWrapper(localName = "translate", targetNamespace = "http://cef.dgt.ec.europa.eu", className = "eu.researchalps.etranslation.Translate")
    @ResponseWrapper(localName = "translateResponse", targetNamespace = "http://cef.dgt.ec.europa.eu", className = "eu.researchalps.etranslation.TranslateResponse")
    @WebResult(name = "return", targetNamespace = "")
    public java.lang.Long translate(
        @WebParam(name = "arg0", targetNamespace = "")
                Translate.Arg0 arg0
    );
}
