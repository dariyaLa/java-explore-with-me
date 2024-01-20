package ru.practicum.users;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import ru.practicum.ServiceMain;
import ru.practicum.exception.ConflictException;

import java.util.Collection;
import java.util.stream.Collectors;

import static ru.practicum.constants.Constant.USER_DUPLICATE_EXCEPTION;

@Service
@RequiredArgsConstructor
public class UserService implements ServiceMain<UserDto, UserDtoOut> {

    private final UserRepoImpl repository;

    @Override
    public UserDtoOut add(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        try {
            user = repository.add(user);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(String.format(USER_DUPLICATE_EXCEPTION, userDto.getName()));
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDtoOut update(UserDto userDto, Long id) {
        return UserMapper.toUserDto(repository.update(UserMapper.toUser(userDto), id));
    }

    @Override
    public UserDtoOut find(Long id) {
        return UserMapper.toUserDto(repository.find(id));
    }

    @Override
    public Collection<UserDtoOut> findAll(Integer from, Integer size) {
        return repository.findAll(from, size).stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

    public Collection<UserDtoOut> findAllIds(Collection<Long> ids, Integer from, Integer size) {
        Collection<User> users = repository.findAllIds(ids, from, size);
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public void changeProfile(long userId, boolean isPublicProfile) {
        repository.changeProfile(userId, isPublicProfile);
    }
}
