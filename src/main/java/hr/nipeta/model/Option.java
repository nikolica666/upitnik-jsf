package hr.nipeta.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class Option implements Serializable {
    private String value;
    private String label;
}