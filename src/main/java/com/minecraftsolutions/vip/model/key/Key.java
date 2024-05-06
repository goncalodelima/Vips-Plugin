package com.minecraftsolutions.vip.model.key;

import com.minecraftsolutions.vip.model.vip.Vip;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class Key {

    private @NonNull String name;
    private @NonNull Vip vip;
    private long time;

}
