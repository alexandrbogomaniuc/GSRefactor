package com.abs.casino.common.util;

import com.dgphoenix.casino.common.util.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * User: isirbis
 * Date: 19.09.14
 */
public class RefererDomains {
    private static final String ARRAY_DELIMITER = " ";
    private static final String WILDCARD_MASK = "*.";
    private static final String DOMAIN_DELIMITER = ".";

    private List<String> refererDomainsList;

    public RefererDomains() {
        refererDomainsList = new ArrayList<>();
    }

    public RefererDomains(List<String> refererDomainsList) {
        this.refererDomainsList = refererDomainsList;
    }

    public RefererDomains(String refererDomainsString) {
        this.refererDomainsList = parseDomainsList(refererDomainsString);
    }

    public void setRefererDomains(String refererDomainsString) {
        refererDomainsList = parseDomainsList(refererDomainsString);
    }

    public boolean isContains(String domain) {
        return refererDomainsList.contains(domain);
    }

    public boolean isAllowed(String domain) {
        if (CollectionUtils.isEmpty(refererDomainsList)) {
            return true;
        }
        if (isTrimmedEmpty(domain)) {
            return false;
        }

        for (String allowedDomain : refererDomainsList) {
            if (isInSameDNSZone(allowedDomain, domain)) return true;
        }
        return false;
    }

    public boolean isForbidden(String domain) {
        if (CollectionUtils.isEmpty(refererDomainsList)) {
            return false;
        }
        if (isTrimmedEmpty(domain)) {
            return false;
        }
        for (String forbiddenDomain : refererDomainsList) {
            if (isInSameDNSZone(forbiddenDomain, domain)) {
                return true;
            }
        }
        return false;
    }

    private List<String> parseDomainsList(String domains) {
        List<String> result = new ArrayList<String>();
        if (!isTrimmedEmpty(domains)) {
            Collections.addAll(result, domains.split(ARRAY_DELIMITER));
        }
        return result;
    }

    private boolean isInSameDNSZone(String storedDomain, String refererDomain) {
        boolean isWildcard = isWildcard(storedDomain);
        if (isWildcard) {
            storedDomain = StringUtils.substringAfter(storedDomain, WILDCARD_MASK);
        }
        return StringUtils.equals(storedDomain, refererDomain) || isWildcard && isSubdomain(storedDomain, refererDomain);
    }

    private boolean isSubdomain(String domain, String subDomain) {
        String rootDomain = DOMAIN_DELIMITER + domain;
        return StringUtils.endsWith(subDomain, rootDomain);
    }

    private boolean isWildcard(String domain) {
        return StringUtils.startsWith(domain, WILDCARD_MASK);
    }

    public boolean isEmpty() {
        return refererDomainsList.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RefererDomains that = (RefererDomains) o;

        if ((refererDomainsList == null) || (that.refererDomainsList == null)) {
            return (refererDomainsList == that.refererDomainsList);
        }

        Set<String> domainsSet = new HashSet<>(refererDomainsList);
        Set<String> oSet = new HashSet<>(that.refererDomainsList);

        return domainsSet.equals(oSet);
    }

    @Override
    public int hashCode() {
        return refererDomainsList != null ? refererDomainsList.hashCode() : 0;
    }

    @Override
    public String toString() {
        return StringUtils.join(refererDomainsList, ARRAY_DELIMITER);
    }
}
