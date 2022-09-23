package it.plansoft.depot.operation;

import lombok.Value;

import java.util.List;

@Value
public class OperationValue {
    private String refType;
    private String refKey;
    private String refId;
    private List<OperationDetailValue> details;
}
