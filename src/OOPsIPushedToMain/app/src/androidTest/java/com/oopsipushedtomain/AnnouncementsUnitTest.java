package com.oopsipushedtomain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.oopsipushedtomain.Announcements.Announcement;

import org.junit.Test;

import java.util.ArrayList;

public class AnnouncementsUnitTest {

    private ArrayList<Announcement> mockAnnouncementList() {
        ArrayList<Announcement> announcementList = new ArrayList<>();
        announcementList.add(mockAnnouncement());
        return announcementList;
    }
    private Announcement mockAnnouncement() {
        return new Announcement("title", "body", "imageId", "eventId", "anmtId");
    }

    @Test
    public void testAdd() {
        ArrayList<Announcement> announcementList = mockAnnouncementList();
        assertEquals(1, announcementList.size());
        Announcement announcement = new Announcement("Title2", "Body2", "imageId2", "eventId2", "anmtId2");
        announcementList.add(announcement);
        assertEquals(2, announcementList.size());
        assertTrue(announcementList.contains(announcement));
    }

    @Test
    public void testGetAnnouncements() {
        ArrayList<Announcement> announcementList = mockAnnouncementList();
        Announcement announcement = new Announcement("Title2", "Body2", "imageId2", "eventId2", "anmtId2");
        announcementList.add(announcement);
        assertSame(announcement, announcementList.get(1));
        assertNotEquals(mockAnnouncement(), announcementList.get(1));
    }

    @Test
    public void testHasAnnouncement() {
        ArrayList<Announcement> announcementList = mockAnnouncementList();
        Announcement announcementShouldExist = new Announcement("Title2", "Body2", "imageId2", "eventId2", "anmtId2") ;
        announcementList.add(announcementShouldExist);
        assertTrue(announcementList.contains(announcementShouldExist));
        Announcement announcementShouldNotExist = new Announcement("Title2", "Body2", "imageId2", "eventId2", "anmtId2");
        assertFalse(announcementList.contains(announcementShouldNotExist));
    }

    @Test
    public void testRemoveAnnouncement() {
        ArrayList<Announcement> announcementList = mockAnnouncementList();
        Announcement announcement = new Announcement("Title2", "Body2", "imageId2", "eventId2", "anmtId2");
        announcementList.add(announcement);
        assertTrue(announcementList.contains(announcement));
        announcementList.remove(announcement);
        assertFalse(announcementList.contains(announcement));
    }

    @Test
    public void testCountAnnouncements() {
        ArrayList<Announcement> announcementList = mockAnnouncementList();
        assertEquals(1, announcementList.size());
        Announcement announcement = new Announcement("Title2", "Body2", "imageId2", "eventId2", "anmtId2");
        announcementList.add(announcement);
        assertEquals(2, announcementList.size());
    }
}
