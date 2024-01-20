package ru.practicum.constants;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class Constant {

    public static ZoneOffset getZoneOffset() {
        return OffsetDateTime.now().getOffset();
    }

    //для успешного ответа
    public static final String SUCCESSFUL = "SUCCESSFUL";

    //ключи для фильтра
    public static final String USERS = "users";
    public static final String STATES = "states";
    public static final String CATEGORIES = "categories";

    public static final String START = "start";

    public static final String END = "end";

    public static final String FROM = "from";

    public static final String SIZE = "size";

    public static final String IS_PUBLISHED = "isPublished";

    public static final String TEXT = "text";

    public static final String PAID = "paid";

    public static final String SORT = "sort";

    public static final String INITIATORS = "initiator";

    public static final String USER = "user";

    public static final String ONLY_AVAILABLE = "onlyAvailable";

    public static final String IP = "ip";

    //константы для exception
    public static final String CATEGORY_DUPLICATE_EXCEPTION = "Категория с названием %s уже существует";

    public static final String CATEGORY_WITH_EVENTS_EXCEPTION = "Категория с id=%s не может быть удалена. " +
            "К категории привязано(ы) %s событие(й)";
    public static final String USER_DUPLICATE_EXCEPTION = "Пользователь с именем %s уже существует";

    public static final String REQUEST_DUPLICATE_EXCEPTION = "Уже был запрос на участие в событии с id=%s от " +
            "пользователя с id=%s";

    public static final String REQUEST_INITIATOR_EXCEPTION = "Пользователь, инициирующий событие, не может " +
            "создать заявку на участие в нём же. Событие id=%s, пользователь id=%s";

    public static final String EVENT_OVERFULL_PARTICIPANT_EXCEPTION = "Превышен лимит " +
            "запросов на участие в событии с id=%s";

    public static final String EVENT_PUBLISHED_EDIT_EXCEPTION = "Недоступно изменение уже опубликованного события с id=%s";

}
