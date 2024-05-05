package com.minecraftsolutions.vip.model.key;

import com.minecraftsolutions.vip.model.vip.Vip;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Key {

    private String name;
    private Vip vip;
    private long time;

}
