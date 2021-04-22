const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite(event => {
    const user_id = event.params.user_id;
    const notification_id = event.params.notification_id;

    console.log('The User ID is :' , user_id);

});