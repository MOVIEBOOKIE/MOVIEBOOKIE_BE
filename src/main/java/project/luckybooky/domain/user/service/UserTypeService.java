package project.luckybooky.domain.user.service;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.user.converter.UserConverter;
import project.luckybooky.domain.user.dto.request.UserTypeAssignRequest;
import project.luckybooky.domain.user.dto.response.UserTypeAssignResponse;
import project.luckybooky.domain.user.dto.response.UserTypeResultDTO;
import project.luckybooky.domain.user.entity.ContentCategory;
import project.luckybooky.domain.user.entity.GroupType;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.entity.UserType;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class UserTypeService {

    private final UserRepository userRepo;

    /* 1) 유형검사 수행 & 저장 */
    @Transactional
    public UserTypeAssignResponse assignCurrentUser(UserTypeAssignRequest req) {

        User user = userRepo.findByEmail(AuthenticatedUserUtils.getAuthenticatedUserEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        /* STEP 2 → Group */
        GroupType group = req.getStep2Question().getGroup();   // 메서드명 확인!

        /* STEP 3 × Group → UserType */
        UserType userType = Arrays.stream(UserType.values())
                .filter(t -> t.getCategory() == req.getFavoriteCategory()
                        && t.getGroup()    == group)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_TYPE_NOT_FOUND));

        /* 저장 */
        user.setGroupType(group);
        user.setUserType(userType);
        userRepo.save(user);

        /* Converter 이용 */
        return UserConverter.toAssignResponse(userType);
    }

    /* 2) 유형검사 결과 조회 */
    @Transactional(readOnly = true)
    public UserTypeResultDTO getCurrentUserType() {

        User user = userRepo.findByEmail(AuthenticatedUserUtils.getAuthenticatedUserEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() == null || user.getGroupType() == null) {
            throw new BusinessException(ErrorCode.USER_TYPE_NOT_ASSIGNED);
        }

        return UserConverter.toResultDTO(user);
    }
}