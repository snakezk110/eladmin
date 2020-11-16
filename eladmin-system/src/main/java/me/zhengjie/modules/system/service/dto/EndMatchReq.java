package me.zhengjie.modules.system.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class EndMatchReq {
    private String phone;
    private Long match_id;
    private Map<String,Double> result;
    private double result_total;
}
