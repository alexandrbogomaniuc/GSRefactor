package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.exception.CommonException;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: van0ss
 * Date: 02/10/2016
 */
public class ReflectionUtils {
    private static final Logger LOG = Logger.getLogger(ReflectionUtils.class);

    final static Pattern NUMBER = Pattern.compile("\\d+"); // Unsigned integer
    final static String CT_FIRST_VER_PATTERN = "transfer.common.";
    final static String CW_CT_PATTERN = "commonv\\d+\\.";

    /**
     * Find CW or CT version by package name, or package name of superclass/interfaces.
     * @param fullClassName name of class with package.
     * @param versionPattern pattern for regular expression to find version number.
     *                       <p>IF <b>null</b> then default pattern "commonv\\d+.".</p>
     * @return version, if version 0, then can't detect.
     * @throws CommonException ClassNotFound, many than one version detected.
     */
    public static int findCwCtVersion(String fullClassName, String versionPattern) throws CommonException {
        Class<?> clientClass;
        try {
            clientClass = Class.forName(fullClassName);
        } catch (ClassNotFoundException e) {
            LOG.error("Can't find class for name: " + fullClassName, e);
            throw new CommonException("Can't find class for name: " + fullClassName,e);
        }
        return findCwCtVersion(clientClass, versionPattern);
    }
    /**
     * Find CW or CT version by package name, or package name of superclass/interfaces.
     * @param clientClass CW client class.
     * @param versionPattern pattern for regular expression to find version number.
     *                       <p>IF <b>null</b> then default pattern "commonv\\d+.".</p>
     * @return version, if version 0, then can't detect.
     * @throws CommonException ClassNotFound, many than one version detected.
     */
    public static int findCwCtVersion(Class<?> clientClass, String versionPattern) throws CommonException {
        if (versionPattern == null) {
            versionPattern = CW_CT_PATTERN;
        }
        Pattern pattern = Pattern.compile(versionPattern);
        if (clientClass.getName().contains(CT_FIRST_VER_PATTERN)) {
            return 1;
        }
        int version = detectVersion(pattern, clientClass.getName());
        if (version != 0) {
            return version;
        }
        int refVersion = 0; // Reference version from superclasses
        Class[] interfases = clientClass.getInterfaces();
        if (interfases.length != 0) {
            for (Class<?> interfase : interfases) {
                Integer parseVersion = detectVersion(pattern, interfase.getName());
                if (parseVersion > refVersion) {
                    refVersion = parseVersion; // Take the highest version.
                }
            }
        }
        Class<?> superClass = clientClass.getSuperclass();
        Integer parseVersion = detectVersion(pattern, superClass.getName());
        if (parseVersion > refVersion) {
            refVersion = parseVersion; // Take the highest version.
        }
        return refVersion;
    }

    private static int detectVersion(Pattern pattern, String classStr) throws CommonException {

        Matcher version = pattern.matcher(classStr);
        List<String> matches = new ArrayList<String>();
        while (version.find()) {
            matches.add(version.group());
        }
        if (matches.size() == 0) {
            return 0;
        }
        if (matches.size() > 1) {
            LOG.error(classStr + " have more than one version");
            throw new CommonException(classStr + " have more than one version");
        }
        Matcher clearVersion = NUMBER.matcher(matches.get(0));

        if (clearVersion.find()) {
            return Integer.parseInt(String.valueOf(clearVersion.group()));
        }
        throw new CommonException(clearVersion.group() + " not have number in pattern");
    }

    /**
     * Checks that specified method can be invoked.
     *
     * @param clazz          target class
     * @param methodName     target method
     * @param parameterTypes parameter types for target method
     * @return <code>true</code> if target class has specified method and that method is neither abstract nor private,
     * <code>false</code> otherwise
     */
    public static boolean canInvokeMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getMethod(methodName, parameterTypes);
            int methodModifiers = method.getModifiers();
            return !Modifier.isAbstract(methodModifiers) && !Modifier.isPrivate(methodModifiers);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
