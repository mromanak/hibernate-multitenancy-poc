package com.mromanak.multitenant.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mromanak.multitenant.test.model.dto.AssetDto;
import com.mromanak.multitenant.test.model.entity.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * A service that performs basic CRUD operations for {@link Asset Assets}.
 */
@Service
public class AssetService extends AbstractEntityCrudService<UUID, AssetDto, Asset> {

    @Autowired
    public AssetService(ObjectMapper objectMapper, PagingAndSortingRepository<Asset, UUID> repository) {
        super(objectMapper, repository, AssetDto.class, Asset.class);
    }

    @Override
    protected void applyUpdates(AssetDto updateDto, Asset entity) {
        Asset updateEntity = toEntity(updateDto);
        entity.setName(updateEntity.getName());
        entity.setCreatedTimestamp(updateEntity.getCreatedTimestamp());
        entity.setServerUrl(updateEntity.getServerUrl());
    }
}
