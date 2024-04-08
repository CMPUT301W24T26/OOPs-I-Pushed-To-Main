const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.androidPushNotification = functions.firestore.document("events/{eventId}/announcements/{anmtId}").onCreate(
    (snapshot, context) => {
        admin.messaging().sendToTopic(
            snapshot.data().eventId,  // Topic to send to (eventId, attendees that check-in to events will be subscribed to this topic)
            {
                notification: {
                    title: snapshot.data().title,
                    body: snapshot.data().body
                }
            }
        );
    }
);

exports.tenMultCheckInNotification = functions.firestore.document("events/{eventId}")
    .onUpdate((change, context) => {
        const eventId = context.params.eventId; // Retrieving eventId from context
        const after = change.after.data();
        if (after && after.checkIns && after.checkIns > 0 && after.checkIns % 10 == 0) {
            const organizerTopic = `${eventId}-ORGANIZER`;
            admin.messaging().sendToTopic(
                organizerTopic,  // Topic to send to (eventId concatenated with "-ORGANIZER" suffix)
                {
                    notification: {
                        title: "Milestone reached!",
                        body: `Congratulations, your event ${after.title} has reached ${after.checkIns} check-ins!`
                    }
                }
            );
        }
    }
);