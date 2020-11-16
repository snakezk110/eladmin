package me.zhengjie.modules.system.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserReq {
    private String username;
    private String nickName;
    private String email;
    private String phone;
    private String phoneCode;
    private Long school_id;
    private String password;

}
