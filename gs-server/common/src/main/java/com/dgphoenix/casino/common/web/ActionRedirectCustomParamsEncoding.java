package com.abs.casino.common.web;

import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.struts.action.ActionRedirect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ActionRedirectCustomParamsEncoding extends ActionRedirect {

    public ActionRedirectCustomParamsEncoding(String path) {
        super(path);
    }

    @Override
    public ActionRedirect addParameter(String fieldName, Object valueObj) {
        String value = (valueObj != null) ? valueObj.toString() : "";

        if (parameterValues == null) {
            parameterValues = new HashMap();
        }

        value = StringUtils.encodeUriComponent(value);
        Object currentValue = parameterValues.get(fieldName);

        if (currentValue == null) {
            // there's no value for this param yet; add it to the map
            parameterValues.put(fieldName, value);
        } else if (currentValue instanceof String) {
            // there's already a value; let's use an array for these parameters
            String[] newValue = new String[2];

            newValue[0] = (String) currentValue;
            newValue[1] = value;
            parameterValues.put(fieldName, newValue);
        } else if (currentValue instanceof String[]) {
            // add the value to the list of existing values
            List newValues =
                    new ArrayList(Arrays.asList((Object[]) currentValue));

            newValues.add(value);
            parameterValues.put(fieldName,
                    newValues.toArray(new String[newValues.size()]));
        }
        return this;
    }
}
