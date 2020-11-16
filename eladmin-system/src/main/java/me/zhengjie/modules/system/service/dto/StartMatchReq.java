package me.zhengjie.modules.system.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartMatchReq {
    private String phone;
    private Long match_id;
}
