/**
 * User: val
 * Date: Jan 29, 2003
 * Time: 9:38:05 PM
 */
package com.abs.casino.common.util.xml.parser;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ReflectionUtils;
import com.abs.casino.common.util.xml.IXmlElement;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;

public class XmlHandler implements IXmlHandler {
    private static final Logger LOG = Logger.getLogger(XmlHandler.class);

    private HashMap <String,String> processors = new HashMap <String,String> ();
    private HashMap <String, Object> processorClasses = new HashMap<String, Object>();

    public XmlHandler() {
        registerAll();
    }

    public boolean isAtom(String name) {
        try {
            if(processors.get(name)!=null) return true;
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    public void process(IXmlElement element,
                        LinkedList <IXmlElement> path,
                        Object result) throws CommonException {

        // Get processor
        IXmlElementProcessor processor = getProcessor(element.getName());

        // Process request
        processor.process(element, path, result);

    }

    private IXmlElementProcessor getProcessor(String name)
            throws CommonException {
        try {
            if(processors == null || processors.get(name)==null)
                throw new Exception("Processor " +name+ " no found");

            if(processorClasses.get(name)==null)
                processorClasses.put(name, ReflectionUtils.forNameWithCompatibilityAliases((String)processors.get(name)).newInstance());
            return (IXmlElementProcessor)processorClasses.get(name);

        } catch(Exception e) {
            throw new CommonException("getProcessor  "+ "can't find processor", e);
        }

    }

    protected void registerProcessor(String name, String className) {
        processors.put(name, className);
    }

    protected void registerAll() {}
}
