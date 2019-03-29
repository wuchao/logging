package com.github.wuchao.webproject.controller;

import com.github.wuchao.webproject.service.ElasticSearchService;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserLogController {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @GetMapping("/findByIndex")
    public void testFindByIndex() {
        Map<String, IndexStats> indices;
        indices = elasticSearchService.searchIndices("app-staging-tcp");
        if (MapUtils.isNotEmpty(indices)) {
            // 词条查询时未经分析的，因此需要提供跟索引文档中的词条完全匹配的词条
            // 索引在建立时，userLog 会变成 userlog
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("logType", "userlog");
            indices.forEach((key, value) -> {
                System.out.println("----------------------------------------");
                SearchResponse searchResponse = elasticSearchService
                        .findByIndexAndQueryBuilder(key, "doc", termQueryBuilder, "@timestamp", SortOrder.DESC, 20);
                SearchHits searchHits = searchResponse.getHits();
                searchHits.forEach(hit -> {
                    System.out.println(hit.getSource());
                });
            });
        }
    }
}
