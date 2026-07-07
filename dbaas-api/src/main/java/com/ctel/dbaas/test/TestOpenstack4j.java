package com.ctel.dbaas.test;

import com.ctel.dbaas.common.context.OSContext;
import lombok.SneakyThrows;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

public class TestOpenstack4j {

    @SneakyThrows
    public static void main(String[] args) {
        String serverId = "f5df2098-a061-46fe-a86b-86e69cf4e09f";
        String newFlavorId = "d1";

        OSClient.OSClientV3 os = OSContext.getInstance().getClient();

        Server server = os.compute().servers().get(serverId);
        Flavor flavor = os.compute().flavors().get(newFlavorId);

        if (server == null || flavor == null) {
            return;
        }

        ActionResponse response = os.compute().servers().resize(serverId, newFlavorId);
        TimeUnit.SECONDS.sleep(10);
        server = os.compute().servers().get(serverId);
        if (response.isSuccess() && server.getStatus().equals(Server.Status.VERIFY_RESIZE)) {
            os.compute().servers().confirmResize(serverId);
        }


    }

}
