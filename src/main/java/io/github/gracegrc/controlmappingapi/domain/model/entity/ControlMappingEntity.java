package io.github.gracegrc.controlmappingapi.domain.model.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Table("control_mappings")
public record ControlMappingEntity(
		@Id UUID id,
		UUID requirementId,
		UUID controlId,
		String rationale,
		@InsertOnlyProperty Instant createdAt,
		@InsertOnlyProperty String createdBy) {
}
