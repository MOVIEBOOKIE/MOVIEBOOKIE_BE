package project.luckybooky.domain.user.service;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.user.dto.request.UserTypeAssignRequest;
import project.luckybooky.domain.user.dto.response.UserTypeAssignResponse;
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

    @Transactional
    public UserTypeAssignResponse assignCurrentUser(UserTypeAssignRequest req) {

        User user = userRepo.findByEmail(AuthenticatedUserUtils.getAuthenticatedUserEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        GroupType group = req.getStep2Question().getGroup();               // STEP 2 → 그룹
        ContentCategory favoriteCategory = req.getFavoriteCategory();                   // STEP 3 → 콘텐츠

        UserType userType = Arrays.stream(UserType.values())
                .filter(t -> t.getCategory() == favoriteCategory && t.getGroup() == group)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_TYPE_NOT_FOUND));

        user.setUserType(userType);
        userRepo.save(user);

        return new UserTypeAssignResponse(
                userType.name(),      // "MOVIE_TRENDY_VIEWER"
                userType.getLabel(),  // "🍿 핫플릭스만 골라보는 감각 감상러"
                group);
    }

}