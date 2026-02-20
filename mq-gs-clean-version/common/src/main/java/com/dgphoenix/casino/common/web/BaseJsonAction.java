package com.dgphoenix.casino.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.struts.Globals;
import org.apache.struts.action.*;
import org.apache.struts.util.MessageResources;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public abstract class BaseJsonAction<T extends ActionForm> extends BaseAction<T> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected ActionForward process(ActionMapping mapping, T form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(APPLICATION_JSON.toString());
        String result = validateForm(mapping, form, request);
        if (result != null) {
            LOG.error("Form parameters not valid");
            makeInvalidParameterResponse(response, result);
            return null;
        }
        processAction(mapping, form, request, response);
        return null;
    }

    protected abstract ActionForward processAction(ActionMapping mapping, T form, HttpServletRequest request,
                                                   HttpServletResponse response) throws Exception;

    private String validateForm(ActionMapping mapping, T form, HttpServletRequest request) {
        ActionErrors errors = form.validate(mapping, request);
        if (!errors.isEmpty()) {
            return formatErrors((MessageResources) request.getAttribute(Globals.MESSAGES_KEY), errors);
        } else {
            return null;
        }
    }

    private String formatErrors(MessageResources resources, ActionErrors errors) {
        StringBuilder validErrors = new StringBuilder();
        Iterator<ActionMessage> iterator = errors.get();
        while (iterator.hasNext()) {
            ActionMessage error = iterator.next();
            if (error.isResource()) {
                validErrors.append(resources.getMessage(error.getKey(), error.getValues()));
            } else {
                validErrors.append(error.getKey());
            }
            if (iterator.hasNext()) {
                validErrors.append("|");
            }
        }
        return validErrors.toString();
    }

    protected void makeInvalidParameterResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String responseBody = mapper.writeValueAsString(JsonResult.createErrorResult(errorMessage));
        response.getWriter().write(responseBody);
    }

    protected void makeOkResponse(HttpServletResponse response, String message) throws IOException {
        String responseBody = mapper.writeValueAsString(JsonResult.createSuccessResult(message));
        response.getWriter().write(responseBody);
    }
}