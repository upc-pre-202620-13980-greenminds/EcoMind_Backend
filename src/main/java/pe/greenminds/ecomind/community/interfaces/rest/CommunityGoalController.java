package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.commandservices.CommunityGoalCommandService;
import pe.greenminds.ecomind.community.application.queryservices.CommunityGoalQueryService;
import pe.greenminds.ecomind.community.domain.model.commands.IncrementCommunityGoalCommand;
import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunityGoalsQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.*;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CommunityGoalResourceFromEntityAssembler;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CreateCommunityGoalCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@Tag(name="Community Goal",description="Goals tracked by a community")
@RequestMapping(value="/api/v1/Community/Community-goals",produces=MediaType.APPLICATION_JSON_VALUE)
public class CommunityGoalController{
 private final CommunityGoalCommandService commands;
 private final CommunityGoalQueryService queries;
 private final ResponseEntityAssembler responses;

 public CommunityGoalController(CommunityGoalCommandService c,CommunityGoalQueryService q,ResponseEntityAssembler r){
  commands=c;queries=q;responses=r;
 }

 @GetMapping
 public List<CommunityGoalResource> list(@RequestParam(required=false)Long community_id){
  return queries.handle(new SearchCommunityGoalsQuery(community_id)).stream().map(CommunityGoalResourceFromEntityAssembler::toResourceFromEntity).toList();
 }

 @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE)
 public ResponseEntity<?> create(@Valid @RequestBody CreateCommunityGoalResource r){
  var c=CreateCommunityGoalCommandFromResourceAssembler.toCommandFromResource(r);
  return responses.toResponseEntityFromResult(commands.handle(c),CommunityGoalResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.CREATED);
 }

 @PatchMapping("/{id}/progress")
 public ResponseEntity<?> increment(@PathVariable Long id,@RequestParam Long user_id){
  return responses.toResponseEntityFromResult(commands.handle(new IncrementCommunityGoalCommand(id,user_id)),CommunityGoalResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.OK);
 }
}
