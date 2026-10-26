package io.github.gracegrc.controlmappingapi.domain.repository;

import io.github.gracegrc.controlmappingapi.domain.model.entity.ControlMappingEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface ControlMappingRepository extends ListCrudRepository<ControlMappingEntity, UUID> {

	List<ControlMappingEntity> findByRequirementIdIn(Collection<UUID> requirementIds);

	List<ControlMappingEntity> findByControlId(UUID controlId);

	List<ControlMappingEntity> findByRequirementIdInAndControlId(Collection<UUID> requirementIds, UUID controlId);

}
