package hr.nipeta.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class Option implements Serializable {
    private String value;
    private String label;
}