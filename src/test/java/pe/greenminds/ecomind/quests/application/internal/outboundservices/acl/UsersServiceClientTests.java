package pe.greenminds.ecomind.quests.application.internal.outboundservices.acl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade;

class UsersServiceClientTests {

    private UsersContextFacade usersContextFacade;
    private UsersServiceClient usersServiceClient;

    @BeforeEach
    void setUp() {
        usersContextFacade = mock(UsersContextFacade.class);
        usersServiceClient = new UsersServiceClient(usersContextFacade);
    }

    @Test
    void shouldDelegateUserFriendshipAndFamilyQueriesToUsersFacade() {
        when(usersContextFacade.existsUser(2L)).thenReturn(true);
        when(usersContextFacade.areFriends(1L, 2L)).thenReturn(true);
        when(usersContextFacade.isFamilyMember(10L, 2L)).thenReturn(true);

        assertTrue(usersServiceClient.existsUser(2L));
        assertTrue(usersServiceClient.areFriends(1L, 2L));
        assertTrue(usersServiceClient.isFamilyMember(10L, 2L));

        verify(usersContextFacade).existsUser(2L);
        verify(usersContextFacade).areFriends(1L, 2L);
        verify(usersContextFacade).isFamilyMember(10L, 2L);
    }

    @Test
    void shouldRecognizeUsersFromTheSameFamily() {
        when(usersContextFacade.getFamilyIdOfUser(1L)).thenReturn(Optional.of(10L));
        when(usersContextFacade.isFamilyMember(10L, 2L)).thenReturn(true);

        assertTrue(usersServiceClient.belongToSameFamily(1L, 2L));
    }

    @Test
    void shouldRejectSameFamilyCheckWhenFirstUserHasNoFamily() {
        when(usersContextFacade.getFamilyIdOfUser(1L)).thenReturn(Optional.empty());

        assertFalse(usersServiceClient.belongToSameFamily(1L, 2L));
    }
}
