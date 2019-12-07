package com.github.wuchao.logging.service;

import org.apache.commons.collections.MapUtils;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles(value = {"test"})
public class ElasticSearchServiceTests {

    @Autowired
    private ElasticTransportClientService elasticSearchService;

    @Test
    public void testDeleteIndices() {
        elasticSearchService.deleteIndices("app-staging", 3);
    }

    @Test
    public void testSearchIndices() {
        Map<String, IndexStats> indices = elasticSearchService.searchIndices("app-staging-tcp");
        if (MapUtils.isNotEmpty(indices)) {
            indices.forEach((key, value) -> {
                System.out.println(key + " : " + value);
            });
        }
    }

    @Test
    public void testFindByIndex() {
        Map<String, IndexStats> indices = elasticSearchService.searchIndices("app-staging-tcp");
        if (MapUtils.isNotEmpty(indices)) {
            // 词条查询时未经分析的，因此需要提供跟索引文档中的词条完全匹配的词条
            // 索引在建立时，userLog 会变成 userlog
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("logType", "userlog");
            indices.forEach((key, value) -> {
                SearchResponse searchResponse = elasticSearchService
                        .findByIndexAndQueryBuilder(key, "doc", termQueryBuilder, "@timestamp", SortOrder.DESC, 20);
                SearchHits searchHits = searchResponse.getHits();
                searchHits.forEach(hit -> {
                    System.out.println(hit.getSourceAsMap());
                });
            });
        }
    }

}
