package com.github.wuchao.webproject.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
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

    private TransportClient transportClient;

    private AdminClient adminClient = null;

    @PostConstruct
    public void init() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", elasticsearchClusterName).build();
        transportClient = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName(elasticsearchClusterHost), 9300));
        adminClient = transportClient.admin();
    }

    /**
     * 根据索引和 QueryBuilder 查询 ES 保存的数据
     *
     * @param index
     * @param builder
     * @param orderField
     * @param order
     * @param size
     * @return
     */
    public SearchResponse findByIndexAndQueryBuilder(final String index,
                                                     final QueryBuilder builder,
                                                     final String orderField,
                                                     final SortOrder order,
                                                     final Integer size) {

        SearchRequestBuilder searchRequestBuilder = transportClient
                .prepareSearch(index)
                .addSort(orderField, order)
                .setSize(size)
                .setQuery(builder);
        return searchRequestBuilder.execute().actionGet(30000);
    }

    /**
     * 根据索引前缀名搜索索引
     *
     * @param indexPreffix
     * @return
     */
    public Map<String, IndexStats> searchIndices(final String indexPreffix) {
        IndicesAdminClient indicesAdminClient = adminClient.indices();
        IndicesStatsResponse response = indicesAdminClient.prepareStats(indexPreffix + "-*").all().get();
        return response.getIndices();
    }

    /**
     * 根据索引前缀名搜索某个日期前的索引
     *
     * @param indexPreffix 索引前缀（// index => "app-staging-tcp-%{+YYYY.MM.dd}"）
     * @param date
     */
    public List<String> searchIndicesBefore(final String indexPreffix, final String date) {
        Map<String, IndexStats> indices = searchIndices(indexPreffix);
        if (MapUtils.isNotEmpty(indices)) {
            List<String> hitIndices = new ArrayList<>(indices.size());
            Set<String> keySet = indices.keySet();
            for (String key : keySet) {
                String d = key.substring(key.lastIndexOf("-") + 1);
                int c = d.compareTo(date);
                if (c < 0) {
                    hitIndices.add(indexPreffix + "-" + d);
                }
            }
            return hitIndices;

        } else {
            return new ArrayList<>(0);
        }
    }

    /**
     * 根据索引前缀名搜索某个日期后的索引
     *
     * @param indexPreffix 索引前缀（// index => "app-staging-tcp-%{+YYYY.MM.dd}"）
     * @param date
     */
    public List<String> searchIndicesNameAfter(final String indexPreffix, final String date) {
        Map<String, IndexStats> indices = searchIndices(indexPreffix);
        if (MapUtils.isNotEmpty(indices)) {
            List<String> hitIndices = new ArrayList<>(indices.size());
            Set<String> keySet = indices.keySet();
            for (String key : keySet) {
                String d = key.substring(key.lastIndexOf("-") + 1);
                int c = d.compareTo(date);
                if (c > 0) {
                    hitIndices.add(indexPreffix + "-" + d);
                }
            }
            return hitIndices;

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
        String expiryDate = getDateBefore(daysBefore);
        List<String> indices = searchIndicesBefore(indexPreffix, expiryDate);
        delete(indices);
    }

    /**
     * 获取多少天以前的日期
     *
     * @param num
     * @return
     */
    private String getDateBefore(int num) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DATE, c.get(Calendar.DATE) - num);
        return new SimpleDateFormat("yyyy.MM.dd").format(c.getTime());
    }

}
