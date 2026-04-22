package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

import ch.uzh.ifi.hase.soprafs26.entity.Game;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStateGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GamePostDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

	@Mapping(source = "username", target = "username")
	@Mapping(source = "password", target = "password")
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "creationDate", target = "creationDate")
	@Mapping(source = "points", target = "points")
	@Mapping(source = "token", target = "token")
	UserGetDTO convertEntityToUserGetDTO(User user);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "hostname", target = "hostname")
	@Mapping(source = "code", target = "code")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "currentRound", target = "currentRound")
	@Mapping(source = "maxRounds", target = "maxRounds")
	@Mapping(source = "currentQuestionId", target = "currentQuestionId")
	@Mapping(source = "stageDeadline", target = "stageDeadline")
	@Mapping(target = "answerSubmitted", ignore = true)
	@Mapping(target = "voteSubmitted", ignore = true)
	@Mapping(source = "players", target = "players")
	@Mapping(source = "players", target = "scores")
	GameGetDTO convertEntityToGameGetDTO(Game game);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "hostname", target = "hostname")
	@Mapping(source = "code", target = "code")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "status", target = "stage")
	@Mapping(source = "currentRound", target = "currentRound")
	@Mapping(source = "maxRounds", target = "maxRounds")
	@Mapping(source = "currentQuestionId", target = "currentQuestionId")
	@Mapping(source = "stageDeadline", target = "stageDeadline")
	@Mapping(target = "answerSubmitted", ignore = true)
	@Mapping(target = "voteSubmitted", ignore = true)
	@Mapping(target = "question", ignore = true)
	@Mapping(target = "answers", ignore = true)
	@Mapping(target = "submittedUsernames", ignore = true)
	@Mapping(source = "players", target = "players")
	@Mapping(source = "players", target = "scores")
	GameStateGetDTO convertEntityToGameStateGetDTO(Game game);
}
