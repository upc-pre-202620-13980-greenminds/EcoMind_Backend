package pe.greenminds.ecomind.community.application.commandservices;

public interface EventLifecycleService {
    void delete(Long eventId);

    void expireMessages(Long eventId);
}