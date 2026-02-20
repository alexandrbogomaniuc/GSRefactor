package com.dgphoenix.casino.common.vault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nkurtushin on 21.03.17.
 */
public class VaultError {
    private List<String> errors = new ArrayList<String>();

    public VaultError(String... errors) {
        this.errors.addAll(Arrays.asList(errors));
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
