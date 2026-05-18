package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
 
import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Friend;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.Invite;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRepository;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.InviteRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendsDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InviteDataGetDTO;
 
import java.util.List;
import java.util.Optional;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
public class FriendServiceTest {
 
    @Mock
    private FriendRepository friendRepository;
 
    @Mock
    private FriendRequestRepository friendRequestRepository;
 
    @Mock
    private UserRepository userRepository;
 
    @Mock
    private InviteRepository inviteRepository;
 
    @InjectMocks
    private FriendService friendService;
 
    private User sender;
    private User receiver;
 
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
 
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");
        sender.setStatus(UserStatus.ONLINE);
 
        receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");
        receiver.setStatus(UserStatus.OFFLINE);
    }
 
 
    @Test
    public void getFriendsData_returnsCorrectDTO() {
        Friend friend = new Friend();
        friend.setId(10L);
        friend.setSenderUsername("sender");
        friend.setReceiverUsername("other");
        friend.setStatus("ONLINE");
 
        FriendRequest incoming = new FriendRequest();
        FriendRequest outgoing = new FriendRequest();
 
        when(friendRepository.findBySenderUsernameOrReceiverUsername("sender", "sender"))
                .thenReturn(List.of(friend));
        when(friendRequestRepository.findByReceiverId(1L)).thenReturn(List.of(incoming));
        when(friendRequestRepository.findBySenderId(1L)).thenReturn(List.of(outgoing));
 
        FriendsDataGetDTO result = friendService.getFriendsData(sender);
 
        assertNotNull(result);
        assertEquals(1, result.getFriends().size());
        assertEquals(1, result.getIncomingRequests().size());
        assertEquals(1, result.getOutgoingRequests().size());
    }
 
    @Test
    public void getFriendsData_normalizesFlippedFriendship() {
        Friend friend = new Friend();
        friend.setId(10L);
        friend.setSenderUsername("other");
        friend.setReceiverUsername("sender"); // user is receiver 
        friend.setStatus("ONLINE");
 
        when(friendRepository.findBySenderUsernameOrReceiverUsername("sender", "sender"))
                .thenReturn(List.of(friend));
        when(friendRequestRepository.findByReceiverId(1L)).thenReturn(List.of());
        when(friendRequestRepository.findBySenderId(1L)).thenReturn(List.of());
 
        FriendsDataGetDTO result = friendService.getFriendsData(sender);
 
        Friend normalized = result.getFriends().get(0);
        assertEquals("sender", normalized.getSenderUsername());
        assertEquals("other", normalized.getReceiverUsername());
    }

 
    @Test
    public void sendFriendRequest_validRequest_success() {
        when(userRepository.findByUsername("receiver")).thenReturn(receiver);
        when(friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(friendRepository.findBySenderUsernameAndReceiverUsername("sender", "receiver")).thenReturn(null);
 
        friendService.sendFriendRequest(sender, "receiver");
 
        verify(friendRequestRepository, times(1)).save(Mockito.any(FriendRequest.class));
    }
 
    @Test
    public void sendFriendRequest_toSelf_throwsConflict() {
		when(userRepository.findByUsername("sender")).thenReturn(sender);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> friendService.sendFriendRequest(sender, sender.getUsername()));
 
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
 
    @Test
    public void sendFriendRequest_duplicateRequest_throwsConflict() {
        FriendRequest existing = new FriendRequest();
        when(userRepository.findByUsername("receiver")).thenReturn(receiver);
        when(friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(existing);
 
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> friendService.sendFriendRequest(sender, "receiver"));
 
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
 
    @Test
    public void sendFriendRequest_alreadyFriends_throwsConflict() {
        Friend existing = new Friend();
        when(userRepository.findByUsername("receiver")).thenReturn(receiver);
        when(friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(null);
        when(friendRepository.findBySenderUsernameAndReceiverUsername("sender", "receiver")).thenReturn(existing);
 
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> friendService.sendFriendRequest(sender, "receiver"));
 
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
 
    @Test
    public void acceptFriend_pendingRequest_createsFriendship() {
        FriendRequest request = new FriendRequest();
        request.setStatus(FriendRequestStatus.PENDING);
        request.setSenderUsername("sender");
        request.setReceiverId(2L);
 
        when(friendRequestRepository.findFriendRequestById(1L)).thenReturn(request);
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(friendRepository.save(Mockito.any(Friend.class))).thenAnswer(i -> i.getArgument(0));
 
        friendService.acceptFriend(1L);
 
        assertEquals(FriendRequestStatus.ACCEPTED, request.getStatus());
        verify(friendRepository, times(1)).save(Mockito.any(Friend.class));
    }
 
    @Test
    public void acceptFriend_alreadyAccepted_throwsConflict() {
        FriendRequest request = new FriendRequest();
        request.setStatus(FriendRequestStatus.ACCEPTED);
 
        when(friendRequestRepository.findFriendRequestById(1L)).thenReturn(request);
 
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> friendService.acceptFriend(1L));
 
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
 
 
    @Test
    public void declineFriend_pendingRequest_setsDeclined() {
        FriendRequest request = new FriendRequest();
        request.setStatus(FriendRequestStatus.PENDING);
 
        when(friendRequestRepository.findFriendRequestById(1L)).thenReturn(request);
 
        friendService.declineFriend(1L);
 
        assertEquals(FriendRequestStatus.DECLINED, request.getStatus());
    }
 
    @Test
    public void declineFriend_alreadyDeclined_throwsConflict() {
        FriendRequest request = new FriendRequest();
        request.setStatus(FriendRequestStatus.DECLINED);
 
        when(friendRequestRepository.findFriendRequestById(1L)).thenReturn(request);
 
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> friendService.declineFriend(1L));
 
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
 
 
    @Test
    public void removeFriend_existingFriendship_deletesAll() {
        Friend friendship = new Friend();
        friendship.setSenderUsername("sender");
        friendship.setReceiverUsername("receiver");
 
        FriendRequest request = new FriendRequest();
 
        when(friendRepository.findFriendById(10L)).thenReturn(friendship);
        when(userRepository.findByUsername("sender")).thenReturn(sender);
        when(userRepository.findByUsername("receiver")).thenReturn(receiver);
        when(friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(request);
 
        friendService.removeFriend(10L);
 
        verify(friendRepository, times(1)).delete(friendship);
        verify(friendRequestRepository, times(1)).delete(request);
    }
 
 
    @Test
    public void inviteFriend_validInvite_success() {
        when(userRepository.findByUsername("receiver")).thenReturn(receiver);
        when(inviteRepository.findBySenderUsernameAndReceiverUsername("sender", "receiver")).thenReturn(null);
 
        friendService.inviteFriend(sender, "receiver", "GAME123");
 
        verify(inviteRepository, times(1)).save(Mockito.any(Invite.class));
    }
 
    @Test
    public void inviteFriend_duplicateInvite_throwsConflict() {
        Invite existing = new Invite();
        when(userRepository.findByUsername("receiver")).thenReturn(receiver);
        when(inviteRepository.findBySenderUsernameAndReceiverUsername("sender", "receiver")).thenReturn(existing);
 
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> friendService.inviteFriend(sender, "receiver", "GAME123"));
 
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
 
 
    @Test
    public void acceptInvite_pendingInvite_returnsGameCode() {
        Invite invite = new Invite();
        invite.setStatus(InviteStatus.PENDING);
        invite.setGameCode("GAME123");
 
        when(inviteRepository.findInviteById(1L)).thenReturn(invite);
 
        String code = friendService.acceptInvite(1L);
 
        assertEquals("GAME123", code);
        assertEquals(InviteStatus.ACCEPTED, invite.getStatus());
    }
 
    @Test
    public void acceptInvite_alreadyAccepted_throwsConflict() {
        Invite invite = new Invite();
        invite.setStatus(InviteStatus.ACCEPTED);
 
        when(inviteRepository.findInviteById(1L)).thenReturn(invite);
 
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> friendService.acceptInvite(1L));
 
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
 
 
    @Test
    public void declineInvite_pendingInvite_deletesInvite() {
        Invite invite = new Invite();
        invite.setStatus(InviteStatus.PENDING);
 
        when(inviteRepository.findInviteById(1L)).thenReturn(invite);
 
        friendService.declineInvite(1L);
 
        verify(inviteRepository, times(1)).delete(invite);
    }
 
    @Test
    public void declineInvite_alreadyDeclined_throwsConflict() {
        Invite invite = new Invite();
        invite.setStatus(InviteStatus.DECLINED);
 
        when(inviteRepository.findInviteById(1L)).thenReturn(invite);
 
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> friendService.declineInvite(1L));
 
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
 
 
    @Test
    public void getInvites_returnsAllForReceiver() {
        Invite invite1 = new Invite();
        Invite invite2 = new Invite();
 
        when(inviteRepository.findByReceiverUsername("sender")).thenReturn(List.of(invite1, invite2));
 
        InviteDataGetDTO result = friendService.getInvites(sender);
 
        assertNotNull(result);
        assertEquals(2, result.getInvites().size());
    }
}