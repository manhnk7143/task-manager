package com.ctel.dbaas.test;

import com.ctel.dbaas.common.context.OSContext;
import lombok.SneakyThrows;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.SecurityGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestAttachDetachSgOpenstack {

    @SneakyThrows
    public static void main(String[] args) {
        OSClient.OSClientV3 instance = OSContext.getInstance().getClient();
        Port currentPort = instance.networking().port().get("edd58ca9-174c-4f85-8516-8caa4a21d75d");
        String FE_security_group_1 = "8d700c3b-ca6d-4af3-b18a-86d0012292f1";

        List<String> lstSg = currentPort.getSecurityGroups();
        lstSg.remove(FE_security_group_1);

//        NeutronPort neutronPort = (NeutronPort) currentPort;
        Field[] fields = currentPort.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("createdTime") || field.getName().equals("updatedTime")) {
                field.setAccessible(true);
                field.set(currentPort, null);
            }
        }

        Port updatedPort = OSContext.getInstance().getClient().networking().port().update(currentPort);
        System.out.println(updatedPort);
    }

}
