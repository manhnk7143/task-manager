package com.dev.dbaas.manager;

import com.dev.dbaas.cache.CacheMemcacheCluster;
import com.dev.dbaas.config.Config;
import org.apache.log4j.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CacheManager {

	private static final Logger LOGGER = Logger.getLogger(CacheManager.class);
	private static final ConcurrentMap<Integer, CacheMemcacheCluster> MAP_CLUSTER_CACHE = new ConcurrentHashMap<>();
	public static final int DEFAULT = 1;
	
	public CacheManager() {
		loadCacheClusterInfo();
	}

	static class CacheManagerHolder {
		private static final CacheManager INSTANCE = new CacheManager();
	}

	public static CacheManager getInstance() {
		return CacheManagerHolder.INSTANCE;
	}

	private void loadCacheClusterInfo() {
		String[] tmpClusters = Config.cache_centers.trim().split(";");
		for (String tmpCluster : tmpClusters) {
			String tmpClusterStand = tmpCluster.trim();
			if (!"".equals(tmpClusterStand)) {
				String[] tmpClusterInfos = tmpClusterStand.split(",");
				int clusterId = Integer.valueOf(tmpClusterInfos[0].trim());

				if (clusterId < 1) {
					LOGGER.error("=====================================");
					LOGGER.error("Node ID must > 0");
					LOGGER.error("=====================================");
					System.exit(0);
				}

				String tmpClusterAddress = tmpClusterInfos[1].trim();
				CacheMemcacheCluster cacheCluster = new CacheMemcacheCluster(tmpClusterAddress, clusterId);
				MAP_CLUSTER_CACHE.put(clusterId, cacheCluster);
			}
		}
	}

	public void set(String key, int timeout, String data, int clusterId)
			throws Exception {

		// set default cluster for cache
		if (clusterId < 1) {
			clusterId = DEFAULT;
		}
		MAP_CLUSTER_CACHE.get(clusterId).set(key, timeout, data);
	}

	public String get(String key, int clusterId) throws Exception {

		// set default cluster for cache
		if (clusterId < 1) {
			clusterId = DEFAULT;
		}
		return MAP_CLUSTER_CACHE.get(clusterId).get(key);
	}

	public void resetCache(){

	}
}
