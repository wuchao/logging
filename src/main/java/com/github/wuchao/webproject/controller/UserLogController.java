package com.github.wuchao.webproject.controller;

import com.github.wuchao.webproject.service.ElasticTransportClientService;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserLogController {

    @Autowired
    private ElasticTransportClientService elasticTransportClientService;

    @GetMapping("/findByIndex")
    public ResponseEntity testFindByIndex(@RequestParam(defaultValue = "app-staging-tcp") String indexPrefix) {
        Map<String, IndexStats> indices;
        indices = elasticTransportClientService.searchIndices(indexPrefix);
        if (MapUtils.isNotEmpty(indices)) {
            // 词条查询时未经分析的，因此需要提供跟索引文档中的词条完全匹配的词条
            // 索引在建立时，userLog 会变成 userlog
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("logType", "userlog");
            List<List<Map<String, Object>>> indexes = new ArrayList<>(indices.size());

            indices.forEach((key, value) -> {
                SearchResponse searchResponse = elasticTransportClientService
                        .findByIndexAndQueryBuilder(key, "doc", termQueryBuilder, "@timestamp", SortOrder.DESC, 20);
                SearchHits searchHits = searchResponse.getHits();
                if (searchHits.totalHits > 0) {
                    List<Map<String, Object>> indexList = new ArrayList<>(Integer.valueOf(String.valueOf(searchHits.totalHits)));
                    searchHits.forEach(hit -> {
                        indexList.add(hit.getSourceAsMap());
                    });
                    indexes.add(indexList);
                }
            });

            return ResponseEntity.ok(indexes);
        }
        return ResponseEntity.ok(Collections.emptyList());
    }
}
