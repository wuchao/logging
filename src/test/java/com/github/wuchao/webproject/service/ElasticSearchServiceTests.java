package com.github.wuchao.webproject.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchServiceTests {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Test
    public void testDeleteIndices() {
        elasticSearchService.deleteIndices("dam-staging", 3);
    }

}
