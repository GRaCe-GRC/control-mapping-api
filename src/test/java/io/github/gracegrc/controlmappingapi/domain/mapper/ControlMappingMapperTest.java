package io.github.gracegrc.controlmappingapi.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingResponseDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.entity.ControlMappingEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ControlMappingMapperTest {

	private static final UUID ID = UUID.fromString("4e5f6a7b-8c9d-4e0f-a1b2-c3d4e5f6a7b8");

	private static final UUID REQUIREMENT_ID = UUID.fromString("1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d");

	private static final UUID CONTROL_ID = UUID.fromString("7c3d2e1f-0a9b-4c8d-8e7f-6a5b4c3d2e1f");

	private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");

	@Test
	void toEntityFromRequestHasNoIdAndUsesTheGivenCreatedAt() {
		ControlMappingRequestDto request = new ControlMappingRequestDto(REQUIREMENT_ID, CONTROL_ID,
				"Covers account reviews.", "admin-console");

		ControlMappingEntity entity = ControlMappingMapper.toEntity(request, CREATED_AT);

		assertThat(entity).isEqualTo(new ControlMappingEntity(null, REQUIREMENT_ID, CONTROL_ID,
				"Covers account reviews.", CREATED_AT, "admin-console"));
	}

	@Test
	void toEntityFromUpdateChangesOnlyTheRationale() {
		ControlMappingEntity existing = new ControlMappingEntity(ID, REQUIREMENT_ID, CONTROL_ID, null, CREATED_AT,
				"admin-console");

		ControlMappingEntity entity = ControlMappingMapper.toEntity(existing,
				new ControlMappingUpdateRequestDto("Covers account reviews."));

		assertThat(entity).isEqualTo(new ControlMappingEntity(ID, REQUIREMENT_ID, CONTROL_ID,
				"Covers account reviews.", CREATED_AT, "admin-console"));
	}

	@Test
	void toResponseDtoCopiesEveryField() {
		ControlMappingEntity entity = new ControlMappingEntity(ID, REQUIREMENT_ID, CONTROL_ID, "Covers it.",
				CREATED_AT, "admin-console");

		ControlMappingResponseDto response = ControlMappingMapper.toResponseDto(entity);

		assertThat(response).isEqualTo(new ControlMappingResponseDto(ID, REQUIREMENT_ID, CONTROL_ID, "Covers it.",
				CREATED_AT, "admin-console"));
	}

}
