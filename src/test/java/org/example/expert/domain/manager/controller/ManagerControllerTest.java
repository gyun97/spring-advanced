package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerController.class)
class ManagerControllerTest {

    @MockBean
    private ManagerService managerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    // UserReole에서 IRE 발생하는데 원인을 못 찾겠음...
    @Test
    void 일정에_담당자_배치() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;
        User user = User.fromAuthUser(authUser);

        Todo todo = new Todo("title", "content", "sunny", user);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);
        User managerUser = new User("email", "password", UserRole.USER);
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);


        Manager manager = new Manager(managerUser, todo);
        ManagerSaveResponse managerSaveResponse = new ManagerSaveResponse(
                manager.getId(),
                new UserResponse(managerUser.getId(),
                        managerUser.getEmail(
                        )));

        given(managerService.saveManager(authUser, todoId, managerSaveRequest)).willReturn(managerSaveResponse);

        // when & then
        ResultActions result = mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(managerSaveRequest)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void 일정의_매니저_목록_조회() throws Exception {
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        Manager manager = new Manager(user, todo);
        ManagerResponse managerResponse = new ManagerResponse(
                manager.getId(),
                new UserResponse(user.getId(), user.getEmail())
        );

        List<ManagerResponse> responseList = List.of(managerResponse);

        given(managerService.getManagers(todoId)).willReturn(responseList);

        // when & then
        ResultActions result = mockMvc.perform(get("/todos/{todoId}/managers", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(managerResponse.getId()))
                .andExpect(jsonPath("$[0].user.id").value(managerResponse.getUser().getId()))
                .andExpect(jsonPath("$[0].user.email").value(managerResponse.getUser().getEmail()))
                .andDo(print());
    }

    @Test
    void 일정에서_담당자_삭제() throws Exception {
        // given
        long todoId = 1L;
        long managerId = 2L;
        long userId = 1L;

        // request.getAttribute("userId")를 통해 userId를 가져오는 부분을 Mock으로 설정
        MockHttpServletRequestBuilder requestBuilder = delete("/todos/{todoId}/managers/{managerId}", todoId, managerId)
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("userId", userId); // request에 userId 설정

        // managerService.deleteManager 호출에 대한 Mock 설정
        doNothing().when(managerService).deleteManager(userId, todoId, managerId);

        // when & then
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk()) // 삭제가 정상적으로 처리될 때 200 상태 코드 기대
                .andDo(print());

        // managerService.deleteManager 호출 확인
        verify(managerService, times(1)).deleteManager(userId, todoId, managerId);
    }




}