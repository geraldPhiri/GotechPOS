const functions = require('firebase-functions');
const admin = require('firebase-admin');

const lodash = require('lodash.toarray');

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
exports.decrementStockItemCount= functions.database.ref('ProductionDB/Reports/{key}').onCreate((snapshot,context) =>{
    console.log('Decrement: In onCreate');
	/*var valu=snapshot.val;*/
    return admin.database().ref('ProductionDB/Reports/' + snapshot.key).once('value').then((snap)=>{
		snap.forEach(function(child) {
            const childKey = child.key;  // <- here you get the key of each child of the '/account/' + userId node

            const childVal = child.val; // <- and here you get the values of these children as JavaScript objects

            //const ind=_.toArray(childVal);
            var db = admin.database()
            console.log("above forEach");
            child.forEach(function(c){
                if(c.key==="5"){
                        console.log("almost in transaction");
                        var upvotesRef = db.ref("ProductionDB/Stock/"+c.val()+"/1");
                        upvotesRef.transaction(function (current_value) {
                          console.log("In transaction");

                          console.log("count: "+c.val());
                          var ans=parseFloat(current_value)-1;
                          return (ans.toString() || "0");
                        },
                        (error, committed, snapshot) => {
                                if(committed) {
                                  // Send the message.
                                  console.log("successful decrement");
                                }
                              });
                }

            })
			
        });
		return 1;
		}
    ).catch((error) => {
                          console.error('Error sending message:', error);
                      });
	


});
