const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.androidPushNotification = functions.firestore.document("announcements/{anmtId}").onCreate(
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