package com.minecraftsolutions.vip.model.user;

import com.minecraftsolutions.vip.model.vip.Vip;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Data
public class User {

    private UUID uuid;
    private String name;
    private Vip enabledVip;
    private Map<Vip, Long> time;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(uuid, user.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

}
