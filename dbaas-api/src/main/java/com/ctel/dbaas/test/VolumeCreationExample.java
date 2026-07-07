package com.ctel.dbaas.test;

import com.ctel.dbaas.common.context.OSContext;
import lombok.SneakyThrows;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.storage.block.Volume;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class VolumeCreationExample {
    private static final String AUTH_URL = "YOUR_AUTH_URL";
    private static final String USERNAME = "YOUR_USERNAME";
    private static final String PASSWORD = "YOUR_PASSWORD";
    private static final String PROJECT_NAME = "YOUR_PROJECT_NAME";
    private static final String USER_DOMAIN_NAME = "YOUR_USER_DOMAIN_NAME";
    private static final String PROJECT_DOMAIN_NAME = "YOUR_PROJECT_DOMAIN_NAME";

    @SneakyThrows
    public static void main(String[] args) {
        OSClient.OSClientV3 os = OSContext.getInstance().getClient();

        String serverId = "f5df2098-a061-46fe-a86b-86e69cf4e09f";
        String newFlavorId = "d2";

        Server server = os.compute().servers().get(serverId);
        Flavor flavor = os.compute().flavors().get(newFlavorId);

        if (server == null || flavor == null) {
            return;
        }

        os.compute().servers().resize(serverId, newFlavorId);
        boolean isVerityResize = waitForServerStatus(os, serverId, "VERIFY_RESIZE", 10);
        if (isVerityResize) {
            os.compute().servers().confirmResize(serverId);
        } else {
            server = os.compute().servers().get(serverId);
            System.out.println("status server not is VERIFY_RESIZE ! current status: " + server.getStatus());
        }
    }

    private static boolean waitForServerStatus(OSClient.OSClientV3 os, String serverId, String expectedServerStatus, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds);

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            Server server = os.compute().servers().get(serverId);
            if (server.getStatus().equals(Server.Status.forValue(expectedServerStatus))) {
                return true;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                return false;
            }
        }

        return false;
    }
}