package com.dev.dbaas.cache;

import com.dev.dbaas.config.Config;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class CacheRedisCluster implements ICacheClient{

	private static final Logger LOGGER = Logger.getLogger(CacheMemcacheCluster.class);
	private int idCluster;
	private static JedisCluster cluster;
	private Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();

	public CacheRedisCluster(String serverInfos, int idCluster){
		this.idCluster = idCluster;
		buildClients(serverInfos);
	}

	public JedisCluster getClient() {
		return cluster;
	}

	private void buildClients(String serverInfos) {

		String[] serverInfoArr = serverInfos.split(";");
		for(int i = 0; i < serverInfoArr.length; i++){
			String[] serverInfo = serverInfoArr[i].split(":");
			if(serverInfo.length == 3){
				String host = serverInfo[0];
				int port = Integer.parseInt(serverInfo[1]);
				jedisClusterNodes.add(new HostAndPort(host, port));
			}
		}
		if(!jedisClusterNodes.isEmpty()){
			GenericObjectPoolConfig poolConfig =new JedisPoolConfig();
			poolConfig.setMaxTotal(20);
			poolConfig.setMaxIdle(3);
			poolConfig.setMaxWaitMillis(3000);
			poolConfig.setTestOnBorrow(true);
			cluster = new JedisCluster(jedisClusterNodes, 5000, 100, 10, Config.redis_cluster_password,poolConfig);
		}
	}

	public int getIdCluster() {
		return idCluster;
	}

	public void setIdCluster(int idCluster) {
		this.idCluster = idCluster;
	}

	@Override
	public void set(String key, int timeout, String data) throws TimeoutException, InterruptedException, MemcachedException{
		if(cluster == null){
			LOGGER.info("CacheRedisCluster is null");
			return;
		}
		cluster.set(key, data);
		cluster.expire(key, timeout);
	}
	@Override
	public String get(String key) throws TimeoutException, InterruptedException, MemcachedException{
		return cluster.get(key);
	}
}
