package com.dev.dbaas.worker.processor.control;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.OSContextManager;
import com.dev.dbaas.worker.job.ControlJob;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ResizeInstanceProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(ResizeInstanceProcessor.class);
    private static final int TIMEOUT_SECONDS_RESIZE_INSTANCE = 300;
    private static final int TIMEOUT_SECONDS_CONFIRM_RESIZE = 180;
    private static final int POLLING_INTERVAL_SECONDS = 5;
    private static final int MAX_THREADS = 10;

    @Override
    public boolean process(ControlJob job) throws Exception {
        LOGGER.info("[4CMS] ResizeInstanceProcessor " + job.getServiceId() + " - " + job.getData());
        job.decodePacket();
        JSONObject input = job.getJsonData();
        LOGGER.warn("ResizeInstanceProcessor input : " + input);

        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        String regionId = input.optString("regionId");
        String newFlavorId = input.optString("newFlavorId");
        String computeIdsString = input.optString("computeIds", "[]");
        JSONArray computeIds = new JSONArray(computeIdsString);

        LOGGER.warn("ResizeInstanceProcessor instanceId : " + instanceId + " datastore : " + datastore +
                " regionId : " + regionId + " newFlavorId : " + newFlavorId + " computeIds : " + computeIds);

        if (computeIds.isEmpty()) {
            LOGGER.warn("ResizeInstanceProcessor computeIds is empty");
            throw new Exception("Compute IDs list is empty");
        }

        TbInstance instance = InstanceManager.findById(instanceId);
        if (instance == null || instance.getDeletedAt() != null || Constaint.DELETING.equals(instance.getStatus())) {
            LOGGER.warn("Instance not found - instanceId: " + instanceId);
            throw new Exception("Instance not found: " + instanceId);
        }

        OSClient.OSClientV3 os = OSContextManager.getInstance().getClient(regionId, datastore);
        if (os == null) {
            LOGGER.warn("Failed to get OpenStack client for region: " + regionId + ", datastore: " + datastore);
            throw new Exception("Failed to get OpenStack client");
        }

        Flavor newFlavor = os.compute().flavors().get(newFlavorId);
        if (newFlavor == null) {
            LOGGER.warn("New flavor not found - flavorId: " + newFlavorId);
            throw new Exception("New flavor not found: " + newFlavorId);
        }

        List<TbCompute> computesToResize = new ArrayList<>();
        for (int i = 0; i < computeIds.length(); i++) {
            String computeId = computeIds.getString(i);
            TbCompute compute = ComputeManager.findById(computeId);
            if (compute == null || compute.getDeletedAt() != null || Constaint.DELETING.equals(compute.getStatus())) {
                LOGGER.warn("Compute not found - computeId: " + computeId);
                throw new Exception("Compute not found: " + computeId);
            }

            Server server = os.compute().servers().get(compute.getNovaInstanceId());
            if (server == null) {
                LOGGER.warn("Server not found in OpenStack - computeId: " + computeId + ", novaInstanceId: " + compute.getNovaInstanceId());
                throw new Exception("Server not found in OpenStack: " + compute.getNovaInstanceId());
            }

            if (server.getStatus() != Server.Status.ACTIVE && server.getStatus() != Server.Status.SHUTOFF) {
                LOGGER.warn("Server is not in ACTIVE or SHUTOFF state - computeId: " + computeId +
                        ", novaInstanceId: " + compute.getNovaInstanceId() + ", current status: " + server.getStatus());
                throw new Exception("Server " + compute.getNovaInstanceId() + " is not in ACTIVE or SHUTOFF state. Current status: " + server.getStatus());
            }

            computesToResize.add(compute);
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(computesToResize.size(), MAX_THREADS));
        List<String> successfullyResizedComputes = new ArrayList<>();
        try {
            Map<String, CompletableFuture<Boolean>> resizeFutures = new ConcurrentHashMap<>();
            for (TbCompute compute : computesToResize) {
                String computeId = compute.getId();
                String serverId = compute.getNovaInstanceId();

                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                    LOGGER.info("Starting resize for compute: " + computeId + ", novaInstanceId: " + serverId);
                    return resizeFlavor(os, serverId, newFlavorId, TIMEOUT_SECONDS_RESIZE_INSTANCE);
                }, executor);

                resizeFutures.put(computeId, future);
            }

            CompletableFuture<Void> allResizes = CompletableFuture.allOf(resizeFutures.values().toArray(new CompletableFuture[0]));
            try {
                allResizes.join();
                for (TbCompute compute : computesToResize) {
                    String computeId = compute.getId();
                    if (Boolean.TRUE.equals(resizeFutures.get(computeId).join())) {
                        successfullyResizedComputes.add(computeId);
                    } else {
                        throw new Exception("Resize failed for compute: " + computeId);
                    }
                }

                Map<String, CompletableFuture<Boolean>> confirmFutures = new ConcurrentHashMap<>();
                for (TbCompute compute : computesToResize) {
                    String computeId = compute.getId();
                    String serverId = compute.getNovaInstanceId();

                    CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                        LOGGER.info("Confirming resize for compute: " + computeId + ", novaInstanceId: " + serverId);
                        return confirmResize(os, serverId, TIMEOUT_SECONDS_CONFIRM_RESIZE);
                    }, executor);

                    confirmFutures.put(computeId, future);
                }

                // Wait for all confirm operations
                CompletableFuture<Void> allConfirms = CompletableFuture.allOf(confirmFutures.values().toArray(new CompletableFuture[0]));
                allConfirms.join();

                for (TbCompute compute : computesToResize) {
                    String computeId = compute.getId();
                    if (!Boolean.TRUE.equals(confirmFutures.get(computeId).join())) {
                        throw new Exception("Confirm resize failed for compute: " + computeId);
                    }

                    compute.setFlavorId(newFlavorId);
                    compute.setUpdatedAt(LocalDateTime.now());
                    ComputeManager.update(compute);
                }

                instance.setFlavorId(newFlavorId);
                instance.setUpdatedAt(LocalDateTime.now());
                InstanceManager.update(instance);

                LOGGER.info("Successfully resized all computes and updated instance: " + instanceId);
            } catch (Exception e) {
                LOGGER.error("Error during resize/confirm process", e);
                if (!successfullyResizedComputes.isEmpty()) {
                    revertAllResizesAsync(os, successfullyResizedComputes, computesToResize, executor);
                }
                // gửi thông báo lỗi resize flavor cho dbaas
                throw e;
            }

        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // gửi thông báo resize flavor thành công cho dbaas

        LOGGER.warn("[4CMS] ResizeInstanceProcessor DONE: " + job.getServiceId() + " - " + job.getData());
        return true;
    }

    private void revertAllResizesAsync(OSClient.OSClientV3 os, List<String> resizedComputes,
                                       List<TbCompute> allComputes, ExecutorService executor) {
        LOGGER.warn("Reverting all resizes to maintain consistency");
        Map<String, TbCompute> computeMap = new HashMap<>();
        for (TbCompute compute : allComputes) {
            computeMap.put(compute.getId(), compute);
        }

        List<CompletableFuture<Void>> revertFutures = new ArrayList<>();

        for (String computeId : resizedComputes) {
            TbCompute compute = computeMap.get(computeId);
            if (compute != null) {
                String serverId = compute.getNovaInstanceId();

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        Server server = os.compute().servers().get(serverId);
                        if (server.getStatus() == Server.Status.VERIFY_RESIZE) {
                            LOGGER.info("Reverting resize for compute: " + computeId);
                            ActionResponse response = os.compute().servers().revertResize(serverId);
                            if (response.isSuccess()) {
                                waitForServerStatus(os, serverId, Server.Status.ACTIVE, TIMEOUT_SECONDS_RESIZE_INSTANCE);
                                LOGGER.info("Successfully reverted resize for compute: " + computeId);
                            } else {
                                LOGGER.warn("Failed to revert resize for compute: " + computeId + ": " + response.getFault());
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error reverting resize for compute: " + computeId, e);
                    }
                }, executor);

                revertFutures.add(future);
            }
        }

        CompletableFuture<Void> allReverts = CompletableFuture.allOf(
                revertFutures.toArray(new CompletableFuture[0]));

        try {
            allReverts.join();
            LOGGER.info("All revert operations completed");
        } catch (Exception e) {
            LOGGER.error("Error during revert operations", e);
        }
    }

    private boolean resizeFlavor(OSClient.OSClientV3 os, String serverId, String flavorId, int timeoutSeconds) {
        try {
            Server server = os.compute().servers().get(serverId);
            if (server.getStatus().equals(Server.Status.VERIFY_RESIZE)) {
                LOGGER.info("Server " + serverId + " is in VERIFY_RESIZE state, reverting resize first");
                ActionResponse revertResponse = os.compute().servers().revertResize(serverId);
                if (!revertResponse.isSuccess()) {
                    LOGGER.warn("Failed to revert resize for server " + serverId + ": " + revertResponse.getFault());
                    return false;
                }

                if (!waitForServerStatus(os, serverId, Server.Status.ACTIVE, 60)) {
                    LOGGER.warn("Server " + serverId + " failed to return to ACTIVE state after revert");
                    return false;
                }
            }

            LOGGER.info("Resizing server " + serverId + " to flavor " + flavorId);
            ActionResponse response = os.compute().servers().resize(serverId, flavorId);
            if (!response.isSuccess()) {
                LOGGER.warn("Failed to initiate resize for server " + serverId + ": " + response.getFault());
                return false;
            }

            return waitForServerStatus(os, serverId, Server.Status.VERIFY_RESIZE, timeoutSeconds);

        } catch (Exception e) {
            LOGGER.error("Error during resize operation for server " + serverId, e);
            return false;
        }
    }

    private boolean confirmResize(OSClient.OSClientV3 os, String serverId, int timeoutSeconds) {
        try {
            Server server = os.compute().servers().get(serverId);
            if (server.getStatus() != Server.Status.VERIFY_RESIZE) {
                LOGGER.warn("Server " + serverId + " is not in VERIFY_RESIZE state, cannot confirm resize");
                return false;
            }

            ActionResponse response = os.compute().servers().confirmResize(serverId);
            if (!response.isSuccess()) {
                LOGGER.warn("Failed to confirm resize for server " + serverId + ": " + response.getFault());
                return false;
            }

            return waitForServerStatus(os, serverId, Server.Status.ACTIVE, timeoutSeconds);

        } catch (Exception e) {
            LOGGER.error("Error during confirm resize operation for server " + serverId, e);
            return false;
        }
    }

    private boolean waitForServerStatus(OSClient.OSClientV3 os, String serverId, Server.Status targetStatus, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds);

        LOGGER.info("Waiting for server " + serverId + " to reach status " + targetStatus);

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                Server server = os.compute().servers().get(serverId);
                Server.Status currentStatus = server.getStatus();

                LOGGER.debug("Server " + serverId + " current status: " + currentStatus);

                if (currentStatus.equals(targetStatus)) {
                    LOGGER.info("Server " + serverId + " reached target status: " + targetStatus);
                    return true;
                }

                if (currentStatus.equals(Server.Status.ERROR)) {
                    LOGGER.warn("Server " + serverId + " entered ERROR state during wait for " + targetStatus);
                    return false;
                }

                TimeUnit.SECONDS.sleep(POLLING_INTERVAL_SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting for server status", e);
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception e) {
                LOGGER.error("Error while checking server status", e);
            }
        }

        LOGGER.warn("Timeout waiting for server " + serverId + " to reach status " + targetStatus);
        return false;
    }
}
