package com.hotel.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChartSeries implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> labels = new ArrayList<>();
    private List<Number> values = new ArrayList<>();

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Number> getValues() {
        return values;
    }

    public void setValues(List<Number> values) {
        this.values = values;
    }
}
