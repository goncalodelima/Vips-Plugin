package com.minecraftsolutions.vip.model.user;

import com.minecraftsolutions.vip.model.vip.Vip;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
public class User {

    private String name;
    private List<Vip> vips;
    private Vip enabledVip;
    private Map<Vip, Long> time;

}
