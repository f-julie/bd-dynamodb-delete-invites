package com.amazon.ata.dynamodbdeleteiterators.classroom.activity;

import com.amazon.ata.dynamodbdeleteiterators.classroom.dao.EventDao;
import com.amazon.ata.dynamodbdeleteiterators.classroom.dao.InviteDao;
import com.amazon.ata.dynamodbdeleteiterators.classroom.dao.models.CanceledInvite;
import com.amazon.ata.dynamodbdeleteiterators.classroom.dao.models.Event;
import com.amazon.ata.dynamodbdeleteiterators.classroom.dao.models.Invite;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.inject.Inject;

/**
 * Handles requests to get invites for a given member.
 */
public class GetInvitesForMemberActivity {
    private InviteDao inviteDao;
    private EventDao eventDao;

    /**
     * Constructs an Activity with the given DAO.
     * @param inviteDao The InviteDao to use to fetch invites
     */
    @Inject
    public GetInvitesForMemberActivity(InviteDao inviteDao, EventDao eventDao) {
        this.inviteDao = inviteDao;
        this.eventDao = eventDao;
    }

    /**
     * Fetches all invites sent to a given member.
     *
     * NOTE: A little deviation from usual.
     * Here we're using values directly in our arguments and return value,
     * whereas in a typical Coral service we'd have Request/Result objects
     * that would be generated from configuration via Coral. We haven't
     * created service infrastructure for this activity, so we're just
     * using the values directly.
     *
     * @param memberId The ID of the member to fetch invites for
     * @return List of Invites sent to the member (if any found)
     */
    public List<Invite> handleRequest(final String memberId) {
        List<Invite> invites = inviteDao.getInvitesSentToMember(memberId);

        // Store results locally so exception is thrown here
        // before the other loop that modifies the database data
        Map<String, Event> eventMap = new HashMap<>();
        for (Invite invite : invites) {
            eventMap.put(invite.getEventId(), eventDao.getEvent(invite.getEventId()));
        }

        ListIterator<Invite> inviteListIterator = invites.listIterator();
        while (inviteListIterator.hasNext()) {
            Invite invite = inviteListIterator.next();
            Event event = eventMap.get(invite.getEventId());

            if (event.isCanceled()) {
                inviteDao.cancelInvite(event.getId(), invite.getMemberId());
                inviteListIterator.remove();
                inviteListIterator.add(new CanceledInvite(invite));
            }
        }

        return invites;
    }
}
