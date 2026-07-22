/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.spi;

import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;

/**
 * Given a classname locate associated PackageInfo (jar name, version name).
 *
 * @author James Strachan
 * @author Ceki G&uuml;lc&uuml;
 */
public class PackagingDataCalculator {

    final static StackTraceElementProxy[] STEP_ARRAY_TEMPLATE = new StackTraceElementProxy[0];

    HashMap<String, ClassPackagingData> cache = new HashMap<String, ClassPackagingData>();

    private static boolean GET_CALLER_CLASS_METHOD_AVAILABLE = false; // private static boolean
                                                                      // HAS_GET_CLASS_LOADER_PERMISSION = false;

    static {
        // if either the Reflection class or the getCallerClass method
        // are unavailable, then we won't invoke Reflection.getCallerClass()
        // This approach ensures that this class will *run* on JDK's lacking
        // sun.reflect.Reflection class. However, this class will *not compile*
        // on JDKs lacking sun.reflect.Reflection.
        try {
            // Reflection.getCallerClass(2);
            // GET_CALLER_CLASS_METHOD_AVAILABLE = true;
        } catch (NoClassDefFoundError e) {
        } catch (NoSuchMethodError e) {
        } catch (UnsupportedOperationException e) {
        } catch (Throwable e) {
            System.err.println("Unexpected exception");
            e.printStackTrace();
        }
    }

    public void calculate(IThrowableProxy tp) {
        while (tp != null) {
            populateFrames(tp.getStackTraceElementProxyArray());
            IThrowableProxy[] suppressed = tp.getSuppressed();
            if (suppressed != null) {
                for (IThrowableProxy current : suppressed) {
                    populateFrames(current.getStackTraceElementProxyArray());
                }
            }
            tp = tp.getCause();
        }
    }

    @SuppressWarnings("unused")
    void populateFrames(StackTraceElementProxy[] stepArray) {
        // in the initial part of this method we populate package information for
        // common stack frames
        final Throwable t = new Throwable("local stack reference");
        final StackTraceElement[] localSTEArray = t.getStackTrace();
        final int commonFrames = STEUtil.findNumberOfCommonFrames(localSTEArray, stepArray);
        final int localFirstCommon = localSTEArray.length - commonFrames;
        final int stepFirstCommon = stepArray.length - commonFrames;

        ClassLoader lastExactClassLoader = null;
        ClassLoader firsExactClassLoader = null;

        int missfireCount = 0;
        for (int i = 0; i < commonFrames; i++) {
            Class<?> callerClass = null;
            if (GET_CALLER_CLASS_METHOD_AVAILABLE) {
                // callerClass = Reflection.getCallerClass(localFirstCommon + i - missfireCount
                // + 1);
            }
            StackTraceElementProxy step = stepArray[stepFirstCommon + i];
            String stepClassname = step.ste.getClassName();

            if (callerClass != null && stepClassname.equals(callerClass.getName())) {
                // see also LBCLASSIC-263
                lastExactClassLoader = callerClass.getClassLoader();
                if (firsExactClassLoader == null) {
                    firsExactClassLoader = lastExactClassLoader;
                }
                //ClassPackagingData pi = calculateByExactType(callerClass);
            } else {
                missfireCount++;
                //ClassPackagingData pi = computeBySTEP(step, lastExactClassLoader);
            }
        }
        populateUncommonFrames(commonFrames, stepArray, firsExactClassLoader);
    }

    void populateUncommonFrames(int commonFrames, StackTraceElementProxy[] stepArray,
            ClassLoader firstExactClassLoader) {
        int uncommonFrames = stepArray.length - commonFrames;
        for (int i = 0; i < uncommonFrames; i++) {
            StackTraceElementProxy step = stepArray[i];
        }
    }

}
