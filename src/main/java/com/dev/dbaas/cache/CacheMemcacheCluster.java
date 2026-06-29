package com.dev.dbaas.cache;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.apache.log4j.Logger;
import java.io.IOException;

public class CacheMemcacheCluster implements ICacheClient{
	
	private static final Logger LOGGER = Logger.getLogger(CacheMemcacheCluster.class);
	private int idCluster;
	private static MemcachedClient client;
	
	public CacheMemcacheCluster(String memcachedAddrs, int idCluster){
		this.idCluster = idCluster;
		buildClients(memcachedAddrs);
	}

	public MemcachedClient getClient() {
		return client;
	}

	private void buildClients(String memcacheds) {
		
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(memcacheds));
		builder.setCommandFactory(new BinaryCommandFactory());
		try {
			client = builder.build();
		} catch (IOException ex) {
			LOGGER.error("could not build memcached clients", ex);
		}
	}

	public int getIdCluster() {
		return idCluster;
	}

	public void setIdCluster(int idCluster) {
		this.idCluster = idCluster;
	}

	@Override
	public void set(String key, int timeout, String data) throws Exception{
		client.set(key, timeout, data);
	}
	@Override
	public String get(String key) throws Exception{
		return client.get(key);
	}
}
