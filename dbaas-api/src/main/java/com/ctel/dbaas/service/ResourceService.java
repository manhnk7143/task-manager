package com.ctel.dbaas.service;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.cmc_cloud.QuotaInfo;
import com.ctel.dbaas.dto.resource.ResourceOverview;
import com.ctel.dbaas.dto.resource.ResourceUsedReq;
import com.ctel.dbaas.dto.resource.ResourceUsedRes;
import com.ctel.dbaas.entity.dbaas.ResourceEntity;
import com.ctel.dbaas.entity.dbaas.ResourceInstanceEntity;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.ResourceInstanceRepository;
import com.ctel.dbaas.repository.dbaas.ResourceRepository;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private CmcCloudService cmcCloudService;
    @Autowired
    private ResourceInstanceRepository resourceInstanceRepository;

    public ResourceOverview getResourceOverview(String orgId, String regionId, String projectId) {
        ResourceEntity resourceEntity = resourceRepository.findFirstByOrgIdAndRegionIdAndProjectId(orgId, regionId, projectId);
        if (resourceEntity == null) {
            resourceEntity = new ResourceEntity();
            resourceEntity.setProjectId(projectId);
            resourceEntity.setOrgId(orgId);
            resourceEntity.setRegionId(regionId);
            resourceRepository.save(resourceEntity);
        }

        ResourceUsedRes resourceUsed = CommonUtils.toObject(resourceEntity.getResourceUsed(), ResourceUsedRes.class);
        if (resourceUsed == null) {
            throw new AppException(new ErrorResponse("Error while get resource used"));
        }

        QuotaInfo quotaInfo = cmcCloudService.getQuotas(orgId, regionId, "dbv2", projectId);
        if (quotaInfo == null) {
            throw new AppException(new ErrorResponse("Error while get quotas info"));
        }

        Map<String, ResourceOverview.ResourceStatus> mapResource = new HashMap<>();
        mapResource.put("cpu", new ResourceOverview.ResourceStatus(resourceUsed.getCpu(), quotaInfo.getDbv2Cpu()));
        mapResource.put("ram", new ResourceOverview.ResourceStatus(resourceUsed.getRam(), quotaInfo.getDbv2Ram()));
        mapResource.put("systemDisk", new ResourceOverview.ResourceStatus(resourceUsed.getSystemDisk(), quotaInfo.getDbv2SystemDiskGb()));
        mapResource.put("volume", new ResourceOverview.ResourceStatus(resourceUsed.getVolumeGb(), quotaInfo.getDbv2VolumeGb()));

        return new ResourceOverview(mapResource);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ResourceEntity checkAndSetResourceUsed(ResourceUsedReq resourceRequest, RequestInfo requestCtx) {
        QuotaInfo quotaInfo = cmcCloudService.getQuotas(requestCtx.getOrgId(), requestCtx.getRegionId(), "dbv2", requestCtx.getProjectId());
        if (quotaInfo == null) {
            throw new AppException(new ErrorResponse("Error while get quotas info"));
        }

        ResourceEntity resourceEntity = resourceRepository.findFirstByOrgIdAndRegionIdAndProjectId(
                requestCtx.getOrgId(), requestCtx.getRegionId(), requestCtx.getProjectId());
        if (resourceEntity == null) {
            resourceEntity = new ResourceEntity();
            resourceEntity.setProjectId(requestCtx.getProjectId());
            resourceEntity.setOrgId(requestCtx.getOrgId());
            resourceEntity.setRegionId(requestCtx.getRegionId());
        }

        ResourceUsedRes resourceCheckQuota = CommonUtils.toObject(resourceEntity.getResourceUsed(), ResourceUsedRes.class);
        if (resourceCheckQuota == null) {
            throw new AppException(new ErrorResponse("Error while check quotas"));
        }
        resourceCheckQuota.setCpu(resourceCheckQuota.getCpu() + resourceRequest.getCpu());
        resourceCheckQuota.setRam(resourceCheckQuota.getRam() + resourceRequest.getRam());
        resourceCheckQuota.setSystemDisk(resourceCheckQuota.getSystemDisk() + resourceRequest.getSystemDisk());
        resourceCheckQuota.setVolumeGb(resourceCheckQuota.getVolumeGb() + resourceRequest.getVolumeGb());

        this.checkQuota(resourceCheckQuota, quotaInfo);

        resourceEntity.setResourceUsed(CommonUtils.toJson(resourceCheckQuota));
        resourceRepository.save(resourceEntity);

        return resourceEntity;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteResourceInstance(String instanceId, RequestInfo requestCtx) {
        ResourceInstanceEntity resourceInstance = resourceInstanceRepository.findFirstByInstanceIdAndDeletedAtIsNull(instanceId);
        if (resourceInstance != null) {
            ResourceEntity resourceEntity = resourceRepository.findFirstByOrgIdAndRegionIdAndProjectId(
                    requestCtx.getOrgId(), requestCtx.getRegionId(), requestCtx.getProjectId());
            if (resourceEntity == null) {
                throw new AppException(new ErrorResponse("Error while get resource instance"));
            }

            ResourceUsedRes resourceTotal = CommonUtils.toObject(resourceEntity.getResourceUsed(), ResourceUsedRes.class);
            ResourceUsedRes resourceDelete = CommonUtils.toObject(resourceEntity.getResourceUsed(), ResourceUsedRes.class);
            if (resourceTotal == null || resourceDelete == null) {
                throw new AppException(new ErrorResponse("Error while get resource instance or resource total"));
            }

            resourceTotal.setCpu(resourceTotal.getCpu() - resourceDelete.getCpu());
            resourceTotal.setRam(resourceTotal.getRam() - resourceDelete.getRam());
            resourceTotal.setSystemDisk(resourceTotal.getSystemDisk() - resourceDelete.getSystemDisk());
            resourceTotal.setVolumeGb(resourceTotal.getVolumeGb() - resourceDelete.getVolumeGb());
            resourceEntity.setResourceUsed(CommonUtils.toJson(resourceTotal));
            resourceRepository.save(resourceEntity);

            resourceInstance.setDeletedAt(LocalDateTime.now());
            resourceInstanceRepository.save(resourceInstance);
        }
    }

    private void checkQuota(ResourceUsedRes resourceCheckQuota, QuotaInfo quotaInfo) {
        if (!Constant.RESOURCE_UNLIMITED.equals(quotaInfo.getDbv2Cpu()) && resourceCheckQuota.getCpu() > quotaInfo.getDbv2Cpu()) {
            throw new AppException(new ErrorResponse("CPU resource limit exceeded"));
        }

        if (!Constant.RESOURCE_UNLIMITED.equals(quotaInfo.getDbv2Ram()) && resourceCheckQuota.getRam() > quotaInfo.getDbv2Ram()) {
            throw new AppException(new ErrorResponse("RAM resource limit exceeded"));
        }

        if (!Constant.RESOURCE_UNLIMITED.equals(quotaInfo.getDbv2SystemDiskGb()) && resourceCheckQuota.getSystemDisk() > quotaInfo.getDbv2SystemDiskGb()) {
            throw new AppException(new ErrorResponse("System disk resource limit exceeded"));
        }

        if (!Constant.RESOURCE_UNLIMITED.equals(quotaInfo.getDbv2VolumeGb()) && resourceCheckQuota.getVolumeGb() > quotaInfo.getDbv2VolumeGb()) {
            throw new AppException(new ErrorResponse("Volume resource limit exceeded"));
        }
    }

}
