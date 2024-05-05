package com.minecraftsolutions.vip.model.vip.service;

import com.minecraftsolutions.vip.model.vip.Vip;

import java.util.List;

public interface VipFoundationService {

    void put(Vip vip);

    Vip get(String identifier);

    List<Vip> getAll();

}
