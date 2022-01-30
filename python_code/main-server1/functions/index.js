const functions = require('firebase-functions');

const express = require('express')
const app = express()
const port = 3000

var admin = require("firebase-admin");

var serviceAccount = require("./firebase.json");
const { user } = require('firebase-functions/lib/providers/auth');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://project-mod8.firebaseio.com"
});


app.get('/', (req, res) => {
  res.send('Hello World!')
})

app.get('/users', (req, res) => {
    ref = admin.database().ref("Users").once('value', (snap) => {
        // res.send(snap.val())
        result = [] 
        snap.forEach((childSnapshot) => {
            var childKey = childSnapshot.key;
            var childData = childSnapshot.val();
            result.push(childKey)
          });
        res.send(result)
    })

})

app.get('/cancelReserve/:userid/', (req, res) => {
    ref = admin.database().ref("Rooms").once('value', (snap) => {
        // res.send(snap.val())
        var result = [] 
        var resultTime = "0";
        snap.forEach((childSnapshot) => {
            var childKey = childSnapshot.key;
            var childData = childSnapshot;
            var userid = req.params["userid"]
            var roomid = childKey
            var commuteTime = parseInt(req.params["commute"])
            if (true) {
                var roomData = childData;
                var capacity = 0;
                var reserveListData;
                var currentUserData;
                roomData.forEach((roomChildSnapshot) => {
                    var roomChildKey = roomChildSnapshot.key;
                    var roomchildData = roomChildSnapshot;
                    // console.log(roomChildKey)
                    if (roomChildKey == "reservedList") {
                        console.log("found reserved list")
                        reserveListData = roomchildData
                    }
                })
                blackList = []
                reserveListData.forEach((reserveListDataSnapShot) => {
                    var reserveChildKey = reserveListDataSnapShot.key;
                    var reservechildData = reserveListDataSnapShot;
                    let ts = Date.now()
                    if (reserveChildKey == "Default") {
                        
                    } else {
                        if (ts > parseInt(reservechildData.val()) || userid == reserveChildKey){
                            blackList.push(reserveChildKey)
                        } 
                    }
                })
                
                blackList.forEach(blackEle => {
                    if (blackEle == userid) {
                        resultTime = "1"
                    }

                    admin.database().ref("Rooms").child(roomid).child("reservedList").child(blackEle).remove()
                });
                console.log(blackList)
                console.log(reserveListData.val())
            }
          });
        res.send(String(resultTime))
    })

})

app.get('/reserve/:userid/:roomid/:commute', (req, res) => {
    ref = admin.database().ref("Rooms").once('value', (snap) => {
        // res.send(snap.val())
        var result = [] 
        var resultTime = 0;
        snap.forEach((childSnapshot) => {
            var childKey = childSnapshot.key;
            var childData = childSnapshot;
            var roomid = req.params["roomid"]
            var userid = req.params["userid"]
            var commuteTime = parseInt(req.params["commute"])
            if (childKey == req.params["roomid"]) {
                var roomData = childData;
                var capacity = 0;
                var reserveListData;
                var currentUserData;
                roomData.forEach((roomChildSnapshot) => {
                    var roomChildKey = roomChildSnapshot.key;
                    var roomchildData = roomChildSnapshot;
                    // console.log(roomChildKey)
                    if (roomChildKey == "capacity") {
                        console.log("found capacity")
                        capacity = parseInt(roomchildData.val())
                    }
                    if (roomChildKey == "reservedList") {
                        console.log("found reserved list")
                        reserveListData = roomchildData
                    }
                    if (roomChildKey == "currentUser") {
                        console.log("found currentUserList")
                        currentUserData = roomchildData
                    }
                })
                blackList = []
                reserveListData.forEach((reserveListDataSnapShot) => {
                    var reserveChildKey = reserveListDataSnapShot.key;
                    var reservechildData = reserveListDataSnapShot;
                    let ts = new Date().getTime()
                    if (reserveChildKey == "Default") {
                        
                    } else if (ts > parseInt(reservechildData.val())){
                        blackList.push(reserveChildKey)
                    }
                })

                var availableSlot = capacity - currentUserData.numChildren() - reserveListData.numChildren() + 2 + blackList.length
                
                if (availableSlot > 0) {
                    let ts1 = new Date()
                    console.log(ts1)
                    let ts2 = parseInt(ts1.getTime())
                    ts2 += commuteTime*60*1000;
                    let ts = new Date(ts2)
                    // console.log(ts.getDate() +"/" + ts.getMonth() + "/" + ts.getFullYear() + " " + ts.getHours() + ":" + ts.getMinutes() + ":" + ts.getSeconds)
                    console.log(userid)
                    console.log(ts)
                    admin.database().ref("Rooms").child(roomid).child("reservedList").child(userid).set(String(ts.getTime()))
                    resultTime = ts.getTime()
                }
                
                blackList.forEach(blackEle => {
                    if (userid != blackEle)
                        admin.database().ref("Rooms").child(roomid).child("reservedList").child(blackEle).remove()
                });
                console.log(blackList)
                console.log(availableSlot)
                console.log(capacity)
                console.log(currentUserData.val())
                console.log(reserveListData.val())
            }
          });
        res.send(String(resultTime))
    })

})

app.get('/getAvailability', (req, res) => {
    ref = admin.database().ref("Rooms").once('value', (snap) => {
        // res.send(snap.val())
        var result = {'list':[]} 
        var resultTime = 0;
        snap.forEach((childSnapshot) => {
            var childKey = childSnapshot.key;
            var childData = childSnapshot;
            var commuteTime = parseInt(req.params["commute"])
            if (true) {
                var roomData = childData;
                var capacity = 0;
                var reserveListData;
                var currentUserData;
                roomData.forEach((roomChildSnapshot) => {
                    var roomChildKey = roomChildSnapshot.key;
                    var roomchildData = roomChildSnapshot;
                    // console.log(roomChildKey)
                    if (roomChildKey == "capacity") {
                        console.log("found capacity")
                        capacity = parseInt(roomchildData.val())
                    }
                    if (roomChildKey == "reservedList") {
                        console.log("found reserved list")
                        reserveListData = roomchildData
                    }
                    if (roomChildKey == "currentUser") {
                        console.log("found currentUserList")
                        currentUserData = roomchildData
                    }
                })
                blackList = []
                reserveListData.forEach((reserveListDataSnapShot) => {
                    var reserveChildKey = reserveListDataSnapShot.key;
                    var reservechildData = reserveListDataSnapShot;
                    let ts = new Date().getTime()
                    if (reserveChildKey == "Default") {
                        
                    } else if (ts > parseInt(reservechildData.val())){
                        blackList.push(reserveChildKey)
                    }
                })

                var availableSlot = capacity - currentUserData.numChildren() - reserveListData.numChildren() + 2 + blackList.length
                
                result['list'].push({'room_id' : childKey , 'slots': availableSlot})
                
                blackList.forEach(blackEle => {
                    if (userid != blackEle)
                        admin.database().ref("Rooms").child(roomid).child("reservedList").child(blackEle).remove()
                });
                console.log(blackList)
                console.log(availableSlot)
                console.log(capacity)
                console.log(currentUserData.val())
                console.log(reserveListData.val())
            }
          });
        res.send(result)
    })

})


app.get('/isReserve/:userid', (req, res) => {
    ref = admin.database().ref("Rooms").once('value', (snap) => {
        // res.send(snap.val())
        var result = [] 
        var resultTime = "0";
        snap.forEach((childSnapshot) => {
            var childKey = childSnapshot.key;
            var childData = childSnapshot;
            var roomid = childKey
            var userid = req.params["userid"]
            var commuteTime = parseInt(req.params["commute"])
            if (true) {
                var roomData = childData;
                var capacity = 0;
                var reserveListData;
                var currentUserData;
                roomData.forEach((roomChildSnapshot) => {
                    var roomChildKey = roomChildSnapshot.key;
                    var roomchildData = roomChildSnapshot;
                    // console.log(roomChildKey)
                    if (roomChildKey == "reservedList") {
                        console.log("found reserved list")
                        reserveListData = roomchildData
                    }
                })
                blackList = []
                reserveListData.forEach((reserveListDataSnapShot) => {
                    var reserveChildKey = reserveListDataSnapShot.key;
                    var reservechildData = reserveListDataSnapShot;
                    let ts = new Date().getTime()
                    if (reserveChildKey == "Default") {
                        
                    } else {
                        if (ts > parseInt(reservechildData.val())){
                            blackList.push(reserveChildKey)
                        } else if (reserveChildKey == userid) {
                            resultTime = childKey + "/" + String(parseInt(reservechildData.val()))
                        } 
                    }
                })
                
                blackList.forEach(blackEle => {
                    admin.database().ref("Rooms").child(roomid).child("reservedList").child(blackEle).remove()
                });
                console.log(blackList)
                console.log(reserveListData.val())
            }
          });
        res.send(String(resultTime))
    })

})

app.get('/login/:userid', (req, res) => {
    ref = admin.database().ref("Users").once('value', (snap) => {
        // res.send(snap.val())
        result = "None"
        snap.forEach((childSnapshot) => {
            var childKey = childSnapshot.key;
            var childData = childSnapshot.val();
            if (childKey == req.params['userid']) {
                result = childData['currentRoom']
            }
          });
        res.send(result)
    })

})

exports.app = functions.https.onRequest(app);
