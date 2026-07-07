package com.ctel.dbaas.common.context;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;

import java.util.Date;

public class OSContext {

    private static Date dateExpires;
    private static OSClient.OSClientV3 instance;

    private OSContext() {
        refreshToken();
    }

    public static OSContext getInstance() {
        if (InstanceHolder.INSTANCE == null) {
            InstanceHolder.INSTANCE = new OSContext();
        }
        return InstanceHolder.INSTANCE;
    }

    public synchronized OSClient.OSClientV3 getClient() {
//        if (dateExpires == null || dateExpires.before(new Date())) {
//            refreshToken();
//        }
        refreshToken();
        return instance;
    }

    private synchronized void refreshToken() {
        instance = OSFactory.builderV3()
                .endpoint("http://10.60.65.155/identity/v3")
                .credentials("admin", "secret", Identifier.byName("default"))
                .scopeToProject(Identifier.byName("admin"), Identifier.byName("default"))
                .authenticate();
        dateExpires = instance.getToken().getExpires();
    }

    private static class InstanceHolder {
        private static OSContext INSTANCE;
    }
}
