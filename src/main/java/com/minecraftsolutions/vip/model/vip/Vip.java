package com.minecraftsolutions.vip.model.vip;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class Vip {

    private String identifier;
    private String name;
    private String color;
    private List<String> setCommands;
    private List<String> removeCommands;
    private String roleId;

}
