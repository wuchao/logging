package com.github.wuchao.webproject.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class ElasticSearchService {

    @Value("${elasticsearch.cluster.name}")
    private String elasticsearchClusterName;

    @Value("${elasticsearch.cluster.host}")
    private String elasticsearchClusterHost;

    private AdminClient adminClient = null;

    @PostConstruct
    public void init() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", elasticsearchClusterName).build();
        TransportClient transportClient = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName(elasticsearchClusterHost), 9300));
        adminClient = transportClient.admin();
    }

    /**
     * 获取多少天以前的日期
     *
     * @param num
     * @return
     */
    private String getExpiryDate(int num) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DATE, c.get(Calendar.DATE) - num);
        return new SimpleDateFormat("yyyy.MM.dd").format(c.getTime());
    }

    /**
     * 根据索引前缀名搜索出要过期的索引名
     *
     * @param indexPreffix 索引名称前缀
     * @param daysBefore   过期天数
     */
    public List<String> searchIndexName(String indexPreffix, int daysBefore) {
        IndicesAdminClient indicesAdminClient = adminClient.indices();
        IndicesStatsResponse response = indicesAdminClient.prepareStats(indexPreffix + "-*").all().get();
        Map<String, IndexStats> indices = response.getIndices();

        if (MapUtils.isNotEmpty(indices)) {
            String expiryDate = getExpiryDate(daysBefore);
            List<String> deleteIndices = new ArrayList<>(indices.size());
            Set<String> keySet = indices.keySet();
            for (String key : keySet) {
                String d = key.substring(key.lastIndexOf("-") + 1);
                int c = d.compareTo(expiryDate);
                if (c < 0) {
                    deleteIndices.add(indexPreffix + "-log-" + d);
                }
            }
            return deleteIndices;

        } else {
            return new ArrayList<>(0);
        }
    }

    /**
     * 删除整个索引
     *
     * @param indices
     */
    public void delete(List<String> indices) {
        if (CollectionUtils.isNotEmpty(indices)) {
            IndicesAdminClient indicesAdminClient = adminClient.indices();
            DeleteIndexResponse response;
            for (String s : indices) {
                response = indicesAdminClient.prepareDelete(s).execute().actionGet();
                log.debug("删除索引 " + s + " " + response.isAcknowledged());
            }
        }
    }

    /**
     * 查找 daysBefore 天之前索引名称前缀为 indexPreffix 的索引，并删除
     *
     * @param indexPreffix
     * @param daysBefore
     */
    public void deleteIndices(String indexPreffix, int daysBefore) {
        List<String> indices = searchIndexName(indexPreffix, daysBefore);
        delete(indices);
    }

}
