package com.github.wuchao.webproject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wuchao
 * @date 2019/2/28 10:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long userId;

    private String username;

}
