const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.liveUrlChange = functions.database.ref('ProductionDB/Stock/{barcode}/6').onWrite((change,context) => {
    // Exit if data is deleted.
    //if (!change.after.exists()) return null;

    // Grab the current value of what was written to the Realtime Database.
    const value = change.after.val();

    /*if("send notification"!==value[4]){
        return;
    }
    value.ref.remove();*/

    /*if(){
        return null;
    }*/
    // Build the messaging notification, sending to the 'all' topic.

    var message = {

        "data": {
            "message": 'Database update',
            "title": 'Please update prices on device'
        },
    };


    // Get a database reference to our blog
    var db = admin.database()
    var upvotesRef = db.ref("ProductionDB/notificationHelper/");
    upvotesRef.transaction(function (current_value) {
      return (current_value || 0) + 1;
    },
    (error, committed, snapshot) => {
            if(committed) {
              // Send the message.
                  return admin.messaging().sendToTopic('update',message)
                      .then((message) => console.log('Successfully sent message:', message)
                      )
                      .catch((error) => {
                          console.error('Error sending message:', error);
                      });
            }
          });




});


//change stock count on every item sold. use info in reports to do so
exports.decrementStockItemCount = functions.database.ref('ProductionDB/Reports/{key}').onCreate((snapshot,context) =>{
    console.log('Decrement: In onCreate')
    snapshot.forEach(function(childSnapshot) {
                            console.log('Decrement: In forEach')
                           // childData will be the actual contents of the child
                           var childData = childSnapshot.val();
                           console.log('Decrement:', childData[0])
                      });
});
