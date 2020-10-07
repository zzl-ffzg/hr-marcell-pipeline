//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.12.15 at 04:25:43 PM CET 
//


package xlike_hr.model.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sentence complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sentence">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="tokens" type="{}tokenList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sentence", propOrder = {
    "text",
    "tokens"
})
public class Sentence {

    @XmlElement(required = true)
    protected String text;
    protected TokenList tokens;
    @XmlAttribute(name = "id")
    protected String id;

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the tokens property.
     * 
     * @return
     *     possible object is
     *     {@link TokenList }
     *     
     */
    public TokenList getTokens() {
        return tokens;
    }

    /**
     * Sets the value of the tokens property.
     * 
     * @param value
     *     allowed object is
     *     {@link TokenList }
     *     
     */
    public void setTokens(TokenList value) {
        this.tokens = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
