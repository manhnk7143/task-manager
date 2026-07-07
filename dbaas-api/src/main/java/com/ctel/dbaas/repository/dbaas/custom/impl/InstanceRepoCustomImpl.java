package com.ctel.dbaas.repository.dbaas.custom.impl;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.repository.dbaas.custom.InstanceRepoCustom;
import com.ctel.dbaas.repository.dbaas.projection.instance.InstanceDropdownRes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InstanceRepoCustomImpl implements InstanceRepoCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<InstanceDropdownRes> queryInstanceDropdown(String nameSearch, List<String> datastoreCodes, String status, RequestInfo reqCtx) {
        Map<String, Object> paramaterMap = new HashMap<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT instance.id AS id, instance.name AS name, datastore.name AS datastoreName FROM InstanceEntity instance INNER JOIN DatastoreEntity datastore ON instance.datastoreId = datastore.id WHERE 1=1 ");
        if (StringUtils.isNotBlank(nameSearch)) {
            queryBuilder.append(" AND instance.name LIKE '%:name%' ");
            paramaterMap.put("name", nameSearch);
        }

        if (StringUtils.isNotBlank(status)) {
            queryBuilder.append(" AND instance.status = :status ");
            paramaterMap.put("status", status);
        }

        if (datastoreCodes != null) {
            queryBuilder.append(" AND datastore.code in (:datastoreCodes)");
            paramaterMap.put("datastoreCodes", datastoreCodes);
        }

        queryBuilder.append(" AND instance.deletedAt IS NULL AND instance.projectId = :projectId AND instance.orgId = :orgId ");
        paramaterMap.put("projectId", reqCtx.getProjectId());
        paramaterMap.put("orgId", reqCtx.getOrgId());

        Query query = entityManager.createQuery(queryBuilder.toString(), InstanceDropdownRes.class);
        for (String key : paramaterMap.keySet()) {
            query.setParameter(key, paramaterMap.get(key));
        }

//        Pageable pageable = null;
//        query.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
//        query.setMaxResults(pageable.getPageSize());

        return (List<InstanceDropdownRes>) query.getResultList();
    }

}
