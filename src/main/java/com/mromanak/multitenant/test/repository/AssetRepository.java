package com.mromanak.multitenant.test.repository;

import com.mromanak.multitenant.test.model.entity.Asset;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * A basic {@link PagingAndSortingRepository} for {@link Asset Assets}.
 * <p/>
 * TODO: Implement more complex query functionality to show off some Spring Data magic
 */
@Repository
public interface AssetRepository extends PagingAndSortingRepository<Asset, UUID> {
}
