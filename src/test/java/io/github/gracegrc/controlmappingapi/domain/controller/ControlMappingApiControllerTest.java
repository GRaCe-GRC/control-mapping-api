package io.github.gracegrc.controlmappingapi.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingResponseDto;
import io.github.gracegrc.controlmappingapi.domain.model.dto.ControlMappingUpdateRequestDto;
import io.github.gracegrc.controlmappingapi.domain.model.exception.ControlMappingAlreadyExistsException;
import io.github.gracegrc.controlmappingapi.domain.model.exception.ControlMappingNotFoundException;
import io.github.gracegrc.controlmappingapi.domain.service.ControlMappingService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ControlMappingApiController.class)
class ControlMappingApiControllerTest {

	private static final UUID ID = UUID.fromString("4e5f6a7b-8c9d-4e0f-a1b2-c3d4e5f6a7b8");

	private static final UUID REQUIREMENT_ID = UUID.fromString("1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d");

	private static final UUID OTHER_REQUIREMENT_ID = UUID.fromString("6f5e4d3c-2b1a-4f9e-8d7c-6b5a4f3e2d1c");

	private static final UUID CONTROL_ID = UUID.fromString("7c3d2e1f-0a9b-4c8d-8e7f-6a5b4c3d2e1f");

	private static final ControlMappingResponseDto MAPPING = new ControlMappingResponseDto(ID, REQUIREMENT_ID,
			CONTROL_ID, "Covers it.", Instant.parse("2026-10-01T10:15:30Z"), "admin-console");

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private ControlMappingService controlMappingService;

	@Test
	void postCreatesTheMappingAndReturnsAnEmptyBody() {
		given(controlMappingService
			.create(new ControlMappingRequestDto(REQUIREMENT_ID, CONTROL_ID, "Covers it.", "admin-console")))
			.willReturn(ID);

		assertThat(mvc.post()
			.uri("/api/v1/control-mappings")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"requirementId\":\"" + REQUIREMENT_ID + "\",\"controlId\":\"" + CONTROL_ID
					+ "\",\"rationale\":\"Covers it.\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/control-mappings/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post()
			.uri("/api/v1/control-mappings")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"controlId\":\"" + CONTROL_ID + "\",\"createdBy\":\"\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"requirementId\":\"must not be null\","
					+ "\"createdBy\":\"must not be blank\"}}");
		then(controlMappingService).shouldHaveNoInteractions();
	}

	@Test
	void postWithADuplicatePairReturnsConflict() {
		given(controlMappingService.create(any()))
			.willThrow(new ControlMappingAlreadyExistsException(REQUIREMENT_ID, CONTROL_ID));

		assertThat(mvc.post()
			.uri("/api/v1/control-mappings")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"requirementId\":\"" + REQUIREMENT_ID + "\",\"controlId\":\"" + CONTROL_ID
					+ "\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":409,\"detail\":\"Requirement " + REQUIREMENT_ID
					+ " is already mapped to control " + CONTROL_ID + "\"}");
	}

	@Test
	void getReturnsTheList() {
		given(controlMappingService.findAll(null, null)).willReturn(List.of(MAPPING));

		assertThat(mvc.get().uri("/api/v1/control-mappings")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"controlId\":\"" + CONTROL_ID + "\"}]");
	}

	@Test
	void getWithRepeatedRequirementIdsPassesEveryIdToTheService() {
		given(controlMappingService.findAll(List.of(REQUIREMENT_ID, OTHER_REQUIREMENT_ID), null))
			.willReturn(List.of(MAPPING));

		assertThat(mvc.get()
			.uri("/api/v1/control-mappings?requirementId={a}&requirementId={b}", REQUIREMENT_ID, OTHER_REQUIREMENT_ID))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\"}]");
	}

	@Test
	void getWithRequirementIdAndControlIdPassesBothToTheService() {
		given(controlMappingService.findAll(List.of(REQUIREMENT_ID), CONTROL_ID)).willReturn(List.of());

		assertThat(mvc.get()
			.uri("/api/v1/control-mappings?requirementId={r}&controlId={c}", REQUIREMENT_ID, CONTROL_ID))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[]");
	}

	@Test
	void getWithAMalformedRequirementIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/v1/control-mappings?requirementId=nope")).hasStatus(HttpStatus.BAD_REQUEST);
		then(controlMappingService).shouldHaveNoInteractions();
	}

	@Test
	void getByIdReturnsTheMapping() {
		given(controlMappingService.findById(ID)).willReturn(MAPPING);

		assertThat(mvc.get().uri("/api/v1/control-mappings/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"requirementId\":\"" + REQUIREMENT_ID
					+ "\",\"rationale\":\"Covers it.\"}");
	}

	@Test
	void getByIdWithAnUnknownIdReturnsNotFound() {
		given(controlMappingService.findById(ID)).willThrow(new ControlMappingNotFoundException(ID));

		assertThat(mvc.get().uri("/api/v1/control-mappings/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"Control mapping " + ID + " not found\"}");
	}

	@Test
	void getByIdWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/v1/control-mappings/not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
		then(controlMappingService).shouldHaveNoInteractions();
	}

	@Test
	void putUpdatesTheRationale() {
		ControlMappingResponseDto updated = new ControlMappingResponseDto(ID, REQUIREMENT_ID, CONTROL_ID,
				"Covers quarterly reviews.", MAPPING.createdAt(), "admin-console");
		given(controlMappingService.update(ID, new ControlMappingUpdateRequestDto("Covers quarterly reviews.")))
			.willReturn(updated);

		assertThat(mvc.put()
			.uri("/api/v1/control-mappings/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"rationale\":\"Covers quarterly reviews.\"}"))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"rationale\":\"Covers quarterly reviews.\"}");
	}

	@Test
	void putWithAnUnknownIdReturnsNotFound() {
		given(controlMappingService.update(eq(ID), any())).willThrow(new ControlMappingNotFoundException(ID));

		assertThat(mvc.put()
			.uri("/api/v1/control-mappings/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"rationale\":\"x\"}"))
			.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/v1/control-mappings/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT);
		then(controlMappingService).should().delete(ID);
	}

	@Test
	void deleteWithAnUnknownIdReturnsNotFound() {
		willThrow(new ControlMappingNotFoundException(ID)).given(controlMappingService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/control-mappings/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND);
	}

}
