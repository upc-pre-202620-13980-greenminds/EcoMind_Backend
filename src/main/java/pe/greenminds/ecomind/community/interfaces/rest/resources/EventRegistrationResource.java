package pe.greenminds.ecomind.community.interfaces.rest.resources;
public record EventRegistrationResource(Long id,Long event_id,Long user_id,String registration_type,Long family_id,Integer participant_count,String status){}
