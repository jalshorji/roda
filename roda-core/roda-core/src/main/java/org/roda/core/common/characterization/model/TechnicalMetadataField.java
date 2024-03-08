package org.roda.core.common.characterization.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "metadata")

public class TechnicalMetadataField {

  public TechnicalMetadataField() {
    //empty constructor
  }

  @XmlAttribute(name = "name")
  private String name;

  @XmlValue
  private String value;

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
