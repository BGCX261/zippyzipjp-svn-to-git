package jp.zippyzip.web;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * Helper for PersistenceManagerFactory.
 */
public class PMF {

    /** Only one PersistenceManagerFactory */
    private static final PersistenceManagerFactory pmfInstance =
        JDOHelper.getPersistenceManagerFactory("transactions-optional");

    /** Default constracor */
    private PMF() {}

    /**
     * Getter of PersistenceManagerFactory.
     * 
     * @return PersistenceManagerFactory
     */
    public static PersistenceManagerFactory get() {
        return pmfInstance;
    }
}
